/**
 * Cross-Tab Authentication Coordinator
 *
 * Coordinates authentication state and token refresh mutex across multiple browser tabs.
 *
 * Guarantees:
 * 1. Zero access token persistence in Web Storage or BroadcastChannel (in-memory only).
 * 2. Strict mutual exclusion for /auth/refresh-token requests to prevent concurrent
 *    refresh-token reuse detection on the backend.
 * 3. Immediate cross-tab propagation of logout and session expiration.
 */

export const AUTH_SIGNALS = {
  REFRESH_START: 'AUTH_REFRESH_START',
  REFRESH_SUCCESS: 'AUTH_REFRESH_SUCCESS',
  REFRESH_FAILURE: 'AUTH_REFRESH_FAILURE',
  LOGOUT: 'AUTH_LOGOUT',
  LOGIN: 'AUTH_LOGIN'
}

export const CHANNEL_NAME = 'japanese_learning_auth_channel'
export const SYNC_STORAGE_KEY = '_auth_sync_signal_'
export const LOCK_STORAGE_KEY = '_auth_refresh_lock_'

export const DEFAULT_LOCK_TTL = 5000 // 5 seconds
export const DEFAULT_ACQUIRE_TIMEOUT = 8000 // 8 seconds
export const POLL_INTERVAL = 50 // ms

export function generateTabId() {
  return 'tab_' + Math.random().toString(36).substring(2, 9) + '_' + Date.now()
}

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

export class AuthCoordinator {
  constructor(options = {}) {
    this.tabId = options.tabId || generateTabId()
    this.channelName = options.channelName || CHANNEL_NAME
    this.syncStorageKey = options.syncStorageKey || SYNC_STORAGE_KEY
    this.lockStorageKey = options.lockStorageKey || LOCK_STORAGE_KEY
    this.listeners = new Set()
    this.seenMessageIds = new Set()
    this.channel = null
    this.storageListener = null

    this._init()
  }

  _init() {
    if (typeof BroadcastChannel !== 'undefined') {
      try {
        this.channel = new BroadcastChannel(this.channelName)
        this.channel.onmessage = (event) => {
          this._handleIncomingMessage(event?.data)
        }
      } catch {
        this.channel = null
      }
    }

    if (typeof window !== 'undefined' && typeof window.addEventListener === 'function') {
      this.storageListener = (event) => {
        if (event.key === this.syncStorageKey && event.newValue) {
          try {
            const parsed = JSON.parse(event.newValue)
            this._handleIncomingMessage(parsed)
          } catch {}
        }
      }
      window.addEventListener('storage', this.storageListener)
    }
  }

  _handleIncomingMessage(message) {
    if (!message || typeof message !== 'object') return
    if (message.tabId === this.tabId) return // Ignore self-messages

    if (message.id) {
      if (this.seenMessageIds.has(message.id)) return
      this.seenMessageIds.add(message.id)
      if (this.seenMessageIds.size > 200) {
        const first = this.seenMessageIds.values().next().value
        this.seenMessageIds.delete(first)
      }
    }

    this.listeners.forEach((callback) => {
      try {
        callback(message)
      } catch (e) {
        console.error('[AuthCoordinator] Listener error:', e)
      }
    })
  }

  /**
   * Broadcast a session signal across tabs.
   * STRICT SECURITY: Access tokens and credentials must NEVER be included!
   */
  sendSignal(type, payload = {}) {
    if (payload) {
      const forbiddenKeys = ['accessToken', 'refreshToken', 'token', 'access_token', 'refresh_token', 'password']
      for (const key of forbiddenKeys) {
        if (key in payload) {
          throw new Error(`Security Violation: Token or sensitive field "${key}" must NEVER be broadcast or persisted across tabs!`)
        }
      }
    }

    const messageId = `${this.tabId}_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`
    const message = {
      id: messageId,
      type,
      tabId: this.tabId,
      timestamp: Date.now(),
      ...payload
    }

    this.seenMessageIds.add(messageId)

    // 1. Post to BroadcastChannel
    if (this.channel) {
      try {
        this.channel.postMessage(message)
      } catch {}
    }

    // 2. Fallback via localStorage storage event
    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        window.localStorage.setItem(this.syncStorageKey, JSON.stringify(message))
        setTimeout(() => {
          try {
            if (window.localStorage.getItem(this.syncStorageKey) === JSON.stringify(message)) {
              window.localStorage.removeItem(this.syncStorageKey)
            }
          } catch {}
        }, 100)
      } catch {}
    }
  }

  onSignal(callback) {
    this.listeners.add(callback)
    return () => {
      this.listeners.delete(callback)
    }
  }

  getLockRecord() {
    if (typeof window === 'undefined' || !window.localStorage) return null
    try {
      const raw = window.localStorage.getItem(this.lockStorageKey)
      if (!raw) return null
      const parsed = JSON.parse(raw)
      if (!parsed || typeof parsed !== 'object') return null
      return parsed
    } catch {
      return null
    }
  }

  setLockRecord(lock) {
    if (typeof window === 'undefined' || !window.localStorage) return
    try {
      window.localStorage.setItem(this.lockStorageKey, JSON.stringify(lock))
    } catch {}
  }

  removeLockRecord() {
    if (typeof window === 'undefined' || !window.localStorage) return
    try {
      window.localStorage.removeItem(this.lockStorageKey)
    } catch {}
  }

  isLockHeldByOtherTab() {
    const lock = this.getLockRecord()
    if (!lock) return false
    const now = Date.now()
    if (now >= lock.expiresAt) return false
    return lock.tabId !== this.tabId
  }

  /**
   * Acquire refresh lock with timeout, stale lock recovery, and mutual exclusion.
   */
  async acquireRefreshLock(options = {}) {
    const timeout = options.timeout ?? DEFAULT_ACQUIRE_TIMEOUT
    const ttl = options.ttl ?? DEFAULT_LOCK_TTL
    const startTime = Date.now()

    let signalUnsubscribe = null
    let wakeUpWaiter = null

    const abortPromise = new Promise((_, reject) => {
      signalUnsubscribe = this.onSignal((signal) => {
        if (signal.type === AUTH_SIGNALS.REFRESH_FAILURE) {
          reject(new Error(signal.reason || 'Authentication refresh failed in another tab'))
        } else if (signal.type === AUTH_SIGNALS.LOGOUT) {
          reject(new Error('User logged out in another tab'))
        } else if (signal.type === AUTH_SIGNALS.REFRESH_SUCCESS) {
          if (wakeUpWaiter) {
            wakeUpWaiter()
            wakeUpWaiter = null
          }
        }
      })
    })

    const acquireLoop = async () => {
      while (Date.now() - startTime < timeout) {
        const rawLock = this.getLockRecord()
        const now = Date.now()

        // Lock is free, expired, or re-entrant for current tab
        if (!rawLock || now >= rawLock.expiresAt || rawLock.tabId === this.tabId) {
          const lockData = {
            tabId: this.tabId,
            timestamp: now,
            expiresAt: now + ttl
          }
          this.setLockRecord(lockData)

          // Verify if our tab won the lock
          const confirmed = this.getLockRecord()
          if (confirmed && confirmed.tabId === this.tabId) {
            this.sendSignal(AUTH_SIGNALS.REFRESH_START)
            return {
              tabId: this.tabId,
              release: (success = true, meta = {}) => {
                this.releaseRefreshLock(success, meta)
              }
            }
          }
        }

        // Wait until wakeUpWaiter is called (e.g. by REFRESH_SUCCESS) or poll interval
        await new Promise((resolve) => {
          wakeUpWaiter = resolve
          setTimeout(resolve, POLL_INTERVAL)
        })
      }

      throw new Error('Acquire refresh lock timed out')
    }

    try {
      return await Promise.race([acquireLoop(), abortPromise])
    } finally {
      if (signalUnsubscribe) {
        signalUnsubscribe()
      }
      wakeUpWaiter = null
    }
  }

  /**
   * Release the refresh lock.
   */
  releaseRefreshLock(success = true, meta = {}) {
    const currentLock = this.getLockRecord()
    if (currentLock && currentLock.tabId === this.tabId) {
      this.removeLockRecord()
    }

    if (success) {
      this.sendSignal(AUTH_SIGNALS.REFRESH_SUCCESS, meta)
    } else {
      this.sendSignal(AUTH_SIGNALS.REFRESH_FAILURE, meta)
    }
  }

  /**
   * Execute a refresh function with mutex locking and cross-tab signal notification.
   */
  async runWithRefreshLock(refreshFn, options = {}) {
    const lock = await this.acquireRefreshLock(options)
    try {
      const result = await refreshFn()
      lock.release(true)
      return result
    } catch (err) {
      lock.release(false, { reason: err?.message || 'Refresh failed' })
      throw err
    }
  }

  notifyLogout() {
    this.sendSignal(AUTH_SIGNALS.LOGOUT)
  }

  notifyLogin() {
    this.sendSignal(AUTH_SIGNALS.LOGIN)
  }

  notifyRefreshFailure(reason = 'Refresh failed') {
    this.sendSignal(AUTH_SIGNALS.REFRESH_FAILURE, { reason })
  }

  _resetCoordinatorState() {
    this.removeLockRecord()
    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        window.localStorage.removeItem(this.syncStorageKey)
        window.localStorage.removeItem(this.lockStorageKey)
      } catch {}
    }
    this.seenMessageIds.clear()
    this.tabId = generateTabId()
  }

  destroy() {
    if (this.channel) {
      try {
        this.channel.close()
      } catch {}
      this.channel = null
    }
    if (this.storageListener && typeof window !== 'undefined') {
      window.removeEventListener('storage', this.storageListener)
      this.storageListener = null
    }
    this.listeners.clear()
    this.seenMessageIds.clear()
  }
}

export const defaultCoordinator = new AuthCoordinator()

export const acquireRefreshLock = (...args) => defaultCoordinator.acquireRefreshLock(...args)
export const releaseRefreshLock = (...args) => defaultCoordinator.releaseRefreshLock(...args)
export const runWithRefreshLock = (...args) => defaultCoordinator.runWithRefreshLock(...args)
export const sendSignal = (...args) => defaultCoordinator.sendSignal(...args)
export const onSignal = (...args) => defaultCoordinator.onSignal(...args)
export const notifyLogout = (...args) => defaultCoordinator.notifyLogout(...args)
export const notifyLogin = (...args) => defaultCoordinator.notifyLogin(...args)
export const notifyRefreshFailure = (...args) => defaultCoordinator.notifyRefreshFailure(...args)
export const isLockHeldByOtherTab = (...args) => defaultCoordinator.isLockHeldByOtherTab(...args)
export const getLockRecord = (...args) => defaultCoordinator.getLockRecord(...args)
export const setLockRecord = (...args) => defaultCoordinator.setLockRecord(...args)
export const removeLockRecord = (...args) => defaultCoordinator.removeLockRecord(...args)
export const _resetCoordinatorState = (...args) => defaultCoordinator._resetCoordinatorState(...args)
export const getTabId = () => defaultCoordinator.tabId
export const setTabId = (id) => { defaultCoordinator.tabId = id }

export default defaultCoordinator

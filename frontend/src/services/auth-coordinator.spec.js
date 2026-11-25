import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import defaultCoordinator, { AuthCoordinator, AUTH_SIGNALS, LOCK_STORAGE_KEY, SYNC_STORAGE_KEY } from './auth-coordinator'

describe('auth-coordinator cross-tab coordination and security', () => {
  beforeEach(() => {
    defaultCoordinator._resetCoordinatorState()
    vi.restoreAllMocks()
  })

  afterEach(() => {
    defaultCoordinator._resetCoordinatorState()
  })

  describe('Security Guarantees', () => {
    it('throws security error if attempting to broadcast accessToken in payload', () => {
      expect(() => {
        defaultCoordinator.sendSignal(AUTH_SIGNALS.REFRESH_SUCCESS, {
          accessToken: 'secret-token-123'
        })
      }).toThrow(/Security Violation/)
    })

    it('throws security error if attempting to broadcast refreshToken in payload', () => {
      expect(() => {
        defaultCoordinator.sendSignal(AUTH_SIGNALS.REFRESH_SUCCESS, {
          refreshToken: 'secret-refresh-cookie'
        })
      }).toThrow(/Security Violation/)
    })

    it('never stores tokens or passwords in localStorage lock records', async () => {
      const lock = await defaultCoordinator.acquireRefreshLock({ timeout: 500, ttl: 2000, jitter: false })

      const raw = window.localStorage.getItem(LOCK_STORAGE_KEY)
      expect(raw).toBeTruthy()
      const parsed = JSON.parse(raw)

      expect(parsed.tabId).toBe(defaultCoordinator.tabId)
      expect(parsed.accessToken).toBeUndefined()
      expect(parsed.refreshToken).toBeUndefined()
      expect(parsed.token).toBeUndefined()

      lock.release(true)
    })
  })

  describe('Signal Propagation & Filtering across tabs', () => {
    let tabA
    let tabB

    beforeEach(() => {
      tabA = new AuthCoordinator({ tabId: 'tab-A' })
      tabB = new AuthCoordinator({ tabId: 'tab-B' })
    })

    afterEach(() => {
      tabA.destroy()
      tabB.destroy()
    })

    it('delivers signals from Tab A to Tab B, while Tab A ignores its own signal', async () => {
      const listenerA = vi.fn()
      const listenerB = vi.fn()

      tabA.onSignal(listenerA)
      tabB.onSignal(listenerB)

      tabA.sendSignal(AUTH_SIGNALS.LOGIN)

      // Wait a tick for BroadcastChannel message dispatch
      await new Promise((r) => setTimeout(r, 20))

      expect(listenerA).not.toHaveBeenCalled()
      expect(listenerB).toHaveBeenCalledTimes(1)
      expect(listenerB.mock.calls[0][0].type).toBe(AUTH_SIGNALS.LOGIN)
      expect(listenerB.mock.calls[0][0].tabId).toBe('tab-A')
    })

    it('deduplicates duplicate messages with the same message id', () => {
      const listener = vi.fn()
      tabB.onSignal(listener)

      const payload = JSON.stringify({
        id: 'dup-msg-123',
        type: AUTH_SIGNALS.REFRESH_SUCCESS,
        tabId: 'tab-A',
        timestamp: Date.now()
      })

      // Simulate BroadcastChannel and storage event arriving for the same message
      window.dispatchEvent(new StorageEvent('storage', { key: SYNC_STORAGE_KEY, newValue: payload }))
      window.dispatchEvent(new StorageEvent('storage', { key: SYNC_STORAGE_KEY, newValue: payload }))

      expect(listener).toHaveBeenCalledTimes(1)
    })
  })

  describe('Mutex Locking & Mutual Exclusion across tabs', () => {
    let tab1
    let tab2

    beforeEach(() => {
      tab1 = new AuthCoordinator({ tabId: 'tab-1' })
      tab2 = new AuthCoordinator({ tabId: 'tab-2' })
    })

    afterEach(() => {
      tab1.destroy()
      tab2.destroy()
    })

    it('allows a tab to acquire lock and then releases lock cleanly', async () => {
      const lock = await tab1.acquireRefreshLock({ timeout: 500, ttl: 2000, jitter: false })

      expect(lock.tabId).toBe('tab-1')
      expect(tab1.isLockHeldByOtherTab()).toBe(false)
      expect(tab2.isLockHeldByOtherTab()).toBe(true)

      lock.release(true)
      expect(tab1.getLockRecord()).toBeNull()
      expect(tab2.isLockHeldByOtherTab()).toBe(false)
    })

    it('prevents concurrent refresh: Tab 2 waits until Tab 1 releases lock', async () => {
      const lock1 = await tab1.acquireRefreshLock({ timeout: 1000, ttl: 3000, jitter: false })

      let lock2Acquired = false
      const lock2Promise = tab2.acquireRefreshLock({ timeout: 1000, ttl: 3000, jitter: false }).then((lock) => {
        lock2Acquired = true
        return lock
      })

      // tab-2 is currently waiting
      await new Promise((r) => setTimeout(r, 50))
      expect(lock2Acquired).toBe(false)

      // Tab 1 releases lock
      lock1.release(true)

      // Tab 2 now acquires lock
      const lock2 = await lock2Promise
      expect(lock2Acquired).toBe(true)
      expect(lock2.tabId).toBe('tab-2')

      lock2.release(true)
    })

    it('aborts waiting tab if active refreshing tab reports REFRESH_FAILURE', async () => {
      const lock1 = await tab1.acquireRefreshLock({ timeout: 1000, ttl: 3000, jitter: false })

      const waitingPromise = tab2.acquireRefreshLock({ timeout: 1000, ttl: 3000, jitter: false })

      // Active tab fails refresh
      lock1.release(false, { reason: 'Refresh token expired or revoked' })

      // Waiting tab should immediately reject without attempting refresh
      await expect(waitingPromise).rejects.toThrow('Refresh token expired or revoked')
    })

    it('aborts waiting tab if active tab reports LOGOUT', async () => {
      await tab1.acquireRefreshLock({ timeout: 1000, ttl: 3000, jitter: false })

      const waitingPromise = tab2.acquireRefreshLock({ timeout: 1000, ttl: 3000, jitter: false })

      // Active tab logs out
      tab1.notifyLogout()

      // Waiting tab should immediately reject
      await expect(waitingPromise).rejects.toThrow('User logged out in another tab')
    })

    it('recovers from dead lock after TTL expires', async () => {
      // Simulate crashed tab leaving stale lock
      tab1.setLockRecord({
        tabId: 'dead-tab',
        timestamp: Date.now() - 500,
        expiresAt: Date.now() - 100 // already expired
      })

      const lock = await tab2.acquireRefreshLock({ timeout: 500, ttl: 2000, jitter: false })
      expect(lock.tabId).toBe('tab-2')
      lock.release(true)
    })
  })

  describe('runWithRefreshLock helper', () => {
    it('executes task within lock and releases lock on success', async () => {
      const fn = vi.fn().mockResolvedValue('refresh-result')

      const res = await defaultCoordinator.runWithRefreshLock(fn, { jitter: false })

      expect(res).toBe('refresh-result')
      expect(fn).toHaveBeenCalledTimes(1)
      expect(defaultCoordinator.getLockRecord()).toBeNull()
    })

    it('releases lock with failure when task throws', async () => {
      const fn = vi.fn().mockRejectedValue(new Error('Network error on refresh'))

      await expect(defaultCoordinator.runWithRefreshLock(fn, { jitter: false })).rejects.toThrow('Network error on refresh')

      expect(defaultCoordinator.getLockRecord()).toBeNull()
    })
  })
})

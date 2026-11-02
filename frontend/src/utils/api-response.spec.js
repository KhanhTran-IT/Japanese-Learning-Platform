import { describe, it, expect } from 'vitest'
import {
  isApiSuccess,
  getApiResult,
  getHttpStatus,
  getApiErrorCode,
  getApiErrorPayload
} from './api-response'

describe('api-response utility', () => {
  it('isApiSuccess identifies successful response with code 1000', () => {
    expect(isApiSuccess({ data: { code: 1000, result: 'ok' } })).toBe(true)
    expect(isApiSuccess({ data: { code: 1001, result: null } })).toBe(false)
    expect(isApiSuccess(null)).toBe(false)
    expect(isApiSuccess({})).toBe(false)
  })

  it('getApiResult extracts result on success or returns fallback', () => {
    const successRes = { data: { code: 1000, result: [1, 2, 3] } }
    expect(getApiResult(successRes)).toEqual([1, 2, 3])

    const errorRes = { data: { code: 1001, result: null } }
    expect(getApiResult(errorRes, [])).toEqual([])
    expect(getApiResult(null, 'default')).toBe('default')
  })

  it('getHttpStatus extracts HTTP status from error or response', () => {
    const errorWithResponse = { response: { status: 404 } }
    expect(getHttpStatus(errorWithResponse)).toBe(404)

    const directResponse = { status: 200 }
    expect(getHttpStatus(directResponse)).toBe(200)

    expect(getHttpStatus({})).toBeNull()
  })

  it('getApiErrorCode extracts business code from error response', () => {
    const errorWithCode = { response: { data: { code: 5002, message: 'Not found' } } }
    expect(getApiErrorCode(errorWithCode)).toBe(5002)

    expect(getApiErrorCode({})).toBeNull()
  })

  it('getApiErrorPayload extracts entire error payload', () => {
    const payload = { code: 5002, message: 'Not found' }
    const error = { response: { data: payload } }
    expect(getApiErrorPayload(error)).toEqual(payload)

    expect(getApiErrorPayload({})).toBeNull()
  })
})

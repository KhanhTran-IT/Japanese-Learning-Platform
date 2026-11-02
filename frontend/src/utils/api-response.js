/**
 * Shared API Response and Error Utilities
 * Provides standard helpers for inspecting Axios responses and errors
 * adhering to the backend ApiResponse contract: { code, message, result }
 */
import { getApiErrorMessage } from './api-error'

/**
 * Checks whether an Axios response indicates a successful business operation (code === 1000).
 * @param {any} response
 * @returns {boolean}
 */
export const isApiSuccess = (response) => {
  return Boolean(response && response.data && response.data.code === 1000)
}

/**
 * Safely extracts the result payload from an Axios response.
 * If the response is successful and has a result, returns it; otherwise returns fallback.
 * @param {any} response
 * @param {any} [fallback=null]
 * @returns {any}
 */
export const getApiResult = (response, fallback = null) => {
  if (isApiSuccess(response) && response.data.result !== undefined && response.data.result !== null) {
    return response.data.result
  }
  return fallback
}

/**
 * Safely extracts the HTTP status code from an Axios error or response.
 * @param {any} errorOrResponse
 * @returns {number|null}
 */
export const getHttpStatus = (errorOrResponse) => {
  return errorOrResponse?.response?.status ?? errorOrResponse?.status ?? null
}

/**
 * Safely extracts the backend application error code (e.g., 5002, 1002) from an Axios error.
 * @param {any} error
 * @returns {number|null}
 */
export const getApiErrorCode = (error) => {
  return error?.response?.data?.code ?? null
}

/**
 * Safely extracts the backend error response payload ({ code, message, result }).
 * @param {any} error
 * @returns {any}
 */
export const getApiErrorPayload = (error) => {
  return error?.response?.data ?? null
}

export { getApiErrorMessage }
export default {
  isApiSuccess,
  getApiResult,
  getHttpStatus,
  getApiErrorCode,
  getApiErrorPayload,
  getApiErrorMessage
}

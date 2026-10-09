/**
 * Extracts a user-facing error message from an API error.
 * Supports Axios errors, backend ApiResponse errors, network errors, and timeouts.
 * 
 * @param {Error} error The error object caught in a try/catch block
 * @param {string} fallbackMsg A default message to show if extraction fails
 * @returns {string} The extracted Vietnamese error message
 */
export const getApiErrorMessage = (error, fallbackMsg = 'Có lỗi xảy ra, vui lòng thử lại sau.') => {
  // Check if it's an Axios error
  if (error.isAxiosError) {
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      return 'Kết nối đến máy chủ bị quá hạn. Vui lòng kiểm tra mạng và thử lại.'
    }
    
    if (!error.response) {
      return 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng của bạn.'
    }

    // Backend returned a response (4xx, 5xx)
    const { data } = error.response
    
    // Handle our backend's standard ApiResponse structure: { code, message, result }
    if (data && data.message) {
      // If validation error mapped by backend, we might want to format it
      // but usually the backend provides a clear message in `data.message`
      return data.message
    }
    
    // Fallback for standard HTTP errors if backend didn't format it as ApiResponse
    if (error.response.status === 400) return 'Dữ liệu không hợp lệ.'
    if (error.response.status === 401) return 'Không có quyền truy cập hoặc phiên đăng nhập đã hết hạn.'
    if (error.response.status === 403) return 'Bạn không có quyền thực hiện thao tác này.'
    if (error.response.status === 404) return 'Không tìm thấy dữ liệu yêu cầu.'
    if (error.response.status >= 500) return 'Lỗi hệ thống máy chủ. Vui lòng thử lại sau.'
  }

  // Native JS Error or unknown format
  return error.message || fallbackMsg
}

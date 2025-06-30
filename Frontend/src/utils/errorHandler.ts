export const handleApiError = (error: any, customMessage = '') => {
  console.error('Error de API:', error)
  
  const status = error.response?.status
  const message = error.response?.data?.message || error.message || 'Error desconocido'
  
  // Manejar errores comunes
  if (status === 401) {
    alert('Tu sesión ha expirado. Serás redirigido al login.')
    if (typeof window !== 'undefined') {
      localStorage.removeItem('access_token')
      window.location.href = '/landing'
    }
    return
  }
  
  if (status === 403) {
    alert(`No tienes permisos para acceder a este recurso. ${customMessage || ''}`.trim())
    return
  }
  
  // Error genérico
  alert(customMessage || `Error: ${message}`)
}

export const logApiCall = (method: string, url: string, status?: number | null, data?: any) => {
  const icon = status && status >= 200 && status < 300 ? '✅' : '❌'
  console.log(`${icon} ${method.toUpperCase()} ${url}${status ? ` (${status})` : ''}`)
  if (data && (Array.isArray(data) ? data.length > 0 : Object.keys(data).length > 0)) {
    console.log('📊 Datos:', Array.isArray(data) ? `${data.length} elementos` : data)
  }
}
/**
 * Utilidad centralizada para manejo de tokens de autenticación
 */

export const tokenUtils = {
  // Obtener token del localStorage
  getToken(): string | null {
    if (typeof window === 'undefined') return null
    return localStorage.getItem('access_token')
  },

  // Guardar token en localStorage
  setToken(token: string): void {
    if (typeof window === 'undefined') return
    localStorage.setItem('access_token', token)
  },

  // Eliminar token del localStorage
  removeToken(): void {
    if (typeof window === 'undefined') return
    localStorage.removeItem('access_token')
  },

  // Verificar si hay token válido
  hasValidToken(): boolean {
    const token = this.getToken()
    return !!token && token.length > 0
  },

  // Obtener preview del token para logs (primeros 20 caracteres)
  getTokenPreview(): string {
    const token = this.getToken()
    return token ? token.substring(0, 20) + '...' : 'null'
  },

  // Limpiar todos los datos de autenticación
  clearAuth(): void {
    if (typeof window === 'undefined') return
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    localStorage.removeItem('userData')
    localStorage.removeItem('user')
  }
}

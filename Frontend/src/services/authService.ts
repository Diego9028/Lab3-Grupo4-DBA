import API_ROUTES from '../api-routes'
import { tokenUtils } from '../utils/tokenUtils'
import type { AxiosInstance } from 'axios'
import type { Router } from 'vue-router'

export const handleLogout = async (apiClient: AxiosInstance, router: Router) => {
  try {
    if (typeof window === 'undefined') {
      console.warn('localStorage no está disponible en el servidor.')
      return
    }
    
    // Obtener token usando utilidad centralizada
    const accessToken = tokenUtils.getToken()

    if (accessToken) {
      // Intentar hacer logout en el servidor
      try {
        await apiClient.post(`${API_ROUTES.AUTH}/logout`)
        console.log('✅ Logout exitoso en el servidor')
      } catch (error) {
        console.warn('⚠️ Error en logout del servidor, pero continuando con limpieza local:', error)
      }
    }

    // Siempre limpiar datos locales, independientemente del resultado del servidor
    tokenUtils.clearAuth()
    
    console.log('✅ Sesión cerrada localmente')
    
    // Redirigir a la landing page
    router.push('/landing')
    
  } catch (error) {
    console.error('Error al cerrar sesión:', error)
    
    // Aunque haya error, limpiar datos locales
    tokenUtils.clearAuth()
    router.push('/landing')
  }
}
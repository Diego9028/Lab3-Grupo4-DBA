/**
 * Composable para la lógica común de autenticación en páginas
 * Simplifica el manejo de estado de autenticación
 */

import { onMounted } from 'vue'
import { tokenUtils } from '../src/utils/tokenUtils'

export const useAuthPage = () => {
  // Verificar autenticación al montar la página
  const checkAuth = () => {
    if (process.client) {
      if (!tokenUtils.hasValidToken()) {
        console.log('No valid token found, redirecting to landing')
        navigateTo('/landing', { replace: true })
        return false
      }
      return true
    }
    return true // No verificar en SSR
  }

  // Auto-verificar al montar
  onMounted(() => {
    checkAuth()
  })

  return {
    checkAuth,
    hasValidToken: () => tokenUtils.hasValidToken(),
    getTokenPreview: () => tokenUtils.getTokenPreview()
  }
}

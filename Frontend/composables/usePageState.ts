/**
 * Composable para manejo común de loading y errores
 * Reduce código redundante en páginas
 */

import { ref } from 'vue'

export const usePageState = () => {
  const loading = ref(false)
  const error = ref('')
  
  const setLoading = (state: boolean) => {
    loading.value = state
  }
  
  const setError = (message: string) => {
    error.value = message
    loading.value = false
  }
  
  const clearError = () => {
    error.value = ''
  }
  
  const withLoading = async <T>(operation: () => Promise<T>): Promise<T | null> => {
    try {
      setLoading(true)
      clearError()
      return await operation()
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || 
                          err.response?.data?.error || 
                          err.message || 
                          'Error inesperado'
      setError(errorMessage)
      console.error('Operation failed:', err)
      return null
    } finally {
      setLoading(false)
    }
  }
  
  return {
    loading: readonly(loading),
    error: readonly(error),
    setLoading,
    setError,
    clearError,
    withLoading
  }
}

import { defineNuxtPlugin } from '#app'
import axios from 'axios'

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()

  // Crear una instancia de Axios
  const $apiClient = axios.create({
    baseURL: `http://localhost:8090`,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // Interceptor para agregar el token JWT a cada solicitud
  $apiClient.interceptors.request.use((config) => {
    // Solo ejecutar en el cliente (no en SSR)
    if (process.client) {
      const access_token = localStorage.getItem('access_token')
      const publicRoutes = ['/auth/login', '/auth/registrar', '/auth/refresh', '/auth/logout']
      
      if (access_token && !publicRoutes.includes(config.url || '')) {
        config.headers.Authorization = `Bearer ${access_token}`
        console.log('[Axios] Token agregado a la solicitud:', config.url)
      } else {
        console.log('[Axios] Sin token o ruta pública:', config.url)
      }
    }
    return config
  })

  // Interceptor de respuesta para manejo de errores de autenticación
  $apiClient.interceptors.response.use(
    (response) => {
      console.log('[Axios] Respuesta exitosa:', response.config.url, response.status)
      return response
    },
    (error) => {
      console.log('[Axios] Error en solicitud:', error.config?.url, error.response?.status, error.response?.statusText)
      
      if (error.response?.status === 403) {
        console.log('[Axios] Error 403 - posible problema de autenticación o CORS')
      }
      
      return Promise.reject(error)
    }
  )

  // Hacer que la instancia esté disponible globalmente
  nuxtApp.provide('apiClient', $apiClient)
})
// https://nuxt.com/docs/api/configuration/nuxt-config
import { defineNuxtConfig } from 'nuxt/config'

export default defineNuxtConfig({
  css: ['bootstrap/dist/css/bootstrap.min.css',
    '@/src/assets/styles.css',
  ],
  compatibilityDate: '2024-11-01',
  pages: true,
  devtools: { enabled: true },
  ssr: true,
  // Configuración para transiciones de página suaves
  app: {
    pageTransition: { 
      name: 'page', 
      mode: 'out-in',
      appear: true 
    },
    layoutTransition: { 
      name: 'layout', 
      mode: 'out-in' 
    },
    head: {
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' }
      ]
    }
  },
  runtimeConfig: {
    public: {
      backendServer: process.env.BACKEND_SERVER,
      backendPort: process.env.BACKEND_PORT
    }
  },
  vite: {
    optimizeDeps: {
      include: ['leaflet']
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: '@use "sass:math";'
        }
      }
    },
    server: {
      // Sin proxy - comunicación directa con el backend
    }
  },
  nitro: {
    experimental: {
      wasm: true
    },
    // Ignorar rutas problemáticas
    routeRules: {
      '/.well-known/**': { 
        redirect: '/' 
      },
      // Optimización para la página de landing
      '/landing': { 
        ssr: false, // Deshabilitar SSR para evitar conflictos de hidratación
        prerender: false
      },
      // Deshabilitar SSR para la página principal también
      '/': {
        ssr: false, // Evitar mismatch entre server y client
        prerender: false
      },
      // Páginas de autenticación sin SSR
      '/login': {
        ssr: false,
        prerender: false
      },
      '/register': {
        ssr: false,
        prerender: false
      },
      // Páginas del dashboard sin SSR para evitar problemas de auth
      '/cliente': {
        ssr: false,
        prerender: false
      },
      '/desempeno': {
        ssr: false,
        prerender: false
      },
      // Otras páginas del dashboard
      '/clienteCobertura': { ssr: false },
      '/clientesEnZona': { ssr: false },
      '/clientesSinEmpresaCercana': { ssr: false },
      '/zonaDensa': { ssr: false },
      '/empresasTop': { ssr: false },
      '/pedidosMasLejanos': { ssr: false },
      '/pedidosMultiplesZonas': { ssr: false },
      '/puntoInteres': { ssr: false },
      '/cercanosEmpresa': { ssr: false },
      '/distanciaRecorrida': { ssr: false }
    }
  },
  router: {
    options: {
      strict: false,
      // Mejorar el manejo de redirecciones
      scrollBehaviorType: 'smooth'
    }
  }
})


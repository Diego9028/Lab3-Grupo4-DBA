export default defineNuxtPlugin(async () => {
  // Precargar Leaflet solo en el cliente
  if (process.client) {
    try {
      // Pre-cargar CSS de Leaflet
      const link = document.createElement('link')
      link.rel = 'stylesheet'
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
      link.integrity = 'sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY='
      link.crossOrigin = 'anonymous'
      document.head.appendChild(link)

      // Pre-cargar el módulo de Leaflet
      await import('leaflet')
      
      console.log('Leaflet plugin loaded successfully')
    } catch (error) {
      console.error('Error loading Leaflet plugin:', error)
    }
  }
})

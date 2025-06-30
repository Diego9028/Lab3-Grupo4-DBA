<template>
    <ClientOnly>
        <div class="map-container">
            <div class="map-header">
                <h5>📍 Selecciona tu ubicación</h5>
                <p>Haz clic en el mapa para seleccionar tu ubicación</p>
            </div>
            <div class="map-wrapper">
                <div v-show="loading" class="map-loading">
                    <div class="loading-spinner"></div>
                    <span>Cargando mapa...</span>
                </div>
                <div ref="mapContainer" class="map" v-show="!loading"></div>
            </div>
            <div v-if="selectedLocation" class="location-info animate-success">
                <div class="location-success">
                    <span class="success-icon">✅</span>
                    <span>Ubicación seleccionada: {{ selectedLocation.lat.toFixed(4) }}, {{ selectedLocation.lng.toFixed(4) }}</span>
                </div>
                <div class="location-help">
                    💡 Puedes arrastrar el marcador para ajustar la ubicación
                </div>
            </div>
            <div v-else class="location-warning">
                ⚠️ Selecciona una ubicación en el mapa
            </div>
            <div class="map-actions">
                <button 
                    type="button" 
                    class="action-btn primary" 
                    @click="getCurrentLocation" 
                    :disabled="loading"
                >
                    <span class="btn-icon">📍</span>
                    <span>Usar mi ubicación actual</span>
                </button>
                <button 
                    type="button" 
                    class="action-btn secondary" 
                    @click="searchLocation" 
                    :disabled="loading"
                >
                    <span class="btn-icon">🔍</span>
                    <span>Buscar dirección</span>
                </button>
                <button 
                    v-if="selectedLocation" 
                    type="button" 
                    class="action-btn tertiary" 
                    @click="adjustLocation"
                >
                    <span class="btn-icon">🎯</span>
                    <span>Ajustar ubicación</span>
                </button>
            </div>
        </div>
        <template #fallback>
            <div class="map-fallback">
                <div class="text-center p-4">
                    <div class="spinner-border text-primary mb-2" role="status">
                        <span class="visually-hidden">Cargando...</span>
                    </div>
                    <p>Cargando componente de mapa...</p>
                </div>
            </div>
        </template>
    </ClientOnly>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'

const props = defineProps({
    modelValue: {
        type: Object,
        default: () => ({ lat: null, lng: null })
    }
})

const emit = defineEmits(['update:modelValue'])

const mapContainer = ref(null)
const selectedLocation = ref(null)
const loading = ref(true)
let map = null
let marker = null
let L = null

const initializeMap = async () => {
    try {
        // Verificar que estamos en el cliente
        if (!process.client) {
            return
        }
        
        // Importar Leaflet dinámicamente
        L = (await import('leaflet')).default
        
        // Configurar los iconos de Leaflet
        delete L.Icon.Default.prototype._getIconUrl
        L.Icon.Default.mergeOptions({
            iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
            iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
        })

        // Esperar a que el DOM esté listo
        await nextTick()
        
        if (!mapContainer.value) {
            loading.value = false
            return
        }
        
        // Inicializar el mapa centrado en Ecuador (Quito)
        map = L.map(mapContainer.value, {
            center: [-0.1807, -78.4678],
            zoom: 12,
            zoomControl: true,
            attributionControl: true
        })

        // Agregar capa de mapa con configuración optimizada
        const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
            maxZoom: 18,
            tileSize: 256,
            zoomOffset: 0
        }).addTo(map)

        // Esperar a que las tiles se carguen
        tileLayer.on('load', () => {
            loading.value = false
            nextTick(() => {
                if (map) {
                    map.invalidateSize()
                }
            })
        })

        // Timeout de respaldo
        setTimeout(() => {
            loading.value = false
            nextTick(() => {
                if (map) {
                    map.invalidateSize()
                }
            })
        }, 1000)

        // Agregar evento de clic en el mapa
        map.on('click', (e) => {
            setLocation(e.latlng)
        })

        // Forzar el redimensionamiento del mapa
        setTimeout(() => {
            if (map) {
                map.invalidateSize()
            }
        }, 100)

        // Si ya hay una ubicación seleccionada, mostrarla
        if (props.modelValue?.lat && props.modelValue?.lng) {
            const location = { lat: props.modelValue.lat, lng: props.modelValue.lng }
            setLocation(location)
            map.setView([location.lat, location.lng], 15)
        }

        // Establecer loading = false inmediatamente después de crear el mapa
        loading.value = false
        
        // Forzar actualización del DOM
        await nextTick()
    } catch (error) {
        console.error('Error inicializando el mapa:', error)
        loading.value = false
    }
}

onMounted(async () => {
    // Solo ejecutar en el cliente
    if (!process.client) {
        return
    }
    
    // Timeout de seguridad para evitar loading infinito
    const timeoutId = setTimeout(() => {
        loading.value = false
    }, 10000)
    
    // Esperar un poco más para asegurar que el DOM esté completamente listo
    await nextTick()
    
    setTimeout(async () => {
        try {
            await initializeMap()
            clearTimeout(timeoutId)
        } catch (error) {
            console.error('Error during map initialization:', error)
            loading.value = false
            clearTimeout(timeoutId)
        }
    }, 100)
})

onUnmounted(() => {
    if (map) {
        map.remove()
        map = null
    }
})

const setLocation = (location) => {
    if (!map || !L) return
    
    selectedLocation.value = location
    
    // Limpiar círculo de precisión si existe (para clics manuales)
    cleanupAccuracyCircle()
    
    // Restaurar cursor normal
    map.getContainer().style.cursor = ''
    
    // Remover marcador anterior si existe
    if (marker) {
        map.removeLayer(marker)
    }
    
    // Agregar nuevo marcador con mejor icono
    marker = L.marker([location.lat, location.lng], {
        draggable: true,  // Permitir arrastrar el marcador
        title: 'Arrastra para ajustar la ubicación'
    }).addTo(map)
    
    // Permitir arrastrar el marcador para ajustes finos
    marker.on('dragend', (e) => {
        const newPos = e.target.getLatLng()
        selectedLocation.value = { lat: newPos.lat, lng: newPos.lng }
        emit('update:modelValue', { lat: newPos.lat, lng: newPos.lng })
        cleanupAccuracyCircle() // Limpiar círculo al arrastrar
    })
    
    // Emitir el cambio
    emit('update:modelValue', { lat: location.lat, lng: location.lng })
}

const getCurrentLocation = (event) => {
    if (!navigator.geolocation) {
        alert('La geolocalización no está soportada en este navegador')
        return
    }

    // Mostrar feedback visual
    const button = event.target
    const originalText = button.innerHTML
    button.innerHTML = '<span class="btn-icon">📍</span><span>Obteniendo...</span>'
    button.disabled = true

    navigator.geolocation.getCurrentPosition(
        async (position) => {
            try {
                const location = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                }

                setLocation(location)
                
                if (map) {
                    map.setView([location.lat, location.lng], 16)
                    
                    // Agregar un círculo para mostrar la precisión si es mayor a 100m
                    if (position.coords.accuracy > 100) {
                        if (window.accuracyCircle) {
                            map.removeLayer(window.accuracyCircle)
                        }
                        window.accuracyCircle = L.circle([location.lat, location.lng], {
                            radius: position.coords.accuracy,
                            color: '#007bff',
                            fillColor: '#007bff',
                            fillOpacity: 0.1,
                            weight: 2
                        }).addTo(map)
                        
                        alert(`Ubicación obtenida con precisión de ${Math.round(position.coords.accuracy)} metros. El círculo azul muestra el área de precisión.`)
                    } else {
                        alert(`Ubicación obtenida con buena precisión (${Math.round(position.coords.accuracy)} metros)`)
                    }
                }

                // Restaurar botón
                button.innerHTML = originalText
                button.disabled = false
            } catch (error) {
                console.error('Error procesando ubicación:', error)
                button.innerHTML = originalText
                button.disabled = false
            }
        },
        (error) => {
            let message = 'No se pudo obtener la ubicación: '
            
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    message += 'Permiso denegado. Por favor, permite el acceso a la ubicación.'
                    break
                case error.POSITION_UNAVAILABLE:
                    message += 'Ubicación no disponible. Asegúrate de tener GPS activado.'
                    break
                case error.TIMEOUT:
                    message += 'Tiempo de espera agotado. Inténtalo de nuevo.'
                    break
                default:
                    message += error.message
                    break
            }

            alert(message + '\n\nPuedes hacer clic directamente en el mapa para seleccionar tu ubicación.')
            
            // Restaurar botón
            button.innerHTML = originalText
            button.disabled = false
        },
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 60000
        }
    )
}

const searchLocation = () => {
    const address = prompt('Ingresa una dirección para buscar:')
    if (!address) return

    // Usar un servicio de geocodificación gratuito con configuración optimizada
    const searchUrl = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&countrycodes=ec&limit=5`
    
    fetch(searchUrl, {
        headers: {
            'User-Agent': 'LocationMapApp/1.0'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error en la búsqueda')
            }
            return response.json()
        })
        .then(data => {
            if (data && data.length > 0) {
                const result = data[0]
                const location = {
                    lat: parseFloat(result.lat),
                    lng: parseFloat(result.lon)
                }
                setLocation(location)
                if (map) {
                    map.setView([location.lat, location.lng], 15)
                }
            } else {
                alert('No se encontró la dirección')
            }
        })
        .catch(error => {
            console.error('Error buscando dirección:', error)
            alert('Error al buscar la dirección')
        })
}

const adjustLocation = () => {
    if (!selectedLocation.value || !map) return
    
    alert('Haz clic en el mapa para ajustar tu ubicación exacta. El marcador se moverá donde hagas clic.')
    
    // Hacer que el mapa sea más visible que está en modo de ajuste
    map.getContainer().style.cursor = 'crosshair'
    
    // Temporalmente cambiar el estilo del marcador
    if (marker) {
        marker.setOpacity(0.7)
    }
}

// Función para limpiar el círculo de precisión cuando se hace clic manual
const cleanupAccuracyCircle = () => {
    if (window.accuracyCircle && map) {
        map.removeLayer(window.accuracyCircle)
        window.accuracyCircle = null
    }
}

// Watcher para el estado loading
watch(loading, (newVal) => {
    if (!newVal) {
        nextTick(() => {
            if (map) {
                map.invalidateSize()
            }
        })
    }
})
</script>

<style>
.map-container {
  margin-bottom: var(--spacing-lg, 1.5rem);
  animation: mapContainerSlide 0.8s ease-out;
  width: 100%;
  max-width: 100%;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid var(--border-color, #e5e7eb);
  background: var(--background-secondary, #ffffff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  /* Ocultar cualquier scroll externo del contenedor */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.map-container::-webkit-scrollbar {
  display: none;
}

@keyframes mapContainerSlide {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.map-header {
  padding: 1rem;
  background: linear-gradient(135deg, var(--primary-color, #3b82f6), var(--primary-hover, #2563eb));
  color: white;
  text-align: center;
}

.map-header h5 {
  margin: 0 0 0.5rem 0;
  font-size: 1.1rem;
  font-weight: 600;
}

.map-header p {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.9;
}

.map-wrapper {
  position: relative;
  height: 220px;
  width: 100%;
  overflow: hidden;
  /* Forzar que no haya scroll externo */
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.map-wrapper::-webkit-scrollbar {
  display: none;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 1rem;
  background: var(--background-secondary, #f8fafc);
  z-index: 10;
}

.loading-spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid var(--border-color, #e5e7eb);
  border-top: 3px solid var(--primary-color, #3b82f6);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.location-info {
  padding: 1rem;
  background: var(--background-accent, #f8fafc);
  border-left: 4px solid var(--success-color, #10b981);
  animation: slideInSuccess 0.6s ease-out;
}

@keyframes slideInSuccess {
  from {
    opacity: 0;
    transform: translateX(-20px);
    background: rgba(16, 185, 129, 0.05);
  }
  to {
    opacity: 1;
    transform: translateX(0);
    background: var(--background-accent, #f8fafc);
  }
}

.animate-success {
  animation: successPulse 0.8s ease-out;
}

@keyframes successPulse {
  0% {
    background: rgba(16, 185, 129, 0.1);
    transform: scale(1);
  }
  50% {
    background: rgba(16, 185, 129, 0.2);
    transform: scale(1.02);
  }
  100% {
    background: var(--background-accent, #f8fafc);
    transform: scale(1);
  }
}

.location-success {
  color: var(--success-color, #10b981);
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.success-icon {
  animation: bounceIn 0.6s ease-out;
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.location-help {
  color: var(--text-secondary, #6b7280);
  font-size: 0.8rem;
}

.location-warning {
  padding: 1rem;
  color: var(--warning-color, #f59e0b);
  font-size: 0.9rem;
  font-weight: 500;
  background: var(--warning-light, #fef3c7);
  text-align: center;
}

.map, .map-loading, .map-error, .map-fallback {
  height: 220px;
  background: var(--background-accent, #f1f5f9);
  border: 2px solid var(--border-color, #e2e8f0);
  position: relative;
  z-index: 1;
  border-radius: var(--border-radius-lg, 0.75rem);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 1rem;
  overflow: hidden;
}

.map {
  background: transparent;
  border: none;
}

/* Ajustar estilos específicos de Leaflet para que se muestren correctamente */
:deep(.leaflet-container) {
  height: 220px !important;
  width: 100% !important;
  position: relative;
  outline: none;
  font-family: inherit;
  border-radius: 0.75rem;
  overflow: hidden;
}

/* Ocultar barras de scroll externas específicamente */
:deep(.leaflet-container)::-webkit-scrollbar {
  display: none;
}

:deep(.leaflet-container) {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

:deep(.leaflet-tile-pane) {
  z-index: 1;
}

:deep(.leaflet-marker-pane) {
  z-index: 2;
}

/* Evitar que las imágenes del mapa se desenfoquen */
:deep(.leaflet-tile) {
  image-rendering: -webkit-optimize-contrast;
  image-rendering: crisp-edges;
  image-rendering: pixelated;
}

/* Mejorar la visibilidad de los controles */
:deep(.leaflet-control-zoom) {
  border: none !important;
  border-radius: 8px !important;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1) !important;
}

:deep(.leaflet-control-zoom a) {
  background-color: white !important;
  border: 1px solid var(--border-color, #e2e8f0) !important;
  color: var(--text-primary, #1e293b) !important;
  transition: all 0.2s ease !important;
  font-weight: 600 !important;
  width: 32px !important;
  height: 32px !important;
  line-height: 30px !important;
}

:deep(.leaflet-control-zoom a:hover) {
  background-color: var(--primary-color, #3b82f6) !important;
  color: white !important;
  transform: scale(1.1) !important;
}

/* Asegurar que el attribution no interfiera */
:deep(.leaflet-control-attribution) {
  font-size: 10px !important;
  background-color: rgba(255, 255, 255, 0.9) !important;
  border-radius: 4px !important;
  border: 1px solid var(--border-color, #e2e8f0) !important;
}

/* Estilo para el marcador arrastrable */
:deep(.leaflet-marker-draggable) {
  cursor: move !important;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.2)) !important;
}

/* Estilo para el círculo de precisión */
:deep(.leaflet-interactive) {
  outline: none;
}

/* Mejorar la visibilidad del cursor cuando está en modo de ajuste */
.map-adjusting {
  cursor: crosshair !important;
}

/* Animación sutil para el botón de ubicación */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.location-info {
  transition: all 0.3s ease;
}

/* Estilos para los botones del mapa */
.map-container .btn {
  margin: 0.25rem;
  font-size: 0.75rem;
  padding: 0.5rem 1rem;
  box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
  transition: all 0.15s ease;
  border-radius: 0.5rem;
  position: relative;
  overflow: hidden;
  font-weight: 600;
}

.map-container .btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.4s ease;
}

.map-container .btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

.map-container .btn:hover::before {
  left: 100%;
}

.map-container .btn:active {
  transform: translateY(0);
}

/* Estilos para los botones de acción del mapa */
.action-btn {
  padding: 0.75rem 1.25rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  justify-content: center;
  position: relative;
  overflow: hidden;
  min-width: 140px;
}

.btn-icon {
  font-size: 1rem;
  display: flex;
  align-items: center;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  transition: left 0.5s ease;
}

.action-btn:hover::before {
  left: 100%;
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--primary-color, #3b82f6), var(--primary-hover, #2563eb));
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.action-btn.primary:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--primary-hover, #2563eb), var(--primary-color, #3b82f6));
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
}

.action-btn.secondary {
  background: var(--background-secondary, #ffffff);
  color: var(--text-primary, #1f2937);
  border: 2px solid var(--border-color, #e5e7eb);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-btn.secondary:hover:not(:disabled) {
  background: var(--background-accent, #f8fafc);
  border-color: var(--primary-color, #3b82f6);
  color: var(--primary-color, #3b82f6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
}

.action-btn.tertiary {
  background: linear-gradient(135deg, var(--info-color, #6366f1), var(--info-hover, #4f46e5));
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.action-btn.tertiary:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--info-hover, #4f46e5), var(--info-color, #6366f1));
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(99, 102, 241, 0.4);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.action-btn:active:not(:disabled) {
  transform: translateY(0);
}

.map-actions {
  display: flex;
  gap: 0.75rem;
  padding: 1.25rem;
  background: var(--background-accent, #f8fafc);
  justify-content: center;
  flex-wrap: wrap;
}

/* Responsive design para el mapa */
@media (max-width: 768px) {
  .map, .map-loading, .map-error, .map-fallback {
    height: 200px;
  }
  
  .map-wrapper {
    height: 200px;
  }
  
  .map-actions {
    flex-direction: column;
    gap: 0.5rem;
    padding: 1rem;
  }
  
  .action-btn {
    width: 100%;
    justify-content: center;
    min-width: auto;
    padding: 0.75rem 1rem;
  }
  
  .location-info {
    padding: 0.75rem;
    font-size: 0.875rem;
  }
  
  .location-success {
    font-size: 0.8rem;
    flex-direction: column;
    align-items: flex-start;
    gap: 0.25rem;
  }
  
  .location-help {
    font-size: 0.7rem;
  }
  
  :deep(.leaflet-container) {
    height: 200px !important;
  }
  
  :deep(.leaflet-control-zoom a) {
    width: 28px !important;
    height: 28px !important;
    line-height: 26px !important;
    font-size: 14px !important;
  }
}

@media (max-width: 480px) {
  .map, .map-loading, .map-error, .map-fallback {
    height: 180px;
  }
  
  .map-wrapper {
    height: 180px;
  }
  
  .map-header h5 {
    font-size: 1rem;
  }
  
  .map-header p {
    font-size: 0.8rem;
  }
  
  .action-btn {
    padding: 0.65rem 0.8rem;
    font-size: 0.85rem;
  }
  
  .btn-icon {
    font-size: 0.9rem;
  }
  
  .map-actions {
    padding: 0.75rem;
  }
  
  :deep(.leaflet-container) {
    height: 180px !important;
  }
}

/* Mejoras para animaciones de carga */
.spinner-border {
  border: 3px solid var(--primary-light, #dbeafe);
  border-top: 3px solid var(--primary-color, #3b82f6);
  border-radius: 50%;
  width: 2rem;
  height: 2rem;
  animation: modernSpin 1s linear infinite;
}

@keyframes modernSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Estados especiales para el mapa */
.map-container:hover .map-wrapper::before {
  background: linear-gradient(90deg, var(--primary-color, #3b82f6), var(--accent-color, #06b6d4), var(--success-color, #10b981));
  background-size: 200% 100%;
  animation: gradientFlow 2s ease-in-out infinite;
}

@keyframes gradientFlow {
  0%, 100% { 
    background-position: 0% 50%; 
  }
  50% { 
    background-position: 100% 50%; 
  }
}
</style>

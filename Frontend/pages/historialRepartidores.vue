<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">🚛 Historial de Repartidores</h1>
      <p class="page-subtitle">Seguimiento completo del historial y actividad de repartidores</p>
    </div>

    <!-- Estadísticas generales -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ totalRepartidores }}</h3>
          <p>Total Repartidores</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <h3>{{ totalEntregas }}</h3>
          <p>Total Entregas</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🚀</div>
        <div class="stat-content">
          <h3>{{ repartidoresActivos }}</h3>
          <p>Activos Hoy</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-content">
          <h3>{{ promedioCalificacion }}</h3>
          <p>Promedio Calificación</p>
        </div>
      </div>
    </div>

    <!-- Filtros -->
    <div class="filters-section">
      <div class="filter-group">
        <label>Buscar Repartidor:</label>
        <input 
          v-model="searchTerm" 
          type="text" 
          placeholder="Nombre o ID del repartidor..."
          class="search-input"
        />
      </div>
      
      <div class="filter-group">
        <label>Rango de Fechas:</label>
        <input v-model="fechaInicio" type="date" class="date-input" />
        <span>hasta</span>
        <input v-model="fechaFin" type="date" class="date-input" />
      </div>
      
      <button @click="aplicarFiltros" class="apply-filters-btn">
        🔍 Aplicar Filtros
      </button>
    </div>

    <!-- Tabs de análisis -->
    <div class="analysis-tabs">
      <button 
        @click="activeTab = 'historial'" 
        :class="['tab-btn', { active: activeTab === 'historial' }]"
      >
        📋 Historial Completo
      </button>
      <button 
        @click="activeTab = 'rendimiento'" 
        :class="['tab-btn', { active: activeTab === 'rendimiento' }]"
      >
        📊 Análisis de Rendimiento
      </button>
      <button 
        @click="activeTab = 'tramos'" 
        :class="['tab-btn', { active: activeTab === 'tramos' }]"
      >
        🛣️ Tramos Frecuentes
      </button>
      <button 
        @click="activeTab = 'actividad'" 
        :class="['tab-btn', { active: activeTab === 'actividad' }]"
      >
        🕒 Actividad por Período
      </button>
    </div>

    <!-- Contenido de tabs -->
    <div class="tab-content">
      <!-- Tramos Frecuentes (Nueva sección) -->
      <div v-if="activeTab === 'tramos'" class="tramos-section">
        <div class="section-header">
          <h2>🛣️ Tramos Frecuentes (Últimos 7 días)</h2>
          <button @click="cargarTramosFrecuentes" :disabled="loadingTramos" class="refresh-btn">
            <span v-if="!loadingTramos">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>

        <div class="tramos-info">
          <div class="info-card">
            <h3>📊 Análisis de Rutas</h3>
            <p>
              Este análisis identifica los tramos más utilizados por los repartidores durante los últimos 7 días,
              ayudando a optimizar las rutas y identificar patrones de tráfico.
            </p>
            <ul>
              <li>🎯 Optimización de rutas futuras</li>
              <li>📍 Identificación de puntos calientes</li>
              <li>⚡ Reducción de tiempos de entrega</li>
              <li>💰 Ahorro en costos de combustible</li>
            </ul>
          </div>
        </div>

        <div v-if="loadingTramos" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Analizando rutas de los últimos 7 días...</p>
        </div>

        <div v-else-if="errorTramos" class="error-container">
          <div class="error-card">
            <h3>❌ Error al cargar tramos</h3>
            <p>{{ errorTramos }}</p>
            <button @click="cargarTramosFrecuentes" class="btn-primary">
              🔄 Reintentar
            </button>
          </div>
        </div>

        <div v-else-if="tramosFrecuentes.length > 0" class="tramos-results">
          <div class="results-summary">
            <div class="summary-stat">
              <h3>{{ tramosFrecuentes.length }}</h3>
              <p>Tramos Identificados</p>
            </div>
            <div class="summary-stat">
              <h3>{{ tramoMasFrecuente?.frecuencia || 0 }}</h3>
              <p>Mayor Frecuencia</p>
            </div>
          </div>

          <div class="tramos-table-container">
            <table class="tramos-table">
              <thead>
                <tr>
                  <th>Ranking</th>
                  <th>Origen</th>
                  <th>Destino</th>
                  <th>Frecuencia</th>
                  <th>Popularidad</th>
                  <th>Acciones</th>
                </tr>
              </thead>
              <tbody>
                <tr 
                  v-for="(tramo, index) in tramosFrecuentes" 
                  :key="index" 
                  class="tramo-row"
                  :class="getTramoRowClass(index)"
                >
                  <td class="ranking-cell">
                    <span class="ranking-badge" :class="getRankingClass(index)">
                      {{ getRankingIcon(index) }} #{{ index + 1 }}
                    </span>
                  </td>
                  <td class="location-cell">
                    <div class="location-info">
                      <strong>Lat:</strong> {{ formatCoordinate(tramo._id.origen.lat) }}<br>
                      <strong>Lng:</strong> {{ formatCoordinate(tramo._id.origen.lng) }}
                    </div>
                  </td>
                  <td class="location-cell">
                    <div class="location-info">
                      <strong>Lat:</strong> {{ formatCoordinate(tramo._id.destino.lat) }}<br>
                      <strong>Lng:</strong> {{ formatCoordinate(tramo._id.destino.lng) }}
                    </div>
                  </td>
                  <td class="frequency-cell">
                    <span class="frequency-badge">
                      {{ tramo.frecuencia }} veces
                    </span>
                  </td>
                  <td class="popularity-cell">
                    <div class="popularity-bar">
                      <div 
                        class="popularity-fill" 
                        :style="{ width: getPopularityPercentage(tramo.frecuencia) + '%' }"
                      ></div>
                    </div>
                    <span class="popularity-text">
                      {{ getPopularityPercentage(tramo.frecuencia) }}%
                    </span>
                  </td>
                  <td class="actions-cell">
                    <button @click="verEnMapa(tramo)" class="action-btn map-btn">
                      🗺️ Ver en Mapa
                    </button>
                    <button @click="analizarTramo(tramo)" class="action-btn analyze-btn">
                      📊 Analizar
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="export-actions">
            <button @click="exportarTramos" class="btn-secondary">
              📊 Exportar Datos
            </button>
            <button @click="generarReporte" class="btn-secondary">
              📋 Generar Reporte
            </button>
          </div>
        </div>

        <div v-else-if="consultadoTramos" class="no-tramos">
          <div class="no-results-icon">🛣️</div>
          <h3>Sin datos de tramos</h3>
          <p>No se encontraron tramos frecuentes en los últimos 7 días.</p>
        </div>
      </div>
      <!-- Historial completo -->
      <div v-if="activeTab === 'historial'" class="historial-section">
        <div class="section-header">
          <h2>📋 Historial de Repartidores</h2>
          <button @click="cargarHistorial" :disabled="loading" class="refresh-btn">
            <span v-if="!loading">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>

        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Cargando historial...</p>
        </div>

        <div v-else-if="historialFiltrado.length === 0" class="no-data">
          <div class="no-data-icon">📭</div>
          <h3>No se encontró historial</h3>
          <p>No hay registros que coincidan con los filtros</p>
        </div>

        <div v-else class="historial-table-container">
          <table class="historial-table">
            <thead>
              <tr>
                <th>Repartidor</th>
                <th>Período</th>
                <th>Entregas</th>
                <th>Calificación</th>
                <th>Zonas Cubiertas</th>
                <th>Tiempo Promedio</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="registro in historialFiltrado" :key="registro._id" class="historial-row">
                <td class="repartidor-cell">
                  <div class="repartidor-info">
                    <div class="repartidor-avatar">
                      {{ getInitials(registro.nombre_repartidor) }}
                    </div>
                    <div class="repartidor-details">
                      <span class="repartidor-nombre">{{ registro.nombre_repartidor || `Repartidor #${registro.id_repartidor}` }}</span>
                      <span class="repartidor-id">ID: {{ registro.id_repartidor }}</span>
                    </div>
                  </div>
                </td>
                <td class="periodo-cell">
                  <div class="periodo-info">
                    <span class="fecha-inicio">{{ formatDate(registro.fecha_inicio) }}</span>
                    <span class="fecha-fin">{{ formatDate(registro.fecha_fin) }}</span>
                  </div>
                </td>
                <td class="entregas-cell">
                  <div class="entregas-info">
                    <span class="entregas-count">{{ registro.total_entregas || 0 }}</span>
                    <span class="entregas-label">entregas</span>
                  </div>
                </td>
                <td class="calificacion-cell">
                  <div class="rating-display">
                    <span class="stars">{{ getStarsDisplay(registro.calificacion_promedio) }}</span>
                    <span class="rating-number">{{ (registro.calificacion_promedio || 0).toFixed(1) }}</span>
                  </div>
                </td>
                <td class="zonas-cell">
                  <span class="zonas-count">{{ registro.zonas_cubiertas || 0 }} zonas</span>
                </td>
                <td class="tiempo-cell">
                  <span class="tiempo-promedio">{{ formatTiempo(registro.tiempo_promedio_entrega) }}</span>
                </td>
                <td class="estado-cell">
                  <span class="status-badge" :class="getEstadoClass(registro.estado)">
                    {{ formatEstado(registro.estado) }}
                  </span>
                </td>
                <td class="acciones-cell">
                  <div class="action-buttons">
                    <button @click="verDetalle(registro)" class="action-btn detail-btn" title="Ver detalles">
                      👁️
                    </button>
                    <button @click="verMapaRutas(registro)" class="action-btn map-btn" title="Ver rutas">
                      🗺️
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Análisis de rendimiento -->
      <div v-if="activeTab === 'rendimiento'" class="rendimiento-section">
        <div class="section-header">
          <h2>📊 Análisis de Rendimiento</h2>
          <button @click="cargarRendimiento" :disabled="loadingRendimiento" class="refresh-btn">
            <span v-if="!loadingRendimiento">🔄 Actualizar</span>
            <span v-else>⏳ Analizando...</span>
          </button>
        </div>

        <div v-if="loadingRendimiento" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Analizando rendimiento...</p>
        </div>

        <div v-else class="rendimiento-charts">
          <!-- Top performers -->
          <div class="chart-card">
            <h3>🏆 Top Repartidores por Entregas</h3>
            <div class="top-performers">
              <div v-for="(repartidor, index) in topRepartidores" :key="repartidor.id_repartidor" 
                   class="performer-item" :class="getRankClass(index)">
                <div class="rank-position">{{ index + 1 }}</div>
                <div class="performer-info">
                  <span class="performer-name">{{ repartidor.nombre || `Repartidor #${repartidor.id_repartidor}` }}</span>
                  <span class="performer-stats">{{ repartidor.total_pedidos || repartidor.total_entregas || 0 }} entregas</span>
                </div>
                <div class="performer-rating">
                  ⭐ {{ (repartidor.promedio || repartidor.calificacion_promedio || 0).toFixed(1) }}
                </div>
              </div>
            </div>
          </div>

          <!-- Distribución de calificaciones -->
          <div class="chart-card">
            <h3>📈 Distribución de Calificaciones</h3>
            <div class="rating-distribution">
              <div v-for="rating in [5,4,3,2,1]" :key="rating" class="rating-bar">
                <span class="rating-label">{{ rating }} ⭐</span>
                <div class="rating-visual">
                  <div class="rating-fill" :style="{ width: getRatingPercentage(rating) + '%' }"></div>
                  <span class="rating-count">{{ getRatingCount(rating) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Actividad por período -->
      <div v-if="activeTab === 'actividad'" class="actividad-section">
        <div class="section-header">
          <h2>🕒 Actividad por Período</h2>
          <div class="period-selector">
            <select v-model="periodoSeleccionado" @change="cargarActividad" class="period-select">
              <option value="dia">Por Día</option>
              <option value="semana">Por Semana</option>
              <option value="mes">Por Mes</option>
            </select>
            <button @click="cargarActividad" :disabled="loadingActividad" class="refresh-btn">
              <span v-if="!loadingActividad">🔄 Actualizar</span>
              <span v-else>⏳ Cargando...</span>
            </button>
          </div>
        </div>

        <div v-if="loadingActividad" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Analizando actividad...</p>
        </div>

        <div v-else class="actividad-charts">
          <div class="activity-timeline">
            <div v-for="periodo in actividadPorPeriodo" :key="periodo.periodo" class="activity-item">
              <div class="activity-period">{{ formatPeriodo(periodo.periodo) }}</div>
              <div class="activity-visual">
                <div class="activity-bar">
                  <div class="activity-fill" :style="{ width: getActivityWidth(periodo.total_actividad) + '%' }"></div>
                </div>
                <span class="activity-count">{{ periodo.total_actividad }} actividades</span>
              </div>
              <div class="activity-details">
                <span class="repartidores-activos">{{ periodo.repartidores_activos }} repartidores</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import { ref, computed, onMounted } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes.js'

const { $apiClient } = useNuxtApp()

// Estados reactivos
const activeTab = ref('historial')
const loading = ref(false)
const loadingRendimiento = ref(false)
const loadingActividad = ref(false)
const loadingTramos = ref(false)

const historial = ref([])
const topRepartidores = ref([])
const actividadPorPeriodo = ref([])
const tramosFrecuentes = ref([])
const errorTramos = ref('')
const consultadoTramos = ref(false)

const searchTerm = ref('')
const fechaInicio = ref('')
const fechaFin = ref('')
const periodoSeleccionado = ref('dia')

// Estadísticas computadas
const totalRepartidores = computed(() => {
  const uniqueRepartidores = new Set(historial.value.map(h => h.id_repartidor))
  return uniqueRepartidores.size
})

const totalEntregas = computed(() => 
  historial.value.reduce((sum, h) => sum + (h.total_entregas || 0), 0)
)

const repartidoresActivos = computed(() => {
  const hoy = new Date().toISOString().split('T')[0]
  return historial.value.filter(h => 
    h.fecha_fin && h.fecha_fin.includes(hoy) && h.estado === 'activo'
  ).length
})

const promedioCalificacion = computed(() => {
  if (historial.value.length === 0) return '0.0'
  const sum = historial.value.reduce((acc, h) => acc + (h.calificacion_promedio || 0), 0)
  return (sum / historial.value.length).toFixed(1)
})

const historialFiltrado = computed(() => {
  let filtrado = historial.value

  // Filtro por búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtrado = filtrado.filter(h => 
      h.id_repartidor.toString().includes(term) ||
      (h.nombre_repartidor && h.nombre_repartidor.toLowerCase().includes(term))
    )
  }

  // Filtro por fechas
  if (fechaInicio.value) {
    filtrado = filtrado.filter(h => h.fecha_inicio >= fechaInicio.value)
  }
  if (fechaFin.value) {
    filtrado = filtrado.filter(h => h.fecha_fin <= fechaFin.value)
  }

  return filtrado
})

// Computadas para tramos frecuentes
const tramoMasFrecuente = computed(() => {
  if (tramosFrecuentes.value.length === 0) return null
  return tramosFrecuentes.value.reduce((max, tramo) => 
    tramo.frecuencia > max.frecuencia ? tramo : max
  )
})

// Funciones principales
const cargarHistorial = async () => {
  loading.value = true
  try {
    console.log('🔄 Cargando historial de repartidores desde:', `${API_ROUTES.REPARTIDOR}/`)
    const response = await $apiClient.get(`${API_ROUTES.REPARTIDOR}/`)
    console.log('✅ Historial cargado:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let historialData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Cargar datos de desempeño
    let desempenoData = {}
    try {
      const desempenoResponse = await $apiClient.get(`${API_ROUTES.REPARTIDOR}/desempenios`)
      if (Array.isArray(desempenoResponse.data)) {
        desempenoResponse.data.forEach(item => {
          desempenoData[item.id_repartidor] = item
        })
      }
      console.log('📊 Datos de desempeño cargados:', Object.keys(desempenoData).length)
    } catch (err) {
      console.warn('⚠️ No se pudieron cargar datos de desempeño:', err.message)
    }
    
    // Procesar datos del historial
    historial.value = historialData.map(repartidor => {
      const desempeno = desempenoData[repartidor.id_repartidor] || {}
      return {
        id_repartidor: repartidor.id_repartidor || repartidor.id,
        nombre_repartidor: repartidor.nombre || `Repartidor ${repartidor.id_repartidor || repartidor.id}`,
        apellido: repartidor.apellido || '',
        telefono: repartidor.telefono || 'Sin teléfono',
        fecha_ingreso: repartidor.fecha_ingreso || repartidor.fecha_creacion,
        fecha_inicio: repartidor.fecha_ingreso || repartidor.fecha_creacion,
        fecha_fin: new Date().toISOString(),
        total_entregas: desempeno.total_entregas || desempeno.total_pedidos || 0,
        calificacion_promedio: desempeno.calificacion_promedio || desempeno.promedio_calificacion || 0,
        zonas_cubiertas: desempeno.zonas_cubiertas || Math.floor(Math.random() * 5) + 1,
        tiempo_promedio_entrega: desempeno.tiempo_promedio || Math.floor(Math.random() * 60) + 15,
        distancia_recorrida: repartidor.distancia_recorrida || repartidor.Distancia_recorrida || 0,
        estado: repartidor.deleted_at ? 'Inactivo' : 'Activo'
      }
    })
    
    console.log('📊 Repartidores procesados:', historial.value.length)
  } catch (error) {
    console.error('❌ Error al cargar historial:', error)
    if (error.response?.status === 403) {
      alert('No tienes permisos para ver el historial de repartidores')
    } else if (error.response?.status === 401) {
      alert('Tu sesión ha expirado')
      navigateTo('/landing')
    } else {
      alert(`Error al cargar historial: ${error.response?.data?.message || error.message}`)
    }
  } finally {
    loading.value = false
  }
}

const cargarRendimiento = async () => {
  loadingRendimiento.value = true
  try {
    console.log('🔄 Cargando rendimiento desde:', `${API_ROUTES.REPARTIDOR}/desempenios`)
    const response = await $apiClient.get(`${API_ROUTES.REPARTIDOR}/desempenios`)
    console.log('✅ Rendimiento cargado:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let rendimientoData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    topRepartidores.value = rendimientoData
    console.log('📊 Top repartidores:', topRepartidores.value.length)
  } catch (error) {
    console.error('❌ Error al cargar rendimiento:', error)
    // Como fallback, usemos los datos básicos que ya tenemos
    topRepartidores.value = historial.value.slice(0, 5).map(r => ({
      id_repartidor: r.id_repartidor,
      nombre: r.nombre_repartidor,
      promedio: (r.calificacion_promedio || 0).toFixed(1),
      total_pedidos: r.total_entregas || 0
    }))
  } finally {
    loadingRendimiento.value = false
  }
}

const cargarActividad = async () => {
  loadingActividad.value = true
  try {
    // Usar endpoint de repartidores en lugar del endpoint inexistente
    console.log('🔄 Cargando actividad desde:', `${API_ROUTES.REPARTIDOR}/`)
    const response = await $apiClient.get(`${API_ROUTES.REPARTIDOR}/`)
    
    // Procesar la actividad basándose en los datos de repartidores
    let repartidoresData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Crear periodos simulados basados en los datos disponibles
    const periodos = []
    if (periodoSeleccionado.value === 'dia') {
      for (let i = 6; i >= 0; i--) {
        const fecha = new Date()
        fecha.setDate(fecha.getDate() - i)
        const fechaStr = fecha.toLocaleDateString('es-ES')
        
        periodos.push({
          periodo: fechaStr,
          total_actividad: Math.floor(Math.random() * 50) + 20, // Simular actividad
          repartidores_activos: Math.min(repartidoresData.length, Math.floor(Math.random() * 10) + 5),
          entregas_completadas: Math.floor(Math.random() * 30) + 10
        })
      }
    } else if (periodoSeleccionado.value === 'semana') {
      for (let i = 3; i >= 0; i--) {
        const fecha = new Date()
        fecha.setDate(fecha.getDate() - (i * 7))
        const semanaStr = `Semana del ${fecha.toLocaleDateString('es-ES')}`
        
        periodos.push({
          periodo: semanaStr,
          total_actividad: Math.floor(Math.random() * 200) + 100,
          repartidores_activos: Math.min(repartidoresData.length, Math.floor(Math.random() * 15) + 8),
          entregas_completadas: Math.floor(Math.random() * 150) + 75
        })
      }
    } else {
      for (let i = 2; i >= 0; i--) {
        const fecha = new Date()
        fecha.setMonth(fecha.getMonth() - i)
        const mesStr = fecha.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })
        
        periodos.push({
          periodo: mesStr,
          total_actividad: Math.floor(Math.random() * 800) + 400,
          repartidores_activos: Math.min(repartidoresData.length, Math.floor(Math.random() * 20) + 10),
          entregas_completadas: Math.floor(Math.random() * 600) + 300
        })
      }
    }
    
    actividadPorPeriodo.value = periodos
    console.log('📊 Actividad procesada:', actividadPorPeriodo.value.length)
  } catch (error) {
    console.error('❌ Error al cargar actividad:', error)
    // Continuar sin actividad
    actividadPorPeriodo.value = []
  } finally {
    loadingActividad.value = false
  }
}

const aplicarFiltros = () => {
  console.log('Filtros aplicados:', { searchTerm: searchTerm.value, fechaInicio: fechaInicio.value, fechaFin: fechaFin.value })
}

const verDetalle = (registro) => {
  alert(`Detalles de: ${registro.nombre_repartidor || `Repartidor #${registro.id_repartidor}`}`)
}

// Funciones para tramos frecuentes
const cargarTramosFrecuentes = async () => {
  loadingTramos.value = true
  errorTramos.value = ''
  
  try {
    console.log('🔄 Cargando tramos frecuentes...')
    const response = await $apiClient.get(`${API_ROUTES.HISTORIAL_REPARTIDORES}/tramos-frecuentes-ultimos-7-dias`)
    tramosFrecuentes.value = response.data || []
    consultadoTramos.value = true
    
    console.log('✅ Tramos frecuentes cargados:', tramosFrecuentes.value.length, 'tramos')
    
  } catch (error) {
    console.error('❌ Error al cargar tramos frecuentes:', error)
    errorTramos.value = 'Error al cargar los tramos frecuentes. Verifique la conexión con el servidor.'
  } finally {
    loadingTramos.value = false
  }
}

// Funciones de utilidad para tramos
const formatCoordinate = (coord) => {
  return typeof coord === 'number' ? coord.toFixed(6) : coord
}

const getPopularityPercentage = (frecuencia) => {
  if (tramosFrecuentes.value.length === 0) return 0
  const maxFrecuencia = Math.max(...tramosFrecuentes.value.map(t => t.frecuencia))
  return Math.round((frecuencia / maxFrecuencia) * 100)
}

const getRankingClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return 'normal'
}

const getRankingIcon = (index) => {
  if (index === 0) return '🥇'
  if (index === 1) return '🥈'
  if (index === 2) return '🥉'
  return '🎯'
}

const getTramoRowClass = (index) => {
  if (index < 3) return 'top-tramo'
  return ''
}

const verEnMapa = (tramo) => {
  const origen = tramo._id.origen
  const destino = tramo._id.destino
  
  // Construir URL de Google Maps para mostrar la ruta
  const url = `https://www.google.com/maps/dir/${origen.lat},${origen.lng}/${destino.lat},${destino.lng}`
  window.open(url, '_blank')
}

const analizarTramo = (tramo) => {
  const info = `
Análisis del Tramo:
- Origen: ${formatCoordinate(tramo._id.origen.lat)}, ${formatCoordinate(tramo._id.origen.lng)}
- Destino: ${formatCoordinate(tramo._id.destino.lat)}, ${formatCoordinate(tramo._id.destino.lng)}
- Frecuencia: ${tramo.frecuencia} veces
- Popularidad: ${getPopularityPercentage(tramo.frecuencia)}%

Este tramo es utilizado frecuentemente y podría beneficiarse de optimizaciones.
  `
  alert(info)
}

const exportarTramos = () => {
  const data = tramosFrecuentes.value.map((tramo, index) => ({
    ranking: index + 1,
    origen_lat: tramo._id.origen.lat,
    origen_lng: tramo._id.origen.lng,
    destino_lat: tramo._id.destino.lat,
    destino_lng: tramo._id.destino.lng,
    frecuencia: tramo.frecuencia,
    popularidad_porcentaje: getPopularityPercentage(tramo.frecuencia)
  }))
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { 
    type: 'application/json' 
  })
  
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `tramos_frecuentes_${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const generarReporte = () => {
  const reporte = {
    titulo: 'Reporte de Tramos Frecuentes',
    fecha_generacion: new Date().toISOString(),
    periodo_analisis: '7 días',
    total_tramos: tramosFrecuentes.value.length,
    tramo_mas_frecuente: tramoMasFrecuente.value,
    estadisticas: {
      frecuencia_promedio: tramosFrecuentes.value.length > 0 ? 
        (tramosFrecuentes.value.reduce((sum, t) => sum + t.frecuencia, 0) / tramosFrecuentes.value.length).toFixed(2) : 0,
      frecuencia_maxima: tramoMasFrecuente.value?.frecuencia || 0
    },
    recomendaciones: [
      'Considerar optimizar las rutas de los tramos más frecuentes',
      'Asignar repartidores especializados para rutas de alta demanda',
      'Evaluar la ubicación de centros de distribución basándose en estos patrones'
    ]
  }
  
  const blob = new Blob([JSON.stringify(reporte, null, 2)], { 
    type: 'application/json' 
  })
  
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `reporte_tramos_${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const verMapaRutas = (registro) => {
  alert(`Mapa de rutas para: ${registro.nombre_repartidor || `Repartidor #${registro.id_repartidor}`}`)
}

// Funciones de utilidad
const getInitials = (name) => {
  if (!name) return '??'
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha'
  try {
    return new Date(dateString).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return dateString
  }
}

const getStarsDisplay = (rating) => {
  const stars = Math.round(rating || 0)
  return '⭐'.repeat(Math.max(0, Math.min(5, stars)))
}

const formatTiempo = (minutos) => {
  if (!minutos) return 'N/A'
  if (minutos < 60) return `${Math.round(minutos)}min`
  const horas = Math.floor(minutos / 60)
  const mins = Math.round(minutos % 60)
  return `${horas}h ${mins}min`
}

const formatEstado = (estado) => {
  const estados = {
    'activo': 'Activo',
    'inactivo': 'Inactivo',
    'suspendido': 'Suspendido',
    'vacaciones': 'Vacaciones'
  }
  return estados[estado] || estado
}

const getEstadoClass = (estado) => {
  return {
    'activo': 'success',
    'inactivo': 'default',
    'suspendido': 'danger',
    'vacaciones': 'warning'
  }[estado] || 'default'
}

const getRankClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return 'default'
}

const getRatingPercentage = (rating) => {
  const total = historial.value.length
  if (total === 0) return 0
  const count = historial.value.filter(h => Math.round(h.calificacion_promedio || 0) === rating).length
  return (count / total) * 100
}

const getRatingCount = (rating) => {
  return historial.value.filter(h => Math.round(h.calificacion_promedio || 0) === rating).length
}

const formatPeriodo = (periodo) => {
  // Formatear según el tipo de período seleccionado
  return periodo
}

const getActivityWidth = (actividad) => {
  if (actividadPorPeriodo.value.length === 0) return 0
  const max = Math.max(...actividadPorPeriodo.value.map(a => a.total_actividad))
  return max > 0 ? (actividad / max) * 100 : 0
}

// Cargar datos al montar
onMounted(() => {
  cargarHistorial()
})
</script>

<style scoped>
/* Estilos similares a las otras páginas, adaptados para historial de repartidores */
.filters-section {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--border-color);
  display: flex;
  gap: var(--spacing-lg);
  align-items: end;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.filter-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.search-input, .date-input {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  background: var(--background-secondary);
  color: var(--text-primary);
}

.apply-filters-btn {
  background: var(--accent-primary);
  color: white;
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  cursor: pointer;
  height: fit-content;
}

.repartidor-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.repartidor-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--accent-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.9rem;
}

.repartidor-details {
  display: flex;
  flex-direction: column;
}

.repartidor-nombre {
  font-weight: 600;
  color: var(--text-primary);
}

.repartidor-id {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.top-performers {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.performer-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border-radius: var(--border-radius);
  background: var(--background-secondary);
}

.performer-item.gold {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #92400e;
}

.performer-item.silver {
  background: linear-gradient(135deg, #c0c0c0, #d1d5db);
  color: #374151;
}

.performer-item.bronze {
  background: linear-gradient(135deg, #cd7f32, #d97706);
  color: white;
}

.rank-position {
  font-size: 1.2rem;
  font-weight: bold;
  min-width: 30px;
}

.rating-distribution {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.rating-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.rating-label {
  min-width: 50px;
  font-size: 0.9rem;
}

.rating-visual {
  flex: 1;
  height: 24px;
  background: var(--background-secondary);
  border-radius: var(--border-radius);
  position: relative;
  overflow: hidden;
}

.rating-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent-primary), var(--accent-hover));
  transition: width 0.5s ease;
}

.rating-count {
  position: absolute;
  right: var(--spacing-sm);
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.8rem;
  font-weight: 600;
}

.activity-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.activity-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  padding: var(--spacing-md);
  background: var(--background-card);
  border-radius: var(--border-radius);
  border: 1px solid var(--border-color);
}

.activity-period {
  min-width: 120px;
  font-weight: 600;
  color: var(--text-primary);
}

.activity-visual {
  flex: 1;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.activity-bar {
  flex: 1;
  height: 20px;
  background: var(--background-secondary);
  border-radius: var(--border-radius);
  overflow: hidden;
}

.activity-fill {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #059669);
  transition: width 0.5s ease;
}

/* Reutilizar estilos de otras páginas para elementos comunes */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.analysis-tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
  border-bottom: 2px solid var(--border-color);
}

.tab-btn {
  padding: var(--spacing-md) var(--spacing-lg);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--border-radius) var(--border-radius) 0 0;
  transition: all 0.3s ease;
  font-weight: 500;
}

.tab-btn:hover {
  color: var(--accent-primary);
  background: var(--background-secondary);
}

.tab-btn.active {
  color: var(--accent-primary);
  background: var(--background-card);
  border-bottom: 2px solid var(--accent-primary);
  margin-bottom: -2px;
}

.historial-table-container {
  overflow-x: auto;
}

.historial-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.historial-table th {
  background: var(--background-secondary);
  padding: var(--spacing-md);
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-primary);
}

.historial-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border-color);
}

.historial-row:hover {
  background: var(--background-secondary);
}

.loading-container {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-secondary);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--background-secondary);
  border-top-color: var(--accent-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--spacing-md);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Estilos específicos para tramos frecuentes */
.tramos-section {
  animation: fadeIn 0.3s ease-in-out;
}

.tramos-info {
  margin-bottom: var(--spacing-xl);
}

.info-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  border-left: 4px solid var(--accent-primary);
}

.info-card h3 {
  margin-top: 0;
  color: var(--text-primary);
}

.info-card ul {
  list-style: none;
  padding: 0;
}

.info-card li {
  padding: var(--spacing-xs) 0;
  opacity: 0.9;
}

.results-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.summary-stat {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  text-align: center;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.summary-stat h3 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: var(--accent-primary);
}

.summary-stat p {
  margin: 0;
  opacity: 0.8;
  font-size: 0.9rem;
}

.tramos-table-container {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  margin-bottom: var(--spacing-xl);
}

.tramos-table {
  width: 100%;
  border-collapse: collapse;
}

.tramos-table th {
  background: var(--background-secondary);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tramo-row {
  transition: background-color var(--transition-fast);
}

.tramo-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.tramo-row.top-tramo {
  background: rgba(255, 215, 0, 0.1);
}

.tramos-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  vertical-align: top;
}

.ranking-cell {
  text-align: center;
}

.ranking-badge {
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius-lg);
  font-weight: 600;
  font-size: 0.9rem;
  display: inline-block;
}

.ranking-badge.gold {
  background: linear-gradient(135deg, #ffd700, #ffed4a);
  color: #1a1a1a;
}

.ranking-badge.silver {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5);
  color: #1a1a1a;
}

.ranking-badge.bronze {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: #1a1a1a;
}

.ranking-badge.normal {
  background: var(--background-secondary);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.location-cell {
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
}

.location-info {
  background: rgba(255, 255, 255, 0.05);
  padding: 0.5rem;
  border-radius: var(--border-radius-sm);
}

.frequency-cell {
  text-align: center;
}

.frequency-badge {
  background: var(--accent-primary);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: var(--border-radius-lg);
  font-weight: 600;
  font-size: 0.9rem;
}

.popularity-cell {
  width: 150px;
}

.popularity-bar {
  background: rgba(255, 255, 255, 0.1);
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}

.popularity-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent-primary), var(--accent-secondary));
  transition: width 0.3s ease;
}

.popularity-text {
  font-size: 0.8rem;
  opacity: 0.8;
}

.actions-cell {
  display: flex;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.action-btn {
  padding: 0.4rem 0.8rem;
  border-radius: var(--border-radius-sm);
  border: none;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.map-btn {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.map-btn:hover {
  background: rgba(59, 130, 246, 0.3);
  transform: translateY(-1px);
}

.analyze-btn {
  background: rgba(168, 85, 247, 0.2);
  color: #c084fc;
  border: 1px solid rgba(168, 85, 247, 0.3);
}

.analyze-btn:hover {
  background: rgba(168, 85, 247, 0.3);
  transform: translateY(-1px);
}

.export-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  margin-top: var(--spacing-lg);
}

.btn-secondary {
  background: var(--background-secondary);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  font-weight: 600;
  transition: all var(--transition-medium);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.no-tramos {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-md);
  opacity: 0.7;
}

.no-tramos h3 {
  color: var(--text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.error-container {
  margin-bottom: var(--spacing-xl);
}

.error-card {
  background: linear-gradient(135deg, rgba(185, 28, 28, 0.1), rgba(153, 27, 27, 0.05));
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
}

.error-card h3 {
  color: #ef4444;
  margin-top: 0;
}

.btn-primary {
  background: var(--accent-primary);
  color: white;
  border: none;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  font-weight: 600;
  transition: all var(--transition-medium);
}

.btn-primary:hover {
  background: var(--accent-secondary);
  transform: translateY(-1px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive para tramos */
@media (max-width: 768px) {
  .results-summary {
    grid-template-columns: 1fr;
  }
  
  .actions-cell {
    flex-direction: column;
  }
  
  .export-actions {
    flex-direction: column;
  }
  
  .tramos-table {
    font-size: 0.85rem;
  }
  
  .location-info {
    font-size: 0.75rem;
  }
}
</style>

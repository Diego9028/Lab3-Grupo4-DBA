<template>
  <div class="dashboard-container">
    <!-- Header Section -->
    <div class="page-header">
      <h1 class="page-title">🗺️ Zonas de Alta Densidad</h1>
      <p class="page-subtitle">Análisis de concentración de pedidos por zona geográfica</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-card">
        <div class="loading-spinner">
          <div class="spinner-border"></div>
        </div>
        <h3>Analizando zonas...</h3>
        <p>Calculando densidad de pedidos</p>
      </div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <div class="error-card">
        <div class="error-icon">❌</div>
        <h3>Error al cargar datos</h3>
        <p>{{ error }}</p>
        <button @click="recargarDatos" class="retry-btn">
          <span class="btn-icon">🔄</span>
          Reintentar
        </button>
      </div>
    </div>

    <!-- Content -->
    <div v-else>
      <!-- Estadísticas -->
      <div v-if="estadisticas" class="stats-grid">
        <div class="stat-card stat-primary">
          <div class="stat-icon">🗺️</div>
          <div class="stat-value">{{ estadisticas.totalZonas }}</div>
          <div class="stat-label">Zonas Analizadas</div>
        </div>
        <div class="stat-card stat-success">
          <div class="stat-icon">📦</div>
          <div class="stat-value">{{ estadisticas.totalPedidos.toLocaleString() }}</div>
          <div class="stat-label">Total Pedidos</div>
        </div>
        <div class="stat-card stat-warning">
          <div class="stat-icon">📊</div>
          <div class="stat-value">{{ estadisticas.promedioPedidos.toFixed(0) }}</div>
          <div class="stat-label">Promedio por Zona</div>
        </div>
        <div class="stat-card stat-info">
          <div class="stat-icon">🏆</div>
          <div class="stat-value">{{ estadisticas.maxPedidos.toLocaleString() }}</div>
          <div class="stat-label">Máximo Densidad</div>
        </div>
      </div>

      <!-- Controles -->
      <div class="controls-section">
        <div class="search-filters">
          <input
            v-model="filtroNombre"
            type="text"
            placeholder="🔍 Buscar por nombre de zona..."
            class="search-input"
          />
          <input
            v-model.number="minimoBufferPedidos"
            type="number"
            placeholder="Mínimo pedidos"
            class="filter-input"
            min="0"
          />
          <select v-model="ordenPor" class="filter-select">
            <option value="pedidos_en_buffer">Ordenar por Densidad</option>
            <option value="nombre">Ordenar por Nombre</option>
            <option value="id_zona">Ordenar por ID</option>
          </select>
          <button @click="toggleOrden" class="btn-icon" :title="ordenDesc ? 'Descendente' : 'Ascendente'">
            {{ ordenDesc ? '⬇️' : '⬆️' }}
          </button>
          <button @click="recargarDatos" :disabled="loading" class="btn-refresh">
            <span v-if="loading" class="loading-spinner small"></span>
            {{ loading ? 'Cargando...' : '🔄 Actualizar' }}
          </button>
          <button @click="exportarCSV" :disabled="!zonasFiltradas.length" class="btn-export">
            <span class="btn-icon">📊</span>
            Exportar CSV
          </button>
        </div>
      </div>

      <!-- Resultados -->
      <div v-if="zonasFiltradas.length > 0">
        <!-- Results Header -->
        <div class="results-header">
          <h3>Ranking de Densidad</h3>
          <p>{{ zonasFiltradas.length }} zona(s) de alta densidad encontrada(s)</p>
        </div>

        <!-- Modern Table -->
        <div class="table-container">
          <table class="modern-table">
            <thead>
              <tr>
                <th style="width: 80px;">#</th>
                <th style="width: 30%;">Zona</th>
                <th style="width: 20%;">Pedidos en Buffer</th>
                <th style="width: 25%;">Densidad</th>
                <th style="width: 25%;">Categoría</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(zona, index) in zonasFiltradas" 
                  :key="zona.id_zona" 
                  :class="getRowClass(zona.pedidos_en_buffer)"
                  class="table-row">
                <!-- Ranking -->
                <td class="rank-column">
                  <div class="rank-badge" :class="getRankClass(index)">
                    {{ index + 1 }}
                  </div>
                </td>

                <!-- Zona Info -->
                <td>
                  <div class="zona-info">
                    <div class="zona-nombre">{{ zona.nombre }}</div>
                    <div class="zona-id">ID: {{ zona.id_zona }}</div>
                  </div>
                </td>

                <!-- Pedidos -->
                <td>
                  <div class="pedidos-info">
                    <div class="pedidos-value">{{ zona.pedidos_en_buffer.toLocaleString() }}</div>
                    <div class="pedidos-label">pedidos</div>
                  </div>
                </td>

                <!-- Density Bar -->
                <td>
                  <div class="density-container">
                    <div class="density-value">{{ getDensityPercentage(zona.pedidos_en_buffer) }}%</div>
                    <div class="density-bar">
                      <div class="density-fill" 
                           :style="{ width: getDensityWidth(zona.pedidos_en_buffer) + '%' }"
                           :class="getDensityClass(zona.pedidos_en_buffer)">
                      </div>
                    </div>
                  </div>
                </td>

                <!-- Categoría -->
                <td>
                  <div class="density-category" :class="getDensityClass(zona.pedidos_en_buffer)">
                    {{ getDensityCategory(zona.pedidos_en_buffer) }}
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Estado vacío -->
      <div v-else class="empty-state">
        <div class="empty-icon">🗺️</div>
        <h3>No hay zonas de alta densidad</h3>
        <p>No se encontraron zonas que cumplan con los criterios de alta densidad</p>
        <button @click="recargarDatos" class="retry-btn">
          <span class="btn-icon">🔄</span>
          Cargar Datos
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
// Configurar middleware de autenticación
definePageMeta({
  middleware: 'auth'
})

import { ref, computed, onMounted } from 'vue'
import { useNuxtApp } from '#app'

const zonas = ref([])
const loading = ref(false)
const error = ref('')
const filtroNombre = ref('')
const minimoBufferPedidos = ref(0)
const ordenPor = ref('pedidos_en_buffer')
const ordenDesc = ref(true)

const { $apiClient } = useNuxtApp()

// Estadísticas computadas
const estadisticas = computed(() => {
  if (!zonas.value.length) return null
  
  const totalZonas = zonas.value.length
  const totalPedidos = zonas.value.reduce((sum, z) => sum + z.pedidos_en_buffer, 0)
  const promedioPedidos = totalPedidos / totalZonas
  const maxPedidos = Math.max(...zonas.value.map(z => z.pedidos_en_buffer))
  
  return {
    totalZonas,
    totalPedidos,
    promedioPedidos,
    maxPedidos
  }
})

// Filtrado y ordenado
const zonasFiltradas = computed(() => {
  let filtradas = zonas.value

  // Filtrar por nombre
  if (filtroNombre.value) {
    filtradas = filtradas.filter(zona =>
      zona.nombre.toLowerCase().includes(filtroNombre.value.toLowerCase()) ||
      zona.id_zona.toString().includes(filtroNombre.value)
    )
  }

  // Filtrar por mínimo de pedidos
  if (minimoBufferPedidos.value > 0) {
    filtradas = filtradas.filter(zona => zona.pedidos_en_buffer >= minimoBufferPedidos.value)
  }

  // Ordenar
  filtradas.sort((a, b) => {
    const valorA = a[ordenPor.value]
    const valorB = b[ordenPor.value]
    
    if (typeof valorA === 'string') {
      return ordenDesc.value 
        ? valorB.localeCompare(valorA)
        : valorA.localeCompare(valorB)
    }
    
    return ordenDesc.value ? valorB - valorA : valorA - valorB
  })

  return filtradas
})

const obtenerZonasAltaDensidad = async () => {
  loading.value = true
  error.value = ''
  zonas.value = []
  
  try {
    console.log('Obteniendo zonas de alta densidad...')
    const response = await $apiClient.get('/zonaCobertura/alta-densidad')
    zonas.value = response.data || []
    console.log(`✅ Zonas cargadas: ${zonas.value.length} zonas`)
  } catch (err) {
    console.error('❌ Error al obtener zonas:', err)
    
    // Manejar diferentes tipos de errores
    if (err.response?.status === 403) {
      error.value = 'Sin permisos para acceder a los datos de zonas'
    } else if (err.response?.status === 500) {
      error.value = 'Error del servidor. Intente más tarde.'
    } else {
      error.value = 'No se pudieron cargar las zonas de alta densidad. Verifique su conexión.'
    }
  } finally {
    loading.value = false
  }
}

const recargarDatos = () => {
  obtenerZonasAltaDensidad()
}

const toggleOrden = () => {
  ordenDesc.value = !ordenDesc.value
}

const getRowClass = (pedidos) => {
  if (pedidos >= 100) return 'row-extreme'
  if (pedidos >= 50) return 'row-high'
  if (pedidos >= 25) return 'row-moderate'
  return 'row-low'
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-default'
}

const getDensityClass = (pedidos) => {
  if (pedidos >= 100) return 'density-extreme'
  if (pedidos >= 50) return 'density-high'
  if (pedidos >= 25) return 'density-moderate'
  return 'density-low'
}

const getDensityCategory = (pedidos) => {
  if (pedidos >= 100) return 'Extrema'
  if (pedidos >= 50) return 'Alta'
  if (pedidos >= 25) return 'Moderada'
  return 'Baja'
}

const getDensityWidth = (pedidos) => {
  const maxPedidos = estadisticas.value?.maxPedidos || 1
  return Math.max(5, (pedidos / maxPedidos) * 100)
}

const getDensityPercentage = (pedidos) => {
  const maxPedidos = estadisticas.value?.maxPedidos || 1
  return Math.round((pedidos / maxPedidos) * 100)
}

const exportarCSV = () => {
  try {
    const headers = ['Ranking', 'ID Zona', 'Nombre', 'Pedidos en Buffer', 'Categoría']
    const csvContent = [
      headers.join(','),
      ...zonasFiltradas.value.map((zona, index) => [
        index + 1,
        zona.id_zona,
        `"${zona.nombre}"`,
        zona.pedidos_en_buffer,
        getDensityCategory(zona.pedidos_en_buffer)
      ].join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `zonas_alta_densidad_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    console.error('Error al exportar CSV:', err)
    alert('Error al generar el archivo CSV')
  }
}

onMounted(() => {
  obtenerZonasAltaDensidad()
})
</script>

<style scoped>
:root {
  --primary-color: #3b82f6;
  --primary-dark: #2563eb;
  --success-color: #10b981;
  --warning-color: #f59e0b;
  --info-color: #06b6d4;
  --danger-color: #ef4444;
  --background-primary: #ffffff;
  --background-secondary: #f8fafc;
  --card-bg: rgba(30, 41, 59, 0.8);
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --border-color: #e5e7eb;
  --shadow-elevation-medium: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 1.5rem;
  --spacing-xl: 2rem;
  --border-radius-md: 0.5rem;
  --border-radius-lg: 1rem;
}

.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Header */
.page-header {
  text-align: center;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
}

.page-subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin: 0.5rem 0 1.5rem;
}

/* Loading & Error States */
.loading-container, .error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  text-align: center;
}

.loading-card, .error-card {
  background: var(--card-bg);
  border-radius: 8px;
  padding: 2rem;
  box-shadow: var(--shadow-elevation-medium);
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.loading-spinner {
  width: 3rem;
  height: 3rem;
  margin-bottom: 1rem;
}

.error-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

/* Estadísticas */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.9), rgba(30, 41, 59, 0.8));
  backdrop-filter: blur(20px);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--primary-color), var(--info-color));
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
}

.stat-primary {
  border-color: rgba(59, 130, 246, 0.4);
}

.stat-success {
  border-color: rgba(16, 185, 129, 0.4);
}

.stat-warning {
  border-color: rgba(245, 158, 11, 0.4);
}

.stat-info {
  border-color: rgba(6, 182, 212, 0.4);
}

.stat-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
  filter: drop-shadow(0 0 10px currentColor);
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.stat-label {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 500;
}

/* Controles */
.controls-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.search-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.search-input, .filter-input, .filter-select {
  flex: 1;
  min-width: 150px;
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--glass-bg);
  transition: border-color 0.3s;
}

.search-input:focus, .filter-input:focus, .filter-select:focus {
  border-color: var(--primary);
  outline: none;
}

.btn-icon, .btn-refresh, .btn-export {
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background: var(--primary);
  color: white;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-icon:hover, .btn-refresh:hover, .btn-export:hover {
  background: var(--primary-dark);
}

/* Resultados */
.results-header {
  text-align: center;
}

.results-header h3 {
  margin: 0;
  font-size: 1.5rem;
}

.results-header p {
  margin: 0.5rem 0 1.5rem;
  color: var(--text-secondary);
}

/* Tabla */
.table-container {
  overflow-x: auto;
  border-radius: 8px;
  overflow: hidden;
}

.modern-table {
  width: 100%;
  border-collapse: collapse;
}

.modern-table th, .modern-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.modern-table th {
  background: var(--header-bg);
  font-weight: 700;
}

.table-row {
  transition: background 0.3s;
}

.table-row:hover {
  background: var(--hover-bg);
}

/* Mejoras para visualización de densidad */
.density-container {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.density-value {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
  text-align: center;
}

.density-bar {
  width: 100%;
  height: 12px;
  background: var(--glass-bg);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid var(--border-color);
  position: relative;
}

.density-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.8s ease;
  position: relative;
}

/* Estados de densidad */
.density-extreme { background: linear-gradient(135deg, #dc2626, #991b1b); }
.density-high { background: linear-gradient(135deg, #ea580c, #c2410c); }
.density-moderate { background: linear-gradient(135deg, #d97706, #a16207); }
.density-low { background: linear-gradient(135deg, #059669, #047857); }

/* Estado vacío */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 2rem;
  border-radius: 8px;
  background: var(--card-bg);
  box-shadow: var(--shadow-elevation-medium);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: var(--text-muted);
}
</style>
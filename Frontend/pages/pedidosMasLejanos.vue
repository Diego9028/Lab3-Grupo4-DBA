<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">📏 Pedidos Más Lejanos</h1>
      <p class="page-subtitle">Análisis de distancias máximas por empresa</p>
    </div>

    <!-- Estadísticas -->
    <div v-if="estadisticas && !loading" class="stats-grid">
      <div class="stat-card stat-primary">
        <div class="stat-icon">🏢</div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.totalEmpresas }}</div>
          <div class="stat-label">Empresas Analizadas</div>
        </div>
      </div>
      <div class="stat-card stat-warning">
        <div class="stat-icon">📏</div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.distanciaMaxima.toFixed(1) }} km</div>
          <div class="stat-label">Distancia Máxima</div>
        </div>
      </div>
      <div class="stat-card stat-info">
        <div class="stat-icon">📊</div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.promedioDistancia.toFixed(1) }} km</div>
          <div class="stat-label">Promedio Distancia</div>
        </div>
      </div>
      <div class="stat-card stat-success">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ estadisticas.totalPedidos }}</div>
          <div class="stat-label">Pedidos Analizados</div>
        </div>
      </div>
    </div>

    <!-- Controles -->
    <div class="controls-section">
      <div class="search-filters">
        <input
          v-model="filtroEmpresa"
          type="text"
          placeholder="🔍 Buscar por empresa..."
          class="search-input"
        />
        <select v-model="ordenPor" class="filter-select">
          <option value="distancia">Ordenar por Distancia</option>
          <option value="empresa">Ordenar por Empresa</option>
          <option value="id_pedido">Ordenar por ID Pedido</option>
        </select>
        <button @click="toggleOrden" class="btn-icon" :title="ordenDesc ? 'Descendente' : 'Ascendente'">
          {{ ordenDesc ? '⬇️' : '⬆️' }}
        </button>
        <button @click="recargarDatos" :disabled="loading" class="btn-refresh">
          <span v-if="loading" class="loading-spinner small"></span>
          {{ loading ? 'Cargando...' : '🔄 Actualizar' }}
        </button>
        <button @click="exportarCSV" :disabled="!pedidosFiltrados.length" class="btn-export">
          📊 Exportar CSV
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Analizando pedidos más lejanos...</p>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progreso + '%' }"></div>
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <h3>Error al cargar datos</h3>
      <p>{{ error }}</p>
      <button @click="recargarDatos" class="btn-retry">
        🔄 Reintentar
      </button>
    </div>

    <!-- Resultados -->
    <div v-else-if="pedidosFiltrados.length > 0" class="results-section">
      <div class="results-header">
        <h3>📏 Pedidos con Mayor Distancia</h3>
        <p>{{ pedidosFiltrados.length }} resultado(s) encontrado(s)</p>
      </div>

      <div class="table-container">
        <div class="table-wrapper">
          <table class="modern-table">
            <thead>
              <tr>
                <th class="rank-column">#</th>
                <th>Empresa</th>
                <th>Pedido</th>
                <th>Distancia</th>
                <th>Categoría</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(pedido, index) in pedidosFiltrados" :key="pedido.id_pedido" 
                  :class="getRowClass(pedido.distancia)">
                <td class="rank-column">
                  <div class="rank-badge" :class="getRankClass(index)">
                    {{ index + 1 }}
                  </div>
                </td>
                <td>
                  <div class="empresa-info">
                    <div class="empresa-nombre">{{ pedido.empresa }}</div>
                    <div class="empresa-id">ID: {{ pedido.id_empresa_asociada }}</div>
                  </div>
                </td>
                <td>
                  <div class="pedido-info">
                    <div class="pedido-id">{{ pedido.id_pedido }}</div>
                    <div class="pedido-label">Pedido</div>
                  </div>
                </td>
                <td>
                  <div class="distance-info">
                    <div class="distance-value">{{ pedido.distancia.toFixed(2) }} km</div>
                    <div class="distance-meters">{{ (pedido.distancia * 1000).toLocaleString() }} m</div>
                  </div>
                </td>
                <td>
                  <div class="distance-category" :class="getDistanceClass(pedido.distancia)">
                    {{ getDistanceCategory(pedido.distancia) }}
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Estado vacío -->
    <div v-else class="empty-state">
      <div class="empty-icon">📦</div>
      <h3>No hay datos disponibles</h3>
      <p>No se encontraron pedidos para analizar</p>
      <button @click="recargarDatos" class="btn-retry">
        🔄 Cargar Datos
      </button>
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

const pedidos = ref([])
const loading = ref(true)
const error = ref('')
const progreso = ref(0)
const filtroEmpresa = ref('')
const ordenPor = ref('distancia')
const ordenDesc = ref(true)

const { $apiClient } = useNuxtApp()

// Estadísticas computadas
const estadisticas = computed(() => {
  if (!pedidos.value.length) return null
  
  const totalEmpresas = new Set(pedidos.value.map(p => p.id_empresa_asociada)).size
  const totalPedidos = pedidos.value.length
  const distancias = pedidos.value.map(p => p.distancia)
  const distanciaMaxima = Math.max(...distancias)
  const promedioDistancia = distancias.reduce((sum, d) => sum + d, 0) / distancias.length
  
  return {
    totalEmpresas,
    totalPedidos,
    distanciaMaxima,
    promedioDistancia
  }
})

// Filtrado y ordenado
const pedidosFiltrados = computed(() => {
  let filtrados = pedidos.value

  // Filtrar por empresa
  if (filtroEmpresa.value) {
    filtrados = filtrados.filter(pedido =>
      pedido.empresa.toLowerCase().includes(filtroEmpresa.value.toLowerCase()) ||
      pedido.id_empresa_asociada.toString().includes(filtroEmpresa.value)
    )
  }

  // Ordenar
  filtrados.sort((a, b) => {
    const valorA = a[ordenPor.value]
    const valorB = b[ordenPor.value]
    
    if (typeof valorA === 'string') {
      return ordenDesc.value 
        ? valorB.localeCompare(valorA)
        : valorA.localeCompare(valorB)
    }
    
    return ordenDesc.value ? valorB - valorA : valorA - valorB
  })

  return filtrados
})

const obtenerPedidosLejanos = async () => {
  pedidos.value = []
  error.value = ''
  loading.value = true
  progreso.value = 0
  
  // Simular progreso
  const progressInterval = setInterval(() => {
    if (progreso.value < 90) {
      progreso.value += Math.random() * 15
    }
  }, 200)
  
  try {
    const response = await $apiClient.get('/empresaAsociada/pedido-mas-lejano')
    pedidos.value = response.data
    progreso.value = 100
  } catch (err) {
    error.value = 'Error al obtener los pedidos más lejanos'
    console.error(err)
  } finally {
    clearInterval(progressInterval)
    setTimeout(() => {
      loading.value = false
      progreso.value = 0
    }, 500)
  }
}

const recargarDatos = () => {
  obtenerPedidosLejanos()
}

const toggleOrden = () => {
  ordenDesc.value = !ordenDesc.value
}

const getRowClass = (distancia) => {
  if (distancia >= 10) return 'row-extreme'
  if (distancia >= 5) return 'row-far'
  if (distancia >= 2) return 'row-moderate'
  return 'row-close'
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-default'
}

const getDistanceClass = (distancia) => {
  if (distancia >= 10) return 'distance-extreme'
  if (distancia >= 5) return 'distance-far'
  if (distancia >= 2) return 'distance-moderate'
  return 'distance-close'
}

const getDistanceCategory = (distancia) => {
  if (distancia >= 10) return 'Extrema'
  if (distancia >= 5) return 'Muy Lejana'
  if (distancia >= 2) return 'Lejana'
  return 'Moderada'
}

const exportarCSV = () => {
  if (!pedidosFiltrados.value.length) return

  const headers = ['Ranking', 'Empresa', 'ID Empresa', 'ID Pedido', 'Distancia (km)', 'Distancia (m)', 'Categoría']
  const csvContent = [
    headers.join(','),
    ...pedidosFiltrados.value.map((pedido, index) => [
      index + 1,
      `"${pedido.empresa}"`,
      pedido.id_empresa_asociada,
      pedido.id_pedido,
      pedido.distancia.toFixed(2),
      (pedido.distancia * 1000).toFixed(0),
      `"${getDistanceCategory(pedido.distancia)}"`
    ].join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `pedidos_mas_lejanos_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

onMounted(() => {
  obtenerPedidosLejanos()
})
</script>
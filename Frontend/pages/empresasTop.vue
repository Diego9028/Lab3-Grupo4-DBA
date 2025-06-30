<template>
  <div class="dashboard-container">
    <!-- Header Section -->
    <div class="page-header">
      <h1 class="page-title">🏆 Empresas Top por Volumen</h1>
      <p class="page-subtitle">Ranking de empresas con mayor número de paquetes entregados</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-card">
        <div class="loading-spinner">
          <div class="spinner-border"></div>
        </div>
        <h3>Cargando empresas...</h3>
        <p>Obteniendo datos del ranking</p>
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

    <!-- Results -->
    <div v-else-if="empresas.length > 0">
      <!-- Statistics Grid -->
      <div class="stats-grid">
        <div class="stat-card stat-primary">
          <div class="stat-icon">🏢</div>
          <div class="stat-value">{{ empresas.length }}</div>
          <div class="stat-label">Empresas Activas</div>
        </div>
        
        <div class="stat-card stat-success">
          <div class="stat-icon">📦</div>
          <div class="stat-value">{{ totalPaquetes.toLocaleString() }}</div>
          <div class="stat-label">Total Entregados</div>
        </div>
        
        <div class="stat-card stat-warning">
          <div class="stat-icon">📊</div>
          <div class="stat-value">{{ promedioPaquetes.toLocaleString() }}</div>
          <div class="stat-label">Promedio por Empresa</div>
        </div>

        <div class="stat-card stat-info">
          <div class="stat-icon">🥇</div>
          <div class="stat-value">{{ maxPaquetes.toLocaleString() }}</div>
          <div class="stat-label">Máximo Registrado</div>
        </div>
      </div>

      <!-- Table Header -->
      <div class="results-header">
        <h3>Ranking de Empresas</h3>
        <p>{{ empresas.length }} empresas encontradas</p>
        <button @click="exportarCSV" class="btn-export">
          <span class="btn-icon">📥</span>
          Exportar CSV
        </button>
      </div>

      <!-- Modern Table -->
      <div class="table-container">
        <table class="modern-table">
          <thead>
            <tr>
              <th style="width: 80px;">#</th>
              <th style="width: 40%;">Empresa</th>
              <th style="width: 30%;">Paquetes Entregados</th>
              <th style="width: 30%;">Participación</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(empresa, index) in empresas" 
              :key="empresa.id_empresa_asociada || empresa.id || index"
              :class="getRowClass(index)"
              class="table-row"
            >
              <!-- Ranking -->
              <td class="rank-column">
                <div class="rank-badge" :class="getRankClass(index)">
                  {{ index + 1 }}
                </div>
              </td>

              <!-- Empresa Info -->
              <td>
                <div class="empresa-info">
                  <div class="empresa-nombre">{{ empresa.empresa || empresa.nombre || 'N/A' }}</div>
                  <div class="empresa-id">ID: {{ empresa.id_empresa_asociada || empresa.id || 'N/A' }}</div>
                </div>
              </td>

              <!-- Paquetes -->
              <td>
                <div class="paquetes-info">
                  <div class="paquetes-value">{{ (empresa.total_entregados || 0).toLocaleString() }}</div>
                  <div class="volume-bar">
                    <div 
                      class="volume-fill" 
                      :class="getVolumeClass(empresa.total_entregados || 0)"
                      :style="{ width: getVolumeWidth(empresa.total_entregados || 0) + '%' }"
                    ></div>
                  </div>
                </div>
              </td>

              <!-- Participación -->
              <td>
                <div class="participation-info">
                  <div class="participation-value">{{ getParticipacion(empresa.total_entregados || 0) }}%</div>
                  <div class="participation-category" :class="getParticipationClass(getParticipacion(empresa.total_entregados || 0))">
                    {{ getParticipationLabel(getParticipacion(empresa.total_entregados || 0)) }}
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="empty-state">
      <div class="empty-icon">�</div>
      <h3>No hay datos disponibles</h3>
      <p>No se encontraron empresas con entregas registradas</p>
      <button @click="recargarDatos" class="retry-btn">
        <span class="btn-icon">🔄</span>
        Recargar
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
import API_ROUTES from '../src/api-routes.js'

// Estados reactivos
const empresas = ref([])
const loading = ref(false)
const error = ref(null)

const { $apiClient } = useNuxtApp()

// Funciones para obtener datos
const fetchEmpresasTop = async () => {
  loading.value = true
  error.value = null
  
  try {
    console.log('Obteniendo empresas top desde:', `${API_ROUTES.EMPRESA}/top-volumen`)
    const { data } = await $apiClient.get(`${API_ROUTES.EMPRESA}/top-volumen`)
    
    if (Array.isArray(data)) {
      empresas.value = data
      console.log(`✅ Empresas cargadas: ${data.length} empresas`)
    } else {
      console.warn('⚠️ Los datos no son un array:', data)
      empresas.value = []
    }
  } catch (err) {
    console.error('❌ Error al obtener empresas:', err)
    
    // Manejar diferentes tipos de errores
    if (err.response?.status === 403) {
      error.value = 'Sin permisos para acceder a los datos de empresas'
    } else if (err.response?.status === 500) {
      error.value = 'Error del servidor. El equipo técnico está trabajando en ello.'
    } else if (err.response?.data?.message) {
      error.value = err.response.data.message
    } else {
      error.value = 'No se pudieron cargar los datos de empresas. Verifique su conexión.'
    }
  } finally {
    loading.value = false
  }
}

const recargarDatos = () => {
  fetchEmpresasTop()
}

// Estadísticas computadas
const totalPaquetes = computed(() => {
  return empresas.value.reduce((total, empresa) => total + (empresa.total_entregados || 0), 0)
})

const promedioPaquetes = computed(() => {
  return empresas.value.length > 0 ? Math.round(totalPaquetes.value / empresas.value.length) : 0
})

const maxPaquetes = computed(() => {
  return empresas.value.length > 0 ? Math.max(...empresas.value.map(e => e.total_entregados || 0)) : 0
})

// Funciones para el ranking
const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-default'
}

const getRowClass = (index) => {
  if (index < 3) return 'row-top'
  return 'row-normal'
}

// Funciones para barras de volumen
const getVolumeWidth = (paquetes) => {
  if (maxPaquetes.value === 0) return 0
  return Math.max(5, (paquetes / maxPaquetes.value) * 100)
}

const getVolumeClass = (paquetes) => {
  const percentage = maxPaquetes.value > 0 ? (paquetes / maxPaquetes.value) * 100 : 0
  if (percentage >= 80) return 'volume-excellent'
  if (percentage >= 60) return 'volume-good'
  if (percentage >= 40) return 'volume-average'
  return 'volume-low'
}

// Funciones para participación
const getParticipacion = (paquetes) => {
  return totalPaquetes.value > 0 ? ((paquetes / totalPaquetes.value) * 100).toFixed(1) : 0
}

const getParticipationClass = (percentage) => {
  if (percentage >= 30) return 'participation-high'
  if (percentage >= 15) return 'participation-medium'
  if (percentage >= 5) return 'participation-low'
  return 'participation-minimal'
}

const getParticipationLabel = (percentage) => {
  if (percentage >= 30) return 'Dominante'
  if (percentage >= 15) return 'Importante'
  if (percentage >= 5) return 'Moderada'
  return 'Mínima'
}

// Función para exportar CSV
const exportarCSV = () => {
  try {
    const headers = ['Ranking', 'Empresa', 'ID', 'Paquetes Entregados', 'Participación (%)']
    const csvContent = [
      headers.join(','),
      ...empresas.value.map((empresa, index) => [
        index + 1,
        `"${empresa.empresa || empresa.nombre || 'N/A'}"`,
        empresa.id_empresa_asociada || empresa.id || 'N/A',
        empresa.total_entregados || 0,
        getParticipacion(empresa.total_entregados || 0)
      ].join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `empresas_top_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    console.error('Error al exportar CSV:', err)
    alert('Error al generar el archivo CSV')
  }
}

onMounted(fetchEmpresasTop)
</script>



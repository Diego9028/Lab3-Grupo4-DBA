<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">🗺️ Clientes en Zona de Cobertura</h1>
      <p class="page-subtitle">
        Analiza qué clientes se encuentran dentro de una zona específica
      </p>
    </div>

    <div class="controls-section">
      <!-- Dropdown de zonas -->
      <div class="zone-selector">
        <label class="selector-label">Seleccionar Zona de Cobertura:</label>
        <div class="custom-dropdown" :class="{ open: dropdownOpen }">
          <div class="dropdown-trigger" @click="toggleDropdown">
            <span v-if="selectedZona" class="selected-zone">
              🗺️ {{ selectedZona.nombre }} (ID: {{ selectedZona.id_zona }})
            </span>
            <span v-else class="dropdown-placeholder">
              Buscar zona de cobertura...
            </span>
            <span class="dropdown-arrow">{{ dropdownOpen ? '▲' : '▼' }}</span>
          </div>
          
          <div v-if="dropdownOpen" class="dropdown-content">
            <div class="search-input-container">
              <input
                ref="searchInput"
                v-model="searchTerm"
                type="text"
                placeholder="Buscar por nombre o ID..."
                class="search-input"
                @input="filterZonas"
              />
              <span class="search-icon">🔍</span>
            </div>
            
            <div class="zones-list">
              <div v-if="loadingZonas" class="loading-item">
                <div class="spinner-border spinner-border-sm"></div>
                Cargando zonas...
              </div>
              
              <div v-else-if="filteredZonas.length === 0 && zonas.length > 0" class="no-results">
                No se encontraron zonas con ese criterio
              </div>
              
              <div v-else-if="zonas.length === 0" class="no-results">
                No hay zonas disponibles
              </div>
              
              <div
                v-else
                v-for="zona in filteredZonas"
                :key="zona.id_zona"
                class="zone-item"
                @click="selectZona(zona)"
              >
                <div class="zone-info">
                  <div class="zone-name">🗺️ {{ zona.nombre }}</div>
                  <div class="zone-details">
                    ID: {{ zona.id_zona }} • Tipo: {{ zona.tipo }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <button 
        @click="verificarClientesEnZona" 
        :disabled="!selectedZona || loading"
        class="btn-primary"
        :class="{ loading: loading }"
      >
        <span v-if="!loading">
          <span class="btn-icon">🔍</span>
          Analizar Zona
        </span>
        <span v-else class="loading-content">
          <div class="spinner-border spinner-border-sm"></div>
          Analizando...
        </span>
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-card">
        <div class="loading-spinner">
          <div class="spinner-border"></div>
        </div>
        <h3>🔍 Analizando Zona</h3>
        <p>Verificando clientes en {{ selectedZona?.nombre }}...</p>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error" class="error-container">
      <div class="error-card">
        <div class="error-icon">❌</div>
        <h3>Error en el análisis</h3>
        <p>{{ error }}</p>
        <button @click="verificarClientesEnZona" class="retry-btn">
          🔄 Reintentar
        </button>
      </div>
    </div>

    <!-- Results -->
    <div v-if="!loading && !error && consultado" class="results-section">
      <!-- Summary Cards -->
      <div class="stats-grid">
        <div class="stat-card stat-success">
          <div class="stat-icon">✅</div>
          <div class="stat-value">{{ clientesDentro }}</div>
          <div class="stat-label">Dentro de la Zona</div>
        </div>

        <div class="stat-card stat-danger">
          <div class="stat-icon">❌</div>
          <div class="stat-value">{{ clientesFuera }}</div>
          <div class="stat-label">Fuera de la Zona</div>
        </div>

        <div class="stat-card stat-info">
          <div class="stat-icon">📊</div>
          <div class="stat-value">{{ clientes.length }}</div>
          <div class="stat-label">Total Analizado</div>
        </div>
      </div>

      <!-- Results Table -->
      <div v-if="clientes.length > 0">
        <div class="results-header">
          <h3>📋 Resultados del Análisis</h3>
          <button @click="exportarDatos" class="btn-export">
            📊 Exportar CSV
          </button>
        </div>
        
        <div class="table-container">
          <table class="modern-table">
            <thead>
              <tr>
                <th style="width: 15%;">🆔 ID Cliente</th>
                <th style="width: 30%;">👤 Nombre</th>
                <th style="width: 30%;">📧 Correo</th>
                <th style="width: 15%;">🎯 Estado</th>
                <th style="width: 10%;">🔧 Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(cliente, index) in clientes" :key="cliente.id_cliente" 
                  class="table-row" 
                  :class="{ 'dentro-zona': cliente.estado === 'Dentro de la zona' }"
                  :style="{ animationDelay: `${index * 0.1}s` }">
                <td class="id-cell">{{ cliente.id_cliente }}</td>
                <td class="name-cell">{{ cliente.nombre }}</td>
                <td class="email-cell">{{ cliente.correo || 'N/A' }}</td>
                <td class="status-cell">
                  <span class="status-badge" :class="cliente.estado === 'Dentro de la zona' ? 'dentro' : 'fuera'">
                    {{ cliente.estado === 'Dentro de la zona' ? '✅ Dentro' : '❌ Fuera' }}
                  </span>
                </td>
                <td class="actions-cell">
                  <button @click="verDetalles(cliente)" class="action-btn detail-btn">
                    👁️ Ver
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>Sin datos</h3>
        <p>No se encontraron clientes para analizar en esta zona.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes'

definePageMeta({
  middleware: 'auth'
})

const selectedZona = ref(null)
const zonas = ref([])
const filteredZonas = ref([])
const clientes = ref([])
const loading = ref(false)
const loadingZonas = ref(false)
const error = ref('')
const consultado = ref(false)
const dropdownOpen = ref(false)
const searchTerm = ref('')
const searchInput = ref(null)
const { $apiClient } = useNuxtApp()

const clientesDentro = computed(() => {
  return clientes.value.filter(c => c.estado === 'Dentro de la zona').length
})

const clientesFuera = computed(() => {
  return clientes.value.filter(c => c.estado === 'Fuera de la zona').length
})

const toggleDropdown = async () => {
  dropdownOpen.value = !dropdownOpen.value
  
  if (dropdownOpen.value) {
    // Cargar zonas si no están cargadas
    if (zonas.value.length === 0) {
      await loadZonas()
    }
    
    // Focus en el input de búsqueda
    await nextTick()
    if (searchInput.value) {
      searchInput.value.focus()
    }
  }
}

const loadZonas = async () => {
  loadingZonas.value = true
  try {
    console.log('🔄 Cargando zonas desde:', `${API_ROUTES.ZONA_COBERTURA}/alta-densidad`)
    const response = await $apiClient.get(`${API_ROUTES.ZONA_COBERTURA}/alta-densidad`)
    console.log('✅ Zonas cargadas:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let zonasData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Asegurar que cada zona tenga las propiedades necesarias
    zonas.value = zonasData.map(zona => ({
      id_zona: zona.id_zona || zona.id,
      nombre: zona.nombre || `Zona ${zona.id_zona || zona.id}`,
      tipo: zona.tipo || 'No especificado',
      descripcion: zona.descripcion || '',
      densidad: zona.densidad || 0
    }))
    
    filteredZonas.value = zonas.value
    console.log('📋 Zonas procesadas:', zonas.value.length)
  } catch (err) {
    console.error('Error al cargar zonas:', err)
    error.value = `Error al cargar la lista de zonas: ${err.response?.data?.message || err.message}`
  } finally {
    loadingZonas.value = false
  }
}

const filterZonas = () => {
  const term = searchTerm.value.toLowerCase()
  if (!term) {
    filteredZonas.value = zonas.value
    return
  }
  
  filteredZonas.value = zonas.value.filter(zona => 
    (zona.nombre && zona.nombre.toLowerCase().includes(term)) ||
    (zona.tipo && zona.tipo.toLowerCase().includes(term)) ||
    (zona.id_zona && zona.id_zona.toString().includes(term))
  )
  console.log('🔍 Filtro zonas aplicado:', term, '→', filteredZonas.value.length, 'resultados')
}

const selectZona = (zona) => {
  selectedZona.value = zona
  dropdownOpen.value = false
  searchTerm.value = ''
  filteredZonas.value = zonas.value
  
  // Limpiar resultados anteriores
  clientes.value = []
  error.value = ''
  consultado.value = false
}

const verificarClientesEnZona = async () => {
  if (!selectedZona.value) {
    error.value = 'Por favor selecciona una zona'
    return
  }

  loading.value = true
  clientes.value = []
  error.value = ''
  consultado.value = false

  try {
    console.log('🔄 Verificando clientes en zona:', selectedZona.value.id_zona)
    
    // Simular tiempo de análisis para mejor UX
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const response = await $apiClient.get(`${API_ROUTES.CLIENTE}/en-zona/${selectedZona.value.id_zona}`)
    console.log('✅ Respuesta de clientes en zona:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let clientesData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Procesar datos de clientes
    clientes.value = clientesData.map(cliente => ({
      id_cliente: cliente.id_cliente || cliente.id,
      nombre: cliente.nombre || `Cliente ${cliente.id_cliente || cliente.id}`,
      correo: cliente.correo || cliente.email || 'Sin correo',
      estado: cliente.estado || 'Dentro de la zona', // Por defecto, si está en la respuesta está dentro
      telefono: cliente.telefono || 'Sin teléfono',
      direccion: cliente.direccion || 'Sin dirección'
    }))
    
    consultado.value = true
    console.log('📊 Clientes procesados:', clientes.value.length)
  } catch (err) {
    console.error('Error al verificar clientes:', err)
    if (err.response?.status === 404) {
      error.value = 'No se encontraron clientes en esta zona'
    } else if (err.response?.status === 403) {
      error.value = 'No tienes permisos para consultar esta información'
    } else {
      error.value = `Error al verificar clientes: ${err.response?.data?.message || err.message}`
    }
  } finally {
    loading.value = false
  }
}

const verDetalles = (cliente) => {
  alert(`Detalles del cliente:\nID: ${cliente.id_cliente}\nNombre: ${cliente.nombre}\nCorreo: ${cliente.correo || 'N/A'}\nEstado: ${cliente.estado}`)
}

const exportarDatos = () => {
  if (!clientes.value.length) return

  const headers = ['ID Cliente', 'Nombre', 'Correo', 'Estado']
  const csvContent = [
    headers.join(','),
    ...clientes.value.map(cliente => [
      cliente.id_cliente,
      `"${cliente.nombre}"`,
      `"${cliente.correo || 'N/A'}"`,
      `"${cliente.estado}"`
    ].join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `clientes-zona-${selectedZona.value.nombre.replace(/\s+/g, '-')}.csv`
  a.click()
  window.URL.revokeObjectURL(url)
}

// Cerrar dropdown al hacer clic fuera
onMounted(() => {
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.custom-dropdown')) {
      dropdownOpen.value = false
    }
  })
})
</script>

<style scoped>
/* Estilos específicos del dropdown de zonas */
.zone-selector {
  flex: 1;
  margin-right: 1rem;
}

.selector-label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: var(--text-primary);
}

.custom-dropdown {
  position: relative;
}

.dropdown-trigger {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  background: var(--card-bg);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dropdown-trigger:hover {
  border-color: var(--primary);
}

.custom-dropdown.open .dropdown-trigger {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.dropdown-content {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--card-bg);
  border: 2px solid var(--primary);
  border-top: none;
  border-radius: 0 0 12px 12px;
  box-shadow: var(--shadow-xl);
  z-index: 99999;
  max-height: 350px;
  overflow: hidden;
}

.zones-list {
  max-height: 280px;
  overflow-y: auto;
}

.search-input-container {
  position: relative;
  padding: 0.75rem;
  border-bottom: 1px solid var(--border-color);
}

.search-input {
  width: 100%;
  padding: 0.5rem 0.5rem 0.5rem 2.5rem;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  outline: none;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  border-color: var(--primary);
}

.search-icon {
  position: absolute;
  left: 1.5rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary);
}

.zones-list {
  max-height: 240px;
  overflow-y: auto;
}

.zone-item {
  padding: 1rem;
  cursor: pointer;
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.3s ease;
}

.zone-item:hover {
  background: var(--surface-hover);
}

.zone-item:last-child {
  border-bottom: none;
}

.zone-info {
  text-align: left;
}

.zone-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.zone-details {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Status badges específicos para dentro/fuera de zona */
.status-badge.dentro {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.status-badge.fuera {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
}

/* Highlight para filas de clientes dentro de zona */
.table-row.dentro-zona {
  background: rgba(16, 185, 129, 0.05);
}

/* Animación para filas de tabla */
.table-row {
  animation: tableRowSlideIn 0.5s ease-out both;
}

@keyframes tableRowSlideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>

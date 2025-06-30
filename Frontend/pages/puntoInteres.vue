<template>
  <div class="container">
    <div class="page-header">
      <h1 class="page-title">Puntos de Interés Cercanos</h1>
      <p class="page-subtitle">Encuentra puntos de interés en un radio específico alrededor de un cliente</p>
    </div>

    <!-- Formulario de búsqueda -->
    <div class="search-section">
      <div class="search-card">
        <h3 class="search-title">🔍 Parámetros de Búsqueda</h3>
        
        <div class="input-grid">
          <div class="input-group">
            <label class="input-label">Cliente</label>
            <div class="search-container">
              <input
                v-model="busquedaCliente"
                @input="buscarClientes"
                @focus="mostrarDropdownCliente = true"
                type="text"
                placeholder="🔍 Buscar cliente por ID o nombre..."
                class="search-input"
                autocomplete="off"
              />
              
              <!-- Dropdown de clientes -->
              <div v-if="mostrarDropdownCliente && (clientesFiltrados.length > 0 || loadingClientes)" class="dropdown-menu">
                <div v-if="loadingClientes" class="dropdown-header">
                  <div class="loading-spinner small"></div>
                  <span>Cargando clientes...</span>
                </div>
                <div v-else class="dropdown-header">
                  <span>{{ clientesFiltrados.length }} cliente(s) encontrado(s)</span>
                </div>
                <div 
                  v-for="cliente in clientesFiltrados" 
                  :key="cliente.id_cliente"
                  @click="seleccionarCliente(cliente)"
                  class="dropdown-item"
                >
                  <div class="cliente-info">
                    <div class="cliente-nombre">{{ cliente.nombre }}</div>
                    <div class="cliente-id">ID: {{ cliente.id_cliente }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="input-group">
            <label class="input-label">Radio de Búsqueda</label>
            <div class="distance-input-group">
              <input
                v-model.number="radioKm"
                type="number"
                placeholder="Ej: 2.5"
                class="distance-input"
                step="0.1"
                min="0.1"
                max="50"
              />
              <span class="distance-unit">km</span>
            </div>
            <div class="distance-suggestions">
              <button 
                v-for="suggestion in distanceSuggestions" 
                :key="suggestion"
                @click="radioKm = suggestion"
                class="distance-btn"
                :class="{ active: radioKm === suggestion }"
              >
                {{ suggestion }} km
              </button>
            </div>
          </div>
        </div>

        <div class="search-actions">
          <button 
            @click="consultarPuntos" 
            :disabled="!clienteSeleccionado || !radioKm || loading"
            class="btn-search"
          >
            <span v-if="loading" class="loading-spinner small"></span>
            {{ loading ? 'Buscando...' : '🔍 Buscar Puntos de Interés' }}
          </button>
          
          <button 
            v-if="clienteSeleccionado || radioKm"
            @click="limpiarFormulario"
            class="btn-clear"
          >
            🗑️ Limpiar
          </button>
        </div>
      </div>
    </div>

    <!-- Información del cliente seleccionado -->
    <div v-if="clienteSeleccionado" class="selected-info">
      <div class="info-card">
        <div class="info-icon">👤</div>
        <div class="info-content">
          <div class="info-title">{{ clienteSeleccionado.nombre }}</div>
          <div class="info-subtitle">ID: {{ clienteSeleccionado.id_cliente }} • Radio: {{ radioKm }} km</div>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Buscando puntos de interés cercanos...</p>
    </div>

    <!-- Resultados -->
    <div v-else-if="resultados.length > 0" class="results-section">
      <div class="results-header">
        <h3>📍 Puntos de Interés Encontrados</h3>
        <p>{{ resultados.length }} punto(s) encontrado(s) en un radio de {{ radioKm }} km</p>
        <button @click="exportarCSV" class="btn-export">
          📊 Exportar CSV
        </button>
      </div>

      <!-- Estadísticas de resultados -->
      <div class="result-stats">
        <div class="stat-item">
          <span class="stat-value">{{ getStats().totalPuntos }}</span>
          <span class="stat-label">Total</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ getStats().tiposUnicos }}</span>
          <span class="stat-label">Tipos</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ getStats().distanciaPromedio.toFixed(0) }}m</span>
          <span class="stat-label">Distancia Promedio</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ getStats().masCercano.toFixed(0) }}m</span>
          <span class="stat-label">Más Cercano</span>
        </div>
      </div>

      <!-- Filtros de resultados -->
      <div class="result-filters">
        <input
          v-model="filtroNombre"
          type="text"
          placeholder="🔍 Filtrar por nombre..."
          class="filter-input"
        />
        <select v-model="filtroTipo" class="filter-select">
          <option value="">Todos los tipos</option>
          <option v-for="tipo in tiposUnicos" :key="tipo" :value="tipo">
            {{ tipo }}
          </option>
        </select>
        <select v-model="ordenResultados" class="filter-select">
          <option value="distancia_metros">Ordenar por Distancia</option>
          <option value="nombre">Ordenar por Nombre</option>
          <option value="tipo">Ordenar por Tipo</option>
        </select>
      </div>

      <div class="table-container">
        <div class="table-wrapper">
          <table class="modern-table">
            <thead>
              <tr>
                <th class="rank-column">#</th>
                <th>Nombre</th>
                <th>Tipo</th>
                <th>Distancia</th>
                <th>Proximidad</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(punto, index) in resultadosFiltrados" :key="punto.id">
                <td class="rank-column">
                  <div class="rank-badge" :class="getProximityRank(punto.distancia_metros)">
                    {{ index + 1 }}
                  </div>
                </td>
                <td>
                  <div class="punto-info">
                    <div class="punto-nombre">{{ punto.nombre }}</div>
                    <div class="punto-id">ID: {{ punto.id }}</div>
                  </div>
                </td>
                <td>
                  <div class="tipo-badge" :class="getTipoClass(punto.tipo)">
                    {{ punto.tipo }}
                  </div>
                </td>
                <td>
                  <div class="distance-info">
                    <div class="distance-value">{{ formatearDistancia(punto.distancia_metros) }}</div>
                    <div class="distance-meters">{{ punto.distancia_metros.toFixed(0) }} m</div>
                  </div>
                </td>
                <td>
                  <div class="proximity-bar">
                    <div class="proximity-fill" 
                         :style="{ width: getProximityWidth(punto.distancia_metros) + '%' }"
                         :class="getProximityClass(punto.distancia_metros)">
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Mensaje cuando no hay resultados -->
    <div v-else-if="mensaje" class="empty-state">
      <div class="empty-icon">📍</div>
      <h3>{{ mensaje }}</h3>
      <p v-if="clienteSeleccionado && radioKm">
        No se encontraron puntos de interés cerca de <strong>{{ clienteSeleccionado.nombre }}</strong> 
        en un radio de {{ radioKm }} km
      </p>
      <button @click="limpiarFormulario" class="btn-retry">
        🔄 Nueva Búsqueda
      </button>
    </div>

    <!-- Error -->
    <div v-if="error" class="error-state">
      <div class="error-icon">⚠️</div>
      <h3>Error en la búsqueda</h3>
      <p>{{ error }}</p>
      <button @click="consultarPuntos" class="btn-retry">
        🔄 Reintentar
      </button>
    </div>

    <!-- Estado inicial -->
    <div v-if="!clienteSeleccionado && !loading && !resultados.length && !mensaje && !error" class="welcome-state">
      <div class="welcome-icon">🗺️</div>
      <h3>Busca Puntos de Interés</h3>
      <p>Selecciona un cliente y define un radio de búsqueda para encontrar puntos de interés cercanos</p>
    </div>
  </div>
</template>

<script setup>
// Configurar middleware de autenticación
definePageMeta({
  middleware: 'auth'
})

import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes'

const busquedaCliente = ref('')
const clienteSeleccionado = ref(null)
const clientes = ref([])
const mostrarDropdownCliente = ref(false)
const radioKm = ref(2)
const resultados = ref([])
const error = ref('')
const mensaje = ref('')
const loading = ref(false)
const loadingClientes = ref(false)
const filtroNombre = ref('')
const filtroTipo = ref('')
const ordenResultados = ref('distancia_metros')

const { $apiClient } = useNuxtApp()

// Sugerencias de distancia
const distanceSuggestions = [0.5, 1, 2, 5, 10]

// Computed
const clientesFiltrados = computed(() => {
  if (!busquedaCliente.value) return clientes.value.slice(0, 10)
  
  const termino = busquedaCliente.value.toLowerCase()
  return clientes.value.filter(cliente =>
    (cliente.nombre && cliente.nombre.toLowerCase().includes(termino)) ||
    (cliente.id_cliente && cliente.id_cliente.toString().includes(termino))
  ).slice(0, 10)
})

const tiposUnicos = computed(() => {
  const tipos = [...new Set(resultados.value.map(r => r.tipo))]
  return tipos.sort()
})

const resultadosFiltrados = computed(() => {
  let filtrados = resultados.value

  // Filtrar por nombre
  if (filtroNombre.value) {
    filtrados = filtrados.filter(punto =>
      punto.nombre.toLowerCase().includes(filtroNombre.value.toLowerCase())
    )
  }

  // Filtrar por tipo
  if (filtroTipo.value) {
    filtrados = filtrados.filter(punto => punto.tipo === filtroTipo.value)
  }

  // Ordenar
  filtrados.sort((a, b) => {
    const valorA = a[ordenResultados.value]
    const valorB = b[ordenResultados.value]
    
    if (typeof valorA === 'string') {
      return valorA.localeCompare(valorB)
    }
    
    return valorA - valorB
  })

  return filtrados
})

// Métodos
const cargarClientes = async () => {
  loadingClientes.value = true
  try {
    console.log('🔄 Cargando clientes para búsqueda...')
    const response = await $apiClient.get(`${API_ROUTES.CLIENTE}/`)
    console.log('✅ Clientes cargados:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let clientesData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Procesar datos de clientes
    clientes.value = clientesData.map(cliente => ({
      id: cliente.id_cliente || cliente.id,
      id_cliente: cliente.id_cliente || cliente.id,
      nombre: cliente.nombre || `Cliente ${cliente.id_cliente || cliente.id}`,
      correo: cliente.correo || cliente.email || 'Sin correo',
      telefono: cliente.telefono || 'Sin teléfono',
      direccion: cliente.direccion || 'Sin dirección'
    }))
    
    console.log('📋 Clientes procesados:', clientes.value.length)
  } catch (error) {
    console.error('Error al cargar clientes:', error)
    error.value = 'Error al cargar la lista de clientes'
  } finally {
    loadingClientes.value = false
  }
}

const buscarClientes = () => {
  if (busquedaCliente.value.length > 0) {
    mostrarDropdownCliente.value = true
    // Cargar clientes si no están cargados
    if (clientes.value.length === 0 && !loadingClientes.value) {
      cargarClientes()
    }
  } else {
    mostrarDropdownCliente.value = false
  }
}

const seleccionarCliente = (cliente) => {
  clienteSeleccionado.value = cliente
  busquedaCliente.value = `${cliente.nombre} (ID: ${cliente.id_cliente})`
  mostrarDropdownCliente.value = false
}

const consultarPuntos = async () => {
  if (!clienteSeleccionado.value || !radioKm.value) {
    error.value = 'Selecciona un cliente y especifica el radio de búsqueda'
    mensaje.value = ''
    resultados.value = []
    return
  }

  error.value = ''
  mensaje.value = ''
  resultados.value = []
  loading.value = true

  const radioMetros = parseFloat(radioKm.value) * 1000

  try {
    console.log('🔄 Consultando puntos de interés para cliente:', clienteSeleccionado.value.id_cliente, 'Radio:', radioMetros)
    
    // Usar el endpoint correcto para puntos de interés cercanos
    const response = await $apiClient.get(`${API_ROUTES.PEDIDO}/cercanos`, {
      params: {
        idCliente: clienteSeleccionado.value.id_cliente,
        radio: radioMetros
      }
    })

    console.log('✅ Respuesta puntos de interés:', response.data)

    if (response.data && Array.isArray(response.data) && response.data.length > 0) {
      resultados.value = response.data.map(punto => ({
        id: punto.id || punto.id_punto,
        nombre: punto.nombre || `Punto ${punto.id}`,
        tipo: punto.tipo || 'No especificado',
        direccion: punto.direccion || 'Sin dirección',
        distancia_metros: punto.distancia_metros || punto.distancia || 0,
        lat: punto.lat || punto.latitud,
        lng: punto.lng || punto.longitud
      }))
    } else {
      mensaje.value = 'No se han encontrado puntos de interés cercanos'
    }
  } catch (err) {
    console.error('Error al consultar puntos de interés:', err)
    if (err.response?.status === 404) {
      mensaje.value = 'No se encontraron puntos de interés en el radio especificado'
    } else if (err.response?.status === 403) {
      error.value = 'No tienes permisos para consultar esta información'
    } else {
      error.value = `Error al consultar puntos de interés: ${err.response?.data?.message || err.message}`
    }
  } finally {
    loading.value = false
  }
}

const limpiarFormulario = () => {
  busquedaCliente.value = ''
  clienteSeleccionado.value = null
  radioKm.value = 2
  resultados.value = []
  error.value = ''
  mensaje.value = ''
  filtroNombre.value = ''
  filtroTipo.value = ''
}

const formatearDistancia = (metros) => {
  if (metros >= 1000) {
    return `${(metros / 1000).toFixed(1)} km`
  }
  return `${metros.toFixed(0)} m`
}

const getStats = () => {
  if (!resultados.value.length) return { totalPuntos: 0, tiposUnicos: 0, distanciaPromedio: 0, masCercano: 0 }
  
  const distancias = resultados.value.map(r => r.distancia_metros)
  return {
    totalPuntos: resultados.value.length,
    tiposUnicos: tiposUnicos.value.length,
    distanciaPromedio: distancias.reduce((sum, d) => sum + d, 0) / distancias.length,
    masCercano: Math.min(...distancias)
  }
}

const getProximityRank = (distancia) => {
  if (distancia <= 200) return 'rank-gold'
  if (distancia <= 500) return 'rank-silver'
  if (distancia <= 1000) return 'rank-bronze'
  return 'rank-default'
}

const getProximityClass = (distancia) => {
  if (distancia <= 200) return 'proximity-excellent'
  if (distancia <= 500) return 'proximity-good'
  if (distancia <= 1000) return 'proximity-average'
  return 'proximity-poor'
}

const getProximityWidth = (distancia) => {
  const maxDistancia = Math.max(...resultados.value.map(r => r.distancia_metros))
  return Math.max(20, 100 - (distancia / maxDistancia) * 80)
}

const getTipoClass = (tipo) => {
  const tipos = {
    'Restaurante': 'tipo-restaurant',
    'Tienda': 'tipo-store',
    'Farmacia': 'tipo-pharmacy',
    'Banco': 'tipo-bank',
    'Hospital': 'tipo-hospital',
    'Escuela': 'tipo-school',
    'Parque': 'tipo-park'
  }
  return tipos[tipo] || 'tipo-default'
}

const exportarCSV = () => {
  if (!resultadosFiltrados.value.length) return

  const headers = ['Ranking', 'Nombre', 'Tipo', 'Distancia (m)', 'Distancia (formateada)', 'Cliente', 'Radio (km)']
  const csvContent = [
    headers.join(','),
    ...resultadosFiltrados.value.map((punto, index) => [
      index + 1,
      `"${punto.nombre}"`,
      `"${punto.tipo}"`,
      punto.distancia_metros.toFixed(0),
      `"${formatearDistancia(punto.distancia_metros)}"`,
      `"${clienteSeleccionado.value?.nombre || ''}"`,
      radioKm.value
    ].join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `puntos_interes_cliente_${clienteSeleccionado.value?.id}_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// Click outside para cerrar dropdown
const handleClickOutside = (event) => {
  if (!event.target.closest('.search-container')) {
    mostrarDropdownCliente.value = false
  }
}

onMounted(() => {
  console.log('🚀 Punto de Interés inicializado')
  // Cargar clientes automáticamente al iniciar
  cargarClientes()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.search-section {
  margin-bottom: 2rem;
}

.search-card {
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: 16px;
  padding: 2rem;
  backdrop-filter: blur(10px);
}

.search-title {
  color: var(--text-primary);
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  text-align: center;
}

.input-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

@media (max-width: 768px) {
  .input-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}

.input-group {
  display: flex;
  flex-direction: column;
}

.input-label {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.search-container {
  position: relative;
}

.search-input {
  width: 100%;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: var(--shadow-lg);
  z-index: 99999;
  max-height: 300px;
  overflow-y: auto;
  margin-top: 4px;
}

.dropdown-header {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--border-color);
  background: var(--glass-bg);
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.dropdown-item {
  padding: 0.75rem 1rem;
  cursor: pointer;
  border-bottom: 1px solid var(--border-light);
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background: var(--glass-bg);
}

.dropdown-item:last-child {
  border-bottom: none;
}

.cliente-info {
  text-align: left;
}

.cliente-nombre {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.cliente-id {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.distance-input-group {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.distance-input {
  flex: 1;
}

.distance-unit {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.875rem;
}

.distance-suggestions {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.75rem;
  flex-wrap: wrap;
}

.distance-btn {
  padding: 0.375rem 0.75rem;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  color: var(--text-secondary);
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.distance-btn:hover {
  background: var(--glass-bg);
  border-color: var(--primary);
}

.distance-btn.active {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}

.search-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-search {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
  border: none;
  padding: 0.75rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 200px;
  justify-content: center;
}

.btn-search:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.btn-search:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-clear {
  background: var(--glass-bg);
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-clear:hover {
  background: var(--danger);
  color: white;
  border-color: var(--danger);
}

.selected-info {
  margin-bottom: 1.5rem;
}

.info-card {
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.info-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.info-subtitle {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.results-header h3 {
  color: var(--text-primary);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.results-header p {
  color: var(--text-secondary);
  margin: 0;
  flex: 1;
}

.btn-export {
  background: linear-gradient(135deg, var(--success), #0a8754);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-export:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.result-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  padding: 1rem;
  background: var(--glass-bg);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-value {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-top: 0.25rem;
}

.result-filters {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.filter-input {
  flex: 1;
  min-width: 200px;
}

.filter-select {
  min-width: 180px;
}

.rank-column {
  width: 60px;
  text-align: center;
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  margin: 0 auto;
}

.rank-gold { background: linear-gradient(135deg, #ffd700, #b8860b); color: #333; }
.rank-silver { background: linear-gradient(135deg, #c0c0c0, #808080); color: #333; }
.rank-bronze { background: linear-gradient(135deg, #cd7f32, #8b4513); color: white; }
.rank-default { background: var(--glass-bg); color: var(--text-secondary); border: 1px solid var(--border-color); }

.punto-info {
  text-align: left;
}

.punto-nombre {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.punto-id {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.tipo-badge {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.875rem;
  border: 1px solid transparent;
}

.tipo-restaurant { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; }
.tipo-store { background: linear-gradient(135deg, #06b6d4, #0891b2); color: white; }
.tipo-pharmacy { background: linear-gradient(135deg, #10b981, #059669); color: white; }
.tipo-bank { background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: white; }
.tipo-hospital { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; }
.tipo-school { background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; }
.tipo-park { background: linear-gradient(135deg, #22c55e, #16a34a); color: white; }
.tipo-default { background: var(--glass-bg); color: var(--text-secondary); border-color: var(--border-color); }

.distance-info {
  text-align: left;
}

.distance-value {
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 0.25rem;
}

.distance-meters {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.proximity-bar {
  width: 100%;
  height: 8px;
  background: var(--glass-bg);
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.proximity-fill {
  height: 100%;
  transition: width 0.8s ease;
  border-radius: 3px;
}

.proximity-excellent { background: linear-gradient(135deg, var(--success), #0a8754); }
.proximity-good { background: linear-gradient(135deg, var(--info), #0056b3); }
.proximity-average { background: linear-gradient(135deg, var(--warning), #d08c00); }
.proximity-poor { background: linear-gradient(135deg, var(--danger), #a02622); }

.welcome-state, .empty-state, .error-state {
  text-align: center;
  padding: 3rem 1rem;
}

.welcome-icon, .empty-icon, .error-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
  opacity: 0.6;
}

.welcome-state h3, .empty-state h3, .error-state h3 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.welcome-state p, .empty-state p, .error-state p {
  color: var(--text-secondary);
  max-width: 400px;
  margin: 0 auto 1.5rem;
}

.btn-retry {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-retry:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

.loading-spinner.small {
  width: 16px;
  height: 16px;
  border-width: 2px;
}
</style>

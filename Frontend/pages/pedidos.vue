<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">📦 Gestión de Pedidos</h1>
      <p class="page-subtitle">Administra y monitorea todos los pedidos del sistema</p>
    </div>

    <!-- Filtros y búsqueda -->
    <div class="filters-section">
      <div class="search-bar">
        <input 
          v-model="searchTerm" 
          type="text" 
          placeholder="Buscar por ID, cliente o estado..."
          class="search-input"
        />
        <button @click="buscarPedidos" class="search-btn">
          🔍 Buscar
        </button>
      </div>
      
      <div class="filter-options">
        <select v-model="filtroEstado" @change="aplicarFiltros" class="filter-select">
          <option value="">Todos los estados</option>
          <option value="pendiente">Pendiente</option>
          <option value="en_proceso">En Proceso</option>
          <option value="entregado">Entregado</option>
          <option value="cancelado">Cancelado</option>
        </select>
        
        <select v-model="filtroUrgencia" @change="aplicarFiltros" class="filter-select">
          <option value="">Todas las urgencias</option>
          <option value="alta">Alta</option>
          <option value="media">Media</option>
          <option value="baja">Baja</option>
        </select>
      </div>
    </div>

    <!-- Estadísticas rápidas -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <h3>{{ totalPedidos }}</h3>
          <p>Total Pedidos</p>
        </div>
      </div>
      <div class="stat-card pending">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <h3>{{ pedidosPendientes }}</h3>
          <p>Pendientes</p>
        </div>
      </div>
      <div class="stat-card progress">
        <div class="stat-icon">🚛</div>
        <div class="stat-content">
          <h3>{{ pedidosEnProceso }}</h3>
          <p>En Proceso</p>
        </div>
      </div>
      <div class="stat-card success">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>{{ pedidosEntregados }}</h3>
          <p>Entregados</p>
        </div>
      </div>
    </div>

    <!-- Tabla de pedidos -->
    <div class="table-section">
      <div class="table-header">
        <h2>📋 Lista de Pedidos</h2>
        <div class="table-actions">
          <button @click="cargarPedidos" :disabled="loading" class="refresh-btn">
            <span v-if="!loading">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
          <button @click="exportarPedidos" class="export-btn">
            📊 Exportar
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>Cargando pedidos...</p>
      </div>

      <div v-else-if="pedidosFiltrados.length === 0" class="no-data">
        <div class="no-data-icon">📭</div>
        <h3>No se encontraron pedidos</h3>
        <p>No hay pedidos que coincidan con los filtros seleccionados</p>
      </div>

      <div v-else class="table-container">
        <table class="pedidos-table">
          <thead>
            <tr>
              <th>ID Pedido</th>
              <th>Cliente</th>
              <th>Empresa</th>
              <th>Repartidor</th>
              <th>Estado</th>
              <th>Urgencia</th>
              <th>Total</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="pedido in pedidosFiltrados" :key="pedido.id_pedido" class="pedido-row">
              <td class="id-cell">
                <span class="pedido-id">#{{ pedido.id_pedido }}</span>
              </td>
              <td class="cliente-cell">
                <div class="cliente-info">
                  <span class="cliente-nombre">{{ pedido.cliente_nombre || `Cliente #${pedido.id_cliente}` }}</span>
                  <span class="cliente-id">ID: {{ pedido.id_cliente }}</span>
                </div>
              </td>
              <td class="empresa-cell">
                <span class="empresa-nombre">{{ pedido.empresa_nombre || `Empresa #${pedido.id_empresa}` }}</span>
              </td>
              <td class="repartidor-cell">
                <span class="repartidor-nombre">{{ pedido.repartidor_nombre || `Repartidor #${pedido.id_repartidor}` }}</span>
              </td>
              <td class="estado-cell">
                <span class="status-badge" :class="getEstadoClass(pedido.estado)">
                  {{ formatEstado(pedido.estado) }}
                </span>
              </td>
              <td class="urgencia-cell">
                <span class="urgencia-badge" :class="getUrgenciaClass(pedido.urgencia)">
                  {{ formatUrgencia(pedido.urgencia) }}
                </span>
              </td>
              <td class="total-cell">
                <span class="total-amount">${{ formatCurrency(pedido.total) }}</span>
              </td>
              <td class="fecha-cell">
                <span class="fecha">{{ formatDate(pedido.fecha_pedido) }}</span>
              </td>
              <td class="acciones-cell">
                <div class="action-buttons">
                  <button @click="verDetalle(pedido)" class="action-btn detail-btn" title="Ver detalles">
                    👁️
                  </button>
                  <button @click="editarPedido(pedido)" class="action-btn edit-btn" title="Editar">
                    ✏️
                  </button>
                  <button 
                    v-if="pedido.estado !== 'entregado'" 
                    @click="actualizarEstado(pedido)" 
                    class="action-btn update-btn" 
                    title="Actualizar estado"
                  >
                    🔄
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal de detalles (si se necesita) -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Detalles del Pedido #{{ selectedPedido?.id_pedido }}</h3>
          <button @click="closeModal" class="modal-close">✕</button>
        </div>
        <div class="modal-body">
          <!-- Aquí se mostrarían los detalles completos del pedido -->
          <pre>{{ JSON.stringify(selectedPedido, null, 2) }}</pre>
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
const loading = ref(false)
const pedidos = ref([])
const searchTerm = ref('')
const filtroEstado = ref('')
const filtroUrgencia = ref('')
const showModal = ref(false)
const selectedPedido = ref(null)

// Estadísticas computadas
const totalPedidos = computed(() => pedidos.value.length)
const pedidosPendientes = computed(() => 
  pedidos.value.filter(p => p.estado === 'pendiente').length
)
const pedidosEnProceso = computed(() => 
  pedidos.value.filter(p => p.estado === 'en_proceso').length
)
const pedidosEntregados = computed(() => 
  pedidos.value.filter(p => p.estado === 'entregado').length
)

// Pedidos filtrados
const pedidosFiltrados = computed(() => {
  let filtrados = pedidos.value

  // Filtro por búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtrados = filtrados.filter(pedido => 
      pedido.id_pedido.toString().includes(term) ||
      pedido.id_cliente.toString().includes(term) ||
      pedido.estado.toLowerCase().includes(term) ||
      (pedido.cliente_nombre && pedido.cliente_nombre.toLowerCase().includes(term))
    )
  }

  // Filtro por estado
  if (filtroEstado.value) {
    filtrados = filtrados.filter(pedido => pedido.estado === filtroEstado.value)
  }

  // Filtro por urgencia
  if (filtroUrgencia.value) {
    filtrados = filtrados.filter(pedido => pedido.urgencia === filtroUrgencia.value)
  }

  return filtrados
})

// Funciones principales
const cargarPedidos = async () => {
  loading.value = true
  try {
    console.log('🔄 Cargando pedidos desde:', `${API_ROUTES.PEDIDO}/`)
    const response = await $apiClient.get(`${API_ROUTES.PEDIDO}/`)
    console.log('✅ Respuesta de pedidos:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let pedidosData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Procesar datos de pedidos
    pedidos.value = pedidosData.map(pedido => ({
      id_pedido: pedido.id_pedido || pedido.id,
      id_cliente: pedido.id_cliente || pedido.cliente_id,
      id_empresa: pedido.id_empresa || pedido.empresa_id,
      id_repartidor: pedido.id_repartidor || pedido.repartidor_id,
      cliente_nombre: pedido.cliente_nombre || pedido.cliente?.nombre || `Cliente #${pedido.id_cliente || pedido.cliente_id}`,
      empresa_nombre: pedido.empresa_nombre || pedido.empresa?.nombre,
      repartidor_nombre: pedido.repartidor_nombre || pedido.repartidor?.nombre || `Repartidor #${pedido.id_repartidor || pedido.repartidor_id}`,
      estado: pedido.estado || 'pendiente',
      urgencia: pedido.urgencia || pedido.nivel_urgencia || 'media',
      total: pedido.total || pedido.precio_total || 0,
      fecha_pedido: pedido.fecha_pedido || pedido.fecha_creacion || pedido.created_at,
      direccion_entrega: pedido.direccion_entrega || pedido.direccion,
      descripcion: pedido.descripcion || ''
    }))
    
    console.log('📊 Pedidos procesados:', pedidos.value.length)
    console.log('📋 Muestra de pedido:', pedidos.value[0])
  } catch (error) {
    console.error('❌ Error al cargar pedidos:', error)
    if (error.response?.status === 403) {
      alert('No tienes permisos para ver los pedidos')
    } else if (error.response?.status === 401) {
      alert('Tu sesión ha expirado')
      navigateTo('/landing')
    } else {
      alert(`Error al cargar los pedidos: ${error.response?.data?.message || error.message}`)
    }
  } finally {
    loading.value = false
  }
}

const buscarPedidos = () => {
  // La búsqueda se hace automáticamente a través del computed
  console.log('Buscando:', searchTerm.value)
}

const aplicarFiltros = () => {
  console.log('Filtros aplicados:', { estado: filtroEstado.value, urgencia: filtroUrgencia.value })
}

const verDetalle = (pedido) => {
  selectedPedido.value = pedido
  showModal.value = true
}

const editarPedido = (pedido) => {
  // Implementar navegación a página de edición
  navigateTo(`/pedidos/editar/${pedido.id_pedido}`)
}

const actualizarEstado = async (pedido) => {
  // Implementar actualización de estado
  const nuevoEstado = prompt('Nuevo estado:', pedido.estado)
  if (nuevoEstado && nuevoEstado !== pedido.estado) {
    try {
      await $apiClient.put(`${API_ROUTES.PEDIDO}/${pedido.id_pedido}`, {
        ...pedido,
        estado: nuevoEstado
      })
      await cargarPedidos() // Recargar para ver cambios
      alert('Estado actualizado correctamente')
    } catch (error) {
      console.error('Error al actualizar estado:', error)
      alert('Error al actualizar el estado')
    }
  }
}

const exportarPedidos = () => {
  // Implementar exportación a CSV
  const csv = pedidosFiltrados.value.map(p => 
    `${p.id_pedido},${p.id_cliente},${p.estado},${p.total},${p.fecha_pedido}`
  ).join('\n')
  
  const blob = new Blob([`ID,Cliente,Estado,Total,Fecha\n${csv}`], { type: 'text/csv' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'pedidos.csv'
  a.click()
  window.URL.revokeObjectURL(url)
}

const closeModal = () => {
  showModal.value = false
  selectedPedido.value = null
}

// Funciones de formato
const formatEstado = (estado) => {
  const estados = {
    'pendiente': 'Pendiente',
    'en_proceso': 'En Proceso',
    'entregado': 'Entregado',
    'cancelado': 'Cancelado'
  }
  return estados[estado] || estado
}

const formatUrgencia = (urgencia) => {
  const urgencias = {
    'alta': 'Alta',
    'media': 'Media',
    'baja': 'Baja'
  }
  return urgencias[urgencia] || urgencia
}

const getEstadoClass = (estado) => {
  return {
    'pendiente': 'warning',
    'en_proceso': 'info',
    'entregado': 'success',
    'cancelado': 'danger'
  }[estado] || 'default'
}

const getUrgenciaClass = (urgencia) => {
  return {
    'alta': 'danger',
    'media': 'warning',
    'baja': 'success'
  }[urgencia] || 'default'
}

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('es-ES').format(amount || 0)
}

const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha'
  try {
    return new Date(dateString).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateString
  }
}

// Cargar datos al montar
onMounted(() => {
  cargarPedidos()
})
</script>

<style scoped>
.filters-section {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--border-color);
}

.search-bar {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.search-input {
  flex: 1;
  padding: var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  background: var(--background-secondary);
  color: var(--text-primary);
}

.search-btn {
  background: var(--accent-primary);
  color: white;
  border: none;
  padding: var(--spacing-md);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-btn:hover {
  background: var(--accent-hover);
}

.filter-options {
  display: flex;
  gap: var(--spacing-md);
}

.filter-select {
  padding: var(--spacing-sm) var(--spacing-md);
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius);
  background: var(--background-secondary);
  color: var(--text-primary);
}

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

.stat-card.pending {
  border-left: 4px solid #f59e0b;
}

.stat-card.progress {
  border-left: 4px solid #3b82f6;
}

.stat-card.success {
  border-left: 4px solid #10b981;
}

.table-section {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
}

.table-actions {
  display: flex;
  gap: var(--spacing-md);
}

.refresh-btn, .export-btn {
  padding: var(--spacing-sm) var(--spacing-md);
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn {
  background: var(--accent-primary);
  color: white;
}

.export-btn {
  background: var(--background-secondary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.pedidos-table {
  width: 100%;
  border-collapse: collapse;
}

.pedidos-table th {
  background: var(--background-secondary);
  padding: var(--spacing-md);
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  font-weight: 600;
  color: var(--text-primary);
}

.pedidos-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border-color);
}

.pedido-row:hover {
  background: var(--background-secondary);
}

.status-badge, .urgencia-badge {
  padding: 4px 8px;
  border-radius: var(--border-radius);
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.warning, .urgencia-badge.warning {
  background: #fef3c7;
  color: #92400e;
}

.status-badge.info {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge.success, .urgencia-badge.success {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.danger, .urgencia-badge.danger {
  background: #fee2e2;
  color: #991b1b;
}

.action-buttons {
  display: flex;
  gap: var(--spacing-xs);
}

.action-btn {
  background: none;
  border: 1px solid var(--border-color);
  padding: 4px 8px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: var(--background-secondary);
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: var(--text-secondary);
}

.modal-body {
  padding: var(--spacing-lg);
}

.loading-container, .no-data {
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

.no-data-icon {
  font-size: 3rem;
  margin-bottom: var(--spacing-md);
}
</style>

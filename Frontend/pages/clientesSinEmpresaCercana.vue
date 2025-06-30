<template>
  <div class="page-container">
    <div class="header-section">
      <h1 class="title">🏢 Clientes sin Empresa Cercana</h1>
      <p class="subtitle">
        Análisis automático de clientes que no tienen cobertura empresarial
      </p>
    </div>

    <!-- Loading State -->
    <div v-if="cargando" class="loading-container">
      <div class="loading-card">
        <div class="loading-spinner">
          <div class="spinner-border" role="status"></div>
        </div>
        <h3>🔍 Analizando Datos</h3>
        <p>Identificando clientes sin cobertura empresarial...</p>
        <div class="progress-bar">
          <div class="progress-fill"></div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <transition name="fade-slide">
      <div v-if="error" class="error-container">
        <div class="error-card">
          <div class="error-icon">❌</div>
          <h3>Error en el análisis</h3>
          <p>{{ error }}</p>
          <button @click="obtenerClientesSinCobertura" class="retry-btn">
            🔄 Reintentar
          </button>
        </div>
      </div>
    </transition>

    <!-- Results -->
    <transition name="fade-slide">
      <div v-if="!cargando && !error && consultado" class="results-container">
        <!-- Summary Card -->
        <div class="summary-card">
          <div class="summary-stats">
            <div class="stat-item">
              <div class="stat-number">{{ clientes.length }}</div>
              <div class="stat-label">Clientes Sin Cobertura</div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <div class="stat-number">{{ ((clientes.length / totalClientes) * 100).toFixed(1) }}%</div>
              <div class="stat-label">Del Total</div>
            </div>
          </div>
          
          <div v-if="clientes.length === 0" class="no-results">
            <div class="no-results-icon">✅</div>
            <h3>¡Excelente!</h3>
            <p>Todos los clientes tienen cobertura empresarial</p>
          </div>
        </div>

        <!-- Results Table -->
        <div v-if="clientes.length > 0" class="table-container">
          <div class="table-header">
            <h2>📋 Lista de Clientes Afectados</h2>
            <div class="table-actions">
              <button @click="exportarDatos" class="export-btn">
                📊 Exportar
              </button>
            </div>
          </div>
          
          <div class="table-wrapper">
            <table class="table modern-table">
              <thead>
                <tr>
                  <th style="width: 10%; text-align: center;">
                    <span class="th-content">
                      🆔 ID Cliente
                    </span>
                  </th>
                  <th style="width: 25%;">
                    <span class="th-content">
                      👤 Nombre
                    </span>
                  </th>
                  <th style="width: 30%;">
                    <span class="th-content">
                      📧 Correo
                    </span>
                  </th>
                  <th style="width: 25%;">
                    <span class="th-content">
                      🏠 Dirección
                    </span>
                  </th>
                  <th style="width: 10%; text-align: center;">
                    <span class="th-content">
                      🎯 Acciones
                    </span>
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(cliente, index) in clientes" :key="cliente.id_cliente" 
                    class="table-row" 
                    :style="{ animationDelay: `${index * 0.1}s` }">
                  <td class="id-cell">{{ cliente.id_cliente }}</td>
                  <td class="name-cell">
                    <div class="client-name">{{ cliente.nombre }}</div>
                  </td>
                  <td class="email-cell">{{ cliente.correo }}</td>
                  <td class="address-cell">{{ cliente.direccion }}</td>
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
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes'

definePageMeta({
  middleware: 'auth'
})

const clientes = ref([])
const cargando = ref(true) // Iniciar cargando
const error = ref('')
const consultado = ref(false)
const totalClientes = ref(0)
const { $apiClient } = useNuxtApp()

const obtenerClientesSinCobertura = async () => {
  clientes.value = []
  error.value = ''
  cargando.value = true
  consultado.value = false
  
  try {
    // Simular tiempo de análisis para mejor UX
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    console.log('🔄 Obteniendo clientes sin cobertura...')
    const response = await $apiClient.get(`${API_ROUTES.EMPRESA}/clientes-sin-empresa-cercana`)
    console.log('✅ Respuesta del backend:', response.data)
    
    // Procesar respuesta del backend
    let clientesData = Array.isArray(response.data) ? response.data : [response.data]
    
    // Si los datos vienen con información completa, usarlos directamente
    if (clientesData.length > 0 && clientesData[0].correo && clientesData[0].direccion) {
      clientes.value = clientesData.map(cliente => ({
        id_cliente: cliente.id_cliente || cliente.id,
        nombre: cliente.nombre || `Cliente ${cliente.id_cliente || cliente.id}`,
        correo: cliente.correo || cliente.email || 'Sin correo especificado',
        direccion: cliente.direccion || 'Sin dirección especificada',
        telefono: cliente.telefono || 'Sin teléfono'
      }))
    } else {
      // Si solo vienen IDs o datos incompletos, obtener información completa
      const clienteIds = clientesData.map(item => 
        typeof item === 'object' ? (item.id_cliente || item.id) : item
      )
      
      console.log('📋 IDs de clientes sin cobertura:', clienteIds)
      
      // Obtener todos los clientes para filtrar los que no tienen cobertura
      const todosClientesResponse = await $apiClient.get(`${API_ROUTES.CLIENTE}/`)
      const todosClientes = todosClientesResponse.data
      console.log('👥 Todos los clientes obtenidos:', todosClientes.length)
      
      // Filtrar solo los clientes que están en la lista sin cobertura
      clientes.value = todosClientes
        .filter(cliente => clienteIds.includes(cliente.id_cliente))
        .map(cliente => ({
          id_cliente: cliente.id_cliente || cliente.id,
          nombre: cliente.nombre || `Cliente ${cliente.id_cliente || cliente.id}`,
          correo: cliente.correo || cliente.email || 'Sin correo especificado',
          direccion: cliente.direccion || 'Sin dirección especificada',
          telefono: cliente.telefono || 'Sin teléfono'
        }))
    }
    
    // Obtener total de clientes para el porcentaje (si no lo obtuvimos ya)
    if (!totalClientes.value) {
      try {
        const todosClientesResponse = await $apiClient.get(`${API_ROUTES.CLIENTE}/`)
        totalClientes.value = todosClientesResponse.data.length
      } catch (err) {
        console.warn('No se pudo obtener el total de clientes para el porcentaje')
        totalClientes.value = clientes.value.length // fallback
      }
    }
    
    console.log('📊 Clientes sin cobertura procesados:', clientes.value.length)
    consultado.value = true
  } catch (err) {
    error.value = 'Error al obtener los clientes sin cobertura. Intenta nuevamente.'
    console.error('❌ Error:', err)
  } finally {
    cargando.value = false
  }
}

const verDetalles = (cliente) => {
  // Implementar modal o navegación a detalles
  alert(`Ver detalles de ${cliente.nombre}:\n\nID: ${cliente.id_cliente}\nCorreo: ${cliente.correo}\nDirección: ${cliente.direccion}`)
}

const exportarDatos = () => {
  // Usar punto y coma como separador (estándar europeo)
  const csv = "ID;Nombre;Correo;Dirección\n" + 
    clientes.value.map(c => 
      `${c.id_cliente};"${c.nombre || 'Sin nombre'}";"${c.correo || 'Sin correo'}";"${c.direccion || 'Sin dirección'}"`
    ).join('\n')
  
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'clientes-sin-cobertura.csv'
  a.click()
  window.URL.revokeObjectURL(url)
}

// Cargar datos automáticamente al montar el componente
onMounted(() => {
  obtenerClientesSinCobertura()
})
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-lg);
}

.header-section {
  text-align: center;
  margin-bottom: var(--spacing-2xl);
}

.subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin-top: var(--spacing-md);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-card {
  background: var(--background-card);
  padding: var(--spacing-2xl);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-lg);
  text-align: center;
  max-width: 400px;
  border: 1px solid var(--border-color);
}

.loading-spinner {
  margin-bottom: var(--spacing-lg);
}

.spinner-border {
  width: 3rem;
  height: 3rem;
  border-width: 4px;
  border-color: var(--primary-color);
  border-right-color: transparent;
  animation: spin 1s linear infinite;
}

.loading-card h3 {
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.loading-card p {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-lg);
}

.progress-bar {
  width: 100%;
  height: 4px;
  background: var(--background-accent);
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--primary-color), var(--accent-color));
  border-radius: 2px;
  animation: progressMove 2s ease-in-out infinite;
}

@keyframes progressMove {
  0% { width: 0%; transform: translateX(-100%); }
  50% { width: 60%; transform: translateX(0%); }
  100% { width: 100%; transform: translateX(0%); }
}

.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.error-card {
  background: var(--background-card);
  padding: var(--spacing-2xl);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-lg);
  text-align: center;
  max-width: 400px;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.error-icon {
  font-size: 3rem;
  margin-bottom: var(--spacing-md);
}

.error-card h3 {
  color: var(--error-color);
  margin-bottom: var(--spacing-sm);
}

.error-card p {
  color: var(--text-secondary);
  margin-bottom: var(--spacing-lg);
}

.retry-btn {
  background: var(--error-color);
  color: white;
  border: none;
  padding: var(--spacing-sm) var(--spacing-lg);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.retry-btn:hover {
  background: var(--error-dark);
  transform: translateY(-2px);
}

.results-container {
  animation: slideInUp 0.6s ease-out;
}

.summary-card {
  background: var(--background-card);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-lg);
  margin-bottom: var(--spacing-xl);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.summary-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  color: white;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: var(--spacing-xs);
}

.stat-label {
  font-size: 0.875rem;
  opacity: 0.9;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.stat-divider {
  width: 1px;
  height: 60px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 var(--spacing-xl);
}

.no-results {
  padding: var(--spacing-2xl);
  text-align: center;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-lg);
}

.no-results h3 {
  color: var(--success-color);
  margin-bottom: var(--spacing-sm);
}

.no-results p {
  color: var(--text-secondary);
}

.table-container {
  background: var(--background-card);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-xl);
  background: var(--background-accent);
  border-bottom: 1px solid var(--border-color);
}

.table-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.table-actions {
  display: flex;
  gap: var(--spacing-sm);
}

.export-btn {
  background: var(--success-color);
  color: white;
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: 0.875rem;
}

.export-btn:hover {
  background: var(--success-dark);
  transform: translateY(-2px);
}

.table-wrapper {
  overflow-x: auto;
}

.modern-table {
  margin: 0;
  border-collapse: collapse;
}

.modern-table th {
  padding: var(--spacing-lg);
  background: var(--background-secondary);
  border-bottom: 2px solid var(--primary-color);
  font-weight: 600;
  text-align: left;
}

.th-content {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

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

.modern-table td {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-color);
  vertical-align: middle;
  text-align: left;
}

.id-cell {
  font-weight: 600;
  color: var(--primary-color);
  text-align: center;
  width: 10%;
}

.name-cell {
  font-weight: 600;
  width: 25%;
  padding-left: var(--spacing-lg);
}

.client-name {
  color: var(--text-primary);
  display: flex;
  align-items: center;
  font-size: 1rem;
  text-transform: capitalize;
  letter-spacing: 0.025em;
}

.email-cell {
  color: var(--text-secondary);
  font-size: 0.875rem;
  width: 30%;
  word-break: break-word;
}

.address-cell {
  color: var(--text-secondary);
  font-size: 0.875rem;
  width: 25%;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.actions-cell {
  text-align: center;
  width: 10%;
}

.action-btn {
  padding: var(--spacing-xs) var(--spacing-sm);
  border: none;
  border-radius: var(--border-radius-sm);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-size: 0.8rem;
  font-weight: 500;
}

.detail-btn {
  background: var(--info-color);
  color: white;
}

.detail-btn:hover {
  background: var(--info-dark);
  transform: scale(1.05);
}

/* Animaciones */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.5s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
}

/* Responsive */
@media (max-width: 768px) {
  .page-container {
    padding: var(--spacing-md);
  }
  
  .summary-stats {
    flex-direction: column;
    gap: var(--spacing-lg);
  }
  
  .stat-divider {
    width: 60px;
    height: 1px;
    margin: 0;
  }
  
  .table-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
  }
  
  .modern-table th,
  .modern-table td {
    padding: var(--spacing-sm);
  }
  
  .address-cell {
    max-width: 150px;
  }
}

@media (max-width: 480px) {
  .loading-card,
  .error-card {
    margin: 0 var(--spacing-md);
    padding: var(--spacing-lg);
  }
  
  .stat-number {
    font-size: 2rem;
  }
}
</style>
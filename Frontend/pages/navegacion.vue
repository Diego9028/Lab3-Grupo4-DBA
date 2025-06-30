<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">🔍 Análisis de Navegación</h1>
      <p class="page-subtitle">Clientes que buscaron pero no realizaron pedidos</p>
    </div>

    <!-- Estadísticas principales -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ clientesSinCompra.length }}</h3>
          <p>Clientes sin Conversión</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📉</div>
        <div class="stat-content">
          <h3>{{ tasaConversion }}%</h3>
          <p>Tasa de Conversión</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">💡</div>
        <div class="stat-content">
          <h3>{{ oportunidadesPerdidas }}</h3>
          <p>Oportunidades Perdidas</p>
        </div>
      </div>
    </div>

    <!-- Información sobre el análisis -->
    <div class="info-section">
      <div class="info-card">
        <h2>📊 Análisis de Comportamiento</h2>
        <p>
          Este análisis identifica clientes que realizaron <strong>búsquedas</strong> en la plataforma 
          pero <strong>no confirmaron ningún pedido</strong>. Esta información es valiosa para:
        </p>
        <ul class="info-list">
          <li>🎯 Identificar problemas en el embudo de conversión</li>
          <li>💰 Detectar oportunidades de mejora en ventas</li>
          <li>🔍 Analizar patrones de abandono de carrito</li>
          <li>📈 Optimizar la experiencia de usuario</li>
        </ul>
      </div>
    </div>

    <!-- Controles -->
    <div class="controls-section">
      <button @click="cargarDatos" :disabled="loading" class="btn-primary">
        <span v-if="!loading">🔄 Actualizar Datos</span>
        <span v-else>⏳ Cargando...</span>
      </button>
      
      <button @click="exportarDatos" :disabled="clientesSinCompra.length === 0" class="btn-secondary">
        📊 Exportar Lista
      </button>
    </div>

    <!-- Resultados -->
    <div v-if="!loading && !error && consultado" class="results-section">
      <div v-if="clientesSinCompra.length === 0" class="no-results">
        <div class="no-results-icon">🎉</div>
        <h3>¡Excelente conversión!</h3>
        <p>Todos los clientes que realizaron búsquedas también confirmaron pedidos.</p>
      </div>

      <div v-else class="results-content">
        <div class="results-header">
          <h2>📋 Lista de Clientes sin Conversión</h2>
          <p>{{ clientesSinCompra.length }} clientes identificados</p>
        </div>

        <div class="table-container">
          <table class="results-table">
            <thead>
              <tr>
                <th>ID Cliente</th>
                <th>Estado</th>
                <th>Acciones Sugeridas</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="clienteId in clientesSinCompra" :key="clienteId" class="table-row">
                <td class="id-cell">
                  <span class="client-id">{{ clienteId }}</span>
                </td>
                <td class="status-cell">
                  <span class="status-badge sin-conversion">Sin Conversión</span>
                </td>
                <td class="actions-cell">
                  <button @click="verDetalleCliente(clienteId)" class="action-btn detail-btn">
                    👤 Ver Perfil
                  </button>
                  <button @click="enviarOferta(clienteId)" class="action-btn offer-btn">
                    💌 Enviar Oferta
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Estados de carga y error -->
    <div v-if="loading" class="loading-section">
      <div class="loading-card">
        <div class="loading-spinner"></div>
        <h3>Analizando patrones de navegación...</h3>
        <p>Procesando eventos de búsqueda y pedidos...</p>
      </div>
    </div>

    <div v-if="error" class="error-section">
      <div class="error-card">
        <h3>❌ Error al cargar datos</h3>
        <p>{{ error }}</p>
        <button @click="cargarDatos" class="btn-primary">
          🔄 Reintentar
        </button>
      </div>
    </div>

    <!-- Recomendaciones -->
    <div v-if="clientesSinCompra.length > 0" class="recommendations-section">
      <div class="recommendations-card">
        <h3>💡 Recomendaciones de Mejora</h3>
        <div class="recommendations-grid">
          <div class="recommendation-item">
            <h4>🎯 Retargeting</h4>
            <p>Implementar campañas dirigidas a estos clientes con ofertas personalizadas</p>
          </div>
          <div class="recommendation-item">
            <h4>🔍 Análisis UX</h4>
            <p>Revisar la experiencia de usuario en el proceso de compra</p>
          </div>
          <div class="recommendation-item">
            <h4>💬 Feedback</h4>
            <p>Contactar directamente para entender las barreras de conversión</p>
          </div>
          <div class="recommendation-item">
            <h4>🏷️ Incentivos</h4>
            <p>Ofrecer descuentos o promociones especiales para la primera compra</p>
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
import API_ROUTES from '../src/api-routes'

const { $apiClient } = useNuxtApp()

// Estado reactivo
const clientesSinCompra = ref([])
const loading = ref(false)
const error = ref('')
const consultado = ref(false)

// Computadas
const tasaConversion = computed(() => {
  // Estimación basada en datos hipotéticos
  const totalClientesConBusquedas = 1000
  const clientesConConversion = totalClientesConBusquedas - clientesSinCompra.value.length
  return totalClientesConBusquedas > 0 ? 
    Math.round((clientesConConversion / totalClientesConBusquedas) * 100) : 0
})

const oportunidadesPerdidas = computed(() => {
  return clientesSinCompra.value.length
})

// Funciones
const cargarDatos = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('🔄 Obteniendo datos de navegación desde:', `${API_ROUTES.NAVEGACION}/sin-pedido`)
    const response = await $apiClient.get(`${API_ROUTES.NAVEGACION}/sin-pedido`)
    
    console.log('✅ Respuesta de navegación:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let navegacionData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    clientesSinCompra.value = navegacionData
    consultado.value = true
    
    console.log('📊 Clientes sin compra procesados:', clientesSinCompra.value.length)
    
  } catch (err) {
    console.error('❌ Error al obtener navegación:', err)
    if (err.response?.status === 403) {
      error.value = 'No tienes permisos para consultar datos de navegación'
    } else if (err.response?.status === 404) {
      error.value = 'Endpoint de navegación no encontrado'
    } else if (err.response?.status === 401) {
      error.value = 'Tu sesión ha expirado'
      setTimeout(() => navigateTo('/landing'), 2000)
    } else {
      error.value = `Error al obtener datos de navegación: ${err.response?.data?.message || err.message}`
    }
  } finally {
    loading.value = false
  }
}

const exportarDatos = () => {
  const data = {
    clientes_sin_conversion: clientesSinCompra.value,
    total_clientes: clientesSinCompra.value.length,
    tasa_conversion_estimada: tasaConversion.value,
    fecha_reporte: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { 
    type: 'application/json' 
  })
  
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `clientes_sin_conversion_${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
}

const verDetalleCliente = (clienteId) => {
  // Navegar a la página de detalle del cliente
  navigateTo(`/cliente?id=${clienteId}`)
}

const enviarOferta = (clienteId) => {
  alert(`Funcionalidad pendiente: Enviar oferta personalizada al cliente ${clienteId}`)
}

// Cargar datos al montar
onMounted(cargarDatos)
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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
  transition: all var(--transition-medium);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 2.5rem;
  opacity: 0.8;
}

.stat-content h3 {
  font-size: 2rem;
  font-weight: 700;
  margin: 0 0 0.5rem 0;
  color: var(--accent-primary);
}

.stat-content p {
  margin: 0;
  opacity: 0.8;
  font-size: 0.9rem;
}

.info-section {
  margin-bottom: var(--spacing-xl);
}

.info-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  border-left: 4px solid var(--accent-primary);
}

.info-card h2 {
  margin-top: 0;
  color: var(--text-primary);
}

.info-list {
  list-style: none;
  padding: 0;
}

.info-list li {
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.info-list li:last-child {
  border-bottom: none;
}

.controls-section {
  display: flex;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-xl);
  flex-wrap: wrap;
}

.btn-primary,
.btn-secondary {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--border-radius-md);
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all var(--transition-medium);
}

.btn-primary {
  background: var(--accent-primary);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--accent-secondary);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: var(--background-secondary);
  color: var(--text-primary);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.results-section {
  margin-bottom: var(--spacing-xl);
}

.no-results {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-md);
}

.no-results h3 {
  color: var(--accent-primary);
  margin: 0 0 var(--spacing-sm) 0;
}

.results-content {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.results-header {
  padding: var(--spacing-lg);
  background: var(--background-secondary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.results-header h2 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-primary);
}

.results-header p {
  margin: 0;
  opacity: 0.8;
}

.table-container {
  overflow-x: auto;
}

.results-table {
  width: 100%;
  border-collapse: collapse;
}

.results-table th {
  background: var(--background-secondary);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.table-row {
  transition: background-color var(--transition-fast);
}

.table-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.results-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.client-id {
  font-family: 'Courier New', monospace;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius-sm);
  font-weight: 600;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: var(--border-radius-sm);
  font-size: 0.85rem;
  font-weight: 600;
}

.status-badge.sin-conversion {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.actions-cell {
  display: flex;
  gap: var(--spacing-sm);
}

.action-btn {
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius-sm);
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.detail-btn {
  background: var(--accent-primary);
  color: white;
}

.detail-btn:hover {
  background: var(--accent-secondary);
}

.offer-btn {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.offer-btn:hover {
  background: rgba(34, 197, 94, 0.3);
}

.loading-section,
.error-section {
  margin-bottom: var(--spacing-xl);
}

.loading-card,
.error-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
}

.error-card {
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.error-card h3 {
  color: #ef4444;
  margin-top: 0;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-left: 4px solid var(--accent-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto var(--spacing-md) auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.recommendations-section {
  margin-top: var(--spacing-xl);
}

.recommendations-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  border-left: 4px solid #f59e0b;
}

.recommendations-card h3 {
  margin-top: 0;
  color: var(--text-primary);
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-lg);
}

.recommendation-item {
  background: rgba(255, 255, 255, 0.05);
  padding: var(--spacing-md);
  border-radius: var(--border-radius-md);
}

.recommendation-item h4 {
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--accent-primary);
}

.recommendation-item p {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.8;
}

/* Responsive */
@media (max-width: 768px) {
  .stats-grid,
  .recommendations-grid {
    grid-template-columns: 1fr;
  }
  
  .controls-section {
    flex-direction: column;
  }
  
  .actions-cell {
    flex-direction: column;
  }
}
</style>

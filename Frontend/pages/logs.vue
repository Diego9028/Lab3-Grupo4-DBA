<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">📊 Logs del Sistema</h1>
      <p class="page-subtitle">Análisis de cambios rápidos en pedidos</p>
    </div>

    <!-- Estadísticas principales -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">⚡</div>
        <div class="stat-content">
          <h3>{{ cambiosRapidos }}</h3>
          <p>Pedidos con Cambios Rápidos</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <h3>{{ porcentajeCambiosRapidos }}%</h3>
          <p>Porcentaje del Total</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">⏱️</div>
        <div class="stat-content">
          <h3>≤ 10 min</h3>
          <p>Ventana de Cambios</p>
        </div>
      </div>
    </div>

    <!-- Información sobre el análisis -->
    <div class="info-section">
      <div class="info-card">
        <h2>🔍 Análisis de Cambios Rápidos</h2>
        <p>
          Este análisis identifica pedidos que experimentaron <strong>4 o más cambios de estado</strong> 
          en una ventana de <strong>10 minutos o menos</strong>. Esto puede indicar:
        </p>
        <ul class="info-list">
          <li>⚠️ Problemas operacionales en el sistema</li>
          <li>🔄 Procesos de corrección o ajuste automático</li>
          <li>📱 Interacciones frecuentes del usuario</li>
          <li>🚨 Posibles errores en la lógica de negocio</li>
        </ul>
      </div>
    </div>

    <!-- Acciones y controles -->
    <div class="actions-section">
      <button @click="actualizarDatos" :disabled="loading" class="btn-primary">
        <span v-if="!loading">🔄 Actualizar Datos</span>
        <span v-else>⏳ Cargando...</span>
      </button>
      
      <button @click="exportarDatos" class="btn-secondary">
        📊 Exportar Reporte
      </button>
    </div>

    <!-- Mensajes de estado -->
    <div v-if="error" class="error-section">
      <div class="error-card">
        <h3>❌ Error al cargar datos</h3>
        <p>{{ error }}</p>
        <button @click="actualizarDatos" class="btn-primary">
          🔄 Reintentar
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-section">
      <div class="loading-card">
        <div class="loading-spinner"></div>
        <h3>Analizando logs del sistema...</h3>
        <p>Procesando eventos de cambios de estado...</p>
      </div>
    </div>

    <!-- Información técnica -->
    <div class="technical-info">
      <h3>📋 Información Técnica</h3>
      <div class="info-grid">
        <div class="info-item">
          <strong>Fuente de Datos:</strong> Colección MongoDB "logs_pedidos"
        </div>
        <div class="info-item">
          <strong>Algoritmo:</strong> Función JavaScript en agregación MongoDB
        </div>
        <div class="info-item">
          <strong>Criterio:</strong> 4+ eventos en ventana de 10 minutos
        </div>
        <div class="info-item">
          <strong>Última Actualización:</strong> {{ ultimaActualizacion }}
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
const cambiosRapidos = ref(0)
const loading = ref(false)
const error = ref('')
const ultimaActualizacion = ref('')

// Computadas
const porcentajeCambiosRapidos = computed(() => {
  // Estimación basada en un total hipotético de pedidos
  const totalEstimado = 1000
  return cambiosRapidos.value > 0 ? 
    Math.round((cambiosRapidos.value / totalEstimado) * 100) : 0
})

// Funciones
const obtenerDatos = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('🔄 Obteniendo datos de logs desde:', `${API_ROUTES.LOGS}/cambios-rapidos`)
    const response = await $apiClient.get(`${API_ROUTES.LOGS}/cambios-rapidos`)
    
    console.log('✅ Respuesta de logs:', response.data)
    
    // Procesar la respuesta
    if (typeof response.data === 'number') {
      cambiosRapidos.value = response.data
    } else if (response.data && typeof response.data.count === 'number') {
      cambiosRapidos.value = response.data.count
    } else if (response.data && Array.isArray(response.data)) {
      cambiosRapidos.value = response.data.length
    } else {
      cambiosRapidos.value = 0
    }
    
    ultimaActualizacion.value = new Date().toLocaleString()
    
    console.log('📊 Cambios rápidos procesados:', cambiosRapidos.value)
    
  } catch (err) {
    console.error('❌ Error al obtener logs:', err)
    if (err.response?.status === 403) {
      error.value = 'No tienes permisos para consultar datos de logs'
    } else if (err.response?.status === 404) {
      error.value = 'Endpoint de logs no encontrado'
    } else if (err.response?.status === 401) {
      error.value = 'Tu sesión ha expirado'
      setTimeout(() => navigateTo('/landing'), 2000)
    } else {
      error.value = `Error al obtener datos de logs: ${err.response?.data?.message || err.message}`
    }
  } finally {
    loading.value = false
  }
}

const actualizarDatos = () => {
  obtenerDatos()
}

const exportarDatos = () => {
  const data = {
    pedidos_con_cambios_rapidos: cambiosRapidos.value,
    porcentaje_estimado: porcentajeCambiosRapidos.value,
    fecha_reporte: new Date().toISOString(),
    criterio: '4+ cambios en 10 minutos'
  }
  
  const blob = new Blob([JSON.stringify(data, null, 2)], { 
    type: 'application/json' 
  })
  
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `logs_cambios_rapidos_${new Date().toISOString().split('T')[0]}.json`
  a.click()
  URL.revokeObjectURL(url)
}

// Cargar datos al montar
onMounted(obtenerDatos)
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

.actions-section {
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

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
}

.error-section {
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

.loading-section {
  margin-bottom: var(--spacing-xl);
}

.loading-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
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

.technical-info {
  background: var(--background-secondary);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  margin-top: var(--spacing-xl);
}

.technical-info h3 {
  margin-top: 0;
  color: var(--text-primary);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: var(--spacing-md);
}

.info-item {
  padding: var(--spacing-sm);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--border-radius-sm);
  font-size: 0.9rem;
}

.info-item strong {
  color: var(--accent-primary);
}

/* Responsive */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-section {
    flex-direction: column;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>

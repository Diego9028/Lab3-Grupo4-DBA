<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">🚨 Gestión de Urgencias</h1>
      <p class="page-subtitle">Análisis de pedidos urgentes y medios de pago</p>
    </div>

    <!-- Estadísticas principales -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">⚡</div>
        <div class="stat-content">
          <h3>{{ totalUrgencias }}</h3>
          <p>Total Urgencias</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">💳</div>
        <div class="stat-content">
          <h3>{{ medioMasUsado?.nombre || 'N/A' }}</h3>
          <p>Medio de Pago Preferido</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <h3>{{ porcentajeUrgentes }}%</h3>
          <p>Porcentaje de Urgencia Alta</p>
        </div>
      </div>
    </div>

    <!-- Tabs de análisis -->
    <div class="analysis-tabs">
      <button 
        @click="activeTab = 'lista'" 
        :class="['tab-btn', { active: activeTab === 'lista' }]"
      >
        📋 Lista de Urgencias
      </button>
      <button 
        @click="activeTab = 'pagos'" 
        :class="['tab-btn', { active: activeTab === 'pagos' }]"
      >
        💳 Medios de Pago
      </button>
      <button 
        @click="activeTab = 'crear'" 
        :class="['tab-btn', { active: activeTab === 'crear' }]"
      >
        ➕ Crear Urgencia
      </button>
    </div>

    <!-- Contenido de tabs -->
    <div class="tab-content">
      <!-- Lista de urgencias -->
      <div v-if="activeTab === 'lista'" class="urgencias-section">
        <div class="section-header">
          <h2>📋 Lista de Urgencias</h2>
          <button @click="cargarUrgencias" :disabled="loading" class="refresh-btn">
            <span v-if="!loading">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>

        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Cargando urgencias...</p>
        </div>

        <div v-else-if="error" class="error-container">
          <div class="error-card">
            <h3>❌ Error al cargar datos</h3>
            <p>{{ error }}</p>
            <button @click="cargarUrgencias" class="btn-primary">
              🔄 Reintentar
            </button>
          </div>
        </div>

        <div v-else-if="urgencias.length === 0" class="no-data">
          <div class="no-data-icon">📭</div>
          <h3>No hay urgencias registradas</h3>
          <p>Aún no se han creado urgencias en el sistema</p>
        </div>

        <div v-else class="urgencias-table-container">
          <table class="urgencias-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nivel</th>
                <th>Descripción</th>
                <th>Tiempo Límite</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="urgencia in urgencias" :key="urgencia.id_urgencia" class="urgencia-row">
                <td class="id-cell">
                  <span class="urgencia-id">#{{ urgencia.id_urgencia }}</span>
                </td>
                <td class="nivel-cell">
                  <span class="nivel-badge" :class="getNivelClass(urgencia.nivel)">
                    {{ getNivelIcon(urgencia.nivel) }} {{ urgencia.nivel }}
                  </span>
                </td>
                <td class="descripcion-cell">
                  <div class="descripcion-content">
                    <strong>{{ urgencia.nombre || 'Sin nombre' }}</strong>
                    <p>{{ urgencia.descripcion || 'Sin descripción' }}</p>
                  </div>
                </td>
                <td class="tiempo-cell">
                  <span class="tiempo-limite">{{ urgencia.tiempo_limite || 'No definido' }}</span>
                </td>
                <td class="estado-cell">
                  <span class="estado-badge" :class="getEstadoClass(urgencia.activo)">
                    {{ urgencia.activo ? 'Activa' : 'Inactiva' }}
                  </span>
                </td>
                <td class="actions-cell">
                  <button @click="editarUrgencia(urgencia)" class="action-btn edit-btn">
                    ✏️ Editar
                  </button>
                  <button @click="eliminarUrgencia(urgencia.id_urgencia)" class="action-btn delete-btn">
                    🗑️ Eliminar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Análisis de medios de pago -->
      <div v-if="activeTab === 'pagos'" class="pagos-section">
        <div class="section-header">
          <h2>💳 Medios de Pago en Urgencias Altas</h2>
          <button @click="cargarMediosPago" :disabled="loadingPagos" class="refresh-btn">
            <span v-if="!loadingPagos">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>

        <div class="info-section">
          <div class="info-card">
            <h3>📊 Análisis de Preferencias</h3>
            <p>
              Este análisis muestra qué medios de pago prefieren los clientes cuando realizan 
              pedidos con <strong>urgencia alta</strong>. Esta información es valiosa para:
            </p>
            <ul class="info-list">
              <li>🎯 Optimizar opciones de pago en situaciones urgentes</li>
              <li>💰 Negociar mejores tarifas con proveedores populares</li>
              <li>📱 Mejorar la experiencia de checkout rápido</li>
              <li>📈 Identificar oportunidades de integración</li>
            </ul>
          </div>
        </div>

        <div v-if="loadingPagos" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Analizando medios de pago...</p>
        </div>

        <div v-else-if="mediosPago.length === 0" class="no-data">
          <div class="no-data-icon">💳</div>
          <h3>Sin datos de medios de pago</h3>
          <p>No se encontraron datos de medios de pago para urgencias altas</p>
        </div>

        <div v-else class="pagos-results">
          <div class="pagos-chart">
            <h3>📊 Distribución de Medios de Pago</h3>
            <div class="chart-container">
              <div v-for="(medio, index) in mediosPago" :key="index" class="chart-bar">
                <div class="bar-container">
                  <div 
                    class="bar-fill" 
                    :style="{ height: getBarHeight(medio.total_uso) + '%' }"
                    :class="getBarClass(index)"
                  ></div>
                </div>
                <div class="bar-label">
                  <strong>{{ medio.nombre_medio_pago }}</strong>
                  <span class="bar-value">{{ medio.total_uso }} usos</span>
                </div>
              </div>
            </div>
          </div>

          <div class="pagos-table">
            <table class="medios-table">
              <thead>
                <tr>
                  <th>Ranking</th>
                  <th>Medio de Pago</th>
                  <th>Usos</th>
                  <th>Porcentaje</th>
                  <th>Tendencia</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(medio, index) in mediosPago" :key="index" class="medio-row">
                  <td class="ranking-cell">
                    <span class="ranking-badge" :class="getRankingClass(index)">
                      #{{ index + 1 }}
                    </span>
                  </td>
                  <td class="nombre-cell">
                    <div class="medio-info">
                      <span class="medio-icon">{{ getMedioIcon(medio.nombre_medio_pago) }}</span>
                      <strong>{{ medio.nombre_medio_pago }}</strong>
                    </div>
                  </td>
                  <td class="usos-cell">
                    <span class="usos-badge">{{ medio.total_uso }}</span>
                  </td>
                  <td class="porcentaje-cell">
                    <div class="porcentaje-bar">
                      <div 
                        class="porcentaje-fill" 
                        :style="{ width: getPorcentaje(medio.total_uso) + '%' }"
                      ></div>
                    </div>
                    <span class="porcentaje-text">{{ getPorcentaje(medio.total_uso) }}%</span>
                  </td>
                  <td class="tendencia-cell">
                    <span class="tendencia-icon">{{ getTendenciaIcon(index) }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Crear nueva urgencia -->
      <div v-if="activeTab === 'crear'" class="crear-section">
        <div class="section-header">
          <h2>➕ Crear Nueva Urgencia</h2>
        </div>

        <div class="form-container">
          <form @submit.prevent="crearUrgencia" class="urgencia-form">
            <div class="form-group">
              <label for="nombre">Nombre de la Urgencia:</label>
              <input 
                v-model="nuevaUrgencia.nombre" 
                type="text" 
                id="nombre"
                class="form-input"
                placeholder="Ej: Entrega Express"
                required
              />
            </div>

            <div class="form-group">
              <label for="nivel">Nivel de Urgencia:</label>
              <select v-model="nuevaUrgencia.nivel" id="nivel" class="form-select" required>
                <option value="">Seleccionar nivel</option>
                <option value="Baja">🟢 Baja</option>
                <option value="Media">🟡 Media</option>
                <option value="Alta">🔴 Alta</option>
                <option value="Crítica">🚨 Crítica</option>
              </select>
            </div>

            <div class="form-group">
              <label for="descripcion">Descripción:</label>
              <textarea 
                v-model="nuevaUrgencia.descripcion" 
                id="descripcion"
                class="form-textarea"
                placeholder="Descripción detallada de la urgencia..."
                rows="4"
              ></textarea>
            </div>

            <div class="form-group">
              <label for="tiempo_limite">Tiempo Límite:</label>
              <input 
                v-model="nuevaUrgencia.tiempo_limite" 
                type="text" 
                id="tiempo_limite"
                class="form-input"
                placeholder="Ej: 30 minutos, 2 horas"
              />
            </div>

            <div class="form-actions">
              <button type="submit" :disabled="creando" class="btn-primary">
                <span v-if="!creando">➕ Crear Urgencia</span>
                <span v-else>⏳ Creando...</span>
              </button>
              <button type="button" @click="limpiarFormulario" class="btn-secondary">
                🗑️ Limpiar
              </button>
            </div>
          </form>
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
const activeTab = ref('lista')
const loading = ref(false)
const loadingPagos = ref(false)
const creando = ref(false)
const error = ref('')

const urgencias = ref([])
const mediosPago = ref([])
const nuevaUrgencia = ref({
  nombre: '',
  nivel: '',
  descripcion: '',
  tiempo_limite: ''
})

// Computadas
const totalUrgencias = computed(() => urgencias.value.length)

const medioMasUsado = computed(() => {
  if (mediosPago.value.length === 0) return null
  return mediosPago.value.reduce((max, medio) => 
    medio.total_uso > max.total_uso ? medio : max
  )
})

const porcentajeUrgentes = computed(() => {
  const urgentesAltas = urgencias.value.filter(u => 
    u.nivel === 'Alta' || u.nivel === 'Crítica'
  ).length
  return urgencias.value.length > 0 ? 
    Math.round((urgentesAltas / urgencias.value.length) * 100) : 0
})

// Funciones principales
const cargarUrgencias = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('🔄 Cargando urgencias desde:', `${API_ROUTES.URGENCIA}/`)
    const response = await $apiClient.get(`${API_ROUTES.URGENCIA}/`)
    urgencias.value = response.data || []
    
    console.log('✅ Urgencias cargadas:', urgencias.value.length)
    
  } catch (err) {
    console.error('❌ Error al cargar urgencias:', err)
    error.value = `Error al cargar urgencias: ${err.response?.data?.message || err.message}`
  } finally {
    loading.value = false
  }
}

const cargarMediosPago = async () => {
  loadingPagos.value = true
  
  try {
    console.log('🔄 Cargando medios de pago desde:', `${API_ROUTES.URGENCIA}/medio-pago-mas-usado`)
    const response = await $apiClient.get(`${API_ROUTES.URGENCIA}/medio-pago-mas-usado`)
    mediosPago.value = response.data || []
    
    console.log('✅ Medios de pago cargados:', mediosPago.value.length)
    
  } catch (err) {
    console.error('❌ Error al cargar medios de pago:', err)
    error.value = `Error al cargar medios de pago: ${err.response?.data?.message || err.message}`
  } finally {
    loadingPagos.value = false
  }
}

const crearUrgencia = async () => {
  creando.value = true
  
  try {
    const urgenciaData = {
      ...nuevaUrgencia.value,
      activo: true
    }
    
    console.log('🔄 Creando urgencia:', urgenciaData)
    await $apiClient.post(`${API_ROUTES.URGENCIA}/crear`, urgenciaData)
    
    alert('✅ Urgencia creada exitosamente')
    limpiarFormulario()
    cargarUrgencias()
    activeTab.value = 'lista'
    
  } catch (err) {
    console.error('❌ Error al crear urgencia:', err)
    alert(`Error al crear urgencia: ${err.response?.data?.message || err.message}`)
  } finally {
    creando.value = false
  }
}

const editarUrgencia = (urgencia) => {
  // Cargar datos en el formulario para edición
  nuevaUrgencia.value = { ...urgencia }
  activeTab.value = 'crear'
}

const eliminarUrgencia = async (id) => {
  if (!confirm('¿Estás seguro de que deseas eliminar esta urgencia?')) return
  
  try {
    console.log('🔄 Eliminando urgencia:', id)
    await $apiClient.delete(`${API_ROUTES.URGENCIA}/eliminar/${id}`)
    alert('✅ Urgencia eliminada exitosamente')
    cargarUrgencias()
    
  } catch (err) {
    console.error('❌ Error al eliminar urgencia:', err)
    alert(`Error al eliminar urgencia: ${err.response?.data?.message || err.message}`)
  }
}

const limpiarFormulario = () => {
  nuevaUrgencia.value = {
    nombre: '',
    nivel: '',
    descripcion: '',
    tiempo_limite: ''
  }
}

// Funciones de utilidad
const getNivelClass = (nivel) => {
  const classes = {
    'Baja': 'nivel-baja',
    'Media': 'nivel-media',
    'Alta': 'nivel-alta',
    'Crítica': 'nivel-critica'
  }
  return classes[nivel] || 'nivel-default'
}

const getNivelIcon = (nivel) => {
  const icons = {
    'Baja': '🟢',
    'Media': '🟡',
    'Alta': '🔴',
    'Crítica': '🚨'
  }
  return icons[nivel] || '⚪'
}

const getEstadoClass = (activo) => {
  return activo ? 'estado-activa' : 'estado-inactiva'
}

const getBarHeight = (valor) => {
  if (mediosPago.value.length === 0) return 0
  const max = Math.max(...mediosPago.value.map(m => m.total_uso))
  return (valor / max) * 100
}

const getBarClass = (index) => {
  const classes = ['bar-primary', 'bar-secondary', 'bar-tertiary']
  return classes[index % classes.length]
}

const getPorcentaje = (valor) => {
  const total = mediosPago.value.reduce((sum, m) => sum + m.total_uso, 0)
  return total > 0 ? Math.round((valor / total) * 100) : 0
}

const getRankingClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return 'rank-normal'
}

const getMedioIcon = (nombre) => {
  const icons = {
    'Tarjeta de Crédito': '💳',
    'Tarjeta de Débito': '💳',
    'Efectivo': '💵',
    'Transferencia': '🏦',
    'PayPal': '💰',
    'Cripto': '₿'
  }
  return icons[nombre] || '💳'
}

const getTendenciaIcon = (index) => {
  if (index === 0) return '📈'
  if (index < 3) return '📊'
  return '📉'
}

// Cargar datos al montar
onMounted(() => {
  cargarUrgencias()
  cargarMediosPago()
})
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

.analysis-tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  overflow-x: auto;
}

.tab-btn {
  background: transparent;
  border: none;
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--border-radius-md) var(--border-radius-md) 0 0;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

.tab-btn.active {
  background: var(--accent-primary);
  color: white;
  font-weight: 600;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.section-header h2 {
  margin: 0;
  color: var(--text-primary);
}

.refresh-btn {
  background: var(--accent-primary);
  color: white;
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-medium);
}

.refresh-btn:hover:not(:disabled) {
  background: var(--accent-secondary);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.urgencias-table-container {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.urgencias-table {
  width: 100%;
  border-collapse: collapse;
}

.urgencias-table th {
  background: var(--background-secondary);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.urgencia-row {
  transition: background-color var(--transition-fast);
}

.urgencia-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.urgencias-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  vertical-align: top;
}

.urgencia-id {
  font-family: 'Courier New', monospace;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius-sm);
  font-weight: 600;
}

.nivel-badge {
  padding: 0.25rem 0.75rem;
  border-radius: var(--border-radius-sm);
  font-size: 0.85rem;
  font-weight: 600;
}

.nivel-baja {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.nivel-media {
  background: rgba(245, 158, 11, 0.2);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.nivel-alta {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.nivel-critica {
  background: rgba(153, 27, 27, 0.3);
  color: #f87171;
  border: 1px solid rgba(153, 27, 27, 0.5);
  animation: pulse 2s infinite;
}

.descripcion-content strong {
  color: var(--text-primary);
  display: block;
  margin-bottom: 0.25rem;
}

.descripcion-content p {
  margin: 0;
  opacity: 0.8;
  font-size: 0.9rem;
}

.tiempo-limite {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius-sm);
  font-size: 0.85rem;
}

.estado-badge {
  padding: 0.25rem 0.75rem;
  border-radius: var(--border-radius-sm);
  font-size: 0.85rem;
  font-weight: 600;
}

.estado-activa {
  background: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.estado-inactiva {
  background: rgba(107, 114, 128, 0.2);
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.actions-cell {
  display: flex;
  gap: var(--spacing-xs);
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

.edit-btn {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.3);
}

.edit-btn:hover {
  background: rgba(59, 130, 246, 0.3);
}

.delete-btn {
  background: rgba(239, 68, 68, 0.2);
  color: #fca5a5;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.delete-btn:hover {
  background: rgba(239, 68, 68, 0.3);
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

.info-card h3 {
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

.pagos-results {
  display: grid;
  gap: var(--spacing-xl);
}

.chart-container {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  align-items: end;
  min-height: 200px;
  padding: var(--spacing-lg);
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
}

.chart-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-sm);
}

.bar-container {
  height: 150px;
  width: 50px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--border-radius-sm);
  display: flex;
  align-items: end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  border-radius: var(--border-radius-sm);
  transition: height 0.5s ease;
}

.bar-primary {
  background: linear-gradient(to top, var(--accent-primary), var(--accent-secondary));
}

.bar-secondary {
  background: linear-gradient(to top, #10b981, #34d399);
}

.bar-tertiary {
  background: linear-gradient(to top, #f59e0b, #fbbf24);
}

.bar-label {
  text-align: center;
  font-size: 0.8rem;
}

.bar-label strong {
  display: block;
  margin-bottom: 0.25rem;
}

.bar-value {
  opacity: 0.8;
}

.medios-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  overflow: hidden;
}

.medios-table th {
  background: var(--background-secondary);
  padding: var(--spacing-md);
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.medio-row {
  transition: background-color var(--transition-fast);
}

.medio-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.medios-table td {
  padding: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.ranking-badge {
  padding: 0.25rem 0.5rem;
  border-radius: var(--border-radius-sm);
  font-weight: 600;
  font-size: 0.9rem;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ffed4a);
  color: #1a1a1a;
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5);
  color: #1a1a1a;
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: #1a1a1a;
}

.rank-normal {
  background: var(--background-secondary);
  color: var(--text-primary);
}

.medio-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.medio-icon {
  font-size: 1.2rem;
}

.usos-badge {
  background: var(--accent-primary);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: var(--border-radius-sm);
  font-weight: 600;
}

.porcentaje-bar {
  background: rgba(255, 255, 255, 0.1);
  height: 8px;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0.25rem;
}

.porcentaje-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent-primary), var(--accent-secondary));
  transition: width 0.3s ease;
}

.porcentaje-text {
  font-size: 0.8rem;
  opacity: 0.8;
}

.tendencia-icon {
  font-size: 1.2rem;
}

.form-container {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
}

.urgencia-form {
  max-width: 600px;
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-group label {
  display: block;
  margin-bottom: var(--spacing-sm);
  font-weight: 600;
  color: var(--text-primary);
}

.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--border-radius-md);
  background: var(--background-secondary);
  color: var(--text-primary);
  transition: border-color var(--transition-fast);
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: var(--accent-primary);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  gap: var(--spacing-md);
  margin-top: var(--spacing-xl);
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

.loading-container,
.error-container,
.no-data {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-xl);
  text-align: center;
  margin-bottom: var(--spacing-lg);
}

.error-container {
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

.no-data-icon {
  font-size: 4rem;
  margin-bottom: var(--spacing-md);
  opacity: 0.7;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* Responsive */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .analysis-tabs {
    flex-direction: column;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-md);
  }
  
  .actions-cell {
    flex-direction: column;
  }
  
  .chart-container {
    flex-direction: column;
    height: auto;
  }
  
  .bar-container {
    height: 20px;
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>

<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">💬 Opiniones de Clientes</h1>
      <p class="page-subtitle">Análisis de feedback y satisfacción del cliente</p>
    </div>

    <!-- Estadísticas generales -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <h3>{{ totalOpiniones }}</h3>
          <p>Total Opiniones</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⭐</div>
        <div class="stat-content">
          <h3>{{ promedioGeneral }}</h3>
          <p>Promedio General</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>{{ problemasDetectados }}</h3>
          <p>Problemas Detectados</p>
        </div>
      </div>
    </div>

    <!-- Tabs de análisis -->
    <div class="analysis-tabs">
      <button 
        @click="activeTab = 'all'" 
        :class="['tab-btn', { active: activeTab === 'all' }]"
      >
        📋 Todas las Opiniones
      </button>
      <button 
        @click="activeTab = 'empresas'" 
        :class="['tab-btn', { active: activeTab === 'empresas' }]"
      >
        🏢 Por Empresa
      </button>
      <button 
        @click="activeTab = 'problemas'" 
        :class="['tab-btn', { active: activeTab === 'problemas' }]"
      >
        🚨 Problemas Detectados
      </button>
      <button 
        @click="activeTab = 'horarios'" 
        :class="['tab-btn', { active: activeTab === 'horarios' }]"
      >
        🕒 Por Horarios
      </button>
    </div>

    <!-- Contenido de tabs -->
    <div class="tab-content">
      <!-- Todas las opiniones -->
      <div v-if="activeTab === 'all'" class="opinions-section">
        <div class="section-header">
          <h2>📝 Todas las Opiniones</h2>
          <button @click="cargarOpiniones" :disabled="loading" class="refresh-btn">
            <span v-if="!loading">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>
        
        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Cargando opiniones...</p>
        </div>
        
        <div v-else-if="opiniones.length === 0" class="no-data">
          <p>No hay opiniones disponibles</p>
        </div>
        
        <div v-else class="opinions-list">
          <div v-for="opinion in opiniones" :key="opinion._id" class="opinion-card">
            <div class="opinion-header">
              <div class="opinion-meta">
                <span class="hora-id">Hora {{ opinion.hora }}:00</span>
                <span class="tipo">Opinión del Sistema</span>
                <span class="fecha">{{ formatDate(opinion.fecha) }}</span>
              </div>
              <div class="rating">
                <span class="stars">{{ getStarsDisplay(opinion.puntuacion) }}</span>
                <span class="rating-number">{{ opinion.puntuacion }}/5</span>
              </div>
            </div>
            <div class="opinion-body">
              <p class="comentario">{{ opinion.comentario }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Por empresa -->
      <div v-if="activeTab === 'empresas'" class="empresas-section">
        <div class="section-header">
          <h2>🏢 Promedio por Empresa</h2>
          <button @click="cargarPromedioEmpresas" :disabled="loadingEmpresas" class="refresh-btn">
            <span v-if="!loadingEmpresas">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>
        
        <div v-if="loadingEmpresas" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Analizando empresas...</p>
        </div>
        
        <div v-else class="empresas-ranking">
          <div v-for="(empresa, index) in promedioEmpresas" :key="empresa._id" class="empresa-card">
            <div class="ranking-position" :class="getRankingClass(index)">
              #{{ index + 1 }}
            </div>
            <div class="empresa-info">
              <h3>Empresa #{{ empresa._id }}</h3>
              <div class="empresa-stats">
                <div class="stat">
                  <span class="label">Promedio:</span>
                  <span class="value rating">{{ empresa.promedio_puntuacion.toFixed(1) }} ⭐</span>
                </div>
                <div class="stat">
                  <span class="label">Opiniones:</span>
                  <span class="value">{{ empresa.total_opiniones }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Problemas detectados -->
      <div v-if="activeTab === 'problemas'" class="problemas-section">
        <div class="section-header">
          <h2>🚨 Problemas Detectados</h2>
          <button @click="cargarProblemas" :disabled="loadingProblemas" class="refresh-btn">
            <span v-if="!loadingProblemas">🔄 Actualizar</span>
            <span v-else>⏳ Analizando...</span>
          </button>
        </div>
        
        <div v-if="loadingProblemas" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Detectando problemas en comentarios...</p>
        </div>
        
        <div v-else class="problemas-list">
          <div v-for="problema in problemasComentarios" :key="problema._id" class="problema-card">
            <div class="problema-header">
              <div class="problema-meta">
                <span class="cliente-id">Cliente #{{ problema.cliente_id }}</span>
                <span class="empresa-id">Empresa #{{ problema.empresa_id }}</span>
                <span class="fecha">{{ formatDate(problema.fecha) }}</span>
              </div>
              <div class="rating danger">
                <span class="rating-number">{{ problema.puntuacion }}/5</span>
              </div>
            </div>
            <div class="problema-body">
              <p class="comentario highlight">{{ problema.comentario }}</p>
              <div class="problema-keywords">
                <span class="keyword" v-for="keyword in detectKeywords(problema.comentario)" :key="keyword">
                  {{ keyword }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Por horarios -->
      <div v-if="activeTab === 'horarios'" class="horarios-section">
        <div class="section-header">
          <h2>🕒 Distribución por Horarios</h2>
          <button @click="cargarPorHorarios" :disabled="loadingHorarios" class="refresh-btn">
            <span v-if="!loadingHorarios">🔄 Actualizar</span>
            <span v-else>⏳ Cargando...</span>
          </button>
        </div>
        
        <div v-if="loadingHorarios" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Analizando horarios...</p>
        </div>
        
        <div v-else class="horarios-chart">
          <div v-for="horario in opinionesPorHora" :key="horario._id" class="horario-bar">
            <div class="horario-label">{{ horario._id }}:00</div>
            <div class="horario-visual">
              <div 
                class="horario-fill" 
                :style="{ width: getHorarioWidth(horario.total_opiniones) + '%' }"
              ></div>
              <span class="horario-count">{{ horario.total_opiniones || 0 }}</span>
              <span class="horario-avg">⭐ {{ (horario.promedio_puntuacion || 0).toFixed(1) }}</span>
            </div>
            <!-- Mostrar comentarios si existen -->
            <div v-if="horario.comentarios && horario.comentarios.length > 0" class="horario-comentarios">
              <div v-for="(comentario, index) in horario.comentarios.slice(0, 2)" :key="index" class="mini-comentario">
                "{{ comentario.length > 50 ? comentario.substring(0, 50) + '...' : comentario }}"
              </div>
              <div v-if="horario.comentarios.length > 2" class="mas-comentarios">
                +{{ horario.comentarios.length - 2 }} más
              </div>
            </div>
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
import API_ROUTES from '../src/api-routes.js'

const { $apiClient } = useNuxtApp()

// Estados reactivos
const activeTab = ref('all')
const loading = ref(false)
const loadingEmpresas = ref(false)
const loadingProblemas = ref(false)
const loadingHorarios = ref(false)

const opiniones = ref([])
const promedioEmpresas = ref([])
const problemasComentarios = ref([])
const opinionesPorHora = ref([])

// Estadísticas computadas
const totalOpiniones = computed(() => opiniones.value.length)
const promedioGeneral = computed(() => {
  if (opiniones.value.length === 0) return '0.0'
  const validOpinions = opiniones.value.filter(op => op.puntuacion && !isNaN(op.puntuacion))
  if (validOpinions.length === 0) return '0.0'
  const sum = validOpinions.reduce((acc, op) => acc + Number(op.puntuacion), 0)
  return (sum / validOpinions.length).toFixed(1)
})
const problemasDetectados = computed(() => problemasComentarios.value.length)

// Funciones para cargar datos
const cargarOpiniones = async () => {
  loading.value = true
  try {
    console.log('🔄 Cargando opiniones desde:', `${API_ROUTES.OPINIONES_CLIENTES}/hora-dia`)
    const response = await $apiClient.get(`${API_ROUTES.OPINIONES_CLIENTES}/hora-dia`)
    console.log('✅ Datos recibidos:', response.data)
    
    // Procesar los datos correctamente - convertir de estructura por hora a lista de opiniones
    opiniones.value = []
    if (Array.isArray(response.data)) {
      response.data.forEach(horaDato => {
        // Cada hora puede tener múltiples comentarios
        if (horaDato.comentarios && Array.isArray(horaDato.comentarios)) {
          horaDato.comentarios.forEach((comentario, index) => {
            opiniones.value.push({
              _id: `${horaDato._id}_${index}`,
              cliente_id: horaDato.cliente_id || 'N/A',
              empresa_id: horaDato.empresa_id || 'N/A', 
              hora: horaDato._id, // La hora del día
              puntuacion: horaDato.promedio_puntuacion || 0,
              comentario: comentario,
              fecha: new Date().toISOString() // Fecha simulada
            })
          })
        } else {
          // Si no hay comentarios, crear una opinión básica
          opiniones.value.push({
            _id: horaDato._id,
            cliente_id: horaDato.cliente_id || 'N/A',
            empresa_id: horaDato.empresa_id || 'N/A',
            hora: horaDato._id,
            puntuacion: horaDato.promedio_puntuacion || 0,
            comentario: 'Sin comentario específico',
            fecha: new Date().toISOString()
          })
        }
      })
    }
    
    console.log('✅ Opiniones procesadas:', opiniones.value.length)
  } catch (error) {
    console.error('❌ Error al cargar datos de opiniones:', error)
    error.value = 'Error al cargar datos de opiniones'
  } finally {
    loading.value = false
  }
}

const cargarPromedioEmpresas = async () => {
  loadingEmpresas.value = true
  try {
    const response = await $apiClient.get(`${API_ROUTES.OPINIONES_CLIENTES}/promedio-empresa`)
    promedioEmpresas.value = response.data.sort((a, b) => b.promedio_puntuacion - a.promedio_puntuacion)
    console.log('✅ Promedios por empresa cargados:', promedioEmpresas.value.length)
  } catch (error) {
    console.error('❌ Error al cargar promedios:', error)
    alert('Error al cargar promedios por empresa')
  } finally {
    loadingEmpresas.value = false
  }
}

const cargarProblemas = async () => {
  loadingProblemas.value = true
  try {
    const response = await $apiClient.get(`${API_ROUTES.OPINIONES_CLIENTES}/problemas-detectados`)
    problemasComentarios.value = response.data
    console.log('✅ Problemas detectados:', problemasComentarios.value.length)
  } catch (error) {
    console.error('❌ Error al detectar problemas:', error)
    alert('Error al detectar problemas')
  } finally {
    loadingProblemas.value = false
  }
}

const cargarPorHorarios = async () => {
  loadingHorarios.value = true
  try {
    const response = await $apiClient.get(`${API_ROUTES.OPINIONES_CLIENTES}/hora-dia`)
    opinionesPorHora.value = response.data.sort((a, b) => a._id - b._id)
    console.log('✅ Opiniones por horario cargadas:', opinionesPorHora.value.length)
  } catch (error) {
    console.error('❌ Error al cargar por horarios:', error)
    alert('Error al cargar distribución por horarios')
  } finally {
    loadingHorarios.value = false
  }
}

// Funciones de utilidad
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

const getStarsDisplay = (rating) => {
  return '⭐'.repeat(Math.max(0, Math.min(5, rating)))
}

const getRankingClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return 'default'
}

const detectKeywords = (comentario) => {
  if (!comentario) return []
  const keywords = ['demora', 'error', 'problema', 'tardanza', 'fallo', 'mal']
  return keywords.filter(keyword => 
    comentario.toLowerCase().includes(keyword)
  )
}

const getHorarioWidth = (total) => {
  if (opinionesPorHora.value.length === 0) return 0
  const max = Math.max(...opinionesPorHora.value.map(h => h.total_opiniones || 0))
  return max > 0 ? (total / max) * 100 : 0
}

// Cargar datos al montar
onMounted(() => {
  cargarOpiniones()
})
</script>

<style scoped>
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

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.stat-icon {
  font-size: 2rem;
}

.stat-content h3 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--accent-primary);
}

.stat-content p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 0.9rem;
}

.analysis-tabs {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
  border-bottom: 2px solid var(--border-color);
}

.tab-btn {
  padding: var(--spacing-md) var(--spacing-lg);
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--border-radius) var(--border-radius) 0 0;
  transition: all 0.3s ease;
  font-weight: 500;
}

.tab-btn:hover {
  color: var(--accent-primary);
  background: var(--background-secondary);
}

.tab-btn.active {
  color: var(--accent-primary);
  background: var(--background-card);
  border-bottom: 2px solid var(--accent-primary);
  margin-bottom: -2px;
}

.section-header {
  display: flex;
  justify-content: between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
}

.refresh-btn {
  background: var(--accent-primary);
  color: white;
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: var(--accent-hover);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.opinions-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.opinion-card, .problema-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  border: 1px solid var(--border-color);
}

.opinion-header, .problema-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--spacing-md);
}

.opinion-meta, .problema-meta {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.cliente-id, .empresa-id, .fecha {
  background: var(--background-secondary);
  padding: 2px 8px;
  border-radius: var(--border-radius);
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.rating {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.rating.danger {
  color: var(--color-error);
}

.comentario {
  margin: 0;
  line-height: 1.6;
  color: var(--text-primary);
}

.comentario.highlight {
  background: rgba(239, 68, 68, 0.1);
  padding: var(--spacing-sm);
  border-radius: var(--border-radius);
  border-left: 3px solid var(--color-error);
}

.problema-keywords {
  display: flex;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
}

.keyword {
  background: var(--color-error);
  color: white;
  padding: 2px 6px;
  border-radius: var(--border-radius);
  font-size: 0.7rem;
  font-weight: 600;
}

.empresas-ranking {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.empresa-card {
  background: var(--background-card);
  border-radius: var(--border-radius-lg);
  padding: var(--spacing-lg);
  border: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.ranking-position {
  font-size: 1.5rem;
  font-weight: bold;
  padding: var(--spacing-sm);
  border-radius: var(--border-radius);
  min-width: 60px;
  text-align: center;
}

.ranking-position.gold {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #92400e;
}

.ranking-position.silver {
  background: linear-gradient(135deg, #c0c0c0, #d1d5db);
  color: #374151;
}

.ranking-position.bronze {
  background: linear-gradient(135deg, #cd7f32, #d97706);
  color: white;
}

.ranking-position.default {
  background: var(--background-secondary);
  color: var(--text-secondary);
}

.empresa-stats {
  display: flex;
  gap: var(--spacing-lg);
}

.stat {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs);
}

.stat .label {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.stat .value {
  font-weight: 600;
  color: var(--text-primary);
}

.stat .value.rating {
  color: var(--accent-primary);
}

.horarios-chart {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.horario-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.horario-label {
  min-width: 60px;
  font-weight: 600;
  color: var(--text-primary);
}

.horario-visual {
  flex: 1;
  height: 30px;
  background: var(--background-secondary);
  border-radius: var(--border-radius);
  position: relative;
  overflow: hidden;
}

.horario-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--accent-primary), var(--accent-hover));
  transition: width 0.5s ease;
}

.horario-count {
  position: absolute;
  right: var(--spacing-sm);
  top: 50%;
  transform: translateY(-50%);
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.9rem;
}

.horario-avg {
  position: absolute;
  right: 80px;
  top: 50%;
  transform: translateY(-50%);
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 0.8rem;
}

.loading-container {
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

.no-data {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--text-muted);
  font-style: italic;
}

.horario-comentarios {
  margin-top: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--border-radius-sm);
}

.mini-comentario {
  font-size: 0.8rem;
  color: var(--text-secondary);
  font-style: italic;
  margin-bottom: 0.2rem;
}

.mas-comentarios {
  font-size: 0.7rem;
  color: var(--accent-primary);
  font-weight: 500;
}
</style>

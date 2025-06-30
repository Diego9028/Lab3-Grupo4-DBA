<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h1 class="page-title">📍 Distancia Recorrida por Repartidores</h1>
      <p class="page-subtitle">Estadísticas de kilómetros recorridos en el último mes</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Cargando datos de distancia...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <h3>Error al cargar los datos</h3>
      <p>{{ error }}</p>
      <button @click="fetchDistancias" class="btn-retry">Reintentar</button>
    </div>

    <!-- Empty State -->
    <div v-else-if="listaRepartidores.length === 0" class="empty-state">
      <div class="empty-icon">📊</div>
      <h3>No hay datos disponibles</h3>
      <p>No se encontraron registros de distancia para mostrar.</p>
    </div>

    <!-- Data Table -->
    <div v-else class="table-container">
      <div class="stats-summary">
        <div class="stat-card">
          <span class="stat-label">Total Repartidores</span>
          <span class="stat-value">{{ listaRepartidores.length }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Distancia Total</span>
          <span class="stat-value">{{ totalDistance.toFixed(2) }} km</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Promedio por Repartidor</span>
          <span class="stat-value">{{ averageDistance.toFixed(2) }} km</span>
        </div>
      </div>

      <div class="data-table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID Repartidor</th>
              <th>Nombre</th>
              <th>Distancia (km)</th>
              <th>Rendimiento</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="repartidor in listaRepartidores" :key="repartidor.id" class="table-row">
              <td class="table-cell">
                <span class="id-badge">{{ repartidor.id }}</span>
              </td>
              <td class="table-cell">
                <div class="name-cell">
                  <span class="name">{{ repartidor.nombre }}</span>
                </div>
              </td>
              <td class="table-cell">
                <span class="distance-value">{{ repartidor.distancia_km.toFixed(2) }}</span>
              </td>
              <td class="table-cell">
                <span :class="getPerformanceClass(repartidor.distancia_km)" class="performance-badge">
                  {{ getPerformanceLabel(repartidor.distancia_km) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
// Configurar middleware de autenticación
definePageMeta({
  middleware: 'auth'
})

import { ref, onMounted, computed } from 'vue'
import { useNuxtApp } from '#app'

const listaRepartidores = ref([])
const loading = ref(true)
const error = ref(null)
const { $apiClient } = useNuxtApp()

// Computed properties para estadísticas
const totalDistance = computed(() => {
  return listaRepartidores.value.reduce((total, r) => total + r.distancia_km, 0)
})

const averageDistance = computed(() => {
  const total = totalDistance.value
  const count = listaRepartidores.value.length
  return count > 0 ? total / count : 0
})

// Función para determinar el rendimiento del repartidor
const getPerformanceClass = (distance) => {
  if (distance >= 150) return 'performance-excellent'
  if (distance >= 100) return 'performance-good'
  if (distance >= 50) return 'performance-average'
  return 'performance-low'
}

const getPerformanceLabel = (distance) => {
  if (distance >= 150) return 'Excelente'
  if (distance >= 100) return 'Bueno'
  if (distance >= 50) return 'Promedio'
  return 'Bajo'
}

const fetchDistancias = async () => {
  try {
    loading.value = true
    error.value = null
    
    const repartidoresResponse = await $apiClient.get('/repartidor/')
    const todosRepartidores = repartidoresResponse.data

    const distanciasResponse = await $apiClient.get('/repartidor/distancia-recorrida')
    const distancias = distanciasResponse.data

    const mapaDistancias = {}
    for (const d of distancias) {
      mapaDistancias[d.repartidor] = d.km_recorridos
    }

    listaRepartidores.value = todosRepartidores.map(r => {
      const distancia = mapaDistancias[r.nombre]
      return {
        id: r.id_repartidor,
        nombre: r.nombre,
        distancia_km: distancia !== undefined ? distancia : 0
      }
    }).sort((a, b) => b.distancia_km - a.distancia_km) // Ordenar por distancia descendente
    
  } catch (err) {
    console.error('Error al obtener las distancias:', err)
    error.value = 'No se pudo cargar la información de distancia recorrida. Por favor, intenta nuevamente.'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDistancias()
})
</script>

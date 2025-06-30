<template>
    <div class="dashboard-container">
        <div class="page-header">
            <h1 class="page-title">📊 Dashboard de Desempeño</h1>
            <p class="page-subtitle">Estadísticas de rendimiento y ranking de repartidores</p>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="loading-container">
            <div class="loading-card">
                <div class="loading-spinner">
                    <div class="spinner-border"></div>
                </div>
                <h3>Cargando datos de desempeño...</h3>
                <p>Analizando rendimiento de repartidores</p>
            </div>
        </div>

        <!-- Content -->
        <div v-else>
            <!-- Estadísticas generales -->
            <div v-if="estadisticas" class="stats-grid">
                <div class="stat-card stat-primary">
                    <div class="stat-icon">👥</div>
                    <div class="stat-value">{{ estadisticas.totalRepartidores }}</div>
                    <div class="stat-label">Repartidores Activos</div>
                </div>
                <div class="stat-card stat-success">
                    <div class="stat-icon">📦</div>
                    <div class="stat-value">{{ estadisticas.totalPedidos.toLocaleString() }}</div>
                    <div class="stat-label">Total Pedidos</div>
                </div>
                <div class="stat-card stat-warning">
                    <div class="stat-icon">⭐</div>
                    <div class="stat-value">{{ estadisticas.promedioGeneral.toFixed(1) }}</div>
                    <div class="stat-label">Promedio General</div>
                </div>
                <div class="stat-card stat-info">
                    <div class="stat-icon">🏆</div>
                    <div class="stat-value">{{ estadisticas.mejorPromedio }}</div>
                    <div class="stat-label">Mejor Promedio</div>
                </div>
            </div>

            <!-- Controles -->
            <div class="controls-section">
                <div class="search-filters">
                    <input
                        v-model="filtroNombre"
                        type="text"
                        placeholder="🔍 Buscar por nombre..."
                        class="search-input"
                    />
                    <select v-model="ordenPor" class="filter-select">
                        <option value="promedio">Ordenar por Promedio</option>
                        <option value="total_puntos">Ordenar por Puntos</option>
                        <option value="total_pedidos">Ordenar por Pedidos</option>
                        <option value="nombre">Ordenar por Nombre</option>
                    </select>
                    <button @click="toggleOrden" class="btn-icon" :title="ordenDesc ? 'Descendente' : 'Ascendente'">
                        {{ ordenDesc ? '⬇️' : '⬆️' }}
                    </button>
                    <button @click="exportarCSV" class="btn-export">
                        <span class="btn-icon">📊</span>
                        Exportar CSV
                    </button>
                </div>
            </div>

            <!-- Tabla de resultados -->
            <div v-if="desempenosFiltrados.length > 0">
                <!-- Results Header -->
                <div class="results-header">
                    <h3>Ranking de Repartidores</h3>
                    <p>{{ desempenosFiltrados.length }} repartidores encontrados</p>
                </div>

                <!-- Modern Table -->
                <div class="table-container">
                    <table class="modern-table">
                        <thead>
                            <tr>
                                <th style="width: 80px;">#</th>
                                <th style="width: 30%;">Repartidor</th>
                                <th style="width: 15%;">Promedio</th>
                                <th style="width: 15%;">Total Puntos</th>
                                <th style="width: 15%;">Total Pedidos</th>
                                <th style="width: 25%;">Performance</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(repartidor, index) in desempenosFiltrados" 
                                :key="repartidor.id_repartidor" 
                                :class="getRowClass(repartidor.promedio)"
                                class="table-row">
                                <!-- Ranking -->
                                <td class="rank-column">
                                    <div class="rank-badge" :class="getRankClass(index)">
                                        {{ index + 1 }}
                                    </div>
                                </td>

                                <!-- Repartidor Info -->
                                <td>
                                    <div class="repartidor-info">
                                        <div class="repartidor-nombre">{{ repartidor.nombre }}</div>
                                        <div class="repartidor-id">ID: {{ repartidor.id_repartidor }}</div>
                                    </div>
                                </td>

                                <!-- Promedio con badge -->
                                <td>
                                    <div class="score-badge" :class="getScoreClass(repartidor.promedio)">
                                        {{ repartidor.promedio.toFixed(1) }} ⭐
                                    </div>
                                </td>

                                <!-- Total Puntos -->
                                <td>
                                    <div class="points-display">
                                        {{ repartidor.total_puntos.toLocaleString() }}
                                    </div>
                                </td>

                                <!-- Total Pedidos -->
                                <td>
                                    <div class="pedidos-display">
                                        {{ repartidor.total_pedidos.toLocaleString() }}
                                    </div>
                                </td>

                                <!-- Performance Bar -->
                                <td>
                                    <div class="performance-info">
                                        <div class="performance-label">{{ getPerformanceLabel(repartidor.promedio) }}</div>
                                        <div class="performance-bar">
                                            <div class="performance-fill" 
                                                 :style="{ width: getPerformanceWidth(repartidor.promedio) + '%' }"
                                                 :class="getPerformanceClass(repartidor.promedio)">
                                            </div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Estado vacío -->
            <div v-else class="empty-state">
                <div class="empty-icon">📊</div>
                <h3>No hay datos de desempeño</h3>
                <p>No se encontraron registros de desempeño de repartidores</p>
                <button @click="recargarDatos" class="retry-btn">
                    <span class="btn-icon">🔄</span>
                    Recargar
                </button>
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
import API_ROUTES from '../src/api-routes.js'

const desempenos = ref([])
const loading = ref(false)
const filtroNombre = ref('')
const ordenPor = ref('promedio')
const ordenDesc = ref(true)

const { $apiClient } = useNuxtApp()

// Estadísticas computadas
const estadisticas = computed(() => {
    if (!desempenos.value.length) return null
    
    const totalRepartidores = desempenos.value.length
    const totalPedidos = desempenos.value.reduce((sum, r) => sum + r.total_pedidos, 0)
    const promedioGeneral = desempenos.value.reduce((sum, r) => sum + r.promedio, 0) / totalRepartidores
    const mejorPromedio = Math.max(...desempenos.value.map(r => r.promedio)).toFixed(1)
    
    return {
        totalRepartidores,
        totalPedidos,
        promedioGeneral,
        mejorPromedio
    }
})

// Filtrado y ordenado
const desempenosFiltrados = computed(() => {
    let filtrados = desempenos.value

    // Filtrar por nombre
    if (filtroNombre.value) {
        filtrados = filtrados.filter(repartidor =>
            repartidor.nombre.toLowerCase().includes(filtroNombre.value.toLowerCase())
        )
    }

    // Ordenar
    filtrados.sort((a, b) => {
        const valorA = a[ordenPor.value]
        const valorB = b[ordenPor.value]
        
        if (typeof valorA === 'string') {
            return ordenDesc.value 
                ? valorB.localeCompare(valorA)
                : valorA.localeCompare(valorB)
        }
        
        return ordenDesc.value ? valorB - valorA : valorA - valorB
    })

    return filtrados
})

const fetchdesempenos = async () => {
    loading.value = true
    try {
        console.log('Obteniendo datos de desempeño desde:', `${API_ROUTES.REPARTIDOR}/desempenios`)
        const response = await $apiClient.get(`${API_ROUTES.REPARTIDOR}/desempenios`)
        desempenos.value = response.data || []
        console.log(`✅ Datos de desempeño cargados: ${desempenos.value.length} repartidores`)
    } catch (error) {
        console.error('❌ Error al obtener desempeños:', error)
        desempenos.value = []
        
        // Mejor manejo de errores
        if (error.response?.status === 403) {
            alert('Sin permisos para acceder a los datos de desempeño')
        } else if (error.response?.status === 500) {
            alert('Error del servidor. Intente más tarde.')
        } else {
            alert('No se pudieron cargar los datos de desempeño. Verifique su conexión.')
        }
    } finally {
        loading.value = false
    }
}

const recargarDatos = () => {
    fetchdesempenos()
}

const toggleOrden = () => {
    ordenDesc.value = !ordenDesc.value
}

const getRowClass = (promedio) => {
    if (promedio >= 4.5) return 'row-excellent'
    if (promedio >= 4.0) return 'row-good'
    if (promedio >= 3.5) return 'row-average'
    return 'row-poor'
}

const getRankClass = (index) => {
    if (index === 0) return 'rank-gold'
    if (index === 1) return 'rank-silver'
    if (index === 2) return 'rank-bronze'
    return 'rank-default'
}

const getScoreClass = (promedio) => {
    if (promedio >= 4.5) return 'score-excellent'
    if (promedio >= 4.0) return 'score-good'
    if (promedio >= 3.5) return 'score-average'
    return 'score-poor'
}

const getPerformanceWidth = (promedio) => {
    return Math.max(5, Math.min((promedio / 5) * 100, 100))
}

const getPerformanceClass = (promedio) => {
    if (promedio >= 4.5) return 'performance-excellent'
    if (promedio >= 4.0) return 'performance-good'
    if (promedio >= 3.5) return 'performance-average'
    return 'performance-poor'
}

const getPerformanceLabel = (promedio) => {
    if (promedio >= 4.5) return 'Excelente'
    if (promedio >= 4.0) return 'Bueno'
    if (promedio >= 3.5) return 'Regular'
    return 'Necesita Mejorar'
}

const exportarCSV = () => {
    try {
        const headers = ['Ranking', 'Nombre', 'ID Repartidor', 'Promedio', 'Total Puntos', 'Total Pedidos', 'Performance']
        const csvContent = [
            headers.join(','),
            ...desempenosFiltrados.value.map((repartidor, index) => [
                index + 1,
                `"${repartidor.nombre}"`,
                repartidor.id_repartidor,
                repartidor.promedio.toFixed(1),
                repartidor.total_puntos,
                repartidor.total_pedidos,
                getPerformanceLabel(repartidor.promedio)
            ].join(','))
        ].join('\n')

        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        const url = URL.createObjectURL(blob)
        link.setAttribute('href', url)
        link.setAttribute('download', `desempeno_repartidores_${new Date().toISOString().split('T')[0]}.csv`)
        link.style.visibility = 'hidden'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
    } catch (err) {
        console.error('Error al exportar CSV:', err)
        alert('Error al generar el archivo CSV')
    }
}

onMounted(() => {
    fetchdesempenos()
})
</script>


<template>
    <div class="dashboard-container">
        <div class="page-header">
            <h1 class="page-title">Puntos Cercanos a Empresas</h1>
            <p class="page-subtitle">Encuentra los 5 puntos de entrega más cercanos a una empresa</p>
        </div>

        <!-- Búsqueda de empresa -->
        <div class="search-section">
            <div class="input-group">
                <label class="input-label">Empresa</label>
                <div class="search-container">
                    <input
                        v-model="busquedaEmpresa"
                        @input="buscarEmpresas"
                        @focus="mostrarDropdown = true"
                        type="text"
                        placeholder="🔍 Buscar empresa por ID o nombre..."
                        class="search-input"
                        autocomplete="off"
                    />
                    
                    <!-- Dropdown de empresas -->
                    <div v-if="mostrarDropdown && empresasFiltradas.length > 0" class="dropdown-menu">
                        <div class="dropdown-header">
                            <span>{{ empresasFiltradas.length }} empresa(s) encontrada(s)</span>
                        </div>
                        <div 
                            v-for="empresa in empresasFiltradas" 
                            :key="empresa.id_empresa"
                            @click="seleccionarEmpresa(empresa)"
                            class="dropdown-item"
                        >
                            <div class="empresa-info">
                                <div class="empresa-nombre">{{ empresa.nombre }}</div>
                                <div class="empresa-id">ID: {{ empresa.id_empresa }}</div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <button 
                    @click="buscarPuntosPorEmpresa" 
                    :disabled="!empresaSeleccionada || loading"
                    class="btn-search"
                >
                    <span v-if="loading" class="loading-spinner small"></span>
                    {{ loading ? 'Buscando...' : '🔍 Buscar Puntos' }}
                </button>
            </div>
        </div>

        <!-- Información de empresa seleccionada -->
        <div v-if="empresaSeleccionada" class="selected-info">
            <div class="info-card">
                <div class="info-icon">🏢</div>
                <div class="info-content">
                    <div class="info-title">{{ empresaSeleccionada.nombre }}</div>
                    <div class="info-subtitle">ID: {{ empresaSeleccionada.id_empresa }}</div>
                </div>
            </div>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>Buscando puntos cercanos...</p>
        </div>

        <!-- Resultados -->
        <div v-else-if="puntos.length > 0" class="results-section">
            <div class="results-header">
                <h3>📍 Puntos de Entrega Cercanos</h3>
                <p>{{ puntos.length }} punto(s) encontrado(s) cerca de <strong>{{ empresaSeleccionada?.nombre }}</strong></p>
                <button @click="exportarCSV" class="btn-export">
                    📊 Exportar CSV
                </button>
            </div>

            <div class="table-container">
                <div class="table-wrapper">
                    <table class="modern-table">
                        <thead>
                            <tr>
                                <th class="rank-column">#</th>
                                <th>ID Pedido</th>
                                <th>Distancia</th>
                                <th>Proximidad</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(punto, index) in puntos" :key="punto.id_pedido">
                                <td class="rank-column">
                                    <div class="rank-badge" :class="getRankClass(index)">
                                        {{ index + 1 }}
                                    </div>
                                </td>
                                <td>
                                    <div class="pedido-info">
                                        <div class="pedido-id">{{ punto.id_pedido }}</div>
                                        <div class="pedido-label">Pedido</div>
                                    </div>
                                </td>
                                <td>
                                    <div class="distance-info">
                                        <div class="distance-value">{{ formatearDistancia(punto.distancia_metros) }}</div>
                                        <div class="distance-meters">{{ punto.distancia_metros.toLocaleString() }} m</div>
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

        <!-- Estado vacío -->
        <div v-else-if="busquedaRealizada" class="empty-state">
            <div class="empty-icon">📍</div>
            <h3>No se encontraron puntos cercanos</h3>
            <p v-if="empresaSeleccionada">
                No hay puntos de entrega registrados cerca de <strong>{{ empresaSeleccionada.nombre }}</strong>
            </p>
            <p v-else>Selecciona una empresa para buscar puntos cercanos</p>
        </div>

        <!-- Estado inicial -->
        <div v-else class="welcome-state">
            <div class="welcome-icon">🏢</div>
            <h3>Busca Puntos Cercanos</h3>
            <p>Selecciona una empresa para encontrar los puntos de entrega más cercanos</p>
        </div>
    </div>
</template>


<script setup>
// Configurar middleware de autenticación
definePageMeta({
  middleware: 'auth'
})

import { ref, computed, onMounted } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes.js'

const puntos = ref([])
const empresas = ref([])
const busquedaEmpresa = ref('')
const empresaSeleccionada = ref(null)
const mostrarDropdown = ref(false)
const loading = ref(false)
const busquedaRealizada = ref(false)

const { $apiClient } = useNuxtApp()

// Computed
const empresasFiltradas = computed(() => {
    if (!busquedaEmpresa.value) return empresas.value.slice(0, 10)
    
    const termino = busquedaEmpresa.value.toLowerCase()
    return empresas.value.filter(empresa =>
        empresa.nombre.toLowerCase().includes(termino) ||
        (empresa.id_empresa && empresa.id_empresa.toString().includes(termino))
    ).slice(0, 10)
})

// Métodos
const cargarEmpresas = async () => {
    try {
        console.log('🔄 Intentando cargar empresas desde:', `${API_ROUTES.EMPRESA}/top-volumen`)
        // Usar el endpoint que sí existe
        const response = await $apiClient.get(`${API_ROUTES.EMPRESA}/top-volumen`)
        console.log('✅ Empresas cargadas:', response.data)
        
        // Verificar si la respuesta es un array o un objeto con data
        let empresasData = Array.isArray(response.data) ? response.data : (response.data.data || [])
        
        // Procesar datos de empresas desde el endpoint de top-volumen
        empresas.value = empresasData.map((empresa, index) => ({
            id: empresa.id_empresa || empresa.id || (index + 1),
            id_empresa: empresa.id_empresa || empresa.id || (index + 1),
            nombre: empresa.nombre || empresa.empresa || `Empresa ${empresa.id_empresa || empresa.id || (index + 1)}`,
            tipo: empresa.tipo || 'Logística',
            direccion: empresa.direccion || `Dirección ${index + 1}`,
            total_entregas: empresa.total_entregas || 0
        }))
        
        console.log('📋 Empresas procesadas:', empresas.value.length)
    } catch (error) {
        console.error('Error al cargar empresas:', error)
        // Si no hay endpoint real, usar empresas de ejemplo
        empresas.value = [
            { id: 1, id_empresa: 1, nombre: 'Empresa Alpha', tipo: 'Logística', direccion: 'Av. Principal 123' },
            { id: 2, id_empresa: 2, nombre: 'Empresa Beta', tipo: 'Distribución', direccion: 'Calle Central 456' },
            { id: 3, id_empresa: 3, nombre: 'Empresa Gamma', tipo: 'Courier', direccion: 'Plaza Mayor 789' },
            { id: 4, id_empresa: 4, nombre: 'Empresa Delta', tipo: 'Express', direccion: 'Av. Comercial 101' },
            { id: 5, id_empresa: 5, nombre: 'Empresa Epsilon', tipo: 'Logística', direccion: 'Ruta Nacional 202' }
        ]
        console.log('📋 Usando empresas de ejemplo:', empresas.value.length)
    }
}

const buscarEmpresas = () => {
    if (busquedaEmpresa.value.length > 0) {
        mostrarDropdown.value = true
    }
}

const seleccionarEmpresa = (empresa) => {
    empresaSeleccionada.value = empresa
    busquedaEmpresa.value = `${empresa.nombre} (ID: ${empresa.id_empresa})`
    mostrarDropdown.value = false
    puntos.value = [] // Limpiar resultados anteriores
    busquedaRealizada.value = false
}

const buscarPuntosPorEmpresa = async () => {
    if (!empresaSeleccionada.value) {
        alert('Por favor selecciona una empresa')
        return
    }

    try {
        loading.value = true
        busquedaRealizada.value = true
        console.log('🔄 Buscando puntos cercanos para empresa:', empresaSeleccionada.value.id_empresa)
        
        const response = await $apiClient.get(
            `${API_ROUTES.EMPRESA}/cercanos/${empresaSeleccionada.value.id_empresa}`
        )
        
        console.log('✅ Puntos encontrados:', response.data)
        puntos.value = response.data || []
        
        if (puntos.value.length === 0) {
            alert('No se encontraron puntos cercanos para esta empresa')
        }
    } catch (error) {
        console.error('❌ Error al buscar puntos cercanos:', error)
        puntos.value = []
        if (error.response?.status === 404) {
            alert('No se encontraron puntos cercanos para esta empresa')
        } else {
            alert(`Error al buscar puntos cercanos: ${error.response?.data?.message || error.message}`)
        }
    } finally {
        loading.value = false
    }
}

const formatearDistancia = (metros) => {
    if (metros >= 1000) {
        return `${(metros / 1000).toFixed(1)} km`
    }
    return `${metros} m`
}

const getRankClass = (index) => {
    if (index === 0) return 'rank-gold'
    if (index === 1) return 'rank-silver'
    if (index === 2) return 'rank-bronze'
    return 'rank-default'
}

const getProximityWidth = (distancia) => {
    // Calculamos proximidad inversa (menos distancia = más barra)
    const maxDistancia = Math.max(...puntos.value.map(p => p.distancia_metros))
    return Math.max(20, 100 - (distancia / maxDistancia) * 80)
}

const getProximityClass = (distancia) => {
    if (distancia <= 500) return 'proximity-excellent'
    if (distancia <= 1000) return 'proximity-good'
    if (distancia <= 2000) return 'proximity-average'
    return 'proximity-poor'
}

const exportarCSV = () => {
    if (!puntos.value.length) return

    const headers = ['Ranking', 'ID Pedido', 'Distancia (metros)', 'Distancia (formateada)', 'Empresa']
    const csvContent = [
        headers.join(','),
        ...puntos.value.map((punto, index) => [
            index + 1,
            punto.id_pedido,
            punto.distancia_metros,
            `"${formatearDistancia(punto.distancia_metros)}"`,
            `"${empresaSeleccionada.value?.nombre || ''}"`
        ].join(','))
    ].join('\n')

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `puntos_cercanos_empresa_${empresaSeleccionada.value?.id}_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}

// Click outside para cerrar dropdown
const handleClickOutside = (event) => {
    if (!event.target.closest('.search-container')) {
        mostrarDropdown.value = false
    }
}

onMounted(() => {
    cargarEmpresas()
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

.input-group {
    display: flex;
    gap: 1rem;
    align-items: end;
    flex-wrap: wrap;
}

.input-label {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 0.5rem;
    display: block;
}

.search-container {
    position: relative;
    flex: 1;
    min-width: 300px;
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
    z-index: 1000;
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

.empresa-info {
    text-align: left;
}

.empresa-nombre {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 0.25rem;
}

.empresa-id {
    font-size: 0.875rem;
    color: var(--text-secondary);
}

.btn-search {
    background: linear-gradient(135deg, var(--primary), var(--primary-dark));
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    min-width: 140px;
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

.pedido-info {
    text-align: left;
}

.pedido-id {
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 0.25rem;
}

.pedido-label {
    font-size: 0.875rem;
    color: var(--text-secondary);
}

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

.welcome-state, .empty-state {
    text-align: center;
    padding: 3rem 1rem;
}

.welcome-icon, .empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.6;
}

.welcome-state h3, .empty-state h3 {
    color: var(--text-primary);
    margin-bottom: 0.5rem;
}

.welcome-state p, .empty-state p {
    color: var(--text-secondary);
    max-width: 400px;
    margin: 0 auto;
}

.loading-spinner.small {
    width: 16px;
    height: 16px;
    border-width: 2px;
}
</style>
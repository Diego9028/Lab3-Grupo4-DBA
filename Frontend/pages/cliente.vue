<template>
    <div class="container">
        <h1 class="title">Lista de Clientes</h1>
        
        <div class="buttons">
            <button @click="fetchClientes" :disabled="loading" class="btn-primary">
                {{ loading ? 'Cargando...' : 'Cargar Clientes' }}
            </button>
        </div>
        
        <div class="search">
            <input
                type="text"
                v-model="correo"
                placeholder="Buscar cliente por correo"
                class="input"
                @keyup.enter="buscarClientePorCorreo"
            />
            <button @click="buscarClientePorCorreo" :disabled="loading" class="btn-secondary">
                Buscar
            </button>
        </div>

        <!-- Estados de loading/error simples -->
        <div v-if="loading" class="loading-container">
            <div class="loading-spinner"></div>
            <h3>Cargando clientes...</h3>
            <p>Obteniendo datos del servidor...</p>
        </div>
        
        <div v-else-if="error" class="error-container">
            <div class="error-icon">❌</div>
            <h3>Error</h3>
            <p>{{ error }}</p>
            <button @click="fetchClientes" class="btn-primary">
                🔄 Reintentar
            </button>
        </div>
        
        <!-- Tabla de clientes -->
        <div v-else class="table-container">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Dirección</th>
                        <th>Correo</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="clientes.length === 0" class="no-data">
                        <td colspan="4" style="text-align: center; padding: 20px; color: var(--text-muted);">
                            No hay clientes para mostrar
                        </td>
                    </tr>
                    <tr v-for="cliente in clientes" :key="cliente.id_cliente" class="table-row">
                        <td>{{ cliente.id_cliente }}</td>
                        <td>{{ cliente.nombre }}</td>
                        <td>{{ cliente.direccion }}</td>
                        <td>{{ cliente.correo }}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- Info de resultados -->
        <div v-if="!loading && !error" class="results-info">
            <p><strong>Total de clientes:</strong> {{ clientes.length }}</p>
        </div>
    </div>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

import { ref, onMounted } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes.js'

const clientes = ref([])
const correo = ref('')
const loading = ref(false)
const error = ref('')
const { $apiClient } = useNuxtApp()

const fetchClientes = async (path = '/') => {
    loading.value = true
    error.value = ''
    try {
        console.log('🔄 Cargando clientes desde:', API_ROUTES.CLIENTE + path)
        const response = await $apiClient.get(API_ROUTES.CLIENTE + path)
        clientes.value = response.data
        console.log('✅ Clientes obtenidos:', clientes.value.length)
    } catch (err) {
        console.error('❌ Error al obtener clientes:', err)
        error.value = err.response?.data?.message || 'Error al obtener clientes'
    } finally {
        loading.value = false
    }
}

const buscarClientePorCorreo = async () => {
    if (!correo.value.trim()) {
        alert('Por favor ingresa un correo para buscar.')
        return
    }
    
    loading.value = true
    error.value = ''
    try {
        console.log('🔍 Buscando cliente por correo:', correo.value)
        const response = await $apiClient.get(`${API_ROUTES.CLIENTE}/correo`, {
            params: { correo: correo.value }
        })
        // Asegurarse de que clientes sea siempre un array
        clientes.value = Array.isArray(response.data) ? response.data : [response.data]
        console.log('✅ Cliente encontrado:', clientes.value)
    } catch (err) {
        console.error('❌ Error al buscar cliente:', err)
        error.value = 'No se pudo encontrar el cliente.'
        clientes.value = []
    } finally {
        loading.value = false
    }
}

// Cargar clientes al montar el componente
onMounted(() => {
    fetchClientes()
})
</script>

<style scoped>
.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: var(--spacing-lg);
}

.title {
    color: var(--text-primary);
    margin-bottom: var(--spacing-xl);
    text-align: center;
}

.buttons {
    display: flex;
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-lg);
    justify-content: center;
}

.search {
    display: flex;
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-xl);
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
}

.input {
    flex: 1;
    padding: var(--spacing-md);
    border: 2px solid var(--border-color);
    border-radius: var(--border-radius-md);
    background: var(--background-secondary);
    color: var(--text-primary);
}

.input:focus {
    outline: none;
    border-color: var(--accent-primary);
}

.btn-primary, .btn-secondary {
    padding: var(--spacing-md) var(--spacing-lg);
    border: none;
    border-radius: var(--border-radius-md);
    font-weight: 600;
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

.btn-secondary {
    background: var(--background-secondary);
    color: var(--text-primary);
    border: 1px solid var(--border-color);
}

.btn-secondary:hover:not(:disabled) {
    background: var(--background-accent);
}

.btn-primary:disabled, .btn-secondary:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.table-container {
    background: var(--background-card);
    border-radius: var(--border-radius-lg);
    overflow: hidden;
    box-shadow: var(--shadow-sm);
}

.table {
    width: 100%;
    border-collapse: collapse;
}

.table th {
    background: var(--background-secondary);
    padding: var(--spacing-lg);
    text-align: left;
    font-weight: 600;
    color: var(--text-primary);
    border-bottom: 1px solid var(--border-color);
}

.table-row {
    transition: background-color var(--transition-fast);
}

.table-row:hover {
    background: rgba(255, 255, 255, 0.05);
}

.table td {
    padding: var(--spacing-md) var(--spacing-lg);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.table td:last-child {
    border-right: none;
}

.no-data {
    background: rgba(255, 255, 255, 0.02);
}

.results-info {
    margin-top: var(--spacing-lg);
    text-align: center;
    padding: var(--spacing-md);
    background: var(--background-secondary);
    border-radius: var(--border-radius-md);
    color: var(--text-secondary);
}

/* Estados de loading y error */
.loading-container {
    text-align: center;
    padding: var(--spacing-xl);
    color: var(--text-secondary);
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

.loading-container h3 {
    margin: var(--spacing-md) 0 var(--spacing-sm) 0;
    color: var(--text-primary);
}

.loading-container p {
    margin: 0;
    opacity: 0.8;
}

.error-container {
    text-align: center;
    padding: var(--spacing-xl);
    background: var(--background-card);
    border-radius: var(--border-radius-lg);
    border: 1px solid rgba(239, 68, 68, 0.3);
}

.error-icon {
    font-size: 3rem;
    margin-bottom: var(--spacing-md);
}

.error-container h3 {
    color: #ef4444;
    margin: 0 0 var(--spacing-sm) 0;
}

.error-container p {
    color: var(--text-secondary);
    margin: 0 0 var(--spacing-lg) 0;
}

/* Responsive */
@media (max-width: 768px) {
    .search {
        flex-direction: column;
    }
    
    .table {
        font-size: 0.9rem;
    }
    
    .table th, .table td {
        padding: var(--spacing-sm) var(--spacing-md);
    }
}
</style>
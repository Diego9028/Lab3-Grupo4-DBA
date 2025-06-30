<template>
  <div class="text-center my-8">
    <h1 class="title">🔍 Verificar Zona de Cobertura</h1>
    
    <div class="search-container">
      <!-- Dropdown de clientes -->
      <div class="client-selector">
        <label class="selector-label">Seleccionar Cliente:</label>
        <div class="custom-dropdown" :class="{ open: dropdownOpen }">
          <div class="dropdown-trigger" @click="toggleDropdown">
            <span v-if="selectedCliente" class="selected-client">
              {{ selectedCliente.nombre }} (ID: {{ selectedCliente.id_cliente }})
            </span>
            <span v-else class="dropdown-placeholder">
              Buscar cliente...
            </span>
            <span class="dropdown-arrow">{{ dropdownOpen ? '▲' : '▼' }}</span>
          </div>
          
          <div v-if="dropdownOpen" class="dropdown-content">
            <div class="search-input-container">
              <input
                ref="searchInput"
                v-model="searchTerm"
                type="text"
                placeholder="Buscar por nombre o ID..."
                class="search-input"
                @input="filterClientes"
              />
              <span class="search-icon">🔍</span>
            </div>
            
            <div class="clients-list">
              <div v-if="loadingClientes" class="loading-item">
                <div class="spinner-border spinner-border-sm"></div>
                Cargando clientes...
              </div>
              
              <div v-else-if="filteredClientes.length === 0 && clientes.length > 0" class="no-results">
                No se encontraron clientes con ese criterio
              </div>
              
              <div v-else-if="clientes.length === 0" class="no-results">
                No hay clientes disponibles
              </div>
              
              <div
                v-else
                v-for="cliente in filteredClientes"
                :key="cliente.id_cliente"
                class="client-item"
                @click="selectCliente(cliente)"
              >
                <div class="client-info">
                  <div class="client-name">{{ cliente.nombre }}</div>
                  <div class="client-details">
                    ID: {{ cliente.id_cliente }} • {{ cliente.correo }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <button 
        @click="verificarZona" 
        :disabled="!selectedCliente || loading"
        class="search-btn"
        :class="{ loading: loading }"
      >
        <span v-if="!loading">
          <span class="btn-icon">🔍</span>
          Consultar Cobertura
        </span>
        <span v-else class="loading-content">
          <div class="spinner-border spinner-border-sm"></div>
          Consultando...
        </span>
      </button>
    </div>

    <!-- Resultado -->
    <transition name="fade-slide">
      <div v-if="zona" class="result-card">
        <div class="result-header">
          <h2>✅ Zona de Cobertura Encontrada</h2>
        </div>
        <div class="result-content">
          <div class="result-item">
            <span class="result-label">🗺️ ID Zona:</span>
            <span class="result-value">{{ zona.id_zona }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">📍 Nombre:</span>
            <span class="result-value">{{ zona.nombre }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">🏷️ Tipo:</span>
            <span class="result-value">{{ zona.tipo }}</span>
          </div>
          <div class="result-item">
            <span class="result-label">👤 Cliente:</span>
            <span class="result-value">{{ selectedCliente.nombre }}</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- Error -->
    <transition name="fade-slide">
      <div v-if="error" class="error-card">
        <div class="error-header">
          <span class="error-icon">❌</span>
          <h3>Error en la consulta</h3>
        </div>
        <p class="error-message">{{ error }}</p>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes'

definePageMeta({
  middleware: 'auth'
})

// Estados reactivos
const selectedCliente = ref(null)
const clientes = ref([])
const filteredClientes = ref([])
const zona = ref(null)
const loading = ref(false)
const loadingClientes = ref(false)
const error = ref('')
const dropdownOpen = ref(false)
const searchTerm = ref('')
const searchInput = ref(null)
const { $apiClient } = useNuxtApp()

const toggleDropdown = async () => {
  dropdownOpen.value = !dropdownOpen.value
  
  if (dropdownOpen.value) {
    // Cargar clientes si no están cargados
    if (clientes.value.length === 0) {
      await loadClientes()
    }
    
    // Focus en el input de búsqueda
    await nextTick()
    if (searchInput.value) {
      searchInput.value.focus()
    }
  }
}

const loadClientes = async () => {
  loadingClientes.value = true
  try {
    console.log('🔄 Cargando clientes desde', `${API_ROUTES.CLIENTE}/`)
    const response = await $apiClient.get(`${API_ROUTES.CLIENTE}/`)
    console.log('✅ Clientes cargados:', response.data)
    
    // Verificar si la respuesta es un array o un objeto con data
    let clientesData = Array.isArray(response.data) ? response.data : (response.data.data || [])
    
    // Asegurar que cada cliente tenga las propiedades necesarias
    clientes.value = clientesData.map(cliente => ({
      id_cliente: cliente.id_cliente || cliente.id,
      nombre: cliente.nombre || `Cliente ${cliente.id_cliente || cliente.id}`,
      correo: cliente.correo || cliente.email || 'Sin correo',
      telefono: cliente.telefono || 'Sin teléfono',
      direccion: cliente.direccion || 'Sin dirección'
    }))
    
    filteredClientes.value = clientes.value
    console.log('📋 Lista filtrada actualizada:', filteredClientes.value.length, 'clientes')
  } catch (err) {
    console.error('Error al cargar clientes:', err)
    if (err.response?.status === 403) {
      error.value = 'No tienes permisos para acceder a la lista de clientes'
    } else if (err.response?.status === 401) {
      error.value = 'Tu sesión ha expirado. Redirigiendo al login...'
      setTimeout(() => navigateTo('/login'), 2000)
    } else {
      error.value = `Error al cargar clientes: ${err.response?.data?.message || 'Error desconocido'}`
    }
  } finally {
    loadingClientes.value = false
  }
}

const filterClientes = () => {
  const term = searchTerm.value.toLowerCase()
  if (!term) {
    filteredClientes.value = clientes.value
    return
  }
  
  filteredClientes.value = clientes.value.filter(cliente => 
    (cliente.nombre && cliente.nombre.toLowerCase().includes(term)) ||
    (cliente.correo && cliente.correo.toLowerCase().includes(term)) ||
    (cliente.id_cliente && cliente.id_cliente.toString().includes(term))
  )
  console.log('🔍 Filtro aplicado:', term, '→', filteredClientes.value.length, 'resultados')
}

const selectCliente = (cliente) => {
  selectedCliente.value = cliente
  dropdownOpen.value = false
  searchTerm.value = ''
  filteredClientes.value = clientes.value
  
  // Limpiar resultados anteriores
  zona.value = null
  error.value = ''
}

const verificarZona = async () => {
  if (!selectedCliente.value) {
    error.value = 'Por favor selecciona un cliente'
    return
  }

  loading.value = true
  zona.value = null
  error.value = ''
  
  try {
    const response = await $apiClient.get(`${API_ROUTES.CLIENTE}/zona-de-cliente/${selectedCliente.value.id_cliente}`)
    zona.value = response.data
  } catch (err) {
    console.error('Error al verificar zona de cobertura:', err)
    if (err.response?.status === 403) {
      error.value = 'No tienes permisos para consultar la zona de cobertura'
    } else if (err.response?.status === 404) {
      error.value = `El cliente ${selectedCliente.value.nombre} no tiene cobertura en ninguna zona`
    } else if (err.response?.status === 401) {
      error.value = 'Tu sesión ha expirado. Redirigiendo al login...'
      setTimeout(() => navigateTo('/login'), 2000)
    } else {
      error.value = `Error al verificar cobertura: ${err.response?.data?.message || 'Error desconocido'}`
    }
  } finally {
    loading.value = false
  }
}

// Cerrar dropdown al hacer clic fuera
onMounted(() => {
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.custom-dropdown')) {
      dropdownOpen.value = false
    }
  })
})
</script>

<style scoped>
:root {
  --primary-color: #3b82f6;
  --primary-dark: #2563eb;
  --success-color: #10b981;
  --danger-color: #ef4444;
  --warning-color: #f59e0b;
  --background-primary: #ffffff;
  --background-secondary: #f8fafc;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --border-color: #e5e7eb;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --border-radius-lg: 1rem;
  --border-radius-md: 0.5rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --transition-normal: all 0.2s ease-in-out;
}

.title {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
  text-align: center;
}

.search-container {
  max-width: 600px;
  margin: 0 auto 2rem;
  padding: 2rem;
  background: var(--background-primary);
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
}

.client-selector {
  margin-bottom: 1.5rem;
}

.selector-label {
  display: block;
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
  text-align: left;
}

.custom-dropdown {
  position: relative;
  width: 100%;
}

.dropdown-trigger {
  width: 100%;
  padding: var(--spacing-md);
  border: 2px solid var(--border-color);
  border-radius: var(--border-radius-lg);
  background: var(--background-secondary);
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all var(--transition-normal);
  min-height: 50px;
}

.dropdown-trigger:hover {
  border-color: var(--primary-color);
}

.custom-dropdown.open .dropdown-trigger {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.selected-client {
  color: var(--text-primary);
  font-weight: 500;
}

.dropdown-placeholder {
  color: var(--text-secondary);
}

.dropdown-arrow {
  color: var(--text-secondary);
  transition: transform var(--transition-normal);
}

.dropdown-content {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--background-primary);
  border: 2px solid var(--primary-color);
  border-top: none;
  border-radius: 0 0 var(--border-radius-lg) var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
  z-index: 99999;
  max-height: 350px;
  overflow: hidden;
}

.search-input-container {
  position: relative;
  padding: var(--spacing-sm);
  border-bottom: 1px solid var(--border-color);
}

.search-input {
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-sm) var(--spacing-sm) 2.5rem;
  border: 1px solid var(--border-color);
  border-radius: var(--border-radius-md);
  outline: none;
  transition: border-color var(--transition-normal);
}

.search-input:focus {
  border-color: var(--primary-color);
}

.search-icon {
  position: absolute;
  left: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-secondary);
}

.clients-list {
  max-height: 240px;
  overflow-y: auto;
}

.client-item {
  padding: var(--spacing-md);
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background-color var(--transition-normal);
}

.client-item:hover {
  background-color: var(--background-secondary);
}

.client-item:last-child {
  border-bottom: none;
}

.client-info {
  text-align: left;
}

.client-name {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.client-details {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.loading-item,
.no-results {
  padding: var(--spacing-md);
  text-align: center;
  color: var(--text-secondary);
  font-style: italic;
}

.search-btn {
  width: 100%;
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: var(--border-radius-lg);
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.search-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-xl);
}

.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  margin-right: var(--spacing-sm);
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.result-card,
.error-card {
  max-width: 600px;
  margin: 2rem auto;
  padding: 2rem;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-xl);
}

.result-card {
  background: linear-gradient(135deg, #f0fdf4, #ecfdf5);
  border: 2px solid var(--success-color);
}

.error-card {
  background: linear-gradient(135deg, #fef2f2, #fee2e2);
  border: 2px solid var(--danger-color);
}

.result-header,
.error-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.result-header h2 {
  color: var(--success-color);
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.error-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.error-header h3 {
  color: var(--danger-color);
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
}

.error-icon {
  font-size: 1.5rem;
}

.result-content {
  display: grid;
  gap: 1rem;
}

.result-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: rgba(255, 255, 255, 0.7);
  border-radius: var(--border-radius-md);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.result-label {
  font-weight: 600;
  color: var(--text-primary);
}

.result-value {
  font-weight: 500;
  color: var(--success-color);
}

.error-message {
  margin: 0;
  color: var(--danger-color);
  font-weight: 500;
  text-align: center;
}

.spinner-border {
  width: 1rem;
  height: 1rem;
  border: 2px solid currentColor;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 0.75s linear infinite;
}

.spinner-border-sm {
  width: 0.875rem;
  height: 0.875rem;
  border-width: 1px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
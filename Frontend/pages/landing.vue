<template>
  <div class="landing-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            🚀 Mercado Libren't
          </h1>
          <p class="hero-subtitle">
            La plataforma más avanzada para gestionar tu negocio de delivery. 
            Optimiza rutas, analiza datos y mejora la experiencia de tus clientes.
          </p>
          <div class="hero-features">
            <div class="feature-item">
              <span class="feature-icon">📊</span>
              <span>Analytics en tiempo real</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">🗺️</span>
              <span>Gestión de zonas inteligente</span>
            </div>
            <div class="feature-item">
              <span class="feature-icon">⚡</span>
              <span>Optimización de rutas</span>
            </div>
          </div>
        </div>
        
        <div class="auth-section">
          <div class="auth-card">
            <div class="auth-tabs">
              <button 
                :class="{ active: activeTab === 'login' }" 
                @click="activeTab = 'login'"
                class="tab-btn"
              >
                Iniciar Sesión
              </button>
              <button 
                :class="{ active: activeTab === 'register' }" 
                @click="activeTab = 'register'"
                class="tab-btn"
              >
                Registrarse
              </button>
            </div>
            
            <!-- Login Form -->
            <div v-if="activeTab === 'login'" class="auth-form">
              <form @submit.prevent="handleLogin">
                <div class="form-group">
                  <input 
                    v-model="loginForm.correo" 
                    type="email" 
                    placeholder="Correo electrónico"
                    required
                    class="form-input"
                  />
                  <span class="input-icon">📧</span>
                </div>
                <div class="form-group">
                  <input 
                    v-model="loginForm.password" 
                    type="password" 
                    placeholder="Contraseña"
                    required
                    class="form-input"
                  />
                  <span class="input-icon">🔒</span>
                </div>
                <button 
                  type="submit" 
                  :disabled="isLoading"
                  class="auth-btn"
                  :class="{ loading: isLoading }"
                >
                  <span v-if="!isLoading">Ingresar</span>
                  <span v-else class="loading-content">
                    <div class="spinner"></div>
                    Ingresando...
                  </span>
                </button>
              </form>
            </div>
            
            <!-- Register Form -->
            <div v-if="activeTab === 'register'" class="auth-form">
              <form @submit.prevent="handleRegister">
                <div class="form-group">
                  <input 
                    v-model="registerForm.nombre" 
                    type="text" 
                    placeholder="Nombre completo"
                    required
                    class="form-input"
                  />
                  <span class="input-icon">👤</span>
                </div>
                <div class="form-group">
                  <input 
                    v-model="registerForm.correo" 
                    type="email" 
                    placeholder="Correo electrónico"
                    required
                    class="form-input"
                  />
                  <span class="input-icon">📧</span>
                </div>
                <div class="form-group">
                  <input 
                    v-model="registerForm.direccion" 
                    type="text" 
                    placeholder="Dirección"
                    required
                    class="form-input"
                  />
                  <span class="input-icon">🏠</span>
                </div>
                <div class="form-group">
                  <input 
                    v-model="registerForm.password" 
                    type="password" 
                    placeholder="Contraseña"
                    required
                    class="form-input"
                  />
                  <span class="input-icon">🔒</span>
                </div>
                <div class="location-section">
                  <p class="location-label">📍 Selecciona tu ubicación:</p>
                  <ClientOnly>
                    <LocationMap 
                      v-if="isClient" 
                      v-model="registerForm.location" 
                    />
                    <template #fallback>
                      <div class="map-skeleton">
                        <div class="skeleton-loader"></div>
                        <p>Cargando mapa...</p>
                      </div>
                    </template>
                  </ClientOnly>
                </div>
                <button 
                  type="submit" 
                  :disabled="isLoading"
                  class="auth-btn"
                  :class="{ loading: isLoading }"
                >
                  <span v-if="!isLoading">Registrarse</span>
                  <span v-else class="loading-content">
                    <div class="spinner"></div>
                    Registrando...
                  </span>
                </button>
              </form>
            </div>
            
            <!-- Messages -->
            <transition name="fade">
              <div v-if="message.text" :class="`message ${message.type}`">
                <span class="message-icon">
                  {{ message.type === 'success' ? '✅' : '❌' }}
                </span>
                {{ message.text }}
              </div>
            </transition>
            

          </div>
        </div>
      </div>
    </div>
    
    <!-- Features Section -->
    <div class="features-section">
      <div class="container">
        <h2 class="section-title">¿Por qué elegir Mercado Libren't?</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon-large">📈</div>
            <h3>Analytics Avanzados</h3>
            <p>Obtén insights profundos sobre el rendimiento de tu negocio con nuestros dashboards interactivos.</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-large">🎯</div>
            <h3>Gestión Inteligente</h3>
            <p>Optimiza automáticamente las rutas de entrega y gestiona zonas de cobertura eficientemente.</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon-large">⚡</div>
            <h3>Tiempo Real</h3>
            <p>Monitorea pedidos, repartidores y métricas en tiempo real para tomar decisiones inmediatas.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useNuxtApp } from '#app'
import API_ROUTES from '../src/api-routes'
import LocationMap from '../components/LocationMap.vue'
import { tokenUtils } from '../src/utils/tokenUtils'

// Definir que esta página no requiere autenticación
definePageMeta({
  layout: 'auth' // Usar layout sin navbar
})

const router = useRouter()
const { $apiClient } = useNuxtApp()

// Estado de hidratación
const isClient = ref(false)

onMounted(() => {
  nextTick(() => {
    isClient.value = true
  })
  
  // Debug: Verificar estado inicial del localStorage
  if (process.client) {
    console.log('Estado inicial del localStorage:')
    console.log('access_token:', tokenUtils.getTokenPreview())
    console.log('userData:', localStorage.getItem('userData'))
  }
})

const activeTab = ref('login')
const isLoading = ref(false)
const message = ref({ text: '', type: '' })

const loginForm = ref({
  correo: '',
  password: ''
})

const registerForm = ref({
  nombre: '',
  correo: '',
  direccion: '',
  password: '',
  location: { lat: null, lng: null }
})

const showMessage = (text, type = 'error') => {
  message.value = { text, type }
  setTimeout(() => {
    message.value = { text: '', type: '' }
  }, 5000)
}


const handleLogin = async () => {
  isLoading.value = true
  message.value = { text: '', type: '' }
  
  try {
    console.log('Intentando login con:', { correo: loginForm.value.correo })
    
    const response = await $apiClient.post(`${API_ROUTES.AUTH}/login`, {
      correo: loginForm.value.correo,
      password: loginForm.value.password
    })
    
    console.log('Respuesta del login:', response.data)
    
    // El backend devuelve "access_token"
    const token = response.data.access_token
    
    if (token) {
      tokenUtils.setToken(token)
      localStorage.setItem('userData', JSON.stringify(response.data.user || { correo: loginForm.value.correo }))
      
      console.log('Token guardado:', tokenUtils.getTokenPreview())
      
      showMessage('¡Inicio de sesión exitoso!', 'success')
      
      // Redirigir inmediatamente al dashboard
      console.log('Redirigiendo a dashboard...')
      await navigateTo('/', { replace: true })
    } else {
      console.error('No se recibió token en la respuesta', response.data)
      showMessage('Error: No se recibió token de autenticación')
    }
    
  } catch (error) {
    console.error('Error en login:', error)
    console.error('Detalles del error:', {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status
    })
    
    const errorMessage = error.response?.data?.message || 
                        error.response?.data?.error || 
                        `Error ${error.response?.status || 'de conexión'}: ${error.message}`
    
    showMessage(errorMessage)
  } finally {
    isLoading.value = false
  }
}

const handleRegister = async () => {
  isLoading.value = true
  message.value = { text: '', type: '' }
  
  if (!registerForm.value.location.lat || !registerForm.value.location.lng) {
    showMessage('Por favor selecciona tu ubicación en el mapa')
    isLoading.value = false
    return
  }
  
  try {
    const response = await $apiClient.post(`${API_ROUTES.AUTH}/registrar`, {
      nombre: registerForm.value.nombre,
      direccion: registerForm.value.direccion,
      correo: registerForm.value.correo,
      password: registerForm.value.password,
      latitude: registerForm.value.location.lat,
      longitude: registerForm.value.location.lng
    })
    
    showMessage('¡Registro exitoso! Ahora puedes iniciar sesión.', 'success')
    
    // Limpiar formulario y cambiar a login
    registerForm.value = {
      nombre: '',
      correo: '',
      direccion: '',
      password: '',
      location: { lat: null, lng: null }
    }
    
    setTimeout(() => {
      activeTab.value = 'login'
    }, 2000)
    
  } catch (error) {
    console.error('Error en registro:', error)
    showMessage(error.response?.data?.message || 'Error al registrarse')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.landing-page {
  background: linear-gradient(135deg, 
    #0f172a 0%, 
    #1e293b 50%,
    #334155 100%);
  position: relative;
  overflow-x: hidden;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.hero-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl) var(--spacing-lg);
  position: relative;
  width: 100%;
  box-sizing: border-box;
  flex: 1;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 20"><defs><radialGradient id="a" cx="50%" cy="0%" r="100%"><stop offset="0%" stop-color="rgba(59,130,246,0.15)"/><stop offset="100%" stop-color="rgba(6,182,212,0.1)"/></radialGradient></defs><rect width="100" height="20" fill="url(%23a)"/></svg>');
  animation: patternMove 20s linear infinite;
}

@keyframes patternMove {
  0% { transform: translateX(-100%) translateY(-50%); }
  100% { transform: translateX(100%) translateY(50%); }
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--spacing-xl);
  position: relative;
  z-index: 1;
  width: 100%;
  padding: 0 var(--spacing-lg);
  box-sizing: border-box;
  text-align: center;
}

.hero-text {
  order: 1;
  animation: slideInLeft 0.8s ease-out;
}

.auth-section {
  order: 2;
  animation: slideInRight 0.8s ease-out;
  max-width: 500px;
  width: 100%;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  background: linear-gradient(135deg, #60a5fa, #06b6d4);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: var(--spacing-lg);
  line-height: 1.1;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: #cbd5e1;
  margin-bottom: var(--spacing-xl);
  line-height: 1.6;
}

.hero-features {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  font-size: 1.1rem;
  color: #e2e8f0;
}

.feature-icon {
  font-size: 1.5rem;
}

.auth-section {
  animation: slideInRight 0.8s ease-out;
}

.auth-card {
  background: rgba(30, 41, 59, 0.8);
  backdrop-filter: blur(20px);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-xl);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(59, 130, 246, 0.4);
}

.auth-tabs {
  display: flex;
  margin-bottom: var(--spacing-xl);
  background: var(--background-accent);
  border-radius: var(--border-radius-lg);
  padding: 4px;
}

.tab-btn {
  flex: 1;
  padding: var(--spacing-md) var(--spacing-lg);
  border: none;
  background: transparent;
  border-radius: var(--border-radius-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  font-weight: 600;
  color: var(--text-secondary);
}

.tab-btn.active {
  background: var(--background-secondary);
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.form-group {
  position: relative;
}

.form-input {
  width: 100%;
  padding: var(--spacing-md) var(--spacing-md) var(--spacing-md) 3rem;
  border: 2px solid rgba(59, 130, 246, 0.3);
  border-radius: var(--border-radius-lg);
  font-size: 1rem;
  transition: all var(--transition-normal);
  background: rgba(30, 41, 59, 0.6);
  color: #e2e8f0;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.2);
}

.input-icon {
  position: absolute;
  left: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  color: #94a3b8;
}

.location-section {
  margin: var(--spacing-lg) 0;
  border-radius: var(--border-radius-lg);
  border: 2px solid var(--border-color);
  background: var(--background-secondary);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.location-section:hover {
  border-color: var(--primary-color);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
}

.location-label {
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
  padding: var(--spacing-md);
  background: var(--background-accent);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.95rem;
}

.map-skeleton {
  height: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  background: var(--background-accent);
  color: var(--text-secondary);
}

.skeleton-loader {
  width: 80%;
  height: 60%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--border-radius-md);
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

.auth-btn {
  width: 100%;
  padding: var(--spacing-lg);
  background: linear-gradient(135deg, var(--primary-color), var(--primary-hover));
  color: white;
  border: none;
  border-radius: var(--border-radius-lg);
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  overflow: hidden;
}

.auth-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.auth-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.auth-btn.loading {
  animation: pulse 1.5s infinite;
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.message {
  margin-top: var(--spacing-lg);
  padding: var(--spacing-md);
  border-radius: var(--border-radius-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
}

.message.success {
  background: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.message.error {
  background: rgba(239, 68, 68, 0.1);
  color: var(--error-color);
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.features-section {
  padding: var(--spacing-xl) var(--spacing-lg);
  background: var(--background-secondary);
  width: 100%;
  box-sizing: border-box;
  overflow: hidden;
  /* Evitar que la sección genere scroll interno */
  flex-shrink: 0;
}

.section-title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: var(--spacing-xl);
  color: var(--text-primary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  box-sizing: border-box;
}

.feature-card {
  text-align: center;
  padding: var(--spacing-lg);
  background: var(--background-card);
  border-radius: var(--border-radius-xl);
  box-shadow: var(--shadow-md);
  transition: all var(--transition-normal);
  border: 1px solid var(--border-color);
  /* Evitar que las cards generen altura extra */
  box-sizing: border-box;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-xl);
}

.feature-icon-large {
  font-size: 4rem;
  margin-bottom: var(--spacing-lg);
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: var(--spacing-md);
  color: var(--text-primary);
}

.feature-card p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Animaciones */
@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.fade-enter-active, .fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive - solo para pantallas MUY grandes permitir horizontal */
@media (min-width: 1600px) {
  .hero-content {
    display: grid;
    grid-template-columns: 1fr 450px;
    text-align: left;
    max-width: 1400px;
  }
  
  .hero-text {
    order: unset;
  }
  
  .auth-section {
    order: unset;
    max-width: unset;
  }
}

@media (max-width: 1599px) {
  .hero-content {
    display: flex;
    flex-direction: column;
    text-align: center;
    gap: var(--spacing-xl);
  }
  
  .auth-section {
    max-width: 500px;
    width: 100%;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.5rem;
  }
  
  .hero-subtitle {
    font-size: 1.2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .hero-section {
    padding: var(--spacing-lg);
  }
  
  .hero-content {
    gap: var(--spacing-lg);
    padding: 0 var(--spacing-md);
  }
  
  .features-section {
    padding: var(--spacing-lg) var(--spacing-md);
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .auth-card {
    padding: var(--spacing-lg);
  }
  
  .hero-section {
    padding: var(--spacing-md);
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }
  
  .feature-card {
    padding: var(--spacing-md);
  }
  
  .features-section {
    padding: var(--spacing-md);
  }
}
</style>

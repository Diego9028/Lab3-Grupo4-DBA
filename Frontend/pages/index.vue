<!-- filepath: e:\Universidad\BBDDA\Lab_3\Lab3-Grupo4-DBA\Frontend\pages\index.vue -->
<template>
  <div class="dashboard-container">
    <!-- Dashboard Principal -->
    <div class="welcome-section">
      <div class="welcome-card">
        <h1 class="welcome-title">
          🎉 ¡Bienvenido al Dashboard!
        </h1>
        <p class="welcome-subtitle">
          Aquí tienes acceso rápido a todas las funcionalidades de tu plataforma de delivery.
        </p>
      </div>
    </div>

    <!-- Quick Actions Grid -->
    <div class="quick-actions-grid">
      <NuxtLink to="/cliente" class="action-card hover-lift">
        <div class="action-icon">👥</div>
        <h3>Gestión de Clientes</h3>
        <p>Administra la información de tus clientes</p>
      </NuxtLink>

      <NuxtLink to="/opinionesClientes" class="action-card hover-lift">
        <div class="action-icon">💬</div>
        <h3>Opiniones de Clientes</h3>
        <p>Analiza feedback y detecta problemas</p>
      </NuxtLink>

      <NuxtLink to="/desempeno" class="action-card hover-lift">
        <div class="action-icon">📊</div>
        <h3>Análisis de Desempeño</h3>
        <p>Monitorea métricas y rendimiento</p>
      </NuxtLink>

      <NuxtLink to="/historialRepartidores" class="action-card hover-lift">
        <div class="action-icon">🚛</div>
        <h3>Historial Repartidores</h3>
        <p>Tramos frecuentes y actividad</p>
      </NuxtLink>

      <NuxtLink to="/zonaDensa" class="action-card hover-lift">
        <div class="action-icon">🗺️</div>
        <h3>Gestión de Zonas</h3>
        <p>Optimiza las zonas de entrega</p>
      </NuxtLink>

      <NuxtLink to="/urgencias" class="action-card hover-lift">
        <div class="action-icon">🚨</div>
        <h3>Gestión de Urgencias</h3>
        <p>Administra pedidos urgentes</p>
      </NuxtLink>

      <NuxtLink to="/puntoInteres" class="action-card hover-lift">
        <div class="action-icon">📍</div>
        <h3>Puntos de Interés</h3>
        <p>Explora ubicaciones estratégicas</p>
      </NuxtLink>

      <NuxtLink to="/empresasTop" class="action-card hover-lift">
        <div class="action-icon">🏆</div>
        <h3>Empresas Top</h3>
        <p>Ranking de mejores empresas</p>
      </NuxtLink>

      <NuxtLink to="/logs" class="action-card hover-lift">
        <div class="action-icon">📋</div>
        <h3>Logs del Sistema</h3>
        <p>Análisis de cambios rápidos</p>
      </NuxtLink>

      <NuxtLink to="/navegacion" class="action-card hover-lift">
        <div class="action-icon">🔍</div>
        <h3>Navegación Usuarios</h3>
        <p>Clientes sin conversión</p>
      </NuxtLink>

      <NuxtLink to="/pedidosMasLejanos" class="action-card hover-lift">
        <div class="action-icon">🎯</div>
        <h3>Análisis de Pedidos</h3>
        <p>Estudia patrones de delivery</p>
      </NuxtLink>
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  middleware: 'auth'
})

// Usar el composable común de autenticación
useAuthPage()
</script>

<style scoped>
/* Los estilos principales están ahora en el CSS global */
/* Solo estilos específicos de esta página si es necesario */
</style>

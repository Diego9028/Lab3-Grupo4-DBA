<template>
  <div class="container">
    <!-- Navbar solo en páginas que no sean landing -->
    <header v-if="!isLandingPage" class="navbar">
      <div class="logo">
        <h1 class="title">Mercado Libren't</h1>
      </div>

      <nav class="nav-main">
        <!-- Inicio (sin dropdown) -->
        <nuxt-link to="/" class="nav-item" @click="closeAllDropdowns">Dashboard</nuxt-link>

        <!-- Clientes -->
        <div class="nav-item dropdown" @mouseenter="openDropdown($event)" @mouseleave="closeDropdown($event)">
          <span>Clientes</span>
          <div class="dropdown-content" ref="clientesDropdown">
            <nuxt-link to="/cliente" @click="closeAllDropdowns">Lista Clientes</nuxt-link>
            <nuxt-link to="/clienteCobertura" @click="closeAllDropdowns">Cobertura</nuxt-link>
            <nuxt-link to="/clientesSinEmpresaCercana" @click="closeAllDropdowns">Sin Empresa Cercana</nuxt-link>
            <nuxt-link to="/clientesEnZona" @click="closeAllDropdowns">En Zona</nuxt-link>
            <nuxt-link to="/opinionesClientes" @click="closeAllDropdowns">Opiniones</nuxt-link>
          </div>
        </div>

        <!-- Pedidos & Delivery -->
        <div class="nav-item dropdown" @mouseenter="openDropdown($event)" @mouseleave="closeDropdown($event)">
          <span>Pedidos</span>
          <div class="dropdown-content" ref="pedidosDropdown">
            <nuxt-link to="/pedidos" @click="closeAllDropdowns">Gestión Pedidos</nuxt-link>
            <nuxt-link to="/pedidosMasLejanos" @click="closeAllDropdowns">Pedidos Lejanos</nuxt-link>
            <nuxt-link to="/pedidosMultiplesZonas" @click="closeAllDropdowns">Multi-Zonas</nuxt-link>
            <nuxt-link to="/urgencias" @click="closeAllDropdowns">Urgencias</nuxt-link>
            <nuxt-link to="/historialRepartidores" @click="closeAllDropdowns">Historial Repartidores</nuxt-link>
          </div>
        </div>

        <!-- Empresas & Zonas -->
        <div class="nav-item dropdown" @mouseenter="openDropdown($event)" @mouseleave="closeDropdown($event)">
          <span>Empresas</span>
          <div class="dropdown-content" ref="empresasDropdown">
            <nuxt-link to="/empresasTop" @click="closeAllDropdowns">Top Empresas</nuxt-link>
            <nuxt-link to="/cercanosEmpresa" @click="closeAllDropdowns">Cercanos Empresa</nuxt-link>
            <nuxt-link to="/zonaDensa" @click="closeAllDropdowns">Zona Densa</nuxt-link>
            <nuxt-link to="/puntoInteres" @click="closeAllDropdowns">Puntos de Interés</nuxt-link>
          </div>
        </div>

        <!-- Análisis & Reportes -->
        <div class="nav-item dropdown" @mouseenter="openDropdown($event)" @mouseleave="closeDropdown($event)">
          <span>Análisis</span>
          <div class="dropdown-content" ref="reportesDropdown">
            <nuxt-link to="/desempeno" @click="closeAllDropdowns">Desempeño</nuxt-link>
            <nuxt-link to="/distanciaRecorrida" @click="closeAllDropdowns">Distancia Recorrida</nuxt-link>
            <nuxt-link to="/navegacion" @click="closeAllDropdowns">Navegación Usuarios</nuxt-link>
            <nuxt-link to="/logs" @click="closeAllDropdowns">Logs Sistema</nuxt-link>
          </div>
        </div>

        <!-- Cerrar Sesión -->
        <a href="#" @click.prevent="logoutUser" class="nav-item logout-link">Cerrar Sesión</a>
      </nav>
    </header>

    <main class="main-content">
      <nuxt-page />
    </main>
  </div>
</template>

<script>
export default {
  methods: {
    openDropdown(event) {
      const dropdown = event.currentTarget.querySelector('.dropdown-content')
      this.closeAllDropdowns()
      dropdown.style.display = 'block'
    },
    closeDropdown(event) {
      const dropdown = event.currentTarget.querySelector('.dropdown-content')
      // Pequeño retraso para permitir hacer clic en los enlaces
      setTimeout(() => {
        dropdown.style.display = 'none'
      }, 200)
    },
    closeAllDropdowns() {
      document.querySelectorAll('.dropdown-content').forEach(dropdown => {
        dropdown.style.display = 'none'
      })
    }
  }
}
</script>


<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { handleLogout } from './src/services/authService'
import { useNuxtApp } from '#app'
import { useRouter } from 'vue-router'

const { $apiClient } = useNuxtApp()
const router = useRouter()
const route = useRoute()

// Detectar si estamos en la página de landing
const isLandingPage = computed(() => {
  return route.path === '/landing'
})

const logoutUser = async () => {
  await handleLogout($apiClient, router)
}
</script>

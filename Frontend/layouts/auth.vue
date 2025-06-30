<template>
  <div>
    <slot />
  </div>
</template>

<style scoped>
/* Layout limpio sin navbar para páginas de autenticación */
div {
  min-height: 100vh;
  background: var(--background-primary);
}
</style>

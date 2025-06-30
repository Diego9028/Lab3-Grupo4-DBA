<template>
  <div class="loading-container">
    <div class="loading-spinner" :class="size"></div>
    <h3 v-if="title">{{ title }}</h3>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script setup>
const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  }
})
</script>

<style scoped>
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

.loading-spinner.small {
  width: 20px;
  height: 20px;
  border-width: 2px;
}

.loading-spinner.large {
  width: 60px;
  height: 60px;
  border-width: 6px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

h3 {
  margin: var(--spacing-md) 0 var(--spacing-sm) 0;
  color: var(--text-primary);
}

p {
  margin: 0;
  opacity: 0.8;
}
</style>

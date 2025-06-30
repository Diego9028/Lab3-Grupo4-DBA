<template>
  <div class="error-container">
    <div class="error-icon">❌</div>
    <h3>{{ title || 'Error' }}</h3>
    <p>{{ message }}</p>
    <button v-if="showRetry" @click="$emit('retry')" class="btn-primary">
      🔄 Reintentar
    </button>
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
    required: true
  },
  showRetry: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['retry'])
</script>

<style scoped>
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

h3 {
  color: #ef4444;
  margin: 0 0 var(--spacing-sm) 0;
}

p {
  color: var(--text-secondary);
  margin: 0 0 var(--spacing-lg) 0;
}

.btn-primary {
  background: var(--accent-primary);
  color: white;
  border: none;
  border-radius: var(--border-radius-md);
  padding: var(--spacing-md) var(--spacing-lg);
  cursor: pointer;
  font-weight: 600;
  transition: all var(--transition-medium);
}

.btn-primary:hover {
  background: var(--accent-secondary);
  transform: translateY(-1px);
}
</style>

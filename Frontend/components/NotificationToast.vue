<template>
  <teleport to="body">
    <transition name="notification-fade">
      <div v-if="show" :class="['notification-container', type]">
        <div class="notification-content">
          <div class="notification-icon">
            <span v-if="type === 'error'">❌</span>
            <span v-else-if="type === 'warning'">⚠️</span>
            <span v-else-if="type === 'success'">✅</span>
            <span v-else>ℹ️</span>
          </div>
          <div class="notification-message">
            <h4 v-if="title" class="notification-title">{{ title }}</h4>
            <p class="notification-text">{{ message }}</p>
          </div>
          <button @click="close" class="notification-close">
            <span>✕</span>
          </button>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
const props = defineProps({
  show: {
    type: Boolean,
    required: true
  },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['error', 'warning', 'success', 'info'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    required: true
  },
  duration: {
    type: Number,
    default: 5000
  }
})

const emit = defineEmits(['close'])

const close = () => {
  emit('close')
}

// Auto-close after duration
watch(() => props.show, (newShow) => {
  if (newShow && props.duration > 0) {
    setTimeout(() => {
      close()
    }, props.duration)
  }
})
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  max-width: 400px;
  min-width: 300px;
}

.notification-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.notification-container.error .notification-content {
  background: linear-gradient(135deg, 
    rgba(239, 68, 68, 0.9) 0%, 
    rgba(185, 28, 28, 0.9) 100%);
  border-color: rgba(239, 68, 68, 0.3);
}

.notification-container.warning .notification-content {
  background: linear-gradient(135deg, 
    rgba(245, 158, 11, 0.9) 0%, 
    rgba(180, 83, 9, 0.9) 100%);
  border-color: rgba(245, 158, 11, 0.3);
}

.notification-container.success .notification-content {
  background: linear-gradient(135deg, 
    rgba(34, 197, 94, 0.9) 0%, 
    rgba(21, 128, 61, 0.9) 100%);
  border-color: rgba(34, 197, 94, 0.3);
}

.notification-container.info .notification-content {
  background: linear-gradient(135deg, 
    rgba(59, 130, 246, 0.9) 0%, 
    rgba(29, 78, 216, 0.9) 100%);
  border-color: rgba(59, 130, 246, 0.3);
}

.notification-icon {
  font-size: 20px;
  margin-top: 2px;
}

.notification-message {
  flex: 1;
  color: white;
}

.notification-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
}

.notification-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.4;
  opacity: 0.95;
}

.notification-close {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  opacity: 0.7;
  transition: opacity 0.2s;
  font-size: 14px;
  line-height: 1;
}

.notification-close:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
}

.notification-fade-enter-active,
.notification-fade-leave-active {
  transition: all 0.3s ease;
}

.notification-fade-enter-from {
  opacity: 0;
  transform: translateX(100%) translateY(-20px);
}

.notification-fade-leave-to {
  opacity: 0;
  transform: translateX(100%) translateY(-20px);
}
</style>

import { ref } from 'vue'

interface Notification {
  id: string
  type: 'error' | 'warning' | 'success' | 'info'
  title?: string
  message: string
  duration?: number
}

const notifications = ref<Notification[]>([])

export const useNotifications = () => {
  const addNotification = (notification: Omit<Notification, 'id'>) => {
    const id = Date.now().toString() + Math.random().toString(36).substr(2, 9)
    const newNotification = { ...notification, id }
    notifications.value.push(newNotification)
    
    // Auto-remove after duration
    if (notification.duration !== 0) {
      setTimeout(() => {
        removeNotification(id)
      }, notification.duration || 5000)
    }
    
    return id
  }

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  const showError = (message: string, title?: string) => {
    return addNotification({ type: 'error', message, title })
  }

  const showWarning = (message: string, title?: string) => {
    return addNotification({ type: 'warning', message, title })
  }

  const showSuccess = (message: string, title?: string) => {
    return addNotification({ type: 'success', message, title })
  }

  const showInfo = (message: string, title?: string) => {
    return addNotification({ type: 'info', message, title })
  }

  const clear = () => {
    notifications.value = []
  }

  return {
    notifications: readonly(notifications),
    addNotification,
    removeNotification,
    showError,
    showWarning,
    showSuccess,
    showInfo,
    clear
  }
}

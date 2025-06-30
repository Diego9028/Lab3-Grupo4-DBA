import { ref, onMounted } from 'vue'

export const useClientOnly = () => {
  const isClient = ref(false)

  onMounted(() => {
    isClient.value = true
  })

  return {
    isClient
  }
}

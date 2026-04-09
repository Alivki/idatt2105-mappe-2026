import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

export function useCanManage() {
  const auth = useAuthStore()
  return computed(() => auth.role === 'ADMIN' || auth.role === 'MANAGER')
}

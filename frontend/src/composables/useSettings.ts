import { useMutation, useQueryClient } from '@tanstack/vue-query'
import api from '@/lib/api'
import { useAuthStore } from '@/stores/auth'
import type { UserResponse } from '@/types/auth'
import router from '@/router'

export function useUpdateEmailNotificationsMutation() {
  const auth = useAuthStore()

  return useMutation({
    mutationFn: (enabled: boolean) =>
      api.patch<UserResponse>('/users/me/email-notifications', { enabled }).then((r) => r.data),
    onSuccess: (data) => {
      if (auth.user) {
        auth.user = data
      }
    },
  })
}

export function useDeleteAccountMutation() {
  const auth = useAuthStore()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => api.delete('/users/me'),
    onSuccess: () => {
      auth.clearAuth()
      queryClient.clear()
      router.push('/login')
    },
  })
}

export function useDeleteOrganizationMutation() {
  const auth = useAuthStore()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => api.delete(`/organizations/${auth.organizationId}`),
    onSuccess: () => {
      auth.clearAuth()
      queryClient.clear()
      router.push('/login')
    },
  })
}

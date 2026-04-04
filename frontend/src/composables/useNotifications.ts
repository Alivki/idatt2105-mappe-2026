import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import api from '@/lib/api'
import type { NotificationPage, UnreadCountResponse } from '@/types/notification'
import { computed, type Ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

function useOrgScopedKey(base: string[]) {
  const auth = useAuthStore()
  return computed(() => [...base, auth.organizationId])
}

const notificationsBase = ['notifications']
const unreadCountBase = ['notifications', 'unread-count']

export function useUnreadCountQuery() {
  const queryKey = useOrgScopedKey(unreadCountBase)

  return useQuery({
    queryKey,
    queryFn: () => api.get<UnreadCountResponse>('/notifications/unread-count').then((r) => r.data),
    refetchInterval: 15000,
  })
}

export function useNotificationsQuery(page: Ref<number>) {
  const auth = useAuthStore()
  return useQuery({
    queryKey: computed(() => [...notificationsBase, auth.organizationId, page.value]),
    queryFn: () =>
      api
        .get<NotificationPage>('/notifications', { params: { page: page.value, size: 20 } })
        .then((r) => r.data),
  })
}

export function useMarkAsReadMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (notificationId: number) =>
      api.patch(`/notifications/${notificationId}/read`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationsBase })
    },
  })
}

export function useMarkAllAsReadMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: () => api.post('/notifications/mark-all-read'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: notificationsBase })
    },
  })
}

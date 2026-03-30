import { computed, type Ref } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import api from '@/lib/api'
import type { CreateDeviationRequest, Deviation, OrganizationMember } from '@/types/deviation'

const deviationsQueryKey = ['deviations']
const membersQueryKey = ['members']

export function useDeviationsQuery() {
  return useQuery({
    queryKey: deviationsQueryKey,
    queryFn: () => api.get<Deviation[]>('/deviations').then((r) => r.data),
  })
}

export function useCreateDeviationMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CreateDeviationRequest) =>
      api.post<Deviation>('/deviations', payload).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: deviationsQueryKey })
    },
  })
}

export function useOrganizationMembersQuery(enabled: Ref<boolean>) {
  const queryEnabled = computed(() => enabled.value)

  return useQuery({
    queryKey: membersQueryKey,
    queryFn: () => api.get<OrganizationMember[]>('/users').then((r) => r.data),
    enabled: queryEnabled,
  })
}

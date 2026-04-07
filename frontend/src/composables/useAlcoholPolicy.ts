import { computed } from 'vue'
import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import api from '@/lib/api'
import type { AlcoholPolicy, CreateAlcoholPolicyRequest } from '@/types/alcoholPolicy'

export const alcoholPolicyExistsQueryKey = ['alcohol-policy-exists']
export const alcoholPolicyQueryKey = ['alcohol-policy']

export function useAlcoholPolicyExistsQuery() {
  return useQuery({
    queryKey: alcoholPolicyExistsQueryKey,
    queryFn: async () => {
      try {
        const r = await api.get<{ exists: boolean }>('/alcohol-policy/exists')
        return r.data.exists
      } catch {
        return false
      }
    },
    retry: false,
  })
}

export function useAlcoholPolicyQuery(enabled: () => boolean) {
  return useQuery({
    queryKey: alcoholPolicyQueryKey,
    queryFn: () => api.get<AlcoholPolicy>('/alcohol-policy').then((r) => r.data),
    enabled: computed(enabled),
  })
}

export function useUpsertAlcoholPolicyMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateAlcoholPolicyRequest) =>
      api.post<AlcoholPolicy>('/alcohol-policy', payload).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: alcoholPolicyExistsQueryKey })
      qc.invalidateQueries({ queryKey: alcoholPolicyQueryKey })
    },
  })
}

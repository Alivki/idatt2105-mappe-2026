import { useQuery } from '@tanstack/vue-query'
import api from '@/lib/api'
import type { Deviation } from '@/types/deviation'

const deviationsQueryKey = ['deviations']

export function useDeviationsQuery() {
  return useQuery({
    queryKey: deviationsQueryKey,
    queryFn: () => api.get<Deviation[]>('/deviations').then((r) => r.data),
  })
}

import { useMutation, useQueryClient } from '@tanstack/vue-query'
import api from '@/lib/api'
import type { HaccpSetupRequest, HaccpSetupResponse } from '@/types/haccp-setup'

export function useGenerateChecklistsMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: HaccpSetupRequest) =>
      api.post<HaccpSetupResponse>('/haccp-setup/generate', payload).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['checklists'] })
      queryClient.invalidateQueries({ queryKey: ['checklists', 'stats'] })
    },
  })
}

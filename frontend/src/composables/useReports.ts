import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import api from '@/lib/api'
import type {
  GenerateReportRequest,
  GeneratedReportResponse,
  ReportPreviewResponse,
} from '@/types/report'

const reportsQueryKey = ['reports']

export function useReportsQuery() {
  return useQuery({
    queryKey: reportsQueryKey,
    queryFn: () => api.get<GeneratedReportResponse[]>('/reports').then((r) => r.data),
  })
}

export function usePreviewReportMutation() {
  return useMutation({
    mutationFn: (payload: GenerateReportRequest) =>
      api.post<ReportPreviewResponse>('/reports/preview', payload).then((r) => r.data),
  })
}

export function useGenerateReportMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: GenerateReportRequest) =>
      api.post<GeneratedReportResponse>('/reports/generate', payload).then((r) => r.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reportsQueryKey })
    },
  })
}

export function useDeleteReportMutation() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (reportId: number) => api.delete(`/reports/${reportId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: reportsQueryKey })
    },
  })
}

export function useDownloadReport() {
  return useMutation({
    mutationFn: async (reportId: number) => {
      const { data } = await api.get<{ url: string }>(`/reports/${reportId}/download`)
      return data.url
    },
  })
}

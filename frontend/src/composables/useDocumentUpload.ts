import { useMutation } from '@tanstack/vue-query'
import api from '@/lib/api'
import type { DocumentUploadResponse, DocumentUrlResponse } from '@/types/alcoholPolicy'

export function useDocumentUploadMutation() {
  return useMutation({
    mutationFn: (file: File) => {
      const formData = new FormData()
      formData.append('file', file)
      return api
        .post<DocumentUploadResponse>('/documents/upload', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        })
        .then((r) => r.data)
    },
  })
}

export function useDocumentUrlQuery(id: number) {
  return api.get<DocumentUrlResponse>(`/documents/${id}/url`).then((r) => r.data)
}

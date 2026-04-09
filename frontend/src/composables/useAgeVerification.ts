import { useMutation, useQuery, useQueryClient } from '@tanstack/vue-query'
import { computed, type Ref } from 'vue'
import api from '@/lib/api'
import type {
  ShiftResponse,
  ShiftDetailResponse,
  DailySummaryResponse,
  DayDetailResponse,
  StatsResponse,
  UpdateIdCheckCountRequest,
  CreateShiftDeviationRequest,
  ShiftDeviationResponse,
} from '@/types/ageVerification'

export const ageVerificationQueryKey = ['age-verification']
export const activeShiftQueryKey = [...ageVerificationQueryKey, 'active-shift']

export function useActiveShiftQuery() {
  return useQuery({
    queryKey: activeShiftQueryKey,
    queryFn: () =>
      api.get<ShiftDetailResponse | null>('/age-verification/shifts/active').then((r) => r.data),
    refetchInterval: 30000,
  })
}

export function useStartShiftMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: () =>
      api.post<ShiftResponse>('/age-verification/shifts/start').then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: activeShiftQueryKey })
    },
  })
}

export function useUpdateIdCheckCountMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: UpdateIdCheckCountRequest }) =>
      api.patch<ShiftResponse>(`/age-verification/shifts/${id}/count`, payload).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: activeShiftQueryKey })
    },
  })
}

export function useCreateShiftDeviationMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: CreateShiftDeviationRequest }) =>
      api
        .post<ShiftDeviationResponse>(`/age-verification/shifts/${id}/deviations`, payload)
        .then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: activeShiftQueryKey })
    },
  })
}

export function useEndShiftMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) =>
      api.post<ShiftResponse>(`/age-verification/shifts/${id}/end`).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: activeShiftQueryKey })
      qc.invalidateQueries({ queryKey: ageVerificationQueryKey })
    },
  })
}

export function useReopenShiftMutation() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) =>
      api.post<ShiftResponse>(`/age-verification/shifts/${id}/reopen`).then((r) => r.data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: activeShiftQueryKey })
      qc.invalidateQueries({ queryKey: ageVerificationQueryKey })
    },
  })
}

export function useDailySummariesQuery(from: Ref<string>, to: Ref<string>) {
  return useQuery({
    queryKey: computed(() => [...ageVerificationQueryKey, 'daily-summary', from.value, to.value]),
    queryFn: () =>
      api
        .get<DailySummaryResponse[]>('/age-verification/daily-summary', {
          params: { from: from.value, to: to.value },
        })
        .then((r) => r.data),
  })
}

export function useDayDetailQuery(date: Ref<string>, enabled?: Ref<boolean>) {
  return useQuery({
    queryKey: computed(() => [...ageVerificationQueryKey, 'day-detail', date.value]),
    queryFn: () =>
      api.get<DayDetailResponse>(`/age-verification/daily-summary/${date.value}`).then((r) => r.data),
    enabled: computed(() => !!date.value && (enabled?.value ?? true)),
  })
}

export function useStatsQuery(from: Ref<string>, to: Ref<string>) {
  return useQuery({
    queryKey: computed(() => [...ageVerificationQueryKey, 'stats', from.value, to.value]),
    queryFn: () =>
      api
        .get<StatsResponse>('/age-verification/stats', {
          params: { from: from.value, to: to.value },
        })
        .then((r) => r.data),
  })
}

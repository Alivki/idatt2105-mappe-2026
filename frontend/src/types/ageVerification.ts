import type { AlcoholDeviationType } from './deviation'

export type ShiftStatus = 'ACTIVE' | 'COMPLETED'

export interface ShiftResponse {
  id: number
  organizationId: number
  userId: number
  userName: string
  shiftDate: string
  startedAt: string
  endedAt: string | null
  idsCheckedCount: number
  signedOff: boolean
  signedOffAt: string | null
  status: ShiftStatus
  deviationCount: number
  createdAt: string
  updatedAt: string
}

export interface ShiftDeviationResponse {
  id: number
  deviationType: AlcoholDeviationType
  description: string
  reportedAt: string
}

export interface ShiftDetailResponse {
  shift: ShiftResponse
  deviations: ShiftDeviationResponse[]
}

export interface UpdateIdCheckCountRequest {
  idsCheckedCount: number
}

export interface CreateShiftDeviationRequest {
  deviationType: AlcoholDeviationType
  description?: string
}

export interface DailySummaryResponse {
  date: string
  shiftCount: number
  totalIdsChecked: number
  totalDeviations: number
}

export interface DayDetailResponse {
  date: string
  shifts: ShiftDetailResponse[]
  totalIdsChecked: number
  totalDeviations: number
  deviationsByType: Record<string, number>
}

export interface StatsResponse {
  periodFrom: string
  periodTo: string
  totalShifts: number
  totalIdsChecked: number
  totalDeviations: number
  avgIdsPerShift: number
  dailySummaries: DailySummaryResponse[]
}

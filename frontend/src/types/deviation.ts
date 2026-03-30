export type DeviationModule = 'IK_MAT' | 'IK_ALKOHOL'
export type DeviationSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type DeviationStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED'

export interface Deviation {
  id: number
  organizationId: number
  module: DeviationModule
  title: string
  description: string
  immediateAction: string | null
  severity: DeviationSeverity
  status: DeviationStatus
  reportedByUserId: number
  reportedByUserName: string
  assignedToUserId: number | null
  assignedToUserName: string | null
  reportedAt: string
  resolvedAt: string | null
  closedAt: string | null
  createdAt: string
  updatedAt: string
}

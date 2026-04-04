export type NotificationType =
  | 'BEVILLING_EXPIRY'
  | 'TRAINING_EXPIRY'
  | 'DEVIATION_CREATED'
  | 'DEVIATION_ASSIGNED'
  | 'CHECKLIST_OVERDUE'
  | 'LOG_REMINDER'
  | 'SYSTEM_ALERT'

export type ReferenceType =
  | 'ALCOHOL_POLICY'
  | 'TRAINING_LOG'
  | 'DEVIATION'
  | 'CHECKLIST'
  | 'AGE_VERIFICATION_LOG'

export interface Notification {
  id: number
  title: string
  message: string
  type: NotificationType
  referenceType: ReferenceType | null
  referenceId: number | null
  isRead: boolean
  createdAt: string
}

export interface UnreadCountResponse {
  count: number
}

export interface NotificationPage {
  content: Notification[]
  totalPages: number
  totalElements: number
  last: boolean
  number: number
}

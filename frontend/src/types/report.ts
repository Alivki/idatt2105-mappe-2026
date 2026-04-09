export interface GenerateReportRequest {
  periodFrom: string
  periodTo: string
  title?: string
  sections: ReportSectionsConfig
  signOffName?: string
  signOffTitle?: string
  signOffComments?: string
}

export interface ReportSectionsConfig {
  includeComplianceSummary: boolean
  includeTemperatureLogs: boolean
  includeChecklists: boolean
  selectedChecklistIds?: number[]
  includeHaccpChecklists: boolean
  includeCorrectiveActions: boolean
  includeFoodDeviations: boolean
  includeAlcoholDeviations: boolean
  includeAgeVerification: boolean
  includeTrainingOverview: boolean
  includeLicenseInfo: boolean
  includeSignOff: boolean
}

export interface ReportHeader {
  organizationName: string
  orgNumber: string | null
  periodFrom: string
  periodTo: string
  generatedDate: string
  generatedByName: string
  generatedByRole: string | null
}

export interface ComplianceSummary {
  complianceRate: number
  totalTasks: number
  completedTasks: number
  overdueTasks: number
  openDeviations: number
  closedDeviations: number
  temperatureDeviations: number
  alcoholIncidents: number
}

export interface TemperatureLogEntry {
  location: string
  avgTemp: number
  minTemp: number
  maxTemp: number
  deviationCount: number
  status: string
}

export interface ChecklistCompletion {
  id: number
  name: string
  frequency: string
  completionRate: number
  completedCount: number
  expectedCount: number
}

export interface HaccpSection {
  checklists: HaccpChecklistEntry[]
}

export interface HaccpChecklistEntry {
  name: string
  description: string | null
  frequency: string
  completionRate: number
  items: HaccpChecklistItemEntry[]
}

export interface HaccpChecklistItemEntry {
  title: string
  description: string | null
  completed: boolean
  completedAt: string | null
}

export interface CorrectiveActionEntry {
  id: string
  date: string
  issue: string
  actionTaken: string | null
  status: string
}

export interface DeviationEntry {
  id: number
  date: string
  type: string
  severity: string
  description: string
  immediateAction: string | null
  status: string
  reportedBy: string
}

export interface AgeVerificationEntry {
  date: string
  shift: string
  totalChecks: number
  idRequested: number
  refusals: number
}

export interface TrainingEntry {
  name: string
  role: string | null
  certification: string
  expires: string | null
  status: string
}

export interface LicenseInfoSection {
  bevillingNumber: string | null
  bevillingValidTo: string | null
  holder: string | null
  styrerName: string | null
  stedfortrederName: string | null
  kunnskapsproveType: string | null
  kunnskapsproveCandidateName: string | null
  kunnskapsproveBirthDate: string | null
  kunnskapsprovePassedDate: string | null
  kunnskapsproveMunicipality: string | null
}

export interface SignOffEntry {
  name: string | null
  title: string | null
  date: string
  comments: string | null
}

export interface ReportPreviewResponse {
  header: ReportHeader
  complianceSummary: ComplianceSummary | null
  temperatureLogs: TemperatureLogEntry[] | null
  checklists: ChecklistCompletion[] | null
  haccpChecklists: HaccpSection | null
  correctiveActions: CorrectiveActionEntry[] | null
  foodDeviations: DeviationEntry[] | null
  alcoholDeviations: DeviationEntry[] | null
  ageVerificationLogs: AgeVerificationEntry[] | null
  trainingOverview: TrainingEntry[] | null
  licenseInfo: LicenseInfoSection | null
  signOff: SignOffEntry | null
}

export interface GeneratedReportResponse {
  id: number
  title: string
  periodFrom: string
  periodTo: string
  generatedByUserName: string
  generatedAt: string
  documentId: number | null
  downloadUrl: string | null
}

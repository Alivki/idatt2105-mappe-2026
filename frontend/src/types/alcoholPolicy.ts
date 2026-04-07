export type KnowledgeTestType = 'SALG' | 'SKJENKE' | 'BOTH'
export type AgeCheckLimit = 'UNDER_25' | 'UNDER_23'
export type IdType = 'PASS' | 'FORERKORT' | 'BANKKORT' | 'NASJONALT_ID'

export interface AlcoholPolicy {
  id: number
  bevillingNumber: string | null
  bevillingValidUntil: string | null
  styrerName: string | null
  stedfortrederName: string | null
  bevillingDocumentId: number | null
  kunnskapsproveCandidateName: string | null
  kunnskapsproveBirthDate: string | null
  kunnskapsproveType: KnowledgeTestType | null
  kunnskapsproveMunicipality: string | null
  kunnskapsprovePassedDate: string | null
  kunnskapsproveDocumentId: number | null
  ageCheckLimit: AgeCheckLimit
  acceptedIdTypes: IdType[]
  doubtRoutine: string | null
  intoxicationSigns: string | null
  refusalProcedure: string | null
}

export interface CreateAlcoholPolicyRequest {
  bevillingNumber?: string | null
  bevillingValidUntil?: string | null
  styrerName?: string | null
  stedfortrederName?: string | null
  bevillingDocumentId?: number | null
  kunnskapsproveCandidateName?: string | null
  kunnskapsproveBirthDate?: string | null
  kunnskapsproveType?: KnowledgeTestType | null
  kunnskapsproveMunicipality?: string | null
  kunnskapsprovePassedDate?: string | null
  kunnskapsproveDocumentId?: number | null
  ageCheckLimit?: AgeCheckLimit | null
  acceptedIdTypes?: IdType[] | null
  doubtRoutine?: string | null
  intoxicationSigns?: string | null
  refusalProcedure?: string | null
}

export interface DocumentUploadResponse {
  id: number
  fileName: string
  s3Key: string
  contentType: string
  url: string
}

export interface DocumentUrlResponse {
  id: number
  url: string
}

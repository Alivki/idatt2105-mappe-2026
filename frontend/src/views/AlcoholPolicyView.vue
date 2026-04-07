<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { z } from 'zod'
import axios from 'axios'
import {
  ScrollText,
  Award,
  Pencil,
  FileText,
  ExternalLink,
  Upload,
  X,
  Loader2,
  ShieldCheck,
  CalendarDays,
  User,
  MapPin,
  Hash,
  ScanEye,
  HandMetal,
  CircleHelp,
  CreditCard,
  Printer,
} from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import AppLayout from '@/components/layout/AppLayout.vue'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import { CalendarDate } from '@internationalized/date'
import type { DateValue } from '@internationalized/date'
import {
  useAlcoholPolicyExistsQuery,
  useAlcoholPolicyQuery,
  useUpsertAlcoholPolicyMutation,
} from '@/composables/useAlcoholPolicy'
import { useDocumentUploadMutation, useDocumentUrlQuery } from '@/composables/useDocumentUpload'
import type {
  AgeCheckLimit,
  IdType,
  KnowledgeTestType,
  CreateAlcoholPolicyRequest,
} from '@/types/alcoholPolicy'

const existsQuery = useAlcoholPolicyExistsQuery()
const policyExists = computed(() => existsQuery.data.value === true)
const policyQuery = useAlcoholPolicyQuery(() => policyExists.value)
const policy = computed(() => policyQuery.data.value ?? null)
const upsertMutation = useUpsertAlcoholPolicyMutation()
const bevillingUpload = useDocumentUploadMutation()
const kunnskapsproveUpload = useDocumentUploadMutation()

const editing = ref(false)
const showForm = computed(() => {
  if (existsQuery.isLoading.value) return false
  if (editing.value) return true
  return !policyExists.value
})
const showCard = computed(() => policyExists.value && !editing.value && !!policy.value)

const form = ref<CreateAlcoholPolicyRequest>({})
const submitting = ref(false)
const errors = ref<Record<string, string>>({})
const bevillingNumberSchema = z.string().min(1, 'Bevillingsnummer er påkrevd')
const styrerNameSchema = z.string().min(1, 'Styrer er påkrevd')
const stedfortrederNameSchema = z.string().min(1, 'Stedfortreder er påkrevd')
const candidateNameSchema = z.string().min(1, 'Kandidatens navn er påkrevd')
const municipalitySchema = z.string().min(1, 'Kommune er påkrevd')
const doubtRoutineSchema = z.string().min(1, 'Rutine ved tvil er påkrevd')
const intoxicationSignsSchema = z.string().min(1, 'Beskriv tegn på beruselse')
const refusalProcedureSchema = z.string().min(1, 'Beskriv prosedyre for nekt')
const bevillingValidUntilSchema = z.custom<DateValue>((v) => !!v, 'Gyldig til-dato er påkrevd')
const kunnskapsproveBirthDateSchema = z.custom<DateValue>((v) => !!v, 'Fødselsdato er påkrevd')
const kunnskapsprovePassedDateSchema = z.custom<DateValue>((v) => !!v, 'Bestått dato er påkrevd')
const kunnskapsproveTypeSchema = z.string().min(1, 'Velg type kunnskapsprøve')
const bevillingValidUntilDate = ref<DateValue | undefined>()
const kunnskapsproveBirthDateVal = ref<DateValue | undefined>()
const kunnskapsprovePassedDateVal = ref<DateValue | undefined>()

function stringToCalendarDate(str: string | null | undefined): CalendarDate | undefined {
  if (!str) return undefined
  const [y, m, d] = str.split('-').map(Number)
  return new CalendarDate(y!, m!, d!)
}

function dateValueToString(dv: DateValue | undefined): string | undefined {
  if (!dv) return undefined
  return `${dv.year}-${String(dv.month).padStart(2, '0')}-${String(dv.day).padStart(2, '0')}`
}

watch(bevillingValidUntilDate, (v) => { form.value.bevillingValidUntil = dateValueToString(v) ?? null })
watch(kunnskapsproveBirthDateVal, (v) => { form.value.kunnskapsproveBirthDate = dateValueToString(v) ?? null })
watch(kunnskapsprovePassedDateVal, (v) => { form.value.kunnskapsprovePassedDate = dateValueToString(v) ?? null })

const bevillingFileName = ref<string | null>(null)
const kunnskapsproveFileName = ref<string | null>(null)
const bevillingUploading = ref(false)
const kunnskapsproveUploading = ref(false)
const bevillingFileRef = ref<HTMLInputElement | null>(null)
const kunnskapsproveFileRef = ref<HTMLInputElement | null>(null)

watch(
  () => editing.value,
  (isEditing) => {
    if (isEditing && policy.value) {
      form.value = {
        bevillingNumber: policy.value.bevillingNumber,
        bevillingValidUntil: policy.value.bevillingValidUntil,
        styrerName: policy.value.styrerName,
        stedfortrederName: policy.value.stedfortrederName,
        bevillingDocumentId: policy.value.bevillingDocumentId,
        kunnskapsproveCandidateName: policy.value.kunnskapsproveCandidateName,
        kunnskapsproveBirthDate: policy.value.kunnskapsproveBirthDate,
        kunnskapsproveType: policy.value.kunnskapsproveType,
        kunnskapsproveMunicipality: policy.value.kunnskapsproveMunicipality,
        kunnskapsprovePassedDate: policy.value.kunnskapsprovePassedDate,
        kunnskapsproveDocumentId: policy.value.kunnskapsproveDocumentId,
        ageCheckLimit: policy.value.ageCheckLimit,
        acceptedIdTypes: [...policy.value.acceptedIdTypes],
        doubtRoutine: policy.value.doubtRoutine,
        intoxicationSigns: policy.value.intoxicationSigns,
        refusalProcedure: policy.value.refusalProcedure,
      }
      bevillingFileName.value = null
      kunnskapsproveFileName.value = null
      bevillingValidUntilDate.value = stringToCalendarDate(policy.value.bevillingValidUntil)
      kunnskapsproveBirthDateVal.value = stringToCalendarDate(policy.value.kunnskapsproveBirthDate)
      kunnskapsprovePassedDateVal.value = stringToCalendarDate(policy.value.kunnskapsprovePassedDate)
    }
  },
)

function resetForm() {
  form.value = {}
  bevillingFileName.value = null
  kunnskapsproveFileName.value = null
  bevillingValidUntilDate.value = undefined
  kunnskapsproveBirthDateVal.value = undefined
  kunnskapsprovePassedDateVal.value = undefined
  errors.value = {}
  editing.value = false
}

function setAgeCheck(val: AgeCheckLimit) {
  form.value.ageCheckLimit = val
}

const allIdTypes: { value: IdType; label: string }[] = [
  { value: 'PASS', label: 'Pass' },
  { value: 'FORERKORT', label: 'Førerkort' },
  { value: 'BANKKORT', label: 'Bankkort m/bilde' },
  { value: 'NASJONALT_ID', label: 'Nasjonalt ID-kort' },
]

function toggleIdType(id: IdType) {
  const current = form.value.acceptedIdTypes ?? ['PASS', 'FORERKORT', 'BANKKORT', 'NASJONALT_ID']
  if (current.includes(id)) {
    form.value.acceptedIdTypes = current.filter((t) => t !== id)
  } else {
    form.value.acceptedIdTypes = [...current, id]
  }
}

function isIdTypeSelected(id: IdType): boolean {
  return (form.value.acceptedIdTypes ?? ['PASS', 'FORERKORT', 'BANKKORT', 'NASJONALT_ID']).includes(id)
}

function validateFile(file: File): boolean {
  const allowed = ['application/pdf', 'image/jpeg', 'image/png']
  if (!allowed.includes(file.type)) {
    toast.error('Kun PDF, JPG og PNG er tillatt')
    return false
  }
  return true
}

async function uploadBevillingFile(file: File) {
  if (!validateFile(file)) return
  bevillingUploading.value = true
  bevillingFileName.value = file.name
  try {
    const doc = await bevillingUpload.mutateAsync(file)
    form.value.bevillingDocumentId = doc.id
    toast.success(`Bevillingsdokument lastet opp: ${file.name}`)
  } catch {
    toast.error('Kunne ikke laste opp bevillingsdokument')
    bevillingFileName.value = null
    form.value.bevillingDocumentId = null
  } finally {
    bevillingUploading.value = false
  }
}

async function uploadKunnskapsproveFile(file: File) {
  if (!validateFile(file)) return
  kunnskapsproveUploading.value = true
  kunnskapsproveFileName.value = file.name
  try {
    const doc = await kunnskapsproveUpload.mutateAsync(file)
    form.value.kunnskapsproveDocumentId = doc.id
    toast.success(`Kunnskapsprøve-bevis lastet opp: ${file.name}`)
  } catch {
    toast.error('Kunne ikke laste opp kunnskapsprøve-bevis')
    kunnskapsproveFileName.value = null
    form.value.kunnskapsproveDocumentId = null
  } finally {
    kunnskapsproveUploading.value = false
  }
}

function onFileChange(e: Event, target: 'bevilling' | 'kunnskapsprove') {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (target === 'bevilling') uploadBevillingFile(file)
  else uploadKunnskapsproveFile(file)
}

function onDrop(e: DragEvent, target: 'bevilling' | 'kunnskapsprove') {
  e.preventDefault()
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  if (target === 'bevilling') uploadBevillingFile(file)
  else uploadKunnskapsproveFile(file)
}

function removeBevillingFile() {
  bevillingFileName.value = null
  form.value.bevillingDocumentId = null
}

function removeKunnskapsproveFile() {
  kunnskapsproveFileName.value = null
  form.value.kunnskapsproveDocumentId = null
}

function triggerFileInput(ref: HTMLInputElement | null) {
  ref?.click()
}

async function handleSubmit() {
  const newErrors: Record<string, string> = {}

  const bnResult = bevillingNumberSchema.safeParse((form.value.bevillingNumber ?? '').trim())
  if (!bnResult.success) newErrors.bevillingNumber = bnResult.error.issues[0]?.message ?? ''

  const bvResult = bevillingValidUntilSchema.safeParse(bevillingValidUntilDate.value)
  if (!bvResult.success) newErrors.bevillingValidUntil = bvResult.error.issues[0]?.message ?? ''

  const snResult = styrerNameSchema.safeParse((form.value.styrerName ?? '').trim())
  if (!snResult.success) newErrors.styrerName = snResult.error.issues[0]?.message ?? ''

  const sdResult = stedfortrederNameSchema.safeParse((form.value.stedfortrederName ?? '').trim())
  if (!sdResult.success) newErrors.stedfortrederName = sdResult.error.issues[0]?.message ?? ''

  const cnResult = candidateNameSchema.safeParse((form.value.kunnskapsproveCandidateName ?? '').trim())
  if (!cnResult.success) newErrors.candidateName = cnResult.error.issues[0]?.message ?? ''

  const kbResult = kunnskapsproveBirthDateSchema.safeParse(kunnskapsproveBirthDateVal.value)
  if (!kbResult.success) newErrors.kunnskapsproveBirthDate = kbResult.error.issues[0]?.message ?? ''

  const kpResult = kunnskapsprovePassedDateSchema.safeParse(kunnskapsprovePassedDateVal.value)
  if (!kpResult.success) newErrors.kunnskapsprovePassedDate = kpResult.error.issues[0]?.message ?? ''

  const ktResult = kunnskapsproveTypeSchema.safeParse(form.value.kunnskapsproveType ?? '')
  if (!ktResult.success) newErrors.kunnskapsproveType = ktResult.error.issues[0]?.message ?? ''

  const muResult = municipalitySchema.safeParse((form.value.kunnskapsproveMunicipality ?? '').trim())
  if (!muResult.success) newErrors.municipality = muResult.error.issues[0]?.message ?? ''

  const drResult = doubtRoutineSchema.safeParse((form.value.doubtRoutine ?? '').trim())
  if (!drResult.success) newErrors.doubtRoutine = drResult.error.issues[0]?.message ?? ''

  const isResult = intoxicationSignsSchema.safeParse((form.value.intoxicationSigns ?? '').trim())
  if (!isResult.success) newErrors.intoxicationSigns = isResult.error.issues[0]?.message ?? ''

  const rpResult = refusalProcedureSchema.safeParse((form.value.refusalProcedure ?? '').trim())
  if (!rpResult.success) newErrors.refusalProcedure = rpResult.error.issues[0]?.message ?? ''

  if (Object.keys(newErrors).length > 0) {
    errors.value = newErrors
    return
  }
  errors.value = {}

  submitting.value = true
  try {
    await upsertMutation.mutateAsync(form.value)
    toast.success(editing.value ? 'Skjenkepolicy oppdatert' : 'Skjenkepolicy lagret')
    resetForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const message = error.response?.data?.error?.message
      if (typeof message === 'string' && message.trim().length > 0) {
        toast.error(message)
        return
      }
    }
    toast.error('Kunne ikke lagre skjenkepolicy')
  } finally {
    submitting.value = false
  }
}

async function openDocument(docId: number) {
  try {
    const res = await useDocumentUrlQuery(docId)
    window.open(res.url, '_blank')
  } catch {
    toast.error('Kunne ikke hente dokumentet')
  }
}

function formatDate(iso: string | null): string {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleDateString('nb-NO', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const ageCheckLabel: Record<AgeCheckLimit, string> = {
  UNDER_25: 'Alle under 25',
  UNDER_23: 'Alle under 23',
}

const idTypeLabel: Record<IdType, string> = {
  PASS: 'Pass',
  FORERKORT: 'Førerkort',
  BANKKORT: 'Bankkort m/bilde',
  NASJONALT_ID: 'Nasjonalt ID-kort',
}

const knowledgeTestLabel: Record<KnowledgeTestType, string> = {
  SALG: 'Salg',
  SKJENKE: 'Skjenke',
  BOTH: 'Salg og skjenke',
}

const knowledgeTestTypes: { value: KnowledgeTestType; label: string }[] = [
  { value: 'SALG', label: 'Salg' },
  { value: 'SKJENKE', label: 'Skjenke' },
  { value: 'BOTH', label: 'Salg og skjenke' },
]

function printPage() {
  window.print()
}

function bevillingStatus(validUntil: string | null): { label: string; class: string } {
  if (!validUntil) return { label: 'Ukjent', class: 'status--neutral' }
  const diff = Math.ceil((new Date(validUntil).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  if (diff < 0) return { label: 'Utløpt', class: 'status--expired' }
  if (diff <= 90) return { label: `${diff} dager igjen`, class: 'status--warning' }
  return { label: 'Aktiv', class: 'status--active' }
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger />
        <Separator orientation="vertical" class="header-separator" />
        <span class="page-title">Skjenkepolicy</span>
      </div>
    </header>

    <div class="page-content">
      <div v-if="existsQuery.isLoading.value" class="loading-state">
        <div class="skeleton-card skeleton-card--header"></div>
        <div class="skeleton-row">
          <div class="skeleton-card skeleton-card--doc"></div>
          <div class="skeleton-card skeleton-card--doc"></div>
        </div>
      </div>

      <template v-else-if="showForm">
        <section class="form-wrapper"><div class="form-container form-card">
          <div class="form-header">
            <h1>Skjenkepolicy</h1>
            <p class="form-subtitle">
              Rutiner iht. alkoholloven §1-9 og alkoholforskriften kap. 8. Dokumentasjonen skal
              være tilgjengelig for kontrollmyndigheten.
            </p>
          </div>

          <form @submit.prevent="handleSubmit">
            <!-- Bevillingsinformasjon -->
            <fieldset class="form-section">
              <legend class="section-title">Bevillingsinformasjon</legend>

              <div class="field-row two-col">
                <div :class="['field', { 'field--error': errors.bevillingNumber }]">
                  <label class="field-label">Bevillingsnummer *</label>
                  <Input :model-value="form.bevillingNumber ?? ''" @update:model-value="form.bevillingNumber = $event as string" placeholder="F.eks. 2024/12345" />
                  <p v-if="errors.bevillingNumber" class="error-message">{{ errors.bevillingNumber }}</p>
                </div>
                <div :class="['field', { 'field--error': errors.bevillingValidUntil }]">
                  <label class="field-label">Gyldig til *</label>
                  <DatePicker v-model="bevillingValidUntilDate" placeholder="Velg dato" />
                  <p v-if="errors.bevillingValidUntil" class="error-message">{{ errors.bevillingValidUntil }}</p>
                </div>
              </div>

              <div class="field-row two-col">
                <div :class="['field', { 'field--error': errors.styrerName }]">
                  <label class="field-label">Styrer (navn) *</label>
                  <Input :model-value="form.styrerName ?? ''" @update:model-value="form.styrerName = $event as string" placeholder="Fullt navn" />
                  <p v-if="errors.styrerName" class="error-message">{{ errors.styrerName }}</p>
                </div>
                <div :class="['field', { 'field--error': errors.stedfortrederName }]">
                  <label class="field-label">Stedfortreder (navn) *</label>
                  <Input :model-value="form.stedfortrederName ?? ''" @update:model-value="form.stedfortrederName = $event as string" placeholder="Fullt navn" />
                  <p v-if="errors.stedfortrederName" class="error-message">{{ errors.stedfortrederName }}</p>
                </div>
              </div>

              <div class="field">
                <label class="field-label">Last opp bevillingsdokument</label>
                <input ref="bevillingFileRef" type="file" accept=".pdf,.jpg,.jpeg,.png" class="file-hidden" @change="onFileChange($event, 'bevilling')" />
                <div
                  v-if="!bevillingFileName && !form.bevillingDocumentId"
                  class="drop-zone"
                  @dragover.prevent
                  @drop="onDrop($event, 'bevilling')"
                  @click="triggerFileInput(bevillingFileRef)"
                >
                  <div class="drop-zone-inner">
                    <Upload :size="18" class="drop-zone-icon" />
                    <span>Dra filer hit · PDF, JPG, PNG</span>
                  </div>
                </div>
                <div v-else class="file-chip">
                  <Loader2 v-if="bevillingUploading" :size="16" class="file-chip-spinner" />
                  <FileText v-else :size="16" />
                  <span class="file-chip-name">{{ bevillingFileName ?? 'Bevillingsdokument' }}</span>
                  <span v-if="bevillingUploading" class="file-chip-status">Laster opp...</span>
                  <button v-if="!bevillingUploading" type="button" class="file-chip-remove" @click="removeBevillingFile">
                    <X :size="14" />
                  </button>
                </div>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend class="section-title">Kunnskapsprøve</legend>

              <div class="field-row two-col">
                <div :class="['field', { 'field--error': errors.candidateName }]">
                  <label class="field-label">Kandidatens navn *</label>
                  <Input :model-value="form.kunnskapsproveCandidateName ?? ''" @update:model-value="form.kunnskapsproveCandidateName = $event as string" placeholder="Fullt navn" />
                  <p v-if="errors.candidateName" class="error-message">{{ errors.candidateName }}</p>
                </div>
                <div :class="['field', { 'field--error': errors.kunnskapsproveBirthDate }]">
                  <label class="field-label">Fødselsdato *</label>
                  <DatePicker v-model="kunnskapsproveBirthDateVal" placeholder="Velg dato" />
                  <p v-if="errors.kunnskapsproveBirthDate" class="error-message">{{ errors.kunnskapsproveBirthDate }}</p>
                </div>
              </div>

              <div class="field-row two-col">
                <div :class="['field', { 'field--error': errors.kunnskapsprovePassedDate }]">
                  <label class="field-label">Bestått dato *</label>
                  <DatePicker v-model="kunnskapsprovePassedDateVal" placeholder="Velg dato" />
                  <p v-if="errors.kunnskapsprovePassedDate" class="error-message">{{ errors.kunnskapsprovePassedDate }}</p>
                </div>
                <div :class="['field', { 'field--error': errors.municipality }]">
                  <label class="field-label">Kommune *</label>
                  <Input :model-value="form.kunnskapsproveMunicipality ?? ''" @update:model-value="form.kunnskapsproveMunicipality = $event as string" placeholder="F.eks. Oslo" />
                  <p v-if="errors.municipality" class="error-message">{{ errors.municipality }}</p>
                </div>
              </div>

              <div :class="['field', { 'field--error': errors.kunnskapsproveType }]">
                <label class="field-label">Type kunnskapsprøve *</label>
                <div class="chip-group">
                  <button
                    v-for="t in knowledgeTestTypes"
                    :key="t.value"
                    type="button"
                    class="chip-btn"
                    :class="{ 'chip-btn--active': form.kunnskapsproveType === t.value }"
                    @click="form.kunnskapsproveType = t.value"
                  >
                    {{ t.label }}
                  </button>
                </div>
                <p v-if="errors.kunnskapsproveType" class="error-message">{{ errors.kunnskapsproveType }}</p>
              </div>

              <div class="field">
                <label class="field-label">Last opp kunnskapsprøve-bevis</label>
                <input ref="kunnskapsproveFileRef" type="file" accept=".pdf,.jpg,.jpeg,.png" class="file-hidden" @change="onFileChange($event, 'kunnskapsprove')" />
                <div
                  v-if="!kunnskapsproveFileName && !form.kunnskapsproveDocumentId"
                  class="drop-zone"
                  @dragover.prevent
                  @drop="onDrop($event, 'kunnskapsprove')"
                  @click="triggerFileInput(kunnskapsproveFileRef)"
                >
                  <div class="drop-zone-inner">
                    <Upload :size="18" class="drop-zone-icon" />
                    <span>Dra filer hit · PDF, JPG, PNG</span>
                  </div>
                </div>
                <div v-else class="file-chip">
                  <Loader2 v-if="kunnskapsproveUploading" :size="16" class="file-chip-spinner" />
                  <FileText v-else :size="16" />
                  <span class="file-chip-name">{{ kunnskapsproveFileName ?? 'Kunnskapsprøve-bevis' }}</span>
                  <span v-if="kunnskapsproveUploading" class="file-chip-status">Laster opp...</span>
                  <button v-if="!kunnskapsproveUploading" type="button" class="file-chip-remove" @click="removeKunnskapsproveFile">
                    <X :size="14" />
                  </button>
                </div>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend class="section-title">Rutiner for alderskontroll</legend>
              <p class="section-subtitle">Beskriv hvordan dere kontrollerer alder. Lovpålagt iht. alkoholloven.</p>

              <div class="field">
                <label class="field-label">Aldersgrense for legitimasjonskontroll</label>
                <div class="toggle-group">
                  <button type="button" class="toggle-btn" :class="{ 'toggle-btn--active': (form.ageCheckLimit ?? 'UNDER_25') === 'UNDER_25' }" @click="setAgeCheck('UNDER_25')">
                    <span>Alle under 25</span>
                    <span class="toggle-rec">Anbefalt</span>
                  </button>
                  <button type="button" class="toggle-btn" :class="{ 'toggle-btn--active': form.ageCheckLimit === 'UNDER_23' }" @click="setAgeCheck('UNDER_23')">
                    Alle under 23
                  </button>
                </div>
              </div>

              <div class="field">
                <label class="field-label">Godkjente legitimasjonstyper</label>
                <div class="chip-group">
                  <button
                    v-for="idType in allIdTypes"
                    :key="idType.value"
                    type="button"
                    class="chip-btn"
                    :class="{ 'chip-btn--active': isIdTypeSelected(idType.value) }"
                    @click="toggleIdType(idType.value)"
                  >
                    {{ idType.label }}
                  </button>
                </div>
              </div>

              <div :class="['field', { 'field--error': errors.doubtRoutine }]">
                <label class="field-label">Rutine ved tvil om alder *</label>
                <Textarea :model-value="form.doubtRoutine ?? ''" @update:model-value="form.doubtRoutine = $event as string" placeholder="Beskriv steg-for-steg: Hva gjør bartender/servitør når det er tvil?" rows="3" />
                <p v-if="errors.doubtRoutine" class="error-message">{{ errors.doubtRoutine }}</p>
              </div>
            </fieldset>

            <fieldset class="form-section">
              <legend class="section-title">Ansvarlig servering</legend>
              <p class="section-subtitle">Rutiner for å hindre overskjenking og sikre forsvarlig drift.</p>

              <div :class="['field', { 'field--error': errors.intoxicationSigns }]">
                <label class="field-label">Hvordan identifiserer ansatte åpenbart berusede gjester? *</label>
                <Textarea :model-value="form.intoxicationSigns ?? ''" @update:model-value="form.intoxicationSigns = $event as string" placeholder="Tegn å se etter, prosedyre for servingsnekt..." rows="3" />
                <p v-if="errors.intoxicationSigns" class="error-message">{{ errors.intoxicationSigns }}</p>
              </div>

              <div :class="['field', { 'field--error': errors.refusalProcedure }]">
                <label class="field-label">Prosedyre ved nekt av servering *</label>
                <Textarea :model-value="form.refusalProcedure ?? ''" @update:model-value="form.refusalProcedure = $event as string" placeholder="Hvem tar avgjørelsen, hvordan kommuniseres det, eskaleringsrutine..." rows="3" />
                <p v-if="errors.refusalProcedure" class="error-message">{{ errors.refusalProcedure }}</p>
              </div>
            </fieldset>

            <div class="form-actions">
              <Button v-if="editing" type="button" variant="outline" @click="resetForm">Avbryt</Button>
              <Button type="submit" :disabled="submitting">
                {{ submitting ? 'Lagrer...' : editing ? 'Oppdater' : 'Lagre skjenkepolicy' }}
              </Button>
            </div>
          </form>
        </div></section>
      </template>

      <template v-else-if="showCard">
        <section class="header-row">
          <div>
            <h1>Skjenkepolicy</h1>
            <p>Rutiner iht. alkoholloven §1-9 og alkoholforskriften kap. 8</p>
          </div>
          <div class="header-actions">
            <Button type="button" variant="ghost" size="icon-sm" title="Skriv ut" aria-label="Skriv ut" @click="printPage"><Printer :size="16" aria-hidden="true" /></Button>
            <Button type="button" variant="outline" size="sm" @click="editing = true"><Pencil :size="14" aria-hidden="true" /> Rediger</Button>
          </div>
        </section>

        <!-- Expiry warning banner -->
        <div
          v-if="bevillingStatus(policy!.bevillingValidUntil).class === 'status--warning' || bevillingStatus(policy!.bevillingValidUntil).class === 'status--expired'"
          class="alert-banner"
          :class="bevillingStatus(policy!.bevillingValidUntil).class === 'status--expired' ? 'alert-banner--danger' : 'alert-banner--warning'"
        >
          <div class="alert-banner__content">
            <CalendarDays :size="16" />
            <span v-if="bevillingStatus(policy!.bevillingValidUntil).class === 'status--expired'">
              Bevillingen har utløpt. Oppdater bevillingsinformasjonen umiddelbart.
            </span>
            <span v-else>
              Bevillingen utløper snart ({{ bevillingStatus(policy!.bevillingValidUntil).label }}). Planlegg fornyelse.
            </span>
          </div>
          <Button variant="outline" size="sm" @click="editing = true">Oppdater</Button>
        </div>

        <div class="cards-row">
          <div class="doc-card doc-card--bevilling">
            <div class="doc-card__watermark" aria-hidden="true"><ScrollText :size="120" /></div>
            <div class="doc-card__header">
              <div class="doc-card__badge">
                <ShieldCheck :size="16" />
                <span>Skjenkebevillingslisens</span>
              </div>
              <div class="doc-card__status" :class="bevillingStatus(policy!.bevillingValidUntil).class">
                {{ bevillingStatus(policy!.bevillingValidUntil).label }}
              </div>
            </div>

            <div class="doc-card__body">
              <div class="doc-card__fields">
                <div class="doc-card__field">
                  <span class="doc-card__label"><Hash :size="13" /> Bevillingsnr.</span>
                  <span class="doc-card__value doc-card__value--mono">{{ policy!.bevillingNumber ?? '—' }}</span>
                </div>
                <div class="doc-card__field">
                  <span class="doc-card__label"><CalendarDays :size="13" /> Gyldig til</span>
                  <span class="doc-card__value" :class="{ 'doc-card__value--warn': bevillingStatus(policy!.bevillingValidUntil).class !== 'status--active' }">{{ formatDate(policy!.bevillingValidUntil) }}</span>
                </div>
                <div class="doc-card__field">
                  <span class="doc-card__label"><User :size="13" /> Styrer</span>
                  <span class="doc-card__value">{{ policy!.styrerName ?? '—' }}</span>
                </div>
                <div class="doc-card__field">
                  <span class="doc-card__label"><User :size="13" /> Stedfortreder</span>
                  <span class="doc-card__value">{{ policy!.stedfortrederName ?? '—' }}</span>
                </div>
              </div>
            </div>

            <div v-if="policy!.bevillingDocumentId" class="doc-card__footer">
              <Button variant="outline" size="sm" class="doc-card__doc-btn" @click="openDocument(policy!.bevillingDocumentId!)">
                <ExternalLink :size="14" /> Åpne dokument
              </Button>
            </div>
          </div>

          <div class="doc-card doc-card--kunnskap">
            <div class="doc-card__watermark" aria-hidden="true"><Award :size="100" /></div>
            <div class="doc-card__header">
              <div class="doc-card__badge doc-card__badge--kunnskap">
                <Award :size="16" />
                <span>Kunnskapsprøve</span>
              </div>
              <div class="doc-card__status status--active" v-if="policy!.kunnskapsprovePassedDate">Bestått</div>
            </div>

            <div class="doc-card__body">
              <div class="doc-card__fields">
                <div class="doc-card__field">
                  <span class="doc-card__label"><User :size="13" /> Kandidat</span>
                  <span class="doc-card__value">{{ policy!.kunnskapsproveCandidateName ?? '—' }}</span>
                </div>
                <div class="doc-card__field">
                  <span class="doc-card__label"><ShieldCheck :size="13" /> Type</span>
                  <span class="doc-card__value">{{ policy!.kunnskapsproveType ? knowledgeTestLabel[policy!.kunnskapsproveType] : '—' }}</span>
                </div>
                <div class="doc-card__field">
                  <span class="doc-card__label"><MapPin :size="13" /> Kommune</span>
                  <span class="doc-card__value">{{ policy!.kunnskapsproveMunicipality ?? '—' }}</span>
                </div>
                <div class="doc-card__field">
                  <span class="doc-card__label"><CalendarDays :size="13" /> Bestått</span>
                  <span class="doc-card__value">{{ formatDate(policy!.kunnskapsprovePassedDate) }}</span>
                </div>
              </div>
            </div>

            <div v-if="policy!.kunnskapsproveDocumentId" class="doc-card__footer">
              <Button variant="outline" size="sm" class="doc-card__doc-btn" @click="openDocument(policy!.kunnskapsproveDocumentId!)">
                <ExternalLink :size="14" /> Åpne bevis
              </Button>
            </div>
          </div>
        </div>

        <!-- Routines -->
        <div class="routines-card">
          <div class="routines-card__section">
            <div class="routines-card__header">
              <ScanEye :size="18" class="routines-card__icon routines-card__icon--brand" />
              <h4 class="routines-card__title">Rutiner for alderskontroll</h4>
            </div>

            <div class="routine-items">
              <div class="routine-item">
                <span class="routine-item__label">Aldersgrense for legitimasjonskontroll</span>
                <Badge tone="brand">{{ ageCheckLabel[policy!.ageCheckLimit] }}</Badge>
              </div>

              <div class="routine-item">
                <span class="routine-item__label">Godkjent legitimasjon</span>
                <div class="id-chips">
                  <span
                    v-for="idType in policy!.acceptedIdTypes"
                    :key="idType"
                    class="id-chip"
                  >
                    <CreditCard :size="12" />
                    {{ idTypeLabel[idType] }}
                  </span>
                </div>
              </div>

              <div v-if="policy!.doubtRoutine" class="routine-item routine-item--block">
                <div class="routine-item__header">
                  <CircleHelp :size="14" class="routine-item__icon" />
                  <span class="routine-item__label">Rutine ved tvil om alder</span>
                </div>
                <p class="routine-item__text">{{ policy!.doubtRoutine }}</p>
              </div>
            </div>
          </div>

          <Separator />

          <div class="routines-card__section">
            <div class="routines-card__header">
              <HandMetal :size="18" class="routines-card__icon routines-card__icon--amber" />
              <h4 class="routines-card__title">Ansvarlig servering</h4>
            </div>

            <div class="routine-items">
              <div v-if="policy!.intoxicationSigns" class="routine-item routine-item--block">
                <div class="routine-item__header">
                  <ScanEye :size="14" class="routine-item__icon" />
                  <span class="routine-item__label">Identifisering av berusede gjester</span>
                </div>
                <p class="routine-item__text">{{ policy!.intoxicationSigns }}</p>
              </div>

              <div v-if="policy!.refusalProcedure" class="routine-item routine-item--block">
                <div class="routine-item__header">
                  <HandMetal :size="14" class="routine-item__icon" />
                  <span class="routine-item__label">Prosedyre ved nekt av servering</span>
                </div>
                <p class="routine-item__text">{{ policy!.refusalProcedure }}</p>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-else-if="policyExists && policyQuery.isLoading.value" class="loading-state">
        <div class="skeleton-card skeleton-card--header"></div>
        <div class="skeleton-row">
          <div class="skeleton-card skeleton-card--doc"></div>
          <div class="skeleton-card skeleton-card--doc"></div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.page-header { display: flex; height: 4rem; flex-shrink: 0; align-items: center; }
.page-header-inner { display: flex; align-items: center; gap: 0.5rem; padding: 0 1rem; }
.header-separator { height: 1rem !important; width: 1px !important; margin-right: 0.5rem; }
.page-title { font-weight: 500; color: hsl(var(--sidebar-primary, 245 43% 52%)); }
.page-content { display: flex; flex: 1; flex-direction: column; gap: 1rem; padding: 0 1rem 1rem; }

/* ── Loading skeletons ──────────────────────────────────── */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-row {
  display: flex;
  gap: 14px;
}

.skeleton-card {
  border-radius: var(--radius-xl);
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

.skeleton-card--header { height: 60px; max-width: 400px; }
.skeleton-card--doc { height: 240px; flex: 1; }

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.header-row h1 { margin: 0; font-size: 1.75rem; font-weight: 800; letter-spacing: -0.03em; }
.header-row p { margin-top: 4px; color: var(--text-secondary); font-size: 0.88rem; }

.form-wrapper { display: flex; justify-content: center; width: 100%; }
.form-container { width: 100%; max-width: 820px; }
.form-card {
  background: hsl(var(--card));
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-xl);
  padding: 2rem;
}
.error-message {
  color: hsl(var(--destructive));
  font-size: 0.8rem;
  margin-top: 2px;
}
.field--error :deep(.input),
.field--error :deep(.textarea),
.field--error :deep(.date-picker__trigger) {
  border-color: hsl(var(--destructive));
}
.field--error .chip-btn:not(.chip-btn--active) {
  border-color: hsl(var(--destructive));
}
.form-header h1 { margin: 0; font-size: 1.75rem; font-weight: 800; letter-spacing: -0.03em; }
.form-subtitle { margin-top: 0.5rem; color: var(--text-secondary); font-size: 0.95rem; line-height: 1.5; }
.form-section { border: none; padding: 0; margin: 2rem 0 0; }
.section-title { font-size: 1.2rem; font-weight: 600; letter-spacing: -0.01em; }
.section-subtitle { color: var(--text-secondary); font-size: 0.875rem; margin-top: 0.25rem; margin-bottom: 0.75rem; }

.field-row { display: flex; gap: 1rem; margin-top: 0.75rem; }
.field-row.two-col > .field { flex: 1; }
.field { display: flex; flex-direction: column; gap: 0.375rem; margin-top: 0.75rem; }
.field-label { font-size: 0.85rem; font-weight: 500; color: var(--text-secondary); }



.drop-zone {
  border: 2px dashed hsl(var(--border));
  border-radius: 0.5rem;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 150ms, background 150ms;
}
.drop-zone:hover { border-color: hsl(var(--ring)); background: hsl(var(--accent) / 0.3); }

.drop-zone-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
  pointer-events: none;
}
.drop-zone-icon { color: hsl(var(--muted-foreground)); }

.file-hidden { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }

.file-chip {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  font-size: 0.85rem;
}
.file-chip-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-chip-status { font-size: 0.78rem; color: var(--text-secondary); flex-shrink: 0; }
.file-chip-spinner { animation: spin 1s linear infinite; flex-shrink: 0; color: hsl(var(--primary)); }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.file-chip-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: hsl(var(--muted-foreground));
  padding: 2px;
  border-radius: 4px;
  flex-shrink: 0;
}
.file-chip-remove:hover { background: hsl(var(--accent)); color: hsl(var(--foreground)); }

.toggle-group { display: flex; gap: 0.75rem; }
.toggle-btn {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 0.15rem;
  padding: 0.75rem 1rem; border: 2px solid hsl(var(--border)); border-radius: 0.5rem;
  background: hsl(var(--card)); font: inherit; font-size: 0.9rem; cursor: pointer;
  transition: border-color 150ms, background 150ms;
}
.toggle-btn:hover { border-color: hsl(var(--ring) / 0.5); }
.toggle-btn--active { border-color: hsl(var(--ring)); background: hsl(var(--accent) / 0.5); }
.toggle-rec { font-size: 0.75rem; color: var(--green); font-weight: 500; }

.chip-group { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.chip-btn {
  padding: 0.4rem 0.85rem; border: 1.5px solid hsl(var(--border)); border-radius: var(--radius-pill);
  background: hsl(var(--card)); font: inherit; font-size: 0.85rem; cursor: pointer;
  transition: border-color 150ms, background 150ms, color 150ms;
}
.chip-btn:hover { border-color: hsl(var(--ring) / 0.5); }
.chip-btn--active { border-color: hsl(var(--ring)); background: hsl(var(--accent)); color: hsl(var(--accent-foreground)); font-weight: 500; }

.form-actions { display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid hsl(var(--border)); }

.cards-row {
  display: flex;
  gap: 1rem;
  width: 100%;
}

.doc-card {
  position: relative;
  overflow: hidden;
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-xl);
  background: hsl(var(--card));
  display: flex;
  flex-direction: column;
}
.doc-card--bevilling { flex: 2; }
.doc-card--kunnskap { flex: 1; }

.doc-card__watermark {
  position: absolute;
  right: -16px;
  bottom: -16px;
  opacity: 0.035;
  pointer-events: none;
  color: hsl(var(--foreground));
}

.doc-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid hsl(var(--border));
  background: linear-gradient(135deg, hsl(var(--primary) / 0.03), hsl(var(--primary) / 0.07));
}

.doc-card__badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.78rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: hsl(var(--primary));
}
.doc-card__badge--kunnskap {
  color: hsl(142 60% 40%);
}

.doc-card__status {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-pill);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.status--active { background: hsl(142 60% 94%); color: hsl(142 60% 30%); }
.status--warning { background: hsl(38 95% 92%); color: hsl(30 80% 35%); }
.status--expired { background: hsl(0 70% 94%); color: hsl(0 60% 40%); }
.status--neutral { background: hsl(var(--muted)); color: hsl(var(--muted-foreground)); }

.doc-card__body {
  padding: 1.25rem;
  flex: 1;
}

.doc-card__fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.1rem;
}
.doc-card__fields--single {
  grid-template-columns: 1fr;
}

.doc-card__field {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.doc-card__label {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.72rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.doc-card__value {
  font-size: 0.92rem;
  font-weight: 500;
  color: hsl(var(--foreground));
}
.doc-card__value--mono {
  font-family: ui-monospace, 'SF Mono', 'Cascadia Code', monospace;
  letter-spacing: 0.03em;
}

.doc-card__footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid hsl(var(--border));
}
.doc-card__doc-btn {
  width: 100%;
  justify-content: center;
}

.routines-card {
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-xl);
  background: hsl(var(--card));
  overflow: hidden;
}
.routines-card__section {
  padding: 1.25rem 1.5rem;
}
.routines-card__title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 0.75rem;
}

.license-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.license-field { display: flex; flex-direction: column; gap: 0.2rem; }
.license-label { font-size: 0.78rem; font-weight: 500; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.03em; }
.license-value { font-size: 0.95rem; font-weight: 500; margin: 0; }
.license-text-block { margin-top: 1rem; }
.license-text-block .license-value { margin-top: 0.25rem; font-weight: 400; line-height: 1.5; color: var(--text-secondary); }

/* ── Header actions ────────────────────────────────────── */
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ── Alert banner ─────────────────────────────────────── */
.alert-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-xl);
  font-size: 0.88rem;
}

.alert-banner--warning {
  background: hsl(38 95% 92%);
  border: 1px solid hsl(38 80% 80%);
  color: hsl(30 80% 30%);
}

.alert-banner--danger {
  background: hsl(0 70% 94%);
  border: 1px solid hsl(0 60% 82%);
  color: hsl(0 60% 35%);
}

.alert-banner__content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

/* ── Warn value ───────────────────────────────────────── */
.doc-card__value--warn {
  color: var(--red, hsl(0 60% 40%));
  font-weight: 600;
}

/* ── Routines card header ─────────────────────────────── */
.routines-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.routines-card__icon {
  flex-shrink: 0;
}

.routines-card__icon--brand {
  color: var(--brand, hsl(var(--primary)));
}

.routines-card__icon--amber {
  color: var(--amber, hsl(30 80% 45%));
}

/* ── Routine items ────────────────────────────────────── */
.routine-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.routine-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.routine-item--block {
  flex-direction: column;
  align-items: stretch;
  background: hsl(var(--muted) / 0.5);
  border-radius: var(--radius-lg, 8px);
  padding: 14px 16px;
  gap: 8px;
}

.routine-item__header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.routine-item__icon {
  color: #94a3b8;
  flex-shrink: 0;
}

.routine-item__label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-secondary, #64748b);
}

.routine-item--block .routine-item__label {
  font-weight: 600;
  color: hsl(var(--foreground));
  font-size: 0.82rem;
}

.routine-item__text {
  margin: 0;
  font-size: 0.88rem;
  line-height: 1.6;
  color: var(--text-secondary, #64748b);
}

/* ── ID chips ─────────────────────────────────────────── */
.id-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.id-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: var(--radius-pill, 999px);
  background: hsl(var(--muted));
  font-size: 0.78rem;
  font-weight: 500;
  color: hsl(var(--foreground));
  white-space: nowrap;
}

.actions-trigger {
  display: flex; align-items: center; justify-content: center; width: 2rem; height: 2rem;
  border-radius: var(--radius-md); border: none; background: none; color: hsl(var(--muted-foreground));
  cursor: pointer; transition: background 150ms ease, color 150ms ease;
}
.actions-trigger:hover { background: hsl(var(--accent)); color: hsl(var(--foreground)); }

@media (max-width: 768px) {
  .field-row { flex-direction: column; }
  .cards-row { flex-direction: column; }
  .skeleton-row { flex-direction: column; }
  .doc-card__fields { grid-template-columns: 1fr; }
  .license-grid { grid-template-columns: 1fr; }
  .toggle-group { flex-direction: column; }
  .header-row { flex-direction: column; gap: 12px; }
  .routine-item:not(.routine-item--block) { flex-direction: column; align-items: flex-start; }
  .alert-banner { flex-direction: column; align-items: flex-start; }
}
</style>

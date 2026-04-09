<script setup lang="ts">
import {ref, watch} from 'vue'
import {z} from 'zod'
import axios from 'axios'
import {
  Upload,
  X,
  Loader2,
  FileText,
} from 'lucide-vue-next'
import {toast} from 'vue-sonner'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import type {DateValue} from '@internationalized/date'
import {stringToCalendarDate, dateValueToString} from '@/utils/date'
import {
  useUpsertAlcoholPolicyMutation,
} from '@/composables/useAlcoholPolicy'
import {useDocumentUploadMutation} from '@/composables/useDocumentUpload'
import type {
  AlcoholPolicy,
  AgeCheckLimit,
  IdType,
  KnowledgeTestType,
  CreateAlcoholPolicyRequest,
} from '@/types/alcoholPolicy'

const props = defineProps<{
  policy: AlcoholPolicy | null
  editing: boolean
}>()

const emits = defineEmits<{
  (e: 'saved'): void
  (e: 'cancel'): void
}>()

const upsertMutation = useUpsertAlcoholPolicyMutation()
const bevillingUpload = useDocumentUploadMutation()
const kunnskapsproveUpload = useDocumentUploadMutation()

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

watch(bevillingValidUntilDate, (v) => {
  form.value.bevillingValidUntil = dateValueToString(v) ?? null
})
watch(kunnskapsproveBirthDateVal, (v) => {
  form.value.kunnskapsproveBirthDate = dateValueToString(v) ?? null
})
watch(kunnskapsprovePassedDateVal, (v) => {
  form.value.kunnskapsprovePassedDate = dateValueToString(v) ?? null
})

const bevillingFileName = ref<string | null>(null)
const kunnskapsproveFileName = ref<string | null>(null)
const bevillingUploading = ref(false)
const kunnskapsproveUploading = ref(false)
const bevillingFileRef = ref<HTMLInputElement | null>(null)
const kunnskapsproveFileRef = ref<HTMLInputElement | null>(null)

watch(
  () => props.editing,
  (isEditing) => {
    if (isEditing && props.policy) {
      form.value = {
        bevillingNumber: props.policy.bevillingNumber,
        bevillingValidUntil: props.policy.bevillingValidUntil,
        styrerName: props.policy.styrerName,
        stedfortrederName: props.policy.stedfortrederName,
        bevillingDocumentId: props.policy.bevillingDocumentId,
        kunnskapsproveCandidateName: props.policy.kunnskapsproveCandidateName,
        kunnskapsproveBirthDate: props.policy.kunnskapsproveBirthDate,
        kunnskapsproveType: props.policy.kunnskapsproveType,
        kunnskapsproveMunicipality: props.policy.kunnskapsproveMunicipality,
        kunnskapsprovePassedDate: props.policy.kunnskapsprovePassedDate,
        kunnskapsproveDocumentId: props.policy.kunnskapsproveDocumentId,
        ageCheckLimit: props.policy.ageCheckLimit,
        acceptedIdTypes: [...props.policy.acceptedIdTypes],
        doubtRoutine: props.policy.doubtRoutine,
        intoxicationSigns: props.policy.intoxicationSigns,
        refusalProcedure: props.policy.refusalProcedure,
      }
      bevillingFileName.value = null
      kunnskapsproveFileName.value = null
      bevillingValidUntilDate.value = stringToCalendarDate(props.policy.bevillingValidUntil)
      kunnskapsproveBirthDateVal.value = stringToCalendarDate(props.policy.kunnskapsproveBirthDate)
      kunnskapsprovePassedDateVal.value = stringToCalendarDate(props.policy.kunnskapsprovePassedDate)
    }
  },
  {immediate: true},
)

function resetForm() {
  form.value = {}
  bevillingFileName.value = null
  kunnskapsproveFileName.value = null
  bevillingValidUntilDate.value = undefined
  kunnskapsproveBirthDateVal.value = undefined
  kunnskapsprovePassedDateVal.value = undefined
  errors.value = {}
}

function setAgeCheck(val: AgeCheckLimit) {
  form.value.ageCheckLimit = val
}

const allIdTypes: { value: IdType; label: string }[] = [
  {value: 'PASS', label: 'Pass'},
  {value: 'FORERKORT', label: 'Førerkort'},
  {value: 'BANKKORT', label: 'Bankkort m/bilde'},
  {value: 'NASJONALT_ID', label: 'Nasjonalt ID-kort'},
]

const knowledgeTestTypes: { value: KnowledgeTestType; label: string }[] = [
  {value: 'SALG', label: 'Salg'},
  {value: 'SKJENKE', label: 'Skjenke'},
  {value: 'BOTH', label: 'Salg og skjenke'},
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
    toast.success(props.editing ? 'Skjenkepolicy oppdatert' : 'Skjenkepolicy lagret')
    resetForm()
    emits('saved')
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
</script>

<template>
  <section class="form-wrapper">
    <div class="form-container form-card">
      <div class="form-header">
        <h1>Skjenkepolicy</h1>
        <p class="form-subtitle">
          Rutiner iht. alkoholloven §1-9 og alkoholforskriften kap. 8. Dokumentasjonen skal
          være tilgjengelig for kontrollmyndigheten.
        </p>
      </div>

      <form @submit.prevent="handleSubmit">
        <fieldset class="form-section">
          <legend class="section-title">Bevillingsinformasjon</legend>

          <div class="field-row two-col">
            <div :class="['field', { 'field--error': errors.bevillingNumber }]">
              <label class="field-label">Bevillingsnummer *</label>
              <Input :model-value="form.bevillingNumber ?? ''"
                     @update:model-value="form.bevillingNumber = $event as string"
                     placeholder="F.eks. 2024/12345"/>
              <p v-if="errors.bevillingNumber" class="error-message">{{
                  errors.bevillingNumber
                }}</p>
            </div>
            <div :class="['field', { 'field--error': errors.bevillingValidUntil }]">
              <label class="field-label">Gyldig til *</label>
              <DatePicker v-model="bevillingValidUntilDate" placeholder="Velg dato"/>
              <p v-if="errors.bevillingValidUntil" class="error-message">
                {{ errors.bevillingValidUntil }}</p>
            </div>
          </div>

          <div class="field-row two-col">
            <div :class="['field', { 'field--error': errors.styrerName }]">
              <label class="field-label">Styrer (navn) *</label>
              <Input :model-value="form.styrerName ?? ''"
                     @update:model-value="form.styrerName = $event as string"
                     placeholder="Fullt navn"/>
              <p v-if="errors.styrerName" class="error-message">{{ errors.styrerName }}</p>
            </div>
            <div :class="['field', { 'field--error': errors.stedfortrederName }]">
              <label class="field-label">Stedfortreder (navn) *</label>
              <Input :model-value="form.stedfortrederName ?? ''"
                     @update:model-value="form.stedfortrederName = $event as string"
                     placeholder="Fullt navn"/>
              <p v-if="errors.stedfortrederName" class="error-message">{{
                  errors.stedfortrederName
                }}</p>
            </div>
          </div>

          <div class="field">
            <label class="field-label">Last opp bevillingsdokument</label>
            <input ref="bevillingFileRef" type="file" accept=".pdf,.jpg,.jpeg,.png"
                   class="file-hidden" @change="onFileChange($event, 'bevilling')"/>
            <div
              v-if="!bevillingFileName && !form.bevillingDocumentId"
              class="drop-zone"
              @dragover.prevent
              @drop="onDrop($event, 'bevilling')"
              @click="triggerFileInput(bevillingFileRef)"
            >
              <div class="drop-zone-inner">
                <Upload :size="18" class="drop-zone-icon"/>
                <span>Dra filer hit · PDF, JPG, PNG</span>
              </div>
            </div>
            <div v-else class="file-chip">
              <Loader2 v-if="bevillingUploading" :size="16" class="file-chip-spinner"/>
              <FileText v-else :size="16"/>
              <span class="file-chip-name">{{ bevillingFileName ?? 'Bevillingsdokument' }}</span>
              <span v-if="bevillingUploading" class="file-chip-status">Laster opp...</span>
              <button v-if="!bevillingUploading" type="button" class="file-chip-remove"
                      @click="removeBevillingFile">
                <X :size="14"/>
              </button>
            </div>
          </div>
        </fieldset>

        <fieldset class="form-section">
          <legend class="section-title">Kunnskapsprøve</legend>

          <div class="field-row two-col">
            <div :class="['field', { 'field--error': errors.candidateName }]">
              <label class="field-label">Kandidatens navn *</label>
              <Input :model-value="form.kunnskapsproveCandidateName ?? ''"
                     @update:model-value="form.kunnskapsproveCandidateName = $event as string"
                     placeholder="Fullt navn"/>
              <p v-if="errors.candidateName" class="error-message">{{ errors.candidateName }}</p>
            </div>
            <div :class="['field', { 'field--error': errors.kunnskapsproveBirthDate }]">
              <label class="field-label">Fødselsdato *</label>
              <DatePicker v-model="kunnskapsproveBirthDateVal" placeholder="Velg dato"/>
              <p v-if="errors.kunnskapsproveBirthDate" class="error-message">
                {{ errors.kunnskapsproveBirthDate }}</p>
            </div>
          </div>

          <div class="field-row two-col">
            <div :class="['field', { 'field--error': errors.kunnskapsprovePassedDate }]">
              <label class="field-label">Bestått dato *</label>
              <DatePicker v-model="kunnskapsprovePassedDateVal" placeholder="Velg dato"/>
              <p v-if="errors.kunnskapsprovePassedDate" class="error-message">
                {{ errors.kunnskapsprovePassedDate }}</p>
            </div>
            <div :class="['field', { 'field--error': errors.municipality }]">
              <label class="field-label">Kommune *</label>
              <Input :model-value="form.kunnskapsproveMunicipality ?? ''"
                     @update:model-value="form.kunnskapsproveMunicipality = $event as string"
                     placeholder="F.eks. Oslo"/>
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
            <p v-if="errors.kunnskapsproveType" class="error-message">{{
                errors.kunnskapsproveType
              }}</p>
          </div>

          <div class="field">
            <label class="field-label">Last opp kunnskapsprøve-bevis</label>
            <input ref="kunnskapsproveFileRef" type="file" accept=".pdf,.jpg,.jpeg,.png"
                   class="file-hidden" @change="onFileChange($event, 'kunnskapsprove')"/>
            <div
              v-if="!kunnskapsproveFileName && !form.kunnskapsproveDocumentId"
              class="drop-zone"
              @dragover.prevent
              @drop="onDrop($event, 'kunnskapsprove')"
              @click="triggerFileInput(kunnskapsproveFileRef)"
            >
              <div class="drop-zone-inner">
                <Upload :size="18" class="drop-zone-icon"/>
                <span>Dra filer hit · PDF, JPG, PNG</span>
              </div>
            </div>
            <div v-else class="file-chip">
              <Loader2 v-if="kunnskapsproveUploading" :size="16" class="file-chip-spinner"/>
              <FileText v-else :size="16"/>
              <span class="file-chip-name">{{
                  kunnskapsproveFileName ?? 'Kunnskapsprøve-bevis'
                }}</span>
              <span v-if="kunnskapsproveUploading" class="file-chip-status">Laster opp...</span>
              <button v-if="!kunnskapsproveUploading" type="button" class="file-chip-remove"
                      @click="removeKunnskapsproveFile">
                <X :size="14"/>
              </button>
            </div>
          </div>
        </fieldset>

        <fieldset class="form-section">
          <legend class="section-title">Rutiner for alderskontroll</legend>
          <p class="section-subtitle">Beskriv hvordan dere kontrollerer alder. Lovpålagt iht.
            alkoholloven.</p>

          <div class="field">
            <label class="field-label">Aldersgrense for legitimasjonskontroll</label>
            <div class="toggle-group">
              <button type="button" class="toggle-btn"
                      :class="{ 'toggle-btn--active': (form.ageCheckLimit ?? 'UNDER_25') === 'UNDER_25' }"
                      @click="setAgeCheck('UNDER_25')">
                <span>Alle under 25</span>
                <span class="toggle-rec">Anbefalt</span>
              </button>
              <button type="button" class="toggle-btn"
                      :class="{ 'toggle-btn--active': form.ageCheckLimit === 'UNDER_23' }"
                      @click="setAgeCheck('UNDER_23')">
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
            <Textarea :model-value="form.doubtRoutine ?? ''"
                      @update:model-value="form.doubtRoutine = $event as string"
                      placeholder="Beskriv steg-for-steg: Hva gjør bartender/servitør når det er tvil?"
                      rows="3"/>
            <p v-if="errors.doubtRoutine" class="error-message">{{ errors.doubtRoutine }}</p>
          </div>
        </fieldset>

        <fieldset class="form-section">
          <legend class="section-title">Ansvarlig servering</legend>
          <p class="section-subtitle">Rutiner for å hindre overskjenking og sikre forsvarlig
            drift.</p>

          <div :class="['field', { 'field--error': errors.intoxicationSigns }]">
            <label class="field-label">Hvordan identifiserer ansatte åpenbart berusede gjester?
              *</label>
            <Textarea :model-value="form.intoxicationSigns ?? ''"
                      @update:model-value="form.intoxicationSigns = $event as string"
                      placeholder="Tegn å se etter, prosedyre for servingsnekt..." rows="3"/>
            <p v-if="errors.intoxicationSigns" class="error-message">{{
                errors.intoxicationSigns
              }}</p>
          </div>

          <div :class="['field', { 'field--error': errors.refusalProcedure }]">
            <label class="field-label">Prosedyre ved nekt av servering *</label>
            <Textarea :model-value="form.refusalProcedure ?? ''"
                      @update:model-value="form.refusalProcedure = $event as string"
                      placeholder="Hvem tar avgjørelsen, hvordan kommuniseres det, eskaleringsrutine..."
                      rows="3"/>
            <p v-if="errors.refusalProcedure" class="error-message">{{
                errors.refusalProcedure
              }}</p>
          </div>
        </fieldset>

        <div class="form-actions">
          <Button v-if="editing" type="button" variant="outline"
                  @click="resetForm(); emits('cancel')">Avbryt
          </Button>
          <Button type="submit" :disabled="submitting">
            {{ submitting ? 'Lagrer...' : editing ? 'Oppdater' : 'Lagre skjenkepolicy' }}
          </Button>
        </div>
      </form>
    </div>
  </section>
</template>

<style scoped>
.form-wrapper {
  display: flex;
  justify-content: center;
  width: 100%;
}

.form-container {
  width: 100%;
  max-width: 820px;
}

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

.form-header h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.form-subtitle {
  margin-top: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.5;
}

.form-section {
  border: none;
  padding: 0;
  margin: 2rem 0 0;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.section-subtitle {
  color: var(--text-secondary);
  font-size: 0.875rem;
  margin-top: 0.25rem;
  margin-bottom: 0.75rem;
}

.field-row {
  display: flex;
  gap: 1rem;
  margin-top: 0.75rem;
}

.field-row.two-col > .field {
  flex: 1;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  margin-top: 0.75rem;
}

.field-label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.drop-zone {
  border: 2px dashed hsl(var(--border));
  border-radius: 0.5rem;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 150ms, background 150ms;
}

.drop-zone:hover {
  border-color: hsl(var(--ring));
  background: hsl(var(--accent) / 0.3);
}

.drop-zone-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
  pointer-events: none;
}

.drop-zone-icon {
  color: hsl(var(--muted-foreground));
}

.file-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
}

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

.file-chip-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-chip-status {
  font-size: 0.78rem;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.file-chip-spinner {
  animation: spin 1s linear infinite;
  flex-shrink: 0;
  color: hsl(var(--primary));
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

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

.file-chip-remove:hover {
  background: hsl(var(--accent));
  color: hsl(var(--foreground));
}

.toggle-group {
  display: flex;
  gap: 0.75rem;
}

.toggle-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.15rem;
  padding: 0.75rem 1rem;
  border: 2px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: hsl(var(--card));
  font: inherit;
  font-size: 0.9rem;
  cursor: pointer;
  transition: border-color 150ms, background 150ms;
}

.toggle-btn:hover {
  border-color: hsl(var(--ring) / 0.5);
}

.toggle-btn--active {
  border-color: hsl(var(--ring));
  background: hsl(var(--accent) / 0.5);
}

.toggle-rec {
  font-size: 0.75rem;
  color: var(--green);
  font-weight: 500;
}

.chip-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.chip-btn {
  padding: 0.4rem 0.85rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: var(--radius-pill);
  background: hsl(var(--card));
  font: inherit;
  font-size: 0.85rem;
  cursor: pointer;
  transition: border-color 150ms, background 150ms, color 150ms;
}

.chip-btn:hover {
  border-color: hsl(var(--ring) / 0.5);
}

.chip-btn--active {
  border-color: hsl(var(--ring));
  background: hsl(var(--accent));
  color: hsl(var(--accent-foreground));
  font-weight: 500;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid hsl(var(--border));
}

@media (max-width: 768px) {
  .field-row {
    flex-direction: column;
  }

  .toggle-group {
    flex-direction: column;
  }
}
</style>

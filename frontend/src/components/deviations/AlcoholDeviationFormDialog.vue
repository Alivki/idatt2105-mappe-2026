<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { z } from 'zod'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import Button from '@/components/ui/button/Button.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import TimePicker from '@/components/ui/time-picker/TimePicker.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import { CalendarDate } from '@internationalized/date'
import type { DateValue } from '@internationalized/date'
import type {
  AlcoholDeviation,
  AlcoholReportSource,
  AlcoholDeviationType,
  AlcoholCausalAnalysis,
  AlcoholDeviationStatus,
  CreateAlcoholDeviationRequest,
  UpdateAlcoholDeviationRequest,
  PenaltyPointSummary,
} from '@/types/deviation'

interface MemberOption { userId: number; label: string }

const props = withDefaults(
  defineProps<{
    open: boolean
    mode?: 'create' | 'edit'
    initial?: AlcoholDeviation | null
    submitting?: boolean
    members: MemberOption[]
    penaltySummary?: PenaltyPointSummary | null
    inline?: boolean
  }>(),
  { mode: 'create', initial: null, submitting: false, penaltySummary: null, inline: false },
)

const emits = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'create', payload: CreateAlcoholDeviationRequest): void
  (e: 'update', payload: { id: number; data: UpdateAlcoholDeviationRequest }): void
}>()

function stringToCalendarDate(str: string): CalendarDate | undefined {
  if (!str) return undefined
  const [y, m, d] = str.split('-').map(Number)
  return new CalendarDate(y!, m!, d!)
}

function dateValueToString(dv: DateValue | undefined): string {
  if (!dv) return ''
  return `${dv.year}-${String(dv.month).padStart(2, '0')}-${String(dv.day).padStart(2, '0')}`
}

function timeToString(h: number | undefined, m: number | undefined): string {
  if (h == null || m == null) return ''
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

const reportedDate = ref<DateValue | undefined>()
const reportedHours = ref<number | undefined>()
const reportedMinutes = ref<number | undefined>()
const reportedByUserId = ref<string>('')
const reportSource = ref<AlcoholReportSource>('EGENRAPPORT')
const deviationType = ref<AlcoholDeviationType | ''>('')
const description = ref('')
const immediateAction = ref('')
const causalAnalysis = ref<AlcoholCausalAnalysis | ''>('')
const causalExplanation = ref('')
const preventiveMeasures = ref('')
const preventiveDeadline = ref<DateValue | undefined>()
const preventiveResponsibleUserId = ref<string>('')
const status = ref<AlcoholDeviationStatus>('OPEN')
const errors = ref<Record<string, string>>({})

const descriptionSchema = z.string().min(1, 'Beskrivelse er påkrevd')
const deviationTypeSchema = z.string().min(1, 'Velg type hendelse')
const reportedDateSchema = z.custom<DateValue>((v) => !!v, 'Dato er påkrevd')
const reportedTimeSchema = z.number({ error: 'Tidspunkt er påkrevd' }).min(0)
const reporterSchema = z.string().min(1, 'Velg hvem som rapporterer')
const immediateActionSchema = z.string().min(1, 'Umiddelbar handling er påkrevd')
const causalAnalysisSchema = z.string().min(1, 'Velg årsak')
const causalExplanationSchema = z.string().min(1, 'Utdyp årsaken')
const preventiveSchema = z.string().min(1, 'Forebyggende tiltak er påkrevd')
const preventiveResponsibleSchema = z.string().min(1, 'Velg ansvarlig person')
const preventiveDeadlineSchema = z.custom<DateValue>((v) => !!v, 'Frist er påkrevd')

const POINTS_MAP: Record<AlcoholDeviationType, number> = {
  SKJENKING_MINDREAARIGE: 8,
  BRUDD_BISTANDSPLIKT: 8,
  UFORSVARLIG_DRIFT: 8,
  HINDRING_KONTROLL: 8,
  SKJENKING_APENBART_BERUSET: 4,
  BRUDD_SJENKETIDER: 4,
  BRENNEVIN_18_19: 4,
  BERUSET_PERSON_I_LOKALET: 2,
  MANGLER_IK_SYSTEM: 2,
  MANGLER_STYRER_STEDFORTREDER: 2,
  NARKOTIKA: 2,
  ALKOHOLFRI_ALTERNATIV_MANGLER: 1,
  MEDBRAKT_ALKOHOL: 1,
  REKLAMEBRUDD: 1,
  VILKAARSBRUDD: 1,
  NEKTET_VISE_LEGITIMASJON: 0,
  GLEMTE_SJEKKE_LEGITIMASJON: 0,
  MINDREAARIG_FORSOK: 0,
  FALSK_LEGITIMASJON: 0,
  UTGAATT_LEGITIMASJON: 0,
  LEGITIMASJON_ANNET: 0,
}

const reportSourceOptions: Array<{ value: AlcoholReportSource; label: string; sub: string }> = [
  { value: 'EGENRAPPORT', label: 'Egenrapport', sub: 'Oppdaget internt' },
  { value: 'SJENKEKONTROLL', label: 'Skjenkekontroll', sub: 'Kommunal kontroll' },
  { value: 'POLITIRAPPORT', label: 'Politirapport', sub: 'Fra politiet' },
]

const deviationTypeGroups = [
  { points: 8, types: [
    { value: 'SKJENKING_MINDREAARIGE' as AlcoholDeviationType, label: 'Skjenking til mindreårige' },
    { value: 'BRUDD_BISTANDSPLIKT' as AlcoholDeviationType, label: 'Brudd på bistandsplikt' },
    { value: 'UFORSVARLIG_DRIFT' as AlcoholDeviationType, label: 'Uforsvarlig drift' },
    { value: 'HINDRING_KONTROLL' as AlcoholDeviationType, label: 'Hindring av kontroll' },
  ]},
  { points: 4, types: [
    { value: 'SKJENKING_APENBART_BERUSET' as AlcoholDeviationType, label: 'Skjenking til åpenbart beruset' },
    { value: 'BRUDD_SJENKETIDER' as AlcoholDeviationType, label: 'Brudd på skjenketider' },
    { value: 'BRENNEVIN_18_19' as AlcoholDeviationType, label: 'Brennevin til 18-19-åringer' },
  ]},
  { points: 2, types: [
    { value: 'BERUSET_PERSON_I_LOKALET' as AlcoholDeviationType, label: 'Beruset person i lokalet' },
    { value: 'MANGLER_IK_SYSTEM' as AlcoholDeviationType, label: 'Mangler IK-system' },
    { value: 'MANGLER_STYRER_STEDFORTREDER' as AlcoholDeviationType, label: 'Mangler styrer/stedfortreder' },
    { value: 'NARKOTIKA' as AlcoholDeviationType, label: 'Narkotika' },
  ]},
  { points: 1, types: [
    { value: 'ALKOHOLFRI_ALTERNATIV_MANGLER' as AlcoholDeviationType, label: 'Alkoholfri alternativ mangler' },
    { value: 'MEDBRAKT_ALKOHOL' as AlcoholDeviationType, label: 'Medbrakt alkohol' },
    { value: 'REKLAMEBRUDD' as AlcoholDeviationType, label: 'Reklamebrudd' },
    { value: 'VILKAARSBRUDD' as AlcoholDeviationType, label: 'Vilkårsbrudd' },
  ]},
  { points: 0, types: [
    { value: 'NEKTET_VISE_LEGITIMASJON' as AlcoholDeviationType, label: 'Nektet å vise legitimasjon' },
    { value: 'GLEMTE_SJEKKE_LEGITIMASJON' as AlcoholDeviationType, label: 'Glemte å sjekke legitimasjon' },
    { value: 'MINDREAARIG_FORSOK' as AlcoholDeviationType, label: 'Mindreårig forsøk' },
    { value: 'FALSK_LEGITIMASJON' as AlcoholDeviationType, label: 'Falsk legitimasjon' },
    { value: 'UTGAATT_LEGITIMASJON' as AlcoholDeviationType, label: 'Utgått legitimasjon' },
    { value: 'LEGITIMASJON_ANNET' as AlcoholDeviationType, label: 'Legitimasjon – annet' },
  ]},
]

const allDeviationTypes = deviationTypeGroups.flatMap((g) =>
  g.types.map((t) => ({ ...t, points: g.points })),
)

const causalOptions: Array<{ value: AlcoholCausalAnalysis; label: string }> = [
  { value: 'MANGLENDE_OPPLAERING', label: 'Manglende opplæring' },
  { value: 'RUTINE_IKKE_FULGT', label: 'Rutine ikke fulgt' },
  { value: 'RUTINE_MANGLER', label: 'Rutine mangler' },
  { value: 'HOYT_TRYKK_STRESS', label: 'Høyt trykk / stress' },
  { value: 'UNDERBEMANNING', label: 'Underbemanning' },
  { value: 'KOMMUNIKASJON', label: 'Kommunikasjon' },
  { value: 'ANNET', label: 'Annet' },
]

const statusOptions: Array<{ value: AlcoholDeviationStatus; label: string }> = [
  { value: 'OPEN', label: 'Åpen' },
  { value: 'UNDER_TREATMENT', label: 'Under behandling' },
  { value: 'CLOSED', label: 'Lukket' },
]

const showPenaltyWarning = computed(() => {
  if (!deviationType.value) return false
  return reportSource.value === 'SJENKEKONTROLL' || reportSource.value === 'POLITIRAPPORT'
})

const selectedPoints = computed(() => {
  if (!deviationType.value) return 0
  return POINTS_MAP[deviationType.value as AlcoholDeviationType] ?? 0
})

const currentTotalPoints = computed(() => props.penaltySummary?.totalPoints ?? 0)

const dialogTitle = computed(() => props.mode === 'create' ? 'Registrer alkoholavvik' : 'Rediger alkoholavvik')
const dialogDescription = computed(() =>
  'Registrer alle hendelser — både de som oppdages internt og de som avdekkes ved kommunal kontroll.',
)
const submitLabel = computed(() => {
  if (props.submitting) return props.mode === 'create' ? 'Registrerer...' : 'Lagrer...'
  return props.mode === 'create' ? 'Registrer avvik' : 'Lagre endringer'
})

watch(
  () => [props.open, props.mode, props.initial],
  () => {
    if (!props.open) return
    if (props.initial) {
      const d = props.initial
      const dt = d.reportedAt ? new Date(d.reportedAt) : null
      reportedDate.value = dt ? new CalendarDate(dt.getFullYear(), dt.getMonth() + 1, dt.getDate()) : undefined
      reportedHours.value = dt ? dt.getHours() : undefined
      reportedMinutes.value = dt ? dt.getMinutes() : undefined
      reportedByUserId.value = d.reportedByUserId ? String(d.reportedByUserId) : ''
      reportSource.value = d.reportSource
      deviationType.value = d.deviationType
      description.value = d.description
      immediateAction.value = d.immediateAction ?? ''
      causalAnalysis.value = d.causalAnalysis ?? ''
      causalExplanation.value = d.causalExplanation ?? ''
      preventiveMeasures.value = d.preventiveMeasures ?? ''
      preventiveDeadline.value = d.preventiveDeadline
        ? stringToCalendarDate(new Date(d.preventiveDeadline).toISOString().slice(0, 10))
        : undefined
      preventiveResponsibleUserId.value = d.preventiveResponsibleUserId ? String(d.preventiveResponsibleUserId) : ''
      status.value = d.status
    } else {
      reportedDate.value = undefined
      reportedHours.value = undefined
      reportedMinutes.value = undefined
      reportedByUserId.value = ''
      reportSource.value = 'EGENRAPPORT'
      deviationType.value = ''
      description.value = ''
      immediateAction.value = ''
      causalAnalysis.value = ''
      causalExplanation.value = ''
      preventiveMeasures.value = ''
      preventiveDeadline.value = undefined
      preventiveResponsibleUserId.value = ''
      status.value = 'OPEN'
    }
    errors.value = {}
  },
)

function onReporterChange(val: string) { reportedByUserId.value = val }
function onDeviationTypeChange(val: string) { deviationType.value = val as AlcoholDeviationType }
function onPreventiveByChange(val: string) { preventiveResponsibleUserId.value = val }

function handleSubmit() {
  const newErrors: Record<string, string> = {}

  const dateResult = reportedDateSchema.safeParse(reportedDate.value)
  if (!dateResult.success) newErrors.reportedDate = dateResult.error.issues[0]?.message ?? ''

  const hoursResult = reportedTimeSchema.safeParse(reportedHours.value)
  if (!hoursResult.success) newErrors.reportedTime = 'Tidspunkt er påkrevd'

  const reporterResult = reporterSchema.safeParse(reportedByUserId.value)
  if (!reporterResult.success) newErrors.reporter = reporterResult.error.issues[0]?.message ?? ''

  const typeResult = deviationTypeSchema.safeParse(deviationType.value)
  if (!typeResult.success) newErrors.deviationType = typeResult.error.issues[0]?.message ?? ''

  const descResult = descriptionSchema.safeParse(description.value.trim())
  if (!descResult.success) newErrors.description = descResult.error.issues[0]?.message ?? ''

  const iaResult = immediateActionSchema.safeParse(immediateAction.value.trim())
  if (!iaResult.success) newErrors.immediateAction = iaResult.error.issues[0]?.message ?? ''

  const causalResult = causalAnalysisSchema.safeParse(causalAnalysis.value)
  if (!causalResult.success) newErrors.causalAnalysis = causalResult.error.issues[0]?.message ?? ''

  const causalExpResult = causalExplanationSchema.safeParse(causalExplanation.value.trim())
  if (!causalExpResult.success) newErrors.causalExplanation = causalExpResult.error.issues[0]?.message ?? ''

  const prevResult = preventiveSchema.safeParse(preventiveMeasures.value.trim())
  if (!prevResult.success) newErrors.preventiveMeasures = prevResult.error.issues[0]?.message ?? ''

  const prevRespResult = preventiveResponsibleSchema.safeParse(preventiveResponsibleUserId.value)
  if (!prevRespResult.success) newErrors.preventiveResponsible = prevRespResult.error.issues[0]?.message ?? ''

  const deadlineResult = preventiveDeadlineSchema.safeParse(preventiveDeadline.value)
  if (!deadlineResult.success) newErrors.preventiveDeadline = deadlineResult.error.issues[0]?.message ?? ''

  if (Object.keys(newErrors).length > 0) {
    errors.value = newErrors
    return
  }
  errors.value = {}

  const base: CreateAlcoholDeviationRequest = {
    reportSource: reportSource.value,
    deviationType: deviationType.value as AlcoholDeviationType,
    description: description.value.trim(),
  }

  if (reportedDate.value) {
    const dateStr = dateValueToString(reportedDate.value)
    const time = timeToString(reportedHours.value, reportedMinutes.value) || '00:00'
    base.reportedAt = new Date(`${dateStr}T${time}:00`).toISOString()
  }
  if (immediateAction.value.trim()) base.immediateAction = immediateAction.value.trim()
  if (causalAnalysis.value) base.causalAnalysis = causalAnalysis.value as AlcoholCausalAnalysis
  if (causalExplanation.value.trim()) base.causalExplanation = causalExplanation.value.trim()
  if (preventiveMeasures.value.trim()) base.preventiveMeasures = preventiveMeasures.value.trim()
  if (preventiveDeadline.value) {
    const dateStr = dateValueToString(preventiveDeadline.value)
    base.preventiveDeadline = new Date(`${dateStr}T00:00:00`).toISOString()
  }
  if (preventiveResponsibleUserId.value) base.preventiveResponsibleUserId = Number(preventiveResponsibleUserId.value)

  if (props.mode === 'edit' && props.initial) {
    emits('update', { id: props.initial.id, data: { ...base, status: status.value } })
  } else {
    emits('create', base)
  }
}
</script>

<template>
  <component :is="inline ? 'div' : Dialog" v-bind="inline ? {} : { open, 'onUpdate:open': (v: boolean) => emits('update:open', v) }">
    <component :is="inline ? 'div' : DialogContent" :class="inline ? '' : 'alcohol-dialog'">
      <DialogHeader v-if="!inline">
        <DialogTitle>{{ dialogTitle }}</DialogTitle>
        <DialogDescription>{{ dialogDescription }}</DialogDescription>
      </DialogHeader>

      <div v-if="inline" class="description-box">
        <p>{{ dialogDescription }}</p>
      </div>

      <form class="form" @submit.prevent="handleSubmit">
        <!-- 1. Grunninfo -->
        <div class="step-header">1. Grunninfo</div>
        <div class="row-2">
          <label :class="['field', { 'field--error': errors.reportedDate }]">
            <span>Dato for hendelse *</span>
            <DatePicker v-model="reportedDate" placeholder="Velg dato" />
            <p v-if="errors.reportedDate" class="error-message">{{ errors.reportedDate }}</p>
          </label>
          <label :class="['field', { 'field--error': errors.reportedTime }]">
            <span>Tidspunkt (ca.) *</span>
            <TimePicker :hours="reportedHours" :minutes="reportedMinutes" @update:hours="reportedHours = $event" @update:minutes="reportedMinutes = $event" placeholder="Velg tid" />
            <p v-if="errors.reportedTime" class="error-message">{{ errors.reportedTime }}</p>
          </label>
        </div>
        <div :class="['field', { 'field--error': errors.reporter }]">
          <span>Hvem rapporterer? *</span>
          <Select :model-value="reportedByUserId" @update:model-value="onReporterChange">
            <SelectTrigger>
              <SelectValue placeholder="Velg ansatt..." />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="m in members" :key="m.userId" :value="String(m.userId)">{{ m.label }}</SelectItem>
            </SelectContent>
          </Select>
          <p v-if="errors.reporter" class="error-message">{{ errors.reporter }}</p>
        </div>

        <!-- Source -->
        <div class="field">
          <span>Kilde</span>
          <div class="segmented-grid segmented-grid--3">
            <button
              v-for="opt in reportSourceOptions"
              :key="opt.value"
              type="button"
              class="segment-button source-btn"
              :class="{ 'segment-button--active': reportSource === opt.value }"
              @click="reportSource = opt.value"
            >
              <strong>{{ opt.label }}</strong>
              <small>{{ opt.sub }}</small>
            </button>
          </div>
        </div>

        <!-- 2. Hva skjedde -->
        <div class="step-header">2. Hva skjedde?</div>
        <div :class="['field', { 'field--error': errors.deviationType }]">
          <span>Type hendelse *</span>
          <Select :model-value="deviationType" @update:model-value="onDeviationTypeChange">
            <SelectTrigger>
              <SelectValue placeholder="Velg kategori..." />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="t in allDeviationTypes" :key="t.value" :value="t.value">
                {{ t.label }} ({{ t.points }}p)
              </SelectItem>
            </SelectContent>
          </Select>
          <p v-if="errors.deviationType" class="error-message">{{ errors.deviationType }}</p>
        </div>

        <!-- Penalty warning -->
        <div v-if="showPenaltyWarning && deviationType" class="penalty-warning">
          <div class="penalty-warning-header">
            <strong class="penalty-points">{{ selectedPoints }}</strong>
            <div>
              <strong>prikker ved {{ reportSource === 'SJENKEKONTROLL' ? 'kommunal kontroll' : 'politirapport' }}</strong>
              <p>Dere har nå {{ currentTotalPoints }} prikker. Dette avviket ville bringe dere til {{ currentTotalPoints + selectedPoints }} {{ currentTotalPoints + selectedPoints >= 12 ? '= inndragning' : '' }}</p>
            </div>
          </div>
        </div>

        <label :class="['field', { 'field--error': errors.description }]">
          <span>Beskriv hendelsen i detalj *</span>
          <Textarea v-model="description" rows="3" placeholder="Hva skjedde? Hvem var involvert? Hvor i lokalet? Var det vitner?" />
          <p v-if="errors.description" class="error-message">{{ errors.description }}</p>
        </label>

        <!-- 3. Umiddelbar handling -->
        <div class="step-header">3. Umiddelbar handling</div>
        <label :class="['field', { 'field--error': errors.immediateAction }]">
          <span>Hva ble gjort med en gang for å håndtere situasjonen? *</span>
          <Textarea v-model="immediateAction" rows="3" placeholder="F.eks.: Stoppet servering, sjekket ID, tilkalte vekter, ringte taxi..." />
          <p v-if="errors.immediateAction" class="error-message">{{ errors.immediateAction }}</p>
        </label>

        <!-- 4. Årsaksanalyse -->
        <div class="step-header">4. Årsaksanalyse</div>
        <div :class="['field', { 'field--error': errors.causalAnalysis }]">
          <span>Hvorfor skjedde dette? Hva sviktet? *</span>
          <div class="chip-grid">
            <button
              v-for="opt in causalOptions"
              :key="opt.value"
              type="button"
              class="chip"
              :class="{ 'chip--active': causalAnalysis === opt.value }"
              @click="causalAnalysis = causalAnalysis === opt.value ? '' : opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
          <p v-if="errors.causalAnalysis" class="error-message">{{ errors.causalAnalysis }}</p>
        </div>
        <label :class="['field', { 'field--error': errors.causalExplanation }]">
          <span>Utdyp: Hva er den egentlige årsaken? *</span>
          <Textarea v-model="causalExplanation" rows="2" placeholder="Utdyp: Hva er den egentlige årsaken?" />
          <p v-if="errors.causalExplanation" class="error-message">{{ errors.causalExplanation }}</p>
        </label>

        <!-- 5. Forebyggende tiltak -->
        <div class="step-header">5. Forebyggende tiltak</div>
        <label :class="['field', { 'field--error': errors.preventiveMeasures }]">
          <span>Hva skal gjøres for å hindre at dette skjer igjen? *</span>
          <Textarea v-model="preventiveMeasures" rows="3" placeholder="F.eks.: Oppdatere rutine, gjennomføre ny opplæring, endre bemanning..." />
          <p v-if="errors.preventiveMeasures" class="error-message">{{ errors.preventiveMeasures }}</p>
        </label>
        <div class="row-2">
          <div :class="['field', { 'field--error': errors.preventiveResponsible }]">
            <span>Ansvarlig for oppfølging *</span>
            <Select :model-value="preventiveResponsibleUserId" @update:model-value="onPreventiveByChange">
              <SelectTrigger>
                <SelectValue placeholder="Velg person..." />
              </SelectTrigger>
              <SelectContent open-upward>
                <SelectItem v-for="m in members" :key="m.userId" :value="String(m.userId)">{{ m.label }}</SelectItem>
              </SelectContent>
            </Select>
            <p v-if="errors.preventiveResponsible" class="error-message">{{ errors.preventiveResponsible }}</p>
          </div>
          <div :class="['field', 'field--date-right', { 'field--error': errors.preventiveDeadline }]">
            <span>Frist for gjennomføring *</span>
            <DatePicker v-model="preventiveDeadline" placeholder="Velg frist" open-upward />
            <p v-if="errors.preventiveDeadline" class="error-message">{{ errors.preventiveDeadline }}</p>
          </div>
        </div>

        <!-- Status (edit mode) -->
        <div v-if="mode === 'edit'" class="status-section">
          <span class="status-title">Status</span>
          <div class="status-chips">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              type="button"
              class="status-chip"
              :class="{ 'status-chip--active': status === opt.value }"
              @click="status = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <div class="form-footer">
          <Button type="button" variant="outline" @click="emits('update:open', false)">Avbryt</Button>
          <Button type="submit" :disabled="submitting">{{ submitLabel }}</Button>
        </div>
      </form>
    </component>
  </component>
</template>

<style scoped>
.alcohol-dialog { max-width: 48rem; max-height: 90vh; overflow-y: auto; overflow-x: hidden; display: flex; flex-direction: column; }
.form { display: flex; flex-direction: column; gap: 14px; min-width: 0; }
.field { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.field > span { font-size: 0.92rem; font-weight: 600; }

.description-box {
  background: hsl(var(--secondary));
  border-radius: var(--radius-md);
  padding: 14px 16px;
  margin: 4px 0 8px;
}
.description-box p {
  margin: 0;
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.45;
}

.row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; min-width: 0; }
.row-2 > * { min-width: 0; }

.step-header {
  font-size: 1.05rem;
  font-weight: 700;
  margin-top: 6px;
  color: hsl(var(--foreground));
}

.segmented-grid { display: grid; gap: 8px; }
.segmented-grid--3 { grid-template-columns: repeat(3, minmax(0, 1fr)); }

.segment-button {
  border: 1px solid hsl(var(--input));
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  border-radius: var(--radius-md);
  padding: 10px 8px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  transition: all 150ms ease;
}
.segment-button strong { font-size: 0.95rem; }
.segment-button small { font-size: 0.78rem; color: hsl(var(--muted-foreground)); }

.source-btn.segment-button--active {
  border-color: hsl(var(--primary));
  background: hsl(var(--accent));
  color: hsl(var(--accent-foreground));
}

.chip-grid { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  border: 1px solid hsl(var(--input));
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  border-radius: var(--radius-pill);
  padding: 8px 16px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms ease;
}
.chip--active {
  border-color: hsl(var(--primary));
  background: hsl(var(--accent));
  color: hsl(var(--accent-foreground));
  font-weight: 700;
}

.penalty-warning {
  background: #fde8e8;
  border-left: 4px solid #c62828;
  border-radius: var(--radius-md);
  padding: 12px 14px;
}
.penalty-warning-header {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.penalty-points {
  font-size: 1.5rem;
  color: #c62828;
  line-height: 1;
}
.penalty-warning strong { color: #7f1d1d; font-size: 0.9rem; }
.penalty-warning p { margin: 2px 0 0; color: #991b1b; font-size: 0.82rem; }

.status-section {
  background: hsl(var(--secondary));
  border-radius: var(--radius-md);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.status-title { font-weight: 600; font-size: 0.95rem; }
.status-chips { display: flex; gap: 8px; flex-wrap: wrap; }
.status-chip {
  border: 1.5px solid hsl(var(--input));
  background: hsl(var(--card));
  border-radius: var(--radius-pill);
  padding: 6px 14px;
  font-size: 0.88rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms ease;
}
.status-chip--active {
  border-color: hsl(var(--primary));
  background: hsl(var(--accent));
  color: hsl(var(--accent-foreground));
  font-weight: 700;
}

.error-message { color: hsl(var(--destructive)); font-size: 0.86rem; }

.field--error :deep(.input),
.field--error :deep(.textarea),
.field--error :deep(.date-picker__trigger),
.field--error :deep(.time-picker__field),
.field--error :deep(.select-trigger) {
  border-color: hsl(var(--destructive));
}

.field--error .chip:not(.chip--active) {
  border-color: hsl(var(--destructive));
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  padding-top: 4px;
}

@media (max-width: 640px) {
  .row-2 { grid-template-columns: 1fr; }
  .segmented-grid--3 { grid-template-columns: 1fr; }
  .form-footer { flex-direction: column-reverse; }
}

.field--date-right :deep(.date-picker__panel) {
  left: auto;
  right: 0;
}
</style>

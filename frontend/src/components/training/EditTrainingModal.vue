<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import axios from 'axios'
import { toast } from 'vue-sonner'
import { z } from 'zod'
import { CalendarDate } from '@internationalized/date'
import type { DateValue } from '@internationalized/date'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import {
  useUpdateTrainingLogMutation,
  useOrganizationMembersQuery,
} from '@/composables/useTrainingLogs'
import type { TrainingLog, TrainingStatus } from '@/types/training'

const props = defineProps<{
  open: boolean
  training: TrainingLog | null
}>()
const emits = defineEmits<{ (e: 'update:open', value: boolean): void }>()

const updateMutation = useUpdateTrainingLogMutation()
const canManage = computed(() => true)
const membersQuery = useOrganizationMembersQuery(canManage)
const members = computed(() => membersQuery.data.value ?? [])

const employeeUserId = ref<string>('')
const title = ref('')
const description = ref('')
const completedAt = ref<DateValue | undefined>()
const expiresAt = ref<DateValue | undefined>()
const status = ref<TrainingStatus>('COMPLETED')
const errors = ref<Record<string, string>>({})

const titleSchema = z.string().min(1, 'Opplæringstype er påkrevd')
const descriptionSchema = z.string().min(1, 'Beskrivelse er påkrevd')
const completedAtSchema = z.custom<DateValue>((v) => !!v, 'Fullført dato er påkrevd')
const expiresAtSchema = z.custom<DateValue>((v) => !!v, 'Utløpsdato er påkrevd')

const statusOptions: Array<{ value: TrainingStatus; label: string }> = [
  { value: 'COMPLETED', label: 'Fullført' },
  { value: 'EXPIRES_SOON', label: 'Utløper snart' },
  { value: 'EXPIRED', label: 'Utgått' },
  { value: 'NOT_COMPLETED', label: 'Ikke fullført' },
]

function isoToDateInput(iso: string | null): string {
  if (!iso) return ''
  return iso.slice(0, 10)
}

function stringToCalendarDate(str: string): CalendarDate | undefined {
  if (!str) return undefined
  const [y, m, d] = str.split('-').map(Number)
  return new CalendarDate(y!, m!, d!)
}

function dateValueToIso(dv: DateValue | undefined): string | undefined {
  if (!dv) return undefined
  return new Date(dv.year, dv.month - 1, dv.day).toISOString()
}

watch(
  () => [props.open, props.training],
  ([isOpen]) => {
    if (!isOpen) return
    if (props.training) {
      employeeUserId.value = String(props.training.employeeUserId)
      title.value = props.training.title
      description.value = props.training.description ?? ''
      completedAt.value = stringToCalendarDate(isoToDateInput(props.training.completedAt))
      expiresAt.value = stringToCalendarDate(isoToDateInput(props.training.expiresAt))
      status.value = props.training.status
    }
    errors.value = {}
  },
)

function closeDialog() {
  emits('update:open', false)
}

async function handleSubmit() {
  if (!props.training) return

  const newErrors: Record<string, string> = {}

  const titleResult = titleSchema.safeParse(title.value.trim())
  if (!titleResult.success) newErrors.title = titleResult.error.issues[0]?.message ?? ''

  const descResult = descriptionSchema.safeParse(description.value.trim())
  if (!descResult.success) newErrors.description = descResult.error.issues[0]?.message ?? ''

  const completedResult = completedAtSchema.safeParse(completedAt.value)
  if (!completedResult.success) newErrors.completedAt = completedResult.error.issues[0]?.message ?? ''

  const expiresResult = expiresAtSchema.safeParse(expiresAt.value)
  if (!expiresResult.success) newErrors.expiresAt = expiresResult.error.issues[0]?.message ?? ''

  if (Object.keys(newErrors).length > 0) {
    errors.value = newErrors
    return
  }
  errors.value = {}

  try {
    await updateMutation.mutateAsync({
      id: props.training.id,
      payload: {
        employeeUserId: Number(employeeUserId.value) || undefined,
        title: title.value.trim() || undefined,
        description: description.value.trim() || undefined,
        completedAt: dateValueToIso(completedAt.value),
        expiresAt: dateValueToIso(expiresAt.value),
        status: status.value,
      },
    })
    toast.success('Opplæring oppdatert')
    closeDialog()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const msg = error.response?.data?.error?.message
      if (typeof msg === 'string' && msg.trim()) {
        toast.error(msg)
        return
      }
    }
    toast.error('Kunne ikke oppdatere opplæring')
  }
}
</script>

<template>
  <Dialog :open="open" @update:open="(value) => emits('update:open', value)">
    <DialogContent class="training-dialog">
      <DialogHeader>
        <DialogTitle>Rediger opplæring</DialogTitle>
        <DialogDescription>Oppdater informasjonen for valgt opplæring</DialogDescription>
      </DialogHeader>

      <form class="form" @submit.prevent="handleSubmit">
        <div :class="['field', { 'field--error': errors.employee }]">
          <span>Ansatt</span>
          <Select v-model="employeeUserId">
            <SelectTrigger>
              <SelectValue placeholder="Velg ansatt..." />
            </SelectTrigger>
            <SelectContent>
              <SelectItem v-for="m in members" :key="m.userId" :value="String(m.userId)">
                {{ m.userFullName }}
              </SelectItem>
            </SelectContent>
          </Select>
          <p v-if="errors.employee" class="error-message">{{ errors.employee }}</p>
        </div>

        <label :class="['field', { 'field--error': errors.title }]">
          <span>Opplæringstype *</span>
          <Input v-model="title" placeholder="Skriv inn type..." />
          <p v-if="errors.title" class="error-message">{{ errors.title }}</p>
        </label>

        <label :class="['field', { 'field--error': errors.description }]">
          <span>Beskrivelse *</span>
          <Textarea v-model="description" rows="3" placeholder="Beskriv opplæringen..." />
          <p v-if="errors.description" class="error-message">{{ errors.description }}</p>
        </label>

        <div class="field-row">
          <div :class="['field', { 'field--error': errors.completedAt }]">
            <span>Fullført dato *</span>
            <DatePicker v-model="completedAt" placeholder="Velg dato" open-upward />
            <p v-if="errors.completedAt" class="error-message">{{ errors.completedAt }}</p>
          </div>
          <div :class="['field', { 'field--error': errors.expiresAt }]">
            <span>Utløpsdato *</span>
            <DatePicker v-model="expiresAt" placeholder="Velg dato" open-upward />
            <p v-if="errors.expiresAt" class="error-message">{{ errors.expiresAt }}</p>
          </div>
        </div>

        <div class="field">
          <span>Status</span>
          <div class="segmented-grid segmented-grid--4">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              type="button"
              class="segment-button"
              :class="[
                `segment-button--${opt.value.toLowerCase()}`,
                { 'segment-button--active': status === opt.value },
              ]"
              @click="status = opt.value"
            >
              {{ opt.label }}
            </button>
          </div>
        </div>

        <DialogFooter>
          <Button type="button" variant="outline" @click="closeDialog">Avbryt</Button>
          <Button type="submit" :disabled="updateMutation.isPending.value">
            {{ updateMutation.isPending.value ? 'Lagrer...' : 'Lagre endringer' }}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
.form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field > span {
  font-size: 0.92rem;
  font-weight: 600;
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.segmented-grid {
  display: grid;
  gap: 8px;
}

.segmented-grid--4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.segment-button {
  border: 1px solid hsl(var(--input));
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  border-radius: var(--radius-md);
  height: 2.5rem;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 150ms ease, border-color 150ms ease, color 150ms ease, box-shadow 150ms ease;
}

.segment-button--active {
  border-color: hsl(var(--primary));
  background: hsl(var(--accent));
  color: hsl(var(--accent-foreground));
}

.segment-button--completed.segment-button--active {
  border-color: #71a66a;
  background: #dcebd8;
  color: #2f6f34;
}

.segment-button--expires_soon.segment-button--active {
  border-color: #d1a768;
  background: #f4e6d1;
  color: #d0a11f;
}

.segment-button--expired.segment-button--active {
  border-color: #d68b3b;
  background: #f6dfc8;
  color: #a2550c;
}

.segment-button--not_completed.segment-button--active {
  border-color: #c95d5d;
  background: #f4e0e0;
  color: #902324;
}

.training-dialog {
  max-width: 28rem;
}

.error-message {
  color: hsl(var(--destructive));
  font-size: 0.8rem;
  margin-top: 2px;
}

.field--error :deep(.input),
.field--error :deep(.textarea),
.field--error :deep(.date-picker__trigger),
.field--error :deep(.select-trigger) {
  border-color: hsl(var(--destructive));
}

@media (max-width: 780px) {
  .segmented-grid--4 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>

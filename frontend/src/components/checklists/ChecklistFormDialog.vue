<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {z} from 'zod'
import type {
  Checklist,
  ChecklistFrequency,
  CreateChecklistRequest,
  UpdateChecklistRequest
} from '@/types/checklist'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    mode: 'create' | 'edit'
    initialChecklist?: Checklist | null
    submitting?: boolean
  }>(),
  {
    initialChecklist: null,
    submitting: false,
  },
)

const emits = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'create', payload: CreateChecklistRequest): void
  (e: 'update', payload: { checklistId: number; data: UpdateChecklistRequest }): void
}>()

const formName = ref('')
const formDescription = ref('')
const formFrequency = ref<ChecklistFrequency>('DAILY')
const formActive = ref(true)
const nameSchema = z.string().min(1, 'Navn på sjekkliste er påkrevd')
const errors = ref<Record<string, string>>({})

const title = computed(() =>
  props.mode === 'create' ? 'Opprett ny sjekkliste' : 'Rediger sjekkliste',
)

const submitLabel = computed(() => {
  if (props.submitting) {
    return props.mode === 'create' ? 'Oppretter...' : 'Lagrer...'
  }
  return props.mode === 'create' ? 'Opprett sjekkliste' : 'Lagre endringer'
})

watch(
  () => [props.open, props.initialChecklist, props.mode],
  ([open]) => {
    if (!open) {
      return
    }

    if (props.mode === 'edit' && props.initialChecklist) {
      formName.value = props.initialChecklist.name
      formDescription.value = props.initialChecklist.description ?? ''
      formFrequency.value = props.initialChecklist.frequency
      formActive.value = props.initialChecklist.active
    } else {
      formName.value = ''
      formDescription.value = ''
      formFrequency.value = 'DAILY'
      formActive.value = true
    }

    errors.value = {}
  },
  {immediate: true},
)

function closeDialog() {
  emits('update:open', false)
}

function handleSubmit() {
  const newErrors: Record<string, string> = {}
  const nameResult = nameSchema.safeParse(formName.value.trim())
  if (!nameResult.success) newErrors.name = nameResult.error.issues[0]?.message ?? ''

  if (Object.keys(newErrors).length > 0) {
    errors.value = newErrors
    return
  }
  errors.value = {}

  const name = formName.value.trim()

  if (props.mode === 'create') {
    emits('create', {
      name,
      description: formDescription.value.trim() || undefined,
      frequency: formFrequency.value,
    })
    return
  }

  if (!props.initialChecklist) {
    return
  }

  emits('update', {
    checklistId: props.initialChecklist.id,
    data: {
      name,
      description: formDescription.value.trim() || undefined,
      frequency: formFrequency.value,
      active: formActive.value,
    },
  })
}
</script>

<template>
  <Dialog :open="open" @update:open="(value) => emits('update:open', value)">
    <DialogContent class="checklist-dialog">
      <DialogHeader>
        <DialogTitle>{{ title }}</DialogTitle>
        <DialogDescription>
          Definer navn, frekvens og innhold for sjekklisten.
        </DialogDescription>
      </DialogHeader>

      <form class="form" @submit.prevent="handleSubmit">
        <label :class="['field', { 'field--error': errors.name }]">
          <span>Navn</span>
          <Input v-model="formName" placeholder="For eksempel: Morgenrutine kjøkken"/>
          <p v-if="errors.name" class="error-message">{{ errors.name }}</p>
        </label>

        <div class="field">
          <span>Frekvens</span>
          <Select v-model="formFrequency">
            <SelectTrigger>
              <SelectValue placeholder="Velg frekvens"/>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="DAILY">Daglig</SelectItem>
              <SelectItem value="WEEKLY">Ukentlig</SelectItem>
              <SelectItem value="MONTHLY">Månedlig</SelectItem>
              <SelectItem value="YEARLY">Årlig</SelectItem>
            </SelectContent>
          </Select>
        </div>

        <label class="field">
          <span>Beskrivelse (valgfritt)</span>
          <Textarea v-model="formDescription" rows="3"
                    placeholder="Kort beskrivelse av hva sjekklisten gjelder"/>
        </label>

        <DialogFooter>
          <Button type="button" variant="outline" @click="closeDialog">Avbryt</Button>
          <Button type="submit" :disabled="submitting">{{ submitLabel }}</Button>
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

.error-message {
  color: hsl(var(--destructive));
  font-size: 0.8rem;
  margin-top: 2px;
}

.field--error :deep(.input),
.field--error :deep(.textarea),
.field--error :deep(.select-trigger) {
  border-color: hsl(var(--destructive));
}

@media (max-width: 720px) {
  .checklist-dialog {
    width: min(96vw, 32rem);
  }
}
</style>

<script setup lang="ts">
import {ref, watch} from 'vue'
import {z} from 'zod'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import {getDefaultThreshold} from '@/composables/useTemperatureMonitoring'
import type {TemperatureApplianceType, TemperatureThreshold} from '@/types/temperature'

const props = withDefaults(defineProps<{
  open: boolean
  mode?: 'create' | 'edit'
  initialName?: string
  initialType?: TemperatureApplianceType
  initialMin?: number
  initialMax?: number
}>(), {
  mode: 'create',
  initialName: '',
  initialType: 'FRIDGE',
  initialMin: 0,
  initialMax: 4,
})

const emits = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'submit', payload: {
    name: string;
    type: TemperatureApplianceType;
    threshold: TemperatureThreshold
  }): void
}>()

const name = ref('')
const type = ref<TemperatureApplianceType>('FRIDGE')
const min = ref(0)
const max = ref(4)
const errors = ref<Record<string, string>>({})
const nameSchema = z.string().min(1, 'Navn er påkrevd')

watch(() => props.open, (isOpen) => {
  if (!isOpen) return
  name.value = props.initialName
  type.value = props.initialType
  min.value = props.initialMin
  max.value = props.initialMax
  errors.value = {}
})

watch(type, (nextType) => {
  if (props.mode === 'create') {
    const defaults = getDefaultThreshold(nextType)
    min.value = defaults.min
    max.value = defaults.max
  }
})

function toTypeLabel(t: TemperatureApplianceType): string {
  return t === 'FRIDGE' ? 'Kjøleskap' : 'Fryser'
}

function handleSubmit() {
  const newErrors: Record<string, string> = {}
  const trimmed = name.value.trim()

  const nameResult = nameSchema.safeParse(trimmed)
  if (!nameResult.success) newErrors.name = nameResult.error.issues[0]?.message ?? ''

  const threshold: TemperatureThreshold = {min: Number(min.value), max: Number(max.value)}
  if (threshold.min >= threshold.max) newErrors.threshold = 'Min må være lavere enn maks'

  if (Object.keys(newErrors).length > 0) {
    errors.value = newErrors
    return
  }
  errors.value = {}

  emits('submit', {name: trimmed, type: type.value, threshold})
}
</script>

<template>
  <Dialog :open="open" @update:open="(v) => emits('update:open', v)">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{{
            mode === 'create' ? 'Legg til hvitevare' : 'Rediger hvitevare'
          }}
        </DialogTitle>
        <DialogDescription>
          {{
            mode === 'create'
              ? 'Type setter standard grense automatisk. Du kan endre verdiene før lagring.'
              : 'Oppdater navn og temperaturgrense for valgt enhet.'
          }}
        </DialogDescription>
      </DialogHeader>

      <div class="form-grid">
        <label :class="['field', { 'field--error': errors.name }]">
          <span>Navn</span>
          <Input v-model="name"
                 :placeholder="mode === 'create' ? 'For eksempel: Kjøleskap kjøkken' : 'Navn'"/>
          <p v-if="errors.name" class="field-error" role="alert">{{ errors.name }}</p>
        </label>

        <div class="field" v-if="mode === 'create'">
          <span>Type</span>
          <Select :model-value="type"
                  @update:model-value="(v) => (type = v as TemperatureApplianceType)">
            <SelectTrigger>
              <SelectValue placeholder="Velg type"/>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="FRIDGE">Kjøleskap</SelectItem>
              <SelectItem value="FREEZER">Fryser</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <label v-else class="field">
          <span>Type</span>
          <Input :model-value="toTypeLabel(type)" disabled/>
        </label>

        <label class="field">
          <span>Min temperatur (°C)</span>
          <Input v-model="min" type="number"/>
        </label>

        <label :class="['field', { 'field--error': errors.threshold }]">
          <span>Maks temperatur (°C)</span>
          <Input v-model="max" type="number"/>
          <p v-if="errors.threshold" class="field-error" role="alert">{{ errors.threshold }}</p>
        </label>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="emits('update:open', false)">Avbryt</Button>
        <Button @click="handleSubmit">{{
            mode === 'create' ? 'Lagre enhet' : 'Lagre endringer'
          }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field span {
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
}

.field-error {
  color: hsl(var(--destructive));
  font-size: 0.8rem;
  margin-top: 2px;
}

.field--error :deep(.input),
.field--error :deep(.select-trigger) {
  border-color: hsl(var(--destructive));
}

@media (max-width: 920px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { z } from 'zod'
import type {
  Checklist,
  ChecklistItem,
  CreateChecklistItemRequest,
  UpdateChecklistItemRequest,
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

const props = withDefaults(
  defineProps<{
    open: boolean
    mode: 'create' | 'edit'
    checklist: Checklist | null
    initialItem?: ChecklistItem | null
    submitting?: boolean
  }>(),
  {
    initialItem: null,
    submitting: false,
  },
)

const emits = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'create', payload: { checklistId: number; data: CreateChecklistItemRequest }): void
  (e: 'update', payload: { checklistId: number; itemId: number; data: UpdateChecklistItemRequest }): void
}>()

const formTitle = ref('')
const formDescription = ref('')
const titleSchema = z.string().min(1, 'Oppgaven må ha en tittel')
const errors = ref<Record<string, string>>({})

const modalTitle = computed(() =>
  props.mode === 'create' ? 'Legg til oppgave' : 'Rediger oppgave',
)

const submitLabel = computed(() => {
  if (props.submitting) {
    return props.mode === 'create' ? 'Lagrer...' : 'Oppdaterer...'
  }
  return props.mode === 'create' ? 'Legg til oppgave' : 'Lagre oppgave'
})

watch(
  () => [props.open, props.initialItem, props.mode],
  ([open]) => {
    if (!open) {
      return
    }

    if (props.mode === 'edit' && props.initialItem) {
      formTitle.value = props.initialItem.title
      formDescription.value = props.initialItem.description ?? ''
    } else {
      formTitle.value = ''
      formDescription.value = ''
    }

    errors.value = {}
  },
  { immediate: true },
)

function closeDialog() {
  emits('update:open', false)
}

function handleSubmit() {
  const newErrors: Record<string, string> = {}
  const titleResult = titleSchema.safeParse(formTitle.value.trim())
  if (!titleResult.success) newErrors.title = titleResult.error.issues[0]?.message ?? ''

  if (Object.keys(newErrors).length > 0) {
    errors.value = newErrors
    return
  }
  errors.value = {}

  const title = formTitle.value.trim()

  if (!props.checklist) {
    return
  }

  const data = {
    title,
    description: formDescription.value.trim() || undefined,
  }

  if (props.mode === 'create') {
    emits('create', {
      checklistId: props.checklist.id,
      data,
    })
    return
  }

  if (!props.initialItem) {
    return
  }

  emits('update', {
    checklistId: props.checklist.id,
    itemId: props.initialItem.id,
    data,
  })
}
</script>

<template>
  <Dialog :open="open" @update:open="(value) => emits('update:open', value)">
    <DialogContent class="item-dialog">
      <DialogHeader>
        <DialogTitle>{{ modalTitle }}</DialogTitle>
        <DialogDescription>
          {{ checklist?.name }}
        </DialogDescription>
      </DialogHeader>

      <form class="form" @submit.prevent="handleSubmit">
        <label :class="['field', { 'field--error': errors.title }]">
          <span>Tittel</span>
          <Input v-model="formTitle" placeholder="For eksempel: Sjekk temperatur i kjøleskap" />
          <p v-if="errors.title" class="error-message">{{ errors.title }}</p>
        </label>

        <label class="field">
          <span>Beskrivelse (valgfritt)</span>
          <Textarea v-model="formDescription" rows="3" placeholder="Detaljerte instrukser" />
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

.field span {
  font-size: 0.92rem;
  font-weight: 600;
}

.error-message {
  color: hsl(var(--destructive));
  font-size: 0.86rem;
}

.field--error :deep(.input),
.field--error :deep(.textarea) {
  border-color: hsl(var(--destructive));
}

@media (max-width: 720px) {
  .item-dialog {
    width: min(96vw, 32rem);
  }
}
</style>

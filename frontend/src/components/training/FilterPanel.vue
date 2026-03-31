<script setup lang="ts">
import { ref } from 'vue'
import { Filter, ChevronDown, ChevronUp, X } from 'lucide-vue-next'
import { Separator } from '@/components/ui/separator'

const props = defineProps<{
  types: string[]
  modelType: string
  modelStatus: string
}>()
const emit = defineEmits<{
  'update:modelType':   [value: string]
  'update:modelStatus': [value: string]
}>()

const open = ref(false)
const statuses = ['Gyldig', 'Utløper snart', 'Mangler'] as const

function toggle(field: 'type' | 'status', val: string): void {
  if (field === 'type') {
    emit('update:modelType', props.modelType === val ? '' : val)
  } else {
    emit('update:modelStatus', props.modelStatus === val ? '' : val)
  }
}

const hasFilters = (): boolean => !!(props.modelType || props.modelStatus)
</script>

<template>
  <div class="mb-3">
    <!-- Bar -->
    <div class="flex items-center gap-2 flex-wrap">
      <button
        class="flex items-center gap-1.5 border border-stone-200 bg-white rounded-lg px-3 py-1.5 text-sm text-gray-600 hover:bg-stone-50 transition-colors"
        @click="open = !open"
      >
        <Filter :size="14" /> Filtrer
        <component :is="open ? ChevronUp : ChevronDown" :size="13" />
      </button>

      <!-- Active chips -->
      <template v-if="hasFilters()">
        <span v-if="modelType" class="flex items-center gap-1 text-xs bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-full px-2.5 py-0.5">
          {{ modelType }}
          <button @click="emit('update:modelType', '')"><X :size="11" /></button>
        </span>
        <span v-if="modelStatus" class="flex items-center gap-1 text-xs bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-full px-2.5 py-0.5">
          {{ modelStatus }}
          <button @click="emit('update:modelStatus', '')"><X :size="11" /></button>
        </span>
        <button class="text-xs text-gray-400 underline" @click="emit('update:modelType', ''); emit('update:modelStatus', '')">Fjern alle</button>
      </template>
    </div>

    <!-- Panel -->
    <div v-if="open" class="mt-2 bg-white border border-stone-200 rounded-xl p-4 flex flex-col gap-3">
      <div>
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Opplæringstype</p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="t in types" :key="t"
            :class="['text-sm px-3 py-1 rounded-full border transition-colors', modelType === t ? 'bg-emerald-700 text-white border-emerald-700' : 'border-stone-200 text-gray-600 hover:bg-stone-50']"
            @click="toggle('type', t)"
          >{{ t }}</button>
        </div>
      </div>
      <Separator />
      <div>
        <p class="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Status</p>
        <div class="flex flex-wrap gap-2">
          <button
            v-for="s in statuses" :key="s"
            :class="['text-sm px-3 py-1 rounded-full border transition-colors', modelStatus === s ? 'bg-emerald-700 text-white border-emerald-700' : 'border-stone-200 text-gray-600 hover:bg-stone-50']"
            @click="toggle('status', s)"
          >{{ s }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

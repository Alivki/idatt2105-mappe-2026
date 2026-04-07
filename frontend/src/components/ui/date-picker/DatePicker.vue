<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount } from "vue"
import type { DateValue } from "@internationalized/date"
import { DateFormatter, getLocalTimeZone, today } from "@internationalized/date"
import { CalendarIcon } from "lucide-vue-next"
import { Calendar } from "@/components/ui/calendar"

const props = withDefaults(defineProps<{
  modelValue?: DateValue
  placeholder?: string
  locale?: string
  openUpward?: boolean
}>(), {
  placeholder: "Velg dato",
  locale: "nb-NO",
  openUpward: false,
})

const emits = defineEmits<{
  (e: "update:modelValue", value: DateValue): void
}>()

const isOpen = ref(false)
const rootRef = ref<HTMLElement | null>(null)
const defaultPlaceholder = today(getLocalTimeZone())
const df = new DateFormatter(props.locale, { dateStyle: "long" })

const displayText = computed(() => {
  if (!props.modelValue) return null
  return df.format(props.modelValue.toDate(getLocalTimeZone()))
})

function onSelect(val: DateValue) {
  emits("update:modelValue", val)
  isOpen.value = false
}

function onClickOutside(e: MouseEvent) {
  if (rootRef.value && !rootRef.value.contains(e.target as Node)) {
    isOpen.value = false
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === "Escape") isOpen.value = false
}

watch(isOpen, (open) => {
  if (open) {
    document.addEventListener("mousedown", onClickOutside)
    document.addEventListener("keydown", onKeydown)
  } else {
    document.removeEventListener("mousedown", onClickOutside)
    document.removeEventListener("keydown", onKeydown)
  }
})

onBeforeUnmount(() => {
  document.removeEventListener("mousedown", onClickOutside)
  document.removeEventListener("keydown", onKeydown)
})
</script>

<template>
  <div ref="rootRef" class="date-picker">
    <button type="button" class="date-picker__trigger" @click="isOpen = !isOpen">
      <span class="date-picker__icon">
        <CalendarIcon />
      </span>
      <span :class="['date-picker__text', !modelValue && 'date-picker__text--placeholder']">
        {{ displayText ?? placeholder }}
      </span>
    </button>

    <Transition name="date-picker-panel">
      <div v-if="isOpen" :class="['date-picker__panel', openUpward && 'date-picker__panel--up']">
        <Calendar
          :model-value="modelValue"
          :default-placeholder="defaultPlaceholder"
          initial-focus
          @update:model-value="onSelect"
        />
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.date-picker {
  position: relative;
  display: inline-block;
  width: 100%;
}

.date-picker__trigger {
  display: flex;
  align-items: center;
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--input, 35 15% 85%));
  background-color: hsl(var(--card, 40 25% 98%));
  padding: 0;
  cursor: pointer;
  color: inherit;
  font-family: inherit;
  font-size: 0.875rem;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  transition: border-color 150ms, box-shadow 150ms;
  outline: none;
}

.date-picker__trigger:focus-visible {
  box-shadow: 0 0 0 2px hsl(var(--ring, 245 43% 52%) / 0.2);
  border-color: hsl(var(--primary, 245 43% 52%) / 0.5);
}

.date-picker__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 0.75rem;
  color: hsl(var(--muted-foreground, 24 5% 46%));
}
.date-picker__icon :deep(svg) {
  width: 1rem;
  height: 1rem;
}

.date-picker__text {
  flex: 1;
  text-align: left;
  padding: 0 0.75rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.date-picker__text--placeholder {
  color: hsl(var(--muted-foreground, 24 5% 46%));
}

.date-picker__panel {
  position: absolute;
  z-index: 50;
  top: calc(100% + 4px);
  left: 0;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border, 35 15% 90%));
  background-color: hsl(var(--card, 40 25% 98%));
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
}

.date-picker__panel--up {
  top: auto;
  bottom: calc(100% + 4px);
}

.date-picker-panel-enter-active {
  animation: dp-in 150ms ease-out;
}
.date-picker-panel-leave-active {
  animation: dp-out 100ms ease-in;
}

@keyframes dp-in {
  from { opacity: 0; transform: scale(0.95) translateY(-4px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes dp-out {
  from { opacity: 1; transform: scale(1) translateY(0); }
  to { opacity: 0; transform: scale(0.95) translateY(-4px); }
}
</style>

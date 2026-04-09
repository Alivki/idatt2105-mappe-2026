<script setup lang="ts">
import {ref, computed, watch, nextTick, onBeforeUnmount} from "vue"
import {Clock, ChevronDown} from "lucide-vue-next"

const props = withDefaults(defineProps<{
  hours?: number
  minutes?: number
  minuteStep?: number
  placeholder?: string
}>(), {
  minuteStep: 1,
  placeholder: "00:00",
})

const emits = defineEmits<{
  (e: "update:hours", value: number): void
  (e: "update:minutes", value: number): void
}>()

const isOpen = ref(false)

const hourOptions = Array.from({length: 24}, (_, i) => i)
const minuteOptions = computed(() => {
  const step = props.minuteStep
  return Array.from({length: Math.floor(60 / step)}, (_, i) => i * step)
})

function pad(n: number | undefined): string {
  return n != null ? String(n).padStart(2, "0") : "00"
}

const displayValue = computed(() => `${pad(props.hours)}:${pad(props.minutes)}`)

function parseTime(val: string): { h: number; m: number } | null {
  const trimmed = val.trim()
  const match =
    trimmed.match(/^(\d{1,2})\s*[:,.\-\s]\s*(\d{1,2})$/) ||
    trimmed.match(/^(\d{1,2})(\d{2})$/) ||
    trimmed.match(/^(\d{1,2})$/)
  if (!match) return null
  return {
    h: Math.min(23, Math.max(0, Number(match[1]))),
    m: match[2] ? Math.min(59, Math.max(0, Number(match[2]))) : 0,
  }
}

function onInputChange(e: Event) {
  const el = e.target as HTMLInputElement
  const parsed = parseTime(el.value)
  if (parsed) {
    emits("update:hours", parsed.h)
    emits("update:minutes", parsed.m)
  }
  el.value = displayValue.value
}

function selectHour(h: number) {
  emits("update:hours", h)
}

function selectMinute(m: number) {
  emits("update:minutes", m)
  isOpen.value = false
}

const rootRef = ref<HTMLElement | null>(null)
const hoursCol = ref<HTMLElement | null>(null)
const minutesCol = ref<HTMLElement | null>(null)

function scrollActiveIntoView() {
  const hActive = hoursCol.value?.querySelector("[data-active]") as HTMLElement | null
  if (hActive && hoursCol.value) {
    hoursCol.value.scrollTop = hActive.offsetTop - hoursCol.value.offsetHeight / 2 + hActive.offsetHeight / 2
  }
  const mActive = minutesCol.value?.querySelector("[data-active]") as HTMLElement | null
  if (mActive && minutesCol.value) {
    minutesCol.value.scrollTop = mActive.offsetTop - minutesCol.value.offsetHeight / 2 + mActive.offsetHeight / 2
  }
}

function onClickOutside(e: MouseEvent) {
  if (rootRef.value && !rootRef.value.contains(e.target as Node)) {
    isOpen.value = false
  }
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === "Escape") isOpen.value = false
}

watch(isOpen, async (open) => {
  if (open) {
    await nextTick()
    scrollActiveIntoView()
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
  <div ref="rootRef" class="time-picker">
    <div class="time-picker__field">
      <span class="time-picker__icon">
        <Clock/>
      </span>
      <input
        type="text"
        class="time-picker__input"
        :value="displayValue"
        :placeholder="placeholder"
        @change="onInputChange"
      >
      <button type="button" class="time-picker__toggle" @click="isOpen = !isOpen">
        <ChevronDown/>
      </button>
    </div>

    <Transition name="time-picker-panel">
      <div v-if="isOpen" class="time-picker__panel">
        <div ref="hoursCol" class="time-picker__col">
          <div class="time-picker__col-header">Time</div>
          <button
            v-for="h in hourOptions"
            :key="h"
            type="button"
            :data-active="h === hours || undefined"
            :class="['time-picker__item', h === hours && 'time-picker__item--active']"
            @click="selectHour(h)"
          >
            {{ pad(h) }}
          </button>
        </div>
        <div ref="minutesCol" class="time-picker__col">
          <div class="time-picker__col-header">Min</div>
          <button
            v-for="m in minuteOptions"
            :key="m"
            type="button"
            :data-active="m === minutes || undefined"
            :class="['time-picker__item', m === minutes && 'time-picker__item--active']"
            @click="selectMinute(m)"
          >
            {{ pad(m) }}
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.time-picker {
  position: relative;
  display: inline-block;
  width: 100%;
}

/* ── Field ── */
.time-picker__field {
  display: flex;
  align-items: center;
  height: 2.5rem;
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--input, 35 15% 85%));
  background-color: hsl(var(--card, 40 25% 98%));
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  transition: border-color 150ms, box-shadow 150ms;
}

.time-picker__field:focus-within {
  box-shadow: 0 0 0 2px hsl(var(--ring, 245 43% 52%) / 0.2);
  border-color: hsl(var(--primary, 245 43% 52%) / 0.5);
}

.time-picker__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 0.75rem;
  color: hsl(var(--muted-foreground, 24 5% 46%));
}

.time-picker__icon :deep(svg) {
  width: 1rem;
  height: 1rem;
}

.time-picker__input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  background: transparent;
  padding: 0 0.75rem;
  font-size: 0.875rem;
  font-variant-numeric: tabular-nums;
  color: inherit;
  font-family: inherit;
  outline: none;
}

.time-picker__input::placeholder {
  color: hsl(var(--muted-foreground, 24 5% 46%));
}

.time-picker__toggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 1.5rem;
  padding: 0 0.5rem;
  margin-right: 0.25rem;
  border-radius: 0.25rem;
  border: none;
  background: transparent;
  color: hsl(var(--muted-foreground, 24 5% 46%));
  cursor: pointer;
  transition: background-color 150ms, color 150ms;
}

.time-picker__toggle:hover {
  background-color: hsl(var(--accent, 250 40% 95%));
  color: hsl(var(--accent-foreground, 245 43% 42%));
}

.time-picker__toggle :deep(svg) {
  width: 1rem;
  height: 1rem;
}

/* ── Dropdown ── */
.time-picker__panel {
  position: absolute;
  z-index: 50;
  top: calc(100% + 4px);
  left: 0;
  display: flex;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border, 35 15% 90%));
  background-color: hsl(var(--card, 40 25% 98%));
  color: inherit;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
  overflow: hidden;
}

.time-picker__col {
  display: flex;
  flex-direction: column;
  max-height: 14rem;
  overflow-y: auto;
  padding: 0 0.25rem 0.25rem;
  min-width: 3rem;
  scrollbar-width: none;
}

.time-picker__col::-webkit-scrollbar {
  display: none;
}

.time-picker__col + .time-picker__col {
  border-left: 1px solid hsl(var(--border, 35 15% 90%));
}

.time-picker__col-header {
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: hsl(var(--muted-foreground, 24 5% 46%));
  text-align: center;
  padding: 0.375rem 0.25rem 0.25rem;
  position: sticky;
  top: 0;
  z-index: 1;
  background-color: hsl(var(--card, 40 25% 98%));
}

.time-picker__item {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0.25rem;
  padding: 0.1875rem 0.375rem;
  font-size: 0.8125rem;
  font-variant-numeric: tabular-nums;
  cursor: pointer;
  border: none;
  background: transparent;
  color: inherit;
  font-family: inherit;
  outline: none;
  transition: background-color 100ms;
}

.time-picker__item:hover {
  background-color: hsl(var(--accent, 250 40% 95%));
  color: hsl(var(--accent-foreground, 245 43% 42%));
}

.time-picker__item--active {
  background-color: hsl(var(--primary, 245 43% 52%));
  color: hsl(var(--primary-foreground, 0 0% 100%));
}

.time-picker__item--active:hover {
  background-color: hsl(var(--primary, 245 43% 52%) / 0.85);
  color: hsl(var(--primary-foreground, 0 0% 100%));
}

.time-picker-panel-enter-active {
  animation: tp-in 150ms ease-out;
}

.time-picker-panel-leave-active {
  animation: tp-out 100ms ease-in;
}

@keyframes tp-in {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(-4px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

@keyframes tp-out {
  from {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
  to {
    opacity: 0;
    transform: scale(0.95) translateY(-4px);
  }
}
</style>

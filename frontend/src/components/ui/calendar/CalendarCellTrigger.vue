<script setup lang="ts">
import { inject, computed } from "vue"
import type { DateValue } from "@internationalized/date"

const props = defineProps<{
  day: DateValue
  month: DateValue
}>()

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const ctx = inject("calendar") as any

const selected = computed(() => ctx.isSelected(props.day))
const isToday = computed(() => ctx.isToday(props.day))
const outsideView = computed(() => ctx.isOutsideView(props.day, props.month))
const disabled = computed(() => ctx.isDisabled(props.day))

function onClick() {
  if (disabled.value) return
  ctx.selectDate(props.day)
}
</script>

<template>
  <button
    type="button"
    class="calendar-cell-trigger"
    role="button"
    :data-selected="selected || undefined"
    :data-today="isToday || undefined"
    :data-outside-view="outsideView || undefined"
    :data-disabled="disabled || undefined"
    :disabled="disabled"
    :tabindex="selected || isToday ? 0 : -1"
    @click="onClick"
  >
    <slot>{{ day.day }}</slot>
  </button>
</template>

<style scoped>
.calendar-cell-trigger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 0.375rem;
  border: none;
  background: transparent;
  font-size: 0.875rem;
  font-weight: 400;
  cursor: pointer;
  color: inherit;
  font-family: inherit;
  padding: 0;
  outline: none;
  transition: background-color 100ms, color 100ms;
}

.calendar-cell-trigger:hover:not([data-disabled]) {
  background-color: hsl(var(--accent, 250 40% 95%));
  color: hsl(var(--accent-foreground, 245 43% 42%));
}

.calendar-cell-trigger:focus-visible {
  outline: 2px solid hsl(var(--ring, 245 43% 52%));
  outline-offset: -2px;
}

/* Today */
.calendar-cell-trigger[data-today]:not([data-selected]) {
  background-color: hsl(var(--accent, 250 40% 95%));
  color: hsl(var(--accent-foreground, 245 43% 42%));
}

/* Selected */
.calendar-cell-trigger[data-selected] {
  background-color: hsl(var(--primary, 245 43% 52%));
  color: hsl(var(--primary-foreground, 0 0% 100%));
}
.calendar-cell-trigger[data-selected]:hover {
  background-color: hsl(var(--primary, 245 43% 52%));
  color: hsl(var(--primary-foreground, 0 0% 100%));
}

/* Disabled */
.calendar-cell-trigger[data-disabled] {
  color: hsl(var(--muted-foreground, 24 5% 46%));
  opacity: 0.5;
  pointer-events: none;
}

/* Outside current month */
.calendar-cell-trigger[data-outside-view] {
  color: hsl(var(--muted-foreground, 24 5% 46%));
  opacity: 0.5;
}
.calendar-cell-trigger[data-outside-view][data-selected] {
  background-color: hsl(var(--accent, 250 40% 95%));
  opacity: 0.3;
}
</style>

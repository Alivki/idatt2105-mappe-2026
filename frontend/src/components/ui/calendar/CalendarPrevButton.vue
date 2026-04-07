<script setup lang="ts">
import { inject } from "vue"
import { ChevronLeft } from "lucide-vue-next"

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const { prevMonth, disabled } = inject("calendar") as any
</script>

<template>
  <button
    type="button"
    class="calendar-nav-btn"
    aria-label="Previous month"
    :disabled="disabled"
    @click="prevMonth"
  >
    <slot>
      <ChevronLeft class="calendar-nav-btn__icon" />
    </slot>
  </button>
</template>

<style scoped>
.calendar-nav-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.75rem;
  height: 1.75rem;
  border-radius: 0.375rem;
  border: 1px solid hsl(var(--border, 35 15% 90%));
  background: transparent;
  cursor: pointer;
  opacity: 0.5;
  transition: opacity 150ms, background-color 150ms;
  color: inherit;
  padding: 0;
  font-family: inherit;
}
.calendar-nav-btn:hover {
  opacity: 1;
  background-color: hsl(var(--accent, 250 40% 95%));
}
.calendar-nav-btn:disabled {
  pointer-events: none;
  opacity: 0.3;
}
.calendar-nav-btn__icon {
  width: 1rem;
  height: 1rem;
}
</style>

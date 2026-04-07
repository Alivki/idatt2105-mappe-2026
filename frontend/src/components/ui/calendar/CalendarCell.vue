<script setup lang="ts">
import type { DateValue } from "@internationalized/date"

defineProps<{
  date: DateValue
}>()
</script>

<template>
  <td class="calendar-cell">
    <slot />
  </td>
</template>

<style scoped>
.calendar-cell {
  position: relative;
  padding: 0;
  text-align: center;
  font-size: 0.875rem;
}
</style>

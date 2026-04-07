<script setup lang="ts">
</script>

<template>
  <tr class="calendar-grid-row">
    <slot />
  </tr>
</template>

<style scoped>
.calendar-grid-row {
  display: flex;
  width: 100%;
}
</style>

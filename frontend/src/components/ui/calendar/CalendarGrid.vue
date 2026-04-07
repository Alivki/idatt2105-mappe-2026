<script setup lang="ts">
</script>

<template>
  <table class="calendar-grid" role="grid">
    <slot />
  </table>
</template>

<style scoped>
.calendar-grid {
  width: 100%;
  border-collapse: collapse;
}
</style>

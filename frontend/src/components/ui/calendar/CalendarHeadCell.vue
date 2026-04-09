<script setup lang="ts">
</script>

<template>
  <th class="calendar-head-cell">
    <slot/>
  </th>
</template>

<style scoped>
.calendar-head-cell {
  width: 2rem;
  border-radius: 0.375rem;
  font-size: 0.8rem;
  font-weight: 400;
  text-align: center;
  color: hsl(var(--muted-foreground, 24 5% 46%));
}
</style>

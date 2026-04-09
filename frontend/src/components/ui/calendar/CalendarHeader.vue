<script setup lang="ts">
</script>

<template>
  <div class="calendar-header">
    <slot/>
  </div>
</template>

<style scoped>
.calendar-header {
  position: relative;
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  padding-top: 0.25rem;
}
</style>

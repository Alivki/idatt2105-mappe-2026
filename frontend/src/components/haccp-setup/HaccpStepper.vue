<script setup lang="ts">
defineProps<{
  currentStep: number
  totalSteps: number
}>()
</script>

<template>
  <div class="stepper">
    <div class="stepper-bars">
      <div
        v-for="step in totalSteps"
        :key="step"
        class="stepper-bar"
        :class="{
          'stepper-bar--active': step === currentStep,
          'stepper-bar--completed': step < currentStep,
        }"
      />
    </div>
    <span class="stepper-label">Trinn {{ currentStep }} av {{ totalSteps }}</span>
  </div>
</template>

<style scoped>
.stepper {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stepper-bars {
  display: flex;
  flex: 1;
  gap: 0.375rem;
}

.stepper-bar {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background-color: hsl(var(--muted) / 0.6);
  transition: background-color 0.3s ease;
}

.stepper-bar--active,
.stepper-bar--completed {
  background-color: hsl(var(--primary));
}

.stepper-label {
  flex-shrink: 0;
  font-size: 0.8125rem;
  color: hsl(var(--muted-foreground));
  white-space: nowrap;
}
</style>

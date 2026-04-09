<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(defineProps<{
  icon?: Component
  variant?: 'default' | 'green' | 'danger'
  title: string
  description?: string
}>(), {
  variant: 'default',
})
</script>

<template>
  <div class="empty-state">
    <div class="empty-state-inner">
      <div v-if="icon" :class="['empty-state-icon', `empty-state-icon--${variant}`]">
        <component :is="icon" :stroke-width="1.5" aria-hidden="true" />
      </div>
      <div class="empty-state-text">
        <h2>{{ title }}</h2>
        <p v-if="description">{{ description }}</p>
      </div>
      <div v-if="$slots.actions" class="empty-state-actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  position: relative;
  display: flex;
  min-height: 260px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  border: 2px dashed hsl(var(--muted-foreground) / 0.2);
  background: hsl(var(--muted) / 0.3);
  padding: 2rem;
}

.empty-state-inner {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  text-align: center;
}

.empty-state-icon {
  display: flex;
  height: 4rem;
  width: 4rem;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  background-color: hsl(var(--muted));
}

.empty-state-icon :deep(svg) {
  width: 2rem;
  height: 2rem;
  color: hsl(var(--muted-foreground));
}

.empty-state-icon--green {
  background-color: var(--green-soft);
  box-shadow: 0 0 0 4px var(--green-soft);
}

.empty-state-icon--green :deep(svg) {
  color: var(--green);
}

.empty-state-icon--danger {
  background-color: hsl(var(--destructive) / 0.1);
  box-shadow: 0 0 0 4px hsl(var(--destructive) / 0.05);
}

.empty-state-icon--danger :deep(svg) {
  color: hsl(var(--destructive) / 0.7);
}

.empty-state-text h2 {
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.empty-state-text p {
  max-width: 28rem;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  margin-top: 0.25rem;
}

.empty-state-actions {
  display: flex;
  gap: 0.5rem;
}

@media (max-width: 760px) {
  .empty-state-actions {
    width: 100%;
    flex-direction: column;
  }

  .empty-state-actions > :deep(*) {
    width: 100%;
  }
}
</style>

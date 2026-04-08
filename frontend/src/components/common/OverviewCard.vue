<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(defineProps<{
  label: string
  value: string | number
  variant?: 'neutral' | 'open' | 'in-progress' | 'resolved'
  subLabel?: string
  valueClass?: string
  icon?: Component
}>(), {
  variant: 'neutral',
  valueClass: '',
})
</script>

<template>
  <div :class="['overview-card', `overview-card--${variant}`]">
    <div class="card-header">
      <component :is="icon" v-if="icon" :size="16" class="card-icon" aria-hidden="true" />
      <p class="card-label">{{ label }}</p>
    </div>
    <p class="card-value" :class="valueClass">{{ value }}</p>
    <slot />
    <p v-if="subLabel" class="card-sub">{{ subLabel }}</p>
  </div>
</template>

<style scoped>
.overview-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-radius: var(--radius-lg);
  padding: 16px;
  border: 2px solid hsl(var(--border));
  background: hsl(var(--card));
  min-height: 6.25rem;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-icon {
  flex-shrink: 0;
  opacity: 0.6;
}

.card-label {
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  margin: 0;
  font-weight: 500;
}

.card-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
  color: hsl(var(--foreground));
  margin: 0;
}

.card-sub {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  margin: 4px 0 0;
}

/* Variants */

.overview-card--open {
  background: var(--red-soft);
  border-color: color-mix(in srgb, var(--red-soft) 50%, var(--red) 20%);
}

.overview-card--open .card-value {
  color: var(--red);
}

.overview-card--open .card-icon {
  color: var(--red);
}

.overview-card--in-progress {
  background: var(--amber-soft);
  border-color: color-mix(in srgb, var(--amber-soft) 50%, var(--amber) 20%);
}

.overview-card--in-progress .card-value {
  color: var(--amber);
}

.overview-card--in-progress .card-icon {
  color: var(--amber);
}

.overview-card--resolved {
  background: var(--green-soft);
  border-color: color-mix(in srgb, var(--green-soft) 50%, var(--green) 20%);
}

.overview-card--resolved .card-value {
  color: var(--green);
}

.overview-card--resolved .card-icon {
  color: var(--green);
}

.overview-card--neutral .card-icon {
  color: hsl(var(--muted-foreground));
}

/* Support for color classes from props */
:deep(.val-green), .val-green { color: var(--green); }
:deep(.val-amber), .val-amber { color: var(--amber); }
:deep(.val-red), .val-red { color: var(--red); }
</style>

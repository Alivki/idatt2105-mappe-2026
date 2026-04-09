<script setup lang="ts">
import {computed} from 'vue'

type BadgeVariant = 'default' | 'secondary' | 'destructive' | 'outline'
type BadgeTone = 'neutral' | 'ok' | 'warning' | 'danger' | 'brand'

const props = defineProps<{
  variant?: BadgeVariant
  tone?: BadgeTone
  class?: string
}>()

const classes = computed(() => {
  const parts = ['badge']
  if (props.tone) {
    parts.push(`badge--tone-${props.tone}`)
  } else {
    parts.push(`badge--${props.variant ?? 'default'}`)
  }
  if (props.class) parts.push(props.class)
  return parts.join(' ')
})
</script>

<template>
  <div :class="classes">
    <slot/>
  </div>
</template>

<style scoped>
.badge {
  display: inline-flex;
  gap: 0.25rem;
  align-items: center;
  border-radius: 999px;
  border: 1px solid transparent;
  padding: 0.25rem 0.75rem;
  font-size: 0.8rem;
  line-height: 1;
  font-weight: 600;
  transition: colors 150ms ease;
  outline: none;
}

.badge:focus {
  outline: none;
  box-shadow: 0 0 0 2px hsl(var(--ring, 245 43% 52%)), 0 0 0 4px hsl(var(--ring, 245 43% 52%) / 0.2);
}

/* Variants — use :where() for low specificity so utility classes can override */

:where(.badge--default) {
  border-color: hsl(var(--primary, 245 43% 52%) / 0.3);
  background-color: hsl(var(--primary, 245 43% 52%));
  color: hsl(var(--primary-foreground, 0 0% 100%));
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

:where(.badge--default):hover {
  background-color: hsl(var(--primary, 245 43% 52%) / 0.8);
}

:where(.badge--secondary) {
  border-color: hsl(var(--secondary-foreground, 24 10% 20%) / 0.15);
  background-color: hsl(var(--secondary, 40 20% 93%));
  color: hsl(var(--secondary-foreground, 24 10% 20%));
}

:where(.badge--secondary):hover {
  background-color: hsl(var(--secondary, 40 20% 93%) / 0.8);
}

:where(.badge--destructive) {
  border-color: hsl(var(--destructive, 0 55% 42%) / 0.3);
  background-color: hsl(var(--destructive, 0 55% 42%));
  color: hsl(var(--destructive-foreground, 0 0% 100%));
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
}

:where(.badge--destructive):hover {
  background-color: hsl(var(--destructive, 0 55% 42%) / 0.8);
}

:where(.badge--outline) {
  border-color: currentColor;
  color: hsl(var(--foreground, 24 10% 10%));
  background-color: transparent;
}

:where(.badge--tone-neutral) {
  background-color: #e9e9e8;
  border-color: #d6d6d3;
  color: #464b52;
}

:where(.badge--tone-ok) {
  background-color: var(--green-soft);
  border-color: color-mix(in srgb, var(--green-soft) 68%, #2f6f34 32%);
  color: #2f6f34;
}

:where(.badge--tone-warning) {
  background-color: var(--amber-soft);
  border-color: color-mix(in srgb, var(--amber-soft) 68%, #8e5713 32%);
  color: #8e5713;
}

:where(.badge--tone-danger) {
  background-color: var(--red-soft);
  border-color: color-mix(in srgb, var(--red-soft) 68%, #902324 32%);
  color: #902324;
}

:where(.badge--tone-brand) {
  background-color: var(--brand-soft);
  border-color: color-mix(in srgb, var(--brand-soft) 68%, #413da9 32%);
  color: #413da9;
}
</style>

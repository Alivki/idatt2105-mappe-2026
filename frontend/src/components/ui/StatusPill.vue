<script setup lang="ts">
import Badge from './badge/Badge.vue'

withDefaults(
  defineProps<{
    label: string
    tone?: 'neutral' | 'ok' | 'danger' | 'warning' | 'brand'
  }>(),
  {
    tone: 'neutral',
  },
)
</script>

<template>
  <Badge :tone="tone">{{ label }}</Badge>
</template>

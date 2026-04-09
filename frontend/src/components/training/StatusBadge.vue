<script setup lang="ts">
import {computed} from 'vue'
import Badge from '@/components/ui/badge/Badge.vue'

const props = defineProps<{ status: string }>()

const tone = computed(() => {
  if (props.status === 'Gyldig') return 'ok'
  if (props.status === 'Utløper snart') return 'warning'
  if (props.status === 'Mangler' || props.status === 'Utgått') return 'danger'
  return 'neutral'
})
</script>

<template>
  <Badge :tone="tone">{{ status }}</Badge>
</template>

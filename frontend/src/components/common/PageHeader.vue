<script setup lang="ts">
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'

defineProps<{
  title: string
}>()
</script>

<template>
  <header class="page-header">
    <div class="page-header-inner">
      <SidebarTrigger/>
      <Separator orientation="vertical" class="header-separator"/>
      <span class="page-title">{{ title }}</span>
    </div>
  </header>
</template>

<style scoped>
.page-header {
  display: flex;
  height: 4rem;
  flex-shrink: 0;
  align-items: center;
}

.page-header-inner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0 1rem;
}

.header-separator {
  height: 1rem !important;
  width: 1px !important;
  margin-right: 0.5rem;
}

.page-title {
  font-weight: 500;
  color: hsl(var(--sidebar-primary, 245 43% 52%));
}

@media (max-width: 760px) {
  .page-header-inner {
    width: 100%;
    padding: 0 0.75rem;
  }
}
</style>

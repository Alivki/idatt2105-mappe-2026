<script setup lang="ts">
import {Calendar, Clock} from 'lucide-vue-next'
import Badge from '@/components/ui/badge/Badge.vue'

defineProps<{
  reportedByName: string | null
  reportedAt: string | null
  updatedAt: string | null
  statusLabel: string
  statusTone: 'danger' | 'warning' | 'ok' | 'neutral' | 'brand'
  fields: { label: string; value: string }[]
}>()

function formatDate(value: string | null): string {
  if (!value) return '-'
  return new Date(value).toLocaleString('nb-NO', {
    day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

function getInitials(name: string | null): string {
  if (!name) return '?'
  const parts = name.split(' ').filter(Boolean)
  if (parts.length >= 2) return ((parts[0]?.[0] ?? '') + (parts[parts.length - 1]?.[0] ?? '')).toUpperCase()
  return parts[0]?.[0]?.toUpperCase() ?? '?'
}
</script>

<template>
  <aside class="detail-sidebar">
    <div class="sidebar-card">
      <h3 class="sidebar-title">Detaljer</h3>
      <div class="sidebar-field">
        <span class="sidebar-label">Rapportert av</span>
        <div class="person-row">
          <div class="avatar">{{ getInitials(reportedByName) }}</div>
          <span class="person-name">{{ reportedByName }}</span>
        </div>
      </div>
      <div class="sidebar-field">
        <span class="sidebar-label">Rapportert</span>
        <div class="date-row">
          <Calendar :size="14" class="date-icon"/>
          <span>{{ formatDate(reportedAt) }}</span></div>
      </div>
      <div class="sidebar-field">
        <span class="sidebar-label">Sist oppdatert</span>
        <div class="date-row">
          <Clock :size="14" class="date-icon"/>
          <span>{{ formatDate(updatedAt) }}</span></div>
      </div>
      <div class="sidebar-field">
        <span class="sidebar-label">Status</span>
        <Badge :tone="statusTone">{{ statusLabel }}</Badge>
      </div>
      <div v-for="field in fields" :key="field.label" class="sidebar-field">
        <span class="sidebar-label">{{ field.label }}</span>
        <span class="sidebar-value">{{ field.value }}</span>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.detail-sidebar {
  position: sticky;
  top: 1rem;
}

.sidebar-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--card-bg);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sidebar-title {
  margin: 0;
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.sidebar-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-label {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--text-secondary);
}

.sidebar-value {
  font-size: 0.85rem;
  font-weight: 500;
  color: hsl(var(--foreground));
}

.person-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 0.5rem;
  background-color: hsl(var(--primary));
  color: hsl(var(--primary-foreground));
  font-weight: 600;
  font-size: 0.7rem;
  flex-shrink: 0;
}

.person-name {
  font-size: 0.85rem;
  font-weight: 500;
}

.date-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.82rem;
  color: hsl(var(--foreground));
}

.date-icon {
  color: var(--text-secondary);
  flex-shrink: 0;
}

@media (max-width: 860px) {
  .detail-sidebar {
    position: static;
  }
}
</style>

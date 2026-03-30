<script setup lang="ts">
import { computed } from 'vue'
import { ChevronDown, Pencil } from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'
import StatusPill from '@/components/ui/StatusPill.vue'
import Collapsible from '@/components/ui/collapsible/Collapsible.vue'
import CollapsibleContent from '@/components/ui/collapsible/CollapsibleContent.vue'
import CollapsibleTrigger from '@/components/ui/collapsible/CollapsibleTrigger.vue'
import type { Deviation, DeviationModule, DeviationSeverity, DeviationStatus } from '@/types/deviation'

const props = defineProps<{
  deviation: Deviation
  canManage: boolean
}>()

const emits = defineEmits<{
  (e: 'edit', deviation: Deviation): void
}>()

const moduleLabel: Record<DeviationModule, string> = {
  IK_MAT: 'IK-Mat',
  IK_ALKOHOL: 'IK-Alkohol',
}

const statusLabel: Record<DeviationStatus, string> = {
  OPEN: 'Åpen',
  IN_PROGRESS: 'Under behandling',
  RESOLVED: 'Løst',
  CLOSED: 'Lukket',
}

const severityLabel: Record<DeviationSeverity, string> = {
  LOW: 'Lav',
  MEDIUM: 'Middels',
  HIGH: 'Høy',
  CRITICAL: 'Kritisk',
}

const severityTone: Record<DeviationSeverity, 'neutral' | 'ok' | 'danger' | 'warning' | 'brand'> = {
  LOW: 'ok',
  MEDIUM: 'warning',
  HIGH: 'warning',
  CRITICAL: 'danger',
}

const statusTone: Record<DeviationStatus, 'neutral' | 'ok' | 'danger' | 'warning' | 'brand'> = {
  OPEN: 'danger',
  IN_PROGRESS: 'warning',
  RESOLVED: 'ok',
  CLOSED: 'neutral',
}

const severityRailClass: Record<DeviationSeverity, string> = {
  LOW: 'deviation-card--low',
  MEDIUM: 'deviation-card--medium',
  HIGH: 'deviation-card--high',
  CRITICAL: 'deviation-card--critical',
}

const relativeTime = computed(() => toRelativeTime(props.deviation.reportedAt))

function toRelativeTime(value: string): string {
  const timestamp = new Date(value).getTime()
  if (Number.isNaN(timestamp)) {
    return '-'
  }

  const diffMs = Date.now() - timestamp
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diffMs < hour) {
    const minutes = Math.max(1, Math.floor(diffMs / minute))
    return `${minutes} min siden`
  }

  if (diffMs < day) {
    const hours = Math.floor(diffMs / hour)
    return `${hours} time${hours > 1 ? 'r' : ''} siden`
  }

  const days = Math.floor(diffMs / day)
  return `${days} dag${days > 1 ? 'er' : ''} siden`
}
</script>

<template>
  <Collapsible :default-open="false" :class="`deviation-card ${severityRailClass[deviation.severity]}`">
    <div class="deviation-card-header">
      <div class="header-main">
        <div class="tag-row">
          <StatusPill :label="severityLabel[deviation.severity]" :tone="severityTone[deviation.severity]" />
          <StatusPill :label="moduleLabel[deviation.module]" tone="brand" />
          <StatusPill :label="statusLabel[deviation.status]" :tone="statusTone[deviation.status]" />
        </div>

        <h3>{{ deviation.title }}</h3>
        <p class="description">{{ deviation.description }}</p>

        <div class="meta-line">
          <span>Rapportert av: {{ deviation.reportedByUserName }}</span>
          <span>Tildelt: {{ deviation.assignedToUserName ?? 'Ikke tildelt' }}</span>
        </div>
      </div>

      <div class="header-side">
        <span class="time-label">{{ relativeTime }}</span>
        <CollapsibleTrigger as-child>
          <Button variant="outline" size="icon-sm" class="expand-btn" aria-label="Vis detaljer">
            <ChevronDown />
          </Button>
        </CollapsibleTrigger>
      </div>
    </div>

    <CollapsibleContent>
      <div class="details-wrap">
        <div class="details-grid">
          <div>
            <h4>Full beskrivelse</h4>
            <p>{{ deviation.description }}</p>
          </div>

          <div>
            <h4>Umiddelbar korrigerende handling</h4>
            <p>{{ deviation.immediateAction ?? 'Ikke angitt' }}</p>
          </div>
        </div>

        <div class="details-grid details-grid--meta">
          <div>
            <h4>Opprettet</h4>
            <p>{{ new Date(deviation.createdAt).toLocaleString('nb-NO') }}</p>
          </div>
          <div>
            <h4>Sist oppdatert</h4>
            <p>{{ new Date(deviation.updatedAt).toLocaleString('nb-NO') }}</p>
          </div>
        </div>

        <div v-if="canManage" class="details-actions">
          <Button variant="secondary" @click="emits('edit', deviation)">
            <Pencil />
            Rediger avvik
          </Button>
        </div>
      </div>
    </CollapsibleContent>
  </Collapsible>
</template>

<style scoped>
.deviation-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  padding: 14px 14px 12px;
  border-left-width: 4px;
}

.deviation-card--critical {
  border-left-color: var(--red);
}

.deviation-card--high,
.deviation-card--medium {
  border-left-color: var(--amber);
}

.deviation-card--low {
  border-left-color: var(--green);
}

.deviation-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.header-main {
  min-width: 0;
  flex: 1;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

h3 {
  margin: 10px 0 6px;
  font-size: 1.65rem;
  letter-spacing: -0.02em;
}

.description {
  color: var(--text-primary);
  margin: 0;
  font-size: 1.14rem;
  line-height: 1.35;
}

.meta-line {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 1rem;
}

.header-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.time-label {
  color: var(--text-secondary);
  font-size: 1.05rem;
  text-align: right;
}

.expand-btn :deep(svg) {
  transition: transform 160ms ease;
}

[data-state='open'] .expand-btn :deep(svg) {
  transform: rotate(180deg);
}

.details-wrap {
  margin-top: 12px;
  border-top: 1px solid #e4e4df;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.details-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.details-grid--meta {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

h4 {
  margin: 0 0 4px;
  font-size: 0.96rem;
  color: var(--text-secondary);
}

.details-grid p {
  margin: 0;
  font-size: 1.02rem;
}

.details-actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 860px) {
  .deviation-card-header {
    flex-direction: column;
  }

  .header-side {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .details-grid,
  .details-grid--meta {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup lang="ts">
import type { PrerequisiteStatus, WizardState } from '@/types/haccp-setup'
import { prerequisiteStatusLabels } from '@/types/haccp-setup'
import { CircleCheck, AlertTriangle, CircleX, Info, ClipboardCheck } from 'lucide-vue-next'
import { computed } from 'vue'

const { wizard } = defineProps<{ wizard: WizardState }>()

const statusOptions: { value: PrerequisiteStatus; label: string; icon: typeof CircleCheck }[] = [
  { value: 'OK', label: prerequisiteStatusLabels.OK, icon: CircleCheck },
  { value: 'NEEDS_WORK', label: prerequisiteStatusLabels.NEEDS_WORK, icon: AlertTriangle },
  { value: 'MISSING', label: prerequisiteStatusLabels.MISSING, icon: CircleX },
]

function setStatus(prereqId: string, status: PrerequisiteStatus) {
  const prereq = wizard.prerequisites.find((p) => p.id === prereqId)
  if (prereq) prereq.status = status
}

const summary = computed(() => {
  const ok = wizard.prerequisites.filter((p) => p.status === 'OK').length
  const work = wizard.prerequisites.filter((p) => p.status === 'NEEDS_WORK').length
  const missing = wizard.prerequisites.filter((p) => p.status === 'MISSING').length
  return { ok, work, missing, total: wizard.prerequisites.length }
})
</script>

<template>
  <div class="step-content">
    <div class="info-banner">
      <Info :size="16" :stroke-width="1.5" />
      <div>
        <p>
          Grunnforutsetninger er basiskravene fra Mattilsynet som må være på plass for et fungerende IK-mat-system.
          <strong>Vi genererer sjekklister for hvert av disse områdene</strong> — gå gjennom listen og angi status
          slik at vi kan tilpasse sjekklistene til din virksomhet.
        </p>
      </div>
    </div>

    <div class="checklist-note">
      <ClipboardCheck :size="15" :stroke-width="1.5" />
      <span>Sjekklister vil bli opprettet for disse områdene basert på dine svar.</span>
    </div>

    <div class="summary-bar">
      <div class="summary-item summary-item--ok">
        <CircleCheck :size="14" :stroke-width="2" />
        {{ summary.ok }} på plass
      </div>
      <div class="summary-item summary-item--work">
        <AlertTriangle :size="14" :stroke-width="2" />
        {{ summary.work }} under arbeid
      </div>
      <div class="summary-item summary-item--missing">
        <CircleX :size="14" :stroke-width="2" />
        {{ summary.missing }} mangler
      </div>
    </div>

    <div class="prereq-list">
      <div
        v-for="prereq in wizard.prerequisites"
        :key="prereq.id"
        class="prereq-card"
        :class="{
          'prereq-card--ok': prereq.status === 'OK',
          'prereq-card--work': prereq.status === 'NEEDS_WORK',
          'prereq-card--missing': prereq.status === 'MISSING',
        }"
      >
        <div class="prereq-main">
          <div class="prereq-info">
            <span class="prereq-name">{{ prereq.name }}</span>
            <p class="prereq-desc">{{ prereq.description }}</p>
          </div>
          <div class="prereq-status-row">
            <button
              v-for="opt in statusOptions"
              :key="opt.value"
              class="status-btn"
              :class="{
                'status-btn--ok': prereq.status === opt.value && opt.value === 'OK',
                'status-btn--work': prereq.status === opt.value && opt.value === 'NEEDS_WORK',
                'status-btn--missing': prereq.status === opt.value && opt.value === 'MISSING',
              }"
              @click="setStatus(prereq.id, opt.value)"
            >
              <component :is="opt.icon" :size="12" :stroke-width="2" />
              {{ opt.label }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.step-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.info-banner {
  display: flex;
  gap: 0.625rem;
  padding: 0.875rem 1rem;
  background: hsl(var(--primary) / 0.04);
  border: 1px solid hsl(var(--primary) / 0.12);
  border-radius: 0.5rem;
  font-size: 0.8125rem;
  color: hsl(var(--foreground));
  line-height: 1.5;
}

.info-banner svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
  color: hsl(var(--primary));
}

.info-banner p {
  margin: 0;
}

.checklist-note {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.8125rem;
  font-weight: 500;
  color: hsl(var(--primary));
}

.summary-bar {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  font-weight: 500;
}

.summary-item--ok { color: #16a34a; }
.summary-item--work { color: #d97706; }
.summary-item--missing { color: #dc2626; }

.prereq-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.prereq-card {
  padding: 0.875rem 1rem;
  border: 1px solid hsl(var(--border));
  border-left: 3px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: white;
  transition: border-color 0.15s ease;
}

.prereq-card--ok { border-left-color: #16a34a; }
.prereq-card--work { border-left-color: #d97706; }
.prereq-card--missing { border-left-color: #dc2626; }

.prereq-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.prereq-info {
  flex: 1;
  min-width: 0;
}

.prereq-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.prereq-desc {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.4;
  margin-top: 0.25rem;
}

.prereq-status-row {
  display: flex;
  gap: 0.25rem;
  flex-shrink: 0;
}

.status-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.625rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: 999px;
  background: white;
  cursor: pointer;
  font-size: 0.6875rem;
  font-weight: 500;
  color: hsl(var(--muted-foreground));
  transition: all 0.15s ease;
  white-space: nowrap;
}

.status-btn:hover {
  border-color: hsl(var(--primary) / 0.3);
}

.status-btn--ok {
  background: #f0fdf4;
  border-color: #bbf7d0;
  color: #16a34a;
  font-weight: 600;
}

.status-btn--work {
  background: #fffbeb;
  border-color: #fde68a;
  color: #d97706;
  font-weight: 600;
}

.status-btn--missing {
  background: #fef2f2;
  border-color: #fecaca;
  color: #dc2626;
  font-weight: 600;
}
</style>

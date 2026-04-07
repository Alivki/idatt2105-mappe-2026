<script setup lang="ts">
import type { WizardState } from '@/types/haccp-setup'
import { hazardTypeLabels, hazardTypeColors } from '@/types/haccp-setup'
import { Info, ShieldAlert } from 'lucide-vue-next'
import { computed } from 'vue'
import Select from '@/components/ui/select/Select.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import Textarea from '@/components/ui/textarea/Textarea.vue'

const wizard = defineModel<WizardState>('wizard', { required: true })

function getRiskLevel(likelihood: number, severity: number) {
  const risk = likelihood * severity
  if (risk >= 6) return { label: 'Høy', color: '#dc2626', bg: '#fef2f2' }
  if (risk >= 3) return { label: 'Middels', color: '#d97706', bg: '#fffbeb' }
  return { label: 'Lav', color: '#16a34a', bg: '#f0fdf4' }
}

function isKKP(likelihood: number, severity: number): boolean {
  return likelihood * severity >= 6
}

const kkpCount = computed(() =>
  wizard.value!.hazardEntries.filter((e) => isKKP(e.likelihood, e.severity)).length,
)

function updateLikelihood(entryId: string, val: string) {
  const entry = wizard.value!.hazardEntries.find((e) => e.id === entryId)
  if (entry) entry.likelihood = Number(val) as 1 | 2 | 3
}

function updateSeverity(entryId: string, val: string) {
  const entry = wizard.value!.hazardEntries.find((e) => e.id === entryId)
  if (entry) entry.severity = Number(val) as 1 | 2 | 3
}
</script>

<template>
  <div class="step-content">
    <div class="info-banner">
      <Info :size="16" :stroke-width="1.5" />
      <p>
        Vurder sannsynlighet og alvorlighet for hver fare. Farer med risiko 6–9 foreslås som KKP.
        Legg til forebyggende tiltak.
      </p>
    </div>

    <div v-if="kkpCount > 0" class="kkp-summary">
      <ShieldAlert :size="16" :stroke-width="1.5" />
      <span><strong>{{ kkpCount }}</strong> fare{{ kkpCount !== 1 ? 'r' : '' }} identifisert som KKP</span>
    </div>

    <div class="entries">
      <div
        v-for="entry in wizard.hazardEntries"
        :key="entry.id"
        class="entry-card"
        :class="{ 'entry-card--kkp': isKKP(entry.likelihood, entry.severity) }"
      >
        <div class="entry-header">
          <span class="entry-step">{{ entry.processStepName }}</span>
          <span
            class="hazard-badge"
            :style="{
              color: hazardTypeColors[entry.hazardType].text,
              borderColor: hazardTypeColors[entry.hazardType].border,
              background: hazardTypeColors[entry.hazardType].bg,
            }"
          >
            {{ hazardTypeLabels[entry.hazardType] }}
          </span>
          <div class="entry-risk-badge" :style="{ color: getRiskLevel(entry.likelihood, entry.severity).color, background: getRiskLevel(entry.likelihood, entry.severity).bg }">
            {{ entry.likelihood * entry.severity }}
            <span class="risk-text">{{ getRiskLevel(entry.likelihood, entry.severity).label }}</span>
          </div>
          <span v-if="isKKP(entry.likelihood, entry.severity)" class="kkp-badge">KKP</span>
        </div>

        <div class="entry-body">
          <div class="entry-field">
            <label>Farebeskrivelse</label>
            <Textarea
              :model-value="entry.hazardDescription"
              placeholder="Beskriv faren..."
              class="entry-textarea"
              @update:model-value="entry.hazardDescription = String($event)"
            />
          </div>

          <div class="entry-row">
            <div class="entry-field entry-field--half">
              <label>Sannsynlighet</label>
              <Select :model-value="String(entry.likelihood)" @update:model-value="updateLikelihood(entry.id, $event)">
                <SelectTrigger class="entry-select">
                  <SelectValue placeholder="Velg..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">1 – Lav</SelectItem>
                  <SelectItem value="2">2 – Middels</SelectItem>
                  <SelectItem value="3">3 – Høy</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div class="entry-field entry-field--half">
              <label>Alvorlighet</label>
              <Select :model-value="String(entry.severity)" @update:model-value="updateSeverity(entry.id, $event)">
                <SelectTrigger class="entry-select">
                  <SelectValue placeholder="Velg..." />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">1 – Lav</SelectItem>
                  <SelectItem value="2">2 – Middels</SelectItem>
                  <SelectItem value="3">3 – Høy</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div class="entry-field">
            <label>Forebyggende tiltak</label>
            <Textarea
              :model-value="entry.preventiveMeasure"
              placeholder="Beskriv tiltak..."
              class="entry-textarea"
              @update:model-value="entry.preventiveMeasure = String($event)"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="risk-legend">
      <span class="legend-title">Risikoskala (sannsynlighet x alvorlighet):</span>
      <span class="legend-item" style="color: #16a34a;">1–2 Lav</span>
      <span class="legend-item" style="color: #d97706;">3–4 Middels</span>
      <span class="legend-item" style="color: #dc2626;">6–9 Høy (KKP)</span>
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
  padding: 0.75rem 1rem;
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

.info-banner p { margin: 0; }

.kkp-summary {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.875rem;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 0.5rem;
  font-size: 0.8125rem;
  color: #92400e;
}

.entries {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.entry-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.625rem;
  background: white;
  overflow: hidden;
}

.entry-card--kkp {
  border-color: #fde68a;
}

.entry-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1rem;
  background: hsl(var(--muted) / 0.2);
  border-bottom: 1px solid hsl(var(--border));
}

.entry-card--kkp .entry-header {
  background: #fffdf5;
}

.entry-step {
  font-size: 0.8125rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.hazard-badge {
  display: inline-block;
  padding: 0.0625rem 0.5rem;
  border: 1px solid;
  border-radius: 999px;
  font-size: 0.625rem;
  font-weight: 600;
}

.entry-risk-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.0625rem 0.5rem;
  border-radius: 0.25rem;
  font-size: 0.75rem;
  font-weight: 700;
  margin-left: auto;
}

.risk-text {
  font-size: 0.625rem;
  font-weight: 600;
  text-transform: uppercase;
}

.kkp-badge {
  padding: 0.0625rem 0.375rem;
  background: #fef3c7;
  border: 1px solid #fde68a;
  border-radius: 999px;
  font-size: 0.625rem;
  font-weight: 700;
  color: #d97706;
}

.entry-body {
  padding: 0.75rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
}

.entry-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.entry-field label {
  font-size: 0.6875rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.entry-field--half {
  flex: 1;
  min-width: 0;
}

.entry-row {
  display: flex;
  gap: 0.625rem;
}

.entry-textarea {
  min-height: 2.5rem !important;
  font-size: 0.8125rem !important;
  padding: 0.375rem 0.625rem !important;
}

.entry-select {
  height: 2.25rem !important;
  font-size: 0.8125rem !important;
}

.risk-legend {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  flex-wrap: wrap;
}

.legend-title {
  font-weight: 600;
}

.legend-item {
  font-weight: 500;
}
</style>

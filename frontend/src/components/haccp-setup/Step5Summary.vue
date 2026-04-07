<script setup lang="ts">
import type { WizardState, HaccpTrinn } from '@/types/haccp-setup'
import {
  businessTypeLabels,
  businessSizeLabels,
} from '@/types/haccp-setup'
import { computed } from 'vue'
import { ShieldCheck, ClipboardCheck, Info, AlertTriangle } from 'lucide-vue-next'

const props = defineProps<{
  wizard: WizardState
  haccpTrinn: HaccpTrinn
  isGenerating: boolean
}>()

defineEmits<{
  generate: []
}>()

const prereqSummary = computed(() => {
  const ok = props.wizard.prerequisites.filter((p) => p.status === 'OK').length
  const total = props.wizard.prerequisites.length
  return { ok, total }
})

const kkpCount = computed(() => {
  return props.wizard.hazardEntries.filter((e) => e.likelihood * e.severity >= 6).length
})

const previewChecklists = computed(() => {
  const w = props.wizard
  const list: { name: string; frequency: string; description: string }[] = []

  list.push({ name: 'Daglig renhold', frequency: 'Daglig', description: 'Rutiner for daglig rengjøring av lokaler, utstyr og overflater.' })
  list.push({ name: 'Personlig hygiene', frequency: 'Daglig', description: 'Håndvask, arbeidstøy, sykdomskontroll og hygieneatferd.' })
  list.push({ name: 'Avfallshåndtering', frequency: 'Daglig', description: 'Sortering, oppbevaring og henting av avfall.' })

  if (w.temperatureEquipment.includes('REFRIGERATORS'))
    list.push({ name: 'Temperaturkontroll – Kjøleskap', frequency: 'Daglig', description: 'Loggføring av kjøleskaptemperaturer. Maks 4°C.' })
  if (w.temperatureEquipment.includes('FREEZERS'))
    list.push({ name: 'Temperaturkontroll – Frysere', frequency: 'Daglig', description: 'Loggføring av frysertemperaturer. Maks -18°C.' })

  const hasNonShelfStable = !w.foodTypes.includes('SHELF_STABLE_ONLY') || w.foodTypes.length > 1
  if (hasNonShelfStable)
    list.push({ name: 'Mottakskontroll', frequency: 'Daglig', description: 'Kontroll av temperatur, holdbarhet og emballasje ved varemottak.' })

  if (w.processes.includes('COOKING_HEAT_TREATMENT'))
    list.push({ name: 'Tilberedningskontroll', frequency: 'Daglig', description: 'Kjernetemperatur ved varmebehandling. Min 75°C.' })
  if (w.processes.includes('COOLING_COOKED'))
    list.push({ name: 'Nedkjølingskontroll', frequency: 'Daglig', description: 'Nedkjøling fra 60°C til 4°C innen 4 timer.' })

  if (w.servesVulnerableGroups || w.foodTypes.includes('ALLERGEN_CONTAINING'))
    list.push({ name: 'Allergenkontroll', frequency: 'Daglig', description: 'Merking, kryssforurensning og informasjon til gjester om allergener.' })

  if (w.servesVulnerableGroups || w.handlesHighRiskProducts)
    list.push({ name: 'Kritiske kontrollpunkter (KKP)', frequency: 'Daglig', description: 'Overvåking av kritiske kontrollpunkter med grenseverdier og korrigerende tiltak.' })

  if (w.temperatureEquipment.includes('HOT_HOLDING'))
    list.push({ name: 'Varmholdingskontroll', frequency: 'Daglig', description: 'Varmholding over 60°C. Maks 2 timer under 60°C.' })

  list.push({ name: 'Ukentlig renhold', frequency: 'Ukentlig', description: 'Grundig rengjøring av kjøleskap, ventilasjon, gulv og vegger.' })
  list.push({ name: 'Skadedyrkontroll', frequency: 'Månedlig', description: 'Inspeksjon for tegn på skadedyr og forebyggende tiltak.' })

  const hasRealEquipment = w.temperatureEquipment.length > 0 && !w.temperatureEquipment.includes('NONE')
  if (hasRealEquipment)
    list.push({ name: 'Vedlikehold av utstyr', frequency: 'Månedlig', description: 'Kalibrering av termometre og vedlikehold av kjøle-/fryseutstyr.' })

  list.push({ name: 'Årlig gjennomgang av IK-mat', frequency: 'Årlig', description: 'Gjennomgang og oppdatering av internkontrollsystemet.' })
  list.push({ name: 'Opplæringsplan', frequency: 'Årlig', description: 'Opplæring av ansatte i mathygiene, HACCP og IK-mat-rutiner.' })

  return list
})
</script>

<template>
  <div class="step-content">
    <!-- HACCP Level summary -->
    <div class="haccp-summary-card">
      <div class="haccp-summary-header">
        <ShieldCheck :size="20" :stroke-width="1.5" />
        <span>{{ haccpTrinn.label }}</span>
      </div>
      <div class="haccp-summary-details">
        <div class="detail-item">
          <span class="detail-label">Virksomhet</span>
          <span class="detail-value">
            {{ wizard.businessType ? businessTypeLabels[wizard.businessType] : '–' }}
            · {{ wizard.businessSize ? businessSizeLabels[wizard.businessSize] : '–' }}
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Grunnforutsetninger</span>
          <span class="detail-value">{{ prereqSummary.ok }} av {{ prereqSummary.total }} på plass</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Prosesstrinn</span>
          <span class="detail-value">{{ wizard.processSteps.length }} trinn i flytskjema</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">Fareanalyse</span>
          <span class="detail-value">{{ wizard.hazardEntries.length }} farer vurdert · {{ kkpCount }} KKP</span>
        </div>
      </div>
    </div>

    <div v-if="prereqSummary.ok < prereqSummary.total" class="prereq-warning">
      <AlertTriangle :size="16" :stroke-width="1.5" />
      <p>
        <strong>{{ prereqSummary.total - prereqSummary.ok }} grunnforutsetning{{ prereqSummary.total - prereqSummary.ok !== 1 ? 'er' : '' }}</strong>
        er ikke markert som «på plass». Vi anbefaler å få disse på plass. Sjekklistene genereres uansett.
      </p>
    </div>

    <div class="preview-section">
      <h3>
        <ClipboardCheck :size="18" :stroke-width="1.5" />
        {{ previewChecklists.length }} sjekklister vil bli generert
      </h3>
      <div class="checklist-grid">
        <div
          v-for="cl in previewChecklists"
          :key="cl.name"
          class="checklist-preview-card"
        >
          <div class="checklist-preview-header">
            <span class="checklist-preview-name">{{ cl.name }}</span>
            <span class="checklist-preview-freq">{{ cl.frequency }}</span>
          </div>
          <p class="checklist-preview-desc">{{ cl.description }}</p>
        </div>
      </div>
    </div>

    <div class="info-banner">
      <Info :size="16" :stroke-width="1.5" />
      <p>
        Sjekklistene blir lagt inn i din sjekkliste-modul og vil dukke opp som oppgaver for kjøkkenpersonalet
        basert på frekvens (daglig, ukentlig, månedlig, årlig). Du kan tilpasse dem når som helst.
      </p>
    </div>
  </div>
</template>

<style scoped>
.step-content {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.haccp-summary-card {
  padding: 1.25rem;
  border-radius: 0.75rem;
  background: hsl(var(--primary) / 0.04);
  border: 1px solid hsl(var(--primary) / 0.12);
}

.haccp-summary-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.0625rem;
  font-weight: 700;
  color: hsl(var(--primary));
  margin-bottom: 0.875rem;
}

.haccp-summary-details {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.detail-item {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  font-size: 0.8125rem;
}

.detail-label {
  width: 140px;
  flex-shrink: 0;
  font-weight: 500;
  color: hsl(var(--muted-foreground));
}

.detail-value {
  color: hsl(var(--foreground));
  font-weight: 500;
}

.prereq-warning {
  display: flex;
  gap: 0.625rem;
  padding: 0.75rem 1rem;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 0.5rem;
  font-size: 0.8125rem;
  color: #92400e;
  line-height: 1.5;
}

.prereq-warning svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.prereq-warning p { margin: 0; }

.preview-section h3 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
  color: hsl(var(--foreground));
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checklist-grid {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.checklist-preview-card {
  padding: 0.625rem 0.875rem;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: white;
}

.checklist-preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.125rem;
}

.checklist-preview-name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.checklist-preview-freq {
  font-size: 0.625rem;
  font-weight: 500;
  padding: 0.0625rem 0.5rem;
  border-radius: 999px;
  background: hsl(var(--primary) / 0.06);
  color: hsl(var(--primary));
  border: 1px solid hsl(var(--primary) / 0.15);
}

.checklist-preview-desc {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.4;
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

.info-banner p { margin: 0; }
</style>

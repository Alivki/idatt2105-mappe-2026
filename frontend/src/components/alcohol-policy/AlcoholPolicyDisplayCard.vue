<script setup lang="ts">
import {
  ScrollText,
  Award,
  Pencil,
  ExternalLink,
  CalendarDays,
  User,
  MapPin,
  Hash,
  ShieldCheck,
  ScanEye,
  HandMetal,
  CircleHelp,
  CreditCard,
} from 'lucide-vue-next'
import {Separator} from '@/components/ui/separator'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import type {AlcoholPolicy, AgeCheckLimit, IdType, KnowledgeTestType} from '@/types/alcoholPolicy'

defineProps<{
  policy: AlcoholPolicy
}>()

const emits = defineEmits<{
  (e: 'edit'): void
  (e: 'open-document', docId: number): void
}>()

function formatDate(iso: string | null): string {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleDateString('nb-NO', {day: '2-digit', month: '2-digit', year: 'numeric'})
}

const ageCheckLabel: Record<AgeCheckLimit, string> = {
  UNDER_25: 'Alle under 25',
  UNDER_23: 'Alle under 23',
}

const idTypeLabel: Record<IdType, string> = {
  PASS: 'Pass',
  FORERKORT: 'Førerkort',
  BANKKORT: 'Bankkort m/bilde',
  NASJONALT_ID: 'Nasjonalt ID-kort',
}

const knowledgeTestLabel: Record<KnowledgeTestType, string> = {
  SALG: 'Salg',
  SKJENKE: 'Skjenke',
  BOTH: 'Salg og skjenke',
}

function bevillingStatus(validUntil: string | null): { label: string; class: string } {
  if (!validUntil) return {label: 'Ukjent', class: 'status--neutral'}
  const diff = Math.ceil((new Date(validUntil).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
  if (diff < 0) return {label: 'Utløpt', class: 'status--expired'}
  if (diff <= 90) return {label: `${diff} dager igjen`, class: 'status--warning'}
  return {label: 'Aktiv', class: 'status--active'}
}
</script>

<template>
  <section class="header-row">
    <div>
      <h1>Skjenkepolicy</h1>
      <p>Rutiner iht. alkoholloven §1-9 og alkoholforskriften kap. 8</p>
    </div>
    <div class="header-actions">
      <Button type="button" variant="outline" size="sm" @click="emits('edit')">
        <Pencil :size="14" aria-hidden="true"/>
        Rediger
      </Button>
    </div>
  </section>

  <div
    v-if="bevillingStatus(policy.bevillingValidUntil).class === 'status--warning' || bevillingStatus(policy.bevillingValidUntil).class === 'status--expired'"
    class="alert-banner"
    :class="bevillingStatus(policy.bevillingValidUntil).class === 'status--expired' ? 'alert-banner--danger' : 'alert-banner--warning'"
  >
    <div class="alert-banner__content">
      <CalendarDays :size="16"/>
      <span v-if="bevillingStatus(policy.bevillingValidUntil).class === 'status--expired'">
        Bevillingen har utløpt. Oppdater bevillingsinformasjonen umiddelbart.
      </span>
      <span v-else>
        Bevillingen utløper snart ({{ bevillingStatus(policy.bevillingValidUntil).label }}). Planlegg fornyelse.
      </span>
    </div>
    <Button variant="outline" size="sm" @click="emits('edit')">Oppdater</Button>
  </div>

  <div class="cards-row">
    <div class="doc-card doc-card--bevilling">
      <div class="doc-card__watermark" aria-hidden="true">
        <ScrollText :size="120"/>
      </div>
      <div class="doc-card__header">
        <div class="doc-card__badge">
          <ShieldCheck :size="16"/>
          <span>Skjenkebevillingslisens</span>
        </div>
        <div class="doc-card__status" :class="bevillingStatus(policy.bevillingValidUntil).class">
          {{ bevillingStatus(policy.bevillingValidUntil).label }}
        </div>
      </div>

      <div class="doc-card__body">
        <div class="doc-card__fields">
          <div class="doc-card__field">
            <span class="doc-card__label"><Hash :size="13"/> Bevillingsnr.</span>
            <span class="doc-card__value doc-card__value--mono">{{
                policy.bevillingNumber ?? '—'
              }}</span>
          </div>
          <div class="doc-card__field">
            <span class="doc-card__label"><CalendarDays :size="13"/> Gyldig til</span>
            <span class="doc-card__value"
                  :class="{ 'doc-card__value--warn': bevillingStatus(policy.bevillingValidUntil).class !== 'status--active' }">{{
                formatDate(policy.bevillingValidUntil)
              }}</span>
          </div>
          <div class="doc-card__field">
            <span class="doc-card__label"><User :size="13"/> Styrer</span>
            <span class="doc-card__value">{{ policy.styrerName ?? '—' }}</span>
          </div>
          <div class="doc-card__field">
            <span class="doc-card__label"><User :size="13"/> Stedfortreder</span>
            <span class="doc-card__value">{{ policy.stedfortrederName ?? '—' }}</span>
          </div>
        </div>
      </div>

      <div v-if="policy.bevillingDocumentId" class="doc-card__footer">
        <Button variant="outline" size="sm" class="doc-card__doc-btn"
                @click="emits('open-document', policy.bevillingDocumentId!)">
          <ExternalLink :size="14"/>
          Åpne dokument
        </Button>
      </div>
    </div>

    <div class="doc-card doc-card--kunnskap">
      <div class="doc-card__watermark" aria-hidden="true">
        <Award :size="100"/>
      </div>
      <div class="doc-card__header">
        <div class="doc-card__badge doc-card__badge--kunnskap">
          <Award :size="16"/>
          <span>Kunnskapsprøve</span>
        </div>
        <div class="doc-card__status status--active" v-if="policy.kunnskapsprovePassedDate">
          Bestått
        </div>
      </div>

      <div class="doc-card__body">
        <div class="doc-card__fields">
          <div class="doc-card__field">
            <span class="doc-card__label"><User :size="13"/> Kandidat</span>
            <span class="doc-card__value">{{ policy.kunnskapsproveCandidateName ?? '—' }}</span>
          </div>
          <div class="doc-card__field">
            <span class="doc-card__label"><ShieldCheck :size="13"/> Type</span>
            <span class="doc-card__value">{{
                policy.kunnskapsproveType ? knowledgeTestLabel[policy.kunnskapsproveType] : '—'
              }}</span>
          </div>
          <div class="doc-card__field">
            <span class="doc-card__label"><MapPin :size="13"/> Kommune</span>
            <span class="doc-card__value">{{ policy.kunnskapsproveMunicipality ?? '—' }}</span>
          </div>
          <div class="doc-card__field">
            <span class="doc-card__label"><CalendarDays :size="13"/> Bestått</span>
            <span class="doc-card__value">{{ formatDate(policy.kunnskapsprovePassedDate) }}</span>
          </div>
        </div>
      </div>

      <div v-if="policy.kunnskapsproveDocumentId" class="doc-card__footer">
        <Button variant="outline" size="sm" class="doc-card__doc-btn"
                @click="emits('open-document', policy.kunnskapsproveDocumentId!)">
          <ExternalLink :size="14"/>
          Åpne bevis
        </Button>
      </div>
    </div>
  </div>

  <div class="routines-card">
    <div class="routines-card__section">
      <div class="routines-card__header">
        <ScanEye :size="18" class="routines-card__icon routines-card__icon--brand"/>
        <h4 class="routines-card__title">Rutiner for alderskontroll</h4>
      </div>

      <div class="routine-items">
        <div class="routine-item">
          <span class="routine-item__label">Aldersgrense for legitimasjonskontroll</span>
          <Badge tone="brand">{{ ageCheckLabel[policy.ageCheckLimit] }}</Badge>
        </div>

        <div class="routine-item">
          <span class="routine-item__label">Godkjent legitimasjon</span>
          <div class="id-chips">
            <span
              v-for="idType in policy.acceptedIdTypes"
              :key="idType"
              class="id-chip"
            >
              <CreditCard :size="12"/>
              {{ idTypeLabel[idType] }}
            </span>
          </div>
        </div>

        <div v-if="policy.doubtRoutine" class="routine-item routine-item--block">
          <div class="routine-item__header">
            <CircleHelp :size="14" class="routine-item__icon"/>
            <span class="routine-item__label">Rutine ved tvil om alder</span>
          </div>
          <p class="routine-item__text">{{ policy.doubtRoutine }}</p>
        </div>
      </div>
    </div>

    <Separator/>

    <div class="routines-card__section">
      <div class="routines-card__header">
        <HandMetal :size="18" class="routines-card__icon routines-card__icon--amber"/>
        <h4 class="routines-card__title">Ansvarlig servering</h4>
      </div>

      <div class="routine-items">
        <div v-if="policy.intoxicationSigns" class="routine-item routine-item--block">
          <div class="routine-item__header">
            <ScanEye :size="14" class="routine-item__icon"/>
            <span class="routine-item__label">Identifisering av berusede gjester</span>
          </div>
          <p class="routine-item__text">{{ policy.intoxicationSigns }}</p>
        </div>

        <div v-if="policy.refusalProcedure" class="routine-item routine-item--block">
          <div class="routine-item__header">
            <HandMetal :size="14" class="routine-item__icon"/>
            <span class="routine-item__label">Prosedyre ved nekt av servering</span>
          </div>
          <p class="routine-item__text">{{ policy.refusalProcedure }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.header-row h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.header-row p {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 0.88rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-xl);
  font-size: 0.88rem;
}

.alert-banner--warning {
  background: hsl(38 95% 92%);
  border: 1px solid hsl(38 80% 80%);
  color: hsl(30 80% 30%);
}

.alert-banner--danger {
  background: hsl(0 70% 94%);
  border: 1px solid hsl(0 60% 82%);
  color: hsl(0 60% 35%);
}

.alert-banner__content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.cards-row {
  display: flex;
  gap: 1rem;
  width: 100%;
}

.doc-card {
  position: relative;
  overflow: hidden;
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-xl);
  background: hsl(var(--card));
  display: flex;
  flex-direction: column;
}

.doc-card--bevilling {
  flex: 2;
}

.doc-card--kunnskap {
  flex: 1;
}

.doc-card__watermark {
  position: absolute;
  right: -16px;
  bottom: -16px;
  opacity: 0.035;
  pointer-events: none;
  color: hsl(var(--foreground));
}

.doc-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid hsl(var(--border));
  background: linear-gradient(135deg, hsl(var(--primary) / 0.03), hsl(var(--primary) / 0.07));
}

.doc-card__badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.78rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: hsl(var(--primary));
}

.doc-card__badge--kunnskap {
  color: hsl(142 60% 40%);
}

.doc-card__status {
  font-size: 0.72rem;
  font-weight: 600;
  padding: 0.2rem 0.6rem;
  border-radius: var(--radius-pill);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.status--active {
  background: hsl(142 60% 94%);
  color: hsl(142 60% 30%);
}

.status--warning {
  background: hsl(38 95% 92%);
  color: hsl(30 80% 35%);
}

.status--expired {
  background: hsl(0 70% 94%);
  color: hsl(0 60% 40%);
}

.status--neutral {
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
}

.doc-card__body {
  padding: 1.25rem;
  flex: 1;
}

.doc-card__fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.1rem;
}

.doc-card__field {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.doc-card__label {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.72rem;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.doc-card__value {
  font-size: 0.92rem;
  font-weight: 500;
  color: hsl(var(--foreground));
}

.doc-card__value--mono {
  font-family: ui-monospace, 'SF Mono', 'Cascadia Code', monospace;
  letter-spacing: 0.03em;
}

.doc-card__value--warn {
  color: var(--red, hsl(0 60% 40%));
  font-weight: 600;
}

.doc-card__footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid hsl(var(--border));
}

.doc-card__doc-btn {
  width: 100%;
  justify-content: center;
}

.routines-card {
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-xl);
  background: hsl(var(--card));
  overflow: hidden;
}

.routines-card__section {
  padding: 1.25rem 1.5rem;
}

.routines-card__title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0 0 0.75rem;
}

.routines-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.routines-card__icon {
  flex-shrink: 0;
}

.routines-card__icon--brand {
  color: var(--brand, hsl(var(--primary)));
}

.routines-card__icon--amber {
  color: var(--amber, hsl(30 80% 45%));
}

.routine-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.routine-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.routine-item--block {
  flex-direction: column;
  align-items: stretch;
  background: hsl(var(--muted) / 0.5);
  border-radius: var(--radius-lg, 8px);
  padding: 14px 16px;
  gap: 8px;
}

.routine-item__header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.routine-item__icon {
  color: #94a3b8;
  flex-shrink: 0;
}

.routine-item__label {
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--text-secondary, #64748b);
}

.routine-item--block .routine-item__label {
  font-weight: 600;
  color: hsl(var(--foreground));
  font-size: 0.82rem;
}

.routine-item__text {
  margin: 0;
  font-size: 0.88rem;
  line-height: 1.6;
  color: var(--text-secondary, #64748b);
}

.id-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.id-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: var(--radius-pill, 999px);
  background: hsl(var(--muted));
  font-size: 0.78rem;
  font-weight: 500;
  color: hsl(var(--foreground));
  white-space: nowrap;
}

@media (max-width: 768px) {
  .cards-row {
    flex-direction: column;
  }

  .doc-card--bevilling, .doc-card--kunnskap {
    flex: none;
  }

  .doc-card__body {
    flex: none;
  }

  .doc-card__fields {
    grid-template-columns: 1fr;
  }

  .header-row {
    flex-direction: column;
    gap: 12px;
  }

  .routine-item:not(.routine-item--block) {
    flex-direction: column;
    align-items: flex-start;
  }

  .alert-banner {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

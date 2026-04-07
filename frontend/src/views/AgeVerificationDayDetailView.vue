<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, AlertTriangle, IdCard } from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import Button from '@/components/ui/button/Button.vue'
import OverviewCard from '@/components/common/OverviewCard.vue'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { useDayDetailQuery } from '@/composables/useAgeVerification'
import type { AlcoholDeviationType } from '@/types/deviation'

const route = useRoute()
const router = useRouter()

const date = ref(route.params.date as string)
const dayDetailQuery = useDayDetailQuery(date)
const detail = computed(() => dayDetailQuery.data.value)

function formatDate(iso: string): string {
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString('nb-NO', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('nb-NO', { hour: '2-digit', minute: '2-digit' })
}

const deviationLabel: Partial<Record<AlcoholDeviationType, string>> = {
  NEKTET_VISE_LEGITIMASJON: 'Nektet å vise leg',
  GLEMTE_SJEKKE_LEGITIMASJON: 'Glemte å sjekke leg',
  MINDREAARIG_FORSOK: 'Mindreårig forsøk',
  FALSK_LEGITIMASJON: 'Falsk legitimasjon',
  UTGAATT_LEGITIMASJON: 'Utgått legitimasjon',
  LEGITIMASJON_ANNET: 'Annet',
}
</script>

<template>
  <AppLayout>
    <template #header>
      <div class="flex items-center gap-3 px-4 py-3 border-b border-stone-200 bg-stone-50">
        <SidebarTrigger />
        <span class="text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-full px-3 py-1">
          IK-Alkohol
        </span>
      </div>
    </template>

    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger />
        <Separator orientation="vertical" class="header-separator" />
        <span class="page-title">Bevilling</span>
      </div>
    </header>

    <div class="page-content">
      <Button variant="ghost" size="sm" class="back-btn" @click="router.push({ name: 'bevilling' })">
        <ArrowLeft :size="16" />
        Tilbake til oversikt
      </Button>

      <p v-if="dayDetailQuery.isLoading.value" class="state-line">Laster...</p>

      <template v-else-if="detail">
        <section class="header-row">
          <div>
            <h1>{{ formatDate(date) }}</h1>
            <p>Detaljert oversikt over alderskontroller denne dagen</p>
          </div>
        </section>

        <section class="cards-group">
          <OverviewCard label="Skift" :value="detail.shifts.length" />
          <OverviewCard label="Leg sjekket" :value="detail.totalIdsChecked" variant="resolved" />
          <OverviewCard label="Avvik totalt" :value="detail.totalDeviations" :variant="detail.totalDeviations > 0 ? 'open' : 'neutral'" />
        </section>

        <section v-if="Object.keys(detail.deviationsByType).length > 0" class="breakdown-section">
          <h2>Avvik fordelt på type</h2>
          <div class="breakdown-grid">
            <div v-for="(count, type) in detail.deviationsByType" :key="type" class="breakdown-item">
              <span class="breakdown-label">{{ deviationLabel[type as AlcoholDeviationType] ?? type }}</span>
              <span class="breakdown-value">{{ count }}</span>
            </div>
          </div>
        </section>

        <section class="shifts-section">
          <h2>Individuelle skift</h2>

          <div v-for="s in detail.shifts" :key="s.shift.id" class="shift-card">
            <div class="shift-card-header">
              <div class="shift-user">
                <div class="user-avatar">
                  {{ s.shift.userName.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase() }}
                </div>
                <div>
                  <p class="shift-user-name">{{ s.shift.userName }}</p>
                  <p class="shift-time">
                    {{ formatTime(s.shift.startedAt) }}
                    <template v-if="s.shift.endedAt"> – {{ formatTime(s.shift.endedAt) }}</template>
                    <template v-else> · <span class="active-badge">Pågår</span></template>
                  </p>
                </div>
              </div>
              <div class="shift-stats-inline">
                <span class="stat-chip stat-chip--ids">
                  <IdCard :size="14" />
                  {{ s.shift.idsCheckedCount }}
                </span>
                <span v-if="s.deviations.length > 0" class="stat-chip stat-chip--dev">
                  <AlertTriangle :size="14" />
                  {{ s.deviations.length }}
                </span>
              </div>
            </div>

            <div v-if="s.deviations.length > 0" class="shift-deviations">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Type</TableHead>
                    <TableHead>Beskrivelse</TableHead>
                    <TableHead>Tidspunkt</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow v-for="dev in s.deviations" :key="dev.id">
                    <TableCell class="cell-bold">{{ deviationLabel[dev.deviationType] ?? dev.deviationType }}</TableCell>
                    <TableCell class="cell-text">{{ dev.description }}</TableCell>
                    <TableCell class="cell-text">{{ formatTime(dev.reportedAt) }}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </div>

            <p v-else class="no-deviations">Ingen avvik registrert i dette skiftet</p>
          </div>

          <div v-if="detail.shifts.length === 0" class="empty-state-small">
            Ingen skift registrert denne dagen.
          </div>
        </section>
      </template>
    </div>
  </AppLayout>
</template>

<style scoped>
.page-header { display: flex; height: 4rem; flex-shrink: 0; align-items: center; }
.page-header-inner { display: flex; align-items: center; gap: 0.5rem; padding: 0 1rem; }
.header-separator { height: 1rem !important; width: 1px !important; margin-right: 0.5rem; }
.page-title { font-weight: 500; color: hsl(var(--sidebar-primary, 245 43% 52%)); }
.page-content { display: flex; flex: 1; flex-direction: column; gap: 1rem; padding: 0 1rem 1rem; }

.back-btn { margin-bottom: 0.5rem; align-self: flex-start; }

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

h1 { margin: 0; font-size: 1.8rem; letter-spacing: -0.02em; }
h1::first-letter { text-transform: uppercase; }
.header-row p { margin-top: 6px; color: var(--text-secondary); font-size: 1rem; }

h2 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 12px;
  letter-spacing: -0.01em;
}

.cards-group {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.breakdown-section {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
  padding: 16px;
}

.breakdown-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.breakdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--background));
  font-size: 0.85rem;
}

.breakdown-label { color: hsl(var(--muted-foreground)); }
.breakdown-value { font-weight: 700; color: #dc2626; }

.shifts-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shift-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
  overflow: hidden;
}

.shift-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  gap: 12px;
}

.shift-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.shift-user-name { font-weight: 600; margin: 0; font-size: 0.9rem; }

.shift-time {
  font-size: 0.8rem;
  color: hsl(var(--muted-foreground));
  margin: 2px 0 0;
}

.active-badge {
  font-size: 0.7rem;
  font-weight: 600;
  color: #059669;
  background: #ecfdf5;
  padding: 1px 6px;
  border-radius: 4px;
}

.shift-stats-inline {
  display: flex;
  gap: 8px;
}

.stat-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 8px;
}

.stat-chip--ids { background: #ecfdf5; color: #059669; }
.stat-chip--dev { background: #fef2f2; color: #dc2626; }

.shift-deviations {
  border-top: 1px solid hsl(var(--border));
}

.no-deviations {
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
  padding: 0 16px 16px;
  margin: 0;
}

.cell-bold { font-weight: 500; }
.cell-text { color: hsl(var(--muted-foreground)); font-size: 0.85rem; }

.empty-state-small {
  text-align: center;
  padding: 32px;
  font-size: 0.9rem;
  color: hsl(var(--muted-foreground));
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
}

.state-line {
  border-radius: var(--radius-md); border: 1px solid hsl(var(--border));
  background: hsl(var(--card)); padding: 12px; color: var(--text-secondary);
}

@media (max-width: 768px) {
  .cards-group { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .shift-card-header { flex-direction: column; align-items: flex-start; }
}
</style>

<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  ClipboardCheck,
  Thermometer,
  AlertTriangle,
  Gauge,
  GraduationCap,
} from 'lucide-vue-next'
import OverviewCard from '@/components/common/OverviewCard.vue'
import DeviationBarChart from '@/components/dashboard/DeviationBarChart.vue'
import ChecklistCompletionChart from '@/components/dashboard/ChecklistCompletionChart.vue'
import DeviationDonutChart from '@/components/dashboard/DeviationDonutChart.vue'
import PenaltyPointsStatus from '@/components/deviations/PenaltyPointsStatus.vue'
import TodayIdChecksCard from '@/components/dashboard/TodayIdChecksCard.vue'
import SetupCtaCard from '@/components/dashboard/SetupCtaCard.vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { useChecklistsQuery, useCompletionHistoryQuery } from '@/composables/useChecklists'
import { useFoodDeviationsQuery } from '@/composables/useFoodDeviations'
import { useAlcoholDeviationsQuery } from '@/composables/useAlcoholDeviations'
import { usePenaltyPointsQuery } from '@/composables/usePenaltyPoints'
import { useTemperatureMonitoring } from '@/composables/useTemperatureMonitoring'
import { useTrainingLogsQuery } from '@/composables/useTrainingLogs'
import { useAlcoholPolicyExistsQuery } from '@/composables/useAlcoholPolicy'
import { useActiveShiftQuery, useDayDetailQuery } from '@/composables/useAgeVerification'
import { useAuthStore } from '@/stores/auth'
import type { Checklist } from '@/types/checklist'

const auth = useAuthStore()
const isManagerOrAdmin = computed(() => ['ADMIN', 'MANAGER'].includes(auth.role ?? ''))

const foodQuery = useFoodDeviationsQuery()
const alcoholQuery = useAlcoholDeviationsQuery()
const checklistsQuery = useChecklistsQuery()
const completionHistoryQuery = useCompletionHistoryQuery()
const penaltyQuery = usePenaltyPointsQuery()
const tempMonitoring = useTemperatureMonitoring()
const trainingQuery = useTrainingLogsQuery()
const alcoholPolicyQuery = useAlcoholPolicyExistsQuery()
const todayDate = ref(new Date().toISOString().slice(0, 10))
const todayDetailQuery = useDayDetailQuery(todayDate, isManagerOrAdmin)
const activeShiftQuery = useActiveShiftQuery()

// ── KPI helpers ──

function getDailyChecklistStats(checklists: Checklist[]): { total: number; completed: number } {
  const daily = checklists.filter((item) => item.frequency === 'DAILY' && item.active)
  return {
    total: daily.length,
    completed: daily.filter((item) => item.status === 'COMPLETED').length,
  }
}

function getFrequencyStats(checklists: Checklist[], freq: 'WEEKLY' | 'MONTHLY') {
  const items = checklists.filter((c) => c.frequency === freq && c.active)
  return items.filter((c) => c.status !== 'COMPLETED').length
}

const dailyStats = computed(() => getDailyChecklistStats(checklistsQuery.data.value ?? []))
const weeklyRemaining = computed(() => getFrequencyStats(checklistsQuery.data.value ?? [], 'WEEKLY'))
const monthlyRemaining = computed(() => getFrequencyStats(checklistsQuery.data.value ?? [], 'MONTHLY'))

const checklistProgress = computed(() => {
  const { completed, total } = dailyStats.value
  if (total <= 0) return 0
  return Math.round((completed / total) * 100)
})

const tempDeviationCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return tempMonitoring.entries.value.filter(
    (e) => e.status === 'DEVIATION' && e.measuredAt.slice(0, 10) === today,
  ).length
})

const openDeviationCount = computed(() => {
  const foodItems = foodQuery.data.value ?? []
  const alcoholItems = alcoholQuery.data.value ?? []
  return (
    foodItems.filter((d) => d.status === 'OPEN').length +
    alcoholItems.filter((d) => d.status === 'OPEN').length
  )
})

const completedApplianceCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  const countByAppliance: Record<number, number> = {}
  for (const e of tempMonitoring.entries.value) {
    if (e.measuredAt.slice(0, 10) === today) {
      countByAppliance[e.applianceId] = (countByAppliance[e.applianceId] ?? 0) + 1
    }
  }
  return tempMonitoring.activeAppliances.value.filter((a) => (countByAppliance[a.id] ?? 0) >= 2).length
})

const trainingStats = computed(() => {
  const logs = trainingQuery.data.value ?? []
  const completed = logs.filter((l) => l.status === 'COMPLETED').length
  const expiringSoon = logs.filter((l) => l.status === 'EXPIRES_SOON').length
  return { completed, total: logs.length, expiringSoon }
})

const todayShiftsCount = computed(() => {
  if (isManagerOrAdmin.value) return todayDetailQuery.data.value?.shifts.length ?? 0
  return activeShiftQuery.data.value ? 1 : 0
})
const todayIdsChecked = computed(() => {
  if (isManagerOrAdmin.value) return todayDetailQuery.data.value?.totalIdsChecked ?? 0
  return activeShiftQuery.data.value?.shift.idsCheckedCount ?? 0
})
const todayShiftDeviations = computed(() => {
  if (isManagerOrAdmin.value) return todayDetailQuery.data.value?.totalDeviations ?? 0
  return activeShiftQuery.data.value?.shift.deviationCount ?? 0
})

const showHaccpSetup = computed(() => {
  const checklists = checklistsQuery.data.value ?? []
  return checklists.filter((c) => c.source === 'HACCP_WIZARD').length === 0
})

const showAlcoholPolicySetup = computed(() => alcoholPolicyQuery.data.value === false)

const todayFormatted = computed(() => {
  return new Date().toLocaleDateString('nb-NO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
})
</script>

<template>
  <AppLayout>
    <PageHeader title="Oversikt" />

    <div class="page-content">
      <p class="dashboard-date">{{ todayFormatted }}</p>

      <!-- Setup CTAs — top if either is missing -->
      <SetupCtaCard
        :show-haccp="showHaccpSetup"
        :show-alcohol-policy="showAlcoholPolicySetup"
      />

      <!-- KPI cards -->
      <section class="kpi-grid">
        <router-link to="/sjekklister" class="kpi-link">
          <OverviewCard
            label="Sjekklister i dag"
            :value="`${dailyStats.completed}/${dailyStats.total}`"
            :icon="ClipboardCheck"
          >
            <div
              class="progress-track"
              role="progressbar"
              :aria-valuenow="dailyStats.completed"
              :aria-valuemax="Math.max(dailyStats.total, 1)"
            >
              <div
                class="progress-fill progress-fill--green"
                :style="{ width: `${checklistProgress}%` }"
              />
            </div>
            <p class="kpi-meta">
              {{ weeklyRemaining }} ukentlige &middot; {{ monthlyRemaining }} månedlige gjenstår
            </p>
          </OverviewCard>
        </router-link>

        <router-link to="/temperatur" class="kpi-link">
          <OverviewCard
            label="Temp.avvik"
            :value="String(tempDeviationCount)"
            :variant="tempDeviationCount > 0 ? 'open' : 'neutral'"
            :sub-label="tempDeviationCount > 0 ? 'Krever tiltak' : 'Alt OK'"
            :icon="Thermometer"
          />
        </router-link>

        <router-link to="/avvik" class="kpi-link">
          <OverviewCard
            label="Åpne avvik"
            :value="String(openDeviationCount)"
            :icon="AlertTriangle"
            :variant="openDeviationCount > 0 ? 'open' : 'neutral'"
            :sub-label="openDeviationCount > 0 ? 'Trenger oppfølging' : 'Ingen åpne avvik'"
          />
        </router-link>

        <router-link to="/temperatur/hvitevarer" class="kpi-link">
          <OverviewCard
            label="Ferdigmålte enheter"
            :value="`${completedApplianceCount}/${tempMonitoring.activeAppliances.value.length}`"
            :icon="Gauge"
            sub-label="Ferdig = 2 målinger i dag"
          />
        </router-link>

        <router-link to="/opplaering" class="kpi-link">
          <OverviewCard
            label="Opplæring"
            :value="`${trainingStats.completed}/${trainingStats.total}`"
            :icon="GraduationCap"
            :sub-label="trainingStats.expiringSoon > 0 ? `${trainingStats.expiringSoon} utløper snart` : 'Alle oppdatert'"
            :variant="trainingStats.expiringSoon > 0 ? 'in-progress' : 'neutral'"
          />
        </router-link>
      </section>

      <!-- Bento grid -->
      <section class="bento-grid">
        <!-- Row 1: Bar chart (wide) + ID card (narrow) + Donut -->
        <div class="bento-col-wide">
          <DeviationBarChart
            :food-deviations="foodQuery.data.value ?? []"
            :alcohol-deviations="alcoholQuery.data.value ?? []"
          />
        </div>

        <TodayIdChecksCard
          :shifts-today="todayShiftsCount"
          :ids-checked="todayIdsChecked"
          :deviations-today="todayShiftDeviations"
        />

        <DeviationDonutChart
          :food-deviations="foodQuery.data.value ?? []"
          :alcohol-deviations="alcoholQuery.data.value ?? []"
        />

        <!-- Row 2: Penalty points + Checklist chart -->
        <div class="bento-span-full">
          <PenaltyPointsStatus :summary="penaltyQuery.data.value ?? null" />
          <div class="bento-flex-child">
            <ChecklistCompletionChart
              :checklists="checklistsQuery.data.value ?? []"
              :completion-history="completionHistoryQuery.data.value ?? []"
            />
          </div>
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<style scoped>
.page-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 1.25rem;
  padding: 0 1rem 2rem;
}

.dashboard-date {
  margin: 0;
  color: hsl(var(--muted-foreground));
  font-size: 0.84rem;
  text-transform: capitalize;
}

/* ── KPI grid ── */

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.kpi-link {
  display: flex;
  text-decoration: none;
  color: inherit;
  border-radius: var(--radius-lg);
  transition: transform 120ms ease, box-shadow 120ms ease;
}

.kpi-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.kpi-link > * {
  flex: 1;
}

.progress-track {
  height: 5px;
  border-radius: var(--radius-pill);
  background: hsl(var(--border));
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: var(--radius-pill);
  transition: width 500ms ease;
}

.progress-fill--green {
  background: var(--green);
}

.kpi-meta {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  margin: 0;
}

/* ── Bento grid ── */

.bento-grid {
  display: grid;
  grid-template-columns: 5fr 2fr 3fr;
  gap: 14px;
}

.bento-col-wide {
  display: flex;
}

.bento-col-wide > * {
  flex: 1;
}

.bento-span-full {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: 3fr 7fr;
  gap: 14px;
  margin-top: 2px;
}

.bento-flex-child {
  display: flex;
}

.bento-flex-child > * {
  flex: 1;
}

/* ── Responsive ── */

@media (max-width: 1120px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .bento-grid {
    grid-template-columns: 1fr 1fr;
  }

  .bento-col-wide {
    grid-column: span 2;
  }

  .bento-span-full {
    grid-column: span 2;
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 760px) {
  .page-content {
    padding: 0 0.75rem 1rem;
    gap: 0.75rem;
  }

  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }

  .bento-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .bento-col-wide {
    grid-column: span 1;
  }

  .bento-span-full {
    grid-column: span 1;
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>

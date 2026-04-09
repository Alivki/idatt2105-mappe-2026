<script setup lang="ts">
import {computed, ref, watch} from 'vue'
import {Thermometer, TriangleAlert, CheckCircle2, Clock3} from 'lucide-vue-next'
import {useMediaQuery} from '@vueuse/core'
import AppLayout from '@/components/layout/AppLayout.vue'
import OverviewCard from '@/components/common/OverviewCard.vue'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import TemperatureLogForm from '@/components/temperature/TemperatureLogForm.vue'
import TemperatureLogTable from '@/components/temperature/TemperatureLogTable.vue'
import FoodDeviationFormDialog from '@/components/deviations/FoodDeviationFormDialog.vue'
import {
  evaluateTemperatureStatus,
  formatThreshold,
  useTemperatureMonitoring,
} from '@/composables/useTemperatureMonitoring'
import {useCreateFoodDeviationMutation} from '@/composables/useFoodDeviations'
import {useMemberNamesQuery} from '@/composables/useMembers'
import {useCanManage} from '@/composables/useCanManage'
import {useAuthStore} from '@/stores/auth'
import type {CreateFoodDeviationRequest, DeviationSeverity} from '@/types/deviation'
import {toast} from 'vue-sonner'

const auth = useAuthStore()
const canManage = useCanManage()
const memberNamesQuery = useMemberNamesQuery()
const createFoodDeviation = useCreateFoodDeviationMutation()
const {
  activeAppliances,
  appliances,
  entries,
  deleteEntries,
  registerTemperature
} = useTemperatureMonitoring()

const selectedApplianceId = ref<number | null>(null)
const temperatureInput = ref('')
const note = ref('')
const selectedEntryIds = ref<number[]>([])
const deviationDialogOpen = ref(false)
const deviationFormPrefill = ref<Partial<CreateFoodDeviationRequest> | null>(null)
const currentPage = ref(1)
const entriesPerPage = 10
const isMobile = useMediaQuery('(max-width: 768px)')

watch(
  activeAppliances,
  (list) => {
    if (!selectedApplianceId.value && list[0]) {
      selectedApplianceId.value = list[0].id
    }
    if (selectedApplianceId.value != null && !list.some((item) => item.id === selectedApplianceId.value)) {
      selectedApplianceId.value = list[0]?.id ?? null
    }
  },
  {immediate: true},
)

const selectedAppliance = computed(() => {
  if (selectedApplianceId.value == null) return null
  return activeAppliances.value.find((item) => item.id === selectedApplianceId.value) ?? null
})

const recentEntries = computed(() => {
  return entries.value.map((entry) => {
    const appliance = appliances.value.find((item) => item.id === entry.applianceId)
    return {
      ...entry,
      applianceName: appliance?.name ?? 'Ukjent enhet',
      applianceType: appliance?.type ?? 'FRIDGE',
      threshold: appliance ? formatThreshold(appliance.threshold) : '-',
      statusTone: entry.status === 'OK' ? ('ok' as const) : ('danger' as const),
    }
  })
})

const totalPages = computed(() => Math.max(1, Math.ceil(recentEntries.value.length / entriesPerPage)))

const pagedEntries = computed(() => {
  const start = (currentPage.value - 1) * entriesPerPage
  return recentEntries.value.slice(start, start + entriesPerPage)
})

const allRowsSelected = computed(() => {
  return pagedEntries.value.length > 0 && pagedEntries.value.every((entry) => selectedEntryIds.value.includes(entry.id))
})

const paginationSummary = computed(() => {
  if (recentEntries.value.length === 0) return 'Viser 0 av 0'
  const start = (currentPage.value - 1) * entriesPerPage + 1
  const end = Math.min(currentPage.value * entriesPerPage, recentEntries.value.length)
  return `Viser ${start}-${end} av ${recentEntries.value.length}`
})

const expectedMeasurementsToday = computed(() => activeAppliances.value.length * 2)

const measurementsToday = computed(() => {
  const today = new Date()
  const year = today.getFullYear()
  const month = today.getMonth()
  const day = today.getDate()
  return entries.value.filter((entry) => {
    const measured = new Date(entry.measuredAt)
    return measured.getFullYear() === year && measured.getMonth() === month && measured.getDate() === day
  })
})

const measurementsTodayCount = computed(() => measurementsToday.value.length)

const completedUnitsToday = computed(() => {
  const countsByAppliance = measurementsToday.value.reduce<Record<string, number>>((acc, entry) => {
    acc[entry.applianceId] = (acc[entry.applianceId] ?? 0) + 1
    return acc
  }, {})
  return activeAppliances.value.filter((appliance) => (countsByAppliance[appliance.id] ?? 0) >= 2).length
})

const completedUnitsTarget = computed(() => activeAppliances.value.length)

function progressVariant(done: number, total: number): 'open' | 'in-progress' | 'resolved' | 'neutral' {
  if (total <= 0) return 'neutral'
  if (done <= 0) return 'open'
  if (done >= total) return 'resolved'
  return 'in-progress'
}

const measurementsTodayVariant = computed(() => progressVariant(measurementsTodayCount.value, expectedMeasurementsToday.value))
const completedUnitsVariant = computed(() => progressVariant(completedUnitsToday.value, completedUnitsTarget.value))

const isCreatingDeviation = computed(() => createFoodDeviation.isPending.value)
const memberOptions = computed(() => (memberNamesQuery.data.value ?? []).map((m) => ({
  userId: m.userId,
  label: m.fullName
})))

watch(recentEntries, (rows) => {
  const validIds = new Set(rows.map((row) => row.id))
  selectedEntryIds.value = selectedEntryIds.value.filter((id) => validIds.has(id))
  if (currentPage.value > totalPages.value) currentPage.value = totalPages.value
})

const deviationCount = computed(() => entries.value.filter((item) => item.status === 'DEVIATION').length)
const latestEntry = computed(() => entries.value[0] ?? null)

const formStatus = computed(() => {
  if (!selectedAppliance.value) return null
  const value = Number(temperatureInput.value)
  if (Number.isNaN(value)) return null
  return evaluateTemperatureStatus(value, selectedAppliance.value.threshold)
})

function formatDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return new Intl.DateTimeFormat('nb-NO', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

function toDeviationSeverity(temperature: number, min: number, max: number): DeviationSeverity {
  const distance = temperature < min ? min - temperature : temperature - max
  if (distance >= 5) return 'CRITICAL'
  if (distance >= 2) return 'HIGH'
  return 'MEDIUM'
}

async function submitTemperature(): Promise<void> {
  if (!selectedAppliance.value) return
  const temperature = Number(temperatureInput.value)
  if (Number.isNaN(temperature)) return

  const entry = await registerTemperature({
    applianceId: selectedAppliance.value.id,
    temperature,
    note: note.value,
  })

  if (!entry) return

  if (entry.status === 'DEVIATION') {
    const threshold = selectedAppliance.value.threshold
    deviationFormPrefill.value = {
      reportedAt: entry.measuredAt,
      deviationType: 'TEMPERATUR',
      severity: toDeviationSeverity(entry.temperature, threshold.min, threshold.max),
      description: `Temperaturavvik registrert for ${selectedAppliance.value.name}. Målt ${entry.temperature.toFixed(1)}°C (grense ${threshold.min} til ${threshold.max}°C).`,
      immediateAction: note.value.trim() || undefined,
    }
    deviationDialogOpen.value = true
    toast.warning('Temperaturavvik registrert. Fyll ut avviksskjemaet.')
  }

  temperatureInput.value = ''
  note.value = ''
}

function toggleEntrySelection(entryId: number, checked: boolean): void {
  if (checked) {
    if (!selectedEntryIds.value.includes(entryId)) selectedEntryIds.value = [...selectedEntryIds.value, entryId]
    return
  }
  selectedEntryIds.value = selectedEntryIds.value.filter((id) => id !== entryId)
}

function toggleSelectAll(checked: boolean): void {
  if (!checked) {
    const pageIds = new Set(pagedEntries.value.map((entry) => entry.id))
    selectedEntryIds.value = selectedEntryIds.value.filter((id) => !pageIds.has(id))
    return
  }
  const nextSelected = new Set(selectedEntryIds.value)
  pagedEntries.value.forEach((entry) => nextSelected.add(entry.id))
  selectedEntryIds.value = [...nextSelected]
}

async function deleteSelectedMeasurements(): Promise<void> {
  const deletedCount = await deleteEntries(selectedEntryIds.value)
  selectedEntryIds.value = []
  if (deletedCount > 0) toast.success(`${deletedCount} måling${deletedCount > 1 ? 'er' : ''} slettet`)
}

async function handleCreateDeviation(payload: CreateFoodDeviationRequest): Promise<void> {
  try {
    await createFoodDeviation.mutateAsync(payload)
    toast.success('Matavvik registrert')
    deviationDialogOpen.value = false
    deviationFormPrefill.value = null
  } catch {
    toast.error('Kunne ikke registrere matavvik')
  }
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">Temperaturmåling</span>
      </div>
    </header>

    <div class="page-content">
      <section class="page-intro">
        <h1>Temperaturmåling</h1>
        <p>Manuell registrering av temperaturer på kjøleskap og frysere.</p>
      </section>

      <section class="overview-grid">
        <OverviewCard
          label="Målinger i dag"
          :value="`${measurementsTodayCount}/${expectedMeasurementsToday}`"
          :icon="Thermometer"
          :variant="measurementsTodayVariant"
          sub-label="Mål: 2 målinger per aktiv enhet"
        />
        <OverviewCard
          label="Ferdigmålte enheter"
          :value="`${completedUnitsToday}/${completedUnitsTarget}`"
          :icon="CheckCircle2"
          :variant="completedUnitsVariant"
          sub-label="En enhet er ferdig når den har 2 målinger i dag"
        />
        <OverviewCard label="Avvik registrert" :value="deviationCount" :icon="TriangleAlert"
                      variant="open"/>
        <OverviewCard
          label="Siste måling"
          :value="latestEntry ? `${latestEntry.temperature.toFixed(1)}°C` : '-'"
          :icon="Clock3"
          :sub-label="latestEntry ? `${formatDateTime(latestEntry.measuredAt)} • ${latestEntry.measuredBy}` : 'Ingen målinger ennå'"
          :value-class="latestEntry?.status === 'DEVIATION' ? 'val-red' : 'val-green'"
        />
      </section>

      <section class="workspace-grid">
        <TemperatureLogForm
          :active-appliances="activeAppliances"
          :selected-appliance-id="selectedApplianceId"
          :temperature-input="temperatureInput"
          :note="note"
          :registered-by-name="auth.user?.fullName ?? 'Innlogget bruker'"
          :selected-appliance="selectedAppliance"
          :form-status="formStatus"
          @update:selected-appliance-id="selectedApplianceId = $event"
          @update:temperature-input="temperatureInput = $event"
          @update:note="note = $event"
          @submit="submitTemperature"
        />

        <TemperatureLogTable
          :entries="pagedEntries"
          :total-entries="recentEntries.length"
          :can-manage="canManage"
          :selected-entry-ids="selectedEntryIds"
          :all-rows-selected="allRowsSelected"
          :is-mobile="isMobile"
          :pagination-summary="paginationSummary"
          :current-page="currentPage"
          :total-pages="totalPages"
          @toggle-entry="toggleEntrySelection"
          @toggle-all="toggleSelectAll"
          @delete-selected="deleteSelectedMeasurements"
          @prev-page="currentPage > 1 && currentPage--"
          @next-page="currentPage < totalPages && currentPage++"
        />
      </section>

      <FoodDeviationFormDialog
        :open="deviationDialogOpen"
        :members="memberOptions"
        :submitting="isCreatingDeviation"
        :prefill="deviationFormPrefill"
        @update:open="(v) => (deviationDialogOpen = v)"
        @create="handleCreateDeviation"
      />
    </div>
  </AppLayout>
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

.page-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 1rem;
  padding: 0 1rem 1rem;
}

.page-intro h1 {
  margin-top: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.page-intro p {
  margin-top: 0.25rem;
  color: hsl(var(--muted-foreground));
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.75rem;
}

.workspace-grid {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 0.75rem;
  align-items: start;
  min-width: 0;
}

@media (max-width: 1080px) {
  .page-intro h1 {
    font-size: 1.5rem;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .workspace-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page-content {
    padding: 0 0.75rem 0.75rem;
  }

  .page-header-inner {
    width: 100%;
    padding: 0 0.75rem;
  }
}
</style>

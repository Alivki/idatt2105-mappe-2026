<script setup lang="ts">
import {computed, ref} from 'vue'
import {Plus, Power, PowerOff, Refrigerator, TriangleAlert} from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import OverviewCard from '@/components/common/OverviewCard.vue'
import Button from '@/components/ui/button/Button.vue'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import {Separator} from '@/components/ui/separator'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import {SidebarTrigger} from '@/components/ui/sidebar'
import ApplianceCard from '@/components/temperature/ApplianceCard.vue'
import ApplianceFormDialog from '@/components/temperature/ApplianceFormDialog.vue'
import {
  getDefaultThreshold,
  useTemperatureMonitoring,
} from '@/composables/useTemperatureMonitoring'
import {
  useApplianceSorting,
  type ApplianceGroupOption,
  type ApplianceSortOption,
} from '@/composables/useApplianceSorting'
import type {
  TemperatureAppliance,
  TemperatureApplianceType,
  TemperatureThreshold
} from '@/types/temperature'

const {
  appliancesWithLastEntry,
  createAppliance,
  updateAppliance,
  deleteAppliance,
} = useTemperatureMonitoring()

const isCreateDialogOpen = ref(false)
const isEditDialogOpen = ref(false)
const editingAppliance = ref<TemperatureAppliance | null>(null)
const isDeleteDialogOpen = ref(false)
const appliancePendingDelete = ref<TemperatureAppliance | null>(null)

const {sortOption, groupOption, groupedAppliances} = useApplianceSorting(appliancesWithLastEntry)

const activeCount = computed(() => appliancesWithLastEntry.value.filter((i) => i.isActive).length)
const inactiveCount = computed(() => appliancesWithLastEntry.value.length - activeCount.value)
const withDeviationCount = computed(() => appliancesWithLastEntry.value.filter((i) => i.lastEntry?.status === 'DEVIATION').length)

const lastRegisteredLabel = computed(() => {
  const latest = appliancesWithLastEntry.value
    .filter((i) => i.lastEntry)
    .sort((a, b) => new Date(b.lastEntry?.measuredAt ?? 0).getTime() - new Date(a.lastEntry?.measuredAt ?? 0).getTime())[0]
  if (!latest?.lastEntry) return 'Ingen målinger registrert'
  const d = new Date(latest.lastEntry.measuredAt)
  return `Sist registrert: ${new Intl.DateTimeFormat('nb-NO', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(d)}`
})

function openCreateDialog() {
  isCreateDialogOpen.value = true
}

async function handleCreate(payload: {
  name: string;
  type: TemperatureApplianceType;
  threshold: TemperatureThreshold
}) {
  const created = await createAppliance({
    name: payload.name,
    type: payload.type,
    threshold: payload.threshold
  })
  if (created) isCreateDialogOpen.value = false
}

function openEditDialog(appliance: TemperatureAppliance) {
  editingAppliance.value = appliance
  isEditDialogOpen.value = true
}

async function handleEdit(payload: { name: string; threshold: TemperatureThreshold }) {
  if (!editingAppliance.value) return
  const updated = await updateAppliance(editingAppliance.value.id, {
    name: payload.name,
    threshold: payload.threshold
  })
  if (updated) {
    isEditDialogOpen.value = false
    editingAppliance.value = null
  }
}

async function toggleActive(appliance: TemperatureAppliance) {
  await updateAppliance(appliance.id, {isActive: !appliance.isActive})
}

function openDeleteDialog(appliance: TemperatureAppliance) {
  appliancePendingDelete.value = appliance
  isDeleteDialogOpen.value = true
}

async function confirmDelete() {
  if (!appliancePendingDelete.value) return
  await deleteAppliance(appliancePendingDelete.value.id)
  isDeleteDialogOpen.value = false
  appliancePendingDelete.value = null
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">Hvitevarer</span>
      </div>
    </header>

    <div class="page-content">
      <section class="page-intro">
        <div>
          <h1>Hvitevarer</h1>
          <p>Legg til, rediger og vedlikehold kjøleskap og frysere for temperaturmåling.</p>
        </div>
        <div>
          <Button @click="openCreateDialog">
            <Plus aria-hidden="true"/>
            Ny enhet
          </Button>
        </div>
      </section>

      <section class="overview-grid">
        <OverviewCard label="Aktive enheter" :value="activeCount" :icon="Power" variant="resolved"/>
        <OverviewCard label="Inaktive enheter" :value="inactiveCount" :icon="PowerOff"
                      variant="neutral"/>
        <OverviewCard label="Avvik sist målt" :value="withDeviationCount" :icon="TriangleAlert"
                      variant="open" :sub-label="lastRegisteredLabel"/>
      </section>

      <section class="controls-row">
        <div class="control-field">
          <span>Sortering</span>
          <Select :model-value="sortOption"
                  @update:model-value="(v) => (sortOption = v as ApplianceSortOption)">
            <SelectTrigger>
              <SelectValue/>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="NAME_ASC">Navn (A-Å)</SelectItem>
              <SelectItem value="NAME_DESC">Navn (Å-A)</SelectItem>
              <SelectItem value="CREATED_NEWEST">Nyeste opprettet</SelectItem>
              <SelectItem value="CREATED_OLDEST">Eldst opprettet</SelectItem>
              <SelectItem value="LAST_MEASURED_NEWEST">Sist målt (nyest)</SelectItem>
              <SelectItem value="LAST_MEASURED_OLDEST">Sist målt (eldst)</SelectItem>
              <SelectItem value="DEVIATION_FIRST">Avvik først</SelectItem>
            </SelectContent>
          </Select>
        </div>
        <div class="control-field">
          <span>Gruppering</span>
          <Select :model-value="groupOption"
                  @update:model-value="(v) => (groupOption = v as ApplianceGroupOption)">
            <SelectTrigger>
              <SelectValue/>
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="NONE">Ingen gruppering</SelectItem>
              <SelectItem value="TYPE">Grupper etter type</SelectItem>
              <SelectItem value="STATUS">Grupper etter aktiv/inaktiv</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </section>

      <section v-if="appliancesWithLastEntry.length === 0" class="empty-state">
        <div class="empty-state-inner">
          <div class="empty-state-icon">
            <Refrigerator :stroke-width="1.5" aria-hidden="true"/>
          </div>
          <div class="empty-state-text">
            <h2>Ingen hvitevarer registrert</h2>
            <p>Legg til kjøleskap og frysere for å komme i gang med temperaturovervåking.</p>
          </div>
          <Button @click="openCreateDialog">
            <Plus aria-hidden="true"/>
            Legg til hvitevare
          </Button>
        </div>
      </section>

      <section v-if="appliancesWithLastEntry.length > 0" class="groups-wrap">
        <section v-for="group in groupedAppliances" :key="group.key" class="group-section">
          <h2 v-if="group.label" class="group-title">{{ group.label }}</h2>
          <div class="device-grid">
            <ApplianceCard
              v-for="item in group.items"
              :key="item.id"
              :appliance="item"
              @edit="openEditDialog"
              @toggle-active="toggleActive"
              @delete="openDeleteDialog"
            />
          </div>
        </section>
      </section>
    </div>

    <AlertDialog v-model:open="isDeleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Slette enhet?</AlertDialogTitle>
          <AlertDialogDescription>
            Er du sikker på at du vil slette {{ appliancePendingDelete?.name }}? Alle tilhørende
            temperaturregistreringer for denne enheten blir også slettet.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Avbryt</AlertDialogCancel>
          <AlertDialogAction variant="destructive" @click="confirmDelete">Slett enhet
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <ApplianceFormDialog
      :open="isCreateDialogOpen"
      mode="create"
      :initial-min="getDefaultThreshold('FRIDGE').min"
      :initial-max="getDefaultThreshold('FRIDGE').max"
      @update:open="(v) => (isCreateDialogOpen = v)"
      @submit="handleCreate"
    />

    <ApplianceFormDialog
      :open="isEditDialogOpen"
      mode="edit"
      :initial-name="editingAppliance?.name ?? ''"
      :initial-type="editingAppliance?.type ?? 'FRIDGE'"
      :initial-min="editingAppliance?.threshold.min ?? 0"
      :initial-max="editingAppliance?.threshold.max ?? 4"
      @update:open="(v) => (isEditDialogOpen = v)"
      @submit="handleEdit"
    />
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

.page-intro {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.page-intro h1 {
  margin-top: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.2;
}

.page-intro p {
  margin-top: 0.375rem;
  color: hsl(var(--muted-foreground));
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.75rem;
}

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.75rem;
}

.controls-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.control-field {
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.control-field span {
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
}

.groups-wrap {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.group-section {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.group-title {
  font-size: 1rem;
  font-weight: 700;
  color: hsl(var(--foreground));
}

.empty-state {
  display: flex;
  min-height: 220px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  border: 2px dashed hsl(var(--muted-foreground) / 0.2);
  background: hsl(var(--muted) / 0.3);
  padding: 2rem;
}

.empty-state-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  text-align: center;
}

.empty-state-icon {
  display: flex;
  height: 4rem;
  width: 4rem;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  background-color: hsl(var(--muted));
}

.empty-state-icon :deep(svg) {
  width: 2rem;
  height: 2rem;
  color: hsl(var(--muted-foreground));
}

.empty-state-text h2 {
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.empty-state-text p {
  max-width: 24rem;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  margin-top: 0.25rem;
}

@media (max-width: 920px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .device-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .page-intro {
    flex-direction: column;
    align-items: stretch;
  }

  .page-intro > div:last-child {
    align-self: flex-end;
  }

  .controls-row {
    flex-direction: column;
  }

  .page-intro h1 {
    font-size: 1.5rem;
  }
}

@media (max-width: 600px) {
  .device-grid {
    grid-template-columns: 1fr;
  }
}
</style>

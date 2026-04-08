<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { z } from 'zod'
import { MoreVertical, Pencil, Plus, Power, PowerOff, Refrigerator, Snowflake, Trash2, TriangleAlert } from 'lucide-vue-next'
import AppLayout from '@/components/layout/AppLayout.vue'
import OverviewCard from '@/components/common/OverviewCard.vue'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogContent from '@/components/ui/dialog/DialogContent.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import Input from '@/components/ui/input/Input.vue'
import { Separator } from '@/components/ui/separator'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import { SidebarTrigger } from '@/components/ui/sidebar'
import {
  formatThreshold,
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
  TemperatureThreshold,
} from '@/types/temperature'

const {
  appliancesWithLastEntry,
  createAppliance,
  updateAppliance,
  deleteAppliance,
} = useTemperatureMonitoring()

const isCreateDialogOpen = ref(false)
const isEditDialogOpen = ref(false)
const editingApplianceId = ref<number | null>(null)
const isDeleteDialogOpen = ref(false)
const appliancePendingDelete = ref<TemperatureAppliance | null>(null)

const createName = ref('')
const createType = ref<TemperatureApplianceType>('FRIDGE')
const createMin = ref<number>(getDefaultThreshold('FRIDGE').min)
const createMax = ref<number>(getDefaultThreshold('FRIDGE').max)

const editName = ref('')
const editType = ref<TemperatureApplianceType>('FRIDGE')
const editMin = ref(0)
const editMax = ref(4)

const createErrors = ref<Record<string, string>>({})
const editErrors = ref<Record<string, string>>({})
const applianceNameSchema = z.string().min(1, 'Navn er påkrevd')
const {
  sortOption,
  groupOption,
  groupedAppliances,
} = useApplianceSorting(appliancesWithLastEntry)

watch(createType, (nextType) => {
  const defaults = getDefaultThreshold(nextType)
  createMin.value = defaults.min
  createMax.value = defaults.max
})

const activeCount = computed(() => {
  return appliancesWithLastEntry.value.filter((item) => item.isActive).length
})

const inactiveCount = computed(() => {
  return appliancesWithLastEntry.value.length - activeCount.value
})

const withDeviationCount = computed(() => {
  return appliancesWithLastEntry.value.filter((item) => item.lastEntry?.status === 'DEVIATION').length
})

const lastRegisteredLabel = computed(() => {
  const latest = appliancesWithLastEntry.value
    .filter((item) => item.lastEntry)
    .sort((a, b) => {
      const aTime = new Date(a.lastEntry?.measuredAt ?? 0).getTime()
      const bTime = new Date(b.lastEntry?.measuredAt ?? 0).getTime()
      return bTime - aTime
    })[0]

  if (!latest?.lastEntry) {
    return 'Ingen målinger registrert'
  }

  return `Sist registrert: ${toShortDateTime(latest.lastEntry.measuredAt)}`
})

function toTypeLabel(type: TemperatureApplianceType): string {
  return type === 'FRIDGE' ? 'Kjøleskap' : 'Fryser'
}

function toShortDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '-'
  }

  return new Intl.DateTimeFormat('nb-NO', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
}

function openCreateDialog(): void {
  createName.value = ''
  createType.value = 'FRIDGE'
  createMin.value = getDefaultThreshold('FRIDGE').min
  createMax.value = getDefaultThreshold('FRIDGE').max
  createErrors.value = {}
  isCreateDialogOpen.value = true
}

async function submitCreate(): Promise<void> {
  const newErrors: Record<string, string> = {}
  const name = createName.value.trim()

  const nameResult = applianceNameSchema.safeParse(name)
  if (!nameResult.success) newErrors.name = nameResult.error.issues[0]?.message ?? ''

  const threshold: TemperatureThreshold = {
    min: Number(createMin.value),
    max: Number(createMax.value),
  }

  if (threshold.min >= threshold.max) newErrors.threshold = 'Min må være lavere enn maks'

  if (Object.keys(newErrors).length > 0) {
    createErrors.value = newErrors
    return
  }
  createErrors.value = {}

  const created = await createAppliance({
    name,
    type: createType.value,
    threshold,
  })

  if (!created) {
    return
  }

  isCreateDialogOpen.value = false
}

function openEditDialog(appliance: TemperatureAppliance): void {
  editingApplianceId.value = appliance.id
  editName.value = appliance.name
  editType.value = appliance.type
  editMin.value = appliance.threshold.min
  editMax.value = appliance.threshold.max
  editErrors.value = {}
  isEditDialogOpen.value = true
}

async function submitEdit(): Promise<void> {
  if (!editingApplianceId.value) return
  const newErrors: Record<string, string> = {}
  const name = editName.value.trim()

  const nameResult = applianceNameSchema.safeParse(name)
  if (!nameResult.success) newErrors.name = nameResult.error.issues[0]?.message ?? ''

  const threshold: TemperatureThreshold = {
    min: Number(editMin.value),
    max: Number(editMax.value),
  }

  if (threshold.min >= threshold.max) newErrors.threshold = 'Min må være lavere enn maks'

  if (Object.keys(newErrors).length > 0) {
    editErrors.value = newErrors
    return
  }
  editErrors.value = {}

  const updated = await updateAppliance(editingApplianceId.value, {
    name,
    threshold,
  })

  if (!updated) {
    return
  }

  isEditDialogOpen.value = false
  editingApplianceId.value = null
}

async function toggleActive(appliance: TemperatureAppliance): Promise<void> {
  await updateAppliance(appliance.id, {
    isActive: !appliance.isActive,
  })
}

async function removeApplianceById(applianceId: number): Promise<void> {
  await deleteAppliance(applianceId)
}

function openDeleteDialog(appliance: TemperatureAppliance): void {
  appliancePendingDelete.value = appliance
  isDeleteDialogOpen.value = true
}

async function confirmDeleteAppliance(): Promise<void> {
  if (!appliancePendingDelete.value) {
    return
  }

  await removeApplianceById(appliancePendingDelete.value.id)
  isDeleteDialogOpen.value = false
  appliancePendingDelete.value = null
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger />
        <Separator orientation="vertical" class="header-separator" />
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
            <Plus aria-hidden="true" />
            Ny enhet
          </Button>
        </div>
      </section>

      <section class="overview-grid">
        <OverviewCard label="Aktive enheter" :value="activeCount" :icon="Power" variant="resolved" />
        <OverviewCard label="Inaktive enheter" :value="inactiveCount" :icon="PowerOff" variant="neutral" />
        <OverviewCard label="Avvik sist målt" :value="withDeviationCount" :icon="TriangleAlert" variant="open" :sub-label="lastRegisteredLabel" />
      </section>

      <section class="controls-row">
        <div class="control-field">
          <span>Sortering</span>
          <Select :model-value="sortOption" @update:model-value="(v) => (sortOption = v as ApplianceSortOption)">
            <SelectTrigger>
              <SelectValue />
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
          <Select :model-value="groupOption" @update:model-value="(v) => (groupOption = v as ApplianceGroupOption)">
            <SelectTrigger>
              <SelectValue />
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
            <Refrigerator :stroke-width="1.5" aria-hidden="true" />
          </div>
          <div class="empty-state-text">
            <h2>Ingen hvitevarer registrert</h2>
            <p>Legg til kjøleskap og frysere for å komme i gang med temperaturovervåking.</p>
          </div>
          <Button @click="openCreateDialog">
            <Plus aria-hidden="true" />
            Legg til hvitevare
          </Button>
        </div>
      </section>

      <section v-if="appliancesWithLastEntry.length > 0" class="groups-wrap">
        <section v-for="group in groupedAppliances" :key="group.key" class="group-section">
          <h2 v-if="group.label" class="group-title">{{ group.label }}</h2>

          <div class="device-grid">
            <article
              v-for="item in group.items"
              :key="item.id"
              :class="['device-card', { 'device-card--inactive': !item.isActive }]"
            >
              <div class="device-card-head">
                <div class="device-icon-wrap">
                  <Refrigerator v-if="item.type === 'FRIDGE'" aria-hidden="true" />
                  <Snowflake v-else aria-hidden="true" />
                </div>
                <div>
                  <h3>{{ item.name }}</h3>
                </div>
                <div class="device-meta-actions">
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button variant="ghost" size="icon-sm" class="actions-trigger" aria-label="Åpne handlinger">
                        <MoreVertical :size="18" aria-hidden="true" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" :side-offset="4">
                      <DropdownMenuItem @click="openEditDialog(item)">
                        <Pencil :size="16" aria-hidden="true" />
                        Rediger
                      </DropdownMenuItem>
                      <DropdownMenuItem @click="toggleActive(item)">
                        <PowerOff v-if="item.isActive" :size="16" aria-hidden="true" />
                        <Power v-else :size="16" aria-hidden="true" />
                        {{ item.isActive ? 'Sett inaktiv' : 'Aktiver' }}
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem class="menu-item--danger" @click="openDeleteDialog(item)">
                        <Trash2 :size="16" aria-hidden="true" />
                        Slett enhet
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </div>

              <Badge class="device-status-badge" :tone="item.isActive ? 'ok' : 'neutral'">
                {{ item.isActive ? 'Aktiv' : 'Inaktiv' }}
              </Badge>

              <dl class="device-facts">
                <div>
                  <dt>Grense</dt>
                  <dd>{{ formatThreshold(item.threshold) }}</dd>
                </div>
                <div>
                  <dt>Siste temp</dt>
                  <dd v-if="item.lastEntry">{{ item.lastEntry.temperature.toFixed(1) }}°C</dd>
                  <dd v-else>-</dd>
                </div>
                <div>
                  <dt>Sist registrert</dt>
                  <dd v-if="item.lastEntry">{{ toShortDateTime(item.lastEntry.measuredAt) }}</dd>
                  <dd v-else>Ingen måling</dd>
                </div>
                <div>
                  <dt>Status</dt>
                  <dd>
                    <Badge v-if="item.lastEntry" :tone="item.lastEntry.status === 'OK' ? 'ok' : 'danger'">
                      {{ item.lastEntry.status === 'OK' ? 'OK' : 'Avvik' }}
                    </Badge>
                    <span v-else class="muted">Ingen data</span>
                  </dd>
                </div>
              </dl>

            </article>
          </div>
        </section>
      </section>
    </div>

    <AlertDialog v-model:open="isDeleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Slette enhet?</AlertDialogTitle>
          <AlertDialogDescription>
            Er du sikker på at du vil slette {{ appliancePendingDelete?.name }}? Alle tilhørende temperaturregistreringer for denne enheten blir også slettet.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Avbryt</AlertDialogCancel>
          <AlertDialogAction variant="destructive" @click="confirmDeleteAppliance">
            Slett enhet
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <Dialog :open="isCreateDialogOpen" @update:open="(v) => (isCreateDialogOpen = v)">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Legg til hvitevare</DialogTitle>
          <DialogDescription>
            Type setter standard grense automatisk. Du kan endre verdiene før lagring.
          </DialogDescription>
        </DialogHeader>

        <div class="form-grid">
          <label :class="['field', { 'field--error': createErrors.name }]">
            <span>Navn</span>
            <Input v-model="createName" placeholder="For eksempel: Kjøleskap kjøkken" />
            <p v-if="createErrors.name" class="field-error" role="alert">{{ createErrors.name }}</p>
          </label>

          <div class="field">
            <span>Type</span>
            <Select :model-value="createType" @update:model-value="(v) => (createType = v as TemperatureApplianceType)">
              <SelectTrigger>
                <SelectValue placeholder="Velg type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="FRIDGE">Kjøleskap</SelectItem>
                <SelectItem value="FREEZER">Fryser</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <label class="field">
            <span>Min temperatur (°C)</span>
            <Input v-model="createMin" type="number" />
          </label>

          <label :class="['field', { 'field--error': createErrors.threshold }]">
            <span>Maks temperatur (°C)</span>
            <Input v-model="createMax" type="number" />
            <p v-if="createErrors.threshold" class="field-error" role="alert">{{ createErrors.threshold }}</p>
          </label>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="isCreateDialogOpen = false">Avbryt</Button>
          <Button @click="submitCreate">Lagre enhet</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>

    <Dialog :open="isEditDialogOpen" @update:open="(v) => (isEditDialogOpen = v)">
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Rediger hvitevare</DialogTitle>
          <DialogDescription>
            Oppdater navn og temperaturgrense for valgt enhet.
          </DialogDescription>
        </DialogHeader>

        <div class="form-grid">
          <label :class="['field', { 'field--error': editErrors.name }]">
            <span>Navn</span>
            <Input v-model="editName" placeholder="Navn" />
            <p v-if="editErrors.name" class="field-error" role="alert">{{ editErrors.name }}</p>
          </label>

          <label class="field">
            <span>Type</span>
            <Input :model-value="toTypeLabel(editType)" disabled />
          </label>

          <label class="field">
            <span>Min temperatur (°C)</span>
            <Input v-model="editMin" type="number" />
          </label>

          <label :class="['field', { 'field--error': editErrors.threshold }]">
            <span>Maks temperatur (°C)</span>
            <Input v-model="editMax" type="number" />
            <p v-if="editErrors.threshold" class="field-error" role="alert">{{ editErrors.threshold }}</p>
          </label>
        </div>

        <DialogFooter>
          <Button variant="outline" @click="isEditDialogOpen = false">Avbryt</Button>
          <Button @click="submitEdit">Lagre endringer</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
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

.device-card {
  border-radius: 14px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  padding: 0.85rem;
  display: flex;
  flex-direction: column;
  aspect-ratio: 1;
}

.device-card--inactive {
  opacity: 0.74;
}

.device-card-head {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.device-meta-actions {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  margin-left: auto;
  flex-shrink: 0;
}

.device-icon-wrap {
  display: flex;
  height: 2rem;
  width: 2rem;
  align-items: center;
  justify-content: center;
  border-radius: 0.5rem;
  background: var(--brand-soft);
  color: var(--brand);
  flex-shrink: 0;
}

.device-icon-wrap :deep(svg) {
  width: 1rem;
  height: 1rem;
}

.device-card-head h3 {
  font-size: 0.95rem;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.device-card-head p {
  display: none;
}

.device-status-badge {
  margin-top: 0.5rem;
  align-self: flex-start;
}

.device-facts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  gap: 0;
  flex: 1;
  margin-top: 0.65rem;
  border-radius: 0.5rem;
  overflow: hidden;
}

.device-facts > div {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 0.5rem 0.35rem;
}

.device-facts dt {
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: hsl(var(--muted-foreground));
}

.device-facts dd {
  margin-top: 0.2rem;
  font-size: 0.85rem;
  font-weight: 600;
}

.muted {
  color: hsl(var(--muted-foreground));
  font-size: 0.85rem;
}

.actions-trigger {
  margin-left: 0;
}

:deep(.menu-item--danger) {
  color: hsl(var(--destructive));
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field span {
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
}

.field-error {
  color: hsl(var(--destructive));
  font-size: 0.8rem;
  margin-top: 2px;
}

.field--error :deep(.input),
.field--error :deep(.select-trigger) {
  border-color: hsl(var(--destructive));
}

.empty-state {
  display: flex; min-height: 220px;
  flex-direction: column; align-items: center; justify-content: center;
  border-radius: 1rem;
  border: 2px dashed hsl(var(--muted-foreground) / 0.2);
  background: hsl(var(--muted) / 0.3);
  padding: 2rem;
}

.empty-state-inner { display: flex; flex-direction: column; align-items: center; gap: 1rem; text-align: center; }

.empty-state-icon {
  display: flex; height: 4rem; width: 4rem; align-items: center; justify-content: center;
  border-radius: var(--radius-lg); background-color: hsl(var(--muted));
}

.empty-state-icon :deep(svg) { width: 2rem; height: 2rem; color: hsl(var(--muted-foreground)); }

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
  }

  .controls-row {
    flex-direction: column;
  }

  .page-intro h1 {
    font-size: 1.5rem;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>

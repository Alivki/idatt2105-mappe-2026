<script setup lang="ts">
import axios from 'axios'
import { ClipboardCheck, ListChecks, Clock3, CircleCheckBig, CircleDashed } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from 'vue-sonner'
import AppLayout from '@/components/layout/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import ChecklistCard from '@/components/checklists/ChecklistCard.vue'
import OverviewCard from '@/components/common/OverviewCard.vue'
import ChecklistFormDialog from '@/components/checklists/ChecklistFormDialog.vue'

import {
  useChecklistsQuery,
  useCreateChecklistMutation,
  useDeleteChecklistMutation,
  useSetChecklistCompletionMutation,
  useUpdateChecklistMutation,
} from '@/composables/useChecklists'
import { useAuthStore } from '@/stores/auth'
import type {
  Checklist,
  ChecklistFrequency,
  CreateChecklistRequest,
  UpdateChecklistRequest,
} from '@/types/checklist'

type FrequencyFilter = 'ALL' | ChecklistFrequency

const router = useRouter()
const auth = useAuthStore()

const checklistQuery = useChecklistsQuery()
const createChecklist = useCreateChecklistMutation()
const updateChecklist = useUpdateChecklistMutation()
const deleteChecklist = useDeleteChecklistMutation()
const setChecklistCompletion = useSetChecklistCompletionMutation()

const activeFilter = ref<FrequencyFilter>('ALL')

const checklistDialogOpen = ref(false)
const checklistDialogMode = ref<'create' | 'edit'>('create')
const activeChecklist = ref<Checklist | null>(null)

const canManage = computed(() => auth.role === 'ADMIN' || auth.role === 'MANAGER')
const canComplete = computed(() => !!auth.role)

const checklists = computed(() => checklistQuery.data.value ?? [])

const stats = computed(() => {
  const list = checklists.value
  return {
    total: list.length,
    completed: list.filter((c) => c.status === 'COMPLETED').length,
    inProgress: list.filter((c) => c.status === 'IN_PROGRESS').length,
    notStarted: list.filter((c) => c.status === 'NOT_STARTED').length,
  }
})

const frequencyOrder: ChecklistFrequency[] = ['DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY']

const filters: Array<{ label: string; value: FrequencyFilter }> = [
  { label: 'Alle', value: 'ALL' },
  { label: 'Daglig', value: 'DAILY' },
  { label: 'Ukentlig', value: 'WEEKLY' },
  { label: 'Månedlig', value: 'MONTHLY' },
  { label: 'Årlig', value: 'YEARLY' },
]

const groupedChecklists = computed(() => {
  const source =
    activeFilter.value === 'ALL'
      ? checklists.value
      : checklists.value.filter((entry) => entry.frequency === activeFilter.value)

  return frequencyOrder
    .map((frequency) => {
      const items = source.filter((entry) => entry.frequency === frequency)
      return {
        frequency,
        label: sectionHeadingLabel(frequency),
        items,
      }
    })
    .filter((section) => section.items.length > 0)
})

function sectionHeadingLabel(frequency: ChecklistFrequency): string {
  const now = new Date()
  const dateLabel = new Intl.DateTimeFormat('nb-NO', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(now)
  const monthLabel = new Intl.DateTimeFormat('nb-NO', {
    month: 'long',
    year: 'numeric',
  }).format(now)

  switch (frequency) {
    case 'DAILY':
      return `Daglige sjekklister - ${dateLabel}`
    case 'WEEKLY':
      return `Ukentlige sjekklister - uke ${getIsoWeek(now)}`
    case 'MONTHLY':
      return `Månedlige sjekklister - ${monthLabel}`
    case 'YEARLY':
      return `Årlige sjekklister - ${now.getFullYear()}`
    default:
      return 'Sjekklister'
  }
}

function getIsoWeek(date: Date): number {
  const target = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()))
  const day = target.getUTCDay() || 7
  target.setUTCDate(target.getUTCDate() + 4 - day)
  const yearStart = new Date(Date.UTC(target.getUTCFullYear(), 0, 1))
  return Math.ceil((((target.getTime() - yearStart.getTime()) / 86400000) + 1) / 7)
}

function openCreateChecklistDialog() {
  checklistDialogMode.value = 'create'
  activeChecklist.value = null
  checklistDialogOpen.value = true
}

function openEditChecklistDialog(checklist: Checklist) {
  checklistDialogMode.value = 'edit'
  activeChecklist.value = checklist
  checklistDialogOpen.value = true
}

async function handleCreateChecklist(payload: CreateChecklistRequest) {
  try {
    await createChecklist.mutateAsync(payload)
    checklistDialogOpen.value = false
    toast.success('Sjekkliste opprettet')
  } catch (error) {
    handleMutationError(error, 'Kunne ikke opprette sjekkliste')
  }
}

async function handleUpdateChecklist(payload: { checklistId: number; data: UpdateChecklistRequest }) {
  try {
    await updateChecklist.mutateAsync({
      checklistId: payload.checklistId,
      payload: payload.data,
    })
    checklistDialogOpen.value = false
    toast.success('Sjekkliste oppdatert')
  } catch (error) {
    handleMutationError(error, 'Kunne ikke oppdatere sjekkliste')
  }
}

async function handleDeleteChecklist(checklistId: number) {
  try {
    await deleteChecklist.mutateAsync(checklistId)
    toast.success('Sjekkliste slettet')
  } catch (error) {
    handleMutationError(error, 'Kunne ikke slette sjekkliste')
  }
}

function handleOpenChecklist(checklist: Checklist) {
  router.push(`/sjekklister/${checklist.id}`)
}

async function handleToggleChecklistCompleted(payload: { checklistId: number; completed: boolean }) {
  try {
    await setChecklistCompletion.mutateAsync(payload)
  } catch (error) {
    handleMutationError(error, 'Kunne ikke oppdatere sjekklistestatus')
  }
}

function handleMutationError(error: unknown, fallbackMessage: string) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.error?.message
    if (typeof message === 'string' && message.trim().length > 0) {
      toast.error(message)
      return
    }
  }

  toast.error(fallbackMessage)
}
</script>

<template>
  <AppLayout active-menu-item="Sjekklister">
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger />
        <Separator orientation="vertical" class="header-separator" />
        <span class="page-title">Sjekklister</span>
      </div>
    </header>

    <div class="page-content">
      <section class="header-row">
        <div>
          <h1>Sjekklister</h1>
          <p>Dynamiske rutiner for daglig drift og internkontroll.</p>
        </div>

        <Button v-if="canManage" @click="openCreateChecklistDialog">
          + Ny sjekkliste
        </Button>
      </section>

      <!-- Stats cards -->
      <section class="cards-section" aria-label="Sjekklisteoversikt">
        <OverviewCard label="Totalt sjekklister" :value="stats.total" :icon="ClipboardCheck" />
        <OverviewCard label="Fullført" :value="stats.completed" :icon="CircleCheckBig" variant="resolved" />
        <OverviewCard label="Under arbeid" :value="stats.inProgress" :icon="Clock3" variant="in-progress" />
        <OverviewCard label="Ikke startet" :value="stats.notStarted" :icon="CircleDashed" variant="open" />
      </section>

      <section class="filters-row" aria-label="Filtrer frekvens">
        <Button
          v-for="filter in filters"
          :key="filter.value"
          :class="activeFilter === filter.value ? 'filter-button filter-button--active' : 'filter-button'"
          :aria-pressed="activeFilter === filter.value"
          variant="outline"
          size="sm"
          @click="activeFilter = filter.value"
        >
          {{ filter.label }}
        </Button>
      </section>

      <section class="list-section">
        <div v-if="checklistQuery.isLoading.value" class="skeleton-list">
          <div v-for="n in 4" :key="n" class="skeleton-card">
            <div class="skeleton-line skeleton-line--title"></div>
            <div class="skeleton-line skeleton-line--short"></div>
          </div>
        </div>
        <div v-else-if="checklistQuery.isError.value" class="empty-state">
          <div class="empty-state-inner">
            <div class="empty-state-icon">
              <ClipboardCheck :stroke-width="1.5" aria-hidden="true" />
            </div>
            <div class="empty-state-text">
              <h2>Kunne ikke hente sjekklister</h2>
              <p>Noe gikk galt under lasting av sjekklister. Prøv igjen senere.</p>
            </div>
          </div>
        </div>

        <div v-else-if="checklists.length === 0" class="empty-state">
          <div class="empty-state-inner">
            <div class="empty-state-icon empty-state-icon--green">
              <ListChecks :stroke-width="1.5" aria-hidden="true" />
            </div>
            <div class="empty-state-text">
              <h2>Ingen sjekklister ennå</h2>
              <p>Vi anbefaler å bruke HACCP-veiviseren for å sette opp sjekklistene dine. Veiviseren stiller noen spørsmål om virksomheten og genererer skreddersydde sjekklister basert på Mattilsynets krav.</p>
            </div>
            <div class="empty-state-actions">
              <Button v-if="canManage" @click="router.push('/haccp-oppsett')">
                Gå til HACCP-oppsett
              </Button>
              <Button v-if="canManage" variant="outline" @click="openCreateChecklistDialog">
                + Opprett manuelt
              </Button>
            </div>
          </div>
        </div>

        <div v-else-if="groupedChecklists.length === 0" class="empty-state">
          <div class="empty-state-inner">
            <div class="empty-state-icon">
              <ClipboardCheck :stroke-width="1.5" aria-hidden="true" />
            </div>
            <div class="empty-state-text">
              <h2>Ingen sjekklister i valgt frekvens</h2>
              <p>Prøv å endre filteret for å se sjekklister med en annen frekvens.</p>
            </div>
          </div>
        </div>

        <div v-else class="sections-wrap">
          <section v-for="section in groupedChecklists" :key="section.frequency" class="frequency-section">
            <div class="section-divider">
              <span>{{ section.label }}</span>
            </div>

            <div class="checklist-grid">
              <ChecklistCard
                v-for="checklist in section.items"
                :key="checklist.id"
                :checklist="checklist"
                :can-manage="canManage"
                :can-complete="canComplete"
                @open="handleOpenChecklist"
                @edit-checklist="openEditChecklistDialog"
                @delete-checklist="handleDeleteChecklist"
                @toggle-checklist-completed="handleToggleChecklistCompleted"
              />
            </div>
          </section>
        </div>
      </section>
    </div>

    <ChecklistFormDialog
      v-model:open="checklistDialogOpen"
      :mode="checklistDialogMode"
      :initial-checklist="activeChecklist"
      :submitting="createChecklist.isPending.value || updateChecklist.isPending.value"
      @create="handleCreateChecklist"
      @update="handleUpdateChecklist"
    />

  </AppLayout>
</template>

<style scoped>
.page-header { display: flex; height: 4rem; flex-shrink: 0; align-items: center; }
.page-header-inner { display: flex; align-items: center; gap: 0.5rem; padding: 0 1rem; }
.header-separator { height: 1rem !important; width: 1px !important; margin-right: 0.5rem; }
.page-title { font-weight: 500; color: hsl(var(--sidebar-primary, 245 43% 52%)); }
.page-content { display: flex; flex: 1; flex-direction: column; gap: 1rem; padding: 0 1rem 1rem; }

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.header-row p {
  margin-top: 6px;
  color: var(--text-secondary);
}

.cards-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.filters-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-button {
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-pill);
  background: hsl(var(--muted));
  padding: 8px 16px;
  cursor: pointer;
  font-size: 0.95rem;
  color: hsl(var(--foreground));
}

.filter-button--active {
  border-color: var(--brand);
  background: var(--brand-soft);
  color: color-mix(in srgb, var(--brand) 80%, black);
  font-weight: 600;
}

.state-line {
  padding: 14px;
  border-radius: var(--radius-md);
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
}

.state-line--danger {
  background: var(--red-soft);
  color: var(--red);
}

.empty-state {
  position: relative;
  display: flex;
  min-height: 320px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  border: 2px dashed hsl(var(--muted-foreground) / 0.2);
  background: hsl(var(--muted) / 0.3);
  padding: 2rem;
}

.empty-state-inner {
  position: relative;
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
  border-radius: 1rem;
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
  max-width: 28rem;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  margin-top: 0.25rem;
}

.empty-state-icon--green {
  background-color: var(--green-soft);
  box-shadow: 0 0 0 4px var(--green-soft);
}

.empty-state-icon--green :deep(svg) {
  color: var(--green);
}

.empty-state-actions {
  display: flex;
  gap: 0.5rem;
}

.checklist-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sections-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.frequency-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-divider {
  padding-top: 2px;
}

.section-divider span {
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-size: 0.96rem;
  color: hsl(var(--foreground));
  font-weight: 700;
}

@media (max-width: 760px) {
  .page-header-inner {
    width: 100%;
    padding: 0 0.75rem;
  }

  .page-content {
    padding: 0 0.75rem 0.75rem;
    gap: 0.75rem;
  }

  .header-row {
    flex-direction: column;
  }

  h1 {
    font-size: 1.5rem;
  }

  .cards-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filters-row {
    flex-wrap: nowrap;
    overflow-x: auto;
    padding-bottom: 0.25rem;
    scrollbar-width: none;
  }

  .filters-row::-webkit-scrollbar {
    display: none;
  }

  .filter-button {
    flex: 0 0 auto;
  }

  .empty-state-actions {
    width: 100%;
    flex-direction: column;
  }

  .empty-state-actions > * {
    width: 100%;
  }

  .section-divider span {
    font-size: 0.85rem;
  }
}

.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card { padding: 16px; border-radius: var(--radius-lg); border: 1px solid hsl(var(--border)); background: var(--card-bg); }
.skeleton-line { height: 14px; border-radius: 6px; background: hsl(var(--muted)); animation: shimmer 1.4s ease-in-out infinite; }
.skeleton-line--title { width: 55%; margin-bottom: 10px; }
.skeleton-line--short { width: 35%; }
@keyframes shimmer { 0%,100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>

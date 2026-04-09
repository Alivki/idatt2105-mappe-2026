<script setup lang="ts">
import axios from 'axios'
import {computed, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {ArrowLeft, AlertTriangle, Pencil, Trash2, Shield, Zap, Search} from 'lucide-vue-next'
import {toast} from 'vue-sonner'
import AppLayout from '@/components/layout/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import Badge from '@/components/ui/badge/Badge.vue'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import DeviationStatusWorkflow from '@/components/deviations/DeviationStatusWorkflow.vue'
import DeviationDetailSidebar from '@/components/deviations/DeviationDetailSidebar.vue'
import FoodDeviationFormDialog from '@/components/deviations/FoodDeviationFormDialog.vue'
import AlcoholDeviationFormDialog from '@/components/deviations/AlcoholDeviationFormDialog.vue'
import {useAuthStore} from '@/stores/auth'
import {useCanManage} from '@/composables/useCanManage'
import {useMembersQuery} from '@/composables/useMembers'
import {
  useFoodDeviationsQuery,
  useUpdateFoodDeviationMutation,
  useDeleteFoodDeviationMutation,
} from '@/composables/useFoodDeviations'
import {
  useAlcoholDeviationsQuery,
  useUpdateAlcoholDeviationMutation,
  useDeleteAlcoholDeviationMutation,
} from '@/composables/useAlcoholDeviations'
import {usePenaltyPointsQuery} from '@/composables/usePenaltyPoints'
import type {
  FoodDeviation,
  AlcoholDeviation,
  FoodDeviationType,
  FoodDeviationStatus,
  AlcoholDeviationStatus,
  AlcoholReportSource,
  AlcoholDeviationType,
  AlcoholCausalAnalysis,
  UpdateFoodDeviationRequest,
  UpdateAlcoholDeviationRequest,
} from '@/types/deviation'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const canManage = useCanManage()
const membersQuery = useMembersQuery(canManage)

const deviationModule = computed(() => route.params.module as 'mat' | 'alkohol')
const deviationId = computed(() => Number(route.params.id))

const foodQuery = useFoodDeviationsQuery()
const alcoholQuery = useAlcoholDeviationsQuery()
const penaltyQuery = usePenaltyPointsQuery()
const updateFood = useUpdateFoodDeviationMutation()
const deleteFood = useDeleteFoodDeviationMutation()
const updateAlcohol = useUpdateAlcoholDeviationMutation()
const deleteAlcohol = useDeleteAlcoholDeviationMutation()

const editFoodOpen = ref(false)
const editAlcoholOpen = ref(false)
const deleteDialogOpen = ref(false)
const statusChanging = ref(false)

const isLoading = computed(() =>
  deviationModule.value === 'mat' ? foodQuery.isLoading.value : alcoholQuery.isLoading.value,
)

const foodDeviation = computed<FoodDeviation | null>(() =>
  deviationModule.value === 'mat'
    ? (foodQuery.data.value ?? []).find((d) => d.id === deviationId.value) ?? null
    : null,
)

const alcoholDeviation = computed<AlcoholDeviation | null>(() =>
  deviationModule.value === 'alkohol'
    ? (alcoholQuery.data.value ?? []).find((d) => d.id === deviationId.value) ?? null
    : null,
)

const notFound = computed(() =>
  !isLoading.value && !foodDeviation.value && !alcoholDeviation.value,
)

const memberOptions = computed(() => {
  if (canManage.value) {
    return (membersQuery.data.value ?? []).map((m) => ({
      userId: m.userId,
      label: `${m.userFullName} (${m.role})`,
    }))
  }
  if (!auth.user) return []
  return [{userId: auth.user.id, label: `${auth.user.fullName} (Deg)`}]
})

const foodTypeLabel: Record<FoodDeviationType, string> = {
  TEMPERATUR: 'Temperatur', RENHOLD: 'Renhold', PERSONLIG_HYGIENE: 'Personlig hygiene',
  ALLERGEN: 'Allergen', SKADEDYR: 'Skadedyr', MOTTAKSKONTROLL: 'Mottakskontroll', ANNET: 'Annet',
}

const foodStatusLabel: Record<FoodDeviationStatus, string> = {
  OPEN: 'Åpen', UNDER_TREATMENT: 'Under behandling', CLOSED: 'Lukket',
}

const alcoholStatusLabel: Record<AlcoholDeviationStatus, string> = {
  OPEN: 'Åpen', UNDER_TREATMENT: 'Under behandling', CLOSED: 'Lukket',
}

const sourceLabel: Record<AlcoholReportSource, string> = {
  EGENRAPPORT: 'Egenrapport', SJENKEKONTROLL: 'Skjenkekontroll', POLITIRAPPORT: 'Politirapport',
}

const alcoholTypeLabel: Record<AlcoholDeviationType, string> = {
  SKJENKING_MINDREAARIGE: 'Skjenking til mindreårige',
  BRUDD_BISTANDSPLIKT: 'Brudd på bistandsplikt',
  UFORSVARLIG_DRIFT: 'Uforsvarlig drift',
  HINDRING_KONTROLL: 'Hindring av kontroll',
  SKJENKING_APENBART_BERUSET: 'Skjenking til åpenbart beruset',
  BRUDD_SJENKETIDER: 'Brudd på skjenketider',
  BRENNEVIN_18_19: 'Brennevin til 18-19-åringer',
  BERUSET_PERSON_I_LOKALET: 'Beruset person i lokalet',
  MANGLER_IK_SYSTEM: 'Mangler IK-system',
  MANGLER_STYRER_STEDFORTREDER: 'Mangler styrer/stedfortreder',
  NARKOTIKA: 'Narkotika',
  ALKOHOLFRI_ALTERNATIV_MANGLER: 'Alkoholfri alternativ mangler',
  MEDBRAKT_ALKOHOL: 'Medbrakt alkohol',
  REKLAMEBRUDD: 'Reklamebrudd',
  VILKAARSBRUDD: 'Vilkårsbrudd',
  NEKTET_VISE_LEGITIMASJON: 'Nektet å vise legitimasjon',
  GLEMTE_SJEKKE_LEGITIMASJON: 'Glemte å sjekke legitimasjon',
  MINDREAARIG_FORSOK: 'Mindreårig forsøk',
  FALSK_LEGITIMASJON: 'Falsk legitimasjon',
  UTGAATT_LEGITIMASJON: 'Utgått legitimasjon',
  LEGITIMASJON_ANNET: 'Annet (legitimasjon)',
}

const causalLabel: Record<AlcoholCausalAnalysis, string> = {
  MANGLENDE_OPPLAERING: 'Manglende opplæring', RUTINE_IKKE_FULGT: 'Rutine ikke fulgt',
  RUTINE_MANGLER: 'Rutine mangler', HOYT_TRYKK_STRESS: 'Høyt trykk / stress',
  UNDERBEMANNING: 'Underbemanning', KOMMUNIKASJON: 'Kommunikasjon', ANNET: 'Annet',
}

const currentStatus = computed<FoodDeviationStatus | AlcoholDeviationStatus | null>(() => {
  return foodDeviation.value?.status ?? alcoholDeviation.value?.status ?? null
})

const statusSteps = computed(() => {
  const s = currentStatus.value
  return [
    {key: 'OPEN' as const, label: 'Åpen', done: true, active: s === 'OPEN'},
    {
      key: 'UNDER_TREATMENT' as const,
      label: 'Under behandling',
      done: s === 'UNDER_TREATMENT' || s === 'CLOSED',
      active: s === 'UNDER_TREATMENT'
    },
    {key: 'CLOSED' as const, label: 'Lukket', done: s === 'CLOSED', active: s === 'CLOSED'},
  ]
})

const nextStatus = computed<FoodDeviationStatus | AlcoholDeviationStatus | null>(() => {
  if (currentStatus.value === 'OPEN') return 'UNDER_TREATMENT'
  if (currentStatus.value === 'UNDER_TREATMENT') return 'CLOSED'
  return null
})

const nextStatusLabel = computed(() => {
  if (nextStatus.value === 'UNDER_TREATMENT') return 'Start behandling'
  if (nextStatus.value === 'CLOSED') return 'Lukk avvik'
  return null
})

async function changeStatus() {
  if (!nextStatus.value || !canManage.value) return
  statusChanging.value = true
  try {
    if (foodDeviation.value) {
      await updateFood.mutateAsync({
        id: foodDeviation.value.id,
        payload: {status: nextStatus.value as FoodDeviationStatus}
      })
    } else if (alcoholDeviation.value) {
      await updateAlcohol.mutateAsync({
        id: alcoholDeviation.value.id,
        payload: {status: nextStatus.value as AlcoholDeviationStatus}
      })
    }
    toast.success(`Status endret til "${nextStatus.value === 'UNDER_TREATMENT' ? 'Under behandling' : 'Lukket'}"`)
  } catch (err) {
    handleError(err, 'Kunne ikke endre status')
  } finally {
    statusChanging.value = false
  }
}

function formatDate(value: string | null): string {
  if (!value) return '-'
  return new Date(value).toLocaleString('nb-NO', {
    day: 'numeric', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

async function handleUpdateFood(p: { id: number; data: UpdateFoodDeviationRequest }) {
  try {
    await updateFood.mutateAsync({id: p.id, payload: p.data})
    editFoodOpen.value = false
    toast.success('Avvik oppdatert')
  } catch (err) {
    handleError(err, 'Kunne ikke oppdatere avvik')
  }
}

async function handleUpdateAlcohol(p: { id: number; data: UpdateAlcoholDeviationRequest }) {
  try {
    await updateAlcohol.mutateAsync({id: p.id, payload: p.data})
    editAlcoholOpen.value = false
    toast.success('Avvik oppdatert')
  } catch (err) {
    handleError(err, 'Kunne ikke oppdatere avvik')
  }
}

async function handleDelete() {
  try {
    if (foodDeviation.value) await deleteFood.mutateAsync(foodDeviation.value.id)
    else if (alcoholDeviation.value) await deleteAlcohol.mutateAsync(alcoholDeviation.value.id)
    toast.success('Avvik slettet')
    router.push('/avvik')
  } catch (err) {
    handleError(err, 'Kunne ikke slette avvik')
  }
}

function handleError(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const msg = error.response?.data?.error?.message
    if (typeof msg === 'string' && msg.trim().length > 0) {
      toast.error(msg);
      return
    }
  }
  toast.error(fallback)
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">Avvik</span>
      </div>
    </header>

    <div class="page-content">
      <Button variant="ghost" class="back-button" @click="router.push('/avvik')">
        <ArrowLeft :size="18" aria-hidden="true"/>
        <span>Tilbake til oversikt</span>
      </Button>

      <div v-if="isLoading" class="loading-state">
        <div class="skeleton-header"></div>
        <div class="skeleton-bar"></div>
        <div class="skeleton-block"></div>
        <div class="skeleton-block skeleton-block--short"></div>
      </div>

      <div v-else-if="notFound" class="empty-state">
        <div class="empty-state-bg"/>
        <div class="empty-state-inner">
          <div class="empty-state-icon">
            <AlertTriangle :stroke-width="1.5"/>
          </div>
          <div class="empty-state-text">
            <h3>Avvik ikke funnet</h3>
            <p>Avviket du leter etter finnes ikke eller har blitt slettet.</p>
          </div>
          <Button @click="router.push('/avvik')">Tilbake til oversikt</Button>
        </div>
      </div>

      <template v-else-if="foodDeviation">
        <div class="detail-layout">
          <div class="detail-main">
            <div class="detail-hero"
                 :class="`detail-hero--${foodDeviation.severity === 'CRITICAL' || foodDeviation.severity === 'HIGH' ? 'danger' : foodDeviation.severity === 'MEDIUM' ? 'warning' : 'ok'}`">
              <div class="hero-top">
                <div class="tag-row">
                  <Badge tone="brand">IK-Mat</Badge>
                  <Badge tone="neutral">{{ foodTypeLabel[foodDeviation.deviationType] }}</Badge>
                </div>
                <div v-if="canManage" class="hero-actions">
                  <Button variant="ghost" size="icon-sm" @click="editFoodOpen = true"
                          title="Rediger avvik" aria-label="Rediger avvik">
                    <Pencil :size="16" aria-hidden="true"/>
                  </Button>
                  <Button variant="ghost" size="icon-sm" @click="deleteDialogOpen = true"
                          title="Slett avvik" aria-label="Slett avvik" class="delete-icon">
                    <Trash2 :size="16" aria-hidden="true"/>
                  </Button>
                </div>
              </div>
              <div class="hero-severity">
                <span class="hero-id">#{{ foodDeviation.id }}</span>
              </div>
              <p class="hero-description">{{ foodDeviation.description }}</p>
            </div>

            <DeviationStatusWorkflow
              :steps="statusSteps"
              :next-status-label="nextStatusLabel"
              :can-manage="canManage"
              :status-changing="statusChanging"
              @change-status="changeStatus"
            />

            <div class="sections">
              <section class="content-section">
                <div class="section-header">
                  <Zap :size="16" class="section-icon section-icon--amber"/>
                  <h2>Umiddelbar handling</h2>
                </div>
                <div v-if="foodDeviation.immediateAction">
                  <p class="detail-body">{{ foodDeviation.immediateAction }}</p>
                  <div v-if="foodDeviation.immediateActionByUserName" class="meta-inline">
                    Utført av {{ foodDeviation.immediateActionByUserName }}
                    <span v-if="foodDeviation.immediateActionAt"> · {{
                        formatDate(foodDeviation.immediateActionAt)
                      }}</span>
                  </div>
                </div>
                <p v-else class="empty-field">Ikke registrert</p>
              </section>

              <section class="content-section">
                <div class="section-header">
                  <Search :size="16" class="section-icon section-icon--brand"/>
                  <h2>Årsak</h2>
                </div>
                <p v-if="foodDeviation.cause" class="detail-body">{{ foodDeviation.cause }}</p>
                <p v-else class="empty-field">Ikke registrert</p>
              </section>

              <section class="content-section">
                <div class="section-header">
                  <Shield :size="16" class="section-icon section-icon--green"/>
                  <h2>Forebyggende tiltak</h2>
                </div>
                <div v-if="foodDeviation.preventiveMeasures">
                  <p class="detail-body">{{ foodDeviation.preventiveMeasures }}</p>
                  <div v-if="foodDeviation.preventiveResponsibleUserName" class="meta-inline">
                    Ansvarlig: {{ foodDeviation.preventiveResponsibleUserName }}
                    <span v-if="foodDeviation.preventiveDeadline"> · Frist: {{
                        formatDate(foodDeviation.preventiveDeadline)
                      }}</span>
                  </div>
                </div>
                <p v-else class="empty-field">Ikke registrert</p>
              </section>
            </div>
          </div>

          <DeviationDetailSidebar
            :reported-by-name="foodDeviation.reportedByUserName"
            :reported-at="foodDeviation.reportedAt"
            :updated-at="foodDeviation.updatedAt"
            :status-label="foodStatusLabel[foodDeviation.status]"
            :status-tone="foodDeviation.status === 'OPEN' ? 'danger' : foodDeviation.status === 'UNDER_TREATMENT' ? 'warning' : 'ok'"
            :fields="[{ label: 'Type', value: foodTypeLabel[foodDeviation.deviationType] }]"
          />
        </div>
      </template>

      <template v-else-if="alcoholDeviation">
        <div class="detail-layout">
          <div class="detail-main">
            <!-- Hero header -->
            <div class="detail-hero detail-hero--warning">
              <div class="hero-top">
                <div class="tag-row">
                  <Badge tone="brand">IK-Alkohol</Badge>
                  <Badge
                    :tone="alcoholDeviation.reportSource === 'EGENRAPPORT' ? 'neutral' : alcoholDeviation.reportSource === 'SJENKEKONTROLL' ? 'warning' : 'danger'">
                    {{ sourceLabel[alcoholDeviation.reportSource] }}
                  </Badge>
                </div>
                <div v-if="canManage" class="hero-actions">
                  <Button variant="ghost" size="icon-sm" @click="editAlcoholOpen = true"
                          title="Rediger avvik" aria-label="Rediger avvik">
                    <Pencil :size="16" aria-hidden="true"/>
                  </Button>
                  <Button variant="ghost" size="icon-sm" @click="deleteDialogOpen = true"
                          title="Slett avvik" aria-label="Slett avvik" class="delete-icon">
                    <Trash2 :size="16" aria-hidden="true"/>
                  </Button>
                </div>
              </div>
              <div class="hero-severity">
                <Badge tone="neutral" class="severity-badge">
                  {{ alcoholTypeLabel[alcoholDeviation.deviationType] }}
                </Badge>
                <span class="hero-id">#{{ alcoholDeviation.id }}</span>
              </div>
              <p class="hero-description">{{ alcoholDeviation.description }}</p>
            </div>

            <DeviationStatusWorkflow
              :steps="statusSteps"
              :next-status-label="nextStatusLabel"
              :can-manage="canManage"
              :status-changing="statusChanging"
              @change-status="changeStatus"
            />

            <div class="sections">
              <section v-if="alcoholDeviation.immediateAction" class="content-section">
                <div class="section-header">
                  <Zap :size="16" class="section-icon section-icon--amber"/>
                  <h2>Umiddelbar handling</h2>
                </div>
                <p class="detail-body">{{ alcoholDeviation.immediateAction }}</p>
              </section>

              <section class="content-section">
                <div class="section-header">
                  <Search :size="16" class="section-icon section-icon--brand"/>
                  <h2>Årsaksanalyse</h2>
                </div>
                <div v-if="alcoholDeviation.causalAnalysis">
                  <Badge tone="neutral">{{ causalLabel[alcoholDeviation.causalAnalysis] }}</Badge>
                  <p v-if="alcoholDeviation.causalExplanation"
                     class="detail-body detail-body--spaced">{{
                      alcoholDeviation.causalExplanation
                    }}</p>
                </div>
                <p v-else class="empty-field">Ikke registrert</p>
              </section>

              <section class="content-section">
                <div class="section-header">
                  <Shield :size="16" class="section-icon section-icon--green"/>
                  <h2>Forebyggende tiltak</h2>
                </div>
                <div v-if="alcoholDeviation.preventiveMeasures">
                  <p class="detail-body">{{ alcoholDeviation.preventiveMeasures }}</p>
                  <div v-if="alcoholDeviation.preventiveResponsibleUserName" class="meta-inline">
                    Ansvarlig: {{ alcoholDeviation.preventiveResponsibleUserName }}
                    <span v-if="alcoholDeviation.preventiveDeadline"> · Frist: {{
                        formatDate(alcoholDeviation.preventiveDeadline)
                      }}</span>
                  </div>
                </div>
                <p v-else class="empty-field">Ikke registrert</p>
              </section>
            </div>
          </div>

          <DeviationDetailSidebar
            :reported-by-name="alcoholDeviation.reportedByUserName"
            :reported-at="alcoholDeviation.reportedAt"
            :updated-at="alcoholDeviation.updatedAt"
            :status-label="alcoholStatusLabel[alcoholDeviation.status]"
            :status-tone="alcoholDeviation.status === 'OPEN' ? 'danger' : alcoholDeviation.status === 'UNDER_TREATMENT' ? 'warning' : 'ok'"
            :fields="[
              { label: 'Kilde', value: sourceLabel[alcoholDeviation.reportSource] },
              { label: 'Type', value: alcoholTypeLabel[alcoholDeviation.deviationType] },
            ]"
          />
        </div>
      </template>
    </div>

    <AlertDialog v-model:open="deleteDialogOpen">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Slette avvik?</AlertDialogTitle>
          <AlertDialogDescription>Avviket blir permanent slettet og kan ikke gjenopprettes.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Avbryt</AlertDialogCancel>
          <AlertDialogAction variant="destructive" @click="handleDelete">Slett</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <FoodDeviationFormDialog
      v-model:open="editFoodOpen"
      mode="edit"
      :initial="foodDeviation"
      :submitting="updateFood.isPending.value"
      :members="memberOptions"
      @update="handleUpdateFood"
    />
    <AlcoholDeviationFormDialog
      v-model:open="editAlcoholOpen"
      mode="edit"
      :initial="alcoholDeviation"
      :submitting="updateAlcohol.isPending.value"
      :members="memberOptions"
      :penalty-summary="penaltyQuery.data.value ?? null"
      @update="handleUpdateAlcohol"
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
  gap: 1.25rem;
  padding: 0 1.25rem 2rem;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  background: none !important;
  border: none;
  cursor: pointer;
  color: hsl(var(--muted-foreground));
  font-size: 0.85rem;
  padding: 4px 0;
  transition: color 150ms ease;
}

.back-button:hover {
  background: none !important;
  color: hsl(var(--foreground));
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 1.25rem;
  align-items: start;
}

.detail-main {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-width: 0;
}

.detail-hero {
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--card-bg);
  padding: 20px 24px;
  position: relative;
  overflow: hidden;
}

.detail-hero::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: hsl(var(--border));
}

.detail-hero--danger::before {
  background: var(--red);
}

.detail-hero--warning::before {
  background: var(--amber);
}

.detail-hero--ok::before {
  background: var(--green);
}

.hero-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.hero-actions {
  display: flex;
  gap: 2px;
}

.delete-icon {
  color: var(--red);
}

.delete-icon:hover {
  background: var(--red-soft);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.hero-severity {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.severity-badge {
  font-size: 0.82rem;
}

.hero-id {
  font-size: 0.82rem;
  color: var(--text-secondary);
  font-weight: 500;
  font-variant-numeric: tabular-nums;
}

.hero-description {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.6;
  color: hsl(var(--foreground));
  white-space: pre-wrap;
}

.sections {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.content-section {
  padding: 18px 20px;
  border: 1px solid var(--border);
  background: var(--card-bg);
}

.content-section:first-child {
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.content-section:last-child {
  border-radius: 0 0 var(--radius-xl) var(--radius-xl);
}

.content-section:only-child {
  border-radius: var(--radius-xl);
}

.content-section + .content-section {
  border-top: none;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.section-icon {
  flex-shrink: 0;
}

.section-icon--amber {
  color: var(--amber);
}

.section-icon--brand {
  color: var(--brand);
}

.section-icon--green {
  color: var(--green);
}

.content-section h2 {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.detail-body {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.65;
  white-space: pre-wrap;
  color: hsl(var(--foreground) / 0.85);
}

.detail-body--spaced {
  margin-top: 8px;
}

.meta-inline {
  font-size: 0.8rem;
  color: var(--text-secondary);
  margin-top: 8px;
}

.empty-field {
  color: var(--text-secondary);
  font-size: 0.85rem;
  font-style: italic;
  margin: 0;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 720px;
}

.skeleton-header, .skeleton-bar, .skeleton-block {
  border-radius: var(--radius-lg);
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

.skeleton-header {
  height: 80px;
}

.skeleton-bar {
  height: 48px;
}

.skeleton-block {
  height: 120px;
}

.skeleton-block--short {
  height: 80px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.empty-state {
  position: relative;
  display: flex;
  min-height: 260px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 1rem;
  border: 2px dashed hsl(var(--muted-foreground) / 0.2);
  background: linear-gradient(to bottom right, hsl(var(--muted) / 0.4), hsl(var(--muted) / 0.2), hsl(var(--background)));
  padding: 2rem;
}

.empty-state-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at center, hsl(var(--muted)) 0%, transparent 70%);
  opacity: 0.5;
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
  height: 5rem;
  width: 5rem;
  align-items: center;
  justify-content: center;
  border-radius: 1rem;
  background-color: hsl(var(--primary) / 0.1);
  box-shadow: 0 0 0 4px hsl(var(--primary) / 0.05);
}

.empty-state-icon :deep(svg) {
  width: 2.5rem;
  height: 2.5rem;
  color: hsl(var(--primary) / 0.7);
}

.empty-state-text h3 {
  font-size: 1.125rem;
  font-weight: 600;
}

.empty-state-text p {
  max-width: 24rem;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  margin-top: 0.25rem;
}

@media (max-width: 860px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 600px) {
  .hero-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-actions {
    align-self: flex-end;
    margin-top: -28px;
  }
}
</style>

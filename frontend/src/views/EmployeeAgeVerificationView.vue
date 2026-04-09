<script setup lang="ts">
import {computed, ref} from 'vue'
import {toast} from 'vue-sonner'
import axios from 'axios'
import {
  ShieldCheck, Play, AlertTriangle, IdCard, UserX, HelpCircle, Ban, FileWarning, Clock,
} from 'lucide-vue-next'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import Button from '@/components/ui/button/Button.vue'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import AlcoholDeviationFormDialog from '@/components/deviations/AlcoholDeviationFormDialog.vue'
import IdCheckCounter from '@/components/age-verification/IdCheckCounter.vue'
import QuickDeviationGrid from '@/components/age-verification/QuickDeviationGrid.vue'
import {useAuthStore} from '@/stores/auth'
import {
  useActiveShiftQuery,
  useStartShiftMutation,
  useUpdateIdCheckCountMutation,
  useCreateShiftDeviationMutation,
  useEndShiftMutation,
  useReopenShiftMutation,
} from '@/composables/useAgeVerification'
import {useDeleteAlcoholDeviationMutation} from '@/composables/useAlcoholDeviations'
import {useMemberNamesQuery} from '@/composables/useMembers'
import type {AlcoholDeviationType, CreateAlcoholDeviationRequest} from '@/types/deviation'

const auth = useAuthStore()
const activeShiftQuery = useActiveShiftQuery()
const startShift = useStartShiftMutation()
const updateCount = useUpdateIdCheckCountMutation()
const createDeviation = useCreateShiftDeviationMutation()
const endShift = useEndShiftMutation()
const reopenShift = useReopenShiftMutation()
const deleteDeviation = useDeleteAlcoholDeviationMutation()
const memberNamesQuery = useMemberNamesQuery()

const shift = computed(() => activeShiftQuery.data.value?.shift ?? null)
const deviations = computed(() => activeShiftQuery.data.value?.deviations ?? [])
const hasActiveShift = computed(() => shift.value !== null && shift.value.status === 'ACTIVE')
const hasCompletedTodayShift = computed(() => shift.value !== null && shift.value.status === 'COMPLETED')

const endShiftDialogOpen = ref(false)
const deviationFormOpen = ref(false)
const prefillDeviationType = ref<AlcoholDeviationType | ''>('')

const memberOptions = computed(() =>
  (memberNamesQuery.data.value ?? []).map((m) => ({userId: m.userId, label: m.fullName})),
)

const quickDeviationTypes: { type: AlcoholDeviationType; label: string; icon: typeof UserX }[] = [
  {type: 'NEKTET_VISE_LEGITIMASJON', label: 'Nektet å vise leg', icon: UserX},
  {type: 'GLEMTE_SJEKKE_LEGITIMASJON', label: 'Glemte å sjekke leg', icon: HelpCircle},
  {type: 'MINDREAARIG_FORSOK', label: 'Mindreårig forsøk', icon: Ban},
  {type: 'FALSK_LEGITIMASJON', label: 'Falsk legitimasjon', icon: FileWarning},
  {type: 'UTGAATT_LEGITIMASJON', label: 'Utgått legitimasjon', icon: Clock},
  {type: 'LEGITIMASJON_ANNET', label: 'Annet', icon: AlertTriangle},
]

const deviationLabel: Partial<Record<AlcoholDeviationType, string>> = {
  NEKTET_VISE_LEGITIMASJON: 'Nektet å vise leg',
  GLEMTE_SJEKKE_LEGITIMASJON: 'Glemte å sjekke leg',
  MINDREAARIG_FORSOK: 'Mindreårig forsøk',
  FALSK_LEGITIMASJON: 'Falsk legitimasjon',
  UTGAATT_LEGITIMASJON: 'Utgått legitimasjon',
  LEGITIMASJON_ANNET: 'Annet',
}

function handleError(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const msg = error.response?.data?.error?.message
    if (typeof msg === 'string' && msg.trim()) {
      toast.error(msg);
      return
    }
  }
  toast.error(fallback)
}

async function handleStartShift() {
  try {
    await startShift.mutateAsync();
    toast.success('Skift startet')
  } catch (e) {
    handleError(e, 'Kunne ikke starte skift')
  }
}

async function handleIncrement() {
  if (!shift.value) return
  try {
    await updateCount.mutateAsync({
      id: shift.value.id,
      payload: {idsCheckedCount: shift.value.idsCheckedCount + 1}
    })
  } catch (e) {
    handleError(e, 'Kunne ikke oppdatere antall')
  }
}

async function handleDecrement() {
  if (!shift.value || shift.value.idsCheckedCount <= 0) return
  try {
    await updateCount.mutateAsync({
      id: shift.value.id,
      payload: {idsCheckedCount: shift.value.idsCheckedCount - 1}
    })
  } catch (e) {
    handleError(e, 'Kunne ikke oppdatere antall')
  }
}

function openDeviationForm(type: AlcoholDeviationType) {
  prefillDeviationType.value = type
  deviationFormOpen.value = true
}

async function handleDeviationCreate(payload: CreateAlcoholDeviationRequest) {
  if (!shift.value) return
  try {
    await createDeviation.mutateAsync({
      id: shift.value.id,
      payload: {deviationType: payload.deviationType, description: payload.description}
    })
    toast.success('Avvik registrert');
    deviationFormOpen.value = false
  } catch (e) {
    handleError(e, 'Kunne ikke registrere avvik')
  }
}

async function handleDeleteDeviation(deviationId: number) {
  try {
    await deleteDeviation.mutateAsync(deviationId);
    activeShiftQuery.refetch();
    toast.success('Avvik fjernet')
  } catch (e) {
    handleError(e, 'Kunne ikke fjerne avvik')
  }
}

async function handleEndShift() {
  if (!shift.value) return
  try {
    await endShift.mutateAsync(shift.value.id);
    await activeShiftQuery.refetch();
    toast.success('Skift avsluttet og signert');
    endShiftDialogOpen.value = false
  } catch (e) {
    handleError(e, 'Kunne ikke avslutte skift')
  }
}

async function handleReopenShift() {
  if (!shift.value) return
  try {
    await reopenShift.mutateAsync(shift.value.id);
    toast.success('Dagens skift er gjenapnet')
  } catch (e) {
    handleError(e, 'Kunne ikke gjenapne skift')
  }
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('nb-NO', {hour: '2-digit', minute: '2-digit'})
}
</script>

<template>
  <header class="page-header">
    <div class="page-header-inner">
      <SidebarTrigger/>
      <Separator orientation="vertical" class="header-separator"/>
      <span class="page-title">Bevilling</span>
    </div>
  </header>

  <div class="page-content">
    <template v-if="hasCompletedTodayShift && !activeShiftQuery.isLoading.value">
      <section class="header-row">
        <div>
          <h1>Alderskontroll</h1>
          <p>Skiftet ditt er ferdig for i dag. Du kan starte nytt skift i morgen.</p>
        </div>
      </section>
      <div class="start-card">
        <div class="start-card-body">
          <div class="start-icon">
            <ShieldCheck :size="36" :stroke-width="1.5" aria-hidden="true"/>
          </div>
          <div class="start-info">
            <div class="info-item">
              <IdCard :size="18" aria-hidden="true"/>
              <span>Skift ferdig: {{ shift?.idsCheckedCount ?? 0 }} legitimasjoner sjekket</span>
            </div>
            <div class="info-item">
              <AlertTriangle :size="18" aria-hidden="true"/>
              <span>{{ deviations.length }} avvik registrert i dagens skift</span></div>
          </div>
          <Button :disabled="reopenShift.isPending.value" @click="handleReopenShift">Gjenapne dagens
            skift
          </Button>
        </div>
      </div>
    </template>

    <template v-else-if="!hasActiveShift && !activeShiftQuery.isLoading.value">
      <section class="header-row">
        <div>
          <h1>Alderskontroll</h1>
          <p>Start skiftet ditt for å begynne å registrere alderskontroller</p>
        </div>
      </section>
      <div class="start-card">
        <div class="start-card-body">
          <div class="start-icon">
            <ShieldCheck :size="36" :stroke-width="1.5" aria-hidden="true"/>
          </div>
          <div class="start-info">
            <div class="info-item">
              <IdCard :size="18" aria-hidden="true"/>
              <span>Tell hver ID-sjekk med + og - knappene</span></div>
            <div class="info-item">
              <AlertTriangle :size="18" aria-hidden="true"/>
              <span>Registrer avvik raskt med snarveisknapper</span></div>
          </div>
          <Button :disabled="startShift.isPending.value" @click="handleStartShift">
            <Play :size="18" aria-hidden="true"/>
            Start skift
          </Button>
        </div>
      </div>
    </template>

    <template v-else-if="hasActiveShift">
      <section class="header-row">
        <div>
          <h1>Alderskontroll</h1>
          <p>{{ auth.user?.fullName }} · Startet {{ formatTime(shift!.startedAt) }}</p>
        </div>
        <Button variant="destructive-ghost" @click="endShiftDialogOpen = true">Avslutt skift
        </Button>
      </section>

      <IdCheckCounter
        :count="shift!.idsCheckedCount"
        :disabled="updateCount.isPending.value"
        @increment="handleIncrement"
        @decrement="handleDecrement"
      />

      <QuickDeviationGrid
        :quick-types="quickDeviationTypes"
        :deviations="deviations"
        :deviation-labels="deviationLabel"
        @create="openDeviationForm"
        @delete="handleDeleteDeviation"
      />
    </template>

    <AlcoholDeviationFormDialog
      :open="deviationFormOpen"
      mode="create"
      :members="memberOptions"
      :submitting="createDeviation.isPending.value"
      :initial="prefillDeviationType ? ({
        reportSource: 'EGENRAPPORT', deviationType: prefillDeviationType, description: '',
        reportedAt: new Date().toISOString(), id: 0, organizationId: 0,
        reportedByUserId: auth.user?.id ?? 0, reportedByUserName: auth.user?.fullName ?? '',
        immediateAction: null, causalAnalysis: null, causalExplanation: null,
        preventiveMeasures: null, preventiveDeadline: null,
        preventiveResponsibleUserId: null, preventiveResponsibleUserName: null,
        status: 'OPEN', createdAt: '', updatedAt: '',
      } as any) : null"
      @update:open="deviationFormOpen = $event"
      @create="handleDeviationCreate"
    />

    <AlertDialog :open="endShiftDialogOpen"
                 @update:open="(v: boolean) => { endShiftDialogOpen = v }">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Avslutt og signer skift?</AlertDialogTitle>
          <AlertDialogDescription>
            Ved å avslutte skiftet bekrefter du at informasjonen du har registrert er korrekt.
            Du har sjekket <strong>{{ shift?.idsCheckedCount ?? 0 }}</strong> legitimasjoner
            og registrert <strong>{{ deviations.length }}</strong> avvik.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>Avbryt</AlertDialogCancel>
          <AlertDialogAction @click="handleEndShift">Signer og avslutt</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
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
  padding: 0 1rem 2rem;
  width: 100%;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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
  font-size: 1.08rem;
}

.start-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
  max-width: 480px;
  margin: 0 auto;
  width: 100%;
}

.start-card-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 20px;
  padding: 40px 24px;
}

.start-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
}

.start-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
}

.info-item svg {
  flex-shrink: 0;
  color: hsl(var(--muted-foreground));
}

@media (max-width: 400px) {
  h1 {
    font-size: 1.5rem;
  }
}
</style>

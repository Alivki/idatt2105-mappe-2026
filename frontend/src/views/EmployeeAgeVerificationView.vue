<script setup lang="ts">
import { computed, ref } from 'vue'
import { toast } from 'vue-sonner'
import axios from 'axios'
import {
  ShieldCheck,
  Plus,
  Minus,
  Play,
  AlertTriangle,
  IdCard,
  UserX,
  HelpCircle,
  Ban,
  FileWarning,
  Clock,
  Trash2,
} from 'lucide-vue-next'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
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
import { useAuthStore } from '@/stores/auth'
import {
  useActiveShiftQuery,
  useStartShiftMutation,
  useUpdateIdCheckCountMutation,
  useCreateShiftDeviationMutation,
  useEndShiftMutation,
  useReopenShiftMutation,
} from '@/composables/useAgeVerification'
import { useDeleteAlcoholDeviationMutation } from '@/composables/useAlcoholDeviations'
import { useMemberNamesQuery } from '@/composables/useMembers'
import type { AlcoholDeviationType, CreateAlcoholDeviationRequest } from '@/types/deviation'

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
  (memberNamesQuery.data.value ?? []).map((m) => ({
    userId: m.userId,
    label: m.fullName,
  })),
)

const quickDeviationTypes: { type: AlcoholDeviationType; label: string; icon: typeof UserX }[] = [
  { type: 'NEKTET_VISE_LEGITIMASJON', label: 'Nektet å vise leg', icon: UserX },
  { type: 'GLEMTE_SJEKKE_LEGITIMASJON', label: 'Glemte å sjekke leg', icon: HelpCircle },
  { type: 'MINDREAARIG_FORSOK', label: 'Mindreårig forsøk', icon: Ban },
  { type: 'FALSK_LEGITIMASJON', label: 'Falsk legitimasjon', icon: FileWarning },
  { type: 'UTGAATT_LEGITIMASJON', label: 'Utgått legitimasjon', icon: Clock },
  { type: 'LEGITIMASJON_ANNET', label: 'Annet', icon: AlertTriangle },
]

function handleError(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const msg = error.response?.data?.error?.message
    if (typeof msg === 'string' && msg.trim()) {
      toast.error(msg)
      return
    }
  }
  toast.error(fallback)
}

async function handleStartShift() {
  try {
    await startShift.mutateAsync()
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
      payload: { idsCheckedCount: shift.value.idsCheckedCount + 1 },
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
      payload: { idsCheckedCount: shift.value.idsCheckedCount - 1 },
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
      payload: {
        deviationType: payload.deviationType,
        description: payload.description,
      },
    })
    toast.success('Avvik registrert')
    deviationFormOpen.value = false
  } catch (e) {
    handleError(e, 'Kunne ikke registrere avvik')
  }
}

async function handleDeleteDeviation(deviationId: number) {
  try {
    await deleteDeviation.mutateAsync(deviationId)
    activeShiftQuery.refetch()
    toast.success('Avvik fjernet')
  } catch (e) {
    handleError(e, 'Kunne ikke fjerne avvik')
  }
}

async function handleEndShift() {
  if (!shift.value) return
  try {
    await endShift.mutateAsync(shift.value.id)
    await activeShiftQuery.refetch()
    toast.success('Skift avsluttet og signert')
    endShiftDialogOpen.value = false
  } catch (e) {
    handleError(e, 'Kunne ikke avslutte skift')
  }
}

async function handleReopenShift() {
  if (!shift.value) return
  try {
    await reopenShift.mutateAsync(shift.value.id)
    toast.success('Dagens skift er gjenapnet')
  } catch (e) {
    handleError(e, 'Kunne ikke gjenapne skift')
  }
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
  <header class="page-header">
    <div class="page-header-inner">
      <SidebarTrigger />
      <Separator orientation="vertical" class="header-separator" />
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
            <ShieldCheck :size="36" :stroke-width="1.5" aria-hidden="true" />
          </div>
          <div class="start-info">
            <div class="info-item">
              <IdCard :size="18" aria-hidden="true" />
              <span>Skift ferdig: {{ shift?.idsCheckedCount ?? 0 }} legitimasjoner sjekket</span>
            </div>
            <div class="info-item">
              <AlertTriangle :size="18" aria-hidden="true" />
              <span>{{ deviations.length }} avvik registrert i dagens skift</span>
            </div>
          </div>
          <Button
            :disabled="reopenShift.isPending.value"
            @click="handleReopenShift"
          >
            Gjenapne dagens skift
          </Button>
        </div>
      </div>
    </template>

    <!-- No shift today: show start view -->
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
            <ShieldCheck :size="36" :stroke-width="1.5" aria-hidden="true" />
          </div>
          <div class="start-info">
            <div class="info-item">
              <IdCard :size="18" aria-hidden="true" />
              <span>Tell hver ID-sjekk med + og - knappene</span>
            </div>
            <div class="info-item">
              <AlertTriangle :size="18" aria-hidden="true" />
              <span>Registrer avvik raskt med snarveisknapper</span>
            </div>
          </div>
          <Button
            :disabled="startShift.isPending.value"
            @click="handleStartShift"
          >
            <Play :size="18" aria-hidden="true" />
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
        <Button variant="destructive-ghost" @click="endShiftDialogOpen = true">
          Avslutt skift
        </Button>
      </section>

      <div class="counter-row">
        <span class="counter-label">Legitimasjoner sjekket</span>
        <div class="counter">
          <button
            class="counter-btn"
            aria-label="Reduser antall"
            :disabled="shift!.idsCheckedCount <= 0 || updateCount.isPending.value"
            @click="handleDecrement"
          >
            <Minus :size="22" aria-hidden="true" />
          </button>
          <span class="counter-value">{{ shift!.idsCheckedCount }}</span>
          <button
            class="counter-btn"
            aria-label="Øk antall"
            :disabled="updateCount.isPending.value"
            @click="handleIncrement"
          >
            <Plus :size="22" aria-hidden="true" />
          </button>
        </div>
      </div>

      <div class="deviation-section">
        <p class="section-label">Registrer avvik</p>
        <div class="deviation-grid">
          <button
            v-for="d in quickDeviationTypes"
            :key="d.type"
            class="deviation-btn"
            @click="openDeviationForm(d.type)"
          >
            <component :is="d.icon" :size="18" aria-hidden="true" />
            <span>{{ d.label }}</span>
          </button>
        </div>
      </div>

      <!-- Deviations logged this shift -->
      <div v-if="deviations.length > 0" class="deviation-log">
        <p class="section-label">Avvik i dette skiftet ({{ deviations.length }})</p>
        <div class="deviation-list">
          <div v-for="dev in deviations" :key="dev.id" class="deviation-item">
            <AlertTriangle :size="16" class="deviation-item-icon" aria-hidden="true" />
            <div class="deviation-item-content">
              <span class="deviation-item-type">{{ deviationLabel[dev.deviationType] ?? dev.deviationType }}</span>
              <span class="deviation-item-time">{{ formatTime(dev.reportedAt) }}</span>
            </div>
            <button class="deviation-item-delete" aria-label="Slett avvik" @click="handleDeleteDeviation(dev.id)">
              <Trash2 :size="15" aria-hidden="true" />
            </button>
          </div>
        </div>
      </div>
    </template>

    <AlcoholDeviationFormDialog
      :open="deviationFormOpen"
      mode="create"
      :members="memberOptions"
      :submitting="createDeviation.isPending.value"
      :initial="prefillDeviationType ? ({
        reportSource: 'EGENRAPPORT',
        deviationType: prefillDeviationType,
        description: '',
        reportedAt: new Date().toISOString(),
        id: 0,
        organizationId: 0,
        reportedByUserId: auth.user?.id ?? 0,
        reportedByUserName: auth.user?.fullName ?? '',
        immediateAction: null,
        causalAnalysis: null,
        causalExplanation: null,
        preventiveMeasures: null,
        preventiveDeadline: null,
        preventiveResponsibleUserId: null,
        preventiveResponsibleUserName: null,
        status: 'OPEN',
        createdAt: '',
        updatedAt: '',
      } as any) : null"
      @update:open="deviationFormOpen = $event"
      @create="handleDeviationCreate"
    />

    <!-- End shift confirmation -->
    <AlertDialog :open="endShiftDialogOpen" @update:open="(v: boolean) => { endShiftDialogOpen = v }">
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
          <AlertDialogAction @click="handleEndShift">
            Signer og avslutt
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  </div>
</template>

<style scoped>
.page-header { display: flex; height: 4rem; flex-shrink: 0; align-items: center; }
.page-header-inner { display: flex; align-items: center; gap: 0.5rem; padding: 0 1rem; }
.header-separator { height: 1rem !important; width: 1px !important; margin-right: 0.5rem; }
.page-title { font-weight: 500; color: hsl(var(--sidebar-primary, 245 43% 52%)); }

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

h1 { margin: 0; font-size: 1.75rem; font-weight: 800; letter-spacing: -0.03em; }
.header-row p { margin-top: 6px; color: var(--text-secondary); font-size: 1.08rem; }

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

.info-item svg { flex-shrink: 0; color: hsl(var(--muted-foreground)); }

.counter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid hsl(var(--border));
}

.counter-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.counter {
  display: flex;
  align-items: stretch;
  border: 2px solid hsl(var(--border));
  border-radius: 12px;
  overflow: hidden;
}

.counter-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  border: none;
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  cursor: pointer;
  transition: background 150ms ease, color 150ms ease;
}

.counter-btn:hover:not(:disabled) {
  background: hsl(var(--primary) / 0.08);
  color: hsl(var(--primary));
}

.counter-btn:active:not(:disabled) {
  background: hsl(var(--primary) / 0.15);
}

.counter-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.counter-value {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 12px 0;
  font-size: 1.8rem;
  font-weight: 800;
  line-height: 1;
  color: hsl(var(--foreground));
  font-variant-numeric: tabular-nums;
  border-left: 2px solid hsl(var(--border));
  border-right: 2px solid hsl(var(--border));
  background: hsl(var(--background));
}

.deviation-section { display: flex; flex-direction: column; }

.section-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  margin: 0 0 10px;
}

.deviation-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.deviation-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  font: inherit;
  font-size: 0.85rem;
  font-weight: 500;
  color: hsl(var(--foreground));
  cursor: pointer;
  transition: border-color 150ms ease, background 150ms ease, color 150ms ease;
}

.deviation-btn:hover {
  border-color: var(--red);
  background: var(--red-soft);
  color: var(--red);
}

.deviation-btn:active { transform: scale(0.98); }

.deviation-btn svg { flex-shrink: 0; opacity: 0.6; }

.deviation-log { display: flex; flex-direction: column; }

.deviation-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.deviation-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
}

.deviation-item-icon { flex-shrink: 0; color: var(--red); }

.deviation-item-content {
  flex: 1;
  min-width: 0;
}

.deviation-item-type { font-size: 0.85rem; font-weight: 500; }

.deviation-item-time {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  margin-left: 8px;
}

.deviation-item-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  border: none;
  background: none;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  flex-shrink: 0;
  transition: background 150ms ease, color 150ms ease;
}

.deviation-item-delete:hover {
  background: var(--red-soft);
  color: var(--red);
}

@media (max-width: 400px) {
  h1 { font-size: 1.5rem; }
  .deviation-grid { grid-template-columns: 1fr; }
  .counter-row { flex-direction: column; gap: 12px; align-items: stretch; }
  .counter { align-self: center; }
}
</style>

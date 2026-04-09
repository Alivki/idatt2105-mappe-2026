<script setup lang="ts">
import {computed, ref} from 'vue'
import {MoreVertical, Trash2, Pencil, CircleDot, Clock, CheckCircle2} from 'lucide-vue-next'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import {relativeTime} from '@/utils/date'

type DeviationStatus = 'OPEN' | 'UNDER_TREATMENT' | 'CLOSED'

const props = defineProps<{
  deviation: {
    id: number;
    reportedAt: string;
    description: string;
    status: DeviationStatus;
    deviationType: string
  }
  canManage: boolean
  typeLabel: string
  moduleLabel: string
  moduleTone?: 'brand' | 'neutral'
  deleteTitle: string
}>()

const emits = defineEmits<{
  (e: 'edit'): void
  (e: 'delete', id: number): void
  (e: 'update-status', id: number, status: DeviationStatus): void
  (e: 'open'): void
}>()

const deleteDialogOpen = ref(false)

const statusLabel: Record<DeviationStatus, string> = {
  OPEN: 'Åpen', UNDER_TREATMENT: 'Under behandling', CLOSED: 'Lukket',
}

const statusTone: Record<DeviationStatus, 'danger' | 'warning' | 'ok'> = {
  OPEN: 'danger', UNDER_TREATMENT: 'warning', CLOSED: 'ok',
}

const timeAgo = computed(() => relativeTime(props.deviation.reportedAt))
</script>

<template>
  <div class="deviation-card" role="button" tabindex="0" @click="emits('open')"
       @keydown.enter="emits('open')">
    <div class="card-top">
      <div class="card-body">
        <div class="tag-row">
          <Badge tone="neutral">{{ typeLabel }}</Badge>
          <Badge :tone="moduleTone ?? 'brand'">{{ moduleLabel }}</Badge>
          <Badge :tone="statusTone[deviation.status]">{{ statusLabel[deviation.status] }}</Badge>
        </div>
        <p class="description">{{ deviation.description }}</p>
        <span class="time-label">{{ timeAgo }}</span>
      </div>

      <div v-if="canManage" class="card-actions" @click.stop>
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger"
                    aria-label="Handlinger">
              <MoreVertical :size="18" aria-hidden="true"/>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" :side-offset="4">
            <DropdownMenuItem @click="emits('edit')">
              <Pencil :size="16" aria-hidden="true"/>
              Rediger
            </DropdownMenuItem>
            <DropdownMenuSeparator/>
            <DropdownMenuItem v-if="deviation.status !== 'OPEN'" class="menu-item--status-open"
                              @click="emits('update-status', deviation.id, 'OPEN')">
              <CircleDot :size="16" aria-hidden="true"/>
              Åpen
            </DropdownMenuItem>
            <DropdownMenuItem v-if="deviation.status !== 'UNDER_TREATMENT'"
                              class="menu-item--status-progress"
                              @click="emits('update-status', deviation.id, 'UNDER_TREATMENT')">
              <Clock :size="16" aria-hidden="true"/>
              Under behandling
            </DropdownMenuItem>
            <DropdownMenuItem v-if="deviation.status !== 'CLOSED'" class="menu-item--status-closed"
                              @click="emits('update-status', deviation.id, 'CLOSED')">
              <CheckCircle2 :size="16" aria-hidden="true"/>
              Lukket
            </DropdownMenuItem>
            <DropdownMenuSeparator/>
            <DropdownMenuItem class="menu-item--danger" @click="deleteDialogOpen = true">
              <Trash2 :size="16" aria-hidden="true"/>
              Slett
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>

        <AlertDialog v-model:open="deleteDialogOpen">
          <AlertDialogContent>
            <AlertDialogHeader>
              <AlertDialogTitle>{{ deleteTitle }}</AlertDialogTitle>
              <AlertDialogDescription>Avviket blir permanent slettet.</AlertDialogDescription>
            </AlertDialogHeader>
            <AlertDialogFooter>
              <AlertDialogCancel>Avbryt</AlertDialogCancel>
              <AlertDialogAction variant="destructive" @click="emits('delete', deviation.id)">
                Slett
              </AlertDialogAction>
            </AlertDialogFooter>
          </AlertDialogContent>
        </AlertDialog>
      </div>
    </div>
  </div>
</template>

<style scoped>
.deviation-card {
  position: relative;
  width: 100%;
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  display: flex;
  flex-direction: column;
  transition: box-shadow 150ms ease;
  cursor: pointer;
}

.deviation-card:hover {
  box-shadow: 0 6px 14px rgb(0 0 0 / 0.08);
}

.card-top {
  display: flex;
  align-items: flex-start;
}

.card-body {
  flex: 1;
  min-width: 0;
  padding: 14px 16px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}

.description {
  margin: 6px 0 4px;
  font-size: 1.05rem;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-label {
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
}

.card-actions {
  display: flex;
  align-items: flex-start;
  padding: 12px 12px 0 0;
  flex-shrink: 0;
}

.actions-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.75rem;
  height: 2.75rem;
  border-radius: var(--radius-md);
  border: none;
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 150ms ease;
}

.actions-trigger:hover {
  background: hsl(var(--accent));
  color: hsl(var(--foreground));
}

.menu-item--status-open {
  color: var(--red);
}

.menu-item--status-progress {
  color: var(--amber);
}

.menu-item--status-closed {
  color: var(--green);
}

.menu-item--danger {
  color: var(--red);
}

.menu-item--danger:hover {
  background-color: var(--red-soft) !important;
  color: var(--red) !important;
}
</style>

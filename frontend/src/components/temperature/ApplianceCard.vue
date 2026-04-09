<script setup lang="ts">
import {
  MoreVertical,
  Pencil,
  Power,
  PowerOff,
  Refrigerator,
  Snowflake,
  Trash2
} from 'lucide-vue-next'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {formatThreshold} from '@/composables/useTemperatureMonitoring'
import type {TemperatureApplianceWithLastEntry} from '@/types/temperature'

defineProps<{
  appliance: TemperatureApplianceWithLastEntry
}>()

const emits = defineEmits<{
  (e: 'edit', appliance: TemperatureApplianceWithLastEntry): void
  (e: 'toggle-active', appliance: TemperatureApplianceWithLastEntry): void
  (e: 'delete', appliance: TemperatureApplianceWithLastEntry): void
}>()

function toShortDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return new Intl.DateTimeFormat('nb-NO', {
    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
  }).format(date)
}
</script>

<template>
  <article :class="['device-card', { 'device-card--inactive': !appliance.isActive }]">
    <div class="device-card-head">
      <div class="device-icon-wrap">
        <Refrigerator v-if="appliance.type === 'FRIDGE'" aria-hidden="true"/>
        <Snowflake v-else aria-hidden="true"/>
      </div>
      <div>
        <h3>{{ appliance.name }}</h3>
      </div>
      <div class="device-meta-actions">
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button variant="ghost" size="icon-sm" class="actions-trigger"
                    aria-label="Åpne handlinger">
              <MoreVertical :size="18" aria-hidden="true"/>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" :side-offset="4">
            <DropdownMenuItem @click="emits('edit', appliance)">
              <Pencil :size="16" aria-hidden="true"/>
              Rediger
            </DropdownMenuItem>
            <DropdownMenuItem @click="emits('toggle-active', appliance)">
              <PowerOff v-if="appliance.isActive" :size="16" aria-hidden="true"/>
              <Power v-else :size="16" aria-hidden="true"/>
              {{ appliance.isActive ? 'Sett inaktiv' : 'Aktiver' }}
            </DropdownMenuItem>
            <DropdownMenuSeparator/>
            <DropdownMenuItem class="menu-item--danger" @click="emits('delete', appliance)">
              <Trash2 :size="16" aria-hidden="true"/>
              Slett enhet
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </div>

    <Badge class="device-status-badge" :tone="appliance.isActive ? 'ok' : 'neutral'">
      {{ appliance.isActive ? 'Aktiv' : 'Inaktiv' }}
    </Badge>

    <dl class="device-facts">
      <div>
        <dt>Grense</dt>
        <dd>{{ formatThreshold(appliance.threshold) }}</dd>
      </div>
      <div>
        <dt>Siste temp</dt>
        <dd v-if="appliance.lastEntry">{{ appliance.lastEntry.temperature.toFixed(1) }}°C</dd>
        <dd v-else>-</dd>
      </div>
      <div>
        <dt>Sist registrert</dt>
        <dd v-if="appliance.lastEntry">{{ toShortDateTime(appliance.lastEntry.measuredAt) }}</dd>
        <dd v-else>Ingen måling</dd>
      </div>
      <div>
        <dt>Status</dt>
        <dd>
          <Badge v-if="appliance.lastEntry"
                 :tone="appliance.lastEntry.status === 'OK' ? 'ok' : 'danger'">
            {{ appliance.lastEntry.status === 'OK' ? 'OK' : 'Avvik' }}
          </Badge>
          <span v-else class="muted">Ingen data</span>
        </dd>
      </div>
    </dl>
  </article>
</template>

<style scoped>
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

@media (max-width: 600px) {
  .device-card {
    aspect-ratio: auto;
  }
}
</style>

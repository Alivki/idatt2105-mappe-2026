<script setup lang="ts">
import {Thermometer} from 'lucide-vue-next'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import Checkbox from '@/components/ui/checkbox/Checkbox.vue'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow
} from '@/components/ui/table'

interface LogEntry {
  id: number
  measuredAt: string
  applianceName: string
  applianceType: string
  temperature: number
  threshold: string
  measuredBy: string
  status: string
  statusTone: 'ok' | 'danger'
  note?: string | null
}

defineProps<{
  entries: LogEntry[]
  totalEntries: number
  canManage: boolean
  selectedEntryIds: number[]
  allRowsSelected: boolean
  isMobile: boolean
  paginationSummary: string
  currentPage: number
  totalPages: number
}>()

const emits = defineEmits<{
  (e: 'toggle-entry', id: number, checked: boolean): void
  (e: 'toggle-all', checked: boolean): void
  (e: 'delete-selected'): void
  (e: 'prev-page'): void
  (e: 'next-page'): void
}>()

function formatDateTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return new Intl.DateTimeFormat('nb-NO', {
    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
  }).format(date)
}
</script>

<template>
  <article class="log-panel">
    <div class="panel-header">
      <div>
        <h2>Siste registreringer</h2>
        <p>En oversikt som viser temp, grense, ansvarlig og om målingen ga avvik.</p>
      </div>
      <Button v-if="canManage" variant="outline" size="sm" :disabled="selectedEntryIds.length === 0"
              @click="emits('delete-selected')">
        Slett valgte ({{ selectedEntryIds.length }})
      </Button>
    </div>

    <div class="log-table-scroll">
      <div v-if="isMobile" class="mobile-entries">
        <article
          v-for="entry in entries"
          :key="entry.id"
          class="mobile-entry"
          :class="entry.status === 'DEVIATION' ? 'mobile-entry--deviation' : ''"
        >
          <div v-if="canManage" class="mobile-entry-row mobile-entry-row--checkbox">
            <Checkbox
              :checked="selectedEntryIds.includes(entry.id)"
              @update:checked="(checked) => emits('toggle-entry', entry.id, checked)"
            />
          </div>
          <div class="mobile-entry-row">
            <span>Tidspunkt</span>
            <strong>{{ formatDateTime(entry.measuredAt) }}</strong>
          </div>
          <div class="mobile-entry-row">
            <span>Enhet</span>
            <strong>{{ entry.applianceName }}</strong>
          </div>
          <div class="mobile-entry-row">
            <span>Type</span>
            <span>{{ entry.applianceType === 'FRIDGE' ? 'Kjøleskap' : 'Fryser' }}</span>
          </div>
          <div class="mobile-entry-row">
            <span>Temp</span>
            <strong :class="entry.status === 'DEVIATION' ? 'danger-text' : 'ok-text'">{{
                entry.temperature.toFixed(1)
              }}°C</strong>
          </div>
          <div class="mobile-entry-row">
            <span>Grense</span>
            <span>{{ entry.threshold }}</span>
          </div>
          <div class="mobile-entry-row">
            <span>Registrert av</span>
            <span>{{ entry.measuredBy }}</span>
          </div>
          <div class="mobile-entry-row">
            <span>Status</span>
            <Badge :tone="entry.statusTone">{{ entry.status === 'OK' ? 'OK' : 'Avvik' }}</Badge>
          </div>
          <div v-if="entry.note" class="mobile-entry-note">
            {{ entry.note }}
          </div>
        </article>
        <div v-if="entries.length === 0" class="table-empty-state">
          <div class="table-empty-icon">
            <Thermometer :stroke-width="1.5" aria-hidden="true"/>
          </div>
          <div class="table-empty-text">
            <h2>Ingen temperaturregistreringer enda</h2>
            <p>Registrer den første målingen i panelet til venstre.</p>
          </div>
        </div>
      </div>

      <Table v-else class="temperature-table">
        <TableHeader>
          <TableRow>
            <TableHead v-if="canManage">
              <Checkbox :checked="allRowsSelected"
                        @update:checked="(checked) => emits('toggle-all', checked)"/>
            </TableHead>
            <TableHead>Tidspunkt</TableHead>
            <TableHead>Enhet</TableHead>
            <TableHead>Temp</TableHead>
            <TableHead>Grense</TableHead>
            <TableHead>Registrert av</TableHead>
            <TableHead>Status</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableEmpty v-if="totalEntries === 0" :colspan="canManage ? 7 : 6">
            <div class="table-empty-state">
              <div class="table-empty-icon">
                <Thermometer :stroke-width="1.5" aria-hidden="true"/>
              </div>
              <div class="table-empty-text">
                <h2>Ingen temperaturregistreringer enda</h2>
                <p>Registrer den første målingen i panelet til venstre.</p>
              </div>
            </div>
          </TableEmpty>

          <TableRow v-for="entry in entries" :key="entry.id"
                    :class="entry.status === 'DEVIATION' ? 'row--deviation' : ''">
            <TableCell v-if="canManage" data-label="Velg">
              <Checkbox
                :checked="selectedEntryIds.includes(entry.id)"
                @update:checked="(checked) => emits('toggle-entry', entry.id, checked)"
              />
            </TableCell>
            <TableCell data-label="Tidspunkt">
              <div class="cell-stack">
                <strong>{{ formatDateTime(entry.measuredAt) }}</strong>
                <span>{{ entry.note || 'Måling registrert' }}</span>
              </div>
            </TableCell>
            <TableCell data-label="Enhet">
              <div class="cell-stack">
                <strong>{{ entry.applianceName }}</strong>
                <span>{{ entry.applianceType === 'FRIDGE' ? 'Kjøleskap' : 'Fryser' }}</span>
              </div>
            </TableCell>
            <TableCell data-label="Temp">
              <strong :class="entry.status === 'DEVIATION' ? 'danger-text' : 'ok-text'">{{
                  entry.temperature.toFixed(1)
                }}°C</strong>
            </TableCell>
            <TableCell data-label="Grense">{{ entry.threshold }}</TableCell>
            <TableCell data-label="Registrert av">{{ entry.measuredBy }}</TableCell>
            <TableCell data-label="Status">
              <Badge :tone="entry.statusTone">{{ entry.status === 'OK' ? 'OK' : 'Avvik' }}</Badge>
            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </div>

    <div class="pagination-row">
      <span>{{ paginationSummary }}</span>
      <div class="pagination-actions">
        <Button variant="outline" size="sm" :disabled="currentPage <= 1"
                @click="emits('prev-page')">
          Forrige
        </Button>
        <span>Side {{ currentPage }} av {{ totalPages }}</span>
        <Button variant="outline" size="sm" :disabled="currentPage >= totalPages"
                @click="emits('next-page')">
          Neste
        </Button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.log-panel {
  border-radius: 14px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  box-shadow: 0 1px 2px hsl(var(--foreground) / 0.04);
  padding: 1rem;
  min-width: 0;
  height: 680px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  flex-shrink: 0;
}

.panel-header h2 {
  margin-top: 0.5rem;
  font-size: 1.25rem;
}

.panel-header p {
  margin-top: 0.25rem;
  color: hsl(var(--muted-foreground));
}

.log-table-scroll {
  margin-top: 0.75rem;
  overflow: auto;
  flex: 1;
  min-height: 0;
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.75rem;
  color: hsl(var(--muted-foreground));
  font-size: 0.85rem;
}

.pagination-actions {
  display: flex;
  align-items: center;
  gap: 0.55rem;
}

.table-empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 2rem 1rem;
  text-align: center;
  white-space: normal;
  max-width: 20rem;
  margin: 0 auto;
}

.table-empty-icon {
  display: flex;
  height: 3.5rem;
  width: 3.5rem;
  align-items: center;
  justify-content: center;
  border-radius: 0.75rem;
  background-color: hsl(var(--muted));
}

.table-empty-icon :deep(svg) {
  width: 1.5rem;
  height: 1.5rem;
  color: hsl(var(--muted-foreground));
}

.table-empty-text h2 {
  font-size: 0.95rem;
  font-weight: 600;
}

.table-empty-text p {
  color: hsl(var(--muted-foreground));
  font-size: 0.85rem;
  margin-top: 0.15rem;
}

.cell-stack {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.cell-stack span {
  font-size: 0.8rem;
  color: hsl(var(--muted-foreground));
}

.mobile-entries {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.mobile-entry {
  border: 1px solid hsl(var(--border));
  border-radius: 0.65rem;
  background: hsl(var(--card));
  padding: 0.45rem 0.65rem;
}

.mobile-entry--deviation {
  background: var(--red-soft);
}

.mobile-entry-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.34rem 0;
}

.mobile-entry-row > span:first-child {
  font-size: 0.73rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.mobile-entry-row > :last-child {
  text-align: right;
}

.mobile-entry-row--checkbox {
  justify-content: flex-end;
}

.mobile-entry-note {
  margin-top: 0.25rem;
  padding-top: 0.35rem;
  border-top: 1px dashed hsl(var(--border));
  font-size: 0.8rem;
  color: hsl(var(--muted-foreground));
}

.row--deviation {
  background: var(--red-soft);
}

.danger-text {
  color: var(--red);
}

.ok-text {
  color: var(--green);
}

@media (max-width: 1080px) {
  .log-panel {
    height: 560px;
  }
}

@media (max-width: 720px) {
  .panel-header {
    flex-direction: column;
  }

  .log-panel {
    height: auto;
  }

  .log-table-scroll {
    overflow: visible;
  }

  .pagination-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .temperature-table :deep(thead) {
    display: none;
  }

  .temperature-table :deep(tbody) {
    display: flex;
    flex-direction: column;
    gap: 0.6rem;
  }

  .temperature-table :deep(tr.table-row) {
    display: block;
    border: 1px solid hsl(var(--border));
    border-radius: 0.65rem;
    background: hsl(var(--card));
    padding: 0.4rem 0.55rem;
  }

  .temperature-table :deep(td.table-cell) {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 0.75rem;
    width: 100%;
    padding: 0.38rem 0;
    border: none;
  }

  .temperature-table :deep(td.table-cell[data-label='Velg']) {
    justify-content: flex-end;
  }

  .temperature-table :deep(td.table-cell[data-label]::before) {
    content: attr(data-label);
    font-size: 0.74rem;
    font-weight: 600;
    color: hsl(var(--muted-foreground));
    text-transform: uppercase;
    letter-spacing: 0.02em;
    flex-shrink: 0;
  }

  .temperature-table :deep(td.table-cell > *) {
    margin-left: auto;
    text-align: right;
    min-width: 0;
  }

  .temperature-table :deep(td.table-cell .cell-stack) {
    align-items: flex-end;
  }
}
</style>

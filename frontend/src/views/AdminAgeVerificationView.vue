<script setup lang="ts">
import { computed, ref, type Ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowUpDown, AlertTriangle, IdCard, CalendarDays, TrendingUp } from 'lucide-vue-next'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import Button from '@/components/ui/button/Button.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import { CalendarDate } from '@internationalized/date'
import type { DateValue } from '@internationalized/date'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  useDailySummariesQuery,
  useStatsQuery,
} from '@/composables/useAgeVerification'
import type { DailySummaryResponse } from '@/types/ageVerification'

const router = useRouter()

const today = new Date()
const thirtyDaysAgo = new Date(today)
thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30)

const fromDate = ref(new CalendarDate(thirtyDaysAgo.getFullYear(), thirtyDaysAgo.getMonth() + 1, thirtyDaysAgo.getDate())) as Ref<DateValue>
const toDate = ref(new CalendarDate(today.getFullYear(), today.getMonth() + 1, today.getDate())) as Ref<DateValue>

const from = computed(() => {
  const d = fromDate.value
  return `${d.year}-${String(d.month).padStart(2, '0')}-${String(d.day).padStart(2, '0')}`
})
const to = computed(() => {
  const d = toDate.value
  return `${d.year}-${String(d.month).padStart(2, '0')}-${String(d.day).padStart(2, '0')}`
})

const summariesQuery = useDailySummariesQuery(from, to)
const statsQuery = useStatsQuery(from, to)

const summaries = computed(() => summariesQuery.data.value ?? [])
const stats = computed(() => statsQuery.data.value)

const search = ref('')

type SortField = 'date' | 'shifts' | 'ids' | 'deviations'
type SortDir = 'asc' | 'desc'
const sortField = ref<SortField>('date')
const sortDir = ref<SortDir>('desc')

function toggleSort(field: SortField) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = field === 'date' ? 'desc' : 'asc'
  }
}

function formatDate(iso: string): string {
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString('nb-NO', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' })
}

const filteredAndSorted = computed(() => {
  const q = search.value.toLowerCase().trim()
  let list = summaries.value.slice()

  if (q) {
    list = list.filter((s) => formatDate(s.date).toLowerCase().includes(q))
  }

  list.sort((a: DailySummaryResponse, b: DailySummaryResponse) => {
    let cmp = 0
    if (sortField.value === 'date') cmp = a.date.localeCompare(b.date)
    else if (sortField.value === 'shifts') cmp = a.shiftCount - b.shiftCount
    else if (sortField.value === 'ids') cmp = a.totalIdsChecked - b.totalIdsChecked
    else if (sortField.value === 'deviations') cmp = a.totalDeviations - b.totalDeviations
    return sortDir.value === 'desc' ? -cmp : cmp
  })

  return list
})

function navigateToDay(date: string) {
  router.push({ name: 'bevilling-dag', params: { date } })
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
    <section class="header-row">
      <div>
        <h1>Alderskontroll</h1>
        <p>Oversikt over daglig alderskontroll-aktivitet</p>
      </div>
    </section>

    <section v-if="stats" class="cards-group">
      <div class="stat-card stat-card--neutral">
        <div class="stat-card-header">
          <CalendarDays :size="16" class="stat-card-icon" aria-hidden="true" />
          <span class="stat-card-label">Totalt skift</span>
        </div>
        <p class="stat-card-value">{{ stats.totalShifts }}</p>
      </div>
      <div class="stat-card stat-card--resolved">
        <div class="stat-card-header">
          <IdCard :size="16" class="stat-card-icon" aria-hidden="true" />
          <span class="stat-card-label">Leg sjekket</span>
        </div>
        <p class="stat-card-value">{{ stats.totalIdsChecked }}</p>
      </div>
      <div class="stat-card" :class="stats.totalDeviations > 0 ? 'stat-card--open' : 'stat-card--neutral'">
        <div class="stat-card-header">
          <AlertTriangle :size="16" class="stat-card-icon" aria-hidden="true" />
          <span class="stat-card-label">Avvik</span>
        </div>
        <p class="stat-card-value">{{ stats.totalDeviations }}</p>
      </div>
      <div class="stat-card stat-card--in-progress">
        <div class="stat-card-header">
          <TrendingUp :size="16" class="stat-card-icon" aria-hidden="true" />
          <span class="stat-card-label">Snitt leg/skift</span>
        </div>
        <p class="stat-card-value">{{ stats.avgIdsPerShift.toFixed(1) }}</p>
      </div>
    </section>

    <section class="table-section">
      <div class="search-row">
        <div class="search-wrapper">
          <Search :size="16" class="search-icon" aria-hidden="true" />
          <input v-model="search" class="search-input" placeholder="Søk etter dato..." aria-label="Søk etter dato" />
        </div>
        <div class="date-filters">
          <div class="date-label">
            <span>Fra</span>
            <DatePicker v-model="fromDate" placeholder="Fra dato" />
          </div>
          <div class="date-label">
            <span>Til</span>
            <DatePicker v-model="toDate" placeholder="Til dato" />
          </div>
        </div>
      </div>

      <div v-if="summariesQuery.isLoading.value" class="loading-state">
        <div class="skeleton-item" v-for="n in 4" :key="n"></div>
      </div>

      <div v-else-if="summariesQuery.isError.value" class="empty-state">
        <div class="empty-state-inner">
          <div class="empty-state-icon">
            <AlertTriangle :stroke-width="1.5" aria-hidden="true" />
          </div>
          <div class="empty-state-text">
            <h2>Kunne ikke hente data</h2>
            <p>Noe gikk galt under lasting. Prøv igjen senere.</p>
          </div>
        </div>
      </div>

      <div v-else class="table-card">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead class="th-date">
                <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('date')">
                  Dato
                  <ArrowUpDown :size="14" class="sort-icon" aria-hidden="true" :class="{ 'sort-icon--active': sortField === 'date' }" />
                </Button>
              </TableHead>
              <TableHead>
                <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('shifts')">
                  Skift
                  <ArrowUpDown :size="14" class="sort-icon" aria-hidden="true" :class="{ 'sort-icon--active': sortField === 'shifts' }" />
                </Button>
              </TableHead>
              <TableHead>
                <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('ids')">
                  Leg sjekket
                  <ArrowUpDown :size="14" class="sort-icon" aria-hidden="true" :class="{ 'sort-icon--active': sortField === 'ids' }" />
                </Button>
              </TableHead>
              <TableHead>
                <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('deviations')">
                  Avvik
                  <ArrowUpDown :size="14" class="sort-icon" aria-hidden="true" :class="{ 'sort-icon--active': sortField === 'deviations' }" />
                </Button>
              </TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            <TableRow
              v-for="row in filteredAndSorted"
              :key="row.date"
              class="clickable-row"
              @click="navigateToDay(row.date)"
            >
              <TableCell class="cell-bold">{{ formatDate(row.date) }}</TableCell>
              <TableCell class="cell-text">{{ row.shiftCount }}</TableCell>
              <TableCell class="cell-text">{{ row.totalIdsChecked }}</TableCell>
              <TableCell>
                <span :class="row.totalDeviations > 0 ? 'deviation-count' : 'cell-text'">
                  {{ row.totalDeviations }}
                </span>
              </TableCell>
            </TableRow>

            <TableEmpty v-if="filteredAndSorted.length === 0" :colspan="4">
              Ingen data for valgt periode.
            </TableEmpty>
          </TableBody>
        </Table>
      </div>
    </section>
  </div>
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
  gap: 1rem;
}

h1 { margin: 0; font-size: 1.75rem; font-weight: 800; letter-spacing: -0.03em; }
.header-row p { margin-top: 4px; color: var(--text-secondary); font-size: 0.88rem; }

.date-filters {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.date-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  color: hsl(var(--muted-foreground));
}

.cards-group {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  border-radius: var(--radius-lg);
  padding: 16px;
  border: 2px solid hsl(var(--border));
  background: hsl(var(--card));
  min-height: 6.25rem;
}

.stat-card-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-card-icon {
  flex-shrink: 0;
  opacity: 0.6;
}

.stat-card-label {
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
  font-weight: 500;
}

.stat-card-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
  margin: 0;
}

.stat-card--neutral { background: var(--card-bg); border-color: hsl(var(--border)); }
.stat-card--neutral .stat-card-value { color: hsl(var(--foreground)); }
.stat-card--neutral .stat-card-icon { color: hsl(var(--muted-foreground)); }

.stat-card--resolved { background: var(--green-soft); border-color: color-mix(in srgb, var(--green-soft) 50%, var(--green) 20%); }
.stat-card--resolved .stat-card-value { color: var(--green); }
.stat-card--resolved .stat-card-icon { color: var(--green); }

.stat-card--open { background: var(--red-soft); border-color: color-mix(in srgb, var(--red-soft) 50%, var(--red) 20%); }
.stat-card--open .stat-card-value { color: var(--red); }
.stat-card--open .stat-card-icon { color: var(--red); }

.stat-card--in-progress { background: var(--amber-soft); border-color: color-mix(in srgb, var(--amber-soft) 50%, var(--amber) 20%); }
.stat-card--in-progress .stat-card-value { color: var(--amber); }
.stat-card--in-progress .stat-card-icon { color: var(--amber); }

.search-wrapper { position: relative; flex: 1; max-width: 20rem; }

.search-icon {
  position: absolute; left: 0.65rem; top: 50%; transform: translateY(-50%);
  color: hsl(var(--muted-foreground)); pointer-events: none;
}

.search-input {
  width: 100%; height: 2.5rem; border: 1px solid hsl(var(--border)); border-radius: 0.5rem;
  padding: 0 0.75rem 0 2.1rem; font: inherit; font-size: 0.85rem;
  background: hsl(var(--card)); color: hsl(var(--foreground)); box-sizing: border-box;
}

.search-input:focus {
  outline: none; border-color: hsl(var(--ring));
  box-shadow: 0 0 0 2px hsl(var(--ring) / 0.15);
}

.table-section { display: flex; flex-direction: column; gap: 0.75rem; }
.search-row { display: flex; align-items: flex-end; gap: 1.5rem; }

.table-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
}

.sort-btn {
  display: inline-flex; align-items: center; gap: 0.35rem;
  background: none; border: none; font: inherit; font-weight: 500;
  color: hsl(var(--muted-foreground)); cursor: pointer;
  padding: 0.25rem 0.4rem; border-radius: var(--radius-md);
  margin: -0.25rem -0.4rem; transition: background 150ms ease, color 150ms ease;
}

.sort-btn:hover {
  background: hsl(var(--accent));
  color: hsl(var(--foreground));
}

.sort-icon { opacity: 0.4; transition: opacity 150ms; }
.sort-icon--active { opacity: 1; }

.clickable-row { cursor: pointer; }
.clickable-row:hover { background: hsl(var(--accent) / 0.5); }

.cell-bold { font-weight: 600; color: hsl(var(--foreground)); }
.cell-text { color: hsl(var(--muted-foreground)); }
.deviation-count { color: var(--red); font-weight: 600; }

.th-date { min-width: 12rem; }

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-item {
  height: 56px;
  border-radius: var(--radius-xl);
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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
.empty-state-text h2 { font-size: 1.125rem; font-weight: 600; letter-spacing: -0.01em; }
.empty-state-text p { max-width: 24rem; font-size: 0.875rem; color: hsl(var(--muted-foreground)); margin-top: 0.25rem; }

@media (max-width: 1100px) {
  .cards-group { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 768px) {
  h1 { font-size: 1.5rem; }
  .cards-group { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .header-row { flex-direction: column; }
  .search-row { flex-direction: column; align-items: stretch; gap: 0.75rem; }
  .date-filters { width: 100%; }
  .search-wrapper { max-width: none; }
}
</style>

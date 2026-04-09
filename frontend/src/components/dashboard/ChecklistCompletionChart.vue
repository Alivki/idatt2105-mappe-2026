<script setup lang="ts">
import {ref, computed} from 'vue'
import {Line} from 'vue-chartjs'
import {ClipboardList} from 'lucide-vue-next'
import type {Checklist, CompletionHistoryEntry} from '@/types/checklist'

const props = defineProps<{
  checklists: Checklist[]
  completionHistory: CompletionHistoryEntry[]
}>()

type RangeKey = '1w' | '1m'
const activeRange = ref<RangeKey>('1w')

const rangeOptions: { key: RangeKey; label: string }[] = [
  {key: '1w', label: 'Uke'},
  {key: '1m', label: 'Måned'},
]

const dailyChecklists = computed(() =>
  props.checklists.filter((c) => c.active && c.frequency === 'DAILY'),
)

const hasData = computed(() => dailyChecklists.value.length > 0)

const chartData = computed(() => {
  const days = activeRange.value === '1w' ? 7 : 30
  const now = new Date()
  const labels: string[] = []
  const data: number[] = []

  const totalDaily = dailyChecklists.value.length

  if (totalDaily === 0) {
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date(now)
      d.setDate(d.getDate() - i)
      labels.push(formatLabel(d))
      data.push(0)
    }
  } else {
    const completionsByDate: Record<string, Set<number>> = {}
    const dailyIds = new Set(dailyChecklists.value.map((c) => c.id))

    for (const entry of props.completionHistory) {
      if (!dailyIds.has(entry.checklistId)) continue
      const dateKey = entry.completedAt.slice(0, 10)
      if (!completionsByDate[dateKey]) {
        completionsByDate[dateKey] = new Set()
      }
      completionsByDate[dateKey]!.add(entry.checklistId)
    }

    for (let i = days - 1; i >= 0; i--) {
      const d = new Date(now)
      d.setDate(d.getDate() - i)
      const dateKey = d.toISOString().slice(0, 10)
      labels.push(formatLabel(d))

      const completedCount = completionsByDate[dateKey]?.size ?? 0
      const rate = Math.round((completedCount / totalDaily) * 100)
      data.push(Math.min(100, rate))
    }
  }

  const step = activeRange.value === '1w' ? 1 : Math.max(1, Math.floor(days / 7))
  const thinLabels = labels.map((l, i) => (i % step === 0 ? l : ''))

  return {
    labels: thinLabels,
    datasets: [{
      label: 'Fullføringsgrad',
      data,
      borderColor: '#4f4bcf',
      backgroundColor: 'rgba(79, 75, 207, 0.06)',
      fill: true,
      tension: 0.4,
      pointRadius: activeRange.value === '1w' ? 5 : 3,
      pointHoverRadius: 6,
      pointBackgroundColor: '#4f4bcf',
      pointBorderColor: '#fff',
      pointBorderWidth: 2,
      borderWidth: 2.5,
    }],
  }
})

function formatLabel(d: Date): string {
  if (activeRange.value === '1w') {
    return d.toLocaleDateString('nb-NO', {weekday: 'short', day: 'numeric'})
  }
  return d.toLocaleDateString('nb-NO', {day: 'numeric', month: 'short'})
}

const avgCompletion = computed(() => {
  const dataset = chartData.value.datasets[0]
  if (!dataset) return 0
  const values = dataset.data
  if (values.length === 0) return 0
  return Math.round(values.reduce((a, b) => a + b, 0) / values.length)
})

const completionInsight = computed(() => {
  const avg = avgCompletion.value
  if (avg >= 80) return {
    title: `${avg}% snittrate`,
    desc: 'Stabil og god fullføringsgrad',
    color: 'var(--green)'
  }
  if (avg >= 50) return {
    title: `${avg}% snittrate`,
    desc: 'Noe forbedringspotensial i rutinene',
    color: 'var(--brand)'
  }
  return {
    title: `${avg}% snittrate`,
    desc: 'Lav fullføringsgrad. Gjennomgå rutiner med teamet.',
    color: 'var(--red)'
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {display: false},
    tooltip: {
      backgroundColor: 'hsl(40, 25%, 98%)',
      titleColor: 'hsl(24, 10%, 15%)',
      bodyColor: 'hsl(24, 5%, 46%)',
      borderColor: 'hsl(35, 18%, 88%)',
      borderWidth: 1,
      padding: 12,
      cornerRadius: 8,
      titleFont: {weight: 'bold' as const, size: 13},
      bodyFont: {size: 12},
      callbacks: {
        label: (ctx: { parsed: { y: number | null } }) => `${ctx.parsed.y ?? 0}%`,
      },
    },
  },
  scales: {
    x: {
      grid: {display: false},
      border: {display: false},
      ticks: {
        color: 'hsl(24, 5%, 56%)',
        font: {size: 11},
        maxRotation: 0,
      },
    },
    y: {
      min: 0,
      max: 105,
      border: {display: false},
      grid: {
        color: 'rgba(0, 0, 0, 0.05)',
      },
      ticks: {
        color: 'hsl(24, 5%, 56%)',
        font: {size: 11},
        callback: (v: string | number) => `${v}%`,
        stepSize: 25,
      },
    },
  },
}
</script>

<template>
  <div class="chart-card">
    <div class="chart-header">
      <div>
        <h3 class="chart-title">Sjekkliste-fullførelse</h3>
        <span class="chart-subtitle">Snitt {{ avgCompletion }}% fullført</span>
      </div>
      <div class="toggle-group">
        <button
          v-for="opt in rangeOptions"
          :key="opt.key"
          class="toggle-btn"
          :class="{ active: activeRange === opt.key }"
          @click="activeRange = opt.key"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <!-- Empty state -->
    <div v-if="!hasData" class="empty-state">
      <div class="empty-icon">
        <ClipboardList :size="28"/>
      </div>
      <p class="empty-heading">Ingen aktive sjekklister</p>
      <p class="empty-body">Opprett sjekklister for å se fullføringsgraden over tid.</p>
    </div>

    <div v-else class="chart-container">
      <Line :data="chartData" :options="chartOptions"/>
    </div>

    <div v-if="hasData" class="chart-status">
      <span class="status-dot" :style="{ background: completionInsight.color }"/>
      <span class="status-text">{{ completionInsight.title }} — {{ completionInsight.desc }}</span>
    </div>
  </div>
</template>

<style scoped>
.chart-card {
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  padding: 1.25rem 1.5rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.chart-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.chart-subtitle {
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
}

.toggle-group {
  display: flex;
  background: hsl(var(--secondary));
  border-radius: var(--radius-md);
  padding: 3px;
  gap: 2px;
}

.toggle-btn {
  padding: 5px 12px;
  font-size: 0.78rem;
  font-weight: 500;
  border: none;
  background: transparent;
  border-radius: calc(var(--radius-md) - 2px);
  cursor: pointer;
  color: hsl(var(--muted-foreground));
  transition: all 150ms ease;
}

.toggle-btn.active {
  background: var(--card-bg);
  color: hsl(var(--foreground));
  box-shadow: var(--shadow-sm);
}

.chart-container {
  position: relative;
  flex: 1;
  min-height: 160px;
}

.chart-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 4px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-text {
  font-size: 0.78rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.3;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  gap: 8px;
}

.empty-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: hsl(var(--secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  color: hsl(var(--muted-foreground));
  margin-bottom: 4px;
}

.empty-heading {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.empty-body {
  margin: 0;
  font-size: 0.78rem;
  color: hsl(var(--muted-foreground));
  text-align: center;
  line-height: 1.4;
  max-width: 220px;
}

@media (max-width: 640px) {
  .chart-card {
    padding: 1rem;
  }

  .chart-container {
    height: 170px;
  }
}
</style>

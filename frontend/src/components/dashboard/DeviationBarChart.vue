<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import { BarChart3, TrendingDown, TrendingUp, Minus } from 'lucide-vue-next'
import type { FoodDeviation } from '@/types/deviation'
import type { AlcoholDeviation } from '@/types/deviation'

const props = defineProps<{
  foodDeviations: FoodDeviation[]
  alcoholDeviations: AlcoholDeviation[]
}>()

function toDateKey(dateStr: string): string {
  return dateStr.slice(0, 10)
}

const chartData = computed(() => {
  const now = new Date()
  const dayOfWeek = now.getDay()
  const monday = new Date(now)
  monday.setDate(now.getDate() - ((dayOfWeek + 6) % 7))
  monday.setHours(0, 0, 0, 0)

  const labels: string[] = []
  const dateKeys: string[] = []
  const dayNames = ['Man', 'Tir', 'Ons', 'Tor', 'Fre', 'Lør', 'Søn']

  for (let i = 0; i < 7; i++) {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    labels.push(dayNames[i]!)
    dateKeys.push(d.toISOString().slice(0, 10))
  }

  const foodByDate: Record<string, number> = {}
  const alcoholByDate: Record<string, number> = {}

  for (const d of props.foodDeviations) {
    const key = toDateKey(d.reportedAt)
    foodByDate[key] = (foodByDate[key] ?? 0) + 1
  }
  for (const d of props.alcoholDeviations) {
    const key = toDateKey(d.reportedAt)
    alcoholByDate[key] = (alcoholByDate[key] ?? 0) + 1
  }

  const foodData = dateKeys.map((k) => foodByDate[k] ?? 0)
  const alcoholData = dateKeys.map((k) => alcoholByDate[k] ?? 0)

  return {
    labels,
    datasets: [
      {
        label: 'IK-Mat',
        data: foodData,
        backgroundColor: '#7c78d9',
        borderRadius: 6,
        barPercentage: 0.7,
        categoryPercentage: 0.65,
      },
      {
        label: 'IK-Alkohol',
        data: alcoholData,
        backgroundColor: '#2d2a6e',
        borderRadius: 6,
        barPercentage: 0.7,
        categoryPercentage: 0.65,
      },
    ],
  }
})

const weekLabel = computed(() => {
  const now = new Date()
  const dayOfWeek = now.getDay()
  const monday = new Date(now)
  monday.setDate(now.getDate() - ((dayOfWeek + 6) % 7))
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)

  const fmt = (d: Date) => d.toLocaleDateString('nb-NO', { day: 'numeric', month: 'short' })
  return `${fmt(monday)} – ${fmt(sunday)}`
})

const hasData = computed(() =>
  props.foodDeviations.length > 0 || props.alcoholDeviations.length > 0,
)

const totalFood = computed(() =>
  chartData.value.datasets[0]?.data.reduce((a, b) => a + b, 0) ?? 0,
)

const totalAlcohol = computed(() =>
  chartData.value.datasets[1]?.data.reduce((a, b) => a + b, 0) ?? 0,
)

const totalThisWeek = computed(() => totalFood.value + totalAlcohol.value)

const trendInfo = computed(() => {
  const total = totalThisWeek.value
  if (total === 0) return {
    icon: TrendingDown,
    title: 'Ingen avvik',
    description: 'Ingen registrerte avvik denne uken. Godt arbeid!',
    color: 'var(--green)',
    bg: 'var(--green-soft)',
  }
  if (total <= 2) return {
    icon: Minus,
    title: `${total} avvik denne uken`,
    description: 'Normalt nivå. Fortsett med gode rutiner.',
    color: 'var(--brand)',
    bg: 'var(--brand-soft)',
  }
  return {
    icon: TrendingUp,
    title: `${total} avvik denne uken`,
    description: 'Høyere enn normalt. Gjennomgå rutiner og tiltak.',
    color: 'var(--red)',
    bg: 'var(--red-soft)',
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  interaction: {
    mode: 'index' as const,
    intersect: false,
  },
  plugins: {
    legend: { display: false },
    tooltip: {
      backgroundColor: 'hsl(40, 25%, 98%)',
      titleColor: 'hsl(24, 10%, 15%)',
      bodyColor: 'hsl(24, 5%, 46%)',
      borderColor: 'hsl(35, 18%, 88%)',
      borderWidth: 1,
      padding: 12,
      cornerRadius: 8,
      titleFont: { weight: 'bold' as const, size: 13 },
      bodyFont: { size: 12 },
      usePointStyle: true,
      boxPadding: 6,
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { display: false },
      ticks: {
        color: 'hsl(24, 5%, 56%)',
        font: { size: 12 },
      },
    },
    y: {
      beginAtZero: true,
      border: { display: false },
      grid: {
        color: 'rgba(0, 0, 0, 0.05)',
      },
      ticks: {
        color: 'hsl(24, 5%, 56%)',
        font: { size: 11 },
        stepSize: 1,
      },
      grace: '15%',
    },
  },
}
</script>

<template>
  <div class="chart-card">
    <div class="chart-header">
      <div class="chart-header-left">
        <h3 class="chart-title">Avvik denne uken</h3>
        <span class="chart-subtitle">{{ weekLabel }}</span>
      </div>
      <div v-if="hasData" class="chart-header-right">
        <div class="stat-pill">
          <span class="stat-dot" style="background: #7c78d9" />
          <span class="stat-label">IK-Mat</span>
          <strong class="stat-value">{{ totalFood }}</strong>
        </div>
        <div class="stat-pill">
          <span class="stat-dot" style="background: #2d2a6e" />
          <span class="stat-label">IK-Alkohol</span>
          <strong class="stat-value">{{ totalAlcohol }}</strong>
        </div>
      </div>
    </div>

    <div v-if="!hasData" class="empty-state">
      <div class="empty-icon">
        <BarChart3 :size="28" />
      </div>
      <p class="empty-heading">Ingen avvik registrert</p>
      <p class="empty-body">Avvik denne uken vises her.</p>
    </div>

    <div v-else class="chart-container">
      <Bar :data="chartData" :options="chartOptions" />
    </div>

    <div v-if="hasData" class="chart-bottom">
      <div class="chart-footer" :style="{ background: trendInfo.bg }">
        <div class="footer-icon" :style="{ color: trendInfo.color }">
          <component :is="trendInfo.icon" :size="18" />
        </div>
        <div class="footer-text">
          <strong class="footer-title" :style="{ color: trendInfo.color }">{{ trendInfo.title }}</strong>
          <span class="footer-desc">{{ trendInfo.description }}</span>
        </div>
      </div>

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

.chart-header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
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

.chart-header-right {
  display: flex;
  gap: 1px;
}

.stat-pill {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 20px;
  background: hsl(var(--secondary));
  gap: 2px;
  min-width: 90px;
}

.stat-pill:first-child {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
}

.stat-pill:last-child {
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.stat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 0.72rem;
  color: hsl(var(--muted-foreground));
  white-space: nowrap;
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  gap: 8px;
  flex: 1;
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
  max-width: 200px;
}

.chart-bottom {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: auto;
}

.chart-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: var(--radius-md);
}

.footer-icon {
  flex-shrink: 0;
}

.footer-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.footer-title {
  font-size: 0.88rem;
  font-weight: 700;
}

.footer-desc {
  font-size: 0.78rem;
  color: hsl(var(--muted-foreground));
}

.chart-container {
  position: relative;
  height: 200px;
}

@media (max-width: 640px) {
  .chart-card {
    padding: 1rem;
  }

  .chart-header {
    flex-direction: column;
  }

  .chart-header-right {
    align-self: flex-start;
  }

  .chart-container {
    height: 180px;
  }

  .stat-pill {
    padding: 6px 12px;
  }

  .stat-value {
    font-size: 1.1rem;
  }
}
</style>

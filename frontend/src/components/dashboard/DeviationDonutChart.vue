<script setup lang="ts">
import {computed} from 'vue'
import {Doughnut} from 'vue-chartjs'
import {CircleSlash} from 'lucide-vue-next'
import type {FoodDeviation} from '@/types/deviation'
import type {AlcoholDeviation} from '@/types/deviation'

const props = defineProps<{
  foodDeviations: FoodDeviation[]
  alcoholDeviations: AlcoholDeviation[]
}>()

const statusCounts = computed(() => {
  const counts = {open: 0, underTreatment: 0, closed: 0}
  for (const d of props.foodDeviations) {
    if (d.status === 'OPEN') counts.open++
    else if (d.status === 'UNDER_TREATMENT') counts.underTreatment++
    else counts.closed++
  }
  for (const d of props.alcoholDeviations) {
    if (d.status === 'OPEN') counts.open++
    else if (d.status === 'UNDER_TREATMENT') counts.underTreatment++
    else counts.closed++
  }
  return counts
})

const total = computed(() => {
  const c = statusCounts.value
  return c.open + c.underTreatment + c.closed
})

const closedPercent = computed(() => {
  if (total.value === 0) return 0
  return Math.round((statusCounts.value.closed / total.value) * 100)
})

const footerInfo = computed(() => {
  const pct = closedPercent.value
  if (pct >= 80) return {
    label: `${pct}% lukket`,
    desc: 'God oppfølging av avvik',
    color: 'var(--green)',
    bg: 'var(--green-soft)'
  }
  if (pct >= 50) return {
    label: `${pct}% lukket`,
    desc: 'Noen avvik gjenstår',
    color: 'var(--brand)',
    bg: 'var(--brand-soft)'
  }
  return {
    label: `${pct}% lukket`,
    desc: 'Flere åpne avvik krever oppfølging',
    color: 'var(--red)',
    bg: 'var(--red-soft)'
  }
})

const chartData = computed(() => ({
  labels: ['Åpne', 'Under behandling', 'Lukket'],
  datasets: [{
    data: [
      statusCounts.value.open,
      statusCounts.value.underTreatment,
      statusCounts.value.closed,
    ],
    backgroundColor: [
      '#2d2a6e',
      '#5753b5',
      '#bcbae6',
    ],
    borderWidth: 0,
    hoverOffset: 4,
  }],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  cutout: '68%',
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
    },
  },
}
</script>

<template>
  <div class="chart-card">
    <h3 class="chart-title">Avviksfordeling</h3>
    <span class="chart-subtitle">Alle registrerte avvik</span>

    <!-- Empty state -->
    <template v-if="total === 0">
      <div class="empty-state">
        <div class="empty-icon">
          <CircleSlash :size="32"/>
        </div>
        <p class="empty-heading">Ingen avvik registrert</p>
        <p class="empty-body">Når avvik registreres vil fordelingen vises her.</p>
      </div>
    </template>

    <!-- Chart -->
    <template v-else>
      <div class="donut-wrapper">
        <div class="donut-container">
          <Doughnut :data="chartData" :options="chartOptions"/>
          <div class="donut-center">
            <strong class="donut-total">{{ total }}</strong>
            <span class="donut-label">Avvik</span>
          </div>
        </div>
      </div>

      <div class="legend">
        <div class="legend-item">
          <span class="legend-dot" style="background: #2d2a6e"/>
          <span class="legend-text">Åpne</span>
          <strong class="legend-value">{{ statusCounts.open }}</strong>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #5753b5"/>
          <span class="legend-text">Under behandling</span>
          <strong class="legend-value">{{ statusCounts.underTreatment }}</strong>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background: #bcbae6"/>
          <span class="legend-text">Lukket</span>
          <strong class="legend-value">{{ statusCounts.closed }}</strong>
        </div>
      </div>

      <div class="chart-status">
        <span class="status-dot" :style="{ background: footerInfo.color }"/>
        <span class="status-text">{{ footerInfo.label }} — {{ footerInfo.desc }}</span>
      </div>
    </template>
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
  gap: 10px;
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
  margin-top: -4px;
}

/* ── Empty state ── */

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
  width: 56px;
  height: 56px;
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

/* ── Donut ── */

.donut-wrapper {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.donut-container {
  position: relative;
  width: 180px;
  height: 180px;
}

.donut-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.donut-total {
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1;
}

.donut-label {
  font-size: 0.78rem;
  color: hsl(var(--muted-foreground));
  margin-top: 2px;
}

.legend {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-text {
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
  flex: 1;
}

.legend-value {
  font-size: 0.88rem;
  font-weight: 700;
}

.chart-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 4px;
  margin-top: auto;
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

@media (max-width: 640px) {
  .chart-card {
    padding: 1rem;
  }

  .donut-container {
    width: 150px;
    height: 150px;
  }

  .donut-total {
    font-size: 1.6rem;
  }
}
</style>

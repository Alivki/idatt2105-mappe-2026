<script setup lang="ts">
import {computed} from 'vue'
import {IdCard, AlertTriangle, ShieldCheck, ShieldX, Clock, CalendarCheck} from 'lucide-vue-next'

const props = defineProps<{
  shiftsToday: number
  idsChecked: number
  deviationsToday: number
}>()

const complianceInfo = computed(() => {
  if (props.deviationsToday === 0 && props.idsChecked > 0)
    return {
      title: 'Full kontroll',
      desc: 'Ingen avvik registrert i dag',
      icon: ShieldCheck,
      color: 'var(--green)',
      bg: 'var(--green-soft)'
    }
  if (props.deviationsToday > 0)
    return {
      title: 'Krever tiltak',
      desc: `${props.deviationsToday} avvik trenger oppfølging`,
      icon: ShieldX,
      color: 'var(--red)',
      bg: 'var(--red-soft)'
    }
  return {
    title: 'Venter på data',
    desc: 'Ingen kontroller registrert enda',
    icon: Clock,
    color: 'hsl(var(--muted-foreground))',
    bg: 'hsl(var(--secondary))'
  }
})
</script>

<template>
  <div class="card">
    <h3 class="card-title">Dagens kontroll</h3>

    <div class="stats">
      <div class="stat-block">
        <div class="stat-header">
          <CalendarCheck :size="16" class="stat-icon" style="color: var(--brand)"
                         aria-hidden="true"/>
          <span class="stat-label">Skift i dag</span>
        </div>
        <strong class="stat-value">{{ shiftsToday }}</strong>
      </div>

      <div class="stat-divider"/>

      <div class="stat-block">
        <div class="stat-header">
          <IdCard :size="16" class="stat-icon" style="color: var(--brand)" aria-hidden="true"/>
          <span class="stat-label">ID-kontroller</span>
        </div>
        <strong class="stat-value">{{ idsChecked }}</strong>
      </div>

      <div class="stat-divider"/>

      <div class="stat-block">
        <div class="stat-header">
          <AlertTriangle :size="16" class="stat-icon"
                         :style="{ color: deviationsToday > 0 ? 'var(--red)' : 'var(--green)' }"
                         aria-hidden="true"/>
          <span class="stat-label">Avvik i dag</span>
        </div>
        <strong class="stat-value" :class="{ danger: deviationsToday > 0 }">{{
            deviationsToday
          }}</strong>
      </div>
    </div>

    <div class="card-status">
      <component :is="complianceInfo.icon" :size="14"
                 :style="{ color: complianceInfo.color, flexShrink: 0 }"/>
      <span class="status-text">{{ complianceInfo.title }} — {{ complianceInfo.desc }}</span>
    </div>
  </div>
</template>

<style scoped>
.card {
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-lg);
  background: var(--card-bg);
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
}

.card-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
  justify-content: center;
  padding: 16px 0;
}

.stat-divider {
  height: 1px;
  background: hsl(var(--border));
}

.stat-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-label {
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
  font-weight: 500;
}

.stat-value {
  font-size: 2.2rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1;
}

.stat-value.danger {
  color: var(--red);
}

.card-status {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: auto;
  padding-top: 4px;
  border-top: 1px solid hsl(var(--border));
}

.status-text {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.3;
}
</style>

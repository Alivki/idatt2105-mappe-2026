<script setup lang="ts">
import {ChevronRight} from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'

defineProps<{
  steps: { key: string; label: string; done: boolean; active: boolean }[]
  nextStatusLabel: string | null
  canManage: boolean
  statusChanging: boolean
}>()

defineEmits<{
  (e: 'change-status'): void
}>()
</script>

<template>
  <div class="status-workflow" role="group" aria-label="Avviksstatus">
    <div class="status-track" role="list">
      <div
        v-for="(step, i) in steps"
        :key="step.key"
        class="status-step"
        :class="{ 'status-step--done': step.done, 'status-step--active': step.active }"
        role="listitem"
        :aria-current="step.active ? 'step' : undefined"
      >
        <div class="step-dot" aria-hidden="true">
          <svg v-if="step.done && !step.active" width="12" height="12" viewBox="0 0 24 24"
               fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round"
               stroke-linejoin="round" aria-hidden="true">
            <path d="M20 6 9 17l-5-5"/>
          </svg>
          <span v-else class="step-number">{{ i + 1 }}</span>
        </div>
        <span class="step-label">{{ step.label }}</span>
        <ChevronRight v-if="i < steps.length - 1" :size="14" class="step-arrow" aria-hidden="true"/>
      </div>
    </div>
    <Button
      v-if="canManage && nextStatusLabel"
      size="sm"
      :disabled="statusChanging"
      @click="$emit('change-status')"
      class="status-action-btn"
    >
      {{ statusChanging ? 'Endrer...' : nextStatusLabel }}
    </Button>
  </div>
</template>

<style scoped>
.status-workflow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--card-bg);
}

.status-track {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-step {
  display: flex;
  align-items: center;
  gap: 6px;
}

.step-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--muted));
  color: var(--text-secondary);
  font-size: 0.7rem;
  font-weight: 700;
  flex-shrink: 0;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
}

.status-step--done .step-dot {
  background: var(--green-soft);
  color: var(--green);
}

.status-step--active .step-dot {
  background: var(--brand);
  color: hsl(var(--primary-foreground));
  box-shadow: 0 0 0 3px var(--brand-soft);
}

.step-label {
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--text-secondary);
  white-space: nowrap;
}

.status-step--active .step-label {
  color: hsl(var(--foreground));
  font-weight: 600;
}

.status-step--done .step-label {
  color: var(--green);
}

.step-arrow {
  color: hsl(var(--border));
  flex-shrink: 0;
  margin: 0 2px;
}

.step-number {
  line-height: 1;
}

.status-action-btn {
  flex-shrink: 0;
}

@media (max-width: 860px) {
  .status-workflow {
    flex-direction: column;
    align-items: stretch;
  }

  .status-action-btn {
    width: 100%;
  }
}

@media (max-width: 600px) {
  .status-track {
    flex-wrap: wrap;
  }
}
</style>

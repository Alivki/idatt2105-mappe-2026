<script setup lang="ts">
import {Plus, Minus} from 'lucide-vue-next'

defineProps<{
  count: number
  disabled?: boolean
}>()

const emits = defineEmits<{
  (e: 'increment'): void
  (e: 'decrement'): void
}>()
</script>

<template>
  <div class="counter-row">
    <span class="counter-label">Legitimasjoner sjekket</span>
    <div class="counter">
      <button
        class="counter-btn"
        aria-label="Reduser antall"
        :disabled="count <= 0 || disabled"
        @click="emits('decrement')"
      >
        <Minus :size="22" aria-hidden="true"/>
      </button>
      <span class="counter-value">{{ count }}</span>
      <button
        class="counter-btn"
        aria-label="Øk antall"
        :disabled="disabled"
        @click="emits('increment')"
      >
        <Plus :size="22" aria-hidden="true"/>
      </button>
    </div>
  </div>
</template>

<style scoped>
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

@media (max-width: 400px) {
  .counter-row {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .counter {
    align-self: center;
  }
}
</style>

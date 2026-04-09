<script setup lang="ts">
import {AlertTriangle, Trash2} from 'lucide-vue-next'
import type {AlcoholDeviationType} from '@/types/deviation'
import type {Component} from 'vue'

interface DeviationItem {
  id: number
  deviationType: AlcoholDeviationType
  reportedAt: string
}

defineProps<{
  quickTypes: Array<{ type: AlcoholDeviationType; label: string; icon: Component }>
  deviations: DeviationItem[]
  deviationLabels: Partial<Record<AlcoholDeviationType, string>>
}>()

const emits = defineEmits<{
  (e: 'create', type: AlcoholDeviationType): void
  (e: 'delete', id: number): void
}>()

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('nb-NO', {hour: '2-digit', minute: '2-digit'})
}
</script>

<template>
  <div class="deviation-section">
    <p class="section-label">Registrer avvik</p>
    <div class="deviation-grid">
      <button
        v-for="d in quickTypes"
        :key="d.type"
        class="deviation-btn"
        @click="emits('create', d.type)"
      >
        <component :is="d.icon" :size="18" aria-hidden="true"/>
        <span>{{ d.label }}</span>
      </button>
    </div>
  </div>

  <div v-if="deviations.length > 0" class="deviation-log">
    <p class="section-label">Avvik i dette skiftet ({{ deviations.length }})</p>
    <div class="deviation-list">
      <div v-for="dev in deviations" :key="dev.id" class="deviation-item">
        <AlertTriangle :size="16" class="deviation-item-icon" aria-hidden="true"/>
        <div class="deviation-item-content">
          <span class="deviation-item-type">{{
              deviationLabels[dev.deviationType] ?? dev.deviationType
            }}</span>
          <span class="deviation-item-time">{{ formatTime(dev.reportedAt) }}</span>
        </div>
        <button class="deviation-item-delete" aria-label="Slett avvik"
                @click="emits('delete', dev.id)">
          <Trash2 :size="15" aria-hidden="true"/>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.deviation-section {
  display: flex;
  flex-direction: column;
}

.section-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  margin: 0 0 10px;
}

.deviation-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.deviation-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  font: inherit;
  font-size: 0.85rem;
  font-weight: 500;
  color: hsl(var(--foreground));
  cursor: pointer;
  transition: border-color 150ms ease, background 150ms ease, color 150ms ease;
}

.deviation-btn:hover {
  border-color: var(--red);
  background: var(--red-soft);
  color: var(--red);
}

.deviation-btn:active {
  transform: scale(0.98);
}

.deviation-btn svg {
  flex-shrink: 0;
  opacity: 0.6;
}

.deviation-log {
  display: flex;
  flex-direction: column;
}

.deviation-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.deviation-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
}

.deviation-item-icon {
  flex-shrink: 0;
  color: var(--red);
}

.deviation-item-content {
  flex: 1;
  min-width: 0;
}

.deviation-item-type {
  font-size: 0.85rem;
  font-weight: 500;
}

.deviation-item-time {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  margin-left: 8px;
}

.deviation-item-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  border: none;
  background: none;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  flex-shrink: 0;
  transition: background 150ms ease, color 150ms ease;
}

.deviation-item-delete:hover {
  background: var(--red-soft);
  color: var(--red);
}

@media (max-width: 400px) {
  .deviation-grid {
    grid-template-columns: 1fr;
  }
}
</style>

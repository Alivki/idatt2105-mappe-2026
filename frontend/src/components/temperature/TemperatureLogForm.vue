<script setup lang="ts">
import {Thermometer, TriangleAlert} from 'lucide-vue-next'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Select from '@/components/ui/select/Select.vue'
import SelectContent from '@/components/ui/select/SelectContent.vue'
import SelectItem from '@/components/ui/select/SelectItem.vue'
import SelectTrigger from '@/components/ui/select/SelectTrigger.vue'
import SelectValue from '@/components/ui/select/SelectValue.vue'
import {formatThreshold} from '@/composables/useTemperatureMonitoring'
import type {TemperatureAppliance} from '@/types/temperature'

defineProps<{
  activeAppliances: TemperatureAppliance[]
  selectedApplianceId: number | null
  temperatureInput: string
  note: string
  registeredByName: string
  selectedAppliance: TemperatureAppliance | null
  formStatus: 'OK' | 'DEVIATION' | null
}>()

const emits = defineEmits<{
  (e: 'update:selectedApplianceId', v: number): void
  (e: 'update:temperatureInput', v: string): void
  (e: 'update:note', v: string): void
  (e: 'submit'): void
}>()
</script>

<template>
  <article class="form-panel">
    <div class="panel-header">
      <div>
        <h2>Velg enhet og logg temp</h2>
        <p>Bruk samme liste av registrerte enheter hver gang, så slipper du manuell
          tekstinnskriving.</p>
      </div>
    </div>

    <div v-if="activeAppliances.length === 0" class="empty-warning">
      <TriangleAlert aria-hidden="true"/>
      <div>
        <strong>Ingen aktive enheter</strong>
        <p>Legg til eller aktiver kjøleskap og frysere før du registrerer målinger.</p>
      </div>
    </div>

    <div class="form-grid">
      <div class="field field--full">
        <span>Hvitevare</span>
        <Select :model-value="selectedApplianceId == null ? '' : String(selectedApplianceId)"
                @update:model-value="(v) => emits('update:selectedApplianceId', Number(v))">
          <SelectTrigger :disabled="activeAppliances.length === 0">
            <SelectValue placeholder="Velg enhet"/>
          </SelectTrigger>
          <SelectContent>
            <SelectItem v-for="item in activeAppliances" :key="item.id" :value="String(item.id)">
              {{ item.name }} · {{ item.type === 'FRIDGE' ? 'Kjøleskap' : 'Fryser' }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>

      <label class="field">
        <span>Temperatur (°C)</span>
        <Input :model-value="temperatureInput" type="number" step="0.1"
               placeholder="Skriv inn målt verdi"
               @update:model-value="emits('update:temperatureInput', $event as string)"/>
      </label>

      <label class="field">
        <span>Registrert av</span>
        <Input :model-value="registeredByName" disabled/>
      </label>

      <label class="field field--full">
        <span>Merknad</span>
        <Input :model-value="note" placeholder="Valgfri kommentar, for eksempel årsak til avvik"
               @update:model-value="emits('update:note', $event as string)"/>
      </label>
    </div>

    <div class="status-strip" v-if="selectedAppliance">
      <div>
        <span>Standardgrense</span>
        <strong>{{ formatThreshold(selectedAppliance.threshold) }}</strong>
      </div>
      <div>
        <span>Forventet status</span>
        <Badge :tone="formStatus === 'DEVIATION' ? 'danger' : 'ok'">
          {{ formStatus === 'DEVIATION' ? 'Avvik' : 'OK' }}
        </Badge>
      </div>
    </div>

    <Button class="submit-btn"
            :disabled="activeAppliances.length === 0 || !selectedAppliance || !temperatureInput"
            @click="emits('submit')">
      <Thermometer aria-hidden="true"/>
      Lagre temperatur
    </Button>
  </article>
</template>

<style scoped>
.form-panel {
  border-radius: 14px;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  box-shadow: 0 1px 2px hsl(var(--foreground) / 0.04);
  padding: 1rem;
  min-width: 0;
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

.empty-warning {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.9rem;
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--amber-soft) 65%, var(--amber) 35%);
  background: var(--amber-soft);
  padding: 0.75rem;
}

.empty-warning :deep(svg) {
  width: 1.1rem;
  height: 1.1rem;
  color: var(--amber);
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.empty-warning strong {
  display: block;
  color: var(--amber);
}

.empty-warning p {
  margin-top: 0.15rem;
  color: var(--amber);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0.75rem;
  margin-top: 0.9rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field--full {
  grid-column: 1 / -1;
}

.field span {
  font-size: 0.85rem;
  color: hsl(var(--muted-foreground));
}

.status-strip {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 0.9rem;
  border-radius: 12px;
  background: hsl(var(--muted) / 0.4);
  padding: 0.8rem 0.9rem;
}

.status-strip span {
  display: block;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: hsl(var(--muted-foreground));
}

.status-strip strong {
  margin-top: 0.2rem;
  display: block;
}

.submit-btn {
  margin-top: 0.9rem;
}

.submit-btn :deep(svg) {
  width: 1rem;
  height: 1rem;
}

@media (max-width: 720px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .status-strip {
    flex-direction: column;
  }
}
</style>

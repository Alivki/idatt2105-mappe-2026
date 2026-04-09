<script setup lang="ts">
import type {HazardType, WizardState} from '@/types/haccp-setup'
import {hazardTypeLabels} from '@/types/haccp-setup'
import {ArrowDown, Plus, X, Info} from 'lucide-vue-next'

const wizard = defineModel<WizardState>('wizard', {required: true})

const allHazardTypes: HazardType[] = ['BIOLOGICAL', 'CHEMICAL', 'PHYSICAL', 'ALLERGEN']

const hazardColors: Record<HazardType, string> = {
  BIOLOGICAL: 'var(--red)',
  CHEMICAL: 'var(--amber)',
  PHYSICAL: 'var(--brand)',
  ALLERGEN: '#9333ea',
}

function toggleHazard(stepId: string, hazard: HazardType) {
  const step = wizard.value!.processSteps.find((s) => s.id === stepId)
  if (!step) return
  const idx = step.hazards.indexOf(hazard)
  if (idx >= 0) step.hazards.splice(idx, 1)
  else step.hazards.push(hazard)
}

function toggleKKP(stepId: string) {
  const step = wizard.value!.processSteps.find((s) => s.id === stepId)
  if (step) step.isKKP = !step.isKKP
}

function addStep(afterIndex: number) {
  const newId = String(Date.now())
  wizard.value!.processSteps.splice(afterIndex + 1, 0, {
    id: newId,
    name: '',
    hazards: [],
    isKKP: false,
  })
}

function removeStep(stepId: string) {
  const idx = wizard.value!.processSteps.findIndex((s) => s.id === stepId)
  if (idx >= 0) wizard.value!.processSteps.splice(idx, 1)
}
</script>

<template>
  <div class="step-content">
    <div class="info-banner">
      <Info :size="16" :stroke-width="1.5" aria-hidden="true"/>
      <p>
        Flytskjemaet viser produksjonsprosessen fra varemottak til servering.
        Merk farer og KKP-kandidater for hvert trinn. Vi har forhåndsutfylt basert på svarene dine.
      </p>
    </div>

    <div class="flow-container">
      <div
        v-for="(step, index) in wizard.processSteps"
        :key="step.id"
        class="flow-step-wrapper"
      >
        <div class="flow-step" :class="{ 'flow-step--kkp': step.isKKP }">
          <div class="flow-step-top">
            <span class="flow-step-number">{{ index + 1 }}</span>
            <input
              v-model="step.name"
              type="text"
              class="flow-step-input"
              placeholder="Navn på trinn..."
            />
            <button
              v-if="wizard.processSteps.length > 2"
              class="flow-step-remove"
              title="Fjern"
              @click="removeStep(step.id)"
            >
              <X :size="14" :stroke-width="1.5" aria-hidden="true"/>
            </button>
          </div>

          <div class="flow-step-tags">
            <button
              v-for="hazard in allHazardTypes"
              :key="hazard"
              class="tag"
              :class="{ 'tag--active': step.hazards.includes(hazard) }"
              :style="step.hazards.includes(hazard) ? { color: hazardColors[hazard], borderColor: hazardColors[hazard] + '40' } : {}"
              @click="toggleHazard(step.id, hazard)"
            >
              {{ hazardTypeLabels[hazard] }}
            </button>
            <button
              class="tag tag--kkp"
              :class="{ 'tag--kkp-active': step.isKKP }"
              @click="toggleKKP(step.id)"
            >
              KKP
            </button>
          </div>
        </div>

        <div v-if="index < wizard.processSteps.length - 1" class="flow-connector">
          <ArrowDown :size="14" :stroke-width="1.5" class="flow-arrow" aria-hidden="true"/>
          <button class="flow-add-btn" title="Legg til trinn" @click="addStep(index)">
            <Plus :size="12" :stroke-width="2" aria-hidden="true"/>
          </button>
        </div>
      </div>

      <button class="flow-add-end" @click="addStep(wizard.processSteps.length - 1)">
        <Plus :size="14" :stroke-width="2" aria-hidden="true"/>
        Legg til trinn
      </button>
    </div>
  </div>
</template>

<style scoped>
.step-content {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.info-banner {
  display: flex;
  gap: 0.625rem;
  padding: 0.75rem 1rem;
  background: hsl(var(--primary) / 0.04);
  border: 1px solid hsl(var(--primary) / 0.12);
  border-radius: 0.5rem;
  font-size: 0.8125rem;
  color: hsl(var(--foreground));
  line-height: 1.5;
}

.info-banner svg {
  flex-shrink: 0;
  margin-top: 0.125rem;
  color: hsl(var(--primary));
}

.info-banner p {
  margin: 0;
}

.flow-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flow-step-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flow-step {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: 0.625rem;
  background: hsl(var(--card));
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
}

.flow-step--kkp {
  border-color: var(--amber);
  background: var(--amber-soft);
}

.flow-step-top {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.flow-step-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.5rem;
  height: 1.5rem;
  border-radius: 50%;
  background: hsl(var(--primary) / 0.08);
  color: hsl(var(--primary));
  font-weight: 700;
  font-size: 0.75rem;
  flex-shrink: 0;
}

.flow-step--kkp .flow-step-number {
  background: var(--amber-soft);
  color: var(--amber);
}

.flow-step-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.875rem;
  font-weight: 600;
  color: hsl(var(--foreground));
  outline: none;
  padding: 0.125rem 0;
}

.flow-step-input::placeholder {
  color: hsl(var(--muted-foreground));
  font-weight: 400;
}

.flow-step-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem;
  border: none;
  border-radius: 0.25rem;
  background: transparent;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  opacity: 0.5;
  transition: opacity 0.15s ease, color 0.15s ease;
}

.flow-step-remove:hover {
  opacity: 1;
  color: var(--red);
}

.flow-step-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.tag {
  padding: 0.125rem 0.5rem;
  border: 1px solid hsl(var(--border));
  border-radius: 999px;
  background: hsl(var(--card));
  cursor: pointer;
  font-size: 0.6875rem;
  font-weight: 500;
  color: hsl(var(--muted-foreground));
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
}

.tag:hover {
  border-color: hsl(var(--primary) / 0.3);
}

.tag--active {
  font-weight: 600;
  background: transparent;
}

.tag--kkp {
  border-style: dashed;
}

.tag--kkp-active {
  border-style: solid;
  background: var(--amber-soft);
  border-color: color-mix(in srgb, var(--amber-soft) 50%, var(--amber) 30%);
  color: var(--amber);
  font-weight: 600;
}

.flow-connector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0;
}

.flow-arrow {
  color: hsl(var(--muted-foreground) / 0.4);
}

.flow-add-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.25rem;
  height: 1.25rem;
  border: 1.5px dashed hsl(var(--border));
  border-radius: 50%;
  background: hsl(var(--card));
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
  opacity: 0.5;
}

.flow-add-btn:hover {
  opacity: 1;
  border-color: hsl(var(--primary));
  color: hsl(var(--primary));
}

.flow-add-end {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.375rem 0.875rem;
  border: 1.5px dashed hsl(var(--border));
  border-radius: 999px;
  background: hsl(var(--card));
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  font-size: 0.75rem;
  margin-top: 0.5rem;
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
}

.flow-add-end:hover {
  border-color: hsl(var(--primary));
  color: hsl(var(--primary));
}
</style>

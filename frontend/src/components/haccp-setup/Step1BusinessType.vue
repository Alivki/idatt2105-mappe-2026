<script setup lang="ts">
import type {
  BusinessType,
  BusinessSize,
  FoodType,
  FoodProcess,
  Facility,
  TempEquipment,
  GoodsReceiving,
  WizardState,
  HaccpTrinn,
} from '@/types/haccp-setup'
import {
  businessTypeLabels,
  businessSizeLabels,
  foodTypeLabels,
  foodProcessLabels,
  processTrinnTrigger,
  facilityLabels,
  tempEquipmentLabels,
  goodsReceivingLabels,
} from '@/types/haccp-setup'
import {
  UtensilsCrossed, Coffee, Wine, Store, Truck, Building2,
  Soup, Factory, ShoppingCart, MapPin, Check, ShieldCheck,
} from 'lucide-vue-next'
import type { Component } from 'vue'

const { wizard, haccpTrinn } = defineProps<{
  wizard: WizardState
  haccpTrinn: HaccpTrinn
}>()

const businessTypeOptions: { value: BusinessType; label: string; icon: Component }[] = [
  { value: 'RESTAURANT', label: businessTypeLabels.RESTAURANT, icon: UtensilsCrossed },
  { value: 'CAFE', label: businessTypeLabels.CAFE, icon: Coffee },
  { value: 'BAR', label: businessTypeLabels.BAR, icon: Wine },
  { value: 'KIOSK', label: businessTypeLabels.KIOSK, icon: Store },
  { value: 'CATERING', label: businessTypeLabels.CATERING, icon: Truck },
  { value: 'CANTEEN', label: businessTypeLabels.CANTEEN, icon: Building2 },
  { value: 'INSTITUTION_KITCHEN', label: businessTypeLabels.INSTITUTION_KITCHEN, icon: Soup },
  { value: 'FOOD_PRODUCTION', label: businessTypeLabels.FOOD_PRODUCTION, icon: Factory },
  { value: 'RETAIL', label: businessTypeLabels.RETAIL, icon: ShoppingCart },
  { value: 'MOBILE_VENDOR', label: businessTypeLabels.MOBILE_VENDOR, icon: MapPin },
]

const businessSizeOptions: { value: BusinessSize; label: string }[] = (
  Object.entries(businessSizeLabels) as [BusinessSize, string][]
).map(([value, label]) => ({ value, label }))

const foodTypeOptions: { value: FoodType; label: string }[] = (
  Object.entries(foodTypeLabels) as [FoodType, string][]
).map(([value, label]) => ({ value, label }))

const foodProcessOptions: { value: FoodProcess; label: string; trinn?: 2 | 3 }[] = (
  Object.entries(foodProcessLabels) as [FoodProcess, string][]
).map(([value, label]) => ({ value, label, trinn: processTrinnTrigger[value] }))

const facilityOptions: { value: Facility; label: string }[] = (
  Object.entries(facilityLabels) as [Facility, string][]
).map(([value, label]) => ({ value, label }))

const tempEquipmentOptions: { value: TempEquipment; label: string }[] = (
  Object.entries(tempEquipmentLabels) as [TempEquipment, string][]
).map(([value, label]) => ({ value, label }))

const goodsReceivingOptions: { value: GoodsReceiving; label: string }[] = (
  Object.entries(goodsReceivingLabels) as [GoodsReceiving, string][]
).map(([value, label]) => ({ value, label }))

function toggleFoodType(type: FoodType) {
  const idx = wizard.foodTypes.indexOf(type)
  if (idx >= 0) wizard.foodTypes.splice(idx, 1)
  else wizard.foodTypes.push(type)
}

function toggleProcess(process: FoodProcess) {
  const idx = wizard.processes.indexOf(process)
  if (idx >= 0) wizard.processes.splice(idx, 1)
  else wizard.processes.push(process)
}

function toggleFacility(facility: Facility) {
  const idx = wizard.facilities.indexOf(facility)
  if (idx >= 0) wizard.facilities.splice(idx, 1)
  else wizard.facilities.push(facility)
}

function toggleTempEquipment(equipment: TempEquipment) {
  if (equipment === 'NONE') {
    wizard.temperatureEquipment = wizard.temperatureEquipment.includes('NONE') ? [] : ['NONE']
    return
  }
  const noneIdx = wizard.temperatureEquipment.indexOf('NONE')
  if (noneIdx >= 0) wizard.temperatureEquipment.splice(noneIdx, 1)
  const idx = wizard.temperatureEquipment.indexOf(equipment)
  if (idx >= 0) wizard.temperatureEquipment.splice(idx, 1)
  else wizard.temperatureEquipment.push(equipment)
}
</script>

<template>
  <div class="step-content">
    <!-- Business type -->
    <div class="step-section">
      <h3>Hva slags virksomhet driver du?</h3>
      <div class="option-grid">
        <button
          v-for="opt in businessTypeOptions"
          :key="opt.value"
          class="option-card"
          :class="{ 'option-card--selected': wizard.businessType === opt.value }"
          @click="wizard.businessType = opt.value"
        >
          <component :is="opt.icon" :size="20" :stroke-width="1.5" />
          <span>{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <div class="step-section">
      <h3>Hvor mange ansatte har virksomheten?</h3>
      <div class="option-row">
        <button
          v-for="opt in businessSizeOptions"
          :key="opt.value"
          class="option-pill"
          :class="{ 'option-pill--selected': wizard.businessSize === opt.value }"
          @click="wizard.businessSize = opt.value"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <!-- Food types / raw materials -->
    <div class="step-section">
      <h3>Hvilke råvarer og mattyper håndterer dere?</h3>
      <p class="step-hint">Velg alle som gjelder</p>
      <div class="checkbox-list">
        <button
          v-for="opt in foodTypeOptions"
          :key="opt.value"
          class="checkbox-item"
          :class="{ 'checkbox-item--selected': wizard.foodTypes.includes(opt.value) }"
          @click="toggleFoodType(opt.value)"
        >
          <span class="checkbox-box">
            <Check v-if="wizard.foodTypes.includes(opt.value)" :size="14" :stroke-width="2.5" />
          </span>
          <span>{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <div class="step-section">
      <h3>Hvilke aktiviteter utfører dere?</h3>
      <p class="step-hint">Velg alle som gjelder — noen aktiviteter utløser høyere HACCP-trinn</p>
      <div class="checkbox-list">
        <button
          v-for="opt in foodProcessOptions"
          :key="opt.value"
          class="checkbox-item"
          :class="{ 'checkbox-item--selected': wizard.processes.includes(opt.value) }"
          @click="toggleProcess(opt.value)"
        >
          <span class="checkbox-box">
            <Check v-if="wizard.processes.includes(opt.value)" :size="14" :stroke-width="2.5" />
          </span>
          <span class="checkbox-label-row">
            <span>{{ opt.label }}</span>
            <span v-if="opt.trinn" class="trinn-badge" :class="`trinn-badge--${opt.trinn}`">
              Utløser Trinn {{ opt.trinn }}
            </span>
          </span>
        </button>
      </div>
    </div>

    <!-- Facilities -->
    <div class="step-section">
      <h3>Hvilke fasiliteter har dere?</h3>
      <p class="step-hint">Velg alle som gjelder</p>
      <div class="checkbox-list checkbox-list--compact">
        <button
          v-for="opt in facilityOptions"
          :key="opt.value"
          class="checkbox-item"
          :class="{ 'checkbox-item--selected': wizard.facilities.includes(opt.value) }"
          @click="toggleFacility(opt.value)"
        >
          <span class="checkbox-box">
            <Check v-if="wizard.facilities.includes(opt.value)" :size="14" :stroke-width="2.5" />
          </span>
          <span>{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <div class="step-section">
      <h3>Hvilket utstyr krever temperaturovervåking?</h3>
      <div class="checkbox-list checkbox-list--compact">
        <button
          v-for="opt in tempEquipmentOptions"
          :key="opt.value"
          class="checkbox-item"
          :class="{ 'checkbox-item--selected': wizard.temperatureEquipment.includes(opt.value) }"
          @click="toggleTempEquipment(opt.value)"
        >
          <span class="checkbox-box">
            <Check v-if="wizard.temperatureEquipment.includes(opt.value)" :size="14" :stroke-width="2.5" />
          </span>
          <span>{{ opt.label }}</span>
        </button>
      </div>
    </div>

    <!-- Risk questions -->
    <div class="step-section">
      <h3>Serverer dere sårbare grupper?</h3>
      <p class="step-hint">Barn, eldre, sykehuspasienter eller andre med nedsatt immunforsvar</p>
      <div class="option-row">
        <button
          class="option-pill"
          :class="{ 'option-pill--selected': wizard.servesVulnerableGroups === true }"
          @click="wizard.servesVulnerableGroups = true"
        >Ja</button>
        <button
          class="option-pill"
          :class="{ 'option-pill--selected': wizard.servesVulnerableGroups === false }"
          @click="wizard.servesVulnerableGroups = false"
        >Nei</button>
      </div>
    </div>

    <div class="step-section">
      <h3>Håndterer dere høyrisikoprodukter?</h3>
      <p class="step-hint">Sushi, tartare, rå østers, upasteurisert ost, medium-stekte burgere</p>
      <div class="option-row">
        <button
          class="option-pill"
          :class="{ 'option-pill--selected': wizard.handlesHighRiskProducts === true }"
          @click="wizard.handlesHighRiskProducts = true"
        >Ja</button>
        <button
          class="option-pill"
          :class="{ 'option-pill--selected': wizard.handlesHighRiskProducts === false }"
          @click="wizard.handlesHighRiskProducts = false"
        >Nei</button>
      </div>
    </div>

    <div class="step-section">
      <h3>Hvordan mottar dere varer?</h3>
      <div class="option-row">
        <button
          v-for="opt in goodsReceivingOptions"
          :key="opt.value"
          class="option-pill"
          :class="{ 'option-pill--selected': wizard.goodsReceiving === opt.value }"
          @click="wizard.goodsReceiving = opt.value"
        >
          {{ opt.label }}
        </button>
      </div>
    </div>

    <div class="haccp-trinn-card">
      <div class="haccp-trinn-header">
        <ShieldCheck :size="20" :stroke-width="1.5" />
        <span class="haccp-trinn-label">Ditt HACCP-nivå</span>
      </div>
      <div class="haccp-trinn-level">{{ haccpTrinn.label }}</div>
      <p class="haccp-trinn-desc">{{ haccpTrinn.description }}</p>
    </div>
  </div>
</template>

<style scoped>
.step-content {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
}

.step-section h3 {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
  color: hsl(var(--foreground));
}

.step-hint {
  font-size: 0.8125rem;
  color: hsl(var(--muted-foreground));
  margin-bottom: 0.75rem;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.option-card {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: white;
  cursor: pointer;
  font-size: 0.875rem;
  color: hsl(var(--foreground));
  transition: all 0.15s ease;
  text-align: left;
}

.option-card:hover {
  border-color: hsl(var(--primary) / 0.4);
  background: hsl(var(--primary) / 0.03);
}

.option-card--selected {
  border-color: hsl(var(--primary));
  background: hsl(var(--primary) / 0.06);
  color: hsl(var(--primary));
  font-weight: 500;
}

.option-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.option-pill {
  padding: 0.5rem 1.25rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: 999px;
  background: white;
  cursor: pointer;
  font-size: 0.875rem;
  color: hsl(var(--foreground));
  transition: all 0.15s ease;
}

.option-pill:hover {
  border-color: hsl(var(--primary) / 0.4);
  background: hsl(var(--primary) / 0.03);
}

.option-pill--selected {
  border-color: hsl(var(--primary));
  background: hsl(var(--primary) / 0.06);
  color: hsl(var(--primary));
  font-weight: 500;
}

.checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.checkbox-list--compact {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.625rem 0.875rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: white;
  cursor: pointer;
  font-size: 0.875rem;
  color: hsl(var(--foreground));
  transition: all 0.15s ease;
  text-align: left;
}

.checkbox-item:hover {
  border-color: hsl(var(--primary) / 0.4);
  background: hsl(var(--primary) / 0.03);
}

.checkbox-item--selected {
  border-color: hsl(var(--primary));
  background: hsl(var(--primary) / 0.06);
}

.checkbox-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 1.125rem;
  height: 1.125rem;
  border: 1.5px solid hsl(var(--border));
  border-radius: 0.25rem;
  flex-shrink: 0;
  transition: all 0.15s ease;
}

.checkbox-item--selected .checkbox-box {
  background: hsl(var(--primary));
  border-color: hsl(var(--primary));
  color: white;
}

.checkbox-label-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.trinn-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.125rem 0.5rem;
  border-radius: 999px;
  font-size: 0.6875rem;
  font-weight: 600;
  white-space: nowrap;
}

.trinn-badge--2 {
  background: #fffbeb;
  color: #d97706;
  border: 1px solid #fde68a;
}

.trinn-badge--3 {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
}

.haccp-trinn-card {
  padding: 1.25rem;
  border-radius: 0.75rem;
  background: hsl(var(--primary) / 0.05);
  border: 1px solid hsl(var(--primary) / 0.15);
}

.haccp-trinn-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: hsl(var(--primary));
  margin-bottom: 0.5rem;
}

.haccp-trinn-label {
  font-size: 0.8125rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.haccp-trinn-level {
  font-size: 1.125rem;
  font-weight: 700;
  color: hsl(var(--primary));
  margin-bottom: 0.375rem;
}

.haccp-trinn-desc {
  font-size: 0.8125rem;
  color: hsl(var(--muted-foreground));
  line-height: 1.5;
}
</style>

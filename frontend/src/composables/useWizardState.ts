import { computed, reactive, ref, watch } from 'vue'
import type {
  HaccpSetupRequest,
  HaccpTrinn,
  WizardState,
} from '@/types/haccp-setup'
import {
  getDefaultPrerequisites,
  getDefaultProcessSteps,
  getDefaultHazardEntries,
} from '@/types/haccp-setup'

const TOTAL_STEPS = 5

export function useWizardState() {
  const currentStep = ref(1)

  const wizardData = reactive<WizardState>({
    businessType: null,
    businessSize: null,
    foodTypes: [],
    processes: [],
    facilities: [],
    temperatureEquipment: [],
    servesVulnerableGroups: null,
    handlesHighRiskProducts: null,
    goodsReceiving: null,

    prerequisites: [],

    processSteps: [],

    hazardEntries: [],
  })

  const haccpTrinn = computed<HaccpTrinn>(() => {
    if (wizardData.servesVulnerableGroups || wizardData.handlesHighRiskProducts) {
      return {
        level: 3,
        label: 'Trinn 3 – Kritiske kontrollpunkter (KKP)',
        description: 'Virksomheten håndterer høyrisikoprodukter eller serverer sårbare grupper. Full HACCP med kritiske kontrollpunkter og overvåkingsrutiner kreves.',
      }
    }

    const hasComplexProcesses = wizardData.processes.some(
      (p) => p === 'VACUUM_PACKING' || p === 'CURING_SMOKING_PRESERVING',
    )
    if (hasComplexProcesses) {
      return {
        level: 3,
        label: 'Trinn 3 – Kritiske kontrollpunkter (KKP)',
        description: 'Virksomheten utfører prosesser som vakuumpakking eller konservering. Full HACCP med kritiske kontrollpunkter kreves.',
      }
    }

    const hasCookingOrCooling = wizardData.processes.some(
      (p) => p === 'COOKING_HEAT_TREATMENT' || p === 'COOLING_COOKED',
    )
    const hasPerishable = wizardData.foodTypes.some(
      (t) => t !== 'SHELF_STABLE_ONLY',
    )
    if (hasCookingOrCooling || hasPerishable) {
      return {
        level: 2,
        label: 'Trinn 2 – Fareanalyse',
        description: 'Virksomheten håndterer lett bedervelige varer eller utfører varmebehandling/nedkjøling. Fareanalyse med forebyggende tiltak kreves.',
      }
    }

    return {
      level: 1,
      label: 'Trinn 1 – Grunnforutsetninger',
      description: 'Virksomheten håndterer hovedsakelig hyllevarer. Grunnleggende hygiene- og renholdsrutiner er tilstrekkelig.',
    }
  })

  watch(currentStep, (newStep) => {
    if (newStep === 2 && wizardData.prerequisites.length === 0) {
      wizardData.prerequisites = getDefaultPrerequisites(wizardData)
    }
    if (newStep === 3 && wizardData.processSteps.length === 0) {
      wizardData.processSteps = getDefaultProcessSteps(wizardData)
    }
    if (newStep === 4 && wizardData.hazardEntries.length === 0) {
      wizardData.hazardEntries = getDefaultHazardEntries(wizardData.processSteps)
    }
  })

  const canProceed = computed(() => {
    switch (currentStep.value) {
      case 1:
        return (
          wizardData.businessType !== null &&
          wizardData.businessSize !== null &&
          wizardData.foodTypes.length > 0 &&
          wizardData.processes.length > 0 &&
          wizardData.servesVulnerableGroups !== null &&
          wizardData.handlesHighRiskProducts !== null &&
          wizardData.goodsReceiving !== null
        )
      case 2:
        return wizardData.prerequisites.length > 0
      case 3:
        return wizardData.processSteps.length > 0
      case 4:
        return wizardData.hazardEntries.length > 0
      case 5:
        return true
      default:
        return false
    }
  })

  function nextStep() {
    if (currentStep.value < TOTAL_STEPS && canProceed.value) {
      currentStep.value++
    }
  }

  function prevStep() {
    if (currentStep.value > 1) {
      currentStep.value--
    }
  }

  function goToStep(step: number) {
    if (step >= 1 && step <= TOTAL_STEPS) {
      currentStep.value = step
    }
  }

  function buildRequest(): HaccpSetupRequest {
    return {
      businessType: wizardData.businessType!,
      businessSize: wizardData.businessSize!,
      foodTypes: [...wizardData.foodTypes],
      processes: [...wizardData.processes],
      facilities: [...wizardData.facilities],
      temperatureEquipment: [...wizardData.temperatureEquipment],
      servesVulnerableGroups: wizardData.servesVulnerableGroups!,
      handlesHighRiskProducts: wizardData.handlesHighRiskProducts!,
      goodsReceiving: wizardData.goodsReceiving!,
    }
  }

  function reset() {
    currentStep.value = 1
    wizardData.businessType = null
    wizardData.businessSize = null
    wizardData.foodTypes = []
    wizardData.processes = []
    wizardData.facilities = []
    wizardData.temperatureEquipment = []
    wizardData.servesVulnerableGroups = null
    wizardData.handlesHighRiskProducts = null
    wizardData.goodsReceiving = null
    wizardData.prerequisites = []
    wizardData.processSteps = []
    wizardData.hazardEntries = []
  }

  return {
    currentStep,
    totalSteps: TOTAL_STEPS,
    wizardData,
    haccpTrinn,
    canProceed,
    nextStep,
    prevStep,
    goToStep,
    buildRequest,
    reset,
  }
}

import type { Checklist } from './checklist'


export type BusinessType =
  | 'RESTAURANT'
  | 'CAFE'
  | 'BAR'
  | 'KIOSK'
  | 'CATERING'
  | 'CANTEEN'
  | 'INSTITUTION_KITCHEN'
  | 'FOOD_PRODUCTION'
  | 'RETAIL'
  | 'MOBILE_VENDOR'

export type BusinessSize = 'SMALL_1_5' | 'MEDIUM_6_15' | 'LARGE_16_50' | 'ENTERPRISE_50_PLUS'

export type FoodType =
  | 'READY_TO_EAT'
  | 'PERISHABLE_TEMP_CONTROL'
  | 'RAW_MEAT_POULTRY'
  | 'SEAFOOD_FISH'
  | 'DAIRY'
  | 'ALLERGEN_CONTAINING'
  | 'SHELF_STABLE_ONLY'

export type FoodProcess =
  | 'COOKING_HEAT_TREATMENT'
  | 'COOLING_COOKED'
  | 'THAWING_FROZEN'
  | 'VACUUM_PACKING'
  | 'CURING_SMOKING_PRESERVING'
  | 'NONE_PRE_MADE'

export type Facility =
  | 'COMMERCIAL_KITCHEN'
  | 'COLD_STORAGE'
  | 'FREEZER_STORAGE'
  | 'DRY_STORAGE'
  | 'SEPARATE_PREP_AREAS'
  | 'DISHWASHING_STATION'
  | 'HANDWASHING_STATIONS'
  | 'STAFF_CHANGING_AREA'

export type TempEquipment = 'REFRIGERATORS' | 'FREEZERS' | 'HOT_HOLDING' | 'COOKING_EQUIPMENT' | 'NONE'

export type GoodsReceiving = 'DIRECT_DELIVERY' | 'PICK_UP' | 'BOTH'

export type PrerequisiteStatus = 'OK' | 'NEEDS_WORK' | 'MISSING'

export interface Prerequisite {
  id: string
  name: string
  description: string
  status: PrerequisiteStatus
}

export type HazardType = 'BIOLOGICAL' | 'CHEMICAL' | 'PHYSICAL' | 'ALLERGEN'

export interface ProcessStep {
  id: string
  name: string
  hazards: HazardType[]
  isKKP: boolean
}

export type LikelihoodLevel = 1 | 2 | 3
export type SeverityLevel = 1 | 2 | 3

export interface HazardEntry {
  id: string
  processStepId: string
  processStepName: string
  hazardType: HazardType
  hazardDescription: string
  likelihood: LikelihoodLevel
  severity: SeverityLevel
  preventiveMeasure: string
}

export interface WizardState {
  businessType: BusinessType | null
  businessSize: BusinessSize | null
  foodTypes: FoodType[]
  processes: FoodProcess[]
  facilities: Facility[]
  temperatureEquipment: TempEquipment[]
  servesVulnerableGroups: boolean | null
  handlesHighRiskProducts: boolean | null
  goodsReceiving: GoodsReceiving | null

  prerequisites: Prerequisite[]

  processSteps: ProcessStep[]

  hazardEntries: HazardEntry[]
}

export interface HaccpSetupRequest {
  businessType: BusinessType
  businessSize: BusinessSize
  foodTypes: FoodType[]
  processes: FoodProcess[]
  facilities: Facility[]
  temperatureEquipment: TempEquipment[]
  servesVulnerableGroups: boolean
  handlesHighRiskProducts: boolean
  goodsReceiving: GoodsReceiving
}

export interface GenerationSummary {
  totalChecklists: number
  totalItems: number
}

export interface HaccpSetupResponse {
  generatedChecklists: Checklist[]
  summary: GenerationSummary
}

export interface HaccpTrinn {
  level: 1 | 2 | 3
  label: string
  description: string
}

export const businessTypeLabels: Record<BusinessType, string> = {
  RESTAURANT: 'Restaurant',
  CAFE: 'Kafé',
  BAR: 'Bar',
  KIOSK: 'Kiosk',
  CATERING: 'Catering',
  CANTEEN: 'Kantine',
  INSTITUTION_KITCHEN: 'Institusjonskjøkken',
  FOOD_PRODUCTION: 'Matproduksjon',
  RETAIL: 'Dagligvare / butikk',
  MOBILE_VENDOR: 'Mobil matservering',
}

export const businessSizeLabels: Record<BusinessSize, string> = {
  SMALL_1_5: '1–5 ansatte',
  MEDIUM_6_15: '6–15 ansatte',
  LARGE_16_50: '16–50 ansatte',
  ENTERPRISE_50_PLUS: '50+ ansatte',
}

export const foodTypeLabels: Record<FoodType, string> = {
  READY_TO_EAT: 'Ferdigmat (ready-to-eat)',
  PERISHABLE_TEMP_CONTROL: 'Lett bedervelige varer',
  RAW_MEAT_POULTRY: 'Rått kjøtt / fjærkre',
  SEAFOOD_FISH: 'Sjømat / fisk',
  DAIRY: 'Meieriprodukter',
  ALLERGEN_CONTAINING: 'Allergenholdig mat',
  SHELF_STABLE_ONLY: 'Kun hyllevarer',
}

export const foodProcessLabels: Record<FoodProcess, string> = {
  COOKING_HEAT_TREATMENT: 'Koking / varmebehandling',
  COOLING_COOKED: 'Nedkjøling av tilberedt mat',
  THAWING_FROZEN: 'Tining av frosne varer',
  VACUUM_PACKING: 'Vakuumpakking',
  CURING_SMOKING_PRESERVING: 'Røyking / salting / konservering',
  NONE_PRE_MADE: 'Ingen – kun servering av ferdigmat',
}

export const processTrinnTrigger: Partial<Record<FoodProcess, 2 | 3>> = {
  COOKING_HEAT_TREATMENT: 2,
  COOLING_COOKED: 2,
  VACUUM_PACKING: 3,
  CURING_SMOKING_PRESERVING: 3,
}

export const facilityLabels: Record<Facility, string> = {
  COMMERCIAL_KITCHEN: 'Produksjonskjøkken',
  COLD_STORAGE: 'Kjølerom / -skap',
  FREEZER_STORAGE: 'Fryselagring',
  DRY_STORAGE: 'Tørrlager',
  SEPARATE_PREP_AREAS: 'Separate soner for rått/ferdig',
  DISHWASHING_STATION: 'Oppvaskstasjon',
  HANDWASHING_STATIONS: 'Håndvaskstasjoner',
  STAFF_CHANGING_AREA: 'Garderobe / personalrom',
}

export const tempEquipmentLabels: Record<TempEquipment, string> = {
  REFRIGERATORS: 'Kjøleskap',
  FREEZERS: 'Frysere',
  HOT_HOLDING: 'Varmholding',
  COOKING_EQUIPMENT: 'Kokeapparat',
  NONE: 'Ingen',
}

export const goodsReceivingLabels: Record<GoodsReceiving, string> = {
  DIRECT_DELIVERY: 'Direkte levering',
  PICK_UP: 'Henter selv',
  BOTH: 'Begge deler',
}

export const hazardTypeLabels: Record<HazardType, string> = {
  BIOLOGICAL: 'Biologisk',
  CHEMICAL: 'Kjemisk',
  PHYSICAL: 'Fysisk',
  ALLERGEN: 'Allergen',
}

export const hazardTypeColors: Record<HazardType, { bg: string; text: string; border: string }> = {
  BIOLOGICAL: { bg: '#fef2f2', text: '#dc2626', border: '#fecaca' },
  CHEMICAL: { bg: '#fefce8', text: '#ca8a04', border: '#fef08a' },
  PHYSICAL: { bg: '#eff6ff', text: '#2563eb', border: '#bfdbfe' },
  ALLERGEN: { bg: '#faf5ff', text: '#9333ea', border: '#e9d5ff' },
}

export const prerequisiteStatusLabels: Record<PrerequisiteStatus, string> = {
  OK: 'På plass',
  NEEDS_WORK: 'Under arbeid',
  MISSING: 'Mangler',
}

export const prerequisiteStatusColors: Record<PrerequisiteStatus, { bg: string; text: string; border: string }> = {
  OK: { bg: '#f0fdf4', text: '#16a34a', border: '#bbf7d0' },
  NEEDS_WORK: { bg: '#fffbeb', text: '#d97706', border: '#fde68a' },
  MISSING: { bg: '#fef2f2', text: '#dc2626', border: '#fecaca' },
}

export const likelihoodLabels: Record<LikelihoodLevel, string> = {
  1: 'Lav',
  2: 'Middels',
  3: 'Høy',
}

export const severityLabels: Record<SeverityLevel, string> = {
  1: 'Lav',
  2: 'Middels',
  3: 'Høy',
}

export function getDefaultPrerequisites(state: WizardState): Prerequisite[] {
  const prereqs: Prerequisite[] = [
    { id: 'lokaler', name: 'Lokaler og utforming', description: 'Lokalene er utformet og vedlikeholdt slik at mat kan håndteres trygt. Egnede materialer, god belysning og ventilasjon.', status: 'MISSING' },
    { id: 'renhold', name: 'Renhold og desinfisering', description: 'Renholdsplan med rutiner for daglig og periodisk rengjøring av lokaler, utstyr og overflater.', status: 'MISSING' },
    { id: 'skadedyr', name: 'Skadedyrkontroll', description: 'Forebyggende tiltak mot skadedyr, og avtale med skadedyrbekjemper ved behov.', status: 'MISSING' },
    { id: 'vann', name: 'Vannforsyning', description: 'Tilgang til rent drikkevann i tilstrekkelig mengde for produksjon og renhold.', status: 'MISSING' },
    { id: 'personlig', name: 'Personlig hygiene', description: 'Rutiner for håndvask, arbeidstøy, sykemelding ved smittefare, og generell hygieneatferd.', status: 'MISSING' },
    { id: 'opplaering', name: 'Opplæring', description: 'Alle ansatte har tilstrekkelig opplæring i mathygiene og IK-mat tilpasset sine arbeidsoppgaver.', status: 'MISSING' },
    { id: 'avfall', name: 'Avfallshåndtering', description: 'Rutiner for sortering, oppbevaring og henting av avfall slik at det ikke utgjør en hygienisk risiko.', status: 'MISSING' },
    { id: 'sporbarhet', name: 'Sporbarhet og merking', description: 'System for å spore råvarer og produkter ett ledd tilbake og ett ledd frem i leveringskjeden.', status: 'MISSING' },
    { id: 'varemottak', name: 'Varemottak', description: 'Kontroll av varer ved mottak: temperatur, holdbarhet, emballasje og dokumentasjon.', status: 'MISSING' },
  ]

  const hasTempEquipment = state.temperatureEquipment.length > 0 && !state.temperatureEquipment.includes('NONE')
  if (hasTempEquipment) {
    prereqs.push({ id: 'temperatur', name: 'Temperaturkontroll', description: 'Rutiner for overvåking og logging av temperaturer i kjøl, frys og ved varmebehandling.', status: 'MISSING' })
  }

  if (state.foodTypes.includes('ALLERGEN_CONTAINING') || state.servesVulnerableGroups) {
    prereqs.push({ id: 'allergen', name: 'Allergenhåndtering', description: 'Rutiner for å identifisere, merke og informere om allergener i maten som serveres.', status: 'MISSING' })
  }

  if (hasTempEquipment) {
    prereqs.push({ id: 'vedlikehold', name: 'Vedlikehold av utstyr', description: 'Plan for regelmessig vedlikehold og kalibrering av utstyr, spesielt temperaturmålere.', status: 'MISSING' })
  }

  return prereqs
}

export function getDefaultProcessSteps(state: WizardState): ProcessStep[] {
  const steps: ProcessStep[] = []
  let id = 1

  steps.push({ id: String(id++), name: 'Varemottak', hazards: ['BIOLOGICAL', 'PHYSICAL'], isKKP: false })

  if (state.temperatureEquipment.includes('REFRIGERATORS') || state.temperatureEquipment.includes('FREEZERS')) {
    steps.push({ id: String(id++), name: 'Kjøle-/fryselagring', hazards: ['BIOLOGICAL'], isKKP: false })
  }
  if (state.facilities.includes('DRY_STORAGE')) {
    steps.push({ id: String(id++), name: 'Tørrlagring', hazards: ['PHYSICAL'], isKKP: false })
  }

  if (state.processes.includes('THAWING_FROZEN')) {
    steps.push({ id: String(id++), name: 'Tining', hazards: ['BIOLOGICAL'], isKKP: false })
  }

  if (state.facilities.includes('COMMERCIAL_KITCHEN') || state.facilities.includes('SEPARATE_PREP_AREAS')) {
    steps.push({ id: String(id++), name: 'Klargjøring og kutting', hazards: ['BIOLOGICAL', 'PHYSICAL', 'ALLERGEN'], isKKP: false })
  }

  if (state.processes.includes('COOKING_HEAT_TREATMENT')) {
    steps.push({ id: String(id++), name: 'Varmebehandling / koking', hazards: ['BIOLOGICAL'], isKKP: true })
  }

  if (state.processes.includes('COOLING_COOKED')) {
    steps.push({ id: String(id++), name: 'Nedkjøling', hazards: ['BIOLOGICAL'], isKKP: true })
  }

  if (state.temperatureEquipment.includes('HOT_HOLDING')) {
    steps.push({ id: String(id++), name: 'Varmholding', hazards: ['BIOLOGICAL'], isKKP: true })
  }

  if (state.processes.includes('VACUUM_PACKING')) {
    steps.push({ id: String(id++), name: 'Vakuumpakking', hazards: ['BIOLOGICAL', 'CHEMICAL'], isKKP: true })
  }
  if (state.processes.includes('CURING_SMOKING_PRESERVING')) {
    steps.push({ id: String(id++), name: 'Røyking / salting / konservering', hazards: ['BIOLOGICAL', 'CHEMICAL'], isKKP: true })
  }

  steps.push({ id: String(id++), name: 'Servering / salg', hazards: ['BIOLOGICAL', 'ALLERGEN'], isKKP: false })

  return steps
}

export function getDefaultHazardEntries(processSteps: ProcessStep[]): HazardEntry[] {
  const entries: HazardEntry[] = []
  let id = 1

  const defaultDescriptions: Record<HazardType, Record<string, string>> = {
    BIOLOGICAL: {
      'Varemottak': 'Mikrobiell vekst ved for høy temperatur under transport',
      'Kjøle-/fryselagring': 'Temperaturavvik som gir bakterievekst',
      'Tining': 'Ukontrollert tining gir bakterievekst i ytre lag',
      'Klargjøring og kutting': 'Kryssforurensning mellom rått og ferdig mat',
      'Varmebehandling / koking': 'Utilstrekkelig kjernetemperatur – patogener overlever',
      'Nedkjøling': 'For langsom nedkjøling gir bakterievekst',
      'Varmholding': 'Temperatur under 60°C gir bakterievekst',
      'Vakuumpakking': 'Anaerobe bakterier (Clostridium) ved feil temperatur',
      'Røyking / salting / konservering': 'Utilstrekkelig konservering gir bakterievekst',
      'Servering / salg': 'For lang tid i romtemperatur',
    },
    CHEMICAL: {
      'Vakuumpakking': 'Rester av rengjøringsmidler på emballasje',
      'Røyking / salting / konservering': 'For høy konsentrasjon av tilsetningsstoffer',
    },
    PHYSICAL: {
      'Varemottak': 'Fremmedlegemer i råvarer (glass, plast, metall)',
      'Tørrlagring': 'Skadedyr eller fremmedlegemer i tørrvarer',
      'Klargjøring og kutting': 'Fremmedlegemer fra utstyr eller emballasje',
    },
    ALLERGEN: {
      'Klargjøring og kutting': 'Kryssforurensning av allergener mellom retter',
      'Servering / salg': 'Feil allergenmerking eller manglende informasjon til gjest',
    },
  }

  for (const step of processSteps) {
    for (const hazard of step.hazards) {
      const desc = defaultDescriptions[hazard]?.[step.name] ?? ''
      entries.push({
        id: String(id++),
        processStepId: step.id,
        processStepName: step.name,
        hazardType: hazard,
        hazardDescription: desc,
        likelihood: 2,
        severity: 2,
        preventiveMeasure: '',
      })
    }
  }

  return entries
}

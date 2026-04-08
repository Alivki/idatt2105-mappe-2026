import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import BusinessSetupStep from '../Step1BusinessType.vue'


vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })
  return {
    UtensilsCrossed: icon('UtensilsCrossed'),
    Coffee: icon('Coffee'),
    Wine: icon('Wine'),
    Store: icon('Store'),
    Truck: icon('Truck'),
    Building2: icon('Building2'),
    Soup: icon('Soup'),
    Factory: icon('Factory'),
    ShoppingCart: icon('ShoppingCart'),
    MapPin: icon('MapPin'),
    Check: icon('Check'),
    ShieldCheck: icon('ShieldCheck'),
    CircleCheck: icon('CircleCheck'),
    AlertTriangle: icon('AlertTriangle'),
    CircleX: icon('CircleX'),
    ClipboardCheck: icon('ClipboardCheck'),
    Info: icon('Info'),
    ArrowDown: icon('ArrowDown'),
    Plus: icon('Plus'),
    X: icon('X'),
    ShieldAlert: icon('ShieldAlert'),
  }
})


vi.mock('@/types/haccp-setup', () => ({
  businessTypeLabels: {
    RESTAURANT: 'Restaurant',
    CAFE: 'Kafé',
    BAR: 'Bar',
    KIOSK: 'Kiosk',
    CATERING: 'Catering',
    CANTEEN: 'Kantine',
    INSTITUTION_KITCHEN: 'Institusjonskjøkken',
    FOOD_PRODUCTION: 'Matproduksjon',
    RETAIL: 'Butikk',
    MOBILE_VENDOR: 'Mobil servering',
  },
  businessSizeLabels: {
    SMALL: '1–5 ansatte',
    MEDIUM: '6–20 ansatte',
    LARGE: '21+ ansatte',
  },
  foodTypeLabels: {
    MEAT: 'Kjøtt',
    FISH: 'Fisk',
    ALLERGEN_CONTAINING: 'Allergener',
    SHELF_STABLE_ONLY: 'Kun tørrvarer',
  },
  foodProcessLabels: {
    RECEIVING: 'Varemottak',
    COLD_STORAGE: 'Kjølelagring',
    COOKING_HEAT_TREATMENT: 'Varmebehandling',
    COOLING_COOKED: 'Nedkjøling',
  },
  processTrinnTrigger: {
    COOKING_HEAT_TREATMENT: 3,
    COOLING_COOKED: 2,
  },
  facilityLabels: {
    HAND_WASH_STATION: 'Håndvask',
    DISHWASHER: 'Oppvaskmaskin',
  },
  tempEquipmentLabels: {
    NONE: 'Ingen',
    REFRIGERATORS: 'Kjøleskap',
    FREEZERS: 'Frysere',
    HOT_HOLDING: 'Varmholding',
  },
  goodsReceivingLabels: {
    SUPPLIER_TEMP_CHECK: 'Temperatursjekk',
    PACKAGING_INSPECTION: 'Emballasjekontroll',
  },
}))

function makeWizard() {
  return {
    businessType: null,
    businessSize: null,
    foodTypes: [] as string[],
    processes: [] as string[],
    facilities: [] as string[],
    temperatureEquipment: [] as string[],
    goodsReceiving: [] as string[],
    servesVulnerableGroups: false,
    handlesHighRiskProducts: false,
  }
}

describe('BusinessSetupStep', () => {
  it('selects business type and size', async () => {
    const wizard = makeWizard()
    const wrapper = mount(BusinessSetupStep, {
      props: {
        wizard,
        haccpTrinn: { level: 1, label: 'Trinn 1' },
      },
    })

    const buttons = wrapper.findAll('button')
    await buttons.find((b) => b.text().includes('Restaurant'))!.trigger('click')
    await buttons.find((b) => b.text().includes('1–5 ansatte'))!.trigger('click')

    expect(wizard.businessType).toBe('RESTAURANT')
    expect(wizard.businessSize).toBe('SMALL')
  })

  it('toggles food types, processes and facilities', async () => {
    const wizard = makeWizard()
    const wrapper = mount(BusinessSetupStep, {
      props: {
        wizard,
        haccpTrinn: { level: 2, label: 'Trinn 2' },
      },
    })

    const buttons = wrapper.findAll('button')
    const meat = buttons.find((b) => b.text().includes('Kjøtt'))!
    const cooking = buttons.find((b) => b.text().includes('Varmebehandling'))!
    const handWash = buttons.find((b) => b.text().includes('Håndvask'))!

    await meat.trigger('click')
    await cooking.trigger('click')
    await handWash.trigger('click')

    expect(wizard.foodTypes).toEqual(['MEAT'])
    expect(wizard.processes).toEqual(['COOKING_HEAT_TREATMENT'])
    expect(wizard.facilities).toEqual(['HAND_WASH_STATION'])

    await meat.trigger('click')
    await cooking.trigger('click')
    await handWash.trigger('click')

    expect(wizard.foodTypes).toEqual([])
    expect(wizard.processes).toEqual([])
    expect(wizard.facilities).toEqual([])
  })

  it('handles temperature equipment NONE as exclusive', async () => {
    const wizard = makeWizard()
    wizard.temperatureEquipment = ['REFRIGERATORS']
    const wrapper = mount(BusinessSetupStep, {
      props: {
        wizard,
        haccpTrinn: { level: 2, label: 'Trinn 2' },
      },
    })

    const buttons = wrapper.findAll('button')
    const noneBtn = buttons.find((b) => b.text().includes('Ingen'))!
    const fridgeBtn = buttons.find((b) => b.text().includes('Kjøleskap'))!
    const freezerBtn = buttons.find((b) => b.text().includes('Frysere'))!

    await noneBtn.trigger('click')
    expect(wizard.temperatureEquipment).toEqual(['NONE'])

    await freezerBtn.trigger('click')
    expect(wizard.temperatureEquipment).toEqual(['FREEZERS'])

    await fridgeBtn.trigger('click')
    expect(wizard.temperatureEquipment).toEqual(expect.arrayContaining(['FREEZERS', 'REFRIGERATORS']))
  })
})

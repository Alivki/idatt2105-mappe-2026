import {mount} from '@vue/test-utils'
import {defineComponent, h} from 'vue'
import {describe, expect, it, vi} from 'vitest'
import SummaryGenerateStep from '../Step5Summary.vue'


vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', {'data-icon': name})
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
  },
  businessSizeLabels: {
    SMALL: '1–5 ansatte',
  },
}))

describe('SummaryGenerateStep', () => {
  it('renders summary, warning and generated checklist preview', () => {
    const wizard = {
      businessType: 'RESTAURANT',
      businessSize: 'SMALL',
      prerequisites: [
        {id: '1', status: 'OK'},
        {id: '2', status: 'MISSING'},
      ],
      processSteps: [{id: 'a'}, {id: 'b'}],
      hazardEntries: [
        {id: '1', likelihood: 3, severity: 2},
        {id: '2', likelihood: 1, severity: 1},
      ],
      temperatureEquipment: ['REFRIGERATORS', 'HOT_HOLDING'],
      foodTypes: ['ALLERGEN_CONTAINING'],
      processes: ['COOKING_HEAT_TREATMENT', 'COOLING_COOKED'],
      servesVulnerableGroups: true,
      handlesHighRiskProducts: false,
    }

    const wrapper = mount(SummaryGenerateStep, {
      props: {
        wizard,
        haccpTrinn: {label: 'Trinn 3'},
        isGenerating: false,
      },
    })

    expect(wrapper.text()).toContain('Trinn 3')
    expect(wrapper.text()).toContain('Restaurant · 1–5 ansatte')
    expect(wrapper.text()).toContain('1 av 2 på plass')
    expect(wrapper.text()).toContain('2 farer vurdert · 1 KKP')
    expect(wrapper.text()).toContain('1 grunnforutsetning')
    expect(wrapper.text()).toContain('Temperaturkontroll – Kjøleskap')
    expect(wrapper.text()).toContain('Varmholdingskontroll')
    expect(wrapper.text()).toContain('Allergenkontroll')
    expect(wrapper.text()).toContain('Kritiske kontrollpunkter (KKP)')
    expect(wrapper.text()).toContain('Tilberedningskontroll')
    expect(wrapper.text()).toContain('Nedkjølingskontroll')
  })

  it('hides warning when all prerequisites are OK and excludes equipment-dependent checklists', () => {
    const wizard = {
      businessType: 'RESTAURANT',
      businessSize: 'SMALL',
      prerequisites: [
        {id: '1', status: 'OK'},
      ],
      processSteps: [],
      hazardEntries: [],
      temperatureEquipment: ['NONE'],
      foodTypes: ['SHELF_STABLE_ONLY'],
      processes: [],
      servesVulnerableGroups: false,
      handlesHighRiskProducts: false,
    }

    const wrapper = mount(SummaryGenerateStep, {
      props: {
        wizard,
        haccpTrinn: {label: 'Trinn 1'},
        isGenerating: true,
      },
    })

    expect(wrapper.text()).not.toContain('er ikke markert som «på plass»')
    expect(wrapper.text()).not.toContain('Temperaturkontroll – Kjøleskap')
    expect(wrapper.text()).not.toContain('Vedlikehold av utstyr')
    expect(wrapper.text()).not.toContain('Mottakskontroll')
    expect(wrapper.text()).toContain('Årlig gjennomgang av IK-mat')
  })
})

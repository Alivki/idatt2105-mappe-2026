import {mount} from '@vue/test-utils'
import {defineComponent, h} from 'vue'
import {describe, expect, it, vi} from 'vitest'
import HazardAnalysisStep from '../Step4RiskAssessment.vue'


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
  hazardTypeLabels: {
    BIOLOGICAL: 'Biologisk',
    CHEMICAL: 'Kjemisk',
    PHYSICAL: 'Fysisk',
    ALLERGEN: 'Allergen',
  },
  hazardTypeColors: {
    BIOLOGICAL: {text: 'red', border: 'pink', bg: 'mistyrose'},
    CHEMICAL: {text: 'orange', border: 'gold', bg: 'lemonchiffon'},
    PHYSICAL: {text: 'blue', border: 'lightblue', bg: 'aliceblue'},
    ALLERGEN: {text: 'purple', border: 'violet', bg: 'lavender'},
  },
}))

vi.mock('@/components/ui/textarea/Textarea.vue', () => ({
  default: defineComponent({
    name: 'TextareaStub',
    props: {modelValue: {type: String, default: ''}},
    emits: ['update:modelValue'],
    setup(props, {emit}) {
      return () =>
        h('textarea', {
          value: props.modelValue,
          onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLTextAreaElement).value),
        })
    },
  }),
}))

vi.mock('@/components/ui/select/Select.vue', () => ({
  default: defineComponent({
    name: 'SelectStub',
    props: {modelValue: {type: String, default: ''}},
    emits: ['update:modelValue'],
    setup(props, {emit, slots}) {
      return () =>
        h('select', {
          value: props.modelValue,
          onChange: (e: Event) => emit('update:modelValue', (e.target as HTMLSelectElement).value),
        }, slots.default?.())
    },
  }),
}))
vi.mock('@/components/ui/select/SelectTrigger.vue', () => ({
  default: defineComponent({
    setup(_, {slots}) {
      return () => h('div', slots.default?.())
    }
  })
}))
vi.mock('@/components/ui/select/SelectValue.vue', () => ({
  default: defineComponent({
    setup(_, {slots}) {
      return () => h('span', slots.default?.())
    }
  })
}))
vi.mock('@/components/ui/select/SelectContent.vue', () => ({
  default: defineComponent({
    setup(_, {slots}) {
      return () => h('div', slots.default?.())
    }
  })
}))
vi.mock('@/components/ui/select/SelectItem.vue', () => ({
  default: defineComponent({
    props: {value: {type: String, required: true}},
    setup(props, {slots}) {
      return () => h('option', {value: props.value}, slots.default?.())
    },
  }),
}))

describe('HazardAnalysisStep', () => {
  it('shows KKP summary and risk labels from current entries', () => {
    const wizard = {
      hazardEntries: [
        {
          id: '1',
          processStepName: 'Mottak',
          hazardType: 'BIOLOGICAL',
          hazardDescription: '',
          likelihood: 3,
          severity: 2,
          preventiveMeasure: ''
        },
        {
          id: '2',
          processStepName: 'Lagring',
          hazardType: 'CHEMICAL',
          hazardDescription: '',
          likelihood: 1,
          severity: 2,
          preventiveMeasure: ''
        },
      ],
    }

    const wrapper = mount(HazardAnalysisStep, {props: {wizard}})
    expect(wrapper.text()).toContain('1 fare identifisert som KKP')
    expect(wrapper.text()).toContain('Høy')
    expect(wrapper.text()).toContain('Lav')
    expect(wrapper.findAll('.entry-card--kkp')).toHaveLength(1)
  })

  it('updates textareas and select values', async () => {
    const wizard = {
      hazardEntries: [
        {
          id: '1',
          processStepName: 'Mottak',
          hazardType: 'BIOLOGICAL',
          hazardDescription: '',
          likelihood: 1,
          severity: 1,
          preventiveMeasure: ''
        },
      ],
    }

    const wrapper = mount(HazardAnalysisStep, {props: {wizard}})

    const textareas = wrapper.findAll('textarea')
    await textareas[0].setValue('Beskrivelse')
    await textareas[1].setValue('Tiltak')
    const selects = wrapper.findAll('select')
    await selects[0].setValue('2')
    await selects[1].setValue('3')

    expect(wizard.hazardEntries[0].hazardDescription).toBe('Beskrivelse')
    expect(wizard.hazardEntries[0].preventiveMeasure).toBe('Tiltak')
    expect(wizard.hazardEntries[0].likelihood).toBe(2)
    expect(wizard.hazardEntries[0].severity).toBe(3)
    expect(wrapper.text()).toContain('6')
    expect(wrapper.text()).toContain('Høy')
  })
})

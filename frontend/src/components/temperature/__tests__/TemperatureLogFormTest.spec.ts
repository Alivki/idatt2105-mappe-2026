import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import TemperatureLogForm from '../TemperatureLogForm.vue'

vi.mock('@/composables/useTemperatureMonitoring', () => ({
  formatThreshold: vi.fn((threshold: { min: number; max: number }) => `${threshold.min} til ${threshold.max}°C`),
}))

vi.mock('lucide-vue-next', () => ({
  Thermometer: { template: '<svg />' },
  TriangleAlert: { template: '<svg />' },
}))

const InputStub = defineComponent({
  name: 'InputStub',
  inheritAttrs: false,
  props: {
    modelValue: { type: [String, Number], default: '' },
    disabled: { type: Boolean, default: false },
    placeholder: { type: String, default: '' },
    type: { type: String, default: 'text' },
  },
  emits: ['update:modelValue'],
  setup(props, { emit, attrs }) {
    return () => h('input', {
      ...attrs,
      value: props.modelValue,
      disabled: props.disabled,
      placeholder: props.placeholder,
      type: props.type,
      onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLInputElement).value),
    })
  },
})

const ButtonStub = defineComponent({
  name: 'ButtonStub',
  props: { disabled: { type: Boolean, default: false } },
  emits: ['click'],
  setup(props, { slots, emit, attrs }) {
    return () => h('button', { ...attrs, disabled: props.disabled, onClick: (e: MouseEvent) => !props.disabled && emit('click', e) }, slots.default ? slots.default() : [])
  },
})

const BadgeStub = defineComponent({
  name: 'BadgeStub',
  props: { tone: { type: String, default: '' } },
  setup(props, { slots }) {
    return () => h('span', { 'data-tone': props.tone }, slots.default ? slots.default() : [])
  },
})

const SelectStub = defineComponent({
  name: 'SelectStub',
  props: { modelValue: { type: String, default: '' } },
  emits: ['update:modelValue'],
  setup(_, { slots }) {
    return () => h('div', { 'data-test': 'appliance-select' }, slots.default ? slots.default() : [])
  },
})

function mountComponent(overrides?: Record<string, unknown>) {
  return mount(TemperatureLogForm, {
    props: {
      activeAppliances: [],
      selectedApplianceId: null,
      temperatureInput: '',
      note: '',
      registeredByName: 'Ada Lovelace',
      selectedAppliance: null,
      formStatus: null,
      ...overrides,
    },
    global: {
      stubs: {
        Badge: BadgeStub,
        Button: ButtonStub,
        Input: InputStub,
        Select: SelectStub,
        SelectContent: { template: '<div><slot /></div>' },
        SelectItem: { template: '<div><slot /></div>' },
        SelectTrigger: { template: '<div><slot /></div>' },
        SelectValue: { template: '<div><slot /></div>' },
      },
    },
  })
}

describe('TemperatureLogForm', () => {
  it('shows empty warning and disables submit when no active appliances exist', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Ingen aktive enheter')
    const submitButton = wrapper.findAll('button').find((node) => node.text().includes('Lagre temperatur'))!
    expect(submitButton.attributes('disabled')).toBeDefined()
  })

  it('emits updates for selected appliance, temperature input and note', async () => {
    const activeAppliances = [
      { id: 5, name: 'Fryser', type: 'FREEZER', threshold: { min: -18, max: -15 } },
    ]
    const wrapper = mountComponent({
      activeAppliances,
      selectedApplianceId: null,
      temperatureInput: '',
      note: '',
    })

    wrapper.getComponent(SelectStub).vm.$emit('update:modelValue', '5')
    const inputs = wrapper.findAll('input')
    await inputs[0].setValue('-17.3')
    await inputs[2].setValue('Målt etter rengjøring')

    expect(wrapper.emitted('update:selectedApplianceId')![0]).toEqual([5])
    expect(wrapper.emitted('update:temperatureInput')![0]).toEqual(['-17.3'])
    expect(wrapper.emitted('update:note')![0]).toEqual(['Målt etter rengjøring'])
  })

  it('shows threshold and expected deviation status when appliance is selected', () => {
    const wrapper = mountComponent({
      activeAppliances: [{ id: 1, name: 'Kjøl', type: 'FRIDGE', threshold: { min: 0, max: 4 } }],
      selectedApplianceId: 1,
      selectedAppliance: { id: 1, name: 'Kjøl', type: 'FRIDGE', threshold: { min: 0, max: 4 } },
      temperatureInput: '8.0',
      formStatus: 'DEVIATION',
    })

    expect(wrapper.text()).toContain('0 til 4°C')
    expect(wrapper.text()).toContain('Avvik')
    expect(wrapper.find('[data-tone="danger"]').exists()).toBe(true)
  })

  it('enables submit and emits submit when form has appliance and temperature', async () => {
    const wrapper = mountComponent({
      activeAppliances: [{ id: 1, name: 'Kjøl', type: 'FRIDGE', threshold: { min: 0, max: 4 } }],
      selectedApplianceId: 1,
      selectedAppliance: { id: 1, name: 'Kjøl', type: 'FRIDGE', threshold: { min: 0, max: 4 } },
      temperatureInput: '3.1',
      formStatus: 'OK',
    })

    const submitButton = wrapper.findAll('button').find((node) => node.text().includes('Lagre temperatur'))!
    expect(submitButton.attributes('disabled')).toBeUndefined()
    await submitButton.trigger('click')

    expect(wrapper.emitted('submit')).toBeTruthy()
  })
})

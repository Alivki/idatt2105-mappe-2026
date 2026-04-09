import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import TemperatureApplianceDialog from '../ApplianceFormDialog.vue'

const mocks = vi.hoisted(() => ({
  getDefaultThreshold: vi.fn((type: string) =>
    type === 'FREEZER' ? { min: -18, max: -15 } : { min: 0, max: 4 },
  ),
}))

vi.mock('@/composables/useTemperatureMonitoring', () => ({
  getDefaultThreshold: mocks.getDefaultThreshold,
}))

vi.mock('lucide-vue-next', () => ({}))

const DialogStub = defineComponent({
  name: 'DialogStub',
  props: { open: { type: Boolean, default: false } },
  emits: ['update:open'],
  setup(props, { slots, emit }) {
    return () =>
      h('div', { 'data-test': 'dialog', 'data-open': String(props.open) }, [
        h(
          'button',
          { 'data-test': 'open-false', onClick: () => emit('update:open', false) },
          'close',
        ),
        ...(slots.default ? slots.default() : []),
      ])
  },
})

const InputStub = defineComponent({
  name: 'InputStub',
  inheritAttrs: false,
  props: {
    modelValue: { type: [String, Number], default: '' },
    placeholder: { type: String, default: '' },
    disabled: { type: Boolean, default: false },
    type: { type: String, default: 'text' },
  },
  emits: ['update:modelValue'],
  setup(props, { emit, attrs }) {
    return () =>
      h('input', {
        ...attrs,
        value: props.modelValue,
        placeholder: props.placeholder,
        disabled: props.disabled,
        type: props.type,
        onInput: (event: Event) =>
          emit('update:modelValue', (event.target as HTMLInputElement).value),
      })
  },
})

const ButtonStub = defineComponent({
  name: 'ButtonStub',
  props: { disabled: { type: Boolean, default: false } },
  emits: ['click'],
  setup(props, { slots, emit, attrs }) {
    return () =>
      h(
        'button',
        {
          ...attrs,
          disabled: props.disabled,
          onClick: (event: MouseEvent) => {
            if (!props.disabled) emit('click', event)
          },
        },
        slots.default ? slots.default() : [],
      )
  },
})

const SelectStub = defineComponent({
  name: 'SelectStub',
  props: { modelValue: { type: String, default: '' } },
  emits: ['update:modelValue'],
  setup(props, { emit, slots }) {
    return () =>
      h('div', [
        h('select', {
          'data-test': 'type-select',
          value: props.modelValue,
          onChange: (event: Event) =>
            emit('update:modelValue', (event.target as HTMLSelectElement).value),
        }, [
          h('option', { value: 'FRIDGE' }, 'Kjøleskap'),
          h('option', { value: 'FREEZER' }, 'Fryser'),
        ]),
        ...(slots.default ? slots.default() : []),
      ])
  },
})

function mountComponent(overrides?: Record<string, unknown>) {
  return mount(TemperatureApplianceDialog, {
    props: {
      open: true,
      ...overrides,
    },
    global: {
      stubs: {
        Dialog: DialogStub,
        DialogContent: { template: '<div><slot /></div>' },
        DialogDescription: { template: '<div><slot /></div>' },
        DialogFooter: { template: '<div><slot /></div>' },
        DialogHeader: { template: '<div><slot /></div>' },
        DialogTitle: { template: '<div><slot /></div>' },
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

describe('TemperatureApplianceDialog', () => {
  beforeEach(() => {
    mocks.getDefaultThreshold.mockClear()
  })

  it('loads initial values when dialog is opened in edit mode and shows readonly type', async () => {
    const wrapper = mountComponent({
      open: false,
      mode: 'edit',
      initialName: 'Fryser lager',
      initialType: 'FREEZER',
      initialMin: -22,
      initialMax: -18,
    })

    await wrapper.setProps({ open: true })

    const inputs = wrapper.findAll('input')
    expect((inputs[0].element as HTMLInputElement).value).toBe('Fryser lager')
    expect((inputs[1].element as HTMLInputElement).value).toBe('Fryser')
    expect((inputs[1].element as HTMLInputElement).disabled).toBe(true)
    expect((inputs[2].element as HTMLInputElement).value).toBe('-22')
    expect((inputs[3].element as HTMLInputElement).value).toBe('-18')
    expect(wrapper.text()).toContain('Rediger hvitevare')
    expect(wrapper.find('[data-test="type-select"]').exists()).toBe(false)
  })

  it('updates threshold defaults when type changes in create mode', async () => {
    const wrapper = mountComponent({
      mode: 'create',
      initialName: '',
      initialType: 'FRIDGE',
      initialMin: 0,
      initialMax: 4,
    })

    const select = wrapper.get('[data-test="type-select"]')
    await select.setValue('FREEZER')

    const inputs = wrapper.findAll('input')
    expect((inputs[1].element as HTMLInputElement).value).toBe('-18')
    expect((inputs[2].element as HTMLInputElement).value).toBe('-15')
    expect(mocks.getDefaultThreshold).toHaveBeenCalledWith('FREEZER')
  })

  it('does not overwrite thresholds on type watcher in edit mode', async () => {
    const wrapper = mountComponent({
      mode: 'edit',
      initialName: 'Kjøl',
      initialType: 'FRIDGE',
      initialMin: 1,
      initialMax: 7,
    })

    await wrapper.setProps({
      open: false,
    })
    await wrapper.setProps({
      open: true,
      initialType: 'FREEZER',
      initialMin: -12,
      initialMax: -8,
    })

    const inputs = wrapper.findAll('input')
    expect((inputs[2].element as HTMLInputElement).value).toBe('-12')
    expect((inputs[3].element as HTMLInputElement).value).toBe('-8')
  })

  it('shows validation errors for empty name and invalid threshold', async () => {
    const wrapper = mountComponent()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('   ')
    await inputs[1].setValue('5')
    await inputs[2].setValue('2')

    const submitButton = wrapper.findAll('button').find((node) => node.text().includes('Lagre enhet'))
    await submitButton!.trigger('click')

    expect(wrapper.text()).toContain('Navn er påkrevd')
    expect(wrapper.text()).toContain('Min må være lavere enn maks')
    expect(wrapper.emitted('submit')).toBeFalsy()
  })

  it('emits trimmed submit payload when values are valid', async () => {
    const wrapper = mountComponent()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('  Kjøleskap kjøkken  ')
    await inputs[1].setValue('1')
    await inputs[2].setValue('6')

    const submitButton = wrapper.findAll('button').find((node) => node.text().includes('Lagre enhet'))
    await submitButton!.trigger('click')

    expect(wrapper.emitted('submit')).toBeTruthy()
    expect(wrapper.emitted('submit')![0][0]).toEqual({
      name: 'Kjøleskap kjøkken',
      type: 'FRIDGE',
      threshold: { min: 1, max: 6 },
    })
  })

  it('emits update:open false when cancel is clicked', async () => {
    const wrapper = mountComponent()
    const cancelButton = wrapper.findAll('button').find((node) => node.text().includes('Avbryt'))
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('update:open')![0]).toEqual([false])
  })
})

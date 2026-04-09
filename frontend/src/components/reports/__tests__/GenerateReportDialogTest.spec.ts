import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { CalendarDate } from '@internationalized/date'
import GenerateReportDialog from '../GenerateReportDialog.vue'

const { toastError } = vi.hoisted(() => ({
  toastError: vi.fn(),
}))

vi.mock('vue-sonner', () => ({
  toast: {
    error: toastError,
  },
}))

const DialogStub = defineComponent({
  props: {
    open: { type: Boolean, default: false },
  },
  emits: ['update:open'],
  setup(props, { slots, emit }) {
    return () =>
      h('div', { 'data-test': 'dialog', 'data-open': String(props.open) }, [
        h(
          'button',
          {
            'data-test': 'dialog-open',
            type: 'button',
            onClick: () => emit('update:open', true),
          },
          'open',
        ),
        h(
          'button',
          {
            'data-test': 'dialog-close',
            type: 'button',
            onClick: () => emit('update:open', false),
          },
          'close',
        ),
        ...(slots.default ? slots.default() : []),
      ])
  },
})

const InputStub = defineComponent({
  props: {
    modelValue: { type: String, default: '' },
    placeholder: { type: String, default: '' },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        value: props.modelValue,
        placeholder: props.placeholder,
        onInput: (event: Event) => {
          emit('update:modelValue', (event.target as HTMLInputElement).value)
        },
      })
  },
})

const CheckboxStub = defineComponent({
  name: 'Checkbox',
  props: {
    checked: { type: Boolean, default: false },
  },
  emits: ['update:checked'],
  setup(props, { emit }) {
    return () =>
      h('input', {
        type: 'checkbox',
        checked: props.checked,
        onChange: (event: Event) => {
          emit('update:checked', (event.target as HTMLInputElement).checked)
        },
      })
  },
})

const DatePickerStub = defineComponent({
  name: 'DatePicker',
  props: {
    modelValue: { type: Object, default: undefined },
    placeholder: { type: String, default: '' },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    return () =>
      h(
        'button',
        {
          type: 'button',
          'data-placeholder': props.placeholder,
          onClick: () => {
            const nextDate = props.placeholder.includes('fra')
              ? new CalendarDate(2026, 3, 1)
              : new CalendarDate(2026, 3, 31)
            emit('update:modelValue', nextDate)
          },
        },
        props.placeholder,
      )
  },
})

const ButtonStub = defineComponent({
  props: {
    disabled: { type: Boolean, default: false },
    variant: { type: String, default: undefined },
  },
  emits: ['click'],
  setup(props, { slots, emit }) {
    return () =>
      h(
        'button',
        {
          type: 'button',
          disabled: props.disabled,
          'data-variant': props.variant,
          onClick: (event: MouseEvent) => {
            if (!props.disabled) emit('click', event)
          },
        },
        slots.default ? slots.default() : [],
      )
  },
})

function mountComponent(overrides?: Record<string, unknown>) {
  return mount(GenerateReportDialog, {
    props: {
      open: true,
      checklists: [
        { id: 1, name: 'Daglig sjekkliste' },
        { id: 2, name: 'Ukentlig sjekkliste' },
      ],
      isPreviewing: false,
      ...overrides,
    },
    global: {
      stubs: {
        Dialog: DialogStub,
        DialogScrollContent: { template: '<div><slot /></div>' },
        DialogHeader: { template: '<div><slot /></div>' },
        DialogTitle: { template: '<div><slot /></div>' },
        DialogDescription: { template: '<div><slot /></div>' },
        DialogFooter: { template: '<div><slot /></div>' },
        Button: ButtonStub,
        Input: InputStub,
        Checkbox: CheckboxStub,
        DatePicker: DatePickerStub,
        Eye: { template: '<span />' },
        ArrowUpDown: { template: '<span />' },
      },
    },
  })
}

async function enableSection(wrapper: ReturnType<typeof mountComponent>, labelText: string) {
  const labels = wrapper.findAll('label.section-option')
  const label = labels.find((node) => node.text().includes(labelText))
  expect(label, `Could not find section label: ${labelText}`).toBeTruthy()
  await label!.find('input[type="checkbox"]').setValue(true)
}

describe('GenerateReportDialog', () => {
  beforeEach(() => {
    toastError.mockReset()
  })

  it('shows toast error and does not emit preview when dates are missing', async () => {
    const wrapper = mountComponent()

    const previewButton = wrapper.findAll('button').find((node) => node.text().includes('Forhåndsvis'))
    expect(previewButton).toBeTruthy()

    await previewButton!.trigger('click')

    expect(toastError).toHaveBeenCalledWith('Velg både fra- og til-dato')
    expect(wrapper.emitted('preview')).toBeFalsy()
  })

  it('emits preview with trimmed title, selected checklist ids and sign-off fields', async () => {
    const wrapper = mountComponent()

    const inputs = wrapper.findAll('input[placeholder]')
    await inputs[0].setValue('  Marsrapport  ')

    const dateButtons = wrapper.findAll('button').filter((node) => {
      const placeholder = node.attributes('data-placeholder')
      return placeholder === 'Velg fra-dato' || placeholder === 'Velg til-dato'
    })
    expect(dateButtons).toHaveLength(2)
    await dateButtons[0].trigger('click')
    await dateButtons[1].trigger('click')

    await enableSection(wrapper, 'Sjekklister')
    await enableSection(wrapper, 'Signering')

    const toggleButton = wrapper.find('button.checklist-picker-toggle')
    await toggleButton.trigger('click')

    const checklistOptions = wrapper.findAll('.checklist-option input[type="checkbox"]')
    expect(checklistOptions).toHaveLength(2)
    await checklistOptions[0].setValue(true)
    await checklistOptions[1].setValue(true)

    const signoffInputs = wrapper.findAll('input[placeholder="Fullt navn"], input[placeholder="F.eks. Daglig leder"]')
    await signoffInputs[0].setValue('Ada Lovelace')
    await signoffInputs[1].setValue('Daglig leder')
    await wrapper.find('textarea').setValue('Alt ser bra ut')

    const previewButton = wrapper.findAll('button').find((node) => node.text().includes('Forhåndsvis'))
    await previewButton!.trigger('click')

    expect(wrapper.emitted('preview')).toHaveLength(1)
    expect(wrapper.emitted('preview')![0][0]).toEqual({
      periodFrom: '2026-03-01',
      periodTo: '2026-03-31',
      title: 'Marsrapport',
      sections: {
        includeComplianceSummary: true,
        includeTemperatureLogs: false,
        includeChecklists: true,
        selectedChecklistIds: [1, 2],
        includeHaccpChecklists: false,
        includeCorrectiveActions: false,
        includeFoodDeviations: false,
        includeAlcoholDeviations: false,
        includeAgeVerification: false,
        includeTrainingOverview: false,
        includeLicenseInfo: false,
        includeSignOff: true,
      },
      signOffName: 'Ada Lovelace',
      signOffTitle: 'Daglig leder',
      signOffComments: 'Alt ser bra ut',
    })
  })

  it('emits undefined optional fields when title and sign-off are empty', async () => {
    const wrapper = mountComponent()

    const dateButtons = wrapper.findAll('button').filter((node) => {
      const placeholder = node.attributes('data-placeholder')
      return placeholder === 'Velg fra-dato' || placeholder === 'Velg til-dato'
    })
    await dateButtons[0].trigger('click')
    await dateButtons[1].trigger('click')

    const previewButton = wrapper.findAll('button').find((node) => node.text().includes('Forhåndsvis'))
    await previewButton!.trigger('click')

    expect(wrapper.emitted('preview')![0][0]).toEqual({
      periodFrom: '2026-03-01',
      periodTo: '2026-03-31',
      title: undefined,
      sections: {
        includeComplianceSummary: true,
        includeTemperatureLogs: false,
        includeChecklists: false,
        selectedChecklistIds: undefined,
        includeHaccpChecklists: false,
        includeCorrectiveActions: false,
        includeFoodDeviations: false,
        includeAlcoholDeviations: false,
        includeAgeVerification: false,
        includeTrainingOverview: false,
        includeLicenseInfo: false,
        includeSignOff: false,
      },
      signOffName: undefined,
      signOffTitle: undefined,
      signOffComments: undefined,
    })
  })

  it('resets state and emits update:open when dialog is opened again', async () => {
    const wrapper = mountComponent()

    await wrapper.find('input[placeholder="F.eks. Internkontroll Mars 2026"]').setValue('Midlertidig tittel')

    const dateButtons = wrapper.findAll('button').filter((node) => {
      const placeholder = node.attributes('data-placeholder')
      return placeholder === 'Velg fra-dato' || placeholder === 'Velg til-dato'
    })
    await dateButtons[0].trigger('click')
    await dateButtons[1].trigger('click')

    await enableSection(wrapper, 'Sjekklister')
    const toggleButton = wrapper.find('button.checklist-picker-toggle')
    await toggleButton.trigger('click')
    await wrapper.find('.checklist-option input[type="checkbox"]').setValue(true)

    await wrapper.get('[data-test="dialog-open"]').trigger('click')

    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('update:open')![0]).toEqual([true])
    expect((wrapper.find('input[placeholder="F.eks. Internkontroll Mars 2026"]').element as HTMLInputElement).value).toBe('')
    expect(wrapper.find('button.checklist-picker-toggle').exists()).toBe(false)
    expect(wrapper.find('.checklist-picker').exists()).toBe(false)
    expect(wrapper.find('textarea').exists()).toBe(false)
  })

  it('emits update:open false when cancel button is clicked', async () => {
    const wrapper = mountComponent()

    const cancelButton = wrapper.findAll('button').find((node) => node.text().includes('Avbryt'))
    expect(cancelButton).toBeTruthy()

    await cancelButton!.trigger('click')

    expect(wrapper.emitted('update:open')).toBeTruthy()
    expect(wrapper.emitted('update:open')![0]).toEqual([false])
  })

  it('disables preview button and shows loading text while previewing', () => {
    const wrapper = mountComponent({ isPreviewing: true })

    const previewButton = wrapper.findAll('button').find((node) => node.text().includes('Genererer...'))
    expect(previewButton).toBeTruthy()
    expect(previewButton!.attributes('disabled')).toBeDefined()
  })
})

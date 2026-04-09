import { mount } from '@vue/test-utils'
import { defineComponent, h, nextTick } from 'vue'
import { describe, expect, it, beforeEach, vi } from 'vitest'
import AlcoholPolicyForm from '../AlcoholPolicyForm.vue'

const {
  upsertMutateAsync,
  uploadMutateAsync,
  toastSuccess,
  toastError,
} = vi.hoisted(() => ({
  upsertMutateAsync: vi.fn(),
  uploadMutateAsync: vi.fn(),
  toastSuccess: vi.fn(),
  toastError: vi.fn(),
}))

vi.mock('axios', () => ({
  default: {
    isAxiosError: (error: unknown) => Boolean((error as { isAxiosError?: boolean })?.isAxiosError),
  },
}))

vi.mock('vue-sonner', () => ({
  toast: {
    success: toastSuccess,
    error: toastError,
  },
}))

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })
  return {
    Upload: icon('Upload'),
    X: icon('X'),
    Loader2: icon('Loader2'),
    FileText: icon('FileText'),
  }
})

vi.mock('@/utils/date', () => ({
  stringToCalendarDate: (value: string | null | undefined) => value ?? undefined,
  dateValueToString: (value: string | undefined) => value ?? null,
}))

vi.mock('@/composables/useAlcoholPolicy', () => ({
  useUpsertAlcoholPolicyMutation: () => ({
    mutateAsync: upsertMutateAsync,
  }),
}))

vi.mock('@/composables/useDocumentUpload', () => ({
  useDocumentUploadMutation: () => ({
    mutateAsync: uploadMutateAsync,
  }),
}))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    name: 'Button',
    emits: ['click'],
    props: {
      type: { type: String, default: 'button' },
      disabled: { type: Boolean, default: false },
    },
    setup(props, { slots, emit, attrs }) {
      return () =>
        h(
          'button',
          {
            ...attrs,
            type: props.type,
            disabled: props.disabled,
            onClick: (e: MouseEvent) => emit('click', e),
          },
          slots.default?.(),
        )
    },
  }),
}))

vi.mock('@/components/ui/input/Input.vue', () => ({
  default: defineComponent({
    name: 'Input',
    props: { modelValue: { type: String, default: '' } },
    emits: ['update:model-value'],
    setup(props, { emit, attrs }) {
      return () =>
        h('input', {
          ...attrs,
          value: props.modelValue,
          onInput: (e: Event) => emit('update:model-value', (e.target as HTMLInputElement).value),
        })
    },
  }),
}))

vi.mock('@/components/ui/textarea/Textarea.vue', () => ({
  default: defineComponent({
    name: 'Textarea',
    props: { modelValue: { type: String, default: '' } },
    emits: ['update:model-value'],
    setup(props, { emit, attrs }) {
      return () =>
        h('textarea', {
          ...attrs,
          value: props.modelValue,
          onInput: (e: Event) => emit('update:model-value', (e.target as HTMLTextAreaElement).value),
        })
    },
  }),
}))

vi.mock('@/components/ui/date-picker/DatePicker.vue', () => ({
  default: defineComponent({
    name: 'DatePicker',
    props: { modelValue: { type: [String, Object], required: false } },
    emits: ['update:modelValue'],
    setup(props, { emit, attrs }) {
      return () =>
        h('input', {
          ...attrs,
          'data-datepicker': 'true',
          value: typeof props.modelValue === 'string' ? props.modelValue : '',
          onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLInputElement).value),
        })
    },
  }),
}))

describe('AlcoholPolicyForm', () => {
  beforeEach(() => {
    upsertMutateAsync.mockReset()
    uploadMutateAsync.mockReset()
    toastSuccess.mockReset()
    toastError.mockReset()
    uploadMutateAsync.mockResolvedValue({ id: 555 })
  })

  it('prefills values in editing mode and submits successfully', async () => {
    upsertMutateAsync.mockResolvedValue({})

    const wrapper = mount(AlcoholPolicyForm, {
      props: {
        editing: true,
        policy: {
          bevillingNumber: '2024/12345',
          bevillingValidUntil: '2099-12-31',
          styrerName: 'Ola Nordmann',
          stedfortrederName: 'Kari Nordmann',
          bevillingDocumentId: 11,
          kunnskapsproveCandidateName: 'Per Person',
          kunnskapsproveBirthDate: '1990-01-01',
          kunnskapsproveType: 'BOTH',
          kunnskapsproveMunicipality: 'Oslo',
          kunnskapsprovePassedDate: '2024-01-15',
          kunnskapsproveDocumentId: 22,
          ageCheckLimit: 'UNDER_25',
          acceptedIdTypes: ['PASS', 'BANKKORT'],
          doubtRoutine: 'Be om legitimasjon ved tvil.',
          intoxicationSigns: 'Se etter tydelige tegn.',
          refusalProcedure: 'Avslå og informer leder.',
        },
      },
    })

    await nextTick()

    const inputs = wrapper.findAll('input:not([type="file"])')

    expect((inputs[0].element as HTMLInputElement).value).toBe('2024/12345')
    expect((inputs[2].element as HTMLInputElement).value).toBe('Ola Nordmann')
    expect((inputs[3].element as HTMLInputElement).value).toBe('Kari Nordmann')
    expect((inputs[4].element as HTMLInputElement).value).toBe('Per Person')
    expect((inputs[7].element as HTMLInputElement).value).toBe('Oslo')

    const text = wrapper.text()
    expect(text).toContain('Bevillingsdokument')
    expect(text).toContain('Kunnskapsprøve-bevis')

    await wrapper.find('form').trigger('submit.prevent')

    expect(upsertMutateAsync).toHaveBeenCalledTimes(1)
    expect(upsertMutateAsync).toHaveBeenCalledWith(
      expect.objectContaining({
        bevillingNumber: '2024/12345',
        ageCheckLimit: 'UNDER_25',
        kunnskapsproveType: 'BOTH',
      }),
    )
    expect(toastSuccess).toHaveBeenCalledWith('Skjenkepolicy oppdatert')
    expect(wrapper.emitted('saved')).toHaveLength(1)
  })

  it('shows validation errors for empty required fields', async () => {
    const wrapper = mount(AlcoholPolicyForm, {
      props: {
        editing: false,
        policy: null,
      },
    })

    await wrapper.find('form').trigger('submit.prevent')

    expect(upsertMutateAsync).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Bevillingsnummer')
    expect(wrapper.findAll('.error-message').length).toBeGreaterThan(0)
  })

  it('uploads and removes a bevilling file', async () => {
    const wrapper = mount(AlcoholPolicyForm, {
      props: {
        editing: false,
        policy: null,
      },
    })

    const file = new File(['dummy'], 'bevilling.pdf', { type: 'application/pdf' })
    const fileInputs = wrapper.findAll('input[type="file"]')
    const input = fileInputs[0].element as HTMLInputElement

    Object.defineProperty(input, 'files', {
      value: [file],
      configurable: true,
    })

    await fileInputs[0].trigger('change')
    await nextTick()
    await Promise.resolve()
    await nextTick()

    expect(uploadMutateAsync).toHaveBeenCalledWith(file)
    expect(wrapper.text()).toContain('bevilling.pdf')

    const removeButtons = wrapper.findAll('button.file-chip-remove')
    expect(removeButtons.length).toBeGreaterThan(0)
    await removeButtons[0].trigger('click')

    expect(wrapper.text()).not.toContain('bevilling.pdf')
  })

  it('shows upload error for invalid file type', async () => {
    const wrapper = mount(AlcoholPolicyForm, {
      props: {
        editing: false,
        policy: null,
      },
    })

    const file = new File(['dummy'], 'bevilling.txt', { type: 'text/plain' })
    const fileInputs = wrapper.findAll('input[type="file"]')
    const input = fileInputs[0].element as HTMLInputElement

    Object.defineProperty(input, 'files', {
      value: [file],
      configurable: true,
    })

    await fileInputs[0].trigger('change')

    expect(toastError).toHaveBeenCalledWith('Kun PDF, JPG og PNG er tillatt')
    expect(uploadMutateAsync).not.toHaveBeenCalled()
  })
})

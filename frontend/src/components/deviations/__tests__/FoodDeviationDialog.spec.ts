import { mount } from '@vue/test-utils'
import { defineComponent, h, nextTick } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import FoodDeviationDialog from '../FoodDeviationFormDialog.vue'

vi.mock('@internationalized/date', () => ({
  CalendarDate: class CalendarDate {
    year: number
    month: number
    day: number

    constructor(year: number, month: number, day: number) {
      this.year = year
      this.month = month
      this.day = day
    }
  },
}))

function pass(name: string, tag = 'div') {
  return defineComponent({
    name,
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h(tag, attrs, slots.default?.())
    },
  })
}

vi.mock('@/components/ui/dialog/Dialog.vue', () => ({ default: pass('Dialog') }))
vi.mock('@/components/ui/dialog/DialogContent.vue', () => ({ default: pass('DialogContent') }))
vi.mock('@/components/ui/dialog/DialogDescription.vue', () => ({
  default: pass('DialogDescription', 'p'),
}))
vi.mock('@/components/ui/dialog/DialogHeader.vue', () => ({ default: pass('DialogHeader') }))
vi.mock('@/components/ui/dialog/DialogTitle.vue', () => ({ default: pass('DialogTitle', 'h2') }))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    name: 'ButtonStub',
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h('button', attrs, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/date-picker/DatePicker.vue', () => ({
  default: defineComponent({
    name: 'DatePickerStub',
    emits: ['update:modelValue'],
    setup(_, { emit }) {
      return () =>
        h(
          'button',
          {
            class: 'stub-date',
            type: 'button',
            onClick: () => emit('update:modelValue', { year: 2026, month: 4, day: 8 }),
          },
          'set-date',
        )
    },
  }),
}))

vi.mock('@/components/ui/time-picker/TimePicker.vue', () => ({
  default: defineComponent({
    name: 'TimePickerStub',
    emits: ['update:hours', 'update:minutes'],
    setup(_, { emit }) {
      return () =>
        h('div', [
          h(
            'button',
            {
              class: 'stub-hours',
              type: 'button',
              onClick: () => emit('update:hours', 9),
            },
            'hours',
          ),
          h(
            'button',
            {
              class: 'stub-minutes',
              type: 'button',
              onClick: () => emit('update:minutes', 30),
            },
            'minutes',
          ),
        ])
    },
  }),
}))

vi.mock('@/components/ui/textarea/Textarea.vue', () => ({
  default: defineComponent({
    name: 'TextareaStub',
    props: {
      modelValue: { type: String, default: '' },
    },
    emits: ['update:modelValue'],
    setup(props, { emit, attrs }) {
      return () =>
        h('textarea', {
          ...attrs,
          value: props.modelValue,
          onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLTextAreaElement).value),
        })
    },
  }),
}))

vi.mock('@/components/ui/select/Select.vue', () => ({
  default: defineComponent({
    name: 'SelectStub',
    inheritAttrs: false,
    props: {
      modelValue: { type: String, default: '' },
    },
    emits: ['update:modelValue'],
    setup(props, { emit, slots, attrs }) {
      return () =>
        h(
          'select',
          {
            ...attrs,
            value: props.modelValue,
            onChange: (e: Event) => emit('update:modelValue', (e.target as HTMLSelectElement).value),
          },
          slots.default?.(),
        )
    },
  }),
}))

vi.mock('@/components/ui/select/SelectContent.vue', () => ({
  default: defineComponent({
    name: 'SelectContentStub',
    setup(_, { slots }) {
      return () => h('div', {}, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectItem.vue', () => ({
  default: defineComponent({
    name: 'SelectItemStub',
    props: {
      value: { type: String, default: '' },
    },
    setup(props, { slots }) {
      return () => h('option', { value: props.value }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectTrigger.vue', () => ({
  default: defineComponent({
    name: 'SelectTriggerStub',
    setup(_, { slots }) {
      return () => h('div', {}, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectValue.vue', () => ({
  default: defineComponent({
    name: 'SelectValueStub',
    setup(_, { slots }) {
      return () => h('span', {}, slots.default?.())
    },
  }),
}))

const members = [
  { userId: 1, label: 'Ada' },
  { userId: 2, label: 'Linus' },
]

describe('FoodDeviationDialog', () => {
  it('shows validation errors on empty submit', async () => {
    const wrapper = mount(FoodDeviationDialog, { props: { open: true, members } })

    await wrapper.get('form').trigger('submit.prevent')

    const text = wrapper.text()
    expect(text).toContain('Dato er påkrevd')
    expect(text).toContain('Tidspunkt er påkrevd')
    expect(text).toContain('Velg hvem som oppdaget avviket')
    expect(text).toContain('Beskrivelse er påkrevd')
    expect(text).toContain('Forebyggende tiltak er påkrevd')
    expect(wrapper.emitted('create')).toBeFalsy()
  })

  it('creates payload from filled form in create mode', async () => {
    const wrapper = mount(FoodDeviationDialog, { props: { open: true, members } })

    await wrapper.get('.stub-date').trigger('click')
    await wrapper.get('.stub-hours').trigger('click')
    await wrapper.get('.stub-minutes').trigger('click')

    const selects = wrapper.findAll('select')
    await selects[0].setValue('1')
    await wrapper.findAll('.chip')[1].trigger('click')
    await wrapper.findAll('.segment-button')[1].trigger('click')

    const textareas = wrapper.findAll('textarea')
    await textareas[0].setValue('  Kjøl var for varm  ')
    await textareas[1].setValue(' Flyttet varer ')
    await selects[1].setValue('2')
    await wrapper.findAll('.stub-hours')[1].trigger('click')
    await wrapper.findAll('.stub-minutes')[1].trigger('click')
    await textareas[2].setValue(' Feil innstilling ')
    await textareas[3].setValue(' Ny rutine ')
    await selects[2].setValue('1')
    await wrapper.findAll('.stub-date')[1].trigger('click')

    await wrapper.get('form').trigger('submit.prevent')

    const payload = wrapper.emitted('create')?.[0]?.[0] as Record<string, unknown>
    expect(payload.description).toBe('Kjøl var for varm')
    expect(payload.immediateAction).toBe('Flyttet varer')
    expect(payload.cause).toBe('Feil innstilling')
    expect(payload.preventiveMeasures).toBe('Ny rutine')
    expect(payload.immediateActionByUserId).toBe(2)
    expect(payload.preventiveResponsibleUserId).toBe(1)
    expect(payload.severity).toBe('MEDIUM')
    expect(payload.deviationType).toBe('RENHOLD')
    expect(payload.reportedAt).toBe('2026-04-08T07:30:00.000Z')
  })

  it('prefills from initial data in edit mode and emits update', async () => {
    const initial = {
      id: 44,
      deviationType: 'ALLERGEN',
      severity: 'HIGH',
      description: 'Gammel tekst',
      immediateAction: 'Stoppet servering',
      immediateActionByUserId: 2,
      immediateActionAt: '2026-04-08T08:15:00Z',
      cause: 'Manglende merking',
      preventiveMeasures: 'Ny sjekkliste',
      preventiveResponsibleUserId: 1,
      preventiveDeadline: '2026-04-20',
      status: 'UNDER_TREATMENT',
      reportedAt: '2026-04-08T07:00:00Z',
    }

    const wrapper = mount(FoodDeviationDialog, {
      props: {
        open: false,
        mode: 'edit',
        members,
        initial,
      },
    })

    await wrapper.setProps({ open: true })
    await nextTick()

    expect(wrapper.text()).toContain('Rediger matavvik')

    const textareas = wrapper.findAll('textarea')
    expect((textareas[0].element as HTMLTextAreaElement).value).toBe('Gammel tekst')

    const selects = wrapper.findAll('select')
    await selects[0].setValue('1')
    await textareas[0].setValue(' Oppdatert tekst ')
    await wrapper.get('form').trigger('submit.prevent')

    const emitted = wrapper.emitted('update')?.[0]?.[0] as {
      id: number
      data: Record<string, unknown>
    }

    expect(emitted.id).toBe(44)
    expect(emitted.data.description).toBe('Oppdatert tekst')
    expect(emitted.data.status).toBe('UNDER_TREATMENT')
  })
})

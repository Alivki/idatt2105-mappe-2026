import {mount} from '@vue/test-utils'
import {defineComponent, h, nextTick} from 'vue'
import {describe, expect, it, vi} from 'vitest'
import AlcoholDeviationDialog from '../AlcoholDeviationFormDialog.vue'

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
    setup(_, {attrs, slots}) {
      return () => h(tag, attrs, slots.default?.())
    },
  })
}

vi.mock('@/components/ui/dialog/Dialog.vue', () => ({default: pass('Dialog')}))
vi.mock('@/components/ui/dialog/DialogContent.vue', () => ({default: pass('DialogContent')}))
vi.mock('@/components/ui/dialog/DialogDescription.vue', () => ({
  default: pass('DialogDescription', 'p'),
}))
vi.mock('@/components/ui/dialog/DialogHeader.vue', () => ({default: pass('DialogHeader')}))
vi.mock('@/components/ui/dialog/DialogTitle.vue', () => ({default: pass('DialogTitle', 'h2')}))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    name: 'ButtonStub',
    inheritAttrs: false,
    setup(_, {attrs, slots}) {
      return () => h('button', attrs, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/date-picker/DatePicker.vue', () => ({
  default: defineComponent({
    name: 'DatePickerStub',
    emits: ['update:modelValue'],
    setup(_, {emit}) {
      return () =>
        h(
          'button',
          {
            class: 'stub-date',
            type: 'button',
            onClick: () => emit('update:modelValue', {year: 2026, month: 4, day: 8}),
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
    setup(_, {emit}) {
      return () =>
        h('div', [
          h(
            'button',
            {
              class: 'stub-hours',
              type: 'button',
              onClick: () => emit('update:hours', 10),
            },
            'hours',
          ),
          h(
            'button',
            {
              class: 'stub-minutes',
              type: 'button',
              onClick: () => emit('update:minutes', 5),
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
      modelValue: {type: String, default: ''},
    },
    emits: ['update:modelValue'],
    setup(props, {emit, attrs}) {
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
      modelValue: {type: String, default: ''},
    },
    emits: ['update:modelValue'],
    setup(props, {emit, slots, attrs}) {
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
    setup(_, {slots}) {
      return () => h('div', {}, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectItem.vue', () => ({
  default: defineComponent({
    name: 'SelectItemStub',
    props: {
      value: {type: String, default: ''},
    },
    setup(props, {slots}) {
      return () => h('option', {value: props.value}, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectTrigger.vue', () => ({
  default: defineComponent({
    name: 'SelectTriggerStub',
    setup(_, {slots}) {
      return () => h('div', {}, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectValue.vue', () => ({
  default: defineComponent({
    name: 'SelectValueStub',
    setup(_, {slots}) {
      return () => h('span', {}, slots.default?.())
    },
  }),
}))

const members = [
  {userId: 1, label: 'Ada'},
  {userId: 2, label: 'Linus'},
]

const penaltySummary = {totalPoints: 3, entries: []}

describe('AlcoholDeviationDialog', () => {
  it('shows penalty warning only for control and police sources when a deviation type is chosen', async () => {
    const wrapper = mount(AlcoholDeviationDialog, {
      props: {open: true, members, penaltySummary},
    })

    expect(wrapper.text()).not.toContain('prikker ved kommunal kontroll')
    expect(wrapper.text()).not.toContain('prikker ved politirapport')

    const selects = wrapper.findAll('select')
    await selects[1].setValue('BRUDD_SJENKETIDER')
    await wrapper.findAll('.source-btn')[1].trigger('click')

    expect(wrapper.text()).toContain('prikker ved kommunal kontroll')
    expect(wrapper.text()).toContain('3')
    expect(wrapper.text()).toContain('7')
  })

  it('shows validation errors on empty submit', async () => {
    const wrapper = mount(AlcoholDeviationDialog, {
      props: {open: true, members, penaltySummary},
    })

    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Dato er påkrevd')
    expect(wrapper.text()).toContain('Tidspunkt er påkrevd')
    expect(wrapper.text()).toContain('Velg hvem som rapporterer')
    expect(wrapper.text()).toContain('Velg type hendelse')
    expect(wrapper.text()).toContain('Umiddelbar handling er påkrevd')
    expect(wrapper.emitted('create')).toBeFalsy()
  })

  it('creates payload from filled form', async () => {
    const wrapper = mount(AlcoholDeviationDialog, {
      props: {open: true, members, penaltySummary},
    })

    await wrapper.get('.stub-date').trigger('click')
    await wrapper.get('.stub-hours').trigger('click')
    await wrapper.get('.stub-minutes').trigger('click')

    const selects = wrapper.findAll('select')
    await selects[0].setValue('1')
    await selects[1].setValue('BRUDD_SJENKETIDER')

    const textareas = wrapper.findAll('textarea')
    await textareas[0].setValue('  Beskrivelse  ')
    await textareas[1].setValue('  Stoppet servering  ')
    await wrapper.findAll('.chip')[4].trigger('click')
    await textareas[2].setValue('  Underbemanning  ')
    await textareas[3].setValue('  Ny opplæring  ')
    await selects[2].setValue('2')
    await wrapper.findAll('.stub-date')[1].trigger('click')

    await wrapper.get('form').trigger('submit.prevent')

    const payload = wrapper.emitted('create')?.[0]?.[0] as Record<string, unknown>
    expect(payload.reportSource).toBe('EGENRAPPORT')
    expect(payload.deviationType).toBe('BRUDD_SJENKETIDER')
    expect(payload.description).toBe('Beskrivelse')
    expect(payload.immediateAction).toBe('Stoppet servering')
    expect(payload.causalAnalysis).toBe('UNDERBEMANNING')
    expect(payload.causalExplanation).toBe('Underbemanning')
    expect(payload.preventiveMeasures).toBe('Ny opplæring')
    expect(payload.preventiveResponsibleUserId).toBe(2)
    expect(payload.reportedAt).toBe('2026-04-08T08:05:00.000Z')
  })

  it('prefills edit mode and emits update with status', async () => {
    const initial = {
      id: 9,
      reportSource: 'POLITIRAPPORT',
      deviationType: 'NARKOTIKA',
      description: 'Opprinnelig tekst',
      immediateAction: 'Ringte politi',
      causalAnalysis: 'RUTINE_MANGLER',
      causalExplanation: 'Årsak',
      preventiveMeasures: 'Tiltak',
      preventiveResponsibleUserId: 1,
      preventiveDeadline: '2026-04-20',
      status: 'CLOSED',
      reportedByUserId: 2,
      reportedAt: '2026-04-08T09:00:00Z',
    }

    const wrapper = mount(AlcoholDeviationDialog, {
      props: {
        open: false,
        mode: 'edit',
        members,
        penaltySummary,
        initial,
      },
    })

    await wrapper.setProps({open: true})
    await nextTick()

    expect(wrapper.text()).toContain('Rediger alkoholavvik')

    const textareas = wrapper.findAll('textarea')
    expect((textareas[0].element as HTMLTextAreaElement).value).toBe('Opprinnelig tekst')

    const selects = wrapper.findAll('select')
    await selects[0].setValue('2')
    await textareas[0].setValue(' Ny tekst ')
    await textareas[1].setValue(' Oppdatert handling ')
    await textareas[2].setValue(' Ny årsak ')
    await textareas[3].setValue(' Nye tiltak ')
    await wrapper.get('form').trigger('submit.prevent')

    const emitted = wrapper.emitted('update')?.[0]?.[0] as {
      id: number
      data: Record<string, unknown>
    }

    expect(emitted.id).toBe(9)
    expect(emitted.data.description).toBe('Ny tekst')
    expect(emitted.data.status).toBe('CLOSED')
  })
})

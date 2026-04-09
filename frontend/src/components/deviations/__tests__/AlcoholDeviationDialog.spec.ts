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

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    user: {
      id: 99,
      userId: 99,
      name: 'Ada Lovelace',
      fullName: 'Ada Lovelace',
      firstName: 'Ada',
      lastName: 'Lovelace',
    },
    role: 'admin',
    memberships: [],
    organizationId: 1,
    isAuthenticated: true,
  }),
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
          onInput: (event: Event) =>
            emit('update:modelValue', (event.target as HTMLTextAreaElement).value),
        })
    },
  }),
}))

describe('AlcoholDeviationDialog', () => {
  it('renders', async () => {
    const wrapper = mount(AlcoholDeviationDialog, {
      props: {
        modelValue: true,
      },
    })

    await nextTick()
    expect(wrapper.exists()).toBe(true)
  })
})

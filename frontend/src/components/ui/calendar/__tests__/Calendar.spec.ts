import { mount } from '@vue/test-utils'
import { defineComponent, h, nextTick } from 'vue'
import { describe, it, expect } from 'vitest'
import { CalendarDate } from '@internationalized/date'
import Calendar from '../Calendar.vue'

const SelectStub = defineComponent({
  name: 'Select',
  props: {
    modelValue: String,
  },
  emits: ['update:modelValue'],
  setup(_, { slots }) {
    return () => h('div', { class: 'select-stub' }, slots.default?.())
  },
})

const SelectTriggerStub = defineComponent({
  name: 'SelectTrigger',
  setup(_, { slots }) {
    return () => h('button', { class: 'select-trigger-stub' }, slots.default?.())
  },
})

const SelectContentStub = defineComponent({
  name: 'SelectContent',
  setup(_, { slots }) {
    return () => h('div', { class: 'select-content-stub' }, slots.default?.())
  },
})

const SelectItemStub = defineComponent({
  name: 'SelectItem',
  props: {
    value: String,
  },
  setup(_, { slots }) {
    return () => h('div', { class: 'select-item-stub' }, slots.default?.())
  },
})

function mountCalendar(props: Record<string, unknown> = {}) {
  return mount(Calendar, {
    props: {
      defaultPlaceholder: new CalendarDate(2025, 5, 10),
      ...props,
    },
    global: {
      stubs: {
        Select: SelectStub,
        SelectTrigger: SelectTriggerStub,
        SelectContent: SelectContentStub,
        SelectItem: SelectItemStub,
      },
    },
  })
}

describe('Calendar', () => {
  it('renders', () => {
    const wrapper = mountCalendar()
    expect(wrapper.exists()).toBe(true)
  })

  it('renders select stubs', () => {
    const wrapper = mountCalendar()
    const selects = wrapper.findAllComponents(SelectStub)

    expect(selects).toHaveLength(2)
  })

  it('renders month and year text', () => {
    const wrapper = mountCalendar()
    const text = wrapper.text()

    expect(text.length).toBeGreaterThan(0)
  })

  it('updates placeholder when month and year selects emit values', async () => {
    const wrapper = mountCalendar()
    const selects = wrapper.findAllComponents(SelectStub)

    expect(selects).toHaveLength(2)

    await selects[0]!.vm.$emit('update:modelValue', '12')
    await selects[1]!.vm.$emit('update:modelValue', '2030')
    await nextTick()

    const emitted = wrapper.emitted('update:placeholder')
    expect(emitted).toBeTruthy()
    expect(emitted!.length).toBeGreaterThanOrEqual(2)

    const last = emitted!.at(-1)![0] as CalendarDate
    expect(last.month).toBe(12)
    expect(last.year).toBe(2030)
  })

  it('resets day to 1 when month and year change', async () => {
    const wrapper = mountCalendar()
    const selects = wrapper.findAllComponents(SelectStub)

    expect(selects).toHaveLength(2)

    await selects[0]!.vm.$emit('update:modelValue', '1')
    await selects[1]!.vm.$emit('update:modelValue', '2026')
    await nextTick()

    const emitted = wrapper.emitted('update:placeholder')
    expect(emitted).toBeTruthy()

    const last = emitted!.at(-1)![0] as CalendarDate
    expect(last.month).toBe(1)
    expect(last.year).toBe(2026)
    expect(last.day).toBe(1)
  })

  it('emits multiple placeholder updates when both selects change', async () => {
    const wrapper = mountCalendar()
    const selects = wrapper.findAllComponents(SelectStub)

    await selects[0]!.vm.$emit('update:modelValue', '6')
    await selects[1]!.vm.$emit('update:modelValue', '2027')
    await nextTick()

    const emitted = wrapper.emitted('update:placeholder')
    expect(emitted).toBeTruthy()
    expect(emitted!.length).toBeGreaterThanOrEqual(2)
  })

  it('uses provided defaultPlaceholder', () => {
    const wrapper = mountCalendar({
      defaultPlaceholder: new CalendarDate(2032, 8, 4),
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text().length).toBeGreaterThan(0)
  })
})

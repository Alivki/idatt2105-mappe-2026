import {mount} from '@vue/test-utils'
import {defineComponent, h, nextTick} from 'vue'
import {describe, it, expect, vi, beforeEach, afterEach} from 'vitest'
import {CalendarDate} from '@internationalized/date'
import DatePicker from '../DatePicker.vue'

vi.mock('lucide-vue-next', () => ({
  CalendarIcon: defineComponent({
    name: 'CalendarIcon',
    setup() {
      return () => h('svg', {class: 'calendar-icon-stub'})
    },
  }),
}))

vi.mock('@/components/ui/calendar', () => ({
  Calendar: defineComponent({
    name: 'Calendar',
    props: {
      modelValue: {type: null, required: false},
      defaultPlaceholder: {type: null, required: false},
      initialFocus: {type: Boolean, required: false},
    },
    emits: ['update:modelValue'],
    setup(props, {emit}) {
      return () =>
        h('div', {class: 'calendar-stub'}, [
          h('div', {class: 'calendar-props'}, [
            h('span', {class: 'calendar-has-model'}, String(!!props.modelValue)),
            h('span', {class: 'calendar-has-default-placeholder'}, String(!!props.defaultPlaceholder)),
            h('span', {class: 'calendar-initial-focus'}, String(!!props.initialFocus)),
          ]),
          h(
            'button',
            {
              class: 'calendar-select-btn',
              onClick: () => emit('update:modelValue', new CalendarDate(2026, 12, 24)),
            },
            'select date',
          ),
        ])
    },
  }),
}))

const TransitionStub = defineComponent({
  name: 'TransitionStub',
  setup(_, {slots}) {
    return () => slots.default?.()
  },
})

describe('DatePicker', () => {
  const addSpy = vi.spyOn(document, 'addEventListener')
  const removeSpy = vi.spyOn(document, 'removeEventListener')

  beforeEach(() => {
    addSpy.mockClear()
    removeSpy.mockClear()
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  function mountComponent(props: Record<string, unknown> = {}) {
    return mount(DatePicker, {
      props,
      global: {
        stubs: {
          Transition: TransitionStub,
        },
      },
    })
  }

  it('renders placeholder when modelValue is missing', () => {
    const wrapper = mountComponent({
      placeholder: 'Velg dato her',
    })

    expect(wrapper.text()).toContain('Velg dato her')
    expect(wrapper.find('.date-picker__text--placeholder').exists()).toBe(true)
  })

  it('uses default placeholder text when placeholder prop is not provided', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Velg dato')
  })

  it('renders formatted date when modelValue exists', () => {
    const wrapper = mountComponent({
      modelValue: new CalendarDate(2025, 5, 17),
    })

    expect(wrapper.find('.date-picker__text--placeholder').exists()).toBe(false)
    expect(wrapper.text()).toContain('17')
    expect(wrapper.text()).toContain('2025')
  })

  it('toggles calendar panel when trigger is clicked', async () => {
    const wrapper = mountComponent()

    expect(wrapper.find('.date-picker__panel').exists()).toBe(false)

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(false)
  })

  it('passes expected props to Calendar', async () => {
    const wrapper = mountComponent({
      modelValue: new CalendarDate(2025, 1, 5),
    })

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    expect(wrapper.find('.calendar-stub').exists()).toBe(true)
    expect(wrapper.find('.calendar-has-model').text()).toBe('true')
    expect(wrapper.find('.calendar-has-default-placeholder').text()).toBe('true')
    expect(wrapper.find('.calendar-initial-focus').text()).toBe('true')
  })

  it('emits update:modelValue and closes panel on calendar select', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)

    await wrapper.find('.calendar-select-btn').trigger('click')
    await nextTick()

    const emitted = wrapper.emitted('update:modelValue')
    expect(emitted).toBeTruthy()
    expect(emitted).toHaveLength(1)

    const value = emitted![0][0] as CalendarDate
    expect(value.year).toBe(2026)
    expect(value.month).toBe(12)
    expect(value.day).toBe(24)

    expect(wrapper.find('.date-picker__panel').exists()).toBe(false)
  })

  it('closes when clicking outside', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)

    document.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}))
    await nextTick()

    expect(wrapper.find('.date-picker__panel').exists()).toBe(false)
  })

  it('does not close when clicking inside root element', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)

    await wrapper.find('.date-picker').trigger('mousedown')
    await nextTick()

    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)
  })

  it('closes on Escape key', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape', bubbles: true}))
    await nextTick()

    expect(wrapper.find('.date-picker__panel').exists()).toBe(false)
  })

  it('does not close on other keys', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()
    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}))
    await nextTick()

    expect(wrapper.find('.date-picker__panel').exists()).toBe(true)
  })

  it('adds document listeners when opened', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    expect(addSpy).toHaveBeenCalledWith('mousedown', expect.any(Function))
    expect(addSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
  })

  it('removes document listeners when closed', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    expect(removeSpy).toHaveBeenCalledWith('mousedown', expect.any(Function))
    expect(removeSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
  })

  it('removes listeners on unmount', async () => {
    const wrapper = mountComponent()

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    wrapper.unmount()

    expect(removeSpy).toHaveBeenCalledWith('mousedown', expect.any(Function))
    expect(removeSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
  })

  it('applies upward panel class when openUpward is true', async () => {
    const wrapper = mountComponent({
      openUpward: true,
    })

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    const panel = wrapper.find('.date-picker__panel')
    expect(panel.exists()).toBe(true)
    expect(panel.classes()).toContain('date-picker__panel--up')
  })

  it('does not apply upward panel class when openUpward is false', async () => {
    const wrapper = mountComponent({
      openUpward: false,
    })

    await wrapper.find('.date-picker__trigger').trigger('click')
    await nextTick()

    const panel = wrapper.find('.date-picker__panel')
    expect(panel.exists()).toBe(true)
    expect(panel.classes()).not.toContain('date-picker__panel--up')
  })

  it('renders icon stub', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('.calendar-icon-stub').exists()).toBe(true)
  })
})

import {mount} from '@vue/test-utils'
import {defineComponent, h, nextTick} from 'vue'
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import TimePicker from '../TimePicker.vue'

vi.mock('lucide-vue-next', () => ({
  Clock: defineComponent({
    name: 'Clock',
    setup() {
      return () => h('svg', {class: 'clock-icon-stub'})
    },
  }),
  ChevronDown: defineComponent({
    name: 'ChevronDown',
    setup() {
      return () => h('svg', {class: 'chevron-icon-stub'})
    },
  }),
}))

const TransitionStub = defineComponent({
  name: 'TransitionStub',
  setup(_, {slots}) {
    return () => slots.default?.()
  },
})

describe('TimePicker', () => {
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
    return mount(TimePicker, {
      props,
      attachTo: document.body,
      global: {
        stubs: {
          Transition: TransitionStub,
        },
      },
    })
  }

  function getHourButtons(wrapper: ReturnType<typeof mount>) {
    return wrapper.findAll('.time-picker__col')[0]!.findAll('.time-picker__item')
  }

  function getMinuteButtons(wrapper: ReturnType<typeof mount>) {
    return wrapper.findAll('.time-picker__col')[1]!.findAll('.time-picker__item')
  }

  function getLastEmittedValue(wrapper: ReturnType<typeof mount>, eventName: string) {
    const events = wrapper.emitted(eventName)
    expect(events).toBeTruthy()
    return events!.at(-1)![0]
  }

  async function changeInput(wrapper: ReturnType<typeof mount>, value: string) {
    const input = wrapper.get('.time-picker__input')
    ;(input.element as HTMLInputElement).value = value
    await input.trigger('change')
    await nextTick()
    return input
  }

  it('renders with default display value and placeholder prop', () => {
    const wrapper = mountComponent()

    const input = wrapper.get('.time-picker__input')
    expect((input.element as HTMLInputElement).value).toBe('00:00')
    expect(input.attributes('placeholder')).toBe('00:00')
  })

  it('renders provided hours and minutes padded', () => {
    const wrapper = mountComponent({
      hours: 7,
      minutes: 5,
      placeholder: 'Velg tid',
    })

    const input = wrapper.get('.time-picker__input')
    expect((input.element as HTMLInputElement).value).toBe('07:05')
    expect(input.attributes('placeholder')).toBe('Velg tid')
  })

  it('renders icon stubs', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('.clock-icon-stub').exists()).toBe(true)
    expect(wrapper.find('.chevron-icon-stub').exists()).toBe(true)
  })

  it('toggles panel when toggle button is clicked', async () => {
    const wrapper = mountComponent()

    expect(wrapper.find('.time-picker__panel').exists()).toBe(false)

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()
    expect(wrapper.find('.time-picker__panel').exists()).toBe(true)

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()
    expect(wrapper.find('.time-picker__panel').exists()).toBe(false)
  })

  it('renders 24 hour options and 60 minute options by default', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    expect(getHourButtons(wrapper)).toHaveLength(24)
    expect(getMinuteButtons(wrapper)).toHaveLength(60)
  })

  it('uses minuteStep to reduce minute options', async () => {
    const wrapper = mountComponent({
      minuteStep: 15,
    })

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    const minuteButtons = getMinuteButtons(wrapper)
    expect(minuteButtons).toHaveLength(4)
    expect(minuteButtons[0]!.text()).toBe('00')
    expect(minuteButtons[1]!.text()).toBe('15')
    expect(minuteButtons[2]!.text()).toBe('30')
    expect(minuteButtons[3]!.text()).toBe('45')
  })

  it('marks active hour and minute', async () => {
    const wrapper = mountComponent({
      hours: 9,
      minutes: 30,
      minuteStep: 15,
    })

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    const active = wrapper.findAll('.time-picker__item--active')
    expect(active).toHaveLength(2)
    expect(active[0]!.text()).toBe('09')
    expect(active[1]!.text()).toBe('30')
  })

  it('emits update:hours when selecting an hour and keeps panel open', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    await getHourButtons(wrapper)[13]!.trigger('click')
    await nextTick()

    expect(getLastEmittedValue(wrapper, 'update:hours')).toBe(13)
    expect(wrapper.find('.time-picker__panel').exists()).toBe(true)
  })

  it('emits update:minutes when selecting a minute and closes panel', async () => {
    const wrapper = mountComponent({
      minuteStep: 15,
    })

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    await getMinuteButtons(wrapper)[2]!.trigger('click')
    await nextTick()

    expect(getLastEmittedValue(wrapper, 'update:minutes')).toBe(30)
    expect(wrapper.find('.time-picker__panel').exists()).toBe(false)
  })

  it('parses hh:mm input on change', async () => {
    const wrapper = mountComponent({
      hours: 1,
      minutes: 2,
    })

    const input = await changeInput(wrapper, '09:45')

    expect(getLastEmittedValue(wrapper, 'update:hours')).toBe(9)
    expect(getLastEmittedValue(wrapper, 'update:minutes')).toBe(45)
    expect((input.element as HTMLInputElement).value).toBe('01:02')
  })

  it('parses hh.mm input on change', async () => {
    const wrapper = mountComponent({
      hours: 3,
      minutes: 4,
    })

    const input = await changeInput(wrapper, '7.8')

    expect(getLastEmittedValue(wrapper, 'update:hours')).toBe(7)
    expect(getLastEmittedValue(wrapper, 'update:minutes')).toBe(8)
    expect((input.element as HTMLInputElement).value).toBe('03:04')
  })

  it('parses compact hhmm input on change', async () => {
    const wrapper = mountComponent()

    await changeInput(wrapper, '1345')

    expect(getLastEmittedValue(wrapper, 'update:hours')).toBe(13)
    expect(getLastEmittedValue(wrapper, 'update:minutes')).toBe(45)
  })

  it('parses single hour input and sets minutes to zero', async () => {
    const wrapper = mountComponent()

    await changeInput(wrapper, '8')

    expect(getLastEmittedValue(wrapper, 'update:hours')).toBe(8)
    expect(getLastEmittedValue(wrapper, 'update:minutes')).toBe(0)
  })

  it('clamps oversized input values', async () => {
    const wrapper = mountComponent()

    await changeInput(wrapper, '77:99')

    expect(getLastEmittedValue(wrapper, 'update:hours')).toBe(23)
    expect(getLastEmittedValue(wrapper, 'update:minutes')).toBe(59)
  })

  it('ignores invalid input and restores displayed value', async () => {
    const wrapper = mountComponent({
      hours: 6,
      minutes: 30,
    })

    const beforeHoursCount = wrapper.emitted('update:hours')?.length ?? 0
    const beforeMinutesCount = wrapper.emitted('update:minutes')?.length ?? 0

    const input = await changeInput(wrapper, 'abc')

    const afterHoursCount = wrapper.emitted('update:hours')?.length ?? 0
    const afterMinutesCount = wrapper.emitted('update:minutes')?.length ?? 0

    expect(afterHoursCount).toBe(beforeHoursCount)
    expect(afterMinutesCount).toBe(beforeMinutesCount)
    expect((input.element as HTMLInputElement).value).toBe('06:30')
  })

  it('adds document listeners when opened', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    expect(addSpy).toHaveBeenCalledWith('mousedown', expect.any(Function))
    expect(addSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
  })

  it('removes document listeners when closed with toggle', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()
    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    expect(removeSpy).toHaveBeenCalledWith('mousedown', expect.any(Function))
    expect(removeSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
  })

  it('closes on click outside', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()
    expect(wrapper.find('.time-picker__panel').exists()).toBe(true)

    document.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}))
    await nextTick()

    expect(wrapper.find('.time-picker__panel').exists()).toBe(false)
  })

  it('does not close on click inside component', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()
    expect(wrapper.find('.time-picker__panel').exists()).toBe(true)

    await wrapper.get('.time-picker').trigger('mousedown')
    await nextTick()

    expect(wrapper.find('.time-picker__panel').exists()).toBe(true)
  })

  it('closes on Escape key', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape', bubbles: true}))
    await nextTick()

    expect(wrapper.find('.time-picker__panel').exists()).toBe(false)
  })

  it('does not close on non-Escape key', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter', bubbles: true}))
    await nextTick()

    expect(wrapper.find('.time-picker__panel').exists()).toBe(true)
  })

  it('removes document listeners on unmount', async () => {
    const wrapper = mountComponent()

    await wrapper.get('.time-picker__toggle').trigger('click')
    await nextTick()

    wrapper.unmount()

    expect(removeSpy).toHaveBeenCalledWith('mousedown', expect.any(Function))
    expect(removeSpy).toHaveBeenCalledWith('keydown', expect.any(Function))
  })
})

import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { CalendarDate } from '@internationalized/date'
import CalendarCellTrigger from '../CalendarCellTrigger.vue'

function makeCalendarCtx(overrides: Record<string, unknown> = {}) {
  return {
    isSelected: vi.fn(() => false),
    isToday: vi.fn(() => false),
    isOutsideView: vi.fn(() => false),
    isDisabled: vi.fn(() => false),
    selectDate: vi.fn(),
    ...overrides,
  }
}

describe('CalendarCellTrigger', () => {
  it('renders the day number by default', () => {
    const day = new CalendarDate(2026, 4, 8)
    const ctx = makeCalendarCtx()
    const wrapper = mount(CalendarCellTrigger, {
      props: { day, month: new CalendarDate(2026, 4, 1) },
      global: { provide: { calendar: ctx } },
    })

    expect(wrapper.text()).toBe('8')
  })

  it('sets selected and today attributes and gives focusable tabindex', () => {
    const day = new CalendarDate(2026, 4, 8)
    const ctx = makeCalendarCtx({
      isSelected: vi.fn(() => true),
      isToday: vi.fn(() => true),
    })

    const wrapper = mount(CalendarCellTrigger, {
      props: { day, month: new CalendarDate(2026, 4, 1) },
      global: { provide: { calendar: ctx } },
    })

    const button = wrapper.get('button')
    expect(button.attributes('data-selected')).toBe('true')
    expect(button.attributes('data-today')).toBe('true')
    expect(button.attributes('tabindex')).toBe('0')
  })

  it('marks outside-view and disabled dates and does not select disabled dates', async () => {
    const day = new CalendarDate(2026, 5, 1)
    const ctx = makeCalendarCtx({
      isOutsideView: vi.fn(() => true),
      isDisabled: vi.fn(() => true),
    })

    const wrapper = mount(CalendarCellTrigger, {
      props: { day, month: new CalendarDate(2026, 4, 1) },
      global: { provide: { calendar: ctx } },
    })

    const button = wrapper.get('button')
    expect(button.attributes('data-outside-view')).toBe('true')
    expect(button.attributes('data-disabled')).toBe('true')
    expect(button.attributes()).toHaveProperty('disabled')
    expect(button.attributes('tabindex')).toBe('-1')

    await button.trigger('click')
    expect(ctx.selectDate).not.toHaveBeenCalled()
  })

  it('selects an enabled date on click', async () => {
    const day = new CalendarDate(2026, 4, 8)
    const ctx = makeCalendarCtx()

    const wrapper = mount(CalendarCellTrigger, {
      props: { day, month: new CalendarDate(2026, 4, 1) },
      global: { provide: { calendar: ctx } },
    })

    await wrapper.get('button').trigger('click')
    expect(ctx.selectDate).toHaveBeenCalledWith(day)
  })

  it('renders slot content instead of the day number', () => {
    const day = new CalendarDate(2026, 4, 8)
    const ctx = makeCalendarCtx()

    const wrapper = mount(CalendarCellTrigger, {
      props: { day, month: new CalendarDate(2026, 4, 1) },
      slots: { default: 'custom day' },
      global: { provide: { calendar: ctx } },
    })

    expect(wrapper.text()).toBe('custom day')
  })
})

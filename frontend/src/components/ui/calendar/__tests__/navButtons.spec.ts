import {describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import CalendarNextButton from '../CalendarNextButton.vue'
import CalendarPrevButton from '../CalendarPrevButton.vue'

vi.mock('lucide-vue-next', () => ({
  ChevronLeft: {name: 'ChevronLeft', template: '<svg data-test="chevron-left" />'},
  ChevronRight: {name: 'ChevronRight', template: '<svg data-test="chevron-right" />'},
}))

describe('calendar navigation buttons', () => {
  it('calls prevMonth when previous button is clicked', async () => {
    const prevMonth = vi.fn()
    const wrapper = mount(CalendarPrevButton, {
      global: {
        provide: {
          calendar: {prevMonth, disabled: false},
        },
      },
    })

    await wrapper.get('button').trigger('click')
    expect(prevMonth).toHaveBeenCalledTimes(1)
    expect(wrapper.get('button').attributes('aria-label')).toBe('Previous month')
    expect(wrapper.find('[data-test="chevron-left"]').exists()).toBe(true)
  })

  it('calls nextMonth when next button is clicked', async () => {
    const nextMonth = vi.fn()
    const wrapper = mount(CalendarNextButton, {
      global: {
        provide: {
          calendar: {nextMonth, disabled: false},
        },
      },
    })

    await wrapper.get('button').trigger('click')
    expect(nextMonth).toHaveBeenCalledTimes(1)
    expect(wrapper.get('button').attributes('aria-label')).toBe('Next month')
    expect(wrapper.find('[data-test="chevron-right"]').exists()).toBe(true)
  })

  it('respects disabled state and allows slot override', () => {
    const wrapper = mount(CalendarPrevButton, {
      slots: {default: '<span data-test="custom-slot">custom</span>'},
      global: {
        provide: {
          calendar: {prevMonth: vi.fn(), disabled: true},
        },
      },
    })

    expect(wrapper.get('button').attributes()).toHaveProperty('disabled')
    expect(wrapper.find('[data-test="custom-slot"]').exists()).toBe(true)
  })
})

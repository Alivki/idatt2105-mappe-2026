import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { CalendarDate } from '@internationalized/date'
import CalendarCell from '../CalendarCell.vue'
import CalendarGrid from '../CalendarGrid.vue'
import CalendarGridBody from '../CalendarGridBody.vue'
import CalendarGridHead from '../CalendarGridHead.vue'
import CalendarGridRow from '../CalendarGridRow.vue'
import CalendarHeadCell from '../CalendarHeadCell.vue'
import CalendarHeader from '../CalendarHeader.vue'

describe('presentational calendar components', () => {
  it('renders CalendarCell with slot content', () => {
    const wrapper = mount(CalendarCell, {
      props: { date: new CalendarDate(2026, 4, 8) },
      slots: { default: 'day cell' },
    })

    expect(wrapper.find('td.calendar-cell').exists()).toBe(true)
    expect(wrapper.text()).toContain('day cell')
  })

  it('renders CalendarGrid and nested body/head/row/head cell/header slots', () => {
    const GridFixture = {
      components: {
        CalendarGrid,
        CalendarGridBody,
        CalendarGridHead,
        CalendarGridRow,
        CalendarHeadCell,
        CalendarHeader,
      },
      template: `
        <div>
          <CalendarHeader>header text</CalendarHeader>
          <CalendarGrid>
            <CalendarGridHead>
              <CalendarGridRow>
                <CalendarHeadCell>Mon</CalendarHeadCell>
              </CalendarGridRow>
            </CalendarGridHead>
            <CalendarGridBody>
              <CalendarGridRow>
                <td>body cell</td>
              </CalendarGridRow>
            </CalendarGridBody>
          </CalendarGrid>
        </div>
      `,
    }

    const wrapper = mount(GridFixture)

    expect(wrapper.find('.calendar-header').text()).toContain('header text')
    expect(wrapper.find('table.calendar-grid[role="grid"]').exists()).toBe(true)
    expect(wrapper.find('thead').exists()).toBe(true)
    expect(wrapper.find('tbody').exists()).toBe(true)
    expect(wrapper.find('tr.calendar-grid-row').exists()).toBe(true)
    expect(wrapper.find('th.calendar-head-cell').text()).toBe('Mon')
    expect(wrapper.text()).toContain('body cell')
  })
})

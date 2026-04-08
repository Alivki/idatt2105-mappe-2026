import { describe, expect, it } from 'vitest'
import { ref } from 'vue'
import { CalendarDate } from '@internationalized/date'
import { useCalendar } from '../useCalendar'

describe('useCalendar', () => {
  it('uses sensible defaults and returns one month grid', () => {
    const placeholder = ref(new CalendarDate(2026, 4, 8))
    const { weekDays, grid } = useCalendar({ placeholder })

    expect(weekDays.value).toHaveLength(7)
    expect(grid.value).toHaveLength(1)
    expect(grid.value[0]?.value.toString()).toBe('2026-04-01')
  })

  it('respects custom locale and week start', () => {
    const placeholder = ref(new CalendarDate(2026, 4, 8))
    const { weekDays } = useCalendar({
      placeholder,
      locale: 'en-US',
      weekStartsOn: 0,
    })

    expect(weekDays.value[0]?.toLowerCase()).toContain('sun')
    expect(weekDays.value[6]?.toLowerCase()).toContain('sat')
  })

  it('updates grid reactively when placeholder changes', () => {
    const placeholder = ref(new CalendarDate(2026, 4, 8))
    const { grid } = useCalendar({ placeholder })

    expect(grid.value[0]?.value.toString()).toBe('2026-04-01')
    placeholder.value = new CalendarDate(2027, 1, 2)
    expect(grid.value[0]?.value.toString()).toBe('2027-01-01')
  })

  it('builds a 6-row grid when fixedWeeks is true', () => {
    const placeholder = ref(new CalendarDate(2026, 2, 10))
    const { grid } = useCalendar({ placeholder, fixedWeeks: true })

    expect(grid.value[0]?.rows).toHaveLength(6)
    expect(grid.value[0]?.rows.every((row) => row.length === 7)).toBe(true)
  })

  it('handles leap years correctly', () => {
    const placeholder = ref(new CalendarDate(2024, 2, 10))
    const { grid } = useCalendar({ placeholder })

    const allDays = grid.value[0]!.rows.flat().map((d) => d.toString())
    expect(allDays).toContain('2024-02-29')
  })

  it('includes leading and trailing dates to fill calendar weeks', () => {
    const placeholder = ref(new CalendarDate(2026, 3, 10))
    const { grid } = useCalendar({ placeholder, weekStartsOn: 1 })

    const firstRow = grid.value[0]!.rows[0]!
    const lastRow = grid.value[0]!.rows.at(-1)!

    expect(firstRow[0]!.month).not.toBe(3)
    expect(lastRow.at(-1)!.month).not.toBe(3)
  })
})

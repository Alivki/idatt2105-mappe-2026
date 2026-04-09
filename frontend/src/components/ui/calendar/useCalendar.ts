import {type Ref, computed} from "vue"
import {CalendarDate, type DateValue} from "@internationalized/date"

export interface MonthGrid {
  value: CalendarDate
  rows: CalendarDate[][]
}

export function useCalendar(opts: {
  placeholder: Ref<DateValue>
  locale?: string
  weekStartsOn?: number
  fixedWeeks?: boolean
}) {
  const locale = opts.locale ?? "nb-NO"
  const weekStartsOn = opts.weekStartsOn ?? 1
  const fixedWeeks = opts.fixedWeeks ?? false

  const weekDays = computed(() => {
    const fmt = new Intl.DateTimeFormat(locale, {weekday: "short"})
    const days: string[] = []
    // Jan 5 2025 is a Sunday (day 0)
    for (let i = 0; i < 7; i++) {
      const d = new Date(2025, 0, 5 + weekStartsOn + i)
      days.push(fmt.format(d))
    }
    return days
  })

  const grid = computed<MonthGrid[]>(() => {
    const p = opts.placeholder.value
    return [buildMonth(p.year, p.month, weekStartsOn, fixedWeeks)]
  })

  return {grid, weekDays}
}

function daysInMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}

function buildMonth(year: number, month: number, weekStartsOn: number, fixedWeeks: boolean): MonthGrid {
  const total = daysInMonth(year, month)
  const firstDow = new Date(year, month - 1, 1).getDay()

  let offset = firstDow - weekStartsOn
  if (offset < 0) offset += 7

  const cells = offset + total
  const rowCount = fixedWeeks ? 6 : Math.ceil(cells / 7)

  const rows: CalendarDate[][] = []
  const cursor = new Date(year, month - 1, 1 - offset)

  for (let r = 0; r < rowCount; r++) {
    const week: CalendarDate[] = []
    for (let c = 0; c < 7; c++) {
      week.push(new CalendarDate(cursor.getFullYear(), cursor.getMonth() + 1, cursor.getDate()))
      cursor.setDate(cursor.getDate() + 1)
    }
    rows.push(week)
  }

  return {value: new CalendarDate(year, month, 1), rows}
}

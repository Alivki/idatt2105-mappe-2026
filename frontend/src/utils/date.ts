import { CalendarDate } from '@internationalized/date'
import type { DateValue } from '@internationalized/date'

export function stringToCalendarDate(str: string | null | undefined): CalendarDate | undefined {
  if (!str) return undefined
  const [y, m, d] = str.split('-').map(Number)
  return new CalendarDate(y!, m!, d!)
}

export function dateValueToString(dv: DateValue | undefined): string {
  if (!dv) return ''
  return `${dv.year}-${String(dv.month).padStart(2, '0')}-${String(dv.day).padStart(2, '0')}`
}

export function dateValueToIso(dv: DateValue | undefined): string | undefined {
  if (!dv) return undefined
  return new Date(dv.year, dv.month - 1, dv.day).toISOString()
}

export function timeToString(h: number | undefined, m: number | undefined): string {
  if (h == null || m == null) return ''
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

export function isoToDateString(iso: string | null): string {
  if (!iso) return ''
  return iso.slice(0, 10)
}

export function relativeTime(dateStr: string): string {
  const diffMs = Date.now() - new Date(dateStr).getTime()
  const min = 60_000; const hr = 3_600_000; const day = 86_400_000
  if (diffMs < hr) return `${Math.max(1, Math.floor(diffMs / min))} min siden`
  if (diffMs < day) { const h = Math.floor(diffMs / hr); return `${h} time${h > 1 ? 'r' : ''} siden` }
  const d = Math.floor(diffMs / day); return `${d} dag${d > 1 ? 'er' : ''} siden`
}

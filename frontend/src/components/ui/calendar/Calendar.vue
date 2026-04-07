<script setup lang="ts">
import { provide, ref, computed, watch, nextTick, onMounted, type Ref } from "vue"
import { CalendarDate, type DateValue, today, getLocalTimeZone, isSameDay } from "@internationalized/date"
import { useCalendar } from "./useCalendar"
import CalendarHeader from "./CalendarHeader.vue"
import CalendarPrevButton from "./CalendarPrevButton.vue"
import CalendarNextButton from "./CalendarNextButton.vue"
import CalendarGrid from "./CalendarGrid.vue"
import CalendarGridHead from "./CalendarGridHead.vue"
import CalendarGridBody from "./CalendarGridBody.vue"
import CalendarGridRow from "./CalendarGridRow.vue"
import CalendarHeadCell from "./CalendarHeadCell.vue"
import CalendarCell from "./CalendarCell.vue"
import CalendarCellTrigger from "./CalendarCellTrigger.vue"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
} from "@/components/ui/select"

const props = withDefaults(defineProps<{
  modelValue?: DateValue
  defaultPlaceholder?: DateValue
  initialFocus?: boolean
  locale?: string
  weekStartsOn?: number
  fixedWeeks?: boolean
  disabled?: boolean
  isDateDisabled?: (date: DateValue) => boolean
  minValue?: DateValue
  maxValue?: DateValue
}>(), {
  locale: "nb-NO",
  weekStartsOn: 1,
  fixedWeeks: false,
  disabled: false,
})

const emits = defineEmits<{
  (e: "update:modelValue", value: DateValue): void
  (e: "update:placeholder", value: DateValue): void
}>()

const todayDate = today(getLocalTimeZone())
const placeholder = ref(props.modelValue ?? props.defaultPlaceholder ?? todayDate) as Ref<DateValue>

watch(() => props.modelValue, (v) => {
  if (v) placeholder.value = v
})

const { grid, weekDays } = useCalendar({
  placeholder,
  locale: props.locale,
  weekStartsOn: props.weekStartsOn,
  fixedWeeks: props.fixedWeeks,
})

function selectDate(date: DateValue) {
  if (props.disabled) return
  if (props.isDateDisabled?.(date)) return
  if (props.minValue && date.compare(props.minValue) < 0) return
  if (props.maxValue && date.compare(props.maxValue) > 0) return
  emits("update:modelValue", date)
  placeholder.value = date
}

function prevMonth() {
  const p = placeholder.value
  if (p.month === 1) {
    placeholder.value = new CalendarDate(p.year - 1, 12, 1)
  } else {
    placeholder.value = new CalendarDate(p.year, p.month - 1, 1)
  }
  emits("update:placeholder", placeholder.value)
}

function nextMonth() {
  const p = placeholder.value
  if (p.month === 12) {
    placeholder.value = new CalendarDate(p.year + 1, 1, 1)
  } else {
    placeholder.value = new CalendarDate(p.year, p.month + 1, 1)
  }
  emits("update:placeholder", placeholder.value)
}

function setMonth(month: number) {
  const p = placeholder.value
  placeholder.value = new CalendarDate(p.year, month, 1)
  emits("update:placeholder", placeholder.value)
}

function setYear(year: number) {
  const p = placeholder.value
  placeholder.value = new CalendarDate(year, p.month, 1)
  emits("update:placeholder", placeholder.value)
}

function isSelected(date: DateValue): boolean {
  return !!props.modelValue && isSameDay(props.modelValue, date)
}

function isTodayDate(date: DateValue): boolean {
  return isSameDay(todayDate, date)
}

function isOutsideView(date: DateValue, monthValue: DateValue): boolean {
  return date.month !== monthValue.month || date.year !== monthValue.year
}

function isDateDisabledCheck(date: DateValue): boolean {
  if (props.disabled) return true
  if (props.isDateDisabled?.(date)) return true
  if (props.minValue && date.compare(props.minValue) < 0) return true
  if (props.maxValue && date.compare(props.maxValue) > 0) return true
  return false
}

provide("calendar", {
  placeholder,
  selectDate,
  isSelected,
  isToday: isTodayDate,
  isOutsideView,
  isDisabled: isDateDisabledCheck,
  prevMonth,
  nextMonth,
  disabled: computed(() => props.disabled),
})

// Month/year select data
const months = [
  { value: '1', short: 'jan' },
  { value: '2', short: 'feb' },
  { value: '3', short: 'mar' },
  { value: '4', short: 'apr' },
  { value: '5', short: 'mai' },
  { value: '6', short: 'jun' },
  { value: '7', short: 'jul' },
  { value: '8', short: 'aug' },
  { value: '9', short: 'sep' },
  { value: '10', short: 'okt' },
  { value: '11', short: 'nov' },
  { value: '12', short: 'des' },
]

const currentYear = new Date().getFullYear()
const years = Array.from({ length: 110 }, (_, i) => currentYear - 100 + i)

const rootRef = ref<HTMLElement | null>(null)
onMounted(() => {
  if (props.initialFocus) {
    nextTick(() => {
      const btn = rootRef.value?.querySelector("[data-today], [data-selected]") as HTMLElement | null
      btn?.focus()
    })
  }
})
</script>

<template>
  <div ref="rootRef" class="calendar" :data-disabled="disabled || undefined">
    <CalendarHeader>
      <CalendarPrevButton />
      <div class="calendar__selects">
        <Select
          :model-value="String(placeholder.month)"
          @update:model-value="(v: string) => setMonth(Number(v))"
        >
          <SelectTrigger class="calendar__select calendar__select--month">
            <span class="select-trigger__content">{{ months[placeholder.month - 1]!.short }}</span>
          </SelectTrigger>
          <SelectContent class="calendar__select-dropdown">
            <SelectItem v-for="m in months" :key="m.value" :value="m.value">
              {{ m.short }}
            </SelectItem>
          </SelectContent>
        </Select>
        <Select
          :model-value="String(placeholder.year)"
          @update:model-value="(v: string) => setYear(Number(v))"
        >
          <SelectTrigger class="calendar__select calendar__select--year">
            <span class="select-trigger__content">{{ placeholder.year }}</span>
          </SelectTrigger>
          <SelectContent class="calendar__select-dropdown">
            <SelectItem v-for="y in years" :key="y" :value="String(y)">
              {{ y }}
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
      <CalendarNextButton />
    </CalendarHeader>

    <div class="calendar__grids">
      <CalendarGrid v-for="month in grid" :key="month.value.toString()">
        <CalendarGridHead>
          <CalendarGridRow>
            <CalendarHeadCell v-for="day in weekDays" :key="day">
              {{ day }}
            </CalendarHeadCell>
          </CalendarGridRow>
        </CalendarGridHead>
        <CalendarGridBody>
          <CalendarGridRow v-for="(weekDates, index) in month.rows" :key="`week-${index}`">
            <CalendarCell
              v-for="weekDate in weekDates"
              :key="weekDate.toString()"
              :date="weekDate"
            >
              <CalendarCellTrigger :day="weekDate" :month="month.value" />
            </CalendarCell>
          </CalendarGridRow>
        </CalendarGridBody>
      </CalendarGrid>
    </div>
  </div>
</template>

<style scoped>
.calendar {
  padding: 0.75rem;
}

.calendar__selects {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.calendar__grids {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
}

:deep(.calendar__select) {
  height: 1.625rem;
  min-height: 0;
  padding: 0 0.375rem;
  font-size: 0.75rem;
  line-height: 1;
  border-radius: 0.375rem;
  gap: 0.125rem;
  box-shadow: none;
}

:deep(.calendar__select--month) {
  width: 3.75rem;
}

:deep(.calendar__select--year) {
  width: 4rem;
}

:deep(.calendar__select .select-trigger__icon) {
  width: 0.75rem;
  height: 0.75rem;
  margin-left: 0.125rem;
}

:deep(.calendar__select-dropdown) {
  max-height: 15rem;
}
</style>

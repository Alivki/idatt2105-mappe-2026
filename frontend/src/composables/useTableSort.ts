import { ref, type Ref } from 'vue'

export function useTableSort<T extends string>(defaultField: T, defaultDir: 'asc' | 'desc' = 'asc') {
  const sortField = ref<T>(defaultField) as Ref<T>
  const sortDir = ref<'asc' | 'desc'>(defaultDir)

  function toggleSort(field: T) {
    if (sortField.value === field) {
      sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
    } else {
      sortField.value = field
      sortDir.value = 'asc'
    }
  }

  return { sortField, sortDir, toggleSort }
}

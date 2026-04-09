import { ref, computed, type Ref, type ComputedRef } from 'vue'

export function usePagination<T>(items: Ref<T[]> | ComputedRef<T[]>, perPage = 10) {
  const currentPage = ref(1)

  const totalPages = computed(() => Math.max(1, Math.ceil(items.value.length / perPage)))

  const pagedItems = computed(() => {
    const start = (currentPage.value - 1) * perPage
    return items.value.slice(start, start + perPage)
  })

  const summary = computed(() => {
    const total = items.value.length
    if (total === 0) return 'Viser 0 av 0'
    const start = (currentPage.value - 1) * perPage + 1
    const end = Math.min(currentPage.value * perPage, total)
    return `Viser ${start}–${end} av ${total}`
  })

  function goToPage(page: number) {
    currentPage.value = Math.max(1, Math.min(page, totalPages.value))
  }

  return { currentPage, totalPages, pagedItems, summary, goToPage }
}

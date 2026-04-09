<script setup lang="ts">
import {ref, computed} from 'vue'
import {toast} from 'vue-sonner'
import AppLayout from '@/components/layout/AppLayout.vue'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import AlcoholPolicyForm from '@/components/alcohol-policy/AlcoholPolicyForm.vue'
import AlcoholPolicyDisplayCard from '@/components/alcohol-policy/AlcoholPolicyDisplayCard.vue'
import {
  useAlcoholPolicyExistsQuery,
  useAlcoholPolicyQuery,
} from '@/composables/useAlcoholPolicy'
import {useDocumentUrlQuery} from '@/composables/useDocumentUpload'

const existsQuery = useAlcoholPolicyExistsQuery()
const policyExists = computed(() => existsQuery.data.value === true)
const policyQuery = useAlcoholPolicyQuery(() => policyExists.value)
const policy = computed(() => policyQuery.data.value ?? null)

const editing = ref(false)
const showForm = computed(() => {
  if (existsQuery.isLoading.value) return false
  if (editing.value) return true
  return !policyExists.value
})
const showCard = computed(() => policyExists.value && !editing.value && !!policy.value)

function handleSaved() {
  editing.value = false
}

async function openDocument(docId: number) {
  try {
    const res = await useDocumentUrlQuery(docId)
    window.open(res.url, '_blank')
  } catch {
    toast.error('Kunne ikke hente dokumentet')
  }
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">Skjenkepolicy</span>
      </div>
    </header>

    <div class="page-content">
      <div v-if="existsQuery.isLoading.value" class="loading-state">
        <div class="skeleton-card skeleton-card--header"></div>
        <div class="skeleton-row">
          <div class="skeleton-card skeleton-card--doc"></div>
          <div class="skeleton-card skeleton-card--doc"></div>
        </div>
      </div>

      <AlcoholPolicyForm
        v-else-if="showForm"
        :policy="policy"
        :editing="editing"
        @saved="handleSaved"
        @cancel="editing = false"
      />

      <AlcoholPolicyDisplayCard
        v-else-if="showCard"
        :policy="policy!"
        @edit="editing = true"
        @open-document="openDocument"
      />

      <div v-else-if="policyExists && policyQuery.isLoading.value" class="loading-state">
        <div class="skeleton-card skeleton-card--header"></div>
        <div class="skeleton-row">
          <div class="skeleton-card skeleton-card--doc"></div>
          <div class="skeleton-card skeleton-card--doc"></div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<style scoped>
.page-header {
  display: flex;
  height: 4rem;
  flex-shrink: 0;
  align-items: center;
}

.page-header-inner {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0 1rem;
}

.header-separator {
  height: 1rem !important;
  width: 1px !important;
  margin-right: 0.5rem;
}

.page-title {
  font-weight: 500;
  color: hsl(var(--sidebar-primary, 245 43% 52%));
}

.page-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 1rem;
  padding: 0 1rem 1rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-row {
  display: flex;
  gap: 14px;
}

.skeleton-card {
  border-radius: var(--radius-xl);
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

.skeleton-card--header {
  height: 60px;
  max-width: 400px;
}

.skeleton-card--doc {
  height: 240px;
  flex: 1;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 768px) {
  .skeleton-row {
    flex-direction: column;
  }
}
</style>

<script setup lang="ts">
import {computed} from 'vue'
import {useAuthStore} from '@/stores/auth'
import AppLayout from '@/components/layout/AppLayout.vue'
import {SidebarTrigger} from '@/components/ui/sidebar'
import AdminAgeVerificationView from './AdminAgeVerificationView.vue'
import EmployeeAgeVerificationView from './EmployeeAgeVerificationView.vue'

const auth = useAuthStore()
const canSeeAll = computed(() =>
  ['ADMIN', 'MANAGER'].includes(auth.role ?? '')
)
</script>

<template>
  <AppLayout>
    <template #header>
      <div class="section-header">
        <SidebarTrigger/>
        <span class="section-badge">IK-Alkohol</span>
      </div>
    </template>

    <AdminAgeVerificationView v-if="canSeeAll"/>
    <EmployeeAgeVerificationView v-else/>
  </AppLayout>
</template>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid hsl(var(--border));
  background: hsl(var(--muted) / 0.5);
}

.section-badge {
  font-size: 0.75rem;
  font-weight: 500;
  background: hsl(var(--green-bg, 143 60% 95%));
  color: var(--green);
  border: 1px solid hsl(var(--green-border, 143 40% 80%));
  border-radius: var(--radius-pill);
  padding: 0.25rem 0.75rem;
}
</style>

<script setup lang="ts">
import {computed, ref, toRef} from 'vue'
import {useMediaQuery} from '@vueuse/core'
import {Shield, ShieldCheck, User, Plus} from 'lucide-vue-next'
import {toast} from 'vue-sonner'
import AppLayout from '@/components/layout/AppLayout.vue'
import Button from '@/components/ui/button/Button.vue'
import {Separator} from '@/components/ui/separator'
import {SidebarTrigger} from '@/components/ui/sidebar'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import EmployeeTable from '@/components/employees/EmployeeTable.vue'
import InviteEmployeeDialog from '@/components/employees/InviteEmployeeDialog.vue'
import {useAuthStore} from '@/stores/auth'
import {
  useMembersQuery,
  useOrganizationQuery,
  useUpdateMemberRoleMutation,
  useRemoveMemberMutation,
  useInviteMutation,
} from '@/composables/useMembers'
import {useTableSort} from '@/composables/useTableSort'
import type {OrganizationMember} from '@/types/member'
import type {AxiosError} from 'axios'

type ApiError = AxiosError<{ error: { message: string } }>

const auth = useAuthStore()
const search = ref('')

const orgId = toRef(() => auth.organizationId)
const {data: members, isLoading: membersLoading} = useMembersQuery()
const {data: org} = useOrganizationQuery(orgId)

const updateRoleMutation = useUpdateMemberRoleMutation()
const removeMemberMutation = useRemoveMemberMutation()
const inviteMutation = useInviteMutation()
const isMobile = useMediaQuery('(max-width: 768px)')

const roleLabel: Record<string, string> = {ADMIN: 'Admin', MANAGER: 'Leder', EMPLOYEE: 'Ansatt'}
const roleOptions = ['ADMIN', 'MANAGER', 'EMPLOYEE'] as const

type SortField = 'name' | 'email' | 'role'
const {sortField, sortDir, toggleSort} = useTableSort<SortField>('name')

const isSelf = (member: OrganizationMember) => member.userId === auth.user?.id
const isAdmin = computed(() => auth.role === 'ADMIN')

const sortedAndFilteredMembers = computed(() => {
  if (!members.value) return []
  const q = search.value.toLowerCase().trim()
  let list = members.value.slice()
  if (q) {
    list = list.filter((m) =>
      [m.userFullName, m.userEmail, roleLabel[m.role] ?? m.role].some((v) => v.toLowerCase().includes(q)),
    )
  }
  const self = list.filter((m) => isSelf(m))
  const others = list.filter((m) => !isSelf(m))
  others.sort((a, b) => {
    let cmp = 0
    if (sortField.value === 'name') cmp = a.userFullName.localeCompare(b.userFullName, 'nb')
    else if (sortField.value === 'email') cmp = a.userEmail.localeCompare(b.userEmail, 'nb')
    else if (sortField.value === 'role') {
      const order = {ADMIN: 0, MANAGER: 1, EMPLOYEE: 2}
      cmp = (order[a.role as keyof typeof order] ?? 3) - (order[b.role as keyof typeof order] ?? 3)
    }
    return sortDir.value === 'desc' ? -cmp : cmp
  })
  return [...self, ...others]
})

const stats = computed(() => {
  const list = members.value ?? []
  return {
    total: list.length,
    managers: list.filter((m) => m.role === 'MANAGER').length,
    admins: list.filter((m) => m.role === 'ADMIN').length,
  }
})

const roleDialogOpen = ref(false)
const editingMember = ref<OrganizationMember | null>(null)
const selectedRole = ref('')

function openRoleEdit(member: OrganizationMember) {
  editingMember.value = member
  selectedRole.value = member.role
  roleDialogOpen.value = true
}

function saveRole() {
  const member = editingMember.value
  const role = selectedRole.value
  roleDialogOpen.value = false
  editingMember.value = null
  if (!member || role === member.role) return
  updateRoleMutation.mutate({userId: member.userId, payload: {role}})
}

function cancelRoleEdit() {
  roleDialogOpen.value = false
  editingMember.value = null
}

const removeDialogOpen = ref(false)
const removingMember = ref<OrganizationMember | null>(null)

function openRemoveDialog(member: OrganizationMember) {
  removingMember.value = member
  removeDialogOpen.value = true
}

function confirmRemove() {
  const member = removingMember.value
  removeDialogOpen.value = false
  removingMember.value = null
  if (!member) return
  removeMemberMutation.mutate(member.userId)
}

function cancelRemove() {
  removeDialogOpen.value = false
  removingMember.value = null
}

const addDialogOpen = ref(false)

function handleInvite(payload: { email: string; role: string }) {
  inviteMutation.mutate(
    {email: payload.email, role: payload.role},
    {
      onSuccess: () => {
        addDialogOpen.value = false
        toast.success('Invitasjon sendt til ' + payload.email)
      },
      onError: (error: Error) => {
        const msg = (error as ApiError)?.response?.data?.error?.message ?? 'Kunne ikke sende invitasjon'
        toast.error(msg)
      },
    },
  )
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger/>
        <Separator orientation="vertical" class="header-separator"/>
        <span class="page-title">Ansatte</span>
      </div>
    </header>

    <div class="page-content">
      <section class="header-row">
        <div>
          <h1>Ansatte</h1>
          <p>Administrer og se oversikt over alle ansatte.</p>
        </div>
        <Button @click="addDialogOpen = true">
          <Plus :size="16" aria-hidden="true"/>
          Legg til ansatt
        </Button>
      </section>

      <div v-if="membersLoading" class="loading-state">
        <div class="skeleton-item skeleton-item--wide"></div>
        <div class="skeleton-item skeleton-item--table"></div>
      </div>

      <template v-else-if="members && members.length > 0">
        <section class="org-card">
          <div class="org-left">
            <div class="org-avatar">{{ (org?.name ?? '??').slice(0, 2).toUpperCase() }}</div>
            <div class="org-details">
              <h2>{{ org?.name ?? 'Laster...' }}</h2>
              <span v-if="org?.orgNumber" class="org-meta">Org. nr: {{ org.orgNumber }}</span>
            </div>
          </div>
          <div class="org-stats">
            <div class="stat-item">
              <span class="stat-label">ANSATTE</span>
              <strong class="stat-value">{{ stats.total }}</strong>
            </div>
            <div class="stat-item">
              <span class="stat-label">LEDERE</span>
              <strong class="stat-value">{{ stats.managers }}</strong>
            </div>
            <div class="stat-item">
              <span class="stat-label">ADMIN</span>
              <strong class="stat-value">{{ stats.admins }}</strong>
            </div>
          </div>
        </section>

        <EmployeeTable
          :members="sortedAndFilteredMembers"
          :search="search"
          :sort-field="sortField"
          :sort-dir="sortDir"
          :is-mobile="isMobile"
          :is-admin="isAdmin"
          :current-user-id="auth.user?.id"
          @update:search="search = $event"
          @toggle-sort="toggleSort($event as SortField)"
          @edit-role="openRoleEdit"
          @remove-member="openRemoveDialog"
        />
      </template>

      <section v-else-if="!membersLoading" class="landing-card">
        <h1>Ansatte</h1>
        <p>Denne virksomheten har ingen ansatte registrert ennå.</p>
        <div class="landing-box">
          <h3>Tom oversikt</h3>
          <span>Når ansatte legges til for denne virksomheten, vil oversikten vises her.</span>
        </div>
      </section>
    </div>

    <AlertDialog :open="roleDialogOpen" @update:open="(v: boolean) => { roleDialogOpen = v }">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Endre rolle</AlertDialogTitle>
          <AlertDialogDescription>
            Velg ny rolle for {{ editingMember?.userFullName }}.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <div class="role-options">
          <label
            v-for="role in roleOptions"
            :key="role"
            class="role-option"
            :class="{ 'role-option--selected': selectedRole === role }"
          >
            <input v-model="selectedRole" type="radio" :value="role" class="sr-only"/>
            <Shield v-if="role === 'ADMIN'" :size="18" aria-hidden="true"/>
            <ShieldCheck v-else-if="role === 'MANAGER'" :size="18" aria-hidden="true"/>
            <User v-else :size="18" aria-hidden="true"/>
            <span>{{ roleLabel[role] }}</span>
          </label>
        </div>
        <AlertDialogFooter>
          <AlertDialogCancel @click="cancelRoleEdit">Avbryt</AlertDialogCancel>
          <AlertDialogAction @click="saveRole">Lagre</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <AlertDialog :open="removeDialogOpen" @update:open="(v: boolean) => { removeDialogOpen = v }">
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>Fjern fra organisasjon?</AlertDialogTitle>
          <AlertDialogDescription>
            {{ removingMember?.userFullName }} vil miste tilgangen til denne organisasjonen.
            Brukerkontoen blir ikke slettet.
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel @click="cancelRemove">Avbryt</AlertDialogCancel>
          <AlertDialogAction variant="destructive" @click="confirmRemove">Fjern</AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>

    <InviteEmployeeDialog
      :open="addDialogOpen"
      :is-pending="inviteMutation.isPending.value"
      @update:open="addDialogOpen = $event"
      @invite="handleInvite"
    />
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

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.header-row p {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 0.88rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-item {
  border-radius: var(--radius-xl);
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

.skeleton-item--wide {
  height: 80px;
}

.skeleton-item--table {
  height: 300px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.org-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
}

.org-left {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  min-width: 0;
}

.org-avatar {
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--brand-soft);
  color: var(--brand);
  font-size: 1.1rem;
  font-weight: 700;
  flex-shrink: 0;
}

.org-details {
  min-width: 0;
}

.org-details h2 {
  margin: 0;
  font-size: 1.15rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.org-meta {
  color: hsl(var(--muted-foreground));
  font-size: 0.85rem;
}

.org-stats {
  display: flex;
  gap: 1.5rem;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.1rem;
}

.stat-label {
  font-size: 0.6rem;
  font-weight: 500;
  letter-spacing: 0.06em;
  color: hsl(var(--muted-foreground));
}

.stat-value {
  font-size: 1.4rem;
  font-weight: 700;
  line-height: 1;
  color: hsl(var(--foreground));
}

.landing-card {
  padding: 1.5rem;
  border: 1px solid hsl(var(--border));
  border-radius: 1rem;
  background: hsl(var(--card));
}

.landing-card h1 {
  margin: 0;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.landing-card p {
  margin: 0.35rem 0 1.25rem;
  color: hsl(var(--muted-foreground));
}

.landing-box {
  border: 1px dashed color-mix(in srgb, var(--brand-soft) 60%, var(--brand) 15%);
  background: var(--brand-soft);
  color: var(--brand);
  border-radius: 1rem;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.landing-box h3 {
  margin: 0;
  font-size: 1.1rem;
}

.landing-box span {
  color: hsl(var(--muted-foreground));
}

.role-options {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s;
}

.role-option:hover {
  background: hsl(var(--muted) / 0.5);
}

.role-option--selected {
  border-color: var(--brand);
  background: var(--brand-soft);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

@media (max-width: 700px) {
  h1 {
    font-size: 1.5rem;
  }

  .header-row {
    flex-direction: column;
    gap: 0.75rem;
  }

  .org-card {
    flex-direction: column;
    align-items: flex-start;
    padding: 0.9rem 1rem;
  }

  .org-stats {
    align-self: flex-start;
    flex-wrap: wrap;
    gap: 0.75rem 1rem;
  }

  .header-row > :last-child {
    width: 100%;
  }

  .header-row > :last-child :deep(button) {
    width: 100%;
  }
}
</style>

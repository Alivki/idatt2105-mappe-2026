<script setup lang="ts">
import {MoreVertical, ShieldCheck, UserMinus, ArrowUpDown, Search} from 'lucide-vue-next'
import Badge from '@/components/ui/badge/Badge.vue'
import Button from '@/components/ui/button/Button.vue'
import {
  Table, TableBody, TableCell, TableEmpty, TableHead, TableHeader, TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem,
  DropdownMenuLabel, DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu'
import type {OrganizationMember} from '@/types/member'

const props = defineProps<{
  members: OrganizationMember[]
  search: string
  sortField: string
  sortDir: string
  isMobile: boolean
  isAdmin: boolean
  currentUserId: number | undefined
}>()

const emits = defineEmits<{
  (e: 'update:search', v: string): void
  (e: 'toggle-sort', field: string): void
  (e: 'edit-role', member: OrganizationMember): void
  (e: 'remove-member', member: OrganizationMember): void
}>()

const roleLabel: Record<string, string> = {
  ADMIN: 'Admin',
  MANAGER: 'Leder',
  EMPLOYEE: 'Ansatt',
}

function initials(name: string) {
  return name.split(' ').slice(0, 2).map((w) => w[0]).join('').toUpperCase()
}

function badgeTone(role: string) {
  if (role === 'ADMIN') return 'brand'
  if (role === 'MANAGER') return 'warning'
  return 'ok'
}

function isSelf(member: OrganizationMember) {
  return member.userId === props.currentUserId
}
</script>

<template>
  <section class="table-section">
    <div class="search-wrapper">
      <Search :size="16" class="search-icon" aria-hidden="true"/>
      <input :value="search" class="search-input" placeholder="Søk etter ansatt..."
             aria-label="Søk etter ansatt"
             @input="emits('update:search', ($event.target as HTMLInputElement).value)"/>
    </div>

    <div v-if="isMobile" class="mobile-member-list">
      <article
        v-for="member in members"
        :key="member.id"
        class="mobile-member-card"
        :class="isSelf(member) ? 'row-self' : ''"
      >
        <div class="mobile-member-top">
          <div class="cell-user">
            <div class="user-avatar" :class="{ 'user-avatar--self': isSelf(member) }">
              {{ initials(member.userFullName) }}
            </div>
            <div>
              <span class="user-name">{{ member.userFullName }}</span>
              <span v-if="isSelf(member)" class="you-label">(deg)</span>
            </div>
          </div>

          <div v-if="!isSelf(member)" class="actions-wrapper">
            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger"
                        aria-label="Handlinger">
                  <MoreVertical :size="18" aria-hidden="true"/>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" :side-offset="4">
                <DropdownMenuLabel>Handlinger</DropdownMenuLabel>
                <DropdownMenuSeparator/>
                <DropdownMenuItem @click="emits('edit-role', member)">
                  <ShieldCheck :size="16" aria-hidden="true"/>
                  Endre rolle
                </DropdownMenuItem>
                <template v-if="isAdmin">
                  <DropdownMenuSeparator/>
                  <DropdownMenuItem class="menu-item--danger"
                                    @click="emits('remove-member', member)">
                    <UserMinus :size="16" aria-hidden="true"/>
                    Fjern fra organisasjon
                  </DropdownMenuItem>
                </template>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>

        <div class="mobile-member-row">
          <span>E-post</span>
          <span class="cell-email">{{ member.userEmail }}</span>
        </div>
        <div class="mobile-member-row">
          <span>Rolle</span>
          <Badge :tone="badgeTone(member.role)">
            {{ roleLabel[member.role] ?? member.role }}
          </Badge>
        </div>
      </article>

      <div v-if="members.length === 0" class="landing-box">
        <h3>Ingen ansatte matcher søket</h3>
        <span>Prøv et annet søk eller inviter en ny ansatt.</span>
      </div>
    </div>

    <div v-else class="table-card">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead class="th-name">
              <Button variant="ghost" size="sm" class="sort-btn"
                      @click="emits('toggle-sort', 'name')">
                Navn
                <ArrowUpDown :size="14" class="sort-icon"
                             :class="{ 'sort-icon--active': sortField === 'name' }"
                             aria-hidden="true"/>
              </Button>
            </TableHead>
            <TableHead class="th-email">
              <Button variant="ghost" size="sm" class="sort-btn"
                      @click="emits('toggle-sort', 'email')">
                E-post
                <ArrowUpDown :size="14" class="sort-icon"
                             :class="{ 'sort-icon--active': sortField === 'email' }"
                             aria-hidden="true"/>
              </Button>
            </TableHead>
            <TableHead class="th-role">
              <Button variant="ghost" size="sm" class="sort-btn"
                      @click="emits('toggle-sort', 'role')">
                Rolle
                <ArrowUpDown :size="14" class="sort-icon"
                             :class="{ 'sort-icon--active': sortField === 'role' }"
                             aria-hidden="true"/>
              </Button>
            </TableHead>
            <TableHead class="th-actions"/>
          </TableRow>
        </TableHeader>

        <TableBody>
          <TableRow
            v-for="member in members"
            :key="member.id"
            :class="isSelf(member) ? 'row-self' : ''"
          >
            <TableCell>
              <div class="cell-user">
                <div class="user-avatar" :class="{ 'user-avatar--self': isSelf(member) }">
                  {{ initials(member.userFullName) }}
                </div>
                <div>
                  <span class="user-name">{{ member.userFullName }}</span>
                  <span v-if="isSelf(member)" class="you-label">(deg)</span>
                </div>
              </div>
            </TableCell>
            <TableCell class="cell-email">{{ member.userEmail }}</TableCell>
            <TableCell>
              <Badge :tone="badgeTone(member.role)">
                {{ roleLabel[member.role] ?? member.role }}
              </Badge>
            </TableCell>
            <TableCell class="cell-actions">
              <div v-if="!isSelf(member)" class="actions-wrapper">
                <DropdownMenu>
                  <DropdownMenuTrigger as-child>
                    <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger"
                            aria-label="Handlinger">
                      <MoreVertical :size="18" aria-hidden="true"/>
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" :side-offset="4">
                    <DropdownMenuLabel>Handlinger</DropdownMenuLabel>
                    <DropdownMenuSeparator/>
                    <DropdownMenuItem @click="emits('edit-role', member)">
                      <ShieldCheck :size="16" aria-hidden="true"/>
                      Endre rolle
                    </DropdownMenuItem>
                    <template v-if="isAdmin">
                      <DropdownMenuSeparator/>
                      <DropdownMenuItem class="menu-item--danger"
                                        @click="emits('remove-member', member)">
                        <UserMinus :size="16" aria-hidden="true"/>
                        Fjern fra organisasjon
                      </DropdownMenuItem>
                    </template>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            </TableCell>
          </TableRow>

          <TableEmpty v-if="members.length === 0" :colspan="4">
            Ingen ansatte matcher søket.
          </TableEmpty>
        </TableBody>
      </Table>
    </div>
  </section>
</template>

<style scoped>
.table-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.search-wrapper {
  position: relative;
  width: 16rem;
}

.search-icon {
  position: absolute;
  left: 0.65rem;
  top: 50%;
  transform: translateY(-50%);
  color: hsl(var(--muted-foreground));
  pointer-events: none;
}

.search-input {
  width: 100%;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  padding: 0.5rem 0.75rem 0.5rem 2.1rem;
  font: inherit;
  font-size: 0.85rem;
  color: hsl(var(--foreground));
  background: hsl(var(--card));
}

.search-input:focus {
  outline: none;
  border-color: hsl(var(--ring));
  box-shadow: 0 0 0 2px hsl(var(--ring) / 0.15);
}

.table-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.sort-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  background: none;
  border: none;
  font: inherit;
  font-weight: 500;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  padding: 0.25rem 0.4rem;
  border-radius: var(--radius-md);
  margin: -0.25rem -0.4rem;
  transition: background 150ms ease, color 150ms ease;
}

.sort-btn:hover {
  background: hsl(var(--accent));
  color: hsl(var(--foreground));
}

.sort-icon {
  opacity: 0.4;
  transition: opacity 150ms;
}

.sort-icon--active {
  opacity: 1;
}

.row-self {
  background-color: hsl(var(--muted) / 0.6) !important;
}

.row-self:hover {
  background-color: hsl(var(--muted) / 0.75) !important;
}

.cell-user {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-avatar {
  width: 2.25rem;
  height: 2.25rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  font-size: 0.75rem;
  font-weight: 600;
  flex-shrink: 0;
}

.user-avatar--self {
  background: var(--brand-soft);
  color: var(--brand);
}

.user-name {
  font-weight: 500;
}

.you-label {
  color: hsl(var(--muted-foreground));
  font-size: 0.8rem;
  margin-left: 0.25rem;
}

.cell-email {
  color: hsl(var(--muted-foreground));
}

.cell-actions {
  width: 3rem;
  text-align: right;
}

.actions-wrapper {
  position: relative;
  display: inline-flex;
}

.actions-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2.25rem;
  height: 2.25rem;
  border-radius: var(--radius-md);
  border: none;
  background: none;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition: background 150ms ease, color 150ms ease;
}

.actions-trigger:hover {
  background: hsl(var(--accent));
  color: hsl(var(--foreground));
}

.menu-item--danger {
  color: var(--red);
}

.mobile-member-list {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
}

.mobile-member-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
  padding: 0.5rem 0.65rem;
}

.mobile-member-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.mobile-member-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.3rem 0;
}

.mobile-member-row > span:first-child {
  font-size: 0.73rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.mobile-member-row > :last-child {
  text-align: right;
  min-width: 0;
}

.th-name {
  min-width: 10rem;
}

.th-email {
  min-width: 10rem;
}

.th-role {
  min-width: 6rem;
}

.th-actions {
  width: 3rem;
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

@media (max-width: 700px) {
  .table-card .th-email, .table-card .cell-email {
    display: none;
  }

  .search-wrapper {
    width: 100%;
  }

  .table-card :deep(table) {
    min-width: 36rem;
  }

  .table-section {
    gap: 0.5rem;
  }
}
</style>

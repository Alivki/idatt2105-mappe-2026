<script setup lang="ts">
import {ref, onMounted, onUnmounted} from 'vue'
import {ChevronDown, ChevronUp, MoreVertical, Pencil, Trash2} from 'lucide-vue-next'
import type {TrainingRow} from '@/stores/training.ts'
import EmployeeAvatar from './EmployeeAvatar.vue'
import StatusBadge from './StatusBadge.vue'

defineProps<{
  groupedTrainings: Record<string, TrainingRow[]>
}>()

const emit = defineEmits<{
  edit: [row: TrainingRow]
  delete: [row: TrainingRow]
}>()

const collapsed = ref<Record<string, boolean>>({})
const openMenuId = ref<number | null>(null)

const toggle = (type: string): void => {
  collapsed.value[type] = !collapsed.value[type]
}

function toggleMenu(id: number): void {
  openMenuId.value = openMenuId.value === id ? null : id
}

function handleEdit(row: TrainingRow): void {
  openMenuId.value = null
  emit('edit', row)
}

function handleDelete(row: TrainingRow): void {
  openMenuId.value = null
  emit('delete', row)
}

function onOutsideClick(e: MouseEvent): void {
  if (!(e.target as HTMLElement).closest('.ctx-wrap')) openMenuId.value = null
}

onMounted(() => document.addEventListener('click', onOutsideClick))
onUnmounted(() => document.removeEventListener('click', onOutsideClick))
</script>

<template>
  <div class="training-table">

    <div v-if="!Object.keys(groupedTrainings).length" class="empty-state">
      Ingen resultater matcher filteret.
    </div>

    <div
      v-for="(rows, type) in groupedTrainings"
      :key="type"
      class="group-section"
    >
      <!-- Group header -->
      <button class="group-header" @click="toggle(type)">
        <span class="group-name">{{ type }}</span>
        <span class="group-count">{{ rows.length }} ansatte</span>
        <component :is="collapsed[type] ? ChevronDown : ChevronUp" :size="15" class="chevron-icon"/>
      </button>

      <!-- Table -->
      <div v-if="!collapsed[type]" class="table-wrap">
        <table class="table">
          <thead>
          <tr class="table-header-row">
            <th class="th">Ansatt</th>
            <th class="th hide-mobile">Opplæringstype</th>
            <th class="th hide-mobile">Fullført</th>
            <th class="th">Utløper</th>
            <th class="th">Status</th>
            <th class="th th-action"></th>
          </tr>
          </thead>
          <tbody>
          <tr
            v-for="row in rows"
            :key="row.id"
            class="table-row"
          >
            <td class="td">
              <div class="employee-cell">
                <EmployeeAvatar :initials="row.employee.initials" :color="row.employee.color"
                                size="sm"/>
                <div>
                  <p class="employee-name">{{ row.employee.name }}</p>
                  <p class="employee-role">{{ row.employee.role }}</p>
                </div>
              </div>
            </td>
            <td class="td hide-mobile">{{ row.type }}</td>
            <td class="td hide-mobile">
              {{ row.completed ?? '—' }}
            </td>
            <td class="td" :class="{ 'td--warning': row.status === 'Utløper snart' }">
              {{ row.expires ?? '—' }}
            </td>
            <td class="td">
              <StatusBadge :status="row.status"/>
            </td>

            <!-- ⋮ context menu -->
            <td class="td td-action">
              <div class="ctx-wrap">
                <button
                  :class="['ctx-btn', { 'ctx-btn--active': openMenuId === row.id }]"
                  @click.stop="toggleMenu(row.id)"
                  aria-label="Alternativer"
                >
                  <MoreVertical :size="16"/>
                </button>

                <Transition
                  enter-active-class="menu-enter-active"
                  enter-from-class="menu-enter-from"
                  enter-to-class="menu-enter-to"
                  leave-active-class="menu-leave-active"
                  leave-from-class="menu-enter-to"
                  leave-to-class="menu-enter-from"
                >
                  <div v-if="openMenuId === row.id" class="ctx-menu">
                    <button class="ctx-menu-item" @click="handleEdit(row)">
                      <Pencil :size="14" class="ctx-menu-icon"/>
                      Rediger
                    </button>
                    <div class="ctx-menu-divider"/>
                    <button class="ctx-menu-item ctx-menu-item--danger" @click="handleDelete(row)">
                      <Trash2 :size="14" class="ctx-menu-icon"/>
                      Slett
                    </button>
                  </div>
                </Transition>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<style scoped>
.training-table {
  background: hsl(var(--background));
  border: 1px solid hsl(var(--border));
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.empty-state {
  padding: 4rem 0;
  text-align: center;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
}

.group-section {
  border-bottom: 1px solid hsl(var(--border));
}

.group-section:last-child {
  border-bottom: none;
}

.group-header {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.25rem;
  background: hsl(var(--muted) / 0.5);
  text-align: left;
  border: none;
  cursor: pointer;
  transition: background-color 150ms ease;
}

.group-header:hover {
  background: hsl(var(--muted));
}

.group-name {
  flex: 1;
  font-size: 0.875rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.group-count {
  font-size: 0.75rem;
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
  border-radius: var(--radius-pill);
  padding: 0.125rem 0.625rem;
}

.chevron-icon {
  color: hsl(var(--muted-foreground));
}

.table-wrap {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
}

.table-header-row {
  border-bottom: 1px solid hsl(var(--border));
}

.th {
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  padding: 0.625rem 1.25rem;
}

.th-action {
  width: 2.5rem;
  padding: 0.625rem 0.75rem;
}

.table-row {
  border-bottom: 1px solid hsl(var(--border) / 0.5);
  transition: background-color 150ms ease;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: hsl(var(--muted) / 0.3);
}

.td {
  padding: 0.875rem 1.25rem;
  font-size: 0.875rem;
  color: hsl(var(--muted-foreground));
}

.td--warning {
  color: var(--amber);
  font-weight: 600;
}

.td-action {
  padding: 0.875rem 0.75rem;
}

.employee-cell {
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.employee-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: hsl(var(--foreground));
  margin: 0;
}

.employee-role {
  font-size: 0.75rem;
  color: hsl(var(--muted-foreground));
  margin: 0;
}

.ctx-wrap {
  position: relative;
  display: flex;
  justify-content: center;
}

.ctx-btn {
  width: 1.75rem;
  height: 1.75rem;
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: hsl(var(--muted-foreground));
  border: none;
  background: none;
  cursor: pointer;
  transition: all 150ms ease;
}

.ctx-btn:hover,
.ctx-btn--active {
  background: hsl(var(--muted));
  color: hsl(var(--foreground));
}

.ctx-menu {
  position: absolute;
  right: 0;
  top: 2rem;
  z-index: 50;
  width: 10rem;
  background: hsl(var(--background));
  border-radius: var(--radius-lg);
  border: 1px solid hsl(var(--border));
  box-shadow: var(--shadow-md);
  padding: 0.25rem 0;
  transform-origin: top right;
}

.ctx-menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.5rem 0.875rem;
  font-size: 0.875rem;
  color: hsl(var(--foreground));
  background: none;
  border: none;
  cursor: pointer;
  transition: background-color 150ms ease;
}

.ctx-menu-item:hover {
  background: hsl(var(--muted) / 0.5);
}

.ctx-menu-item--danger {
  color: var(--red);
}

.ctx-menu-item--danger:hover {
  background: hsl(0 80% 95%);
}

.ctx-menu-icon {
  color: hsl(var(--muted-foreground));
}

.ctx-menu-item--danger .ctx-menu-icon {
  color: var(--red);
}

.ctx-menu-divider {
  margin: 0.25rem 0;
  border-top: 1px solid hsl(var(--border));
}

.menu-enter-active {
  transition: opacity 100ms ease, transform 100ms ease;
}

.menu-leave-active {
  transition: opacity 75ms ease, transform 75ms ease;
}

.menu-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.menu-enter-to {
  opacity: 1;
  transform: scale(1);
}

@media (max-width: 768px) {
  .hide-mobile {
    display: none;
  }
}
</style>

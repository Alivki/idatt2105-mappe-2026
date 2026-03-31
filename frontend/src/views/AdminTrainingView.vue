<script setup lang="ts">
import { ref, computed } from 'vue'
import { Plus, Pencil, X, Trash2 } from 'lucide-vue-next'
import type { TrainingRow } from '@/stores/training'
import { useTrainingStore } from '@/stores/training'
import StatCard from '@/components/training/StatCard.vue'
import StatusBadge from '@/components/training/StatusBadge.vue'
import EmployeeAvatar from '@/components/training/EmployeeAvatar.vue'
import FilterPanel from '@/components/training/FilterPanel.vue'
import EditTrainingModal from '@/components/training/EditTrainingModal.vue'
import RegisterTrainingModal from '@/components/training/RegisterTrainingModal.vue'

const store = useTrainingStore()

const filterType   = ref('')
const filterStatus = ref('')

const filtered = computed(() =>
  store.allTrainings.filter(t =>
    (!filterType.value   || t.type   === filterType.value) &&
    (!filterStatus.value || t.status === filterStatus.value)
  )
)

// ── Edit / bulk-delete mode ──────────────────────────────────────────────────
const editMode = ref(false)
const selected = ref<Set<number>>(new Set())

function toggleEditMode(): void {
  editMode.value = !editMode.value
  selected.value = new Set()
}

function toggleSelect(id: number): void {
  const s = new Set(selected.value)
  s.has(id) ? s.delete(id) : s.add(id)
  selected.value = s
}

const allSelected = computed(() =>
  filtered.value.length > 0 && filtered.value.every(r => selected.value.has(r.id))
)

function toggleSelectAll(): void {
  selected.value = allSelected.value
    ? new Set()
    : new Set(filtered.value.map(r => r.id))
}

function deleteSelected(): void {
  selected.value.forEach(id => {
    const row = store.allTrainings.find(r => r.id === id)
    if (row) store.deleteTraining(row.employee.id, row.id)
  })
  selected.value = new Set()
  editMode.value = false
}

// ── Single-row edit / delete ─────────────────────────────────────────────────
const showRegister     = ref(false)
const editModal        = ref(false)
const editRow          = ref<TrainingRow | null>(null)
const deleteConfirmRow = ref<TrainingRow | null>(null)

function openEdit(row: TrainingRow): void {
  editRow.value   = row
  editModal.value = true
}

function confirmDelete(): void {
  if (!deleteConfirmRow.value) return
  store.deleteTraining(deleteConfirmRow.value.employee.id, deleteConfirmRow.value.id)
  deleteConfirmRow.value = null
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 py-7 pb-16">
    <div class="flex items-start justify-between flex-wrap gap-3 mb-7">
      <div>
        <h1 class="text-2xl sm:text-3xl font-bold text-gray-900 tracking-tight">
          Opplæring og sertifiseringer
        </h1>
        <p class="text-sm text-gray-400 mt-0.5">Oversikt over ansattes opplæringsstatus</p>
      </div>

      <div class="flex items-center gap-2">
        <button
          v-if="!editMode"
          class="flex items-center gap-1.5 border border-stone-200 bg-white text-gray-600 rounded-xl px-4 py-2.5 text-sm font-semibold hover:bg-stone-50 transition-colors"
          @click="toggleEditMode"
        >
          <Pencil :size="14" /> Rediger
        </button>

        <template v-else>
          <button
            v-if="selected.size > 0"
            class="flex items-center gap-1.5 bg-red-600 text-white rounded-xl px-4 py-2.5 text-sm font-semibold hover:bg-red-700 transition-colors"
            @click="deleteSelected"
          >
            <Trash2 :size="14" /> Slett ({{ selected.size }})
          </button>
          <button
            class="flex items-center gap-1.5 border border-stone-200 bg-white text-gray-500 rounded-xl px-4 py-2.5 text-sm font-semibold hover:bg-stone-50 transition-colors"
            @click="toggleEditMode"
          >
            <X :size="14" /> Avbryt
          </button>
        </template>

        <button
          v-if="!editMode"
          class="flex items-center gap-1.5 border border-stone-200 bg-white text-indigo-600 rounded-xl px-4 py-2.5 text-sm font-semibold hover:bg-indigo-50 transition-colors"
          @click="showRegister = true"
        >
          <Plus :size="16" /> Registrer opplæring
        </button>
      </div>
    </div>

    <div class="grid grid-cols-2 sm:grid-cols-3 gap-3 mb-6">
      <StatCard label="Totalt ansatte" :value="store.totalEmployees" />

      <StatCard
        label="Fullført opplæring"
        :value="`${store.completedCount} / ${store.totalEmployees}`"
        value-class="text-emerald-700"
      >
        <div class="mt-2.5 h-1.5 bg-stone-100 rounded-full overflow-hidden">
          <div
            class="h-full bg-emerald-600 rounded-full transition-all duration-500"
            :style="{ width: (store.completedCount / store.totalEmployees * 100) + '%' }"
          />
        </div>
      </StatCard>

      <StatCard
        label="Utløper snart"
        :value="store.expiringSoonCount"
        value-class="text-amber-600"
        sub-label="Innen 30 dager"
        class="col-span-2 sm:col-span-1"
      />
    </div>

    <FilterPanel
      :types="store.trainingTypes"
      v-model:modelType="filterType"
      v-model:modelStatus="filterStatus"
    />

    <div class="bg-white border border-stone-200 rounded-2xl overflow-hidden">
      <div v-if="!filtered.length" class="py-16 text-center text-sm text-gray-400">
        Ingen resultater matcher filteret.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
          <tr class="border-b border-stone-100">
            <th v-if="editMode" class="w-12 px-4 py-3">
              <input
                type="checkbox"
                :checked="allSelected"
                class="w-4 h-4 rounded cursor-pointer accent-emerald-600"
                @change="toggleSelectAll"
              />
            </th>
            <th class="text-left text-xs font-semibold text-gray-400 px-5 py-3">Ansatt</th>
            <th class="text-left text-xs font-semibold text-gray-400 px-5 py-3 hidden md:table-cell">Opplæringstype</th>
            <th class="text-left text-xs font-semibold text-gray-400 px-5 py-3 hidden md:table-cell">Fullført</th>
            <th class="text-left text-xs font-semibold text-gray-400 px-5 py-3">Utløper</th>
            <th class="text-left text-xs font-semibold text-gray-400 px-5 py-3">Status</th>
            <th v-if="!editMode" class="w-10 px-3 py-3" />
          </tr>
          </thead>

          <tbody>
          <tr
            v-for="row in filtered"
            :key="row.id"
            :class="[
                'border-b border-stone-100 last:border-b-0 transition-colors',
                editMode ? 'cursor-pointer' : '',
                editMode && selected.has(row.id)
                  ? 'bg-red-50/60'
                  : 'hover:bg-stone-50/60'
              ]"
            @click="editMode && toggleSelect(row.id)"
          >
            <td v-if="editMode" class="px-4 py-3.5">
              <input
                type="checkbox"
                :checked="selected.has(row.id)"
                class="w-4 h-4 rounded cursor-pointer accent-emerald-600"
                @click.stop
                @change="toggleSelect(row.id)"
              />
            </td>

            <td class="px-5 py-3.5">
              <div class="flex items-center gap-2.5">
                <EmployeeAvatar :initials="row.employee.initials" :color="row.employee.color" size="sm" />
                <div>
                  <p class="text-sm font-semibold text-gray-900">{{ row.employee.name }}</p>
                  <p class="text-xs text-gray-400">{{ row.employee.role }}</p>
                </div>
              </div>
            </td>

            <td class="px-5 py-3.5 text-sm text-gray-600 hidden md:table-cell">{{ row.type }}</td>
            <td class="px-5 py-3.5 text-sm text-gray-600 hidden md:table-cell">{{ row.completed ?? '—' }}</td>

            <td
              class="px-5 py-3.5 text-sm"
              :class="row.status === 'Utløper snart' ? 'text-amber-600 font-semibold' : 'text-gray-600'"
            >
              {{ row.expires ?? '—' }}
            </td>

            <td class="px-5 py-3.5">
              <StatusBadge :status="row.status" />
            </td>

            <td v-if="!editMode" class="px-3 py-3.5">
              <button
                class="w-7 h-7 rounded-lg flex items-center justify-center text-gray-300 hover:text-gray-600 hover:bg-stone-100 transition-colors"
                aria-label="Rediger"
                @click.stop="openEdit(row)"
              >
                <Pencil :size="14" />
              </button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <EditTrainingModal
      v-model="editModal"
      :training="editRow"
      :employee-id="editRow?.employee?.id"
    />
    <RegisterTrainingModal v-model="showRegister" />

    <Teleport to="body">
      <div
        v-if="deleteConfirmRow"
        class="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4"
        @click.self="deleteConfirmRow = null"
      >
        <div class="bg-white rounded-2xl w-full max-w-sm shadow-2xl p-6">
          <h2 class="text-base font-bold text-gray-900 mb-1">Slett opplæring</h2>
          <p class="text-sm text-gray-500 mb-5">
            Er du sikker på at du vil slette
            <strong>{{ deleteConfirmRow.type }}</strong> for
            <strong>{{ deleteConfirmRow.employee.name }}</strong>?
            Dette kan ikke angres.
          </p>
          <div class="flex justify-end gap-2">
            <button
              class="border border-stone-200 rounded-lg px-4 py-2 text-sm text-gray-500 hover:bg-stone-50 transition-colors"
              @click="deleteConfirmRow = null"
            >
              Avbryt
            </button>
            <button
              class="bg-red-600 text-white rounded-lg px-4 py-2 text-sm font-semibold hover:bg-red-700 transition-colors"
              @click="confirmDelete"
            >
              Slett
            </button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

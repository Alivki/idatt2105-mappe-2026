<script setup lang="ts">
import {ref, reactive, computed} from 'vue'
import {CalendarDate} from '@internationalized/date'
import {Eye, ArrowUpDown} from 'lucide-vue-next'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Checkbox from '@/components/ui/checkbox/Checkbox.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogScrollContent from '@/components/ui/dialog/DialogScrollContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import {toast} from 'vue-sonner'
import type {GenerateReportRequest, ReportSectionsConfig} from '@/types/report'
import type {Checklist} from '@/types/checklist'

defineProps<{
  open: boolean
  checklists: Checklist[]
  isPreviewing: boolean
}>()

const emits = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'preview', request: GenerateReportRequest): void
}>()

const periodFrom = ref<InstanceType<typeof CalendarDate> | undefined>()
const periodTo = ref<InstanceType<typeof CalendarDate> | undefined>()
const reportTitle = ref('')
const signOffName = ref('')
const signOffTitle = ref('')
const signOffComments = ref('')
const showChecklistPicker = ref(false)
const selectedChecklistIds = ref<Set<number>>(new Set())

const sections = reactive<ReportSectionsConfig>({
  includeComplianceSummary: true,
  includeTemperatureLogs: false,
  includeChecklists: false,
  selectedChecklistIds: undefined,
  includeHaccpChecklists: false,
  includeCorrectiveActions: false,
  includeFoodDeviations: false,
  includeAlcoholDeviations: false,
  includeAgeVerification: false,
  includeTrainingOverview: false,
  includeLicenseInfo: false,
  includeSignOff: false,
})

const sectionGroups = [
  {
    label: 'Generelt',
    options: [{
      key: 'includeComplianceSummary' as keyof ReportSectionsConfig,
      label: 'Samsvarssammendrag'
    }]
  },
  {
    label: 'IK-Mat', options: [
      {key: 'includeTemperatureLogs' as keyof ReportSectionsConfig, label: 'Temperaturlogg'},
      {key: 'includeChecklists' as keyof ReportSectionsConfig, label: 'Sjekklister'},
      {key: 'includeHaccpChecklists' as keyof ReportSectionsConfig, label: 'HACCP-sjekklister'},
      {key: 'includeCorrectiveActions' as keyof ReportSectionsConfig, label: 'Korrigerende tiltak'},
      {key: 'includeFoodDeviations' as keyof ReportSectionsConfig, label: 'Matavvik'},
    ]
  },
  {
    label: 'IK-Alkohol', options: [
      {key: 'includeAlcoholDeviations' as keyof ReportSectionsConfig, label: 'Alkoholavvik'},
      {key: 'includeAgeVerification' as keyof ReportSectionsConfig, label: 'Alderskontroll'},
      {key: 'includeLicenseInfo' as keyof ReportSectionsConfig, label: 'Lisensinformasjon'},
    ]
  },
  {
    label: 'Felles', options: [
      {key: 'includeTrainingOverview' as keyof ReportSectionsConfig, label: 'Opplæringsoversikt'},
      {key: 'includeSignOff' as keyof ReportSectionsConfig, label: 'Signering'},
    ]
  },
]

function toggleChecklistId(id: number) {
  const s = new Set(selectedChecklistIds.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  selectedChecklistIds.value = s
}

function handleOpen(v: boolean) {
  if (v) {
    periodFrom.value = undefined
    periodTo.value = undefined
    reportTitle.value = ''
    signOffName.value = ''
    signOffTitle.value = ''
    signOffComments.value = ''
    selectedChecklistIds.value = new Set()
    showChecklistPicker.value = false
    Object.assign(sections, {
      includeComplianceSummary: true,
      includeTemperatureLogs: false,
      includeChecklists: false,
      selectedChecklistIds: undefined,
      includeHaccpChecklists: false,
      includeCorrectiveActions: false,
      includeFoodDeviations: false,
      includeAlcoholDeviations: false,
      includeAgeVerification: false,
      includeTrainingOverview: false,
      includeLicenseInfo: false,
      includeSignOff: false,
    })
  }
  emits('update:open', v)
}

function handlePreview() {
  if (!periodFrom.value || !periodTo.value) {
    toast.error('Velg både fra- og til-dato')
    return
  }
  const from = `${periodFrom.value.year}-${String(periodFrom.value.month).padStart(2, '0')}-${String(periodFrom.value.day).padStart(2, '0')}`
  const to = `${periodTo.value.year}-${String(periodTo.value.month).padStart(2, '0')}-${String(periodTo.value.day).padStart(2, '0')}`

  emits('preview', {
    periodFrom: from,
    periodTo: to,
    title: reportTitle.value.trim() || undefined,
    sections: {
      ...sections,
      selectedChecklistIds: sections.includeChecklists && selectedChecklistIds.value.size > 0
        ? [...selectedChecklistIds.value]
        : undefined,
    },
    signOffName: sections.includeSignOff ? signOffName.value || undefined : undefined,
    signOffTitle: sections.includeSignOff ? signOffTitle.value || undefined : undefined,
    signOffComments: sections.includeSignOff ? signOffComments.value || undefined : undefined,
  })
}
</script>

<template>
  <Dialog :open="open" @update:open="handleOpen">
    <DialogScrollContent class="create-dialog">
      <DialogHeader>
        <DialogTitle>Ny rapport</DialogTitle>
        <DialogDescription>Velg periode og innhold for rapporten.</DialogDescription>
      </DialogHeader>

      <div class="create-form">
        <div class="form-field">
          <label class="form-label">Tittel (valgfritt)</label>
          <Input v-model="reportTitle" placeholder="F.eks. Internkontroll Mars 2026"/>
        </div>

        <div class="form-dates">
          <div class="form-field form-date-field">
            <label class="form-label">Fra dato</label>
            <DatePicker v-model="periodFrom" placeholder="Velg fra-dato"/>
          </div>
          <div class="form-field form-date-field">
            <label class="form-label">Til dato</label>
            <DatePicker v-model="periodTo" placeholder="Velg til-dato"/>
          </div>
        </div>

        <div class="form-field">
          <label class="form-label">Seksjoner</label>
          <div class="section-groups">
            <div v-for="group in sectionGroups" :key="group.label" class="section-group">
              <span class="section-group-label">{{ group.label }}</span>
              <div class="section-group-items">
                <label v-for="opt in group.options" :key="opt.key" class="section-option">
                  <Checkbox
                    :checked="!!sections[opt.key]"
                    @update:checked="(v: boolean) => { (sections as any)[opt.key] = v }"
                  />
                  <span>{{ opt.label }}</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <div v-if="sections.includeChecklists && checklists.length > 0" class="form-field">
          <label class="form-label">Velg sjekklister</label>
          <button class="checklist-picker-toggle"
                  @click="showChecklistPicker = !showChecklistPicker">
            {{
              selectedChecklistIds.size === 0 ? 'Alle sjekklister' : `${selectedChecklistIds.size} sjekkliste(r) valgt`
            }}
            <ArrowUpDown :size="14"/>
          </button>
          <div v-if="showChecklistPicker" class="checklist-picker">
            <label v-for="cl in checklists" :key="cl.id" class="checklist-option">
              <Checkbox :checked="selectedChecklistIds.has(cl.id)"
                        @update:checked="() => toggleChecklistId(cl.id)"/>
              <span>{{ cl.name }}</span>
            </label>
          </div>
        </div>

        <div v-if="sections.includeSignOff" class="signoff-fields">
          <div class="form-field">
            <label class="form-label">Signert av (navn)</label>
            <Input v-model="signOffName" placeholder="Fullt navn"/>
          </div>
          <div class="form-field">
            <label class="form-label">Tittel/rolle</label>
            <Input v-model="signOffTitle" placeholder="F.eks. Daglig leder"/>
          </div>
          <div class="form-field">
            <label class="form-label">Kommentarer</label>
            <textarea v-model="signOffComments" class="form-textarea"
                      placeholder="Eventuelle kommentarer..." rows="2"/>
          </div>
        </div>
      </div>

      <DialogFooter>
        <Button variant="outline" @click="emits('update:open', false)">Avbryt</Button>
        <Button @click="handlePreview" :disabled="isPreviewing">
          <Eye :size="16"/>
          {{ isPreviewing ? 'Genererer...' : 'Forhåndsvis' }}
        </Button>
      </DialogFooter>
    </DialogScrollContent>
  </Dialog>
</template>

<style scoped>
.create-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.form-dates {
  display: flex;
  gap: 1rem;
}

.form-date-field {
  flex: 1;
  position: relative;
}

.form-date-field:first-child {
  z-index: 21;
}

.form-date-field:last-child {
  z-index: 20;
}

.form-textarea {
  width: 100%;
  border: 1px solid hsl(var(--input));
  border-radius: 0.5rem;
  padding: 0.5rem 0.75rem;
  font: inherit;
  font-size: 0.875rem;
  background: hsl(var(--card));
  resize: vertical;
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  transition: all 150ms ease;
  outline: none;
}

.form-textarea:focus {
  box-shadow: 0 0 0 2px hsl(var(--ring) / 0.2);
  border-color: hsl(var(--primary) / 0.5);
}

.section-groups {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.section-group {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.section-group-label {
  font-size: 0.72rem;
  font-weight: 700;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding-bottom: 0.15rem;
  border-bottom: 1px solid hsl(var(--border) / 0.5);
}

.section-group-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.75rem;
}

.section-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 0.3rem 0.4rem;
  border-radius: 0.375rem;
  transition: background 150ms;
}

.section-option:hover {
  background: hsl(var(--accent));
}

.checklist-picker-toggle {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: 1px solid hsl(var(--input));
  border-radius: 0.5rem;
  padding: 0.5rem 0.75rem;
  font: inherit;
  font-size: 0.875rem;
  background: hsl(var(--card));
  cursor: pointer;
  color: hsl(var(--muted-foreground));
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05);
  transition: all 150ms;
}

.checklist-picker-toggle:hover {
  border-color: hsl(var(--primary) / 0.5);
}

.checklist-picker {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.5rem;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  max-height: 12rem;
  overflow-y: auto;
  margin-top: 0.25rem;
}

.checklist-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.84rem;
  padding: 0.25rem 0.35rem;
  border-radius: 0.25rem;
}

.checklist-option:hover {
  background: hsl(var(--accent));
}

.signoff-fields {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: hsl(var(--muted) / 0.3);
}

@media (max-width: 768px) {
  .form-dates {
    flex-direction: column;
  }
}
</style>

<style>
.dialog-scroll-content.create-dialog {
  --dialog-max-width: 36rem;
}
</style>

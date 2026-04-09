<script setup lang="ts">
import {computed, ref, reactive} from 'vue'
import {useMediaQuery} from '@vueuse/core'
import axios from 'axios'
import {
  MoreVertical,
  ArrowUpDown,
  Search,
  FileText,
  Download,
  Trash2,
  AlertTriangle,
  Eye,
  Save,
  Plus,
} from 'lucide-vue-next'
import {toast} from 'vue-sonner'
import {CalendarDate} from '@internationalized/date'
import AppLayout from '@/components/layout/AppLayout.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import ReportPreview from '@/components/reports/ReportPreview.vue'
import Button from '@/components/ui/button/Button.vue'
import Input from '@/components/ui/input/Input.vue'
import Checkbox from '@/components/ui/checkbox/Checkbox.vue'
import DatePicker from '@/components/ui/date-picker/DatePicker.vue'
import {
  Table,
  TableBody,
  TableCell,
  TableEmpty,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
} from '@/components/ui/dropdown-menu'
import Dialog from '@/components/ui/dialog/Dialog.vue'
import DialogScrollContent from '@/components/ui/dialog/DialogScrollContent.vue'
import DialogHeader from '@/components/ui/dialog/DialogHeader.vue'
import DialogTitle from '@/components/ui/dialog/DialogTitle.vue'
import DialogDescription from '@/components/ui/dialog/DialogDescription.vue'
import DialogFooter from '@/components/ui/dialog/DialogFooter.vue'
import AlertDialog from '@/components/ui/alert-dialog/AlertDialog.vue'
import AlertDialogAction from '@/components/ui/alert-dialog/AlertDialogAction.vue'
import AlertDialogCancel from '@/components/ui/alert-dialog/AlertDialogCancel.vue'
import AlertDialogContent from '@/components/ui/alert-dialog/AlertDialogContent.vue'
import AlertDialogDescription from '@/components/ui/alert-dialog/AlertDialogDescription.vue'
import AlertDialogFooter from '@/components/ui/alert-dialog/AlertDialogFooter.vue'
import AlertDialogHeader from '@/components/ui/alert-dialog/AlertDialogHeader.vue'
import AlertDialogTitle from '@/components/ui/alert-dialog/AlertDialogTitle.vue'
import {
  useReportsQuery,
  usePreviewReportMutation,
  useGenerateReportMutation,
  useDeleteReportMutation,
  useDownloadReport,
} from '@/composables/useReports'
import {useChecklistsQuery} from '@/composables/useChecklists'
import type {
  GenerateReportRequest,
  ReportPreviewResponse,
  GeneratedReportResponse,
  ReportSectionsConfig,
} from '@/types/report'

const reportsQuery = useReportsQuery()
const previewMutation = usePreviewReportMutation()
const generateMutation = useGenerateReportMutation()
const deleteMutation = useDeleteReportMutation()
const downloadMutation = useDownloadReport()
const checklistsQuery = useChecklistsQuery()
const isMobile = useMediaQuery('(max-width: 768px)')

const reports = computed(() => reportsQuery.data.value ?? [])
const checklists = computed(() => (checklistsQuery.data.value ?? []).filter((c) => c.source !== 'HACCP_WIZARD'))

const search = ref('')

type SortField = 'title' | 'period' | 'generatedBy' | 'generatedAt'
type SortDir = 'asc' | 'desc'
const sortField = ref<SortField>('generatedAt')
const sortDir = ref<SortDir>('desc')

function toggleSort(field: SortField) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = 'asc'
  }
}

function formatPeriod(from: string, to: string): string {
  return `${from} – ${to}`
}

function toIsoDate(ddmmyyyy: string): string {
  const parts = ddmmyyyy.split('.')
  if (parts.length === 3) return `${parts[2]}-${parts[1]}-${parts[0]}`
  return ddmmyyyy
}

const filteredAndSorted = computed(() => {
  const q = search.value.toLowerCase().trim()
  let list = reports.value.slice()
  if (q) {
    list = list.filter((r) =>
      [r.title, r.generatedByUserName, formatPeriod(r.periodFrom, r.periodTo)].some((v) =>
        v.toLowerCase().includes(q),
      ),
    )
  }
  list.sort((a, b) => {
    let cmp = 0
    if (sortField.value === 'title') cmp = a.title.localeCompare(b.title, 'nb')
    else if (sortField.value === 'period') cmp = a.periodFrom.localeCompare(b.periodFrom)
    else if (sortField.value === 'generatedBy') cmp = a.generatedByUserName.localeCompare(b.generatedByUserName, 'nb')
    else if (sortField.value === 'generatedAt') cmp = a.generatedAt.localeCompare(b.generatedAt)
    return sortDir.value === 'desc' ? -cmp : cmp
  })
  return list
})

const createDialogOpen = ref(false)
const periodFrom = ref<InstanceType<typeof CalendarDate> | undefined>()
const periodTo = ref<InstanceType<typeof CalendarDate> | undefined>()

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

const signOffName = ref('')
const signOffTitle = ref('')
const signOffComments = ref('')
const reportTitle = ref('')

const showChecklistPicker = ref(false)
const selectedChecklistIds = ref<Set<number>>(new Set())

function toggleChecklistId(id: number) {
  const s = new Set(selectedChecklistIds.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  selectedChecklistIds.value = s
}

function openCreateDialog() {
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
  createDialogOpen.value = true
}

function buildRequest(): GenerateReportRequest | null {
  if (!periodFrom.value || !periodTo.value) {
    toast.error('Velg både fra- og til-dato')
    return null
  }
  const from = `${periodFrom.value.year}-${String(periodFrom.value.month).padStart(2, '0')}-${String(periodFrom.value.day).padStart(2, '0')}`
  const to = `${periodTo.value.year}-${String(periodTo.value.month).padStart(2, '0')}-${String(periodTo.value.day).padStart(2, '0')}`

  return {
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
  }
}

const previewOpen = ref(false)
const previewData = ref<ReportPreviewResponse | null>(null)
const lastRequest = ref<GenerateReportRequest | null>(null)

async function handlePreview() {
  const req = buildRequest()
  if (!req) return
  lastRequest.value = req
  try {
    previewData.value = await previewMutation.mutateAsync(req)
    createDialogOpen.value = false
    previewOpen.value = true
  } catch (error) {
    handleError(error, 'Kunne ikke generere forhåndsvisning')
  }
}

async function handleSave() {
  const req = lastRequest.value
  if (!req) return
  try {
    await generateMutation.mutateAsync(req)
    toast.success('Rapporten er lagret')
    previewOpen.value = false
  } catch (error) {
    handleError(error, 'Kunne ikke lagre rapporten')
  }
}

async function handleDownloadFromPreview() {
  const req = lastRequest.value
  if (!req) return
  try {
    const report = await generateMutation.mutateAsync(req)
    if (report.downloadUrl) {
      window.open(report.downloadUrl, '_blank')
    }
    toast.success('Rapporten er lastet ned og lagret automatisk')
    previewOpen.value = false
  } catch (error) {
    handleError(error, 'Kunne ikke laste ned rapporten')
  }
}

async function handleDownloadExisting(report: GeneratedReportResponse) {
  try {
    const url = await downloadMutation.mutateAsync(report.id)
    window.open(url, '_blank')
    toast.success('Nedlasting startet')
  } catch (error) {
    handleError(error, 'Kunne ikke laste ned rapporten')
  }
}

async function handlePreviewExisting(report: GeneratedReportResponse) {
  const req: GenerateReportRequest = {
    periodFrom: toIsoDate(report.periodFrom),
    periodTo: toIsoDate(report.periodTo),
    title: report.title,
    sections: {
      includeComplianceSummary: true,
      includeTemperatureLogs: true,
      includeChecklists: true,
      includeHaccpChecklists: true,
      includeCorrectiveActions: true,
      includeFoodDeviations: true,
      includeAlcoholDeviations: true,
      includeAgeVerification: true,
      includeTrainingOverview: true,
      includeLicenseInfo: true,
      includeSignOff: true,
    },
  }
  lastRequest.value = req
  try {
    previewData.value = await previewMutation.mutateAsync(req)
    previewOpen.value = true
  } catch (error) {
    handleError(error, 'Kunne ikke hente forhåndsvisning')
  }
}

const deleteDialogOpen = ref(false)
const deletingReport = ref<GeneratedReportResponse | null>(null)

function openDelete(report: GeneratedReportResponse) {
  deletingReport.value = report
  deleteDialogOpen.value = true
}

async function confirmDelete() {
  if (!deletingReport.value) return
  try {
    await deleteMutation.mutateAsync(deletingReport.value.id)
    toast.success('Rapporten er slettet')
  } catch (error) {
    handleError(error, 'Kunne ikke slette rapporten')
  } finally {
    deleteDialogOpen.value = false
    deletingReport.value = null
  }
}

function handleError(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.error?.message
    if (typeof message === 'string' && message.trim().length > 0) {
      toast.error(message)
      return
    }
  }
  toast.error(fallback)
}

const sectionGroups = [
  {
    label: 'Generelt',
    options: [
      {key: 'includeComplianceSummary' as keyof ReportSectionsConfig, label: 'Samsvarssammendrag'},
    ],
  },
  {
    label: 'IK-Mat',
    options: [
      {key: 'includeTemperatureLogs' as keyof ReportSectionsConfig, label: 'Temperaturlogg'},
      {key: 'includeChecklists' as keyof ReportSectionsConfig, label: 'Sjekklister'},
      {key: 'includeHaccpChecklists' as keyof ReportSectionsConfig, label: 'HACCP-sjekklister'},
      {key: 'includeCorrectiveActions' as keyof ReportSectionsConfig, label: 'Korrigerende tiltak'},
      {key: 'includeFoodDeviations' as keyof ReportSectionsConfig, label: 'Matavvik'},
    ],
  },
  {
    label: 'IK-Alkohol',
    options: [
      {key: 'includeAlcoholDeviations' as keyof ReportSectionsConfig, label: 'Alkoholavvik'},
      {key: 'includeAgeVerification' as keyof ReportSectionsConfig, label: 'Alderskontroll'},
      {key: 'includeLicenseInfo' as keyof ReportSectionsConfig, label: 'Lisensinformasjon'},
    ],
  },
  {
    label: 'Felles',
    options: [
      {key: 'includeTrainingOverview' as keyof ReportSectionsConfig, label: 'Opplæringsoversikt'},
      {key: 'includeSignOff' as keyof ReportSectionsConfig, label: 'Signering'},
    ],
  },
]

</script>

<template>
  <AppLayout>
    <PageHeader title="Rapporter" />

    <div class="page-content">
      <section class="header-row">
        <div>
          <h1>Rapporter</h1>
          <p>Generer og administrer internkontrollrapporter</p>
        </div>
        <Button @click="openCreateDialog">
          <Plus :size="16"/>
          Ny rapport
        </Button>
      </section>

      <section class="table-section">
        <div class="search-row">
          <div class="search-wrapper">
            <Search :size="16" class="search-icon"/>
            <Input v-model="search" class="search-input" placeholder="Søk etter rapport..." />
          </div>
        </div>

        <div v-if="reportsQuery.isLoading.value" class="loading-state">
          <div class="skeleton-item" v-for="n in 4" :key="n"></div>
        </div>

        <EmptyState
          v-else-if="reportsQuery.isError.value"
          :icon="AlertTriangle"
          variant="danger"
          title="Kunne ikke hente rapporter"
          description="Noe gikk galt under lasting. Prøv igjen senere."
        />

        <div v-else-if="isMobile" class="mobile-report-list">
          <article v-for="report in filteredAndSorted" :key="report.id" class="mobile-report-card">
            <div class="mobile-report-top">
              <strong class="mobile-report-title">{{ report.title }}</strong>
              <DropdownMenu>
                <DropdownMenuTrigger as-child>
                  <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger">
                    <MoreVertical :size="18"/>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" :side-offset="4">
                  <DropdownMenuLabel>Handlinger</DropdownMenuLabel>
                  <DropdownMenuSeparator/>
                  <DropdownMenuItem @click="handlePreviewExisting(report)">
                    <Eye :size="16"/>
                    Forhåndsvis
                  </DropdownMenuItem>
                  <DropdownMenuItem v-if="report.documentId"
                                    @click="handleDownloadExisting(report)">
                    <Download :size="16"/>
                    Last ned
                  </DropdownMenuItem>
                  <DropdownMenuSeparator/>
                  <DropdownMenuItem class="menu-item--danger" @click="openDelete(report)">
                    <Trash2 :size="16"/>
                    Slett
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            <div class="mobile-report-row"><span>Periode</span><span>{{
                formatPeriod(report.periodFrom, report.periodTo)
              }}</span></div>
            <div class="mobile-report-row">
              <span>Generert av</span><span>{{ report.generatedByUserName }}</span></div>
            <div class="mobile-report-row"><span>Dato</span><span>{{ report.generatedAt }}</span>
            </div>
          </article>
          <EmptyState
            v-if="filteredAndSorted.length === 0"
            :icon="FileText"
            title="Ingen rapporter ennå"
            description="Opprett din første rapport ved å klikke &quot;Ny rapport&quot; ovenfor."
          />
        </div>

        <div v-else class="table-card">
          <Table class="reports-table">
            <TableHeader>
              <TableRow>
                <TableHead class="th-title">
                  <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('title')">
                    Tittel
                    <ArrowUpDown :size="14" class="sort-icon"
                                 :class="{ 'sort-icon--active': sortField === 'title' }"/>
                  </Button>
                </TableHead>
                <TableHead class="th-period">
                  <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('period')">
                    Periode
                    <ArrowUpDown :size="14" class="sort-icon"
                                 :class="{ 'sort-icon--active': sortField === 'period' }"/>
                  </Button>
                </TableHead>
                <TableHead class="th-user hide-mobile">
                  <Button variant="ghost" size="sm" class="sort-btn"
                          @click="toggleSort('generatedBy')">Generert av
                    <ArrowUpDown :size="14" class="sort-icon"
                                 :class="{ 'sort-icon--active': sortField === 'generatedBy' }"/>
                  </Button>
                </TableHead>
                <TableHead class="th-date">
                  <Button variant="ghost" size="sm" class="sort-btn"
                          @click="toggleSort('generatedAt')">Dato
                    <ArrowUpDown :size="14" class="sort-icon"
                                 :class="{ 'sort-icon--active': sortField === 'generatedAt' }"/>
                  </Button>
                </TableHead>
                <TableHead class="th-actions"/>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="report in filteredAndSorted" :key="report.id">
                <TableCell data-label="Tittel">
                  <div class="cell-title">
                    <FileText :size="16" class="cell-title-icon"/>
                    <span class="cell-title-text">{{ report.title }}</span></div>
                </TableCell>
                <TableCell class="cell-text" data-label="Periode">
                  {{ formatPeriod(report.periodFrom, report.periodTo) }}
                </TableCell>
                <TableCell class="cell-text hide-mobile" data-label="Generert av">
                  {{ report.generatedByUserName }}
                </TableCell>
                <TableCell class="cell-text" data-label="Dato">{{ report.generatedAt }}</TableCell>
                <TableCell class="cell-actions" data-label="Handlinger">
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger">
                        <MoreVertical :size="18"/>
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" :side-offset="4">
                      <DropdownMenuLabel>Handlinger</DropdownMenuLabel>
                      <DropdownMenuSeparator/>
                      <DropdownMenuItem @click="handlePreviewExisting(report)">
                        <Eye :size="16"/>
                        Forhåndsvis
                      </DropdownMenuItem>
                      <DropdownMenuItem v-if="report.documentId"
                                        @click="handleDownloadExisting(report)">
                        <Download :size="16"/>
                        Last ned
                      </DropdownMenuItem>
                      <DropdownMenuSeparator/>
                      <DropdownMenuItem class="menu-item--danger" @click="openDelete(report)">
                        <Trash2 :size="16"/>
                        Slett
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
              <TableEmpty v-if="filteredAndSorted.length === 0" :colspan="5">Ingen rapporter ennå.
                Klikk "Ny rapport" for å opprette din første.
              </TableEmpty>
            </TableBody>
          </Table>
        </div>
      </section>

      <Dialog :open="createDialogOpen" @update:open="(v: boolean) => { createDialogOpen = v }">
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
            <Button variant="outline" @click="createDialogOpen = false">Avbryt</Button>
            <Button @click="handlePreview" :disabled="previewMutation.isPending.value">
              <Eye :size="16"/>
              {{ previewMutation.isPending.value ? 'Genererer...' : 'Forhåndsvis' }}
            </Button>
          </DialogFooter>
        </DialogScrollContent>
      </Dialog>

      <Dialog :open="previewOpen" @update:open="(v: boolean) => { previewOpen = v }">
        <DialogScrollContent class="preview-dialog">
          <DialogHeader>
            <DialogTitle>Forhåndsvisning av rapport</DialogTitle>
            <DialogDescription>Kontroller innholdet før du lagrer eller laster ned.</DialogDescription>
          </DialogHeader>

          <ReportPreview v-if="previewData" :data="previewData" />

          <DialogFooter>
            <Button variant="outline" @click="previewOpen = false">Lukk</Button>
            <Button variant="outline" @click="handleSave"
                    :disabled="generateMutation.isPending.value">
              <Save :size="16"/>
              Lagre
            </Button>
            <Button @click="handleDownloadFromPreview" :disabled="generateMutation.isPending.value">
              <Download :size="16"/>
              Last ned
            </Button>
          </DialogFooter>
        </DialogScrollContent>
      </Dialog>

      <AlertDialog :open="deleteDialogOpen" @update:open="(v: boolean) => { deleteDialogOpen = v }">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Slett rapport?</AlertDialogTitle>
            <AlertDialogDescription>Er du sikker på at du vil slette rapporten
              "{{ deletingReport?.title }}"? Dette kan ikke angres.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Avbryt</AlertDialogCancel>
            <AlertDialogAction variant="destructive" @click="confirmDelete">Slett
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  </AppLayout>
</template>

<style scoped>
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
  color: hsl(var(--muted-foreground, 24 5% 46%));
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

.form-date-field:first-child { z-index: 21; }
.form-date-field:last-child { z-index: 20; }

.form-textarea {
  width: 100%;
  border: 1px solid hsl(var(--input, 35 15% 85%));
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
  box-shadow: 0 0 0 2px hsl(var(--ring, 245 43% 52%) / 0.2);
  border-color: hsl(var(--primary, 245 43% 52%) / 0.5);
}

.section-groups { display: flex; flex-direction: column; gap: 0.75rem; }
.section-group { display: flex; flex-direction: column; gap: 0.35rem; }

.section-group-label {
  font-size: 0.72rem;
  font-weight: 700;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding-bottom: 0.15rem;
  border-bottom: 1px solid hsl(var(--border) / 0.5);
}

.section-group-items { display: flex; flex-wrap: wrap; gap: 0.25rem 0.75rem; }

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

.section-option:hover { background: hsl(var(--accent, 250 40% 95%)); }

.checklist-picker-toggle {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  border: 1px solid hsl(var(--input, 35 15% 85%));
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

.checklist-picker-toggle:hover { border-color: hsl(var(--primary, 245 43% 52%) / 0.5); }

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

.checklist-option:hover { background: hsl(var(--accent, 250 40% 95%)); }

.signoff-fields {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.75rem;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  background: hsl(var(--muted, 40 18% 93%) / 0.3);
}

.table-section { display: flex; flex-direction: column; gap: 0.75rem; }

.search-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.search-wrapper { position: relative; width: 20rem; }

.search-icon {
  position: absolute;
  left: 0.65rem;
  top: 50%;
  transform: translateY(-50%);
  color: hsl(var(--muted-foreground));
  pointer-events: none;
  z-index: 1;
}

.search-input { padding-left: 2.1rem; }

.table-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.75rem;
  background: hsl(var(--card));
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
  border-radius: var(--radius-md, 0.375rem);
  margin: -0.25rem -0.4rem;
  transition: background 150ms, color 150ms;
}

.sort-btn:hover {
  background: hsl(var(--accent, 250 40% 95%));
  color: hsl(var(--foreground));
}

.sort-icon { opacity: 0.4; transition: opacity 150ms; }
.sort-icon--active { opacity: 1; }

.cell-title { display: flex; align-items: center; gap: 0.5rem; }
.cell-title-icon { flex-shrink: 0; color: hsl(var(--primary, 245 43% 52%)); }
.cell-title-text { font-weight: 500; }
.cell-text { color: hsl(var(--muted-foreground)); }
.cell-actions { width: 3rem; text-align: right; }

.actions-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: var(--radius-md, 0.375rem);
  border: none;
  background: none;
  color: hsl(var(--muted-foreground));
  cursor: pointer;
  transition: background 150ms, color 150ms;
}

.actions-trigger:hover {
  background: hsl(var(--accent, 250 40% 95%));
  color: hsl(var(--foreground));
}

.menu-item--danger { color: var(--red); }

.th-title { min-width: 10rem; }
.th-period { min-width: 10rem; }
.th-user { min-width: 8rem; }
.th-date { min-width: 6rem; }
.th-actions { width: 3rem; }

.mobile-report-list { display: flex; flex-direction: column; gap: 0.65rem; }

.mobile-report-card {
  border: 1px solid hsl(var(--border));
  border-radius: 0.65rem;
  background: hsl(var(--card));
  padding: 0.45rem 0.65rem;
}

.mobile-report-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.mobile-report-title { font-size: 0.9rem; }

.mobile-report-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.32rem 0;
}

.mobile-report-row > span:first-child {
  font-size: 0.73rem;
  font-weight: 600;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.mobile-report-row > :last-child { text-align: right; }

.loading-state { display: flex; flex-direction: column; gap: 10px; }

.skeleton-item {
  height: 56px;
  border-radius: var(--radius-xl);
  background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite ease-in-out;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 768px) {
  .hide-mobile { display: none !important; }
  .header-row { flex-direction: column; }
  .search-row { flex-direction: column; align-items: stretch; }
  .search-wrapper { width: 100%; }
  .form-dates { flex-direction: column; }
}

@media (max-width: 760px) {
  .page-content { padding: 0 0.75rem 0.75rem; gap: 0.75rem; }
}
</style>

<style>
.dialog-scroll-content.preview-dialog {
  --dialog-max-width: 64rem;
  width: 95vw;
}

.dialog-scroll-content.create-dialog {
  --dialog-max-width: 36rem;
}
</style>

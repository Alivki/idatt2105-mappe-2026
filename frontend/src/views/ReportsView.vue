<script setup lang="ts">
import { computed, ref, reactive } from 'vue'
import { useMediaQuery } from '@vueuse/core'
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
import { toast } from 'vue-sonner'
import { CalendarDate } from '@internationalized/date'
import AppLayout from '@/components/layout/AppLayout.vue'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
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
import { useChecklistsQuery } from '@/composables/useChecklists'
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
      { key: 'includeComplianceSummary' as keyof ReportSectionsConfig, label: 'Samsvarssammendrag' },
    ],
  },
  {
    label: 'IK-Mat',
    options: [
      { key: 'includeTemperatureLogs' as keyof ReportSectionsConfig, label: 'Temperaturlogg' },
      { key: 'includeChecklists' as keyof ReportSectionsConfig, label: 'Sjekklister' },
      { key: 'includeHaccpChecklists' as keyof ReportSectionsConfig, label: 'HACCP-sjekklister' },
      { key: 'includeCorrectiveActions' as keyof ReportSectionsConfig, label: 'Korrigerende tiltak' },
      { key: 'includeFoodDeviations' as keyof ReportSectionsConfig, label: 'Matavvik' },
    ],
  },
  {
    label: 'IK-Alkohol',
    options: [
      { key: 'includeAlcoholDeviations' as keyof ReportSectionsConfig, label: 'Alkoholavvik' },
      { key: 'includeAgeVerification' as keyof ReportSectionsConfig, label: 'Alderskontroll' },
      { key: 'includeLicenseInfo' as keyof ReportSectionsConfig, label: 'Lisensinformasjon' },
    ],
  },
  {
    label: 'Felles',
    options: [
      { key: 'includeTrainingOverview' as keyof ReportSectionsConfig, label: 'Opplæringsoversikt' },
      { key: 'includeSignOff' as keyof ReportSectionsConfig, label: 'Signering' },
    ],
  },
]

function getStatusClass(status: string): string {
  const s = status.toLowerCase()
  if (s === 'normal' || s === 'gyldig' || s === 'lukket' || s === 'resolved' || s === 'yes') return 'badge-green'
  if (s === 'advarsel' || s === 'utløper snart' || s === 'under behandling' || s === 'warning' || s === 'expiring soon' || s === 'åpen' || s === 'open') return 'badge-yellow'
  if (s === 'kritisk' || s === 'utløpt' || s === 'ikke fullført' || s === 'alert' || s === 'expired') return 'badge-red'
  return 'badge-gray'
}

function getRefusalClass(val: number): string {
  return val > 0 ? 'refusal-highlight' : ''
}
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger />
        <Separator orientation="vertical" class="header-separator" />
        <span class="page-title">Rapporter</span>
      </div>
    </header>

    <div class="page-content">
      <section class="header-row">
        <div>
          <h1>Rapporter</h1>
          <p>Generer og administrer internkontrollrapporter</p>
        </div>
        <Button @click="openCreateDialog">
          <Plus :size="16" />
          Ny rapport
        </Button>
      </section>

      <section class="table-section">
        <div class="search-row">
          <div class="search-wrapper">
            <Search :size="16" class="search-icon" />
            <input v-model="search" class="search-input" placeholder="Søk etter rapport..." aria-label="Søk etter rapport" />
          </div>
        </div>

        <div v-if="reportsQuery.isLoading.value" class="loading-state">
          <div class="skeleton-item" v-for="n in 4" :key="n"></div>
        </div>

        <div v-else-if="reportsQuery.isError.value" class="empty-state">
          <div class="empty-state-inner">
            <div class="empty-state-icon empty-state-icon--danger"><AlertTriangle :stroke-width="1.5" /></div>
            <div class="empty-state-text">
              <h3>Kunne ikke hente rapporter</h3>
              <p>Noe gikk galt under lasting. Prøv igjen senere.</p>
            </div>
          </div>
        </div>

        <div v-else-if="isMobile" class="mobile-report-list">
          <article v-for="report in filteredAndSorted" :key="report.id" class="mobile-report-card">
            <div class="mobile-report-top">
              <strong class="mobile-report-title">{{ report.title }}</strong>
              <DropdownMenu>
                <DropdownMenuTrigger as-child>
                  <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger"><MoreVertical :size="18" /></Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" :side-offset="4">
                  <DropdownMenuLabel>Handlinger</DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem @click="handlePreviewExisting(report)"><Eye :size="16" /> Forhåndsvis</DropdownMenuItem>
                  <DropdownMenuItem v-if="report.documentId" @click="handleDownloadExisting(report)"><Download :size="16" /> Last ned</DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem class="menu-item--danger" @click="openDelete(report)"><Trash2 :size="16" /> Slett</DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
            <div class="mobile-report-row"><span>Periode</span><span>{{ formatPeriod(report.periodFrom, report.periodTo) }}</span></div>
            <div class="mobile-report-row"><span>Generert av</span><span>{{ report.generatedByUserName }}</span></div>
            <div class="mobile-report-row"><span>Dato</span><span>{{ report.generatedAt }}</span></div>
          </article>
          <div v-if="filteredAndSorted.length === 0" class="empty-state">
            <div class="empty-state-inner">
              <div class="empty-state-icon"><FileText :stroke-width="1.5" /></div>
              <div class="empty-state-text">
                <h3>Ingen rapporter ennå</h3>
                <p>Opprett din første rapport ved å klikke "Ny rapport" ovenfor.</p>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="table-card">
          <Table class="reports-table">
            <TableHeader>
              <TableRow>
                <TableHead class="th-title">
                  <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('title')">Tittel <ArrowUpDown :size="14" class="sort-icon" :class="{ 'sort-icon--active': sortField === 'title' }" /></Button>
                </TableHead>
                <TableHead class="th-period">
                  <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('period')">Periode <ArrowUpDown :size="14" class="sort-icon" :class="{ 'sort-icon--active': sortField === 'period' }" /></Button>
                </TableHead>
                <TableHead class="th-user hide-mobile">
                  <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('generatedBy')">Generert av <ArrowUpDown :size="14" class="sort-icon" :class="{ 'sort-icon--active': sortField === 'generatedBy' }" /></Button>
                </TableHead>
                <TableHead class="th-date">
                  <Button variant="ghost" size="sm" class="sort-btn" @click="toggleSort('generatedAt')">Dato <ArrowUpDown :size="14" class="sort-icon" :class="{ 'sort-icon--active': sortField === 'generatedAt' }" /></Button>
                </TableHead>
                <TableHead class="th-actions" />
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow v-for="report in filteredAndSorted" :key="report.id">
                <TableCell data-label="Tittel"><div class="cell-title"><FileText :size="16" class="cell-title-icon" /><span class="cell-title-text">{{ report.title }}</span></div></TableCell>
                <TableCell class="cell-text" data-label="Periode">{{ formatPeriod(report.periodFrom, report.periodTo) }}</TableCell>
                <TableCell class="cell-text hide-mobile" data-label="Generert av">{{ report.generatedByUserName }}</TableCell>
                <TableCell class="cell-text" data-label="Dato">{{ report.generatedAt }}</TableCell>
                <TableCell class="cell-actions" data-label="Handlinger">
                  <DropdownMenu>
                    <DropdownMenuTrigger as-child>
                      <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger"><MoreVertical :size="18" /></Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" :side-offset="4">
                      <DropdownMenuLabel>Handlinger</DropdownMenuLabel>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem @click="handlePreviewExisting(report)"><Eye :size="16" /> Forhåndsvis</DropdownMenuItem>
                      <DropdownMenuItem v-if="report.documentId" @click="handleDownloadExisting(report)"><Download :size="16" /> Last ned</DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem class="menu-item--danger" @click="openDelete(report)"><Trash2 :size="16" /> Slett</DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
              <TableEmpty v-if="filteredAndSorted.length === 0" :colspan="5">Ingen rapporter ennå. Klikk "Ny rapport" for å opprette din første.</TableEmpty>
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
              <Input v-model="reportTitle" placeholder="F.eks. Internkontroll Mars 2026" />
            </div>

            <div class="form-dates">
              <div class="form-field form-date-field">
                <label class="form-label">Fra dato</label>
                <DatePicker v-model="periodFrom" placeholder="Velg fra-dato" />
              </div>
              <div class="form-field form-date-field">
                <label class="form-label">Til dato</label>
                <DatePicker v-model="periodTo" placeholder="Velg til-dato" />
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
              <button class="checklist-picker-toggle" @click="showChecklistPicker = !showChecklistPicker">
                {{ selectedChecklistIds.size === 0 ? 'Alle sjekklister' : `${selectedChecklistIds.size} sjekkliste(r) valgt` }}
                <ArrowUpDown :size="14" />
              </button>
              <div v-if="showChecklistPicker" class="checklist-picker">
                <label v-for="cl in checklists" :key="cl.id" class="checklist-option">
                  <Checkbox :checked="selectedChecklistIds.has(cl.id)" @update:checked="() => toggleChecklistId(cl.id)" />
                  <span>{{ cl.name }}</span>
                </label>
              </div>
            </div>

            <div v-if="sections.includeSignOff" class="signoff-fields">
              <div class="form-field">
                <label class="form-label">Signert av (navn)</label>
                <Input v-model="signOffName" placeholder="Fullt navn" />
              </div>
              <div class="form-field">
                <label class="form-label">Tittel/rolle</label>
                <Input v-model="signOffTitle" placeholder="F.eks. Daglig leder" />
              </div>
              <div class="form-field">
                <label class="form-label">Kommentarer</label>
                <textarea v-model="signOffComments" class="form-textarea" placeholder="Eventuelle kommentarer..." rows="2" />
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" @click="createDialogOpen = false">Avbryt</Button>
            <Button @click="handlePreview" :disabled="previewMutation.isPending.value">
              <Eye :size="16" />
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

          <div v-if="previewData" class="pv-scroll-wrapper">
          <div class="pv">
            <div class="pv-header">
              <div class="pv-header-left">
                <span class="pv-header-label">INTERNKONTROLL RAPPORT</span>
                <h2 class="pv-header-org">{{ previewData.header.organizationName }}</h2>
                <span v-if="previewData.header.orgNumber" class="pv-header-orgnr">Org.nr: {{ previewData.header.orgNumber }}</span>
              </div>
              <div class="pv-header-right">
                <span>Periode: <strong>{{ previewData.header.periodFrom }} – {{ previewData.header.periodTo }}</strong></span>
                <span>Generert: {{ previewData.header.generatedDate }}</span>
                <span>Av: {{ previewData.header.generatedByName }}<template v-if="previewData.header.generatedByRole"> ({{ previewData.header.generatedByRole }})</template></span>
              </div>
            </div>

            <div v-if="previewData.complianceSummary" class="pv-section">
              <h3 class="pv-section-title">SAMSVARSSAMMENDRAG</h3>
              <div class="pv-kpi-row">
                <div class="pv-kpi-card pv-kpi-card--green">
                  <span class="pv-kpi-label">SAMSVARSRATE</span>
                  <span class="pv-kpi-value pv-kpi-value--green">{{ previewData.complianceSummary.complianceRate.toFixed(1) }}%</span>
                </div>
                <div class="pv-kpi-card pv-kpi-card--olive">
                  <span class="pv-kpi-label">FORFALTE OPPGAVER</span>
                  <span class="pv-kpi-value pv-kpi-value--olive">{{ previewData.complianceSummary.overdueTasks }}</span>
                </div>
                <div class="pv-kpi-card pv-kpi-card--gold">
                  <span class="pv-kpi-label">ÅPNE AVVIK</span>
                  <span class="pv-kpi-value pv-kpi-value--gold">{{ previewData.complianceSummary.openDeviations }}</span>
                </div>
                <div class="pv-kpi-card pv-kpi-card--red">
                  <span class="pv-kpi-label">TEMP.AVVIK</span>
                  <span class="pv-kpi-value pv-kpi-value--red">{{ previewData.complianceSummary.temperatureDeviations }}</span>
                </div>
              </div>
              <div class="pv-kpi-bottom">
                <div class="pv-kpi-stat"><span>Totalt oppgaver</span><strong>{{ previewData.complianceSummary.totalTasks }}</strong></div>
                <div class="pv-kpi-stat"><span>Lukkede avvik</span><strong>{{ previewData.complianceSummary.closedDeviations }}</strong></div>
                <div class="pv-kpi-stat"><span>Alkoholhendelser</span><strong>{{ previewData.complianceSummary.alcoholIncidents }}</strong></div>
              </div>
            </div>

            <div v-if="previewData.temperatureLogs && previewData.temperatureLogs.length > 0" class="pv-section">
              <div class="pv-module-badge">IK-MAT — MATSAMSVAR</div>
              <h3 class="pv-section-title">TEMPERATURLOGG</h3>
              <table class="pv-table">
                <thead><tr><th>PLASSERING</th><th>SNI °C</th><th>MIN °C</th><th>MAKS °C</th><th>AVVIK</th><th>STATUS</th></tr></thead>
                <tbody>
                  <tr v-for="(t, i) in previewData.temperatureLogs" :key="i">
                    <td class="pv-td-bold">{{ t.location }}</td>
                    <td>{{ t.avgTemp.toFixed(1) }}</td>
                    <td>{{ t.minTemp.toFixed(1) }}</td>
                    <td>{{ t.maxTemp.toFixed(1) }}</td>
                    <td>{{ t.deviationCount }}</td>
                    <td><span :class="['pv-badge', getStatusClass(t.status)]">{{ t.status.toUpperCase() }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="previewData.checklists && previewData.checklists.length > 0" class="pv-section">
              <h3 class="pv-section-title">SJEKKLISTE-FULLFØRING</h3>
              <div v-for="c in previewData.checklists" :key="c.id" class="pv-progress-row">
                <span class="pv-progress-name">{{ c.name }}</span>
                <div class="pv-progress-bar">
                  <div class="pv-progress-fill" :class="c.completionRate >= 100 ? 'pv-fill--green' : 'pv-fill--gold'" :style="{ width: Math.min(c.completionRate, 100) + '%' }"></div>
                </div>
                <span class="pv-progress-pct" :class="c.completionRate >= 100 ? 'pv-pct--green' : 'pv-pct--gold'">{{ c.completionRate.toFixed(0) }}% ({{ c.completedCount }}/{{ c.expectedCount }})</span>
              </div>
            </div>

            <div v-if="previewData.haccpChecklists && previewData.haccpChecklists.checklists.length > 0" class="pv-section">
              <h3 class="pv-section-title">HACCP-SJEKKLISTER</h3>
              <div v-for="(hc, hi) in previewData.haccpChecklists.checklists" :key="hi" class="pv-haccp-group">
                <div class="pv-haccp-header">
                  <strong>{{ hc.name }}</strong> <span class="pv-haccp-freq">({{ hc.frequency }})</span>
                  <span class="pv-haccp-pct">{{ hc.completionRate.toFixed(0) }}%</span>
                </div>
                <div class="pv-progress-bar pv-progress-bar--sm">
                  <div class="pv-progress-fill" :class="hc.completionRate >= 100 ? 'pv-fill--green' : 'pv-fill--gold'" :style="{ width: Math.min(hc.completionRate, 100) + '%' }"></div>
                </div>
              </div>
            </div>

            <div v-if="previewData.correctiveActions && previewData.correctiveActions.length > 0" class="pv-section">
              <h3 class="pv-section-title">KORRIGERENDE TILTAK</h3>
              <table class="pv-table">
                <thead><tr><th>ID</th><th>DATO</th><th>PROBLEM</th><th>TILTAK</th><th>STATUS</th></tr></thead>
                <tbody>
                  <tr v-for="ca in previewData.correctiveActions" :key="ca.id">
                    <td class="pv-td-bold">{{ ca.id }}</td>
                    <td>{{ ca.date }}</td>
                    <td>{{ ca.issue }}</td>
                    <td>{{ ca.actionTaken ?? '—' }}</td>
                    <td><span :class="['pv-badge', getStatusClass(ca.status)]">{{ ca.status.toUpperCase() }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="previewData.foodDeviations && previewData.foodDeviations.length > 0" class="pv-section">
              <h3 class="pv-section-title">MATAVVIK</h3>
              <table class="pv-table">
                <thead><tr><th>ID</th><th>DATO</th><th>TYPE</th><th>ALVORLIGHET</th><th>BESKRIVELSE</th><th>STATUS</th></tr></thead>
                <tbody>
                  <tr v-for="fd in previewData.foodDeviations" :key="fd.id">
                    <td class="pv-td-bold">{{ fd.id }}</td>
                    <td>{{ fd.date }}</td>
                    <td>{{ fd.type }}</td>
                    <td>{{ fd.severity }}</td>
                    <td>{{ fd.description }}</td>
                    <td><span :class="['pv-badge', getStatusClass(fd.status)]">{{ fd.status.toUpperCase() }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="previewData.alcoholDeviations && previewData.alcoholDeviations.length > 0" class="pv-section">
              <div class="pv-module-badge">IK-ALKOHOL — ALKOHOLSAMSVAR</div>
              <h3 class="pv-section-title">HENDELSESRAPPORTER</h3>
              <table class="pv-table">
                <thead><tr><th>ID</th><th>DATO</th><th>BESKRIVELSE</th><th>TILTAK</th><th>LØST</th></tr></thead>
                <tbody>
                  <tr v-for="ad in previewData.alcoholDeviations" :key="ad.id">
                    <td class="pv-td-bold">INC-{{ String(ad.id).padStart(3, '0') }}</td>
                    <td>{{ ad.date }}</td>
                    <td>{{ ad.description }}</td>
                    <td>{{ ad.immediateAction ?? '—' }}</td>
                    <td><span :class="['pv-badge', ad.status === 'Lukket' ? 'badge-green' : 'badge-yellow']">{{ ad.status === 'Lukket' ? 'JA' : 'NEI' }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="previewData.ageVerificationLogs && previewData.ageVerificationLogs.length > 0" class="pv-section">
              <h3 class="pv-section-title">ALDERSKONTROLL</h3>
              <table class="pv-table">
                <thead><tr><th>DATO</th><th>SKIFT</th><th>TOTALT SJEKKET</th><th>ID FORESPURT</th><th>AVVISNINGER</th></tr></thead>
                <tbody>
                  <tr v-for="(av, i) in previewData.ageVerificationLogs" :key="i">
                    <td>{{ av.date }}</td>
                    <td>{{ av.shift }}</td>
                    <td>{{ av.totalChecks }}</td>
                    <td>{{ av.idRequested }}</td>
                    <td :class="getRefusalClass(av.refusals)">{{ av.refusals }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="previewData.trainingOverview && previewData.trainingOverview.length > 0" class="pv-section">
              <h3 class="pv-section-title">OPPLÆRINGSOVERSIKT</h3>
              <table class="pv-table">
                <thead><tr><th>NAVN</th><th>ROLLE</th><th>SERTIFISERING</th><th>UTLØPER</th><th>STATUS</th></tr></thead>
                <tbody>
                  <tr v-for="(t, i) in previewData.trainingOverview" :key="i">
                    <td class="pv-td-bold">{{ t.name }}</td>
                    <td>{{ t.role ?? '—' }}</td>
                    <td>{{ t.certification }}</td>
                    <td>{{ t.expires ?? '—' }}</td>
                    <td><span :class="['pv-badge', getStatusClass(t.status)]">{{ t.status.toUpperCase() }}</span></td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="previewData.licenseInfo" class="pv-section">
              <h3 class="pv-section-title">LISENSINFORMASJON</h3>
              <div v-if="previewData.licenseInfo.bevillingNumber || previewData.licenseInfo.holder || previewData.licenseInfo.styrerName" class="pv-license-card" style="margin-bottom: 0.75rem;">
                <div class="pv-license-subtitle">SKJENKEBEVILLING</div>
                <div class="pv-license-grid">
                  <div v-if="previewData.licenseInfo.bevillingNumber" class="pv-license-item">
                    <span class="pv-license-label">BEVILLINGSNUMMER</span>
                    <strong>{{ previewData.licenseInfo.bevillingNumber }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.bevillingValidTo" class="pv-license-item">
                    <span class="pv-license-label">GYLDIG TIL</span>
                    <strong>{{ previewData.licenseInfo.bevillingValidTo }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.holder" class="pv-license-item">
                    <span class="pv-license-label">BEVILLINGSHAVER</span>
                    <strong>{{ previewData.licenseInfo.holder }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.styrerName" class="pv-license-item">
                    <span class="pv-license-label">STYRER</span>
                    <strong>{{ previewData.licenseInfo.styrerName }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.stedfortrederName" class="pv-license-item">
                    <span class="pv-license-label">STEDFORTREDER</span>
                    <strong>{{ previewData.licenseInfo.stedfortrederName }}</strong>
                  </div>
                </div>
              </div>
              <div v-if="previewData.licenseInfo.kunnskapsproveType || previewData.licenseInfo.kunnskapsproveCandidateName" class="pv-license-card">
                <div class="pv-license-subtitle">KUNNSKAPSPRØVE</div>
                <div class="pv-license-grid">
                  <div v-if="previewData.licenseInfo.kunnskapsproveType" class="pv-license-item">
                    <span class="pv-license-label">TYPE</span>
                    <strong>{{ previewData.licenseInfo.kunnskapsproveType }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.kunnskapsproveCandidateName" class="pv-license-item">
                    <span class="pv-license-label">KANDIDAT</span>
                    <strong>{{ previewData.licenseInfo.kunnskapsproveCandidateName }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.kunnskapsprovePassedDate" class="pv-license-item">
                    <span class="pv-license-label">BESTÅTT DATO</span>
                    <strong>{{ previewData.licenseInfo.kunnskapsprovePassedDate }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.kunnskapsproveMunicipality" class="pv-license-item">
                    <span class="pv-license-label">KOMMUNE</span>
                    <strong>{{ previewData.licenseInfo.kunnskapsproveMunicipality }}</strong>
                  </div>
                  <div v-if="previewData.licenseInfo.kunnskapsproveBirthDate" class="pv-license-item">
                    <span class="pv-license-label">FØDSELSDATO</span>
                    <strong>{{ previewData.licenseInfo.kunnskapsproveBirthDate }}</strong>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="previewData.signOff" class="pv-section">
              <h3 class="pv-section-title">SIGNERING</h3>
              <div class="pv-signoff">
                <div class="pv-signoff-row">
                  <div class="pv-signoff-col">
                    <span class="pv-signoff-label">LEDERS SIGNATUR</span>
                    <div class="pv-signoff-name">{{ previewData.signOff.name }}</div>
                    <div class="pv-signoff-line"></div>
                    <span v-if="previewData.signOff.title" class="pv-signoff-role">{{ previewData.signOff.name }} &#8212; {{ previewData.signOff.title }}</span>
                  </div>
                  <div class="pv-signoff-col">
                    <span class="pv-signoff-label">DATO</span>
                    <div class="pv-signoff-date">{{ previewData.signOff.date }}</div>
                    <div class="pv-signoff-line"></div>
                  </div>
                </div>
                <div v-if="previewData.signOff.comments" class="pv-signoff-comments">
                  <span class="pv-signoff-label">KOMMENTARER</span>
                  <p>{{ previewData.signOff.comments }}</p>
                </div>
              </div>
            </div>
          </div>
          </div>

          <DialogFooter>
            <Button variant="outline" @click="previewOpen = false">Lukk</Button>
            <Button variant="outline" @click="handleSave" :disabled="generateMutation.isPending.value"><Save :size="16" /> Lagre</Button>
            <Button @click="handleDownloadFromPreview" :disabled="generateMutation.isPending.value"><Download :size="16" /> Last ned</Button>
          </DialogFooter>
        </DialogScrollContent>
      </Dialog>

      <AlertDialog :open="deleteDialogOpen" @update:open="(v: boolean) => { deleteDialogOpen = v }">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Slett rapport?</AlertDialogTitle>
            <AlertDialogDescription>Er du sikker på at du vil slette rapporten "{{ deletingReport?.title }}"? Dette kan ikke angres.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Avbryt</AlertDialogCancel>
            <AlertDialogAction variant="destructive" @click="confirmDelete">Slett</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  </AppLayout>
</template>

<style scoped>
.page-header { display: flex; height: 4rem; flex-shrink: 0; align-items: center; }
.page-header-inner { display: flex; align-items: center; gap: 0.5rem; padding: 0 1rem; }
.header-separator { height: 1rem !important; width: 1px !important; margin-right: 0.5rem; }
.page-title { font-weight: 500; color: hsl(var(--sidebar-primary, 245 43% 52%)); }
.page-content { display: flex; flex: 1; flex-direction: column; gap: 1rem; padding: 0 1rem 1rem; }
.header-row { display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; }
h1 { margin: 0; font-size: 1.75rem; font-weight: 800; letter-spacing: -0.03em; }
.header-row p { margin-top: 4px; color: var(--text-secondary); font-size: 0.88rem; }

.create-form { display: flex; flex-direction: column; gap: 1.25rem; }
.form-field { display: flex; flex-direction: column; gap: 0.4rem; }
.form-label { font-size: 0.8rem; font-weight: 600; color: hsl(var(--muted-foreground, 24 5% 46%)); text-transform: uppercase; letter-spacing: 0.02em; }
.form-dates { display: flex; gap: 1rem; }
.form-date-field { flex: 1; position: relative; }
.form-date-field:first-child { z-index: 21; }
.form-date-field:last-child { z-index: 20; }
.form-textarea { width: 100%; border: 1px solid hsl(var(--input, 35 15% 85%)); border-radius: 0.5rem; padding: 0.5rem 0.75rem; font: inherit; font-size: 0.875rem; background: hsl(var(--card)); resize: vertical; box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); transition: all 150ms ease; outline: none; }
.form-textarea:focus { box-shadow: 0 0 0 2px hsl(var(--ring, 245 43% 52%) / 0.2); border-color: hsl(var(--primary, 245 43% 52%) / 0.5); }

.section-groups { display: flex; flex-direction: column; gap: 0.75rem; }
.section-group { display: flex; flex-direction: column; gap: 0.35rem; }
.section-group-label { font-size: 0.72rem; font-weight: 700; color: hsl(var(--muted-foreground)); text-transform: uppercase; letter-spacing: 0.04em; padding-bottom: 0.15rem; border-bottom: 1px solid hsl(var(--border) / 0.5); }
.section-group-items { display: flex; flex-wrap: wrap; gap: 0.25rem 0.75rem; }
.section-option { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.85rem; padding: 0.3rem 0.4rem; border-radius: 0.375rem; transition: background 150ms; }
.section-option:hover { background: hsl(var(--accent, 250 40% 95%)); }

.checklist-picker-toggle { display: inline-flex; align-items: center; gap: 0.35rem; border: 1px solid hsl(var(--input, 35 15% 85%)); border-radius: 0.5rem; padding: 0.5rem 0.75rem; font: inherit; font-size: 0.875rem; background: hsl(var(--card)); cursor: pointer; color: hsl(var(--muted-foreground)); box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); transition: all 150ms; }
.checklist-picker-toggle:hover { border-color: hsl(var(--primary, 245 43% 52%) / 0.5); }
.checklist-picker { display: flex; flex-direction: column; gap: 0.25rem; padding: 0.5rem; border: 1px solid hsl(var(--border)); border-radius: 0.5rem; max-height: 12rem; overflow-y: auto; margin-top: 0.25rem; }
.checklist-option { display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.84rem; padding: 0.25rem 0.35rem; border-radius: 0.25rem; }
.checklist-option:hover { background: hsl(var(--accent, 250 40% 95%)); }
.signoff-fields { display: flex; flex-direction: column; gap: 0.75rem; padding: 0.75rem; border: 1px solid hsl(var(--border)); border-radius: 0.5rem; background: hsl(var(--muted, 40 18% 93%) / 0.3); }

.table-section { display: flex; flex-direction: column; gap: 0.75rem; }
.search-row { display: flex; align-items: center; justify-content: space-between; gap: 0.75rem; }
.search-wrapper { position: relative; width: 20rem; }
.search-icon { position: absolute; left: 0.65rem; top: 50%; transform: translateY(-50%); color: hsl(var(--muted-foreground)); pointer-events: none; }
.search-input { width: 100%; border: 1px solid hsl(var(--border)); border-radius: 0.5rem; padding: 0.5rem 0.75rem 0.5rem 2.1rem; font: inherit; font-size: 0.85rem; background: hsl(var(--card)); }
.search-input:focus { outline: none; border-color: hsl(var(--ring, 245 43% 52%)); box-shadow: 0 0 0 2px hsl(var(--ring, 245 43% 52%) / 0.15); }
.table-card { border: 1px solid hsl(var(--border)); border-radius: 0.75rem; background: hsl(var(--card)); }
.sort-btn { display: inline-flex; align-items: center; gap: 0.35rem; background: none; border: none; font: inherit; font-weight: 500; color: hsl(var(--muted-foreground)); cursor: pointer; padding: 0.25rem 0.4rem; border-radius: var(--radius-md, 0.375rem); margin: -0.25rem -0.4rem; transition: background 150ms, color 150ms; }
.sort-btn:hover { background: hsl(var(--accent, 250 40% 95%)); color: hsl(var(--foreground)); }
.sort-icon { opacity: 0.4; transition: opacity 150ms; }
.sort-icon--active { opacity: 1; }
.cell-title { display: flex; align-items: center; gap: 0.5rem; }
.cell-title-icon { flex-shrink: 0; color: hsl(var(--primary, 245 43% 52%)); }
.cell-title-text { font-weight: 500; }
.cell-text { color: hsl(var(--muted-foreground)); }
.cell-actions { width: 3rem; text-align: right; }
.actions-trigger { display: flex; align-items: center; justify-content: center; width: 2rem; height: 2rem; border-radius: var(--radius-md, 0.375rem); border: none; background: none; color: hsl(var(--muted-foreground)); cursor: pointer; transition: background 150ms, color 150ms; }
.actions-trigger:hover { background: hsl(var(--accent, 250 40% 95%)); color: hsl(var(--foreground)); }
.menu-item--danger { color: var(--red); }
.th-title { min-width: 10rem; } .th-period { min-width: 10rem; } .th-user { min-width: 8rem; } .th-date { min-width: 6rem; } .th-actions { width: 3rem; }

.mobile-report-list { display: flex; flex-direction: column; gap: 0.65rem; }
.mobile-report-card { border: 1px solid hsl(var(--border)); border-radius: 0.65rem; background: hsl(var(--card)); padding: 0.45rem 0.65rem; }
.mobile-report-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.25rem; }
.mobile-report-title { font-size: 0.9rem; }
.mobile-report-row { display: flex; justify-content: space-between; align-items: flex-start; gap: 0.75rem; padding: 0.32rem 0; }
.mobile-report-row > span:first-child { font-size: 0.73rem; font-weight: 600; color: hsl(var(--muted-foreground)); text-transform: uppercase; letter-spacing: 0.02em; }
.mobile-report-row > :last-child { text-align: right; }

.loading-state { display: flex; flex-direction: column; gap: 10px; }
.skeleton-item { height: 56px; border-radius: var(--radius-xl); background: linear-gradient(90deg, hsl(var(--muted)) 25%, hsl(var(--muted) / 0.5) 50%, hsl(var(--muted)) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite ease-in-out; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.empty-state { position: relative; display: flex; min-height: 260px; flex-direction: column; align-items: center; justify-content: center; overflow: hidden; border-radius: 1rem; border: 2px dashed hsl(var(--muted-foreground) / 0.2); background: hsl(var(--muted) / 0.3); padding: 2rem; }
.empty-state-inner { position: relative; display: flex; flex-direction: column; align-items: center; gap: 1rem; text-align: center; }
.empty-state-icon { display: flex; height: 5rem; width: 5rem; align-items: center; justify-content: center; border-radius: 1rem; background-color: hsl(var(--primary) / 0.1); box-shadow: 0 0 0 4px hsl(var(--primary) / 0.05); }
.empty-state-icon--danger { background-color: hsl(var(--destructive) / 0.1); box-shadow: 0 0 0 4px hsl(var(--destructive) / 0.05); }
.empty-state-icon--danger :deep(svg) { color: hsl(var(--destructive) / 0.7); }
.empty-state-icon :deep(svg) { width: 2.5rem; height: 2.5rem; color: hsl(var(--primary) / 0.7); }
.empty-state-text h3 { font-size: 1.125rem; font-weight: 600; letter-spacing: -0.01em; }
.empty-state-text p { max-width: 24rem; font-size: 0.875rem; color: hsl(var(--muted-foreground)); margin-top: 0.25rem; }

.pv-scroll-wrapper { overflow-x: auto; -webkit-overflow-scrolling: touch; }
.pv { display: flex; flex-direction: column; gap: 1.75rem; font-family: 'Inter', system-ui, -apple-system, sans-serif; color: #1a1a2e; min-width: 640px; }
.pv-header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 1rem; border-bottom: 3px solid #1a1a2e; }
.pv-header-left { display: flex; flex-direction: column; gap: 0.15rem; }
.pv-header-label { font-size: 0.7rem; font-weight: 600; letter-spacing: 0.12em; color: #888; text-transform: uppercase; }
.pv-header-org { margin: 0; font-size: 1.4rem; font-weight: 800; color: #1a1a2e; letter-spacing: -0.02em; }
.pv-header-orgnr { font-size: 0.82rem; color: #666; }
.pv-header-right { text-align: right; display: flex; flex-direction: column; gap: 0.15rem; font-size: 0.82rem; color: #666; }
.pv-section { display: flex; flex-direction: column; gap: 0.75rem; }
.pv-section-title { margin: 0; font-size: 1rem; font-weight: 800; color: #1a1a2e; letter-spacing: 0.02em; text-transform: uppercase; padding-bottom: 0.4rem; border-bottom: 3px solid #1a1a2e; }
.pv-module-badge { display: inline-block; background: #1a1a2e; color: #fff; font-size: 0.7rem; font-weight: 700; letter-spacing: 0.06em; padding: 0.3rem 0.75rem; border-radius: 0.25rem; text-transform: uppercase; align-self: flex-start; }

.pv-kpi-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 0.65rem; }
.pv-kpi-card { background: #f5f5f8; border-radius: 0.5rem; padding: 0.85rem 1rem; display: flex; flex-direction: column; gap: 0.25rem; border-left: 4px solid transparent; }
.pv-kpi-card--green { border-left-color: #2d5a27; }
.pv-kpi-card--olive { border-left-color: #8b7d3c; }
.pv-kpi-card--gold { border-left-color: #b8960c; }
.pv-kpi-card--red { border-left-color: #c0392b; }
.pv-kpi-label { font-size: 0.65rem; font-weight: 700; letter-spacing: 0.06em; color: #888; text-transform: uppercase; }
.pv-kpi-value { font-size: 1.75rem; font-weight: 800; font-variant-numeric: tabular-nums; }
.pv-kpi-value--green { color: #2d5a27; }
.pv-kpi-value--olive { color: #8b7d3c; }
.pv-kpi-value--gold { color: #b8960c; }
.pv-kpi-value--red { color: #c0392b; }
.pv-kpi-bottom { display: grid; grid-template-columns: repeat(3, 1fr); gap: 0.5rem; }
.pv-kpi-stat { display: flex; justify-content: space-between; align-items: center; background: #f5f5f8; border-radius: 0.35rem; padding: 0.5rem 0.75rem; font-size: 0.82rem; }
.pv-kpi-stat span { color: #666; }
.pv-kpi-stat strong { font-size: 1rem; color: #1a1a2e; }

.pv-table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }
.pv-table thead th { text-align: left; padding: 0.55rem 0.65rem; font-size: 0.7rem; font-weight: 700; letter-spacing: 0.06em; color: #888; text-transform: uppercase; border-bottom: 3px solid #1a1a2e; }
.pv-table tbody td { padding: 0.6rem 0.65rem; border-bottom: 1px solid #e8e8f0; vertical-align: top; }
.pv-table tbody tr:last-child td { border-bottom: none; }
.pv-td-bold { font-weight: 600; }

.pv-badge { display: inline-block; padding: 0.15rem 0.55rem; border-radius: 0.25rem; font-size: 0.72rem; font-weight: 700; letter-spacing: 0.03em; }
.badge-green { background: #dcf5dc; color: #1a6b1a; }
.badge-yellow { background: #fef3cd; color: #856404; }
.badge-red { background: #fde2e2; color: #c0392b; }
.badge-gray { background: #e8e8f0; color: #555; }
.refusal-highlight { color: #c0392b; font-weight: 700; }

.pv-progress-row { display: flex; align-items: center; gap: 0.65rem; padding: 0.35rem 0; }
.pv-progress-name { flex-shrink: 0; width: 10rem; font-size: 0.88rem; font-weight: 600; color: #1a1a2e; }
.pv-progress-bar { flex: 1; height: 0.65rem; background: #e0e0e0; border-radius: 0.35rem; overflow: hidden; }
.pv-progress-bar--sm { height: 0.45rem; margin-top: 0.25rem; }
.pv-progress-fill { height: 100%; border-radius: 0.35rem; transition: width 300ms ease; }
.pv-fill--green { background: #2d5a27; }
.pv-fill--gold { background: #b8960c; }
.pv-progress-pct { flex-shrink: 0; width: 2.5rem; font-size: 0.82rem; font-weight: 700; text-align: right; }
.pv-pct--green { color: #2d5a27; }
.pv-pct--gold { color: #b8960c; }

.pv-haccp-group { margin-bottom: 0.5rem; }
.pv-haccp-header { font-size: 0.88rem; }
.pv-haccp-freq { color: #888; font-size: 0.78rem; font-weight: 400; }
.pv-haccp-pct { float: right; font-weight: 700; color: #2d5a27; }

.pv-license-card { background: #f5f5f8; border-radius: 0.5rem; padding: 1rem; }
.pv-license-subtitle { font-size: 0.7rem; font-weight: 700; letter-spacing: 0.08em; color: #1a1a2e; text-transform: uppercase; margin-bottom: 0.6rem; }
.pv-license-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 0.75rem; }
.pv-license-item { display: flex; flex-direction: column; gap: 0.2rem; }
.pv-license-label { font-size: 0.65rem; font-weight: 700; letter-spacing: 0.06em; color: #888; text-transform: uppercase; }
.pv-license-item strong { font-size: 0.92rem; color: #1a1a2e; }

.pv-signoff { border: 1px solid #e0e0e0; border-radius: 0.5rem; padding: 1.25rem; }
.pv-signoff-row { display: grid; grid-template-columns: 1fr 1fr; gap: 2rem; }
.pv-signoff-col { display: flex; flex-direction: column; gap: 0.25rem; }
.pv-signoff-label { font-size: 0.65rem; font-weight: 700; letter-spacing: 0.06em; color: #888; text-transform: uppercase; }
.pv-signoff-name { font-size: 1.15rem; font-style: italic; font-weight: 600; color: #1a1a2e; margin-top: 0.75rem; }
.pv-signoff-date { font-size: 1.05rem; font-weight: 600; color: #1a1a2e; margin-top: 0.75rem; font-variant-numeric: tabular-nums; }
.pv-signoff-line { border-bottom: 2px solid #1a1a2e; margin-top: 0.25rem; }
.pv-signoff-role { font-size: 0.8rem; color: #888; margin-top: 0.25rem; }
.pv-signoff-comments { margin-top: 1rem; }
.pv-signoff-comments p { margin: 0.35rem 0 0; font-size: 0.88rem; font-style: italic; color: #555; border: 1px solid #e0e0e0; border-radius: 0.35rem; padding: 0.65rem; }

@media (max-width: 768px) {
  .hide-mobile { display: none !important; }
  .header-row { flex-direction: column; }
  .search-row { flex-direction: column; align-items: stretch; }
  .search-wrapper { width: 100%; }
  .form-dates { flex-direction: column; }
  .pv-header { flex-direction: column; gap: 0.5rem; }
  .pv-header-right { text-align: left; }
  .pv-kpi-row { grid-template-columns: repeat(2, 1fr); }
  .pv-kpi-bottom { grid-template-columns: 1fr; }
  .pv-license-grid { grid-template-columns: 1fr; }
  .pv-signoff-row { grid-template-columns: 1fr; gap: 1rem; }
  .pv-progress-name { width: 7rem; font-size: 0.8rem; }
}
</style>

<style>
.dialog-scroll-content.preview-dialog { --dialog-max-width: 64rem; width: 95vw; }
.dialog-scroll-content.create-dialog { --dialog-max-width: 36rem; }
</style>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import axios from 'axios'
import {
  ScrollText,
  MoreVertical,
  Pencil,
  FileText,
  ExternalLink,
  Upload,
  X,
  Loader2,
} from 'lucide-vue-next'
import { toast } from 'vue-sonner'
import AppLayout from '@/components/layout/AppLayout.vue'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import Button from '@/components/ui/button/Button.vue'
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from '@/components/ui/dropdown-menu'
import {
  useAlcoholPolicyExistsQuery,
  useAlcoholPolicyQuery,
  useUpsertAlcoholPolicyMutation,
} from '@/composables/useAlcoholPolicy'
import { useDocumentUploadMutation, useDocumentUrlQuery } from '@/composables/useDocumentUpload'
import type {
  AgeCheckLimit,
  IdType,
  KnowledgeTestType,
  CreateAlcoholPolicyRequest,
} from '@/types/alcoholPolicy'

// ── Queries ──
const existsQuery = useAlcoholPolicyExistsQuery()
const policyExists = computed(() => existsQuery.data.value === true)
const policyQuery = useAlcoholPolicyQuery(() => policyExists.value)
const policy = computed(() => policyQuery.data.value ?? null)
const upsertMutation = useUpsertAlcoholPolicyMutation()
const bevillingUpload = useDocumentUploadMutation()
const kunnskapsproveUpload = useDocumentUploadMutation()

const editing = ref(false)
const showForm = computed(() => {
  if (existsQuery.isLoading.value) return false
  if (editing.value) return true
  return !policyExists.value
})
const showCard = computed(() => policyExists.value && !editing.value && !!policy.value)

// ── Form state ──
const form = ref<CreateAlcoholPolicyRequest>({})
const submitting = ref(false)

// File upload state
const bevillingFileName = ref<string | null>(null)
const kunnskapsproveFileName = ref<string | null>(null)
const bevillingUploading = ref(false)
const kunnskapsproveUploading = ref(false)
const bevillingFileRef = ref<HTMLInputElement | null>(null)
const kunnskapsproveFileRef = ref<HTMLInputElement | null>(null)

// Populate form when editing existing policy
watch(
  () => editing.value,
  (isEditing) => {
    if (isEditing && policy.value) {
      form.value = {
        bevillingNumber: policy.value.bevillingNumber,
        bevillingValidUntil: policy.value.bevillingValidUntil,
        styrerName: policy.value.styrerName,
        stedfortrederName: policy.value.stedfortrederName,
        bevillingDocumentId: policy.value.bevillingDocumentId,
        kunnskapsproveCandidateName: policy.value.kunnskapsproveCandidateName,
        kunnskapsproveBirthDate: policy.value.kunnskapsproveBirthDate,
        kunnskapsproveType: policy.value.kunnskapsproveType,
        kunnskapsproveMunicipality: policy.value.kunnskapsproveMunicipality,
        kunnskapsprovePassedDate: policy.value.kunnskapsprovePassedDate,
        kunnskapsproveDocumentId: policy.value.kunnskapsproveDocumentId,
        ageCheckLimit: policy.value.ageCheckLimit,
        acceptedIdTypes: [...policy.value.acceptedIdTypes],
        doubtRoutine: policy.value.doubtRoutine,
        intoxicationSigns: policy.value.intoxicationSigns,
        refusalProcedure: policy.value.refusalProcedure,
      }
      bevillingFileName.value = null
      kunnskapsproveFileName.value = null
    }
  },
)

function resetForm() {
  form.value = {}
  bevillingFileName.value = null
  kunnskapsproveFileName.value = null
  editing.value = false
}

// ── Age check limit ──
function setAgeCheck(val: AgeCheckLimit) {
  form.value.ageCheckLimit = val
}

// ── ID types ──
const allIdTypes: { value: IdType; label: string }[] = [
  { value: 'PASS', label: 'Pass' },
  { value: 'FORERKORT', label: 'Førerkort' },
  { value: 'BANKKORT', label: 'Bankkort m/bilde' },
  { value: 'NASJONALT_ID', label: 'Nasjonalt ID-kort' },
]

function toggleIdType(id: IdType) {
  const current = form.value.acceptedIdTypes ?? ['PASS', 'FORERKORT', 'BANKKORT', 'NASJONALT_ID']
  if (current.includes(id)) {
    form.value.acceptedIdTypes = current.filter((t) => t !== id)
  } else {
    form.value.acceptedIdTypes = [...current, id]
  }
}

function isIdTypeSelected(id: IdType): boolean {
  return (form.value.acceptedIdTypes ?? ['PASS', 'FORERKORT', 'BANKKORT', 'NASJONALT_ID']).includes(id)
}

// ── Immediate file upload ──
function validateFile(file: File): boolean {
  const allowed = ['application/pdf', 'image/jpeg', 'image/png']
  if (!allowed.includes(file.type)) {
    toast.error('Kun PDF, JPG og PNG er tillatt')
    return false
  }
  return true
}

async function uploadBevillingFile(file: File) {
  if (!validateFile(file)) return
  bevillingUploading.value = true
  bevillingFileName.value = file.name
  try {
    const doc = await bevillingUpload.mutateAsync(file)
    form.value.bevillingDocumentId = doc.id
    toast.success(`Bevillingsdokument lastet opp: ${file.name}`)
  } catch {
    toast.error('Kunne ikke laste opp bevillingsdokument')
    bevillingFileName.value = null
    form.value.bevillingDocumentId = null
  } finally {
    bevillingUploading.value = false
  }
}

async function uploadKunnskapsproveFile(file: File) {
  if (!validateFile(file)) return
  kunnskapsproveUploading.value = true
  kunnskapsproveFileName.value = file.name
  try {
    const doc = await kunnskapsproveUpload.mutateAsync(file)
    form.value.kunnskapsproveDocumentId = doc.id
    toast.success(`Kunnskapsprøve-bevis lastet opp: ${file.name}`)
  } catch {
    toast.error('Kunne ikke laste opp kunnskapsprøve-bevis')
    kunnskapsproveFileName.value = null
    form.value.kunnskapsproveDocumentId = null
  } finally {
    kunnskapsproveUploading.value = false
  }
}

function onFileChange(e: Event, target: 'bevilling' | 'kunnskapsprove') {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  if (target === 'bevilling') uploadBevillingFile(file)
  else uploadKunnskapsproveFile(file)
}

function onDrop(e: DragEvent, target: 'bevilling' | 'kunnskapsprove') {
  e.preventDefault()
  const file = e.dataTransfer?.files?.[0]
  if (!file) return
  if (target === 'bevilling') uploadBevillingFile(file)
  else uploadKunnskapsproveFile(file)
}

function removeBevillingFile() {
  bevillingFileName.value = null
  form.value.bevillingDocumentId = null
}

function removeKunnskapsproveFile() {
  kunnskapsproveFileName.value = null
  form.value.kunnskapsproveDocumentId = null
}

function triggerFileInput(ref: HTMLInputElement | null) {
  ref?.click()
}

// ── Submit ──
async function handleSubmit() {
  submitting.value = true
  try {
    await upsertMutation.mutateAsync(form.value)
    toast.success(editing.value ? 'Skjenkepolicy oppdatert' : 'Skjenkepolicy lagret')
    resetForm()
  } catch (error) {
    if (axios.isAxiosError(error)) {
      const message = error.response?.data?.error?.message
      if (typeof message === 'string' && message.trim().length > 0) {
        toast.error(message)
        return
      }
    }
    toast.error('Kunne ikke lagre skjenkepolicy')
  } finally {
    submitting.value = false
  }
}

// ── View document ──
async function openDocument(docId: number) {
  try {
    const res = await useDocumentUrlQuery(docId)
    window.open(res.url, '_blank')
  } catch {
    toast.error('Kunne ikke hente dokumentet')
  }
}

// ── Helpers ──
function formatDate(iso: string | null): string {
  if (!iso) return '—'
  const d = new Date(iso)
  return d.toLocaleDateString('nb-NO', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

const ageCheckLabel: Record<AgeCheckLimit, string> = {
  UNDER_25: 'Alle under 25',
  UNDER_23: 'Alle under 23',
}

const idTypeLabel: Record<IdType, string> = {
  PASS: 'Pass',
  FORERKORT: 'Førerkort',
  BANKKORT: 'Bankkort m/bilde',
  NASJONALT_ID: 'Nasjonalt ID-kort',
}

const knowledgeTestLabel: Record<KnowledgeTestType, string> = {
  SALG: 'Salg',
  SKJENKE: 'Skjenke',
  BOTH: 'Begge',
}

const knowledgeTestTypes: { value: KnowledgeTestType; label: string }[] = [
  { value: 'SALG', label: 'Salg' },
  { value: 'SKJENKE', label: 'Skjenke' },
  { value: 'BOTH', label: 'Begge' },
]
</script>

<template>
  <AppLayout>
    <header class="page-header">
      <div class="page-header-inner">
        <SidebarTrigger />
        <Separator orientation="vertical" class="header-separator" />
        <span class="page-title">Skjenkepolicy</span>
      </div>
    </header>

    <div class="page-content">
      <!-- Loading exists check -->
      <p v-if="existsQuery.isLoading.value" class="state-line">Laster...</p>

      <!-- ═══ FORM VIEW ═══ -->
      <template v-else-if="showForm">
        <section class="form-container">
          <div class="form-header">
            <h1>Skjenkepolicy</h1>
            <p class="form-subtitle">
              Rutiner iht. alkoholloven §1-9 og alkoholforskriften kap. 8. Dokumentasjonen skal
              være tilgjengelig for kontrollmyndigheten.
            </p>
          </div>

          <form @submit.prevent="handleSubmit">
            <!-- Bevillingsinformasjon -->
            <fieldset class="form-section">
              <legend class="section-title">Bevillingsinformasjon</legend>

              <div class="field-row two-col">
                <div class="field">
                  <label class="field-label">Bevillingsnummer</label>
                  <input v-model="form.bevillingNumber" type="text" class="field-input" placeholder="F.eks. 2024/12345" />
                </div>
                <div class="field">
                  <label class="field-label">Gyldig til</label>
                  <input v-model="form.bevillingValidUntil" type="date" class="field-input" />
                </div>
              </div>

              <div class="field-row two-col">
                <div class="field">
                  <label class="field-label">Styrer (navn)</label>
                  <input v-model="form.styrerName" type="text" class="field-input" placeholder="Fullt navn" />
                </div>
                <div class="field">
                  <label class="field-label">Stedfortreder (navn)</label>
                  <input v-model="form.stedfortrederName" type="text" class="field-input" placeholder="Fullt navn" />
                </div>
              </div>

              <!-- Bevilling document upload -->
              <div class="field">
                <label class="field-label">Last opp bevillingsdokument</label>
                <input ref="bevillingFileRef" type="file" accept=".pdf,.jpg,.jpeg,.png" class="file-hidden" @change="onFileChange($event, 'bevilling')" />
                <div
                  v-if="!bevillingFileName && !form.bevillingDocumentId"
                  class="drop-zone"
                  @dragover.prevent
                  @drop="onDrop($event, 'bevilling')"
                  @click="triggerFileInput(bevillingFileRef)"
                >
                  <div class="drop-zone-inner">
                    <Upload :size="18" class="drop-zone-icon" />
                    <span>Dra filer hit · PDF, JPG, PNG</span>
                  </div>
                </div>
                <div v-else class="file-chip">
                  <Loader2 v-if="bevillingUploading" :size="16" class="file-chip-spinner" />
                  <FileText v-else :size="16" />
                  <span class="file-chip-name">{{ bevillingFileName ?? 'Bevillingsdokument' }}</span>
                  <span v-if="bevillingUploading" class="file-chip-status">Laster opp...</span>
                  <button v-if="!bevillingUploading" type="button" class="file-chip-remove" @click="removeBevillingFile">
                    <X :size="14" />
                  </button>
                </div>
              </div>
            </fieldset>

            <!-- Kunnskapsprøve -->
            <fieldset class="form-section">
              <legend class="section-title">Kunnskapsprøve</legend>

              <div class="field-row two-col">
                <div class="field">
                  <label class="field-label">Kandidatens navn</label>
                  <input v-model="form.kunnskapsproveCandidateName" type="text" class="field-input" placeholder="Fullt navn" />
                </div>
                <div class="field">
                  <label class="field-label">Fødselsdato</label>
                  <input v-model="form.kunnskapsproveBirthDate" type="date" class="field-input" />
                </div>
              </div>

              <div class="field-row two-col">
                <div class="field">
                  <label class="field-label">Bestått dato</label>
                  <input v-model="form.kunnskapsprovePassedDate" type="date" class="field-input" />
                </div>
                <div class="field">
                  <label class="field-label">Kommune</label>
                  <input v-model="form.kunnskapsproveMunicipality" type="text" class="field-input" placeholder="F.eks. Oslo" />
                </div>
              </div>

              <div class="field">
                <label class="field-label">Type kunnskapsprøve</label>
                <div class="chip-group">
                  <button
                    v-for="t in knowledgeTestTypes"
                    :key="t.value"
                    type="button"
                    class="chip-btn"
                    :class="{ 'chip-btn--active': form.kunnskapsproveType === t.value }"
                    @click="form.kunnskapsproveType = t.value"
                  >
                    {{ t.label }}
                  </button>
                </div>
              </div>

              <!-- Kunnskapsprøve document upload -->
              <div class="field">
                <label class="field-label">Last opp kunnskapsprøve-bevis</label>
                <input ref="kunnskapsproveFileRef" type="file" accept=".pdf,.jpg,.jpeg,.png" class="file-hidden" @change="onFileChange($event, 'kunnskapsprove')" />
                <div
                  v-if="!kunnskapsproveFileName && !form.kunnskapsproveDocumentId"
                  class="drop-zone"
                  @dragover.prevent
                  @drop="onDrop($event, 'kunnskapsprove')"
                  @click="triggerFileInput(kunnskapsproveFileRef)"
                >
                  <div class="drop-zone-inner">
                    <Upload :size="18" class="drop-zone-icon" />
                    <span>Dra filer hit · PDF, JPG, PNG</span>
                  </div>
                </div>
                <div v-else class="file-chip">
                  <Loader2 v-if="kunnskapsproveUploading" :size="16" class="file-chip-spinner" />
                  <FileText v-else :size="16" />
                  <span class="file-chip-name">{{ kunnskapsproveFileName ?? 'Kunnskapsprøve-bevis' }}</span>
                  <span v-if="kunnskapsproveUploading" class="file-chip-status">Laster opp...</span>
                  <button v-if="!kunnskapsproveUploading" type="button" class="file-chip-remove" @click="removeKunnskapsproveFile">
                    <X :size="14" />
                  </button>
                </div>
              </div>
            </fieldset>

            <!-- Rutiner for alderskontroll -->
            <fieldset class="form-section">
              <legend class="section-title">Rutiner for alderskontroll</legend>
              <p class="section-subtitle">Beskriv hvordan dere kontrollerer alder. Lovpålagt iht. alkoholloven.</p>

              <div class="field">
                <label class="field-label">Aldersgrense for legitimasjonskontroll</label>
                <div class="toggle-group">
                  <button type="button" class="toggle-btn" :class="{ 'toggle-btn--active': (form.ageCheckLimit ?? 'UNDER_25') === 'UNDER_25' }" @click="setAgeCheck('UNDER_25')">
                    <span>Alle under 25</span>
                    <span class="toggle-rec">Anbefalt</span>
                  </button>
                  <button type="button" class="toggle-btn" :class="{ 'toggle-btn--active': form.ageCheckLimit === 'UNDER_23' }" @click="setAgeCheck('UNDER_23')">
                    Alle under 23
                  </button>
                </div>
              </div>

              <div class="field">
                <label class="field-label">Godkjente legitimasjonstyper</label>
                <div class="chip-group">
                  <button
                    v-for="idType in allIdTypes"
                    :key="idType.value"
                    type="button"
                    class="chip-btn"
                    :class="{ 'chip-btn--active': isIdTypeSelected(idType.value) }"
                    @click="toggleIdType(idType.value)"
                  >
                    {{ idType.label }}
                  </button>
                </div>
              </div>

              <div class="field">
                <label class="field-label">Rutine ved tvil om alder</label>
                <textarea v-model="form.doubtRoutine" class="field-textarea" placeholder="Beskriv steg-for-steg: Hva gjør bartender/servitør når det er tvil?" rows="3" />
              </div>
            </fieldset>

            <!-- Ansvarlig servering -->
            <fieldset class="form-section">
              <legend class="section-title">Ansvarlig servering</legend>
              <p class="section-subtitle">Rutiner for å hindre overskjenking og sikre forsvarlig drift.</p>

              <div class="field">
                <label class="field-label">Hvordan identifiserer ansatte åpenbart berusede gjester?</label>
                <textarea v-model="form.intoxicationSigns" class="field-textarea" placeholder="Tegn å se etter, prosedyre for servingsnekt..." rows="3" />
              </div>

              <div class="field">
                <label class="field-label">Prosedyre ved nekt av servering</label>
                <textarea v-model="form.refusalProcedure" class="field-textarea" placeholder="Hvem tar avgjørelsen, hvordan kommuniseres det, eskaleringsrutine..." rows="3" />
              </div>
            </fieldset>

            <div class="form-actions">
              <Button v-if="editing" type="button" variant="outline" @click="resetForm">Avbryt</Button>
              <Button type="submit" :disabled="submitting">
                {{ submitting ? 'Lagrer...' : editing ? 'Oppdater' : 'Lagre skjenkepolicy' }}
              </Button>
            </div>
          </form>
        </section>
      </template>

      <!-- ═══ CARD VIEW ═══ -->
      <template v-else-if="showCard">
        <section class="header-row">
          <div>
            <h1>Skjenkepolicy</h1>
            <p>Rutiner iht. alkoholloven §1-9 og alkoholforskriften kap. 8</p>
          </div>
        </section>

        <div class="license-card">
          <div class="license-card-header">
            <div class="license-card-header-left">
              <div class="license-icon"><ScrollText :size="20" /></div>
              <div>
                <h3 class="license-card-title">Bevillingslisens</h3>
                <p v-if="policy!.bevillingValidUntil" class="license-card-sub">Gyldig til {{ formatDate(policy!.bevillingValidUntil) }}</p>
              </div>
            </div>
            <DropdownMenu>
              <DropdownMenuTrigger as-child>
                <Button type="button" variant="ghost" size="icon-sm" class="actions-trigger"><MoreVertical :size="18" /></Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" :side-offset="4">
                <DropdownMenuItem @click="editing = true"><Pencil :size="16" /> Rediger</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>

          <div class="license-card-body">
            <div class="license-grid">
              <div class="license-field"><span class="license-label">Bevillingsnummer</span><span class="license-value">{{ policy!.bevillingNumber ?? '—' }}</span></div>
              <div class="license-field"><span class="license-label">Gyldig til</span><span class="license-value">{{ formatDate(policy!.bevillingValidUntil) }}</span></div>
              <div class="license-field"><span class="license-label">Styrer</span><span class="license-value">{{ policy!.styrerName ?? '—' }}</span></div>
              <div class="license-field"><span class="license-label">Stedfortreder</span><span class="license-value">{{ policy!.stedfortrederName ?? '—' }}</span></div>
            </div>
            <Button v-if="policy!.bevillingDocumentId" variant="outline" size="sm" class="doc-link-btn" @click="openDocument(policy!.bevillingDocumentId!)">
              <ExternalLink :size="14" /> Åpne bevillingsdokument
            </Button>
          </div>

          <Separator />

          <div class="license-card-body">
            <h4 class="license-sub-title">Kunnskapsprøve</h4>
            <div class="license-grid">
              <div class="license-field"><span class="license-label">Kandidat</span><span class="license-value">{{ policy!.kunnskapsproveCandidateName ?? '—' }}</span></div>
              <div class="license-field"><span class="license-label">Type</span><span class="license-value">{{ policy!.kunnskapsproveType ? knowledgeTestLabel[policy!.kunnskapsproveType] : '—' }}</span></div>
              <div class="license-field"><span class="license-label">Kommune</span><span class="license-value">{{ policy!.kunnskapsproveMunicipality ?? '—' }}</span></div>
              <div class="license-field"><span class="license-label">Bestått dato</span><span class="license-value">{{ formatDate(policy!.kunnskapsprovePassedDate) }}</span></div>
            </div>
            <Button v-if="policy!.kunnskapsproveDocumentId" variant="outline" size="sm" class="doc-link-btn" @click="openDocument(policy!.kunnskapsproveDocumentId!)">
              <ExternalLink :size="14" /> Åpne kunnskapsprøve-bevis
            </Button>
          </div>

          <Separator />

          <div class="license-card-body">
            <h4 class="license-sub-title">Rutiner for alderskontroll</h4>
            <div class="license-grid">
              <div class="license-field"><span class="license-label">Aldersgrense</span><span class="license-value">{{ ageCheckLabel[policy!.ageCheckLimit] }}</span></div>
              <div class="license-field"><span class="license-label">Godkjent legitimasjon</span><span class="license-value">{{ policy!.acceptedIdTypes.map(t => idTypeLabel[t]).join(', ') }}</span></div>
            </div>
            <div v-if="policy!.doubtRoutine" class="license-text-block">
              <span class="license-label">Rutine ved tvil</span>
              <p class="license-value">{{ policy!.doubtRoutine }}</p>
            </div>
          </div>

          <Separator />

          <div class="license-card-body">
            <h4 class="license-sub-title">Ansvarlig servering</h4>
            <div v-if="policy!.intoxicationSigns" class="license-text-block">
              <span class="license-label">Identifisering av berusede gjester</span>
              <p class="license-value">{{ policy!.intoxicationSigns }}</p>
            </div>
            <div v-if="policy!.refusalProcedure" class="license-text-block">
              <span class="license-label">Prosedyre ved nekt</span>
              <p class="license-value">{{ policy!.refusalProcedure }}</p>
            </div>
          </div>
        </div>
      </template>

      <!-- Policy exists but full data loading -->
      <p v-else-if="policyExists && policyQuery.isLoading.value" class="state-line">Laster skjenkepolicy...</p>
    </div>
  </AppLayout>
</template>

<style scoped>
.page-header { display: flex; height: 4rem; flex-shrink: 0; align-items: center; }
.page-header-inner { display: flex; align-items: center; gap: 0.5rem; padding: 0 1rem; }
.header-separator { height: 1rem !important; width: 1px !important; margin-right: 0.5rem; }
.page-title { font-weight: 500; color: hsl(var(--sidebar-primary, 245 43% 52%)); }
.page-content { display: flex; flex: 1; flex-direction: column; gap: 1rem; padding: 0 1rem 1rem; align-items: center; }

.state-line {
  border-radius: var(--radius-md);
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  padding: 12px;
  color: var(--text-secondary);
  width: 100%;
  max-width: 48rem;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  width: 100%;
  max-width: 48rem;
}
.header-row h1 { margin: 0; font-size: 2.4rem; letter-spacing: -0.02em; }
.header-row p { margin-top: 6px; color: var(--text-secondary); font-size: 1.08rem; }

/* ═══ FORM ═══ */
.form-container { width: 100%; max-width: 48rem; }
.form-header h1 { margin: 0; font-size: 2rem; letter-spacing: -0.02em; }
.form-subtitle { margin-top: 0.5rem; color: var(--text-secondary); font-size: 0.95rem; line-height: 1.5; }
.form-section { border: none; padding: 0; margin: 2rem 0 0; }
.section-title { font-size: 1.2rem; font-weight: 600; letter-spacing: -0.01em; }
.section-subtitle { color: var(--text-secondary); font-size: 0.875rem; margin-top: 0.25rem; margin-bottom: 0.75rem; }

.field-row { display: flex; gap: 1rem; margin-top: 0.75rem; }
.field-row.two-col > .field { flex: 1; }
.field { display: flex; flex-direction: column; gap: 0.375rem; margin-top: 0.75rem; }
.field-label { font-size: 0.85rem; font-weight: 500; color: var(--text-secondary); }

.field-input {
  width: 100%;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  padding: 0.6rem 0.75rem;
  font: inherit;
  font-size: 0.9rem;
  background: hsl(var(--card));
  color: hsl(var(--foreground));
}
.field-input:focus { outline: none; border-color: hsl(var(--ring)); box-shadow: 0 0 0 2px hsl(var(--ring) / 0.15); }

.field-textarea {
  width: 100%;
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  padding: 0.6rem 0.75rem;
  font: inherit;
  font-size: 0.9rem;
  background: hsl(var(--card));
  color: hsl(var(--foreground));
  resize: vertical;
}
.field-textarea:focus { outline: none; border-color: hsl(var(--ring)); box-shadow: 0 0 0 2px hsl(var(--ring) / 0.15); }

/* Drop zone */
.drop-zone {
  border: 2px dashed hsl(var(--border));
  border-radius: 0.5rem;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: border-color 150ms, background 150ms;
}
.drop-zone:hover { border-color: hsl(var(--ring)); background: hsl(var(--accent) / 0.3); }

.drop-zone-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
  pointer-events: none;
}
.drop-zone-icon { color: hsl(var(--muted-foreground)); }

.file-hidden { position: absolute; width: 1px; height: 1px; overflow: hidden; clip: rect(0, 0, 0, 0); }

.file-chip {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid hsl(var(--border));
  background: hsl(var(--card));
  font-size: 0.85rem;
}
.file-chip-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.file-chip-status { font-size: 0.78rem; color: var(--text-secondary); flex-shrink: 0; }
.file-chip-spinner { animation: spin 1s linear infinite; flex-shrink: 0; color: hsl(var(--primary)); }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.file-chip-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: hsl(var(--muted-foreground));
  padding: 2px;
  border-radius: 4px;
  flex-shrink: 0;
}
.file-chip-remove:hover { background: hsl(var(--accent)); color: hsl(var(--foreground)); }

/* Toggle group */
.toggle-group { display: flex; gap: 0.75rem; }
.toggle-btn {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 0.15rem;
  padding: 0.75rem 1rem; border: 2px solid hsl(var(--border)); border-radius: 0.5rem;
  background: hsl(var(--card)); font: inherit; font-size: 0.9rem; cursor: pointer;
  transition: border-color 150ms, background 150ms;
}
.toggle-btn:hover { border-color: hsl(var(--ring) / 0.5); }
.toggle-btn--active { border-color: hsl(var(--ring)); background: hsl(var(--accent) / 0.5); }
.toggle-rec { font-size: 0.75rem; color: var(--green); font-weight: 500; }

/* Chip group */
.chip-group { display: flex; flex-wrap: wrap; gap: 0.5rem; }
.chip-btn {
  padding: 0.4rem 0.85rem; border: 1.5px solid hsl(var(--border)); border-radius: var(--radius-pill);
  background: hsl(var(--card)); font: inherit; font-size: 0.85rem; cursor: pointer;
  transition: border-color 150ms, background 150ms, color 150ms;
}
.chip-btn:hover { border-color: hsl(var(--ring) / 0.5); }
.chip-btn--active { border-color: hsl(var(--ring)); background: hsl(var(--accent)); color: hsl(var(--accent-foreground)); font-weight: 500; }

/* Form actions */
.form-actions { display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid hsl(var(--border)); }

/* ═══ LICENSE CARD ═══ */
.license-card { border: 1px solid hsl(var(--border)); border-radius: var(--radius-xl); background: hsl(var(--card)); overflow: hidden; width: 100%; max-width: 48rem; }
.license-card-header { display: flex; align-items: center; justify-content: space-between; padding: 1.25rem 1.5rem; background: linear-gradient(135deg, hsl(var(--primary) / 0.04), hsl(var(--primary) / 0.08)); border-bottom: 1px solid hsl(var(--border)); }
.license-card-header-left { display: flex; align-items: center; gap: 0.75rem; }
.license-icon { display: flex; align-items: center; justify-content: center; width: 2.5rem; height: 2.5rem; border-radius: 0.625rem; background: hsl(var(--primary) / 0.1); color: hsl(var(--primary)); }
.license-card-title { font-size: 1.1rem; font-weight: 600; margin: 0; }
.license-card-sub { font-size: 0.8rem; color: var(--text-secondary); margin: 0; }
.license-card-body { padding: 1.25rem 1.5rem; }
.license-sub-title { font-size: 1rem; font-weight: 600; margin: 0 0 0.75rem; }
.license-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.license-field { display: flex; flex-direction: column; gap: 0.2rem; }
.license-label { font-size: 0.78rem; font-weight: 500; color: var(--text-secondary); text-transform: uppercase; letter-spacing: 0.03em; }
.license-value { font-size: 0.95rem; font-weight: 500; margin: 0; }
.license-text-block { margin-top: 1rem; }
.license-text-block .license-value { margin-top: 0.25rem; font-weight: 400; line-height: 1.5; color: var(--text-secondary); }
.doc-link-btn { margin-top: 1rem; }

.actions-trigger {
  display: flex; align-items: center; justify-content: center; width: 2rem; height: 2rem;
  border-radius: var(--radius-md); border: none; background: none; color: hsl(var(--muted-foreground));
  cursor: pointer; transition: background 150ms ease, color 150ms ease;
}
.actions-trigger:hover { background: hsl(var(--accent)); color: hsl(var(--foreground)); }

@media (max-width: 768px) {
  .field-row { flex-direction: column; }
  .license-grid { grid-template-columns: 1fr; }
  .toggle-group { flex-direction: column; }
}
</style>

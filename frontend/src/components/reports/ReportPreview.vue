<script setup lang="ts">
import type {ReportPreviewResponse} from '@/types/report'

defineProps<{
  data: ReportPreviewResponse
}>()

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
  <div class="pv-scroll-wrapper">
    <div class="pv">
      <div class="pv-header">
        <div class="pv-header-left">
          <span class="pv-header-label">INTERNKONTROLL RAPPORT</span>
          <h2 class="pv-header-org">{{ data.header.organizationName }}</h2>
          <span v-if="data.header.orgNumber"
                class="pv-header-orgnr">Org.nr: {{ data.header.orgNumber }}</span>
        </div>
        <div class="pv-header-right">
          <span>Periode: <strong>{{ data.header.periodFrom }} – {{ data.header.periodTo }}</strong></span>
          <span>Generert: {{ data.header.generatedDate }}</span>
          <span>Av: {{ data.header.generatedByName }}<template v-if="data.header.generatedByRole"> ({{
              data.header.generatedByRole
            }})</template></span>
        </div>
      </div>

      <div v-if="data.complianceSummary" class="pv-section">
        <h3 class="pv-section-title">SAMSVARSSAMMENDRAG</h3>
        <div class="pv-kpi-row">
          <div class="pv-kpi-card pv-kpi-card--green">
            <span class="pv-kpi-label">SAMSVARSRATE</span>
            <span class="pv-kpi-value pv-kpi-value--green">{{
                data.complianceSummary.complianceRate.toFixed(1)
              }}%</span>
          </div>
          <div class="pv-kpi-card pv-kpi-card--olive">
            <span class="pv-kpi-label">FORFALTE OPPGAVER</span>
            <span class="pv-kpi-value pv-kpi-value--olive">{{
                data.complianceSummary.overdueTasks
              }}</span>
          </div>
          <div class="pv-kpi-card pv-kpi-card--gold">
            <span class="pv-kpi-label">ÅPNE AVVIK</span>
            <span class="pv-kpi-value pv-kpi-value--gold">{{
                data.complianceSummary.openDeviations
              }}</span>
          </div>
          <div class="pv-kpi-card pv-kpi-card--red">
            <span class="pv-kpi-label">TEMP.AVVIK</span>
            <span class="pv-kpi-value pv-kpi-value--red">{{
                data.complianceSummary.temperatureDeviations
              }}</span>
          </div>
        </div>
        <div class="pv-kpi-bottom">
          <div class="pv-kpi-stat">
            <span>Totalt oppgaver</span><strong>{{ data.complianceSummary.totalTasks }}</strong>
          </div>
          <div class="pv-kpi-stat">
            <span>Lukkede avvik</span><strong>{{ data.complianceSummary.closedDeviations }}</strong>
          </div>
          <div class="pv-kpi-stat"><span>Alkoholhendelser</span><strong>{{
              data.complianceSummary.alcoholIncidents
            }}</strong></div>
        </div>
      </div>

      <div v-if="data.temperatureLogs && data.temperatureLogs.length > 0" class="pv-section">
        <div class="pv-module-badge">IK-MAT — MATSAMSVAR</div>
        <h3 class="pv-section-title">TEMPERATURLOGG</h3>
        <table class="pv-table">
          <thead>
          <tr>
            <th>PLASSERING</th>
            <th>SNI °C</th>
            <th>MIN °C</th>
            <th>MAKS °C</th>
            <th>AVVIK</th>
            <th>STATUS</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(t, i) in data.temperatureLogs" :key="i">
            <td class="pv-td-bold">{{ t.location }}</td>
            <td>{{ t.avgTemp.toFixed(1) }}</td>
            <td>{{ t.minTemp.toFixed(1) }}</td>
            <td>{{ t.maxTemp.toFixed(1) }}</td>
            <td>{{ t.deviationCount }}</td>
            <td><span :class="['pv-badge', getStatusClass(t.status)]">{{
                t.status.toUpperCase()
              }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.checklists && data.checklists.length > 0" class="pv-section">
        <h3 class="pv-section-title">SJEKKLISTE-FULLFØRING</h3>
        <div v-for="c in data.checklists" :key="c.id" class="pv-progress-row">
          <span class="pv-progress-name">{{ c.name }}</span>
          <div class="pv-progress-bar">
            <div class="pv-progress-fill"
                 :class="c.completionRate >= 100 ? 'pv-fill--green' : 'pv-fill--gold'"
                 :style="{ width: Math.min(c.completionRate, 100) + '%' }"></div>
          </div>
          <span class="pv-progress-pct"
                :class="c.completionRate >= 100 ? 'pv-pct--green' : 'pv-pct--gold'">{{
              c.completionRate.toFixed(0)
            }}% ({{ c.completedCount }}/{{ c.expectedCount }})</span>
        </div>
      </div>

      <div v-if="data.haccpChecklists && data.haccpChecklists.checklists.length > 0"
           class="pv-section">
        <h3 class="pv-section-title">HACCP-SJEKKLISTER</h3>
        <div v-for="(hc, hi) in data.haccpChecklists.checklists" :key="hi" class="pv-haccp-group">
          <div class="pv-haccp-header">
            <strong>{{ hc.name }}</strong> <span class="pv-haccp-freq">({{ hc.frequency }})</span>
            <span class="pv-haccp-pct">{{ hc.completionRate.toFixed(0) }}%</span>
          </div>
          <div class="pv-progress-bar pv-progress-bar--sm">
            <div class="pv-progress-fill"
                 :class="hc.completionRate >= 100 ? 'pv-fill--green' : 'pv-fill--gold'"
                 :style="{ width: Math.min(hc.completionRate, 100) + '%' }"></div>
          </div>
        </div>
      </div>

      <div v-if="data.correctiveActions && data.correctiveActions.length > 0" class="pv-section">
        <h3 class="pv-section-title">KORRIGERENDE TILTAK</h3>
        <table class="pv-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>DATO</th>
            <th>PROBLEM</th>
            <th>TILTAK</th>
            <th>STATUS</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="ca in data.correctiveActions" :key="ca.id">
            <td class="pv-td-bold">{{ ca.id }}</td>
            <td>{{ ca.date }}</td>
            <td>{{ ca.issue }}</td>
            <td>{{ ca.actionTaken ?? '—' }}</td>
            <td><span :class="['pv-badge', getStatusClass(ca.status)]">{{
                ca.status.toUpperCase()
              }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.foodDeviations && data.foodDeviations.length > 0" class="pv-section">
        <h3 class="pv-section-title">MATAVVIK</h3>
        <table class="pv-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>DATO</th>
            <th>TYPE</th>
            <th>ALVORLIGHET</th>
            <th>BESKRIVELSE</th>
            <th>STATUS</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="fd in data.foodDeviations" :key="fd.id">
            <td class="pv-td-bold">{{ fd.id }}</td>
            <td>{{ fd.date }}</td>
            <td>{{ fd.type }}</td>
            <td>{{ fd.severity }}</td>
            <td>{{ fd.description }}</td>
            <td><span :class="['pv-badge', getStatusClass(fd.status)]">{{
                fd.status.toUpperCase()
              }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.alcoholDeviations && data.alcoholDeviations.length > 0" class="pv-section">
        <div class="pv-module-badge">IK-ALKOHOL — ALKOHOLSAMSVAR</div>
        <h3 class="pv-section-title">HENDELSESRAPPORTER</h3>
        <table class="pv-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>DATO</th>
            <th>BESKRIVELSE</th>
            <th>TILTAK</th>
            <th>LØST</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="ad in data.alcoholDeviations" :key="ad.id">
            <td class="pv-td-bold">INC-{{ String(ad.id).padStart(3, '0') }}</td>
            <td>{{ ad.date }}</td>
            <td>{{ ad.description }}</td>
            <td>{{ ad.immediateAction ?? '—' }}</td>
            <td><span
              :class="['pv-badge', ad.status === 'Lukket' ? 'badge-green' : 'badge-yellow']">{{
                ad.status === 'Lukket' ? 'JA' : 'NEI'
              }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.ageVerificationLogs && data.ageVerificationLogs.length > 0"
           class="pv-section">
        <h3 class="pv-section-title">ALDERSKONTROLL</h3>
        <table class="pv-table">
          <thead>
          <tr>
            <th>DATO</th>
            <th>SKIFT</th>
            <th>TOTALT SJEKKET</th>
            <th>ID FORESPURT</th>
            <th>AVVISNINGER</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(av, i) in data.ageVerificationLogs" :key="i">
            <td>{{ av.date }}</td>
            <td>{{ av.shift }}</td>
            <td>{{ av.totalChecks }}</td>
            <td>{{ av.idRequested }}</td>
            <td :class="getRefusalClass(av.refusals)">{{ av.refusals }}</td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.trainingOverview && data.trainingOverview.length > 0" class="pv-section">
        <h3 class="pv-section-title">OPPLÆRINGSOVERSIKT</h3>
        <table class="pv-table">
          <thead>
          <tr>
            <th>NAVN</th>
            <th>ROLLE</th>
            <th>SERTIFISERING</th>
            <th>UTLØPER</th>
            <th>STATUS</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(t, i) in data.trainingOverview" :key="i">
            <td class="pv-td-bold">{{ t.name }}</td>
            <td>{{ t.role ?? '—' }}</td>
            <td>{{ t.certification }}</td>
            <td>{{ t.expires ?? '—' }}</td>
            <td><span :class="['pv-badge', getStatusClass(t.status)]">{{
                t.status.toUpperCase()
              }}</span></td>
          </tr>
          </tbody>
        </table>
      </div>

      <div v-if="data.licenseInfo" class="pv-section">
        <h3 class="pv-section-title">LISENSINFORMASJON</h3>
        <div
          v-if="data.licenseInfo.bevillingNumber || data.licenseInfo.holder || data.licenseInfo.styrerName"
          class="pv-license-card" style="margin-bottom: 0.75rem;">
          <div class="pv-license-subtitle">SKJENKEBEVILLING</div>
          <div class="pv-license-grid">
            <div v-if="data.licenseInfo.bevillingNumber" class="pv-license-item">
              <span class="pv-license-label">BEVILLINGSNUMMER</span>
              <strong>{{ data.licenseInfo.bevillingNumber }}</strong>
            </div>
            <div v-if="data.licenseInfo.bevillingValidTo" class="pv-license-item">
              <span class="pv-license-label">GYLDIG TIL</span>
              <strong>{{ data.licenseInfo.bevillingValidTo }}</strong>
            </div>
            <div v-if="data.licenseInfo.holder" class="pv-license-item">
              <span class="pv-license-label">BEVILLINGSHAVER</span>
              <strong>{{ data.licenseInfo.holder }}</strong>
            </div>
            <div v-if="data.licenseInfo.styrerName" class="pv-license-item">
              <span class="pv-license-label">STYRER</span>
              <strong>{{ data.licenseInfo.styrerName }}</strong>
            </div>
            <div v-if="data.licenseInfo.stedfortrederName" class="pv-license-item">
              <span class="pv-license-label">STEDFORTREDER</span>
              <strong>{{ data.licenseInfo.stedfortrederName }}</strong>
            </div>
          </div>
        </div>
        <div
          v-if="data.licenseInfo.kunnskapsproveType || data.licenseInfo.kunnskapsproveCandidateName"
          class="pv-license-card">
          <div class="pv-license-subtitle">KUNNSKAPSPRØVE</div>
          <div class="pv-license-grid">
            <div v-if="data.licenseInfo.kunnskapsproveType" class="pv-license-item">
              <span class="pv-license-label">TYPE</span>
              <strong>{{ data.licenseInfo.kunnskapsproveType }}</strong>
            </div>
            <div v-if="data.licenseInfo.kunnskapsproveCandidateName" class="pv-license-item">
              <span class="pv-license-label">KANDIDAT</span>
              <strong>{{ data.licenseInfo.kunnskapsproveCandidateName }}</strong>
            </div>
            <div v-if="data.licenseInfo.kunnskapsprovePassedDate" class="pv-license-item">
              <span class="pv-license-label">BESTÅTT DATO</span>
              <strong>{{ data.licenseInfo.kunnskapsprovePassedDate }}</strong>
            </div>
            <div v-if="data.licenseInfo.kunnskapsproveMunicipality" class="pv-license-item">
              <span class="pv-license-label">KOMMUNE</span>
              <strong>{{ data.licenseInfo.kunnskapsproveMunicipality }}</strong>
            </div>
            <div v-if="data.licenseInfo.kunnskapsproveBirthDate" class="pv-license-item">
              <span class="pv-license-label">FØDSELSDATO</span>
              <strong>{{ data.licenseInfo.kunnskapsproveBirthDate }}</strong>
            </div>
          </div>
        </div>
      </div>

      <div v-if="data.signOff" class="pv-section">
        <h3 class="pv-section-title">SIGNERING</h3>
        <div class="pv-signoff">
          <div class="pv-signoff-row">
            <div class="pv-signoff-col">
              <span class="pv-signoff-label">LEDERS SIGNATUR</span>
              <div class="pv-signoff-name">{{ data.signOff.name }}</div>
              <div class="pv-signoff-line"></div>
              <span v-if="data.signOff.title" class="pv-signoff-role">{{ data.signOff.name }} &#8212; {{
                  data.signOff.title
                }}</span>
            </div>
            <div class="pv-signoff-col">
              <span class="pv-signoff-label">DATO</span>
              <div class="pv-signoff-date">{{ data.signOff.date }}</div>
              <div class="pv-signoff-line"></div>
            </div>
          </div>
          <div v-if="data.signOff.comments" class="pv-signoff-comments">
            <span class="pv-signoff-label">KOMMENTARER</span>
            <p>{{ data.signOff.comments }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pv-scroll-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.pv {
  display: flex;
  flex-direction: column;
  gap: 1.75rem;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
  color: hsl(var(--foreground));
  min-width: 640px;
}

.pv-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 1rem;
  border-bottom: 3px solid hsl(var(--foreground));
}

.pv-header-left {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.pv-header-label {
  font-size: 0.7rem;
  font-weight: 600;
  letter-spacing: 0.12em;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
}

.pv-header-org {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 800;
  color: hsl(var(--foreground));
  letter-spacing: -0.02em;
}

.pv-header-orgnr {
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
}

.pv-header-right {
  text-align: right;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  font-size: 0.82rem;
  color: hsl(var(--muted-foreground));
}

.pv-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.pv-section-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 800;
  color: hsl(var(--foreground));
  letter-spacing: 0.02em;
  text-transform: uppercase;
  padding-bottom: 0.4rem;
  border-bottom: 3px solid hsl(var(--foreground));
}

.pv-module-badge {
  display: inline-block;
  background: hsl(var(--foreground));
  color: hsl(var(--background));
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  padding: 0.3rem 0.75rem;
  border-radius: 0.25rem;
  text-transform: uppercase;
  align-self: flex-start;
}

.pv-kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.65rem;
}

.pv-kpi-card {
  background: hsl(var(--muted));
  border-radius: 0.5rem;
  padding: 0.85rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  border-left: 4px solid transparent;
}

.pv-kpi-card--green {
  border-left-color: var(--green);
}

.pv-kpi-card--olive {
  border-left-color: var(--amber);
}

.pv-kpi-card--gold {
  border-left-color: var(--amber);
}

.pv-kpi-card--red {
  border-left-color: var(--red);
}

.pv-kpi-label {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
}

.pv-kpi-value {
  font-size: 1.75rem;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

.pv-kpi-value--green {
  color: var(--green);
}

.pv-kpi-value--olive {
  color: var(--amber);
}

.pv-kpi-value--gold {
  color: var(--amber);
}

.pv-kpi-value--red {
  color: var(--red);
}

.pv-kpi-bottom {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}

.pv-kpi-stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: hsl(var(--muted));
  border-radius: 0.35rem;
  padding: 0.5rem 0.75rem;
  font-size: 0.82rem;
}

.pv-kpi-stat span {
  color: hsl(var(--muted-foreground));
}

.pv-kpi-stat strong {
  font-size: 1rem;
  color: hsl(var(--foreground));
}

.pv-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

.pv-table thead th {
  text-align: left;
  padding: 0.55rem 0.65rem;
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
  border-bottom: 3px solid hsl(var(--foreground));
}

.pv-table tbody td {
  padding: 0.6rem 0.65rem;
  border-bottom: 1px solid hsl(var(--border));
  vertical-align: top;
}

.pv-table tbody tr:last-child td {
  border-bottom: none;
}

.pv-td-bold {
  font-weight: 600;
}

.pv-badge {
  display: inline-block;
  padding: 0.15rem 0.55rem;
  border-radius: 0.25rem;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.03em;
}

.badge-green {
  background: var(--green-soft);
  color: var(--green);
}

.badge-yellow {
  background: var(--amber-soft);
  color: var(--amber);
}

.badge-red {
  background: var(--red-soft);
  color: var(--red);
}

.badge-gray {
  background: hsl(var(--muted));
  color: hsl(var(--muted-foreground));
}

.refusal-highlight {
  color: var(--red);
  font-weight: 700;
}

.pv-progress-row {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  padding: 0.35rem 0;
}

.pv-progress-name {
  flex-shrink: 0;
  width: 10rem;
  font-size: 0.88rem;
  font-weight: 600;
  color: hsl(var(--foreground));
}

.pv-progress-bar {
  flex: 1;
  height: 0.65rem;
  background: hsl(var(--muted));
  border-radius: 0.35rem;
  overflow: hidden;
}

.pv-progress-bar--sm {
  height: 0.45rem;
  margin-top: 0.25rem;
}

.pv-progress-fill {
  height: 100%;
  border-radius: 0.35rem;
  transition: width 300ms ease;
}

.pv-fill--green {
  background: var(--green);
}

.pv-fill--gold {
  background: var(--amber);
}

.pv-progress-pct {
  flex-shrink: 0;
  width: 2.5rem;
  font-size: 0.82rem;
  font-weight: 700;
  text-align: right;
}

.pv-pct--green {
  color: var(--green);
}

.pv-pct--gold {
  color: var(--amber);
}

.pv-haccp-group {
  margin-bottom: 0.5rem;
}

.pv-haccp-header {
  font-size: 0.88rem;
}

.pv-haccp-freq {
  color: hsl(var(--muted-foreground));
  font-size: 0.78rem;
  font-weight: 400;
}

.pv-haccp-pct {
  float: right;
  font-weight: 700;
  color: var(--green);
}

.pv-license-card {
  background: hsl(var(--muted));
  border-radius: 0.5rem;
  padding: 1rem;
}

.pv-license-subtitle {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  color: hsl(var(--foreground));
  text-transform: uppercase;
  margin-bottom: 0.6rem;
}

.pv-license-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
}

.pv-license-item {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.pv-license-label {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
}

.pv-license-item strong {
  font-size: 0.92rem;
  color: hsl(var(--foreground));
}

.pv-signoff {
  border: 1px solid hsl(var(--border));
  border-radius: 0.5rem;
  padding: 1.25rem;
}

.pv-signoff-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.pv-signoff-col {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.pv-signoff-label {
  font-size: 0.65rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  color: hsl(var(--muted-foreground));
  text-transform: uppercase;
}

.pv-signoff-name {
  font-size: 1.15rem;
  font-style: italic;
  font-weight: 600;
  color: hsl(var(--foreground));
  margin-top: 0.75rem;
}

.pv-signoff-date {
  font-size: 1.05rem;
  font-weight: 600;
  color: hsl(var(--foreground));
  margin-top: 0.75rem;
  font-variant-numeric: tabular-nums;
}

.pv-signoff-line {
  border-bottom: 2px solid hsl(var(--foreground));
  margin-top: 0.25rem;
}

.pv-signoff-role {
  font-size: 0.8rem;
  color: hsl(var(--muted-foreground));
  margin-top: 0.25rem;
}

.pv-signoff-comments {
  margin-top: 1rem;
}

.pv-signoff-comments p {
  margin: 0.35rem 0 0;
  font-size: 0.88rem;
  font-style: italic;
  color: hsl(var(--muted-foreground));
  border: 1px solid hsl(var(--border));
  border-radius: 0.35rem;
  padding: 0.65rem;
}

@media (max-width: 768px) {
  .pv-header {
    flex-direction: column;
    gap: 0.5rem;
  }

  .pv-header-right {
    text-align: left;
  }

  .pv-kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }

  .pv-kpi-bottom {
    grid-template-columns: 1fr;
  }

  .pv-license-grid {
    grid-template-columns: 1fr;
  }

  .pv-signoff-row {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .pv-progress-name {
    width: 7rem;
    font-size: 0.8rem;
  }
}
</style>

import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ReportPreview from '../ReportPreview.vue'

const fullData = {
  header: {
    organizationName: 'Test Bistro',
    orgNumber: '999888777',
    periodFrom: '2026-03-01',
    periodTo: '2026-03-31',
    generatedDate: '2026-04-09',
    generatedByName: 'Ada Admin',
    generatedByRole: 'Daglig leder',
  },
  complianceSummary: {
    complianceRate: 97.5,
    overdueTasks: 2,
    openDeviations: 3,
    temperatureDeviations: 1,
    totalTasks: 120,
    closedDeviations: 8,
    alcoholIncidents: 4,
  },
  temperatureLogs: [
    {
      location: 'Kjølerom',
      avgTemp: 3.2,
      minTemp: 1.5,
      maxTemp: 7.1,
      deviationCount: 1,
      status: 'Warning',
    },
  ],
  checklists: [
    { id: 1, name: 'Daglig sjekkliste', completionRate: 100, completedCount: 10, expectedCount: 10 },
    { id: 2, name: 'Ukentlig sjekkliste', completionRate: 80, completedCount: 4, expectedCount: 5 },
  ],
  haccpChecklists: {
    checklists: [
      { name: 'Varemottak', frequency: 'Daglig', completionRate: 100 },
      { name: 'Nedkjøling', frequency: 'Ukentlig', completionRate: 75 },
    ],
  },
  correctiveActions: [
    { id: 10, date: '2026-03-10', issue: 'For høy temperatur', actionTaken: null, status: 'Resolved' },
  ],
  foodDeviations: [
    { id: 20, date: '2026-03-11', type: 'Temperatur', severity: 'Høy', description: 'Avvik i kjølerom', status: 'Expired' },
  ],
  alcoholDeviations: [
    { id: 7, date: '2026-03-12', description: 'Mangler legitimasjon', immediateAction: null, status: 'Lukket' },
    { id: 8, date: '2026-03-13', description: 'Uro i lokalet', immediateAction: 'Vakthold', status: 'Åpen' },
  ],
  ageVerificationLogs: [
    { date: '2026-03-14', shift: 'Kveld', totalChecks: 20, idRequested: 12, refusals: 2 },
    { date: '2026-03-15', shift: 'Dag', totalChecks: 10, idRequested: 3, refusals: 0 },
  ],
  trainingOverview: [
    { name: 'Per', role: null, certification: 'Ansvarlig vertskap', expires: null, status: 'Gyldig' },
    { name: 'Kari', role: 'Bartender', certification: 'Kunnskapsprøve', expires: '2026-06-01', status: 'Utløper snart' },
  ],
  licenseInfo: {
    bevillingNumber: 'BEV-123',
    bevillingValidTo: '2026-12-31',
    holder: 'Test Bistro AS',
    styrerName: 'Ada Admin',
    stedfortrederName: 'Bob Backup',
    kunnskapsproveType: 'Skjenkebevilling',
    kunnskapsproveCandidateName: 'Ada Admin',
    kunnskapsprovePassedDate: '2024-05-02',
    kunnskapsproveMunicipality: 'Trondheim',
    kunnskapsproveBirthDate: '1998-01-01',
  },
  signOff: {
    name: 'Ada Admin',
    title: 'Daglig leder',
    date: '2026-04-09',
    comments: 'Godkjent uten merknader.',
  },
}

describe('ReportPreview', () => {
  it('renders the full report and applies the correct visual status classes', () => {
    const wrapper = mount(ReportPreview, {
      props: {
        data: fullData,
      },
    })

    expect(wrapper.text()).toContain('Test Bistro')
    expect(wrapper.text()).toContain('Org.nr: 999888777')
    expect(wrapper.text()).toContain('97.5%')
    expect(wrapper.text()).toContain('INC-007')
    expect(wrapper.text()).toContain('JA')
    expect(wrapper.text()).toContain('NEI')
    expect(wrapper.text()).toContain('Godkjent uten merknader.')

    expect(wrapper.find('.badge-yellow').exists()).toBe(true)
    expect(wrapper.find('.badge-green').exists()).toBe(true)
    expect(wrapper.find('.badge-red').exists()).toBe(true)
    expect(wrapper.find('.refusal-highlight').text()).toBe('2')

    const progressFills = wrapper.findAll('.pv-progress-fill')
    expect(progressFills[0].classes()).toContain('pv-fill--green')
    expect(progressFills[1].classes()).toContain('pv-fill--gold')

    const progressText = wrapper.text()
    expect(progressText).toContain('100% (10/10)')
    expect(progressText).toContain('80% (4/5)')
    expect(progressText).toContain('—')
  })

  it('hides optional sections when the corresponding data is missing or empty', () => {
    const wrapper = mount(ReportPreview, {
      props: {
        data: {
          header: {
            organizationName: 'Minimal Cafe',
            orgNumber: '',
            periodFrom: '2026-04-01',
            periodTo: '2026-04-09',
            generatedDate: '2026-04-09',
            generatedByName: 'System',
            generatedByRole: '',
          },
          complianceSummary: null,
          temperatureLogs: [],
          checklists: [],
          haccpChecklists: { checklists: [] },
          correctiveActions: [],
          foodDeviations: [],
          alcoholDeviations: [],
          ageVerificationLogs: [],
          trainingOverview: [],
          licenseInfo: null,
          signOff: null,
        },
      },
    })

    expect(wrapper.text()).toContain('Minimal Cafe')
    expect(wrapper.text()).not.toContain('SAMSVARSSAMMENDRAG')
    expect(wrapper.text()).not.toContain('TEMPERATURLOGG')
    expect(wrapper.text()).not.toContain('SJEKKLISTE-FULLFØRING')
    expect(wrapper.text()).not.toContain('HACCP-SJEKKLISTER')
    expect(wrapper.text()).not.toContain('KORRIGERENDE TILTAK')
    expect(wrapper.text()).not.toContain('MATAVVIK')
    expect(wrapper.text()).not.toContain('HENDELSESRAPPORTER')
    expect(wrapper.text()).not.toContain('ALDERSKONTROLL')
    expect(wrapper.text()).not.toContain('OPPLÆRINGSOVERSIKT')
    expect(wrapper.text()).not.toContain('LISENSINFORMASJON')
    expect(wrapper.text()).not.toContain('SIGNERING')
  })
})

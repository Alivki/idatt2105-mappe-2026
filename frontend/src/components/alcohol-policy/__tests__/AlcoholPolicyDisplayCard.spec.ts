import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import AlcoholPolicyView from '../AlcoholPolicyDisplayCard.vue'

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })
  return {
    ScrollText: icon('ScrollText'),
    Award: icon('Award'),
    Pencil: icon('Pencil'),
    ExternalLink: icon('ExternalLink'),
    CalendarDays: icon('CalendarDays'),
    User: icon('User'),
    MapPin: icon('MapPin'),
    Hash: icon('Hash'),
    ShieldCheck: icon('ShieldCheck'),
    ScanEye: icon('ScanEye'),
    HandMetal: icon('HandMetal'),
    CircleHelp: icon('CircleHelp'),
    CreditCard: icon('CreditCard'),
  }
})

vi.mock('@/components/ui/separator', () => ({
  Separator: defineComponent({
    name: 'Separator',
    setup() {
      return () => h('hr')
    },
  }),
}))

vi.mock('@/components/ui/badge/Badge.vue', () => ({
  default: defineComponent({
    name: 'Badge',
    props: { tone: { type: String, required: false } },
    setup(_, { slots }) {
      return () => h('span', { class: 'badge-stub' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    emits: ['click'],
    props: {
      type: { type: String, default: 'button' },
      disabled: { type: Boolean, default: false },
    },
    setup(props, { slots, emit, attrs }) {
      return () =>
        h(
          'button',
          {
            ...attrs,
            type: props.type,
            disabled: props.disabled,
            onClick: (e: MouseEvent) => emit('click', e),
          },
          slots.default?.(),
        )
    },
  }),
}))

describe('AlcoholPolicyView', () => {
  const basePolicy = {
    bevillingNumber: '2024/12345',
    bevillingValidUntil: '2099-12-31',
    styrerName: 'Ola Nordmann',
    stedfortrederName: 'Kari Nordmann',
    bevillingDocumentId: 10,
    kunnskapsproveCandidateName: 'Test Person',
    kunnskapsproveBirthDate: '1990-01-01',
    kunnskapsproveType: 'BOTH',
    kunnskapsproveMunicipality: 'Oslo',
    kunnskapsprovePassedDate: '2024-01-10',
    kunnskapsproveDocumentId: 20,
    ageCheckLimit: 'UNDER_25',
    acceptedIdTypes: ['PASS', 'BANKKORT'],
    doubtRoutine: 'Be alltid om legitimasjon ved tvil.',
    intoxicationSigns: 'Se etter ustø gange og sløret tale.',
    refusalProcedure: 'Avslå servering rolig og tilkall leder ved behov.',
  }

  it('renders policy details and emits edit/open-document actions', async () => {
    const wrapper = mount(AlcoholPolicyView, {
      props: {
        policy: basePolicy,
      },
    })

    expect(wrapper.text()).toContain('Skjenkepolicy')
    expect(wrapper.text()).toContain('2024/12345')
    expect(wrapper.text()).toContain('Ola Nordmann')
    expect(wrapper.text()).toContain('Kari Nordmann')
    expect(wrapper.text()).toContain('Salg og skjenke')
    expect(wrapper.text()).toContain('Alle under 25')
    expect(wrapper.text()).toContain('Pass')
    expect(wrapper.text()).toContain('Bankkort m/bilde')
    expect(wrapper.text()).toContain('Be alltid om legitimasjon ved tvil.')

    const buttons = wrapper.findAll('button')
    await buttons.find((b) => b.text().includes('Rediger'))!.trigger('click')
    await buttons.find((b) => b.text().includes('Åpne dokument'))!.trigger('click')
    await buttons.find((b) => b.text().includes('Åpne bevis'))!.trigger('click')

    expect(wrapper.emitted('edit')).toHaveLength(1)
    expect(wrapper.emitted('open-document')).toEqual([[10], [20]])
  })

  it('shows warning banner and update action when bevilling expires soon', async () => {
    const soon = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()
    const wrapper = mount(AlcoholPolicyView, {
      props: {
        policy: {
          ...basePolicy,
          bevillingValidUntil: soon,
          bevillingDocumentId: null,
          kunnskapsproveDocumentId: null,
        },
      },
    })

    expect(wrapper.text()).toContain('Bevillingen utløper snart')

    const updateBtn = wrapper.findAll('button').find((b) => b.text().includes('Oppdater'))
    expect(updateBtn).toBeTruthy()
    await updateBtn!.trigger('click')

    expect(wrapper.emitted('edit')).toHaveLength(1)
  })
})

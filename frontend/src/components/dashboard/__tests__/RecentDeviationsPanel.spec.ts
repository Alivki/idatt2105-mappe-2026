import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import RecentDeviationsPanel from '../LatestDeviationCard.vue'

vi.mock('@/components/ui/badge/Badge.vue', () => ({
  default: defineComponent({
    name: 'BadgeStub',
    props: {
      tone: {
        type: String,
        default: '',
      },
    },
    setup(props, { slots }) {
      return () => h('span', { 'data-testid': 'badge', 'data-tone': props.tone }, slots.default?.())
    },
  }),
}))

describe('RecentDeviationsPanel', () => {
  it('renders empty state when there are no deviations', () => {
    const wrapper = mount(RecentDeviationsPanel, {
      props: {
        deviations: [],
      },
    })

    expect(wrapper.get('h2').text()).toBe('Siste avvik')
    expect(wrapper.get('.empty-state').text()).toContain('Ingen registrerte avvik ennå.')
    expect(wrapper.findAll('.entry')).toHaveLength(0)
  })

  it('renders deviation content with low severity mapping', () => {
    const wrapper = mount(RecentDeviationsPanel, {
      props: {
        deviations: [
          {
            id: 1,
            title: 'Manglende signatur',
            moduleLabel: 'Renhold',
            reportedBy: 'Ola',
            relativeTime: 'for 2 timer siden',
            severityLabel: 'Lav',
          },
        ],
      },
    })

    const entry = wrapper.get('.entry')
    const badge = wrapper.get('[data-testid="badge"]')

    expect(entry.classes()).toContain('entry--low')
    expect(entry.text()).toContain('Manglende signatur')
    expect(entry.text()).toContain('Renhold · Rapportert av Ola · for 2 timer siden')
    expect(badge.text()).toBe('Lav')
    expect(badge.attributes('data-tone')).toBe('ok')
    expect(wrapper.find('.empty-state').exists()).toBe(false)
  })

  it('maps medium severity to warning and medium rail class', () => {
    const wrapper = mount(RecentDeviationsPanel, {
      props: {
        deviations: [
          {
            id: 2,
            title: 'Temperatur nær grense',
            moduleLabel: 'Kjøl',
            reportedBy: 'Kari',
            relativeTime: 'i går',
            severityLabel: 'Middels',
          },
        ],
      },
    })

    expect(wrapper.get('.entry').classes()).toContain('entry--medium')
    expect(wrapper.get('[data-testid="badge"]').attributes('data-tone')).toBe('warning')
  })

  it('maps high severity to warning and high rail class', () => {
    const wrapper = mount(RecentDeviationsPanel, {
      props: {
        deviations: [
          {
            id: 3,
            title: 'Feil lagringstemperatur',
            moduleLabel: 'Fryser',
            reportedBy: 'Nora',
            relativeTime: 'nå nettopp',
            severityLabel: 'Høy',
          },
        ],
      },
    })

    expect(wrapper.get('.entry').classes()).toContain('entry--high')
    expect(wrapper.get('[data-testid="badge"]').attributes('data-tone')).toBe('warning')
  })

  it('maps critical severity to danger and critical rail class', () => {
    const wrapper = mount(RecentDeviationsPanel, {
      props: {
        deviations: [
          {
            id: 4,
            title: 'Kritisk avvik',
            moduleLabel: 'HMS',
            reportedBy: 'Admin',
            relativeTime: 'for 5 min siden',
            severityLabel: 'Kritisk',
          },
        ],
      },
    })

    expect(wrapper.get('.entry').classes()).toContain('entry--critical')
    expect(wrapper.get('[data-testid="badge"]').attributes('data-tone')).toBe('danger')
  })

  it('renders multiple deviations in order', () => {
    const wrapper = mount(RecentDeviationsPanel, {
      props: {
        deviations: [
          {
            id: 1,
            title: 'Første',
            moduleLabel: 'Modul A',
            reportedBy: 'A',
            relativeTime: 'nå',
            severityLabel: 'Lav',
          },
          {
            id: 2,
            title: 'Andre',
            moduleLabel: 'Modul B',
            reportedBy: 'B',
            relativeTime: 'senere',
            severityLabel: 'Kritisk',
          },
        ],
      },
    })

    const entries = wrapper.findAll('.entry')
    const badges = wrapper.findAll('[data-testid="badge"]')

    expect(entries).toHaveLength(2)
    expect(entries[0].text()).toContain('Første')
    expect(entries[1].text()).toContain('Andre')
    expect(badges[0].text()).toBe('Lav')
    expect(badges[1].text()).toBe('Kritisk')
  })
})

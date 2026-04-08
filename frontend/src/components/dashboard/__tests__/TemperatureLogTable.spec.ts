import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import TemperatureLogTable from '../TemperatureLogCard.vue'

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

describe('TemperatureLogTable', () => {
  it('renders table header labels', () => {
    const wrapper = mount(TemperatureLogTable, {
      props: {
        rows: [],
      },
    })

    const headers = wrapper.findAll('th').map((th) => th.text())
    expect(wrapper.get('h2').text()).toBe('Dagens temperaturlogg')
    expect(headers).toEqual(['Plassering', 'Temp', 'Grense', 'Status'])
  })

  it('renders OK row without danger text and with ok badge tone', () => {
    const wrapper = mount(TemperatureLogTable, {
      props: {
        rows: [
          {
            location: 'Kjølerom 1',
            temperature: '+3°C',
            limit: '≤ +4°C',
            status: 'OK',
          },
        ],
      },
    })

    const cells = wrapper.findAll('tbody td')
    const badge = wrapper.get('[data-testid="badge"]')

    expect(cells[0].text()).toBe('Kjølerom 1')
    expect(cells[1].text()).toBe('+3°C')
    expect(cells[1].classes()).not.toContain('danger-text')
    expect(cells[2].text()).toBe('≤ +4°C')
    expect(badge.text()).toBe('OK')
    expect(badge.attributes('data-tone')).toBe('ok')
  })

  it('renders Avvik row with danger text and danger badge tone', () => {
    const wrapper = mount(TemperatureLogTable, {
      props: {
        rows: [
          {
            location: 'Fryser 2',
            temperature: '-10°C',
            limit: '≤ -18°C',
            status: 'Avvik',
          },
        ],
      },
    })

    const cells = wrapper.findAll('tbody td')
    const badge = wrapper.get('[data-testid="badge"]')

    expect(cells[1].classes()).toContain('danger-text')
    expect(badge.text()).toBe('Avvik')
    expect(badge.attributes('data-tone')).toBe('danger')
  })

  it('renders multiple rows and one badge per row', () => {
    const wrapper = mount(TemperatureLogTable, {
      props: {
        rows: [
          {
            location: 'Kjølerom 1',
            temperature: '+3°C',
            limit: '≤ +4°C',
            status: 'OK',
          },
          {
            location: 'Buffet',
            temperature: '+12°C',
            limit: '≤ +8°C',
            status: 'Avvik',
          },
        ],
      },
    })

    const rows = wrapper.findAll('tbody tr')
    const badges = wrapper.findAll('[data-testid="badge"]')

    expect(rows).toHaveLength(2)
    expect(rows[0].text()).toContain('Kjølerom 1')
    expect(rows[1].text()).toContain('Buffet')
    expect(badges).toHaveLength(2)
    expect(badges[0].attributes('data-tone')).toBe('ok')
    expect(badges[1].attributes('data-tone')).toBe('danger')
  })
})

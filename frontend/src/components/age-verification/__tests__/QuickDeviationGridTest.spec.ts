import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { afterEach, describe, expect, it, vi } from 'vitest'
import DeviationLog from '../QuickDeviationGrid.vue'

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })

  return {
    AlertTriangle: icon('AlertTriangle'),
    Trash2: icon('Trash2'),
  }
})

describe('DeviationLog', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  function makeProps() {
    return {
      quickTypes: [
        {
          type: 'AGE_VERIFICATION_MISSING',
          label: 'Manglende legitimasjonssjekk',
          icon: defineComponent({
            name: 'MockQuickIcon',
            setup() {
              return () => h('svg', { 'data-icon': 'MockQuickIcon' })
            },
          }),
        },
        {
          type: 'INTOXICATED_CUSTOMER',
          label: 'Overstadig beruset kunde',
          icon: defineComponent({
            name: 'SecondMockQuickIcon',
            setup() {
              return () => h('svg', { 'data-icon': 'SecondMockQuickIcon' })
            },
          }),
        },
      ],
      deviations: [
        {
          id: 11,
          deviationType: 'AGE_VERIFICATION_MISSING',
          reportedAt: '2026-04-09T18:15:00.000Z',
        },
        {
          id: 12,
          deviationType: 'UNKNOWN_TYPE',
          reportedAt: '2026-04-09T19:45:00.000Z',
        },
      ],
      deviationLabels: {
        AGE_VERIFICATION_MISSING: 'Manglende legitimasjonssjekk',
      },
    }
  }

  it('renders quick action buttons and emits create with the selected type', async () => {
    const wrapper = mount(DeviationLog, {
      props: makeProps(),
    })

    const quickButtons = wrapper.findAll('.deviation-btn')

    expect(quickButtons).toHaveLength(2)
    expect(wrapper.text()).toContain('Registrer avvik')
    expect(wrapper.text()).toContain('Manglende legitimasjonssjekk')
    expect(wrapper.text()).toContain('Overstadig beruset kunde')

    await quickButtons[0].trigger('click')
    await quickButtons[1].trigger('click')

    expect(wrapper.emitted('create')).toEqual([
      ['AGE_VERIFICATION_MISSING'],
      ['INTOXICATED_CUSTOMER'],
    ])
  })

  it('renders deviations, uses label fallback, and shows the count', () => {
    vi.spyOn(Date.prototype, 'toLocaleTimeString').mockReturnValue('20:15')

    const wrapper = mount(DeviationLog, {
      props: makeProps(),
    })

    expect(wrapper.text()).toContain('Avvik i dette skiftet (2)')
    expect(wrapper.text()).toContain('Manglende legitimasjonssjekk')
    expect(wrapper.text()).toContain('UNKNOWN_TYPE')
    expect(wrapper.text()).toContain('20:15')
    expect(wrapper.findAll('.deviation-item')).toHaveLength(2)
  })

  it('emits delete with the deviation id when delete is clicked', async () => {
    const wrapper = mount(DeviationLog, {
      props: makeProps(),
    })

    const deleteButtons = wrapper.findAll('.deviation-item-delete')

    await deleteButtons[0].trigger('click')
    await deleteButtons[1].trigger('click')

    expect(wrapper.emitted('delete')).toEqual([[11], [12]])
  })

  it('does not render the deviation log when there are no deviations', () => {
    const wrapper = mount(DeviationLog, {
      props: {
        ...makeProps(),
        deviations: [],
      },
    })

    expect(wrapper.text()).toContain('Registrer avvik')
    expect(wrapper.find('.deviation-log').exists()).toBe(false)
    expect(wrapper.findAll('.deviation-item')).toHaveLength(0)
  })
})

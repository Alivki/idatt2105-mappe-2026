import {mount} from '@vue/test-utils'
import {describe, expect, it} from 'vitest'
import PenaltyStatusCard from '../PenaltyPointsStatus.vue'

describe('PenaltyStatusCard', () => {
  it('renders zero-state when summary is null', () => {
    const wrapper = mount(PenaltyStatusCard, {props: {summary: null}})
    expect(wrapper.text()).toContain('Prikkstatus')
    expect(wrapper.text()).toContain('0')
    expect(wrapper.text()).toContain('av 12')
    expect(wrapper.text()).toContain('Ingen')
    expect(wrapper.text()).toContain('0 hendelser registrert')
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 0%')
  })

  it('renders latest entry label, date and amber progress', () => {
    const wrapper = mount(PenaltyStatusCard, {
      props: {
        summary: {
          totalPoints: 6,
          entries: [
            {createdAt: '2026-04-08T00:00:00Z', violationType: 'BRUDD_SJENKETIDER', points: 4},
            {createdAt: '2026-04-01T00:00:00Z', violationType: 'REKLAMEBRUDD', points: 1},
          ],
        },
      },
    })

    expect(wrapper.text()).toContain('6')
    expect(wrapper.text()).toContain('Brudd sjenketider (4p)')
    expect(wrapper.text()).toContain('2 hendelser registrert')
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 50%')
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('var(--amber)')
  })

  it('clamps progress at 100 and turns red for high totals', () => {
    const wrapper = mount(PenaltyStatusCard, {
      props: {
        summary: {
          totalPoints: 14,
          entries: [
            {createdAt: '2026-04-08T00:00:00Z', violationType: 'UKJENT_TYPE', points: 8},
          ],
        },
      },
    })

    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 100%')
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('var(--red)')
    expect(wrapper.text()).toContain('UKJENT_TYPE (8p)')
  })
})

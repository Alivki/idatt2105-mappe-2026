import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import KpiCard from '../KpiCard.vue'

describe('KpiCard', () => {
  it('renders required title and value with default highlight and without optional content', () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'Avvik åpne',
        value: '12',
      },
    })

    expect(wrapper.get('.kpi-title').text()).toBe('Avvik åpne')
    expect(wrapper.get('.kpi-value').text()).toBe('12')
    expect(wrapper.get('.kpi-value').classes()).toContain('kpi-value--default')
    expect(wrapper.find('.kpi-subtitle').exists()).toBe(false)
    expect(wrapper.find('[role="progressbar"]').exists()).toBe(false)
  })

  it('renders subtitle and success highlight when provided', () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'Fullført',
        value: '98%',
        subtitle: 'Bedre enn i går',
        highlight: 'success',
      },
    })

    expect(wrapper.get('.kpi-value').classes()).toContain('kpi-value--success')
    expect(wrapper.get('.kpi-subtitle').text()).toBe('Bedre enn i går')
    expect(wrapper.get('.kpi-subtitle').classes()).toContain('kpi-subtitle--success')
  })

  it('renders danger highlight classes', () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'For sent',
        value: '7',
        subtitle: 'Krever oppfølging',
        highlight: 'danger',
      },
    })

    expect(wrapper.get('.kpi-value').classes()).toContain('kpi-value--danger')
    expect(wrapper.get('.kpi-subtitle').classes()).toContain('kpi-subtitle--danger')
  })

  it('shows progressbar with rounded percent for normal progress', () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'Sjekklister',
        value: '3/4',
        progress: {
          current: 3,
          total: 4,
        },
      },
    })

    const progressbar = wrapper.get('[role="progressbar"]')
    const fill = wrapper.get('.progress-fill')

    expect(progressbar.attributes('aria-valuenow')).toBe('3')
    expect(progressbar.attributes('aria-valuemax')).toBe('4')
    expect(fill.attributes('style')).toContain('width: 75%')
  })

  it('returns 0 percent when progress is missing or total is zero/negative', async () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'Temperaturer',
        value: '0',
        progress: {
          current: 5,
          total: 0,
        },
      },
    })

    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 0%')

    await wrapper.setProps({
      progress: {
        current: 5,
        total: -2,
      },
    })

    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 0%')
  })

  it('clamps progress below 0 to 0 percent', () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'Negativ test',
        value: '-1',
        progress: {
          current: -1,
          total: 4,
        },
      },
    })

    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 0%')
  })

  it('clamps progress above 100 to 100 percent', () => {
    const wrapper = mount(KpiCard, {
      props: {
        title: 'Over mål',
        value: '12/10',
        progress: {
          current: 12,
          total: 10,
        },
      },
    })

    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 100%')
  })
})

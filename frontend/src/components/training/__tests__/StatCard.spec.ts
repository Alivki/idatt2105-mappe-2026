import {describe, it, expect} from 'vitest'
import {mount} from '@vue/test-utils'
import StatCard from '../StatCard.vue'

describe('StatCard', () => {
  it('renders label correctly', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Totalt ansatte', value: 42},
    })
    expect(wrapper.find('.stat-label').text()).toBe('Totalt ansatte')
  })

  it('renders numeric value correctly', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Kurs', value: 7},
    })
    expect(wrapper.find('.stat-value').text()).toBe('7')
  })

  it('renders string value correctly', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Status', value: 'Aktiv'},
    })
    expect(wrapper.find('.stat-value').text()).toBe('Aktiv')
  })

  it('does not render subLabel when not provided', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Label', value: 1},
    })
    expect(wrapper.find('.stat-sub').exists()).toBe(false)
  })

  it('renders subLabel when provided', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Label', value: 3, subLabel: 'Utløper snart'},
    })
    expect(wrapper.find('.stat-sub').text()).toBe('Utløper snart')
  })

  it('applies valueClass to the value element', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Label', value: 5, valueClass: 'val-green'},
    })
    expect(wrapper.find('.stat-value').classes()).toContain('val-green')
  })

  it('defaults valueClass to empty string (no extra class)', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Label', value: 5},
    })
    const classes = wrapper.find('.stat-value').classes()
    expect(classes).toContain('stat-value')
  })

  it('renders slot content', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Label', value: 1},
      slots: {default: '<span class="custom-slot">Slot content</span>'},
    })
    expect(wrapper.find('.custom-slot').exists()).toBe(true)
    expect(wrapper.find('.custom-slot').text()).toBe('Slot content')
  })

  it('renders the stat-card container', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Test', value: 0},
    })
    expect(wrapper.find('.stat-card').exists()).toBe(true)
  })

  it('renders value of 0 correctly', () => {
    const wrapper = mount(StatCard, {
      props: {label: 'Nullverdi', value: 0},
    })
    expect(wrapper.find('.stat-value').text()).toBe('0')
  })
})

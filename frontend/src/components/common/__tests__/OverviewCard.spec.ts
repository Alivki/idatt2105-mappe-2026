import {mount} from '@vue/test-utils'
import {describe, expect, it} from 'vitest'
import OverviewCard from '../OverviewCard.vue'

describe('OverviewCard', () => {
  it('renders required label and value props', () => {
    const wrapper = mount(OverviewCard, {
      props: {
        label: 'Åpne avvik',
        value: 12,
      },
    })

    expect(wrapper.get('.card-label').text()).toBe('Åpne avvik')
    expect(wrapper.get('.card-value').text()).toBe('12')
  })

  it('uses neutral as default variant and empty string as default valueClass', () => {
    const wrapper = mount(OverviewCard, {
      props: {
        label: 'Status',
        value: 'OK',
      },
    })

    expect(wrapper.classes()).toContain('overview-card')
    expect(wrapper.classes()).toContain('overview-card--neutral')
    expect(wrapper.get('.card-value').classes()).not.toContain('undefined')
  })

  it('renders subLabel only when provided', () => {
    const withSub = mount(OverviewCard, {
      props: {
        label: 'Lukket',
        value: 4,
        subLabel: 'Siste 30 dager',
      },
    })

    expect(withSub.get('.card-sub').text()).toBe('Siste 30 dager')

    const withoutSub = mount(OverviewCard, {
      props: {
        label: 'Lukket',
        value: 4,
      },
    })

    expect(withoutSub.find('.card-sub').exists()).toBe(false)
  })

  it('renders slot content between value and subLabel', () => {
    const wrapper = mount(OverviewCard, {
      props: {
        label: 'Frist',
        value: 3,
        subLabel: 'Denne uken',
      },
      slots: {
        default: '<span class="custom-slot">Tilpasset innhold</span>',
      },
    })

    expect(wrapper.get('.custom-slot').text()).toBe('Tilpasset innhold')
    expect(wrapper.text()).toContain('Frist')
    expect(wrapper.text()).toContain('3')
    expect(wrapper.text()).toContain('Tilpasset innhold')
    expect(wrapper.text()).toContain('Denne uken')
  })

  it('applies each supported variant class', () => {
    const variants = ['neutral', 'open', 'in-progress', 'resolved'] as const

    for (const variant of variants) {
      const wrapper = mount(OverviewCard, {
        props: {
          label: 'Variant',
          value: 1,
          variant,
        },
      })

      expect(wrapper.classes()).toContain(`overview-card--${variant}`)
    }
  })

  it('applies custom valueClass to the value element', () => {
    const wrapper = mount(OverviewCard, {
      props: {
        label: 'Progresjon',
        value: 'Pågår',
        valueClass: 'val-amber emphasis',
      },
    })

    expect(wrapper.get('.card-value').classes()).toContain('val-amber')
    expect(wrapper.get('.card-value').classes()).toContain('emphasis')
  })

  it('supports string and numeric values', () => {
    const stringWrapper = mount(OverviewCard, {
      props: {
        label: 'Ansvarlig',
        value: 'Team A',
      },
    })

    const numberWrapper = mount(OverviewCard, {
      props: {
        label: 'Antall',
        value: 0,
      },
    })

    expect(stringWrapper.get('.card-value').text()).toBe('Team A')
    expect(numberWrapper.get('.card-value').text()).toBe('0')
  })
})

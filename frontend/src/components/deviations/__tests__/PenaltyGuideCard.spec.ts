import {mount} from '@vue/test-utils'
import {describe, expect, it} from 'vitest'
import PenaltyGuideCard from '../PenaltyPointsGuide.vue'

describe('PenaltyGuideCard', () => {
  it('renders all penalty tiers and main guidance text', () => {
    const wrapper = mount(PenaltyGuideCard)
    expect(wrapper.text()).toContain('Prikkguide — kjenn risikoen')
    expect(wrapper.text()).toContain('Alkoholforskriften §10-3')
    expect(wrapper.findAll('.tier')).toHaveLength(4)
    expect(wrapper.text()).toContain('8p')
    expect(wrapper.text()).toContain('4p')
    expect(wrapper.text()).toContain('2p')
    expect(wrapper.text()).toContain('1p')
  })

  it('includes example violations for each band', () => {
    const wrapper = mount(PenaltyGuideCard)
    expect(wrapper.text()).toContain('Skjenking til mindreåring')
    expect(wrapper.text()).toContain('Skjenking til åpenbart beruset')
    expect(wrapper.text()).toContain('Mangler IK-system')
    expect(wrapper.text()).toContain('Alkoholfrie alternativer mangler')
  })
})

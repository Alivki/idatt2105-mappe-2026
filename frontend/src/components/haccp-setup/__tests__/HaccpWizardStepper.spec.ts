import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import WizardStepper from '../HaccpStepper.vue'

describe('WizardStepper', () => {
  it('renders the correct number of bars and label', () => {
    const wrapper = mount(WizardStepper, {
      props: { currentStep: 2, totalSteps: 5 },
    })

    const bars = wrapper.findAll('.stepper-bar')
    expect(bars).toHaveLength(5)
    expect(wrapper.text()).toContain('Trinn 2 av 5')
  })

  it('marks previous bars as completed and current as active', () => {
    const wrapper = mount(WizardStepper, {
      props: { currentStep: 3, totalSteps: 4 },
    })

    const bars = wrapper.findAll('.stepper-bar')
    expect(bars[0].classes()).toContain('stepper-bar--completed')
    expect(bars[1].classes()).toContain('stepper-bar--completed')
    expect(bars[2].classes()).toContain('stepper-bar--active')
    expect(bars[3].classes()).not.toContain('stepper-bar--active')
    expect(bars[3].classes()).not.toContain('stepper-bar--completed')
  })

  it('shows no completed bars on first step', () => {
    const wrapper = mount(WizardStepper, {
      props: { currentStep: 1, totalSteps: 3 },
    })

    const bars = wrapper.findAll('.stepper-bar')
    expect(bars[0].classes()).toContain('stepper-bar--active')
    expect(bars[1].classes()).not.toContain('stepper-bar--completed')
    expect(bars[2].classes()).not.toContain('stepper-bar--completed')
  })
})

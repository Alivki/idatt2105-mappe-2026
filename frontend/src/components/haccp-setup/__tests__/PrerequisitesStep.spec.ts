import {mount} from '@vue/test-utils'
import {defineComponent, h} from 'vue'
import {describe, expect, it, vi} from 'vitest'
import PrerequisitesStep from '../Step2FoodHandling.vue'


vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', {'data-icon': name})
    },
  })
  return {
    UtensilsCrossed: icon('UtensilsCrossed'),
    Coffee: icon('Coffee'),
    Wine: icon('Wine'),
    Store: icon('Store'),
    Truck: icon('Truck'),
    Building2: icon('Building2'),
    Soup: icon('Soup'),
    Factory: icon('Factory'),
    ShoppingCart: icon('ShoppingCart'),
    MapPin: icon('MapPin'),
    Check: icon('Check'),
    ShieldCheck: icon('ShieldCheck'),
    CircleCheck: icon('CircleCheck'),
    AlertTriangle: icon('AlertTriangle'),
    CircleX: icon('CircleX'),
    ClipboardCheck: icon('ClipboardCheck'),
    Info: icon('Info'),
    ArrowDown: icon('ArrowDown'),
    Plus: icon('Plus'),
    X: icon('X'),
    ShieldAlert: icon('ShieldAlert'),
  }
})


vi.mock('@/types/haccp-setup', () => ({
  prerequisiteStatusLabels: {
    OK: 'På plass',
    NEEDS_WORK: 'Under arbeid',
    MISSING: 'Mangler',
  },
}))

function makeWizard() {
  return {
    prerequisites: [
      {id: '1', name: 'Renhold', description: 'Rutiner for renhold', status: 'OK'},
      {id: '2', name: 'Vedlikehold', description: 'Rutiner for utstyr', status: 'NEEDS_WORK'},
      {id: '3', name: 'Skadedyr', description: 'Forebygging', status: 'MISSING'},
    ],
  }
}

describe('PrerequisitesStep', () => {
  it('renders summary counts from current statuses', () => {
    const wrapper = mount(PrerequisitesStep, {
      props: {wizard: makeWizard()},
    })

    expect(wrapper.text()).toContain('1 på plass')
    expect(wrapper.text()).toContain('1 under arbeid')
    expect(wrapper.text()).toContain('1 mangler')
    expect(wrapper.text()).toContain('Sjekklister vil bli opprettet')
  })

  it('updates prerequisite status and summary when status buttons are clicked', async () => {
    const wizard = makeWizard()
    const wrapper = mount(PrerequisitesStep, {
      props: {wizard},
    })

    const workCard = wrapper.findAll('.prereq-card')[1]
    const missingCard = wrapper.findAll('.prereq-card')[2]

    await workCard.findAll('button')[0].trigger('click')
    await missingCard.findAll('button')[1].trigger('click')

    expect(wizard.prerequisites[1].status).toBe('OK')
    expect(wizard.prerequisites[2].status).toBe('NEEDS_WORK')

    expect(wrapper.text()).toContain('2 på plass')
    expect(wrapper.text()).toContain('1 under arbeid')
    expect(wrapper.text()).toContain('0 mangler')
  })

  it('applies status classes to cards and active buttons', () => {
    const wrapper = mount(PrerequisitesStep, {
      props: {wizard: makeWizard()},
    })

    const cards = wrapper.findAll('.prereq-card')
    expect(cards[0].classes()).toContain('prereq-card--ok')
    expect(cards[1].classes()).toContain('prereq-card--work')
    expect(cards[2].classes()).toContain('prereq-card--missing')

    const firstCardButtons = cards[0].findAll('.status-btn')
    expect(firstCardButtons[0].classes()).toContain('status-btn--ok')
  })
})

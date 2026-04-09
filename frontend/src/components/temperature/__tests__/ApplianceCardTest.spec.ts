import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import TemperatureApplianceCard from '../ApplianceCard.vue'

vi.mock('@/composables/useTemperatureMonitoring', () => ({
  formatThreshold: vi.fn((threshold: { min: number; max: number }) => `${threshold.min} til ${threshold.max}°C`),
}))

vi.mock('lucide-vue-next', () => ({
  MoreVertical: { template: '<svg data-icon="more" />' },
  Pencil: { template: '<svg data-icon="pencil" />' },
  Power: { template: '<svg data-icon="power" />' },
  PowerOff: { template: '<svg data-icon="poweroff" />' },
  Refrigerator: { template: '<svg data-icon="refrigerator" />' },
  Snowflake: { template: '<svg data-icon="snowflake" />' },
  Trash2: { template: '<svg data-icon="trash" />' },
}))

const ButtonStub = defineComponent({
  name: 'ButtonStub',
  emits: ['click'],
  setup(_, { slots, emit, attrs }) {
    return () => h('button', { ...attrs, onClick: (e: MouseEvent) => emit('click', e) }, slots.default ? slots.default() : [])
  },
})

const BadgeStub = defineComponent({
  name: 'BadgeStub',
  props: { tone: { type: String, default: '' } },
  setup(props, { slots }) {
    return () => h('span', { 'data-tone': props.tone, class: 'badge-stub' }, slots.default ? slots.default() : [])
  },
})

function mountComponent(appliance: Record<string, unknown>) {
  return mount(TemperatureApplianceCard, {
    props: { appliance },
    global: {
      stubs: {
        Badge: BadgeStub,
        Button: ButtonStub,
        DropdownMenu: { template: '<div><slot /></div>' },
        DropdownMenuContent: { template: '<div><slot /></div>' },
        DropdownMenuItem: ButtonStub,
        DropdownMenuSeparator: { template: '<hr />' },
        DropdownMenuTrigger: { template: '<div><slot /></div>' },
      },
    },
  })
}

describe('TemperatureApplianceCard', () => {
  it('renders appliance details and last entry status for active fridge', () => {
    const wrapper = mountComponent({
      id: 1,
      name: 'Kjøleskap kjøkken',
      type: 'FRIDGE',
      isActive: true,
      threshold: { min: 0, max: 4 },
      lastEntry: {
        temperature: 3.2,
        measuredAt: '2026-04-09T10:15:00Z',
        status: 'OK',
      },
    })

    expect(wrapper.text()).toContain('Kjøleskap kjøkken')
    expect(wrapper.text()).toContain('Aktiv')
    expect(wrapper.text()).toContain('0 til 4°C')
    expect(wrapper.text()).toContain('3.2°C')
    expect(wrapper.text()).toContain('OK')
    expect(wrapper.find('[data-icon="refrigerator"]').exists()).toBe(true)
    expect(wrapper.classes()).not.toContain('device-card--inactive')
  })

  it('renders inactive freezer without data and shows fallback text', () => {
    const wrapper = mountComponent({
      id: 2,
      name: 'Fryser lager',
      type: 'FREEZER',
      isActive: false,
      threshold: { min: -18, max: -15 },
      lastEntry: null,
    })

    expect(wrapper.text()).toContain('Inaktiv')
    expect(wrapper.text()).toContain('Ingen måling')
    expect(wrapper.text()).toContain('Ingen data')
    expect(wrapper.find('[data-icon="snowflake"]').exists()).toBe(true)
    expect(wrapper.classes()).toContain('device-card--inactive')
  })

  it('emits edit, toggle-active and delete actions with appliance payload', async () => {
    const appliance = {
      id: 3,
      name: 'Testenhet',
      type: 'FRIDGE',
      isActive: true,
      threshold: { min: 0, max: 4 },
      lastEntry: null,
    }
    const wrapper = mountComponent(appliance)
    const buttons = wrapper.findAll('button')

    await buttons.find((node) => node.text().includes('Rediger'))!.trigger('click')
    await buttons.find((node) => node.text().includes('Sett inaktiv'))!.trigger('click')
    await buttons.find((node) => node.text().includes('Slett enhet'))!.trigger('click')

    expect(wrapper.emitted('edit')![0][0]).toEqual(appliance)
    expect(wrapper.emitted('toggle-active')![0][0]).toEqual(appliance)
    expect(wrapper.emitted('delete')![0][0]).toEqual(appliance)
  })
})

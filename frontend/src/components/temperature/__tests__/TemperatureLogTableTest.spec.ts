import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import TemperatureLogTable from '../TemperatureLogTable.vue'

vi.mock('lucide-vue-next', () => ({
  Thermometer: { template: '<svg data-icon="thermometer" />' },
}))

const ButtonStub = defineComponent({
  name: 'ButtonStub',
  props: { disabled: { type: Boolean, default: false } },
  emits: ['click'],
  setup(props, { slots, emit, attrs }) {
    return () => h('button', { ...attrs, disabled: props.disabled, onClick: (e: MouseEvent) => !props.disabled && emit('click', e) }, slots.default ? slots.default() : [])
  },
})

const BadgeStub = defineComponent({
  name: 'BadgeStub',
  props: { tone: { type: String, default: '' } },
  setup(props, { slots }) {
    return () => h('span', { 'data-tone': props.tone }, slots.default ? slots.default() : [])
  },
})

const CheckboxStub = defineComponent({
  name: 'CheckboxStub',
  props: { checked: { type: Boolean, default: false } },
  emits: ['update:checked'],
  setup(props, { emit }) {
    return () => h('input', {
      type: 'checkbox',
      checked: props.checked,
      onChange: (e: Event) => emit('update:checked', (e.target as HTMLInputElement).checked),
    })
  },
})

const passthrough = (tag: string) => defineComponent({
  name: `${tag}Stub`,
  setup(_, { slots, attrs }) {
    return () => h(tag, attrs, slots.default ? slots.default() : [])
  },
})

function mountComponent(overrides?: Record<string, unknown>) {
  return mount(TemperatureLogTable, {
    props: {
      entries: [],
      totalEntries: 0,
      canManage: false,
      selectedEntryIds: [],
      allRowsSelected: false,
      isMobile: false,
      paginationSummary: 'Viser 0–0 av 0',
      currentPage: 1,
      totalPages: 1,
      ...overrides,
    },
    global: {
      stubs: {
        Badge: BadgeStub,
        Button: ButtonStub,
        Checkbox: CheckboxStub,
        Table: passthrough('table'),
        TableBody: passthrough('tbody'),
        TableCell: passthrough('td'),
        TableEmpty: defineComponent({
          props: { colspan: { type: Number, default: 1 } },
          setup(props, { slots }) {
            return () => h('tr', [h('td', { colspan: props.colspan }, slots.default ? slots.default() : [])])
          },
        }),
        TableHead: passthrough('th'),
        TableHeader: passthrough('thead'),
        TableRow: passthrough('tr'),
      },
    },
  })
}

const sampleEntries = [
  {
    id: 11,
    measuredAt: '2026-04-09T10:30:00Z',
    applianceName: 'Kjøl 1',
    applianceType: 'FRIDGE',
    temperature: 3.4,
    threshold: '0 til 4°C',
    measuredBy: 'Ada',
    status: 'OK',
    statusTone: 'ok',
    note: 'Alt normalt',
  },
  {
    id: 12,
    measuredAt: '2026-04-09T11:00:00Z',
    applianceName: 'Fryser 1',
    applianceType: 'FREEZER',
    temperature: -8.2,
    threshold: '-18 til -15°C',
    measuredBy: 'Bob',
    status: 'DEVIATION',
    statusTone: 'danger',
    note: null,
  },
]

describe('TemperatureLogTable', () => {
  it('renders empty state in desktop table when there are no entries', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Ingen temperaturregistreringer enda')
    expect(wrapper.find('[data-icon="thermometer"]').exists()).toBe(true)
  })

  it('renders desktop rows and emits selection, deletion and pagination events', async () => {
    const wrapper = mountComponent({
      entries: sampleEntries,
      totalEntries: 2,
      canManage: true,
      selectedEntryIds: [11],
      allRowsSelected: false,
      paginationSummary: 'Viser 1–2 av 2',
      currentPage: 1,
      totalPages: 2,
    })

    expect(wrapper.text()).toContain('Slett valgte (1)')
    expect(wrapper.text()).toContain('Kjøl 1')
    expect(wrapper.text()).toContain('Fryser')
    expect(wrapper.text()).toContain('Avvik')
    expect(wrapper.find('.row--deviation').exists()).toBe(true)

    const checkboxes = wrapper.findAll('input[type="checkbox"]')
    await checkboxes[0].setValue(true)
    await checkboxes[1].setValue(false)

    const deleteButton = wrapper.findAll('button').find((node) => node.text().includes('Slett valgte'))!
    const nextButton = wrapper.findAll('button').find((node) => node.text().includes('Neste'))!
    await deleteButton.trigger('click')
    await nextButton.trigger('click')

    expect(wrapper.emitted('toggle-all')![0]).toEqual([true])
    expect(wrapper.emitted('toggle-entry')![0]).toEqual([11, false])
    expect(wrapper.emitted('delete-selected')).toBeTruthy()
    expect(wrapper.emitted('next-page')).toBeTruthy()
  })

  it('renders mobile cards and emits entry toggle in mobile mode', async () => {
    const wrapper = mountComponent({
      entries: sampleEntries,
      totalEntries: 2,
      canManage: true,
      selectedEntryIds: [],
      isMobile: true,
      paginationSummary: 'Viser 1–2 av 2',
      currentPage: 2,
      totalPages: 2,
    })

    expect(wrapper.find('.mobile-entries').exists()).toBe(true)
    expect(wrapper.find('.mobile-entry--deviation').exists()).toBe(true)
    expect(wrapper.text()).toContain('Alt normalt')

    const checkbox = wrapper.find('input[type="checkbox"]')
    await checkbox.setValue(true)

    expect(wrapper.emitted('toggle-entry')![0]).toEqual([11, true])
  })

  it('disables pagination buttons at boundaries', () => {
    const wrapper = mountComponent({ currentPage: 1, totalPages: 1 })
    const buttons = wrapper.findAll('button')
    const prevButton = buttons.find((node) => node.text().includes('Forrige'))!
    const nextButton = buttons.find((node) => node.text().includes('Neste'))!

    expect(prevButton.attributes('disabled')).toBeDefined()
    expect(nextButton.attributes('disabled')).toBeDefined()
  })
})

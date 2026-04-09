import {describe, it, expect} from 'vitest'
import {mount} from '@vue/test-utils'
import TrainingTable from '../TrainingTable.vue'

interface TrainingRow {
  id: number
  type: string
  completed: string | null
  expires: string | null
  status: string
  employee: {
    name: string
    initials: string
    color: string
    role: string
  }
}

const EmployeeAvatarStub = {
  name: 'EmployeeAvatar',
  template: '<div class="avatar-stub" :data-initials="initials" :data-color="color" />',
  props: ['initials', 'color', 'size'],
}

const StatusBadgeStub = {
  name: 'StatusBadge',
  template: '<span class="status-badge-stub">{{ status }}</span>',
  props: ['status'],
}

const iconStub = {template: '<span />'}

const globalStubs = {
  EmployeeAvatar: EmployeeAvatarStub,
  StatusBadge: StatusBadgeStub,
  ChevronDown: iconStub,
  ChevronUp: iconStub,
  MoreVertical: iconStub,
  Pencil: iconStub,
  Trash2: iconStub,
}

const makeRow = (overrides: Partial<TrainingRow> = {}): TrainingRow => ({
  id: 1,
  type: 'Brannvern',
  completed: '2024-01-15',
  expires: '2025-01-15',
  status: 'Gyldig',
  employee: {
    name: 'Ola Nordmann',
    initials: 'ON',
    color: '#4ade80',
    role: 'Ansatt',
  },
  ...overrides,
})

const makeGrouped = (
  rows: TrainingRow[],
  key = 'Brannvern',
): Record<string, TrainingRow[]> => ({[key]: rows})

describe('TrainingTable', () => {
  describe('empty state', () => {
    it('shows empty message when groupedTrainings is empty', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: {}},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Ingen resultater matcher filteret.')
    })

    it('does not show table when groupedTrainings is empty', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: {}},
        global: {stubs: globalStubs},
      })
      expect(wrapper.find('table').exists()).toBe(false)
    })
  })

  describe('group header', () => {
    it('renders group type as header text', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Brannvern')
    })

    it('shows correct employee count badge', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow(), makeRow({id: 2})])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('2 ansatte')
    })

    it('renders multiple groups', () => {
      const wrapper = mount(TrainingTable, {
        props: {
          groupedTrainings: {
            Brannvern: [makeRow()],
            HMS: [makeRow({id: 2, type: 'HMS'})],
          },
        },
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Brannvern')
      expect(wrapper.text()).toContain('HMS')
    })
  })

  describe('table content', () => {
    it('renders employee name in a row', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Ola Nordmann')
    })

    it('renders employee role in a row', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Ansatt')
    })

    it('renders training type in a row', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Brannvern')
    })

    it('renders completed date', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('2024-01-15')
    })

    it('renders expires date', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('2025-01-15')
    })

    it('renders "—" when completed is null', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow({completed: null})])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('—')
    })

    it('renders "—" when expires is null', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow({expires: null})])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('—')
    })

    it('renders EmployeeAvatar with correct props', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      const avatar = wrapper.find('.avatar-stub')
      expect(avatar.attributes('data-initials')).toBe('ON')
      expect(avatar.attributes('data-color')).toBe('#4ade80')
    })

    it('renders StatusBadge with correct status', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.find('.status-badge-stub').text()).toBe('Gyldig')
    })

    it('applies amber text class to expires cell when status is Utløper snart', () => {
      const wrapper = mount(TrainingTable, {
        props: {
          groupedTrainings: makeGrouped([makeRow({
            status: 'Utløper snart',
            expires: '2024-06-01'
          })]),
        },
        global: {stubs: globalStubs},
      })
      const tds = wrapper.findAll('td')
      const expiresTd = tds.find((td) => td.text().includes('2024-06-01'))
      expect(expiresTd?.classes()).toContain('text-amber-600')
    })

    it('renders multiple rows in the same group', () => {
      const row1 = makeRow({
        id: 1,
        employee: {name: 'Person A', initials: 'PA', color: '#000', role: 'Ansatt'}
      })
      const row2 = makeRow({
        id: 2,
        employee: {name: 'Person B', initials: 'PB', color: '#fff', role: 'Leder'}
      })
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([row1, row2])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.text()).toContain('Person A')
      expect(wrapper.text()).toContain('Person B')
    })
  })

  describe('collapse/expand', () => {
    it('shows table by default (not collapsed)', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.find('table').exists()).toBe(true)
    })

    it('hides table when group header is clicked', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      await wrapper.find('button').trigger('click')
      expect(wrapper.find('table').exists()).toBe(false)
    })

    it('shows table again after double click on group header', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      const headerBtn = wrapper.find('button')
      await headerBtn.trigger('click') // collapse
      await headerBtn.trigger('click') // expand
      expect(wrapper.find('table').exists()).toBe(true)
    })
  })

  describe('context menu', () => {
    it('context menu is hidden by default', () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
      })
      expect(wrapper.find('.absolute.right-0').exists()).toBe(false)
    })

    it('opens context menu on MoreVertical button click', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      const menuBtn = wrapper.find('.ctx-wrap button')
      await menuBtn.trigger('click')
      expect(wrapper.find('.ctx-wrap .absolute').exists()).toBe(true)
      wrapper.unmount()
    })

    it('closes context menu when clicking the button again', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      const menuBtn = wrapper.find('.ctx-wrap button')
      await menuBtn.trigger('click')
      await menuBtn.trigger('click')
      expect(wrapper.find('.ctx-wrap .absolute').exists()).toBe(false)
      wrapper.unmount()
    })

    it('emits edit event with row when Rediger is clicked', async () => {
      const row = makeRow()
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([row])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      await wrapper.find('.ctx-wrap button').trigger('click')
      const dropdownBtns = wrapper.findAll('.ctx-wrap .absolute button')
      await dropdownBtns[0].trigger('click')
      expect(wrapper.emitted('edit')).toBeTruthy()
      expect(wrapper.emitted('edit')?.[0]).toEqual([row])
      wrapper.unmount()
    })

    it('emits delete event with row when Slett is clicked', async () => {
      const row = makeRow()
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([row])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      await wrapper.find('.ctx-wrap button').trigger('click')
      const dropdownBtns = wrapper.findAll('.ctx-wrap .absolute button')
      await dropdownBtns[1].trigger('click')
      expect(wrapper.emitted('delete')).toBeTruthy()
      expect(wrapper.emitted('delete')?.[0]).toEqual([row])
      wrapper.unmount()
    })

    it('closes context menu after edit is clicked', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      await wrapper.find('.ctx-wrap button').trigger('click')
      const dropdownBtns = wrapper.findAll('.ctx-wrap .absolute button')
      await dropdownBtns[0].trigger('click')
      expect(wrapper.find('.ctx-wrap .absolute').exists()).toBe(false)
      wrapper.unmount()
    })

    it('closes context menu after delete is clicked', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      await wrapper.find('.ctx-wrap button').trigger('click')
      const dropdownBtns = wrapper.findAll('.ctx-wrap .absolute button')
      await dropdownBtns[1].trigger('click')
      expect(wrapper.find('.ctx-wrap .absolute').exists()).toBe(false)
      wrapper.unmount()
    })
  })

  describe('outside click', () => {
    it('closes context menu on outside click', async () => {
      const wrapper = mount(TrainingTable, {
        props: {groupedTrainings: makeGrouped([makeRow()])},
        global: {stubs: globalStubs},
        attachTo: document.body,
      })
      await wrapper.find('.ctx-wrap button').trigger('click')
      expect(wrapper.find('.ctx-wrap .absolute').exists()).toBe(true)

      document.body.dispatchEvent(new MouseEvent('click', {bubbles: true}))
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.ctx-wrap .absolute').exists()).toBe(false)
      wrapper.unmount()
    })
  })
})

import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import EmployeeMembersList from '../EmployeeTable.vue'

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })
  return {
    MoreVertical: icon('MoreVertical'),
    ShieldCheck: icon('ShieldCheck'),
    UserMinus: icon('UserMinus'),
    ArrowUpDown: icon('ArrowUpDown'),
    Search: icon('Search'),
  }
})

vi.mock('@/components/ui/badge/Badge.vue', () => ({
  default: defineComponent({
    name: 'Badge',
    props: { tone: { type: String, default: '' } },
    setup(props, { slots }) {
      return () => h('span', { class: 'badge', 'data-tone': props.tone }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    name: 'Button',
    emits: ['click'],
    props: {
      type: { type: String, default: 'button' },
      disabled: { type: Boolean, default: false },
    },
    setup(props, { slots, attrs, emit }) {
      return () => h('button', {
        ...attrs,
        type: props.type,
        disabled: props.disabled,
        onClick: (e: MouseEvent) => emit('click', e),
      }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/table', () => {
  const simple = (name: string, tag: string) => defineComponent({
    name,
    props: { colspan: { type: [String, Number], required: false } },
    setup(props, { slots }) {
      return () => h(tag, props.colspan ? { colspan: props.colspan } : {}, slots.default?.())
    },
  })
  return {
    Table: simple('Table', 'table'),
    TableBody: simple('TableBody', 'tbody'),
    TableCell: simple('TableCell', 'td'),
    TableEmpty: simple('TableEmpty', 'tr'),
    TableHead: simple('TableHead', 'th'),
    TableHeader: simple('TableHeader', 'thead'),
    TableRow: simple('TableRow', 'tr'),
  }
})

vi.mock('@/components/ui/dropdown-menu', () => {
  const passthrough = (name: string, tag = 'div') => defineComponent({
    name,
    emits: ['click'],
    setup(_, { slots, emit, attrs }) {
      return () => h(tag, { ...attrs, onClick: (e: MouseEvent) => emit('click', e) }, slots.default?.())
    },
  })
  return {
    DropdownMenu: passthrough('DropdownMenu'),
    DropdownMenuTrigger: passthrough('DropdownMenuTrigger'),
    DropdownMenuContent: passthrough('DropdownMenuContent'),
    DropdownMenuItem: passthrough('DropdownMenuItem', 'button'),
    DropdownMenuLabel: passthrough('DropdownMenuLabel'),
    DropdownMenuSeparator: passthrough('DropdownMenuSeparator', 'hr'),
  }
})

const members = [
  {
    id: 1,
    userId: 10,
    userFullName: 'Ola Nordmann',
    userEmail: 'ola@example.com',
    role: 'ADMIN',
  },
  {
    id: 2,
    userId: 11,
    userFullName: 'Kari Hansen',
    userEmail: 'kari@example.com',
    role: 'EMPLOYEE',
  },
]

describe('EmployeeMembersList', () => {
  it('renders desktop rows, marks current user, updates search, and toggles sort', async () => {
    const wrapper = mount(EmployeeMembersList, {
      props: {
        members,
        search: '',
        sortField: 'name',
        sortDir: 'asc',
        isMobile: false,
        isAdmin: true,
        currentUserId: 10,
      },
    })

    expect(wrapper.text()).toContain('Ola Nordmann')
    expect(wrapper.text()).toContain('(deg)')
    expect(wrapper.text()).toContain('Admin')
    expect(wrapper.text()).toContain('Ansatt')
    expect(wrapper.text()).toContain('ON')
    expect(wrapper.text()).toContain('KH')

    const searchInput = wrapper.get('input[aria-label="Søk etter ansatt"]')
    await searchInput.setValue('kari')
    expect(wrapper.emitted('update:search')?.[0]).toEqual(['kari'])

    const sortButtons = wrapper.findAll('button.sort-btn')
    await sortButtons[1].trigger('click')
    expect(wrapper.emitted('toggle-sort')?.[0]).toEqual(['email'])
  })

  it('emits edit-role and remove-member for non-self member in desktop mode', async () => {
    const wrapper = mount(EmployeeMembersList, {
      props: {
        members,
        search: '',
        sortField: 'name',
        sortDir: 'asc',
        isMobile: false,
        isAdmin: true,
        currentUserId: 10,
      },
    })

    const actionButtons = wrapper.findAll('button')
    const editButton = actionButtons.find((b) => b.text().includes('Endre rolle'))
    const removeButton = actionButtons.find((b) => b.text().includes('Fjern fra organisasjon'))

    expect(editButton).toBeTruthy()
    expect(removeButton).toBeTruthy()

    await editButton!.trigger('click')
    await removeButton!.trigger('click')

    expect(wrapper.emitted('edit-role')?.[0]).toEqual([members[1]])
    expect(wrapper.emitted('remove-member')?.[0]).toEqual([members[1]])
  })

  it('renders mobile empty state when there are no members', () => {
    const wrapper = mount(EmployeeMembersList, {
      props: {
        members: [],
        search: 'zzz',
        sortField: 'name',
        sortDir: 'asc',
        isMobile: true,
        isAdmin: false,
        currentUserId: 10,
      },
    })

    expect(wrapper.text()).toContain('Ingen ansatte matcher søket')
    expect(wrapper.text()).toContain('Prøv et annet søk eller inviter en ny ansatt.')
  })
})

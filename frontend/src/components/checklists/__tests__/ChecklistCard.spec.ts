import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ChecklistCard from '../ChecklistCard.vue'

function makePassThrough(name: string, tag = 'div') {
  return defineComponent({
    name,
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h(tag, attrs, slots.default?.())
    },
  })
}

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })

  return {
    MoreVertical: icon('MoreVertical'),
    CheckCircle2: icon('CheckCircle2'),
    Pencil: icon('Pencil'),
    Trash2: icon('Trash2'),
    Check: icon('Check'),
  }
})

vi.mock('@/components/ui/badge/Badge.vue', () => ({
  default: defineComponent({
    name: 'BadgeStub',
    props: { tone: { type: String, default: '' } },
    setup(props, { slots }) {
      return () => h('span', { 'data-testid': 'badge', 'data-tone': props.tone }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    name: 'ButtonStub',
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h('button', attrs, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/dropdown-menu', () => {
  const passThrough = (name: string, tag = 'div') => defineComponent({
    name,
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h(tag, attrs, slots.default?.())
    },
  })

  return {
    DropdownMenu: passThrough('DropdownMenu'),
    DropdownMenuTrigger: passThrough('DropdownMenuTrigger'),
    DropdownMenuContent: passThrough('DropdownMenuContent'),
    DropdownMenuItem: passThrough('DropdownMenuItem', 'button'),
    DropdownMenuSeparator: passThrough('DropdownMenuSeparator', 'hr'),
  }
})

vi.mock('@/components/ui/alert-dialog/AlertDialog.vue', () => ({
  default: defineComponent({
    name: 'AlertDialogStub',
    props: { open: { type: Boolean, default: false } },
    emits: ['update:open'],
    setup(props, { slots }) {
      return () => props.open ? h('div', { 'data-testid': 'alert-dialog' }, slots.default?.()) : null
    },
  }),
}))

vi.mock('@/components/ui/alert-dialog/AlertDialogAction.vue', () => ({ default: makePassThrough('AlertDialogAction', 'button') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogCancel.vue', () => ({ default: makePassThrough('AlertDialogCancel', 'button') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogContent.vue', () => ({ default: makePassThrough('AlertDialogContent') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogDescription.vue', () => ({ default: makePassThrough('AlertDialogDescription', 'p') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogFooter.vue', () => ({ default: makePassThrough('AlertDialogFooter') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogHeader.vue', () => ({ default: makePassThrough('AlertDialogHeader') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogTitle.vue', () => ({ default: makePassThrough('AlertDialogTitle', 'h2') }))

const baseChecklist = {
  id: 7,
  name: 'Kjøkkenrutine',
  description: 'Vask benk og gulv',
  frequency: 'DAILY',
  itemCount: 4,
  completedItemCount: 2,
  status: 'IN_PROGRESS',
  items: [],
}

describe('ChecklistCard', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-04-08T10:30:00Z'))
  })

  it('renders mapped frequency, tone, meta line and progress for in-progress checklist', () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: baseChecklist,
        canManage: true,
        canComplete: true,
      },
    })

    expect(wrapper.text()).toContain('Kjøkkenrutine')
    expect(wrapper.get('[data-testid="badge"]').text()).toBe('Daglig')
    expect(wrapper.get('[data-testid="badge"]').attributes('data-tone')).toBe('brand')
    expect(wrapper.text()).toContain('2/4 punkter · Vask benk og gulv')

    const fill = wrapper.get('.progress-fill')
    expect(fill.attributes('style')).toContain('width: 50%')
    expect(fill.attributes('style')).toContain('var(--amber)')
    expect(wrapper.get('.circle-count').text()).toBe('2/4')
  })

  it('emits open when the card body is clicked', async () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: baseChecklist,
        canManage: false,
        canComplete: false,
      },
    })

    await wrapper.get('.card-body').trigger('click')
    expect(wrapper.emitted('open')).toEqual([[baseChecklist]])
  })

  it('shows completed state, final meta text and latest completion time', () => {
    const completedChecklist = {
      ...baseChecklist,
      description: '',
      frequency: 'YEARLY',
      itemCount: 2,
      completedItemCount: 2,
      status: 'COMPLETED',
      items: [
        { id: 1, completedAt: '2026-04-08T08:15:00Z' },
        { id: 2, completedAt: '2026-04-08T09:45:00Z' },
      ],
    }

    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: completedChecklist,
        canManage: true,
        canComplete: true,
      },
    })

    const expectedLatestTime = new Date('2026-04-08T09:45:00Z').toLocaleTimeString('no-NO', {
      hour: '2-digit',
      minute: '2-digit',
    })

    expect(wrapper.text()).toContain('Årlig')
    expect(wrapper.get('[data-testid="badge"]').attributes('data-tone')).toBe('neutral')
    expect(wrapper.text()).toContain('2/2 punkter fullført · Fullført')
    expect(wrapper.text()).toContain(expectedLatestTime)
    expect(wrapper.find('.circle-check').exists()).toBe(true)
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 100%')
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('var(--green)')
  })

  it('falls back to raw frequency and neutral styling for unknown frequency', () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: { ...baseChecklist, frequency: 'CUSTOM', status: 'NOT_STARTED', completedItemCount: 0 },
        canManage: false,
        canComplete: false,
      },
    })

    expect(wrapper.get('[data-testid="badge"]').text()).toBe('CUSTOM')
    expect(wrapper.get('[data-testid="badge"]').attributes('data-tone')).toBe('neutral')
    expect(wrapper.text()).toContain('0/4 punkter · Vask benk og gulv')
    expect(wrapper.get('.progress-fill').attributes('style')).toContain('width: 0%')
  })

  it('emits toggle event with completed=true when completing an unfinished checklist', async () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: baseChecklist,
        canManage: false,
        canComplete: true,
      },
    })

    const completeButton = wrapper.findAll('button').find((btn) => btn.text().includes('Fullfør hele'))
    expect(completeButton).toBeTruthy()

    await completeButton!.trigger('click')
    expect(wrapper.emitted('toggle-checklist-completed')).toEqual([
      [{ checklistId: 7, completed: true }],
    ])
  })

  it('emits toggle event with completed=false when undoing a completed checklist', async () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: {
          ...baseChecklist,
          itemCount: 3,
          completedItemCount: 3,
          status: 'COMPLETED',
        },
        canManage: false,
        canComplete: true,
      },
    })

    const undoButton = wrapper.findAll('button').find((btn) => btn.text().includes('Angre fullføring'))
    expect(undoButton).toBeTruthy()

    await undoButton!.trigger('click')
    expect(wrapper.emitted('toggle-checklist-completed')).toEqual([
      [{ checklistId: 7, completed: false }],
    ])
  })

  it('emits edit event when edit menu item is clicked', async () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: baseChecklist,
        canManage: true,
        canComplete: false,
      },
    })

    const editButton = wrapper.findAll('button').find((btn) => btn.text().includes('Rediger'))
    expect(editButton).toBeTruthy()

    await editButton!.trigger('click')
    expect(wrapper.emitted('edit-checklist')).toEqual([[baseChecklist]])
  })

  it('opens delete dialog and emits delete event', async () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: baseChecklist,
        canManage: true,
        canComplete: false,
      },
    })

    const deleteMenuButton = wrapper.findAll('button').find((btn) => btn.text().includes('Slett sjekkliste'))
    expect(deleteMenuButton).toBeTruthy()

    await deleteMenuButton!.trigger('click')
    expect(wrapper.find('[data-testid="alert-dialog"]').exists()).toBe(true)

    const dialogDeleteButton = wrapper.findAll('button').find((btn) => btn.text() === 'Slett')
    expect(dialogDeleteButton).toBeTruthy()

    await dialogDeleteButton!.trigger('click')
    expect(wrapper.emitted('delete-checklist')).toEqual([[7]])
  })

  it('hides actions when user cannot manage or complete', () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: baseChecklist,
        canManage: false,
        canComplete: false,
      },
    })

    expect(wrapper.find('.card-actions').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Fullfør hele')
    expect(wrapper.text()).not.toContain('Rediger')
  })

  it('does not show completion action when checklist has no items', () => {
    const wrapper = mount(ChecklistCard, {
      props: {
        checklist: { ...baseChecklist, itemCount: 0, completedItemCount: 0 },
        canManage: false,
        canComplete: true,
      },
    })

    expect(wrapper.text()).not.toContain('Fullfør hele')
    expect(wrapper.text()).not.toContain('Angre fullføring')
  })

})

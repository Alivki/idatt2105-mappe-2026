import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AlcoholDeviationCard from '../AlcoholDeviationListCard.vue'

function pass(name: string, tag = 'div') {
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
    Trash2: icon('Trash2'),
    Pencil: icon('Pencil'),
    CircleDot: icon('CircleDot'),
    Clock: icon('Clock'),
    CheckCircle2: icon('CheckCircle2'),
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

vi.mock('@/components/ui/dropdown-menu', () => ({
  DropdownMenu: pass('DropdownMenu'),
  DropdownMenuTrigger: pass('DropdownMenuTrigger'),
  DropdownMenuContent: pass('DropdownMenuContent'),
  DropdownMenuItem: pass('DropdownMenuItem', 'button'),
  DropdownMenuSeparator: pass('DropdownMenuSeparator', 'hr'),
}))

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
vi.mock('@/components/ui/alert-dialog/AlertDialogAction.vue', () => ({ default: pass('AlertDialogAction', 'button') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogCancel.vue', () => ({ default: pass('AlertDialogCancel', 'button') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogContent.vue', () => ({ default: pass('AlertDialogContent') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogDescription.vue', () => ({ default: pass('AlertDialogDescription', 'p') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogFooter.vue', () => ({ default: pass('AlertDialogFooter') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogHeader.vue', () => ({ default: pass('AlertDialogHeader') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogTitle.vue', () => ({ default: pass('AlertDialogTitle', 'h2') }))

const baseDeviation = {
  id: 12,
  deviationType: 'BRUDD_SJENKETIDER',
  description: 'Servering fortsatte etter stengetid',
  status: 'UNDER_TREATMENT',
  reportedAt: '2026-04-08T10:00:00Z',
}

describe('AlcoholDeviationCard', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-04-08T12:00:00Z'))
  })

  it('renders mapped labels, tones and relative time', () => {
    const wrapper = mount(AlcoholDeviationCard, {
      props: { deviation: baseDeviation, canManage: true },
    })

    const badges = wrapper.findAll('[data-testid="badge"]')
    expect(badges).toHaveLength(3)
    expect(badges[0].text()).toBe('Brudd sjenketider')
    expect(badges[1].text()).toBe('IK-Alkohol')
    expect(badges[2].text()).toBe('Under behandling')
    expect(badges[2].attributes('data-tone')).toBe('warning')
    expect(wrapper.text()).toContain('2 timer siden')
  })

  it('emits open on click and enter', async () => {
    const wrapper = mount(AlcoholDeviationCard, {
      props: { deviation: baseDeviation, canManage: false },
    })

    await wrapper.get('.deviation-card').trigger('click')
    await wrapper.get('.deviation-card').trigger('keydown.enter')

    expect(wrapper.emitted('open')).toEqual([[baseDeviation], [baseDeviation]])
  })

  it('emits edit and status changes from menu actions', async () => {
    const wrapper = mount(AlcoholDeviationCard, {
      props: { deviation: baseDeviation, canManage: true },
    })

    await wrapper.findAll('button').find((b) => b.text().includes('Rediger'))!.trigger('click')
    await wrapper.findAll('button').find((b) => b.text().includes('Åpen'))!.trigger('click')
    await wrapper.findAll('button').find((b) => b.text().includes('Lukket'))!.trigger('click')

    expect(wrapper.emitted('edit')).toEqual([[baseDeviation]])
    expect(wrapper.emitted('update-status')).toEqual([
      [12, 'OPEN'],
      [12, 'CLOSED'],
    ])
  })

  it('opens delete dialog and emits delete', async () => {
    const wrapper = mount(AlcoholDeviationCard, {
      props: { deviation: baseDeviation, canManage: true },
    })

    await wrapper.findAll('button').find((b) => b.text().trim() === 'Slett')!.trigger('click')
    expect(wrapper.find('[data-testid="alert-dialog"]').exists()).toBe(true)

    await wrapper.findAll('button').filter((b) => b.text().trim() === 'Slett')[1].trigger('click')
    expect(wrapper.emitted('delete')).toEqual([[12]])
  })

  it('hides management actions when canManage is false', () => {
    const wrapper = mount(AlcoholDeviationCard, {
      props: { deviation: { ...baseDeviation, status: 'OPEN' }, canManage: false },
    })

    expect(wrapper.find('.card-actions').exists()).toBe(false)
    expect(wrapper.text()).not.toContain('Rediger')
  })

  it('renders minute and day based relative time branches', () => {
    const minuteWrapper = mount(AlcoholDeviationCard, {
      props: {
        deviation: { ...baseDeviation, reportedAt: '2026-04-08T11:45:00Z', status: 'OPEN' },
        canManage: false,
      },
    })
    expect(minuteWrapper.text()).toContain('15 min siden')

    const dayWrapper = mount(AlcoholDeviationCard, {
      props: {
        deviation: { ...baseDeviation, reportedAt: '2026-04-05T12:00:00Z', status: 'CLOSED' },
        canManage: false,
      },
    })
    expect(dayWrapper.text()).toContain('3 dager siden')
    expect(dayWrapper.findAll('[data-testid="badge"]')[2].attributes('data-tone')).toBe('ok')
  })
})

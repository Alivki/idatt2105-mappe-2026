import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import FoodDeviationCard from '../FoodDeviationListCard.vue'

function pass(name: string, tag = 'div') {
  return defineComponent({ name, inheritAttrs: false, setup(_, { attrs, slots }) { return () => h(tag, attrs, slots.default?.()) } })
}

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({ name, setup() { return () => h('svg', { 'data-icon': name }) } })
  return { MoreVertical: icon('MoreVertical'), Trash2: icon('Trash2'), Pencil: icon('Pencil'), CircleDot: icon('CircleDot'), Clock: icon('Clock'), CheckCircle2: icon('CheckCircle2') }
})
vi.mock('@/components/ui/badge/Badge.vue', () => ({ default: defineComponent({ props: { tone: { type: String, default: '' } }, setup(props, { slots }) { return () => h('span', { 'data-testid': 'badge', 'data-tone': props.tone }, slots.default?.()) } }) }))
vi.mock('@/components/ui/button/Button.vue', () => ({ default: defineComponent({ inheritAttrs: false, setup(_, { attrs, slots }) { return () => h('button', attrs, slots.default?.()) } }) }))
vi.mock('@/components/ui/dropdown-menu', () => ({ DropdownMenu: pass('DropdownMenu'), DropdownMenuTrigger: pass('DropdownMenuTrigger'), DropdownMenuContent: pass('DropdownMenuContent'), DropdownMenuItem: pass('DropdownMenuItem', 'button'), DropdownMenuSeparator: pass('DropdownMenuSeparator', 'hr') }))
vi.mock('@/components/ui/alert-dialog/AlertDialog.vue', () => ({ default: defineComponent({ props: { open: { type: Boolean, default: false } }, setup(props, { slots }) { return () => props.open ? h('div', { 'data-testid': 'alert-dialog' }, slots.default?.()) : null } }) }))
vi.mock('@/components/ui/alert-dialog/AlertDialogAction.vue', () => ({ default: pass('AlertDialogAction', 'button') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogCancel.vue', () => ({ default: pass('AlertDialogCancel', 'button') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogContent.vue', () => ({ default: pass('AlertDialogContent') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogDescription.vue', () => ({ default: pass('AlertDialogDescription', 'p') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogFooter.vue', () => ({ default: pass('AlertDialogFooter') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogHeader.vue', () => ({ default: pass('AlertDialogHeader') }))
vi.mock('@/components/ui/alert-dialog/AlertDialogTitle.vue', () => ({ default: pass('AlertDialogTitle', 'h2') }))

const baseDeviation = {
  id: 8,
  deviationType: 'PERSONLIG_HYGIENE',
  description: 'Ansatt manglet hårnett',
  status: 'OPEN',
  reportedAt: '2026-04-08T11:00:00Z',
}

describe('FoodDeviationCard', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-04-08T12:00:00Z'))
  })

  it('renders mapped labels and open badge tone', () => {
    const wrapper = mount(FoodDeviationCard, { props: { deviation: baseDeviation, canManage: true } })
    const badges = wrapper.findAll('[data-testid="badge"]')
    expect(badges[0].text()).toBe('Personlig hygiene')
    expect(badges[1].text()).toBe('IK-Mat')
    expect(badges[2].text()).toBe('Åpen')
    expect(badges[2].attributes('data-tone')).toBe('danger')
    expect(wrapper.text()).toContain('1 time siden')
  })

  it('emits open, edit and status updates', async () => {
    const wrapper = mount(FoodDeviationCard, { props: { deviation: { ...baseDeviation, status: 'UNDER_TREATMENT' }, canManage: true } })
    await wrapper.get('.deviation-card').trigger('click')
    await wrapper.findAll('button').find((b) => b.text().includes('Rediger'))!.trigger('click')
    await wrapper.findAll('button').find((b) => b.text().includes('Åpen'))!.trigger('click')
    await wrapper.findAll('button').find((b) => b.text().includes('Lukket'))!.trigger('click')
    expect(wrapper.emitted('open')).toBeTruthy()
    expect(wrapper.emitted('edit')).toEqual([[{ ...baseDeviation, status: 'UNDER_TREATMENT' }]])
    expect(wrapper.emitted('update-status')).toEqual([[8, 'OPEN'], [8, 'CLOSED']])
  })

  it('shows delete dialog and emits delete', async () => {
    const wrapper = mount(FoodDeviationCard, { props: { deviation: baseDeviation, canManage: true } })
    await wrapper.findAll('button').find((b) => b.text().trim() === 'Slett')!.trigger('click')
    expect(wrapper.find('[data-testid="alert-dialog"]').exists()).toBe(true)
    await wrapper.findAll('button').filter((b) => b.text().trim() === 'Slett')[1].trigger('click')
    expect(wrapper.emitted('delete')).toEqual([[8]])
  })

  it('renders day branch and closed tone', () => {
    const wrapper = mount(FoodDeviationCard, { props: { deviation: { ...baseDeviation, status: 'CLOSED', reportedAt: '2026-04-06T12:00:00Z' }, canManage: false } })
    expect(wrapper.text()).toContain('2 dager siden')
    expect(wrapper.findAll('[data-testid="badge"]')[2].attributes('data-tone')).toBe('ok')
    expect(wrapper.find('.card-actions').exists()).toBe(false)
  })
})

import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import StatusBadge from '../StatusBadge.vue'

// Mock the Badge UI component
const BadgeStub = {
  name: 'Badge',
  template: '<span :data-tone="tone" class="badge"><slot /></span>',
  props: ['tone'],
}

describe('StatusBadge', () => {
  const mountBadge = (status: string) =>
    mount(StatusBadge, {
      props: { status },
      global: { stubs: { Badge: BadgeStub } },
    })

  it('renders the status text', () => {
    const wrapper = mountBadge('Gyldig')
    expect(wrapper.text()).toBe('Gyldig')
  })

  it('applies "ok" tone for Gyldig', () => {
    const wrapper = mountBadge('Gyldig')
    expect(wrapper.find('[data-tone]').attributes('data-tone')).toBe('ok')
  })

  it('applies "warning" tone for Utløper snart', () => {
    const wrapper = mountBadge('Utløper snart')
    expect(wrapper.find('[data-tone]').attributes('data-tone')).toBe('warning')
  })

  it('applies "danger" tone for Mangler', () => {
    const wrapper = mountBadge('Mangler')
    expect(wrapper.find('[data-tone]').attributes('data-tone')).toBe('danger')
  })

  it('applies "danger" tone for Utgått', () => {
    const wrapper = mountBadge('Utgått')
    expect(wrapper.find('[data-tone]').attributes('data-tone')).toBe('danger')
  })

  it('applies "neutral" tone for unknown status', () => {
    const wrapper = mountBadge('Ukjent')
    expect(wrapper.find('[data-tone]').attributes('data-tone')).toBe('neutral')
  })

  it('applies "neutral" tone for empty string', () => {
    const wrapper = mountBadge('')
    expect(wrapper.find('[data-tone]').attributes('data-tone')).toBe('neutral')
  })

  it('renders status text as slot content inside Badge', () => {
    const wrapper = mountBadge('Utløper snart')
    expect(wrapper.find('.badge').text()).toBe('Utløper snart')
  })
})

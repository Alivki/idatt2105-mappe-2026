import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import CounterRow from '../IdCheckCounter.vue'

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) => defineComponent({
    name,
    setup() {
      return () => h('svg', { 'data-icon': name })
    },
  })

  return {
    Plus: icon('Plus'),
    Minus: icon('Minus'),
  }
})

describe('CounterRow', () => {
  it('renders label and current count', () => {
    const wrapper = mount(CounterRow, {
      props: {
        count: 3,
      },
    })

    expect(wrapper.text()).toContain('Legitimasjoner sjekket')
    expect(wrapper.text()).toContain('3')
  })

  it('emits increment when the plus button is clicked', async () => {
    const wrapper = mount(CounterRow, {
      props: {
        count: 1,
      },
    })

    const buttons = wrapper.findAll('button')
    const incrementButton = buttons[1]

    await incrementButton.trigger('click')

    expect(wrapper.emitted('increment')).toEqual([[]])
    expect(wrapper.emitted('decrement')).toBeUndefined()
  })

  it('emits decrement when the minus button is clicked and count is above zero', async () => {
    const wrapper = mount(CounterRow, {
      props: {
        count: 2,
      },
    })

    const buttons = wrapper.findAll('button')
    const decrementButton = buttons[0]

    expect((decrementButton.element as HTMLButtonElement).disabled).toBe(false)

    await decrementButton.trigger('click')

    expect(wrapper.emitted('decrement')).toEqual([[]])
    expect(wrapper.emitted('increment')).toBeUndefined()
  })

  it('disables decrement when count is zero', () => {
    const wrapper = mount(CounterRow, {
      props: {
        count: 0,
      },
    })

    const buttons = wrapper.findAll('button')
    const decrementButton = buttons[0]
    const incrementButton = buttons[1]

    expect((decrementButton.element as HTMLButtonElement).disabled).toBe(true)
    expect((incrementButton.element as HTMLButtonElement).disabled).toBe(false)
  })

  it('disables both buttons when disabled is true', () => {
    const wrapper = mount(CounterRow, {
      props: {
        count: 5,
        disabled: true,
      },
    })

    const buttons = wrapper.findAll('button')

    expect((buttons[0].element as HTMLButtonElement).disabled).toBe(true)
    expect((buttons[1].element as HTMLButtonElement).disabled).toBe(true)
  })
})

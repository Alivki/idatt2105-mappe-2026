import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, nextTick, ref} from 'vue'

import SwitchComponent from '../Switch.vue'

describe('Switch', () => {
  it('renders as a button with switch role', () => {
    const wrapper = mount(SwitchComponent)

    expect(wrapper.element.tagName).toBe('BUTTON')
    expect(wrapper.attributes('type')).toBe('button')
    expect(wrapper.attributes('role')).toBe('switch')
  })

  it('renders unchecked by default', () => {
    const wrapper = mount(SwitchComponent)

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')
    expect(wrapper.get('.switch__thumb').attributes('data-state')).toBe('unchecked')
  })

  it('uses defaultChecked=true when uncontrolled', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        defaultChecked: true,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.attributes('data-state')).toBe('checked')
    expect(wrapper.get('.switch__thumb').attributes('data-state')).toBe('checked')
  })

  it('uses checked prop when provided as controlled state', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        checked: true,
        defaultChecked: false,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.attributes('data-state')).toBe('checked')
  })

  it('prefers checked over defaultChecked when both are provided', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        checked: false,
        defaultChecked: true,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')
  })

  it('emits update:checked=true when clicked while unchecked', async () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        defaultChecked: false,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toEqual([[true]])
  })

  it('emits update:checked=false when clicked while checked', async () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        defaultChecked: true,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toEqual([[false]])
  })

  it('emits toggled value from controlled checked prop', async () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        checked: true,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toEqual([[false]])
  })

  it('does not change rendered state after click when uncontrolled because state is prop-derived', async () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        defaultChecked: false,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('false')

    await wrapper.trigger('click')

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')
  })

  it('updates rendered state when controlled prop changes', async () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        checked: false,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('false')

    await wrapper.setProps({checked: true})
    await nextTick()

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.attributes('data-state')).toBe('checked')
  })

  it('applies disabled attribute when disabled=true', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        disabled: true,
      },
    })

    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('does not render disabled attribute when disabled=false', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        disabled: false,
      },
    })

    expect(wrapper.attributes('disabled')).toBeUndefined()
  })

  it('does not emit when clicked while disabled', async () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        disabled: true,
        checked: false,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toBeUndefined()
  })

  it('forwards id prop', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        id: 'notifications-switch',
      },
    })

    expect(wrapper.attributes('id')).toBe('notifications-switch')
  })

  it('does not forward name prop to DOM in this implementation', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        name: 'notifications',
      },
    })

    expect(wrapper.attributes('name')).toBeUndefined()
  })

  it('does not forward value prop to DOM in this implementation', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        value: 'on',
      },
    })

    expect(wrapper.attributes('value')).toBeUndefined()
  })

  it('applies custom class together with base switch class', () => {
    const wrapper = mount(SwitchComponent, {
      props: {
        class: 'my-switch',
      },
    })

    expect(wrapper.classes()).toContain('switch')
    expect(wrapper.classes()).toContain('my-switch')
  })

  it('renders thumb slot content', () => {
    const wrapper = mount(SwitchComponent, {
      slots: {
        thumb: '<span data-test="thumb-content">T</span>',
      },
    })

    expect(wrapper.find('[data-test="thumb-content"]').exists()).toBe(true)
    expect(wrapper.get('.switch__thumb').text()).toContain('T')
  })

  it('works with v-model in a parent component', async () => {
    const Host = defineComponent({
      components: {AppSwitch: SwitchComponent},
      setup() {
        const checked = ref(false)
        return {checked}
      },
      template: `<AppSwitch v-model:checked="checked" data-test="switch" />`,
    })

    const wrapper = mount(Host)
    const switchEl = wrapper.get('[data-test="switch"]')

    expect((wrapper.vm as { checked: boolean }).checked).toBe(false)
    expect(switchEl.attributes('aria-checked')).toBe('false')

    await switchEl.trigger('click')
    await nextTick()

    expect((wrapper.vm as { checked: boolean }).checked).toBe(true)
    expect(switchEl.attributes('aria-checked')).toBe('true')
  })
})

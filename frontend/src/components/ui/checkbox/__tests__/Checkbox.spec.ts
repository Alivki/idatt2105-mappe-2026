import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import Checkbox from '../Checkbox.vue'

describe('Checkbox', () => {
  it('renders as a button with checkbox role', () => {
    const wrapper = mount(Checkbox)

    expect(wrapper.element.tagName).toBe('BUTTON')
    expect(wrapper.attributes('type')).toBe('button')
    expect(wrapper.attributes('role')).toBe('checkbox')
  })

  it('renders unchecked by default', () => {
    const wrapper = mount(Checkbox)

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')
    expect(wrapper.find('.checkbox__indicator').exists()).toBe(false)
  })

  it('uses defaultChecked=true when uncontrolled', () => {
    const wrapper = mount(Checkbox, {
      props: {
        defaultChecked: true,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.attributes('data-state')).toBe('checked')
    expect(wrapper.find('.checkbox__indicator').exists()).toBe(true)
    expect(wrapper.find('.checkbox__icon').exists()).toBe(true)
  })

  it('uses checked prop when provided as controlled state', () => {
    const wrapper = mount(Checkbox, {
      props: {
        checked: true,
        defaultChecked: false,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.attributes('data-state')).toBe('checked')
    expect(wrapper.find('.checkbox__indicator').exists()).toBe(true)
  })

  it('prefers checked over defaultChecked when both are provided', () => {
    const wrapper = mount(Checkbox, {
      props: {
        checked: false,
        defaultChecked: true,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')
    expect(wrapper.find('.checkbox__indicator').exists()).toBe(false)
  })

  it('emits update:checked=true when clicked while unchecked', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        defaultChecked: false,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toEqual([[true]])
  })

  it('emits update:checked=false when clicked while checked', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        defaultChecked: true,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toEqual([[false]])
  })

  it('emits toggled value from controlled checked prop', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        checked: true,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toEqual([[false]])
  })

  it('does not change rendered state after click when uncontrolled because state is prop-derived', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        defaultChecked: false,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')

    await wrapper.trigger('click')

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')
  })

  it('updates rendered state when controlled prop changes', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        checked: false,
      },
    })

    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('data-state')).toBe('unchecked')

    await wrapper.setProps({ checked: true })

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.attributes('data-state')).toBe('checked')
    expect(wrapper.find('.checkbox__indicator').exists()).toBe(true)
  })

  it('applies disabled attribute when disabled=true', () => {
    const wrapper = mount(Checkbox, {
      props: {
        disabled: true,
      },
    })

    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('does not render disabled attribute when disabled=false', () => {
    const wrapper = mount(Checkbox, {
      props: {
        disabled: false,
      },
    })

    expect(wrapper.attributes('disabled')).toBeUndefined()
  })

  it('does not emit when clicked while disabled', async () => {
    const wrapper = mount(Checkbox, {
      props: {
        disabled: true,
        checked: false,
      },
    })

    await wrapper.trigger('click')

    expect(wrapper.emitted('update:checked')).toBeUndefined()
  })

  it('forwards id attribute', () => {
    const wrapper = mount(Checkbox, {
      props: {
        id: 'terms-checkbox',
      },
    })

    expect(wrapper.attributes('id')).toBe('terms-checkbox')
  })

  it('applies custom class together with base checkbox class', () => {
    const wrapper = mount(Checkbox, {
      props: {
        class: 'my-checkbox',
      },
    })

    expect(wrapper.classes()).toContain('checkbox')
    expect(wrapper.classes()).toContain('my-checkbox')
  })

  it('renders custom slot instead of default icon when checked', () => {
    const wrapper = mount(Checkbox, {
      props: {
        checked: true,
      },
      slots: {
        default: '<span data-test="custom-indicator">X</span>',
      },
    })

    expect(wrapper.find('.checkbox__indicator').exists()).toBe(true)
    expect(wrapper.find('[data-test="custom-indicator"]').exists()).toBe(true)
    expect(wrapper.find('.checkbox__icon').exists()).toBe(false)
  })

  it('does not render slot content when unchecked', () => {
    const wrapper = mount(Checkbox, {
      props: {
        checked: false,
      },
      slots: {
        default: '<span data-test="custom-indicator">X</span>',
      },
    })

    expect(wrapper.find('[data-test="custom-indicator"]').exists()).toBe(false)
    expect(wrapper.find('.checkbox__indicator').exists()).toBe(false)
  })
})

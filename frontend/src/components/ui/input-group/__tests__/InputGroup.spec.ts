import {describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'

import InputGroup from '../InputGroup.vue'
import InputGroupAddon from '../InputGroupAddon.vue'
import InputGroupButton from '../InputGroupButton.vue'
import InputGroupText from '../InputGroupText.vue'

describe('InputGroup', () => {
  it('renders slot content', () => {
    const wrapper = mount(InputGroup, {
      slots: {
        default: '<div data-test="child">Child</div>',
      },
    })

    expect(wrapper.find('[data-test="child"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Child')
  })

  it('renders as a div with group role', () => {
    const wrapper = mount(InputGroup)

    expect(wrapper.element.tagName).toBe('DIV')
    expect(wrapper.attributes('role')).toBe('group')
    expect(wrapper.attributes('data-slot')).toBe('input-group')
  })

  it('applies base class and custom class', () => {
    const wrapper = mount(InputGroup, {
      props: {
        class: 'custom-group',
      },
    })

    expect(wrapper.classes()).toContain('input-group')
    expect(wrapper.classes()).toContain('custom-group')
  })

  it('sets data-disabled when disabled=true', () => {
    const wrapper = mount(InputGroup, {
      props: {
        disabled: true,
      },
    })

    expect(wrapper.attributes('data-disabled')).toBe('true')
  })

  it('does not set data-disabled when disabled=false', () => {
    const wrapper = mount(InputGroup, {
      props: {
        disabled: false,
      },
    })

    expect(wrapper.attributes('data-disabled')).toBeUndefined()
  })
})

describe('InputGroupAddon', () => {
  it('renders slot content and default align', () => {
    const wrapper = mount(InputGroupAddon, {
      slots: {
        default: 'kr',
      },
    })

    expect(wrapper.attributes('role')).toBe('group')
    expect(wrapper.attributes('data-slot')).toBe('input-group-addon')
    expect(wrapper.attributes('data-align')).toBe('inline-start')
    expect(wrapper.classes()).toContain('input-group-addon')
    expect(wrapper.classes()).toContain('input-group-addon--inline-start')
    expect(wrapper.text()).toContain('kr')
  })

  it('applies custom align and class', () => {
    const wrapper = mount(InputGroupAddon, {
      props: {
        align: 'block-end',
        class: 'addon-extra',
      },
      slots: {
        default: 'Text',
      },
    })

    expect(wrapper.attributes('data-align')).toBe('block-end')
    expect(wrapper.classes()).toContain('input-group-addon--block-end')
    expect(wrapper.classes()).toContain('addon-extra')
  })

  it('focuses sibling input when clicked outside a button', async () => {
    const focusSpy = vi.fn()

    const wrapper = mount({
      components: {InputGroupAddon},
      template: `
        <div>
          <InputGroupAddon data-test="addon">Prefix</InputGroupAddon>
          <input data-test="input" />
        </div>
      `,
    })

    const input = wrapper.get('[data-test="input"]').element as HTMLInputElement
    input.focus = focusSpy

    await wrapper.get('[data-test="addon"]').trigger('click')

    expect(focusSpy).toHaveBeenCalledTimes(1)
  })

  it('does not focus sibling input when click target is inside a button', async () => {
    const focusSpy = vi.fn()

    const wrapper = mount({
      components: {InputGroupAddon},
      template: `
        <div>
          <InputGroupAddon data-test="addon">
            <button data-test="inner-button">Click</button>
          </InputGroupAddon>
          <input data-test="input" />
        </div>
      `,
    })

    const input = wrapper.get('[data-test="input"]').element as HTMLInputElement
    input.focus = focusSpy

    await wrapper.get('[data-test="inner-button"]').trigger('click')

    expect(focusSpy).not.toHaveBeenCalled()
  })
})

describe('InputGroupButton', () => {
  it('renders as a button with base class', () => {
    const wrapper = mount(InputGroupButton, {
      slots: {
        default: 'X',
      },
    })

    expect(wrapper.element.tagName).toBe('BUTTON')
    expect(wrapper.attributes('type')).toBe('button')
    expect(wrapper.classes()).toContain('input-group-button')
    expect(wrapper.text()).toContain('X')
  })

  it('applies custom class', () => {
    const wrapper = mount(InputGroupButton, {
      props: {
        class: 'button-extra',
      },
    })

    expect(wrapper.classes()).toContain('input-group-button')
    expect(wrapper.classes()).toContain('button-extra')
  })

  it('sets disabled attribute when disabled=true', () => {
    const wrapper = mount(InputGroupButton, {
      props: {
        disabled: true,
      },
    })

    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('does not set disabled attribute when disabled=false', () => {
    const wrapper = mount(InputGroupButton, {
      props: {
        disabled: false,
      },
    })

    expect(wrapper.attributes('disabled')).toBeUndefined()
  })
})

describe('InputGroupText', () => {
  it('renders as a span with base class and slot content', () => {
    const wrapper = mount(InputGroupText, {
      slots: {
        default: 'Label',
      },
    })

    expect(wrapper.element.tagName).toBe('SPAN')
    expect(wrapper.classes()).toContain('input-group-text')
    expect(wrapper.text()).toContain('Label')
  })

  it('applies custom class', () => {
    const wrapper = mount(InputGroupText, {
      props: {
        class: 'text-extra',
      },
      slots: {
        default: 'Hello',
      },
    })

    expect(wrapper.classes()).toContain('input-group-text')
    expect(wrapper.classes()).toContain('text-extra')
  })
})

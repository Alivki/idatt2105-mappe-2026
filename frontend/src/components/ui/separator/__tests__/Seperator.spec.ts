import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import Separator from '../Separator.vue'

describe('Separator', () => {
  it('renders as a div by default', () => {
    const wrapper = mount(Separator)

    expect(wrapper.element.tagName).toBe('DIV')
  })

  it('renders with role="none" by default', () => {
    const wrapper = mount(Separator)

    expect(wrapper.attributes('role')).toBe('none')
  })

  it('applies base separator class', () => {
    const wrapper = mount(Separator)

    expect(wrapper.classes()).toContain('separator')
  })

  it('applies horizontal class by default', () => {
    const wrapper = mount(Separator)

    expect(wrapper.classes()).toContain('separator--horizontal')
    expect(wrapper.classes()).not.toContain('separator--vertical')
  })

  it('applies vertical orientation correctly', () => {
    const wrapper = mount(Separator, {
      props: {
        orientation: 'vertical',
      },
    })

    expect(wrapper.classes()).toContain('separator--vertical')
    expect(wrapper.classes()).not.toContain('separator--horizontal')
  })

  it('applies decorative mode correctly', () => {
    const wrapper = mount(Separator, {
      props: {
        decorative: true,
      },
    })

    expect(wrapper.attributes('role')).toBe('none')
    expect(wrapper.attributes('aria-orientation')).toBeUndefined()
  })

  it('uses separator role when decorative is false', () => {
    const wrapper = mount(Separator, {
      props: {
        decorative: false,
      },
    })

    expect(wrapper.attributes('role')).toBe('separator')
  })

  it('applies custom class together with base class', () => {
    const wrapper = mount(Separator, {
      props: {
        class: 'custom-separator',
      },
    })

    expect(wrapper.classes()).toContain('separator')
    expect(wrapper.classes()).toContain('custom-separator')
  })

  it('applies custom class together with vertical orientation', () => {
    const wrapper = mount(Separator, {
      props: {
        orientation: 'vertical',
        class: 'extra-vertical',
      },
    })

    expect(wrapper.classes()).toContain('separator')
    expect(wrapper.classes()).toContain('separator--vertical')
    expect(wrapper.classes()).toContain('extra-vertical')
  })
})

import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import Skeleton from '../Skeleton.vue'

describe('Skeleton', () => {
  it('renders as a div', () => {
    const wrapper = mount(Skeleton)

    expect(wrapper.element.tagName).toBe('DIV')
  })

  it('applies base skeleton class by default', () => {
    const wrapper = mount(Skeleton)

    expect(wrapper.classes()).toContain('skeleton')
  })

  it('applies custom class together with base class', () => {
    const wrapper = mount(Skeleton, {
      props: {
        class: 'custom-skeleton',
      },
    })

    expect(wrapper.classes()).toContain('skeleton')
    expect(wrapper.classes()).toContain('custom-skeleton')
  })

  it('renders with no text content', () => {
    const wrapper = mount(Skeleton)

    expect(wrapper.text()).toBe('')
  })

  it('keeps base class when custom class is empty string', () => {
    const wrapper = mount(Skeleton, {
      props: {
        class: '',
      },
    })

    expect(wrapper.classes()).toContain('skeleton')
  })
})

import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'

import Spinner from '../Spinner.vue'

describe('Spinner', () => {
  it('renders as an svg', () => {
    const wrapper = mount(Spinner)

    expect(wrapper.element.tagName.toLowerCase()).toBe('svg')
  })

  it('has accessibility attributes', () => {
    const wrapper = mount(Spinner)

    expect(wrapper.attributes('role')).toBe('status')
    expect(wrapper.attributes('aria-label')).toBe('Loading')
  })

  it('applies base spinner class', () => {
    const wrapper = mount(Spinner)

    expect(wrapper.classes()).toContain('spinner')
  })

  it('applies custom class together with base class', () => {
    const wrapper = mount(Spinner, {
      props: {
        class: 'spinner-extra',
      },
    })

    expect(wrapper.classes()).toContain('spinner')
    expect(wrapper.classes()).toContain('spinner-extra')
  })

  it('renders expected svg attributes', () => {
    const wrapper = mount(Spinner)

    expect(wrapper.attributes('xmlns')).toBe('http://www.w3.org/2000/svg')
    expect(wrapper.attributes('viewBox')).toBe('0 0 24 24')
    expect(wrapper.attributes('fill')).toBe('none')
    expect(wrapper.attributes('stroke')).toBe('currentColor')
    expect(wrapper.attributes('stroke-width')).toBe('2')
    expect(wrapper.attributes('stroke-linecap')).toBe('round')
    expect(wrapper.attributes('stroke-linejoin')).toBe('round')
  })

  it('renders a single path with the expected d attribute', () => {
    const wrapper = mount(Spinner)

    const path = wrapper.get('path')
    expect(path.exists()).toBe(true)
    expect(path.attributes('d')).toBe('M21 12a9 9 0 1 1-6.219-8.56')
  })

  it('has no text content', () => {
    const wrapper = mount(Spinner)

    expect(wrapper.text()).toBe('')
  })
})

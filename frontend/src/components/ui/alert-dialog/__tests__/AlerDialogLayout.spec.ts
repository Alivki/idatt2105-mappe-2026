import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import AlertDialogTitle from '../AlertDialogTitle.vue'
import AlertDialogDescription from '../AlertDialogDescription.vue'
import AlertDialogHeader from '../AlertDialogHeader.vue'
import AlertDialogFooter from '../AlertDialogFooter.vue'

describe('AlertDialog presentational components', () => {
  it('renders title slot and class', () => {
    const wrapper = mount(AlertDialogTitle, {
      props: { class: 'custom-title' },
      slots: { default: 'Bekreft sletting' },
    })

    expect(wrapper.element.tagName).toBe('H2')
    expect(wrapper.text()).toContain('Bekreft sletting')
    expect(wrapper.classes()).toContain('alert-title')
    expect(wrapper.classes()).toContain('custom-title')
  })

  it('renders description slot and class', () => {
    const wrapper = mount(AlertDialogDescription, {
      props: { class: 'custom-description' },
      slots: { default: 'Denne handlingen kan ikke angres.' },
    })

    expect(wrapper.element.tagName).toBe('P')
    expect(wrapper.text()).toContain('Denne handlingen kan ikke angres.')
    expect(wrapper.classes()).toContain('alert-description')
    expect(wrapper.classes()).toContain('custom-description')
  })

  it('renders header slot and class', () => {
    const wrapper = mount(AlertDialogHeader, {
      props: { class: 'custom-header' },
      slots: { default: '<span data-test="header-child">Innhold</span>' },
    })

    expect(wrapper.find('[data-test="header-child"]').exists()).toBe(true)
    expect(wrapper.classes()).toContain('alert-header')
    expect(wrapper.classes()).toContain('custom-header')
  })

  it('renders footer slot and class', () => {
    const wrapper = mount(AlertDialogFooter, {
      props: { class: 'custom-footer' },
      slots: { default: '<button data-test="footer-child">OK</button>' },
    })

    expect(wrapper.find('[data-test="footer-child"]').exists()).toBe(true)
    expect(wrapper.classes()).toContain('alert-footer')
    expect(wrapper.classes()).toContain('custom-footer')
  })

})

import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import SelectGroup from '../SelectGroup.vue'
import SelectLabel from '../SelectLabel.vue'
import SelectSeparator from '../SelectSeparator.vue'

describe('Select layout components', () => {
  it('SelectGroup renders role, class and slot content', () => {
    const wrapper = mount(SelectGroup, {
      props: {
        class: 'group-extra',
      },
      slots: {
        default: '<div data-test="child">Child</div>',
      },
    })

    expect(wrapper.element.tagName).toBe('DIV')
    expect(wrapper.attributes('role')).toBe('group')
    expect(wrapper.classes()).toContain('select-group')
    expect(wrapper.classes()).toContain('group-extra')
    expect(wrapper.find('[data-test="child"]').exists()).toBe(true)
  })

  it('SelectLabel renders class and slot content', () => {
    const wrapper = mount(SelectLabel, {
      props: {
        class: 'label-extra',
      },
      slots: {
        default: 'Fruits',
      },
    })

    expect(wrapper.element.tagName).toBe('DIV')
    expect(wrapper.classes()).toContain('select-label')
    expect(wrapper.classes()).toContain('label-extra')
    expect(wrapper.text()).toContain('Fruits')
  })

  it('SelectSeparator renders base class and custom class', () => {
    const wrapper = mount(SelectSeparator, {
      props: {
        class: 'separator-extra',
      },
    })

    expect(wrapper.element.tagName).toBe('DIV')
    expect(wrapper.classes()).toContain('select-separator')
    expect(wrapper.classes()).toContain('separator-extra')
  })
})

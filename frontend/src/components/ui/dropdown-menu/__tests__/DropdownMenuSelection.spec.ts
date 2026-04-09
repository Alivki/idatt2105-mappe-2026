import {describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent} from 'vue'

import DropdownMenuCheckboxItem from '../DropdownMenuCheckboxItem.vue'
import DropdownMenuRadioGroup from '../DropdownMenuRadioGroup.vue'
import DropdownMenuRadioItem from '../DropdownMenuRadioItem.vue'

vi.mock('lucide-vue-next', () => ({
  Check: {
    name: 'CheckIconStub',
    template: '<svg class="dropdown-checkbox-icon" data-test="check-icon" />',
  },
  Circle: {
    name: 'CircleIconStub',
    template: '<svg class="dropdown-radio-icon" data-test="circle-icon" />',
  },
}))

describe('DropdownMenuCheckboxItem', () => {
  it('renders unchecked state by default', () => {
    const wrapper = mount(DropdownMenuCheckboxItem, {
      slots: {default: 'Option'},
    })

    expect(wrapper.attributes('role')).toBe('menuitemcheckbox')
    expect(wrapper.attributes('aria-checked')).toBe('false')
    expect(wrapper.attributes('tabindex')).toBe('0')
    expect(wrapper.find('[data-test="check-icon"]').exists()).toBe(false)
    expect(wrapper.text()).toContain('Option')
  })

  it('renders checked state with icon', () => {
    const wrapper = mount(DropdownMenuCheckboxItem, {
      props: {checked: true, class: 'extra-checkbox'},
      slots: {default: 'Option'},
    })

    expect(wrapper.attributes('aria-checked')).toBe('true')
    expect(wrapper.classes()).toContain('extra-checkbox')
    expect(wrapper.find('[data-test="check-icon"]').exists()).toBe(true)
  })

  it('emits toggled checked value on click', async () => {
    const wrapper = mount(DropdownMenuCheckboxItem, {
      props: {checked: false},
    })

    await wrapper.trigger('click')
    expect(wrapper.emitted('update:checked')).toEqual([[true]])
  })

  it('does not emit when disabled and applies disabled state', async () => {
    const wrapper = mount(DropdownMenuCheckboxItem, {
      props: {checked: true, disabled: true, class: 'disabled-extra'},
    })

    expect(wrapper.attributes('tabindex')).toBe('-1')
    expect(wrapper.classes()).toContain('dropdown-checkbox-item--disabled')
    expect(wrapper.classes()).toContain('disabled-extra')

    await wrapper.trigger('click')
    expect(wrapper.emitted('update:checked')).toBeUndefined()
  })
})

describe('DropdownMenuRadioGroup and DropdownMenuRadioItem', () => {
  it('renders group and items with correct checked state', () => {
    const Host = defineComponent({
      components: {DropdownMenuRadioGroup, DropdownMenuRadioItem},
      template: `
        <DropdownMenuRadioGroup modelValue="b">
          <DropdownMenuRadioItem value="a" data-test="a">A</DropdownMenuRadioItem>
          <DropdownMenuRadioItem value="b" data-test="b">B</DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      `,
    })

    const wrapper = mount(Host)
    const a = wrapper.get('[data-test="a"]')
    const b = wrapper.get('[data-test="b"]')

    expect(a.attributes('role')).toBe('menuitemradio')
    expect(a.attributes('aria-checked')).toBe('false')
    expect(b.attributes('aria-checked')).toBe('true')
    expect(a.find('[data-test="circle-icon"]').exists()).toBe(false)
    expect(b.find('[data-test="circle-icon"]').exists()).toBe(true)
  })

  it('emits selected value when non-disabled item is clicked', async () => {
    const Host = defineComponent({
      components: {DropdownMenuRadioGroup, DropdownMenuRadioItem},
      template: `
        <DropdownMenuRadioGroup modelValue="a">
          <DropdownMenuRadioItem value="b" data-test="b">B</DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      `,
    })

    const wrapper = mount(Host)
    await wrapper.get('[data-test="b"]').trigger('click')

    const group = wrapper.findComponent(DropdownMenuRadioGroup)
    expect(group.emitted('update:modelValue')).toEqual([['b']])
  })

  it('does not emit when disabled and applies disabled state', async () => {
    const Host = defineComponent({
      components: {DropdownMenuRadioGroup, DropdownMenuRadioItem},
      template: `
        <DropdownMenuRadioGroup modelValue="a">
          <DropdownMenuRadioItem value="b" disabled class="extra-disabled" data-test="b">B</DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      `,
    })

    const wrapper = mount(Host)
    const item = wrapper.get('[data-test="b"]')

    expect(item.attributes('tabindex')).toBe('-1')
    expect(item.classes()).toContain('dropdown-radio-item--disabled')
    expect(item.classes()).toContain('extra-disabled')

    await item.trigger('click')
    const group = wrapper.findComponent(DropdownMenuRadioGroup)
    expect(group.emitted('update:modelValue')).toBeUndefined()
  })
})

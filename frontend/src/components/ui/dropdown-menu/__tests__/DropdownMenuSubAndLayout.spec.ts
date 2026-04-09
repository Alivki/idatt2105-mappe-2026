import {afterEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, nextTick} from 'vue'

import DropdownMenuGroup from '../DropdownMenuGroup.vue'
import DropdownMenuLabel from '../DropdownMenuLabel.vue'
import DropdownMenuSeparator from '../DropdownMenuSeparator.vue'
import DropdownMenuShortcut from '../DropdownMenuShortcut.vue'
import DropdownMenuSub from '../DropdownMenuSub.vue'
import DropdownMenuSubTrigger from '../DropdownMenuSubTrigger.vue'
import DropdownMenuSubContent from '../DropdownMenuSubContent.vue'

vi.mock('lucide-vue-next', () => ({
  ChevronRight: {
    name: 'ChevronRightStub',
    template: '<svg class="dropdown-sub-chevron" data-test="chevron-right" />',
  },
}))

afterEach(() => {
  document.body.innerHTML = ''
})

describe('DropdownMenu simple wrappers', () => {
  it('group renders role and custom class', () => {
    const wrapper = mount(DropdownMenuGroup, {
      props: {class: 'group-extra'},
      slots: {default: '<span data-test="child">Child</span>'},
    })

    expect(wrapper.attributes('role')).toBe('group')
    expect(wrapper.classes()).toContain('group-extra')
    expect(wrapper.find('[data-test="child"]').exists()).toBe(true)
  })

  it('label renders inset and custom class', () => {
    const wrapper = mount(DropdownMenuLabel, {
      props: {inset: true, class: 'label-extra'},
      slots: {default: 'Label'},
    })

    expect(wrapper.classes()).toContain('dropdown-label')
    expect(wrapper.classes()).toContain('dropdown-label--inset')
    expect(wrapper.classes()).toContain('label-extra')
    expect(wrapper.text()).toContain('Label')
  })

  it('separator renders role and class', () => {
    const wrapper = mount(DropdownMenuSeparator, {
      props: {class: 'separator-extra'},
    })

    expect(wrapper.attributes('role')).toBe('separator')
    expect(wrapper.classes()).toContain('dropdown-separator')
    expect(wrapper.classes()).toContain('separator-extra')
  })

  it('shortcut renders slot and class', () => {
    const wrapper = mount(DropdownMenuShortcut, {
      props: {class: 'shortcut-extra'},
      slots: {default: '⌘K'},
    })

    expect(wrapper.element.tagName).toBe('SPAN')
    expect(wrapper.classes()).toContain('dropdown-shortcut')
    expect(wrapper.classes()).toContain('shortcut-extra')
    expect(wrapper.text()).toContain('⌘K')
  })
})

describe('DropdownMenu sub components', () => {
  it('sub trigger opens on mouseenter and closes on mouseleave', async () => {
    const Host = defineComponent({
      components: {DropdownMenuSub, DropdownMenuSubTrigger, DropdownMenuSubContent},
      template: `
        <DropdownMenuSub>
          <DropdownMenuSubTrigger class="trigger-extra" data-test="trigger">More</DropdownMenuSubTrigger>
          <DropdownMenuSubContent class="content-extra">
            <div data-test="sub-content">Sub menu</div>
          </DropdownMenuSubContent>
        </DropdownMenuSub>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    const trigger = wrapper.get('[data-test="trigger"]')
    expect(trigger.classes()).toContain('dropdown-sub-trigger')
    expect(trigger.classes()).toContain('trigger-extra')
    expect(trigger.classes()).not.toContain('dropdown-sub-trigger--open')
    expect(trigger.find('[data-test="chevron-right"]').exists()).toBe(true)

    await trigger.trigger('mouseenter')
    await nextTick()

    expect(trigger.classes()).toContain('dropdown-sub-trigger--open')

    const content = wrapper.get('[data-test="sub-content"]').element.parentElement as HTMLDivElement
    expect(content.className).toContain('dropdown-sub-content')
    expect(content.className).toContain('content-extra')
    expect(content.getAttribute('role')).toBe('menu')

    await trigger.trigger('mouseleave')
    await nextTick()

    expect(wrapper.find('[data-test="sub-content"]').exists()).toBe(false)
  })

  it('sub content stays open on mouseenter and remains rendered after content mouseleave', async () => {
    const Host = defineComponent({
      components: {DropdownMenuSub, DropdownMenuSubTrigger, DropdownMenuSubContent},
      template: `
        <DropdownMenuSub>
          <DropdownMenuSubTrigger data-test="trigger">More</DropdownMenuSubTrigger>
          <DropdownMenuSubContent data-test="content-root">
            <div data-test="sub-content">Sub menu</div>
          </DropdownMenuSubContent>
        </DropdownMenuSub>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    await wrapper.get('[data-test="trigger"]').trigger('mouseenter')
    await nextTick()

    const contentRoot = wrapper.get('[data-test="content-root"]')
    await contentRoot.trigger('mouseenter')
    await nextTick()

    expect(wrapper.find('[data-test="sub-content"]').exists()).toBe(true)

    await contentRoot.trigger('mouseleave')
    await nextTick()

    expect(wrapper.find('[data-test="sub-content"]').exists()).toBe(true)
  })

  it('clicking outside sub content closes it, but clicking inside sub does not', async () => {
    const Host = defineComponent({
      components: {DropdownMenuSub, DropdownMenuSubTrigger, DropdownMenuSubContent},
      template: `
        <DropdownMenuSub>
          <DropdownMenuSubTrigger data-test="trigger">More</DropdownMenuSubTrigger>
          <DropdownMenuSubContent data-test="content-root">
            <div data-test="sub-content">Sub menu</div>
          </DropdownMenuSubContent>
        </DropdownMenuSub>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {stubs: {transition: true}},
    })

    await wrapper.get('[data-test="trigger"]').trigger('mouseenter')
    await nextTick()
    expect(wrapper.find('[data-test="sub-content"]').exists()).toBe(true)

    wrapper.get('[data-test="trigger"]').element.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}))
    await nextTick()
    expect(wrapper.find('[data-test="sub-content"]').exists()).toBe(true)

    document.dispatchEvent(new MouseEvent('mousedown', {bubbles: true}))
    await nextTick()
    expect(wrapper.find('[data-test="sub-content"]').exists()).toBe(false)

    wrapper.unmount()
  })
})

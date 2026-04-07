import { afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick } from 'vue'

import Popover from '../Popover.vue'
import PopoverTrigger from '../PopoverTrigger.vue'
import PopoverContent from '../PopoverContent.vue'

afterEach(() => {
  document.body.innerHTML = ''
})

describe('PopoverContent', () => {
  it('renders slot content', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent>
            <div data-test="inner">Body</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      global: { stubs: { transition: true } },
    })

    await nextTick()

    expect(wrapper.find('[data-test="inner"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Body')
  })

  it('applies base class and default center alignment', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent class="extra-content">
            Content
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      global: { stubs: { transition: true } },
    })

    await nextTick()

    const content = wrapper.get('.popover-content')
    expect(content.classes()).toContain('popover-content')
    expect(content.classes()).toContain('popover-content--center')
    expect(content.classes()).toContain('extra-content')
  })

  it('applies start alignment class', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent align="start">
            Content
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      global: { stubs: { transition: true } },
    })

    await nextTick()

    expect(wrapper.get('.popover-content').classes()).toContain('popover-content--start')
  })

  it('applies end alignment class', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent align="end">
            Content
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      global: { stubs: { transition: true } },
    })

    await nextTick()

    expect(wrapper.get('.popover-content').classes()).toContain('popover-content--end')
  })

  it('does not close on Escape in this implementation', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: { stubs: { transition: true } },
    })

    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('does not close on non-Escape key', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: { stubs: { transition: true } },
    })

    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }))
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('does not close on outside click in this implementation', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverTrigger, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverTrigger>
            <button data-test="trigger">Trigger</button>
          </PopoverTrigger>
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: { stubs: { transition: true } },
    })

    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    document.body.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('does not close when clicking inside popover content', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: { stubs: { transition: true } },
    })

    await nextTick()

    await wrapper.get('[data-test="content"]').trigger('mousedown')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('does not close when clicking inside the popover root', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverTrigger, PopoverContent },
      template: `
        <Popover :defaultOpen="true">
          <PopoverTrigger>
            <button data-test="trigger">Trigger</button>
          </PopoverTrigger>
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: { stubs: { transition: true } },
    })

    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await wrapper.get('[data-test="trigger"]').trigger('mousedown')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('removes content when popover closes through trigger toggle', async () => {
    const Host = defineComponent({
      components: { Popover, PopoverTrigger, PopoverContent },
      template: `
        <Popover>
          <PopoverTrigger>
            <button data-test="trigger">Toggle</button>
          </PopoverTrigger>
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(Host, {
      global: { stubs: { transition: true } },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
  })
})

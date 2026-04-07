import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref } from 'vue'

import Collapsible from '../Collapsible.vue'
import CollapsibleTrigger from '../CollapsibleTrigger.vue'

describe('Collapsible', () => {
  it('renders slot content', () => {
    const wrapper = mount(Collapsible, {
      slots: {
        default: '<div data-test="content">Hello</div>',
      },
    })

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hello')
  })

  it('forwards custom class to root element', () => {
    const wrapper = mount(Collapsible, {
      props: {
        class: 'custom-root',
      },
    })

    expect(wrapper.classes()).toContain('custom-root')
  })

  it('forwards attrs to root element', () => {
    const wrapper = mount(Collapsible, {
      attrs: {
        id: 'my-collapsible',
        'data-test': 'root',
      },
    })

    expect(wrapper.attributes('id')).toBe('my-collapsible')
    expect(wrapper.attributes('data-test')).toBe('root')
  })

  it('uses defaultOpen=false by default', () => {
    const Host = defineComponent({
      components: { Collapsible, CollapsibleTrigger },
      template: `
        <Collapsible>
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const triggerRoot = wrapper.findComponent(CollapsibleTrigger)

    expect(triggerRoot.attributes('data-state')).toBe('closed')
  })

  it('uses defaultOpen=true when provided', () => {
    const Host = defineComponent({
      components: { Collapsible, CollapsibleTrigger },
      template: `
        <Collapsible :defaultOpen="true">
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const triggerRoot = wrapper.findComponent(CollapsibleTrigger)

    expect(triggerRoot.attributes('data-state')).toBe('open')
  })

  it('passes slot prop open=false by default', () => {
    const wrapper = mount(Collapsible, {
      slots: {
        default: `
          <template #default="{ open }">
            <span data-test="slot-open">{{ open }}</span>
          </template>
        `,
      },
    })

    expect(wrapper.get('[data-test="slot-open"]').text()).toBe('false')
  })

  it('passes slot prop open=true when defaultOpen=true', () => {
    const wrapper = mount(Collapsible, {
      props: {
        defaultOpen: true,
      },
      slots: {
        default: `
          <template #default="{ open }">
            <span data-test="slot-open">{{ open }}</span>
          </template>
        `,
      },
    })

    expect(wrapper.get('[data-test="slot-open"]').text()).toBe('true')
  })

  it('toggles internal state and emits update:open when trigger is clicked', async () => {
    const Host = defineComponent({
      components: { Collapsible, CollapsibleTrigger },
      template: `
        <Collapsible>
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const triggerButton = wrapper.get('[data-test="trigger"]')
    const collapsible = wrapper.findComponent(Collapsible)
    const triggerRoot = wrapper.findComponent(CollapsibleTrigger)

    expect(triggerRoot.attributes('data-state')).toBe('closed')

    await triggerButton.trigger('click')
    await nextTick()

    expect(triggerRoot.attributes('data-state')).toBe('open')
    expect(collapsible.emitted('update:open')).toEqual([[true]])

    await triggerButton.trigger('click')
    await nextTick()

    expect(triggerRoot.attributes('data-state')).toBe('closed')
    expect(collapsible.emitted('update:open')).toEqual([[true], [false]])
  })

  it('syncs internal state when controlled open prop changes', async () => {
    const wrapper = mount(Collapsible, {
      props: {
        open: false,
      },
      slots: {
        default: `
          <template #default="{ open }">
            <span data-test="slot-open">{{ open }}</span>
          </template>
        `,
      },
    })

    expect(wrapper.get('[data-test="slot-open"]').text()).toBe('false')

    await wrapper.setProps({ open: true })
    await nextTick()

    expect(wrapper.get('[data-test="slot-open"]').text()).toBe('true')

    await wrapper.setProps({ open: false })
    await nextTick()

    expect(wrapper.get('[data-test="slot-open"]').text()).toBe('false')
  })

  it('still toggles its own state even when open prop is controlled', async () => {
    const Host = defineComponent({
      components: { Collapsible, CollapsibleTrigger },
      template: `
        <Collapsible :open="false">
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const triggerButton = wrapper.get('[data-test="trigger"]')
    const collapsible = wrapper.findComponent(Collapsible)
    const triggerRoot = wrapper.findComponent(CollapsibleTrigger)

    expect(triggerRoot.attributes('data-state')).toBe('closed')

    await triggerButton.trigger('click')
    await nextTick()

    expect(triggerRoot.attributes('data-state')).toBe('open')
    expect(collapsible.emitted('update:open')).toEqual([[true]])
  })

  it('trigger renders display: contents wrapper and exposes current data-state', () => {
    const Host = defineComponent({
      components: { Collapsible, CollapsibleTrigger },
      template: `
        <Collapsible :defaultOpen="true">
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const triggerRoot = wrapper.findComponent(CollapsibleTrigger)

    expect(triggerRoot.attributes('style')).toContain('display: contents')
    expect(triggerRoot.attributes('data-state')).toBe('open')
  })
})

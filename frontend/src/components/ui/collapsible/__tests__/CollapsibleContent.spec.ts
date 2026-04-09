import {describe, expect, it, vi, beforeEach, afterEach} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, nextTick} from 'vue'

import Collapsible from '../Collapsible.vue'
import CollapsibleTrigger from '../CollapsibleTrigger.vue'
import CollapsibleContent from '../CollapsibleContent.vue'

describe('CollapsibleContent', () => {
  const originalRAF = globalThis.requestAnimationFrame

  beforeEach(() => {
    vi.useFakeTimers()

    globalThis.requestAnimationFrame = ((cb: FrameRequestCallback) => {
      cb(0)
      return 1
    }) as typeof requestAnimationFrame
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
    globalThis.requestAnimationFrame = originalRAF
  })

  it('renders slot content', () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleContent},
      template: `
        <Collapsible>
          <CollapsibleContent>
            <div data-test="inner">Body</div>
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)

    expect(wrapper.find('[data-test="inner"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Body')
  })

  it('applies base class and custom class', () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleContent},
      template: `
        <Collapsible>
          <CollapsibleContent class="custom-content">
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)

    expect(content.classes()).toContain('collapsible-content')
    expect(content.classes()).toContain('custom-content')
  })

  it('starts closed by default with data-state=closed and height=0px', async () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleContent},
      template: `
        <Collapsible>
          <CollapsibleContent>
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)

    await nextTick()

    expect(content.attributes('data-state')).toBe('closed')
    expect((content.element as HTMLElement).style.height).toBe('0px')
  })

  it('starts open when defaultOpen=true and becomes auto after timeout', async () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleContent},
      template: `
        <Collapsible :defaultOpen="true">
          <CollapsibleContent>
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)
    const el = content.element as HTMLElement

    Object.defineProperty(el, 'scrollHeight', {
      configurable: true,
      value: 120,
    })

    await nextTick()
    await nextTick()

    expect(content.attributes('data-state')).toBe('open')
    expect(el.style.height === '120px' || el.style.height === 'auto').toBe(true)

    vi.advanceTimersByTime(200)
    await nextTick()

    expect(el.style.height).toBe('auto')
  })

  it('expands content to scrollHeight when opened', async () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleTrigger, CollapsibleContent},
      template: `
        <Collapsible>
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)
    const el = content.element as HTMLElement

    Object.defineProperty(el, 'scrollHeight', {
      configurable: true,
      value: 80,
    })

    expect(content.attributes('data-state')).toBe('closed')
    expect(el.style.height).toBe('0px')

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()
    await nextTick()

    expect(content.attributes('data-state')).toBe('open')
    expect(el.style.height).toBe('80px')

    vi.advanceTimersByTime(200)
    await nextTick()

    expect(el.style.height).toBe('auto')
  })

  it('collapses from explicit height to 0px when closed', async () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleTrigger, CollapsibleContent},
      template: `
        <Collapsible :defaultOpen="true">
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)
    const el = content.element as HTMLElement

    Object.defineProperty(el, 'scrollHeight', {
      configurable: true,
      value: 90,
    })

    Object.defineProperty(el, 'offsetHeight', {
      configurable: true,
      value: 90,
    })

    await nextTick()
    await nextTick()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()
    await nextTick()

    expect(content.attributes('data-state')).toBe('closed')
    expect(el.style.height).toBe('0px')
  })

  it('updates data-state along with trigger toggles', async () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleTrigger, CollapsibleContent},
      template: `
        <Collapsible>
          <CollapsibleTrigger>
            <button data-test="trigger">Toggle</button>
          </CollapsibleTrigger>
          <CollapsibleContent>
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)

    expect(content.attributes('data-state')).toBe('closed')

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(content.attributes('data-state')).toBe('open')

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(content.attributes('data-state')).toBe('closed')
  })

  it('works with controlled open prop updates', async () => {
    const Host = defineComponent({
      components: {Collapsible, CollapsibleContent},
      data() {
        return {
          open: false,
        }
      },
      template: `
        <Collapsible :open="open">
          <CollapsibleContent>
            Content
          </CollapsibleContent>
        </Collapsible>
      `,
    })

    const wrapper = mount(Host)
    const content = wrapper.findComponent(CollapsibleContent)
    const el = content.element as HTMLElement

    Object.defineProperty(el, 'scrollHeight', {
      configurable: true,
      value: 75,
    })

    expect(content.attributes('data-state')).toBe('closed')

    await wrapper.setData({open: true})
    await nextTick()
    await nextTick()

    expect(content.attributes('data-state')).toBe('open')
    expect(el.style.height).toBe('75px')

    vi.advanceTimersByTime(200)
    await nextTick()

    expect(el.style.height).toBe('auto')
  })
})

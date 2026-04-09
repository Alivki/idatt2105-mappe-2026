import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, nextTick} from 'vue'

import Tooltip from '../Tooltip.vue'
import TooltipTrigger from '../TooltipTrigger.vue'
import TooltipContent from '../TooltipContent.vue'
import TooltipProvider from '../TooltipProvider.vue'

describe('Tooltip behavior', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
    document.body.innerHTML = ''
  })

  it('renders tooltip root and slot content', () => {
    const wrapper = mount(Tooltip, {
      slots: {
        default: '<button data-test="child">Hover me</button>',
      },
    })

    expect(wrapper.find('.tooltip-root').exists()).toBe(true)
    expect(wrapper.find('[data-test="child"]').exists()).toBe(true)
  })

  it('TooltipContent is hidden by default', () => {
    const Host = defineComponent({
      components: {Tooltip, TooltipContent},
      template: `
        <Tooltip>
          <TooltipContent>
            <span data-test="content">Tooltip text</span>
          </TooltipContent>
        </Tooltip>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
    expect(wrapper.find('[role="tooltip"]').exists()).toBe(false)
  })

  it('TooltipTrigger uses display: contents wrapper', () => {
    const Host = defineComponent({
      components: {Tooltip, TooltipTrigger},
      template: `
        <Tooltip>
          <TooltipTrigger>
            <button data-test="trigger">Hover me</button>
          </TooltipTrigger>
        </Tooltip>
      `,
    })

    const wrapper = mount(Host)
    const trigger = wrapper.findComponent(TooltipTrigger)

    expect(trigger.attributes('style')).toContain('display: contents')
  })

  it('shows tooltip on mouseenter after default delay', async () => {
    const Host = defineComponent({
      components: {Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <Tooltip>
          <TooltipTrigger>
            <button data-test="trigger">Hover me</button>
          </TooltipTrigger>
          <TooltipContent>
            <span data-test="content">Tooltip text</span>
          </TooltipContent>
        </Tooltip>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    const trigger = wrapper.findComponent(TooltipTrigger)

    await trigger.trigger('mouseenter')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)

    vi.advanceTimersByTime(699)
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)

    vi.advanceTimersByTime(1)
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)
  })

  it('shows tooltip on focus after default delay', async () => {
    const Host = defineComponent({
      components: {Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <Tooltip>
          <TooltipTrigger>
            <button data-test="trigger">Focus me</button>
          </TooltipTrigger>
          <TooltipContent>
            <span data-test="content">Tooltip text</span>
          </TooltipContent>
        </Tooltip>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    const trigger = wrapper.findComponent(TooltipTrigger)

    await trigger.trigger('focus')
    vi.advanceTimersByTime(700)
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)
  })

  it('hides tooltip on mouseleave and clears pending timeout', async () => {
    const Host = defineComponent({
      components: {Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <Tooltip>
          <TooltipTrigger>
            <button data-test="trigger">Hover me</button>
          </TooltipTrigger>
          <TooltipContent>
            <span data-test="content">Tooltip text</span>
          </TooltipContent>
        </Tooltip>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    const trigger = wrapper.findComponent(TooltipTrigger)

    await trigger.trigger('mouseenter')
    await nextTick()

    await trigger.trigger('mouseleave')
    await nextTick()

    vi.advanceTimersByTime(700)
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
  })

  it('hides tooltip on blur', async () => {
    const Host = defineComponent({
      components: {Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <Tooltip>
          <TooltipTrigger>
            <button data-test="trigger">Focus me</button>
          </TooltipTrigger>
          <TooltipContent>
            <span data-test="content">Tooltip text</span>
          </TooltipContent>
        </Tooltip>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    const trigger = wrapper.findComponent(TooltipTrigger)

    await trigger.trigger('focus')
    vi.advanceTimersByTime(700)
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await trigger.trigger('blur')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
  })

  it('TooltipProvider overrides delayDuration', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="200">
          <Tooltip>
            <TooltipTrigger>
              <button data-test="trigger">Hover me</button>
            </TooltipTrigger>
            <TooltipContent>
              <span data-test="content">Tooltip text</span>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      `,
    })

    const wrapper = mount(Host, {
      global: {stubs: {transition: true}},
    })

    const trigger = wrapper.findComponent(TooltipTrigger)

    await trigger.trigger('mouseenter')
    vi.advanceTimersByTime(199)
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)

    vi.advanceTimersByTime(1)
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)
  })
})

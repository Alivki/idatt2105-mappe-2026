import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, nextTick} from 'vue'

import Tooltip from '../Tooltip.vue'
import TooltipContent from '../TooltipContent.vue'
import TooltipProvider from '../TooltipProvider.vue'
import TooltipTrigger from '../TooltipTrigger.vue'

describe('TooltipContent', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.runOnlyPendingTimers()
    vi.useRealTimers()
    document.body.innerHTML = ''
  })

  it('renders content when tooltip is open', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="0">
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
    vi.runAllTimers()
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)
    expect(wrapper.get('[role="tooltip"]').text()).toContain('Tooltip text')
  })

  it('does not render when hidden prop is true', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="0">
          <Tooltip>
            <TooltipTrigger>
              <button data-test="trigger">Hover me</button>
            </TooltipTrigger>
            <TooltipContent hidden>
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
    vi.runAllTimers()
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
    expect(wrapper.find('[role="tooltip"]').exists()).toBe(false)
  })

  it('applies default top and center classes', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="0">
          <Tooltip>
            <TooltipTrigger>
              <button data-test="trigger">Hover me</button>
            </TooltipTrigger>
            <TooltipContent class="extra-content">
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
    vi.runAllTimers()
    await nextTick()

    const content = wrapper.get('[role="tooltip"]')
    expect(content.classes()).toContain('tooltip-content')
    expect(content.classes()).toContain('tooltip-content--top')
    expect(content.classes()).toContain('tooltip-content--align-center')
    expect(content.classes()).toContain('extra-content')
  })

  it('applies bottom and start classes', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="0">
          <Tooltip>
            <TooltipTrigger>
              <button data-test="trigger">Hover me</button>
            </TooltipTrigger>
            <TooltipContent side="bottom" align="start">
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
    vi.runAllTimers()
    await nextTick()

    const content = wrapper.get('[role="tooltip"]')
    expect(content.classes()).toContain('tooltip-content--bottom')
    expect(content.classes()).toContain('tooltip-content--align-start')
  })

  it('applies left and end classes', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="0">
          <Tooltip>
            <TooltipTrigger>
              <button data-test="trigger">Hover me</button>
            </TooltipTrigger>
            <TooltipContent side="left" align="end">
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
    vi.runAllTimers()
    await nextTick()

    const content = wrapper.get('[role="tooltip"]')
    expect(content.classes()).toContain('tooltip-content--left')
    expect(content.classes()).toContain('tooltip-content--align-end')
  })

  it('applies right and center classes', async () => {
    const Host = defineComponent({
      components: {TooltipProvider, Tooltip, TooltipTrigger, TooltipContent},
      template: `
        <TooltipProvider :delayDuration="0">
          <Tooltip>
            <TooltipTrigger>
              <button data-test="trigger">Hover me</button>
            </TooltipTrigger>
            <TooltipContent side="right" align="center">
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
    vi.runAllTimers()
    await nextTick()

    const content = wrapper.get('[role="tooltip"]')
    expect(content.classes()).toContain('tooltip-content--right')
    expect(content.classes()).toContain('tooltip-content--align-center')
  })
})

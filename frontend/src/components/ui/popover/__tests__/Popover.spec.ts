import {afterEach, describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, nextTick, ref} from 'vue'

import Popover from '../Popover.vue'
import PopoverTrigger from '../PopoverTrigger.vue'
import PopoverContent from '../PopoverContent.vue'

afterEach(() => {
  document.body.innerHTML = ''
})

describe('Popover core behavior', () => {
  it('renders slot content', () => {
    const wrapper = mount(Popover, {
      slots: {
        default: '<div data-test="inside">Hei</div>',
      },
    })

    expect(wrapper.find('[data-test="inside"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hei')
  })

  it('renders root with base class', () => {
    const wrapper = mount(Popover)

    expect(wrapper.classes()).toContain('popover-root')
  })

  it('starts closed by default', async () => {
    const wrapper = mount(Popover, {
      slots: {
        default: `
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        `,
      },
      global: {
        components: {PopoverContent},
        stubs: {transition: true},
      },
    })

    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
    expect(wrapper.find('.popover-content').exists()).toBe(false)

    wrapper.unmount()
  })

  it('respects defaultOpen=true', async () => {
    const wrapper = mount(Popover, {
      props: {defaultOpen: true},
      slots: {
        default: `
          <PopoverContent class="extra-content">
            <div data-test="content">Popover</div>
          </PopoverContent>
        `,
      },
      global: {
        components: {PopoverContent},
        stubs: {transition: true},
      },
    })

    await nextTick()

    const content = wrapper.find('.popover-content')
    expect(content.exists()).toBe(true)
    expect(content.text()).toContain('Popover')
    expect(content.classes()).toContain('extra-content')

    wrapper.unmount()
  })

  it('syncs internal state when controlled open prop changes', async () => {
    const wrapper = mount(Popover, {
      props: {open: false},
      slots: {
        default: `
          <PopoverContent>
            <div data-test="content">Controlled</div>
          </PopoverContent>
        `,
      },
      global: {
        components: {PopoverContent},
        stubs: {transition: true},
      },
    })

    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)

    await wrapper.setProps({open: true})
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await wrapper.setProps({open: false})
    await nextTick()
    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)

    wrapper.unmount()
  })

  it('trigger toggles popover and emits update:open', async () => {
    const wrapper = mount(Popover, {
      slots: {
        default: `
          <PopoverTrigger>
            <button data-test="trigger">Toggle</button>
          </PopoverTrigger>
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        `,
      },
      global: {
        components: {PopoverTrigger, PopoverContent},
        stubs: {transition: true},
      },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)
    expect(wrapper.emitted('update:open')).toEqual([[true]])

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
    expect(wrapper.emitted('update:open')).toEqual([[true], [false]])

    wrapper.unmount()
  })

  it('can be used as a controlled component from the parent', async () => {
    const ControlledHost = defineComponent({
      components: {
        Popover,
        PopoverTrigger,
        PopoverContent,
      },
      setup() {
        const open = ref(false)
        return {open}
      },
      template: `
        <Popover v-model:open="open">
          <PopoverTrigger>
            <button data-test="trigger">Toggle</button>
          </PopoverTrigger>
          <PopoverContent>
            Controlled by parent
          </PopoverContent>
        </Popover>
      `,
    })

    const wrapper = mount(ControlledHost, {
      global: {
        stubs: {transition: true},
      },
    })

    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(false)
    expect(wrapper.find('.popover-content').exists()).toBe(false)

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(true)
    expect(wrapper.find('.popover-content').exists()).toBe(true)

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(false)
    expect(wrapper.find('.popover-content').exists()).toBe(false)

    wrapper.unmount()
  })

  it('trigger registers its root element as the trigger ref by rendering clickable content', async () => {
    const wrapper = mount(Popover, {
      slots: {
        default: `
          <PopoverTrigger>
            <button data-test="trigger">Trigger</button>
          </PopoverTrigger>
          <PopoverContent>
            <div data-test="content">Popover</div>
          </PopoverContent>
        `,
      },
      global: {
        components: {PopoverTrigger, PopoverContent},
        stubs: {transition: true},
      },
    })

    const triggerWrapper = wrapper.findComponent(PopoverTrigger)
    expect(triggerWrapper.attributes('style')).toContain('display: contents')

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })
})

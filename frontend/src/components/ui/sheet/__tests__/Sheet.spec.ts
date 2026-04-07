import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h, nextTick, ref } from 'vue'

import Sheet from '../Sheet.vue'
import SheetTrigger from '../SheetTrigger.vue'
import SheetClose from '../SheetClose.vue'
import SheetContent from '../SheetContent.vue'

vi.mock('lucide-vue-next', () => ({
  X: defineComponent({
    name: 'XIconStub',
    props: {
      class: { type: String, default: '' },
    },
    setup(props) {
      return () => h('svg', { class: props.class, 'data-test': 'x-icon' })
    },
  }),
}))

afterEach(() => {
  document.body.innerHTML = ''
})

describe('Sheet behavior', () => {
  it('renders slot content', () => {
    const wrapper = mount(Sheet, {
      slots: {
        default: '<div data-test="inside">Hei</div>',
      },
    })

    expect(wrapper.find('[data-test="inside"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hei')
  })

  it('starts closed by default', async () => {
    const wrapper = mount(Sheet, {
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            <div data-test="content">Sheet</div>
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(document.body.querySelector('.sheet-overlay')).toBeNull()

    wrapper.unmount()
  })

  it('respects defaultOpen', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent class="extra-content">
            <div data-test="content">Sheet</div>
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="dialog"]') as HTMLDivElement | null
    const overlay = document.body.querySelector('.sheet-overlay')

    expect(dialog).not.toBeNull()
    expect(dialog?.textContent).toContain('Sheet')
    expect(dialog?.className).toContain('sheet-content')
    expect(dialog?.className).toContain('extra-content')
    expect(overlay).not.toBeNull()

    wrapper.unmount()
  })

  it('syncs internal state when controlled open prop changes', async () => {
    const wrapper = mount(Sheet, {
      props: { open: false },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            Controlled
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()

    await wrapper.setProps({ open: true })
    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()

    await wrapper.setProps({ open: false })
    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()

    wrapper.unmount()
  })

  it('trigger toggles sheet open and closed and emits update:open', async () => {
    const wrapper = mount(Sheet, {
      attachTo: document.body,
      slots: {
        default: `
          <SheetTrigger>
            <button data-test="trigger">Åpne</button>
          </SheetTrigger>
          <SheetContent>
            <div data-test="content">Sheet</div>
          </SheetContent>
        `,
      },
      global: {
        components: { SheetTrigger, SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[true]])

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[true], [false]])

    wrapper.unmount()
  })

  it('SheetClose closes the sheet and emits update:open=false', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            <SheetClose>
              <span data-test="close-slot">Lukk</span>
            </SheetClose>
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent, SheetClose },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const closeButton = Array.from(document.body.querySelectorAll('button')).find(
      (el) => el.textContent?.includes('Lukk'),
    ) as HTMLButtonElement | undefined

    expect(closeButton).toBeTruthy()

    closeButton?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('overlay click closes the sheet', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            Body
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const overlay = document.body.querySelector('.sheet-overlay') as HTMLDivElement | null
    expect(overlay).not.toBeNull()

    overlay?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('content close button closes sheet and renders icon', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
        <SheetContent>
          Body
        </SheetContent>
      `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const closeButton = document.body.querySelector('.sheet-close') as HTMLButtonElement | null
    expect(closeButton).not.toBeNull()
    expect(document.body.querySelector('.sheet-close__icon')).not.toBeNull()

    closeButton?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('pressing Escape closes the sheet', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            Body
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('non-Escape key does not close the sheet', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            Body
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }))
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()

    wrapper.unmount()
  })

  it('supports side="right" by default', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent>
            Right side
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="dialog"]') as HTMLDivElement | null
    expect(dialog).not.toBeNull()
    expect(dialog?.className).toContain('sheet-content--right')

    wrapper.unmount()
  })

  it('supports side="left"', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent side="left">
            Left side
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="dialog"]') as HTMLDivElement | null
    expect(dialog?.className).toContain('sheet-content--left')

    wrapper.unmount()
  })

  it('supports side="top"', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent side="top">
            Top side
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="dialog"]') as HTMLDivElement | null
    expect(dialog?.className).toContain('sheet-content--top')

    wrapper.unmount()
  })

  it('supports side="bottom"', async () => {
    const wrapper = mount(Sheet, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <SheetContent side="bottom">
            Bottom side
          </SheetContent>
        `,
      },
      global: {
        components: { SheetContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="dialog"]') as HTMLDivElement | null
    expect(dialog?.className).toContain('sheet-content--bottom')

    wrapper.unmount()
  })

  it('can be used as a fully controlled component from the parent', async () => {
    const ControlledHost = defineComponent({
      components: {
        Sheet,
        SheetTrigger,
        SheetContent,
      },
      setup() {
        const open = ref(false)
        return { open }
      },
      template: `
        <Sheet v-model:open="open">
          <SheetTrigger>
            <button data-test="trigger">Toggle</button>
          </SheetTrigger>
          <SheetContent>
            Controlled by parent
          </SheetContent>
        </Sheet>
      `,
    })

    const wrapper = mount(ControlledHost, {
      attachTo: document.body,
      global: {
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(false)
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(true)
    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(false)
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()

    wrapper.unmount()
  })
})

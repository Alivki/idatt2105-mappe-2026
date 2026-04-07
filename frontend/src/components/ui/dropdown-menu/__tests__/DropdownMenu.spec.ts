import { afterEach, describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref } from 'vue'

import DropdownMenu from '../DropdownMenu.vue'
import DropdownMenuTrigger from '../DropdownMenuTrigger.vue'
import DropdownMenuContent from '../DropdownMenuContent.vue'
import DropdownMenuItem from '../DropdownMenuItem.vue'

afterEach(() => {
  document.body.innerHTML = ''
})

describe('DropdownMenu core behavior', () => {
  it('renders slot content', () => {
    const wrapper = mount(DropdownMenu, {
      slots: {
        default: '<div data-test="inside">Hei</div>',
      },
    })

    expect(wrapper.find('[data-test="inside"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hei')
  })

  it('starts closed by default', async () => {
    const wrapper = mount(DropdownMenu, {
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuContent>
            <div data-test="content">Menu</div>
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    wrapper.unmount()
  })

  it('respects defaultOpen', async () => {
    const wrapper = mount(DropdownMenu, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuContent class="extra-content">
            <div data-test="content">Menu</div>
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    await nextTick()

    const content = document.body.querySelector('[role="menu"]') as HTMLDivElement | null

    expect(content).not.toBeNull()
    expect(content?.textContent).toContain('Menu')
    expect(content?.className).toContain('dropdown-content')
    expect(content?.className).toContain('extra-content')

    wrapper.unmount()
  })

  it('syncs internal state when controlled open prop changes', async () => {
    const wrapper = mount(DropdownMenu, {
      props: { open: false },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger><button data-test="trigger">Open</button></DropdownMenuTrigger>
          <DropdownMenuContent>Controlled</DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 40,
        width: 100,
        height: 30,
      }),
    })

    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    await wrapper.setProps({ open: true })
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    await wrapper.setProps({ open: false })
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    wrapper.unmount()
  })

  it('trigger toggles menu open and closed and emits update:open', async () => {
    const wrapper = mount(DropdownMenu, {
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <div data-test="content">Menu</div>
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 40,
        width: 100,
        height: 30,
      }),
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[true]])

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(document.body.querySelector('[role="menu"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[true], [false]])

    wrapper.unmount()
  })

  it('trigger stores first child element as positioning ref', async () => {
    const wrapper = mount(DropdownMenu, {
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <div>Menu</div>
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 15,
        left: 25,
        right: 125,
        bottom: 55,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    const content = document.body.querySelector('[role="menu"]') as HTMLDivElement | null
    expect(content).not.toBeNull()

    wrapper.unmount()
  })

  it('renders content for bottom/center alignment', async () => {
    const wrapper = mount(DropdownMenu, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="center" side="bottom" :sideOffset="8" class="bottom-center">
            Menu
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 50,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.setProps({ open: true })
    await nextTick()

    const content = document.body.querySelector('[role="menu"]') as HTMLDivElement | null
    expect(content).not.toBeNull()
    expect(content?.className).toContain('dropdown-content')
    expect(content?.className).toContain('bottom-center')
    expect(content?.textContent).toContain('Menu')

    wrapper.unmount()
  })

  it('renders content for top/end alignment', async () => {
    Object.defineProperty(window, 'innerHeight', { value: 900, configurable: true })
    Object.defineProperty(window, 'innerWidth', { value: 1200, configurable: true })

    const wrapper = mount(DropdownMenu, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" side="top" :sideOffset="6" class="top-end">
            Menu
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 100,
        left: 50,
        right: 170,
        bottom: 150,
        width: 120,
        height: 50,
      }),
    })

    await wrapper.setProps({ open: true })
    await nextTick()

    const content = document.body.querySelector('[role="menu"]') as HTMLDivElement | null
    expect(content).not.toBeNull()
    expect(content?.className).toContain('dropdown-content')
    expect(content?.className).toContain('top-end')
    expect(content?.textContent).toContain('Menu')

    wrapper.unmount()
  })

  it('renders content for right/center alignment', async () => {
    const wrapper = mount(DropdownMenu, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="center" side="right" :sideOffset="5" class="right-center">
            Menu
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 40,
        left: 30,
        right: 130,
        bottom: 80,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.setProps({ open: true })
    await nextTick()

    const content = document.body.querySelector('[role="menu"]') as HTMLDivElement | null
    expect(content).not.toBeNull()
    expect(content?.className).toContain('dropdown-content')
    expect(content?.className).toContain('right-center')
    expect(content?.textContent).toContain('Menu')

    wrapper.unmount()
  })

  it('renders content for left/start alignment', async () => {
    Object.defineProperty(window, 'innerWidth', { value: 1000, configurable: true })

    const wrapper = mount(DropdownMenu, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" side="left" :sideOffset="7" class="left-start">
            Menu
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 60,
        left: 100,
        right: 220,
        bottom: 110,
        width: 120,
        height: 50,
      }),
    })

    await wrapper.setProps({ open: true })
    await nextTick()

    const content = document.body.querySelector('[role="menu"]') as HTMLDivElement | null
    expect(content).not.toBeNull()
    expect(content?.className).toContain('dropdown-content')
    expect(content?.className).toContain('left-start')
    expect(content?.textContent).toContain('Menu')

    wrapper.unmount()
  })

  it('clicking outside closes the menu, but clicking trigger does not', async () => {
    const wrapper = mount(DropdownMenu, {
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            Menu
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 50,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    btn.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    wrapper.unmount()
  })

  it('Escape closes the menu, Enter does not', async () => {
    const wrapper = mount(DropdownMenu, {
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            Menu
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 50,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }))
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
    await nextTick()
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    wrapper.unmount()
  })

  it('menu item closes menu when clicked', async () => {
    const wrapper = mount(DropdownMenu, {
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem data-test="item">Item</DropdownMenuItem>
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 50,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    const item = document.body.querySelector('[data-test="item"]') as HTMLDivElement | null
    expect(item?.getAttribute('role')).toBe('menuitem')

    item?.click()
    await nextTick()

    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    wrapper.unmount()
  })

  it('disabled menu item prevents default and does not close', async () => {
    const wrapper = mount(DropdownMenu, {
      props: { defaultOpen: true },
      attachTo: document.body,
      slots: {
        default: `
          <DropdownMenuTrigger>
            <button data-test="trigger">Åpne</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem disabled inset class="extra-item" data-test="item">Item</DropdownMenuItem>
          </DropdownMenuContent>
        `,
      },
      global: {
        components: { DropdownMenuTrigger, DropdownMenuContent, DropdownMenuItem },
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 50,
        width: 100,
        height: 40,
      }),
    })

    await wrapper.setProps({ open: true })
    await nextTick()

    const item = document.body.querySelector('[data-test="item"]') as HTMLDivElement | null
    expect(item?.className).toContain('dropdown-item--disabled')
    expect(item?.className).toContain('dropdown-item--inset')
    expect(item?.className).toContain('extra-item')
    expect(item?.getAttribute('tabindex')).toBe('-1')

    const event = new MouseEvent('click', { bubbles: true, cancelable: true })
    item?.dispatchEvent(event)
    await nextTick()

    expect(event.defaultPrevented).toBe(true)
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    wrapper.unmount()
  })

  it('can be used as a controlled component from the parent', async () => {
    const ControlledHost = defineComponent({
      components: { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent },
      setup() {
        const open = ref(false)
        return { open }
      },
      template: `
        <DropdownMenu v-model:open="open">
          <DropdownMenuTrigger>
            <button data-test="trigger">Toggle</button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            Controlled by parent
          </DropdownMenuContent>
        </DropdownMenu>
      `,
    })

    const wrapper = mount(ControlledHost, {
      attachTo: document.body,
      global: {
        stubs: { teleport: false, transition: true },
      },
    })

    const btn = wrapper.get('[data-test="trigger"]').element as HTMLButtonElement
    Object.defineProperty(btn, 'getBoundingClientRect', {
      value: () => ({
        top: 10,
        left: 20,
        right: 120,
        bottom: 50,
        width: 100,
        height: 40,
      }),
    })

    await nextTick()
    expect((wrapper.vm as { open: boolean }).open).toBe(false)
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(true)
    expect(document.body.querySelector('[role="menu"]')).not.toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { open: boolean }).open).toBe(false)
    expect(document.body.querySelector('[role="menu"]')).toBeNull()

    wrapper.unmount()
  })
})

import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, h, nextTick, ref} from 'vue'

import SidebarProvider from '../SidebarProvider.vue'
import {SIDEBAR_COOKIE_NAME, useSidebar} from '../utils'

const isMobileRef = ref(false)
let registeredKeydownHandler: ((event: KeyboardEvent) => void) | undefined

vi.mock('@vueuse/core', () => ({
  useMediaQuery: vi.fn(() => isMobileRef),
  useEventListener: vi.fn((event: string, handler: (event: KeyboardEvent) => void) => {
    if (event === 'keydown') registeredKeydownHandler = handler
  }),
}))

const SidebarConsumer = defineComponent({
  name: 'SidebarConsumer',
  setup() {
    const sidebar = useSidebar()
    return () =>
      h('div', {'data-test': 'consumer'}, [
        h('span', {'data-test': 'state'}, sidebar.state.value),
        h('span', {'data-test': 'open'}, String(sidebar.open.value)),
        h('span', {'data-test': 'mobile'}, String(sidebar.isMobile.value)),
        h('span', {'data-test': 'open-mobile'}, String(sidebar.openMobile.value)),
        h(
          'button',
          {
            'data-test': 'toggle',
            onClick: () => sidebar.toggleSidebar(),
          },
          'toggle',
        ),
        h(
          'button',
          {
            'data-test': 'set-open',
            onClick: () => sidebar.setOpen(false),
          },
          'set-open',
        ),
        h(
          'button',
          {
            'data-test': 'set-open-mobile',
            onClick: () => sidebar.setOpenMobile(true),
          },
          'set-open-mobile',
        ),
        h('span', {'data-test': 'variant'}, sidebar.variant),
        h('span', {'data-test': 'collapsible'}, sidebar.collapsible),
        h('span', {'data-test': 'side'}, sidebar.side),
      ])
  },
})

describe('SidebarProvider', () => {
  beforeEach(() => {
    isMobileRef.value = false
    registeredKeydownHandler = undefined
    document.cookie = `${SIDEBAR_COOKIE_NAME}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
  })

  afterEach(() => {
    document.body.innerHTML = ''
    document.cookie = `${SIDEBAR_COOKIE_NAME}=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/`
  })

  it('provides sidebar context values', () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    expect(wrapper.get('[data-test="variant"]').text()).toBe('sidebar')
    expect(wrapper.get('[data-test="collapsible"]').text()).toBe('offcanvas')
    expect(wrapper.get('[data-test="side"]').text()).toBe('left')
    expect(wrapper.get('[data-test="state"]').text()).toBe('expanded')
    expect(wrapper.get('[data-test="open"]').text()).toBe('true')
  })

  it('renders wrapper with css variables and custom class', () => {
    const wrapper = mount(SidebarProvider, {
      props: {
        class: 'provider-extra',
      },
    })

    expect(wrapper.classes()).toContain('sidebar-wrapper')
    expect(wrapper.classes()).toContain('provider-extra')
    expect(wrapper.attributes('style')).toContain('--sidebar-width')
    expect(wrapper.attributes('style')).toContain('--sidebar-width-icon')
  })

  it('reads open state from cookie by default', () => {
    document.cookie = `${SIDEBAR_COOKIE_NAME}=false; path=/`

    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    expect(wrapper.get('[data-test="state"]').text()).toBe('collapsed')
    expect(wrapper.get('[data-test="open"]').text()).toBe('false')
  })

  it('uses controlled open prop when provided', async () => {
    const wrapper = mount(SidebarProvider, {
      props: {
        open: false,
      },
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    expect(wrapper.get('[data-test="state"]').text()).toBe('collapsed')
    expect(wrapper.get('[data-test="open"]').text()).toBe('false')

    await wrapper.setProps({open: true})
    await nextTick()

    expect(wrapper.get('[data-test="state"]').text()).toBe('expanded')
    expect(wrapper.get('[data-test="open"]').text()).toBe('true')
  })

  it('toggleSidebar updates desktop open state and emits update:open', async () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    await wrapper.get('[data-test="toggle"]').trigger('click')
    await nextTick()

    expect(wrapper.get('[data-test="state"]').text()).toBe('collapsed')
    expect(wrapper.get('[data-test="open"]').text()).toBe('false')
    expect(wrapper.emitted('update:open')).toEqual([[false]])
    expect(document.cookie).toContain(`${SIDEBAR_COOKIE_NAME}=false`)
  })

  it('toggleSidebar updates mobile openMobile instead of desktop open', async () => {
    isMobileRef.value = true

    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    expect(wrapper.get('[data-test="open-mobile"]').text()).toBe('false')
    expect(wrapper.get('[data-test="state"]').text()).toBe('expanded')

    await wrapper.get('[data-test="toggle"]').trigger('click')
    await nextTick()

    expect(wrapper.get('[data-test="open-mobile"]').text()).toBe('true')
    expect(wrapper.get('[data-test="state"]').text()).toBe('expanded')
    expect(wrapper.get('[data-test="open"]').text()).toBe('true')
    expect(wrapper.emitted('update:open')).toBeUndefined()
  })

  it('setOpenMobile updates only mobile state', async () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    await wrapper.get('[data-test="set-open-mobile"]').trigger('click')
    await nextTick()

    expect(wrapper.get('[data-test="open-mobile"]').text()).toBe('true')
    expect(wrapper.get('[data-test="state"]').text()).toBe('expanded')
    expect(wrapper.get('[data-test="open"]').text()).toBe('true')
  })

  it('setOpen updates state, emits and writes cookie', async () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    await wrapper.get('[data-test="set-open"]').trigger('click')
    await nextTick()

    expect(wrapper.get('[data-test="state"]').text()).toBe('collapsed')
    expect(wrapper.get('[data-test="open"]').text()).toBe('false')
    expect(wrapper.emitted('update:open')).toEqual([[false]])
    expect(document.cookie).toContain(`${SIDEBAR_COOKIE_NAME}=false`)
  })

  it('keyboard shortcut toggles sidebar with ctrl+b', async () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    expect(registeredKeydownHandler).toBeTruthy()

    const event = new KeyboardEvent('keydown', {key: 'b', ctrlKey: true})
    const preventDefault = vi.spyOn(event, 'preventDefault')

    registeredKeydownHandler?.(event)
    await nextTick()

    expect(preventDefault).toHaveBeenCalled()
    expect(wrapper.get('[data-test="state"]').text()).toBe('collapsed')
  })

  it('keyboard shortcut toggles sidebar with meta+b', async () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    const event = new KeyboardEvent('keydown', {key: 'b', metaKey: true})
    registeredKeydownHandler?.(event)
    await nextTick()

    expect(wrapper.get('[data-test="state"]').text()).toBe('collapsed')
  })

  it('ignores unrelated keydown events', async () => {
    const wrapper = mount(SidebarProvider, {
      slots: {
        default: () => h(SidebarConsumer),
      },
    })

    const event = new KeyboardEvent('keydown', {key: 'x', ctrlKey: true})
    registeredKeydownHandler?.(event)
    await nextTick()

    expect(wrapper.get('[data-test="state"]').text()).toBe('expanded')
  })
})

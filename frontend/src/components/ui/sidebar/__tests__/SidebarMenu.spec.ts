import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h, ref } from 'vue'

import SidebarMenu from '../SidebarMenu.vue'
import SidebarMenuAction from '../SidebarMenuAction.vue'
import SidebarMenuBadge from '../SidebarMenuBadge.vue'
import SidebarMenuButton from '../SidebarMenuButton.vue'
import SidebarMenuButtonChild from '../SidebarMenuButtonChild.vue'
import SidebarMenuItem from '../SidebarMenuItem.vue'
import SidebarMenuSkeleton from '../SidebarMenuSkeleton.vue'
import { provideSidebarContext } from '../utils'

const sidebarState = ref<'expanded' | 'collapsed'>('expanded')
const sidebarOpen = ref(true)
const sidebarMobile = ref(false)
const sidebarOpenMobile = ref(false)

function mountWithSidebar(component: unknown, options: Record<string, unknown> = {}) {
  const Provider = defineComponent({
    setup() {
      provideSidebarContext({
        state: sidebarState,
        open: sidebarOpen,
        setOpen: (value: boolean) => {
          sidebarOpen.value = value
          sidebarState.value = value ? 'expanded' : 'collapsed'
        },
        isMobile: sidebarMobile,
        openMobile: sidebarOpenMobile,
        setOpenMobile: (value: boolean) => {
          sidebarOpenMobile.value = value
        },
        toggleSidebar: vi.fn(),
        variant: 'sidebar',
        collapsible: 'offcanvas',
        side: 'left',
      })

      return () =>
        h(
          'div',
          {},
          [
            h(
              component as never,
              {
                ...options.props,
                ...options.attrs,
              } as never,
              options.slots ?? {},
            ),
          ],
        )
    },
  })

  return mount(Provider, {
    global: options.global,
  })
}

vi.mock('@vueuse/core', () => ({
  reactiveOmit: (obj: Record<string, unknown>, ...keys: string[]) => {
    const clone = { ...obj }
    for (const key of keys) delete clone[key]
    return clone
  },
}))

vi.mock('@/components/ui/tooltip', () => ({
  Tooltip: defineComponent({
    name: 'TooltipStub',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'tooltip-root' }, slots.default?.())
    },
  }),
  TooltipTrigger: defineComponent({
    name: 'TooltipTriggerStub',
    props: {
      asChild: { type: Boolean, default: false },
    },
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'tooltip-trigger' }, slots.default?.())
    },
  }),
  TooltipContent: defineComponent({
    name: 'TooltipContentStub',
    props: {
      side: { type: String, default: '' },
      align: { type: String, default: '' },
      hidden: { type: Boolean, default: false },
    },
    setup(props, { slots }) {
      return () =>
        h(
          'div',
          {
            'data-test': 'tooltip-content',
            'data-side': props.side,
            'data-align': props.align,
            'data-hidden': String(props.hidden),
          },
          slots.default?.(),
        )
    },
  }),
}))

vi.mock('@/components/ui/skeleton', () => ({
  Skeleton: defineComponent({
    name: 'SkeletonStub',
    inheritAttrs: false,
    setup(_, { attrs }) {
      return () => h('div', { ...attrs, class: 'skeleton-stub' })
    },
  }),
}))

describe('SidebarMenu and related components', () => {
  it('SidebarMenu renders as ul with slot and class', () => {
    const wrapper = mount(SidebarMenu, {
      props: {
        class: 'menu-extra',
      },
      slots: {
        default: '<li data-test="child">Item</li>',
      },
    })

    expect(wrapper.element.tagName).toBe('UL')
    expect(wrapper.attributes('data-sidebar')).toBe('menu')
    expect(wrapper.classes()).toContain('sidebar-menu')
    expect(wrapper.classes()).toContain('menu-extra')
    expect(wrapper.find('[data-test="child"]').exists()).toBe(true)
  })

  it('SidebarMenuItem renders as li with slot and class', () => {
    const wrapper = mount(SidebarMenuItem, {
      props: {
        class: 'item-extra',
      },
      slots: {
        default: 'Entry',
      },
    })

    expect(wrapper.element.tagName).toBe('LI')
    expect(wrapper.attributes('data-sidebar')).toBe('menu-item')
    expect(wrapper.classes()).toContain('sidebar-menu-item')
    expect(wrapper.classes()).toContain('item-extra')
    expect(wrapper.text()).toContain('Entry')
  })

  it('SidebarMenuAction renders as button by default', () => {
    sidebarState.value = 'expanded'

    const wrapper = mountWithSidebar(SidebarMenuAction, {
      props: { class: 'action-extra' },
      slots: { default: () => 'Act' },
    })

    const action = wrapper.get('[data-sidebar="menu-action"]')
    expect(action.element.tagName).toBe('BUTTON')
    expect(action.classes()).toContain('sidebar-menu-action')
    expect(action.classes()).toContain('action-extra')
    expect(action.text()).toContain('Act')
    expect(action.attributes('style') ?? '').not.toContain('display: none')
  })

  it('SidebarMenuAction supports showOnHover class', () => {
    const wrapper = mountWithSidebar(SidebarMenuAction, {
      props: { showOnHover: true },
    })

    expect(wrapper.get('[data-sidebar="menu-action"]').classes()).toContain('sidebar-menu-action--hover-only')
  })

  it('SidebarMenuAction respects custom "as" prop', () => {
    const wrapper = mountWithSidebar(SidebarMenuAction, {
      props: { as: 'a' },
    })

    expect(wrapper.get('[data-sidebar="menu-action"]').element.tagName).toBe('A')
  })

  it('SidebarMenuAction hides in collapsed icon mode', () => {
    const Provider = defineComponent({
      setup() {
        provideSidebarContext({
          state: ref<'expanded' | 'collapsed'>('collapsed'),
          open: ref(false),
          setOpen: vi.fn(),
          isMobile: ref(false),
          openMobile: ref(false),
          setOpenMobile: vi.fn(),
          toggleSidebar: vi.fn(),
          variant: 'sidebar',
          collapsible: 'icon',
          side: 'left',
        })
        return () => h(SidebarMenuAction)
      },
    })

    const wrapper = mount(Provider)
    expect(wrapper.get('[data-sidebar="menu-action"]').attributes('style')).toContain('display: none')
  })

  it('SidebarMenuBadge renders and hides in collapsed icon mode', () => {
    const Provider = defineComponent({
      setup() {
        provideSidebarContext({
          state: ref<'expanded' | 'collapsed'>('collapsed'),
          open: ref(false),
          setOpen: vi.fn(),
          isMobile: ref(false),
          openMobile: ref(false),
          setOpenMobile: vi.fn(),
          toggleSidebar: vi.fn(),
          variant: 'sidebar',
          collapsible: 'icon',
          side: 'left',
        })
        return () => h(SidebarMenuBadge, { class: 'badge-extra' }, () => '9')
      },
    })

    const wrapper = mount(Provider)
    const badge = wrapper.get('[data-sidebar="menu-badge"]')

    expect(badge.classes()).toContain('sidebar-menu-badge')
    expect(badge.classes()).toContain('badge-extra')
    expect(badge.text()).toContain('9')
    expect(badge.attributes('style')).toContain('display: none')
  })

  it('SidebarMenuButtonChild renders default button variant/size', () => {
    const wrapper = mountWithSidebar(SidebarMenuButtonChild, {
      slots: {
        default: () => h('span', 'Inbox'),
      },
    })

    const button = wrapper.get('[data-sidebar="menu-button"]')
    expect(button.element.tagName).toBe('BUTTON')
    expect(button.attributes('data-size')).toBe('default')
    expect(button.attributes('data-active')).toBeUndefined()
    expect(button.classes()).toContain('sidebar-menu-btn')
    expect(button.classes()).toContain('sidebar-menu-btn--default')
  })

  it('SidebarMenuButtonChild supports outline, lg and active state', () => {
    const wrapper = mountWithSidebar(SidebarMenuButtonChild, {
      props: {
        as: 'a',
        variant: 'outline',
        size: 'lg',
        isActive: true,
        class: 'btn-extra',
      },
      attrs: {
        href: '#',
      },
      slots: {
        default: () => h('span', 'Settings'),
      },
    })

    const button = wrapper.get('[data-sidebar="menu-button"]')
    expect(button.element.tagName).toBe('A')
    expect(button.attributes('data-size')).toBe('lg')
    expect(button.attributes('data-active')).toBe('true')
    expect(button.classes()).toContain('sidebar-menu-btn--outline')
    expect(button.classes()).toContain('sidebar-menu-btn--lg')
    expect(button.classes()).toContain('sidebar-menu-btn--active')
    expect(button.classes()).toContain('btn-extra')
    expect(button.attributes('href')).toBe('#')
  })

  it('SidebarMenuButtonChild adds icon-only class in collapsed icon mode', () => {
    const Provider = defineComponent({
      setup() {
        provideSidebarContext({
          state: ref<'expanded' | 'collapsed'>('collapsed'),
          open: ref(false),
          setOpen: vi.fn(),
          isMobile: ref(false),
          openMobile: ref(false),
          setOpenMobile: vi.fn(),
          toggleSidebar: vi.fn(),
          variant: 'sidebar',
          collapsible: 'icon',
          side: 'left',
        })
        return () => h(SidebarMenuButtonChild)
      },
    })

    const wrapper = mount(Provider)
    expect(wrapper.get('[data-sidebar="menu-button"]').classes()).toContain('sidebar-menu-btn--icon-only')
  })

  it('SidebarMenuButton renders child directly when tooltip is not provided', () => {
    const wrapper = mountWithSidebar(SidebarMenuButton, {
      props: {
        variant: 'outline',
        size: 'sm',
      },
      slots: {
        default: () => h('span', 'Projects'),
      },
    })

    expect(wrapper.find('[data-test="tooltip-root"]').exists()).toBe(false)
    expect(wrapper.get('[data-sidebar="menu-button"]').classes()).toContain('sidebar-menu-btn--outline')
    expect(wrapper.get('[data-sidebar="menu-button"]').classes()).toContain('sidebar-menu-btn--sm')
  })

  it('SidebarMenuButton renders tooltip wrapper when tooltip is string', () => {
    sidebarState.value = 'collapsed'
    sidebarMobile.value = false

    const wrapper = mountWithSidebar(SidebarMenuButton, {
      props: {
        tooltip: 'Projects',
      },
      slots: {
        default: () => h('span', 'Projects'),
      },
    })

    expect(wrapper.find('[data-test="tooltip-root"]').exists()).toBe(true)
    expect(wrapper.get('[data-test="tooltip-content"]').attributes('data-side')).toBe('right')
    expect(wrapper.get('[data-test="tooltip-content"]').attributes('data-align')).toBe('center')
    expect(wrapper.get('[data-test="tooltip-content"]').attributes('data-hidden')).toBe('false')
    expect(wrapper.get('[data-test="tooltip-content"]').text()).toContain('Projects')
  })

  it('SidebarMenuButton hides tooltip content when not collapsed or on mobile', () => {
    sidebarState.value = 'expanded'
    sidebarMobile.value = true

    const wrapper = mountWithSidebar(SidebarMenuButton, {
      props: {
        tooltip: 'Projects',
      },
      slots: {
        default: () => h('span', 'Projects'),
      },
    })

    expect(wrapper.get('[data-test="tooltip-content"]').attributes('data-hidden')).toBe('true')
  })

  it('SidebarMenuButton supports component tooltip', () => {
    const TooltipComponent = defineComponent({
      template: '<span data-test="tooltip-component">Component tooltip</span>',
    })

    sidebarState.value = 'collapsed'
    sidebarMobile.value = false

    const wrapper = mountWithSidebar(SidebarMenuButton, {
      props: {
        tooltip: TooltipComponent,
      },
      slots: {
        default: () => h('span', 'Projects'),
      },
    })

    expect(wrapper.find('[data-test="tooltip-component"]').exists()).toBe(true)
  })

  it('SidebarMenuSkeleton renders text skeleton and optional icon skeleton', () => {
    vi.spyOn(Math, 'random').mockReturnValue(0.5)

    const wrapper = mount(SidebarMenuSkeleton, {
      props: {
        showIcon: true,
        class: 'skeleton-extra',
      },
    })

    expect(wrapper.get('[data-sidebar="menu-skeleton"]').classes()).toContain('sidebar-menu-skeleton')
    expect(wrapper.get('[data-sidebar="menu-skeleton"]').classes()).toContain('skeleton-extra')
    expect(wrapper.find('[data-sidebar="menu-skeleton-icon"]').exists()).toBe(true)
    expect(wrapper.find('[data-sidebar="menu-skeleton-text"]').exists()).toBe(true)
    expect(wrapper.get('[data-sidebar="menu-skeleton-text"]').attributes('style')).toContain('max-width: 70%')

    vi.restoreAllMocks()
  })

  it('SidebarMenuSkeleton renders without icon when showIcon is false', () => {
    const wrapper = mount(SidebarMenuSkeleton)

    expect(wrapper.find('[data-sidebar="menu-skeleton-icon"]').exists()).toBe(false)
    expect(wrapper.find('[data-sidebar="menu-skeleton-text"]').exists()).toBe(true)
  })
})

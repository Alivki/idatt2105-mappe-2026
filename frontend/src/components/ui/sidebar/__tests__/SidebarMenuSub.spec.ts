import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h, ref } from 'vue'

import SidebarMenuSub from '../SidebarMenuSub.vue'
import SidebarMenuSubButton from '../SidebarMenuSubButton.vue'
import SidebarMenuSubItem from '../SidebarMenuSubItem.vue'
import { provideSidebarContext } from '../utils'

function mountWithSidebar(component: unknown, options: Record<string, unknown> = {}) {
  const Provider = defineComponent({
    setup() {
      provideSidebarContext({
        state: ref<'expanded' | 'collapsed'>('expanded'),
        open: ref(true),
        setOpen: vi.fn(),
        isMobile: ref(false),
        openMobile: ref(false),
        setOpenMobile: vi.fn(),
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
                ...(options.props ?? {}),
                ...(options.attrs ?? {}),
              } as never,
              options.slots ?? {},
            ),
          ],
        )
    },
  })

  return mount(Provider)
}

describe('SidebarMenuSub components', () => {
  it('SidebarMenuSub renders as ul with slot and class', () => {
    const wrapper = mountWithSidebar(SidebarMenuSub, {
      props: {
        class: 'sub-extra',
      },
      slots: {
        default: () => h('li', { 'data-test': 'sub-child' }, 'Sub item'),
      },
    })

    const sub = wrapper.get('[data-sidebar="menu-sub"]')
    expect(sub.element.tagName).toBe('UL')
    expect(sub.classes()).toContain('sidebar-menu-sub')
    expect(sub.classes()).toContain('sub-extra')
    expect(wrapper.find('[data-test="sub-child"]').exists()).toBe(true)
    expect(sub.attributes('style') ?? '').not.toContain('display: none')
  })

  it('SidebarMenuSub hides in collapsed icon mode', () => {
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
        return () => h(SidebarMenuSub)
      },
    })

    const wrapper = mount(Provider)
    expect(wrapper.get('[data-sidebar="menu-sub"]').attributes('style')).toContain('display: none')
  })

  it('SidebarMenuSubButton renders as anchor by default', () => {
    const wrapper = mount(SidebarMenuSubButton, {
      slots: {
        default: '<span>Sub link</span>',
      },
    })

    const button = wrapper.get('[data-sidebar="menu-sub-button"]')
    expect(button.element.tagName).toBe('A')
    expect(button.attributes('data-size')).toBe('md')
    expect(button.attributes('data-active')).toBeUndefined()
    expect(button.classes()).toContain('sidebar-menu-sub-btn')
    expect(button.classes()).toContain('sidebar-menu-sub-btn--md')
  })

  it('SidebarMenuSubButton supports custom element, size, active and class', () => {
    const wrapper = mount(SidebarMenuSubButton, {
      props: {
        as: 'button',
        size: 'sm',
        isActive: true,
        class: 'sub-btn-extra',
      },
      attrs: {
        type: 'button',
      },
      slots: {
        default: '<span>Sub link</span>',
      },
    })

    const button = wrapper.get('[data-sidebar="menu-sub-button"]')
    expect(button.element.tagName).toBe('BUTTON')
    expect(button.attributes('data-size')).toBe('sm')
    expect(button.attributes('data-active')).toBe('true')
    expect(button.classes()).toContain('sidebar-menu-sub-btn--sm')
    expect(button.classes()).toContain('sidebar-menu-sub-btn--active')
    expect(button.classes()).toContain('sub-btn-extra')
    expect(button.attributes('type')).toBe('button')
  })

  it('SidebarMenuSubItem renders as li with slot content', () => {
    const wrapper = mount(SidebarMenuSubItem, {
      slots: {
        default: '<a data-test="sub-link">Sub link</a>',
      },
    })

    expect(wrapper.element.tagName).toBe('LI')
    expect(wrapper.find('[data-test="sub-link"]').exists()).toBe(true)
  })
})

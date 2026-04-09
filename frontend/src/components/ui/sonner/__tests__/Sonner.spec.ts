import {describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, h} from 'vue'

import Sonner from '../Sonner.vue'

vi.mock('@vueuse/core', () => ({
  reactiveOmit: (obj: Record<string, unknown>, ...keys: string[]) => {
    const clone = {...obj}
    for (const key of keys) delete clone[key]
    return clone
  },
}))

vi.mock('lucide-vue-next', () => ({
  CircleCheckIcon: defineComponent({
    name: 'CircleCheckIconStub',
    props: {class: {type: String, default: ''}},
    setup(props) {
      return () => h('svg', {'data-test': 'success-icon', class: props.class})
    },
  }),
  InfoIcon: defineComponent({
    name: 'InfoIconStub',
    props: {class: {type: String, default: ''}},
    setup(props) {
      return () => h('svg', {'data-test': 'info-icon', class: props.class})
    },
  }),
  TriangleAlertIcon: defineComponent({
    name: 'TriangleAlertIconStub',
    props: {class: {type: String, default: ''}},
    setup(props) {
      return () => h('svg', {'data-test': 'warning-icon', class: props.class})
    },
  }),
  OctagonXIcon: defineComponent({
    name: 'OctagonXIconStub',
    props: {class: {type: String, default: ''}},
    setup(props) {
      return () => h('svg', {'data-test': 'error-icon', class: props.class})
    },
  }),
  Loader2Icon: defineComponent({
    name: 'Loader2IconStub',
    props: {class: {type: String, default: ''}},
    setup(props) {
      return () => h('svg', {'data-test': 'loading-icon', class: props.class})
    },
  }),
  XIcon: defineComponent({
    name: 'XIconStub',
    props: {class: {type: String, default: ''}},
    setup(props) {
      return () => h('svg', {'data-test': 'close-icon', class: props.class})
    },
  }),
}))

vi.mock('vue-sonner', () => ({
  Toaster: defineComponent({
    name: 'ToasterStub',
    inheritAttrs: false,
    props: {
      theme: {type: String, default: undefined},
      richColors: {type: Boolean, default: undefined},
      expand: {type: Boolean, default: undefined},
      position: {type: String, default: undefined},
      closeButton: {type: Boolean, default: undefined},
      duration: {type: Number, default: undefined},
      visibleToasts: {type: Number, default: undefined},
      toastOptions: {type: Object, default: undefined},
      offset: {type: [Number, String, Object], default: undefined},
      gap: {type: Number, default: undefined},
      containerAriaLabel: {type: String, default: undefined},
      pauseWhenPageIsHidden: {type: Boolean, default: undefined},
      cn: {type: Function, default: undefined},
      hotkey: {type: Array, default: undefined},
      invert: {type: Boolean, default: undefined},
      dir: {type: String, default: undefined},
      icons: {type: Object, default: undefined},
    },
    setup(props, {attrs, slots}) {
      return () =>
        h('div', {...attrs, 'data-test': 'toaster-stub'}, [
          h('div', {'data-test': 'prop-theme'}, String(props.theme)),
          h('div', {'data-test': 'prop-richColors'}, String(props.richColors)),
          h('div', {'data-test': 'prop-expand'}, String(props.expand)),
          h('div', {'data-test': 'prop-position'}, String(props.position)),
          h('div', {'data-test': 'prop-closeButton'}, String(props.closeButton)),
          h('div', {'data-test': 'prop-duration'}, String(props.duration)),
          h('div', {'data-test': 'prop-visibleToasts'}, String(props.visibleToasts)),
          h(
            'div',
            {'data-test': 'prop-toastOptions'},
            JSON.stringify(props.toastOptions),
          ),
          h('div', {'data-test': 'slot-success'}, slots['success-icon']?.()),
          h('div', {'data-test': 'slot-info'}, slots['info-icon']?.()),
          h('div', {'data-test': 'slot-warning'}, slots['warning-icon']?.()),
          h('div', {'data-test': 'slot-error'}, slots['error-icon']?.()),
          h('div', {'data-test': 'slot-loading'}, slots['loading-icon']?.()),
          h('div', {'data-test': 'slot-close'}, slots['close-icon']?.()),
        ])
    },
  }),
}))

describe('Sonner', () => {
  it('renders toaster root with base class', () => {
    const wrapper = mount(Sonner)

    const toaster = wrapper.get('[data-test="toaster-stub"]')
    expect(toaster.classes()).toContain('toaster')
  })

  it('passes delegated props to vue-sonner Toaster', () => {
    const wrapper = mount(Sonner, {
      props: {
        theme: 'dark',
        richColors: true,
        expand: true,
        position: 'top-right',
        closeButton: true,
        duration: 4000,
        visibleToasts: 5,
      },
    })

    expect(wrapper.get('[data-test="prop-theme"]').text()).toBe('dark')
    expect(wrapper.get('[data-test="prop-richColors"]').text()).toBe('true')
    expect(wrapper.get('[data-test="prop-expand"]').text()).toBe('true')
    expect(wrapper.get('[data-test="prop-position"]').text()).toBe('top-right')
    expect(wrapper.get('[data-test="prop-closeButton"]').text()).toBe('true')
    expect(wrapper.get('[data-test="prop-duration"]').text()).toBe('4000')
    expect(wrapper.get('[data-test="prop-visibleToasts"]').text()).toBe('5')
  })

  it('does not forward user toastOptions and uses internal toastOptions classes instead', () => {
    const wrapper = mount(Sonner, {
      props: {
        toastOptions: {
          class: 'user-toast-option',
        },
      },
    })

    const toastOptions = JSON.parse(wrapper.get('[data-test="prop-toastOptions"]').text())

    expect(toastOptions).toEqual({
      classes: {
        toast: 'sonner-toast',
        description: 'sonner-description',
        actionButton: 'sonner-action',
        cancelButton: 'sonner-cancel',
      },
    })

    expect(JSON.stringify(toastOptions)).not.toContain('user-toast-option')
  })

  it('renders success icon slot with correct class', () => {
    const wrapper = mount(Sonner)

    const icon = wrapper.get('[data-test="success-icon"]')
    expect(wrapper.get('[data-test="slot-success"]').find('[data-test="success-icon"]').exists()).toBe(true)
    expect(icon.classes()).toContain('sonner-icon')
  })

  it('renders info icon slot with correct class', () => {
    const wrapper = mount(Sonner)

    const icon = wrapper.get('[data-test="info-icon"]')
    expect(wrapper.get('[data-test="slot-info"]').find('[data-test="info-icon"]').exists()).toBe(true)
    expect(icon.classes()).toContain('sonner-icon')
  })

  it('renders warning icon slot with correct class', () => {
    const wrapper = mount(Sonner)

    const icon = wrapper.get('[data-test="warning-icon"]')
    expect(wrapper.get('[data-test="slot-warning"]').find('[data-test="warning-icon"]').exists()).toBe(true)
    expect(icon.classes()).toContain('sonner-icon')
  })

  it('renders error icon slot with correct class', () => {
    const wrapper = mount(Sonner)

    const icon = wrapper.get('[data-test="error-icon"]')
    expect(wrapper.get('[data-test="slot-error"]').find('[data-test="error-icon"]').exists()).toBe(true)
    expect(icon.classes()).toContain('sonner-icon')
  })

  it('renders loading icon inside wrapper div with spin class', () => {
    const wrapper = mount(Sonner)

    const loadingSlot = wrapper.get('[data-test="slot-loading"]')
    const icon = wrapper.get('[data-test="loading-icon"]')

    expect(loadingSlot.find('div').exists()).toBe(true)
    expect(icon.classes()).toContain('sonner-icon')
    expect(icon.classes()).toContain('sonner-icon--spin')
  })

  it('renders close icon slot with correct class', () => {
    const wrapper = mount(Sonner)

    const icon = wrapper.get('[data-test="close-icon"]')
    expect(wrapper.get('[data-test="slot-close"]').find('[data-test="close-icon"]').exists()).toBe(true)
    expect(icon.classes()).toContain('sonner-icon')
  })

  it('still applies internal toastOptions when other props are passed', () => {
    const wrapper = mount(Sonner, {
      props: {
        theme: 'light',
        position: 'bottom-center',
        expand: false,
      },
    })

    const toastOptions = JSON.parse(wrapper.get('[data-test="prop-toastOptions"]').text())

    expect(wrapper.get('[data-test="prop-theme"]').text()).toBe('light')
    expect(wrapper.get('[data-test="prop-position"]').text()).toBe('bottom-center')
    expect(wrapper.get('[data-test="prop-expand"]').text()).toBe('false')
    expect(toastOptions.classes.toast).toBe('sonner-toast')
    expect(toastOptions.classes.description).toBe('sonner-description')
    expect(toastOptions.classes.actionButton).toBe('sonner-action')
    expect(toastOptions.classes.cancelButton).toBe('sonner-cancel')
  })
})

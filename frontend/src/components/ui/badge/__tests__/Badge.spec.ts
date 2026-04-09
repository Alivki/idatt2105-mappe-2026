import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import Badge from '../Badge.vue'

describe('Badge', () => {
  it('renders slot content', () => {
    const wrapper = mount(Badge, {
      slots: {
        default: 'Gyldig',
      },
    })

    expect(wrapper.text()).toContain('Gyldig')
  })

  it('renders the base badge class', () => {
    const wrapper = mount(Badge)

    expect(wrapper.classes()).toContain('badge')
  })

  it('uses default variant when no props are provided', () => {
    const wrapper = mount(Badge)

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--default')
  })

  it('applies the secondary variant class', () => {
    const wrapper = mount(Badge, {
      props: {
        variant: 'secondary',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--secondary')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the destructive variant class', () => {
    const wrapper = mount(Badge, {
      props: {
        variant: 'destructive',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--destructive')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the outline variant class', () => {
    const wrapper = mount(Badge, {
      props: {
        variant: 'outline',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--outline')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the neutral tone class', () => {
    const wrapper = mount(Badge, {
      props: {
        tone: 'neutral',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-neutral')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the ok tone class', () => {
    const wrapper = mount(Badge, {
      props: {
        tone: 'ok',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-ok')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the warning tone class', () => {
    const wrapper = mount(Badge, {
      props: {
        tone: 'warning',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-warning')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the danger tone class', () => {
    const wrapper = mount(Badge, {
      props: {
        tone: 'danger',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-danger')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('applies the brand tone class', () => {
    const wrapper = mount(Badge, {
      props: {
        tone: 'brand',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-brand')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('uses tone instead of variant when both are provided', () => {
    const wrapper = mount(Badge, {
      props: {
        variant: 'destructive',
        tone: 'ok',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-ok')
    expect(wrapper.classes()).not.toContain('badge--destructive')
    expect(wrapper.classes()).not.toContain('badge--default')
  })

  it('appends a custom class', () => {
    const wrapper = mount(Badge, {
      props: {
        class: 'my-custom-class',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--default')
    expect(wrapper.classes()).toContain('my-custom-class')
  })

  it('appends a custom class together with a tone', () => {
    const wrapper = mount(Badge, {
      props: {
        tone: 'warning',
        class: 'extra-padding',
      },
    })

    expect(wrapper.classes()).toContain('badge')
    expect(wrapper.classes()).toContain('badge--tone-warning')
    expect(wrapper.classes()).toContain('extra-padding')
  })

  it('renders as a div', () => {
    const wrapper = mount(Badge, {
      slots: {
        default: 'Status',
      },
    })

    expect(wrapper.element.tagName).toBe('DIV')
  })
})

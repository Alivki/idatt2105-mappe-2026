import {afterEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, h, nextTick, ref} from 'vue'

import AlertDialog from '../AlertDialog.vue'
import AlertDialogContent from '../AlertDialogContent.vue'
import AlertDialogTrigger from '../AlertDialogTrigger.vue'
import AlertDialogAction from '../AlertDialogAction.vue'
import AlertDialogCancel from '../AlertDialogCancel.vue'

vi.mock('@/components/ui/button', () => ({
  Button: defineComponent({
    name: 'ButtonStub',
    props: {
      variant: {type: String, default: 'default'},
      class: {type: String, default: ''},
    },
    emits: ['click'],
    setup(props, {slots, emit, attrs}) {
      return () =>
        h(
          'button',
          {
            ...attrs,
            class: props.class,
            'data-variant': props.variant,
            onClick: (event: MouseEvent) => emit('click', event),
          },
          slots.default?.(),
        )
    },
  }),
}))

afterEach(() => {
  document.body.innerHTML = ''
})

describe('AlertDialog behavior', () => {
  it('renders slot content', () => {
    const wrapper = mount(AlertDialog, {
      slots: {
        default: '<div data-test="inside">Hei</div>',
      },
    })

    expect(wrapper.find('[data-test="inside"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hei')
  })

  it('starts closed by default', async () => {
    const wrapper = mount(AlertDialog, {
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogContent>
            <div data-test="content">Dialog</div>
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()

    wrapper.unmount()
  })

  it('respects defaultOpen', async () => {
    const wrapper = mount(AlertDialog, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogContent class="extra-content">
            <div data-test="content">Dialog</div>
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="alertdialog"]') as HTMLDivElement | null
    const overlay = document.body.querySelector('.alert-overlay')

    expect(dialog).not.toBeNull()
    expect(dialog?.textContent).toContain('Dialog')
    expect(dialog?.className).toContain('extra-content')
    expect(overlay).not.toBeNull()

    wrapper.unmount()
  })

  it('syncs internal state when controlled open prop changes', async () => {
    const wrapper = mount(AlertDialog, {
      props: {open: false},
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogContent>
            Controlled
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()
    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()

    await wrapper.setProps({open: true})
    await nextTick()
    expect(document.body.querySelector('[role="alertdialog"]')).not.toBeNull()

    await wrapper.setProps({open: false})
    await nextTick()
    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()

    wrapper.unmount()
  })

  it('trigger toggles dialog open and closed and emits update:open', async () => {
    const wrapper = mount(AlertDialog, {
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogTrigger>
            <button data-test="trigger">Åpne</button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            <div data-test="content">Dialog</div>
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogTrigger, AlertDialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(document.body.querySelector('[role="alertdialog"]')).not.toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[true]])

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[true], [false]])

    wrapper.unmount()
  })

  it('action button uses default variant and closes dialog', async () => {
    const wrapper = mount(AlertDialog, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogContent>
            <AlertDialogAction data-test="action">
              Bekreft
            </AlertDialogAction>
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogContent, AlertDialogAction},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const action = document.body.querySelector('[data-test="action"]') as HTMLButtonElement | null
    expect(action).not.toBeNull()
    expect(action?.getAttribute('data-variant')).toBe('default')

    action?.click()
    await nextTick()

    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('action button respects custom variant and class', async () => {
    const wrapper = mount(AlertDialog, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogContent>
            <AlertDialogAction
              variant="destructive"
              class="danger-btn"
              data-test="action"
            >
              Slett
            </AlertDialogAction>
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogContent, AlertDialogAction},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const action = document.body.querySelector('[data-test="action"]') as HTMLButtonElement | null
    expect(action).not.toBeNull()
    expect(action?.getAttribute('data-variant')).toBe('destructive')
    expect(action?.className).toContain('danger-btn')

    wrapper.unmount()
  })

  it('cancel button uses outline variant, merges class and closes dialog', async () => {
    const wrapper = mount(AlertDialog, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <AlertDialogContent>
            <AlertDialogCancel class="cancel-extra" data-test="cancel">
              Avbryt
            </AlertDialogCancel>
          </AlertDialogContent>
        `,
      },
      global: {
        components: {AlertDialogContent, AlertDialogCancel},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const cancel = document.body.querySelector('[data-test="cancel"]') as HTMLButtonElement | null
    expect(cancel).not.toBeNull()
    expect(cancel?.getAttribute('data-variant')).toBe('outline')
    expect(cancel?.className).toContain('alert-cancel')
    expect(cancel?.className).toContain('cancel-extra')

    cancel?.click()
    await nextTick()

    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('can be used as a fully controlled component from the parent', async () => {
    const ControlledHost = defineComponent({
      components: {
        AlertDialog,
        AlertDialogTrigger,
        AlertDialogContent,
      },
      setup() {
        const open = ref(false)
        return {open}
      },
      template: `
        <AlertDialog v-model:open="open">
          <AlertDialogTrigger>
            <button data-test="trigger">Toggle</button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            Controlled by parent
          </AlertDialogContent>
        </AlertDialog>
      `,
    })

    const wrapper = mount(ControlledHost, {
      attachTo: document.body,
      global: {
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    expect(wrapper.vm.open).toBe(false)
    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.vm.open).toBe(true)
    expect(document.body.querySelector('[role="alertdialog"]')).not.toBeNull()

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.vm.open).toBe(false)
    expect(document.body.querySelector('[role="alertdialog"]')).toBeNull()

    wrapper.unmount()
  })
})

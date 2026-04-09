import {afterEach, describe, expect, it, vi} from 'vitest'
import {mount} from '@vue/test-utils'
import {defineComponent, h, nextTick, ref} from 'vue'

import DialogComponent from '../Dialog.vue'
import DialogTrigger from '../DialogTrigger.vue'
import DialogClose from '../DialogClose.vue'
import DialogContent from '../DialogContent.vue'
import DialogScrollContent from '../DialogScrollContent.vue'

vi.mock('lucide-vue-next', () => ({
  X: defineComponent({
    name: 'XIconStub',
    props: {
      class: {type: String, default: ''},
    },
    setup(props) {
      return () => h('svg', {class: props.class, 'data-test': 'x-icon'})
    },
  }),
}))

afterEach(() => {
  document.body.innerHTML = ''
})

describe('Dialog behavior', () => {
  it('renders slot content', () => {
    const wrapper = mount(DialogComponent, {
      slots: {
        default: '<div data-test="inside">Hei</div>',
      },
    })

    expect(wrapper.find('[data-test="inside"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hei')
  })

  it('starts closed by default', async () => {
    const wrapper = mount(DialogComponent, {
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            <div data-test="content">Dialog</div>
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(document.body.querySelector('.dialog-overlay')).toBeNull()

    wrapper.unmount()
  })

  it('respects defaultOpen', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent class="extra-content">
            <div data-test="content">Dialog</div>
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const dialog = document.body.querySelector('[role="dialog"]')
    const overlay = document.body.querySelector('.dialog-overlay')

    expect(dialog).not.toBeNull()
    expect(dialog?.textContent).toContain('Dialog')
    expect(dialog?.className).toContain('dialog-content')
    expect(dialog?.className).toContain('extra-content')
    expect(overlay).not.toBeNull()

    wrapper.unmount()
  })

  it('syncs internal state when controlled open prop changes', async () => {
    const wrapper = mount(DialogComponent, {
      props: {open: false},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            Controlled
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()

    await wrapper.setProps({open: true})
    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()

    await wrapper.setProps({open: false})
    await nextTick()
    expect(document.body.querySelector('[role="dialog"]')).toBeNull()

    wrapper.unmount()
  })

  it('trigger toggles dialog open and closed and emits update:open', async () => {
    const wrapper = mount(DialogComponent, {
      attachTo: document.body,
      slots: {
        default: `
          <DialogTrigger>
            <button data-test="trigger">Åpne</button>
          </DialogTrigger>
          <DialogContent>
            <div data-test="content">Dialog</div>
          </DialogContent>
        `,
      },
      global: {
        components: {DialogTrigger, DialogContent},
        stubs: {teleport: false, transition: true},
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

  it('close component closes dialog and emits update:open=false', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            <DialogClose>
              <span data-test="close-slot">Lukk</span>
            </DialogClose>
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent, DialogClose},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const closeButton = Array.from(document.body.querySelectorAll('button')).find(
      (el) => el.textContent?.includes('Lukk')
    ) as HTMLButtonElement | undefined

    expect(closeButton).toBeTruthy()
    closeButton?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('content overlay click closes dialog', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            Dialog body
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const overlay = document.body.querySelector('.dialog-overlay') as HTMLDivElement | null
    expect(overlay).not.toBeNull()

    overlay?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('content close button closes dialog and renders icon + sr text', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            Dialog body
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const closeButton = document.body.querySelector('.dialog-close') as HTMLButtonElement | null
    expect(closeButton).not.toBeNull()
    expect(document.body.querySelector('.dialog-close__icon')).not.toBeNull()
    expect(document.body.textContent).toContain('Close')

    closeButton?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('pressing Escape closes DialogContent', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            Dialog body
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}))
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('non-Escape key does not close DialogContent', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogContent>
            Dialog body
          </DialogContent>
        `,
      },
      global: {
        components: {DialogContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Enter'}))
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()

    wrapper.unmount()
  })

  it('DialogScrollContent renders when open and respects custom class', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogScrollContent class="scroll-extra">
            <div data-test="scroll-body">Scroll Dialog</div>
          </DialogScrollContent>
        `,
      },
      global: {
        components: {DialogScrollContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const overlay = document.body.querySelector('.dialog-scroll-overlay')
    const dialog = document.body.querySelector('[role="dialog"]')

    expect(overlay).not.toBeNull()
    expect(dialog).not.toBeNull()
    expect(dialog?.textContent).toContain('Scroll Dialog')
    expect(dialog?.className).toContain('dialog-scroll-content')
    expect(dialog?.className).toContain('scroll-extra')

    wrapper.unmount()
  })

  it('DialogScrollContent closes on overlay self click but not inner content click', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogScrollContent>
            <div data-test="scroll-body">Scroll Dialog</div>
          </DialogScrollContent>
        `,
      },
      global: {
        components: {DialogScrollContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const content = document.body.querySelector('.dialog-scroll-content') as HTMLDivElement | null
    expect(content).not.toBeNull()

    content?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).not.toBeNull()

    const overlay = document.body.querySelector('.dialog-scroll-overlay') as HTMLDivElement | null
    overlay?.dispatchEvent(new MouseEvent('click', {bubbles: true}))
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('DialogScrollContent close button closes dialog', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogScrollContent>
            Scroll Dialog
          </DialogScrollContent>
        `,
      },
      global: {
        components: {DialogScrollContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    const closeButton = document.body.querySelector('.dialog-scroll-close') as HTMLButtonElement | null
    expect(closeButton).not.toBeNull()
    expect(document.body.querySelector('.dialog-scroll-close__icon')).not.toBeNull()

    closeButton?.click()
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('pressing Escape closes DialogScrollContent', async () => {
    const wrapper = mount(DialogComponent, {
      props: {defaultOpen: true},
      attachTo: document.body,
      slots: {
        default: `
          <DialogScrollContent>
            Scroll Dialog
          </DialogScrollContent>
        `,
      },
      global: {
        components: {DialogScrollContent},
        stubs: {teleport: false, transition: true},
      },
    })

    await nextTick()

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}))
    await nextTick()

    expect(document.body.querySelector('[role="dialog"]')).toBeNull()
    expect(wrapper.emitted('update:open')).toEqual([[false]])

    wrapper.unmount()
  })

  it('can be used as a fully controlled component from the parent', async () => {
    const ControlledHost = defineComponent({
      components: {
        AppDialog: DialogComponent,
        AppDialogTrigger: DialogTrigger,
        AppDialogContent: DialogContent,
      },
      setup() {
        const open = ref(false)
        return {open}
      },
      template: `
        <AppDialog v-model:open="open">
          <AppDialogTrigger>
            <button data-test="trigger">Toggle</button>
          </AppDialogTrigger>
          <AppDialogContent>
            Controlled by parent
          </AppDialogContent>
        </AppDialog>
      `,
    })

    const wrapper = mount(ControlledHost, {
      attachTo: document.body,
      global: {
        stubs: {teleport: false, transition: true},
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

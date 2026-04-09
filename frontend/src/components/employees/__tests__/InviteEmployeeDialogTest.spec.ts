import { mount } from '@vue/test-utils'
import { defineComponent, h, ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import EmployeeInviteDialog from '../InviteEmployeeDialog.vue'

vi.mock('lucide-vue-next', () => ({
  Mail: defineComponent({
    name: 'Mail',
    setup() {
      return () => h('svg', { 'data-icon': 'Mail' })
    },
  }),
}))

vi.mock('@/components/ui/button/Button.vue', () => ({
  default: defineComponent({
    name: 'Button',
    emits: ['click'],
    props: {
      type: { type: String, default: 'button' },
      disabled: { type: Boolean, default: false },
    },
    setup(props, { slots, emit, attrs }) {
      return () => h('button', {
        ...attrs,
        type: props.type,
        disabled: props.disabled,
        onClick: (e: MouseEvent) => emit('click', e),
      }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/dialog/Dialog.vue', () => ({
  default: defineComponent({
    name: 'Dialog',
    props: { open: { type: Boolean, default: false } },
    emits: ['update:open'],
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'dialog' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/dialog/DialogContent.vue', () => ({
  default: defineComponent({
    name: 'DialogContent',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'dialog-content' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/dialog/DialogHeader.vue', () => ({
  default: defineComponent({
    name: 'DialogHeader',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'dialog-header' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/dialog/DialogTitle.vue', () => ({
  default: defineComponent({
    name: 'DialogTitle',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'dialog-title' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/Select.vue', () => ({
  default: defineComponent({
    name: 'Select',
    props: { modelValue: { type: String, default: '' } },
    emits: ['update:modelValue'],
    setup(props, { emit, slots }) {
      return () => h('div', [
        h('select', {
          class: 'role-select',
          value: props.modelValue,
          onChange: (e: Event) => emit('update:modelValue', (e.target as HTMLSelectElement).value),
        }, [
          h('option', { value: 'ADMIN' }, 'Admin'),
          h('option', { value: 'MANAGER' }, 'Leder'),
          h('option', { value: 'EMPLOYEE' }, 'Ansatt'),
        ]),
        slots.default?.(),
      ])
    },
  }),
}))

vi.mock('@/components/ui/select/SelectContent.vue', () => ({
  default: defineComponent({
    name: 'SelectContent',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'select-content' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectItem.vue', () => ({
  default: defineComponent({
    name: 'SelectItem',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'select-item' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectTrigger.vue', () => ({
  default: defineComponent({
    name: 'SelectTrigger',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'select-trigger' }, slots.default?.())
    },
  }),
}))

vi.mock('@/components/ui/select/SelectValue.vue', () => ({
  default: defineComponent({
    name: 'SelectValue',
    setup(_, { slots }) {
      return () => h('div', { 'data-test': 'select-value' }, slots.default?.())
    },
  }),
}))

describe('EmployeeInviteDialog', () => {
  it('shows validation error for invalid email', async () => {
    const wrapper = mount(EmployeeInviteDialog, {
      props: {
        open: true,
        isPending: false,
      },
    })

    await wrapper.get('#invite-email').setValue('not-an-email')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Ugyldig e-postadresse')
    expect(wrapper.emitted('invite')).toBeFalsy()
  })

  it('emits invite with trimmed email and selected role', async () => {
    const wrapper = mount(EmployeeInviteDialog, {
      props: {
        open: true,
        isPending: false,
      },
    })

    await wrapper.get('#invite-email').setValue('  kari@example.com  ')
    await wrapper.get('select.role-select').setValue('MANAGER')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('invite')?.[0]).toEqual([
      { email: 'kari@example.com', role: 'MANAGER' },
    ])
    expect(wrapper.text()).not.toContain('Ugyldig e-postadresse')
  })

  it('shows validation error and emits close when cancel is clicked', async () => {
    const wrapper = mount(EmployeeInviteDialog, {
      props: {
        open: true,
        isPending: false,
      },
    })

    await wrapper.get('#invite-email').setValue('bad-email')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Ugyldig e-postadresse')
    expect((wrapper.get('#invite-email').element as HTMLInputElement).value).toBe('bad-email')
    expect((wrapper.get('select.role-select').element as HTMLSelectElement).value).toBe('EMPLOYEE')

    const cancelButton = wrapper.findAll('button').find((b) => b.text().includes('Avbryt'))
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('update:open')?.some((e) => e[0] === false)).toBe(true)
  })

  it('disables submit button and shows pending label', () => {
    const wrapper = mount(EmployeeInviteDialog, {
      props: {
        open: true,
        isPending: true,
      },
    })

    const submitButton = wrapper.findAll('button').find((b) => b.attributes('type') === 'submit')
    expect(submitButton?.text()).toContain('Sender...')
    expect(submitButton?.attributes('disabled')).toBeDefined()
  })
})

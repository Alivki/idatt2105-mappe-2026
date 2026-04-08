import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import ChecklistDialog from '../ChecklistFormDialog.vue'

function makePassThrough(name: string, tag = 'div') {
  return defineComponent({
    name,
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      return () => h(tag, attrs, slots.default?.())
    },
  })
}

vi.mock('@/components/ui/dialog/Dialog.vue', () => ({
  default: defineComponent({
    name: 'DialogStub',
    props: { open: { type: Boolean, default: false } },
    emits: ['update:open'],
    setup(props, { attrs, slots }) {
      return () => h('div', { 'data-open': String(props.open), ...attrs }, slots.default?.())
    },
  }),
}))
vi.mock('@/components/ui/dialog/DialogContent.vue', () => ({ default: makePassThrough('DialogContent') }))
vi.mock('@/components/ui/dialog/DialogHeader.vue', () => ({ default: makePassThrough('DialogHeader') }))
vi.mock('@/components/ui/dialog/DialogTitle.vue', () => ({ default: makePassThrough('DialogTitle', 'h2') }))
vi.mock('@/components/ui/dialog/DialogDescription.vue', () => ({ default: makePassThrough('DialogDescription', 'p') }))
vi.mock('@/components/ui/dialog/DialogFooter.vue', () => ({ default: makePassThrough('DialogFooter') }))
vi.mock('@/components/ui/button/Button.vue', () => ({ default: makePassThrough('ButtonStub', 'button') }))
vi.mock('@/components/ui/input/Input.vue', () => ({
  default: defineComponent({
    name: 'UiInputStub',
    props: { modelValue: { type: String, default: '' } },
    emits: ['update:modelValue'],
    inheritAttrs: false,
    setup(props, { emit, attrs }) {
      return () => h('input', {
        ...attrs,
        value: props.modelValue,
        onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLInputElement).value),
      })
    },
  }),
}))
vi.mock('@/components/ui/textarea/Textarea.vue', () => ({
  default: defineComponent({
    name: 'UiTextareaStub',
    props: { modelValue: { type: String, default: '' } },
    emits: ['update:modelValue'],
    inheritAttrs: false,
    setup(props, { emit, attrs }) {
      return () => h('textarea', {
        ...attrs,
        value: props.modelValue,
        onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLTextAreaElement).value),
      })
    },
  }),
}))
vi.mock('@/components/ui/select/Select.vue', () => ({
  default: defineComponent({
    name: 'SelectStub',
    props: { modelValue: { type: String, default: '' } },
    emits: ['update:modelValue'],
    setup(props, { emit, attrs }) {
      return () =>
        h('select', {
          ...attrs,
          value: props.modelValue,
          onChange: (e: Event) => emit('update:modelValue', (e.target as HTMLSelectElement).value),
        }, [
          h('option', { value: 'DAILY' }, 'Daglig'),
          h('option', { value: 'WEEKLY' }, 'Ukentlig'),
          h('option', { value: 'MONTHLY' }, 'Månedlig'),
          h('option', { value: 'YEARLY' }, 'Årlig'),
        ])
    },
  }),
}))
vi.mock('@/components/ui/select/SelectContent.vue', () => ({ default: makePassThrough('SelectContent') }))
vi.mock('@/components/ui/select/SelectItem.vue', () => ({ default: makePassThrough('SelectItem') }))
vi.mock('@/components/ui/select/SelectTrigger.vue', () => ({ default: makePassThrough('SelectTrigger') }))
vi.mock('@/components/ui/select/SelectValue.vue', () => ({ default: makePassThrough('SelectValue') }))

const initialChecklist = {
  id: 12,
  name: 'Stengerutine',
  description: 'Sjekk dører og lys',
  frequency: 'WEEKLY',
  active: false,
}

describe('ChecklistFormDialog', () => {
  it('renders create mode defaults', () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'create',
      },
    })

    expect(wrapper.text()).toContain('Opprett ny sjekkliste')
    expect((wrapper.get('input').element as HTMLInputElement).value).toBe('')
    expect((wrapper.get('textarea').element as HTMLTextAreaElement).value).toBe('')
    expect((wrapper.get('select').element as HTMLSelectElement).value).toBe('DAILY')
  })

  it('shows validation error when name is blank', async () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'create',
      },
    })

    await wrapper.get('input').setValue('   ')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Navn på sjekkliste er påkrevd')
    expect(wrapper.emitted('create')).toBeUndefined()
  })

  it('emits create payload with trimmed values and undefined blank description', async () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'create',
      },
    })

    await wrapper.get('input').setValue('  Morgenrutine  ')
    await wrapper.get('textarea').setValue('   ')
    await wrapper.get('select').setValue('MONTHLY')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('create')).toEqual([[
      {
        name: 'Morgenrutine',
        description: undefined,
        frequency: 'MONTHLY',
      },
    ]])
  })

  it('hydrates edit mode and emits update payload', async () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'edit',
        initialChecklist,
      },
    })

    expect(wrapper.text()).toContain('Rediger sjekkliste')
    expect((wrapper.get('input').element as HTMLInputElement).value).toBe('Stengerutine')
    expect((wrapper.get('textarea').element as HTMLTextAreaElement).value).toBe('Sjekk dører og lys')
    expect((wrapper.get('select').element as HTMLSelectElement).value).toBe('WEEKLY')

    await wrapper.get('input').setValue('  Ny stengerutine ')
    await wrapper.get('textarea').setValue('  Oppdatert tekst ')
    await wrapper.get('select').setValue('YEARLY')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('update')).toEqual([[
      {
        checklistId: 12,
        data: {
          name: 'Ny stengerutine',
          description: 'Oppdatert tekst',
          frequency: 'YEARLY',
          active: false,
        },
      },
    ]])
  })

  it('does not emit update in edit mode when initial checklist is missing', async () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'edit',
        initialChecklist: null,
      },
    })

    await wrapper.get('input').setValue('Noe navn')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('update')).toBeUndefined()
  })

  it('resets form state when opening from edit mode to create mode', async () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'edit',
        initialChecklist,
      },
    })

    await wrapper.setProps({ open: false })
    await wrapper.setProps({ mode: 'create', initialChecklist: null, open: true })

    expect((wrapper.get('input').element as HTMLInputElement).value).toBe('')
    expect((wrapper.get('textarea').element as HTMLTextAreaElement).value).toBe('')
    expect((wrapper.get('select').element as HTMLSelectElement).value).toBe('DAILY')
  })

  it('emits update:open false when cancel button is pressed', async () => {
    const wrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'create',
      },
    })

    const cancelButton = wrapper.findAll('button').find((btn) => btn.text() === 'Avbryt')
    expect(cancelButton).toBeTruthy()
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('update:open')).toEqual([[false]])
  })

  it('shows submitting labels for both modes', () => {
    const createWrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'create',
        submitting: true,
      },
    })
    expect(createWrapper.text()).toContain('Oppretter...')

    const editWrapper = mount(ChecklistDialog, {
      props: {
        open: true,
        mode: 'edit',
        initialChecklist,
        submitting: true,
      },
    })
    expect(editWrapper.text()).toContain('Lagrer...')
  })
})

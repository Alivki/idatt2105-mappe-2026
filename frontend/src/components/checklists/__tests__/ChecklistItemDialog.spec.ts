import { mount } from '@vue/test-utils'
import { defineComponent, h } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import ChecklistItemDialog from '../ChecklistItemFormDialog.vue'

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

const checklist = {
  id: 5,
  name: 'Morgenåpning',
}

const initialItem = {
  id: 99,
  title: 'Skru på kaffemaskin',
  description: 'Må være varm før åpning',
}

describe('ChecklistItemFormDialog', () => {
  it('renders create mode defaults', () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'create',
        checklist,
      },
    })

    expect(wrapper.text()).toContain('Legg til oppgave')
    expect(wrapper.text()).toContain('Morgenåpning')
    expect((wrapper.get('input').element as HTMLInputElement).value).toBe('')
    expect((wrapper.get('textarea').element as HTMLTextAreaElement).value).toBe('')
  })

  it('shows validation error when title is blank', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'create',
        checklist,
      },
    })

    await wrapper.get('input').setValue('   ')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.text()).toContain('Oppgaven må ha en tittel')
    expect(wrapper.emitted('create')).toBeUndefined()
  })

  it('emits create payload with trimmed title and undefined description', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'create',
        checklist,
      },
    })

    await wrapper.get('input').setValue('  Lås opp inngang  ')
    await wrapper.get('textarea').setValue('   ')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('create')).toEqual([[
      {
        checklistId: 5,
        data: {
          title: 'Lås opp inngang',
          description: undefined,
        },
      },
    ]])
  })

  it('hydrates edit mode and emits update payload', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'edit',
        checklist,
        initialItem,
      },
    })

    expect(wrapper.text()).toContain('Rediger oppgave')
    expect((wrapper.get('input').element as HTMLInputElement).value).toBe('Skru på kaffemaskin')
    expect((wrapper.get('textarea').element as HTMLTextAreaElement).value).toBe('Må være varm før åpning')

    await wrapper.get('input').setValue('  Fyll på kaffebønner ')
    await wrapper.get('textarea').setValue('  Gjør dette først ')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('update')).toEqual([[
      {
        checklistId: 5,
        itemId: 99,
        data: {
          title: 'Fyll på kaffebønner',
          description: 'Gjør dette først',
        },
      },
    ]])
  })

  it('does not emit create or update when checklist is missing', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'create',
        checklist: null,
      },
    })

    await wrapper.get('input').setValue('Noe gyldig')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('create')).toBeUndefined()
    expect(wrapper.emitted('update')).toBeUndefined()
  })

  it('does not emit update in edit mode when initial item is missing', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'edit',
        checklist,
        initialItem: null,
      },
    })

    await wrapper.get('input').setValue('Noe gyldig')
    await wrapper.get('form').trigger('submit.prevent')

    expect(wrapper.emitted('update')).toBeUndefined()
  })

  it('resets fields when reopening in create mode', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'edit',
        checklist,
        initialItem,
      },
    })

    await wrapper.setProps({ open: false })
    await wrapper.setProps({ mode: 'create', initialItem: null, open: true })

    expect((wrapper.get('input').element as HTMLInputElement).value).toBe('')
    expect((wrapper.get('textarea').element as HTMLTextAreaElement).value).toBe('')
  })

  it('emits update:open false on cancel', async () => {
    const wrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'create',
        checklist,
      },
    })

    const cancelButton = wrapper.findAll('button').find((btn) => btn.text() === 'Avbryt')
    expect(cancelButton).toBeTruthy()
    await cancelButton!.trigger('click')

    expect(wrapper.emitted('update:open')).toEqual([[false]])
  })

  it('shows submitting labels in both modes', () => {
    const createWrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'create',
        checklist,
        submitting: true,
      },
    })
    expect(createWrapper.text()).toContain('Lagrer...')

    const editWrapper = mount(ChecklistItemDialog, {
      props: {
        open: true,
        mode: 'edit',
        checklist,
        initialItem,
        submitting: true,
      },
    })
    expect(editWrapper.text()).toContain('Oppdaterer...')
  })
})

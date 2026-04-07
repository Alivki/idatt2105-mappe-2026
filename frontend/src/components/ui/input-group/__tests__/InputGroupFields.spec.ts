import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref } from 'vue'

import InputGroupInput from '../InputGroupInput.vue'
import InputGroupTextarea from '../InputGroupTextarea.vue'

describe('InputGroupInput', () => {
  it('renders an input with the correct slot marker', () => {
    const wrapper = mount(InputGroupInput)

    expect(wrapper.element.tagName).toBe('INPUT')
    expect(wrapper.attributes('data-slot')).toBe('input-group-control')
    expect(wrapper.classes()).toContain('input-group-input')
  })

  it('applies custom class', () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        class: 'input-extra',
      },
    })

    expect(wrapper.classes()).toContain('input-group-input')
    expect(wrapper.classes()).toContain('input-extra')
  })

  it('forwards attrs to the input element', () => {
    const wrapper = mount(InputGroupInput, {
      attrs: {
        id: 'email',
        type: 'email',
        placeholder: 'Skriv e-post',
        name: 'email',
        disabled: true,
      },
    })

    expect(wrapper.attributes('id')).toBe('email')
    expect(wrapper.attributes('type')).toBe('email')
    expect(wrapper.attributes('placeholder')).toBe('Skriv e-post')
    expect(wrapper.attributes('name')).toBe('email')
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('uses modelValue when provided', () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        modelValue: 'hello',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('hello')
  })

  it('uses defaultValue when modelValue is not provided', () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        defaultValue: 'default text',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('default text')
  })

  it('prefers modelValue over defaultValue when both are provided', () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        modelValue: 'controlled',
        defaultValue: 'fallback',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('controlled')
  })

  it('emits update:modelValue when user types', async () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        modelValue: '',
      },
    })

    await wrapper.setValue('new value')

    expect(wrapper.emitted('update:modelValue')).toEqual([['new value']])
  })

  it('updates rendered value when modelValue prop changes', async () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        modelValue: 'first',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('first')

    await wrapper.setProps({ modelValue: 'second' })
    await nextTick()

    expect((wrapper.element as HTMLInputElement).value).toBe('second')
  })

  it('works with v-model in a parent component', async () => {
    const Host = defineComponent({
      components: { InputGroupInput },
      setup() {
        const value = ref('start')
        return { value }
      },
      template: `<InputGroupInput v-model="value" data-test="input" />`,
    })

    const wrapper = mount(Host)
    const input = wrapper.get('[data-test="input"]')

    expect((wrapper.vm as { value: string }).value).toBe('start')
    expect((input.element as HTMLInputElement).value).toBe('start')

    await input.setValue('changed')
    await nextTick()

    expect((wrapper.vm as { value: string }).value).toBe('changed')
    expect((input.element as HTMLInputElement).value).toBe('changed')
  })

  it('handles numeric values', async () => {
    const wrapper = mount(InputGroupInput, {
      props: {
        modelValue: 123,
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('123')

    await wrapper.setProps({ modelValue: 456 })
    await nextTick()

    expect((wrapper.element as HTMLInputElement).value).toBe('456')
  })
})

describe('InputGroupTextarea', () => {
  it('renders a textarea with the correct slot marker', () => {
    const wrapper = mount(InputGroupTextarea)

    expect(wrapper.element.tagName).toBe('TEXTAREA')
    expect(wrapper.attributes('data-slot')).toBe('input-group-control')
    expect(wrapper.classes()).toContain('input-group-textarea')
  })

  it('applies custom class', () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        class: 'textarea-extra',
      },
    })

    expect(wrapper.classes()).toContain('input-group-textarea')
    expect(wrapper.classes()).toContain('textarea-extra')
  })

  it('forwards attrs to the textarea element', () => {
    const wrapper = mount(InputGroupTextarea, {
      attrs: {
        id: 'message',
        placeholder: 'Skriv melding',
        rows: '5',
        disabled: true,
      },
    })

    expect(wrapper.attributes('id')).toBe('message')
    expect(wrapper.attributes('placeholder')).toBe('Skriv melding')
    expect(wrapper.attributes('rows')).toBe('5')
    expect(wrapper.attributes('disabled')).toBeDefined()
  })

  it('uses modelValue when provided', () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        modelValue: 'hello',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('hello')
  })

  it('uses defaultValue when modelValue is not provided', () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        defaultValue: 'default text',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('default text')
  })

  it('prefers modelValue over defaultValue when both are provided', () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        modelValue: 'controlled',
        defaultValue: 'fallback',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('controlled')
  })

  it('emits update:modelValue when user types', async () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        modelValue: '',
      },
    })

    await wrapper.setValue('new text')

    expect(wrapper.emitted('update:modelValue')).toEqual([['new text']])
  })

  it('updates rendered value when modelValue prop changes', async () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        modelValue: 'first',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('first')

    await wrapper.setProps({ modelValue: 'second' })
    await nextTick()

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('second')
  })

  it('works with v-model in a parent component', async () => {
    const Host = defineComponent({
      components: { InputGroupTextarea },
      setup() {
        const value = ref('start')
        return { value }
      },
      template: `<InputGroupTextarea v-model="value" data-test="textarea" />`,
    })

    const wrapper = mount(Host)
    const textarea = wrapper.get('[data-test="textarea"]')

    expect((wrapper.vm as { value: string }).value).toBe('start')
    expect((textarea.element as HTMLTextAreaElement).value).toBe('start')

    await textarea.setValue('changed')
    await nextTick()

    expect((wrapper.vm as { value: string }).value).toBe('changed')
    expect((textarea.element as HTMLTextAreaElement).value).toBe('changed')
  })

  it('handles numeric values', async () => {
    const wrapper = mount(InputGroupTextarea, {
      props: {
        modelValue: 10,
      },
    })

    expect(wrapper.element.value).toBe(10)

    await wrapper.setProps({ modelValue: 20 })
    await nextTick()

    expect(wrapper.element.value).toBe(20)
  })
})

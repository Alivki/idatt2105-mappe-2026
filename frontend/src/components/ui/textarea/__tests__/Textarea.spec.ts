import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref } from 'vue'

import Textarea from '../Textarea.vue'

describe('Textarea', () => {
  it('renders a textarea element', () => {
    const wrapper = mount(Textarea)

    expect(wrapper.element.tagName.toLowerCase()).toBe('textarea')
  })

  it('applies the base textarea class', () => {
    const wrapper = mount(Textarea)

    expect(wrapper.classes()).toContain('textarea')
  })

  it('applies a custom class', () => {
    const wrapper = mount(Textarea, {
      props: {
        class: 'custom-textarea',
      },
    })

    expect(wrapper.classes()).toContain('textarea')
    expect(wrapper.classes()).toContain('custom-textarea')
  })

  it('forwards attrs to the textarea element', () => {
    const wrapper = mount(Textarea, {
      attrs: {
        id: 'message',
        name: 'message',
        placeholder: 'Write here',
        rows: '5',
        disabled: true,
        'data-test': 'message-textarea',
      },
    })

    expect(wrapper.attributes('id')).toBe('message')
    expect(wrapper.attributes('name')).toBe('message')
    expect(wrapper.attributes('placeholder')).toBe('Write here')
    expect(wrapper.attributes('rows')).toBe('5')
    expect(wrapper.attributes('disabled')).toBeDefined()
    expect(wrapper.attributes('data-test')).toBe('message-textarea')
  })

  it('uses modelValue when provided', () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: 'hello',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('hello')
  })

  it('uses defaultValue when modelValue is not provided', () => {
    const wrapper = mount(Textarea, {
      props: {
        defaultValue: 'default text',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('default text')
  })

  it('prefers modelValue over defaultValue when both are provided', () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: 'controlled',
        defaultValue: 'fallback',
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe('controlled')
  })

  it('emits update:modelValue when user types a string', async () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: '',
      },
    })

    await wrapper.setValue('new value')

    expect(wrapper.emitted('update:modelValue')).toEqual([['new value']])
  })

  it('emits update:modelValue multiple times as value changes', async () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: '',
      },
    })

    await wrapper.setValue('a')
    await wrapper.setValue('ab')
    await wrapper.setValue('abc')

    expect(wrapper.emitted('update:modelValue')).toEqual([['a'], ['ab'], ['abc']])
  })

  it('updates rendered value when controlled modelValue changes', async () => {
    const wrapper = mount(Textarea, {
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
      components: { Textarea },
      setup() {
        const value = ref('start')
        return { value }
      },
      template: `
        <Textarea v-model="value" data-test="textarea" />
      `,
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

  it('handles numeric modelValue', async () => {
    const wrapper = mount(Textarea, {
      props: {
        modelValue: 10,
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe(10 as unknown as string)

    await wrapper.setProps({ modelValue: 20 })
    await nextTick()

    expect((wrapper.element as HTMLTextAreaElement).value).toBe(20 as unknown as string)
  })

  it('handles numeric defaultValue', () => {
    const wrapper = mount(Textarea, {
      props: {
        defaultValue: 42,
      },
    })

    expect((wrapper.element as HTMLTextAreaElement).value).toBe(42 as unknown as string)
  })
})

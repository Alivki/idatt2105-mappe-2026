import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref } from 'vue'

import Input from '../Input.vue'

describe('Input', () => {
  it('renders an input element', () => {
    const wrapper = mount(Input)

    expect(wrapper.element.tagName).toBe('INPUT')
  })

  it('applies the base input class', () => {
    const wrapper = mount(Input)

    expect(wrapper.classes()).toContain('input')
  })

  it('applies a custom class', () => {
    const wrapper = mount(Input, {
      props: {
        class: 'custom-input',
      },
    })

    expect(wrapper.classes()).toContain('input')
    expect(wrapper.classes()).toContain('custom-input')
  })

  it('forwards attrs to the input element', () => {
    const wrapper = mount(Input, {
      attrs: {
        id: 'email',
        name: 'email',
        type: 'email',
        placeholder: 'Skriv inn e-post',
        disabled: true,
        'data-test': 'email-input',
      },
    })

    expect(wrapper.attributes('id')).toBe('email')
    expect(wrapper.attributes('name')).toBe('email')
    expect(wrapper.attributes('type')).toBe('email')
    expect(wrapper.attributes('placeholder')).toBe('Skriv inn e-post')
    expect(wrapper.attributes('disabled')).toBeDefined()
    expect(wrapper.attributes('data-test')).toBe('email-input')
  })

  it('uses modelValue when provided', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: 'hello',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('hello')
  })

  it('uses defaultValue when modelValue is not provided', () => {
    const wrapper = mount(Input, {
      props: {
        defaultValue: 'default text',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('default text')
  })

  it('prefers modelValue over defaultValue when both are provided', () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: 'controlled',
        defaultValue: 'fallback',
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('controlled')
  })

  it('emits update:modelValue when user types a string', async () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: '',
      },
    })

    await wrapper.setValue('new value')

    expect(wrapper.emitted('update:modelValue')).toEqual([['new value']])
  })

  it('emits update:modelValue multiple times as value changes', async () => {
    const wrapper = mount(Input, {
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
    const wrapper = mount(Input, {
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
      components: { Input },
      setup() {
        const value = ref('start')
        return { value }
      },
      template: `
        <Input v-model="value" data-test="input" />
      `,
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

  it('handles numeric modelValue', async () => {
    const wrapper = mount(Input, {
      props: {
        modelValue: 123,
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('123')

    await wrapper.setProps({ modelValue: 456 })
    await nextTick()

    expect((wrapper.element as HTMLInputElement).value).toBe('456')
  })

  it('handles numeric defaultValue', () => {
    const wrapper = mount(Input, {
      props: {
        defaultValue: 42,
      },
    })

    expect((wrapper.element as HTMLInputElement).value).toBe('42')
  })
})

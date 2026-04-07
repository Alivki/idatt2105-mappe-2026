import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, nextTick, ref } from 'vue'

import Select from '../Select.vue'
import SelectTrigger from '../SelectTrigger.vue'
import SelectValue from '../SelectValue.vue'
import SelectContent from '../SelectContent.vue'
import SelectItem from '../SelectItem.vue'

vi.mock('lucide-vue-next', () => ({
  ChevronDown: {
    name: 'ChevronDownStub',
    template: '<svg class="select-trigger__icon" data-test="chevron-down" />',
  },
  CheckIcon: {
    name: 'CheckIconStub',
    template: '<svg class="select-item__check" data-test="check-icon" />',
  },
}))

afterEach(() => {
  document.body.innerHTML = ''
})

describe('Select core behavior', () => {
  it('renders slot content', () => {
    const wrapper = mount(Select, {
      slots: {
        default: '<div data-test="inside">Hei</div>',
      },
    })

    expect(wrapper.find('[data-test="inside"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Hei')
  })

  it('renders root with base class and forwards attrs', () => {
    const wrapper = mount(Select, {
      attrs: {
        id: 'my-select',
        'data-test': 'root',
      },
    })

    expect(wrapper.classes()).toContain('select-root')
    expect(wrapper.attributes('id')).toBe('my-select')
    expect(wrapper.attributes('data-test')).toBe('root')
  })

  it('SelectValue shows placeholder when nothing is selected', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue },
      template: `
        <Select>
          <SelectTrigger>
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
        </Select>
      `,
    })

    const wrapper = mount(Host)
    const value = wrapper.get('.select-value')

    expect(value.text()).toContain('Choose one')
    expect(value.classes()).toContain('select-value--placeholder')
  })

  it('uses defaultValue as initial selection and shows registered label', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue, SelectContent, SelectItem },
      template: `
        <Select defaultValue="b">
          <SelectTrigger>
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="a">Apple</SelectItem>
            <SelectItem value="b">Banana</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      global: {
        stubs: { transition: true },
      },
    })

    await nextTick()
    await nextTick()

    const value = wrapper.get('.select-value')
    expect(value.text()).toContain('Banana')
    expect(value.classes()).not.toContain('select-value--placeholder')
  })

  it('uses modelValue over defaultValue', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue, SelectContent, SelectItem },
      template: `
        <Select modelValue="a" defaultValue="b">
          <SelectTrigger>
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="a">Apple</SelectItem>
            <SelectItem value="b">Banana</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      global: {
        stubs: { transition: true },
      },
    })

    await nextTick()
    await nextTick()

    expect(wrapper.get('.select-value').text()).toContain('Apple')
  })

  it('syncs selection when controlled modelValue prop changes', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue, SelectContent, SelectItem },
      props: {
        modelValue: {
          type: String,
          default: 'a',
        },
      },
      template: `
        <Select :modelValue="modelValue">
          <SelectTrigger>
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="a">Apple</SelectItem>
            <SelectItem value="b">Banana</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      props: {
        modelValue: 'a',
      },
      global: {
        stubs: { transition: true },
      },
    })

    await nextTick()
    await nextTick()

    expect(wrapper.get('.select-value').text()).toContain('Apple')

    await wrapper.setProps({ modelValue: 'b' })
    await nextTick()

    expect(wrapper.get('.select-value').text()).toContain('Banana')
  })

  it('trigger toggles aria-expanded state', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectContent },
      template: `
        <Select>
          <SelectTrigger data-test="trigger">Open</SelectTrigger>
          <SelectContent>
            <div data-test="content">Menu</div>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    const trigger = wrapper.get('[data-test="trigger"]')

    expect(trigger.attributes('role')).toBe('combobox')
    expect(trigger.attributes('aria-expanded')).toBe('false')
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await trigger.trigger('click')
    await nextTick()

    expect(trigger.attributes('aria-expanded')).toBe('true')
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await trigger.trigger('click')
    await nextTick()

    expect(trigger.attributes('aria-expanded')).toBe('false')
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('trigger renders icon and respects disabled/class', () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger },
      template: `
        <Select>
          <SelectTrigger disabled class="extra-trigger" data-test="trigger">Open</SelectTrigger>
        </Select>
      `,
    })

    const wrapper = mount(Host)
    const trigger = wrapper.get('[data-test="trigger"]')

    expect(trigger.attributes('disabled')).toBeDefined()
    expect(trigger.classes()).toContain('select-trigger')
    expect(trigger.classes()).toContain('extra-trigger')
    expect(trigger.find('[data-test="chevron-down"]').exists()).toBe(true)
  })

  it('content renders and keeps content mounted after outside click in this implementation', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectContent },
      template: `
        <Select>
          <SelectTrigger data-test="trigger">Open</SelectTrigger>
          <SelectContent class="extra-content">
            <div data-test="content">Menu</div>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    const content = wrapper.get('.select-content')
    expect(content.classes()).toContain('extra-content')
    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    document.dispatchEvent(new MouseEvent('mousedown', { bubbles: true }))
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('content does not close when clicking inside root', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectContent },
      template: `
        <Select data-test="root">
          <SelectTrigger data-test="trigger">Open</SelectTrigger>
          <SelectContent>
            <div data-test="content">Menu</div>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    await wrapper.get('[data-test="trigger"]').trigger('mousedown')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('Escape does not close the content in this implementation', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectContent },
      template: `
        <Select>
          <SelectTrigger data-test="trigger">Open</SelectTrigger>
          <SelectContent>
            <div data-test="content">Menu</div>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
    await nextTick()

    expect(wrapper.find('[data-test="content"]').exists()).toBe(true)

    wrapper.unmount()
  })

  it('selecting an item emits update:modelValue, closes content and updates displayed label', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue, SelectContent, SelectItem },
      template: `
        <Select>
          <SelectTrigger data-test="trigger">
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="a" data-test="item-a">Apple</SelectItem>
            <SelectItem value="b" data-test="item-b">Banana</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    const select = wrapper.findComponent(Select)

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    const itemB = wrapper.get('[data-test="item-b"]')
    expect(itemB.attributes('role')).toBe('option')
    expect(itemB.attributes('aria-selected')).toBe('false')
    expect(itemB.attributes('data-state')).toBe('unchecked')

    await itemB.trigger('click')
    await nextTick()

    expect(select.emitted('update:modelValue')).toEqual([['b']])
    expect(wrapper.find('[data-test="content"]').exists()).toBe(false)
    expect(wrapper.get('.select-value').text()).toContain('Banana')
  })

  it('selected item shows selected state and check icon', async () => {
    const Host = defineComponent({
      components: { Select, SelectContent, SelectItem },
      template: `
        <Select defaultValue="a">
          <SelectContent>
            <SelectItem value="a" data-test="item-a">Apple</SelectItem>
            <SelectItem value="b" data-test="item-b">Banana</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      global: {
        stubs: { transition: true },
      },
    })

    await nextTick()
    await nextTick()

    const itemA = wrapper.get('[data-test="item-a"]')
    const itemB = wrapper.get('[data-test="item-b"]')

    expect(itemA.attributes('aria-selected')).toBe('true')
    expect(itemA.attributes('data-state')).toBe('checked')
    expect(itemA.find('[data-test="check-icon"]').exists()).toBe(true)

    expect(itemB.attributes('aria-selected')).toBe('false')
    expect(itemB.attributes('data-state')).toBe('unchecked')
    expect(itemB.find('[data-test="check-icon"]').exists()).toBe(false)
  })

  it('disabled item does not select and applies disabled state', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue, SelectContent, SelectItem },
      template: `
        <Select>
          <SelectTrigger data-test="trigger">
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="a" disabled class="extra-item" data-test="item-a">Apple</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    const select = wrapper.findComponent(Select)

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    const itemA = wrapper.get('[data-test="item-a"]')
    expect(itemA.attributes('data-disabled')).toBe('true')
    expect(itemA.classes()).toContain('select-item--disabled')
    expect(itemA.classes()).toContain('extra-item')

    await itemA.trigger('click')
    await nextTick()

    expect(select.emitted('update:modelValue')).toBeUndefined()
    expect(wrapper.get('.select-value').text()).toContain('Choose one')
  })

  it('works with v-model in a parent component', async () => {
    const Host = defineComponent({
      components: { Select, SelectTrigger, SelectValue, SelectContent, SelectItem },
      setup() {
        const value = ref('')
        return { value }
      },
      template: `
        <Select v-model="value">
          <SelectTrigger data-test="trigger">
            <SelectValue placeholder="Choose one" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="a" data-test="item-a">Apple</SelectItem>
            <SelectItem value="b" data-test="item-b">Banana</SelectItem>
          </SelectContent>
        </Select>
      `,
    })

    const wrapper = mount(Host, {
      attachTo: document.body,
      global: {
        stubs: { transition: true },
      },
    })

    expect((wrapper.vm as { value: string }).value).toBe('')

    await wrapper.get('[data-test="trigger"]').trigger('click')
    await nextTick()

    await wrapper.get('[data-test="item-a"]').trigger('click')
    await nextTick()

    expect((wrapper.vm as { value: string }).value).toBe('a')
    expect(wrapper.get('.select-value').text()).toContain('Apple')
  })
})

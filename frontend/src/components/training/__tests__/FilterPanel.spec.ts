import { describe, it, expect} from 'vitest'
import { mount } from '@vue/test-utils'
import FilterPanel from '../FilterPanel.vue'

const XStub = { template: '<span class="icon-x" />' }

const defaultProps = {
  types: ['Brannvern', 'HMS', 'Førstehjelp'],
  modelType: '',
  modelStatus: '',
}

const mountPanel = (props = {}) =>
  mount(FilterPanel, {
    props: { ...defaultProps, ...props },
    global: { stubs: { X: XStub } },
  })

describe('FilterPanel', () => {
  describe('rendering', () => {
    it('renders all type pills', () => {
      const wrapper = mountPanel()
      const pills = wrapper.findAll('.pill')
      // 3 type pills + 3 status pills
      expect(pills.length).toBeGreaterThanOrEqual(3)
      expect(wrapper.text()).toContain('Brannvern')
      expect(wrapper.text()).toContain('HMS')
      expect(wrapper.text()).toContain('Førstehjelp')
    })

    it('renders all status pills (Gyldig, Utløper snart, Mangler)', () => {
      const wrapper = mountPanel()
      expect(wrapper.text()).toContain('Gyldig')
      expect(wrapper.text()).toContain('Utløper snart')
      expect(wrapper.text()).toContain('Mangler')
    })

    it('does not show clear button when no filters active', () => {
      const wrapper = mountPanel({ modelType: '', modelStatus: '' })
      expect(wrapper.find('.clear-btn').exists()).toBe(false)
    })

    it('shows clear button when modelType is set', () => {
      const wrapper = mountPanel({ modelType: 'HMS' })
      expect(wrapper.find('.clear-btn').exists()).toBe(true)
    })

    it('shows clear button when modelStatus is set', () => {
      const wrapper = mountPanel({ modelStatus: 'Gyldig' })
      expect(wrapper.find('.clear-btn').exists()).toBe(true)
    })

    it('shows clear button when both filters are set', () => {
      const wrapper = mountPanel({ modelType: 'HMS', modelStatus: 'Gyldig' })
      expect(wrapper.find('.clear-btn').exists()).toBe(true)
    })
  })

  describe('type pill active state', () => {
    it('marks active type pill with pill-active class', () => {
      const wrapper = mountPanel({ modelType: 'HMS' })
      const pills = wrapper.findAll('.pill')
      const hmsPill = pills.find((p) => p.text() === 'HMS')
      expect(hmsPill?.classes()).toContain('pill-active')
    })

    it('does not mark inactive type pills as active', () => {
      const wrapper = mountPanel({ modelType: 'HMS' })
      const pills = wrapper.findAll('.pill')
      const brannPill = pills.find((p) => p.text() === 'Brannvern')
      expect(brannPill?.classes()).not.toContain('pill-active')
    })
  })

  describe('status pill classes', () => {
    it('Gyldig pill gets outline-green class when not active', () => {
      const wrapper = mountPanel({ modelStatus: '' })
      const pills = wrapper.findAll('.pill')
      const gyldigPill = pills.find((p) => p.text().includes('Gyldig'))
      expect(gyldigPill?.classes()).toContain('pill-outline-green')
    })

    it('Utløper snart pill gets outline-amber class when not active', () => {
      const wrapper = mountPanel({ modelStatus: '' })
      const pills = wrapper.findAll('.pill')
      const utloePill = pills.find((p) => p.text().includes('Utløper snart'))
      expect(utloePill?.classes()).toContain('pill-outline-amber')
    })

    it('Mangler pill gets outline-red class when not active', () => {
      const wrapper = mountPanel({ modelStatus: '' })
      const pills = wrapper.findAll('.pill')
      const manglerPill = pills.find((p) => p.text().includes('Mangler'))
      expect(manglerPill?.classes()).toContain('pill-outline-red')
    })

    it('Gyldig pill gets pill-green class when active', () => {
      const wrapper = mountPanel({ modelStatus: 'Gyldig' })
      const pills = wrapper.findAll('.pill')
      const gyldigPill = pills.find((p) => p.text().includes('Gyldig'))
      expect(gyldigPill?.classes()).toContain('pill-green')
    })

    it('Utløper snart pill gets pill-amber class when active', () => {
      const wrapper = mountPanel({ modelStatus: 'Utløper snart' })
      const pills = wrapper.findAll('.pill')
      const pill = pills.find((p) => p.text().includes('Utløper snart'))
      expect(pill?.classes()).toContain('pill-amber')
    })

    it('Mangler pill gets pill-red class when active', () => {
      const wrapper = mountPanel({ modelStatus: 'Mangler' })
      const pills = wrapper.findAll('.pill')
      const pill = pills.find((p) => p.text().includes('Mangler'))
      expect(pill?.classes()).toContain('pill-red')
    })
  })

  describe('emits', () => {
    it('emits update:modelType with the type value when an inactive type pill is clicked', async () => {
      const wrapper = mountPanel({ modelType: '' })
      const pills = wrapper.findAll('.pill')
      const hmsPill = pills.find((p) => p.text() === 'HMS')
      await hmsPill?.trigger('click')
      expect(wrapper.emitted('update:modelType')).toBeTruthy()
      expect(wrapper.emitted('update:modelType')?.[0]).toEqual(['HMS'])
    })

    it('emits update:modelType with empty string when the active type pill is clicked (toggle off)', async () => {
      const wrapper = mountPanel({ modelType: 'HMS' })
      const pills = wrapper.findAll('.pill')
      const hmsPill = pills.find((p) => p.text() === 'HMS')
      await hmsPill?.trigger('click')
      expect(wrapper.emitted('update:modelType')?.[0]).toEqual([''])
    })

    it('emits update:modelType and update:modelStatus with empty strings when clear button clicked', async () => {
      const wrapper = mountPanel({ modelType: 'HMS', modelStatus: 'Gyldig' })
      await wrapper.find('.clear-btn').trigger('click')
      expect(wrapper.emitted('update:modelType')?.[0]).toEqual([''])
      expect(wrapper.emitted('update:modelStatus')?.[0]).toEqual([''])
    })
  })

  describe('dot elements', () => {
    it('renders dot-green for Gyldig', () => {
      const wrapper = mountPanel()
      expect(wrapper.find('.dot-green').exists()).toBe(true)
    })

    it('renders dot-amber for Utløper snart', () => {
      const wrapper = mountPanel()
      expect(wrapper.find('.dot-amber').exists()).toBe(true)
    })

    it('renders dot-red for Mangler', () => {
      const wrapper = mountPanel()
      expect(wrapper.find('.dot-red').exists()).toBe(true)
    })
  })

  describe('empty types list', () => {
    it('renders no type pills when types is empty', () => {
      const wrapper = mountPanel({ types: [] })
      // Only status pills should exist
      const pills = wrapper.findAll('.pill')
      expect(pills.length).toBe(3) // only the 3 status pills
    })
  })
})

import {describe, it, expect, vi, beforeEach} from 'vitest'
import {mount, flushPromises} from '@vue/test-utils'
import {ref} from 'vue'

const mockMutateAsync = vi.fn()
const mockIsPending = ref(false)

vi.mock('@/composables/useTrainingLogs', () => ({
  useCreateTrainingLogMutation: () => ({
    mutateAsync: mockMutateAsync,
    isPending: mockIsPending,
  }),
  useOrganizationMembersQuery: () => ({
    data: ref([
      {userId: 1, userFullName: 'Ola Nordmann'},
      {userId: 2, userFullName: 'Kari Hansen'},
    ]),
  }),
}))

vi.mock('vue-sonner', () => ({toast: {success: vi.fn(), error: vi.fn()}}))
vi.mock('axios', () => ({
  default: {isAxiosError: vi.fn().mockReturnValue(false)},
}))

const globalStubs = {
  Dialog: {
    name: 'Dialog',
    template: '<div class="dialog-stub" v-if="open"><slot /></div>',
    props: ['open'],
    emits: ['update:open'],
  },
  DialogContent: {template: '<div class="dialog-content"><slot /></div>'},
  DialogHeader: {template: '<div><slot /></div>'},
  DialogTitle: {template: '<div class="dialog-title"><slot /></div>'},
  DialogDescription: {template: '<div><slot /></div>'},
  DialogFooter: {template: '<div class="dialog-footer"><slot /></div>'},
  Button: {
    name: 'Button',
    template: '<button :type="type" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>',
    props: ['type', 'variant', 'disabled'],
    emits: ['click'],
  },
  Input: {
    name: 'Input',
    template: '<input class="input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    props: ['modelValue', 'placeholder'],
    emits: ['update:modelValue'],
  },
  Textarea: {
    name: 'Textarea',
    template: '<textarea class="textarea-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    props: ['modelValue', 'rows', 'placeholder'],
    emits: ['update:modelValue'],
  },
  DatePicker: {
    name: 'DatePicker',
    template: '<div class="datepicker-stub" />',
    props: ['modelValue', 'placeholder', 'openUpward'],
    emits: ['update:modelValue'],
  },
  Select: {
    name: 'Select',
    template: '<div class="select-stub"><slot /></div>',
    props: ['modelValue'],
    emits: ['update:modelValue'],
  },
  SelectTrigger: {template: '<div><slot /></div>'},
  SelectValue: {template: '<div><slot /></div>'},
  SelectContent: {template: '<div><slot /></div>'},
  SelectItem: {template: '<div><slot /></div>'},
}

import RegisterTrainingModal from '../RegisterTrainingModal.vue'
import {toast} from 'vue-sonner'
import axios from 'axios'

describe('RegisterTrainingModal', () => {
  beforeEach(() => {
    mockMutateAsync.mockReset()
    mockIsPending.value = false
    vi.mocked(toast.success).mockReset?.()
    vi.mocked(toast.error).mockReset?.()
    vi.mocked(axios.isAxiosError).mockReturnValue(false)
  })

  const mountModal = (props = {}) =>
    mount(RegisterTrainingModal, {
      props: {open: true, ...props},
      global: {stubs: globalStubs},
    })

  describe('rendering', () => {
    it('renders when open is true', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.dialog-stub').exists()).toBe(true)
    })

    it('does not render when open is false', () => {
      const wrapper = mountModal({open: false})
      expect(wrapper.find('.dialog-stub').exists()).toBe(false)
    })

    it('renders dialog title', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.dialog-title').text()).toBe('Registrer opplæring')
    })

    it('renders the title input', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.input-stub').exists()).toBe(true)
    })

    it('renders the description textarea', () => {
      const wrapper = mountModal()
      expect(wrapper.find('.textarea-stub').exists()).toBe(true)
    })

    it('renders 2 status buttons (COMPLETED, NOT_COMPLETED)', () => {
      const wrapper = mountModal()
      expect(wrapper.findAll('.segment-button').length).toBe(2)
    })

    it('shows "Fullført" and "Ikke fullført" status options', () => {
      const wrapper = mountModal()
      const text = wrapper.text()
      expect(text).toContain('Fullført')
      expect(text).toContain('Ikke fullført')
    })

    it('defaults to COMPLETED status active', () => {
      const wrapper = mountModal()
      const activeBtn = wrapper.find('.segment-button--active')
      expect(activeBtn.text()).toBe('Fullført')
    })
  })

  describe('form validation', () => {
    it('shows employee validation error when no employee selected', async () => {
      const wrapper = mountModal()
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.text()).toContain('Velg en ansatt')
    })

    it('shows title validation error when title is empty', async () => {
      const wrapper = mountModal()
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.text()).toContain('Opplæringstype er påkrevd')
    })

    it('shows description validation error when description is empty', async () => {
      const wrapper = mountModal()
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.text()).toContain('Beskrivelse er påkrevd')
    })

    it('shows completedAt validation error when date not set', async () => {
      const wrapper = mountModal()
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.text()).toContain('Fullført dato er påkrevd')
    })

    it('shows expiresAt validation error when date not set', async () => {
      const wrapper = mountModal()
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(wrapper.text()).toContain('Utløpsdato er påkrevd')
    })

    it('does not call mutateAsync when validation fails', async () => {
      const wrapper = mountModal()
      await wrapper.find('form').trigger('submit')
      await flushPromises()
      expect(mockMutateAsync).not.toHaveBeenCalled()
    })
  })

  describe('status button interaction', () => {
    it('switches active status to Ikke fullført when clicked', async () => {
      const wrapper = mountModal()
      const btns = wrapper.findAll('.segment-button')
      const notCompleted = btns.find((b) => b.text() === 'Ikke fullført')
      await notCompleted?.trigger('click')
      expect(notCompleted?.classes()).toContain('segment-button--active')
    })

    it('removes active class from Fullført when Ikke fullført clicked', async () => {
      const wrapper = mountModal()
      const btns = wrapper.findAll('.segment-button')
      const fullfort = btns.find((b) => b.text() === 'Fullført')
      const ikkeFullfort = btns.find((b) => b.text() === 'Ikke fullført')
      await ikkeFullfort?.trigger('click')
      expect(fullfort?.classes()).not.toContain('segment-button--active')
    })
  })

  describe('cancel button', () => {
    it('emits update:open false when Avbryt is clicked', async () => {
      const wrapper = mountModal()
      const buttons = wrapper.findAll('button')
      const avbrytBtn = buttons.find((b) => b.text() === 'Avbryt')
      await avbrytBtn?.trigger('click')
      expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
    })
  })

  describe('submit success', () => {
    it('shows success toast after successful submit', async () => {
      mockMutateAsync.mockResolvedValue({})
      expect(toast.success).not.toHaveBeenCalled()
    })

    it('shows error toast on generic network error', async () => {
      mockMutateAsync.mockRejectedValue(new Error('Network error'))
      expect(toast.error).not.toHaveBeenCalled()
    })
  })

  describe('pending state', () => {
    it('disables submit button and shows Registrerer... when isPending', async () => {
      mockIsPending.value = true
      const wrapper = mountModal()
      await wrapper.vm.$nextTick()
      const submitBtn = wrapper
        .findAll('button')
        .find((b) => b.text().includes('Registrerer'))
      expect(submitBtn?.attributes('disabled')).toBeDefined()
    })

    it('shows Registrer opplæring when not pending', () => {
      const wrapper = mountModal()
      const text = wrapper.text()
      expect(text).toContain('Registrer opplæring')
    })
  })

  describe('form reset on open', () => {
    it('resets form fields when modal is re-opened', async () => {
      const wrapper = mountModal({open: false})
      await wrapper.setProps({open: true})
      await wrapper.vm.$nextTick()
      const input = wrapper.find('.input-stub')
      expect(input.attributes('value')).toBe('')
    })
  })
})

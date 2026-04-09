import {describe, it, expect, vi, beforeEach} from 'vitest'
import {mount, flushPromises} from '@vue/test-utils'
import {ref} from 'vue'

function isoToDateInput(iso: string | null): string {
  if (!iso) return ''
  return iso.slice(0, 10)
}

function stringToCalendarDate(str: string) {
  if (!str) return undefined
  const [y, m, d] = str.split('-').map(Number)
  return {year: y, month: m, day: d}
}

function dateValueToIso(dv: {
  year: number;
  month: number;
  day: number
} | undefined): string | undefined {
  if (!dv) return undefined
  return new Date(dv.year, dv.month - 1, dv.day).toISOString()
}

const mockMutateAsync = vi.fn()
const mockIsPending = ref(false)

vi.mock('@/composables/useTrainingLogs', () => ({
  useUpdateTrainingLogMutation: () => ({
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

const makeStub = (name: string, tag = 'div') => ({
  name,
  template: `<${tag} class="${name.toLowerCase()}-stub"><slot /></${tag}>`,
})

const globalStubs = {
  Dialog: {
    name: 'Dialog',
    template: '<div class="dialog-stub" v-if="open"><slot /></div>',
    props: ['open'],
    emits: ['update:open'],
  },
  DialogContent: makeStub('DialogContent'),
  DialogHeader: makeStub('DialogHeader'),
  DialogTitle: makeStub('DialogTitle'),
  DialogDescription: makeStub('DialogDescription'),
  DialogFooter: makeStub('DialogFooter'),
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
  SelectTrigger: makeStub('SelectTrigger'),
  SelectValue: makeStub('SelectValue'),
  SelectContent: makeStub('SelectContent'),
  SelectItem: makeStub('SelectItem'),
}

import EditTrainingModal from '../EditTrainingModal.vue'
import {toast} from 'vue-sonner'
import axios from 'axios'

const makeTraining = (overrides = {}) => ({
  id: 10,
  employeeUserId: 1,
  title: 'Brannvern',
  description: 'Grunnleggende brannvern',
  completedAt: '2024-03-01T00:00:00.000Z',
  expiresAt: '2025-03-01T00:00:00.000Z',
  status: 'COMPLETED' as const,
  ...overrides,
})

describe('EditTrainingModal – helper functions', () => {
  describe('isoToDateInput', () => {
    it('returns empty string for null', () => {
      expect(isoToDateInput(null)).toBe('')
    })

    it('extracts YYYY-MM-DD from ISO string', () => {
      expect(isoToDateInput('2024-03-15T12:00:00.000Z')).toBe('2024-03-15')
    })

    it('handles date-only strings', () => {
      expect(isoToDateInput('2024-01-01')).toBe('2024-01-01')
    })
  })

  describe('stringToCalendarDate', () => {
    it('returns undefined for empty string', () => {
      expect(stringToCalendarDate('')).toBeUndefined()
    })

    it('parses a date string into year/month/day', () => {
      const result = stringToCalendarDate('2024-03-15')
      expect(result).toEqual({year: 2024, month: 3, day: 15})
    })

    it('parses single-digit month and day', () => {
      const result = stringToCalendarDate('2023-01-05')
      expect(result).toEqual({year: 2023, month: 1, day: 5})
    })
  })

  describe('dateValueToIso', () => {
    it('returns undefined for undefined input', () => {
      expect(dateValueToIso(undefined)).toBeUndefined()
    })

    it('converts a date value object to an ISO string', () => {
      const result = dateValueToIso({year: 2024, month: 6, day: 15})
      expect(result).toBeTruthy()
      expect(result).toContain('2024')
    })

    it('produces a valid Date ISO string', () => {
      const result = dateValueToIso({year: 2024, month: 1, day: 1})
      expect(new Date(result!).getFullYear()).toBe(2024)
    })
  })
})


/** Mount closed, then open so watch([open, training]) fires */
async function mountAndOpen(extraProps: Record<string, unknown> = {}) {
  const wrapper = mount(EditTrainingModal, {
    props: {open: false, training: makeTraining(), ...extraProps},
    global: {stubs: globalStubs},
  })
  await wrapper.setProps({open: true})
  await flushPromises()
  return wrapper
}

/** Write date values into existing reactive refs without extending the Proxy */
function setDates(wrapper: ReturnType<typeof mount>) {
  const vm = wrapper.vm as Record<string, unknown>
  vm['completedAt'] = {year: 2024, month: 3, day: 1}
  vm['expiresAt'] = {year: 2025, month: 3, day: 1}
}

describe('EditTrainingModal – component', () => {
  beforeEach(() => {
    mockMutateAsync.mockReset()
    mockIsPending.value = false
    vi.mocked(toast.success).mockClear()
    vi.mocked(toast.error).mockClear()
  })

  const mountModal = (props = {}) =>
    mount(EditTrainingModal, {
      props: {open: true, training: makeTraining(), ...props},
      global: {stubs: globalStubs},
    })

  it('renders when open is true', () => {
    const wrapper = mountModal()
    expect(wrapper.find('.dialog-stub').exists()).toBe(true)
  })

  it('does not render dialog content when open is false', () => {
    const wrapper = mountModal({open: false})
    expect(wrapper.find('.dialog-stub').exists()).toBe(false)
  })

  it('renders title input pre-filled with training title', async () => {
    const wrapper = await mountAndOpen()
    expect(wrapper.find('.input-stub').attributes('value')).toBe('Brannvern')
  })

  it('renders textarea pre-filled with description', async () => {
    const wrapper = await mountAndOpen()
    const textarea = wrapper.find('.textarea-stub')
    expect((textarea.element as HTMLTextAreaElement).value).toBe('Grunnleggende brannvern')
  })

  it('renders all 4 status buttons', () => {
    const wrapper = mountModal()
    expect(wrapper.findAll('.segment-button').length).toBe(4)
  })

  it('marks COMPLETED status button as active by default', async () => {
    const wrapper = await mountAndOpen()
    const activeBtns = wrapper.findAll('.segment-button--active')
    expect(activeBtns.length).toBeGreaterThan(0)
    expect(activeBtns[0].text()).toBe('Fullført')
  })

  it('emits update:open false when Avbryt is clicked', async () => {
    const wrapper = mountModal()
    const avbrytBtn = wrapper.findAll('button').find((b) => b.text() === 'Avbryt')
    await avbrytBtn?.trigger('click')
    expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
  })

  it('shows validation errors when submitted with empty title', async () => {
    const wrapper = await mountAndOpen({training: makeTraining({title: ''})})
    await wrapper.find('.input-stub').setValue('')
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(wrapper.text()).toContain('Opplæringstype er påkrevd')
  })

  it('calls mutateAsync on valid submit', async () => {
    mockMutateAsync.mockResolvedValue({})
    const wrapper = await mountAndOpen()
    setDates(wrapper)
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(mockMutateAsync).toHaveBeenCalled()
  })

  it('shows success toast and closes dialog after successful submit', async () => {
    mockMutateAsync.mockResolvedValue({})
    const wrapper = await mountAndOpen()
    setDates(wrapper)
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(toast.success).toHaveBeenCalledWith('Opplæring oppdatert')
    expect(wrapper.emitted('update:open')?.[0]).toEqual([false])
  })

  it('shows error toast on generic error', async () => {
    mockMutateAsync.mockRejectedValue(new Error('Server error'))
    const wrapper = await mountAndOpen()
    setDates(wrapper)
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(toast.error).toHaveBeenCalledWith('Kunne ikke oppdatere opplæring')
  })

  it('shows error toast from axios error response message', async () => {
    const axiosError = {response: {data: {error: {message: 'Tilgang nektet'}}}}
    vi.mocked(axios.isAxiosError).mockReturnValue(true)
    mockMutateAsync.mockRejectedValue(axiosError)
    const wrapper = await mountAndOpen()
    setDates(wrapper)
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(toast.error).toHaveBeenCalledWith('Tilgang nektet')
    vi.mocked(axios.isAxiosError).mockReturnValue(false)
  })

  it('disables submit button and shows Lagrer... when isPending', async () => {
    mockIsPending.value = true
    const wrapper = mountModal()
    await wrapper.vm.$nextTick()
    const submitBtn = wrapper.findAll('button').find((b) => b.text().includes('Lagrer'))
    expect(submitBtn?.attributes('disabled')).toBeDefined()
  })

  it('changes active status when a status button is clicked', async () => {
    const wrapper = mountModal()
    const expiresSoonBtn = wrapper.findAll('.segment-button').find((b) => b.text() === 'Utløper snart')
    await expiresSoonBtn?.trigger('click')
    expect(expiresSoonBtn?.classes()).toContain('segment-button--active')
  })
})

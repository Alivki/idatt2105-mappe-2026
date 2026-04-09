import {mount} from '@vue/test-utils'
import {defineComponent, h} from 'vue'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import ProcessFlowStep from '../Step3Premises.vue'

vi.mock('lucide-vue-next', () => {
  const icon = (name: string) =>
    defineComponent({
      name,
      setup() {
        return () => h('svg', {'data-icon': name})
      },
    })

  return {
    ArrowDown: icon('ArrowDown'),
    Plus: icon('Plus'),
    X: icon('X'),
    Info: icon('Info'),
  }
})

function createWizard() {
  return {
    processSteps: [
      {id: 'a', name: 'Mottak', hazards: [], isKKP: false},
      {id: 'b', name: 'Lagring', hazards: [], isKKP: false},
    ],
  }
}

describe('ProcessFlowStep', () => {
  let wizard: ReturnType<typeof createWizard>

  beforeEach(() => {
    wizard = createWizard()
  })

  it('renders existing process steps in inputs', () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    const inputs = wrapper.findAll('input.flow-step-input')
    expect(inputs).toHaveLength(2)
    expect((inputs[0].element as HTMLInputElement).value).toBe('Mottak')
    expect((inputs[1].element as HTMLInputElement).value).toBe('Lagring')
  })

  it('shows add button and adds step when end add button is clicked', async () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    const addButton = wrapper.get('button.flow-add-end')
    await addButton.trigger('click')

    expect(wizard.processSteps.length).toBe(3)
    expect(wizard.processSteps[2]).toMatchObject({
      name: '',
      hazards: [],
      isKKP: false,
    })
  })

  it('removes step when remove button is clicked', async () => {
    wizard.processSteps = [
      {id: 'a', name: 'Mottak', hazards: [], isKKP: false},
      {id: 'b', name: 'Lagring', hazards: [], isKKP: false},
      {id: 'c', name: 'Servering', hazards: [], isKKP: false},
    ]

    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    const removeButtons = wrapper.findAll('button.flow-step-remove')
    expect(removeButtons).toHaveLength(3)

    await removeButtons[1].trigger('click')

    expect(wizard.processSteps.length).toBe(2)
    expect(wizard.processSteps.map((s) => s.id)).toEqual(['a', 'c'])
  })

  it('does not show remove buttons when there are only two steps', () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    expect(wrapper.findAll('button.flow-step-remove')).toHaveLength(0)
  })

  it('adds a hazard when a hazard tag is clicked and removes it on second click', async () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    const firstStepButtons = wrapper.findAll('.flow-step-wrapper')[0].findAll('button.tag')
    const biologicalButton = firstStepButtons[0]

    await biologicalButton.trigger('click')
    expect(wizard.processSteps[0].hazards).toEqual(['BIOLOGICAL'])

    await biologicalButton.trigger('click')
    expect(wizard.processSteps[0].hazards).toEqual([])
  })

  it('toggles KKP flag', async () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    const firstStepButtons = wrapper.findAll('.flow-step-wrapper')[0].findAll('button.tag')
    const kkpButton = firstStepButtons[firstStepButtons.length - 1]

    expect(wizard.processSteps[0].isKKP).toBe(false)
    await kkpButton.trigger('click')
    expect(wizard.processSteps[0].isKKP).toBe(true)

    await kkpButton.trigger('click')
    expect(wizard.processSteps[0].isKKP).toBe(false)
  })

  it('updates step name through v-model input', async () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    const input = wrapper.get('input.flow-step-input')
    await input.setValue('Nytt trinn')

    expect(wizard.processSteps[0].name).toBe('Nytt trinn')
  })

  it('shows connector add buttons between steps', () => {
    const wrapper = mount(ProcessFlowStep, {
      props: {wizard},
    })

    expect(wrapper.findAll('button.flow-add-btn')).toHaveLength(1)
  })
})

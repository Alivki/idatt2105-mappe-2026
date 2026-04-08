import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import EmployeeAvatar from '../EmployeeAvatar.vue'

describe('EmployeeAvatar', () => {
  it('renders initials correctly', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'AB', color: '#ff0000' },
    })
    expect(wrapper.text()).toBe('AB')
  })

  it('applies background color from prop', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'AB', color: '#123456' },
    })
    expect(wrapper.attributes('style')).toContain('background: #123456')
  })

  it('defaults to sm size', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'AB', color: '#000' },
    })
    expect(wrapper.classes()).toContain('avatar-sm')
    expect(wrapper.classes()).not.toContain('avatar-lg')
  })

  it('applies lg class when size is lg', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'JD', color: '#000', size: 'lg' },
    })
    expect(wrapper.classes()).toContain('avatar-lg')
    expect(wrapper.classes()).not.toContain('avatar-sm')
  })

  it('applies sm class explicitly when size is sm', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'JD', color: '#000', size: 'sm' },
    })
    expect(wrapper.classes()).toContain('avatar-sm')
  })

  it('renders the avatar div element', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'XY', color: '#abc' },
    })
    expect(wrapper.find('.avatar').exists()).toBe(true)
  })

  it('renders single-character initials', () => {
    const wrapper = mount(EmployeeAvatar, {
      props: { initials: 'A', color: '#000' },
    })
    expect(wrapper.text()).toBe('A')
  })
})

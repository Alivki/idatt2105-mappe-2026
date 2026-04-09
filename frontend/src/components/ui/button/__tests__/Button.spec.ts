import {describe, expect, it} from 'vitest'
import {render} from '@testing-library/vue'
import Button from '../Button.vue'

describe('Button', () => {
  it('renders as a button by default', () => {
    const {getByRole} = render(Button, {
      slots: {
        default: 'Click me',
      },
    })

    const button = getByRole('button')
    expect(button).toBeTruthy()
    expect(button.textContent).toContain('Click me')
  })

  it('applies default classes by default', () => {
    const {getByRole} = render(Button, {
      slots: {
        default: 'Save',
      },
    })

    const button = getByRole('button')
    expect(button.className).toContain('btn')
    expect(button.className).toContain('btn--default')
    expect(button.className).not.toContain('btn--sm')
  })

  it('applies a custom variant class', () => {
    const {getByRole} = render(Button, {
      props: {
        variant: 'destructive',
      },
      slots: {
        default: 'Delete',
      },
    })

    const button = getByRole('button')
    expect(button.className).toContain('btn')
    expect(button.className).toContain('btn--destructive')
  })

  it('applies a custom size class when size is not default', () => {
    const {getByRole} = render(Button, {
      props: {
        size: 'sm',
      },
      slots: {
        default: 'Small',
      },
    })

    const button = getByRole('button')
    expect(button.className).toContain('btn--sm')
  })

  it('does not add a size class when size is default', () => {
    const {getByRole} = render(Button, {
      props: {
        size: 'default',
      },
      slots: {
        default: 'Default',
      },
    })

    const button = getByRole('button')
    expect(button.className).toContain('btn')
    expect(button.className).toContain('btn--default')
    expect(button.className).not.toContain('btn--default ')
    expect(button.className).not.toContain('btn--sm')
    expect(button.className).not.toContain('btn--lg')
    expect(button.className).not.toContain('btn--icon')
  })

  it('applies a custom class passed through props', () => {
    const {getByRole} = render(Button, {
      props: {
        class: 'my-extra-class',
      },
      slots: {
        default: 'Styled',
      },
    })

    const button = getByRole('button')
    expect(button.className).toContain('btn')
    expect(button.className).toContain('my-extra-class')
  })

  it('sets disabled attribute when disabled is true', () => {
    const {getByRole} = render(Button, {
      props: {
        disabled: true,
      },
      slots: {
        default: 'Disabled',
      },
    })

    const button = getByRole('button') as HTMLButtonElement
    expect(button.disabled).toBe(true)
  })

  it('does not set disabled attribute when disabled is false', () => {
    const {getByRole} = render(Button, {
      props: {
        disabled: false,
      },
      slots: {
        default: 'Enabled',
      },
    })

    const button = getByRole('button')
    expect(button.hasAttribute('disabled')).toBe(false)
  })

  it('renders as another element when as prop is provided', () => {
    const {getByText} = render(Button, {
      props: {
        as: 'a',
      },
      slots: {
        default: 'Go somewhere',
      },
    })

    const linkLikeButton = getByText('Go somewhere')
    expect(linkLikeButton.tagName).toBe('A')
    expect(linkLikeButton.className).toContain('btn')
  })

  it('supports another variant and size combination', () => {
    const {getByRole} = render(Button, {
      props: {
        variant: 'outline',
        size: 'lg',
      },
      slots: {
        default: 'Large outline',
      },
    })

    const button = getByRole('button')
    expect(button.className).toContain('btn--outline')
    expect(button.className).toContain('btn--lg')
  })
})

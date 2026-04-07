import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import Table from '../Table.vue'
import TableBody from '../TableBody.vue'
import TableCell from '../TableCell.vue'
import TableFooter from '../TableFooter.vue'
import TableHead from '../TableHead.vue'
import TableHeader from '../TableHeader.vue'
import TableRow from '../TableRow.vue'

describe('Table', () => {
  it('renders wrapper div and table element', () => {
    const wrapper = mount(Table)

    expect(wrapper.classes()).toContain('table-wrapper')
    expect(wrapper.find('table').exists()).toBe(true)
    expect(wrapper.find('table').classes()).toContain('table')
  })

  it('renders slot content inside table', () => {
    const wrapper = mount(Table, {
      slots: {
        default: '<tbody data-test="body"><tr><td>Cell</td></tr></tbody>',
      },
    })

    expect(wrapper.find('[data-test="body"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Cell')
  })

  it('applies custom class to table element', () => {
    const wrapper = mount(Table, {
      props: {
        class: 'table-extra',
      },
    })

    expect(wrapper.find('table').classes()).toContain('table')
    expect(wrapper.find('table').classes()).toContain('table-extra')
  })
})

describe('TableBody', () => {
  it('renders as tbody', () => {
    const wrapper = mount(TableBody)

    expect(wrapper.element.tagName).toBe('TBODY')
  })

  it('renders slot content', () => {
    const wrapper = mount(TableBody, {
      slots: {
        default: '<tr data-test="row"><td>Body row</td></tr>',
      },
    })

    expect(wrapper.find('[data-test="row"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Body row')
  })

  it('applies custom class', () => {
    const wrapper = mount(TableBody, {
      props: {
        class: 'body-extra',
      },
    })

    expect(wrapper.classes()).toContain('body-extra')
  })
})

describe('TableCell', () => {
  it('renders as td', () => {
    const wrapper = mount(TableCell)

    expect(wrapper.element.tagName).toBe('TD')
    expect(wrapper.classes()).toContain('table-cell')
  })

  it('renders slot content', () => {
    const wrapper = mount(TableCell, {
      slots: {
        default: 'Cell text',
      },
    })

    expect(wrapper.text()).toContain('Cell text')
  })

  it('applies custom class', () => {
    const wrapper = mount(TableCell, {
      props: {
        class: 'cell-extra',
      },
    })

    expect(wrapper.classes()).toContain('table-cell')
    expect(wrapper.classes()).toContain('cell-extra')
  })

  it('forwards colspan prop', () => {
    const wrapper = mount(TableCell, {
      props: {
        colspan: 3,
      },
    })

    expect(wrapper.attributes('colspan')).toBe('3')
  })

  it('does not render colspan when not provided', () => {
    const wrapper = mount(TableCell)

    expect(wrapper.attributes('colspan')).toBeUndefined()
  })
})

describe('TableFooter', () => {
  it('renders as tfoot', () => {
    const wrapper = mount(TableFooter)

    expect(wrapper.element.tagName).toBe('TFOOT')
    expect(wrapper.classes()).toContain('table-footer')
  })

  it('renders slot content', () => {
    const wrapper = mount(TableFooter, {
      slots: {
        default: '<tr data-test="row"><td>Total</td></tr>',
      },
    })

    expect(wrapper.find('[data-test="row"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Total')
  })

  it('applies custom class', () => {
    const wrapper = mount(TableFooter, {
      props: {
        class: 'footer-extra',
      },
    })

    expect(wrapper.classes()).toContain('table-footer')
    expect(wrapper.classes()).toContain('footer-extra')
  })
})

describe('TableHead', () => {
  it('renders as th', () => {
    const wrapper = mount(TableHead)

    expect(wrapper.element.tagName).toBe('TH')
    expect(wrapper.classes()).toContain('table-head')
  })

  it('renders slot content', () => {
    const wrapper = mount(TableHead, {
      slots: {
        default: 'Header',
      },
    })

    expect(wrapper.text()).toContain('Header')
  })

  it('applies custom class', () => {
    const wrapper = mount(TableHead, {
      props: {
        class: 'head-extra',
      },
    })

    expect(wrapper.classes()).toContain('table-head')
    expect(wrapper.classes()).toContain('head-extra')
  })
})

describe('TableHeader', () => {
  it('renders as thead', () => {
    const wrapper = mount(TableHeader)

    expect(wrapper.element.tagName).toBe('THEAD')
    expect(wrapper.classes()).toContain('table-header')
  })

  it('renders slot content', () => {
    const wrapper = mount(TableHeader, {
      slots: {
        default: '<tr data-test="row"><th>Col</th></tr>',
      },
    })

    expect(wrapper.find('[data-test="row"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Col')
  })

  it('applies custom class', () => {
    const wrapper = mount(TableHeader, {
      props: {
        class: 'header-extra',
      },
    })

    expect(wrapper.classes()).toContain('table-header')
    expect(wrapper.classes()).toContain('header-extra')
  })
})

describe('TableRow', () => {
  it('renders as tr', () => {
    const wrapper = mount(TableRow)

    expect(wrapper.element.tagName).toBe('TR')
    expect(wrapper.classes()).toContain('table-row')
  })

  it('renders slot content', () => {
    const wrapper = mount(TableRow, {
      slots: {
        default: '<td data-test="cell">Row cell</td>',
      },
    })

    expect(wrapper.find('[data-test="cell"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Row cell')
  })

  it('applies custom class', () => {
    const wrapper = mount(TableRow, {
      props: {
        class: 'row-extra',
      },
    })

    expect(wrapper.classes()).toContain('table-row')
    expect(wrapper.classes()).toContain('row-extra')
  })

  it('forwards arbitrary attributes like data-state', () => {
    const wrapper = mount(TableRow, {
      attrs: {
        'data-state': 'selected',
      },
    })

    expect(wrapper.attributes('data-state')).toBe('selected')
  })
})

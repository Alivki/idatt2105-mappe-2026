import { test, expect } from '@playwright/test'

function uniqueEmail() {
  return `playwright.${Date.now()}@example.com`
}

test.describe('signup flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/signup')
    await expect(page).toHaveURL(/\/signup$/)
    await expect(page.getByText('STEG 1: REGISTRERING')).toBeVisible()
  })

  test('shows both steps in signup UI', async ({ page }) => {
    await expect(page.getByText('STEG 1: REGISTRERING')).toBeVisible()
    await expect(page.getByText('STEG 2: BEKREFTELSE')).toBeVisible()
    await expect(page.getByText(/bekreft e-post/i)).toBeVisible()
  })

  test('submits personal information and stays in confirmation flow', async ({ page }) => {
    await page.getByPlaceholder('Ola', { exact: true }).fill('Playwright')
    await page.getByPlaceholder('Nordmann', { exact: true }).fill('Tester')
    await page.getByPlaceholder('ola@eksempel.no', { exact: true }).fill(uniqueEmail())
    await page.getByPlaceholder('+47 000 00 000', { exact: true }).fill('99999999')
    await page.getByPlaceholder('Minst 8 tegn', { exact: true }).fill('password123')

    await page.getByText(/jeg godtar/i).click()

    const submitButton = page.getByRole('button', { name: /opprett konto/i })
    await expect(submitButton).toBeEnabled()
    await submitButton.click()

    await expect(page).toHaveURL(/\/signup$/)

    await expect(page.getByText('STEG 2: BEKREFTELSE')).toBeVisible()
    await expect(page.getByText(/bekreft e-post/i)).toBeVisible()
  })
})

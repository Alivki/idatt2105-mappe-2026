import { test, expect } from '@playwright/test'

const email = process.env.E2E_EMAIL ?? 'employee@everest.local'
const password = process.env.E2E_PASSWORD ?? 'password'

test.describe('login flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login')
    await expect(page).toHaveURL(/\/login$/)
    await expect(page.getByText('STEG 1: INNLOGGING')).toBeVisible()
  })

  test('shows error on invalid login', async ({ page }) => {
    await page.locator('input[type="email"]').fill('feil@test.no')
    await page.locator('input[type="password"]').fill('feilpassord')
    await page.getByRole('button', { name: /logg inn/i }).click()

    await expect(page.locator('.error-card')).toContainText('Feil e-post eller passord')
    await expect(page).toHaveURL(/\/login$/)
  })

  test('logs in and lands on select organization step', async ({ page }) => {
    await page.locator('input[type="email"]').fill(email)
    await page.locator('input[type="password"]').fill(password)
    await page.getByRole('button', { name: /logg inn/i }).click()

    await expect(page).toHaveURL(/\/select-org$/)
    await expect(page.getByText('STEG 2: VELG ORGANISASJON')).toBeVisible()
  })

  test('can continue from select organization to dashboard', async ({ page }) => {
    await page.goto('/login')

    await page.locator('input[type="email"]').fill(email)
    await page.locator('input[type="password"]').fill(password)
    await page.getByRole('button', { name: /logg inn/i }).click()

    await expect(page).toHaveURL(/\/select-org$/)

    await page.getByText(/Everest Sushi & Fusion AS|Demo Organization/i).first().click()

    const continueButton = page.getByRole('button', { name: /fortsett/i })
    await expect(continueButton).toBeEnabled()
    await continueButton.click()

    await expect(page).toHaveURL(/\/$/)
    await expect(page.locator('.page-title')).toHaveText('Oversikt')
  })
})

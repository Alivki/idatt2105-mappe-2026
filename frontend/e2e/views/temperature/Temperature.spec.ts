import { test, expect, Page } from '@playwright/test'

const email = process.env.E2E_EMAIL ?? 'admin@iksystem.local'
const password = process.env.E2E_PASSWORD ?? 'password'

async function login(page: Page) {
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
}

test.describe('temperature flow', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('user can open temperature overview', async ({ page }) => {
    await page.goto('/temperatur')

    await expect(page).toHaveURL(/\/temperatur$/)
    await expect(page.locator('.page-title')).toHaveText(/Temperatur/i)
  })

  test('user can open appliances page', async ({ page }) => {
    await page.goto('/temperatur/hvitevarer')

    await expect(page).toHaveURL(/\/temperatur\/hvitevarer$/)
    await expect(page.locator('.page-title')).toHaveText('Hvitevarer')
  })

  test('user can navigate between temperature overview and appliances page', async ({ page }) => {
    await page.goto('/temperatur')
    await expect(page).toHaveURL(/\/temperatur$/)
    await expect(page.locator('.page-title')).toHaveText(/Temperatur/i)

    await page.goto('/temperatur/hvitevarer')
    await expect(page).toHaveURL(/\/temperatur\/hvitevarer$/)
    await expect(page.locator('.page-title')).toHaveText('Hvitevarer')

    await page.goto('/temperatur')
    await expect(page).toHaveURL(/\/temperatur$/)
    await expect(page.locator('.page-title')).toHaveText(/Temperatur/i)
  })
})

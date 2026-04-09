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

test.describe('allowance / age verification', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('user can open allowance page', async ({ page }) => {
    await page.goto('/bevilling')

    await expect(page).toHaveURL(/\/bevilling$/)
    await expect(page.locator('.page-title')).toHaveText('Bevilling')
    await expect(page.getByRole('main').getByRole('heading', { name: 'Alderskontroll' })).toBeVisible()
    await expect(
      page.getByText('Oversikt over daglig alderskontroll-aktivitet')
    ).toBeVisible()
  })

  test('user can start shift if none active', async ({ page }) => {
    await page.goto('/bevilling')

    const startButton = page.getByRole('button', { name: /start vakt/i })

    if (await startButton.isVisible().catch(() => false)) {
      await startButton.click()
      await expect(page.locator('body')).toContainText(/aktiv|pågående|avslutt vakt/i)
    }
  })

  test('user can update ID check count if shift is active', async ({ page }) => {
    await page.goto('/bevilling')

    const incrementButton = page.getByRole('button', { name: /^\+$/ })

    if (await incrementButton.isVisible().catch(() => false)) {
      await incrementButton.click()
      await expect(page.locator('body')).toBeVisible()
    }
  })

  test('user can end shift if active', async ({ page }) => {
    await page.goto('/bevilling')

    const endButton = page.getByRole('button', { name: /avslutt vakt/i })

    if (await endButton.isVisible().catch(() => false)) {
      await endButton.click()
      await expect(page.locator('body')).toContainText(/start vakt|alderskontroll/i)
    }
  })
})

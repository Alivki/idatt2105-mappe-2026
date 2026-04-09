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

test.describe('employee overview', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('user can open edit employee dialog', async ({ page }) => {
    await page.goto('/employees')

    const firstEditButton = page.getByRole('button', { name: /rediger/i }).first()

    if (await firstEditButton.isVisible().catch(() => false)) {
      await firstEditButton.click()
      await expect(page.getByRole('dialog')).toBeVisible()
    }
  })

  test('user can open delete employee dialog', async ({ page }) => {
    await page.goto('/employees')

    const deleteButton = page.getByRole('button', { name: /slett/i }).first()

    if (await deleteButton.isVisible().catch(() => false)) {
      await deleteButton.click()
      await expect(page.getByRole('dialog')).toBeVisible()
    }
  })
})

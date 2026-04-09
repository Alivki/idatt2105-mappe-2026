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

test.describe('alcohol policy', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('user can open alcohol policy page and see correct state', async ({ page }) => {
    await page.goto('/skjenkepolicy')

    await expect(page).toHaveURL(/\/skjenkepolicy$/)
    await expect(page.locator('.page-title')).toHaveText('Skjenkepolicy')
  })

  test('user can fill and submit alcohol policy if edit form is visible', async ({ page }) => {
    await page.goto('/skjenkepolicy')

    await expect(page.locator('.page-title')).toHaveText('Skjenkepolicy')

    const saveButton = page.getByRole('button', { name: /lagre skjenkepolicy|oppdater/i })
    const editButton = page.getByRole('button', { name: /rediger/i })

    if (await editButton.isVisible().catch(() => false)) {
      await editButton.click()
    }

    if (await saveButton.isVisible().catch(() => false)) {
      await page.locator('textarea').first().fill('Test policy fra Playwright')
      await saveButton.click()
      await expect(page).toHaveURL(/\/skjenkepolicy$/)
    }
  })
})

import { test, expect, Page } from '@playwright/test'

const email = process.env.E2E_EMAIL ?? 'employee@iksystem.local'
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

test.describe('create business flow', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('user can open create business page and see the form', async ({ page }) => {
    await page.goto('/create-org')
    await expect(page).toHaveURL(/\/create-org$/)

    await expect(page.getByText('OPPRETT VIRKSOMHET')).toBeVisible()
    await expect(
      page.getByText('Fyll inn informasjon om din virksomhet')
    ).toBeVisible()

    await expect(
      page.getByPlaceholder('Eksempel AS', { exact: true })
    ).toBeVisible()

    await expect(
      page.getByPlaceholder('kontakt@eksempel.no', { exact: true })
    ).toBeVisible()

    await expect(
      page.getByPlaceholder('+47 000 00 000', { exact: true })
    ).toBeVisible()

    await expect(
      page.getByPlaceholder('123456789', { exact: true })
    ).toBeVisible()

    await expect(page.getByRole('combobox')).toBeVisible()
    await expect(page.getByRole('button', { name: /^Opprett$/ })).toBeVisible()
  })
})

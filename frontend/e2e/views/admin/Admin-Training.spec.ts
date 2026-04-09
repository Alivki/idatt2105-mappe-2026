import { test, expect, Page } from '@playwright/test'

const email = process.env.E2E_EMAIL ?? 'admin@everest.local'
const password = process.env.E2E_PASSWORD ?? 'password'

async function loginAsAdmin(page: Page) {
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

test.describe('admin training', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('admin can open training page and see main content', async ({ page }) => {
    await page.goto('/opplaering')

    await expect(page).toHaveURL(/\/opplaering$/)
    await expect(
      page.getByRole('heading', { name: 'Opplæring og sertifiseringer' })
    ).toBeVisible()

    await expect(
      page.getByText('Oversikt over ansattes opplæringsstatus')
    ).toBeVisible()

    await expect(
      page.getByRole('button', { name: /registrer opplæring/i })
    ).toBeVisible()

    await expect(
      page.getByRole('textbox', { name: /søk etter ansatt, type eller status/i })
    ).toBeVisible()
  })

  test('admin can search and see empty state when nothing matches', async ({ page }) => {
    await page.goto('/opplaering')

    const searchInput = page.getByRole('textbox', {
      name: /søk etter ansatt, type eller status/i,
    })

    await searchInput.fill('zzznomatchplaywright123')

    await expect(
      page.getByText('Ingen opplæringer matcher søket.')
    ).toBeVisible()
  })

  test('admin can open register training modal', async ({ page }) => {
    await page.goto('/opplaering')

    await page.getByRole('button', { name: /registrer opplæring/i }).click()

    await expect(
      page.getByRole('dialog')
    ).toBeVisible()

    await expect(
      page.getByText(/registrer opplæring|ny opplæring/i).first()
    ).toBeVisible()
  })
})

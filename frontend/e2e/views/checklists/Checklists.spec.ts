// e2e/views/checklists/checklists.spec.ts
import { test, expect, Page } from '@playwright/test'

const email = process.env.E2E_EMAIL ?? 'manager@everest.local'
const password = process.env.E2E_PASSWORD ?? 'password'

function uniqueChecklistName() {
  return `Playwright checklist ${Date.now()}`
}

async function loginAsManager(page: Page) {
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

test.describe('checklists', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsManager(page)
  })

  test('manager can open checklist page', async ({ page }) => {
    await page.goto('/sjekklister')

    await expect(page).toHaveURL(/\/sjekklister$/)
    await expect(page.getByRole('heading', { name: 'Sjekklister' })).toBeVisible()
    await expect(
      page.getByText('Dynamiske rutiner for daglig drift og internkontroll.')
    ).toBeVisible()
    await expect(
      page.getByRole('button', { name: /\+ Ny sjekkliste/i })
    ).toBeVisible()
  })

  test('manager can create a checklist', async ({ page }) => {
    const checklistName = uniqueChecklistName()

    await page.goto('/sjekklister')
    await page.getByRole('button', { name: /\+ Ny sjekkliste/i }).click()

    await expect(
      page.getByRole('heading', { name: 'Opprett ny sjekkliste' })
    ).toBeVisible()

    await page
      .getByPlaceholder('For eksempel: Morgenrutine kjøkken', { exact: true })
      .fill(checklistName)

    await page.getByRole('button', { name: /opprett sjekkliste/i }).click()

    await expect(page.getByText(checklistName)).toBeVisible()
  })

  test('manager can open checklist detail page', async ({ page }) => {
    const checklistName = uniqueChecklistName()

    await page.goto('/sjekklister')
    await page.getByRole('button', { name: /\+ Ny sjekkliste/i }).click()

    await page
      .getByPlaceholder('For eksempel: Morgenrutine kjøkken', { exact: true })
      .fill(checklistName)

    await page.getByRole('button', { name: /opprett sjekkliste/i }).click()

    await expect(page.getByText(checklistName)).toBeVisible()

    await page.getByText(checklistName).first().click()

    await expect(page).toHaveURL(/\/sjekklister\/\d+$/)
    await expect(
      page.getByRole('button', { name: /tilbake til sjekklister/i })
    ).toBeVisible()
    await expect(page.getByRole('heading', { name: checklistName })).toBeVisible()
  })
})

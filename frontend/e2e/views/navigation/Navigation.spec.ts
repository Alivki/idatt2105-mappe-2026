import { test, expect } from '@playwright/test'

const email = process.env.E2E_EMAIL ?? 'employee@everest.local'
const password = process.env.E2E_PASSWORD ?? 'password'

async function login(page) {
  await page.goto('/login')

  await page.locator('input[type="email"]').fill(email)
  await page.locator('input[type="password"]').fill(password)
  await page.getByRole('button', { name: /logg inn/i }).click()

  await expect(page).toHaveURL(/\/select-org$/)

  // velg org
  await page.getByText(/Everest|Demo/i).first().click()

  const continueButton = page.getByRole('button', { name: /fortsett/i })
  await expect(continueButton).toBeEnabled()
  await continueButton.click()

  await expect(page).toHaveURL(/\/$/)
}

test.describe('navigation flow', () => {
  test.beforeEach(async ({ page }) => {
    await login(page)
  })

  test('can navigate to employees', async ({ page }) => {
    await page.getByRole('link', { name: /ansatte/i }).click()
    await expect(page).toHaveURL(/\/ansatte/)
  })

  test('can navigate to checklists', async ({ page }) => {
    await page.getByRole('link', { name: /sjekklister/i }).click()
    await expect(page).toHaveURL(/\/sjekklister/)
  })

  test('can navigate to deviations', async ({ page }) => {
    await page.getByRole('link', { name: /avvik/i }).click()
    await expect(page).toHaveURL(/\/avvik/)
  })

  test('can navigate to temperature', async ({ page }) => {
    await page.getByRole('link', { name: /temperatur/i }).click()
    await expect(page).toHaveURL(/\/temperatur/)
  })

  test('can navigate to training', async ({ page }) => {
    await page.getByRole('link', { name: /opplæring|opplaering/i }).click()
    await expect(page).toHaveURL(/\/opplaering/)
  })

  test('can navigate to allowance', async ({ page }) => {
    await page.getByRole('link', { name: /bevilling/i }).click()
    await expect(page).toHaveURL(/\/bevilling/)
  })

  test('can navigate to alcohol policy', async ({ page }) => {
    await page.getByRole('link', { name: /skjenkepolicy/i }).click()
    await expect(page).toHaveURL(/\/skjenkepolicy/)
  })

  test('can navigate to settings', async ({ page }) => {
    await page.getByRole('link', { name: /innstillinger/i }).click()
    await expect(page).toHaveURL(/\/innstillinger/)
  })
})

import { test, expect, Page } from '@playwright/test'

const adminEmail = process.env.E2E_ADMIN_EMAIL ?? 'admin@iksystem.local'
const adminPassword = process.env.E2E_ADMIN_PASSWORD ?? 'password'

async function loginAsAdmin(page: Page) {
  await page.goto('/login')

  await page.locator('input[type="email"]').fill(adminEmail)
  await page.locator('input[type="password"]').fill(adminPassword)
  await page.getByRole('button', { name: /logg inn/i }).click()

  await expect(page).toHaveURL(/\/select-org$/)

  await page.getByText(/Everest Sushi & Fusion AS|Demo Organization/i).first().click()

  const continueButton = page.getByRole('button', { name: /fortsett/i })
  await expect(continueButton).toBeEnabled()
  await continueButton.click()

  await expect(page).toHaveURL(/\/$/)
}

test.describe('haccp setup', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('admin can open haccp setup page', async ({ page }) => {
    await page.goto('/haccp-oppsett')

    await expect(page).toHaveURL(/\/haccp-oppsett$/)
    await expect(page.locator('.page-title')).toHaveText('HACCP-oppsett')
  })

  test('admin sees either intro state or completed state', async ({ page }) => {
    await page.goto('/haccp-oppsett')

    await expect(page).toHaveURL(/\/haccp-oppsett$/)
    await expect(page.locator('.page-title')).toHaveText('HACCP-oppsett')

    const introHeading = page.getByRole('heading', {
      name: /sett opp haccp-sjekklister/i,
    })
    const completedHeading = page.getByRole('heading', {
      name: /haccp-oppsett er fullført/i,
    })

    await expect(introHeading.or(completedHeading)).toBeVisible()
  })

  test('admin sees a valid haccp primary action', async ({ page }) => {
    await page.goto('/haccp-oppsett')

    const startButton = page.getByRole('button', { name: /start haccp-oppsett/i })
    const rerunButton = page.getByRole('button', { name: /kjør veiviseren på nytt/i })

    await expect(startButton.or(rerunButton)).toBeVisible()
  })
})

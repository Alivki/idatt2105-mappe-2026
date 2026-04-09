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

test.describe('deviations as admin', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page)
  })

  test('admin can open deviation overview and see key UI', async ({ page }) => {
    await page.goto('/avvik')

    await expect(page).toHaveURL(/\/avvik$/)
    await expect(page.getByRole('heading', { name: 'Avviksoversikt' })).toBeVisible()
    await expect(
      page.getByText('Alle registrerte avvik for IK-Mat og IK-Alkohol')
    ).toBeVisible()

    await expect(
      page.getByRole('button', { name: /registrer avvik/i })
    ).toBeVisible()

    await expect(
      page.getByRole('group', { name: 'Statusfilter' })
    ).toBeVisible()

    await expect(
      page.getByRole('group', { name: 'Modulfilter' })
    ).toBeVisible()

    await expect(page.getByLabel('Sortering')).toBeVisible()
  })

  test('admin can use filters and see filtered result or empty state', async ({ page }) => {
    await page.goto('/avvik')

    const filters = page.getByLabel('Filtre')

    await filters.getByRole('button', { name: 'Lukket', exact: true }).click()
    await filters.getByRole('button', { name: 'IK-Alkohol', exact: true }).click()

    const listCards = page.locator('.deviation-card')
    const emptyHeading = page.getByRole('heading', { name: 'Ingen avvik i valgt liste' })

    await expect(listCards.or(emptyHeading)).toBeVisible()

    if (await emptyHeading.isVisible()) {
      await expect(
        page.getByText('Det finnes ingen registrerte avvik med valgte filtre.')
      ).toBeVisible()
    } else {
      await expect(listCards.first()).toBeVisible()
    }
  })

  test('admin can open create dialog and switch module tabs', async ({ page }) => {
    await page.goto('/avvik')

    await page.getByRole('button', { name: /registrer avvik/i }).click()

    const dialog = page.getByRole('dialog')

    await expect(
      dialog.getByRole('heading', { name: 'Registrer avvik' })
    ).toBeVisible()

    await expect(
      dialog.getByRole('button', { name: 'IK-Mat', exact: true })
    ).toBeVisible()

    await expect(
      dialog.getByRole('button', { name: 'IK-Alkohol', exact: true })
    ).toBeVisible()

    await expect(
      dialog.getByText(/Avvik skal registreres umiddelbart/)
    ).toBeVisible()

    await dialog.getByRole('button', { name: 'IK-Alkohol', exact: true }).click()

    await expect(
      dialog.getByRole('button', { name: /skjenkekontroll kommunal/i })
    ).toBeVisible()

    await dialog.getByRole('button', { name: 'IK-Mat', exact: true }).click()

    await expect(
      dialog.getByText(/Avvik skal registreres umiddelbart/)
    ).toBeVisible()
  })

  test('admin sees deviation content on overview', async ({ page }) => {
    await page.goto('/avvik')

    const listCards = page.locator('.deviation-card')
    const emptyText = page.getByText('Ingen avvik i valgt liste')

    await page.waitForLoadState('networkidle')

    const cardCount = await listCards.count()

    if (cardCount > 0) {
      await expect(listCards.first()).toBeVisible()
    } else {
      await expect(emptyText).toBeVisible()
    }
  })

})

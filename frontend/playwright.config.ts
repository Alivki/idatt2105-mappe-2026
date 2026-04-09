import { defineConfig } from '@playwright/test'

export default defineConfig({
  testDir: './e2e/views',
  fullyParallel: true,
  use: {
    baseURL: 'http://localhost',
    trace: 'on-first-retry',
  }
})

import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const IK_MAT_TARGET = 'http://localhost:8081'
const IK_ALKOHOL_TARGET = 'http://localhost:8082'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  test: {
    globals: true,
    environment: 'happy-dom',
    exclude: ['e2e/**', 'node_modules/**'],
  },
  server: {
    proxy: {
      '/api/v1/documents': { target: IK_ALKOHOL_TARGET, changeOrigin: true },
      '/api/v1/deviations/alcohol': { target: IK_ALKOHOL_TARGET, changeOrigin: true },
      '/api/v1/penalty-points': { target: IK_ALKOHOL_TARGET, changeOrigin: true },
      '/api/v1/alcohol-policy': { target: IK_ALKOHOL_TARGET, changeOrigin: true },
      '/api/v1/age-verification': { target: IK_ALKOHOL_TARGET, changeOrigin: true },
      '/api/v1/dashboard/alcohol': { target: IK_ALKOHOL_TARGET, changeOrigin: true },

      '/api/v1/deviations/food': { target: IK_MAT_TARGET, changeOrigin: true },
      '/api/v1': { target: IK_MAT_TARGET, changeOrigin: true },

      '/api/mat': { target: IK_MAT_TARGET, changeOrigin: true },
      '/api/alkohol': { target: IK_ALKOHOL_TARGET, changeOrigin: true },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})

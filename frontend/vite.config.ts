import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig(({ command }) => ({
  plugins: [react()],
  // Em build, a UI e servida pelo backend sob /admin/.
  // Em dev, servimos na raiz do Vite para navegar em /admin/* sem dor.
  base: command === 'build' ? '/admin/' : '/',
  build: {
    outDir: '../src/main/resources/static/admin',
    emptyOutDir: true,
  },
  server: {
    proxy: {
      // Mantem o prefixo /api pois os endpoints do backend ja sao /api/*.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
}))

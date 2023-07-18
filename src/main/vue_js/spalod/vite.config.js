import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const fs = require('fs');
const path = require('path');


// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 80,
    https: {
      key: fs.readFileSync(path.resolve(__dirname, './new-server.key')),
      cert: fs.readFileSync(path.resolve(__dirname, './server.crt')),
    },
    proxy: {
      '/api': {
        target: 'https://localhost:8081',
        changeOrigin: true,
        secure:false,
      },
    },
  },
})

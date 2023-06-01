import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  base: './',
  plugins: [
      vue(),
      AutoImport({
          resolvers: [ElementPlusResolver()],
      }),
      Components({
          resolvers: [ElementPlusResolver()],
      })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server:  {
      cors: true,
      open: true,
      port: 5173,
      proxy: {
          '^/echarts': {
              target: 'https://echarts.apache.org/',
              changeOrigin: true,
              rewrite: (path) => path.replace(/^\/echarts/, '')
          },
          '^/api': {
              target: 'http://localhost:8089/',
              changeOrigin: true,
              // rewrite: (path) => path.replace(/^\/api/, '')
          }
      }
  }
})

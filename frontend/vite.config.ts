import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  // 用户端部署在根路径（独立域名）
  base: '/',
  server: {
    port: 3002, // 采购者端开发端口
    proxy: {
      // 开发环境代理到本地后端
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist-prod',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild'
  }
})


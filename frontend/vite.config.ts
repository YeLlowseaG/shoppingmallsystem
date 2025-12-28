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
  // 根据环境变量设置base路径
  base: process.env.VITE_BUILD_ENV === 'test' ? '/test/' : '/',
  server: {
    port: 3002, // 采购者端开发端口
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/test/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/test/, '')
      },
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: process.env.VITE_BUILD_ENV === 'test' ? 'dist-test' : 'dist-prod',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild'
  }
})


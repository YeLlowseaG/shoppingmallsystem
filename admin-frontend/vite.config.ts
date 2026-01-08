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
  // 管理后台部署在独立域名，base路径为根路径
  base: '/',
  server: {
    port: 3003, // 管理后台开发端口
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
    minify: 'esbuild',
    // 确保所有依赖都被正确打包
    commonjsOptions: {
      include: [/node_modules/],
      transformMixedEsModules: true
    }
  }
})



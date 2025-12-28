import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ command }) => {
  // 判断是否为构建命令
  const isBuild = command === 'build'
  
  // 根据命令和环境变量设置base路径
  // 开发环境（command === 'serve'）：base = '/'
  // 构建环境（command === 'build'）：根据 VITE_BUILD_ENV 设置 base
  const base = isBuild
    ? (process.env.VITE_BUILD_ENV === 'test' ? '/test/admin/' : '/admin/')
    : '/'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    base,
    server: {
      port: 3003, // 管理后台开发端口
      proxy: {
        '/api': {
          target: 'http://localhost:8081',
          changeOrigin: true
          // 不需要 rewrite，保持 /api 前缀
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
  }
})



import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [
    vue(),
    // SPA history fallback：F5 刷新时回退到 index.html，由前端路由接管
    {
      name: 'spa-fallback',
      configureServer(server) {
        server.middlewares.use((req, res, next) => {
          const url = (req.url ?? '').split('?')[0]
          if (
            req.method === 'GET' &&
            !url.startsWith('/api') &&
            !url.startsWith('/@') &&
            !url.startsWith('/node_modules') &&
            // 仅当路径没有文件扩展名（即真正的 SPA 路由）时才回退到 index.html，
            // 避免把 /src/main.ts、*.vue 等模块请求误写成 index.html
            !/\.[a-zA-Z0-9]+$/.test(url)
          ) {
            req.url = '/index.html'
          }
          next()
        })
      },
    },
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5175,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // 关闭自动清空输出目录：本机回收站 trash 操作异常，emptyOutDir 会触发 safe-delete 失败
    emptyOutDir: false,
  },
})

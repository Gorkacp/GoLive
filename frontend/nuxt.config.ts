export default defineNuxtConfig({
  modules: ['@pinia/nuxt'],
  css: [
    'bootstrap/dist/css/bootstrap.min.css',
    'flag-icons/css/flag-icons.min.css',
    'bootstrap-icons/font/bootstrap-icons.css'
  ],
  plugins: [
    { src: '~/plugins/bootstrap.client.js', mode: 'client' }
  ],
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8085',
      paypalClientId: process.env.NUXT_PUBLIC_PAYPAL_CLIENT_ID,
      siteUrl: process.env.NUXT_PUBLIC_SITE_URL,
      appName: process.env.NUXT_PUBLIC_APP_NAME
    }
  },
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true
      }
    }
  }
})

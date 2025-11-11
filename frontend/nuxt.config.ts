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

  app: {
    head: {
      link: [
        { rel: "icon", href: "/favicon_io/favicon.ico" },
        { rel: "icon", type: "image/png", sizes: "16x16", href: "/favicon_io/favicon-16x16.png" },
        { rel: "icon", type: "image/png", sizes: "32x32", href: "/favicon_io/favicon-32x32.png" },

        { rel: "apple-touch-icon", href: "/favicon_io/apple-touch-icon.png" },

        { rel: "icon", type: "image/png", sizes: "192x192", href: "/favicon_io/android-chrome-192x192.png" },
        { rel: "icon", type: "image/png", sizes: "512x512", href: "/favicon_io/android-chrome-512x512.png" },

        { rel: "manifest", href: "/favicon_io/site.webmanifest" }
      ]
    }
  },

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

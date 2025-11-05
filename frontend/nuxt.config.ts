// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  modules: [
    '@pinia/nuxt',
  ],
  css: [
    'bootstrap/dist/css/bootstrap.min.css',
    'flag-icons/css/flag-icons.min.css',  // Flag Icons
    'bootstrap-icons/font/bootstrap-icons.css'       // Bootstrap Icons
  ],
  plugins: [
    { src: '~/plugins/bootstrap.client.js', mode: 'client' }, // Bootstrap JS
    { src: '~/plugins/firebase.js', mode: 'client' }          // Firebase
  ],
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  runtimeConfig: {
    public: {
      firebaseApiKey: process.env.NUXT_PUBLIC_FIREBASE_API_KEY,
      firebaseAuthDomain: process.env.NUXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
      firebaseProjectId: process.env.NUXT_PUBLIC_FIREBASE_PROJECT_ID,
      firebaseStorageBucket: process.env.NUXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
      firebaseMessagingSenderId: process.env.NUXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
      firebaseAppId: process.env.NUXT_PUBLIC_FIREBASE_APP_ID,
      firebaseMeasurementId: process.env.NUXT_PUBLIC_FIREBASE_MEASUREMENT_ID,
      paypalClientId: process.env.NUXT_PUBLIC_PAYPAL_CLIENT_ID
    }
  },
  // ✅ CONFIGURACIÓN DEL PROXY AÑADIDA (sin tocar nada más)
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8085',
        changeOrigin: true,
      }
    }
  }
})
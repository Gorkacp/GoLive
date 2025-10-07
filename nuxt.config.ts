// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  css: [
    'bootstrap/dist/css/bootstrap.min.css',          // Bootstrap CSS
    'bootstrap-icons/font/bootstrap-icons.css'       // Bootstrap Icons
  ],
  plugins: [
    { src: '~/plugins/bootstrap.client.js', mode: 'client' }, // Bootstrap JS
    { src: '~/plugins/firebase.js', mode: 'client' }
  ],
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true }
})

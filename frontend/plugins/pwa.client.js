export default defineNuxtPlugin(() => {
  if (process.client && 'serviceWorker' in navigator) {
    navigator.serviceWorker.register('/service-worker.js').catch(() => {})
  }
})

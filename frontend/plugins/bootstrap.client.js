export default defineNuxtPlugin(async () => {
  if (process.client) {
    const bootstrap = await import('bootstrap/dist/js/bootstrap.bundle.min.js')
    window.bootstrap = bootstrap  
  }
})

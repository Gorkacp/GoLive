export default defineNuxtPlugin(async () => {
  if (process.client) {
    await import('bootstrap/dist/js/bootstrap.bundle.min.js')
    console.log('✅ Bootstrap JS cargado correctamente (modo cliente)')
  }
})


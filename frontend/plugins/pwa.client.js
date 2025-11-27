const SERVICE_WORKER_PATH = '/service-worker.js'

export default defineNuxtPlugin(() => {
  if (!process.client || !('serviceWorker' in navigator)) {
    return
  }

  // Evitamos interferencias en modo dev (HMR, recarga, etc.)
  if (import.meta.dev) {
    navigator.serviceWorker.getRegistration(SERVICE_WORKER_PATH).then((registration) => {
      registration?.unregister()
    })
    return
  }

  const registerSw = () =>
    navigator.serviceWorker
      .register(SERVICE_WORKER_PATH)
      .then((registration) => {
        // Forzamos que una versión nueva reemplace inmediatamente a la anterior
        if (registration.waiting) {
          registration.waiting.postMessage({ type: 'SKIP_WAITING' })
        }

        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing
          if (!newWorker) {
            return
          }
          newWorker.addEventListener('statechange', () => {
            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
              newWorker.postMessage({ type: 'SKIP_WAITING' })
            }
          })
        })
      })
      .catch(() => {})

  if (document.readyState === 'complete') {
    registerSw()
  } else {
    window.addEventListener('load', registerSw, { once: true })
  }
})

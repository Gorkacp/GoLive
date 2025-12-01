const CACHE_VERSION = 'v7'
const CACHE_NAME = `golive-cache-${CACHE_VERSION}`
const APP_SHELL = ['/', '/index.html']
const NUXT_INTERNAL_PREFIX = '/_nuxt/'

const DYNAMIC_ROUTES = [
  '/api/',
  '/events',
  '/dashboard',
  '/reset-password',
  '/login',
  '/register',
  '/perfil',
  '/oficina',
  '/carrito'
]

const isHtmlRequest = (request) => request.headers.get('accept')?.includes('text/html')

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL)).catch(() => {})
  )
  self.skipWaiting()
})

self.addEventListener('activate', (event) => {
  event.waitUntil(
    (async () => {
      await clients.claim()

      const keys = await caches.keys()
      await Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key)
          }
        })
      )

      const allClients = await clients.matchAll({ type: 'window', includeUncontrolled: true })
      allClients.forEach((client) => {
        if ('navigate' in client) {
          client.navigate(client.url)
        }
      })
    })()
  )
})

self.addEventListener('message', (event) => {
  if (event.data?.type === 'SKIP_WAITING') {
    self.skipWaiting()
  }
})

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET' && event.request.method !== 'HEAD') {
    return
  }

  const url = new URL(event.request.url)
  // No interceptamos peticiones de otros orígenes (CloudFront, APIs externas, etc.)
  // para evitar errores cuando no hay caché disponible y la red falla.
  if (url.origin !== self.location.origin) {
    return
  }

  const isDynamic = DYNAMIC_ROUTES.some((route) => url.pathname.includes(route))
  const isNuxtAsset = url.pathname.startsWith(NUXT_INTERNAL_PREFIX)

  // Evitamos cachear assets internos de Nuxt para que siempre se sirvan frescos
  if (isNuxtAsset) {
    event.respondWith(fetch(event.request).catch(() => caches.match(event.request)))
    return
  }

  if (isHtmlRequest(event.request) || isDynamic) {
    event.respondWith(
      fetch(event.request)
        .then((response) => {
          if (!response || response.status !== 200) {
            return caches.match(event.request)
          }
          const clone = response.clone()
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone))
          return response
        })
        .catch(() => caches.match(event.request) || caches.match('/index.html'))
    )
    return
  }

  event.respondWith(
    caches.match(event.request).then((response) => {
      if (response) {
        return response
      }

      return fetch(event.request)
        .then((networkResponse) => {
          if (!networkResponse || networkResponse.status !== 200) {
            return networkResponse
          }
          const clone = networkResponse.clone()
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, clone))
          return networkResponse
        })
        .catch(() => caches.match(event.request))
    })
  )
})


self.addEventListener('push', (event) => {
  if (!event.data) {
    return
  }

  let payload = {}
  try {
    payload = event.data.json()
  } catch (e) {
    try {
      payload = JSON.parse(event.data.text())
    } catch {
      payload = {}
    }
  }

  const title = payload.title || 'GoLive'
  const options = {
    body: payload.body || '',
    icon: payload.icon || '/favicon_io/android-chrome-192x192.png',
    badge: payload.badge || '/favicon_io/android-chrome-192x192.png',
    data: {
      url: payload.url || '/',
      ...payload.data
    }
  }

  event.waitUntil(self.registration.showNotification(title, options))
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()
  const url = event.notification.data?.url || '/'

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if ('focus' in client) {
          if (client.url.includes(self.location.origin)) {
            client.focus()
            client.postMessage({ type: 'OPEN_URL', url })
            return
          }
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(url)
      }
    })
  )
})

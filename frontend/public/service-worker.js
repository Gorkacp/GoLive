const CACHE_VERSION = 'v3'
const CACHE_NAME = `golive-cache-${CACHE_VERSION}`
const APP_SHELL = ['/', '/index.html']
const NUXT_INTERNAL_PREFIX = '/_nuxt/'

// Rutas que NO deben ser cacheadas (dinámicas)
const DYNAMIC_ROUTES = [
  '/api/',
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
    Promise.all([
      clients.claim(),
      caches.keys().then((keys) =>
        Promise.all(
          keys.map((key) => {
            if (key !== CACHE_NAME) {
              return caches.delete(key)
            }
          })
        )
      )
    ])
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
  const isDynamic = DYNAMIC_ROUTES.some((route) => url.pathname.includes(route))
  const isNuxtAsset = url.pathname.startsWith(NUXT_INTERNAL_PREFIX)

  // Evitamos cachear assets internos de Nuxt para que siempre se sirvan frescos
  if (isNuxtAsset) {
    event.respondWith(fetch(event.request).catch(() => caches.match(event.request)))
    return
  }

  // Estrategia network-first para HTML y rutas dinámicas
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

  // Para recursos estáticos: cache-first
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

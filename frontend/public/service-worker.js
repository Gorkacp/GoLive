const CACHE_NAME = 'golive-cache-v1'

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

self.addEventListener('install', (e) => {
  self.skipWaiting()
})

self.addEventListener('activate', (e) => {
  e.waitUntil(clients.claim())
})

self.addEventListener('fetch', (e) => {
  const url = new URL(e.request.url)
  
  // No cachear si es una ruta dinámica
  const isDynamic = DYNAMIC_ROUTES.some(route => url.pathname.includes(route))
  
  // No cachear métodos POST, PUT, DELETE, PATCH
  const isNonCacheable = !['GET', 'HEAD'].includes(e.request.method)
  
  if (isDynamic || isNonCacheable) {
    // Para rutas dinámicas: intentar network, si falla devolver la página principal para que Nuxt la maneje
    e.respondWith(
      fetch(e.request)
        .catch(() => {
          // Si falla el fetch, devolver el index.html para que Nuxt maneje la navegación
          return caches.match('/index.html') || caches.match('/')
        })
    )
    return
  }

  // Para otras solicitudes GET (recursos estáticos): usar estrategia cache-first
  e.respondWith(
    caches.match(e.request)
      .then((response) => {
        if (response) {
          return response
        }
        
        return fetch(e.request).then((response) => {
          // No cachear respuestas de error
          if (!response || response.status !== 200) {
            return response
          }
          
          // Guardar en caché
          const responseToCache = response.clone()
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(e.request, responseToCache)
          })
          
          return response
        })
      })
      .catch(() => {
        // Si falla todo, intentar servir desde caché
        return caches.match(e.request)
      })
  )
})

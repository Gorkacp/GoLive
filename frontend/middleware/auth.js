export default defineNuxtRouteMiddleware(async (to, from) => {
  const { getToken } = useAuth()
  const token = getToken()

  if (!token) {
    return navigateTo('/login')
  }

  // Si estamos en servidor (SSR), verificar que el token sea válido
  if (process.server) {
    const config = useRuntimeConfig()
    const apiBase = config.public.apiBase

    try {
      await $fetch(`${apiBase}/api/auth/me`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
    } catch (error) {
      console.error('Error al validar token en servidor:', error.statusCode)
      if (error.statusCode === 401) {
        return navigateTo('/login')
      }
    }
  }
})

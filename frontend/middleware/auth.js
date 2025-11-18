export default defineNuxtRouteMiddleware((to, from) => {
  const { isAuthenticated } = useAuth()
  
  // Si no hay token, redirigir a login
  if (!isAuthenticated.value) {
    return navigateTo('/login')
  }
})

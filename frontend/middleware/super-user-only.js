export default defineNuxtRouteMiddleware((to) => {
  if (process.client) {
    const userStr = sessionStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        const role = (user.role || '').toLowerCase()
        if (!['super_user', 'admin'].includes(role)) {
          return navigateTo('/')
        }
      } catch (error) {
        console.error('Error parsing user data:', error)
        return navigateTo('/login')
      }
    } else {
      return navigateTo('/login')
    }
  }
})
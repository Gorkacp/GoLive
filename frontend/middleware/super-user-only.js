export default defineNuxtRouteMiddleware((to) => {
  if (process.client) {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user.role !== 'super_user') {
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
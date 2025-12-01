export default defineNuxtRouteMiddleware((to) => {
  if (process.client) {
    const userStr = sessionStorage.getItem('user')

    // Si tenemos datos de usuario en sessionStorage, usamos esa lógica
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        const role = (user.role || '').toLowerCase()
        if (!['super_user', 'admin'].includes(role)) {
          return navigateTo('/')
        }
        // Usuario autorizado, permitimos continuar
        return
      } catch (error) {
        console.error('Error parsing user data:', error)
        // Si falla el parseo, seguimos abajo y comprobamos por token
      }
    }

    // Fallback: si no hay usuario en sessionStorage pero sí token, dejamos pasar
    const authCookie = useCookie('auth_token')
    const token = authCookie.value

    if (!token) {
      // Sin token ni usuario en sessionStorage: redirigimos al login
      return navigateTo('/login')
    }

    // Hay token: permitimos que la página cargue y se encargue
    // de resolver el usuario vía /api/auth/me.
  }
})
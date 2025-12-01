// plugins/axios.js
import axios from 'axios'

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()
  const normalizeBase = (base) => {
    const trimmed = (base || '').replace(/\/+$/, '')
    return trimmed.endsWith('/api') ? trimmed : `${trimmed}/api`
  }
  const baseURL = normalizeBase(config.public.apiBase || 'http://localhost:8085/api')
  
  const api = axios.create({
    baseURL: baseURL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  api.interceptors.request.use(
    (config) => {
      if (process.client) {
        // Obtener token de cookie
        const authCookie = useCookie('auth_token')
        const token = authCookie.value
        
        if (token) {
          // Si el token está expirado, cerramos sesión y evitamos la petición
          if (isTokenExpired(token)) {
            logoutUser()
            return Promise.reject(new Error('Token expirado'))
          }

          config.headers.Authorization = `Bearer ${token}`
        }
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  api.interceptors.response.use(
    (response) => {
      return response
    },
    (error) => {
      if (process.client) {
        const status = error.response?.status
        const url = error.config?.url

        if (status === 401) {
          if (!url?.includes('/auth/login') && !url?.includes('/auth/register')) {
            logoutUser()
            navigateTo('/login?message=session_expired')
          }
        } else if (status === 403) {
        }
      }
      
      return Promise.reject(error)
    }
  )

  const isTokenExpired = (token) => {
    try {
      // Compatibilidad: los tokens antiguos dummy nunca se consideran expirados
      if (token.startsWith('dummy-token-')) {
        return false
      }

      // Para JWT reales: decodificar payload y comprobar 'exp'
      const parts = token.split('.')
      if (parts.length !== 3) {
        return false
      }

      const payload = JSON.parse(atob(parts[1]))
      if (!payload.exp) {
        return false
      }

      const nowInSeconds = Math.floor(Date.now() / 1000)
      return payload.exp < nowInSeconds
    } catch (error) {
      // Si algo falla al decodificar, por seguridad consideramos expirado
      return true
    }
  }

  // Función para cerrar sesión
  const logoutUser = () => {
    if (process.client) {
      const authCookie = useCookie('auth_token')
      authCookie.value = null
      sessionStorage.removeItem('user')
      window.dispatchEvent(new Event('auth-change'))
    }
  }

  return {
    provide: {
      api,
    },
  }
})

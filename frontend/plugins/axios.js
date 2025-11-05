// plugins/axios.js
import axios from 'axios'

export default defineNuxtPlugin((nuxtApp) => {
  const baseURL = process.dev ? '/api' : 'http://localhost:8085'
  
  const api = axios.create({
    baseURL: baseURL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // Interceptor de requests - Añade JWT automáticamente
  api.interceptors.request.use(
    (config) => {
      if (process.client) {
        const token = localStorage.getItem('token')
        if (token) {
          // Verificar que el token no esté expirado
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

  // Interceptor de responses - Maneja errores de autenticación
  api.interceptors.response.use(
    (response) => {
      return response
    },
    (error) => {
      if (process.client) {
        const status = error.response?.status
        const url = error.config?.url

        // Manejo específico de errores de autenticación
        if (status === 401) {
          if (!url?.includes('/auth/login') && !url?.includes('/auth/register')) {
            logoutUser()
            navigateTo('/login?message=session_expired')
          }
        } else if (status === 403) {
          // Podrías redirigir a una página de "sin permisos" si quieres
        }
      }
      
      return Promise.reject(error)
    }
  )

  // Función para verificar si el token está expirado
  const isTokenExpired = (token) => {
    try {
      // Para tokens JWT reales, podrías decodificar y verificar la expiración
      if (token.startsWith('dummy-token-')) {
        // Los tokens dummy nunca expiran
        return false
      }
      
      // Para tokens JWT reales:
      // const payload = JSON.parse(atob(token.split('.')[1]))
      // return payload.exp * 1000 < Date.now()
      
      return false
    } catch (error) {
      return true
    }
  }

  // Función para cerrar sesión
  const logoutUser = () => {
    if (process.client) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      
      // Emitir evento para que otros componentes se actualicen
      window.dispatchEvent(new Event('auth-change'))
    }
  }

  return {
    provide: {
      api,
    },
  }
})
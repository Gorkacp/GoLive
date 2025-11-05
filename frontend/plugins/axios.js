// plugins/axios.js
import axios from 'axios'

export default defineNuxtPlugin((nuxtApp) => {
  const config = useRuntimeConfig()
  const baseURL = process.dev ? '/api' : config.public.apiBase || 'http://localhost:8085'
  
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
        const token = localStorage.getItem('token')
        if (token) {
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
      if (token.startsWith('dummy-token-')) {
        return false
      }
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
      window.dispatchEvent(new Event('auth-change'))
    }
  }

  return {
    provide: {
      api,
    },
  }
})

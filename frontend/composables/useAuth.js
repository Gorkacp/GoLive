/**
 * Composable para manejar autenticación y datos del usuario
 * Centraliza toda la lógica de auth y operaciones de usuario con soporte SSR
 */

export const useAuth = () => {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase
  const isProduction = process.env.NODE_ENV === 'production'

  const authToken = useCookie('auth_token', {
    maxAge: 60 * 60 * 24 * 7, // 7 días
    secure: isProduction, // true en producción (HTTPS), false en desarrollo
    httpOnly: isProduction, // true en producción (solo servidor), false en desarrollo
    sameSite: 'strict', // Más restrictivo para seguridad
    path: '/'
  })

  /**
   * Verifica si el usuario está autenticado
   */
  const isAuthenticated = computed(() => {
    return !!authToken.value
  })

  /**
   * Obtiene el token actual
   */
  const getToken = () => {
    return authToken.value
  }

  /**
   * Establece el token de autenticación
   * En producción: usa HttpOnly cookie
   * En desarrollo: usa cookie regular
   */
  const setToken = (token) => {
    // En cualquier caso, guardar en cookie
    authToken.value = token
    
    // En desarrollo, opcional: guardar también en sessionStorage como backup
    if (!isProduction && typeof window !== 'undefined') {
      sessionStorage.setItem('backup_token', token)
    }
  }

  /**
   * Limpia el token (logout)
   */
  const clearToken = () => {
    authToken.value = null
  }

  // ========== OPERACIONES DE USUARIO ==========

  /**
   * Obtiene los datos del usuario actual desde el backend
   */
  const getCurrentUser = async () => {
    try {
      const token = getToken()
      
      if (!token) {
        throw new Error('No hay token disponible')
      }

      const response = await $fetch(`${apiBase}/api/auth/me`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      return response
    } catch (error) {
      throw error
    }
  }

  /**
   * Actualiza los datos del perfil del usuario
   */
  const updateProfile = async (userId, userData) => {
    try {
      const token = getToken()
      
      if (!token) {
        throw new Error('No hay token disponible')
      }

      const response = await $fetch(`${apiBase}/api/auth/users/${userId}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: {
          name: userData.name,
          lastName: userData.lastName,
          email: userData.email,
          phoneNumber: userData.phoneNumber,
          dateOfBirth: userData.dateOfBirth,
          postalCode: userData.postalCode
        }
      })

      return response
    } catch (error) {
      console.error('Error al actualizar perfil:', error)
      throw error
    }
  }

  /**
   * Sube la foto de perfil del usuario
   */
  const uploadProfilePhoto = async (userId, file) => {
    try {
      const token = getToken()
      
      if (!token) {
        throw new Error('No hay token disponible')
      }

      // Validar tipo
      if (!file.type.startsWith('image/')) {
        throw new Error('Por favor selecciona una imagen válida')
      }

      // Validar tamaño (5MB máximo)
      if (file.size > 5 * 1024 * 1024) {
        throw new Error('La imagen no puede pesar más de 5MB')
      }

      const formData = new FormData()
      formData.append('file', file)

      const response = await $fetch(`${apiBase}/api/auth/users/${userId}/profile-photo`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formData
      })

      return response
    } catch (error) {
      console.error('Error al subir foto de perfil:', error)
      throw error
    }
  }

  /**
   * Cambia la contraseña del usuario
   */
  const changePassword = async (userId, passwordData) => {
    try {
      const token = getToken()
      
      if (!token) {
        throw new Error('No hay token disponible')
      }

      await $fetch(`${apiBase}/api/auth/users/${userId}/change-password`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: {
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword,
          confirmPassword: passwordData.confirmPassword
        }
      })

      return true
    } catch (error) {
      console.error('Error al cambiar contraseña:', error)
      throw error
    }
  }

  /**
   * Elimina la cuenta del usuario
   */
  const deleteAccount = async (userId, password) => {
    try {
      const token = getToken()
      
      if (!token) {
        throw new Error('No hay token disponible')
      }

      await $fetch(`${apiBase}/api/auth/users/${userId}/account`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: {
          password: password
        }
      })

      return true
    } catch (error) {
      console.error('Error al eliminar cuenta:', error)
      throw error
    }
  }

  return {
    // Token management
    authToken,
    isAuthenticated,
    getToken,
    setToken,
    clearToken,
    
    // User operations
    getCurrentUser,
    updateProfile,
    uploadProfilePhoto,
    changePassword,
    deleteAccount
  }
}

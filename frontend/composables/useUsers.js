export const useUsers = () => {
  const config = useRuntimeConfig()
  const { getToken } = useAuth()

  const buildAuthHeaders = () => {
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }
    return { Authorization: `Bearer ${token}` }
  }

  const getAllUsers = async () => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/auth/users`, {
        headers: buildAuthHeaders()
      })
      return data
    } catch (error) {
      console.error('Error obteniendo usuarios:', error)
      throw error
    }
  }

  const getUserStats = async () => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/auth/users/stats`, {
        headers: buildAuthHeaders()
      })
      return data
    } catch (error) {
      console.error('Error obteniendo estadísticas de usuarios:', error)
      throw error
    }
  }

  const getUserById = async (userId) => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/auth/users/${userId}`, {
        headers: buildAuthHeaders()
      })
      return data
    } catch (error) {
      console.error('Error obteniendo usuario:', error)
      throw error
    }
  }

  const updateUser = async (userId, userData) => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/auth/users/${userId}`, {
        method: 'PUT',
        headers: buildAuthHeaders(),
        body: userData
      })
      return data
    } catch (error) {
      console.error('Error actualizando usuario:', error)
      throw error
    }
  }

  const updateUserRole = async (userId, role) => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/auth/users/${userId}/role`, {
        method: 'PATCH',
        headers: buildAuthHeaders(),
        body: { role }
      })
      return data
    } catch (error) {
      console.error('Error actualizando rol:', error)
      throw error
    }
  }

  const deleteUser = async (userId) => {
    try {
      await $fetch(`${config.public.apiBase}/api/auth/users/${userId}`, {
        method: 'DELETE',
        headers: buildAuthHeaders()
      })
    } catch (error) {
      console.error('Error eliminando usuario:', error)
      throw error
    }
  }

  const createUser = async (userData) => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/auth/users`, {
        method: 'POST',
        headers: buildAuthHeaders(),
        body: userData
      })
      return data
    } catch (error) {
      console.error('Error creando usuario:', error)
      throw error
    }
  }

  return {
    getAllUsers,
    getUserStats,
    getUserById,
    updateUser,
    updateUserRole,
    deleteUser,
    createUser
  }
}



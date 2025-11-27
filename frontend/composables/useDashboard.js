export const useDashboard = () => {
  const config = useRuntimeConfig()
  const { getToken } = useAuth()

  const fetchOverview = async ({ userId, role } = {}) => {
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }

    const query = userId ? { userId } : undefined

    const data = await $fetch(`${config.public.apiBase}/dashboard/overview`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`
      },
      query
    })

    return data
  }

  return {
    fetchOverview
  }
}


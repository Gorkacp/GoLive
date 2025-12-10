export const useAnalytics = () => {
  const config = useRuntimeConfig()
  const { getToken } = useAuth()

  const buildAuthHeaders = () => {
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }
    return { Authorization: `Bearer ${token}` }
  }

  const getSalesTrend = async (startDate, endDate) => {
    try {
      // Por ahora usamos el dashboard, pero esto se puede expandir
      const data = await $fetch(`${config.public.apiBase}/dashboard/overview`, {
        headers: buildAuthHeaders()
      })
      return data
    } catch (error) {
      console.error('Error obteniendo tendencia de ventas:', error)
      throw error
    }
  }

  const getEventAnalytics = async (eventId) => {
    try {
      const data = await $fetch(`${config.public.apiBase}/api/tickets/events/${eventId}`, {
        headers: buildAuthHeaders()
      })
      return data
    } catch (error) {
      console.error('Error obteniendo analítica del evento:', error)
      throw error
    }
  }

  return {
    getSalesTrend,
    getEventAnalytics
  }
}



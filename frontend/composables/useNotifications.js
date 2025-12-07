export const useNotifications = () => {
  const config = useRuntimeConfig()
  const { getToken } = useAuth()

  const buildAuthHeaders = () => {
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }
    return { Authorization: `Bearer ${token}` }
  }

  const sendNotification = async (notificationData) => {
    try {
      const response = await $fetch(`${config.public.apiBase}/api/notifications/send`, {
        method: 'POST',
        headers: {
          ...buildAuthHeaders(),
          'Content-Type': 'application/json'
        },
        body: notificationData
      })

      return response
    } catch (error) {
      console.error('Error enviando notificación:', error)
      throw error
    }
  }

  const getNotificationStats = async () => {
    try {
      const response = await $fetch(`${config.public.apiBase}/api/notifications/stats`, {
        method: 'GET',
        headers: buildAuthHeaders()
      })

      return response
    } catch (error) {
      console.error('Error obteniendo estadísticas de notificaciones:', error)
      throw error
    }
  }

  return {
    sendNotification,
    getNotificationStats
  }
}

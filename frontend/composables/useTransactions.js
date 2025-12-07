export const useTransactions = () => {
  const config = useRuntimeConfig()
  const { getToken } = useAuth()

  const buildAuthHeaders = () => {
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }
    return { Authorization: `Bearer ${token}` }
  }

  // Obtener transacciones de un evento específico
  const getEventTransactions = async (eventId) => {
    try {
      // Nota: Necesitaríamos crear este endpoint en el backend
      // Por ahora, podemos usar los tickets como referencia
      const data = await $fetch(`${config.public.apiBase}/api/tickets/events/${eventId}`, {
        headers: buildAuthHeaders()
      })
      return data
    } catch (error) {
      console.error('Error obteniendo transacciones del evento:', error)
      throw error
    }
  }

  // Obtener todas las transacciones (requiere endpoint en backend)
  const getAllTransactions = async (filters = {}) => {
    try {
      // TODO: Crear endpoint /api/transactions en el backend
      // Por ahora retornamos un array vacío
      console.warn('Endpoint de transacciones no implementado aún en el backend')
      return []
    } catch (error) {
      console.error('Error obteniendo transacciones:', error)
      throw error
    }
  }

  return {
    getEventTransactions,
    getAllTransactions
  }
}

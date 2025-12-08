export const useEvents = () => {
  const config = useRuntimeConfig()
  const { getToken } = useAuth()

  const buildAuthHeaders = () => {
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }
    return { Authorization: `Bearer ${token}` }
  }
  
  const getEvents = async () => {
    const data = await $fetch(`${config.public.apiBase}/events`)
    return data
  }

  const getManagedEvents = async (user) => {
    const headers = buildAuthHeaders()
    if (!user?.id) {
      throw new Error('Usuario no válido')
    }
    const role = (user.role || '').toLowerCase()

    if (role === 'super_user') {
      return await $fetch(`${config.public.apiBase}/events`, { headers })
    }

    return await $fetch(`${config.public.apiBase}/events/owner/${user.id}`, {
      headers
    })
  }

  const createEvent = async (event) => {
    try {
      // Asegurar tipos correctos antes de enviar
      const eventData = {
        ...event,
        availableTickets: Number(event.availableTickets) || 0,
        zones: event.zones?.map(z => ({
          name: String(z.name),
          price: Number(z.price),
          availableTickets: Number(z.availableTickets)
        })) || []
      }
      
      const data = await $fetch(`${config.public.apiBase}/events`, {
        method: 'POST',
        headers: buildAuthHeaders(),
        body: eventData
      })
      return data
    } catch (error) {
      console.error('Error creando evento:', error)
      throw error
    }
  }

  const updateEvent = async (id, event) => {
    try {
      const eventData = {
        ...event,
        availableTickets: Number(event.availableTickets) || 0,
        zones: event.zones?.map(z => ({
          name: String(z.name),
          price: Number(z.price),
          availableTickets: Number(z.availableTickets)
        })) || []
      }
      
      const data = await $fetch(`${config.public.apiBase}/events/${id}`, {
        method: 'PUT',
        headers: buildAuthHeaders(),
        body: eventData
      })
      return data
    } catch (error) {
      console.error('Error actualizando evento:', error)
      throw error
    }
  }

  const deleteEvent = async (id) => {
    await $fetch(`${config.public.apiBase}/events/${id}`, {
      method: 'DELETE',
      headers: buildAuthHeaders()
    })
  }

  const getEventAttendees = async (eventId) => {
    if (!eventId) {
      throw new Error('Evento no válido')
    }
    return await $fetch(`${config.public.apiBase}/api/tickets/events/${eventId}`, {
      headers: buildAuthHeaders()
    })
  }

  const getUniqueVenues = async () => {
    try {
      const data = await $fetch(`${config.public.apiBase}/events/venues`)
      return data || []
    } catch (error) {
      console.error('Error obteniendo lugares:', error)
      return []
    }
  }

  const getUniqueLocations = async () => {
    try {
      const data = await $fetch(`${config.public.apiBase}/events/locations`)
      return data || []
    } catch (error) {
      console.error('Error obteniendo ubicaciones:', error)
      return []
    }
  }

  return { 
    getEvents, 
    getManagedEvents,
    createEvent, 
    updateEvent, 
    deleteEvent,
    getEventAttendees,
    getUniqueVenues,
    getUniqueLocations
  }
}
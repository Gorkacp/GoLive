export const useEvents = () => {
  const config = useRuntimeConfig()
  
  const getEvents = async () => {
    const data = await $fetch(`${config.public.apiBase}/events`)
    return data
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
      method: 'DELETE'
    })
  }

  return { 
    getEvents, 
    createEvent, 
    updateEvent, 
    deleteEvent 
  }
}
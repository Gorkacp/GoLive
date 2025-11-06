export const useEvents = () => {
  const config = useRuntimeConfig()
  
  const getEvents = async () => {
    const data = await $fetch(`${config.public.apiBase}/events`)
    return data
  }

  const createEvent = async (event) => {
    const data = await $fetch(`${config.public.apiBase}/events`, {
      method: 'POST',
      body: event
    })
    return data
  }

  const updateEvent = async (id, event) => {
    const data = await $fetch(`${config.public.apiBase}/events/${id}`, {
      method: 'PUT',
      body: event
    })
    return data
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
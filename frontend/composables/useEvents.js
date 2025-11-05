import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8085'
})

export const useEvents = () => {
  const { $api } = useNuxtApp()
  
  const getEvents = async () => {
    const { data } = await $api.get('/events')
    return data
  }

  const createEvent = async (event) => {
    const { data } = await $api.post('/events', event)
    return data
  }

  const updateEvent = async (id, event) => {
    const { data } = await $api.put(`/events/${id}`, event)
    return data
  }

  const deleteEvent = async (id) => {
    await $api.delete(`/events/${id}`)
  }

  return { getEvents, createEvent, updateEvent, deleteEvent }
}
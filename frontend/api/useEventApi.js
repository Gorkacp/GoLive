/**
 * Composable para operaciones de API de eventos
 * Reutiliza el patrón del useEvents existente
 */

export const useEventApi = () => {
  const config = useRuntimeConfig()

  /**
   * Obtiene todos los eventos desde la API
   */
  const getEvents = async () => {
    try {
      const data = await $fetch(`${config.public.apiBase}/events`)
      return {
        success: true,
        data: Array.isArray(data) ? data : []
      }
    } catch (error) {
      console.error('Error cargando eventos:', error)
      return {
        success: false,
        error: error.message || 'Error al cargar los eventos',
        data: []
      }
    }
  }

  /**
   * Busca un evento por slug
   */
  const findEventBySlug = (events, slug, generateSlugFn) => {
    if (!Array.isArray(events) || !slug) return null
    
    return events.find(event => {
      const eventSlug = event.slug || generateSlugFn(event.title)
      return eventSlug === slug
    })
  }

  return {
    getEvents,
    findEventBySlug
  }
}

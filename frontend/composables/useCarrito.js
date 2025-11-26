/**
 * Composable para la lógica del carrito
 * Centraliza toda la lógica de negocio del carrito de compras
 */

export const SERVICE_FEE_PER_TICKET = 1.5
const INSURANCE_COST_PER_TICKET = 1.5
const FEES = 0

/**
 * Hook para la lógica completa del carrito
 * @param {Object} eventData - Datos del evento
 * @returns {Object} Lógica y estado del carrito
 */
export const useCarrito = (eventData) => {
  const event = ref(eventData)
  const expandedZoneId = ref(null)

  /**
   * Calcula el subtotal (precio × cantidad)
   */
  const subtotal = computed(() => {
    if (!event.value.zones) return 0
    return event.value.zones.reduce((sum, z) => {
      const price = Number(z.price) || 0
      const quantity = Number(z.quantity) || 0
      return sum + (price * quantity)
    }, 0)
  })

  /**
   * Calcula el costo del seguro
   */
  const insuranceCost = computed(() => {
    if (!event.value.zones) return 0
    return event.value.zones.reduce((sum, z) => {
      const quantity = Number(z.quantity) || 0
      const hasInsurance = z.insurance ? 1 : 0
      return sum + (quantity * hasInsurance * INSURANCE_COST_PER_TICKET)
    }, 0)
  })

  /**
   * Total de entradas seleccionadas
   */
  const totalSelectedTickets = computed(() => {
    if (!event.value.zones) return 0
    return event.value.zones.reduce((sum, z) => sum + (Number(z.quantity) || 0), 0)
  })

  /**
   * Calcula la comisión fija por servicio
   */
  const commission = computed(() => {
    return totalSelectedTickets.value * SERVICE_FEE_PER_TICKET
  })

  /**
   * Calcula el total final
   */
  const total = computed(() => {
    return subtotal.value + commission.value + insuranceCost.value + FEES
  })

  /**
   * Verifica si hay tickets seleccionados
   */
  const hasTicketsSelected = computed(() => {
    if (!event.value.zones) return false
    return event.value.zones.some(z => (z.quantity || 0) > 0)
  })

  /**
   * Calcula el aviso de disponibilidad
   */
  const availabilityNotice = computed(() => {
    const totalTickets = event.value.zones?.reduce((sum, z) => sum + (z.availableTickets || 0), 0) || 0

    if (totalTickets === 0) {
      return {
        type: 'danger',
        icon: 'exclamation-circle',
        title: 'Evento agotado',
        message: 'Lamentablemente no hay más entradas disponibles para este evento'
      }
    }

    if (totalTickets <= 20) {
      return {
        type: 'warning',
        icon: 'exclamation-triangle',
        title: 'Pocas entradas disponibles',
        message: 'Las entradas se están acabando. Apresúrate para conseguir las tuyas'
      }
    }

    return null
  })

  /**
   * Aumenta la cantidad de tickets
   */
  const increaseQuantity = (index) => {
    const zone = event.value.zones[index]
    const max = zone.availableTickets || 0
    if (zone.quantity < max) {
      zone.quantity++
    }
  }

  /**
   * Disminuye la cantidad de tickets
   */
  const decreaseQuantity = (index) => {
    const zone = event.value.zones[index]
    if (zone.quantity > 0) {
      zone.quantity--
    }
  }

  /**
   * Valida la cantidad de tickets
   */
  const validateQuantity = (index) => {
    const zone = event.value.zones[index]
    const max = zone.availableTickets || 0
    if (zone.quantity < 0) zone.quantity = 0
    if (zone.quantity > max) zone.quantity = max
  }

  /**
   * Obtiene el estado de disponibilidad de una zona
   */
  const getZoneStatus = (zone) => {
    const available = zone.availableTickets || 0
    if (available === 0) return 'Agotadas'
    if (available <= 10) return `${available} disponibles`
    return 'Disponibles'
  }

  /**
   * Obtiene la clase CSS del badge de zona
   */
  const getZoneBadgeClass = (zone) => {
    const available = zone.availableTickets || 0
    if (available === 0) return 'unavailable'
    if (available <= 10) return 'limited'
    return 'available'
  }

  /**
   * Toggle para expandir/colapsar zona en móviles
   */
  const toggleZoneExpanded = (index) => {
    expandedZoneId.value = expandedZoneId.value === index ? null : index
  }

  return {
    event,
    expandedZoneId,
    subtotal,
    insuranceCost,
    commission,
    total,
    totalSelectedTickets,
    hasTicketsSelected,
    availabilityNotice,
    increaseQuantity,
    decreaseQuantity,
    validateQuantity,
    getZoneStatus,
    getZoneBadgeClass,
    toggleZoneExpanded
  }
}
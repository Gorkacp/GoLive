<template>
  <client-only>
    <Header />
  </client-only>

  <div class="carrito-page">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner-border" role="status"></div>
      <p class="mt-3">{{ $t('Cargando evento') }}...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <i class="bi bi-exclamation-circle"></i>
      <h3>{{ $t('Error al cargar') }}</h3>
      <p>{{ error }}</p>
      <NuxtLink to="/" class="btn-back">
        <i class="bi bi-arrow-left"></i> {{ $t('Volver') }}
      </NuxtLink>
    </div>

    <!-- Content -->
    <template v-else>
      <!-- Hero Section con evento -->
      <div class="hero-section">
        <div class="hero-overlay"></div>
        <img v-if="event.image" :src="event.image" :alt="event.title" class="hero-background" />
        
        <div class="hero-content">
          <div class="breadcrumb-nav">
            <NuxtLink to="/">{{ $t('Inicio') }}</NuxtLink>
            <i class="bi bi-chevron-right"></i>
            <span>{{ event.title }}</span>
          </div>
          
          <h1 class="hero-title">{{ event.title }}</h1>
          <div class="event-meta">
            <div class="meta-item">
              <i class="bi bi-geo-alt"></i>
              <span>{{ event.venue }}</span>
            </div>
            <div class="meta-item">
              <i class="bi bi-calendar-event"></i>
              <span>{{ formatDate(event.date) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="content-wrapper">
        <!-- Tickets Section -->
        <div class="tickets-section">
          <div class="section-header">
            <h2>{{ $t('Selecciona tus entradas') }}</h2>
            <p class="section-subtitle">{{ $t('Elige la zona y cantidad') }}</p>
          </div>

          <!-- Availability Notice -->
          <div v-if="availabilityNotice" :class="['notice', availabilityNotice.type]">
            <i :class="`bi bi-${availabilityNotice.icon}`"></i>
            <div class="notice-content">
              <strong>{{ availabilityNotice.title }}</strong>
              <p>{{ availabilityNotice.message }}</p>
            </div>
          </div>

          <!-- Zones Grid - Horizontal Layout -->
          <div class="zones-grid-horizontal">
            <div v-for="(zone, index) in event.zones" :key="index" class="zone-card-compact">
              <div class="zone-header-compact">
                <h3 class="zone-name">{{ zone.name }}</h3>
                <div class="zone-badge" :class="getZoneBadgeClass(zone)">
                  {{ getZoneStatus(zone) }}
                </div>
              </div>

              <div class="zone-details-compact">
                <div class="detail-item">
                  <span class="detail-label">{{ $t('Precio') }}</span>
                  <span class="detail-value">{{ (zone.price || 0).toFixed(2) }} €</span>
                </div>
              </div>

              <!-- Selection Controls -->
              <div v-if="(zone.availableTickets || 0) > 0" class="zone-controls-compact">
                <div class="quantity-selector-compact">
                  <button 
                    class="qty-btn-compact" 
                    @click="decreaseQuantity(index)"
                    :disabled="zone.quantity <= 0"
                  >
                    <i class="bi bi-dash"></i>
                  </button>
                  <select
                    v-model.number="zone.quantity"
                    @change="validateQuantity(index)"
                    class="quantity-select-compact"
                  >
                    <option value="0">{{ $t('Cantidad') }}</option>
                    <option v-for="n in Math.min(6, zone.availableTickets || 0)" :key="n" :value="n">
                      {{ n }} {{ n === 1 ? $t('entrada') : $t('entradas') }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- Zone Details Section - Desktop Visible, Mobile Collapsed -->
              <div class="zone-info-section" :class="{ 'collapsed': expandedZoneId !== index }">
                <div class="info-subsection">
                  <h4 class="subsection-title">{{ $t('Características') }}</h4>
                  <ul class="features-list-compact">
                    <li><i class="bi bi-check2"></i> {{ $t('Acceso al evento') }}</li>
                    <li><i class="bi bi-check2"></i> {{ $t('Entrada digital') }}</li>
                    <li><i class="bi bi-check2"></i> {{ $t('Acceso prioritario') }}</li>
                  </ul>
                </div>

                <div class="info-subsection">
                  <h4 class="subsection-title">{{ $t('Política de cambios') }}</h4>
                  <p class="subsection-text">{{ $t('Puedes cambiar tus entradas hasta 24 horas antes del evento sin coste adicional') }}</p>
                </div>

                <div class="info-subsection">
                  <h4 class="subsection-title">{{ $t('Edad mínima') }}</h4>
                  <div class="age-requirement">
                    <span class="age-badge">{{ zone.minAge || 'Sin restricción' }}</span>
                    <p v-if="zone.minAge" class="age-note">
                      <i class="bi bi-exclamation-circle"></i>
                      {{ $t('Se requiere justificante descargable') }}
                    </p>
                  </div>
                </div>

                <div class="info-subsection">
                  <h4 class="subsection-title">{{ $t('Seguro de evento') }}</h4>
                  <div class="insurance-toggle">
                    <label class="insurance-checkbox">
                      <input 
                        type="checkbox" 
                        v-model="zone.insurance"
                        class="insurance-input"
                      />
                      <span class="checkmark"></span>
                      <span class="insurance-label">{{ $t('Añadir seguro') }} <strong>+1.50 €</strong></span>
                    </label>
                    <p class="insurance-info">{{ $t('Protege tu compra ante cancelaciones') }}</p>
                  </div>
                </div>

                <button class="expand-button" @click="openZoneInfo(zone)">
                  <i class="bi bi-arrow-up-right"></i> {{ $t('Más detalles') }}
                </button>
              </div>

              <!-- Mobile Toggle Button -->
              <button 
                class="toggle-details-btn"
                @click="toggleZoneExpanded(index)"
              >
                <i :class="expandedZoneId === index ? 'bi bi-chevron-up' : 'bi bi-chevron-down'"></i>
                {{ expandedZoneId === index ? $t('Ver menos') : $t('Ver más') }}
              </button>
            </div>
          </div>

          <!-- Additional Information -->
          <div class="additional-info">
            <div class="info-box">
              <i class="bi bi-shield-check"></i>
              <div>
                <h4>{{ $t('Compra segura') }}</h4>
                <p>{{ $t('Tus datos están protegidos con encriptación SSL') }}</p>
              </div>
            </div>
            <div class="info-box">
              <i class="bi bi-ticket-perforated"></i>
              <div>
                <h4>{{ $t('Entradas digitales') }}</h4>
                <p>{{ $t('Recibe tus entradas al correo al instante') }}</p>
              </div>
            </div>
            <div class="info-box">
              <i class="bi bi-arrow-repeat"></i>
              <div>
                <h4>{{ $t('Política de cambios') }}</h4>
                <p>{{ $t('Consulta nuestras condiciones') }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Summary Section - Below Tickets -->
        <div class="summary-section-full">
          <div class="summary-card">
            <h3 class="summary-title">{{ $t('Resumen de compra') }}</h3>

            <!-- Items -->
            <div class="summary-items">
              <div v-for="(zone, index) in event.zones" v-show="zone.quantity > 0" :key="index" class="summary-item">
                <div class="item-info">
                  <span class="item-name">{{ zone.name }}</span>
                  <span class="item-qty">x{{ zone.quantity }}</span>
                </div>
                <span class="item-price">{{ (zone.quantity * (zone.price || 0)).toFixed(2) }} €</span>
              </div>
            </div>

            <!-- Empty State -->
            <div v-if="!hasTicketsSelected" class="empty-summary">
              <i class="bi bi-cart-x"></i>
              <p>{{ $t('Selecciona entradas para ver el resumen') }}</p>
            </div>

            <!-- Divider -->
            <div v-if="hasTicketsSelected" class="summary-divider"></div>

            <!-- Breakdown -->
            <div v-if="hasTicketsSelected" class="price-breakdown">
              <div class="breakdown-row">
                <span>{{ $t('Subtotal') }}</span>
                <span>{{ subtotal.toFixed(2) }} €</span>
              </div>
              <div v-if="insuranceCost > 0" class="breakdown-row">
                <span>{{ $t('Seguro de evento') }}</span>
                <span>{{ insuranceCost.toFixed(2) }} €</span>
              </div>
              <div class="breakdown-row">
                <span>
                  {{ $t('Comisión por servicio') }}
                  ({{ SERVICE_FEE_PER_TICKET.toFixed(2) }} € / {{ $t('entrada') }})
                </span>
                <span>{{ commission.toFixed(2) }} €</span>
              </div>
              <div v-if="fees > 0" class="breakdown-row">
                <span>{{ $t('Gastos de envío') }}</span>
                <span>{{ fees.toFixed(2) }} €</span>
              </div>
            </div>

            <!-- Total -->
            <div v-if="hasTicketsSelected" class="price-total">
              <span class="total-label">{{ $t('Total') }}</span>
              <span class="total-amount">{{ total.toFixed(2) }} €</span>
            </div>

            <!-- CTA Buttons -->
            <button 
              class="btn-purchase" 
              @click="goToPay" 
              :disabled="!hasTicketsSelected"
              :title="!hasTicketsSelected ? $t('Selecciona al menos una entrada') : ''"
            >
              <i class="bi bi-lock-fill"></i>
              {{ $t('Proceder al pago') }}
            </button>

            <NuxtLink to="/" class="btn-continue-shopping">
              <i class="bi bi-arrow-left"></i>
              {{ $t('Seguir comprando') }}
            </NuxtLink>

            <!-- Payment Methods Badge -->
            <div class="payment-methods">
              <span class="method-badge">
                <i class="bi bi-credit-card"></i> Tarjeta
              </span>
              <span class="method-badge">
                <i class="bi bi-phone"></i> PayPal
              </span>
            </div>
          </div>
        </div>

        <!-- Map Section -->
        <div v-if="event.location" class="map-section">
          <div class="map-card">
            <h3 class="map-title">
              <i class="bi bi-geo-alt-fill"></i>
              {{ $t('Ubicación del evento') }}
            </h3>
            <client-only>
              <EventMap :address="event.location" :title="event.title" />
            </client-only>
          </div>
        </div>
      </div>

      <!-- Zone Info Modal -->
      <transition name="modal-fade">
        <div v-if="showZoneModal" class="modal-overlay" @click="closeZoneModal">
          <div class="modal-content" @click.stop>
            <button class="modal-close" @click="closeZoneModal">
              <i class="bi bi-x-lg"></i>
            </button>
            
            <div class="modal-header">
              <h3>{{ selectedZone?.name }}</h3>
            </div>

            <div class="modal-body">
              <div class="info-section">
                <h4>{{ $t('Características') }}</h4>
                <ul class="features-list">
                  <li><i class="bi bi-check2"></i> {{ $t('Acceso al evento') }}</li>
                  <li><i class="bi bi-check2"></i> {{ $t('Entrada digital') }}</li>
                  <li><i class="bi bi-check2"></i> {{ $t('Acceso prioritario') }}</li>
                </ul>
              </div>

              <div class="info-section">
                <h4>{{ $t('Política de cambios') }}</h4>
                <p>{{ $t('Puedes cambiar tus entradas hasta 24 horas antes del evento sin coste adicional') }}</p>
              </div>

              <div class="info-section">
                <h4>{{ $t('Edad mínima') }}</h4>
                <div v-if="selectedZone?.minAge" class="age-info-detailed">
                  <p class="age-requirement-text">
                    <strong>{{ $t('Edad requerida') }}: {{ selectedZone.minAge }} {{ $t('años') }}</strong>
                  </p>
                  <p>{{ $t('Se requerirá un justificante de edad en el evento. Puedes descargarlo e imprimirlo aquí.') }}</p>
                  <button class="btn-download-doc" @click="handleDownloadAuthorizationPDF">
                    <i class="bi bi-file-pdf"></i> {{ $t('Descargar justificante de edad') }}
                  </button>
                </div>
                <p v-else>{{ $t('Sin restricción de edad') }}</p>
              </div>

              <div class="info-section">
                <h4>{{ $t('Seguro de evento') }}</h4>
                <p>{{ $t('Seguro opcional para proteger tu compra ante cancelaciones.') }}</p>
                <p class="insurance-details"><strong>{{ $t('Costo') }}: 1.50 € {{ $t('por entrada') }}</strong></p>
                <p>{{ $t('Puedes añadir o modificar el seguro en la selección de entradas.') }}</p>
              </div>
            </div>

            <div class="modal-footer">
              <button class="btn-modal-close" @click="closeZoneModal">
                {{ $t('Entendido') }}
              </button>
            </div>
          </div>
        </div>
      </transition>
    </template>
  </div>

  <Footer />
</template>

<script setup>
import { useHead } from '#app'
import { downloadAuthorizationPDF } from '~/services/authorizationPdfService'
import { useEventApi } from '~/api/useEventApi'
import { useEvents } from '~/composables/useEvents'
import { useCarrito, SERVICE_FEE_PER_TICKET } from '~/composables/useCarrito'
import { useCartStore } from '~/stores/cart'

// Composables
const { getEvents, findEventBySlug } = useEventApi()

// Router
const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

// Estados principales
const event = ref({
  title: '',
  venue: '',
  image: '',
  zones: [],
  _id: '',
  description: '',
  date: ''
})
const loading = ref(true)
const error = ref('')
const showZoneModal = ref(false)
const selectedZone = ref(null)

// Lógica del carrito usando composable
const carrito = useCarrito(event.value)
const {
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
  toggleZoneExpanded,
  expandedZoneId
} = carrito

// Funciones de utilidad
const generateSlug = (title) => {
  if (!title) return ''
  return title.toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
}

const formatDate = (date) => {
  if (!date) return ''
  try {
    const d = new Date(date)
    return new Intl.DateTimeFormat('es-ES', {
      weekday: 'long',
      day: '2-digit',
      month: 'long',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(d)
  } catch {
    return date
  }
}

const processEventZones = (zones) => {
  return (zones || []).map(zone => ({
    ...zone,
    price: Number(zone.price) || 0,
    availableTickets: Number(zone.availableTickets) || 0,
    quantity: 0,
    insurance: false,
    minAge: zone.minAge || 18
  }))
}

const buildEventSEOMeta = (eventData) => {
  const slug = generateSlug(eventData.title)
  return [
    {
      name: 'description',
      content: `Compra entradas para ${eventData.title} en ${eventData.venue}. Acceso seguro y digital. ${eventData.zones?.length || 0} zonas disponibles.`
    },
    {
      name: 'keywords',
      content: `entradas, ${eventData.title}, ${eventData.venue}, compra online`
    },
    {
      property: 'og:title',
      content: `${eventData.title} - GoLive Entradas`
    },
    {
      property: 'og:description',
      content: `Compra tus entradas para ${eventData.title}. Entradas digitales seguras.`
    },
    {
      property: 'og:image',
      content: eventData.image || '/default-event.jpg'
    },
    {
      property: 'og:type',
      content: 'event'
    },
    {
      property: 'og:url',
      content: `https://golive.com/carrito/${slug}`
    },
    {
      name: 'twitter:card',
      content: 'summary_large_image'
    },
    {
      name: 'twitter:title',
      content: `${eventData.title} - GoLive`
    },
    {
      name: 'twitter:image',
      content: eventData.image || '/default-event.jpg'
    },
    {
      name: 'event-date',
      content: eventData.date || ''
    },
    {
      name: 'event-location',
      content: eventData.venue || ''
    },
    {
      name: 'robots',
      content: 'index, follow'
    }
  ]
}

const buildCanonicalLink = (slug) => {
  return {
    rel: 'canonical',
    href: `https://golive.com/carrito/${slug}`
  }
}

// Modal
const openZoneInfo = (zone) => {
  selectedZone.value = zone
  showZoneModal.value = true
}

const closeZoneModal = () => {
  showZoneModal.value = false
  selectedZone.value = null
}

// Descargar PDF de autorización
const handleDownloadAuthorizationPDF = async () => {
  try {
    const result = await downloadAuthorizationPDF(event.value)
    if (!result.success) {
      console.error(result.error)
      alert('Error al generar el PDF de autorización')
    }
  } catch (error) {
    console.error('Error:', error)
    alert('Error al descargar el PDF')
  }
}

// Cargar evento
const loadEvent = async () => {
  loading.value = true
  error.value = ''

  try {
    const result = await getEvents()

    if (!result.success) {
      throw new Error(result.error)
    }

    const foundEvent = findEventBySlug(result.data, route.params.slug, generateSlug)

    if (foundEvent) {
      const processedZones = processEventZones(foundEvent.zones)
      event.value = { ...foundEvent, zones: processedZones }

      // Actualizar la referencia en carrito
      carrito.event.value = event.value

      // SEO Optimizado
      useHead({
        title: `${event.value.title} - Compra Entradas | GoLive`,
        meta: buildEventSEOMeta(event.value),
        link: [buildCanonicalLink(route.params.slug)]
      })
    } else {
      error.value = 'Evento no encontrado'
    }
  } catch (err) {
    console.error('Error cargando evento:', err)
    error.value = err.message || 'Error al cargar el evento'
  } finally {
    loading.value = false
  }
}

// Proceder al pago
const goToPay = () => {
  if (!hasTicketsSelected.value) {
    alert('Selecciona al menos una entrada')
    return
  }

  const selectedZones = event.value.zones.filter(z => (z.quantity || 0) > 0)
  const tickets = selectedZones.map(zone => ({
    name: zone.name,
    price: zone.price,
    quantity: zone.quantity,
    insurance: !!zone.insurance
  }))

  const eventId = event.value._id || event.value.id || event.value.eventId

  const order = {
    event: {
      _id: eventId,
      id: event.value.id,
      eventId: event.value.eventId,
      title: event.value.title,
      venue: event.value.venue,
      location: event.value.location,
      image: event.value.image,
      date: event.value.date,
      time: event.value.time
    },
    tickets,
    subtotal: subtotal.value,
    insurance: insuranceCost.value,
    commission: commission.value,
    serviceFeePerTicket: SERVICE_FEE_PER_TICKET,
    totalTickets: totalSelectedTickets.value,
    fees: 0,
    total: total.value
  }

  cartStore.setOrder(order)
  localStorage.setItem('currentOrder', JSON.stringify(order))
  router.push(`/pay/${generateSlug(event.value.title)}`)
}


// Lifecycle
onMounted(async () => {
  await loadEvent()

  // Scroll to top solo en cliente
  if (process.client) {
    window.scrollTo(0, 0)
  }
})

</script>

<style scoped>
/* ============ Global ============ */
:deep(html),
:deep(body) {
  overflow-x: hidden !important;
}

.carrito-page {
  width: 100%;
  min-height: 100vh;
  background: #0a0a0a;
  overflow-x: hidden;
}

/* ============ Loading State ============ */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #0a0a0a;
}

.loading-container .spinner-border {
  width: 50px;
  height: 50px;
  border-width: 4px;
  color: #ff0057;
  border-right-color: transparent;
}

.loading-container p {
  margin-top: 20px;
  font-family: 'Poppins', sans-serif;
  color: #ffffff;
  font-weight: 500;
}

/* ============ Error State ============ */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
  padding: 40px 20px;
  background: #0a0a0a;
}

.error-container i {
  font-size: 3.5rem;
  color: #dc2626;
  margin-bottom: 15px;
}

.error-container h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #f8f9fa;
  margin-bottom: 10px;
}

.error-container p {
  font-family: 'Poppins', sans-serif;
  color: #d1d5db;
  margin-bottom: 25px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-back:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.3);
}

/* ============ Hero Section ============ */
.hero-section {
  position: relative;
  height: 450px;
  overflow: hidden;
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(10, 10, 10, 0.5);
  z-index: 1;
  backdrop-filter: blur(2px);
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #ffffff;
  padding: 40px 20px;
  max-width: 900px;
}

.breadcrumb-nav {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 25px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
}

.breadcrumb-nav a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: color 0.3s ease;
}

.breadcrumb-nav a:hover {
  color: #ffffff;
}

.breadcrumb-nav i {
  font-size: 0.8rem;
  opacity: 0.6;
}

.breadcrumb-nav span {
  color: #ffffff;
  font-weight: 600;
}

.hero-title {
  font-family: 'Poppins', sans-serif;
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 20px;
  line-height: 1.1;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.5px;
}

.event-meta {
  display: flex;
  justify-content: center;
  gap: 40px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 500;
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.meta-item i {
  font-size: 1rem;
}

/* ============ Content Wrapper ============ */
.content-wrapper {
  display: flex;
  flex-direction: column;
  gap: 30px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* ============ Tickets Section ============ */
.tickets-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.section-header {
  margin-bottom: 35px;
}

.section-header h2 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #0a0a0a;
  margin-bottom: 8px;
}

.section-subtitle {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #6b7280;
  font-weight: 500;
}

/* ============ Notice ============ */
.notice {
  display: flex;
  gap: 15px;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  border-left: 4px solid;
  font-family: 'Poppins', sans-serif;
}

.notice i {
  font-size: 1.5rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.notice-content strong {
  display: block;
  margin-bottom: 4px;
  font-weight: 700;
  font-size: 0.95rem;
}

.notice-content p {
  margin: 0;
  font-size: 0.85rem;
  opacity: 0.85;
}

.notice.danger {
  background: #fee2e2;
  border-left-color: #dc2626;
  color: #991b1b;
}

.notice.warning {
  background: #fef3c7;
  border-left-color: #eab308;
  color: #854d0e;
}

.notice.success {
  background: #dbeafe;
  border-left-color: #3b82f6;
  color: #1e40af;
}

/* ============ Zones Grid ============ */
.zones-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

/* ============ Zones Grid - Horizontal Compact ============ */
.zones-grid-horizontal {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
  flex-wrap: wrap;
  justify-content: center;
}

.zone-card-compact {
  flex: 1;
  min-width: 200px;
  max-width: 280px;
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.zone-header-compact {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.zone-header-compact .zone-name {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.zone-details-compact {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: 'Poppins', sans-serif;
}

.detail-item .detail-label {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
}

.detail-item .detail-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: #ff0057;
}

.zone-controls-compact {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quantity-selector-compact {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 6px;
  width: 100%;
}

.qty-btn-compact {
  width: 28px;
  height: 28px;
  border: none;
  background: #ffffff;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  color: #ff0057;
  transition: all 0.2s ease;
  border: 2px solid transparent;
  flex-shrink: 0;
}

.qty-btn-compact:hover:not(:disabled) {
  background: #ff0057;
  color: #ffffff;
  border-color: #ff0057;
}

.qty-btn-compact:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  color: #d1d5db;
}

.quantity-select-compact {
  flex: 1;
  border: 2px solid #ff0057;
  background: linear-gradient(to right, #ffffff 0%, #fff5f8 100%);
  text-align: center;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 700;
  color: #ff0057;
  border-radius: 6px;
  padding: 6px 8px;
  cursor: pointer;
  outline: none;
  transition: all 0.2s ease;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ff0057' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 4px center;
  background-size: 14px;
  padding-right: 22px;
}

.quantity-select-compact::-ms-expand {
  display: none;
}

.quantity-select-compact:hover {
  border-color: #ff0057;
  background: linear-gradient(to right, #fff0f6 0%, #ffe6f0 100%), url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ff0057' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e") no-repeat right 4px center;
  background-size: auto, 14px;
}

.quantity-select-compact:focus {
  border-color: #ff0057;
  box-shadow: 0 0 0 2px rgba(255, 0, 87, 0.2);
  outline: none;
}

.quantity-select-compact option {
  text-align: center;
  padding: 6px;
  background: #ffffff;
  color: #0a0a0a;
  appearance: none;
  -webkit-appearance: none;
  background-image: none;
}

.quantity-select-compact option:hover {
  background: #ff0057 !important;
  color: #ffffff !important;
}

.quantity-select-compact option:checked {
  background: #ff0057 !important;
  color: #ffffff !important;
  background-image: none;
}

.zone-unavailable-compact {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: #f3f4f6;
  border-radius: 8px;
  color: #6b7280;
  font-size: 1.1rem;
}

.info-link-compact {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 8px;
  background: transparent;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  color: #0a0a0a;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
}

.info-link-compact:hover {
  border-color: #0a0a0a;
  background: rgba(10, 10, 10, 0.05);
}

/* ============ Zone Info Section ============ */
.zone-info-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

/* Ocultar en móviles por defecto */
@media (max-width: 768px) {
  .zone-info-section.collapsed {
    display: none;
  }
}

/* Mostrar siempre en desktop */
@media (min-width: 769px) {
  .zone-info-section.collapsed {
    display: flex;
  }

  .toggle-details-btn {
    display: none !important;
  }
}

/* ============ Toggle Details Button ============ */
.toggle-details-btn {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 10px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 6px;
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.8rem;
  margin-top: 4px;
  align-self: flex-start;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.toggle-details-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

.toggle-details-btn i {
  font-size: 0.8rem;
  transition: transform 0.3s ease;
}

@media (max-width: 768px) {
  .toggle-details-btn {
    display: flex;
  }
}

.info-subsection {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.subsection-title {
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  color: #0a0a0a;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0;
}

.subsection-text {
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

.features-list-compact {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.features-list-compact li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  color: #374151;
}

.features-list-compact i {
  color: #10b981;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.age-requirement {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.age-badge {
  display: inline-block;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  padding: 4px 8px;
  border-radius: 6px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  text-align: center;
}

.age-note {
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.7rem;
  color: #ff0057;
  margin: 0;
}

.age-note i {
  font-size: 0.75rem;
}

.insurance-toggle {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.insurance-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.insurance-input {
  display: none;
}

.checkmark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border: 2px solid #e5e7eb;
  border-radius: 4px;
  background: #ffffff;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.insurance-input:checked ~ .checkmark {
  background: #ff0057;
  border-color: #ff0057;
}

.insurance-input:checked ~ .checkmark::after {
  content: '✓';
  color: #ffffff;
  font-size: 0.75rem;
  font-weight: 700;
}

.insurance-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  color: #0a0a0a;
  font-weight: 500;
}

.insurance-label strong {
  color: #ff0057;
  font-weight: 700;
}

.insurance-info {
  font-family: 'Poppins', sans-serif;
  font-size: 0.7rem;
  color: #6b7280;
  margin: 0;
}

.expand-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 10px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.85rem;
  margin-top: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.expand-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 0, 87, 0.35);
}

.zone-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.zone-name {
  font-family: 'Poppins', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.zone-badge {
  display: inline-block;
  padding: 6px 12px;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.zone-badge.available {
  background: #d1fae5;
  color: #065f46;
}

.zone-badge.limited {
  background: #fef3c7;
  color: #92400e;
}

.zone-badge.unavailable {
  background: #fee2e2;
  color: #991b1b;
}

.zone-details {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-family: 'Poppins', sans-serif;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  font-size: 0.85rem;
  color: #6b7280;
  font-weight: 500;
}

.detail-value {
  font-size: 1rem;
  font-weight: 700;
  color: #0a0a0a;
}

.zone-description {
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  color: #6b7280;
  margin: 16px 0;
  line-height: 1.5;
}

.zone-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f3f4f6;
  border-radius: 8px;
  padding: 8px;
}

.qty-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #ffffff;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  color: #ff0057;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.qty-btn:hover:not(:disabled) {
  background: #ff0057;
  color: #ffffff;
  border-color: #ff0057;
}

.qty-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  color: #d1d5db;
}

.quantity-input {
  flex: 1;
  border: none;
  background: transparent;
  text-align: center;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #0a0a0a;
  min-width: 50px;
}

.quantity-input:focus {
  outline: none;
}

/* Chrome, Safari, Edge */
.quantity-input::-webkit-outer-spin-button,
.quantity-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Firefox */
.quantity-input[type=number] {
  appearance: textfield;
  -moz-appearance: textfield;
}

.subtotal {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
}

.subtotal-label {
  font-size: 0.85rem;
  color: #6b7280;
  font-weight: 500;
}

.subtotal-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #0a0a0a;
}

.zone-unavailable {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  background: #f3f4f6;
  border-radius: 8px;
  color: #6b7280;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
}

.zone-unavailable i {
  font-size: 1.2rem;
}

.info-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  color: #0a0a0a;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.info-link:hover {
  border-color: #0a0a0a;
  background: rgba(10, 10, 10, 0.05);
}

/* ============ Additional Info ============ */
.additional-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding-top: 40px;
  border-top: 2px solid #e5e7eb;
}

.info-box {
  display: flex;
  gap: 15px;
}

.info-box i {
  font-size: 1.8rem;
  color: #ff0057;
  flex-shrink: 0;
}

.info-box h4 {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 4px 0;
}

.info-box p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

/* ============ Summary Section ============ */
.summary-section {
  position: sticky;
  top: 100px;
}

.summary-section-full {
  width: 100%;
}

.summary-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid #e5e7eb;
}

.summary-title {
  font-family: 'Poppins', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.summary-items {
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f3f4f6;
  font-family: 'Poppins', sans-serif;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.item-name {
  font-weight: 600;
  color: #0a0a0a;
  font-size: 0.9rem;
}

.item-qty {
  font-size: 0.8rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.item-price {
  font-weight: 700;
  color: #0a0a0a;
  font-size: 0.95rem;
  min-width: 60px;
  text-align: right;
}

.empty-summary {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  color: #9ca3af;
}

.empty-summary i {
  font-size: 2.5rem;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-summary p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  margin: 0;
}

.summary-divider {
  height: 2px;
  background: #e5e7eb;
  margin: 16px 0;
}

.price-breakdown {
  margin-bottom: 16px;
}

.breakdown-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  color: #6b7280;
}

.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
  border-radius: 10px;
  margin-bottom: 20px;
  border: 2px solid #e5e7eb;
}

.total-label {
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  color: #0a0a0a;
}

.total-amount {
  font-family: 'Poppins', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #0a0a0a;
}

.btn-purchase {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 12px;
}

.btn-purchase:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.3);
}

.btn-purchase:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-continue-shopping {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 12px;
  background: #f3f4f6;
  color: #0a0a0a;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
  margin-bottom: 16px;
}

.btn-continue-shopping:hover {
  background: #e5e7eb;
  border-color: #d1d5db;
  transform: translateY(-2px);
}

.payment-methods {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.method-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: #f3f4f6;
  border-radius: 6px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 600;
  color: #6b7280;
}

.method-badge i {
  font-size: 0.9rem;
}

/* ============ Modal ============ */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  backdrop-filter: blur(2px);
  overflow: hidden;
}

.modal-content {
  background: #ffffff;
  border-radius: 16px;
  max-width: 500px;
  width: 100%;
  max-height: 80vh;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  display: flex;
  flex-direction: column;
}

.modal-close {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: #6b7280;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  transition: all 0.2s ease;
  z-index: 2;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #0a0a0a;
}

.modal-header {
  padding: 24px;
  border-bottom: 2px solid #e5e7eb;
  flex-shrink: 0;
}

.modal-header h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.info-section {
  margin-bottom: 24px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-section h4 {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 12px 0;
}

.info-section p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.6;
}

.features-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.features-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  color: #374151;
}

.features-list i {
  color: #10b981;
  font-size: 1.1rem;
  flex-shrink: 0;
}

/* ============ Age Info Modal ============ */
.age-info-detailed {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.age-requirement-text {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  color: #ff0057;
  margin: 0;
  font-weight: 700;
}

.btn-download-doc {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-download-doc:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

.insurance-details {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  color: #0a0a0a;
  margin: 8px 0 0 0;
  font-weight: 600;
}

.modal-footer {
  padding: 24px;
  border-top: 2px solid #e5e7eb;
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.btn-modal-close {
  flex: 1;
  padding: 12px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-modal-close:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

/* ============ Transitions ============ */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* ============ Responsive ============ */
@media (max-width: 1024px) {
  .zones-grid-horizontal {
    justify-content: center;
  }

  .zone-card-compact {
    min-width: 180px;
    max-width: 240px;
  }

  .hero-title {
    font-size: 2.2rem;
  }
}

@media (max-width: 768px) {
  .carrito-page {
    background: #0a0a0a;
  }

  .hero-section {
    height: 350px;
  }

  .hero-title {
    font-size: 1.8rem;
  }

  .event-meta {
    gap: 20px;
  }

  .meta-item {
    font-size: 0.9rem;
    padding: 8px 16px;
  }

  .content-wrapper {
    padding: 25px 12px;
  }

  .tickets-section {
    padding: 20px;
  }

  .section-header h2 {
    font-size: 1.4rem;
  }

  .zones-grid-horizontal {
    gap: 12px;
    justify-content: center;
  }

  .zone-card-compact {
    flex: 0 1 calc(50% - 6px);
    min-width: 140px;
    max-width: 200px;
  }

  .zone-card {
    padding: 16px;
  }

  .zone-name {
    font-size: 1.05rem;
  }

  .zone-badge {
    font-size: 0.7rem;
    padding: 4px 8px;
  }

  .additional-info {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .info-box {
    gap: 12px;
  }

  .info-box i {
    font-size: 1.5rem;
  }

  .summary-card {
    padding: 20px;
  }

  .btn-purchase,
  .btn-continue-shopping {
    font-size: 0.85rem;
    padding: 12px;
  }

  .modal-content {
    max-width: 90%;
  }

  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 16px;
  }
}

@media (max-width: 576px) {
  .hero-section {
    height: 280px;
    padding: 20px;
  }

  .hero-title {
    font-size: 1.4rem;
    margin-bottom: 12px;
  }

  .breadcrumb-nav {
    font-size: 0.8rem;
    gap: 6px;
    margin-bottom: 15px;
  }

  .event-meta {
    flex-direction: column;
    gap: 10px;
  }

  .meta-item {
    width: 100%;
    justify-content: center;
    font-size: 0.85rem;
    padding: 8px 12px;
  }

  .content-wrapper {
    padding: 15px 10px;
    gap: 20px;
  }

  .tickets-section {
    padding: 16px;
    border-radius: 12px;
  }

  .section-header h2 {
    font-size: 1.2rem;
  }

  .section-header {
    margin-bottom: 20px;
  }

  .section-subtitle {
    font-size: 0.8rem;
  }

  .zones-grid-horizontal {
    gap: 10px;
    justify-content: center;
    margin-bottom: 30px;
  }

  .zone-card-compact {
    flex: 1 1 100%;
    min-width: 100%;
    max-width: 100%;
    padding: 12px;
  }

  .zone-header-compact {
    flex-direction: column;
  }

  .detail-row {
    font-size: 0.8rem;
  }

  .detail-label {
    font-size: 0.75rem;
  }

  .detail-value {
    font-size: 0.9rem;
  }

  .zone-controls-compact {
    gap: 8px;
  }

  .qty-btn-compact {
    width: 24px;
    height: 24px;
    font-size: 0.8rem;
  }

  .quantity-input-compact {
    font-size: 0.8rem;
  }

  .subtotal {
    padding: 10px;
    font-size: 0.85rem;
  }

  .subtotal-value {
    font-size: 1rem;
  }

  .additional-info {
    gap: 12px;
  }

  .info-box {
    gap: 10px;
  }

  .info-box i {
    font-size: 1.3rem;
  }

  .info-box h4 {
    font-size: 0.85rem;
    margin-bottom: 2px;
  }

  .info-box p {
    font-size: 0.75rem;
  }

  .summary-card {
    padding: 16px;
    border-radius: 12px;
  }

  .summary-title {
    font-size: 1.1rem;
    margin-bottom: 16px;
  }

  .summary-item {
    padding: 10px 0;
    font-size: 0.8rem;
  }

  .item-name {
    font-size: 0.8rem;
  }

  .item-qty {
    font-size: 0.7rem;
    padding: 1px 6px;
  }

  .item-price {
    font-size: 0.85rem;
  }

  .breakdown-row {
    font-size: 0.8rem;
    padding: 8px 0;
  }

  .price-total {
    padding: 12px;
    margin-bottom: 16px;
  }

  .total-label {
    font-size: 0.85rem;
  }

  .total-amount {
    font-size: 1.2rem;
  }

  .btn-purchase,
  .btn-continue-shopping {
    padding: 11px;
    font-size: 0.8rem;
    gap: 6px;
  }

  .payment-methods {
    gap: 6px;
  }

  .method-badge {
    font-size: 0.65rem;
    padding: 5px 8px;
    gap: 4px;
  }

  .notice {
    gap: 12px;
    padding: 12px 14px;
  }

  .notice i {
    font-size: 1.2rem;
  }

  .notice-content strong {
    font-size: 0.85rem;
  }

  .notice-content p {
    font-size: 0.75rem;
  }

  .info-link-compact {
    font-size: 0.75rem;
    padding: 6px;
    gap: 4px;
  }

  .toggle-details-btn {
    padding: 6px 8px;
    font-size: 0.75rem;
  }

  .expand-button {
    padding: 8px;
    font-size: 0.75rem;
  }

  .modal-overlay {
    padding: 10px;
  }

  .modal-content {
    max-width: 100%;
    max-height: 75vh;
  }

  .modal-header {
    padding: 12px 14px;
  }

  .modal-header h3 {
    font-size: 1.05rem;
  }

  .modal-body {
    padding: 14px;
    font-size: 0.85rem;
  }

  .modal-footer {
    padding: 12px 14px;
  }

  .info-section {
    margin-bottom: 16px;
  }

  .info-section:last-child {
    margin-bottom: 0;
  }

  .info-section h4 {
    font-size: 0.9rem;
    margin-bottom: 8px;
  }

  .info-section p {
    font-size: 0.8rem;
    line-height: 1.5;
  }

  .features-list li {
    font-size: 0.8rem;
    padding: 4px 0;
    gap: 6px;
  }

  .features-list i {
    font-size: 0.95rem;
  }

  .age-requirement-text {
    font-size: 0.9rem;
  }

  .btn-download-doc {
    padding: 10px 12px;
    font-size: 0.8rem;
    gap: 6px;
  }

  .insurance-details {
    font-size: 0.8rem;
  }

  .btn-modal-close {
    padding: 10px;
    font-size: 0.8rem;
  }

  .modal-close {
    width: 32px;
    height: 32px;
    top: 12px;
    right: 12px;
    font-size: 1.3rem;
  }

  /* Hero Section Mobile - Only adjust meta items size */
  .event-meta {
    gap: 10px;
    flex-wrap: wrap;
  }

  .meta-item {
    font-size: 0.8rem;
    padding: 6px 12px;
    gap: 3px;
  }

  .meta-item i {
    font-size: 0.85rem;
  }
}

/* ============ Extra Small Devices (<360px) ============ */
@media (max-width: 359px) {
  .event-meta {
    gap: 8px;
  }

  .meta-item {
    font-size: 0.75rem;
    padding: 5px 10px;
    gap: 2px;
  }

  .meta-item i {
    font-size: 0.8rem;
  }
}

/* ============ Map Section ============ */
.map-section {
  width: 100%;
  margin-top: 30px;
}

.map-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid #e5e7eb;
}

.map-title {
  font-family: 'Poppins', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 16px 0;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
  display: flex;
  align-items: center;
  gap: 10px;
}

.map-title i {
  color: #ff0057;
  font-size: 1.4rem;
}

/* Responsive map styles */
@media (max-width: 768px) {
  .map-card {
    padding: 20px;
  }

  .map-title {
    font-size: 1.1rem;
    padding-bottom: 12px;
    margin-bottom: 12px;
  }

  .map-title i {
    font-size: 1.2rem;
  }
}

@media (max-width: 576px) {
  .map-section {
    margin-top: 20px;
  }

  .map-card {
    padding: 16px;
    border-radius: 12px;
  }

  .map-title {
    font-size: 1rem;
    gap: 8px;
  }

  .map-title i {
    font-size: 1.1rem;
  }
}
</style>
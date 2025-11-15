<template>
  <client-only>
    <Header />
  </client-only>

  <div class="container carrito-container">
    <!-- Nombre del evento -->
    <div class="text-center mb-4 mt-header-space">
      <h1 class="fw-bold display-4">{{ event.title }}</h1>
      <p class="text-muted fs-5">{{ event.venue }}</p>
    </div>

    <!-- Imagen -->
    <div class="mb-5">
      <img :src="event.image" :alt="event.title" class="img-fluid rounded-3 w-100 hero-img shadow-sm" />
    </div>

    <!-- Entradas -->
    <div class="row g-4">
      <div v-for="(zone, index) in event.zones" :key="index" class="col-12 col-md-6 col-lg-4">
        <div class="card h-100 shadow ticket-card border-0">
          <div class="card-body d-flex flex-column justify-content-between">
            <div>
              <h5 class="card-title fw-bold">{{ zone.name }}</h5>
              <p class="card-text mb-2 price">{{ (zone.price || 0).toFixed(2) }} €</p>

              <!-- Avisos de disponibilidad -->
              <p v-if="(zone.availableTickets || 0) <= 0" class="text-danger fw-bold">
                {{ $t('Agotado') }}
              </p>
              <p v-else-if="(zone.availableTickets || 0) <= 10" class="text-warning fw-bold">
                {{ $t('Quedan pocas entradas', { zone: zone.name }) }}
              </p>

              <p class="card-text text-primary" v-if="(zone.availableTickets || 0) > 0">
                <small class="cursor-pointer" @click="showPolicy = true">
                  {{ $t('Consulta política') }}
                </small>
              </p>
            </div>

            <div v-if="(zone.availableTickets || 0) > 0" class="mt-3 d-flex justify-content-between align-items-center">
              <div>
                <label class="form-label mb-1">{{ $t('Cantidad') }}:</label>
                <input
                  type="number"
                  min="0"
                  :max="zone.availableTickets || 0"
                  class="form-control quantity-input"
                  v-model.number="zone.quantity"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Total -->
    <div class="text-end mt-4">
      <h4>
        {{ $t('Total') }}: {{ total.toFixed(2) }} €
        <small class="text-muted">{{ $t('Incluye comisión') }}</small>
      </h4>
      <button class="btn btn-dark btn-lg mt-2" @click="goToPay" :disabled="!hasTicketsSelected">
        {{ $t('Comprar Todo') }}
      </button>
    </div>
  </div>

  <!-- Modal -->
  <transition name="fade">
    <div v-if="showPolicy" class="custom-modal d-flex justify-content-center align-items-center">
      <div class="modal-content p-4 rounded-3 shadow-lg">
        <div class="modal-header">
          <h5 class="modal-title">{{ $t('Política de Menores') }}</h5>
          <button type="button" class="btn-close" @click="showPolicy = false"></button>
        </div>
        <div class="modal-body">
          <p>{{ $t('Aviso menores') }}</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" @click="showPolicy = false">
            {{ $t('Aceptar') }}
          </button>
        </div>
      </div>
    </div>
  </transition>

  <Footer />
</template>

<script setup>
import { useHead } from '#app'

// Configuración de la API
const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'http://localhost:8085'

const router = useRouter()
const route = useRoute()

const event = ref({ 
  title: '', 
  venue: '', 
  image: '', 
  zones: [],
  _id: '',
  description: '',
  date: ''
})
const showPolicy = ref(false)
const loading = ref(true)
const error = ref('')

// Función para generar slug igual que en el index
const generateSlug = (title) => {
  if (!title) return ''
  return title.toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
}

// Computed con comisión incluida
const total = computed(() => {
  if (!event.value.zones) return 0
  let sum = event.value.zones.reduce((sum, z) => {
    const price = Number(z.price) || 0
    const quantity = Number(z.quantity) || 0
    return sum + (price * quantity)
  }, 0)
  const totalComision = event.value.zones.reduce((sum, z) => {
    const quantity = Number(z.quantity) || 0
    return sum + (quantity * 1.5)
  }, 0)
  return sum + totalComision
})

// Verificar si hay tickets seleccionados
const hasTicketsSelected = computed(() => {
  if (!event.value.zones) return false
  return event.value.zones.some(z => (z.quantity || 0) > 0)
})

// Cargar evento desde MongoDB
const loadEvent = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('🔄 Cargando evento desde:', `${API_BASE}/events`)
    
    const response = await $fetch(`${API_BASE}/events`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    
    if (!Array.isArray(response)) throw new Error('Formato de respuesta inválido')
    
    const foundEvent = response.find(e => {
      const eventSlug = e.slug || generateSlug(e.title)
      return eventSlug === route.params.slug
    })
    
    if (foundEvent) {
      const zonesWithQuantity = (foundEvent.zones || []).map(zone => ({
        ...zone,
        price: Number(zone.price) || 0,
        availableTickets: Number(zone.availableTickets) || 0,
        quantity: 0
      }))
      
      event.value = { ...foundEvent, zones: zonesWithQuantity }
      console.log('✅ Evento cargado:', event.value.title)
      
      // --- SEO profesional dinámico ---
      useHead({
        title: `${event.value.title} | GoLive`,
        meta: [
          { name: 'description', content: event.value.description || 'Compra tus entradas para este evento en GoLive.' },
          { property: 'og:title', content: `${event.value.title} | GoLive` },
          { property: 'og:description', content: event.value.description || 'Compra tus entradas para este evento en GoLive.' },
          { property: 'og:image', content: event.value.image || '/default-event.jpg' },
          { property: 'og:type', content: 'event' }
        ]
      })
      
    } else {
      error.value = 'Evento no encontrado'
      event.value.title = 'Evento no encontrado'
    }
    
  } catch (err) {
    console.error('❌ Error cargando evento:', err)
    error.value = 'Error al cargar el evento: ' + (err.message || 'Error desconocido')
  } finally {
    loading.value = false
  }
}

// Función para ir a pago
const goToPay = () => {
  if (!hasTicketsSelected.value) {
    alert('Selecciona al menos una entrada')
    return
  }

  const selected = event.value.zones.filter(z => (z.quantity || 0) > 0)
  
  const order = { 
    event: {
      _id: event.value._id,
      title: event.value.title,
      venue: event.value.venue,
      image: event.value.image,
      date: event.value.date
    }, 
    tickets: selected, 
    total: total.value 
  }
  
  localStorage.setItem('currentOrder', JSON.stringify(order))
  router.push(`/pay/${generateSlug(event.value.title)}`)
}

// Cargar evento al montar
onMounted(() => loadEvent())
</script>

<style scoped>
.mt-header-space { 
  margin-top: 100px; 
}

.hero-img { 
  object-fit: cover; 
  max-height: 400px; 
  width: 100%; 
}

.ticket-card { 
  border-radius: 12px; 
  background: #fff; 
  transition: 0.3s ease; 
}

.ticket-card:hover { 
  transform: translateY(-5px); 
  box-shadow: 0 10px 25px rgba(0,0,0,0.15); 
}

.price { 
  font-size: 1.25rem; 
  color: #333; 
}

.quantity-input { 
  width: 80px; 
}

.custom-modal { 
  position: fixed; 
  top: 0; 
  left: 0; 
  width: 100%; 
  height: 100%; 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  background: rgba(0,0,0,0.5); 
  z-index: 1050; 
}

.modal-content { 
  max-width: 500px; 
  background: white; 
  border-radius: 8px; 
}

.cursor-pointer {
  cursor: pointer;
}

/* Estados de botón deshabilitado */
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .display-4 {
    font-size: 2rem;
  }
  
  .fs-5 {
    font-size: 1rem !important;
  }
  
  .hero-img {
    max-height: 300px;
  }
}

@media (max-width: 576px) {
  .display-4 {
    font-size: 1.5rem;
  }
  
  .hero-img {
    max-height: 250px;
  }
}
</style>
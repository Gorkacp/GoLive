<template>
  <div class="d-flex flex-column min-vh-100">
    <!-- Header -->
    <client-only>
      <Header @buscar-evento="filtrarEventos" />
    </client-only>

    <!-- Hero -->
    <div class="hero position-relative text-center d-flex align-items-center justify-content-center">
      <img src="/assets/img/1.jpg" alt="Hero" class="hero-img w-100" />
      <div class="hero-text position-absolute">
        <h1 class="display-3 fw-bold">GoLive</h1>
        <p class="lead">{{ $t('HeroText') }}</p>
      </div>
    </div>

    <!-- Grid de eventos -->
    <div class="container my-5 flex-grow-1">
      <h2 class="mb-4 text-center text-dark">{{ $t('UpcomingEvents') }}</h2>

      <!-- Spinner mientras carga -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-3">{{ $t('LoadingEvents') }}</p>
      </div>

      <!-- Error al cargar -->
      <div v-else-if="error" class="alert alert-danger text-center">
        <i class="fas fa-exclamation-triangle me-2"></i>
        {{ error }}
      </div>

      <!-- Eventos filtrados -->
      <div v-else-if="filteredEvents.length" class="row g-4">
        <div
          v-for="event in filteredEvents"
          :key="event._id || event.id"
          class="col-12 col-sm-6 col-md-4 col-lg-3"
        >
          <EventCard :event="event" />
        </div>
      </div>

      <!-- Mensaje si no hay eventos -->
      <div v-else class="text-center py-5">
        <i class="fas fa-calendar-times fa-3x text-muted mb-3"></i>
        <p class="text-muted">{{ $t('NoEvents') }}</p>
        <button class="btn btn-primary mt-2" @click="loadEvents">
          <i class="fas fa-refresh me-2"></i>Reintentar
        </button>
      </div>
    </div>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup>
const config = useRuntimeConfig()
const API_BASE = config.public.apiBase 

const events = ref([])
const filteredEvents = ref([])
const loading = ref(true)
const error = ref('')

// Función para cargar eventos desde el backend MongoDB
const loadEvents = async () => {
  loading.value = true
  error.value = ''
  
  try {
    console.log('🔄 Cargando eventos desde:', `${API_BASE}/events`)

    const response = await $fetch(`${API_BASE}/events`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      timeout: 10000
    })

    events.value = Array.isArray(response) ? response : []

    if (events.value.length === 0) {
      console.log('ℹ️ No hay eventos en la base de datos')
    }

    // Filtrar solo eventos futuros o activos
    const now = new Date()
    events.value = events.value.filter(event => {
      const eventDate = new Date(event.date || event.eventDate)
      return !event.date || eventDate >= now || event.isActive !== false
    })

    // Ordenar por fecha
    events.value.sort((a, b) => {
      const dateA = new Date(a.date || a.eventDate || '9999-12-31')
      const dateB = new Date(b.date || b.eventDate || '9999-12-31')
      return dateA - dateB
    })

    filteredEvents.value = [...events.value]
    console.log('✅ Eventos cargados:', events.value.length)

  } catch (err) {
    console.error('❌ Error cargando eventos:', err)

    if (err.status === 404) {
      error.value = 'Endpoint no encontrado. Verifica la URL del backend.'
    } else if (err.status === 0 || err.message?.includes('fetch')) {
      error.value = 'Error de conexión. Verifica que el backend esté ejecutándose en ' + API_BASE
    } else if (err.status === 500) {
      error.value = 'Error interno del servidor. Intenta nuevamente.'
    } else {
      error.value = 'Error al cargar los eventos: ' + (err.message || 'Error desconocido')
    }
  } finally {
    loading.value = false
  }
}

// Filtra los eventos por título, lugar o descripción
const filtrarEventos = (texto) => {
  if (!texto) {
    filteredEvents.value = [...events.value]
  } else {
    const searchTerm = texto.toLowerCase()
    filteredEvents.value = events.value.filter(event =>
      (event.title && event.title.toLowerCase().includes(searchTerm)) ||
      (event.venue && event.venue.toLowerCase().includes(searchTerm)) ||
      (event.location && event.location.toLowerCase().includes(searchTerm)) ||
      (event.description && event.description.toLowerCase().includes(searchTerm))
    )
  }
}

// Cargar eventos al montar
onMounted(() => loadEvents())

// Recargar eventos si se vuelve visible la pestaña
onMounted(() => {
  if (process.client) {
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden) loadEvents()
    })
  }
})
</script>


<style scoped>
.hero {
  height: 60vh;
  position: relative;
  overflow: hidden;
}

.hero-img {
  object-fit: cover;
  height: 100%;
  width: 100%;
  filter: brightness(0.5);
}

.hero-text {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.hero-text h1 {
  color: #ffffff;
  font-size: 4rem;
  font-weight: 900;
  text-shadow: 2px 2px 8px rgba(0,0,0,0.7);
}

.hero-text p {
  font-size: 1.5rem;
  color: #ffffff;
}

h2.text-dark {
  color: #0a0a0a;
  font-weight: 700;
}

.spinner-border {
  width: 3rem;
  height: 3rem;
}

.alert {
  border-radius: 10px;
  border: none;
}

@media (max-width: 768px) {
  .hero {
    height: 50vh;
  }

  .hero-text h1 {
    font-size: 2.5rem;
  }

  .hero-text p {
    font-size: 1.2rem;
  }
}

@media (max-width: 576px) {
  .hero {
    height: 40vh;
  }

  .hero-text h1 {
    font-size: 2rem;
  }
}
</style>

<template>
  <div class="d-flex flex-column min-vh-100 main-container">
    <!-- Header -->
    <client-only>
      <Header @buscar-evento="filtrarEventos" />
    </client-only>

    <!-- Hero -->
    <div class="hero position-relative text-center d-flex align-items-center justify-content-center" style="margin-top: 0 !important; padding-top: 0 !important;">
      <img src="/assets/img/1.jpg" alt="Hero" class="hero-img w-100" />
      <div class="hero-text position-absolute">
        <h1 class="display-3 fw-bold">GoLive</h1>
        <p class="lead">{{ $t('HeroText') }}</p>
      </div>
    </div>

    <!-- Grid de eventos -->
    <div class="events-section">
      <div class="events-wrapper">
        <!-- Filtros en línea horizontal (sin título) -->
        <div class="filters-horizontal">
          <!-- Búsqueda por nombre -->
          <div class="search-box-inline">
            <i class="bi bi-search"></i>
            <input 
              v-model="searchQuery" 
              type="text" 
              class="search-input" 
              :placeholder="$t('Busca evento')" 
            />
          </div>

          <!-- Búsqueda por ciudad -->
          <div class="location-box-inline">
            <i class="bi bi-geo-alt"></i>
            <input 
              v-model="locationFilter" 
              type="text" 
              class="search-input" 
              :placeholder="$t('Ciudad')" 
            />
          </div>

          <!-- Separador -->
          <div class="filter-separator"></div>

          <!-- Categorías -->
          <button 
            @click="activeCategory = 'all'" 
            :class="['category-btn', { active: activeCategory === 'all' }]"
            :title="$t('Todos los eventos')"
          >
            <i class="bi bi-music-note-beamed"></i>
            {{ $t('Todos') }}
          </button>
          <button 
            @click="activeCategory = 'concierto'" 
            :class="['category-btn', { active: activeCategory === 'concierto' }]"
            :title="$t('Filtrar conciertos')"
          >
            <i class="bi bi-mic"></i>
            {{ $t('Conciertos') }}
          </button>
          <button 
            @click="activeCategory = 'festival'" 
            :class="['category-btn', { active: activeCategory === 'festival' }]"
            :title="$t('Filtrar festivales')"
          >
            <i class="bi bi-people"></i>
            {{ $t('Festivales') }}
          </button>
        </div>

        <!-- Filtros para dispositivos pequeños -->
        <div class="filters-mobile-search">
          <!-- Búsqueda por nombre -->
          <div class="search-box-inline">
            <i class="bi bi-search"></i>
            <input 
              v-model="searchQuery" 
              type="text" 
              class="search-input" 
              :placeholder="$t('Busca evento')" 
            />
          </div>
        </div>

        <div class="filters-mobile-categories">
          <!-- Categorías -->
          <button 
            @click="activeCategory = 'all'" 
            :class="['category-btn', { active: activeCategory === 'all' }]"
            :title="$t('Todos los eventos')"
          >
            <i class="bi bi-music-note-beamed"></i>
            {{ $t('Todos') }}
          </button>
          <button 
            @click="activeCategory = 'concierto'" 
            :class="['category-btn', { active: activeCategory === 'concierto' }]"
            :title="$t('Filtrar conciertos')"
          >
            <i class="bi bi-mic"></i>
            {{ $t('Conciertos') }}
          </button>
          <button 
            @click="activeCategory = 'festival'" 
            :class="['category-btn', { active: activeCategory === 'festival' }]"
            :title="$t('Filtrar festivales')"
          >
            <i class="bi bi-people"></i>
            {{ $t('Festivales') }}
          </button>
        </div>

        <!-- Spinner mientras carga -->
        <div v-if="loading" class="loading-state">
          <div class="spinner-border" role="status"></div>
          <p class="mt-3">{{ $t('LoadingEvents') }}</p>
        </div>

        <!-- Error al cargar -->
        <div v-else-if="error" class="error-state">
          <i class="bi bi-exclamation-circle"></i>
          <p>{{ error }}</p>
          <button class="btn-retry" @click="loadEvents">
            <i class="bi bi-arrow-clockwise"></i> Reintentar
          </button>
        </div>

        <!-- Eventos filtrados -->
        <div v-else-if="filteredEvents.length" class="events-grid">
          <div
            v-for="event in displayedEvents"
            :key="event._id || event.id"
            class="event-card-wrapper"
          >
            <EventCard :event="event" />
          </div>
        </div>

        <!-- Mensaje si no hay eventos -->
        <div v-else class="empty-state">
          <i class="bi bi-calendar-event"></i>
          <h3>No se encontraron eventos</h3>
          <p>Intenta cambiar tus filtros de búsqueda</p>
          <button class="btn-reset" @click="resetFilters">
            <i class="bi bi-x-circle"></i> Limpiar filtros
          </button>
        </div>

        <!-- Botón Ver más -->
        <div v-if="filteredEvents.length > maxDisplayedEvents && !loading" class="ver-mas-container">
          <button class="btn-ver-mas" @click="goToCategoryPage">
            <span>{{ $t('Ver más') }}</span>
            <i class="bi bi-arrow-right"></i>
          </button>
        </div>
      </div>
    </div>

    <!-- App Download Section -->
    <div class="app-download-section">
      <div class="app-download-container">
        <div class="app-download-content">
          <div class="app-download-text">
            <h2 class="app-download-title">{{ $t('Ten tus entradas siempre a mano') }}</h2>
            <p class="app-download-subtitle">{{ $t('Descarga la app de GoLive y gestiona tus entradas desde cualquier lugar') }}</p>
            
            <div class="app-download-features">
              <div class="feature-item">
                <i class="bi bi-ticket-perforated"></i>
                <span>{{ $t('Entradas siempre contigo') }}</span>
              </div>
              <div class="feature-item">
                <i class="bi bi-qr-code"></i>
                <span>{{ $t('Código QR incluido') }}</span>
              </div>
              <div class="feature-item">
                <i class="bi bi-bell"></i>
                <span>{{ $t('Notificaciones de eventos') }}</span>
              </div>
            </div>

            <div class="app-download-buttons">
              <button class="btn-download btn-android" @click="installApp('android')" title="Descargar para Android">
                <i class="bi bi-android2"></i>
                <span>Play Store</span>
              </button>
            </div>
          </div>

          <div class="app-download-visual">
            <img :src="phone1" alt="GoLive App" class="phone-image" />
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup>
import { useHead } from '#app'
import EventCard from '~/components/EventCard.vue'
import phone1 from '~/assets/img/phone1.png'

useHead({
  title: 'Entradas para tus eventos | GoLive',
  meta: [
    { name: 'description', content: 'Compra entradas para conciertos, festivales y eventos en GoLive. Descubre y vive la mejor música en directo. ¡Tus eventos favoritos en un solo lugar!' },
    { property: 'og:title', content: 'Entradas para tus eventos | GoLive' },
    { property: 'og:description', content: 'Compra entradas para conciertos, festivales y eventos en GoLive. Descubre y vive la mejor música en directo.' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: 'https://golive-hu5d.onrender.com/' },
    { property: 'og:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'Entradas para tus eventos | GoLive' },
    { name: 'twitter:description', content: 'Compra entradas para conciertos, festivales y eventos en GoLive.' },
    { name: 'twitter:image', content: 'https://golive.com/assets/img/1.jpg' }
  ],
  link: [
    { rel: 'canonical', href: 'https://golive-hu5d.onrender.com/' }
  ]
})

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase 

const events = ref([])
const loading = ref(true)
const error = ref('')
const searchQuery = ref('')
const locationFilter = ref('')
const activeCategory = ref('all')
const isMobile = ref(false)

// Detectar si es móvil
const checkMobile = () => {
  if (process.client) {
    isMobile.value = window.innerWidth <= 768
  }
}

// Límite de eventos según el dispositivo
const maxDisplayedEvents = computed(() => {
  return isMobile.value ? 4 : 8
})

// Cargar eventos desde el backend MongoDB
const loadEvents = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await $fetch(`${API_BASE}/events`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      timeout: 10000
    })

    events.value = Array.isArray(response) ? response : []

    // Ordenar por fecha (sin filtrar por futuro)
    events.value.sort((a, b) => {
      const dateA = new Date(a.date || a.eventDate || '9999-12-31')
      const dateB = new Date(b.date || b.eventDate || '9999-12-31')
      return dateA - dateB
    })
  } catch (err) {
    error.value = 'Error al cargar los eventos. Intenta nuevamente.'
  } finally {
    loading.value = false
  }
}

// Limpiar todos los filtros
const resetFilters = () => {
  searchQuery.value = ''
  locationFilter.value = ''
  activeCategory.value = 'all'
}

// Eventos filtrados con lógica combinada
const filteredEvents = computed(() => {
  return events.value.filter((event) => {
    // Filtro de categoría
    if (activeCategory.value !== 'all') {
      const categoryMatch = event.category?.toLowerCase() === activeCategory.value.toLowerCase()
      if (!categoryMatch) return false
    }

    // Filtro de búsqueda por nombre
    if (searchQuery.value.trim()) {
      const query = searchQuery.value.toLowerCase()
      const titleMatch = event.title?.toLowerCase().includes(query) || false
      const descriptionMatch = event.description?.toLowerCase().includes(query) || false
      if (!titleMatch && !descriptionMatch) return false
    }

    // Filtro de ubicación/ciudad
    if (locationFilter.value.trim()) {
      const location = locationFilter.value.toLowerCase()
      const eventLocation = event.location?.toLowerCase() || ''
      const eventVenue = event.venue?.toLowerCase() || ''
      if (!eventLocation.includes(location) && !eventVenue.includes(location)) return false
    }

    return true
  })
})

// Eventos mostrados (limitados según dispositivo)
const displayedEvents = computed(() => {
  return filteredEvents.value.slice(0, maxDisplayedEvents.value)
})

// Función para navegar según la categoría seleccionada
const goToCategoryPage = () => {
  const router = useRouter()
  if (activeCategory.value === 'festival') {
    router.push('/festivales')
  } else {
    router.push('/conciertos')
  }
}

// Filtrar por búsqueda desde el header (compatibilidad)
const filtrarEventos = (texto) => {
  if (!texto) {
    searchQuery.value = ''
  } else {
    searchQuery.value = texto
  }
}

// Funcionalidad de descarga de app (PWA Install)
let deferredPrompt = null

if (process.client) {
  window.addEventListener('beforeinstallprompt', (e) => {
    e.preventDefault()
    deferredPrompt = e
  })
}

const installApp = async (platform) => {
  if (deferredPrompt) {
    deferredPrompt.prompt()
    const { outcome } = await deferredPrompt.userChoice
    if (outcome === 'accepted') {
      console.log('✅ App instalada')
    }
    deferredPrompt = null
  }
}

// Cargar eventos solo una vez al montar la página
onMounted(() => {
  loadEvents()
  checkMobile()
  
  // Escuchar cambios de tamaño de ventana
  if (process.client) {
    window.addEventListener('resize', checkMobile)
  }
})

// Limpiar listener al desmontar
onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('resize', checkMobile)
  }
})

// Función para obtener la URL correcta de la imagen del teléfono
const getPhoneImageUrl = () => {
  if (filteredEvents.value.length > 0 && filteredEvents.value[0].img) {
    let imgUrl = filteredEvents.value[0].img
    // Si la ruta ya empieza con /, usarla tal cual
    if (imgUrl.startsWith('/')) {
      return imgUrl
    }
    // Si no, agregar /assets/img/ al inicio
    if (!imgUrl.includes('/assets/img/')) {
      return `/assets/img/${imgUrl}`
    }
    return imgUrl
  }
  // Fallback por defecto - usar imagen importada
  return phoneImage
}
</script>


<style scoped>
/* ============ Global Reset ============ */
:deep(html),
:deep(body) {
  overflow-x: hidden !important;
  max-width: 100vw !important;
  width: 100vw !important;
}

.main-container {
  width: 100%;
  max-width: 100vw;
  overflow-x: hidden;
  margin: 0;
  padding: 0;
}

/* Solo en móviles: eliminar espacio entre header y contenido */
@media (max-width: 768px) {
  :deep(html),
  :deep(body) {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }

  :deep(body.fixed-top),
  :deep(body.has-fixed-top),
  :deep(body) {
    padding-top: 0 !important;
  }

  :deep(.navbar.fixed-top) {
    margin-bottom: 0 !important;
  }

  client-only {
    display: block;
    margin: 0;
    padding: 0;
  }

  client-only + .hero {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }

  .main-container {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }

  .main-container > .hero {
    margin-top: 0 !important;
    padding-top: 0 !important;
    position: relative;
    top: 0;
  }
}

/* ============ Hero Section ============ */
.hero {
  height: 60vh;
  position: relative;
  overflow: hidden;
  background: transparent;
}

/* Solo en móviles: eliminar espacio superior */
@media (max-width: 768px) {
  .hero {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}

.hero-img {
  object-fit: cover;
  height: 100%;
  width: 100%;
  filter: brightness(0.5);
  display: block;
  margin: 0;
  padding: 0;
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
  font-family: 'Poppins', sans-serif;
  letter-spacing: -1px;
}

.hero-text p {
  font-size: 1.5rem;
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-weight: 300;
}

/* ============ Events Section ============ */
.events-section {
  flex: 1;
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  padding: 0;
  position: relative;
  overflow: hidden;
  margin: 0;
  width: 100%;
  box-sizing: border-box;
}

.events-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 15% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 85% 70%, rgba(0, 0, 0, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.events-wrapper {
  max-width: 100%;
  margin: 0;
  padding: 0;
  position: relative;
  z-index: 1;
  box-sizing: border-box;
}

.events-title {
  font-family: 'Poppins', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  color: #0a0a0a;
  margin-bottom: 35px;
  text-align: center;
  letter-spacing: -0.5px;
  display: none;
}

/* ============ Filters Horizontal ============ */
.filters-horizontal {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  justify-content: flex-start;
  background: #0a0a0a;
  padding: 20px 15px;
  border-radius: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
  border: none;
  width: 100%;
  box-sizing: border-box;
}

.filters-mobile-search {
  display: none;
  width: 100%;
  box-sizing: border-box;
}

.filters-mobile-categories {
  display: none;
  width: 100%;
  box-sizing: border-box;
}

.category-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: 2px solid #ff0057;
  background: transparent;
  color: #ffffff;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  flex-shrink: 0;
}

.category-btn i {
  font-size: 1.1rem;
  display: inline-flex;
  align-items: center;
}

.category-btn:hover {
  border-color: #ff6b35;
  background: rgba(255, 0, 87, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

.category-btn.active {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  background-clip: padding-box;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 10px 18px;
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.5);
  font-weight: 700;
}

.filter-separator {
  width: 2px;
  height: 30px;
  background: rgba(255, 255, 255, 0.3);
  margin: 0 4px;
}

/* ============ Search Filters Inline ============ */
.search-box-inline,
.location-box-inline {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #ff0057;
  border-radius: 8px;
  padding: 0 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 180px;
  flex-shrink: 0;
}

.search-box-inline:focus-within,
.location-box-inline:focus-within {
  border-color: #ff6b35;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

.search-box-inline i,
.location-box-inline i {
  color: #ff0057;
  margin-right: 10px;
  font-size: 1.05rem;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  padding: 14px 0;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #0a0a0a;
  outline: none;
}

.search-input::placeholder {
  color: #b0b0b0;
}

/* ============ Events Grid ============ */
.events-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  animation: fadeIn 0.4s ease;
  width: 100%;
  padding: 10px 15px 0;
  box-sizing: border-box;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.event-card-wrapper {
  transition: all 0.3s ease;
  border-radius: 8px;
  overflow: hidden;
}

.event-card-wrapper:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

/* ============ Loading State ============ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 15px;
  text-align: center;
}

.loading-state .spinner-border {
  width: 50px;
  height: 50px;
  border-width: 4px;
  color: #0a0a0a;
  border-right-color: transparent;
}

.loading-state p {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  color: #6b7280;
  margin-top: 20px;
  font-weight: 500;
}

/* ============ Error State ============ */
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 15px;
  text-align: center;
  background: #fef2f2;
  border: 2px solid #fee2e2;
  border-radius: 12px;
  margin: 40px 15px;
}

.error-state i {
  font-size: 3rem;
  color: #dc2626;
  margin-bottom: 15px;
}

.error-state p {
  font-family: 'Poppins', sans-serif;
  color: #991b1b;
  font-size: 1rem;
  margin-bottom: 20px;
  font-weight: 500;
}

.btn-retry {
  padding: 12px 24px;
  background: #dc2626;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-retry:hover {
  background: #b91c1c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
}

/* ============ Empty State ============ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 15px;
  text-align: center;
  background: #ffffff;
  border: 2px dashed #e5e5e5;
  border-radius: 12px;
  margin: 40px 15px;
}

.empty-state i {
  font-size: 3rem;
  color: #d1d5db;
  margin-bottom: 15px;
}

.empty-state h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.5rem;
  color: #0a0a0a;
  margin-bottom: 8px;
  font-weight: 700;
}

.empty-state p {
  font-family: 'Poppins', sans-serif;
  color: #6b7280;
  font-size: 0.95rem;
  margin-bottom: 20px;
  font-weight: 400;
}

.btn-reset {
  padding: 12px 24px;
  background: #0a0a0a;
  color: #ffffff;
  border: 2px solid #0a0a0a;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.btn-reset:hover {
  background: transparent;
  color: #0a0a0a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(10, 10, 10, 0.1);
}

/* ============ Results Info ============ */
.results-info {
  text-align: center;
  padding: 0;
  margin: 0;
}

.results-info p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #6b7280;
  font-weight: 500;
}

.results-info strong {
  color: #0a0a0a;
  font-weight: 700;
}

/* ============ Ver Más Button ============ */
.ver-mas-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 15px;
  margin-top: 20px;
}

.btn-ver-mas {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.4);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-ver-mas:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(255, 0, 87, 0.6);
  background: linear-gradient(135deg, #ff1a6b 0%, #ff7b45 100%);
}

.btn-ver-mas:active {
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(255, 0, 87, 0.5);
}

.btn-ver-mas i {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.btn-ver-mas:hover i {
  transform: translateX(4px);
}

/* ============ Responsive ============ */
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

  .events-section {
    padding: 40px 0;
  }

  .events-wrapper {
    padding: 0 8px;
  }

  .events-title {
    font-size: 2rem;
    margin-bottom: 25px;
  }

  .filters-horizontal {
    gap: 10px;
    margin-bottom: 30px;
  }

  .category-btn {
    padding: 8px 14px;
    font-size: 0.85rem;
  }

  .category-btn i {
    font-size: 0.9rem;
  }

  .filter-separator {
    height: 26px;
  }

  .search-box-inline,
  .location-box-inline {
    min-width: 160px;
    padding: 0 10px;
  }

  .events-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 18px;
  }

  .loading-state,
  .error-state,
  .empty-state {
    padding: 60px 12px;
  }
}

@media (max-width: 576px) {
  .hero {
    height: 40vh;
    margin: 0;
    padding: 0;
  }

  .hero-text h1 {
    font-size: 1.8rem;
  }

  .hero-text p {
    font-size: 1rem;
  }

  .events-section {
    padding: 0;
    margin: 0;
  }

  .events-wrapper {
    padding: 0;
    margin: 0;
  }

  .events-title {
    font-size: 1.5rem;
    margin-bottom: 18px;
  }

  .filters-horizontal {
    display: none;
  }

  .filters-mobile-search {
    display: flex;
    background: #0a0a0a;
    padding: 12px 8px;
    margin: 0;
    box-sizing: border-box;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
    align-items: center;
  }

  .filters-mobile-search .search-box-inline {
    width: 100%;
    min-width: auto;
    padding: 0 8px;
  }

  .filters-mobile-categories {
    display: flex;
    background: #0a0a0a;
    padding: 10px 8px;
    margin: 0 0 20px 0;
    box-sizing: border-box;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
    gap: 8px;
    justify-content: center;
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .location-box-inline {
    display: none;
  }

  .filter-separator {
    display: none;
  }

  .category-btn {
    padding: 8px 12px;
    font-size: 0.75rem;
    white-space: nowrap;
    flex-shrink: 0;
  }

  .category-btn i {
    font-size: 0.85rem;
  }

  .search-input {
    padding: 10px 0 !important;
    font-size: 0.85rem;
  }

  .events-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    padding: 15px 8px 0;
  }

  .loading-state,
  .error-state,
  .empty-state {
    padding: 50px 8px;
  }

  .results-info p {
    font-size: 0.8rem;
  }

  .ver-mas-container {
    padding: 30px 8px;
    margin-top: 15px;
  }

  .btn-ver-mas {
    padding: 14px 24px;
    font-size: 0.95rem;
  }

  .btn-ver-mas i {
    font-size: 1rem;
  }
}

* {
  box-sizing: border-box;
}

html, body {
  overflow-x: hidden !important;
  max-width: 100vw !important;
  width: 100vw !important;
  margin: 0 !important;
  padding: 0 !important;
  padding-top: 0 !important;
}

body {
  padding-top: 0 !important;
}

/* ============ App Download Section ============ */
.app-download-section {
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  padding: 60px 20px;
  position: relative;
  overflow: hidden;
}

.app-download-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 15% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 85% 70%, rgba(0, 0, 0, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.app-download-section::after {
  content: '';
  position: absolute;
  top: -50%;
  right: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
  z-index: 0;
}

.app-download-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.app-download-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 50px;
  align-items: center;
}

.app-download-text {
  color: #ffffff;
}

.app-download-title {
  font-family: 'Poppins', sans-serif;
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 15px;
  line-height: 1.2;
  letter-spacing: -0.5px;
}

.app-download-subtitle {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  font-weight: 400;
  margin-bottom: 30px;
  opacity: 0.95;
  line-height: 1.5;
}

.app-download-features {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 500;
}

.feature-item i {
  font-size: 1.3rem;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
}

.app-download-buttons {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.phone-image {
  max-width: 100%;
  height: auto;
  max-height: 600px;
  object-fit: contain;
  filter: drop-shadow(0 30px 80px rgba(0, 0, 0, 0.5));
}

.btn-download {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.btn-download i {
  font-size: 1.2rem;
}

.btn-ios {
  background: #000000;
  color: #ffffff;
}

.btn-ios:hover {
  background: #1a1a1a;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.btn-android {
  background: #ffffff;
  color: #ff0057;
}

.btn-android:hover {
  background: #f5f5f5;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.app-download-visual {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 2;
}

.phone-mockup {
  position: relative;
  width: 260px;
  height: 520px;
  background: transparent;
  border-radius: 50px;
  padding: 0;
  overflow: hidden;
  box-shadow: 
    0 30px 80px rgba(0, 0, 0, 0.5),
    inset 0 0 0 8px #1a1a1a,
    inset 0 0 0 10px #2a2a2a,
    0 0 0 1px rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

/* Notch del teléfono */
.phone-notch {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 150px;
  height: 24px;
  background: #0a0a0a;
  border-radius: 0 0 16px 16px;
  z-index: 20;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.6);
}

.phone-screen {
  width: 100%;
  height: 100%;
  background: #0a0a0a;
  border-radius: 45px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
  background-attachment: local !important;
  background-clip: padding-box !important;
}

/* Overlay oscuro sobre la imagen */
.phone-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, rgba(0,0,0,0.3) 0%, rgba(0,0,0,0.6) 100%);
  pointer-events: none;
  z-index: 1;
}

/* Status bar */
.status-bar {
  background: transparent;
  padding: 8px 16px 4px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.7rem;
  color: #ffffff;
  margin-top: 4px;
  position: relative;
  z-index: 2;
}

.status-time {
  font-weight: 600;
  font-size: 0.75rem;
}

/* Header móvil */
.mobile-header {
  background: rgba(10, 10, 10, 0.8);
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
  backdrop-filter: blur(10px);
}

.header-brand {
  font-family: 'Poppins', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #ff0057;
  text-shadow: 0 0 8px rgba(255, 0, 87, 0.5);
}

.header-menu {
  color: #ffffff;
  font-size: 1.2rem;
  cursor: pointer;
}

/* Screen content */
.screen-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 0;
  background: transparent;
  -webkit-overflow-scrolling: touch;
  position: relative;
  z-index: 2;
}

.screen-content::-webkit-scrollbar {
  width: 3px;
}

.screen-content::-webkit-scrollbar-track {
  background: transparent;
}

.screen-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

/* Mobile search bar */
.mobile-search-bar {
  background: #0a0a0a;
  margin: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 2px solid #ff0057;
}

.mobile-search-bar i {
  color: #ff0057;
  font-size: 0.9rem;
}

.mobile-search-bar input {
  flex: 1;
  background: transparent;
  border: none;
  color: #ffffff;
  font-size: 0.8rem;
  outline: none;
  font-family: 'Poppins', sans-serif;
}

.mobile-search-bar input::placeholder {
  color: #888;
}

/* Mobile categories */
.mobile-categories {
  display: flex;
  gap: 8px;
  padding: 10px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.mobile-categories::-webkit-scrollbar {
  height: 0;
}

.cat-btn {
  padding: 8px 12px;
  border-radius: 8px;
  border: 2px solid #ff0057;
  background: rgba(10, 10, 10, 0.7);
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(10px);
}

.cat-btn:hover {
  background: rgba(255, 0, 87, 0.2);
}

.cat-btn.active {
  background: #ff0057;
  border-color: #ff0057;
}

/* Event featured card */
.mobile-event-featured {
  margin: 10px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.8);
  border: 1px solid rgba(255, 0, 87, 0.3);
  backdrop-filter: blur(10px);
}

.event-info-card {
  padding: 10px;
  background: rgba(0, 0, 0, 0.9);
}

.event-label {
  background: #ff6b35;
  color: #ffffff;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.65rem;
  font-weight: 700;
  display: inline-block;
  margin-bottom: 4px;
}

.event-title-featured {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 4px;
}

.event-location-featured {
  font-family: 'Poppins', sans-serif;
  font-size: 0.7rem;
  color: #b0b0b0;
}

/* ============ Responsive App Download ============ */
@media (max-width: 768px) {
  .app-download-section {
    padding: 40px 15px;
  }

  .app-download-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .app-download-title {
    font-size: 1.8rem;
  }

  .app-download-subtitle {
    font-size: 0.95rem;
  }

  .app-download-buttons {
    gap: 12px;
  }

  .btn-download {
    flex: 1;
    padding: 12px 20px;
    font-size: 0.9rem;
  }

  .btn-download i {
    font-size: 1rem;
  }

  .phone-mockup {
    width: 220px;
    height: 440px;
  }

  .mobile-search-bar {
    margin: 8px;
    padding: 6px 10px;
  }

  .event-image-placeholder {
    height: 100px;
    font-size: 0.65rem;
  }

  .event-title-featured {
    font-size: 0.8rem;
  }

  .feature-item {
    font-size: 0.85rem;
  }

  .feature-item i {
    font-size: 1rem;
    width: 35px;
    height: 35px;
    padding: 8px;
  }
}

@media (max-width: 480px) {
  .app-download-section {
    padding: 30px 12px;
  }

  .app-download-title {
    font-size: 1.5rem;
  }

  .app-download-subtitle {
    font-size: 0.9rem;
    margin-bottom: 20px;
  }

  .app-download-features {
    margin-bottom: 25px;
    gap: 12px;
  }

  .app-download-buttons {
    flex-direction: column;
  }

  .btn-download {
    width: 100%;
  }

  .phone-mockup {
    width: 200px;
    height: 240px;
  }

  .header-brand {
    font-size: 1rem;
  }

  .mobile-search-bar {
    margin: 6px;
    padding: 5px 8px;
  }

  .mobile-search-bar input {
    font-size: 0.75rem;
  }

  .cat-btn {
    padding: 6px 10px;
    font-size: 0.65rem;
  }

  .event-image-placeholder {
    height: 80px;
    font-size: 0.6rem;
  }

  .event-title-featured {
    font-size: 0.75rem;
  }

  .event-label {
    font-size: 0.6rem;
  }

  .feature-item {
    font-size: 0.8rem;
  }
}
</style>

<template>
  <div class="d-flex flex-column min-vh-100 main-container">
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
              placeholder="Busca evento..." 
            />
          </div>

          <!-- Búsqueda por ciudad -->
          <div class="location-box-inline">
            <i class="bi bi-geo-alt"></i>
            <input 
              v-model="locationFilter" 
              type="text" 
              class="search-input" 
              placeholder="Ciudad..." 
            />
          </div>

          <!-- Separador -->
          <div class="filter-separator"></div>

          <!-- Categorías -->
          <button 
            @click="activeCategory = 'all'" 
            :class="['category-btn', { active: activeCategory === 'all' }]"
            title="Todos los eventos"
          >
            <i class="bi bi-music-note-beamed"></i>
            Todos
          </button>
          <button 
            @click="activeCategory = 'concierto'" 
            :class="['category-btn', { active: activeCategory === 'concierto' }]"
            title="Filtrar conciertos"
          >
            <i class="bi bi-mic"></i>
            Conciertos
          </button>
          <button 
            @click="activeCategory = 'festival'" 
            :class="['category-btn', { active: activeCategory === 'festival' }]"
            title="Filtrar festivales"
          >
            <i class="bi bi-people"></i>
            Festivales
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
              placeholder="Busca evento..." 
            />
          </div>
        </div>

        <div class="filters-mobile-categories">
          <!-- Categorías -->
          <button 
            @click="activeCategory = 'all'" 
            :class="['category-btn', { active: activeCategory === 'all' }]"
            title="Todos los eventos"
          >
            <i class="bi bi-music-note-beamed"></i>
            Todos
          </button>
          <button 
            @click="activeCategory = 'concierto'" 
            :class="['category-btn', { active: activeCategory === 'concierto' }]"
            title="Filtrar conciertos"
          >
            <i class="bi bi-mic"></i>
            Conciertos
          </button>
          <button 
            @click="activeCategory = 'festival'" 
            :class="['category-btn', { active: activeCategory === 'festival' }]"
            title="Filtrar festivales"
          >
            <i class="bi bi-people"></i>
            Festivales
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
            v-for="event in filteredEvents"
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

        <!-- Mostrar resultados -->
        <div v-if="filteredEvents.length && !loading" class="results-info">
          <p>Se muestran <strong>{{ filteredEvents.length }}</strong> de <strong>{{ events.length }}</strong> eventos</p>
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

useHead({
  title: 'Entradas para tus eventos | GoLive'
})

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase 

const events = ref([])
const loading = ref(true)
const error = ref('')
const searchQuery = ref('')
const locationFilter = ref('')
const activeCategory = ref('all')

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

    console.log('✅ Eventos cargados:', events.value.length)
  } catch (err) {
    console.error('❌ Error cargando eventos:', err)
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

// Filtrar por búsqueda desde el header (compatibilidad)
const filtrarEventos = (texto) => {
  if (!texto) {
    searchQuery.value = ''
  } else {
    searchQuery.value = texto
  }
}

// Cargar eventos al montar
onMounted(() => loadEvents())

// Recargar eventos si se vuelve visible la pestaña
if (process.client) {
  onMounted(() => {
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden) loadEvents()
    })
  })
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

/* ============ Hero Section ============ */
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
}
</style>

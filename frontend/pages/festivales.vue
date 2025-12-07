<template>
  <div class="d-flex flex-column min-vh-100 main-container">
    <!-- Header -->
    <client-only>
      <Header @buscar-evento="filtrarEventos" />
    </client-only>

    <!-- Loading State - Toda la página -->
    <div v-if="loading" class="loading-fullscreen">
      <div class="spinner-border" role="status"></div>
      <p class="mt-3">{{ $t('LoadingEvents') }}</p>
    </div>

    <!-- Content - Solo muestra cuando termina de cargar -->
    <template v-else>
      <!-- Hero Carousel Section -->
      <div v-if="festivals.length" class="hero-carousel position-relative">
        <div class="carousel-container">
          <!-- Carrusel de festivales -->
          <div class="carousel-track" :style="{ transform: `translateX(-${currentCarouselIndex * 100}%)` }">
            <div v-for="(festival, index) in carouselFestivals" :key="index" class="carousel-slide">
              <div class="slide-background">
                <img v-if="festival.image" :src="festival.image" :alt="festival.title" />
                <img v-else src="/assets/img/1.jpg" alt="Festivales" />
              </div>
              <div class="slide-overlay"></div>
              <div class="slide-content">
                <div class="slide-info">
                  <h2 class="slide-title">{{ festival.title }}</h2>
                  <p class="slide-artist" v-if="festival.organizer">{{ festival.organizer }}</p>
                  <div class="slide-details">
                    <span class="detail-item">
                      <i class="bi bi-geo-alt-fill"></i>
                      {{ festival.location || festival.venue }}
                    </span>
                    <span class="detail-item">
                      <i class="bi bi-calendar-event-fill"></i>
                      {{ formatFestivalDate(festival.date || festival.eventDate) }}
                    </span>
                  </div>
                  <button class="btn-see-more" @click="navigateToFestival(festival)">
                    {{ $t('Ver más') }} <i class="bi bi-arrow-right"></i>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Controles -->
          <button class="carousel-control carousel-prev" @click="prevSlide" :disabled="!canPrevSlide">
            <i class="bi bi-chevron-left"></i>
          </button>
          <button class="carousel-control carousel-next" @click="nextSlide" :disabled="!canNextSlide">
            <i class="bi bi-chevron-right"></i>
          </button>

          <!-- Sin indicadores visuales, solo controles de navegación -->
        </div>
      </div>

      <!-- Hero estático de fallback (si no hay festivales) -->
      <div v-else class="hero position-relative text-center d-flex align-items-center justify-content-center">
        <img src="/assets/img/1.jpg" alt="Festivales" class="hero-img w-100" />
        <div class="hero-text position-absolute">
          <h1 class="display-3 fw-bold">{{ $t('Festivales') }}</h1>
          <p class="lead">{{ $t('Descubre los mejores festivales') }}</p>
        </div>
      </div>

      <!-- Festivales Section -->
      <div class="events-section">
      <div class="events-wrapper">
        <!-- Filtros en línea horizontal -->
        <div class="filters-horizontal">
          <!-- Búsqueda avanzada con autocompletado -->
          <div class="advanced-search-container">
            <AdvancedSearch
              v-model="searchQuery"
              :events="festivals"
              :active-category="'festival'"
              :placeholder="$t('Busca festivales, ciudades, venues...')"
              @select-event="handleEventSelect"
              @select-location="handleLocationSelect"
              @select-venue="handleVenueSelect"
            />
          </div>

          <!-- Separador -->
          <div class="filter-separator"></div>

          <!-- Rango de precio con slider -->
          <div class="price-range-filter">
            <div class="price-filter-wrapper">
              <div class="price-filter-header">
                <i class="bi bi-tag-fill"></i>
                <label class="price-filter-label">Rango de precio</label>
              </div>
              <div class="price-slider-container">
                <div class="price-track-bg"></div>
                <input 
                  v-model.number="maxPrice" 
                  type="range" 
                  :min="minPriceRange" 
                  :max="maxPriceAvailable" 
                  step="10"
                  class="price-slider"
                />
                <span class="price-label">{{ minPriceRange }}€ - {{ maxPrice }}€</span>
              </div>
            </div>
          </div>

          <!-- Filtro de fechas con calendario -->
          <div class="calendar-filter">
            <button 
              class="calendar-btn"
              @click="showCalendar = !showCalendar"
              :class="{ 'active': showCalendar }"
            >
              <span class="calendar-btn-content">
                <i class="bi bi-calendar-event-fill"></i>
                <span class="calendar-btn-text">{{ calendarLabel }}</span>
              </span>
              <i class="bi bi-chevron-down calendar-chevron"></i>
            </button>
            
            <!-- Calendario -->
            <div v-if="showCalendar" class="calendar-dropdown">
              <!-- Header del mes -->
              <div class="calendar-header">
                <button class="month-nav" @click="prevMonth" title="Mes anterior">
                  <i class="bi bi-chevron-left"></i>
                </button>
                <h3 class="month-title">{{ monthName }} {{ currentMonth.getFullYear() }}</h3>
                <button class="month-nav" @click="nextMonth" title="Mes siguiente">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>

              <!-- Días de la semana -->
              <div class="weekdays">
                <div class="weekday">lun</div>
                <div class="weekday">mar</div>
                <div class="weekday">mié</div>
                <div class="weekday">jue</div>
                <div class="weekday">vie</div>
                <div class="weekday">sáb</div>
                <div class="weekday">dom</div>
              </div>

                <!-- Días del mes -->
                <div class="calendar-days">
                  <!-- Días vacíos al inicio del mes -->
                  <div 
                    v-for="_ in (getFirstDayOfMonth() === 0 ? 6 : getFirstDayOfMonth() - 1)"
                    :key="'empty-' + _"
                    class="day empty"
                  ></div>

                  <!-- Días del mes -->
                  <button
                    v-for="day in getDaysInMonth()"
                    :key="day"
                    :class="[
                      'day',
                      { 
                        'has-festival': daysWithFestivals.has(day),
                        'selected': selectedDate && selectedDate.getDate() === day && 
                                   selectedDate.getMonth() === currentMonth.getMonth() &&
                                   selectedDate.getFullYear() === currentMonth.getFullYear(),
                        'today': isToday(day)
                      }
                    ]"
                    @click="selectDay(day)"
                  >
                    {{ day }}
                  </button>
                </div>              <!-- Botón para limpiar fecha -->
              <button 
                v-if="selectedDate"
                class="clear-date-btn"
                @click="selectedDate = null; showCalendar = false"
              >
                <i class="bi bi-x"></i> Limpiar fecha
              </button>
            </div>
          </div>
        </div>

        <!-- Filtros para dispositivos pequeños -->
        <div class="filters-mobile-search">
          <!-- Búsqueda avanzada con autocompletado -->
          <AdvancedSearch
            v-model="searchQuery"
            :events="festivals"
            :active-category="'festival'"
            :placeholder="$t('Busca festivales, ciudades, venues...')"
            @select-event="handleEventSelect"
            @select-location="handleLocationSelect"
            @select-venue="handleVenueSelect"
          />
        </div>

        <div class="filters-mobile-categories">
          <button 
            @click="showPriceMobile = !showPriceMobile" 
            :class="['category-btn', { active: showPriceMobile }]"
            :title="$t('Económicos')"
          >
            <i class="bi bi-coin"></i>
            {{ $t('Económicos') }}
          </button>

          <button 
            @click="showCalendarMobile = true" 
            class="category-btn"
            :title="'Calendario'"
          >
            <i class="bi bi-calendar-event-fill"></i>
            Calendario
          </button>
        </div>

        <!-- Barra de precio móvil -->
        <div v-if="showPriceMobile" class="price-mobile-wrapper">
          <div class="price-filter-wrapper-mobile">
            <div class="price-filter-header">
              <i class="bi bi-tag-fill"></i>
              <label class="price-filter-label">Rango de precio</label>
            </div>
            <div class="price-slider-container">
              <div class="price-track-bg"></div>
              <input 
                v-model.number="maxPrice" 
                type="range" 
                :min="minPriceRange" 
                :max="maxPriceAvailable" 
                step="10"
                class="price-slider"
              />
              <span class="price-label">{{ minPriceRange }}€ - {{ maxPrice }}€</span>
            </div>
          </div>
        </div>

        <!-- Modal Calendario Móvil -->
        <div v-if="showCalendarMobile" class="calendar-mobile-modal">
          <div class="calendar-mobile-overlay" @click="showCalendarMobile = false"></div>
          <div class="calendar-mobile-container">
            <div class="calendar-mobile-header">
              <h3>Selecciona una fecha</h3>
              <button class="calendar-mobile-close" @click="showCalendarMobile = false">
                <i class="bi bi-x"></i>
              </button>
            </div>

            <!-- Calendario dentro del modal -->
            <div class="calendar-mobile-content">
              <!-- Header del mes -->
              <div class="calendar-header">
                <button class="month-nav" @click="prevMonth" title="Mes anterior">
                  <i class="bi bi-chevron-left"></i>
                </button>
                <h3 class="month-title">{{ monthName }} {{ currentMonth.getFullYear() }}</h3>
                <button class="month-nav" @click="nextMonth" title="Mes siguiente">
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>

              <!-- Días de la semana -->
              <div class="weekdays">
                <div class="weekday">lun</div>
                <div class="weekday">mar</div>
                <div class="weekday">mié</div>
                <div class="weekday">jue</div>
                <div class="weekday">vie</div>
                <div class="weekday">sáb</div>
                <div class="weekday">dom</div>
              </div>

              <!-- Días del mes -->
              <div class="calendar-days">
                <!-- Días vacíos al inicio del mes -->
                <div 
                  v-for="_ in (getFirstDayOfMonth() === 0 ? 6 : getFirstDayOfMonth() - 1)"
                  :key="'empty-' + _"
                  class="day empty"
                ></div>

                <!-- Días del mes -->
                <button
                  v-for="day in getDaysInMonth()"
                  :key="day"
                  :class="[
                    'day',
                    { 
                      'has-festival': daysWithFestivals.has(day),
                      'selected': selectedDate && selectedDate.getDate() === day && 
                                 selectedDate.getMonth() === currentMonth.getMonth() &&
                                 selectedDate.getFullYear() === currentMonth.getFullYear(),
                      'today': isToday(day)
                    }
                  ]"
                  @click="selectDay(day); showCalendarMobile = false"
                >
                  {{ day }}
                </button>
              </div>

              <!-- Botón para limpiar fecha -->
              <button 
                v-if="selectedDate"
                class="clear-date-btn"
                @click="selectedDate = null; showCalendarMobile = false"
              >
                <i class="bi bi-x"></i> Limpiar fecha
              </button>
            </div>
          </div>
        </div>

        <!-- Spinner mientras carga -->
        <!-- Ahora el loading es fullscreen, no aparece aquí -->

        <!-- Error al cargar - mostrar después de cargar -->
        <div v-if="error" class="error-state">
          <i class="bi bi-exclamation-circle"></i>
          <p>{{ error }}</p>
          <button class="btn-retry" @click="loadFestivals">
            <i class="bi bi-arrow-clockwise"></i> {{ $t('Reintentar') }}
          </button>
        </div>

        <!-- Festivales filtrados -->
        <div v-else-if="filteredFestivals.length" class="events-grid">
          <div
            v-for="festival in filteredFestivals"
            :key="festival._id || festival.id"
            class="event-card-wrapper"
          >
            <EventCard :event="festival" />
          </div>
        </div>

        <!-- Mensaje si no hay festivales -->
        <div v-else class="empty-state">
          <i class="bi bi-calendar-event"></i>
          <h3>{{ $t('No hay festivales disponibles') }}</h3>
          <p>{{ $t('Intenta cambiar tus filtros de búsqueda') }}</p>
          <button class="btn-reset" @click="resetFilters">
            <i class="bi bi-x-circle"></i> {{ $t('Limpiar filtros') }}
          </button>
        </div>

        <!-- Mostrar resultados -->
        <div v-if="filteredFestivals.length && !loading" class="results-info">
          <p>{{ $t('Se muestran') }} <strong>{{ filteredFestivals.length }}</strong> {{ $t('de') }} <strong>{{ festivals.length }}</strong> {{ $t('festivales') }}</p>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <Footer />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useHead } from '#app'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import EventCard from '~/components/EventCard.vue'
import AdvancedSearch from '~/components/AdvancedSearch.vue'
import { formatDateISO } from '~/utils/formatDate'

useHead({
  title: 'Festivales | GoLive',
  meta: [
    { name: 'description', content: 'Descubre y compra entradas para los mejores festivales en GoLive. Vive la experiencia de la música en vivo y los eventos más grandes.' },
    { property: 'og:title', content: 'Festivales | GoLive' },
    { property: 'og:description', content: 'Descubre y compra entradas para los mejores festivales en GoLive. Vive la experiencia de la música en vivo y los eventos más grandes.' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: 'https://golive-hu5d.onrender.com/festivales' },
    { property: 'og:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'Festivales | GoLive' },
    { name: 'twitter:description', content: 'Descubre y compra entradas para los mejores festivales en GoLive.' },
    { name: 'twitter:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' }
  ],
  link: [
    { rel: 'canonical', href: 'https://golive-hu5d.onrender.com/festivales' }
  ]
})

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase
const router = useRouter()
const { t, locale } = useI18n()

// Estado
const festivals = ref([])
const loading = ref(true)
const error = ref('')
const searchQuery = ref('')
const locationFilter = ref('')
const minPriceRange = ref(0)
const maxPrice = ref(500)
const maxPriceAvailable = ref(500)
const selectedDate = ref(null)
const showCalendar = ref(false)
const showPriceMobile = ref(false)
const showCalendarMobile = ref(false)
const currentMonth = ref(new Date())
const currentCarouselIndex = ref(0)
const carouselAutoplayInterval = ref(null)
const touchStartX = ref(0)
const touchEndX = ref(0)

// Opciones de fechas para dropdown antiguo (eliminado)
const dateOptions = [
  { value: '', label: 'Todas las fechas' },
  { value: 'week', label: 'Esta semana' },
  { value: 'month', label: 'Este mes' },
  { value: 'soon', label: 'Próximos 3 meses' }
]

// Obtener días con festivales en el mes actual
const daysWithFestivals = computed(() => {
  const days = new Set()
  festivals.value.forEach(festival => {
    const festivalDate = new Date(festival.date || festival.eventDate)
    if (festivalDate.getMonth() === currentMonth.value.getMonth() &&
        festivalDate.getFullYear() === currentMonth.value.getFullYear()) {
      days.add(festivalDate.getDate())
    }
  })
  return days
})

// Carrusel: obtener solo los primeros 6 festivales para mostrar
const carouselFestivals = computed(() => {
  return festivals.value.slice(0, 6)
})

// Verificar si se puede ir al siguiente slide
const canNextSlide = computed(() => {
  return currentCarouselIndex.value < carouselFestivals.value.length - 1
})

// Verificar si se puede ir al slide anterior
const canPrevSlide = computed(() => {
  return currentCarouselIndex.value > 0
})

// Obtener el nombre del mes actual
const monthName = computed(() => {
  const months = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']
  return months[currentMonth.value.getMonth()]
})

// Label del calendario
const calendarLabel = computed(() => {
  if (!selectedDate.value) return 'Selecciona fecha'
  return selectedDate.value.toLocaleDateString('es-ES', { 
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
})

// Cambiar mes anterior
const prevMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() - 1)
}

// Cambiar mes siguiente
const nextMonth = () => {
  currentMonth.value = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1)
}

// Seleccionar día
const selectDay = (day) => {
  const date = new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), day)
  selectedDate.value = date
  showCalendar.value = false
}

// Obtener días del mes
const getDaysInMonth = () => {
  return new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth() + 1, 0).getDate()
}

// Obtener primer día del mes (0 = domingo, 1 = lunes, etc)
const getFirstDayOfMonth = () => {
  return new Date(currentMonth.value.getFullYear(), currentMonth.value.getMonth(), 1).getDay()
}

// ============ Métodos del Carrusel ============

// Ir al siguiente slide
const nextSlide = () => {
  if (canNextSlide.value) {
    currentCarouselIndex.value++
    resetCarouselAutoplay()
  }
}

// Ir al slide anterior
const prevSlide = () => {
  if (canPrevSlide.value) {
    currentCarouselIndex.value--
    resetCarouselAutoplay()
  }
}

// Resetear autoplay del carrusel
const resetCarouselAutoplay = () => {
  if (carouselAutoplayInterval.value) {
    clearInterval(carouselAutoplayInterval.value)
  }
  startCarouselAutoplay()
}

const handleSwipeStart = (e) => {
  touchStartX.value = e.touches[0].clientX
}

const handleSwipeEnd = (e) => {
  touchEndX.value = e.changedTouches[0].clientX
  const swipeThreshold = 50 // Mínimo de píxeles para considerar un swipe
  const difference = touchStartX.value - touchEndX.value

  if (Math.abs(difference) > swipeThreshold) {
    if (difference > 0) {
      // Swipe izquierda = siguiente slide
      nextSlide()
    } else {
      // Swipe derecha = slide anterior
      prevSlide()
    }
  }
}

// Iniciar autoplay del carrusel
const startCarouselAutoplay = () => {
  carouselAutoplayInterval.value = setInterval(() => {
    if (currentCarouselIndex.value < carouselFestivals.value.length - 1) {
      currentCarouselIndex.value++
    } else {
      currentCarouselIndex.value = 0
    }
  }, 6000) // Cambiar cada 6 segundos
}

// Formatear fecha de festival
const formatFestivalDate = (date) => {
  if (!date) return 'Fecha no disponible'
  const d = new Date(date)
  return d.toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}

// Obtener precio mínimo del concierto
const getMinPrice = (concert) => {
  if (concert.zones?.length) {
    const prices = concert.zones.map(z => z.price)
    return Math.min(...prices)
  }
  return concert.price || 0
}

// Verificar si un día es hoy
const isToday = (day) => {
  const today = new Date()
  return day === today.getDate() &&
         currentMonth.value.getMonth() === today.getMonth() &&
         currentMonth.value.getFullYear() === today.getFullYear()
}

// Cargar festivales desde el backend
const loadFestivals = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const response = await $fetch(`${API_BASE}/events`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      timeout: 10000
    })

    // Filtrar solo festivales
    festivals.value = Array.isArray(response) 
      ? response.filter(event => event.category?.toLowerCase() === 'festival')
      : []

    // Calcular precio mínimo y máximo
    if (festivals.value.length > 0) {
      const allPrices = festivals.value.flatMap(f => 
        f.zones?.map(z => z.price) || [f.price || 0]
      )
      const minPrice = Math.min(...allPrices)
      const maxPrice_value = Math.max(...allPrices)
      minPriceRange.value = minPrice
      maxPrice.value = maxPrice_value // Establecer el máximo al precio más alto
      maxPriceAvailable.value = maxPrice_value // Establecer el máximo disponible
    }

    // Ordenar por fecha
    festivals.value.sort((a, b) => {
      const dateA = new Date(a.date || a.eventDate || '9999-12-31')
      const dateB = new Date(b.date || b.eventDate || '9999-12-31')
      return dateA - dateB
    })

    console.log('✅ Festivales cargados:', festivals.value.length)
  } catch (err) {
    console.error('❌ Error cargando festivales:', err)
    error.value = t('ErrorLoadingEvents') || 'Error al cargar los festivales. Intenta nuevamente.'
  } finally {
    loading.value = false
  }
}

// Función para filtrar por rango de precio
const filterByPrice = (festival) => {
  const minPrice = festival.zones?.length 
    ? Math.min(...festival.zones.map(z => z.price))
    : festival.price || 0

  return minPrice >= minPriceRange.value && minPrice <= maxPrice.value
}

// Función para filtrar por rango de fechas
const filterByDate = (festival) => {
  if (!selectedDate.value) return true
  
  const festivalDate = new Date(festival.date || festival.eventDate)
  const selected = new Date(selectedDate.value)
  
  // Comparar solo año, mes y día
  return festivalDate.toDateString() === selected.toDateString()
}

// Festivales filtrados (mejorado para trabajar con búsqueda avanzada)
const filteredFestivals = computed(() => {
  return festivals.value.filter((festival) => {
    // Filtro de búsqueda mejorado (busca en título, descripción, venue, location, artista y categoría)
    if (searchQuery.value.trim()) {
      const query = searchQuery.value.toLowerCase().trim()
      const titleMatch = festival.title?.toLowerCase().includes(query) || false
      const artistMatch = festival.artist?.toLowerCase().includes(query) || false
      const descriptionMatch = festival.description?.toLowerCase().includes(query) || false
      const venueMatch = festival.venue?.toLowerCase().includes(query) || false
      const locationMatch = festival.location?.toLowerCase().includes(query) || false
      const categoryMatch = festival.category?.toLowerCase().includes(query) || false
      
      if (!titleMatch && !artistMatch && !descriptionMatch && !venueMatch && !locationMatch && !categoryMatch) {
        return false
      }
    }

    // Filtro de ubicación/ciudad (solo si locationFilter está activo y es diferente de searchQuery)
    if (locationFilter.value.trim() && locationFilter.value !== searchQuery.value) {
      const location = locationFilter.value.toLowerCase()
      const eventLocation = festival.location?.toLowerCase() || ''
      const eventVenue = festival.venue?.toLowerCase() || ''
      if (!eventLocation.includes(location) && !eventVenue.includes(location)) return false
    }

    // Filtro de precio
    if (!filterByPrice(festival)) return false

    // Filtro de fecha
    if (!filterByDate(festival)) return false

    return true
  })
})



// Limpiar filtros
const resetFilters = () => {
  searchQuery.value = ''
  locationFilter.value = ''
  maxPrice.value = minPriceRange.value + 200
  selectedDate.value = null
  showCalendar.value = false
  currentMonth.value = new Date()
}

// Filtrar desde el header
const filtrarEventos = (texto) => {
  searchQuery.value = texto || ''
}

// Handlers para el autocompletado avanzado
const handleEventSelect = (event) => {
  // Navegar directamente al evento seleccionado
  const slug = event.title
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
  router.push(`/carrito/${slug}`)
}

const handleLocationSelect = (location) => {
  // Aplicar el filtro de ubicación
  locationFilter.value = location
  searchQuery.value = location
}

const handleVenueSelect = (venue) => {
  // Aplicar el filtro de venue
  locationFilter.value = venue
  searchQuery.value = venue
}

// Navegar al carrito
const navigateToFestival = (festival) => {
  const slug = festival.title
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
  router.push(`/carrito/${slug}`)
}

// Debug: watch showCalendar
watch(showCalendar, (newVal) => {
  console.log('showCalendar changed to:', newVal)
  // Calendar visibility - desktop calendar used on all platforms
})

// Cargar festivales al montar
onMounted(() => {
  loadFestivals()
  
  // Iniciar autoplay del carrusel
  startCarouselAutoplay()
  
  // Event listeners para swipe
  const carouselElement = document.querySelector('.hero-carousel')
  if (carouselElement) {
    carouselElement.addEventListener('touchstart', handleSwipeStart)
    carouselElement.addEventListener('touchend', handleSwipeEnd)
  }
  
  // Cerrar calendario al hacer click fuera
  const handleClickOutside = (event) => {
    const calendarFilter = document.querySelector('.calendar-filter')
    const calendarButton = event.target.closest('.calendar-toggle-btn')
    
    // No cerrar si es el botón de toggle
    if (calendarButton) return
    
    // No cerrar si el click está dentro del calendario
    if (calendarFilter && calendarFilter.contains(event.target)) return
    
    // Cerrar si está fuera
    showCalendar.value = false
  }
  
  document.addEventListener('click', handleClickOutside)
  
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
    // Limpiar autoplay del carrusel
    if (carouselAutoplayInterval.value) {
      clearInterval(carouselAutoplayInterval.value)
    }
    // Limpiar event listeners de swipe
    const carouselElement = document.querySelector('.hero-carousel')
    if (carouselElement) {
      carouselElement.removeEventListener('touchstart', handleSwipeStart)
      carouselElement.removeEventListener('touchend', handleSwipeEnd)
    }
  })
})
</script>

<style scoped>
/* ============ Global Reset ============ */
:deep(html),
:deep(body) {
  overflow-x: hidden !important;
  max-width: 100vw !important;
  width: 100vw !important;
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

  client-only + template > .hero-carousel,
  client-only + template > .hero {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}

.main-container {
  width: 100%;
  max-width: 100vw;
  overflow-x: hidden;
  margin: 0;
  padding: 0;
}

/* Solo en móviles: eliminar espacio superior */
@media (max-width: 768px) {
  .main-container {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }

  .main-container > .hero-carousel,
  .main-container > .hero {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}

/* ============ Hero Carousel Section ============ */
.hero-carousel {
  height: 60vh;
  position: relative;
  overflow: hidden;
  background: #000000;
}

/* Solo en móviles: eliminar espacio superior */
@media (max-width: 768px) {
  .hero-carousel {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}

.carousel-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.carousel-track {
  display: flex;
  height: 100%;
  width: 100%;
  transition: transform 0.7s cubic-bezier(0.4, 0, 0.2, 1);
}

.carousel-slide {
  flex: 0 0 100%;
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slide-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.slide-background img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1);
  animation: zoomIn 0.7s ease-out forwards;
}

@keyframes zoomIn {
  from {
    transform: scale(1.05);
    opacity: 0.8;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.slide-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.4) 0%, rgba(8, 0, 35, 0.6) 50%, rgba(160, 58, 20, 0.3) 100%);
  z-index: 1;
}

.slide-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: flex-start;
  padding: 60px 80px;
  z-index: 2;
}

.slide-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 600px;
  animation: slideUp 0.7s cubic-bezier(0.4, 0, 0.2, 1) 0.2s both;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-category {
  display: inline-block;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  padding: 6px 14px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  width: fit-content;
  box-shadow: 0 4px 15px rgba(255, 0, 87, 0.4);
}

.slide-title {
  font-family: 'Poppins', sans-serif;
  font-size: 3rem;
  font-weight: 900;
  color: #ffffff;
  margin: 0;
  line-height: 1.1;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.5);
  letter-spacing: -1px;
}

.slide-artist {
  font-family: 'Poppins', sans-serif;
  font-size: 1.3rem;
  color: #ffb8c5;
  margin: 0;
  font-weight: 500;
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.5);
}

.slide-details {
  display: flex;
  gap: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #e5e7eb;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 500;
}

.detail-item i {
  color: #ff0057;
  font-size: 1.1rem;
}

.detail-item.price {
  background: rgba(255, 0, 87, 0.2);
  padding: 8px 12px;
  border-radius: 6px;
  border: 2px solid #ff0057;
  color: #ffa0b3;
  font-weight: 700;
}

.btn-see-more {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(255, 0, 87, 0.4);
  width: fit-content;
}

.btn-see-more:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(255, 0, 87, 0.6);
}

.btn-see-more:active {
  transform: translateY(-1px);
}

.btn-see-more i {
  transition: transform 0.3s ease;
}

.btn-see-more:hover i {
  transform: translateX(3px);
}

/* ============ Carousel Controls ============ */
.carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(255, 0, 87, 0.2);
  border: 2px solid #ff0057;
  color: #ffffff;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
  outline: none;
}

.carousel-control:hover:not(:disabled) {
  background: #ff0057;
  box-shadow: 0 8px 24px rgba(255, 0, 87, 0.5);
  transform: translateY(-50%) scale(1.1);
}

.carousel-control:active:not(:disabled) {
  transform: translateY(-50%) scale(0.95);
}

.carousel-control:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.carousel-prev {
  left: 30px;
}

.carousel-next {
  right: 30px;
}

/* ============ Loading Fullscreen ============ */
.loading-fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #000000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(5px);
}

.loading-fullscreen .spinner-border {
  width: 60px;
  height: 60px;
  border-width: 4px;
  color: rgba(255, 255, 255, 0.2);
  border-right-color: #ff0057;
  animation: spin 0.8s linear infinite;
}

.loading-fullscreen p {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  color: #ffffff;
  margin-top: 20px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ============ Hero Section (Fallback) ============ */
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

.advanced-search-container {
  flex: 1;
  min-width: 300px;
  max-width: 600px;
}

.filters-mobile-search {
  display: none;
  width: 100%;
  box-sizing: border-box;
  background: #0a0a0a;
  padding: 12px 8px;
  margin: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
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
.location-box-inline,
.calendar-filter {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #ff0057;
  border-radius: 8px;
  padding: 0 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 200px;
  flex-shrink: 0;
  height: 50px;
}

.price-range-filter {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 0, 87, 0.3);
  border-radius: 10px;
  padding: 0 12px;
  height: 50px;
  flex-shrink: 0;
  transition: all 0.3s ease;
  min-width: 280px;
}

.price-range-filter:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 0, 87, 0.5);
}

.search-box-inline:focus-within,
.location-box-inline:focus-within,
.calendar-filter:focus-within {
  border-color: #ff6b35;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

.search-box-inline i,
.location-box-inline i,
.price-range-filter i,
.calendar-filter i {
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
  cursor: pointer;
}

.search-input::placeholder {
  color: #b0b0b0;
}

/* ============ Price Slider ============ */
.price-slider-container {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.price-slider {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(to right, #ff0057 0%, #ff6b35 100%);
  outline: none;
  -webkit-appearance: none;
  appearance: none;
  cursor: pointer;
}

/* Wrapper del filtro de precio */
.price-filter-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  height: 100%;
}

/* Header del filtro de precio */
.price-filter-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  flex-shrink: 0;
}

.price-filter-header i {
  color: #ff0057;
  font-size: 1rem;
}

.price-filter-label {
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* Contenedor del slider */
.price-slider-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  height: 100%;
}

/* Fondo de la pista */
.price-track-bg {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 4px;
  transform: translateY(-50%);
  background: linear-gradient(90deg, rgba(255, 0, 87, 0.15) 0%, rgba(255, 107, 53, 0.15) 100%);
  border-radius: 2px;
  pointer-events: none;
}

/* Slider input */
.price-slider {
  -webkit-appearance: none;
  appearance: none;
  width: 100%;
  height: 4px;
  border-radius: 2px;
  background: transparent;
  outline: none;
  cursor: pointer;
  position: relative;
  z-index: 5;
  flex: 1;
  min-width: 80px;
}

/* Estilo del thumb (deslizador) para Chrome/Safari */
.price-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  cursor: pointer;
  border: 2px solid #ffffff;
  box-shadow: 0 3px 10px rgba(255, 0, 87, 0.5);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.price-slider::-webkit-slider-thumb:hover {
  transform: scale(1.15);
  box-shadow: 0 5px 16px rgba(255, 0, 87, 0.7);
}

.price-slider::-webkit-slider-thumb:active {
  transform: scale(1.15);
}

/* Estilo del thumb para Firefox */
.price-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  cursor: pointer;
  border: 2px solid #ffffff;
  box-shadow: 0 3px 10px rgba(255, 0, 87, 0.5);
  transition: all 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.price-slider::-moz-range-thumb:hover {
  transform: scale(1.15);
  box-shadow: 0 5px 16px rgba(255, 0, 87, 0.7);
}

/* Track para Firefox */
.price-slider::-moz-range-track {
  background: transparent;
  border: none;
}

/* Label del precio */
.price-label {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  padding: 5px 10px;
  border-radius: 4px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  white-space: nowrap;
  text-align: center;
  box-shadow: 0 3px 10px rgba(255, 0, 87, 0.35);
  transition: all 0.2s ease;
  letter-spacing: 0.3px;
  flex-shrink: 0;
}

.price-slider-container:hover .price-label {
  box-shadow: 0 6px 16px rgba(255, 0, 87, 0.45);
  transform: translateY(-1px);
}

/* ============ Calendar Filter ============ */
.calendar-filter {
  position: relative !important;
  height: auto !important;
  padding: 0 !important;
  background: transparent !important;
  border: none !important;
  min-width: auto !important;
}

.calendar-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 0, 87, 0.3);
  border-radius: 10px;
  padding: 12px 16px;
  color: #e5e7eb;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  outline: none;
  white-space: nowrap;
}

.calendar-btn-content {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.calendar-btn-text {
  color: #ffffff;
  font-weight: 600;
}

.calendar-btn i:first-child {
  color: #ff0057;
  font-size: 1.1rem;
}

.calendar-chevron {
  color: #ff0057;
  font-size: 0.75rem;
  transition: transform 0.3s ease;
  margin-left: 4px;
}

.calendar-btn:hover {
  border-color: rgba(255, 0, 87, 0.6);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 4px 16px rgba(255, 0, 87, 0.2);
}

.calendar-btn.active {
  border-color: #ff0057;
  background: rgba(255, 0, 87, 0.1);
  box-shadow: 0 6px 20px rgba(255, 0, 87, 0.3);
}

.calendar-btn.active .calendar-chevron {
  transform: rotate(180deg);
}

/* ============ Calendar Dropdown ============ */
.calendar-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  border: 2px solid #ff0057;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(255, 0, 87, 0.3);
  z-index: 100;
  padding: 20px;
  min-width: 320px;
  animation: slideDown 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Header del calendario */
.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
  padding-bottom: 15px;
  border-bottom: 2px solid rgba(255, 0, 87, 0.2);
}

.calendar-header .month-nav {
  background: transparent;
  border: 2px solid #ff0057;
  color: #ff0057;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  font-size: 1.2rem;
  position: relative;
  overflow: hidden;
}

.calendar-header .month-nav::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.2s ease;
  border-radius: 12px;
}

.calendar-header .month-nav:hover {
  background: rgba(255, 0, 87, 0.12);
  box-shadow: 0 6px 16px rgba(255, 0, 87, 0.25);
  transform: translateY(-2px);
}

.calendar-header .month-nav:hover::before {
  opacity: 1;
}

.calendar-header .month-nav:active {
  transform: scale(0.95);
}

.month-title {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  flex: 1;
  text-align: center;
}

/* Días de la semana */
.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  color: #9ca3af;
  padding: 8px 0;
  text-transform: uppercase;
}

/* Días del calendario */
.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 16px;
}

.day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.2s ease;
  position: relative;
}

.day.empty {
  background: transparent;
}

.day:not(.empty) {
  cursor: pointer;
  background: rgba(255, 255, 255, 0.06);
  color: #d1d5db;
  border: 2px solid rgba(255, 255, 255, 0.08);
  transition: all 0.2s ease;
}

.day:not(.empty):hover {
  background: rgba(255, 0, 87, 0.2);
  border-color: rgba(255, 0, 87, 0.6);
  color: #ffd9e0;
  box-shadow: 0 6px 16px rgba(255, 0, 87, 0.25);
  transform: translateY(-2px) scale(1.08);
}

/* Día con festival */
.day.has-festival {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.25) 0%, rgba(255, 0, 87, 0.1) 100%);
  border: 2px solid #ff0057;
  font-weight: 700;
  color: #ffa0b3;
  box-shadow: 0 0 0 1px rgba(255, 0, 87, 0.3) inset, 0 0 8px rgba(255, 0, 87, 0.15);
}

.day.has-festival::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.15), transparent);
  pointer-events: none;
}

.day.has-festival::after {
  content: '';
  position: absolute;
  bottom: 3px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: #ff0057;
  border-radius: 50%;
  box-shadow: 0 0 4px rgba(255, 0, 87, 0.6);
}

.day.has-festival:hover {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.35) 0%, rgba(255, 0, 87, 0.15) 100%);
  box-shadow: 0 0 0 1px rgba(255, 0, 87, 0.4) inset, 0 8px 20px rgba(255, 0, 87, 0.35);
  transform: translateY(-3px) scale(1.08);
  border-color: #ff6b35;
  color: #ffc7d1;
}

/* Día de hoy (actual) */
.day.today {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.25) 0%, rgba(255, 107, 53, 0.1) 100%);
  border: 2px solid #ff6b35;
  color: #ffb8a0;
  font-weight: 700;
  box-shadow: 0 0 0 1px rgba(255, 107, 53, 0.4) inset, 0 0 8px rgba(255, 107, 53, 0.2);
  position: relative;
}

.day.today::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px;
  background: radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.25), transparent);
  pointer-events: none;
}

.day.today::after {
  content: '';
  position: absolute;
  bottom: 3px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  background: #ff6b35;
  border-radius: 50%;
}

.day.today:hover {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.35) 0%, rgba(255, 107, 53, 0.15) 100%);
  box-shadow: 
    0 0 0 1px rgba(255, 107, 53, 0.5) inset,
    0 6px 16px rgba(255, 107, 53, 0.3);
  transform: translateY(-3px) scale(1.08);
}

/* Día seleccionado */
.day.selected {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border-color: transparent;
  font-weight: 700;
  box-shadow: 0 8px 24px rgba(255, 0, 87, 0.4);
  transform: translateY(-2px) scale(1.02);
}

.day.selected::after {
  background: #ffffff;
}

/* Botón para limpiar fecha */
.clear-date-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 10px 16px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 6px;
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 0, 87, 0.3);
}

.clear-date-btn:hover {
  box-shadow: 0 6px 16px rgba(255, 0, 87, 0.4);
  transform: translateY(-2px);
}

.clear-date-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 0, 87, 0.3);
}

.clear-date-btn i {
  font-size: 0.95rem;
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
@media (max-width: 992px) {
  .events-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 18px;
  }

  .category-btn {
    padding: 8px 14px;
    font-size: 0.85rem;
  }

  /* Carousel responsivo */
  .slide-content {
    padding: 40px 50px;
  }

  .slide-title {
    font-size: 2.3rem;
  }

  .slide-artist {
    font-size: 1.1rem;
  }

  .slide-details {
    gap: 16px;
  }

  .detail-item {
    font-size: 0.85rem;
  }

  .carousel-control {
    width: 44px;
    height: 44px;
    font-size: 1.1rem;
  }

  .carousel-prev {
    left: 20px;
  }

  .carousel-next {
    right: 20px;
  }
}

@media (max-width: 768px) {
  .hero {
    height: 50vh;
  }

  .hero-carousel {
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
  .location-box-inline,
  .price-range-filter,
  .calendar-filter {
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

  /* Carousel responsive */
  .slide-content {
    padding: 30px 40px;
  }

  .slide-title {
    font-size: 1.8rem;
  }

  .slide-artist {
    font-size: 0.95rem;
  }

  .slide-details {
    gap: 12px;
    font-size: 0.8rem;
  }

  .detail-item {
    font-size: 0.75rem;
  }

  .btn-see-more {
    padding: 10px 24px;
    font-size: 0.85rem;
  }

  .carousel-control {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }

  .carousel-prev {
    left: 15px;
  }

  .carousel-next {
    right: 15px;
  }
}

@media (max-width: 576px) {
  .hero {
    height: 40vh;
    margin: 0;
    padding: 0;
  }

  .hero-carousel {
    height: 45vh;
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
    position: relative;
  }

  .filters-horizontal {
    display: none;
  }

  .filters-mobile-search {
    display: block;
  }

  .advanced-search-container {
    min-width: 100%;
    max-width: 100%;
  }

  .filters-mobile-categories {
    display: flex;
    background: #0a0a0a;
    padding: 10px 8px;
    margin: 0 0 0 0;
    box-sizing: border-box;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
    gap: 8px;
    justify-content: center;
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .location-box-inline,
  .price-range-filter,
  .calendar-filter {
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

  /* Carousel mobile */
  .slide-content {
    padding: 20px;
    align-items: flex-end;
    justify-content: flex-start;
  }

  .slide-info {
    max-width: 100%;
    gap: 10px;
  }

  .slide-title {
    font-size: 1.3rem;
    line-height: 1.2;
  }

  .slide-artist {
    font-size: 0.85rem;
  }

  .slide-category {
    font-size: 0.7rem;
    padding: 4px 10px;
  }

  .slide-details {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .detail-item {
    font-size: 0.7rem;
  }

  .btn-see-more {
    padding: 10px 16px;
    font-size: 0.8rem;
  }

  .carousel-control {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;
  }

  .carousel-prev {
    left: 10px;
  }

  .carousel-next {
    right: 10px;
  }

  .carousel-indicators {
    bottom: 15px;
    gap: 6px;
  }

  .indicator-dot {
    width: 8px;
    height: 8px;
  }
}

@media (max-width: 576px) {
  .hero {
    height: 40vh;
    margin: 0;
    padding: 0;
  }

  .hero-carousel {
    height: 35vh;
    margin-top: 0 !important;
    padding-top: 0 !important;
  }

  .carousel-prev,
  .carousel-next {
    display: none !important;
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
    position: relative;
  }

  .filters-horizontal {
    display: none;
  }

  .filters-mobile-search {
    display: block;
  }

  .advanced-search-container {
    min-width: 100%;
    max-width: 100%;
  }

  .filters-mobile-categories {
    display: flex;
    background: #0a0a0a;
    padding: 10px 8px;
    margin: 0 0 0 0;
    box-sizing: border-box;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
    gap: 8px;
    justify-content: center;
    flex-wrap: nowrap;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .location-box-inline,
  .price-range-filter,
  .calendar-filter {
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

  /* Carousel mobile */
  .slide-content {
    padding: 20px;
  }

  .slide-info {
    max-width: 100%;
    gap: 10px;
  }

  .slide-title {
    font-size: 1.3rem;
    line-height: 1.2;
  }

  .slide-artist {
    font-size: 0.85rem;
  }

  .slide-category {
    font-size: 0.7rem;
    padding: 4px 10px;
  }

  .slide-details {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }

  .detail-item {
    font-size: 0.7rem;
  }

  .btn-see-more {
    padding: 10px 16px;
    font-size: 0.8rem;
  }

  .carousel-control {
    width: 36px;
    height: 36px;
    font-size: 0.9rem;
  }

  .carousel-prev {
    left: 10px;
  }

  .carousel-next {
    right: 10px;
  }

  .carousel-indicator-bar {
    bottom: 15px;
    left: 40px;
    right: 40px;
    height: 2px;
    padding: 0 8px;
  }

  .carousel-counter {
    font-size: 0.7rem;
  }

  /* Estilos para precio y botón más pequeños en móvil */
  .detail-item {
    font-size: 0.65rem;
  }

  .detail-item.price {
    font-size: 0.6rem;
    padding: 5px 8px;
  }

  .btn-see-more {
    padding: 8px 12px;
    font-size: 0.65rem;
  }

  .price-mobile-wrapper {
    position: absolute;
    top: 130px;
    left: 8px;
    right: 8px;
    background: #0a0a0a;
    padding: 8px;
    margin: 0;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
    z-index: 1000;
    border-radius: 8px;
  }

  .price-filter-wrapper-mobile {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 6px 8px;
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 0, 87, 0.3);
    border-radius: 6px;
  }

  .price-filter-wrapper-mobile .price-filter-header {
    display: flex;
    align-items: center;
    gap: 4px;
    margin: 0;
  }

  .price-filter-wrapper-mobile .price-filter-header i {
    color: #ff0057;
    font-size: 0.85rem;
  }

  .price-filter-wrapper-mobile .price-filter-label {
    color: #ffffff;
    font-size: 0.65rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.2px;
  }

  .price-filter-wrapper-mobile .price-slider {
    height: 2px;
  }

  .price-filter-wrapper-mobile .price-label {
    padding: 2px 4px;
    font-size: 0.65rem;
  }

  /* ============ Calendar Mobile Modal ============ */
  .calendar-mobile-modal {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10000;
  }

  .calendar-mobile-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.7);
    animation: fadeIn 0.2s ease;
    z-index: 10000;
  }

  .calendar-mobile-container {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
    border-top: 2px solid #ff0057;
    border-radius: 20px 20px 0 0;
    z-index: 10001;
    max-height: 90vh;
    overflow-y: auto;
    animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }

  @keyframes slideUp {
    from {
      transform: translateY(100%);
      opacity: 0;
    }
    to {
      transform: translateY(0);
      opacity: 1;
    }
  }

  .calendar-mobile-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid rgba(255, 0, 87, 0.2);
    gap: 12px;
  }

  .calendar-mobile-header h3 {
    font-family: 'Poppins', sans-serif;
    font-size: 1.1rem;
    font-weight: 700;
    color: #ffffff;
    margin: 0;
    flex: 1;
  }

  .calendar-mobile-close {
    background: transparent;
    border: none;
    color: #ff0057;
    font-size: 1.5rem;
    cursor: pointer;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    transition: all 0.2s ease;
  }

  .calendar-mobile-close:hover {
    background: rgba(255, 0, 87, 0.2);
  }

  .calendar-mobile-content {
    padding: 16px;
  }

  .calendar-mobile-content .calendar-header {
    margin-bottom: 16px;
  }

  .calendar-mobile-content .clear-date-btn {
    margin-top: 12px;
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


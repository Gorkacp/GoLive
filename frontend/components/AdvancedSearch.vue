<template>
  <div class="advanced-search-wrapper" ref="searchWrapperRef">
    <div class="search-container" :class="{ 'focused': isFocused, 'has-results': showSuggestions && suggestions.length > 0 }">
      <div class="search-input-wrapper">
        <i class="bi bi-search search-icon"></i>
        <input
          ref="searchInputRef"
          v-model="searchQuery"
          type="text"
          class="search-input-advanced"
          :placeholder="computedPlaceholder"
          @focus="handleFocus"
          @blur="handleBlur"
          @input="handleInput"
          @keydown.enter="handleEnter"
          @keydown.arrow-down="navigateDown"
          @keydown.arrow-up="navigateUp"
          @keydown.escape="closeSuggestions"
        />
        <button
          v-if="searchQuery"
          @click="clearSearch"
          class="clear-btn"
          type="button"
          :aria-label="$t('Limpiar búsqueda')"
        >
          <i class="bi bi-x-circle"></i>
        </button>
        <div v-if="isSearching" class="search-spinner">
          <div class="spinner"></div>
        </div>
      </div>

      <!-- Panel de sugerencias -->
      <Transition name="suggestions">
        <div
          v-if="showSuggestions && (suggestions.length > 0 || recentSearches.length > 0)"
          ref="suggestionsPanelRef"
          class="suggestions-panel"
          @mousedown.prevent
          @mouseenter="handlePanelMouseEnter"
          @mouseleave="handlePanelMouseLeave"
          @wheel="handlePanelWheel"
        >
          <!-- Búsquedas recientes (solo si no hay query) -->
          <div v-if="!searchQuery && recentSearches.length > 0" class="suggestions-section">
            <div class="suggestions-header">
              <i class="bi bi-clock-history"></i>
              <span>{{ $t('Búsquedas recientes') }}</span>
              <button @click="clearRecentSearches" class="clear-recent-btn" type="button">
                {{ $t('Limpiar') }}
              </button>
            </div>
            <div
              v-for="(recent, index) in recentSearches"
              :key="`recent-${index}`"
              class="suggestion-item"
              :class="{ 'highlighted': highlightedIndex === index }"
              @click="selectSuggestion(recent)"
              @mouseenter="highlightedIndex = index"
            >
              <i class="bi bi-clock-history"></i>
              <span>{{ recent }}</span>
            </div>
          </div>

          <!-- Sugerencias de eventos -->
          <div v-if="suggestions.length > 0" class="suggestions-section">
            <div class="suggestions-header">
              <i class="bi bi-calendar-event"></i>
              <span>{{ $t('Eventos') }} ({{ suggestions.length }})</span>
            </div>
            <div
              v-for="(suggestion, index) in suggestions"
              :key="`event-${suggestion.id || index}`"
              class="suggestion-item suggestion-event"
              :class="{ 'highlighted': highlightedIndex === (recentSearches.length + index) }"
              @click="selectEvent(suggestion)"
              @mouseenter="highlightedIndex = recentSearches.length + index"
            >
              <div class="suggestion-image" v-if="suggestion.image">
                <img :src="suggestion.image" :alt="suggestion.title" />
              </div>
              <div class="suggestion-content">
                <div class="suggestion-title" v-html="highlightMatch(suggestion.title)"></div>
                <div class="suggestion-meta">
                  <span v-if="suggestion.venue" class="suggestion-venue">
                    <i class="bi bi-geo-alt"></i>
                    {{ suggestion.venue }}
                  </span>
                  <span v-if="suggestion.location" class="suggestion-location">
                    {{ suggestion.location }}
                  </span>
                  <span v-if="suggestion.date" class="suggestion-date">
                    <i class="bi bi-calendar"></i>
                    {{ formatDate(suggestion.date) }}
                  </span>
                </div>
                <div v-if="suggestion.category" class="suggestion-category">
                  <span class="category-badge" :class="getCategoryClass(suggestion.category)">
                    {{ getCategoryLabel(suggestion.category) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Sugerencias de ciudades únicas (solo si hay pocos eventos o ninguno) -->
          <div v-if="citySuggestions.length > 0 && suggestions.length < 3" class="suggestions-section">
            <div class="suggestions-header">
              <i class="bi bi-geo-alt-fill"></i>
              <span>{{ $t('Ciudades') }}</span>
            </div>
            <div
              v-for="(city, index) in citySuggestions"
              :key="`city-${index}`"
              class="suggestion-item suggestion-city"
              :class="{ 'highlighted': highlightedIndex === (recentSearches.length + suggestions.length + index) }"
              @click="selectCity(city)"
              @mouseenter="highlightedIndex = recentSearches.length + suggestions.length + index"
            >
              <i class="bi bi-geo-alt-fill"></i>
              <span>{{ city }}</span>
            </div>
          </div>

          <!-- Sugerencias de venues únicos (solo si hay pocos eventos o ninguno) -->
          <div v-if="venueSuggestions.length > 0 && suggestions.length < 3" class="suggestions-section">
            <div class="suggestions-header">
              <i class="bi bi-building"></i>
              <span>{{ $t('Venues') }}</span>
            </div>
            <div
              v-for="(venue, index) in venueSuggestions"
              :key="`venue-${index}`"
              class="suggestion-item suggestion-venue-item"
              :class="{ 'highlighted': highlightedIndex === (recentSearches.length + suggestions.length + citySuggestions.length + index) }"
              @click="selectVenue(venue)"
              @mouseenter="highlightedIndex = recentSearches.length + suggestions.length + citySuggestions.length + index"
            >
              <i class="bi bi-building"></i>
              <span>{{ venue }}</span>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatDateISO } from '~/utils/formatDate'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  events: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: ''
  },
  activeCategory: {
    type: String,
    default: 'all'
  },
  debounceMs: {
    type: Number,
    default: 300
  }
})

const emit = defineEmits(['update:modelValue', 'select-event', 'select-location', 'select-venue'])

const { t, locale } = useI18n()

const searchQuery = ref(props.modelValue)
const isFocused = ref(false)
const showSuggestions = ref(false)
const isSearching = ref(false)
const highlightedIndex = ref(-1)
const searchInputRef = ref(null)
const searchWrapperRef = ref(null)
const suggestionsPanelRef = ref(null)
const keepPanelOpen = ref(false)

let debounceTimer = null
let recentSearches = ref([])

// Cache para optimizar el rendimiento del scroll
let scrollCache = {
  isScrollable: null,
  scrollHeight: null,
  clientHeight: null,
  lastCheck: 0
}

const CACHE_DURATION = 100 // ms

// Cargar búsquedas recientes del localStorage (solo en cliente)
onMounted(() => {
  if (typeof window !== 'undefined' && window.localStorage) {
    const stored = window.localStorage.getItem('golive_recent_searches')
    if (stored) {
      try {
        recentSearches.value = JSON.parse(stored).slice(0, 5) // Máximo 5
      } catch (e) {
        recentSearches.value = []
      }
    }
  }
})

// Guardar búsquedas recientes (solo en cliente)
const saveRecentSearch = (query) => {
  if (!query || !query.trim()) return
  
  if (typeof window !== 'undefined' && window.localStorage) {
    const searches = recentSearches.value.filter(s => s.toLowerCase() !== query.toLowerCase())
    searches.unshift(query.trim())
    recentSearches.value = searches.slice(0, 5)
    window.localStorage.setItem('golive_recent_searches', JSON.stringify(recentSearches.value))
  }
}

// Limpiar búsquedas recientes (solo en cliente)
const clearRecentSearches = () => {
  recentSearches.value = []
  if (typeof window !== 'undefined' && window.localStorage) {
    window.localStorage.removeItem('golive_recent_searches')
  }
}

// Sincronizar con v-model
watch(() => props.modelValue, (newVal) => {
  if (newVal !== searchQuery.value) {
    searchQuery.value = newVal
  }
})

watch(searchQuery, (newVal) => {
  emit('update:modelValue', newVal)
})

// Placeholder traducido
const computedPlaceholder = computed(() => {
  if (props.placeholder) {
    return props.placeholder
  }
  return t('Buscar eventos, ciudades, venues...')
})

// Búsqueda inteligente con debounce
const handleInput = () => {
  clearTimeout(debounceTimer)
  isSearching.value = true
  
  debounceTimer = setTimeout(() => {
    performSearch()
    isSearching.value = false
  }, props.debounceMs)
}

const performSearch = () => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    showSuggestions.value = isFocused.value
    return
  }

  showSuggestions.value = true
  highlightedIndex.value = -1
}

// Sugerencias de eventos
const suggestions = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    return []
  }

  const query = searchQuery.value.toLowerCase().trim()
  const results = []

  props.events.forEach(event => {
    // Filtrar por categoría activa primero
    if (props.activeCategory !== 'all') {
      const eventCategory = (event.category || '').toLowerCase()
      const activeCategoryLower = props.activeCategory.toLowerCase()
      if (eventCategory !== activeCategoryLower) {
        return // Saltar este evento si no coincide con la categoría
      }
    }

    const title = (event.title || '').toLowerCase()
    const description = (event.description || '').toLowerCase()
    const venue = (event.venue || '').toLowerCase()
    const location = (event.location || '').toLowerCase()
    const category = (event.category || '').toLowerCase()

    const matchesTitle = title.includes(query)
    const matchesDescription = description.includes(query)
    const matchesVenue = venue.includes(query)
    const matchesLocation = location.includes(query)
    const matchesCategory = category.includes(query)

    if (matchesTitle || matchesDescription || matchesVenue || matchesLocation || matchesCategory) {
      // Calcular relevancia (título tiene más peso)
      let relevance = 0
      if (matchesTitle) relevance += 10
      if (title.startsWith(query)) relevance += 5
      if (matchesDescription) relevance += 3
      if (matchesVenue) relevance += 2
      if (matchesLocation) relevance += 2
      if (matchesCategory) relevance += 1

      results.push({
        ...event,
        relevance
      })
    }
  })

  // Ordenar por relevancia y limitar a 8 resultados
  return results
    .sort((a, b) => b.relevance - a.relevance)
    .slice(0, 8)
})

// Forzar recálculo del scroll cuando cambien las sugerencias (solo en cliente)
watch([() => suggestions.value, showSuggestions], () => {
  if (typeof window !== 'undefined' && suggestionsPanelRef.value && showSuggestions.value) {
    // Invalidar cache cuando cambian las sugerencias
    scrollCache.isScrollable = null
    scrollCache.scrollHeight = null
    scrollCache.clientHeight = null
    
    // Forzar recálculo del layout después de que el DOM se actualice
    nextTick(() => {
      if (suggestionsPanelRef.value) {
        // Forzar recálculo accediendo a propiedades que requieren layout
        void suggestionsPanelRef.value.offsetHeight
        void suggestionsPanelRef.value.scrollHeight
      }
    })
  }
}, { flush: 'post' })

// Sugerencias de ciudades únicas
const citySuggestions = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    return []
  }

  const query = searchQuery.value.toLowerCase().trim()
  const cities = new Set()

  props.events.forEach(event => {
    // Filtrar por categoría activa
    if (props.activeCategory !== 'all') {
      const eventCategory = (event.category || '').toLowerCase()
      const activeCategoryLower = props.activeCategory.toLowerCase()
      if (eventCategory !== activeCategoryLower) {
        return // Saltar este evento si no coincide con la categoría
      }
    }

    const location = (event.location || '').trim()
    if (location && location.toLowerCase().includes(query)) {
      cities.add(location)
    }
  })

  return Array.from(cities).slice(0, 5)
})

// Sugerencias de venues únicos
const venueSuggestions = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    return []
  }

  const query = searchQuery.value.toLowerCase().trim()
  const venues = new Set()

  props.events.forEach(event => {
    // Filtrar por categoría activa
    if (props.activeCategory !== 'all') {
      const eventCategory = (event.category || '').toLowerCase()
      const activeCategoryLower = props.activeCategory.toLowerCase()
      if (eventCategory !== activeCategoryLower) {
        return // Saltar este evento si no coincide con la categoría
      }
    }

    const venue = (event.venue || '').trim()
    if (venue && venue.toLowerCase().includes(query)) {
      venues.add(venue)
    }
  })

  return Array.from(venues).slice(0, 5)
})

// Handlers
const handleFocus = () => {
  isFocused.value = true
  if (searchQuery.value && searchQuery.value.trim().length >= 2) {
    showSuggestions.value = true
  } else if (recentSearches.value.length > 0) {
    showSuggestions.value = true
  }
}

const handleBlur = (e) => {
  // Delay para permitir clicks en sugerencias y scroll
  setTimeout(() => {
    // No cerrar si el panel debe mantenerse abierto (usuario interactuando con él)
    if (keepPanelOpen.value) {
      return
    }
    
    // Verificar si el elemento activo está dentro del wrapper (solo en cliente)
    if (typeof document !== 'undefined') {
      const activeElement = document.activeElement
      const wrapper = searchWrapperRef.value
      
      if (!wrapper?.contains(activeElement)) {
        isFocused.value = false
        showSuggestions.value = false
        highlightedIndex.value = -1
        keepPanelOpen.value = false
      }
    }
  }, 200)
}

// Handlers para el panel de sugerencias
const handlePanelMouseEnter = () => {
  keepPanelOpen.value = true
}

const handlePanelMouseLeave = () => {
  keepPanelOpen.value = false
}

const handlePanelWheel = (e) => {
  if (!suggestionsPanelRef.value || typeof window === 'undefined') return
  
  const panel = suggestionsPanelRef.value
  const now = Date.now()
  
  // Usar requestAnimationFrame para mejor rendimiento
  requestAnimationFrame(() => {
    // Cachear valores para evitar lecturas repetidas del DOM
    if (!scrollCache.isScrollable || (now - scrollCache.lastCheck) > CACHE_DURATION) {
      const scrollHeight = panel.scrollHeight
      const clientHeight = panel.clientHeight
      const totalItems = suggestions.value.length + citySuggestions.value.length + venueSuggestions.value.length
      const hasMultipleItems = totalItems >= 2
      
      scrollCache.scrollHeight = scrollHeight
      scrollCache.clientHeight = clientHeight
      scrollCache.isScrollable = scrollHeight > (clientHeight + 1) || 
                                 (scrollHeight >= clientHeight && hasMultipleItems)
      scrollCache.lastCheck = now
    }
    
    // Si no es scrollable, permitir scroll de página
    if (!scrollCache.isScrollable) {
      return
    }
    
    // Obtener valores actuales del scroll
    const scrollTop = panel.scrollTop
    const scrollHeight = scrollCache.scrollHeight
    const clientHeight = scrollCache.clientHeight
    
    // Verificar límites
    const margin = 2
    const isAtTop = scrollTop <= margin
    const isAtBottom = scrollTop + clientHeight >= scrollHeight - margin
    
    // Si estamos en el top y scrolleando hacia arriba, permitir scroll de página
    if (isAtTop && e.deltaY < 0) {
      return
    }
    
    // Si estamos en el bottom y scrolleando hacia abajo, permitir scroll de página
    if (isAtBottom && e.deltaY > 0) {
      return
    }
    
    // Prevenir el scroll de la página
    e.stopPropagation()
    e.preventDefault()
    
    // Aplicar scroll al panel
    const delta = e.deltaY
    const maxScroll = Math.max(0, scrollHeight - clientHeight)
    let newScroll = scrollTop + delta
    
    // Limitar dentro de los límites
    newScroll = Math.max(0, Math.min(newScroll, maxScroll))
    
    // Aplicar el scroll
    panel.scrollTop = newScroll
  })
}

const handleEnter = () => {
  if (highlightedIndex.value >= 0) {
    const totalItems = recentSearches.value.length + suggestions.value.length + citySuggestions.value.length + venueSuggestions.value.length
    if (highlightedIndex.value < totalItems) {
      if (highlightedIndex.value < recentSearches.value.length) {
        selectSuggestion(recentSearches.value[highlightedIndex.value])
      } else if (highlightedIndex.value < recentSearches.value.length + suggestions.value.length) {
        const index = highlightedIndex.value - recentSearches.value.length
        selectEvent(suggestions.value[index])
      } else if (highlightedIndex.value < recentSearches.value.length + suggestions.value.length + citySuggestions.value.length) {
        const index = highlightedIndex.value - recentSearches.value.length - suggestions.value.length
        selectCity(citySuggestions.value[index])
      } else {
        const index = highlightedIndex.value - recentSearches.value.length - suggestions.value.length - citySuggestions.value.length
        selectVenue(venueSuggestions.value[index])
      }
    }
  } else if (searchQuery.value && searchQuery.value.trim()) {
    // Buscar con el query actual
    saveRecentSearch(searchQuery.value)
    closeSuggestions()
  }
}

const navigateDown = (e) => {
  e.preventDefault()
  const totalItems = recentSearches.value.length + suggestions.value.length + citySuggestions.value.length + venueSuggestions.value.length
  if (totalItems === 0) return
  
  highlightedIndex.value = (highlightedIndex.value + 1) % totalItems
  scrollToHighlighted()
}

const navigateUp = (e) => {
  e.preventDefault()
  const totalItems = recentSearches.value.length + suggestions.value.length + citySuggestions.value.length + venueSuggestions.value.length
  if (totalItems === 0) return
  
  highlightedIndex.value = highlightedIndex.value <= 0 ? totalItems - 1 : highlightedIndex.value - 1
  scrollToHighlighted()
}

const scrollToHighlighted = () => {
  nextTick(() => {
    if (typeof document !== 'undefined' && searchWrapperRef.value) {
      const highlighted = searchWrapperRef.value.querySelector('.suggestion-item.highlighted')
      if (highlighted && typeof highlighted.scrollIntoView === 'function') {
        highlighted.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
      }
    }
  })
}

const closeSuggestions = () => {
  showSuggestions.value = false
  highlightedIndex.value = -1
  keepPanelOpen.value = false
}

const clearSearch = () => {
  searchQuery.value = ''
  emit('update:modelValue', '')
  // Focus solo en cliente
  if (typeof document !== 'undefined' && searchInputRef.value && typeof searchInputRef.value.focus === 'function') {
    searchInputRef.value.focus()
  }
}

const selectSuggestion = (query) => {
  searchQuery.value = query
  emit('update:modelValue', query)
  saveRecentSearch(query)
  closeSuggestions()
}

const selectEvent = (event) => {
  saveRecentSearch(searchQuery.value)
  emit('select-event', event)
  closeSuggestions()
}

const selectCity = (city) => {
  searchQuery.value = city
  emit('update:modelValue', city)
  emit('select-location', city)
  saveRecentSearch(city)
  closeSuggestions()
}

const selectVenue = (venue) => {
  searchQuery.value = venue
  emit('update:modelValue', venue)
  emit('select-venue', venue)
  saveRecentSearch(venue)
  closeSuggestions()
}

// Utilidades
const highlightMatch = (text) => {
  if (!searchQuery.value || !text) {
    // Escapar HTML si no hay búsqueda
    return escapeHtml(String(text))
  }
  
  // Escapar HTML del texto primero para seguridad
  const escapedText = escapeHtml(String(text))
  const query = escapeHtml(searchQuery.value.trim())
  
  // Crear regex escapando caracteres especiales
  const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedQuery})`, 'gi')
  
  return escapedText.replace(regex, '<mark>$1</mark>')
}

// Función para escapar HTML (compatible con SSR)
const escapeHtml = (text) => {
  if (!text) return ''
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  }
  return String(text).replace(/[&<>"']/g, (m) => map[m])
}

const formatDate = (date) => {
  if (!date) return ''
  return formatDateISO(date, locale.value)
}

const getCategoryClass = (category) => {
  const cat = (category || '').toLowerCase()
  if (cat === 'concierto') return 'category-concierto'
  if (cat === 'festival') return 'category-festival'
  return 'category-other'
}

const getCategoryLabel = (category) => {
  const cat = (category || '').toLowerCase()
  if (cat === 'concierto') return t('Conciertos')
  if (cat === 'festival') return t('Festivales')
  return category
}

// Limpiar timer y cache al desmontar
onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  // Limpiar cache
  scrollCache = {
    isScrollable: null,
    scrollHeight: null,
    clientHeight: null,
    lastCheck: 0
  }
})
</script>

<style scoped>
.advanced-search-wrapper {
  position: relative;
  width: 100%;
  z-index: 100;
}

.search-container {
  position: relative;
  width: 100%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-container.focused {
  z-index: 1000;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border: 2px solid #ff0057;
  border-radius: 12px;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 52px;
}

.search-container.focused .search-input-wrapper {
  border-color: #ff6b35;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(255, 0, 87, 0.25);
  transform: translateY(-2px);
}

.search-icon {
  color: #ff0057;
  font-size: 1.2rem;
  margin-right: 12px;
  flex-shrink: 0;
  transition: color 0.3s ease;
}

.search-container.focused .search-icon {
  color: #ff6b35;
}

.search-input-advanced {
  flex: 1;
  border: none;
  background: transparent;
  padding: 14px 0;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  color: #0a0a0a;
  outline: none;
  font-weight: 500;
}

.search-input-advanced::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.clear-btn {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 4px;
  margin-left: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  border-radius: 50%;
  width: 28px;
  height: 28px;
}

.clear-btn:hover {
  color: #ff0057;
  background: rgba(255, 0, 87, 0.1);
}

.search-spinner {
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 0, 87, 0.2);
  border-top-color: #ff0057;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Panel de sugerencias */
.suggestions-panel {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  right: 0;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  max-height: min(450px, 55vh);
  height: min(450px, 55vh);
  overflow-y: auto !important;
  overflow-x: hidden;
  z-index: 1000;
  border: 1px solid rgba(255, 0, 87, 0.1);
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  scroll-behavior: smooth;
  pointer-events: auto;
  box-sizing: border-box;
  /* Forzar que el contenido pueda hacer scroll */
  display: flex;
  flex-direction: column;
  /* Optimizaciones de rendimiento */
  will-change: scroll-position;
  transform: translateZ(0);
  backface-visibility: hidden;
  -webkit-font-smoothing: antialiased;
}

.suggestions-panel::-webkit-scrollbar {
  width: 6px;
}

.suggestions-panel::-webkit-scrollbar-track {
  background: #f5f5f5;
  border-radius: 3px;
}

.suggestions-panel::-webkit-scrollbar-thumb {
  background: #ff0057;
  border-radius: 3px;
}

.suggestions-panel::-webkit-scrollbar-thumb:hover {
  background: #ff6b35;
}

.suggestions-section {
  padding: 8px 0;
  min-height: fit-content;
  flex-shrink: 0;
}

/* Asegurar que la última sección tenga padding inferior para que se vea completamente */
.suggestions-section:last-child {
  padding-bottom: 16px;
}

.suggestions-panel > * {
  min-height: fit-content;
  flex-shrink: 0;
}

/* Asegurar que el último elemento tenga espacio para forzar scroll */
.suggestions-panel::after {
  content: '';
  display: block;
  height: 40px;
  min-height: 40px;
  flex-shrink: 0;
}

.suggestions-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid #f3f4f6;
  margin-bottom: 4px;
}

.suggestions-header i {
  font-size: 0.9rem;
  color: #ff0057;
}

.clear-recent-btn {
  margin-left: auto;
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 0.7rem;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
}

.clear-recent-btn:hover {
  color: #ff0057;
  background: rgba(255, 0, 87, 0.1);
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease, border-left-color 0.2s ease;
  border-left: 3px solid transparent;
  font-family: 'Poppins', sans-serif;
  /* Optimización de rendimiento */
  will-change: background-color;
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background: rgba(255, 0, 87, 0.08);
  border-left-color: #ff0057;
}

.suggestion-item i {
  color: #9ca3af;
  font-size: 1.1rem;
  flex-shrink: 0;
  width: 20px;
  text-align: center;
}

.suggestion-item.highlighted i {
  color: #ff0057;
}

.suggestion-item span {
  color: #0a0a0a;
  font-size: 0.95rem;
  font-weight: 500;
}

.suggestion-item.highlighted span {
  color: #ff0057;
  font-weight: 600;
}

/* Evento con imagen */
.suggestion-event {
  align-items: flex-start;
  padding: 12px 16px;
  /* Optimización de rendimiento */
  contain: layout style paint;
}

.suggestion-image {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  /* Optimización de rendimiento */
  will-change: transform;
  transform: translateZ(0);
}

.suggestion-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  /* Optimización de rendimiento */
  will-change: transform;
  transform: translateZ(0);
}

.suggestion-content {
  flex: 1;
  min-width: 0;
}

.suggestion-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #0a0a0a;
  margin-bottom: 6px;
  line-height: 1.3;
}

.suggestion-title :deep(mark) {
  background: rgba(255, 0, 87, 0.2);
  color: #ff0057;
  font-weight: 700;
  padding: 0 2px;
  border-radius: 2px;
}

.suggestion-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 0.8rem;
  color: #6b7280;
  margin-bottom: 6px;
}

.suggestion-venue,
.suggestion-location,
.suggestion-date {
  display: flex;
  align-items: center;
  gap: 4px;
}

.suggestion-venue i,
.suggestion-date i {
  font-size: 0.85rem;
  color: #ff0057;
}

.suggestion-category {
  margin-top: 4px;
}

.category-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.category-badge.category-concierto {
  background: rgba(255, 0, 87, 0.15);
  color: #ff0057;
}

.category-badge.category-festival {
  background: rgba(255, 107, 53, 0.15);
  color: #ff6b35;
}

.category-badge.category-other {
  background: rgba(107, 114, 128, 0.15);
  color: #6b7280;
}

/* Transiciones */
.suggestions-enter-active,
.suggestions-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.suggestions-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.suggestions-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive - Solo móviles */
@media (max-width: 768px) {
  .search-input-wrapper {
    min-height: 44px;
    padding: 0 10px;
    border-radius: 10px;
  }

  .search-icon {
    font-size: 1rem;
    margin-right: 8px;
  }

  .search-input-advanced {
    font-size: 0.85rem;
    padding: 10px 0;
  }

  .clear-btn {
    width: 24px;
    height: 24px;
    font-size: 0.95rem;
  }

  .suggestions-panel {
    max-height: min(400px, 50vh);
    height: min(400px, 50vh);
    border-radius: 12px;
    top: calc(100% + 4px);
  }

  .suggestions-header {
    padding: 8px 12px 6px;
    font-size: 0.65rem;
    margin-bottom: 2px;
  }

  .suggestions-header i {
    font-size: 0.75rem;
  }

  .clear-recent-btn {
    font-size: 0.65rem;
    padding: 2px 6px;
  }

  .suggestion-item {
    padding: 8px 12px;
    gap: 10px;
  }

  .suggestion-item i {
    font-size: 0.95rem;
    width: 18px;
  }

  .suggestion-item span {
    font-size: 0.85rem;
  }

  .suggestion-event {
    padding: 8px 12px;
  }

  .suggestion-image {
    width: 42px;
    height: 42px;
    border-radius: 6px;
  }

  .suggestion-content {
    gap: 0;
  }

  .suggestion-title {
    font-size: 0.8rem;
    margin-bottom: 4px;
    line-height: 1.25;
    font-weight: 600;
  }

  .suggestion-meta {
    font-size: 0.7rem;
    gap: 8px;
    margin-bottom: 4px;
  }

  .suggestion-venue,
  .suggestion-location,
  .suggestion-date {
    gap: 3px;
  }

  .suggestion-venue i,
  .suggestion-date i {
    font-size: 0.7rem;
  }

  .suggestion-category {
    margin-top: 2px;
  }

  .category-badge {
    padding: 1px 6px;
    font-size: 0.6rem;
    border-radius: 8px;
  }

  .suggestions-section {
    padding: 4px 0;
  }

  .suggestions-section:last-child {
    padding-bottom: 12px;
  }

  .suggestions-panel::after {
    height: 30px;
    min-height: 30px;
  }
}
</style>
<template>
  <div class="event-map-wrapper">
    <div ref="mapContainer" class="map-container"></div>
    <div v-if="loading" class="map-loading-overlay">
      <div class="spinner-border" role="status"></div>
      <p>{{ $t('Cargando mapa') }}...</p>
    </div>
    <div v-if="error" class="map-error-overlay">
      <i class="bi bi-exclamation-triangle"></i>
      <p>{{ error }}</p>
    </div>
    
    <!-- Panel de información y acciones -->
    <div v-if="!loading && !error && coordinates" class="map-info-panel">
      <!-- Botones de rutas -->
      <div class="routes-section">
        <h4 class="section-title">
          <i class="bi bi-signpost-split"></i>
          {{ $t('Cómo llegar') || 'Cómo llegar' }}
        </h4>
        <div class="route-buttons">
          <button 
            class="route-btn" 
            @click="openDirections('google')"
            :title="$t('Abrir en Google Maps') || 'Abrir en Google Maps'"
          >
            <i class="bi bi-google"></i>
            <span class="route-btn-text">Google Maps</span>
            <span class="route-btn-text-short">Google</span>
          </button>
          <button 
            class="route-btn" 
            @click="openDirections('apple')"
            :title="$t('Abrir en Apple Maps') || 'Abrir en Apple Maps'"
          >
            <i class="bi bi-apple"></i>
            <span class="route-btn-text">Apple Maps</span>
            <span class="route-btn-text-short">Apple</span>
          </button>
          <button 
            class="route-btn" 
            @click="openDirections('waze')"
            :title="$t('Abrir en Waze') || 'Abrir en Waze'"
          >
            <i class="bi bi-navigation"></i>
            <span class="route-btn-text">Waze</span>
            <span class="route-btn-text-short">Waze</span>
          </button>
        </div>
      </div>

      <!-- Transporte público -->
      <div class="transport-section">
        <button 
          class="section-header-btn"
          @click="toggleTransportSection"
          :aria-expanded="isTransportOpen"
        >
          <h4 class="section-title">
            <i class="bi bi-bus-front"></i>
            {{ $t('Transporte público') || 'Transporte público' }}
          </h4>
          <i 
            class="bi chevron-icon"
            :class="isTransportOpen ? 'bi-chevron-up' : 'bi-chevron-down'"
          ></i>
        </button>
        <div 
          v-show="isTransportOpen"
          class="section-content"
        >
          <div v-if="loadingTransport" class="loading-transport">
            <div class="spinner-border-small"></div>
            <span>{{ $t('Buscando paradas cercanas') || 'Buscando paradas cercanas' }}...</span>
          </div>
          <div v-else-if="transportStops.length > 0" class="transport-list">
            <div 
              v-for="(stop, index) in transportStops.slice(0, 3)" 
              :key="index"
              class="transport-item"
              @click="centerMapOnLocation(stop.lat, stop.lon)"
            >
              <i :class="getTransportIcon(stop.type)"></i>
              <div class="transport-info">
                <strong>{{ stop.name || $t('Parada') || 'Parada' }}</strong>
                <span class="transport-distance">{{ formatDistance(stop.distance) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="no-transport">
            <i class="bi bi-info-circle"></i>
            <span>{{ $t('No se encontraron paradas cercanas') || 'No se encontraron paradas cercanas' }}</span>
          </div>
        </div>
      </div>

      <!-- Parking -->
      <div class="parking-section">
        <button 
          class="section-header-btn"
          @click="toggleParkingSection"
          :aria-expanded="isParkingOpen"
        >
          <h4 class="section-title">
            <i class="bi bi-p-square"></i>
            {{ $t('Parking cercano') || 'Parking cercano' }}
          </h4>
          <i 
            class="bi chevron-icon"
            :class="isParkingOpen ? 'bi-chevron-up' : 'bi-chevron-down'"
          ></i>
        </button>
        <div 
          v-show="isParkingOpen"
          class="section-content"
        >
          <div v-if="loadingParking" class="loading-parking">
            <div class="spinner-border-small"></div>
            <span>{{ $t('Buscando parkings') || 'Buscando parkings' }}...</span>
          </div>
          <div v-else-if="parkings.length > 0" class="parking-list">
            <div 
              v-for="(parking, index) in parkings.slice(0, 3)" 
              :key="index"
              class="parking-item"
              @click="centerMapOnLocation(parking.lat, parking.lon)"
            >
              <i class="bi bi-p-square-fill"></i>
              <div class="parking-info">
                <strong>{{ parking.name || $t('Parking') || 'Parking' }}</strong>
                <span class="parking-distance">{{ formatDistance(parking.distance) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="no-parking">
            <i class="bi bi-info-circle"></i>
            <span>{{ $t('No se encontraron parkings cercanos') || 'No se encontraron parkings cercanos' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({
  address: { type: String, required: true },
  title: { type: String, default: '' }
})

const mapContainer = ref(null)
const loading = ref(true)
const error = ref('')
const coordinates = ref(null)
const transportStops = ref([])
const parkings = ref([])
const loadingTransport = ref(false)
const loadingParking = ref(false)
const isTransportOpen = ref(true) // Abierto por defecto en desktop
const isParkingOpen = ref(true) // Abierto por defecto en desktop
let map = null
let marker = null
let L = null // Referencia a Leaflet
let transportMarkers = []
let parkingMarkers = []

// Toggle secciones (en móvil se colapsan por defecto)
const toggleTransportSection = () => {
  isTransportOpen.value = !isTransportOpen.value
}

const toggleParkingSection = () => {
  isParkingOpen.value = !isParkingOpen.value
}

// Detectar si es móvil y ajustar estado inicial
const checkMobileAndSetInitialState = () => {
  if (process.client) {
    const isMobile = window.innerWidth <= 768
    isTransportOpen.value = !isMobile
    isParkingOpen.value = !isMobile
  }
}

// Calcular distancia entre dos puntos (Haversine)
const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371 // Radio de la Tierra en km
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

// Formatear distancia
const formatDistance = (distance) => {
  if (distance < 1) {
    return `${Math.round(distance * 1000)} m`
  }
  return `${distance.toFixed(1)} km`
}

// Obtener icono según tipo de transporte
const getTransportIcon = (type) => {
  if (type === 'bus') return 'bi bi-bus-front-fill'
  if (type === 'subway' || type === 'metro') return 'bi bi-train-front-fill'
  if (type === 'tram') return 'bi bi-train-tram-fill'
  return 'bi bi-circle-fill'
}

// Geocodificar dirección
const geocodeAddress = async (address) => {
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}&limit=1`,
      { headers: { 'User-Agent': 'GoLive Event Map' } }
    )
    const data = await response.json()
    return data?.length > 0 ? {
      lat: parseFloat(data[0].lat),
      lng: parseFloat(data[0].lon)
    } : null
  } catch {
    return null
  }
}

// Buscar paradas de transporte público cercanas
const findTransportStops = async (lat, lon) => {
  loadingTransport.value = true
  try {
    // Usar Overpass API para buscar paradas de transporte público
    const radius = 1000 // 1km de radio
    const query = `
      [out:json][timeout:25];
      (
        node["public_transport"="stop_position"](around:${radius},${lat},${lon});
        node["highway"="bus_stop"](around:${radius},${lat},${lon});
        node["railway"="station"](around:${radius},${lat},${lon});
        node["railway"="subway_entrance"](around:${radius},${lat},${lon});
      );
      out;
    `
    
    const response = await fetch('https://overpass-api.de/api/interpreter', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `data=${encodeURIComponent(query)}`
    })
    
    const data = await response.json()
    const stops = []
    
    if (data.elements) {
      data.elements.forEach(element => {
        if (element.type === 'node' && element.lat && element.lon) {
          const distance = calculateDistance(lat, lon, element.lat, element.lon)
          let type = 'bus'
          
          if (element.tags?.railway === 'station' || element.tags?.railway === 'subway_entrance') {
            type = 'subway'
          } else if (element.tags?.railway === 'tram_stop') {
            type = 'tram'
          }
          
          stops.push({
            lat: element.lat,
            lon: element.lon,
            name: element.tags?.name || element.tags?.ref || 'Parada',
            type,
            distance
          })
        }
      })
    }
    
    // Ordenar por distancia y tomar los más cercanos
    transportStops.value = stops
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 5)
    
    // Añadir marcadores al mapa
    addTransportMarkers()
  } catch (err) {
    console.error('Error buscando transporte público:', err)
    transportStops.value = []
  } finally {
    loadingTransport.value = false
  }
}

// Buscar parkings cercanos
const findParkings = async (lat, lon) => {
  loadingParking.value = true
  try {
    const radius = 1000 // 1km de radio
    const query = `
      [out:json][timeout:25];
      (
        node["amenity"="parking"](around:${radius},${lat},${lon});
        way["amenity"="parking"](around:${radius},${lat},${lon});
      );
      out center;
    `
    
    const response = await fetch('https://overpass-api.de/api/interpreter', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `data=${encodeURIComponent(query)}`
    })
    
    const data = await response.json()
    const parkingList = []
    
    if (data.elements) {
      data.elements.forEach(element => {
        let parkingLat, parkingLon
        
        if (element.type === 'node') {
          parkingLat = element.lat
          parkingLon = element.lon
        } else if (element.center) {
          parkingLat = element.center.lat
          parkingLon = element.center.lon
        }
        
        if (parkingLat && parkingLon) {
          const distance = calculateDistance(lat, lon, parkingLat, parkingLon)
          parkingList.push({
            lat: parkingLat,
            lon: parkingLon,
            name: element.tags?.name || 'Parking',
            distance
          })
        }
      })
    }
    
    // Ordenar por distancia y tomar los más cercanos
    parkings.value = parkingList
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 5)
    
    // Añadir marcadores al mapa
    addParkingMarkers()
  } catch (err) {
    console.error('Error buscando parkings:', err)
    parkings.value = []
  } finally {
    loadingParking.value = false
  }
}

// Añadir marcadores de transporte al mapa
const addTransportMarkers = () => {
  if (!map || !L) return
  
  // Limpiar marcadores anteriores
  transportMarkers.forEach(m => map.removeLayer(m))
  transportMarkers = []
  
  transportStops.value.forEach(stop => {
    const icon = L.divIcon({
      className: 'transport-marker',
      html: `<div class="transport-marker-icon"><i class="${getTransportIcon(stop.type)}"></i></div>`,
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    })
    
    const marker = L.marker([stop.lat, stop.lon], { icon })
      .addTo(map)
      .bindPopup(`<strong>${stop.name}</strong><br>${formatDistance(stop.distance)}`)
    
    transportMarkers.push(marker)
  })
}

// Añadir marcadores de parking al mapa
const addParkingMarkers = () => {
  if (!map || !L) return
  
  // Limpiar marcadores anteriores
  parkingMarkers.forEach(m => map.removeLayer(m))
  parkingMarkers = []
  
  parkings.value.forEach(parking => {
    const icon = L.divIcon({
      className: 'parking-marker',
      html: '<div class="parking-marker-icon"><i class="bi bi-p-square-fill"></i></div>',
      iconSize: [30, 30],
      iconAnchor: [15, 15]
    })
    
    const marker = L.marker([parking.lat, parking.lon], { icon })
      .addTo(map)
      .bindPopup(`<strong>${parking.name}</strong><br>${formatDistance(parking.distance)}`)
    
    parkingMarkers.push(marker)
  })
}

// Centrar mapa en una ubicación
const centerMapOnLocation = (lat, lon) => {
  if (map) {
    map.setView([lat, lon], 17, { animate: true, duration: 0.5 })
  }
}

// Abrir direcciones en apps externas
const openDirections = (app) => {
  if (!coordinates.value) return
  
  const { lat, lng } = coordinates.value
  const encodedAddress = encodeURIComponent(props.address)
  
  let url = ''
  
  switch (app) {
    case 'google':
      url = `https://www.google.com/maps/dir/?api=1&destination=${lat},${lng}`
      break
    case 'apple':
      url = `https://maps.apple.com/?daddr=${lat},${lng}`
      break
    case 'waze':
      url = `https://waze.com/ul?ll=${lat},${lng}&navigate=yes`
      break
  }
  
  if (url) {
    window.open(url, '_blank')
  }
}

// Limpiar mapa
const cleanupMap = () => {
  if (map) {
    transportMarkers.forEach(m => map.removeLayer(m))
    parkingMarkers.forEach(m => map.removeLayer(m))
    transportMarkers = []
    parkingMarkers = []
    map.remove()
    map = null
    marker = null
  }
}

// Inicializar mapa
const initializeMap = async () => {
  if (!mapContainer.value || !props.address) return

  loading.value = true
  error.value = ''

  try {
    const [leafletModule, coords] = await Promise.all([
      import('leaflet').then(m => m.default || m),
      geocodeAddress(props.address),
      import('leaflet/dist/leaflet.css')
    ])
    
    L = leafletModule // Guardar referencia global
    
    if (!coords) {
      error.value = 'No se pudo encontrar la ubicación en el mapa'
      loading.value = false
      return
    }

    coordinates.value = coords
    map = L.map(mapContainer.value).setView([coords.lat, coords.lng], 15)

    delete L.Icon.Default.prototype._getIconUrl
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowSize: [41, 41]
    })

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19
    }).addTo(map)

    const popupContent = props.title ? `<strong>${props.title}</strong><br>${props.address}` : props.address
    
    marker = L.marker([coords.lat, coords.lng])
      .addTo(map)
      .bindPopup(popupContent)
      .openPopup()

    loading.value = false
    
    // Buscar transporte público y parkings después de cargar el mapa
    setTimeout(() => {
      findTransportStops(coords.lat, coords.lng)
      findParkings(coords.lat, coords.lng)
    }, 500)
  } catch (err) {
    error.value = 'Error al cargar el mapa'
    loading.value = false
  }
}

watch(() => props.address, async (newAddress) => {
  if (newAddress && mapContainer.value) {
    cleanupMap()
    coordinates.value = null
    transportStops.value = []
    parkings.value = []
    await initializeMap()
  }
})

onMounted(async () => {
  checkMobileAndSetInitialState()
  
  if (process.client) {
    window.addEventListener('resize', checkMobileAndSetInitialState)
  }
  
  if (props.address) {
    if (mapContainer.value) {
      await initializeMap()
    } else {
      await nextTick()
      if (mapContainer.value) {
        await initializeMap()
      } else {
        setTimeout(() => mapContainer.value && initializeMap(), 50)
      }
    }
  }
})

onUnmounted(() => {
  cleanupMap()
  if (process.client) {
    window.removeEventListener('resize', checkMobileAndSetInitialState)
  }
})
</script>

<style scoped>
.event-map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.map-container {
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  background: #f9fafb;
}

.map-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(249, 250, 251, 0.95);
  border-radius: 12px;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.map-loading-overlay .spinner-border {
  width: 40px;
  height: 40px;
  border-width: 4px;
  color: #ff0057;
  border-right-color: transparent;
  margin-bottom: 12px;
}

.map-loading-overlay p {
  font-family: 'Poppins', sans-serif;
  color: #6b7280;
  font-size: 0.9rem;
  margin: 0;
}

.map-error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 400px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(254, 226, 226, 0.95);
  border-radius: 12px;
  border: 2px solid #dc2626;
  padding: 20px;
  text-align: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.map-error-overlay i {
  font-size: 2.5rem;
  color: #dc2626;
  margin-bottom: 12px;
}

.map-error-overlay p {
  font-family: 'Poppins', sans-serif;
  color: #991b1b;
  font-size: 0.9rem;
  margin: 0;
}

/* Panel de información */
.map-info-panel {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.section-title i {
  color: #ff0057;
  font-size: 1.1rem;
}

/* Botones de rutas */
.routes-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.route-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 14px;
}

.route-btn {
  flex: 1;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 44px;
}

.route-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.3);
}

.route-btn i {
  font-size: 1rem;
  flex-shrink: 0;
}

.route-btn-text-short {
  display: none;
}

/* Transporte público y Parking - Secciones colapsables */
.transport-section,
.parking-section {
  background: #ffffff;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.section-header-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background 0.2s ease;
  text-align: left;
}

.section-header-btn:hover {
  background: #f9fafb;
}

.section-header-btn .chevron-icon {
  color: #6b7280;
  font-size: 1rem;
  transition: transform 0.3s ease;
  flex-shrink: 0;
  margin-left: 12px;
}

.section-content {
  padding: 0 16px 16px 16px;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
  }
  to {
    opacity: 1;
    max-height: 500px;
  }
}

.loading-transport,
.loading-parking {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  color: #6b7280;
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
}

.spinner-border-small {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e7eb;
  border-top-color: #ff0057;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.transport-list,
.parking-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.transport-item,
.parking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9fafb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.transport-item:hover,
.parking-item:hover {
  background: #f3f4f6;
  border-color: #ff0057;
  transform: translateX(4px);
}

.transport-item i,
.parking-item i {
  font-size: 1.2rem;
  color: #ff0057;
  flex-shrink: 0;
}

.transport-info,
.parking-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.transport-info strong,
.parking-info strong {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  color: #0a0a0a;
}

.transport-distance,
.parking-distance {
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.no-transport,
.no-parking {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  color: #6b7280;
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  background: #f9fafb;
  border-radius: 8px;
}

.no-transport i,
.no-parking i {
  color: #9ca3af;
  font-size: 1rem;
}

/* Marcadores personalizados en el mapa */
:deep(.transport-marker) {
  background: transparent;
  border: none;
}

:deep(.transport-marker-icon) {
  width: 30px;
  height: 30px;
  background: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 0.9rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

:deep(.parking-marker) {
  background: transparent;
  border: none;
}

:deep(.parking-marker-icon) {
  width: 30px;
  height: 30px;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 0.9rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .map-container,
  .map-loading-overlay,
  .map-error-overlay {
    height: 280px;
  }

  .map-info-panel {
    margin-top: 12px;
    gap: 12px;
  }

  .routes-section {
    padding: 12px;
    border-radius: 10px;
  }

  .route-buttons {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
    margin-top: 12px;
  }

  .route-btn {
    min-width: auto;
    padding: 8px 4px;
    font-size: 0.75rem;
    flex-direction: column;
    gap: 4px;
    min-height: 50px;
  }

  .route-btn-text {
    display: none;
  }

  .route-btn-text-short {
    display: block;
    font-size: 0.7rem;
    line-height: 1.2;
  }

  .route-btn i {
    font-size: 1.2rem;
  }

  .transport-section,
  .parking-section {
    border-radius: 10px;
  }

  .section-header-btn {
    padding: 12px;
  }

  .section-title {
    font-size: 0.9rem;
  }

  .section-title i {
    font-size: 1rem;
  }

  .section-content {
    padding: 0 12px 12px 12px;
  }

  .transport-item,
  .parking-item {
    padding: 10px;
    border-radius: 8px;
  }

  .transport-item i,
  .parking-item i {
    font-size: 1.1rem;
  }

  .transport-info strong,
  .parking-info strong {
    font-size: 0.85rem;
  }

  .transport-distance,
  .parking-distance {
    font-size: 0.7rem;
  }
}

@media (max-width: 576px) {
  .map-container,
  .map-loading-overlay,
  .map-error-overlay {
    height: 240px;
    border-radius: 10px;
  }

  .map-info-panel {
    margin-top: 10px;
    gap: 10px;
  }

  .routes-section {
    padding: 10px;
    border-radius: 8px;
  }

  .route-buttons {
    gap: 6px;
    margin-top: 10px;
  }

  .route-btn {
    padding: 8px 3px;
    font-size: 0.7rem;
    border-radius: 6px;
    min-height: 48px;
  }

  .route-btn-text-short {
    font-size: 0.6rem;
  }

  .route-btn i {
    font-size: 1rem;
  }

  .transport-section,
  .parking-section {
    border-radius: 8px;
  }

  .section-header-btn {
    padding: 10px;
  }

  .section-title {
    font-size: 0.85rem;
  }

  .section-title i {
    font-size: 0.95rem;
  }

  .section-content {
    padding: 0 10px 10px 10px;
  }

  .transport-item,
  .parking-item {
    padding: 8px;
    border-radius: 6px;
  }

  .transport-item i,
  .parking-item i {
    font-size: 1rem;
  }

  .transport-info strong,
  .parking-info strong {
    font-size: 0.8rem;
  }

  .transport-distance,
  .parking-distance {
    font-size: 0.65rem;
  }

  .loading-transport,
  .loading-parking {
    padding: 10px;
    font-size: 0.75rem;
  }

  .no-transport,
  .no-parking {
    padding: 10px;
    font-size: 0.75rem;
  }
}
</style>

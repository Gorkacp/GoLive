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
let map = null
let marker = null

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

const cleanupMap = () => {
  if (map) {
    map.remove()
    map = null
    marker = null
  }
}

const initializeMap = async () => {
  if (!mapContainer.value || !props.address) return

  loading.value = true
  error.value = ''

  try {
    const [leafletModule, coordinates] = await Promise.all([
      import('leaflet').then(m => m.default || m),
      geocodeAddress(props.address),
      import('leaflet/dist/leaflet.css')
    ])
    
    const L = leafletModule
    
    if (!coordinates) {
      error.value = 'No se pudo encontrar la ubicación en el mapa'
      loading.value = false
      return
    }

    map = L.map(mapContainer.value).setView([coordinates.lat, coordinates.lng], 15)

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
    
    marker = L.marker([coordinates.lat, coordinates.lng])
      .addTo(map)
      .bindPopup(popupContent)
      .openPopup()

    loading.value = false
  } catch (err) {
    error.value = 'Error al cargar el mapa'
    loading.value = false
  }
}

watch(() => props.address, async (newAddress) => {
  if (newAddress && mapContainer.value) {
    cleanupMap()
    await initializeMap()
  }
})

onMounted(async () => {
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

onUnmounted(cleanupMap)
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

@media (max-width: 768px) {
  .map-container,
  .map-loading-overlay,
  .map-error-overlay {
    height: 300px;
  }
}

@media (max-width: 576px) {
  .map-container,
  .map-loading-overlay,
  .map-error-overlay {
    height: 250px;
  }
}
</style>

<template>
  <div class="event-admin-container">
    <ClientOnly><Header /></ClientOnly>
    
    <!-- Contenido principal -->
    <div class="container-main">
      <!-- Sección Formulario -->
      <section class="form-section">
        <div class="section-header">
          <h1 class="section-title">Crear Nuevo Evento</h1>
          <p class="section-description">Completa los datos del evento para agregarlo a tu catálogo</p>
        </div>

        <div class="form-wrapper">
          <form @submit.prevent="saveEvent" class="event-form">
            <!-- Título -->
            <div class="form-group">
              <label class="form-label">Título del Evento</label>
              <input type="text" v-model="title" class="form-input" placeholder="Ej: Concierto de Anuel AA 2025" required />
            </div>

            <!-- Lugar y Ubicación -->
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Lugar/Recinto</label>
                <input type="text" v-model="venue" class="form-input" placeholder="Ej: Palacio Vistalegre" required />
              </div>
              <div class="form-group">
                <label class="form-label">Ubicación Exacta</label>
                <input type="text" v-model="location" class="form-input" placeholder="Dirección completa" required />
              </div>
            </div>

            <!-- Fecha y Hora -->
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">Fecha</label>
                <input type="date" v-model="date" class="form-input" required />
              </div>
              <div class="form-group">
                <label class="form-label">Hora</label>
                <input type="time" v-model="time" class="form-input" required />
              </div>
            </div>

            <!-- Categoría -->
            <div class="form-group">
              <label class="form-label">Categoría</label>
              <select v-model="category" class="form-input" required>
                <option value="">Selecciona una categoría</option>
                <option value="concierto">Concierto</option>
                <option value="festival">Festival</option>
              </select>
            </div>

            <!-- Imagen -->
            <div class="form-group">
              <label class="form-label">Imagen del Evento</label>
              <div class="file-input-wrapper" @click="$refs.fileInput.click()">
                <input 
                  ref="fileInput"
                  type="file" 
                  @change="handleFile" 
                  class="form-file-input" 
                  accept="image/*" 
                />
                <div class="file-input-placeholder">
                  Haz clic para seleccionar una imagen
                </div>
              </div>
              <input type="url" v-model="imageUrlInput" class="form-input" placeholder="O pega una URL de imagen" style="margin-top: 8px;" />
              <small class="form-hint">JPG, PNG o WebP. Máximo 15MB</small>
            </div>

            <!-- Zonas -->
            <div class="form-group">
              <div class="zones-header">
                <label class="form-label">Zonas y Precios</label>
              </div>
              <p class="zone-helper">Define diferentes zonas con su precio y cantidad de entradas disponibles</p>
              <div class="zones-list">
                <div v-for="(zone, idx) in zones" :key="idx" class="zone-item">
                  <input type="text" v-model="zone.name" class="zone-input" placeholder="General, VIP, Premium..." required />
                  <div class="zone-inputs">
                    <div class="input-with-prefix">
                      <input type="number" v-model.number="zone.price" class="zone-input" placeholder="0.00" min="0" step="0.01" required />
                      <span class="prefix">€</span>
                    </div>
                    <input type="number" v-model.number="zone.availableTickets" class="zone-input" placeholder="Cantidad" min="0" required />
                  </div>
                </div>
              </div>
              <button type="button" @click="zones.push({name: '', price: 0, availableTickets: 0})" class="btn-add-zone">
                Añadir Zona
              </button>
            </div>

            <!-- Botones -->
            <div class="form-actions">
              <button type="submit" class="btn btn-primary">
                {{ editingEvent ? 'Actualizar Evento' : 'Crear Evento' }}
              </button>
              <button v-if="editingEvent" type="button" @click="resetForm" class="btn btn-secondary">
                Cancelar
              </button>
            </div>
          </form>
        </div>
      </section>

      <!-- Sección Eventos -->
      <section class="events-section">
        <div class="section-header">
          <h2 class="section-title">Tus Eventos</h2>
          <p class="section-description">Visualiza y gestiona los eventos que has creado</p>
        </div>

        <div class="events-wrapper">
          <!-- Empty state -->
          <div v-if="!eventsData?.length" class="empty-state">
            <p class="empty-message">No hay eventos aún. Crea tu primer evento para comenzar.</p>
          </div>

          <!-- Events grid -->
          <div v-else class="events-grid">
            <div v-for="event in eventsData" :key="event.id" class="event-card">
              <div class="card-image">
                <img :src="event.image" :alt="event.title" />
                <span class="category-tag">{{ event.category }}</span>
              </div>
              <div class="card-content">
                <h3 class="card-title">{{ event.title }}</h3>
                <div class="card-info">
                  <p class="info-item"><strong>{{ event.venue }}</strong> — {{ event.location }}</p>
                  <p class="info-item">{{ formatDateTime(event.date, event.time, $i18n.locale) }}</p>
                </div>
                <div class="card-zones">
                  <div v-for="zone in event.zones" :key="zone.name" class="zone-info">
                    <span class="zone-name">{{ zone.name }}</span>
                    <span class="zone-price">{{ zone.price }}€</span>
                    <span class="zone-tickets">{{ zone.availableTickets }} tickets</span>
                  </div>
                </div>
              </div>
              <div class="card-actions">
                <button @click="editEvent(event)" class="action-btn edit-btn">Editar</button>
                <button @click="deleteEventFn(event.id)" class="action-btn delete-btn">Eliminar</button>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { formatDateForInput, formatDateTime } from '~/utils/formatDate'

definePageMeta({
  middleware: 'super-user-only'
})

useHead({
  title: 'Crear Evento | GoLive',
  meta: [
    { name: 'description', content: 'Administra y crea tus eventos musicales de forma profesional con GoLive' },
    { property: 'og:title', content: 'Crear Evento | GoLive' },
    { property: 'og:description', content: 'Administra y crea tus eventos musicales de forma profesional' }
  ]
})

const { getEvents, createEvent, updateEvent, deleteEvent } = useEvents()

// Declarar antes de useAsyncData
const title = ref(''), venue = ref(''), location = ref(''), date = ref(''), time = ref(''), category = ref('')
const imageUrlInput = ref(''), zones = ref([{ name: 'General', price: 50, availableTickets: 100 }]), editingEvent = ref(null)
let imageFile = null

const { data: eventsData, refresh: refreshEvents } = await useAsyncData('events', () => getEvents(), { server: true })

const handleFile = (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (file.size > 15 * 1024 * 1024) {
    alert('Archivo demasiado grande (máx 15MB)')
    return
  }
  const reader = new FileReader()
  reader.onload = () => { imageFile = reader.result; imageUrlInput.value = '' }
  reader.readAsDataURL(file)
}

const saveEvent = async () => {
  const finalImage = imageFile || imageUrlInput.value || (editingEvent.value?.image || '')
  const totalTickets = zones.value.reduce((s, z) => s + (Number(z.availableTickets) || 0), 0)
  
  const eventData = {
    title: title.value.trim(),
    venue: venue.value.trim(),
    location: location.value.trim(),
    date: new Date(date.value).toISOString(),
    time: time.value,
    category: category.value,
    image: finalImage,
    availableTickets: totalTickets,
    zones: zones.value.map(z => ({ name: z.name.trim(), price: Number(z.price), availableTickets: Number(z.availableTickets) })).filter(z => z.name)
  }

  try {
    editingEvent.value ? await updateEvent(editingEvent.value.id, eventData) : await createEvent(eventData)
    resetForm()
    await refreshEvents()
  } catch (error) {
    alert(`Error: ${error?.data?.message || error?.message || 'Error desconocido'}`)
  }
}

const deleteEventFn = async (id) => {
  if (confirm('¿Estás seguro de que deseas eliminar este evento?')) {
    try { await deleteEvent(id); await refreshEvents() } catch {}
  }
}

const editEvent = (event) => {
  editingEvent.value = event
  title.value = event.title
  venue.value = event.venue
  location.value = event.location
  date.value = formatDateForInput(event.date)
  time.value = event.time
  category.value = event.category
  imageFile = null
  imageUrlInput.value = event.image || ''
  zones.value = event.zones?.length ? [...event.zones] : [{ name: 'General', price: 50, availableTickets: 100 }]
}

const resetForm = () => {
  title.value = venue.value = location.value = date.value = time.value = category.value = imageUrlInput.value = ''
  imageFile = null
  zones.value = [{ name: 'General', price: 50, availableTickets: 100 }]
  editingEvent.value = null
}
</script>

<style scoped>
* {
  font-family: 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.event-admin-container {
  background: #ffffff;
  min-height: 100vh;
}

/* Encabezado */
.admin-header {
  display: none;
}

/* Contenedor principal */
.container-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px;
}

/* Secciones */
.form-section,
.events-section {
  padding: 40px;
  border-bottom: 1px solid #e5e7eb;
}

.form-section {
  padding-top: 80px;
}

.section-header {
  margin-bottom: 32px;
}

.section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

.section-description {
  font-size: 1.05rem;
  color: #6b7280;
  margin: 8px 0 0 0;
  font-weight: 400;
}

/* Formulario */
.form-wrapper {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 32px;
}

.event-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: 0.2px;
}

.form-input {
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.95rem;
  font-family: 'Poppins', inherit;
  transition: all 0.2s ease;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: #1a1a2e;
  box-shadow: 0 0 0 2px rgba(26, 26, 46, 0.1);
  background: #f9f9fb;
}

.form-input option {
  background: white;
  color: #1a1a2e;
}

.form-input option:hover {
  background: #0a0a0a;
  color: white;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* Archivo */
.file-input-wrapper {
  position: relative;
  border: 2px dashed #d1d5db;
  border-radius: 6px;
  padding: 24px;
  text-align: center;
  transition: all 0.2s ease;
  cursor: pointer;
  background: #f9fafb;
}

.file-input-wrapper:hover {
  border-color: #1a1a2e;
  background: #f3f4f6;
}

.form-file-input {
  display: none;
}

.file-input-placeholder {
  color: #6b7280;
  font-size: 0.95rem;
  font-weight: 500;
}

.form-hint {
  font-size: 0.8rem;
  color: #9ca3af;
  display: block;
  margin-top: 6px;
}

/* Zonas */
.zones-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.zone-helper {
  font-size: 0.9rem;
  color: #6b7280;
  background: #f3f4f6;
  padding: 10px 12px;
  border-radius: 6px;
  margin-bottom: 12px;
}

.zones-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.zone-item {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr;
  gap: 16px;
  padding: 20px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  align-items: center;
}

.zone-input {
  padding: 14px 16px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 1.1rem;
  font-family: 'Poppins', inherit;
  transition: all 0.2s ease;
  width: 100%;
}

.zone-input:focus {
  outline: none;
  border-color: #0f766e;
  background: white;
}

.zone-inputs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  align-items: center;
}

.input-with-prefix {
  position: relative;
  display: flex;
  align-items: center;
}

.input-with-prefix .zone-input {
  padding-right: 32px;
  width: 100%;
}

.prefix {
  position: absolute;
  right: 12px;
  color: #9ca3af;
  font-size: 0.95rem;
  font-weight: 600;
  pointer-events: none;
}

.btn-add-zone {
  padding: 10px 16px;
  border: 1px solid #0a0a0a;
  background: transparent;
  color: #0a0a0a;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Poppins', inherit;
}

.btn-add-zone:hover {
  background: #0a0a0a;
  color: white;
}

/* Botones */
.form-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.btn {
  padding: 12px 40px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.95rem;
  font-family: 'Poppins', inherit;
  text-align: center;
  min-width: 200px;
}

.btn-primary {
  background: #0a0a0a;
  color: white;
}

.btn-primary:hover {
  background: #262626;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.btn-secondary {
  background: #f3f4f6;
  color: #6b7280;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

/* Eventos */
.events-wrapper {
  display: flex;
  flex-direction: column;
}

.empty-state {
  text-align: center;
  padding: 60px 40px;
  color: #9ca3af;
}

.empty-message {
  font-size: 0.95rem;
  margin: 0;
}

.events-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.event-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.event-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #f3f4f6;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.event-card:hover .card-image img {
  transform: scale(1.05);
}

.category-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: capitalize;
  backdrop-filter: blur(4px);
}

.card-content {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 10px 0;
  line-height: 1.3;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
  flex: 1;
}

.info-item {
  font-size: 0.9rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

.card-zones {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
  padding-top: 10px;
  border-top: 1px solid #e5e7eb;
}

.zone-info {
  display: flex;
  gap: 6px;
  align-items: center;
  font-size: 0.8rem;
}

.zone-name {
  background: #f3f4f6;
  color: #1a1a2e;
  padding: 3px 8px;
  border-radius: 3px;
  font-weight: 600;
}

.zone-price {
  color: #0a0a0a;
  font-weight: 600;
}

.zone-tickets {
  color: #6b7280;
  font-size: 0.75rem;
}

/* Acciones */
.card-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
}

.action-btn {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Poppins', inherit;
  text-align: center;
}

.edit-btn {
  background: #0a0a0a;
  color: white;
  border: 2px solid #0a0a0a;
  font-weight: 600;
}

.edit-btn:hover {
  background: #262626;
  color: white;
  border-color: #262626;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.delete-btn {
  background: #991b1b;
  color: white;
  border: 2px solid #991b1b;
  font-weight: 600;
}

.delete-btn:hover {
  background: #7f1515;
  color: white;
  border-color: #7f1515;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(153, 27, 27, 0.2);
}

/* Responsivo */
@media (max-width: 768px) {
  .admin-header {
    flex-direction: column;
    text-align: center;
    padding: 32px 20px;
    gap: 20px;
  }

  .header-content h1 {
    font-size: 1.5rem;
  }

  .header-stats {
    justify-content: center;
  }

  .form-section,
  .events-section {
    padding: 24px 20px;
  }

  .form-wrapper {
    padding: 20px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .zone-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .form-actions {
    grid-template-columns: 1fr;
  }

  .events-grid {
    grid-template-columns: 1fr;
  }

  .card-actions {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 480px) {
  .admin-header {
    padding: 24px 16px;
  }

  .header-content h1 {
    font-size: 1.3rem;
  }

  .form-section,
  .events-section {
    padding: 16px;
  }

  .form-wrapper {
    padding: 16px;
  }

  .section-title {
    font-size: 1.2rem;
  }

  .events-grid {
    gap: 12px;
  }

  .card-image {
    height: 150px;
  }
}
</style>

<style>
html, body {
  overflow-x: hidden;
  background: #ffffff;
  font-family: 'Poppins', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Estilos del input date y time */
input[type="date"],
input[type="time"] {
  font-family: 'Poppins', inherit !important;
}

input[type="date"]::-webkit-calendar-picker-indicator,
input[type="time"]::-webkit-calendar-picker-indicator {
  cursor: pointer;
  border-radius: 4px;
  margin-right: 4px;
  opacity: 0.6;
  filter: invert(0.2);
}

input[type="date"]::-webkit-calendar-picker-indicator:hover,
input[type="time"]::-webkit-calendar-picker-indicator:hover {
  opacity: 1;
}

/* Estilos para Firefox */
input[type="date"]::-moz-calendar-picker-indicator,
input[type="time"]::-moz-calendar-picker-indicator {
  cursor: pointer;
  opacity: 0.6;
}

input[type="date"]::-moz-calendar-picker-indicator:hover,
input[type="time"]::-moz-calendar-picker-indicator:hover {
  opacity: 1;
}
</style>
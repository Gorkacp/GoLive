<template>
  <div class="backoffice-page">
    <ClientOnly>
      <Header />
    </ClientOnly>

    <button
      v-if="!isDesktop"
      type="button"
      class="sidebar-toggle"
      :class="{ open: isSidebarOpen }"
      @click="toggleSidebar"
      aria-label="Alternar menú lateral"
    >
      {{ isSidebarOpen ? '>' : '<' }}
    </button>
    <div
      v-if="!isDesktop"
      class="sidebar-overlay"
      :class="{ visible: isSidebarOpen }"
      @click="toggleSidebar"
    ></div>

    <div class="backoffice-shell">
      <aside
        class="backoffice-sidebar"
        :class="{
          'mobile-sidebar': !isDesktop,
          open: isSidebarOpen || isDesktop
        }"
      >
        <div class="sidebar-header">
          <p class="eyebrow">Panel</p>
          <h2>Backoffice</h2>
          <p class="sidebar-user">
            <span>{{ userData?.name }}</span>
            <small>{{ userRoleLabel }}</small>
          </p>
        </div>

        <nav class="sidebar-nav">
          <button
            v-for="item in navigation"
            :key="item.id"
            class="nav-chip"
            :class="{ active: activeSection === item.id }"
            @click="handleNavClick(item.id)"
          >
            <i :class="item.icon"></i>
            {{ item.label }}
          </button>
        </nav>

        <div class="sidebar-stats">
          <p class="sidebar-label">Eventos activos</p>
          <h3>{{ dashboard?.upcomingEvents || 0 }}</h3>
          <div class="progress">
            <div
              class="progress-bar"
              :style="{ width: `${dashboard?.averageOccupancy || 0}%` }"
            ></div>
          </div>
          <small>Ocupación media {{ dashboard?.averageOccupancy || 0 }}%</small>
        </div>
      </aside>

      <main class="backoffice-content">
        <header class="content-header">
          <div>
            <p class="eyebrow">Bienvenido de nuevo</p>
            <h1>Control total de tus eventos</h1>
            <p class="subtitle">
              Revisa resultados en tiempo real, gestiona ventas y publica nuevos eventos sin salir de este panel.
            </p>
          </div>
          <button class="btn-primary" @click="openNewEventForm">
            <i class="fas fa-plus me-2"></i>
            Nuevo evento
          </button>
        </header>

        <section
          class="metrics-grid"
          id="dashboard"
          ref="dashboardSection"
          v-if="activeSection === 'dashboard'"
        >
          <div class="metric-card" v-for="metric in metricCards" :key="metric.title">
            <div class="metric-icon">
              <i :class="metric.icon"></i>
            </div>
            <p class="metric-label">{{ metric.title }}</p>
            <h3 class="metric-value">{{ metric.value }}</h3>
            <small class="metric-foot">{{ metric.footnote }}</small>
          </div>
        </section>

        <section class="panel-group full-width-panel-group" id="events" ref="eventsSection">
          <div class="panel performance-panel full-width-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Eventos</p>
                <h2>Rendimiento en vivo</h2>
              </div>
              <button class="btn-outline" @click="refreshData" :disabled="loadingDashboard">
                <i class="fas fa-sync me-2"></i>Actualizar
              </button>
            </div>

            <div v-if="loadingDashboard" class="panel-loading">
              <div class="spinner-border text-light" role="status"></div>
              <p>Cargando estadísticas...</p>
            </div>

            <div v-else-if="!mergedEvents.length" class="panel-empty">
              <p>No tienes eventos registrados todavía.</p>
              <small>Crea tu primer evento para comenzar a vender entradas.</small>
            </div>

            <div v-else class="event-list">
              <article
                class="event-card"
                v-for="event in mergedEvents"
                :key="event.id"
                :class="{ expanded: expandedEventId === event.id }"
                @click="viewEventDetails(event)"
              >
                <div class="event-cover">
                  <img :src="event.image || fallbackImage" :alt="event.title" loading="lazy" />
                  <span class="badge">{{ event.category }}</span>
                  <button class="icon-button" @click.stop="selectEvent(event)">
                    <i class="fas fa-edit"></i>
                  </button>
                </div>
                <div class="event-body">
                  <div class="event-meta">
                    <h3>{{ event.title }}</h3>
                    <p>{{ event.venue }} · {{ event.location }}</p>
                    <small>{{ formatDate(event.date, event.time) }}</small>
                  </div>
                  <div class="event-stats">
                    <div class="stat">
                      <p>Entradas vendidas</p>
                      <h4>{{ event.performance?.soldTickets || 0 }} / {{ event.availableTickets }}</h4>
                    </div>
                    <div class="stat">
                      <p>Ingresos</p>
                      <h4>{{ formatCurrency(event.performance?.grossRevenue) }}</h4>
                    </div>
                    <div class="stat">
                      <p>Ocupación</p>
                      <h4>{{ event.performance?.occupancy || 0 }}%</h4>
                    </div>
                  </div>
                  <div class="event-progress">
                    <div class="progress">
                      <div class="progress-bar" :style="{ width: `${event.performance?.occupancy || 0}%` }"></div>
                    </div>
                    <div class="event-actions">
                      <button class="btn-text" @click.stop="selectEvent(event)">
                        <i class="fas fa-pen me-2"></i>Editar
                      </button>
                      <button class="btn-text danger" @click.stop="confirmDeletion(event.id)">
                        <i class="fas fa-trash me-2"></i>Eliminar
                      </button>
                    </div>
                  </div>
                </div>
                <div class="event-detail" v-if="expandedEventId === event.id">
                  <div v-if="attendeesLoading" class="detail-loading">
                    <div class="spinner-border text-light" role="status"></div>
                    <p>Cargando asistentes...</p>
                  </div>
                  <div v-else-if="attendeesError" class="detail-error">
                    <i class="fas fa-exclamation-triangle me-2"></i>{{ attendeesError }}
                  </div>
                  <div v-else>
                    <div class="detail-metrics" v-if="getEventDetails(event.id)">
                      <div class="metric">
                        <p style="color: #fff;">Entradas gestionadas</p>
                        <h4 style="color: #fff;">{{ getEventDetails(event.id)?.totalSold || 0 }}</h4>
                      </div>
                      <div class="metric">
                        <p style="color: #fff;">Ingresos confirmados</p>
                        <h4 style="color: #fff;">{{ formatCurrency(getEventDetails(event.id)?.grossRevenue || 0) }}</h4>
                      </div>
                    </div>
                    <div
                      v-if="getEventDetails(event.id)?.attendees?.length"
                      class="attendee-table-wrapper"
                    >
                      <table class="attendee-table">
                        <thead>
                            <tr>
                              <th style="color: #fff;">Asistente</th>
                              <th style="color: #fff;">Correo</th>
                              <th style="color: #fff;">Zona</th>
                              <th style="color: #fff;">Precio</th>
                              <th style="color: #fff;">Emitido</th>
                            </tr>
                        </thead>
                        <tbody>
                          <tr
                            v-for="attendee in getEventDetails(event.id)?.attendees"
                            :key="attendee.ticketId"
                          >
                            <td style="color: #fff;">
                              <strong>{{ attendee.attendeeName || 'Sin nombre' }}</strong><br>
                              <small style="color: #fff;">Ticket {{ attendee.ticketNumber }}</small>
                            </td>
                            <td style="color: #fff;">{{ attendee.attendeeEmail || 'Sin email' }}</td>
                            <td style="color: #fff;">{{ attendee.zoneName }} <small v-if="attendee.insurance" style="color: #fff;">(Seguro)</small></td>
                            <td style="color: #fff;">{{ formatCurrency(attendee.price + attendee.serviceFee) }}</td>
                            <td style="color: #fff;">{{ attendee.issuedAt ? new Date(attendee.issuedAt).toLocaleDateString() : '-' }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <div v-else class="panel-empty small">
                      <p>Aún no hay asistentes registrados para este evento.</p>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </div>

          <div class="panel form-panel" id="form" ref="formSection" v-if="showFormPanel">
            <button class="panel-close" type="button" @click="closeFormPanel">
              <i class="fas fa-times"></i>
            </button>
            
            <div class="form-section-header">
              <h2 class="form-section-title">{{ editingEvent ? 'Actualizar Evento' : 'Crear Nuevo Evento' }}</h2>
              <p class="form-section-description">
                {{ editingEvent ? 'Modifica los datos del evento' : 'Completa los datos del evento para agregarlo a tu catálogo' }}
              </p>
            </div>

            <div class="form-wrapper">
              <form class="event-form" @submit.prevent="handleSubmit">
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
                  <input type="url" v-model="imageUrlInput" class="form-input" placeholder="Pega una URL de imagen" />
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
                      <button v-if="zones.length > 1" type="button" @click="removeZone(idx)" class="zone-remove-btn">
                        <i class="fas fa-times"></i>
                      </button>
                    </div>
                  </div>
                  <button type="button" @click="addZone" class="btn-add-zone">
                    Añadir Zona
                  </button>
                </div>

                <!-- Botones -->
                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" :disabled="saving">
                    <span v-if="saving">
                      <i class="fas fa-circle-notch fa-spin me-2"></i>Guardando...
                    </span>
                    <span v-else>
                      {{ editingEvent ? 'Actualizar Evento' : 'Crear Evento' }}
                    </span>
                  </button>
                  <button v-if="editingEvent" type="button" @click="resetForm" class="btn btn-secondary">
                    Cancelar
                  </button>
                </div>
              </form>
            </div>

            <transition name="fade">
              <div class="alert success" v-if="successMessage">
                <i class="fas fa-check-circle me-2"></i>{{ successMessage }}
              </div>
            </transition>
            <transition name="fade">
              <div class="alert error" v-if="errorMessage">
                <i class="fas fa-exclamation-triangle me-2"></i>{{ errorMessage }}
              </div>
            </transition>
          </div>

          <div
            class="panel form-panel placeholder-panel"
            v-else-if="activeSection !== 'dashboard'"
          >
            <div class="placeholder-content">
              <p class="eyebrow">Gestión</p>
              <h2>¿Listo para crear o editar?</h2>
              <p class="subtitle">
                Selecciona un evento existente desde el listado o pulsa “Nuevo evento” para abrir el formulario avanzado.
              </p>
              <button class="btn-primary" type="button" @click="openNewEventForm">
                <i class="fas fa-plus me-2"></i>Crear evento
              </button>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { nextTick } from 'vue'
import { formatDateForInput, formatDateTime } from '~/utils/formatDate'

definePageMeta({
  middleware: 'super-user-only'
})

useHead({
  title: 'Backoffice | GoLive',
  meta: [
    { name: 'description', content: 'Dashboard profesional para gestionar eventos, ventas y ganancias.' }
  ]
})

const fallbackImage = '/logo.svg'
const activeSection = ref('dashboard')
const loadingDashboard = ref(false)
const saving = ref(false)
const successMessage = ref('')
const errorMessage = ref('')
const userData = ref(null)
const dashboard = ref(null)
const events = ref([])
const dashboardSection = ref(null)
const eventsSection = ref(null)
const formSection = ref(null)
const showFormPanel = ref(false)
const expandedEventId = ref(null)
const attendeesCache = ref({})
const attendeesLoading = ref(false)
const attendeesError = ref('')
const isDesktop = ref(true)
const isSidebarOpen = ref(true)

const title = ref('')
const venue = ref('')
const location = ref('')
const date = ref('')
const time = ref('')
const category = ref('')
const imageUrlInput = ref('')
const zones = ref([{ name: 'General', price: 40, availableTickets: 100 }])
const editingEvent = ref(null)

const { getManagedEvents, createEvent, updateEvent, deleteEvent, getEventAttendees } = useEvents()
const { fetchOverview } = useDashboard()

const navigation = [
  { id: 'dashboard', label: 'Dashboard', icon: 'fas fa-chart-line' },
  { id: 'events', label: 'Eventos', icon: 'fas fa-music' },
  { id: 'form', label: 'Nuevo evento', icon: 'fas fa-plus-circle' }
]

const updateViewport = () => {
  if (!process.client) return
  const desktop = window.innerWidth >= 1024
  isDesktop.value = desktop
  isSidebarOpen.value = desktop
}

const toggleSidebar = () => {
  if (isDesktop.value) return
  isSidebarOpen.value = !isSidebarOpen.value
}

const scrollToSection = (sectionId) => {
  const target = {
    dashboard: dashboardSection.value,
    events: eventsSection.value,
    form: formSection.value
  }[sectionId]

  if (target) {
    target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

const goToSection = (sectionId) => {
  activeSection.value = sectionId
  nextTick(() => scrollToSection(sectionId))
}

const handleNavClick = (sectionId) => {
  if (sectionId === 'form') {
    showFormPanel.value = true
  }
  goToSection(sectionId)
  if (!isDesktop.value) {
    isSidebarOpen.value = false
  }
}

const getEventDetails = (eventId) => {
  return attendeesCache.value[eventId] || null
}

const userRoleLabel = computed(() => {
  const role = (userData.value?.role || '').toLowerCase()
  if (role === 'super_user') return 'Super usuario'
  if (role === 'admin') return 'Administrador'
  return 'Manager'
})

const metricCards = computed(() => {
  return [
    {
      title: 'Eventos publicados',
      value: dashboard.value?.totalEvents || 0,
      footnote: `${dashboard.value?.upcomingEvents || 0} activos`,
      icon: 'fas fa-bullhorn'
    },
    {
      title: 'Entradas vendidas',
      value: dashboard.value?.totalTicketsSold || 0,
      footnote: 'Últimos 12 meses',
      icon: 'fas fa-ticket-alt'
    },
    {
      title: 'Ingresos totales',
      value: formatCurrency(dashboard.value?.totalRevenue),
      footnote: 'Bruto',
      icon: 'fas fa-euro-sign'
    },
    {
      title: 'Ganancia estimada',
      value: formatCurrency(dashboard.value?.netRevenue),
      footnote: 'Después de comisiones',
      icon: 'fas fa-chart-pie'
    }
  ]
})

const performanceMap = computed(() => {
  const map = {}
  dashboard.value?.events?.forEach(event => {
    map[event.eventId] = event
  })
  return map
})

const mergedEvents = computed(() => {
  return events.value.map(event => ({
    ...event,
    performance: performanceMap.value[event.id] || null
  }))
})

const formatCurrency = (value = 0) => {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
  }).format(value || 0)
}

const formatDate = (date, eventTime) => {
  return formatDateTime(date, eventTime, 'es')
}

const loadUser = () => {
  if (process.client) {
    const userStr = sessionStorage.getItem('user')
    if (userStr) {
      userData.value = JSON.parse(userStr)
    }
  }
}

const refreshData = async () => {
  if (!userData.value?.id) return
  loadingDashboard.value = true
  try {
    const role = (userData.value?.role || '').toLowerCase()
    const options = role === 'super_user' ? {} : { userId: userData.value.id }
    const [userEvents, overview] = await Promise.all([
      getManagedEvents(userData.value),
      fetchOverview(options)
    ])
    events.value = userEvents || []
    dashboard.value = overview
  } catch (error) {
    errorMessage.value = error?.message || 'No se pudo cargar la información'
  } finally {
    loadingDashboard.value = false
  }
}

const viewEventDetails = async (event) => {
  if (!event?.id) return
  attendeesError.value = ''
  if (expandedEventId.value === event.id) {
    expandedEventId.value = null
    return
  }
  expandedEventId.value = event.id
  attendeesLoading.value = true
  try {
    if (!attendeesCache.value[event.id]) {
      const details = await getEventAttendees(event.id)
      attendeesCache.value = { ...attendeesCache.value, [event.id]: details }
    }
  } catch (error) {
    attendeesError.value = error?.data?.error || error?.message || 'No se pudo cargar la información'
  } finally {
    attendeesLoading.value = false
  }
}

const addZone = () => {
  zones.value = [...zones.value, { name: '', price: 0, availableTickets: 0 }]
}

const removeZone = (index) => {
  if (zones.value.length === 1) return
  zones.value = zones.value.filter((_, idx) => idx !== index)
}

const selectEvent = (event) => {
  editingEvent.value = event
  title.value = event.title
  venue.value = event.venue
  location.value = event.location
  date.value = formatDateForInput(event.date)
  time.value = event.time
  category.value = event.category
  imageUrlInput.value = event.image || ''
  zones.value = event.zones?.length ? JSON.parse(JSON.stringify(event.zones)) : [{ name: 'General', price: 40, availableTickets: 100 }]
  showFormPanel.value = true
  goToSection('form')
}

const resetForm = () => {
  editingEvent.value = null
  title.value = ''
  venue.value = ''
  location.value = ''
  date.value = ''
  time.value = ''
  category.value = ''
  imageUrlInput.value = ''
  zones.value = [{ name: 'General', price: 40, availableTickets: 100 }]
}

const openNewEventForm = () => {
  resetForm()
  showFormPanel.value = true
  goToSection('form')
}

const closeFormPanel = () => {
  showFormPanel.value = false
  resetForm()
  goToSection('dashboard')
}

const buildPayload = () => {
  const totalTickets = zones.value.reduce((sum, zone) => sum + Number(zone.availableTickets || 0), 0)
  return {
    title: title.value.trim(),
    venue: venue.value.trim(),
    location: location.value.trim(),
    date: new Date(date.value).toISOString(),
    time: time.value,
    category: category.value,
    image: imageUrlInput.value || editingEvent.value?.image || '',
    availableTickets: totalTickets,
    zones: zones.value.map(zone => ({
      name: zone.name.trim(),
      price: Number(zone.price) || 0,
      availableTickets: Number(zone.availableTickets) || 0
    }))
  }
}

const handleSubmit = async () => {
  if (!userData.value?.id) {
    errorMessage.value = 'No se pudo identificar al usuario'
    return
  }
  saving.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const payload = buildPayload()
    if (editingEvent.value) {
      await updateEvent(editingEvent.value.id, payload)
      successMessage.value = 'Evento actualizado correctamente'
    } else {
      await createEvent(payload)
      successMessage.value = 'Evento creado con éxito'
    }
    resetForm()
    await refreshData()
  } catch (error) {
    errorMessage.value = error?.data?.message || error?.message || 'Error al guardar el evento'
  } finally {
    saving.value = false
  }
}

const confirmDeletion = async (eventId) => {
  if (!eventId) return
  if (confirm('¿Seguro que deseas eliminar este evento?')) {
    try {
      await deleteEvent(eventId)
      successMessage.value = 'Evento eliminado'
      if (expandedEventId.value === eventId) {
        expandedEventId.value = null
      }
      const cache = { ...attendeesCache.value }
      delete cache[eventId]
      attendeesCache.value = cache
      await refreshData()
    } catch (error) {
      errorMessage.value = error?.data?.message || error?.message || 'No se pudo eliminar el evento'
    }
  }
}

onMounted(async () => {
  loadUser()
  if (process.client) {
    updateViewport()
    window.addEventListener('resize', updateViewport)
  }
  if (!userData.value) {
    return navigateTo('/login')
  }
  await refreshData()
})

onUnmounted(() => {
  successMessage.value = ''
  errorMessage.value = ''
  if (process.client) {
    window.removeEventListener('resize', updateViewport)
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

:global(body) {
  background: #050505;
}

.backoffice-page {
  min-height: 100vh;
  background: linear-gradient(120deg, #050505 0%, #0f0f0f 60%, #080808 100%);
  font-family: 'Poppins', sans-serif;
  padding-top: 86px;
  overflow-x: hidden;
}

.backoffice-shell {
  box-sizing: border-box;
  width: min(1400px, 100%);
  margin-left: 0;
  margin-right: auto;
  display: grid;
  grid-template-columns: 270px minmax(0, 1fr);
  gap: 32px;
  align-items: stretch;
  padding: 0 clamp(16px, 4vw, 48px) 40px 0;
}

.backoffice-sidebar {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0 24px 24px 0;
  padding: 0 28px 28px 28px;
  position: sticky;
  top: 0;
  height: fit-content;
  margin-left: 0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.45);
  transition: transform 0.3s ease;
}

.backoffice-sidebar.mobile-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: min(320px, 85vw);
  max-width: 360px;
  transform: translateX(-110%);
  border-radius: 0 24px 24px 0;
  padding-top: 96px;
  padding-left: 24px;
  padding-right: 24px;
  overflow-y: auto;
  z-index: 1200;
  pointer-events: none;
  background: #0a0a0a;
}

.backoffice-sidebar.mobile-sidebar.open {
  transform: translateX(0);
  pointer-events: auto;
}

.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
  z-index: 1100;
}

.sidebar-overlay.visible {
  opacity: 1;
  pointer-events: auto;
}

.sidebar-toggle {
  position: fixed;
  top: 100px;
  left: 0;
  width: 48px;
  height: 64px;
  border: none;
  border-radius: 0 32px 32px 0;
  background: linear-gradient(135deg, #ff0057, #ff8a00);
  color: #fff;
  font-size: 1.4rem;
  font-weight: 600;
  display: none;
  align-items: center;
  justify-content: center;
  z-index: 1300;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
}

.sidebar-toggle.open {
  left: calc(min(320px, 85vw) - 12px);
}

.sidebar-header h2 {
  color: #fff;
  font-size: 1.8rem;
  margin-bottom: 4px;
}

.sidebar-user span {
  display: block;
  color: #fff;
  font-weight: 600;
}

.sidebar-user small {
  color: rgba(255, 255, 255, 0.6);
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 2px;
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 4px;
}

.eyebrow.small {
  font-size: 0.7rem;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin: 24px 0;
}

.nav-chip {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 12px 16px;
  color: #f1f1f1;
  text-align: left;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-chip i {
  margin-right: 8px;
}

.nav-chip.active,
.nav-chip:hover {
  border-color: rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.08);
}

.sidebar-stats {
  margin-top: 24px;
}

.sidebar-stats h3 {
  color: #fff;
  font-size: 2.2rem;
}

.progress {
  width: 100%;
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  overflow: hidden;
  margin: 12px 0 6px;
}

.progress-bar {
  height: 8px;
  background: linear-gradient(90deg, #ff0057, #ff8a00);
}

.sidebar-stats small {
  color: rgba(255, 255, 255, 0.7);
}

.backoffice-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.content-header {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
}

.content-header h1 {
  color: #fff;
  font-size: 2.4rem;
  margin-bottom: 8px;
}

.subtitle {
  color: rgba(255, 255, 255, 0.65);
  margin: 0;
}

.btn-primary {
  background: linear-gradient(135deg, #ff0057, #ff8a00);
  color: #fff;
  border: none;
  border-radius: 16px;
  padding: 14px 24px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.btn-primary:hover {
  transform: translateY(-1px);
}

.btn-outline {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  color: #fff;
  padding: 10px 18px;
  cursor: pointer;
}

.btn-outline.small {
  padding: 8px 14px;
  font-size: 0.85rem;
}

.btn-text {
  background: transparent;
  border: none;
  color: #fff;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
}

.btn-text.danger {
  color: #ff6b6b;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.metric-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 20px;
}

.metric-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff8a00;
  margin-bottom: 12px;
}

.metric-label {
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.metric-value {
  color: #fff;
  font-size: 2rem;
  margin: 4px 0;
}

.metric-foot {
  color: rgba(255, 255, 255, 0.5);
}

.panel-group {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(340px, 0.8fr);
  gap: 24px;
  align-items: flex-start;
}

.panel {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 28px;
  position: relative;
}

.panel-close {
  position: absolute;
  top: 18px;
  right: 18px;
  background: rgba(255, 255, 255, 0.06);
  border: none;
  color: #fff;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
}

.placeholder-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
}

.placeholder-content {
  text-align: center;
  max-width: 360px;
}

.panel-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.panel-heading h2 {
  color: #fff;
  margin: 0;
}

.panel-loading,
.panel-empty {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: rgba(255, 255, 255, 0.7);
  gap: 12px;
}

.panel-empty.small {
  min-height: 120px;
}

.event-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.event-card {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 20px;
  background: rgba(0, 0, 0, 0.35);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.event-card.expanded {
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.event-cover {
  position: relative;
  height: 100%;
  min-height: 180px;
  display: flex;
  align-items: stretch;
}

.event-cover img {
  width: 100%;
  height: 100%;
  min-height: 180px;
  object-fit: cover;
  display: block;
  object-fit: cover;
}

.event-cover .badge {
  position: absolute;
  top: 16px;
  left: 16px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.75rem;
}

.icon-button {
  position: absolute;
  top: 16px;
  right: 16px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
}

.icon-button.danger {
  background: rgba(255, 0, 0, 0.6);
}

.event-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.event-detail {
  grid-column: 1 / -1;
  padding: 18px 20px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
}

.detail-metrics {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.detail-metrics .metric {
  flex: 1;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 14px;
  padding: 14px;
}

.detail-loading,
.detail-error {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.8);
}

.attendee-table-wrapper {
  overflow-x: auto;
}

.attendee-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.attendee-table th,
.attendee-table td {
  text-align: left;
  padding: 12px 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.attendee-table th {
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.attendee-table td small {
  color: rgba(255, 255, 255, 0.6);
}

.event-meta h3 {
  color: #fff;
  margin: 0;
}

.event-meta p,
.event-meta small {
  color: rgba(255, 255, 255, 0.65);
  margin: 2px 0;
}

.event-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.event-stats .stat {
  background: rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  padding: 12px;
}

.event-stats .stat p {
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.event-stats .stat h4 {
  color: #fff;
  margin: 4px 0 0;
}

.event-progress {
  margin-top: auto;
}

.event-actions {
  display: flex;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 8px;
}

.form-panel {
  position: sticky;
  top: 120px;
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  backdrop-filter: blur(10px);
}

.form-section-header {
  margin-bottom: 32px;
}

.form-section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #ff0057, #ff8a00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.form-section-description {
  font-size: 1.05rem;
  color: rgba(255, 255, 255, 0.65);
  margin: 0;
  font-weight: 400;
}

.form-wrapper {
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
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
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.2px;
}

.form-input {
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: 'Poppins', inherit;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.form-input:focus {
  outline: none;
  border-color: rgba(255, 0, 87, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1), 0 0 20px rgba(255, 138, 0, 0.2);
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-1px);
}

.form-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-input option {
  background: #1a1a1a;
  color: #fff;
}

.form-input select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23fff' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-hint {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.5);
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
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 12px 16px;
  border-radius: 10px;
  margin-bottom: 12px;
}

.zones-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 8px;
}

.zones-list::-webkit-scrollbar {
  width: 8px;
}

.zones-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.zones-list::-webkit-scrollbar-thumb {
  background: rgba(255, 138, 0, 0.3);
  border-radius: 10px;
}

.zones-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 138, 0, 0.5);
}

.zone-item {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1.5fr auto;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  align-items: center;
  transition: all 0.3s ease;
}

.zone-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.zone-input {
  padding: 14px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  font-size: 1rem;
  font-family: 'Poppins', inherit;
  transition: all 0.3s ease;
  width: 100%;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.zone-input:focus {
  outline: none;
  border-color: rgba(255, 0, 87, 0.5);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1);
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
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.95rem;
  font-weight: 600;
  pointer-events: none;
}

.zone-remove-btn {
  background: #991b1b;
  color: white;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.zone-remove-btn:hover {
  background: #7f1515;
  transform: scale(1.05);
}

.btn-add-zone {
  padding: 12px 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  border-radius: 10px;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: 'Poppins', inherit;
}

.btn-add-zone:hover {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.2), rgba(255, 138, 0, 0.2));
  border-color: rgba(255, 138, 0, 0.4);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 138, 0, 0.2);
}

.form-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.btn {
  padding: 14px 40px;
  border: none;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.95rem;
  font-family: 'Poppins', inherit;
  text-align: center;
  min-width: 200px;
}

.btn-primary {
  background: linear-gradient(135deg, #ff0057, #ff8a00);
  color: white;
  box-shadow: 0 4px 16px rgba(255, 0, 87, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(255, 0, 87, 0.4);
  background: linear-gradient(135deg, #ff0066, #ff9500);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.alert {
  margin-top: 18px;
  padding: 14px 18px;
  border-radius: 12px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.alert.success {
  background: rgba(76, 175, 80, 0.15);
  color: #8cf598;
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.alert.error {
  background: rgba(255, 87, 34, 0.15);
  color: #ffb199;
  border: 1px solid rgba(255, 87, 34, 0.3);
}

.form-panel .panel-close {
  background: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.form-panel .panel-close:hover {
  background: rgba(255, 0, 87, 0.2);
  color: #fff;
  border-color: rgba(255, 0, 87, 0.4);
}

@media (max-width: 1200px) {
  .backoffice-shell {
    grid-template-columns: 1fr;
    padding: 32px;
  }
  .backoffice-sidebar {
    position: static;
    border-radius: 24px;
  }
  .panel-group {
    grid-template-columns: 1fr;
  }
  .form-panel {
    position: static;
  }
}

@media (max-width: 1023px) {
  .sidebar-toggle {
    display: flex;
  }
  .backoffice-shell {
    padding: 20px 16px 32px;
  }
}

@media (max-width: 768px) {
  .content-header {
    flex-direction: column;
    padding: 20px;
    gap: 16px;
  }
  .content-header h1 {
    font-size: 1.75rem;
  }
  
  .backoffice-content {
    gap: 20px;
  }
  .panel {
    padding: 22px;
  }
  .event-card {
    grid-template-columns: 1fr;
  }
  .event-cover {
    height: 180px;
  }
  .form-two-col,
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .zone-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .zone-inputs {
    grid-template-columns: 1fr;
  }
  
  .form-wrapper {
    padding: 20px;
  }
  
  .form-section-title {
    font-size: 1.5rem;
  }
  
  .form-actions {
    width: 100%;
  }
  
  .btn {
    width: 100%;
  }
  .event-stats {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .metric-card {
    padding: 14px;
  }
  
  .metric-icon {
    width: 36px;
    height: 36px;
    margin-bottom: 10px;
  }
  
  .metric-value {
    font-size: 1.75rem;
  }
  
  .metric-label {
    font-size: 0.85rem;
  }
  
  .metric-foot {
    font-size: 0.75rem;
  }
}

@media (max-width: 600px) {
  .metrics-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .metric-card {
    padding: 12px;
    border-radius: 14px;
  }
  
  .metric-icon {
    width: 32px;
    height: 32px;
    margin-bottom: 8px;
    font-size: 0.9rem;
  }
  
  .metric-value {
    font-size: 1.5rem;
    margin: 2px 0;
  }
  
  .metric-label {
    font-size: 0.8rem;
    margin-bottom: 4px;
  }
  
  .metric-foot {
    font-size: 0.7rem;
  }
  
  .panel-heading {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
/* Panel de rendimiento en vivo a ancho completo */
.full-width-panel-group {
  grid-template-columns: 1fr !important;
}
.full-width-panel {
  width: 100%;
  max-width: 100%;
}

/* Estilos del input date y time */
.form-input[type="date"],
.form-input[type="time"] {
  font-family: 'Poppins', inherit !important;
  color-scheme: dark;
}

.form-input[type="date"]::-webkit-calendar-picker-indicator,
.form-input[type="time"]::-webkit-calendar-picker-indicator {
  cursor: pointer;
  border-radius: 4px;
  margin-right: 4px;
  opacity: 0.7;
  filter: invert(1) brightness(0.8);
  transition: all 0.2s ease;
}

.form-input[type="date"]::-webkit-calendar-picker-indicator:hover,
.form-input[type="time"]::-webkit-calendar-picker-indicator:hover {
  opacity: 1;
  filter: invert(1) brightness(1);
}

/* Estilos para Firefox */
.form-input[type="date"]::-moz-calendar-picker-indicator,
.form-input[type="time"]::-moz-calendar-picker-indicator {
  cursor: pointer;
  opacity: 0.7;
  filter: invert(1) brightness(0.8);
}

.form-input[type="date"]::-moz-calendar-picker-indicator:hover,
.form-input[type="time"]::-moz-calendar-picker-indicator:hover {
  opacity: 1;
  filter: invert(1) brightness(1);
}
</style>


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
                      <button class="btn-text" @click.stop="viewEventAttendees(event)">
                        <i class="fas fa-users me-2"></i>Ver asistentes
                      </button>
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
                    <p>Cargando estadísticas...</p>
                  </div>
                  <div v-else-if="attendeesError" class="detail-error">
                    <i class="fas fa-exclamation-triangle me-2"></i>{{ attendeesError }}
                  </div>
                  <div v-else-if="getEventDetails(event.id)" class="event-statistics">
                    <!-- Métricas principales -->
                    <div class="stats-grid">
                      <div class="stat-card">
                        <div class="stat-icon">
                          <i class="fas fa-users"></i>
                        </div>
                        <div class="stat-content">
                          <p class="stat-label">Total Asistentes</p>
                          <h3 class="stat-value">{{ getEventDetails(event.id)?.totalSold || 0 }}</h3>
                        </div>
                      </div>
                      <div class="stat-card">
                        <div class="stat-icon revenue">
                          <i class="fas fa-euro-sign"></i>
                        </div>
                        <div class="stat-content">
                          <p class="stat-label">Ingresos Totales</p>
                          <h3 class="stat-value">{{ formatCurrency(getEventDetails(event.id)?.grossRevenue || 0) }}</h3>
                        </div>
                      </div>
                      <div class="stat-card">
                        <div class="stat-icon occupancy">
                          <i class="fas fa-chart-pie"></i>
                        </div>
                        <div class="stat-content">
                          <p class="stat-label">Ocupación</p>
                          <h3 class="stat-value">{{ event.performance?.occupancy || 0 }}%</h3>
                        </div>
                      </div>
                      <div class="stat-card">
                        <div class="stat-icon tickets">
                          <i class="fas fa-ticket-alt"></i>
                        </div>
                        <div class="stat-content">
                          <p class="stat-label">Vendidas / Disponibles</p>
                          <h3 class="stat-value">{{ event.performance?.soldTickets || 0 }} / {{ event.availableTickets }}</h3>
                        </div>
                      </div>
                    </div>

                    <!-- Gráficos -->
                    <div class="charts-container">
                      <div class="chart-wrapper">
                        <h4 class="chart-title">Ventas por Zona</h4>
                        <div class="chart-canvas-wrapper">
                          <canvas class="zone-chart" :data-event-id="event.id"></canvas>
                        </div>
                      </div>
                      <div class="chart-wrapper">
                        <h4 class="chart-title">Distribución de Ocupación</h4>
                        <div class="chart-canvas-wrapper">
                          <canvas class="occupancy-chart" :data-event-id="event.id"></canvas>
                        </div>
                      </div>
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

    <!-- Modal de Asistentes -->
    <div v-if="showAttendeesModal" class="modal-overlay" @click.self="closeAttendeesModal">
      <div class="modal-container">
        <div class="modal-header">
          <h2>Asistentes del Evento</h2>
          <button class="modal-close" @click="closeAttendeesModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="modalAttendeesLoading" class="modal-loading">
            <div class="spinner-border text-light" role="status"></div>
            <p>Cargando asistentes...</p>
          </div>
          <div v-else-if="modalAttendeesError" class="modal-error">
            <i class="fas fa-exclamation-triangle me-2"></i>{{ modalAttendeesError }}
          </div>
          <div v-else-if="selectedEventAttendees?.length" class="attendees-content">
            <div class="attendees-summary">
              <p><strong>Total:</strong> {{ filteredAttendees.length }} de {{ selectedEventAttendees.length }} asistentes</p>
            </div>
            
            <!-- Barra de búsqueda -->
            <div class="attendees-search">
              <div class="search-input-wrapper">
                <i class="fas fa-search search-icon"></i>
                <input 
                  type="text" 
                  v-model="searchAttendeeQuery" 
                  placeholder="Buscar por nombre, email, ticket, zona..." 
                  class="search-input"
                />
                <button 
                  v-if="searchAttendeeQuery" 
                  @click="searchAttendeeQuery = ''" 
                  class="search-clear"
                >
                  <i class="fas fa-times"></i>
                </button>
              </div>
            </div>
            
            <!-- Vista de tabla para desktop -->
            <div class="attendees-table-container desktop-view" v-if="filteredAttendees.length > 0">
              <table class="attendees-table">
                <thead>
                  <tr>
                    <th>Asistente</th>
                    <th>Correo</th>
                    <th>Zona</th>
                    <th>Precio</th>
                    <th>Emitido</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="attendee in filteredAttendees" :key="attendee.ticketId">
                    <td>
                      <strong>{{ attendee.attendeeName || 'Sin nombre' }}</strong><br>
                      <small>Ticket {{ attendee.ticketNumber }}</small>
                    </td>
                    <td>{{ attendee.attendeeEmail || 'Sin email' }}</td>
                    <td>
                      {{ attendee.zoneName }}
                      <span v-if="attendee.insurance" class="insurance-badge">Seguro</span>
                    </td>
                    <td>{{ formatCurrency(attendee.price + attendee.serviceFee) }}</td>
                    <td>{{ attendee.issuedAt ? new Date(attendee.issuedAt).toLocaleDateString() : '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Vista de tarjetas para móvil -->
            <div class="attendees-cards-container mobile-view" v-if="filteredAttendees.length > 0">
              <div v-for="attendee in filteredAttendees" :key="attendee.ticketId" class="attendee-card">
                <div class="attendee-card-header">
                  <div class="attendee-name-section">
                    <h4>{{ attendee.attendeeName || 'Sin nombre' }}</h4>
                    <p class="attendee-ticket">Ticket #{{ attendee.ticketNumber }}</p>
                  </div>
                  <span v-if="attendee.insurance" class="insurance-badge">Seguro</span>
                </div>
                <div class="attendee-card-body">
                  <div class="attendee-info-row">
                    <div class="info-label">
                      <i class="fas fa-envelope"></i>
                      <span>Correo</span>
                    </div>
                    <div class="info-value">{{ attendee.attendeeEmail || 'Sin email' }}</div>
                  </div>
                  <div class="attendee-info-row">
                    <div class="info-label">
                      <i class="fas fa-map-marker-alt"></i>
                      <span>Zona</span>
                    </div>
                    <div class="info-value">{{ attendee.zoneName }}</div>
                  </div>
                  <div class="attendee-info-row">
                    <div class="info-label">
                      <i class="fas fa-euro-sign"></i>
                      <span>Precio</span>
                    </div>
                    <div class="info-value price-value">{{ formatCurrency(attendee.price + attendee.serviceFee) }}</div>
                  </div>
                  <div class="attendee-info-row">
                    <div class="info-label">
                      <i class="fas fa-calendar"></i>
                      <span>Fecha de compra</span>
                    </div>
                    <div class="info-value">{{ attendee.issuedAt ? new Date(attendee.issuedAt).toLocaleDateString('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' }) : '-' }}</div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- Mensaje cuando no hay resultados -->
            <div v-if="filteredAttendees.length === 0 && searchAttendeeQuery" class="modal-empty">
              <i class="fas fa-search"></i>
              <p>No se encontraron asistentes que coincidan con "{{ searchAttendeeQuery }}"</p>
            </div>
          </div>
          <div v-else class="modal-empty">
            <i class="fas fa-users"></i>
            <p>Aún no hay asistentes registrados para este evento.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, watch } from 'vue'
import { formatDateForInput, formatDateTime } from '~/utils/formatDate'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

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
const showAttendeesModal = ref(false)
const modalAttendeesLoading = ref(false)
const modalAttendeesError = ref('')
const selectedEventAttendees = ref([])
const selectedEventForModal = ref(null)
const searchAttendeeQuery = ref('')
const zoneChartRefs = ref({})
const occupancyChartRefs = ref({})

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
    destroyCharts(event.id)
    return
  }
  expandedEventId.value = event.id
  attendeesLoading.value = true
  try {
    if (!attendeesCache.value[event.id]) {
      const details = await getEventAttendees(event.id)
      attendeesCache.value = { ...attendeesCache.value, [event.id]: details }
    }
    await nextTick()
    createCharts(event)
  } catch (error) {
    attendeesError.value = error?.data?.error || error?.message || 'No se pudo cargar la información'
  } finally {
    attendeesLoading.value = false
  }
}

const viewEventAttendees = async (event) => {
  selectedEventForModal.value = event
  showAttendeesModal.value = true
  modalAttendeesLoading.value = true
  modalAttendeesError.value = ''
  selectedEventAttendees.value = []
  
  try {
    if (!attendeesCache.value[event.id]) {
      const details = await getEventAttendees(event.id)
      attendeesCache.value = { ...attendeesCache.value, [event.id]: details }
    }
    selectedEventAttendees.value = attendeesCache.value[event.id]?.attendees || []
  } catch (error) {
    modalAttendeesError.value = error?.data?.error || error?.message || 'No se pudieron cargar los asistentes'
  } finally {
    modalAttendeesLoading.value = false
  }
}

const closeAttendeesModal = () => {
  showAttendeesModal.value = false
  selectedEventAttendees.value = []
  selectedEventForModal.value = null
  searchAttendeeQuery.value = ''
}

const filteredAttendees = computed(() => {
  if (!searchAttendeeQuery.value.trim()) {
    return selectedEventAttendees.value
  }
  
  const query = searchAttendeeQuery.value.toLowerCase().trim()
  
  return selectedEventAttendees.value.filter(attendee => {
    const name = (attendee.attendeeName || '').toLowerCase()
    const email = (attendee.attendeeEmail || '').toLowerCase()
    const ticketNumber = (attendee.ticketNumber || '').toString().toLowerCase()
    const zoneName = (attendee.zoneName || '').toLowerCase()
    const price = formatCurrency(attendee.price + attendee.serviceFee).toLowerCase()
    
    return name.includes(query) ||
           email.includes(query) ||
           ticketNumber.includes(query) ||
           zoneName.includes(query) ||
           price.includes(query)
  })
})

const createCharts = async (event) => {
  const eventDetails = getEventDetails(event.id)
  if (!eventDetails) return

  // Destruir gráficos existentes si hay
  destroyCharts(event.id)

  // Esperar a que el DOM se actualice
  await nextTick()
  
  // Función para intentar crear los gráficos
  const tryCreateCharts = () => {
    const zoneCanvas = document.querySelector(`canvas.zone-chart[data-event-id="${event.id}"]`)
    const occupancyCanvas = document.querySelector(`canvas.occupancy-chart[data-event-id="${event.id}"]`)
    
    if (!zoneCanvas || !occupancyCanvas) {
      // Si no están listos, intentar de nuevo
      setTimeout(tryCreateCharts, 100)
      return
    }

    // Gráfico de ventas por zona
    const zoneData = calculateZoneData(eventDetails.attendees || [], event.zones || [])
    createZoneChart(event.id, zoneData)

    // Gráfico de ocupación
    const occupancyData = {
      sold: event.performance?.soldTickets || 0,
      available: event.availableTickets || 0
    }
    createOccupancyChart(event.id, occupancyData)
  }
  
  // Intentar crear los gráficos
  tryCreateCharts()
}

const calculateZoneData = (attendees, zones) => {
  const zoneCounts = {}
  attendees.forEach(attendee => {
    const zoneName = attendee.zoneName || 'Sin zona'
    zoneCounts[zoneName] = (zoneCounts[zoneName] || 0) + 1
  })
  
  // Añadir zonas sin ventas
  zones.forEach(zone => {
    if (!zoneCounts[zone.name]) {
      zoneCounts[zone.name] = 0
    }
  })

  return zoneCounts
}

const createZoneChart = (eventId, zoneData) => {
  const canvas = document.querySelector(`canvas.zone-chart[data-event-id="${eventId}"]`)
  if (!canvas) {
    console.warn(`Canvas no encontrado para evento ${eventId}`)
    return
  }

  // Destruir gráfico existente si hay
  if (zoneChartRefs.value[eventId]) {
    zoneChartRefs.value[eventId].destroy()
  }

  const ctx = canvas.getContext('2d')
  const labels = Object.keys(zoneData)
  const data = Object.values(zoneData)
  
  // Si no hay datos, mostrar mensaje
  if (labels.length === 0 || data.every(d => d === 0)) {
    return
  }
  
  const colors = [
    'rgba(255, 0, 87, 0.8)',
    'rgba(255, 138, 0, 0.8)',
    'rgba(255, 200, 0, 0.8)',
    'rgba(0, 200, 255, 0.8)',
    'rgba(150, 0, 255, 0.8)',
    'rgba(255, 100, 150, 0.8)'
  ]

  try {
    zoneChartRefs.value[eventId] = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Ventas por Zona',
          data: data,
          backgroundColor: colors.slice(0, labels.length),
          borderColor: colors.slice(0, labels.length).map(c => c.replace('0.8', '1')),
          borderWidth: 2,
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(255, 138, 0, 0.5)',
            borderWidth: 1
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)',
              stepSize: 1
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            }
          },
          x: {
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)'
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('Error creando gráfico de zonas:', error)
  }
}

const createOccupancyChart = (eventId, occupancyData) => {
  const canvas = document.querySelector(`canvas.occupancy-chart[data-event-id="${eventId}"]`)
  if (!canvas) {
    console.warn(`Canvas de ocupación no encontrado para evento ${eventId}`)
    return
  }

  // Destruir gráfico existente si hay
  if (occupancyChartRefs.value[eventId]) {
    occupancyChartRefs.value[eventId].destroy()
  }

  const ctx = canvas.getContext('2d')
  const total = occupancyData.sold + occupancyData.available
  
  // Si no hay datos, no crear gráfico
  if (total === 0) {
    return
  }

  try {
    occupancyChartRefs.value[eventId] = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['Vendidas', 'Disponibles'],
        datasets: [{
          data: [occupancyData.sold, occupancyData.available],
          backgroundColor: [
            'rgba(255, 0, 87, 0.8)',
            'rgba(255, 255, 255, 0.1)'
          ],
          borderColor: [
            'rgba(255, 0, 87, 1)',
            'rgba(255, 255, 255, 0.3)'
          ],
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              color: 'rgba(255, 255, 255, 0.8)',
              padding: 15,
              font: {
                size: 12
              }
            }
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.8)',
            titleColor: '#fff',
            bodyColor: '#fff',
            borderColor: 'rgba(255, 138, 0, 0.5)',
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                const label = context.label || ''
                const value = context.parsed || 0
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
                return `${label}: ${value} (${percentage}%)`
              }
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('Error creando gráfico de ocupación:', error)
  }
}

const destroyCharts = (eventId) => {
  if (zoneChartRefs.value[eventId]) {
    zoneChartRefs.value[eventId].destroy()
    delete zoneChartRefs.value[eventId]
  }
  if (occupancyChartRefs.value[eventId]) {
    occupancyChartRefs.value[eventId].destroy()
    delete occupancyChartRefs.value[eventId]
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
  // Destruir todos los gráficos
  Object.keys(zoneChartRefs.value).forEach(eventId => {
    if (zoneChartRefs.value[eventId]) {
      zoneChartRefs.value[eventId].destroy()
    }
  })
  Object.keys(occupancyChartRefs.value).forEach(eventId => {
    if (occupancyChartRefs.value[eventId]) {
      occupancyChartRefs.value[eventId].destroy()
    }
  })
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
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
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
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
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
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
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
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.02);
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
}

.event-statistics {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.2), rgba(255, 138, 0, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff8a00;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.stat-icon.revenue {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.2), rgba(139, 195, 74, 0.2));
  color: #4caf50;
}

.stat-icon.occupancy {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.2), rgba(100, 181, 246, 0.2));
  color: #2196f3;
}

.stat-icon.tickets {
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.2), rgba(186, 104, 200, 0.2));
  color: #9c27b0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.stat-value {
  color: #fff;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.chart-wrapper {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
}

.chart-title {
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.chart-canvas-wrapper {
  position: relative;
  height: 300px;
  width: 100%;
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
    margin: 0;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  .event-card {
    grid-template-columns: 1fr;
    margin: 0;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .event-list {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
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
  
  /* Estadísticas del evento */
  .event-detail {
    padding: 16px;
    margin: 0;
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .event-card {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .event-body {
    padding: 16px;
    box-sizing: border-box;
  }
  
  .charts-container {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .chart-wrapper {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .chart-canvas-wrapper {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 1rem;
  }
  
  .stat-value {
    font-size: 1.25rem;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .chart-wrapper {
    padding: 16px;
  }
  
  .chart-title {
    font-size: 1rem;
    margin-bottom: 12px;
  }
  
  .chart-canvas-wrapper {
    height: 250px;
  }
  
  /* Modal responsive */
  .modal-overlay {
    padding: 0;
    align-items: flex-end;
  }
  
  .modal-container {
    max-width: 100%;
    max-height: 90vh;
    border-radius: 24px 24px 0 0;
  }
  
  .modal-header {
    padding: 20px;
  }
  
  .modal-header h2 {
    font-size: 1.25rem;
  }
  
  .modal-body {
    padding: 20px;
  }
  
  /* Ocultar tabla en móvil, mostrar tarjetas */
  .desktop-view {
    display: none !important;
  }
  
  .mobile-view {
    display: flex !important;
  }
  
  .attendees-search {
    margin-bottom: 12px;
  }
  
  .search-input {
    padding: 14px 16px 14px 48px;
    font-size: 1rem;
  }
  
  .search-clear {
    width: 32px;
    height: 32px;
  }
  
  .attendees-cards-container {
    margin: 0;
  }
  
  .attendee-card {
    padding: 16px;
  }
  
  .attendee-name-section h4 {
    font-size: 1rem;
  }
  
  .info-label {
    font-size: 0.85rem;
  }
  
  .info-value {
    font-size: 0.9rem;
  }
  
  .event-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .btn-text {
    font-size: 0.85rem;
    padding: 8px 12px;
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

/* Modal de Asistentes */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-container {
  background: linear-gradient(135deg, #1a1a1a 0%, #0f0f0f 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  padding: 24px 28px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  color: #fff;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #ff0057, #ff8a00);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.modal-close {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: rgba(255, 0, 87, 0.2);
  border-color: rgba(255, 0, 87, 0.4);
  transform: rotate(90deg);
}

.modal-body {
  padding: 24px 28px;
  overflow-y: auto;
  flex: 1;
}

.modal-loading,
.modal-error,
.modal-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.7);
  gap: 16px;
}

.modal-error {
  color: #ff6b6b;
}

.modal-empty i {
  font-size: 3rem;
  color: rgba(255, 255, 255, 0.3);
}

.attendees-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.attendees-search {
  margin-bottom: 8px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 16px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 1rem;
  pointer-events: none;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 48px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 0.95rem;
  font-family: 'Poppins', inherit;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: rgba(255, 138, 0, 0.5);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 3px rgba(255, 138, 0, 0.1);
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.search-clear {
  position: absolute;
  right: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 1;
}

.search-clear:hover {
  background: rgba(255, 0, 87, 0.2);
  color: #fff;
  transform: scale(1.1);
}

.attendees-summary {
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.attendees-summary p {
  color: #fff;
  margin: 0;
  font-size: 1rem;
}

.attendees-table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.desktop-view {
  display: block;
}

.mobile-view {
  display: none;
}

.attendees-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}

/* Vista de tarjetas para móvil */
.attendees-cards-container {
  display: none;
  flex-direction: column;
  gap: 16px;
}

.attendee-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 16px;
  transition: all 0.3s ease;
}

.attendee-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateY(-2px);
}

.attendee-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.attendee-name-section h4 {
  color: #fff;
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0 0 4px 0;
}

.attendee-ticket {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  margin: 0;
}

.attendee-card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attendee-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  font-weight: 500;
}

.info-label i {
  color: rgba(255, 138, 0, 0.8);
  width: 16px;
  text-align: center;
}

.info-value {
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  text-align: right;
  word-break: break-word;
  flex: 1;
  margin-left: 12px;
}

.info-value.price-value {
  color: #4caf50;
  font-size: 1rem;
}

.attendees-table thead {
  background: rgba(255, 0, 87, 0.1);
}

.attendees-table th {
  padding: 16px;
  text-align: left;
  color: #fff;
  font-weight: 600;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.1);
}

.attendees-table td {
  padding: 16px;
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.attendees-table tbody tr {
  transition: background 0.2s ease;
}

.attendees-table tbody tr:hover {
  background: rgba(255, 255, 255, 0.05);
}

.attendees-table tbody tr:last-child td {
  border-bottom: none;
}

.attendees-table small {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.8rem;
}

.insurance-badge {
  display: inline-block;
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-left: 6px;
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


<template>
  <div class="backoffice-page">
    <ClientOnly>
      <Header />
    </ClientOnly>

    <!-- Loading State - Toda la página -->
    <div v-if="loading" class="loading-fullscreen">
      <div class="spinner-border" role="status"></div>
      <p class="mt-3">Cargando panel...</p>
    </div>

    <!-- Content - Solo muestra cuando termina de cargar -->
    <template v-else>
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
          <!-- Grupo Principal -->
          <div class="nav-group">
            <p class="nav-group-label">Gestión</p>
            <div class="nav-items">
              <button
                v-for="item in navigation.filter(n => n.group === 'main')"
                :key="item.id"
                class="nav-chip"
                :class="{ active: activeSection === item.id }"
                @click="handleNavClick(item.id)"
              >
                <span class="nav-icon">
                  <i :class="item.icon"></i>
                </span>
                <span class="nav-label">{{ item.label }}</span>
              </button>
            </div>
          </div>

          <!-- Grupo Analítica -->
          <div class="nav-group">
            <p class="nav-group-label">Reportes</p>
            <div class="nav-items">
              <button
                v-for="item in navigation.filter(n => n.group === 'analytics')"
                :key="item.id"
                class="nav-chip"
                :class="{ active: activeSection === item.id }"
                @click="handleNavClick(item.id)"
              >
                <span class="nav-icon">
                  <i :class="item.icon"></i>
                </span>
                <span class="nav-label">{{ item.label }}</span>
              </button>
            </div>
          </div>

          <!-- Grupo Financiero -->
          <div class="nav-group">
            <p class="nav-group-label">Finanzas</p>
            <div class="nav-items">
              <button
                v-for="item in navigation.filter(n => n.group === 'financial')"
                :key="item.id"
                class="nav-chip"
                :class="{ active: activeSection === item.id }"
                @click="handleNavClick(item.id)"
              >
                <span class="nav-icon">
                  <i :class="item.icon"></i>
                </span>
                <span class="nav-label">{{ item.label }}</span>
              </button>
            </div>
          </div>
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
        <!-- Sección Dashboard -->
        <section
          class="dashboard-section"
          id="dashboard"
          ref="dashboardSection"
          v-if="activeSection === 'dashboard'"
        >
          <!-- Título del Dashboard -->
          <div class="dashboard-header">
            <h1 class="dashboard-title">Dashboard</h1>
            <p class="dashboard-subtitle">Vista general de tus eventos y métricas de rendimiento</p>
          </div>

          <!-- Métricas principales -->
          <div class="metrics-grid">
            <div class="metric-card" v-for="metric in metricCards" :key="metric.title">
              <div class="metric-icon">
                <i :class="metric.icon"></i>
              </div>
              <p class="metric-label">{{ metric.title }}</p>
              <h3 class="metric-value">{{ metric.value }}</h3>
              <small class="metric-foot">{{ metric.footnote }}</small>
            </div>
          </div>

          <!-- Resumen rápido y gráficos -->
          <div class="dashboard-content-grid">
            <!-- Panel de resumen rápido -->
            <div class="panel dashboard-panel">
              <div class="panel-heading">
                <div>
                  <p class="eyebrow">Resumen Rápido</p>
                  <h2>Vista General</h2>
                </div>
                <button class="btn-outline" @click="refreshData" :disabled="loadingDashboard">
                  <i class="fas fa-sync me-2"></i>Actualizar
                </button>
              </div>

              <div v-if="loadingDashboard" class="panel-loading">
                <div class="spinner-border text-light" role="status"></div>
                <p>Cargando datos...</p>
              </div>

              <div v-else class="quick-summary">
                <div class="summary-row">
                  <div class="summary-item">
                    <div class="summary-icon revenue">
                      <i class="fas fa-euro-sign"></i>
                    </div>
                    <div class="summary-text">
                      <p class="summary-label">Ticket Promedio</p>
                      <h4>{{ averageTicketPrice }}</h4>
                    </div>
                  </div>
                  <div class="summary-item">
                    <div class="summary-icon tickets">
                      <i class="fas fa-ticket-alt"></i>
                    </div>
                    <div class="summary-text">
                      <p class="summary-label">Ocupación Media</p>
                      <h4>{{ dashboard?.averageOccupancy?.toFixed(1) || 0 }}%</h4>
                    </div>
                  </div>
                </div>
                <div class="summary-row">
                  <div class="summary-item">
                    <div class="summary-icon events">
                      <i class="fas fa-calendar"></i>
                    </div>
                    <div class="summary-text">
                      <p class="summary-label">Eventos Totales</p>
                      <h4>{{ dashboard?.totalEvents || 0 }}</h4>
                    </div>
                  </div>
                  <div class="summary-item">
                    <div class="summary-icon conversion">
                      <i class="fas fa-chart-line"></i>
                    </div>
                    <div class="summary-text">
                      <p class="summary-label">Tasa Conversión</p>
                      <h4>{{ conversionRate }}%</h4>
                    </div>
                  </div>
                </div>

                <!-- Mejor evento destacado -->
                <div v-if="dashboard?.topEvent" class="top-event-highlight">
                  <div class="highlight-header">
                    <i class="fas fa-trophy"></i>
                    <span>Mejor Evento</span>
                  </div>
                  <h3>{{ dashboard.topEvent.title }}</h3>
                  <div class="highlight-stats">
                    <div class="highlight-stat">
                      <span class="stat-label">Ingresos</span>
                      <span class="stat-value">{{ formatCurrency(dashboard.topEvent.grossRevenue || 0) }}</span>
                    </div>
                    <div class="highlight-stat">
                      <span class="stat-label">Tickets</span>
                      <span class="stat-value">{{ dashboard.topEvent.soldTickets || 0 }}</span>
                    </div>
                    <div class="highlight-stat">
                      <span class="stat-label">Ocupación</span>
                      <span class="stat-value">{{ dashboard.topEvent.occupancy?.toFixed(1) || 0 }}%</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Gráfico de distribución -->
            <div class="panel dashboard-panel">
              <div class="panel-heading">
                <div>
                  <p class="eyebrow">Distribución</p>
                  <h2>Ingresos por Categoría</h2>
                </div>
              </div>
              <div class="chart-canvas-wrapper">
                <canvas id="category-chart"></canvas>
              </div>
            </div>
          </div>

          <!-- Eventos recientes y próximos -->
          <div class="dashboard-events-grid">
            <!-- Próximos eventos -->
            <div class="panel dashboard-panel">
              <div class="panel-heading">
                <div>
                  <p class="eyebrow">Próximos Eventos</p>
                  <h2>En Venta</h2>
                </div>
              </div>
              <div v-if="upcomingEvents.length === 0" class="panel-empty small">
                <p>No hay eventos próximos</p>
              </div>
              <div v-else class="events-mini-list">
                <div 
                  v-for="event in upcomingEvents.slice(0, 5)" 
                  :key="event.id" 
                  class="mini-event-card"
                  @click="goToSection('events')"
                >
                  <div class="mini-event-image">
                    <img :src="event.image || fallbackImage" :alt="event.title" />
                  </div>
                  <div class="mini-event-info">
                    <h4>{{ event.title }}</h4>
                    <p>{{ formatDate(event.date, event.time) }}</p>
                    <div class="mini-event-stats">
                      <span class="mini-stat">
                        <i class="fas fa-ticket-alt"></i>
                        {{ event.performance?.soldTickets || 0 }}/{{ event.availableTickets }}
                      </span>
                      <span class="mini-stat revenue">
                        <i class="fas fa-euro-sign"></i>
                        {{ formatCurrency(event.performance?.grossRevenue || 0) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Eventos pasados -->
            <div class="panel dashboard-panel">
              <div class="panel-heading">
                <div>
                  <p class="eyebrow">Eventos Pasados</p>
                  <h2>Historial</h2>
                </div>
              </div>
              <div v-if="pastEvents.length === 0" class="panel-empty small">
                <p>No hay eventos pasados</p>
              </div>
              <div v-else class="events-mini-list">
                <div 
                  v-for="event in pastEvents.slice(0, 5)" 
                  :key="event.id" 
                  class="mini-event-card"
                  @click="goToSection('events')"
                >
                  <div class="mini-event-image">
                    <img :src="event.image || fallbackImage" :alt="event.title" />
                  </div>
                  <div class="mini-event-info">
                    <h4>{{ event.title }}</h4>
                    <p>{{ formatDate(event.date, event.time) }}</p>
                    <div class="mini-event-stats">
                      <span class="mini-stat">
                        <i class="fas fa-ticket-alt"></i>
                        {{ event.performance?.soldTickets || 0 }}/{{ event.availableTickets }}
                      </span>
                      <span class="mini-stat revenue">
                        <i class="fas fa-euro-sign"></i>
                        {{ formatCurrency(event.performance?.grossRevenue || 0) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Sección de Eventos -->
        <section 
          class="panel-group full-width-panel-group" 
          id="events" 
          ref="eventsSection"
          v-if="activeSection === 'events'"
        >
          <!-- Título del Eventos -->
          <div class="dashboard-header">
            <h1 class="dashboard-title">Eventos</h1>
            <p class="dashboard-subtitle">Gestiona y visualiza el rendimiento de todos tus eventos</p>
          </div>

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
        </section>

        <!-- Sección de Formulario -->
        <section 
          class="panel-group full-width-panel-group" 
          id="form" 
          ref="formSection"
          v-if="activeSection === 'form'"
        >
          <!-- Título del Formulario -->
          <div class="dashboard-header">
            <h1 class="dashboard-title">Nuevo Evento</h1>
            <p class="dashboard-subtitle">{{ editingEvent ? 'Modifica los datos del evento existente' : 'Crea y configura un nuevo evento para tu catálogo' }}</p>
          </div>

          <div class="panel form-panel full-width-panel" v-if="showFormPanel">
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
                  <div class="form-group" style="position: relative;" ref="venueGroupRef">
                    <label class="form-label">Lugar/Recinto</label>
                    <input 
                      type="text" 
                      v-model="venue" 
                      class="form-input" 
                      placeholder="Ej: Palacio Vistalegre" 
                      @focus="showVenueSuggestions = true; filterVenues()"
                      @blur="handleVenueBlur"
                      @input="filterVenues"
                      required 
                    />
                    <div 
                      v-if="showVenueSuggestions && filteredVenues.length > 0" 
                      class="suggestions-dropdown"
                      @mousedown.prevent
                    >
                      <div 
                        v-for="v in filteredVenues" 
                        :key="v"
                        class="suggestion-item"
                        @mousedown.prevent="selectVenue(v)"
                      >
                        <i class="fas fa-map-marker-alt me-2"></i>{{ v }}
                      </div>
                    </div>
                  </div>
                  <div class="form-group" style="position: relative;" ref="locationGroupRef">
                    <label class="form-label">Ubicación Exacta</label>
                    <input 
                      type="text" 
                      v-model="location" 
                      class="form-input" 
                      placeholder="Dirección completa" 
                      @focus="showLocationSuggestions = true; filterLocations()"
                      @blur="handleLocationBlur"
                      @input="filterLocations"
                      required 
                    />
                    <div 
                      v-if="showLocationSuggestions && filteredLocations.length > 0" 
                      class="suggestions-dropdown"
                      @mousedown.prevent
                    >
                      <div 
                        v-for="loc in filteredLocations" 
                        :key="loc"
                        class="suggestion-item"
                        @mousedown.prevent="selectLocation(loc)"
                      >
                        <i class="fas fa-location-dot me-2"></i>{{ loc }}
                      </div>
                    </div>
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
            v-else-if="activeSection === 'form' && !showFormPanel"
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

        <!-- Sección de Notificaciones -->
        <section
          class="panel-group full-width-panel-group"
          id="notifications"
          ref="notificationsSection"
          v-if="activeSection === 'notifications'"
        >
          <!-- Título de Notificaciones -->
          <div class="dashboard-header">
            <h1 class="dashboard-title">Notificaciones</h1>
            <p class="dashboard-subtitle">Envía notificaciones push a usuarios que han comprado entradas</p>
          </div>

          <div class="dashboard-content-grid">
            <!-- Panel de Estadísticas -->
            <div class="panel dashboard-panel">
              <div class="panel-heading">
                <div>
                  <p class="eyebrow">Estadísticas</p>
                  <h2>Usuarios</h2>
                </div>
                <button 
                  class="btn-outline" 
                  @click="loadNotificationStats" 
                  :disabled="notificationLoading"
                >
                  <i class="fas fa-sync me-2" :class="{ 'fa-spin': notificationLoading }"></i>Actualizar
                </button>
              </div>

              <div v-if="notificationLoading" class="panel-loading">
                <div class="spinner-border text-light" role="status"></div>
                <p>Cargando estadísticas...</p>
              </div>

              <div v-else-if="notificationStats" class="notification-stats">
                <div class="stat-card">
                  <div class="stat-icon">
                    <i class="fas fa-users"></i>
                  </div>
                  <div class="stat-content">
                    <p class="stat-label">Total de usuarios</p>
                    <h3>{{ notificationStats.totalUsers || 0 }}</h3>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon tickets">
                    <i class="fas fa-ticket-alt"></i>
                  </div>
                  <div class="stat-content">
                    <p class="stat-label">Con entradas</p>
                    <h3>{{ notificationStats.usersWithTickets || 0 }}</h3>
                  </div>
                </div>
                <div class="stat-card">
                  <div class="stat-icon no-tickets">
                    <i class="fas fa-user-slash"></i>
                  </div>
                  <div class="stat-content">
                    <p class="stat-label">Sin entradas</p>
                    <h3>{{ notificationStats.usersWithoutTickets || 0 }}</h3>
                  </div>
                </div>
              </div>

              <div v-else class="panel-empty small">
                <p>No hay estadísticas disponibles</p>
              </div>
            </div>

            <!-- Panel de Formulario -->
            <div class="panel dashboard-panel">
              <div class="panel-heading">
                <div>
                  <p class="eyebrow">Crear Notificación</p>
                  <h2>Nueva Notificación</h2>
                </div>
              </div>

              <form @submit.prevent="sendNotificationForm" class="notification-form">
                <!-- Tipo de notificación -->
                <div class="form-group">
                  <label for="notificationType" class="form-label">
                    <i class="fas fa-tags me-2"></i>Tipo de Notificación
                  </label>
                  <select 
                    id="notificationType" 
                    v-model="notificationForm.notificationType" 
                    class="form-control"
                    @change="applyNotificationTemplate"
                  >
                    <option value="custom">Personalizada</option>
                    <option value="postponement">Aplazamiento de evento</option>
                    <option value="few_tickets">Pocas entradas disponibles</option>
                    <option value="reminder">Recordatorio de evento</option>
                    <option value="location_change">Cambio de ubicación</option>
                    <option value="cancellation">Cancelación de evento</option>
                    <option value="special_offer">Oferta especial</option>
                  </select>
                  <small class="form-hint">
                    Selecciona un tipo para usar una plantilla predefinida o personaliza tu mensaje
                  </small>
                </div>

                <!-- Tipo de destinatario -->
                <div class="form-group">
                  <label for="targetType" class="form-label">
                    <i class="fas fa-users me-2"></i>Destinatarios
                  </label>
                  <select 
                    id="targetType" 
                    v-model="notificationForm.targetType" 
                    class="form-control"
                    @change="onTargetTypeChange"
                  >
                    <option value="all">Todos los usuarios</option>
                    <option value="with_tickets">Usuarios con entradas compradas</option>
                    <option value="event_users">Usuarios de un evento específico</option>
                  </select>
                  <small class="form-hint">
                    <span v-if="notificationForm.targetType === 'all'">
                      <strong>Todos los usuarios:</strong> Se enviará a todos los usuarios registrados en la plataforma (con o sin entradas compradas). Ideal para anuncios generales u ofertas especiales.
                    </span>
                    <span v-else-if="notificationForm.targetType === 'with_tickets'">
                      <strong>Usuarios con entradas:</strong> Se enviará a todos los usuarios que han comprado al menos una entrada de cualquier evento. No se filtra por evento específico. Útil para comunicaciones generales a clientes activos.
                    </span>
                    <span v-else>
                      <strong>Evento específico:</strong> Se enviará solo a usuarios que han comprado entradas para el evento seleccionado. Perfecto para notificaciones sobre un evento concreto.
                    </span>
                  </small>
                </div>

                <!-- Selector de evento (solo para evento específico) -->
                <div class="form-group" v-if="notificationForm.targetType === 'event_users'">
                  <label for="eventId" class="form-label">
                    <i class="fas fa-music me-2"></i>Evento
                    <span class="text-danger">*</span>
                  </label>
                  <select 
                    id="eventId" 
                    v-model="notificationForm.eventId" 
                    class="form-control"
                    required
                    @change="onEventChange"
                  >
                    <option value="">Selecciona un evento</option>
                    <option 
                      v-for="event in eventsWithTickets" 
                      :key="event.id" 
                      :value="event.id"
                    >
                      {{ event.title }} - {{ formatDate(event.date, event.time) }} 
                      ({{ event.performance?.soldTickets || 0 }} entradas vendidas, 
                      {{ (event.availableTickets || 0) - (event.performance?.soldTickets || 0) }} disponibles)
                    </option>
                  </select>
                  <small class="form-hint">
                    <strong>Requerido:</strong> Solo se mostrarán eventos futuros con entradas vendidas
                  </small>
                </div>

                <!-- Título -->
                <div class="form-group">
                  <label for="notificationTitle" class="form-label">
                    <i class="fas fa-heading me-2"></i>Título
                  </label>
                  <input 
                    id="notificationTitle"
                    type="text" 
                    v-model="notificationForm.title" 
                    class="form-control"
                    placeholder="Ej: Evento aplazado"
                    required
                    maxlength="100"
                  />
                  <small class="form-hint">
                    {{ notificationForm.title.length }}/100 caracteres
                  </small>
                </div>

                <!-- Cuerpo -->
                <div class="form-group">
                  <label for="notificationBody" class="form-label">
                    <i class="fas fa-align-left me-2"></i>Mensaje
                  </label>
                  <textarea 
                    id="notificationBody"
                    v-model="notificationForm.body" 
                    class="form-control"
                    rows="5"
                    placeholder="Ej: Lamentamos informar que el evento se ha aplazado. La nueva fecha será comunicada próximamente."
                    required
                    maxlength="500"
                  ></textarea>
                  <small class="form-hint">
                    {{ notificationForm.body.length }}/500 caracteres
                  </small>
                </div>

                <!-- Vista previa de URL -->
                <div class="form-group">
                  <label class="form-label">
                    <i class="fas fa-link me-2"></i>URL de destino
                  </label>
                  <div class="url-preview">
                    <code>{{ notificationForm.url || '/' }}</code>
                  </div>
                  <small class="form-hint">
                    Esta es la URL a la que se dirigirá el usuario al hacer clic en la notificación
                  </small>
                </div>

                <!-- Mensajes de éxito/error -->
                <transition name="fade">
                  <div v-if="notificationSuccess" class="alert success">
                    <i class="fas fa-check-circle me-2"></i>{{ notificationSuccess }}
                  </div>
                </transition>
                <transition name="fade">
                  <div v-if="notificationError" class="alert error">
                    <i class="fas fa-exclamation-triangle me-2"></i>{{ notificationError }}
                  </div>
                </transition>

                <!-- Botón de envío -->
                <div class="form-actions">
                  <button 
                    type="submit" 
                    class="btn-primary"
                    :disabled="notificationSending || !notificationForm.title || !notificationForm.body || (notificationForm.targetType === 'event_users' && !notificationForm.eventId) || (notificationForm.notificationType === 'few_tickets' && !notificationForm.eventId)"
                  >
                    <i class="fas fa-paper-plane me-2" :class="{ 'fa-spin': notificationSending }"></i>
                    {{ notificationSending ? 'Enviando...' : 'Enviar Notificación' }}
                  </button>
                  <button 
                    type="button" 
                    class="btn-outline"
                    @click="resetNotificationForm"
                    :disabled="notificationSending"
                  >
                    <i class="fas fa-redo me-2"></i>Limpiar
                  </button>
                </div>
              </form>
            </div>
          </div>
        </section>

        <!-- Sección de Analítica -->
        <section
          class="panel-group full-width-panel-group"
          id="analytics"
          ref="analyticsSection"
          v-if="activeSection === 'analytics'"
        >
          <!-- Título de Analítica -->
          <div class="dashboard-header">
            <h1 class="dashboard-title">Analítica</h1>
            <p class="dashboard-subtitle">Métricas detalladas, tendencias y comparativas de rendimiento</p>
          </div>

          <div class="panel performance-panel full-width-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Analítica Avanzada</p>
                <h2>Métricas y Tendencias</h2>
              </div>
              <button class="btn-outline" @click="refreshAnalytics" :disabled="analyticsLoading">
                <i class="fas fa-sync me-2"></i>Actualizar
              </button>
            </div>

            <div v-if="analyticsLoading" class="panel-loading">
              <div class="spinner-border text-light" role="status"></div>
              <p>Cargando analítica...</p>
            </div>

            <div v-else class="analytics-content">
              <!-- Métricas de rendimiento -->
              <div class="analytics-metrics">
                <div class="analytics-card">
                  <div class="analytics-icon revenue">
                    <i class="fas fa-euro-sign"></i>
                  </div>
                  <div class="analytics-info">
                    <p class="analytics-label">Ingresos Totales</p>
                    <h3>{{ formatCurrency(dashboard?.totalRevenue || 0) }}</h3>
                    <small>Bruto acumulado</small>
                  </div>
                </div>
                <div class="analytics-card">
                  <div class="analytics-icon tickets">
                    <i class="fas fa-ticket-alt"></i>
                  </div>
                  <div class="analytics-info">
                    <p class="analytics-label">Tickets Vendidos</p>
                    <h3>{{ dashboard?.totalTicketsSold || 0 }}</h3>
                    <small>Total histórico</small>
                  </div>
                </div>
                <div class="analytics-card">
                  <div class="analytics-icon events">
                    <i class="fas fa-calendar-check"></i>
                  </div>
                  <div class="analytics-info">
                    <p class="analytics-label">Eventos Activos</p>
                    <h3>{{ dashboard?.upcomingEvents || 0 }}</h3>
                    <small>Próximos eventos</small>
                  </div>
                </div>
                <div class="analytics-card">
                  <div class="analytics-icon occupancy">
                    <i class="fas fa-percentage"></i>
                  </div>
                  <div class="analytics-info">
                    <p class="analytics-label">Ocupación Media</p>
                    <h3>{{ dashboard?.averageOccupancy?.toFixed(1) || 0 }}%</h3>
                    <small>Promedio general</small>
                  </div>
                </div>
              </div>

              <!-- Gráficos de analítica -->
              <div class="analytics-charts-grid">
                <div class="analytics-chart-section full-width">
                  <div class="chart-header">
                    <h3 class="chart-section-title">Tendencia de Ventas</h3>
                    <span class="chart-badge">Evolución temporal</span>
                  </div>
                  <div class="chart-canvas-wrapper">
                    <canvas id="sales-trend-chart"></canvas>
                  </div>
                </div>
              </div>

              <div class="analytics-charts-grid">
                <div class="analytics-chart-section">
                  <div class="chart-header">
                    <h3 class="chart-section-title">Top Eventos por Ingresos</h3>
                    <span class="chart-badge">{{ dashboard?.events?.length || 0 }} eventos</span>
                  </div>
                  <div class="chart-canvas-wrapper">
                    <canvas id="revenue-chart"></canvas>
                  </div>
                </div>

                <div class="analytics-chart-section">
                  <div class="chart-header">
                    <h3 class="chart-section-title">Rendimiento General</h3>
                    <span class="chart-badge">Resumen</span>
                  </div>
                  <div class="performance-summary">
                    <div class="performance-item">
                      <div class="performance-icon">
                        <i class="fas fa-trophy"></i>
                      </div>
                      <div class="performance-info">
                        <p class="performance-label">Mejor Evento</p>
                        <h4>{{ dashboard?.topEvent?.title?.length > 15 ? dashboard.topEvent.title.substring(0, 15) + '...' : (dashboard?.topEvent?.title || 'N/A') }}</h4>
                        <small>{{ formatCurrency(dashboard?.topEvent?.grossRevenue || 0) }}</small>
                      </div>
                    </div>
                    <div class="performance-item">
                      <div class="performance-icon">
                        <i class="fas fa-chart-line"></i>
                      </div>
                      <div class="performance-info">
                        <p class="performance-label">Ticket Promedio</p>
                        <h4>{{ averageTicketPrice }}</h4>
                        <small>Por ticket vendido</small>
                      </div>
                    </div>
                    <div class="performance-item">
                      <div class="performance-icon">
                        <i class="fas fa-percent"></i>
                      </div>
                      <div class="performance-info">
                        <p class="performance-label">Tasa de Conversión</p>
                        <h4>{{ conversionRate }}%</h4>
                        <small>Ocupación promedio</small>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Comparativa de eventos -->
              <div class="analytics-comparison">
                <h3 class="section-subtitle">Comparativa de Eventos</h3>
                <div class="comparison-grid">
                  <div 
                    v-for="event in topEventsByRevenue" 
                    :key="event.id" 
                    class="comparison-card"
                  >
                    <div class="comparison-header">
                      <h4>{{ event.title.length > 25 ? event.title.substring(0, 25) + '...' : event.title }}</h4>
                      <span class="comparison-rank">#{{ topEventsByRevenue.indexOf(event) + 1 }}</span>
                    </div>
                    <div class="comparison-body">
                      <div class="comparison-metric">
                        <span class="metric-name">Ingresos</span>
                        <span class="metric-value revenue">{{ formatCurrency(event.grossRevenue || 0) }}</span>
                      </div>
                      <div class="comparison-metric">
                        <span class="metric-name">Tickets</span>
                        <span class="metric-value">{{ event.soldTickets || 0 }}</span>
                      </div>
                      <div class="comparison-metric">
                        <span class="metric-name">Ocupación</span>
                        <span class="metric-value">{{ event.occupancy?.toFixed(1) || 0 }}%</span>
                      </div>
                      <div class="comparison-progress">
                        <div class="progress">
                          <div 
                            class="progress-bar" 
                            :style="{ width: `${event.occupancy || 0}%` }"
                          ></div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- Sección de Facturación -->
        <section
          class="panel-group full-width-panel-group"
          id="billing"
          ref="billingSection"
          v-if="activeSection === 'billing'"
        >
          <!-- Título de Facturación -->
          <div class="dashboard-header">
            <h1 class="dashboard-title">Facturación</h1>
            <p class="dashboard-subtitle">Transacciones, pagos y gestión financiera de tus eventos</p>
          </div>

          <div class="panel performance-panel full-width-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Facturación</p>
                <h2>Transacciones y Pagos</h2>
              </div>
              <button class="btn-outline" @click="refreshBilling" :disabled="billingLoading">
                <i class="fas fa-sync me-2"></i>Actualizar
              </button>
            </div>

            <div v-if="billingLoading" class="panel-loading">
              <div class="spinner-border text-light" role="status"></div>
              <p>Cargando transacciones...</p>
            </div>

            <div v-else-if="!mergedEvents.length" class="panel-empty">
              <p>No hay eventos con transacciones registradas.</p>
              <small>Las transacciones aparecerán aquí cuando se realicen compras.</small>
            </div>

            <div v-else class="billing-content">
              <div class="billing-summary">
                <div class="summary-card">
                  <p class="summary-label">Total Facturado</p>
                  <h3>{{ formatCurrency(dashboard?.totalRevenue || 0) }}</h3>
                </div>
                <div class="summary-card">
                  <p class="summary-label">Ganancia Neta</p>
                  <h3>{{ formatCurrency(dashboard?.netRevenue || 0) }}</h3>
                </div>
                <div class="summary-card">
                  <p class="summary-label">Transacciones</p>
                  <h3>{{ totalTransactions }}</h3>
                </div>
              </div>

              <!-- Gráfico de ingresos -->
              <div class="billing-chart-section">
                <div class="chart-header">
                  <h3 class="chart-section-title">Evolución de Ingresos</h3>
                  <span class="chart-badge">Por evento</span>
                </div>
                <div class="chart-canvas-wrapper">
                  <canvas id="billing-revenue-chart"></canvas>
                </div>
              </div>

              <div class="transactions-list">
                <div class="list-header">
                  <h3 class="section-subtitle">Transacciones por Evento</h3>
                  <div class="list-filters">
                    <button 
                      class="filter-btn" 
                      :class="{ active: billingFilter === 'all' }"
                      @click="billingFilter = 'all'"
                    >
                      Todos
                    </button>
                    <button 
                      class="filter-btn" 
                      :class="{ active: billingFilter === 'active' }"
                      @click="billingFilter = 'active'"
                    >
                      Activos
                    </button>
                    <button 
                      class="filter-btn" 
                      :class="{ active: billingFilter === 'past' }"
                      @click="billingFilter = 'past'"
                    >
                      Pasados
                    </button>
                    <button 
                      class="filter-btn export-btn" 
                      @click="exportBillingReport"
                      title="Exportar reporte"
                    >
                      <i class="fas fa-download"></i>
                      Exportar
                    </button>
                  </div>
                </div>
                <div class="transaction-item" v-for="event in filteredBillingEvents" :key="event.id">
                  <div class="transaction-header">
                    <div class="transaction-title-section">
                      <h4>{{ event.title }}</h4>
                      <p class="transaction-meta">{{ event.venue }} · {{ formatDate(event.date, event.time) }}</p>
                    </div>
                    <span class="transaction-badge">{{ event.performance?.transactions || 0 }} transacciones</span>
                  </div>
                  <div class="transaction-details">
                    <div class="transaction-detail">
                      <span class="detail-label">
                        <i class="fas fa-euro-sign"></i>
                        Ingresos brutos:
                      </span>
                      <span class="detail-value revenue">{{ formatCurrency(event.performance?.grossRevenue || 0) }}</span>
                    </div>
                    <div class="transaction-detail">
                      <span class="detail-label">
                        <i class="fas fa-ticket-alt"></i>
                        Tickets vendidos:
                      </span>
                      <span class="detail-value">{{ event.performance?.soldTickets || 0 }} / {{ event.availableTickets }}</span>
                    </div>
                    <div class="transaction-detail">
                      <span class="detail-label">
                        <i class="fas fa-percentage"></i>
                        Ocupación:
                      </span>
                      <span class="detail-value">{{ event.performance?.occupancy?.toFixed(1) || 0 }}%</span>
                    </div>
                    <div class="transaction-detail">
                      <span class="detail-label">
                        <i class="fas fa-chart-pie"></i>
                        Ganancia neta:
                      </span>
                      <span class="detail-value net">{{ formatCurrency(event.performance?.netRevenue || 0) }}</span>
                    </div>
                    <div class="transaction-detail">
                      <span class="detail-label">
                        <i class="fas fa-calculator"></i>
                        Comisiones:
                      </span>
                      <span class="detail-value">{{ formatCurrency((event.performance?.grossRevenue || 0) - (event.performance?.netRevenue || 0)) }}</span>
                    </div>
                  </div>
                </div>
              </div>
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
          <div class="modal-actions">
            <button 
              v-if="selectedEventForModal" 
              class="btn-outline small export-csv-btn" 
              type="button" 
              @click="exportAttendeesCsv"
            >
              <i class="fas fa-file-csv me-1"></i>
              <span class="btn-text-label">Exportar CSV</span>
            </button>
            <button class="modal-close" @click="closeAttendeesModal">
              <i class="fas fa-times"></i>
            </button>
          </div>
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
    </template>
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
    { name: 'description', content: 'Dashboard profesional para gestionar eventos, ventas y ganancias.' },
    { property: 'og:title', content: 'Backoffice | GoLive' },
    { property: 'og:description', content: 'Dashboard profesional para gestionar eventos, ventas y ganancias.' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: 'https://golive-hu5d.onrender.com/backoffice' },
    { property: 'og:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'Backoffice | GoLive' },
    { name: 'twitter:description', content: 'Dashboard profesional para gestionar eventos, ventas y ganancias.' },
    { name: 'twitter:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' }
  ],
  link: [
    { rel: 'canonical', href: 'https://golive-hu5d.onrender.com/backoffice' }
  ]
})

const fallbackImage = '/logo.svg'
const loading = ref(true)
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
const notificationsSection = ref(null)
const analyticsSection = ref(null)
const billingSection = ref(null)
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
const revenueChartRef = ref(null)
const categoryChartRef = ref(null)
const salesTrendChartRef = ref(null)
const revenueTrendChartRef = ref(null)

// Variables para nuevas secciones
const analyticsLoading = ref(false)
const billingLoading = ref(false)
const billingFilter = ref('all')

// Variables para notificaciones
const notificationStats = ref(null)
const notificationLoading = ref(false)
const notificationSending = ref(false)
const notificationForm = ref({
  notificationType: 'custom', // 'custom', 'postponement', 'few_tickets', 'reminder', 'location_change', 'cancellation', 'special_offer'
  title: '',
  body: '',
  targetType: 'all', // 'all', 'with_tickets', 'event_users'
  eventId: null,
  url: '/'
})
const notificationSuccess = ref('')
const notificationError = ref('')
const notificationTemplates = {
  postponement: {
    title: 'Evento aplazado',
    body: 'Lamentamos informar que el evento se ha aplazado. La nueva fecha será comunicada próximamente. Mantendremos tu entrada válida para la nueva fecha.',
    url: '/misEntradas'
  },
  few_tickets: {
    title: '¡Quedan pocas entradas!',
    body: 'No te quedes sin tu entrada. Quedan muy pocas disponibles. ¡Asegura tu lugar ahora!',
    url: '/evento/' // Se completará con el eventId
  },
  reminder: {
    title: 'Recordatorio: Tu evento está cerca',
    body: 'Tu evento se acerca. ¡No olvides asistir! Revisa los detalles en tus entradas.',
    url: '/misEntradas'
  },
  location_change: {
    title: 'Cambio de ubicación del evento',
    body: 'El evento ha cambiado de ubicación. Consulta los nuevos detalles en tus entradas. Tu entrada sigue siendo válida.',
    url: '/misEntradas'
  },
  cancellation: {
    title: 'Evento cancelado',
    body: 'Lamentamos informar que el evento ha sido cancelado. Se procesará el reembolso completo en un plazo de 5-7 días hábiles.',
    url: '/misEntradas'
  },
  special_offer: {
    title: 'Oferta especial disponible',
    body: 'Tenemos una oferta especial para ti. ¡No te la pierdas! Descubre nuestros eventos con descuentos exclusivos.',
    url: '/'
  },
  custom: {
    title: '',
    body: '',
    url: '/'
  }
}

const totalTransactions = computed(() => {
  return mergedEvents.value.reduce((sum, event) => {
    return sum + (event.performance?.transactions || 0)
  }, 0)
})

const averageTicketPrice = computed(() => {
  const totalRevenue = dashboard.value?.totalRevenue || 0
  const totalTickets = dashboard.value?.totalTicketsSold || 0
  if (totalTickets === 0) return formatCurrency(0)
  return formatCurrency(totalRevenue / totalTickets)
})

const conversionRate = computed(() => {
  return dashboard.value?.averageOccupancy?.toFixed(1) || 0
})

const topEventsByRevenue = computed(() => {
  if (!dashboard.value?.events?.length) return []
  return [...dashboard.value.events]
    .sort((a, b) => (b.grossRevenue || 0) - (a.grossRevenue || 0))
    .slice(0, 6)
})

const filteredBillingEvents = computed(() => {
  const events = mergedEvents.value
  if (billingFilter.value === 'all') return events
  
  const now = new Date()
  return events.filter(event => {
    if (!event.date) return false
    const eventDate = new Date(event.date)
    if (billingFilter.value === 'active') {
      return eventDate >= now
    } else {
      return eventDate < now
    }
  })
})

const upcomingEvents = computed(() => {
  const now = new Date()
  return mergedEvents.value
    .filter(event => {
      if (!event.date) return false
      return new Date(event.date) >= now
    })
    .sort((a, b) => new Date(a.date) - new Date(b.date))
})

const pastEvents = computed(() => {
  const now = new Date()
  return mergedEvents.value
    .filter(event => {
      if (!event.date) return false
      return new Date(event.date) < now
    })
    .sort((a, b) => new Date(b.date) - new Date(a.date))
})

const eventsWithTickets = computed(() => {
  return upcomingEvents.value.filter(event => {
    const soldTickets = event.performance?.soldTickets || 0
    return soldTickets > 0
  })
})

const title = ref('')
const venue = ref('')
const location = ref('')
const date = ref('')
const time = ref('')
const category = ref('')
const imageUrlInput = ref('')
const zones = ref([{ name: 'General', price: 40, availableTickets: 100 }])
const editingEvent = ref(null)
const availableVenues = ref([])
const availableLocations = ref([])
const showVenueSuggestions = ref(false)
const showLocationSuggestions = ref(false)
const filteredVenues = ref([])
const filteredLocations = ref([])
const venueGroupRef = ref(null)
const locationGroupRef = ref(null)

const { getManagedEvents, createEvent, updateEvent, deleteEvent, getEventAttendees, getUniqueVenues, getUniqueLocations } = useEvents()
const { fetchOverview } = useDashboard()
const { getEventTransactions } = useTransactions()
const { getSalesTrend, getEventAnalytics } = useAnalytics()
const { sendNotification, getNotificationStats } = useNotifications()

const isSuperUser = computed(() => {
  const role = (userData.value?.role || '').toLowerCase()
  return role === 'super_user'
})

const navigation = computed(() => {
  return [
    { id: 'dashboard', label: 'Dashboard', icon: 'fas fa-chart-line', group: 'main' },
    { id: 'events', label: 'Eventos', icon: 'fas fa-music', group: 'main' },
    { id: 'form', label: 'Nuevo Evento', icon: 'fas fa-plus-circle', group: 'main' },
    { id: 'notifications', label: 'Notificaciones', icon: 'fas fa-bell', group: 'main' },
    { id: 'analytics', label: 'Analítica', icon: 'fas fa-chart-bar', group: 'analytics' },
    { id: 'billing', label: 'Facturación', icon: 'fas fa-file-invoice-dollar', group: 'financial' }
  ]
})

const updateViewport = () => {
  if (!process.client) return
  const desktop = window.innerWidth >= 1024
  isDesktop.value = desktop
  isSidebarOpen.value = desktop
}

const toggleSidebar = () => {
  if (isDesktop.value) return
  isSidebarOpen.value = !isSidebarOpen.value
  
  // Asegurar que el body no quede bloqueado cuando se cierra el sidebar
  if (!isSidebarOpen.value && process.client) {
    document.body.style.overflow = ''
    document.body.style.position = ''
  }
}

const scrollToSection = (sectionId) => {
  if (!process.client) return
  
  const target = {
    dashboard: dashboardSection.value,
    events: eventsSection.value,
    form: formSection.value,
    notifications: notificationsSection.value,
    analytics: analyticsSection.value,
    billing: billingSection.value
  }[sectionId]

  if (target) {
    // En PWA/móvil, usar scroll más suave y asegurar que el body no esté bloqueado
    if (document.body.style.overflow === 'hidden') {
      document.body.style.overflow = ''
    }
    
    // Usar requestAnimationFrame para asegurar que el DOM esté listo
    requestAnimationFrame(() => {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' })
    })
  }
}

const goToSection = (sectionId) => {
  activeSection.value = sectionId
  nextTick(() => scrollToSection(sectionId))
}

const handleNavClick = async (sectionId) => {
  // Cerrar formulario si se cambia de sección
  if (activeSection.value === 'form' && sectionId !== 'form') {
    showFormPanel.value = false
    resetForm()
  }
  
  if (sectionId === 'form') {
    showFormPanel.value = true
  } else {
    showFormPanel.value = false
  }
  
  // En móvil/PWA: cerrar sidebar primero antes de hacer scroll
  if (!isDesktop.value) {
    isSidebarOpen.value = false
    // Asegurar que el body no quede bloqueado
    if (process.client) {
      document.body.style.overflow = ''
      document.body.style.position = ''
    }
    // Esperar a que el sidebar se cierre completamente antes de hacer scroll
    await nextTick()
    // Pequeño delay adicional para asegurar que el overlay se haya removido
    await new Promise(resolve => setTimeout(resolve, 100))
  }
  
  goToSection(sectionId)
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

const exportAttendeesCsv = async () => {
  if (!selectedEventForModal.value?.id) return

  try {
    const config = useRuntimeConfig()
    const { getToken } = useAuth()
    const token = getToken()
    if (!token) {
      throw new Error('No hay token de autenticación')
    }

    const apiBase = config.public.apiBase?.replace(/\/+$/, '') || ''
    const eventId = selectedEventForModal.value.id

    const response = await fetch(`${apiBase}/api/tickets/events/${eventId}/export`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })

    if (!response.ok) {
      const text = await response.text()
      throw new Error(text || 'No se pudo exportar el CSV')
    }

    const blob = await response.blob()
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    const safeTitle = (selectedEventForModal.value.title || 'asistentes')
      .toLowerCase()
      .replace(/[^a-z0-9]+/gi, '-')
      .replace(/(^-|-$)/g, '')
    link.download = `asistentes-${safeTitle || 'evento'}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('Error exportando CSV de asistentes:', error)
    modalAttendeesError.value = error?.message || 'No se pudo exportar el CSV de asistentes'
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

const selectEvent = async (event) => {
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
  // Cargar lugares y ubicaciones disponibles
  await loadVenuesAndLocations()
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

const openNewEventForm = async () => {
  resetForm()
  showFormPanel.value = true
  goToSection('form')
  // Cargar lugares y ubicaciones disponibles
  await loadVenuesAndLocations()
}

const loadVenuesAndLocations = async () => {
  try {
    availableVenues.value = await getUniqueVenues()
    availableLocations.value = await getUniqueLocations()
  } catch (error) {
    console.error('Error cargando lugares y ubicaciones:', error)
  }
}

const filterVenues = () => {
  const search = venue.value.toLowerCase().trim()
  if (!search) {
    filteredVenues.value = availableVenues.value.slice(0, 10)
  } else {
    filteredVenues.value = availableVenues.value
      .filter(v => v.toLowerCase().includes(search))
      .slice(0, 10)
  }
}

const filterLocations = () => {
  const search = location.value.toLowerCase().trim()
  if (!search) {
    filteredLocations.value = availableLocations.value.slice(0, 10)
  } else {
    filteredLocations.value = availableLocations.value
      .filter(loc => loc.toLowerCase().includes(search))
      .slice(0, 10)
  }
}

const selectVenue = (selectedVenue) => {
  venue.value = selectedVenue
  showVenueSuggestions.value = false
}

const selectLocation = (selectedLocation) => {
  location.value = selectedLocation
  showLocationSuggestions.value = false
}

const handleVenueBlur = (e) => {
  // Delay para permitir clicks en sugerencias
  setTimeout(() => {
    if (process.client && venueGroupRef.value) {
      const activeElement = document.activeElement
      if (!venueGroupRef.value.contains(activeElement)) {
        showVenueSuggestions.value = false
      }
    } else {
      showVenueSuggestions.value = false
    }
  }, 200)
}

const handleLocationBlur = (e) => {
  // Delay para permitir clicks en sugerencias
  setTimeout(() => {
    if (process.client && locationGroupRef.value) {
      const activeElement = document.activeElement
      if (!locationGroupRef.value.contains(activeElement)) {
        showLocationSuggestions.value = false
      }
    } else {
      showLocationSuggestions.value = false
    }
  }, 200)
}

// Cerrar dropdowns al hacer click fuera
const handleClickOutside = (event) => {
  if (process.client) {
    if (venueGroupRef.value && !venueGroupRef.value.contains(event.target)) {
      showVenueSuggestions.value = false
    }
    if (locationGroupRef.value && !locationGroupRef.value.contains(event.target)) {
      showLocationSuggestions.value = false
    }
  }
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
    // Enviamos la fecha en formato YYYY-MM-DD para que el backend la parsee sin problemas de huso horario
    date: date.value,
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

// Funciones para nuevas secciones
const refreshAnalytics = async () => {
  analyticsLoading.value = true
  try {
    await refreshData()
    await nextTick()
    createRevenueChart()
    createSalesTrendChart()
    createCategoryChart()
  } catch (error) {
    errorMessage.value = error?.message || 'No se pudo cargar la analítica'
  } finally {
    analyticsLoading.value = false
  }
}

// Funciones para notificaciones
const loadNotificationStats = async () => {
  notificationLoading.value = true
  notificationError.value = ''
  try {
    const stats = await getNotificationStats()
    notificationStats.value = stats
  } catch (error) {
    notificationError.value = error?.data?.error || error?.message || 'No se pudieron cargar las estadísticas'
    console.error('Error cargando estadísticas de notificaciones:', error)
  } finally {
    notificationLoading.value = false
  }
}

const applyNotificationTemplate = () => {
  const template = notificationTemplates[notificationForm.value.notificationType]
  if (template && notificationForm.value.notificationType !== 'custom') {
    notificationForm.value.title = template.title
    notificationForm.value.body = template.body
    
    // Para pocas entradas, siempre requiere evento específico
    if (notificationForm.value.notificationType === 'few_tickets') {
      notificationForm.value.targetType = 'event_users'
      if (notificationForm.value.eventId) {
        notificationForm.value.url = `/evento/${notificationForm.value.eventId}`
        const selectedEvent = eventsWithTickets.value.find(e => e.id === notificationForm.value.eventId)
        if (selectedEvent) {
          const availableTickets = (selectedEvent.availableTickets || 0) - (selectedEvent.performance?.soldTickets || 0)
          notificationForm.value.title = `¡Quedan pocas entradas para ${selectedEvent.title}!`
          notificationForm.value.body = `No te quedes sin tu entrada para "${selectedEvent.title}". Solo quedan ${availableTickets} entradas disponibles. ¡Asegura tu lugar ahora!`
        }
      } else {
        notificationForm.value.url = template.url
        notificationError.value = 'Para notificaciones de pocas entradas, debes seleccionar un evento'
      }
    } else {
      notificationForm.value.url = template.url
      // Solo personalizar el mensaje si hay un evento seleccionado Y el targetType es event_users
      if (notificationForm.value.eventId && notificationForm.value.targetType === 'event_users') {
        const selectedEvent = eventsWithTickets.value.find(e => e.id === notificationForm.value.eventId)
        if (selectedEvent) {
          if (notificationForm.value.notificationType === 'postponement') {
            notificationForm.value.body = `Lamentamos informar que el evento "${selectedEvent.title}" se ha aplazado. La nueva fecha será comunicada próximamente. Mantendremos tu entrada válida para la nueva fecha.`
          } else if (notificationForm.value.notificationType === 'reminder') {
            notificationForm.value.title = `Recordatorio: ${selectedEvent.title} está cerca`
            notificationForm.value.body = `Tu evento "${selectedEvent.title}" se acerca. ¡No olvides asistir! Revisa los detalles en tus entradas.`
          } else if (notificationForm.value.notificationType === 'location_change') {
            notificationForm.value.body = `El evento "${selectedEvent.title}" ha cambiado de ubicación. Consulta los nuevos detalles en tus entradas. Tu entrada sigue siendo válida.`
          } else if (notificationForm.value.notificationType === 'cancellation') {
            notificationForm.value.body = `Lamentamos informar que el evento "${selectedEvent.title}" ha sido cancelado. Se procesará el reembolso completo en un plazo de 5-7 días hábiles.`
          }
        }
      }
    }
  } else if (notificationForm.value.notificationType === 'custom') {
    // Limpiar errores al cambiar a personalizada
    notificationError.value = ''
  }
}

const onTargetTypeChange = () => {
  // Limpiar errores previos
  notificationError.value = ''
  
  // Si cambia a "pocas entradas", siempre requiere evento específico
  if (notificationForm.value.notificationType === 'few_tickets') {
    notificationForm.value.targetType = 'event_users'
    // Si no hay evento seleccionado, mostrar mensaje
    if (!notificationForm.value.eventId) {
      notificationError.value = 'Para notificaciones de pocas entradas, debes seleccionar un evento específico'
    }
  }
  
  // Si cambia a "todos los usuarios" o "con entradas", limpiar evento
  if (notificationForm.value.targetType === 'all' || notificationForm.value.targetType === 'with_tickets') {
    notificationForm.value.eventId = null
    // Para "todos" o "con entradas", usar URL por defecto o la de la plantilla
    if (notificationForm.value.notificationType === 'special_offer') {
      notificationForm.value.url = '/'
    } else if (notificationForm.value.notificationType === 'few_tickets') {
      // Si es pocas entradas pero cambió a "all" o "with_tickets", cambiar tipo de notificación
      notificationForm.value.notificationType = 'custom'
      notificationForm.value.url = '/'
    } else {
      const template = notificationTemplates[notificationForm.value.notificationType]
      notificationForm.value.url = template ? template.url : '/'
    }
  }
}

const onEventChange = () => {
  // Solo procesar si el targetType es event_users
  if (notificationForm.value.targetType !== 'event_users') return
  
  // Actualizar URL según el tipo de notificación y el evento seleccionado
  if (notificationForm.value.eventId) {
    const selectedEvent = eventsWithTickets.value.find(e => e.id === notificationForm.value.eventId)
    
    if (!selectedEvent) return
    
    if (notificationForm.value.notificationType === 'few_tickets') {
      notificationForm.value.url = `/evento/${notificationForm.value.eventId}`
      const availableTickets = (selectedEvent.availableTickets || 0) - (selectedEvent.performance?.soldTickets || 0)
      notificationForm.value.title = `¡Quedan pocas entradas para ${selectedEvent.title}!`
      notificationForm.value.body = `No te quedes sin tu entrada para "${selectedEvent.title}". Solo quedan ${availableTickets} entradas disponibles. ¡Asegura tu lugar ahora!`
    } else if (notificationForm.value.notificationType === 'postponement') {
      notificationForm.value.body = `Lamentamos informar que el evento "${selectedEvent.title}" se ha aplazado. La nueva fecha será comunicada próximamente. Mantendremos tu entrada válida para la nueva fecha.`
    } else if (notificationForm.value.notificationType === 'reminder') {
      notificationForm.value.title = `Recordatorio: ${selectedEvent.title} está cerca`
      notificationForm.value.body = `Tu evento "${selectedEvent.title}" se acerca. ¡No olvides asistir! Revisa los detalles en tus entradas.`
    } else if (notificationForm.value.notificationType === 'location_change') {
      notificationForm.value.body = `El evento "${selectedEvent.title}" ha cambiado de ubicación. Consulta los nuevos detalles en tus entradas. Tu entrada sigue siendo válida.`
    } else if (notificationForm.value.notificationType === 'cancellation') {
      notificationForm.value.body = `Lamentamos informar que el evento "${selectedEvent.title}" ha sido cancelado. Se procesará el reembolso completo en un plazo de 5-7 días hábiles.`
    }
  } else {
    // Si se deselecciona el evento, volver a la plantilla base
    if (notificationForm.value.notificationType !== 'custom') {
      applyNotificationTemplate()
    }
  }
}

const sendNotificationForm = async () => {
  if (!notificationForm.value.title || !notificationForm.value.body) {
    notificationError.value = 'Por favor completa todos los campos requeridos'
    return
  }

  // Validar según el tipo de destinatario
  if (notificationForm.value.targetType === 'event_users' && !notificationForm.value.eventId) {
    notificationError.value = 'Por favor selecciona un evento para notificaciones de evento específico'
    return
  }

  // Validar que para "pocas entradas" se requiere evento
  if (notificationForm.value.notificationType === 'few_tickets' && !notificationForm.value.eventId) {
    notificationError.value = 'Para notificaciones de pocas entradas, debes seleccionar un evento'
    return
  }
  
  // Para "todos los usuarios", no se requiere evento
  if (notificationForm.value.targetType === 'all') {
    // Asegurar que no se envíe eventId para "all"
    notificationForm.value.eventId = null
  }

  notificationSending.value = true
  notificationError.value = ''
  notificationSuccess.value = ''

  try {
    // Construir URL final
    let finalUrl = notificationForm.value.url || '/'
    
    // Si es pocas entradas y hay evento, asegurar que la URL sea correcta
    if (notificationForm.value.notificationType === 'few_tickets' && notificationForm.value.eventId) {
      finalUrl = `/evento/${notificationForm.value.eventId}`
    } else if (notificationForm.value.eventId && notificationForm.value.targetType === 'event_users') {
      // Para otros tipos con evento, usar misEntradas o la URL personalizada
      if (['postponement', 'reminder', 'location_change', 'cancellation'].includes(notificationForm.value.notificationType)) {
        finalUrl = '/misEntradas'
      } else if (!finalUrl || finalUrl === '/') {
        finalUrl = `/evento/${notificationForm.value.eventId}`
      }
    }

    const payload = {
      title: notificationForm.value.title.trim(),
      body: notificationForm.value.body.trim(),
      targetType: notificationForm.value.targetType,
      url: finalUrl
    }

    // Solo incluir eventId si es event_users
    if (notificationForm.value.targetType === 'event_users') {
      payload.eventId = notificationForm.value.eventId
    }
    // Para "all" y "with_tickets", no se envía eventId

    const response = await sendNotification(payload)
    
    notificationSuccess.value = `Notificación enviada correctamente a ${response.sentCount || 0} usuario(s)`
    resetNotificationForm()
    
    // Recargar estadísticas después de enviar
    await loadNotificationStats()
  } catch (error) {
    notificationError.value = error?.data?.error || error?.message || 'Error al enviar la notificación'
    console.error('Error enviando notificación:', error)
  } finally {
    notificationSending.value = false
  }
}

const resetNotificationForm = () => {
  notificationForm.value = {
    notificationType: 'custom',
    title: '',
    body: '',
    targetType: 'all',
    eventId: null,
    url: '/'
  }
  notificationError.value = ''
  notificationSuccess.value = ''
}

const refreshBilling = async () => {
  billingLoading.value = true
  try {
    await refreshData()
    await nextTick()
    createBillingRevenueChart()
  } catch (error) {
    errorMessage.value = error?.message || 'No se pudo cargar la facturación'
  } finally {
    billingLoading.value = false
  }
}

const createBillingRevenueChart = async () => {
  await nextTick()
  const canvas = document.getElementById('billing-revenue-chart')
  if (!canvas || !filteredBillingEvents.value.length) return

  if (revenueTrendChartRef.value) {
    revenueTrendChartRef.value.destroy()
  }

  const ctx = canvas.getContext('2d')
  const events = filteredBillingEvents.value
    .sort((a, b) => new Date(a.date) - new Date(b.date))
    .slice(0, 12)

  const labels = events.map(e => {
    if (!e.date) return 'Sin fecha'
    const date = new Date(e.date)
    return date.toLocaleDateString('es-ES', { month: 'short', day: 'numeric' })
  })
  const revenueData = events.map(e => e.performance?.grossRevenue || 0)
  const netData = events.map(e => e.performance?.netRevenue || 0)

  try {
    revenueTrendChartRef.value = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Ingresos Brutos',
            data: revenueData,
            borderColor: 'rgba(76, 175, 80, 1)',
            backgroundColor: 'rgba(76, 175, 80, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4
          },
          {
            label: 'Ganancia Neta',
            data: netData,
            borderColor: 'rgba(33, 150, 243, 1)',
            backgroundColor: 'rgba(33, 150, 243, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: {
              color: 'rgba(255, 255, 255, 0.8)',
              padding: 15,
              font: { size: 12 }
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
                return `${context.dataset.label}: ${formatCurrency(context.parsed.y)}`
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)',
              callback: function(value) {
                return formatCurrency(value)
              }
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
    console.error('Error creando gráfico de facturación:', error)
  }
}

const exportBillingReport = () => {
  // Crear CSV con datos de facturación
  let csv = 'Evento,Fecha,Ingresos Brutos,Ganancia Neta,Tickets Vendidos,Ocupación,Transacciones\n'
  
  filteredBillingEvents.value.forEach(event => {
    const title = (event.title || '').replace(/"/g, '""')
    const date = event.date ? formatDate(event.date, event.time) : 'Sin fecha'
    const gross = event.performance?.grossRevenue || 0
    const net = event.performance?.netRevenue || 0
    const tickets = event.performance?.soldTickets || 0
    const occupancy = event.performance?.occupancy?.toFixed(1) || 0
    const transactions = event.performance?.transactions || 0
    
    csv += `"${title}","${date}",${gross},${net},${tickets},${occupancy},${transactions}\n`
  })

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `reporte-facturacion-${new Date().toISOString().split('T')[0]}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}


const createRevenueChart = async () => {
  await nextTick()
  const canvas = document.getElementById('revenue-chart')
  if (!canvas || !dashboard.value?.events?.length) return

  // Destruir gráfico existente
  if (revenueChartRef.value) {
    revenueChartRef.value.destroy()
  }

  const ctx = canvas.getContext('2d')
  const events = dashboard.value.events
    .sort((a, b) => b.grossRevenue - a.grossRevenue)
    .slice(0, 10) // Top 10

  const labels = events.map(e => e.title.length > 20 ? e.title.substring(0, 20) + '...' : e.title)
  const data = events.map(e => e.grossRevenue)

  try {
    revenueChartRef.value = new Chart(ctx, {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [{
          label: 'Ingresos (€)',
          data: data,
          backgroundColor: 'rgba(255, 138, 0, 0.8)',
          borderColor: 'rgba(255, 138, 0, 1)',
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
            borderWidth: 1,
            callbacks: {
              label: function(context) {
                return `Ingresos: ${formatCurrency(context.parsed.y)}`
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)',
              callback: function(value) {
                return formatCurrency(value)
              }
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            }
          },
          x: {
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)',
              maxRotation: 45,
              minRotation: 45
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('Error creando gráfico de ingresos:', error)
  }
}

const createCategoryChart = async () => {
  await nextTick()
  const canvas = document.getElementById('category-chart')
  if (!canvas || !dashboard.value?.events?.length) return

  if (categoryChartRef.value) {
    categoryChartRef.value.destroy()
  }

  const ctx = canvas.getContext('2d')
  const categoryData = {}
  
  dashboard.value.events.forEach(event => {
    const category = event.category || 'Sin categoría'
    if (!categoryData[category]) {
      categoryData[category] = 0
    }
    categoryData[category] += event.grossRevenue || 0
  })

  const labels = Object.keys(categoryData)
  const data = Object.values(categoryData)

  try {
    categoryChartRef.value = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: [
            'rgba(255, 0, 87, 0.8)',
            'rgba(255, 138, 0, 0.8)',
            'rgba(0, 200, 255, 0.8)',
            'rgba(150, 0, 255, 0.8)',
            'rgba(76, 175, 80, 0.8)'
          ],
          borderColor: [
            'rgba(255, 0, 87, 1)',
            'rgba(255, 138, 0, 1)',
            'rgba(0, 200, 255, 1)',
            'rgba(150, 0, 255, 1)',
            'rgba(76, 175, 80, 1)'
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
              font: { size: 12 }
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
                return `${label}: ${formatCurrency(value)} (${percentage}%)`
              }
            }
          }
        }
      }
    })
  } catch (error) {
    console.error('Error creando gráfico de categorías:', error)
  }
}

const createSalesTrendChart = async () => {
  await nextTick()
  const canvas = document.getElementById('sales-trend-chart')
  if (!canvas || !dashboard.value?.events?.length) return

  if (salesTrendChartRef.value) {
    salesTrendChartRef.value.destroy()
  }

  const ctx = canvas.getContext('2d')
  
  // Agrupar eventos por mes
  const monthlyData = {}
  dashboard.value.events.forEach(event => {
    if (!event.date) return
    const date = new Date(event.date)
    const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    if (!monthlyData[monthKey]) {
      monthlyData[monthKey] = { tickets: 0, revenue: 0 }
    }
    monthlyData[monthKey].tickets += event.soldTickets || 0
    monthlyData[monthKey].revenue += event.grossRevenue || 0
  })

  const sortedMonths = Object.keys(monthlyData).sort()
  const labels = sortedMonths.map(m => {
    const [year, month] = m.split('-')
    const monthNames = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']
    return `${monthNames[parseInt(month) - 1]} ${year}`
  })

  try {
    salesTrendChartRef.value = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Tickets Vendidos',
            data: sortedMonths.map(m => monthlyData[m].tickets),
            borderColor: 'rgba(255, 138, 0, 1)',
            backgroundColor: 'rgba(255, 138, 0, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            yAxisID: 'y'
          },
          {
            label: 'Ingresos (€)',
            data: sortedMonths.map(m => monthlyData[m].revenue),
            borderColor: 'rgba(255, 0, 87, 1)',
            backgroundColor: 'rgba(255, 0, 87, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            yAxisID: 'y1'
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          mode: 'index',
          intersect: false
        },
        plugins: {
          legend: {
            position: 'top',
            labels: {
              color: 'rgba(255, 255, 255, 0.8)',
              padding: 15,
              font: { size: 12 }
            }
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
            type: 'linear',
            display: true,
            position: 'left',
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)',
              stepSize: 1
            },
            grid: {
              color: 'rgba(255, 255, 255, 0.1)'
            }
          },
          y1: {
            type: 'linear',
            display: true,
            position: 'right',
            ticks: {
              color: 'rgba(255, 255, 255, 0.7)',
              callback: function(value) {
                return formatCurrency(value)
              }
            },
            grid: {
              drawOnChartArea: false
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
    console.error('Error creando gráfico de tendencias:', error)
  }
}


onMounted(async () => {
  loading.value = true
  loadUser()
  if (process.client) {
    updateViewport()
    window.addEventListener('resize', updateViewport)
    // Listener para cerrar dropdowns al hacer click fuera
    document.addEventListener('click', handleClickOutside)
  }

  // Si no hay datos en sessionStorage pero sí token válido, intentamos
  // recuperarlos desde el backend en lugar de mandar directamente al login.
  if (!userData.value) {
    try {
      const { getCurrentUser } = useAuth()
      const current = await getCurrentUser()
      userData.value = current

      if (process.client) {
        try {
          sessionStorage.setItem('user', JSON.stringify(current))
        } catch (e) {
          console.error('No se pudo guardar el usuario en sessionStorage:', e)
        }
      }
    } catch (error) {
      // Si el token es inválido o ha expirado, entonces sí mandamos al login
      loading.value = false
      return navigateTo('/login')
    }
  }

  await refreshData()
  loading.value = false
  
  // Crear gráficos del dashboard después de cargar datos
  await nextTick()
  if (activeSection.value === 'dashboard' && dashboard.value) {
    setTimeout(() => {
      createCategoryChart()
    }, 300)
  }
})

// Watch para cargar datos cuando cambia la sección activa
watch(activeSection, async (newSection) => {
  if (newSection === 'form' && showFormPanel.value) {
    await loadVenuesAndLocations()
  }
  if (newSection === 'notifications') {
    await loadNotificationStats()
  }
  if (newSection === 'dashboard' && dashboard.value) {
    await nextTick()
    createCategoryChart()
  }
  
  if (newSection === 'analytics' && dashboard.value) {
    await nextTick()
    createRevenueChart()
    createSalesTrendChart()
  }
  
  if (newSection === 'billing' && dashboard.value) {
    await nextTick()
    createBillingRevenueChart()
  }
  
  // Cerrar formulario si se cambia a otra sección
  if (newSection !== 'form' && showFormPanel.value) {
    showFormPanel.value = false
    resetForm()
  }
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
  if (revenueChartRef.value) {
    revenueChartRef.value.destroy()
  }
  if (categoryChartRef.value) {
    categoryChartRef.value.destroy()
  }
  if (salesTrendChartRef.value) {
    salesTrendChartRef.value.destroy()
  }
  if (revenueTrendChartRef.value) {
    revenueTrendChartRef.value.destroy()
  }
  if (process.client) {
    window.removeEventListener('resize', updateViewport)
    document.removeEventListener('click', handleClickOutside)
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

/* Solo en móviles: eliminar espacio superior */
@media (max-width: 768px) {
  .backoffice-page {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }
}

.backoffice-shell {
  box-sizing: border-box;
  width: 100%;
  margin-left: 0;
  margin-right: auto;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 40px;
  align-items: stretch;
  padding: 0 clamp(16px, 4vw, 48px) 40px 0;
}

/* Centrar el contenido en desktop entre sidebar y borde derecho */
@media (min-width: 1201px) {
  .backoffice-content {
    max-width: 1200px;
    margin-left: auto;
    margin-right: auto;
    width: 100%;
  }
}

.backoffice-sidebar {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04) 0%, rgba(255, 255, 255, 0.02) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 0 24px 24px 0;
  padding: 32px 24px 32px 24px;
  position: sticky;
  top: 0;
  height: fit-content;
  margin-left: 0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.45), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  transition: transform 0.3s ease;
  backdrop-filter: blur(10px);
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

.sidebar-header {
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 0;
}

.sidebar-header h2 {
  color: #fff;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #fff 0%, rgba(255, 255, 255, 0.8) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
  gap: 28px;
  margin: 32px 0;
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-group-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: rgba(255, 255, 255, 0.4);
  margin-bottom: 8px;
  padding: 0 4px;
  font-weight: 600;
}

.nav-items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-chip {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid transparent;
  border-radius: 12px;
  padding: 14px 16px;
  color: rgba(255, 255, 255, 0.8);
  text-align: left;
  font-weight: 500;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  position: relative;
  overflow: hidden;
}

.nav-chip::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #ff0057, #ff8a00);
  transform: scaleY(0);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-chip:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.15);
  color: #fff;
  transform: translateX(4px);
}

.nav-chip:hover::before {
  transform: scaleY(1);
}

.nav-chip.active {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.15), rgba(255, 138, 0, 0.15));
  border-color: rgba(255, 138, 0, 0.3);
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 138, 0, 0.15);
}

.nav-chip.active::before {
  transform: scaleY(1);
}

.nav-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 1rem;
  transition: transform 0.25s ease;
}

.nav-chip:hover .nav-icon {
  transform: scale(1.1);
}

.nav-chip.active .nav-icon {
  color: #ff8a00;
}

.nav-label {
  flex: 1;
  font-weight: 500;
  letter-spacing: 0.2px;
}

.sidebar-stats {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
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
  width: 100%;
  max-width: 100%;
  padding-top: 24px;
}

.content-header {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 40px 32px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
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

/* Dashboard mejorado */
.dashboard-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.dashboard-header {
  margin-bottom: 8px;
}

.dashboard-title {
  color: #fff;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #fff, rgba(255, 255, 255, 0.9));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.dashboard-subtitle {
  color: rgba(255, 255, 255, 0.6);
  font-size: 1rem;
  margin: 0;
  font-weight: 400;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.dashboard-content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 24px;
}

.dashboard-panel {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 28px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.quick-summary {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.summary-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.summary-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateY(-2px);
}

.summary-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.summary-icon.revenue {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.2), rgba(139, 195, 74, 0.2));
  color: #4caf50;
}

.summary-icon.tickets {
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.2), rgba(186, 104, 200, 0.2));
  color: #9c27b0;
}

.summary-icon.events {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.2), rgba(100, 181, 246, 0.2));
  color: #2196f3;
}

.summary-icon.conversion {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.2), rgba(255, 138, 0, 0.2));
  color: #ff8a00;
}

.summary-text {
  flex: 1;
}

.summary-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.summary-text h4 {
  color: #fff;
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0;
}

.top-event-highlight {
  margin-top: 8px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(255, 138, 0, 0.15), rgba(255, 0, 87, 0.15));
  border: 1px solid rgba(255, 138, 0, 0.3);
  border-radius: 16px;
}

.highlight-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ff8a00;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
}

.top-event-highlight h3 {
  color: #fff;
  font-size: 1.2rem;
  font-weight: 700;
  margin: 0 0 16px 0;
}

.highlight-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.highlight-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.highlight-stat .stat-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.75rem;
}

.highlight-stat .stat-value {
  color: #fff;
  font-size: 1rem;
  font-weight: 600;
}

.dashboard-events-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.events-mini-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mini-event-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.mini-event-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateX(4px);
}

.mini-event-image {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}

.mini-event-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.mini-event-info {
  flex: 1;
  min-width: 0;
}

.mini-event-info h4 {
  color: #fff;
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mini-event-info p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.8rem;
  margin: 0 0 6px 0;
}

.mini-event-stats {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.mini-stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.75rem;
}

.mini-stat i {
  color: rgba(255, 138, 0, 0.8);
  font-size: 0.7rem;
}

.mini-stat.revenue {
  color: #4caf50;
  font-weight: 600;
}

.metric-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.metric-card:hover {
  transform: translateY(-4px);
  border-color: rgba(255, 138, 0, 0.3);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 138, 0, 0.1);
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.04) 0%, rgba(255, 255, 255, 0.02) 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 24px;
  padding: 32px;
  position: relative;
  box-sizing: border-box;
  width: 100%;
  max-width: 100%;
  overflow-x: hidden;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
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
  min-height: 400px;
}

.placeholder-content {
  text-align: center;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.placeholder-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.2), rgba(255, 138, 0, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ff8a00;
  font-size: 2.5rem;
  margin-bottom: 8px;
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
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.05) 0%, rgba(255, 255, 255, 0.02) 100%) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
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
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
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
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: rgba(255, 0, 87, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1), 0 0 20px rgba(255, 138, 0, 0.2);
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-1px);
}

.suggestions-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, rgba(20, 20, 20, 0.98) 0%, rgba(15, 15, 15, 0.98) 100%);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  margin-top: 4px;
  max-height: 250px;
  height: 250px;
  overflow-y: auto !important;
  overflow-x: hidden;
  z-index: 1000;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  scroll-behavior: smooth;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  will-change: scroll-position;
  transform: translateZ(0);
  backface-visibility: hidden;
  -webkit-font-smoothing: antialiased;
}

.suggestions-dropdown::-webkit-scrollbar {
  width: 6px;
}

.suggestions-dropdown::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.suggestions-dropdown::-webkit-scrollbar-thumb {
  background: #ff0057;
  border-radius: 3px;
}

.suggestions-dropdown::-webkit-scrollbar-thumb:hover {
  background: #ff6b35;
}

.suggestion-item {
  padding: 12px 16px;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.suggestion-item:last-child {
  border-bottom: none;
}

.suggestion-item:hover {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.15), rgba(255, 138, 0, 0.15));
  color: #fff;
  transform: translateX(4px);
}

.suggestion-item i {
  color: rgba(255, 138, 0, 0.7);
  font-size: 0.9rem;
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
  width: 100%;
  box-sizing: border-box;
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
    width: 100%;
    max-width: 100%;
  }
  
  .form-group {
    width: 100%;
    max-width: 100%;
    min-width: 0;
  }
  
  .form-input {
    width: 100%;
    max-width: 100%;
    min-width: 0;
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
    padding: 16px 18px;
    align-items: flex-start;
    gap: 12px;
  }
  
  .modal-header h2 {
    font-size: 1.15rem;
  }
  
  .modal-body {
    padding: 16px 18px;
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

.modal-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.export-csv-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
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

@media (max-width: 768px) {
  .modal-actions {
    gap: 8px;
  }

  .export-csv-btn {
    padding: 6px 10px;
    font-size: 0.75rem;
  }

  .export-csv-btn .btn-text-label {
    display: none;
  }

  .modal-close {
    width: 32px;
    height: 32px;
  }
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
  border-radius: 50%;
  border-style: solid;
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

/* ============ Estilos para nuevas secciones ============ */

/* Analítica */
.analytics-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.analytics-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
}

.analytics-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
  min-width: 0;
  overflow: hidden;
}

.analytics-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateY(-2px);
}

.analytics-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.analytics-icon.revenue {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.2), rgba(139, 195, 74, 0.2));
  color: #4caf50;
}

.analytics-icon.tickets {
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.2), rgba(186, 104, 200, 0.2));
  color: #9c27b0;
}

.analytics-icon.events {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.2), rgba(100, 181, 246, 0.2));
  color: #2196f3;
}

.analytics-icon.occupancy {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.2), rgba(255, 138, 0, 0.2));
  color: #ff8a00;
}

.analytics-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.analytics-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.analytics-info h3 {
  color: #fff;
  font-size: 1.5rem;
  font-weight: 700;
  margin: 4px 0;
  word-break: break-word;
  line-height: 1.2;
  overflow-wrap: break-word;
}

.analytics-info small {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.75rem;
  display: block;
  margin-top: 2px;
}

.analytics-charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.analytics-chart-section.full-width {
  grid-column: 1 / -1;
}

.analytics-comparison {
  margin-top: 32px;
}

.comparison-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.comparison-card {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.06), rgba(255, 255, 255, 0.03));
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 24px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.comparison-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ff0057, #ff8a00);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.comparison-card:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.06));
  border-color: rgba(255, 138, 0, 0.4);
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(255, 138, 0, 0.15), 0 0 0 1px rgba(255, 138, 0, 0.1);
}

.comparison-card:hover::before {
  opacity: 1;
}

.comparison-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  position: relative;
}

.comparison-header h4 {
  color: #fff;
  font-size: 1.05rem;
  font-weight: 700;
  margin: 0;
  flex: 1;
  line-height: 1.3;
  padding-right: 12px;
}

.comparison-rank {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.3), rgba(255, 138, 0, 0.3));
  color: #fff;
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(255, 138, 0, 0.2);
  border: 1px solid rgba(255, 138, 0, 0.3);
}

.comparison-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.comparison-metric {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.2s ease;
}

.comparison-card:hover .comparison-metric {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.12);
}

.comparison-metric .metric-name {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.comparison-metric .metric-value {
  color: #fff;
  font-weight: 700;
  font-size: 1.1rem;
  line-height: 1.2;
}

.comparison-metric .metric-value.revenue {
  color: #4caf50;
  font-size: 1.2rem;
}

.comparison-progress {
  margin-top: 4px;
  grid-column: 1 / -1;
}

.comparison-progress .progress {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  overflow: hidden;
  position: relative;
}

.comparison-progress .progress-bar {
  height: 8px;
  background: linear-gradient(90deg, #ff0057, #ff8a00);
  border-radius: 999px;
  position: relative;
  overflow: hidden;
}

.comparison-progress .progress-bar::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.analytics-chart-section {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 24px;
  backdrop-filter: blur(10px);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-section-title {
  color: #fff;
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
}

.chart-badge {
  background: rgba(255, 138, 0, 0.2);
  color: #ff8a00;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.performance-summary {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.performance-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.performance-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
  transform: translateX(4px);
}

.performance-icon {
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

.performance-info {
  flex: 1;
}

.performance-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.8rem;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.performance-info h4 {
  color: #fff;
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0 0 4px 0;
}

.performance-info small {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.75rem;
}

/* Facturación */
.billing-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.billing-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
}

.summary-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.summary-card h3 {
  color: #fff;
  font-size: 2rem;
  font-weight: 700;
  margin: 0;
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-subtitle {
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}

.list-filters {
  display: flex;
  gap: 8px;
}

.filter-btn {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.filter-btn.active {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.2), rgba(255, 138, 0, 0.2));
  border-color: rgba(255, 138, 0, 0.4);
  color: #ff8a00;
}

.filter-btn.export-btn {
  background: rgba(76, 175, 80, 0.2);
  border-color: rgba(76, 175, 80, 0.4);
  color: #4caf50;
}

.filter-btn.export-btn:hover {
  background: rgba(76, 175, 80, 0.3);
  color: #66bb6a;
}

.billing-chart-section {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 24px;
  backdrop-filter: blur(10px);
}

.transaction-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s ease;
}

.transaction-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 138, 0, 0.3);
}

.transaction-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 16px;
}

.transaction-title-section {
  flex: 1;
}

.transaction-header h4 {
  color: #fff;
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 6px 0;
}

.transaction-meta {
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.85rem;
  margin: 0;
}

.transaction-badge {
  background: rgba(255, 138, 0, 0.2);
  color: #ff8a00;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.transaction-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.transaction-detail {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.detail-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-label i {
  color: rgba(255, 138, 0, 0.6);
  font-size: 0.85rem;
}

.detail-value {
  color: #fff;
  font-weight: 600;
  font-size: 0.95rem;
}

.detail-value.revenue {
  color: #4caf50;
  font-size: 1rem;
}

.detail-value.net {
  color: #2196f3;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .analytics-metrics,
  .billing-summary {
    grid-template-columns: 1fr;
  }

  .analytics-charts-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-content-grid,
  .dashboard-events-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-title {
    font-size: 2rem;
  }

  .dashboard-subtitle {
    font-size: 0.9rem;
  }

  .summary-row {
    grid-template-columns: 1fr;
  }

  .highlight-stats {
    grid-template-columns: 1fr;
  }

  .comparison-grid {
    grid-template-columns: 1fr;
  }

  .transaction-details {
    grid-template-columns: 1fr;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-filters {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

/* Estilos para Notificaciones */
.notification-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  flex-shrink: 0;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.2), rgba(100, 181, 246, 0.2));
  color: #2196f3;
}

.stat-icon.tickets {
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.2), rgba(186, 104, 200, 0.2));
  color: #9c27b0;
}

.stat-icon.no-tickets {
  background: linear-gradient(135deg, rgba(158, 158, 158, 0.2), rgba(189, 189, 189, 0.2));
  color: #9e9e9e;
}

.stat-content {
  flex: 1;
}

.stat-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  margin: 0 0 6px 0;
  font-weight: 500;
}

.stat-content h3 {
  color: #fff;
  font-size: 1.8rem;
  font-weight: 700;
  margin: 0;
  line-height: 1;
}

.notification-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 8px;
}

.form-control {
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  font-size: 0.95rem;
  font-family: 'Poppins', inherit;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  width: 100%;
  box-sizing: border-box;
}

.form-control:focus {
  outline: none;
  border-color: rgba(255, 0, 87, 0.5);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1), 0 0 20px rgba(255, 138, 0, 0.2);
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-1px);
}

.form-control::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-control option {
  background: #1a1a1a;
  color: #fff;
}

.form-control select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23fff' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
}

.form-control textarea {
  resize: vertical;
  min-height: 120px;
  font-family: 'Poppins', inherit;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions .btn-primary,
.form-actions .btn-outline {
  flex: 1;
}

@media (max-width: 768px) {
  .notification-stats {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .btn-primary,
  .form-actions .btn-outline {
    width: 100%;
  }
}

.url-preview {
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  margin-top: 8px;
}

.url-preview code {
  color: #4caf50;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  word-break: break-all;
}

.notification-form .form-group {
  position: relative;
}

.text-danger {
  color: #ff5252;
  margin-left: 4px;
}

/* ============ Eliminar espacio entre header y contenido (solo móviles) ============ */
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

  ClientOnly {
    display: block;
    margin: 0;
    padding: 0;
  }

  ClientOnly + * {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}
</style>


<template>
  <div class="mis-entradas-page">
    <Header />
    
    <!-- Loading State - Toda la página -->
    <div v-if="loading" class="loading-fullscreen">
      <div class="spinner-border" role="status"></div>
      <p class="mt-3">{{ $t('Cargando tus entradas...') }}</p>
    </div>

    <!-- Content - Solo muestra cuando termina de cargar -->
    <template v-else>
    <div class="tickets-wrapper">
      <div class="page-header">
        <div>
          <p class="eyebrow">{{ $t('Área privada') }}</p>
          <h1>{{ $t('Mis Entradas') }}</h1>
          <p>{{ $t('Descarga y gestiona todas tus entradas digitales desde un único lugar.') }}</p>
        </div>
        <NuxtLink class="cta" to="/conciertos">
          <i class="bi bi-plus-circle"></i> {{ $t('Descubrir eventos') }}
        </NuxtLink>
      </div>

      <div v-if="error" class="state-card state-card--error">
        <i class="bi bi-exclamation-circle"></i>
        <p>{{ error }}</p>
        <button v-if="requiresLogin" class="cta" @click="goToLogin">
          {{ $t('Iniciar sesión') }}
        </button>
        <button v-else class="cta" @click="loadTickets">
          {{ $t('Reintentar') }}
        </button>
      </div>

      <div v-else-if="!tickets.length" class="state-card state-card--empty">
        <i class="bi bi-ticket-perforated"></i>
        <p>{{ $t('Aún no tienes entradas disponibles.') }}</p>
        <NuxtLink class="cta" to="/conciertos">
          {{ $t('Explorar eventos') }}
        </NuxtLink>
      </div>

      <div v-else class="tickets-sections">
        <!-- Eventos Próximos -->
        <section v-if="upcomingTickets.length > 0" class="tickets-section">
          <div class="section-header">
            <div class="section-header__content">
              <div class="section-header__icon upcoming">
                <i class="bi bi-calendar-event"></i>
              </div>
              <div>
                <h2 class="section-header__title">{{ $t('Eventos Próximos') }}</h2>
                <p class="section-header__subtitle">
                  {{ $t('Tienes {count} entrada(s) para eventos próximos', { count: upcomingTickets.length }) }}
                </p>
              </div>
            </div>
            <div class="section-header__badge">
              <span class="badge badge--upcoming">{{ upcomingTickets.length }}</span>
            </div>
          </div>
          <div class="tickets-grid">
            <TicketCard 
              v-for="ticket in upcomingTickets" 
              :key="ticket.id" 
              :ticket="ticket"
              class="ticket-card--upcoming"
            />
          </div>
        </section>

        <!-- Eventos Pasados -->
        <section v-if="pastTickets.length > 0" class="tickets-section">
          <div class="section-header">
            <div class="section-header__content">
              <div class="section-header__icon past">
                <i class="bi bi-clock-history"></i>
              </div>
              <div>
                <h2 class="section-header__title">{{ $t('Eventos Pasados') }}</h2>
                <p class="section-header__subtitle">
                  {{ $t('Historial de {count} entrada(s) de eventos ya realizados', { count: pastTickets.length }) }}
                </p>
              </div>
            </div>
            <div class="section-header__badge">
              <span class="badge badge--past">{{ pastTickets.length }}</span>
            </div>
          </div>
          <div class="tickets-grid tickets-grid--past">
            <TicketCard 
              v-for="ticket in pastTickets" 
              :key="ticket.id" 
              :ticket="ticket"
              class="ticket-card--past"
            />
          </div>
        </section>
      </div>
    </div>
    <Footer />
    </template>
  </div>
</template>

<script setup>
import Header from '~/components/Header.vue'
import Footer from '~/components/Footer.vue'
import TicketCard from '~/components/TicketCard.vue'
import { useAuth } from '~/composables/useAuth'
import { useRouter, useHead, useRuntimeConfig } from '#app'

const tickets = ref([])
const loading = ref(true)
const error = ref('')
const requiresLogin = ref(false)
const { getCurrentUser, getToken } = useAuth()
const router = useRouter()
const config = useRuntimeConfig()
const normalizeApiBase = (base) => {
  const trimmed = (base || '').replace(/\/+$/, '')
  return trimmed.endsWith('/api') ? trimmed : `${trimmed}/api`
}
const apiBase = normalizeApiBase(config.public.apiBase || 'http://localhost:8085')

// Separar tickets en próximos y pasados
const upcomingTickets = computed(() => {
  const now = new Date()
  return tickets.value.filter(ticket => {
    if (!ticket.eventDate) return false
    const eventDate = new Date(ticket.eventDate)
    return eventDate >= now
  }).sort((a, b) => {
    const dateA = new Date(a.eventDate)
    const dateB = new Date(b.eventDate)
    return dateA - dateB // Ordenar por fecha ascendente (más próximos primero)
  })
})

const pastTickets = computed(() => {
  const now = new Date()
  return tickets.value.filter(ticket => {
    if (!ticket.eventDate) return false
    const eventDate = new Date(ticket.eventDate)
    return eventDate < now
  }).sort((a, b) => {
    const dateA = new Date(a.eventDate)
    const dateB = new Date(b.eventDate)
    return dateB - dateA // Ordenar por fecha descendente (más recientes primero)
  })
})

const goToLogin = () => router.push('/login?redirect=/misEntradas')

const loadTickets = async () => {
  loading.value = true
  error.value = ''
  requiresLogin.value = false

  try {
    await getCurrentUser()
    const token = getToken()

    if (!token) {
      requiresLogin.value = true
      error.value = 'Debes iniciar sesión para ver tus entradas.'
      loading.value = false
      return
    }

    tickets.value = await $fetch(`${apiBase}/tickets/me`, {
      headers: {
        Authorization: `Bearer ${token}`
      },
      credentials: 'include'
    })
  } catch (err) {
    if (err?.response?.status === 401) {
      requiresLogin.value = true
      error.value = 'Debes iniciar sesión para ver tus entradas.'
    } else {
      error.value = 'No se pudieron cargar tus entradas. Inténtalo de nuevo en unos segundos.'
    }
  } finally {
    loading.value = false
  }
}

useHead({
  title: 'Mis entradas | GoLive',
  meta: [
    { name: 'description', content: 'Consulta, descarga y gestiona todas tus entradas digitales de eventos en GoLive desde un solo lugar.' },
    { property: 'og:title', content: 'Mis entradas | GoLive' },
    { property: 'og:description', content: 'Consulta, descarga y gestiona todas tus entradas digitales de eventos en GoLive desde un solo lugar.' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: 'https://golive-hu5d.onrender.com/misEntradas' },
    { property: 'og:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'Mis entradas | GoLive' },
    { name: 'twitter:description', content: 'Consulta, descarga y gestiona todas tus entradas digitales de eventos en GoLive desde un solo lugar.' },
    { name: 'twitter:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' }
  ],
  link: [
    { rel: 'canonical', href: 'https://golive-hu5d.onrender.com/misEntradas' }
  ]
})

onMounted(loadTickets)
</script>

<style scoped>
.mis-entradas-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #05070b 0%, #111827 100%);
  color: #f9fafb;
  display: flex;
  flex-direction: column;
}

.tickets-wrapper {
  width: min(1200px, 90%);
  margin: 40px auto;
  padding-top: 80px;
  flex: 1;
  position: relative;
}

.mis-entradas-page {
  padding-top: 0;
  margin-top: 0;
}

/* Solo en móviles: eliminar espacio superior */
@media (max-width: 768px) {
  .tickets-wrapper {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
  
  .mis-entradas-page {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 40px;
  padding: 24px 32px;
  border-radius: 24px;
  background: rgba(15, 23, 42, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 25px 60px rgba(3, 7, 18, 0.45);
  backdrop-filter: blur(14px);
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 2px;
  font-size: 0.75rem;
  color: #f472b6;
}

.page-header h1 {
  margin: 6px 0 12px;
  font-size: 2.3rem;
}

.page-header p {
  color: #9ca3af;
  margin: 0;
}

.tickets-wrapper::before {
  content: '';
  position: absolute;
  top: 10px;
  right: -60px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(255, 0, 87, 0.25), transparent 70%);
  pointer-events: none;
}

.cta {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 10px 20px;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.state-card {
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  color: #e5e7eb;
}

.state-card--error {
  border-color: rgba(248, 113, 113, 0.4);
}

.state-card--empty {
  border-color: rgba(96, 165, 250, 0.4);
}

.state-card i {
  font-size: 2.5rem;
}

.tickets-sections {
  display: flex;
  flex-direction: column;
  gap: 48px;
}

.tickets-section {
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 24px 32px;
  border-radius: 20px;
  background: rgba(15, 23, 42, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(3, 7, 18, 0.4);
  backdrop-filter: blur(12px);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.section-header:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(3, 7, 18, 0.5);
}

.section-header__content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.section-header__icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}

.section-header__icon.upcoming {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(52, 211, 153, 0.2));
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.section-header__icon.past {
  background: linear-gradient(135deg, rgba(107, 114, 128, 0.2), rgba(156, 163, 175, 0.2));
  color: #9ca3af;
  border: 1px solid rgba(107, 114, 128, 0.3);
}

.section-header__title {
  margin: 0 0 6px 0;
  font-size: 1.75rem;
  font-weight: 700;
  color: #f9fafb;
}

.section-header__subtitle {
  margin: 0;
  color: #9ca3af;
  font-size: 0.95rem;
}

.section-header__badge {
  flex-shrink: 0;
}

.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 0.95rem;
}

.badge--upcoming {
  background: linear-gradient(135deg, #10b981, #34d399);
  color: #fff;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.badge--past {
  background: linear-gradient(135deg, #6b7280, #9ca3af);
  color: #fff;
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

.tickets-grid {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
}

@media (max-width: 768px) {
  .tickets-grid {
    gap: 16px;
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .tickets-grid {
    gap: 12px;
  }
}

.tickets-grid--past {
  opacity: 0.85;
}

.ticket-card--upcoming {
  position: relative;
}

.ticket-card--upcoming::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: 16px 16px 0 0;
  z-index: 1;
}

.ticket-card--past {
  position: relative;
  opacity: 0.9;
}

.ticket-card--past::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #6b7280, #9ca3af);
  border-radius: 16px 16px 0 0;
  z-index: 1;
}

.spinner-border {
  width: 40px;
  height: 40px;
  border-width: 3px;
  color: #ff0057;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 20px;
  }

  .tickets-wrapper {
    width: 100%;
    padding: 80px 16px 40px;
  }

  .tickets-wrapper::before {
    display: none;
  }

  .tickets-sections {
    gap: 32px;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
    gap: 16px;
  }

  .section-header__content {
    width: 100%;
  }

  .section-header__badge {
    align-self: flex-end;
  }

  .section-header__title {
    font-size: 1.4rem;
  }

  .section-header__subtitle {
    font-size: 0.85rem;
  }

  .section-header__icon {
    width: 48px;
    height: 48px;
    font-size: 1.3rem;
  }

  .badge {
    min-width: 32px;
    height: 32px;
    padding: 0 12px;
    font-size: 0.85rem;
  }

  .tickets-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .tickets-grid {
    gap: 12px;
  }

  .tickets-wrapper {
    padding: 60px 12px 30px;
  }

  .page-header {
    padding: 16px;
    margin-bottom: 24px;
  }

  .page-header h1 {
    font-size: 1.8rem;
  }

  .page-header p {
    font-size: 0.9rem;
  }

  .section-header {
    padding: 14px;
    margin-bottom: 20px;
  }

  .section-header__content {
    gap: 12px;
  }

  .section-header__icon {
    width: 44px;
    height: 44px;
    font-size: 1.2rem;
  }

  .section-header__title {
    font-size: 1.25rem;
  }

  .section-header__subtitle {
    font-size: 0.8rem;
  }
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

  client-only {
    display: block;
    margin: 0;
    padding: 0;
  }

  client-only + * {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>

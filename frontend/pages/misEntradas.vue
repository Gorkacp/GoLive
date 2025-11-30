<template>
  <div class="mis-entradas-page">
    <Header />
    <div class="tickets-wrapper">
      <div class="page-header">
        <div>
          <p class="eyebrow">{{ $t('Área privada') }}</p>
          <h1>{{ $t('Mis Entradas') }}</h1>
          <p>{{ $t('Descarga y gestiona todas tus entradas digitales desde un único lugar.') }}</p>
        </div>
        <NuxtLink class="cta" to="/eventos">
          <i class="bi bi-plus-circle"></i> {{ $t('Descubrir eventos') }}
        </NuxtLink>
      </div>

      <div v-if="loading" class="state-card">
        <div class="spinner-border" role="status"></div>
        <p>{{ $t('Cargando tus entradas...') }}</p>
      </div>

      <div v-else-if="error" class="state-card state-card--error">
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
        <NuxtLink class="cta" to="/eventos">
          {{ $t('Explorar eventos') }}
        </NuxtLink>
      </div>

      <div v-else class="tickets-grid">
        <TicketCard v-for="ticket in tickets" :key="ticket.id" :ticket="ticket" />
      </div>
    </div>
    <Footer />
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

.tickets-grid {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
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
}
</style>

<template>
  <client-only>
    <Header />
  </client-only>

  <div class="container my-5 page-content">
    <h1 class="text-center mb-4">{{ $t('Pago') }}</h1>

    <div v-if="!userLogged" class="alert alert-warning text-center shadow-sm p-4">
      <h5 class="mb-3">{{ $t('Debes iniciar sesión antes de proceder al pago') }}</h5>
      <button class="btn btn-primary" @click="goToLogin">{{ $t('Ir a Login') }}</button>
    </div>

    <div v-else>
      <!-- Card del evento -->
      <div class="card mb-4 shadow-lg border-0" v-if="event.title !== $t('Evento no encontrado')">
        <img :src="event.image" :alt="event.title" class="card-img-top img-fluid" />
        <div class="card-body text-center">
          <h3 class="card-title fw-bold">{{ event.title }}</h3>
          <p class="text-muted">{{ event.venue }}</p>
        </div>
      </div>

      <div v-else class="alert alert-danger text-center">
        <h5>{{ $t('Evento no encontrado') }}</h5>
      </div>

      <!-- Tickets -->
      <div class="mb-4" v-if="tickets.length">
        <h4 class="fw-semibold">{{ $t('Entradas') }}</h4>
        <ul class="list-group mb-3 shadow-sm">
          <li
            class="list-group-item d-flex justify-content-between align-items-center"
            v-for="(ticket, index) in tickets"
            :key="index"
          >
            {{ ticket.name }} x {{ ticket.quantity }}
            <span class="fw-bold">
              {{ (ticket.price * ticket.quantity + ticket.quantity * COMMISSION).toFixed(2) }} €
            </span>
          </li>
        </ul>
      </div>

      <!-- Formulario comprador -->
      <div class="card p-4 shadow-lg border-0" v-if="event.title !== $t('Evento no encontrado')">
        <h4 class="fw-bold mb-3 text-center">{{ $t('Datos del comprador') }}</h4>
        <form @submit.prevent="payWithPaypal">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">{{ $t('Nombre') }}</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.name"
                :class="{ 'is-invalid': errors.name }"
              />
              <div class="invalid-feedback">{{ errors.name }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">{{ $t('Apellidos') }}</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.lastname"
                :class="{ 'is-invalid': errors.lastname }"
              />
              <div class="invalid-feedback">{{ errors.lastname }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">{{ $t('Código Postal') }}</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.postalCode"
                :class="{ 'is-invalid': errors.postalCode }"
              />
              <div class="invalid-feedback">{{ errors.postalCode }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">{{ $t('DNI') }}</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.dni"
                :class="{ 'is-invalid': errors.dni }"
              />
              <div class="invalid-feedback">{{ errors.dni }}</div>
            </div>
          </div>

          <!-- Total -->
          <div class="mt-4 text-end">
            <h4 class="fw-bold">
              {{ $t('Total') }}: <span class="text-primary">{{ total.toFixed(2) }} €</span>
            </h4>
            <small class="text-muted">
              {{ $t('Incluye comisión por entrada', { commission: COMMISSION.toFixed(2) }) }}
            </small>
          </div>

          <!-- Botón PayPal -->
          <div class="d-flex justify-content-center mt-4">
            <div id="paypal-button-container" class="w-100" style="max-width: 400px;"></div>
          </div>
        </form>
      </div>
    </div>
  </div>

  <Footer />
</template>


<script setup>
import { ref, reactive, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from '#app'
import { useCartStore } from '~/stores/cart'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

const userLogged = ref(true)
const goToLogin = () => router.push('/login')

// Comisión por entrada
const COMMISSION = 1.5 // 1,50 € por entrada

// Genera slug igual que en el carrito
const generateSlug = (title) =>
  title.toLowerCase()
       .normalize('NFD')
       .replace(/[\u0300-\u036f]/g, '')
       .replace(/[^a-z0-9]+/g, '-')
       .replace(/(^-|-$)/g, '')

// Datos del evento y tickets
const event = ref({ title: 'Evento no encontrado', venue: '', image: '' })
const tickets = ref([])
const total = ref(0)

// Datos del comprador
const buyer = ref({
  name: '',
  lastname: '',
  postalCode: '',
  dni: ''
})

// Errores de validación
const errors = reactive({
  name: '',
  lastname: '',
  postalCode: '',
  dni: ''
})

const validateBuyer = () => {
  errors.name = buyer.value.name ? '' : 'El nombre es obligatorio.'
  errors.lastname = buyer.value.lastname ? '' : 'Los apellidos son obligatorios.'
  errors.postalCode = buyer.value.postalCode ? '' : 'El código postal es obligatorio.'
  errors.dni = buyer.value.dni ? '' : 'El DNI es obligatorio.'
  return !errors.name && !errors.lastname && !errors.postalCode && !errors.dni
}

// Función de pago (solo valida antes de PayPal)
const payWithPaypal = () => {
  if (!validateBuyer()) return
}

// Carga los datos del carrito desde store o localStorage
onMounted(async () => {
  let order = cartStore.order

  // Si no hay order en store, intentar cargar de localStorage
  if (!order || !order.event || generateSlug(order.event.title) !== route.params.slug) {
    const saved = localStorage.getItem('currentOrder')
    if (saved) {
      try {
        const savedOrder = JSON.parse(saved)
        if (savedOrder?.event && generateSlug(savedOrder.event.title) === route.params.slug) {
          order = savedOrder
        }
      } catch (e) {
        console.warn('Error parsing localStorage order:', e)
      }
    }
  }

  if (order?.event) {
    event.value = order.event
    tickets.value = order.tickets || []

    // Total con comisión
    total.value = tickets.value.reduce((sum, t) => sum + t.price * t.quantity + t.quantity * COMMISSION, 0)

    // Guardar en localStorage siempre para la primera carga
    localStorage.setItem('currentOrder', JSON.stringify({ ...order, total: total.value }))
  } else {
    event.value = { title: 'Evento no encontrado', venue: '', image: '' }
    tickets.value = []
    total.value = 0
  }

  // Esperar a que Vue haya actualizado el DOM y tickets antes de renderizar PayPal
  await nextTick()
  loadPaypal()
})

// Actualiza localStorage si cambian las cantidades y recalcula total con comisión
watch(tickets, (newTickets) => {
  total.value = newTickets.reduce((sum, t) => sum + t.price * t.quantity + t.quantity * COMMISSION, 0)
  const order = { event: event.value, tickets: newTickets, total: total.value }
  localStorage.setItem('currentOrder', JSON.stringify(order))
}, { deep: true })

// Carga PayPal
const loadPaypal = () => {
  if (!userLogged.value || !tickets.value.length) return
  if (window.paypal) { renderPaypalButton(); return }

  const script = document.createElement('script')
  script.src = 'https://www.paypal.com/sdk/js?client-id=ASR1-nfvyjoSisSGbs8LK3xTAXdwQm48UXi0esdNNH7EWQFXdIgHeGkjV1BfdCDO5kdV53F9u98jzq31&currency=EUR'
  script.addEventListener('load', renderPaypalButton)
  document.body.appendChild(script)
}

// Renderiza el botón PayPal
const renderPaypalButton = () => {
  if (!window.paypal || !tickets.value.length) return

  window.paypal.Buttons({
    style: { layout: 'vertical', color: 'gold', shape: 'rect', label: 'paypal', height: 50 },
    createOrder: (data, actions) => {
      if (!validateBuyer()) return actions.reject()
      return actions.order.create({
        purchase_units: [
          {
            amount: { value: total.value.toFixed(2) },
            description: `${event.value.title || 'Evento'} - Entradas`
          }
        ]
      })
    },
    onApprove: (data, actions) => actions.order.capture().then(() => router.push('/TicketCard')),
    onError: (err) => console.error('Error en el pago:', err)
  }).render('#paypal-button-container')
}
</script>

<style scoped>
.card-img-top { max-height: 280px; object-fit: cover; border-radius: 10px 10px 0 0; }
.is-invalid { border-color: #dc3545; }
.invalid-feedback { display: block; }
.page-content { padding-top: 60px; }
</style>

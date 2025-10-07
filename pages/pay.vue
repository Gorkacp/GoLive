<template>
  <client-only>
    <Header @buscar-evento="filtrarEventos" />
  </client-only>

  <div class="container my-5 page-content">
    <h1 class="text-center mb-4">Pago</h1>

    <!-- Verificación de usuario -->
    <div v-if="!userLogged" class="alert alert-warning text-center shadow-sm p-4">
      <h5 class="mb-3">Debes iniciar sesión antes de proceder al pago</h5>
      <button class="btn btn-primary" @click="goToLogin">Ir a Login</button>
    </div>

    <div v-else>
      <!-- Card del evento -->
      <div class="card mb-4 shadow-lg border-0">
        <img :src="event.image" :alt="event.title" class="card-img-top img-fluid" />
        <div class="card-body text-center">
          <h3 class="card-title fw-bold">{{ event.title }}</h3>
          <p class="text-muted">{{ event.venue }}</p>
        </div>
      </div>

      <!-- Tickets -->
      <div class="mb-4">
        <h4 class="fw-semibold">Entradas</h4>
        <ul class="list-group mb-3 shadow-sm">
          <li
            class="list-group-item d-flex justify-content-between align-items-center"
            v-for="(ticket, index) in tickets"
            :key="index"
          >
            {{ ticket.name }} x {{ ticket.quantity }}
            <span class="fw-bold">{{ (ticket.price * ticket.quantity).toFixed(2) }} €</span>
          </li>
        </ul>
      </div>

      <!-- Formulario comprador -->
      <div class="card p-4 shadow-lg border-0 mb-4">
        <h4 class="fw-bold mb-3 text-center">Datos del comprador</h4>
        <form @submit.prevent="payWithPaypal">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label">Nombre</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.name"
                :class="{ 'is-invalid': errors.name }"
              />
              <div class="invalid-feedback">{{ errors.name }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Apellidos</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.lastname"
                :class="{ 'is-invalid': errors.lastname }"
              />
              <div class="invalid-feedback">{{ errors.lastname }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">Código Postal</label>
              <input
                type="text"
                class="form-control"
                v-model="buyer.postalCode"
                :class="{ 'is-invalid': errors.postalCode }"
              />
              <div class="invalid-feedback">{{ errors.postalCode }}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label">DNI</label>
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
              Total: <span class="text-primary">{{ total.toFixed(2) }} €</span>
            </h4>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from '#app'

const router = useRouter()
const route = useRoute()

const userLogged = ref(true)
const goToLogin = () => router.push('/login')

const event = ref(JSON.parse(route.query.event || '{}'))
const tickets = ref(JSON.parse(route.query.tickets || '[]'))
const total = ref(parseFloat(route.query.total) || 0)

const buyer = ref({
  name: '',
  lastname: '',
  postalCode: '',
  dni: ''
})

// Errores por campo
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

const payWithPaypal = () => {
  if (!validateBuyer()) return
  // Aquí se procesa el pago, sin alertas
}

// Carga del script de PayPal
onMounted(() => {
  if (!userLogged.value) return

  if (window.paypal) {
    renderPaypalButton()
    return
  }

  const script = document.createElement('script')
  script.src =
    'https://www.paypal.com/sdk/js?client-id=ASR1-nfvyjoSisSGbs8LK3xTAXdwQm48UXi0esdNNH7EWQFXdIgHeGkjV1BfdCDO5kdV53F9u98jzq31&currency=EUR'
  script.addEventListener('load', renderPaypalButton)
  document.body.appendChild(script)
})

const renderPaypalButton = () => {
  if (!window.paypal) return

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
    onApprove: (data, actions) => {
      return actions.order.capture().then(() => {
        router.push('/TicketCard')
      })
    },
    onError: (err) => {
      console.error('Error en el pago:', err)
      // No se muestran alertas
    }
  }).render('#paypal-button-container')
}
</script>

<style scoped>
.card-img-top {
  max-height: 280px;
  object-fit: cover;
  border-radius: 10px 10px 0 0;
}

.is-invalid {
  border-color: #dc3545;
}

.invalid-feedback {
  display: block;
}

/* Padding para que el contenido no quede debajo del header fijo */
.page-content {
  padding-top: 60px; /* Ajusta según la altura de tu header */
}
</style>

<template>
  <client-only>
    <Header @buscar-evento="filtrarEventos" />
  </client-only>

  <div class="container carrito-container">

    <!-- Nombre del evento y ubicación -->
    <div class="text-center mb-4 mt-header-space">
      <h1 class="fw-bold display-4">{{ event.title }}</h1>
      <p class="text-muted fs-5">{{ event.venue }}</p>
    </div>

    <!-- Imagen tipo hero -->
    <div class="mb-5">
      <img :src="event.image" :alt="event.title" class="img-fluid rounded-3 w-100 hero-img shadow-sm" />
    </div>

    <!-- Entradas -->
    <div class="row g-4">
      <transition-group name="slide-fade" tag="div" class="row g-4">
        <div
          v-for="(ticket, index) in tickets"
          :key="index"
          class="col-12 col-md-6 col-lg-4"
        >
          <div class="card h-100 shadow ticket-card border-0">
            <div class="card-body d-flex flex-column justify-content-between">
              <div>
                <h5 class="card-title fw-bold">{{ ticket.name }}</h5>
                <p class="card-text mb-2 price">{{ ticket.price.toFixed(2) }} €</p>
                <p class="card-text text-muted">{{ ticket.description }}</p>
                <p class="card-text text-primary">
                  <small class="cursor-pointer" @click="showPolicy = true">
                    Consulta la política de menores
                  </small>
                </p>
              </div>
              <div class="mt-3 d-flex justify-content-between align-items-center">
                <div>
                  <label class="form-label mb-1">Cantidad:</label>
                  <input 
                    type="number" 
                    min="0" 
                    class="form-control quantity-input" 
                    v-model.number="ticket.quantity"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- Total -->
    <div class="text-end mt-4">
      <h4>Total: {{ total.toFixed(2) }} €</h4>
      <button class="btn btn-dark btn-lg mt-2" @click="goToPay">Comprar Todo</button>
    </div>
  </div>

  <!-- Modal política de menores -->
  <transition name="fade">
    <div v-if="showPolicy" class="custom-modal d-flex justify-content-center align-items-center">
      <div class="modal-content p-4 rounded-3 shadow-lg">
        <div class="modal-header">
          <h5 class="modal-title">Política de Menores</h5>
          <button type="button" class="btn-close" @click="showPolicy = false"></button>
        </div>
        <div class="modal-body">
          <p>Los menores de 16 años deberán asistir acompañados de un adulto y contar con la autorización correspondiente.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-primary" @click="showPolicy = false">Aceptar</button>
        </div>
      </div>
    </div>
  </transition>

  <Footer />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from '#app' // Nuxt 3

const router = useRouter()
const route = useRoute()

const event = ref({
  title: '',
  venue: '',
  price: 0,
  image: ''
})

onMounted(() => {
  event.value.title = route.query.title || 'Evento Desconocido'
  event.value.venue = route.query.venue || ''
  event.value.price = parseFloat(route.query.price) || 0
  event.value.image = route.query.image || '/assets/img/default.jpg'
})

const tickets = ref([
  {
    name: 'Entrada General',
    price: 35.00,
    description: 'Incluye acceso al concierto (a pesar de ser entrada GENERAL se ve bien y se disfruta mucho del concierto)',
    quantity: 0
  },
  {
    name: 'Entrada Early Pass',
    price: 50.00,
    description: 'Acceso justo después de los VIP + acceso directo al concierto + vista privilegiada en el concierto',
    quantity: 0
  },
  {
    name: 'Entrada VIP',
    price: 100.00,
    description: 'Acceso prueba de sonido + conocer al artista',
    quantity: 0
  }
])

const total = computed(() => {
  return tickets.value.reduce((acc, ticket) => acc + ticket.price * ticket.quantity, 0)
})

const showPolicy = ref(false)

// Función para ir a pay.vue
const goToPay = () => {
  const selectedTickets = tickets.value.filter(t => t.quantity > 0)
  if (selectedTickets.length === 0) {
    alert('Selecciona al menos un ticket antes de continuar')
    return
  }

  router.push({
    path: '/pay',
    query: {
      total: total.value.toFixed(2),
      tickets: JSON.stringify(selectedTickets),
      event: JSON.stringify({
        title: event.value.title,
        venue: event.value.venue,
        image: event.value.image
      })
    }
  })
}
</script>

<style scoped>
.mt-header-space { margin-top: 100px; }
.hero-img { object-fit: cover; max-height: 400px; width: 100%; }
.ticket-card {
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background-color: #fff;
}
.ticket-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0,0,0,0.15);
}
.price { font-size: 1.25rem; color: #333; }
.quantity-input { width: 80px; }
.custom-modal {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  z-index: 1050;
  display: flex;
  justify-content: center; align-items: center;
}
.custom-modal .modal-content {
  max-width: 500px; background-color: #fff;
  animation: scale-in 0.3s ease forwards;
}
.slide-fade-enter-active, .slide-fade-leave-active { transition: all 0.5s ease; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; transform: translateY(20px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@keyframes scale-in { 0% { transform: scale(0.7); opacity: 0; } 100% { transform: scale(1); opacity: 1; } }
</style>

<template>
  <div class="d-flex flex-column min-vh-100">
    <!-- Header -->
    <client-only>
      <Header @buscar-evento="filtrarEventos" />
    </client-only>

    <!-- Hero -->
    <div class="hero position-relative text-center d-flex align-items-center justify-content-center">
      <img src="/assets/img/1.jpg" alt="Hero" class="hero-img w-100" />
      <div class="hero-text position-absolute">
        <h1 class="display-3 fw-bold">GoLive</h1>
        <p class="lead">Compra tus entradas para conciertos y festivales fácilmente.</p>
      </div>
    </div>

    <!-- Grid de eventos -->
    <div class="container my-5 flex-grow-1">
      <h2 class="mb-4 text-center text-dark">Próximos Eventos</h2>

      <!-- Spinner mientras carga -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-3">Cargando eventos...</p>
      </div>

      <!-- Eventos filtrados -->
      <div v-else-if="filteredEvents.length" class="row g-4">
        <div
          v-for="event in filteredEvents"
          :key="event.id"
          class="col-12 col-sm-6 col-md-4 col-lg-3"
        >
          <EventCard :event="event" />
        </div>
      </div>

      <!-- Mensaje si no hay eventos -->
      <div v-else class="text-center py-5">
        <p class="text-muted">No hay eventos disponibles por el momento.</p>
      </div>
    </div>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup>
import Footer from '~/components/Footer.vue'
import EventCard from '~/components/EventCard.vue'
import Header from '~/components/Header.vue'
import { ref, onMounted } from 'vue'
import { collection, getDocs } from 'firebase/firestore'
import { useNuxtApp } from '#app'

const { $db } = useNuxtApp()

const events = ref([])
const filteredEvents = ref([])
const loading = ref(true)

const loadEvents = async () => {
  loading.value = true
  try {
    const snapshot = await getDocs(collection($db, 'events'))
    events.value = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }))
    filteredEvents.value = [...events.value] // Inicialmente muestra todos
  } catch (error) {
    console.error('Error cargando eventos:', error)
  } finally {
    loading.value = false
  }
}

// Filtra los eventos por título (case-insensitive)
const filtrarEventos = (texto) => {
  if (!texto) {
    filteredEvents.value = [...events.value]
  } else {
    filteredEvents.value = events.value.filter(event =>
      event.title && event.title.toLowerCase().includes(texto.toLowerCase())
    )
  }
}

onMounted(() => {
  loadEvents()
})
</script>

<style scoped>
.hero {
  height: 60vh;
  position: relative;
  overflow: hidden;
}

.hero-img {
  object-fit: cover;
  height: 100%;
  width: 100%;
  filter: brightness(0.5);
}

.hero-text {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.hero-text h1 {
  color: #ffffff; /* Título GoLive en blanco */
  font-size: 4rem; /* Más grande para destacar */
  font-weight: 900;
  text-shadow: 2px 2px 8px rgba(0,0,0,0.7);
}

.hero-text p {
  font-size: 1.5rem;
  color: #ffffff;
}

h2.text-dark {
  color: #0a0a0a; /* Negro oscuro, igual que el footer */
  font-weight: 700;
}
</style>

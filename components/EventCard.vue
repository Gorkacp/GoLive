<template>
  <div class="card h-100 shadow-sm">
    <img :src="event.image" class="card-img-top" :alt="event.title">
    <div class="card-body d-flex flex-column">
      <h5 class="card-title">{{ event.title }}</h5>
      <p class="card-text mb-1"><strong>{{ event.venue }}</strong></p>
      <p class="card-text mb-1">{{ formatDate(event.date) }}</p>
      <p class="card-text mt-auto fw-bold">Desde {{ event.price }} €</p>
      <a class="btn btn-dark mt-2 w-100" @click.prevent="irAlCarrito">
        Comprar Entrada
      </a>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  event: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const irAlCarrito = () => {
  router.push({
    path: '/carrito',
    query: {
      title: props.event.title,
      venue: props.event.venue,
      price: props.event.price,
      image: props.event.image
    }
  })
}

// Función para formatear fechas correctamente
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const safeDateStr = dateStr.replace(/-/g, '/')
  const date = new Date(safeDateStr)
  const options = { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }
  return date.toLocaleDateString('es-ES', options).toUpperCase()
}
</script>

<style scoped>
.card-img-top {
  height: 200px;
  object-fit: cover;
}

/* Efecto hover en el botón negro */
.btn-dark:hover {
  background-color: #333;
  color: #fff;
}
</style>

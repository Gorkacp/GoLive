<template>
  <div class="container my-5">
    <!-- Header -->
    <client-only>
      <Header />
    </client-only>
    <h2 class="mb-5 text-center">Administración de Eventos</h2>

    <!-- Formulario Crear / Editar -->
    <div class="card shadow-sm p-4 mb-5">
      <h4 class="mb-4 text-primary text-center">
        {{ editingEvent ? 'Editar Evento' : 'Crear Evento' }}
      </h4>
      <form @submit.prevent="saveEvent">
        <div class="row g-3">
          <!-- Título -->
          <div class="col-md-6">
            <div class="form-floating">
              <input type="text" v-model="title" class="form-control" id="title" placeholder="Título" required />
              <label for="title">Título del evento</label>
            </div>
          </div>

          <!-- Lugar -->
          <div class="col-md-6">
            <div class="form-floating">
              <input type="text" v-model="venue" class="form-control" id="venue" placeholder="Lugar" required />
              <label for="venue">Lugar</label>
            </div>
          </div>

          <!-- Fecha -->
          <div class="col-md-4">
            <div class="form-floating">
              <input type="date" v-model="date" class="form-control" id="date" placeholder="Fecha" required />
              <label for="date">Fecha</label>
            </div>
          </div>

          <!-- Precio -->
          <div class="col-md-4">
            <div class="form-floating">
              <input type="number" v-model="price" class="form-control" id="price" placeholder="Precio (€)" required />
              <label for="price">Precio (€)</label>
            </div>
          </div>

          <!-- Entradas disponibles -->
          <div class="col-md-4">
            <div class="form-floating">
              <input type="number" v-model="availableTickets" class="form-control" id="tickets" placeholder="Entradas" required />
              <label for="tickets">Entradas disponibles</label>
            </div>
          </div>

          <!-- Categoría -->
          <div class="col-md-6">
            <select v-model="category" class="form-select" required>
              <option disabled value="">Selecciona categoría</option>
              <option value="concierto">Concierto</option>
              <option value="festival">Festival</option>
            </select>
          </div>

          <!-- Imagen: archivo o URL -->
          <div class="col-md-6">
            <label class="form-label">Imagen</label>
            <div class="input-group">
              <input type="file" @change="handleFile" class="form-control" />
              <input type="url" v-model="imageUrlInput" class="form-control" placeholder="O pega una URL de imagen" />
            </div>
            <small class="text-muted">Puedes subir un archivo o usar una URL.</small>
          </div>

          <!-- Botones -->
          <div class="col-12 d-flex gap-2 mt-3">
            <button type="submit" class="btn btn-success flex-fill">
              {{ editingEvent ? 'Actualizar Evento' : 'Crear Evento' }}
            </button>
            <button v-if="editingEvent" type="button" @click="cancelEdit" class="btn btn-secondary flex-fill">
              Cancelar
            </button>
          </div>
        </div>
      </form>
    </div>

    <!-- Lista de Eventos -->
    <div class="row g-4">
      <div v-for="event in events" :key="event.id" class="col-12 col-sm-6 col-md-4 col-lg-3">
        <div class="card h-100 shadow-sm border-0">
          <img
            :src="event.image"
            class="card-img-top"
            :alt="event.title"
            style="object-fit: cover; height: 200px;"
          />
          <div class="card-body d-flex flex-column">
            <h5 class="card-title text-primary">{{ event.title }}</h5>
            <p class="card-text mb-1"><strong>Lugar:</strong> {{ event.venue }}</p>
            <p class="card-text mb-1"><strong>Fecha:</strong> {{ event.date }}</p>
            <p class="card-text mb-1"><strong>Categoría:</strong> {{ event.category }}</p>
            <p class="card-text mb-3 text-success"><strong>Desde:</strong> {{ event.price }} €</p>
            <div class="mt-auto d-flex justify-content-between">
              <button @click="editEvent(event)" class="btn btn-outline-primary btn-sm">Editar</button>
              <button @click="deleteEvent(event.id)" class="btn btn-outline-danger btn-sm">Eliminar</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { collection, getDocs, addDoc, updateDoc, deleteDoc, doc } from 'firebase/firestore'

const { $db } = useNuxtApp()

const title = ref('')
const venue = ref('')
const date = ref('')
const price = ref('')
const availableTickets = ref(100)
const category = ref('')
const editingEvent = ref(null)
const events = ref([])
let imageFile = null
const imageUrlInput = ref('')

const handleFile = (e) => {
  const file = e.target.files[0]
  if (!file) return

  if (file.size > 15 * 1024 * 1024) {
    alert('El archivo debe ser menor de 15MB')
    e.target.value = null
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    imageFile = reader.result
    imageUrlInput.value = ''
  }
  reader.readAsDataURL(file)
}

const loadEvents = async () => {
  const querySnapshot = await getDocs(collection($db, 'events'))
  events.value = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }))
}

onMounted(() => loadEvents())

const saveEvent = async () => {
  const finalImage = imageFile || imageUrlInput.value || (editingEvent.value ? editingEvent.value.image : '')

  const eventData = {
    title: title.value,
    venue: venue.value,
    date: date.value,
    price: Number(price.value),
    availableTickets: Number(availableTickets.value),
    category: category.value,
    image: finalImage
  }

  if (editingEvent.value) {
    await updateDoc(doc($db, 'events', editingEvent.value.id), eventData)
    editingEvent.value = null
  } else {
    await addDoc(collection($db, 'events'), eventData)
  }

  resetForm()
  await loadEvents()
}

const editEvent = (event) => {
  editingEvent.value = event
  title.value = event.title
  venue.value = event.venue
  date.value = event.date
  price.value = event.price
  availableTickets.value = event.availableTickets
  category.value = event.category || ''
  imageFile = event.image || null
  imageUrlInput.value = event.image || ''
}

const cancelEdit = () => resetForm()
const deleteEvent = async (id) => {
  if (!confirm('¿Seguro que quieres eliminar este evento?')) return
  await deleteDoc(doc($db, 'events', id))
  await loadEvents()
}
const resetForm = () => {
  title.value = ''
  venue.value = ''
  date.value = ''
  price.value = ''
  availableTickets.value = 100
  category.value = ''
  imageFile = null
  imageUrlInput.value = ''
  editingEvent.value = null
}
</script>

<style scoped>
.card img {
  transition: transform 0.3s ease;
}
.card:hover img {
  transform: scale(1.03);
}
</style>

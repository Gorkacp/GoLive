<template>
  <div>
    <Head>
      <Title>Administración de Eventos - GoLive</Title>
      <Meta name="description" content="Crea y gestiona eventos musicales, conciertos y festivales. Plataforma completa de administración de eventos." />
      <Meta name="keywords" content="eventos, conciertos, festivales, música, entradas, administración" />
    </Head>

    <main class="container my-5">
      <ClientOnly>
        <Header />
      </ClientOnly>

      <section aria-labelledby="page-title">
        <h1 id="page-title" class="mb-5 text-center mt-header-space">
          {{ $t('Administración de Eventos') }}
        </h1>

        <!-- Formulario Crear / Editar -->
        <article class="card shadow-sm p-4 mb-5" aria-labelledby="form-title">
          <h2 id="form-title" class="mb-4 text-primary text-center">
            {{ editingEvent ? $t('Editar Evento') : $t('Crear Evento') }}
          </h2>
          <form @submit.prevent="saveEvent">
            <div class="row g-3">
              <!-- Título -->
              <div class="col-md-6">
                <div class="form-floating">
                  <input type="text" v-model="title" class="form-control" id="title" :placeholder="$t('Título')" required />
                  <label for="title">{{ $t('Título del evento') }}</label>
                </div>
              </div>

              <!-- Lugar -->
              <div class="col-md-6">
                <div class="form-floating">
                  <input type="text" v-model="venue" class="form-control" id="venue" :placeholder="$t('Lugar')" required />
                  <label for="venue">{{ $t('Lugar') }}</label>
                </div>
              </div>

              <!-- Ubicación exacta -->
              <div class="col-md-6">
                <div class="form-floating">
                  <input type="text" v-model="location" class="form-control" id="location" :placeholder="$t('Ubicación exacta')" required />
                  <label for="location">{{ $t('Ubicación / dirección') }}</label>
                </div>
              </div>

              <!-- Fecha -->
              <div class="col-md-3">
                <div class="form-floating">
                  <input type="date" v-model="date" class="form-control" id="date" required />
                  <label for="date">{{ $t('Fecha') }}</label>
                </div>
              </div>

              <!-- Hora -->
              <div class="col-md-3">
                <div class="form-floating">
                  <input type="time" v-model="time" class="form-control" id="time" required />
                  <label for="time">{{ $t('Hora') }}</label>
                </div>
              </div>

              <!-- Categoría -->
              <div class="col-md-6">
                <select v-model="category" class="form-select" required>
                  <option disabled value="">{{ $t('Selecciona categoría') }}</option>
                  <option value="concierto">{{ $t('Concierto') }}</option>
                  <option value="festival">{{ $t('Festival') }}</option>
                </select>
              </div>

              <!-- Imagen -->
              <div class="col-md-6">
                <label class="form-label">{{ $t('Imagen') }}</label>
                <div class="input-group">
                  <input type="file" @change="handleFile" class="form-control" />
                  <input type="url" v-model="imageUrlInput" class="form-control" :placeholder="$t('O pega una URL de imagen')" />
                </div>
                <small class="text-muted">{{ $t('Puedes subir un archivo o usar una URL.') }}</small>
              </div>

              <!-- Zonas / Entradas -->
              <div class="col-12">
                <h3 class="h5">{{ $t('Zonas y precios') }}</h3>
                <p class="text-muted">{{ $t('Rellena primero el precio y luego el número de entradas para cada zona.') }}</p>
                <div v-for="(zone, index) in zones" :key="index" class="row g-2 mb-2 align-items-end">
                  <div class="col-md-4">
                    <input type="text" class="form-control" v-model="zone.name" :placeholder="$t('Nombre de zona (ej: General, VIP)')" required />
                  </div>
                  <div class="col-md-3">
                    <input type="number" class="form-control" v-model.number="zone.price" :placeholder="$t('Precio (€)')" required />
                  </div>
                  <div class="col-md-3">
                    <input type="number" class="form-control" v-model.number="zone.availableTickets" :placeholder="$t('Entradas disponibles')" required />
                  </div>
                  <div class="col-md-2">
                    <button type="button" class="btn btn-danger w-100" @click="removeZone(index)">
                      {{ $t('Eliminar') }}
                    </button>
                  </div>
                </div>
                <button type="button" class="btn btn-primary mt-2" @click="addZone">{{ $t('Añadir zona') }}</button>
              </div>

              <!-- Botones -->
              <div class="col-12 d-flex gap-2 mt-3">
                <button type="submit" class="btn btn-success flex-fill">{{ editingEvent ? $t('Actualizar Evento') : $t('Crear Evento') }}</button>
                <button v-if="editingEvent" type="button" @click="cancelEdit" class="btn btn-secondary flex-fill">{{ $t('Cancelar') }}</button>
              </div>
            </div>
          </form>
        </article>

        <!-- Lista de Eventos -->
        <section aria-labelledby="events-list-title">
          <h2 id="events-list-title" class="visually-hidden">Lista de Eventos</h2>
          <div class="row g-4">
            <article v-for="event in eventsData" :key="event.id" class="col-12 col-sm-6 col-md-4 col-lg-3">
              <div class="card h-100 shadow-sm border-0">
                <img 
                  :src="event.image" 
                  class="card-img-top" 
                  :alt="event.title" 
                  style="object-fit: cover; height: 200px;"
                  loading="lazy"
                  width="300"
                  height="200"
                />
                <div class="card-body d-flex flex-column">
                  <h3 class="card-title text-primary h5">{{ event.title }}</h3>
                  <p class="card-text mb-1"><strong>{{ $t('Lugar') }}:</strong> {{ event.venue }}</p>
                  <p class="card-text mb-1"><strong>{{ $t('Ubicación') }}:</strong> {{ event.location }}</p>
                  <p class="card-text mb-1"><strong>{{ $t('Fecha y Hora') }}:</strong> {{ event.date }} {{ event.time }}</p>
                  <p class="card-text mb-1"><strong>{{ $t('Categoría') }}:</strong> {{ event.category }}</p>
                  <div v-for="zone in event.zones" :key="zone.name" class="text-success mb-1">
                    {{ zone.name }}: {{ zone.price }} € - {{ zone.availableTickets }} {{ $t('entradas') }}
                  </div>
                  <div class="mt-auto d-flex justify-content-between">
                    <button @click="editEvent(event)" class="btn btn-outline-primary btn-sm">{{ $t('Editar') }}</button>
                    <button @click="deleteEventFn(event.id)" class="btn btn-outline-danger btn-sm">{{ $t('Eliminar') }}</button>
                  </div>
                </div>
              </div>
            </article>
          </div>
        </section>
      </section>
    </main>
  </div>
</template>

<script setup>
// SEO Meta
useSeoMeta({
  title: 'Administración de Eventos - GoLive',
  description: 'Crea y gestiona eventos musicales, conciertos y festivales. Plataforma completa de administración de eventos.',
  ogTitle: 'Administración de Eventos - GoLive',
  ogDescription: 'Plataforma para gestión completa de eventos musicales',
  ogImage: '/images/og-events.jpg',
  twitterCard: 'summary_large_image'
})

const { getEvents, createEvent, updateEvent, deleteEvent } = useEvents()

// Estados reactivos
const title = ref('')
const venue = ref('')
const location = ref('')
const date = ref('')
const time = ref('')
const category = ref('')
const editingEvent = ref(null)
let imageFile = null
const imageUrlInput = ref('')
const zones = ref([{ name: 'General', price: 50, availableTickets: 100 }])

// ✅ OPTIMIZACIÓN: Carga de eventos con SSR + caching
const { data: eventsData, refresh: refreshEvents, pending } = await useAsyncData(
  'events',
  async () => {
    const events = await getEvents()
    return events || []
  },
  {
    server: true,    // Se ejecuta en servidor (SSR)
    client: true,    // Se ejecuta en cliente (hydration)
    transform: (events) => {
      // Transformación optimizada de datos
      return events.map(event => ({
        ...event,
        // Aseguramos tipos correctos
        zones: event.zones?.map(zone => ({
          ...zone,
          price: Number(zone.price) || 0,
          availableTickets: Number(zone.availableTickets) || 0
        })) || []
      }))
    }
  }
)

// ✅ OPTIMIZACIÓN: Manejo de archivos con debounce
const handleFile = (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  if (file.size > 15 * 1024 * 1024) { 
    alert('Archivo >15MB')
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

const addZone = () => zones.value.push({ name: '', price: 0, availableTickets: 0 })
const removeZone = (index) => zones.value.splice(index, 1)

// ✅ OPTIMIZACIÓN: Guardar evento con manejo de estado optimizado
const saveEvent = async () => {
  const finalImage = imageFile || imageUrlInput.value || (editingEvent.value ? editingEvent.value.image : '')
  
  const eventData = {
    title: title.value.trim(),
    venue: venue.value.trim(),
    location: location.value.trim(),
    date: date.value,
    time: time.value,
    category: category.value,
    image: finalImage,
    zones: zones.value.map(z => ({
      name: z.name.trim(),
      price: Number(z.price) || 0,
      availableTickets: Number(z.availableTickets) || 0
    })).filter(zone => zone.name) // Filtra zonas vacías
  }

  try {
    if (editingEvent.value) {
      await updateEvent(editingEvent.value.id, eventData)
      editingEvent.value = null
    } else {
      await createEvent(eventData)
    }
    resetForm()
    await refreshEvents() // ✅ Usa refresh en lugar de recarga completa
  } catch {
    // Error silencioso
  }
}

// ✅ OPTIMIZACIÓN: Eliminar con confirmación nativa
const deleteEventFn = async (id) => {
  try {
    await deleteEvent(id)
    await refreshEvents() // ✅ Actualización optimizada
  } catch {
    // Error silencioso
  }
}

const editEvent = (event) => {
  editingEvent.value = event
  title.value = event.title
  venue.value = event.venue
  location.value = event.location
  date.value = event.date
  time.value = event.time
  category.value = event.category
  imageFile = event.image || null
  imageUrlInput.value = event.image || ''
  zones.value = event.zones?.length ? [...event.zones] : [{ name: 'General', price: 50, availableTickets: 100 }]
}

const cancelEdit = () => resetForm()

const resetForm = () => {
  title.value = ''
  venue.value = ''
  location.value = ''
  date.value = ''
  time.value = ''
  category.value = ''
  imageFile = null
  imageUrlInput.value = ''
  zones.value = [{ name: 'General', price: 50, availableTickets: 100 }]
  editingEvent.value = null
}
</script>

<style scoped>
.mt-header-space {
  margin-top: 100px;
}
.card img { 
  transition: transform 0.3s ease; 
}
.card:hover img { 
  transform: scale(1.03); 
}
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>
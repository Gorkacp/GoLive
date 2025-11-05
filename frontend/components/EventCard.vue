<template>
  <div class="card h-100 shadow-sm cursor-pointer" @click="irAlCarrito">
    <img :src="event.image" class="card-img-top" :alt="event.title">
    <div class="card-body d-flex flex-column">
      <h5 class="card-title">{{ event.title }}</h5>
      <p class="card-text mb-1"><strong>{{ event.venue }}</strong></p>
      <p class="card-text mb-1">{{ formatDate(event.date) }}</p>
      <p class="card-text mt-auto fw-bold">
        {{ t('Desde') }} {{ minPrice }} €
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  event: { type: Object, required: true }
})

const router = useRouter()
const { t, locale } = useI18n() // <-- usamos 't' para traducciones

const irAlCarrito = () => {
  const slug = props.event.title
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') 
    .replace(/[^a-z0-9]+/g, '-')    
    .replace(/(^-|-$)/g, '')        

  router.push(`/carrito/${slug}`)
}

// Formateo de fecha según idioma actual
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const safeDateStr = dateStr.replace(/-/g, '/')
  const date = new Date(safeDateStr)
  const options = { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' }

  let localeStr = 'es-ES'
  if (locale.value === 'en') localeStr = 'en-US'
  else if (locale.value === 'pt') localeStr = 'pt-PT'

  return date.toLocaleDateString(localeStr, options).toUpperCase()
}

// Precio mínimo de todas las zonas
const minPrice = computed(() => {
  if (!props.event.zones || !props.event.zones.length) return props.event.price || 0
  return Math.min(...props.event.zones.map(z => z.price))
})
</script>

<style scoped>
.card-img-top {
  height: 200px;
  object-fit: cover;
}

.cursor-pointer {
  cursor: pointer;
}

.card:hover {
  transform: scale(1.03);
  transition: transform 0.3s ease;
}
</style>

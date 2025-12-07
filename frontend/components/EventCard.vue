<template>
  <div class="card h-100 shadow-sm cursor-pointer" @click="irAlCarrito">
    <div class="image-wrapper">
      <img 
        :src="event.image" 
        class="card-img-top" 
        :alt="event.title"
        loading="lazy"
        decoding="async"
        @error="handleImageError"
      >
      <div v-if="event.category" class="category-badge">{{ event.category }}</div>
      <div v-if="isUpcoming" class="upcoming-badge">
        <i class="bi bi-clock"></i> Próximamente
      </div>
    </div>
    <div class="card-body d-flex flex-column">
      <h5 class="card-title">{{ event.title }}</h5>
      <p class="card-text mb-1">
        <i class="bi bi-geo-alt-fill"></i>
        <strong>{{ event.venue }}</strong>
      </p>
      <p class="card-text mb-1">
        <i class="bi bi-calendar-event"></i>
        {{ formatDateISO(event.date, locale) }}
      </p>
      <p class="card-text mt-auto fw-bold price-text">
        {{ t('Desde') }} <span class="price-value">{{ minPrice }} €</span>
      </p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { formatDateISO } from '~/utils/formatDate'

const props = defineProps({ event: { type: Object, required: true } })
const router = useRouter()
const { t, locale } = useI18n()

const irAlCarrito = () => {
  const slug = props.event.title
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
  router.push(`/carrito/${slug}`)
}

const minPrice = computed(() => {
  if (!props.event.zones?.length) return props.event.price || 0
  return Math.min(...props.event.zones.map(z => z.price))
})

const isUpcoming = computed(() => {
  if (!props.event.date) return false
  const eventDate = new Date(props.event.date)
  const now = new Date()
  return eventDate > now
})

const handleImageError = (event) => {
  // Fallback a una imagen por defecto si falla la carga
  event.target.src = '/assets/img/1.jpg'
}

</script>

<style scoped>
.card {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 12px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
  overflow: hidden;
  position: relative;
}

.card:hover {
  box-shadow: 0 12px 28px rgba(255, 0, 87, 0.35);
  transform: translateY(-6px);
}

.image-wrapper {
  position: relative;
  overflow: hidden;
  height: 200px;
}

.card-img-top {
  height: 200px;
  object-fit: cover;
  width: 100%;
  transition: transform 0.35s ease;
  display: block;
}

.card:hover .card-img-top {
  transform: scale(1.05);
}

.category-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(10, 10, 10, 0.85);
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  backdrop-filter: blur(10px);
  z-index: 2;
}

.upcoming-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(255, 0, 87, 0.4);
}

.upcoming-badge i {
  font-size: 0.7rem;
}

.card-body {
  padding: 16px;
  background: #0a0a0a;
}

.card-title {
  font-size: 1rem;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
  line-height: 1.3;
  transition: color 0.3s ease;
}

.card:hover .card-title {
  color: #ff0057;
}

.card-text {
  font-size: 0.875rem;
  color: #e5e7eb;
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-text i {
  color: #ff0057;
  font-size: 0.8rem;
}

.price-text {
  color: #ffffff;
  font-size: 0.9rem;
}

.price-value {
  color: #ff6b35;
  font-size: 1.1rem;
  font-weight: 700;
}

.cursor-pointer {
  cursor: pointer;
}
</style>

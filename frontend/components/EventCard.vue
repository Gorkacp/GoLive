<template>
  <div class="card h-100 shadow-sm cursor-pointer" @click="irAlCarrito">
    <img :src="event.image" class="card-img-top" :alt="event.title">
    <div class="card-body d-flex flex-column">
      <h5 class="card-title">{{ event.title }}</h5>
      <p class="card-text mb-1"><strong>{{ event.venue }}</strong></p>
      <p class="card-text mb-1">{{ formatDateISO(event.date, locale) }}</p>
      <p class="card-text mt-auto fw-bold">{{ t('Desde') }} {{ minPrice }} €</p>
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

</script>

<style scoped>
.card {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 12px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
  overflow: hidden;
}

.card:hover {
  box-shadow: 0 12px 28px rgba(255, 0, 87, 0.35);
  transform: translateY(-6px);
}

.card-img-top {
  height: 200px;
  object-fit: cover;
  width: 100%;
  transition: transform 0.35s ease;
}

.card:hover .card-img-top {
  transform: scale(1.05);
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
}

.card-text {
  font-size: 0.875rem;
  color: #e5e7eb;
}

.cursor-pointer {
  cursor: pointer;
}
</style>

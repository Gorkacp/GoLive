<template>
  <article class="ticket-card">
    <header class="ticket-card__header">
      <div>
        <p class="ticket-card__event">{{ ticket.eventTitle }}</p>
        <p class="ticket-card__venue">{{ ticket.venue }} · {{ formattedDate }}</p>
        <p class="ticket-card__location" v-if="ticket.location">{{ ticket.location }}</p>
      </div>
      <div class="ticket-card__number">
        <span>Nº</span>
        <strong>{{ ticket.ticketNumber }}</strong>
      </div>
    </header>

    <div class="ticket-card__body">
      <div class="ticket-card__section">
        <span class="label">Zona</span>
        <span class="value">{{ ticket.zoneName }}</span>
      </div>
      <div class="ticket-card__section">
        <span class="label">Asistente</span>
        <span class="value">{{ ticket.attendee?.fullName }}</span>
        <small>{{ ticket.attendee?.email }}</small>
      </div>
      <div class="ticket-card__section" v-if="ticket.attendee?.idNumber">
        <span class="label">Documento</span>
        <span class="value">{{ ticket.attendee?.idType }} · {{ ticket.attendee?.idNumber }}</span>
      </div>
      <div class="ticket-card__section insurance" v-if="ticket.insurance">
        <span class="label">Seguro de evento</span>
        <span class="value value--pill">Activo</span>
      </div>
    </div>

    <footer class="ticket-card__footer">
      <div class="ticket-card__price">
        <span class="label">Importe + gestión</span>
        <span class="value">{{ totalPerTicket }} €</span>
      </div>
      <div class="ticket-card__qr">
        <span class="label">Código seguro</span>
        <code>{{ qrPreview }}</code>
      </div>
    </footer>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  ticket: {
    type: Object,
    required: true
  }
})

const formattedDate = computed(() => {
  if (!props.ticket?.eventDate) return ''
  try {
    const date = new Date(props.ticket.eventDate)
    return new Intl.DateTimeFormat('es-ES', {
      weekday: 'short',
      day: '2-digit',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date)
  } catch {
    return props.ticket.eventDate
  }
})

const totalPerTicket = computed(() => {
  const base = Number(props.ticket?.price) || 0
  const fee = Number(props.ticket?.serviceFee) || 0
  return (base + fee).toFixed(2)
})

const qrPreview = computed(() => {
  const code = props.ticket?.qrCode || props.ticket?.ticketNumber || ''
  if (!code) return ''
  return code.length > 20 ? `${code.slice(0, 20)}…` : code
})
</script>

<style scoped>
.ticket-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ticket-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  border-bottom: 1px dashed rgba(0, 0, 0, 0.1);
  padding-bottom: 12px;
}

.ticket-card__event {
  font-size: 1.2rem;
  font-weight: 700;
  margin: 0;
  color: #111;
}

.ticket-card__venue,
.ticket-card__location {
  margin: 0;
  color: #6b7280;
  font-size: 0.9rem;
}

.ticket-card__number {
  text-align: right;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #6b7280;
}

.ticket-card__number strong {
  display: block;
  font-size: 1rem;
  color: #111;
}

.ticket-card__body {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.ticket-card__section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #9ca3af;
}

.value {
  font-size: 1rem;
  font-weight: 600;
  color: #111;
}

.value--pill {
  background: #10b981;
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  width: fit-content;
}

.insurance {
  border-left: 3px solid #10b981;
  padding-left: 12px;
}

.ticket-card__footer {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed rgba(0, 0, 0, 0.08);
  padding-top: 12px;
}

.ticket-card__price .value {
  font-size: 1.2rem;
  color: #ff0057;
}

.ticket-card__qr code {
  background: #0a0a0a;
  color: #f9fafb;
  padding: 6px 12px;
  border-radius: 8px;
  display: inline-block;
  font-family: 'Source Code Pro', monospace;
  font-size: 0.85rem;
}

@media (max-width: 640px) {
  .ticket-card {
    padding: 16px;
  }

  .ticket-card__body {
    grid-template-columns: 1fr;
  }

  .ticket-card__footer {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
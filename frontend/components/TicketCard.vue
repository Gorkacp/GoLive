<template>
  <article class="ticket-card">
    <div class="ticket-card__glow" />
    <header class="ticket-card__header" :style="heroStyle">
      <div>
        <p class="ticket-card__eyebrow">{{ formattedDate }}</p>
        <p class="ticket-card__event">{{ ticket.eventTitle }}</p>
        <p class="ticket-card__venue">{{ ticket.venue }}</p>
        <p class="ticket-card__location" v-if="ticket.location">{{ ticket.location }}</p>
      </div>
      <div class="ticket-card__number">
        <span>{{ $t?.('Entrada') || 'Entrada' }}</span>
        <strong>{{ ticket.ticketNumber }}</strong>
      </div>
    </header>

    <div class="ticket-card__body">
      <div class="ticket-card__info">
        <div class="ticket-card__section">
          <span class="label">Zona</span>
          <span class="value">{{ ticket.zoneName || 'General' }}</span>
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
        <div class="ticket-card__section insurance" v-if="ticket.insurance || ticket.attendee?.insurance">
          <span class="label">Seguro de evento</span>
          <span class="value value--pill">Activo</span>
        </div>
      </div>

      <footer class="ticket-card__meta">
        <div class="ticket-card__total">
          <span class="label">Importe total</span>
          <span class="value">{{ totalPerTicket }} €</span>
        </div>
        <button class="ticket-card__button" :disabled="!hasDigitalAssets" @click="openAssetsModal">
          <i class="bi bi-qr-code-scan"></i>
          <span>{{ hasDigitalAssets ? 'Ver QR y PDF' : 'Pendiente de generación' }}</span>
        </button>
      </footer>
    </div>

    <Transition name="fade">
      <div v-if="showAssets" class="ticket-modal" @click.self="closeAssetsModal">
        <div class="ticket-modal__dialog">
          <button class="ticket-modal__close" type="button" @click="closeAssetsModal">
            <i class="bi bi-x-lg"></i>
          </button>
          <div class="ticket-modal__header">
            <p class="ticket-modal__eyebrow">Ticket digital</p>
            <h3>{{ ticket.eventTitle }}</h3>
            <p>{{ ticket.venue }} · {{ formattedDate }}</p>
          </div>
          <div class="ticket-modal__content">
            <img v-if="qrImageSrc" :src="qrImageSrc" alt="Código QR de la entrada" />
            <p v-else class="ticket-modal__placeholder">
              Aún no se ha generado el QR para esta entrada.
            </p>
          </div>
          <div class="ticket-modal__actions">
            <button class="ticket-modal__primary" :disabled="!ticket.ticketPdf" @click="downloadTicket">
              <i class="bi bi-file-earmark-arrow-down"></i> Descargar PDF
            </button>
            <button class="ticket-modal__ghost" type="button" @click="closeAssetsModal">
              Cerrar
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </article>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  ticket: {
    type: Object,
    required: true
  }
})

const showAssets = ref(false)

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

const qrImageSrc = computed(() => {
  const data = props.ticket?.qrImageData
  if (!data) return ''
  return data.startsWith('data:') ? data : `data:image/png;base64,${data}`
})

const hasDigitalAssets = computed(() => Boolean(props.ticket?.ticketPdf || props.ticket?.qrImageData))

const heroStyle = computed(() => {
  const image = props.ticket?.eventImage
  if (!image) {
    return {}
  }
  const source = image.startsWith('url(') ? image : `url(${image})`
  return {
    backgroundImage: `linear-gradient(135deg, rgba(5,7,11,0.9) 0%, rgba(17,24,39,0.7) 100%), ${source}`
  }
})

const openAssetsModal = () => {
  if (!hasDigitalAssets.value) return
  showAssets.value = true
}

const closeAssetsModal = () => {
  showAssets.value = false
}

const downloadTicket = () => {
  if (!props.ticket?.ticketPdf || typeof window === 'undefined') return
  try {
    const binary = window.atob(props.ticket.ticketPdf)
    const len = binary.length
    const bytes = new Uint8Array(len)
    for (let i = 0; i < len; i++) {
      bytes[i] = binary.charCodeAt(i)
    }
    const blob = new Blob([bytes], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `entrada-${props.ticket.ticketNumber || 'golive'}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch (err) {
    console.error('No se pudo descargar el PDF del ticket', err)
  }
}
</script>

<style scoped>
.ticket-card {
  position: relative;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.94);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 18px 36px rgba(3, 7, 18, 0.45);
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  color: #f9fafb;
}

.ticket-card__glow {
  position: absolute;
  inset: -40% auto auto -40%;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle, rgba(255,0,87,0.35), transparent 60%);
  pointer-events: none;
}

.ticket-card__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(140deg, rgba(5,7,11,0.95) 0%, rgba(17,24,39,0.55) 100%);
  background-size: cover;
  background-position: center;
  min-height: 130px;
}

@media (max-width: 640px) {
  .ticket-card__header {
    min-height: 100px;
    padding: 12px;
    gap: 12px;
  }
}

.ticket-card__eyebrow {
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #f472b6;
  font-size: 0.75rem;
  margin-bottom: 6px;
}

.ticket-card__event {
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
  line-height: 1.3;
  word-break: break-word;
}

@media (max-width: 640px) {
  .ticket-card__event {
    font-size: 1.1rem;
    line-height: 1.2;
  }
}

.ticket-card__venue,
.ticket-card__location {
  margin: 0;
  color: #d1d5db;
  font-size: 0.95rem;
  line-height: 1.4;
}

@media (max-width: 640px) {
  .ticket-card__venue,
  .ticket-card__location {
    font-size: 0.85rem;
  }
}

.ticket-card__number {
  text-align: right;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: #9ca3af;
}

.ticket-card__number strong {
  display: block;
  font-size: 1.2rem;
  letter-spacing: normal;
  color: #fff;
}

.ticket-card__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-card__info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.ticket-card__section {
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 10px 14px;
}

.ticket-card__section small {
  font-size: 0.8rem;
  color: #cbd5f5;
  word-break: break-word;
  overflow-wrap: anywhere;
  font-family: inherit;
}

.label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: #9ca3af;
}

.value {
  font-size: 1.05rem;
  font-weight: 600;
}

.value--pill {
  background: linear-gradient(135deg, #10b981, #34d399);
  color: #fff;
  padding: 4px 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  width: fit-content;
}

.insurance {
  border-left: 3px solid #10b981;
}

.ticket-card__meta {
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.ticket-card__total {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.ticket-card__total .value {
  font-size: 1.35rem;
  color: #ff6b35;
}

.ticket-card__button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  font-weight: 600;
  cursor: pointer;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #fff;
  transition: transform 0.2s ease, opacity 0.2s ease;
  flex-shrink: 0;
}

.ticket-card__button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ticket-card__button:not(:disabled):hover {
  transform: translateY(-1px);
}

.ticket-modal {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  padding: 24px;
}

.ticket-modal__dialog {
  position: relative;
  width: min(480px, 100%);
  background: #0f172a;
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 0 30px 80px rgba(2, 6, 23, 0.9);
  text-align: center;
}

.ticket-modal__close {
  position: absolute;
  top: 16px;
  right: 16px;
  border: none;
  background: transparent;
  color: #9ca3af;
  font-size: 1.1rem;
  cursor: pointer;
}

.ticket-modal__eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  color: #f472b6;
  font-size: 0.75rem;
  margin-bottom: 8px;
}

.ticket-modal__header h3 {
  margin: 0;
}

.ticket-modal__header p {
  margin: 4px 0 0;
  color: #9ca3af;
}

.ticket-modal__content {
  margin: 24px 0;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 20px;
  padding: 24px;
}

.ticket-modal__content img {
  width: 220px;
  height: 220px;
  object-fit: contain;
}

.ticket-modal__placeholder {
  margin: 0;
  color: #9ca3af;
}

.ticket-modal__actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-modal__primary {
  border: none;
  border-radius: 16px;
  padding: 12px 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  color: #fff;
  cursor: pointer;
}

.ticket-modal__primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.ticket-modal__ghost {
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 12px 16px;
  background: transparent;
  color: #e5e7eb;
  font-weight: 600;
  cursor: pointer;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .ticket-card {
    padding: 12px;
    gap: 10px;
    border-radius: 12px;
  }

  .ticket-card__header {
    flex-direction: column;
    text-align: left;
    min-height: 90px;
    padding: 10px;
    gap: 8px;
    border-radius: 10px;
  }

  .ticket-card__eyebrow {
    font-size: 0.65rem;
    margin-bottom: 4px;
  }

  .ticket-card__number {
    text-align: left;
    font-size: 0.7rem;
  }

  .ticket-card__number strong {
    font-size: 1rem;
  }

  .ticket-card__body {
    gap: 10px;
  }

  .ticket-card__info {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .ticket-card__section {
    padding: 8px 10px;
    border-radius: 10px;
  }

  .label {
    font-size: 0.7rem;
  }

  .value {
    font-size: 0.95rem;
  }

  .ticket-card__section small {
    font-size: 0.75rem;
  }

  .ticket-card__meta {
    flex-direction: column;
    align-items: flex-start;
    width: 100%;
    padding: 10px;
    gap: 10px;
  }

  .ticket-card__total .value {
    font-size: 1.15rem;
  }

  .ticket-card__meta .ticket-card__button {
    width: 100%;
    justify-content: center;
    padding: 10px 14px;
    font-size: 0.9rem;
  }

  .ticket-card__button span {
    font-size: 0.9rem;
  }

  .ticket-modal {
    padding: 16px;
  }
}
</style>
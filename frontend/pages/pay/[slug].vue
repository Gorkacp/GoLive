<template>
  <client-only>
    <Header />
  </client-only>

  <div class="pay-page">
    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="spinner-border" role="status"></div>
      <p class="mt-3">{{ $t('Procesando compra') }}...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <i class="bi bi-exclamation-circle"></i>
      <h3>{{ $t('Error al cargar') }}</h3>
      <p>{{ error }}</p>
      <NuxtLink to="/" class="btn-back">
        <i class="bi bi-arrow-left"></i> {{ $t('Volver') }}
      </NuxtLink>
    </div>

    <!-- Content -->
    <template v-else>
      <!-- Not Logged In -->
      <div v-if="!userLogged" class="not-logged-container">
        <div class="not-logged-card">
          <i class="bi bi-exclamation-triangle"></i>
          <h3>{{ $t('Debes iniciar sesión') }}</h3>
          <p>{{ $t('Necesitas iniciar sesión para completar tu compra') }}</p>
          <button class="btn-login" @click="goToLogin">
            <i class="bi bi-box-arrow-in-right"></i>
            {{ $t('Ir a Login') }}
          </button>
        </div>
      </div>

      <!-- Event Info -->
      <div v-else>
        <!-- Hero Section -->
        <div class="hero-section">
          <img v-if="event.image" :src="event.image" :alt="event.title" class="hero-background" />
          <div class="hero-overlay"></div>
          
          <div class="hero-content">
            <h1 class="hero-title">{{ $t('Completa tu compra') }}</h1>
            <p class="hero-subtitle">{{ event.title }}</p>
            
            <!-- Timer -->
            <div class="timer-container" :class="{ 'timer-warning': timeRemaining < 300 }">
              <i class="bi bi-hourglass-split"></i>
              <div class="timer-content">
                <span class="timer-label">{{ $t('Tiempo restante') }}</span>
                <span class="timer-value">{{ formatTime(timeRemaining) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Main Content -->
        <div class="content-wrapper">
          <!-- Forms Section - FIRST -->
          <div class="forms-section">
            <!-- Section Title -->
            <div class="section-title">
              <h2>{{ $t('Completa tu información') }}</h2>
              <p>{{ $t('Rellena los datos de los asistentes') }}</p>
            </div>

            <!-- Single Ticket Form -->
            <template v-if="totalQuantity === 1">
              <div class="form-card">
                <div class="form-header">
                  <h3>{{ $t('Datos de la entrada') }}</h3>
                </div>
                <form @submit.prevent="payWithPaypal">
                  <div class="form-grid">
                    <!-- Nombre y Apellidos -->
                    <div class="form-group full-width">
                      <label class="form-label">{{ $t('Nombre y apellidos') }} <span class="required">*</span></label>
                      <input
                        type="text"
                        class="form-input"
                        v-model="attendees[0].fullName"
                        :class="{ 'is-invalid': errors[0]?.fullName }"
                        :placeholder="$t('Nombre y apellidos')"
                      />
                      <span v-if="errors[0]?.fullName" class="error-message">{{ errors[0].fullName }}</span>
                    </div>

                    <!-- Email -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('E-mail') }} <span class="required">*</span></label>
                      <input
                        type="email"
                        class="form-input"
                        v-model="attendees[0].email"
                        :class="{ 'is-invalid': errors[0]?.email }"
                        :placeholder="$t('Correo electrónico')"
                      />
                      <span v-if="errors[0]?.email" class="error-message">{{ errors[0].email }}</span>
                    </div>

                    <!-- Fecha de nacimiento -->
                    <div class="form-group full-width">
                      <label class="form-label">{{ $t('Fecha de nacimiento') }} <span class="required">*</span></label>
                      <div class="date-inputs">
                        <select v-model.number="attendees[0].birthDay" class="date-select" :class="{ 'is-invalid': errors[0]?.birthDay }">
                          <option value="">{{ $t('Día') }}</option>
                          <option v-for="day in 31" :key="day" :value="day">{{ String(day).padStart(2, '0') }}</option>
                        </select>
                        <select v-model.number="attendees[0].birthMonth" class="date-select" :class="{ 'is-invalid': errors[0]?.birthMonth }">
                          <option value="">{{ $t('Mes') }}</option>
                          <option v-for="month in 12" :key="month" :value="month">{{ String(month).padStart(2, '0') }}</option>
                        </select>
                        <input
                          type="number"
                          v-model.number="attendees[0].birthYear"
                          class="date-select"
                          :class="{ 'is-invalid': errors[0]?.birthYear }"
                          :placeholder="$t('Año')"
                          min="1900"
                          max="2010"
                        />
                      </div>
                      <span v-if="errors[0]?.birthDay || errors[0]?.birthMonth || errors[0]?.birthYear" class="error-message">{{ $t('Fecha de nacimiento inválida') }}</span>
                    </div>

                    <!-- DNI -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('Documento de identificación') }} <span class="required">*</span></label>
                      <select v-model="attendees[0].idType" class="form-input" :class="{ 'is-invalid': errors[0]?.idType }">
                        <option value="">{{ $t('Tipo de documento') }}</option>
                        <option value="DNI">DNI</option>
                        <option value="NIE">NIE</option>
                        <option value="Pasaporte">{{ $t('Pasaporte') }}</option>
                      </select>
                      <span v-if="errors[0]?.idType" class="error-message">{{ errors[0].idType }}</span>
                    </div>

                    <div class="form-group">
                      <label class="form-label">{{ $t('Número de documento') }} <span class="required">*</span></label>
                      <input
                        type="text"
                        class="form-input"
                        v-model="attendees[0].idNumber"
                        :class="{ 'is-invalid': errors[0]?.idNumber }"
                        :placeholder="$t('12345678Z')"
                      />
                      <span v-if="errors[0]?.idNumber" class="error-message">{{ errors[0].idNumber }}</span>
                    </div>

                    <!-- Código Postal -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('Código postal') }} <span class="required">*</span></label>
                      <input
                        type="text"
                        class="form-input"
                        v-model="attendees[0].postalCode"
                        :class="{ 'is-invalid': errors[0]?.postalCode }"
                        :placeholder="$t('Código postal')"
                      />
                      <span v-if="errors[0]?.postalCode" class="error-message">{{ errors[0].postalCode }}</span>
                    </div>

                    <!-- País -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('País') }} <span class="required">*</span></label>
                      <select v-model="attendees[0].country" class="form-input" :class="{ 'is-invalid': errors[0]?.country }">
                        <option value="">{{ $t('Selecciona un país') }}</option>
                        <option value="ES">España</option>
                        <option value="PT">Portugal</option>
                        <option value="FR">Francia</option>
                        <option value="IT">Italia</option>
                        <option value="DE">Alemania</option>
                        <option value="UK">Reino Unido</option>
                        <option value="Other">{{ $t('Otro') }}</option>
                      </select>
                      <span v-if="errors[0]?.country" class="error-message">{{ errors[0].country }}</span>
                    </div>
                  </div>
                </form>
              </div>
            </template>

            <!-- Multiple Tickets Forms -->
            <template v-else>
              <form @submit.prevent="payWithPaypal">
                <div v-for="(attendee, index) in attendees" :key="index" class="form-card">
                  <div class="form-header attendee-header">
                    <h3>{{ $t('Asistente') }} {{ index + 1 }}</h3>
                    <span class="ticket-type">{{ getTicketType(index) }}</span>
                  </div>

                  <div class="form-grid">
                    <!-- Nombre y Apellidos -->
                    <div class="form-group full-width">
                      <label class="form-label">{{ $t('Nombre y apellidos') }} <span class="required">*</span></label>
                      <input
                        type="text"
                        class="form-input"
                        v-model="attendees[index].fullName"
                        :class="{ 'is-invalid': errors[index]?.fullName }"
                        :placeholder="$t('Nombre y apellidos')"
                      />
                      <span v-if="errors[index]?.fullName" class="error-message">{{ errors[index].fullName }}</span>
                    </div>

                    <!-- Email -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('E-mail') }} <span class="required">*</span></label>
                      <input
                        type="email"
                        class="form-input"
                        v-model="attendees[index].email"
                        :class="{ 'is-invalid': errors[index]?.email }"
                        :placeholder="$t('Correo electrónico')"
                      />
                      <span v-if="errors[index]?.email" class="error-message">{{ errors[index].email }}</span>
                    </div>

                    <!-- Fecha de nacimiento -->
                    <div class="form-group full-width">
                      <label class="form-label">{{ $t('Fecha de nacimiento') }} <span class="required">*</span></label>
                      <div class="date-inputs">
                        <select v-model.number="attendees[index].birthDay" class="date-select" :class="{ 'is-invalid': errors[index]?.birthDay }">
                          <option value="">{{ $t('Día') }}</option>
                          <option v-for="day in 31" :key="day" :value="day">{{ String(day).padStart(2, '0') }}</option>
                        </select>
                        <select v-model.number="attendees[index].birthMonth" class="date-select" :class="{ 'is-invalid': errors[index]?.birthMonth }">
                          <option value="">{{ $t('Mes') }}</option>
                          <option v-for="month in 12" :key="month" :value="month">{{ String(month).padStart(2, '0') }}</option>
                        </select>
                        <input
                          type="number"
                          v-model.number="attendees[index].birthYear"
                          class="date-select"
                          :class="{ 'is-invalid': errors[index]?.birthYear }"
                          :placeholder="$t('Año')"
                          min="1900"
                          max="2010"
                        />
                      </div>
                      <span v-if="errors[index]?.birthDay || errors[index]?.birthMonth || errors[index]?.birthYear" class="error-message">{{ $t('Fecha de nacimiento inválida') }}</span>
                    </div>

                    <!-- DNI -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('Documento de identificación') }} <span class="required">*</span></label>
                      <select v-model="attendees[index].idType" class="form-input" :class="{ 'is-invalid': errors[index]?.idType }">
                        <option value="">{{ $t('Tipo de documento') }}</option>
                        <option value="DNI">DNI</option>
                        <option value="NIE">NIE</option>
                        <option value="Pasaporte">{{ $t('Pasaporte') }}</option>
                      </select>
                      <span v-if="errors[index]?.idType" class="error-message">{{ errors[index].idType }}</span>
                    </div>

                    <div class="form-group">
                      <label class="form-label">{{ $t('Número de documento') }} <span class="required">*</span></label>
                      <input
                        type="text"
                        class="form-input"
                        v-model="attendees[index].idNumber"
                        :class="{ 'is-invalid': errors[index]?.idNumber }"
                        :placeholder="$t('12345678Z')"
                      />
                      <span v-if="errors[index]?.idNumber" class="error-message">{{ errors[index].idNumber }}</span>
                    </div>

                    <!-- Código Postal -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('Código postal') }} <span class="required">*</span></label>
                      <input
                        type="text"
                        class="form-input"
                        v-model="attendees[index].postalCode"
                        :class="{ 'is-invalid': errors[index]?.postalCode }"
                        :placeholder="$t('Código postal')"
                      />
                      <span v-if="errors[index]?.postalCode" class="error-message">{{ errors[index].postalCode }}</span>
                    </div>

                    <!-- País -->
                    <div class="form-group">
                      <label class="form-label">{{ $t('País') }} <span class="required">*</span></label>
                      <select v-model="attendees[index].country" class="form-input" :class="{ 'is-invalid': errors[index]?.country }">
                        <option value="">{{ $t('Selecciona un país') }}</option>
                        <option value="ES">España</option>
                        <option value="PT">Portugal</option>
                        <option value="FR">Francia</option>
                        <option value="IT">Italia</option>
                        <option value="DE">Alemania</option>
                        <option value="UK">Reino Unido</option>
                        <option value="Other">{{ $t('Otro') }}</option>
                      </select>
                      <span v-if="errors[index]?.country" class="error-message">{{ errors[index].country }}</span>
                    </div>
                  </div>
                </div>
              </form>
            </template>
          </div>

          <!-- Event Summary Card - SECOND (After Forms) -->
          <div class="event-summary-card">
            <!-- Section Title -->
            <div class="section-title section-title-summary">
              <h2>{{ $t('Resumen de tu compra') }}</h2>
            </div>

            <div class="summary-header">
              <div class="event-info">
                <img :src="event.image" :alt="event.title" class="event-thumb" />
                <div class="event-details">
                  <h3>{{ event.title }}</h3>
                  <p>{{ event.venue }}</p>
                </div>
              </div>
              <div class="summary-badge">{{ tickets.length }} {{ tickets.length === 1 ? $t('entrada') : $t('entradas') }}</div>
            </div>

            <!-- Tickets Breakdown -->
            <div class="tickets-breakdown">
              <div v-for="(ticket, index) in tickets" :key="index" class="ticket-item">
                <span class="ticket-name">{{ ticket.name }} x{{ ticket.quantity }}</span>
                <span class="ticket-price">{{ (ticket.price * ticket.quantity).toFixed(2) }} €</span>
              </div>
            </div>

            <!-- Price Summary -->
            <div class="price-summary">
              <div class="summary-row">
                <span>{{ $t('Subtotal') }}</span>
                <span>{{ subtotal.toFixed(2) }} €</span>
              </div>
              <div class="summary-row">
                <span>{{ $t('Comisión') }} ({{ SERVICE_FEE_PER_TICKET.toFixed(2) }} € / {{ $t('entrada') }})</span>
                <span>{{ commission.toFixed(2) }} €</span>
              </div>
              <div class="summary-row" v-if="insurance > 0">
                <span>{{ $t('Seguro de evento') }}</span>
                <span>{{ insurance.toFixed(2) }} €</span>
              </div>
              <div class="summary-row" v-if="fees > 0">
                <span>{{ $t('Gastos adicionales') }}</span>
                <span>{{ fees.toFixed(2) }} €</span>
              </div>
              <div class="summary-total">
                <span>{{ $t('Total') }}</span>
                <span class="total-amount">{{ total.toFixed(2) }} €</span>
              </div>
            </div>
          </div>

          <!-- Payment Methods - THIRD (Centered at bottom) -->
          <div class="payment-section">
            <!-- Section Title -->
            <div class="section-title-payment">
              <h2>{{ $t('Selecciona tu método de pago') }}</h2>
            </div>

            <div class="payment-methods-container">
            <p v-if="processingPayment" class="payment-status">
              {{ $t('Procesando tu pago, no cierres esta ventana...') }}
            </p>
              <div id="paypal-button-container" class="paypal-container"></div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>

  <Footer />
</template>

<script setup>
import { ref, reactive, watch, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute, useRouter, useHead, useRuntimeConfig } from '#app'
import { useCartStore } from '~/stores/cart'
import { useAuth } from '~/composables/useAuth'
import { SERVICE_FEE_PER_TICKET } from '~/composables/useCarrito'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
const { getCurrentUser, getToken } = useAuth()
const runtimeConfig = useRuntimeConfig()
const normalizeApiBase = (base) => {
  const trimmed = (base || '').replace(/\/+$/, '')
  return trimmed.endsWith('/api') ? trimmed : `${trimmed}/api`
}
const apiBase = normalizeApiBase(runtimeConfig.public.apiBase || 'http://localhost:8085')
const paypalClientId = runtimeConfig.public.paypalClientId ||
  'ASR1-nfvyjoSisSGbs8LK3xTAXdwQm48UXi0esdNNH7EWQFXdIgHeGkjV1BfdCDO5kdV53F9u98jzq31'

const currentUser = ref(null)
const userLogged = computed(() => !!currentUser.value)
const loading = ref(false)
const error = ref('')
const processingPayment = ref(false)
const insurance = ref(0)
const fees = ref(0)
const timeRemaining = ref(15 * 60) // 15 minutos en segundos

const goToLogin = () => router.push('/login')

// Genera slug igual que en el carrito
const generateSlug = (title) =>
  title.toLowerCase()
       .normalize('NFD')
       .replace(/[\u0300-\u036f]/g, '')
       .replace(/[^a-z0-9]+/g, '-')
       .replace(/(^-|-$)/g, '')

// Datos del evento y tickets
const event = ref({ title: 'Evento no encontrado', venue: '', image: '', description: '' })
const tickets = ref([])

// Attendees data (un formulario por cada entrada)
const attendees = ref([])

// Errores de validación (array de errores por cada asistente)
const errors = ref([])

// Crear estructura de asistentes basado en tickets
const initializeAttendees = () => {
  attendees.value = []
  errors.value = []
  
  tickets.value.forEach((ticket) => {
    for (let i = 0; i < ticket.quantity; i++) {
      attendees.value.push({
        fullName: '',
        email: '',
        birthDay: '',
        birthMonth: '',
        birthYear: '',
        idType: '',
        idNumber: '',
        postalCode: '',
        country: '',
        ticketType: ticket.name,
        insurance: !!ticket.insurance
      })
      errors.value.push({})
    }
  })
}

// Prefill: rellenar SOLO el primer asistente con los datos del usuario logueado
const prefillFirstAttendeeFromUser = () => {
  if (!currentUser.value || !attendees.value.length) return

  const user = currentUser.value
  const first = attendees.value[0]

  // Solo rellenar campos vacíos para no pisar nada que el usuario ya haya escrito
  if (!first.fullName) {
    const fullName = [user.name, user.lastName].filter(Boolean).join(' ').trim()
    first.fullName = fullName || ''
  }

  if (!first.email && user.email) {
    first.email = user.email
  }

  if (!first.postalCode && user.postalCode) {
    first.postalCode = user.postalCode
  }

  // Intentar mapear fecha de nacimiento si viene como Date/string ISO
  if (!first.birthDay && !first.birthMonth && !first.birthYear && user.dateOfBirth) {
    try {
      const d = new Date(user.dateOfBirth)
      if (!isNaN(d.getTime())) {
        first.birthDay = d.getDate()
        first.birthMonth = d.getMonth() + 1
        first.birthYear = d.getFullYear()
      }
    } catch (e) {
      // Si falla el parseo, simplemente no rellenamos la fecha
    }
  }
}

// Calcular totales
const subtotal = computed(() => {
  return tickets.value.reduce((sum, t) => sum + t.price * t.quantity, 0)
})

const commission = computed(() => {
  return attendees.value.length * SERVICE_FEE_PER_TICKET
})

const total = computed(() => {
  return subtotal.value + commission.value + insurance.value + fees.value
})

const totalQuantity = computed(() => {
  return attendees.value.length
})

// Obtener tipo de entrada para mostrar
const getTicketType = (index) => {
  let count = 0
  for (let i = 0; i < tickets.value.length; i++) {
    if (count + tickets.value[i].quantity > index) {
      return tickets.value[i].name
    }
    count += tickets.value[i].quantity
  }
  return ''
}

// Validar un asistente
const validateAttendee = (index) => {
  const attendee = attendees.value[index]
  const attendeeErrors = {
    fullName: '',
    email: '',
    birthDay: '',
    birthMonth: '',
    birthYear: '',
    idType: '',
    idNumber: '',
    postalCode: '',
    country: ''
  }

  // Validar nombre
  if (!attendee.fullName?.trim()) {
    attendeeErrors.fullName = 'El nombre y apellidos son obligatorios.'
  } else if (attendee.fullName.trim().length < 3) {
    attendeeErrors.fullName = 'El nombre debe tener al menos 3 caracteres.'
  }

  // Validar email
  if (!attendee.email?.trim()) {
    attendeeErrors.email = 'El correo es obligatorio.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(attendee.email.trim())) {
    attendeeErrors.email = 'El correo debe ser válido (ej: usuario@correo.com).'
  }

  // Validar fecha de nacimiento
  if (!attendee.birthDay || !attendee.birthMonth || !attendee.birthYear) {
    attendeeErrors.birthDay = 'La fecha de nacimiento es obligatoria.'
  } else {
    const birthDate = new Date(attendee.birthYear, attendee.birthMonth - 1, attendee.birthDay)
    const today = new Date()
    const age = today.getFullYear() - birthDate.getFullYear()
    
    if (age < 13) {
      attendeeErrors.birthDay = 'Debes tener al menos 13 años para comprar entradas.'
    } else if (isNaN(birthDate.getTime())) {
      attendeeErrors.birthDay = 'La fecha de nacimiento no es válida.'
    }
  }

  // Validar tipo de documento
  if (!attendee.idType?.trim()) {
    attendeeErrors.idType = 'El tipo de documento es obligatorio.'
  }

  // Validar número de documento
  if (!attendee.idNumber?.trim()) {
    attendeeErrors.idNumber = 'El número de documento es obligatorio.'
  } else if (attendee.idNumber.trim().length < 3) {
    attendeeErrors.idNumber = 'El número de documento no es válido.'
  }

  // Validar código postal
  if (!attendee.postalCode?.trim()) {
    attendeeErrors.postalCode = 'El código postal es obligatorio.'
  } else if (!/^\d{5}$/.test(attendee.postalCode.replace(/\s/g, ''))) {
    attendeeErrors.postalCode = 'El código postal debe tener 5 dígitos.'
  }

  // Validar país
  if (!attendee.country) {
    attendeeErrors.country = 'El país es obligatorio.'
  }

  errors.value[index] = attendeeErrors
  return Object.values(attendeeErrors).every(e => !e)
}

// Validar todos los asistentes
const validateAllAttendees = () => {
  let allValid = true
  for (let i = 0; i < attendees.value.length; i++) {
    if (!validateAttendee(i)) {
      allValid = false
    }
  }
  return allValid
}

// Formatear tiempo en MM:SS
const formatTime = (seconds) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
}

// Función de pago
const payWithPaypal = () => {
  if (!userLogged.value) {
    goToLogin()
    return false
  }

  if (processingPayment.value) {
    return false
  }

  // Validar que todos los formularios estén completos
  if (!validateAllAttendees()) {
    // Scroll al primer error
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
    // Mostrar alerta al usuario
    return false
  }
  // Si todo está válido, proceder al pago
  return true
}

// Procesar errores del backend y mostrarlos en los campos correspondientes
const processBackendErrors = (errorResponse) => {
  // Limpiar errores previos
  errors.value = attendees.value.map(() => ({}))
  
  // Si hay errores de validación estructurados
  if (errorResponse?.errors && typeof errorResponse.errors === 'object') {
    // Los errores pueden venir como un objeto con arrays de mensajes por campo
    // o como un mensaje simple
    const errorFields = Object.keys(errorResponse.errors)
    
    // Intentar mapear errores a campos de asistentes
    errorFields.forEach(field => {
      const errorMessages = Array.isArray(errorResponse.errors[field]) 
        ? errorResponse.errors[field] 
        : [errorResponse.errors[field]]
      
      // Buscar el campo en todos los asistentes
      attendees.value.forEach((attendee, index) => {
        const fieldMap = {
          'fullName': 'fullName',
          'email': 'email',
          'birthDay': 'birthDay',
          'birthMonth': 'birthMonth',
          'birthYear': 'birthYear',
          'idType': 'idType',
          'idNumber': 'idNumber',
          'postalCode': 'postalCode',
          'country': 'country'
        }
        
        const mappedField = fieldMap[field]
        if (mappedField && !errors.value[index][mappedField]) {
          errors.value[index][mappedField] = errorMessages[0]
        }
      })
    })
  }
  
  // Si hay un mensaje de error general, mostrarlo también
  if (errorResponse?.error || errorResponse?.message) {
    const generalError = errorResponse.error || errorResponse.message
    // Si el error menciona un campo específico, intentar asignarlo
    const fieldPatterns = {
      'nombre': 'fullName',
      'email': 'email',
      'fecha': 'birthDay',
      'documento': 'idNumber',
      'código postal': 'postalCode',
      'postal': 'postalCode',
      'país': 'country',
      'country': 'country'
    }
    
    let errorAssigned = false
    for (const [pattern, field] of Object.entries(fieldPatterns)) {
      if (generalError.toLowerCase().includes(pattern.toLowerCase())) {
        // Asignar el error al primer asistente con ese campo vacío o incorrecto
        for (let i = 0; i < attendees.value.length; i++) {
          if (!errors.value[i][field]) {
            errors.value[i][field] = generalError
            errorAssigned = true
            break
          }
        }
        if (errorAssigned) break
      }
    }
    
    // Si no se pudo asignar a un campo específico, mostrar como error general
    if (!errorAssigned) {
      error.value = generalError
    }
  }
}

const persistPayment = async (details) => {
  const capture = details?.purchase_units?.[0]?.payments?.captures?.[0]
  
  const payload = {
    providerOrderId: details?.id,
    providerCaptureId: capture?.id,
    providerPayerId: details?.payer?.payer_id,
    status: details?.status,
    currency: capture?.amount?.currency_code || 'EUR',
    eventId: event.value._id || event.value.id || event.value.eventId,
    eventTitle: event.value.title,
    eventVenue: event.value.venue,
    eventLocation: event.value.location,
    eventImage: event.value.image,
    subtotal: subtotal.value,
    commission: commission.value,
    insurance: insurance.value,
    fees: fees.value,
    total: total.value,
    userId: currentUser.value?._id || currentUser.value?.id,
    userEmail: currentUser.value?.email,
    payerEmail: details?.payer?.email_address,
    tickets: tickets.value.map(ticket => ({
      name: ticket.name,
      price: ticket.price,
      quantity: ticket.quantity,
      insurance: !!ticket.insurance
    })),
    attendees: attendees.value.map(att => ({
      fullName: att.fullName,
      email: att.email,
      birthDay: att.birthDay,
      birthMonth: att.birthMonth,
      birthYear: att.birthYear,
      idType: att.idType,
      idNumber: att.idNumber,
      postalCode: att.postalCode,
      country: att.country,
      ticketType: att.ticketType,
      insurance: !!att.insurance
    }))
  }

  processingPayment.value = true
  try {
    const token = getToken()
    if (!token) {
      throw new Error('No se encontró token de autenticación')
    }

    const requestConfig = {
      method: 'POST',
      body: payload,
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    }

    await $fetch(`${apiBase}/payments/paypal/capture`, requestConfig)
    cartStore.clearOrder()
    if (process.client) {
      localStorage.removeItem('currentOrder')
      localStorage.removeItem('orderAttendees')
      localStorage.removeItem('lastOrderDetails')
    }
    router.push('/misEntradas?purchase=success')
  } catch (err) {
    // Procesar errores del backend
    const errorData = err?.response?._data || err?.response?.data || {}
    processBackendErrors(errorData)
    
    // Si hay un error general que no se pudo asignar a un campo
    if (!error.value && (errorData?.error || errorData?.message || err?.message)) {
      error.value = errorData.error || errorData.message || err.message || 'No se pudo registrar el pago. Intenta nuevamente.'
    }
    
    // Scroll al primer error
    const firstErrorIndex = errors.value.findIndex(e => Object.values(e).some(err => err))
    if (firstErrorIndex !== -1) {
      await nextTick()
      const formCard = document.querySelectorAll('.form-card')[firstErrorIndex]
      if (formCard) {
        formCard.scrollIntoView({ behavior: 'smooth', block: 'center' })
      }
    } else if (error.value) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
    
    throw err
  } finally {
    processingPayment.value = false
  }
}

// Cargar datos del carrito y usuario
const fetchCurrentUser = async () => {
  try {
    const user = await getCurrentUser()
    currentUser.value = user
  } catch (err) {
    currentUser.value = null
  }
}

const loadOrder = async () => {
  try {
    let order = cartStore.order

    if (!order || !order.event || generateSlug(order.event.title) !== route.params.slug) {
      const saved = process.client ? localStorage.getItem('currentOrder') : null
      if (saved) {
        try {
          const savedOrder = JSON.parse(saved)
          if (savedOrder?.event && generateSlug(savedOrder.event.title) === route.params.slug) {
            order = savedOrder
          }
        } catch (e) {
          order = null
        }
      }
    }

    if (order?.event && order?.tickets?.length) {
      event.value = { ...event.value, ...order.event }
      tickets.value = order.tickets.map(ticket => ({
        ...ticket,
        quantity: Number(ticket.quantity) || 0,
        price: Number(ticket.price) || 0
      }))
      insurance.value = Number(order.insurance) || 0
      fees.value = Number(order.fees) || 0

      initializeAttendees()

      // Si el usuario está logueado, pre-rellenar solo el primer asistente
      prefillFirstAttendeeFromUser()

      // SEO específico de la página de pago para este evento
      const slug = route.params.slug
      useHead({
        title: `Pago - ${event.value.title} | GoLive`,
        meta: [
          {
            name: 'description',
            content: `Completa el pago de tus entradas para ${event.value.title} en ${event.value.venue} de forma segura con GoLive.`
          },
          {
            name: 'keywords',
            content: `pago entradas, checkout, ${event.value.title}, entradas ${event.value.venue}, GoLive`
          },
          {
            property: 'og:title',
            content: `Pago - ${event.value.title} | GoLive`
          },
          {
            property: 'og:description',
            content: `Finaliza la compra de tus entradas para ${event.value.title}. Pago seguro y entradas digitales.`
          },
          {
            property: 'og:image',
            content: event.value.image || '/default-event.jpg'
          },
          {
            property: 'og:type',
            content: 'website'
          },
          {
            property: 'og:url',
            content: `https://golive.com/pay/${slug}`
          },
          {
            name: 'twitter:card',
            content: 'summary_large_image'
          },
          {
            name: 'twitter:title',
            content: `Pago - ${event.value.title} | GoLive`
          },
          {
            name: 'twitter:description',
            content: `Completa tu compra para ${event.value.title} con GoLive.`
          },
          {
            name: 'twitter:image',
            content: event.value.image || '/default-event.jpg'
          }
        ],
        link: [
          {
            rel: 'canonical',
            href: `https://golive.com/pay/${slug}`
          }
        ]
      })
    } else {
      error.value = 'No se encontraron entradas para comprar. Por favor, vuelve atrás.'
    }
  } catch (err) {
    error.value = 'Error al cargar la información. Por favor, intenta de nuevo.'
  }
}

let timerInterval

const startTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
  timerInterval = setInterval(() => {
    timeRemaining.value--
    if (timeRemaining.value <= 0) {
      clearInterval(timerInterval)
      router.push('/')
    }
  }, 1000)
}

onMounted(async () => {
  loading.value = true
  await fetchCurrentUser()
  await loadOrder()
  loading.value = false

  await nextTick()
  if (process.client && userLogged.value && attendees.value.length) {
    loadPaypal()
  }
  startTimer()
})

onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
})

let paypalScriptLoading = false

watch(
  () => [userLogged.value, attendees.value.length],
  ([logged, attendeeCount]) => {
    if (process.client && logged && attendeeCount) {
      loadPaypal()
    }
  }
)

// Carga PayPal
const loadPaypal = () => {
  if (!process.client) return
  if (!userLogged.value || !attendees.value.length) return
  if (window.paypal) {
    renderPaypalButton()
    return
  }

  if (paypalScriptLoading) return
  paypalScriptLoading = true

  const script = document.createElement('script')
  script.src = `https://www.paypal.com/sdk/js?client-id=${paypalClientId}&currency=EUR`
  script.addEventListener('load', () => {
    paypalScriptLoading = false
    renderPaypalButton()
  })
  document.body.appendChild(script)
}

// Renderiza el botón PayPal
const renderPaypalButton = () => {
  if (!window.paypal || !attendees.value.length) return
  const container = document.getElementById('paypal-button-container')
  if (!container) return
  container.innerHTML = ''

  window.paypal.Buttons({
    style: { layout: 'vertical', color: 'gold', shape: 'rect', label: 'paypal', height: 50 },
    createOrder: (data, actions) => {
      // Validar antes de crear la orden
      if (processingPayment.value) {
        return actions.reject()
      }
      const isValid = validateAllAttendees()
      
      if (!isValid) {
        // Encontrar el primer error para enfocarse en él
        const firstErrorIndex = errors.value.findIndex(e => Object.values(e).some(err => err))
        if (firstErrorIndex !== -1) {
          const formCard = document.querySelectorAll('.form-card')[firstErrorIndex]
          if (formCard) {
            formCard.scrollIntoView({ behavior: 'smooth', block: 'center' })
          }
        } else {
          // Scroll al formulario si no hay índice específico
          const formsSection = document.querySelector('.forms-section')
          if (formsSection) {
            formsSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
          }
        }
        return actions.reject()
      }
      
      // Crear la orden si la validación fue exitosa
      return actions.order.create({
        purchase_units: [
          {
            amount: { value: total.value.toFixed(2) },
            description: `${event.value.title || 'Evento'} - ${totalQuantity.value} ${totalQuantity.value === 1 ? 'entrada' : 'entradas'}`
          }
        ]
      }).catch((err) => {
        // Solo mostrar error genérico si no hay errores de validación visibles
        const hasValidationErrors = errors.value.some(e => Object.values(e).some(err => err))
        if (!hasValidationErrors) {
          error.value = 'No se pudo iniciar el pago. Intenta nuevamente.'
        }
        return actions.reject()
      })
    },
    onApprove: (data, actions) => {
      // Validar una última vez antes de confirmar la captura
      if (!validateAllAttendees()) {
        window.scrollTo({ top: 0, behavior: 'smooth' })
        return
      }

      return actions.order.capture().then(async details => {
        if (details.status === 'COMPLETED') {
          try {
            await persistPayment(details)
          } catch (err) {
            // Los errores ya se procesan en persistPayment, solo hacer scroll si es necesario
            // El scroll ya se maneja en persistPayment
          }
        }
      }).catch((err) => {
        // Solo mostrar error genérico si no hay errores de validación visibles
        const hasValidationErrors = errors.value.some(e => Object.values(e).some(err => err))
        if (!hasValidationErrors) {
          error.value = 'No se pudo completar el pago. Intenta nuevamente.'
          window.scrollTo({ top: 0, behavior: 'smooth' })
        }
      })
    },
    onError: (err) => {
      // Si hay errores de validación, no mostrar mensaje genérico
      const hasValidationErrors = errors.value.some(e => Object.values(e).some(err => err))
      if (!hasValidationErrors) {
        error.value = 'Se produjo un error al inicializar el pago. Intenta nuevamente.'
        window.scrollTo({
          top: 0,
          behavior: 'smooth'
        })
      } else {
        // Scroll al primer error de validación
        const firstErrorIndex = errors.value.findIndex(e => Object.values(e).some(err => err))
        if (firstErrorIndex !== -1) {
          nextTick(() => {
            const formCard = document.querySelectorAll('.form-card')[firstErrorIndex]
            if (formCard) {
              formCard.scrollIntoView({ behavior: 'smooth', block: 'center' })
            }
          })
        }
      }
    },
    onCancel: () => {}
  }).render('#paypal-button-container')

}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.pay-page {
  width: 100%;
  min-height: 100vh;
  background: #0a0a0a;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
  padding-top: 80px;
}

/* Solo en móviles: eliminar espacio superior */
@media (max-width: 768px) {
  .pay-page {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }
}

/* ============ Loading State ============ */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #0a0a0a;
  z-index: 9999;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 100vh;
  pointer-events: all;
}

.loading-container .spinner-border {
  display: inline-block;
  width: 50px;
  height: 50px;
  border: 4px solid #ff0057;
  border-top-color: transparent;
  border-right-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.loading-container p {
  margin-top: 20px;
  font-family: 'Poppins', sans-serif;
  color: #ffffff;
  font-weight: 500;
  font-size: 1rem;
}

/* ============ Error State ============ */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  text-align: center;
  padding: 40px 20px;
  background: #0a0a0a;
}

.error-container i {
  font-size: 3.5rem;
  color: #dc2626;
  margin-bottom: 15px;
}

.error-container h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #f8f9fa;
  margin-bottom: 10px;
}

.error-container p {
  font-family: 'Poppins', sans-serif;
  color: #d1d5db;
  margin-bottom: 25px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s ease;
}

.btn-back:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.3);
}

/* ============ Not Logged Container ============ */
.not-logged-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
}

.not-logged-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.not-logged-card i {
  font-size: 3rem;
  color: #ff0057;
  margin-bottom: 20px;
  display: block;
}

.not-logged-card h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 10px 0;
}

.not-logged-card p {
  font-family: 'Poppins', sans-serif;
  color: #6b7280;
  margin: 0 0 25px 0;
  line-height: 1.5;
}

.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.3);
}

/* ============ Hero Section ============ */
.hero-section {
  position: relative;
  height: 350px;
  overflow: hidden;
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.hero-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(10, 10, 10, 0.65);
  z-index: 1;
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.hero-title {
  font-family: 'Poppins', sans-serif;
  font-size: 2.6rem;
  font-weight: 700;
  margin: 0;
  text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  margin: 0;
  opacity: 0.9;
}

/* ============ Timer ============ */
.timer-container {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.15);
  padding: 10px 20px;
  border-radius: 20px;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.timer-container i {
  font-size: 1.3rem;
  animation: timer-pulse 1s ease-in-out infinite;
}

@keyframes timer-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.timer-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.timer-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.timer-value {
  font-family: 'Poppins', sans-serif;
  font-size: 1.2rem;
  font-weight: 700;
  letter-spacing: 1px;
}

.timer-container.timer-warning {
  background: rgba(255, 0, 87, 0.3);
  border-color: rgba(255, 0, 87, 0.6);
  animation: timer-warning-glow 1s ease-in-out infinite;
}

@keyframes timer-warning-glow {
  0%, 100% { box-shadow: 0 0 0 0 rgba(255, 0, 87, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(255, 0, 87, 0.1); }
}

/* ============ Section Titles ============ */
.section-title {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.section-title h2 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.6rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 8px 0;
}

.section-title p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  color: #6b7280;
  margin: 0;
}

.section-title-summary {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e5e7eb;
}

.section-title-summary h2 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.6rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.section-title-payment {
  margin-bottom: 32px;
  padding-bottom: 0;
  border-bottom: none;
  text-align: center;
}

.section-title-payment h2 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.payment-methods-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  width: 100%;
}

.payment-status {
  color: #ff0057;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  margin-bottom: 4px;
  text-align: center;
}

/* ============ Content Wrapper ============ */
.content-wrapper {
  max-width: 100%;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0;
  min-height: auto;
  width: 100%;
}

/* ============ Event Summary Card ============ */
.event-summary-card {
  background: linear-gradient(135deg, #ffffff 0%, #f9fafb 100%);
  border-top: 3px solid #ff0057;
  border-bottom: 3px solid #ff0057;
  padding: 32px 48px;
  box-shadow: none;
  border-radius: 0;
  height: auto;
  position: static;
  overflow-y: visible;
  border: none;
  display: flex;
  flex-direction: column;
  border-right: none;
  border-left: none;
  max-width: 100%;
  margin: 0;
  order: 2;
  width: 100%;
}

.event-summary-card::-webkit-scrollbar {
  width: 6px;
}

.event-summary-card::-webkit-scrollbar-track {
  background: transparent;
}

.event-summary-card::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}

.event-summary-card::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 2px solid #e5e7eb;
}

.event-info {
  flex: 1;
  display: flex;
  gap: 16px;
  min-width: 0;
}

.event-thumb {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
  border: 2px solid #ff0057;
}

.event-details {
  flex: 1;
  min-width: 0;
}

.event-details h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0 0 4px 0;
  white-space: normal;
  word-break: break-word;
  line-height: 1.3;
}

.event-details p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  color: #6b7280;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.summary-badge {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  padding: 8px 14px;
  border-radius: 6px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.tickets-breakdown {
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ticket-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f3f4f6;
  border-radius: 8px;
  border-left: 3px solid #ff0057;
}

.ticket-name {
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  font-weight: 600;
  color: #0a0a0a;
}

.ticket-price {
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  font-weight: 700;
  color: #ff0057;
}

.price-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: auto;
  padding-top: 24px;
  border-top: 2px solid #e5e7eb;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  color: #6b7280;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  padding: 14px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  color: #ffffff;
  margin-top: 10px;
}

.total-amount {
  font-size: 1.2rem;
}

/* ============ Forms Section ============ */
.forms-section {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 40px 48px;
  overflow-y: visible;
  background: #ffffff;
  order: 1;
  width: 100%;
}

.forms-section::-webkit-scrollbar {
  width: 6px;
}

.forms-section::-webkit-scrollbar-track {
  background: transparent;
}

.forms-section::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}

.forms-section::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

.form-card {
  background: transparent;
  border-radius: 0;
  padding: 0 0 32px 0;
  box-shadow: none;
  border: none;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e5e7eb;
  transition: none;
}

.form-card:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.form-card:hover {
  border-color: #e5e7eb;
  box-shadow: none;
}

.form-header {
  margin-bottom: 28px;
  padding-bottom: 0;
  border-bottom: none;
}

.form-header h3 {
  font-family: 'Poppins', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #0a0a0a;
  margin: 0;
}

.attendee-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.attendee-header h3 {
  font-size: 1.1rem;
}

.ticket-type {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  padding: 4px 12px;
  border-radius: 20px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.form-grid .full-width {
  grid-column: 1 / -1;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 600;
  color: #0a0a0a;
  letter-spacing: 0.3px;
}

.required {
  color: #dc2626;
  font-weight: 700;
}

.form-input,
.date-select {
  padding: 14px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  color: #0a0a0a;
  background: #ffffff;
  transition: all 0.3s ease;
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-input:focus,
.date-select:focus {
  outline: none;
  border-color: #ff0057;
  background: #fff5f8;
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1);
}

.form-input.is-invalid,
.date-select.is-invalid {
  border-color: #dc2626;
  background: #fee2e2;
}

.date-inputs {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 12px;
}

.date-select {
  width: 100%;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='%23ff0057' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  padding-right: 36px;
  cursor: pointer;
  font-weight: 500;
  max-height: 200px;
  overflow-y: scroll;
}

.date-select::-ms-expand {
  display: none;
}

/* Mejorar apariencia en navegadores webkit (iOS/Safari) */
.date-select::-webkit-scrollbar {
  width: 8px;
}

.date-select::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.date-select::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
}

.date-select::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* Color para opciones del select en móvil */
.date-select option {
  color: #0a0a0a;
  background: #ffffff;
  padding: 8px;
  margin: 0;
  border: none;
}

.date-select option:checked {
  background: #0a0a0a;
  color: #ffffff;
  font-weight: 600;
}

.date-select option:hover {
  background: #f3f4f6;
  color: #0a0a0a;
}

.error-message {
  font-family: 'Poppins', sans-serif;
  font-size: 0.8rem;
  color: #dc2626;
  font-weight: 500;
  display: block;
  margin-top: 4px;
}

/* ============ Payment Section ============ */
.payment-section {
  margin: 0;
  padding: 40px 48px;
  border-top: none;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 0;
  order: 3;
  width: 100%;
}

.paypal-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  max-width: 500px;
}

/* ============ Responsive ============ */
@media (max-width: 1440px) {
  .forms-section {
    padding: 32px 40px;
  }

  .form-grid {
    gap: 20px;
  }

  .event-summary-card {
    padding: 32px 40px;
  }
}

@media (max-width: 1200px) {
  .forms-section {
    padding: 32px 32px;
  }

  .event-summary-card {
    position: static;
    top: auto;
    border-left: none;
    border-top: 3px solid #ff0057;
    border-radius: 0;
    padding: 32px;
  }

  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    height: 200px;
  }

  .hero-title {
    font-size: 1.6rem;
  }

  .hero-subtitle {
    font-size: 0.9rem;
  }

  .timer-container {
    padding: 8px 16px;
  }

  .timer-value {
    font-size: 1rem;
  }

  .timer-label {
    font-size: 0.65rem;
  }

  .forms-section {
    padding: 24px 20px;
  }

  .form-card {
    padding-bottom: 24px;
    margin-bottom: 24px;
  }

  .event-summary-card {
    padding: 24px 20px;
  }

  .payment-section {
    padding: 24px 20px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .form-input,
  .date-select {
    font-size: 0.9rem;
    padding: 10px 12px;
  }

  .date-inputs {
    gap: 10px;
  }

  .section-title h2 {
    font-size: 1.4rem;
  }

  .section-title-summary h2 {
    font-size: 1.4rem;
  }

  .section-title-payment h2 {
    font-size: 1.4rem;
  }
}

@media (max-width: 480px) {
  .date-inputs {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .date-select {
    font-size: 0.85rem;
    padding: 10px 12px;
    padding-right: 32px;
    background-size: 14px;
  }

  .form-group.full-width {
    grid-column: 1 / -1;
  }

  .hero-title {
    font-size: 1.3rem;
  }

  .hero-subtitle {
    font-size: 0.8rem;
  }

  .hero-content {
    gap: 12px;
  }

  .timer-container {
    padding: 6px 12px;
    gap: 8px;
  }

  .timer-container i {
    font-size: 1rem;
  }

  .timer-value {
    font-size: 0.9rem;
  }

  .timer-label {
    font-size: 0.6rem;
  }

  .forms-section {
    padding: 16px 12px;
  }

  .form-card {
    padding-bottom: 16px;
    margin-bottom: 16px;
  }

  .form-header h3 {
    font-size: 1rem;
  }

  .form-label {
    font-size: 0.8rem;
  }

  .form-input,
  .date-select {
    font-size: 0.85rem;
    padding: 10px 10px;
  }

  .date-inputs {
    gap: 8px;
  }

  .attendee-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .ticket-type {
    padding: 3px 10px;
    font-size: 0.65rem;
  }

  .event-summary-card {
    padding: 16px 12px;
  }

  .payment-section {
    padding: 16px 12px;
  }

  .section-title h2 {
    font-size: 1.2rem;
  }

  .section-title-summary h2 {
    font-size: 1.2rem;
  }

  .section-title-payment h2 {
    font-size: 1.2rem;
  }

  .summary-header {
    flex-direction: column;
    gap: 16px;
  }

  .event-info {
    width: 100%;
  }

  .summary-badge {
    width: 100%;
    text-align: center;
  }
}

/* ============ Eliminar espacio entre header y contenido (solo móviles) ============ */
@media (max-width: 768px) {
  :deep(html),
  :deep(body) {
    padding-top: 0 !important;
    margin-top: 0 !important;
  }

  :deep(body.fixed-top),
  :deep(body.has-fixed-top),
  :deep(body) {
    padding-top: 0 !important;
  }

  :deep(.navbar.fixed-top) {
    margin-bottom: 0 !important;
  }

  client-only {
    display: block;
    margin: 0;
    padding: 0;
  }

  client-only + * {
    margin-top: 0 !important;
    padding-top: 0 !important;
  }
}
</style>

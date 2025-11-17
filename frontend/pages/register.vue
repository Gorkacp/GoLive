<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Registrarse</h2>
      <p class="subtitle">Crea tu cuenta para continuar</p>

      <!-- Paso 1: Información básica -->
      <form v-if="step === 1" @submit.prevent="goToStep2" class="login-form">
        <p class="step-indicator">Paso 1 de 2: Información básica</p>
        
        <div class="input-group">
          <input 
            type="text" 
            v-model="form.name" 
            placeholder="Nombre" 
            required 
            :disabled="loading"
          />
        </div>

        <div class="input-group">
          <input 
            type="text" 
            v-model="form.lastName" 
            placeholder="Apellidos" 
            :disabled="loading"
          />
        </div>

        <div class="input-group">
          <input 
            type="email" 
            v-model="form.email" 
            placeholder="Correo electrónico" 
            required 
            :disabled="loading"
          />
        </div>

        <div class="input-group password-group">
          <input 
            type="password" 
            v-model="form.password" 
            placeholder="Contraseña (mínimo 6 caracteres)" 
            required 
            :disabled="loading"
            minlength="6"
          />
        </div>

        <button 
          type="submit" 
          :disabled="!isStep1Valid || loading"
          class="submit-btn"
          :class="{ 'loading': loading }"
        >
          Siguiente <i class="fas fa-arrow-right"></i>
        </button>
      </form>

      <!-- Paso 2: Información personal adicional -->
      <form v-if="step === 2" @submit.prevent="handleRegister" class="login-form">
        <p class="step-indicator">Paso 2 de 2: Información personal (opcional)</p>
        
        <div class="input-group">
          <input 
            type="tel" 
            v-model="form.phoneNumber" 
            placeholder="Teléfono (ej: +34 600 000 000)" 
            :disabled="loading"
          />
        </div>

        <div class="input-group">
          <input 
            type="date" 
            v-model="form.dateOfBirth" 
            :disabled="loading"
          />
        </div>

        <div class="input-group">
          <input 
            type="text" 
            v-model="form.postalCode" 
            placeholder="Código postal" 
            :disabled="loading"
          />
        </div>

        <div class="button-group">
          <button 
            type="button" 
            @click="goBackToStep1"
            :disabled="loading"
            class="btn-secondary"
          >
            <i class="fas fa-arrow-left"></i> Atrás
          </button>
          <button 
            type="submit" 
            :disabled="loading"
            class="submit-btn"
            :class="{ 'loading': loading }"
          >
            <span v-if="loading">
              <i class="fas fa-spinner fa-spin"></i> Registrando...
            </span>
            <span v-else>
              <i class="fas fa-check"></i> Registrarse
            </span>
          </button>
        </div>
      </form>

      <div v-if="errorMessage" class="alert alert-error">
        <i class="fas fa-exclamation-circle"></i>
        {{ errorMessage }}
      </div>

      <div v-if="successMessage" class="alert alert-success">
        <i class="fas fa-check-circle"></i>
        {{ successMessage }}
      </div>

      <hr v-if="step === 1" />

      <p v-if="step === 1" class="register-text">
        ¿Ya tienes cuenta? 
        <NuxtLink to="/login" class="auth-link">Inicia sesión</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { useHead } from '#app'
useHead({
  title: 'Registro | GoLive'
})

import { ref, computed, reactive, watch } from 'vue'

// Obtener la configuración y asegurar un valor por defecto
const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'https://backend-golive.onrender.com'

console.log('🔧 Configuración API_BASE:', {
  fromConfig: config.public.apiBase,
  finalValue: API_BASE,
  isProduction: !process.dev
})

// Form state
const step = ref(1)
const form = reactive({
  name: '',
  lastName: '',
  email: '',
  password: '',
  phoneNumber: '',
  dateOfBirth: '',
  postalCode: ''
})

// UI state
const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)

// Computed properties
const isStep1Valid = computed(() => {
  return form.name.trim() && 
         form.email.trim() && 
         form.password.length >= 6 &&
         isValidEmail(form.email)
})

const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// Methods
const goToStep2 = () => {
  errorMessage.value = ''
  
  if (!form.name.trim()) {
    errorMessage.value = 'El nombre es obligatorio'
    return
  }

  if (!form.email.trim()) {
    errorMessage.value = 'El email es obligatorio'
    return
  }

  if (!isValidEmail(form.email)) {
    errorMessage.value = 'Por favor ingresa un email válido'
    return
  }

  if (form.password.length < 6) {
    errorMessage.value = 'La contraseña debe tener al menos 6 caracteres'
    return
  }

  step.value = 2
}

const goBackToStep1 = () => {
  step.value = 1
  errorMessage.value = ''
}

const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const endpoint = `${API_BASE}/api/auth/register`
    console.log('🔄 Enviando solicitud de registro a:', endpoint)

    const response = await $fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: form.name.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim().toLowerCase(),
        password: form.password,
        phoneNumber: form.phoneNumber.trim(),
        dateOfBirth: form.dateOfBirth,
        postalCode: form.postalCode.trim()
      }),
      timeout: 15000
    })

    console.log('✅ Respuesta del servidor recibida:', response)

    if (response.token && response.user) {
      // Store authentication data
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(response.user))
      
      successMessage.value = '¡Registro exitoso! Redirigiendo...'
      
      // Redirect after short delay
      setTimeout(() => {
        navigateTo('/')
      }, 1500)
    } else {
      throw new Error('Respuesta inválida del servidor')
    }

  } catch (error) {
    console.error('❌ Error en registro:', {
      error: error,
      message: error.message,
      status: error.status,
      data: error.data
    })
    
    if (error.status === 409) {
      errorMessage.value = 'Este email ya está registrado'
    } else if (error.status === 400) {
      errorMessage.value = 'Datos de registro inválidos'
    } else if (error.status === 0 || error.name === 'FetchError') {
      errorMessage.value = 'Error de conexión con el servidor. Por favor, verifica que el backend esté funcionando.'
    } else if (error.data?.message) {
      errorMessage.value = error.data.message
    } else if (error.message?.includes('timeout')) {
      errorMessage.value = 'El servidor está tardando demasiado en responder. Intenta nuevamente.'
    } else {
      errorMessage.value = 'Error inesperado. Por favor, intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}

// Clear messages when user starts typing
watch([() => form.name, () => form.email, () => form.password], () => {
  if (errorMessage.value || successMessage.value) {
    errorMessage.value = ''
    successMessage.value = ''
  }
})
</script>

<style scoped>

.login-container {
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8Zm9uZG8lMjBkZSUyMHBhbnRhbGxhJTIwZGUlMjBldmVudG9zfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Poppins', sans-serif;
  padding: 20px;
  overflow: auto;
}

.login-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.4) 100%);
  z-index: 0;
}

.login-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 3rem;
  width: 100%;
  max-width: 440px;
  color: #ffffff;
  text-align: center;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.05);
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

h2 {
  margin-bottom: 0.75rem;
  font-size: 2.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #e0e0e0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 1rem;
  margin-bottom: 2rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}

.step-indicator {
  font-size: 0.9rem;
  color: rgba(255, 182, 161, 0.9);
  margin-bottom: 1.5rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.login-form {
  margin-bottom: 1.5rem;
  animation: slideInForm 0.3s ease-out;
}

@keyframes slideInForm {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.input-group {
  margin-bottom: 1.25rem;
}

.input-group input {
  width: 100%;
  padding: 1rem 1.5rem;
  border-radius: 16px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.input-group input::placeholder {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 400;
}

.input-group input:focus {
  border-color: rgba(255, 182, 161, 0.8);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 4px rgba(255, 182, 161, 0.2);
}

.input-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn {
  width: 100%;
  background: linear-gradient(135deg, #ffb6a1 0%, #ff9a8b 100%);
  border: none;
  color: #1a1a1a;
  font-weight: 700;
  border-radius: 16px;
  padding: 1rem 1.5rem;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(255, 182, 161, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.button-group {
  display: flex;
  gap: 15px;
  margin-top: 1rem;
}

.btn-secondary {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-weight: 700;
  border-radius: 16px;
  padding: 1rem 1.5rem;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.alert {
  margin: 1.25rem 0;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.alert-error {
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fecaca;
}

.alert-success {
  background: rgba(34, 197, 94, 0.15);
  border: 1px solid rgba(34, 197, 94, 0.3);
  color: #bbf7d0;
}

hr {
  border: none;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 2rem 0;
}

.register-text {
  margin-top: 1.5rem;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.auth-link {
  color: #ffb6a1;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.auth-link:hover {
  color: #ff9a8b;
  text-decoration: underline;
}

/* Responsive design */
@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
    align-items: center;
    min-height: 100vh;
    height: 100vh;
    background-attachment: scroll;
  }

  .login-card {
    padding: 2rem 1.5rem;
    margin: 2rem 0;
  }

  h2 {
    font-size: 1.875rem;
  }

  .input-group input {
    padding: 0.875rem 1.25rem;
  }

  .button-group {
    flex-direction: column;
    gap: 12px;
  }

  .btn-secondary,
  .submit-btn {
    font-size: 1rem;
    padding: 0.875rem 1.25rem;
  }
}

/* Loading animation */
.fa-spin {
  animation: fa-spin 1s infinite linear;
}

@keyframes fa-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
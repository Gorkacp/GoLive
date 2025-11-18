<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Iniciar sesión</h2>
      <p class="subtitle">¿Ya tienes una cuenta?</p>

      <form @submit.prevent="loginUser" class="login-form">
        <div class="input-group">
          <input 
            type="email" 
            v-model="email" 
            @blur="validateEmailField"
            placeholder="Correo electrónico" 
            required 
            :disabled="loading"
          />
          <div v-if="validationErrors.email" class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            {{ validationErrors.email }}
          </div>
        </div>
        <div class="input-group password-group">
          <input 
            type="password" 
            v-model="password" 
            placeholder="Contraseña" 
            required 
            :disabled="loading"
          />
          <div v-if="validationErrors.password" class="error-message">
            <i class="fas fa-exclamation-circle"></i>
            {{ validationErrors.password }}
          </div>
        </div>

        <button 
          type="submit" 
          :disabled="loading || !isFormValid"
          class="submit-btn"
          :class="{ 'loading': loading }"
        >
          <span v-if="loading">
            <i class="fas fa-spinner fa-spin"></i> Iniciando...
          </span>
          <span v-else>Entrar</span>
        </button>

        <div class="options">
          <label>
            <input type="checkbox" v-model="remember" /> Recordarme
          </label>
          <!-- Cambio aquí: href="javascript:void(0)" evita el # -->
          <a href="javascript:void(0)" class="forgot-password" @click="openResetModal">
            ¿Olvidaste tu contraseña?
          </a>
        </div>
      </form>

      <div v-if="errorMessage" class="alert alert-error">
        <i class="fas fa-exclamation-circle"></i>
        {{ errorMessage }}
      </div>

      <hr />

      <p class="register-text">
        ¿No tienes cuenta? <NuxtLink to="/register" class="auth-link">Regístrate</NuxtLink>
      </p>
    </div>

    <!-- Modal de recuperación de contraseña -->
    <div class="modal fade" id="resetModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content reset-modal">
          <div class="modal-header reset-header">
            <div class="modal-title-wrapper">
              <i class="fas fa-key reset-icon"></i>
              <h5 class="modal-title">Recuperar Contraseña</h5>
            </div>
            <button type="button" class="btn-close-custom" data-bs-dismiss="modal">
              <i class="fas fa-times"></i>
            </button>
          </div>
          
          <div class="modal-body reset-body">
            <p class="reset-description">
              Introduce tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.
            </p>
            
            <div class="reset-input-group">
              <label for="resetEmailInput" class="reset-label">Correo Electrónico</label>
              <input 
                id="resetEmailInput"
                type="email" 
                v-model="resetEmail" 
                @blur="validateResetEmail"
                placeholder="tu@email.com" 
                class="reset-input" 
                required
                :disabled="resetLoading"
              />
              <div v-if="resetEmailError" class="reset-error-message">
                <i class="fas fa-exclamation-circle"></i>
                {{ resetEmailError }}
              </div>
              <div v-if="resetEmailValid && resetEmail" class="reset-success-message">
                <i class="fas fa-check-circle"></i>
                Email válido
              </div>
            </div>

            <div v-if="resetMessage" class="reset-message success">
              <i class="fas fa-check-circle"></i>
              {{ resetMessage }}
            </div>

            <div v-if="resetError" class="reset-message error">
              <i class="fas fa-exclamation-circle"></i>
              {{ resetError }}
            </div>
          </div>
          
          <div class="modal-footer reset-footer">
            <button type="button" class="btn btn-reset-cancel" data-bs-dismiss="modal">
              Cancelar
            </button>
            <button type="button" class="btn btn-reset-send" @click="sendResetEmail" :disabled="resetLoading || !resetEmail">
              <span v-if="resetLoading">
                <i class="fas fa-spinner fa-spin"></i> Enviando...
              </span>
              <span v-else>
                <i class="fas fa-paper-plane"></i> Enviar Enlace
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { useHead } from '#app'
useHead({
  title: 'Login | GoLive'
})

import { ref, onMounted, watch, computed } from 'vue'

const email = ref('')
const password = ref('')
const remember = ref(false)
const errorMessage = ref('')
const loading = ref(false)
const validationErrors = ref({
  email: '',
  password: ''
})

// Validación local del email
const isValidEmail = (emailValue) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(emailValue)
}

// Validar email en blur
const validateEmailField = () => {
  if (!email.value.trim()) {
    validationErrors.value.email = 'El correo electrónico es requerido'
  } else if (!isValidEmail(email.value)) {
    validationErrors.value.email = 'Por favor ingresa un correo válido'
  } else {
    validationErrors.value.email = ''
  }
}

// Validar contraseña
const validatePasswordField = () => {
  if (!password.value) {
    validationErrors.value.password = 'La contraseña es requerida'
  } else if (password.value.length < 8) {
    validationErrors.value.password = 'La contraseña debe tener al menos 8 caracteres'
  } else {
    validationErrors.value.password = ''
  }
}

// Computed para validar si el formulario es válido
const isFormValid = computed(() => {
  return email.value.trim() !== '' && 
         password.value !== '' &&
         isValidEmail(email.value) &&
         !validationErrors.value.email &&
         !validationErrors.value.password
})

// Recuperación de contraseña
const resetEmail = ref('')
const resetLoading = ref(false)
const resetMessage = ref('')
const resetError = ref('')
const resetEmailError = ref('')
const resetEmailValid = ref(false)

// Validar email en el modal
const validateResetEmail = async () => {
  resetEmailError.value = ''
  resetEmailValid.value = false

  if (!resetEmail.value.trim()) {
    resetEmailError.value = 'El correo electrónico es requerido'
    return
  }

  if (!isValidEmail(resetEmail.value)) {
    resetEmailError.value = 'Por favor ingresa un correo válido'
    return
  }

  try {
    // Validar disponibilidad del email con el backend
    const response = await $fetch(`${apiBase}/api/auth/check-email`, {
      method: 'POST',
      body: { email: resetEmail.value.trim() },
      headers: { 'Content-Type': 'application/json' }
    })

    if (response.available) {
      resetEmailError.value = 'Este email no está registrado en el sistema'
    } else {
      resetEmailValid.value = true
    }
  } catch (err) {
    resetEmailError.value = 'Error al validar el email'
  }
}

// Obtener la base URL de la API
const config = useRuntimeConfig()
const apiBase = config.public.apiBase

// Función login
const loginUser = async () => {
  // Validar antes de enviar
  validateEmailField()
  validatePasswordField()
  
  if (!isFormValid.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    const response = await $fetch(`${apiBase}/api/auth/login`, {
      method: 'POST',
      body: { email: email.value.trim(), password: password.value },
      headers: { 'Content-Type': 'application/json' }
    })
    if (response.token) {
      // Construir objeto usuario desde la respuesta
      const user = {
        id: response.id,
        email: response.email,
        name: response.name,
        lastName: response.lastName,
        role: response.role,
        phoneNumber: response.phoneNumber,
        dateOfBirth: response.dateOfBirth,
        postalCode: response.postalCode,
        profilePhoto: response.profilePhoto
      }
      
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify(user))
      
      if (remember.value) {
        localStorage.setItem('rememberedEmail', email.value)
        localStorage.setItem('rememberedPassword', password.value)
      } else {
        localStorage.removeItem('rememberedEmail')
        localStorage.removeItem('rememberedPassword')
      }
      
      // Redirigir según el rol del usuario
      if (response.role === 'ADMIN') {
        navigateTo('/oficina')
      } else {
        navigateTo('/')
      }
    }
  } catch (err) {
    // Manejar errores del backend
    if (err.status === 400) {
      errorMessage.value = err.data?.message || 'Email o contraseña incorrectos'
    } else if (err.status === 403) {
      errorMessage.value = 'Acceso denegado. Verifica tus credenciales'
    } else {
      errorMessage.value = 'Error al iniciar sesión. Por favor intenta de nuevo'
    }
  } finally {
    loading.value = false
  }
}

// Abrir modal de recuperación
const openResetModal = () => {
  resetEmail.value = ''
  resetMessage.value = ''
  resetError.value = ''
  resetEmailError.value = ''
  resetEmailValid.value = false
  const modalEl = document.getElementById('resetModal')
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl)
    modal.show()
  }
}

// Enviar correo de recuperación
const sendResetEmail = async () => {
  // Validar primero
  if (!resetEmail.value) {
    resetEmailError.value = 'El correo electrónico es requerido'
    return
  }

  if (!resetEmailValid.value) {
    await validateResetEmail()
    if (!resetEmailValid.value) return
  }

  resetLoading.value = true
  resetError.value = ''
  
  try {
    const response = await $fetch(`${apiBase}/api/auth/forgot-password`, {
      method: 'POST',
      body: { email: resetEmail.value.trim() },
      headers: { 'Content-Type': 'application/json' }
    })
    
    resetMessage.value = 'Se ha enviado un enlace de recuperación a tu correo. Por favor revisa tu bandeja de entrada.'
    resetEmailError.value = ''
    
    // Cerrar modal después de 2 segundos
    setTimeout(() => {
      const modalEl = document.getElementById('resetModal')
      bootstrap.Modal.getInstance(modalEl)?.hide()
    }, 2000)
  } catch (err) {
    resetError.value = err.data?.message || 'Error al enviar el correo. Por favor intenta de nuevo.'
    resetMessage.value = ''
  } finally {
    resetLoading.value = false
  }
}

// Restablecer contraseña usando token (puedes usar otro componente o modal para esto)
const resetPassword = async (token, newPassword) => {
  try {
    await $fetch(`${apiBase}/api/auth/reset-password?token=${token}`, {
      method: 'POST',
      body: { password: newPassword },
      headers: { 'Content-Type': 'application/json' }
    })
    alert('Contraseña restablecida correctamente.')
  } catch (err) {
    alert(err.data?.message || 'Error al restablecer la contraseña.')
  }
}

// Recordar usuario
onMounted(() => {
  const savedEmail = localStorage.getItem('rememberedEmail')
  const savedPassword = localStorage.getItem('rememberedPassword')
  if (savedEmail && savedPassword) {
    email.value = savedEmail
    password.value = savedPassword
    remember.value = true
  }
})

// Limpiar errores al escribir
watch(email, () => { 
  errorMessage.value = ''
  if (email.value) validationErrors.value.email = ''
})

watch(password, () => { 
  errorMessage.value = ''
  if (password.value) validationErrors.value.password = ''
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
  -webkit-backdrop-filter: blur(20px);
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

@media (max-width: 600px) {
  h2 {
    font-size: 1.75rem;
    margin-bottom: 0.5rem;
  }
}

.subtitle {
  font-size: 1rem;
  margin-bottom: 2rem;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.5;
}

@media (max-width: 600px) {
  .subtitle {
    font-size: 0.85rem;
    margin-bottom: 1rem;
  }
}

.login-form {
  margin-bottom: 1.5rem;
}

@media (max-width: 600px) {
  .login-form {
    margin-bottom: 1rem;
  }
}

.input-group {
  margin-bottom: 1.25rem;
}

@media (max-width: 600px) {
  .input-group {
    margin-bottom: 0.85rem;
  }
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

@media (max-width: 600px) {
  .input-group input {
    padding: 0.75rem 1rem;
    font-size: 0.9rem;
  }
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

/* Mensajes de validación */
.error-message {
  display: flex;
  align-items: flex-start;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: #fecaca;
  font-weight: 500;
  line-height: 1.35;
  word-wrap: break-word;
  flex-wrap: wrap;
  align-content: flex-start;
  margin-top: 0.3rem;
}

@media (max-width: 600px) {
  .error-message {
    font-size: 0.7rem;
    gap: 0.3rem;
    line-height: 1.3;
  }
}

.error-message i {
  margin-top: 0.1rem;
  flex-shrink: 0;
  min-width: 14px;
}

.submit-btn {
  width: 100%;
  background: linear-gradient(90deg, #ff4757 0%, #ff6348 50%, #ff5722 100%);
  border: none;
  color: #ffffff;
  font-weight: 700;
  border-radius: 50px;
  padding: 1rem 1.5rem;
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0.5rem;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 20px rgba(255, 71, 87, 0.3);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

@media (max-width: 600px) {
  .submit-btn {
    padding: 0.75rem 1rem;
    font-size: 0.95rem;
    border-radius: 40px;
  }
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(255, 71, 87, 0.4);
  background: linear-gradient(90deg, #ff3842 0%, #ff5335 50%, #ff4500 100%);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.submit-btn.loading {
  pointer-events: none;
}

.options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1.5rem;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

@media (max-width: 600px) {
  .options {
    margin-top: 1rem;
    font-size: 0.8rem;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
}

.options label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.options input[type="checkbox"] {
  margin: 0;
}

.forgot-password {
  color: #ff6348;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #ff5722;
  text-decoration: underline;
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

@media (max-width: 600px) {
  .alert {
    margin: 1rem 0;
    padding: 0.75rem 1rem;
    font-size: 0.8rem;
    gap: 0.5rem;
  }
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
  color: #ff6348;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.auth-link:hover {
  color: #ff5722;
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

  .options {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
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

/* ========== MODAL PERSONALIZADO ========== */
.reset-modal {
  background: rgba(255, 255, 255, 0.12);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4);
  color: #ffffff;
}

.reset-modal::backdrop {
  background: rgba(0, 0, 0, 0.6);
}

.reset-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 71, 87, 0.1);
  border-radius: 20px 20px 0 0;
}

.modal-title-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.reset-icon {
  font-size: 1.5rem;
  color: #ff6348;
}

.modal-title {
  font-size: 1.3rem;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(90deg, #ff4757 0%, #ff6348 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.btn-close-custom {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.3s ease;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close-custom:hover {
  color: #ff6348;
}

.reset-body {
  padding: 2rem 1.5rem;
}

.reset-description {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.reset-input-group {
  margin-bottom: 1.5rem;
}

.reset-label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: rgba(255, 255, 255, 0.9);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.reset-input {
  width: 100%;
  padding: 0.9rem 1.2rem;
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.1);
  outline: none;
  background: rgba(255, 255, 255, 0.08);
  color: #ffffff;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.3s ease;
  box-sizing: border-box;
  font-family: 'Poppins', sans-serif;
}

.reset-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
  font-weight: 400;
}

.reset-input:focus {
  border-color: rgba(255, 99, 72, 0.8);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 4px rgba(255, 99, 72, 0.2);
}

.reset-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.reset-message {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.2rem;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 500;
  margin-top: 1rem;
}

.reset-message.success {
  background: rgba(34, 197, 94, 0.15);
  border: 1px solid rgba(34, 197, 94, 0.3);
  color: #86efac;
}

.reset-message.error {
  background: rgba(239, 68, 68, 0.15);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fecaca;
}

.reset-message i {
  font-size: 1.2rem;
  flex-shrink: 0;
}

.reset-error-message {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #fecaca;
  font-weight: 500;
  line-height: 1.3;
  word-wrap: break-word;
  margin-top: 0.4rem;
  animation: slideIn 0.3s ease-out;
}

.reset-error-message i {
  margin-top: 0.1rem;
  flex-shrink: 0;
  min-width: 14px;
  font-size: 1rem;
}

.reset-success-message {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  font-size: 0.8rem;
  color: #86efac;
  font-weight: 500;
  line-height: 1.3;
  margin-top: 0.4rem;
  animation: slideIn 0.3s ease-out;
}

.reset-success-message i {
  margin-top: 0.1rem;
  flex-shrink: 0;
  min-width: 14px;
  font-size: 1rem;
}

.reset-footer {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 0 0 20px 20px;
}

.btn-reset-cancel {
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  font-weight: 600;
  border-radius: 12px;
  padding: 0.75rem 1.5rem;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.btn-reset-cancel:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.btn-reset-send {
  background: linear-gradient(90deg, #ff4757 0%, #ff6348 50%, #ff5722 100%);
  border: none;
  color: #ffffff;
  font-weight: 600;
  border-radius: 12px;
  padding: 0.75rem 1.5rem;
  font-size: 0.95rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 6px 16px rgba(255, 71, 87, 0.25);
}

.btn-reset-send:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(255, 71, 87, 0.35);
  background: linear-gradient(90deg, #ff3842 0%, #ff5335 50%, #ff4500 100%);
}

.btn-reset-send:active:not(:disabled) {
  transform: translateY(0);
}

.btn-reset-send:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 600px) {
  .reset-header {
    padding: 1.25rem;
  }

  .reset-body {
    padding: 1.5rem 1.25rem;
  }

  .reset-footer {
    padding: 1.25rem;
    flex-direction: column;
    align-items: center;
  }

  .btn-reset-cancel,
  .btn-reset-send {
    width: 100%;
    padding: 0.75rem 1rem;
    font-size: 0.85rem;
    justify-content: center;
  }

  .modal-title {
    font-size: 1.1rem;
  }

  .reset-description {
    font-size: 0.85rem;
  }
}

</style>
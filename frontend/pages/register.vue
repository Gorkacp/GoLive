<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Registrarse</h2>
      <p class="subtitle">Crea tu cuenta para continuar</p>

      <!-- Formulario de Registro -->
      <form @submit.prevent="handleRegister" class="login-form">
        
        <!-- Campo Nombre -->
        <div class="input-group">
          <label class="input-label">Nombre</label>
          <input 
            type="text" 
            v-model="form.name" 
            placeholder="Nombre" 
            required 
            :disabled="loading"
            @blur="validateName"
          />
          <span v-if="validation.name.error" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ validation.name.error }}
          </span>
          <span v-if="validation.name.success && form.name" class="success-message">
            <i class="fas fa-check-circle"></i> Nombre válido
          </span>
        </div>

        <!-- Campo Email -->
        <div class="input-group">
          <label class="input-label">Correo electrónico</label>
          <input 
            type="email" 
            v-model="form.email" 
            placeholder="tu@email.com" 
            required 
            :disabled="loading"
            @blur="validateEmail"
          />
          <span v-if="validation.email.checking" class="checking-message">
            <i class="fas fa-spinner fa-spin"></i> Verificando disponibilidad...
          </span>
          <span v-else-if="validation.email.error" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ validation.email.error }}
          </span>
          <span v-else-if="validation.email.success && form.email" class="success-message">
            <i class="fas fa-check-circle"></i> Email disponible
          </span>
        </div>

        <!-- Campo Contraseña -->
        <div class="input-group">
          <label class="input-label">Contraseña</label>
          <div class="password-wrapper">
            <input 
              :type="showPassword ? 'text' : 'password'" 
              v-model="form.password" 
              placeholder="Mínimo 8 caracteres" 
              required 
              :disabled="loading"
              @input="validatePassword"
            />
            <button 
              type="button" 
              class="toggle-password" 
              @click="showPassword = !showPassword"
              :disabled="loading"
            >
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
          
          <!-- Contenedor de fortaleza y mensajes con altura fija -->
          <div class="password-feedback-container">
            <!-- Indicador de fortaleza -->
            <div class="strength-meter" :class="{ 'active': form.password }">
              <div class="strength-bar" :class="passwordStrength.class"></div>
            </div>
            
            <!-- Texto de fortaleza y error -->
            <div class="password-feedback">
              <span v-if="form.password" class="strength-text" :class="passwordStrength.textClass">
                {{ passwordStrength.text }}
              </span>
              <span v-else class="strength-text-placeholder"></span>
              
              <span v-if="validation.password.error" class="error-message">
                <i class="fas fa-exclamation-circle"></i> {{ validation.password.error }}
              </span>
            </div>
          </div>
        </div>

        <!-- Campo Confirmar Contraseña -->
        <div class="input-group">
          <label class="input-label">Confirmar contraseña</label>
          <div class="password-wrapper">
            <input 
              :type="showConfirmPassword ? 'text' : 'password'" 
              v-model="form.confirmPassword" 
              placeholder="Repite tu contraseña" 
              required 
              :disabled="loading"
              @blur="validateConfirmPassword"
            />
            <button 
              type="button" 
              class="toggle-password" 
              @click="showConfirmPassword = !showConfirmPassword"
              :disabled="loading"
            >
              <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
          <span v-if="validation.confirmPassword.error" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ validation.confirmPassword.error }}
          </span>
          <span v-else-if="validation.confirmPassword.success && form.confirmPassword" class="success-message">
            <i class="fas fa-check-circle"></i> Las contraseñas coinciden
          </span>
        </div>

        <!-- Checkbox de términos -->
        <div class="checkbox-group">
          <label class="checkbox-label">
            <input 
              type="checkbox" 
              v-model="form.acceptTerms" 
              :disabled="loading"
              required
            />
            <span>Acepto los <a href="#" class="link">términos y condiciones</a></span>
          </label>
          <span v-if="validation.terms.error" class="error-message">
            <i class="fas fa-exclamation-circle"></i> {{ validation.terms.error }}
          </span>
        </div>

        <!-- Botón de envío -->
        <button 
          type="submit" 
          :disabled="!isFormValid || loading"
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
      </form>

      <!-- Mensajes de error -->
      <div v-if="errorMessage" class="alert alert-error">
        <i class="fas fa-exclamation-circle"></i>
        <div>
          <strong>Error en el registro</strong>
          <p>{{ errorMessage }}</p>
        </div>
      </div>

      <!-- Mensajes de éxito -->
      <div v-if="successMessage" class="alert alert-success">
        <i class="fas fa-check-circle"></i>
        <div>
          <strong>¡Registro exitoso!</strong>
          <p>{{ successMessage }}</p>
        </div>
      </div>

      <hr />

      <p class="register-text">
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

// Obtener la configuración
const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'https://backend-golive.onrender.com'

// Form state
const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  acceptTerms: false
})

// UI state
const errorMessage = ref('')
const successMessage = ref('')
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

// Validation state
const validation = reactive({
  name: { error: '', success: false },
  email: { error: '', success: false, checking: false },
  password: { error: '', success: false },
  confirmPassword: { error: '', success: false },
  terms: { error: '' }
})

// Email regex pattern
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

// Validar nombre
const validateName = () => {
  validation.name.error = ''
  validation.name.success = false
  
  if (form.name.trim().length < 2) {
    validation.name.error = 'El nombre debe tener al menos 2 caracteres'
  } else if (form.name.trim().length > 50) {
    validation.name.error = 'El nombre no puede exceder 50 caracteres'
  } else if (!/^[a-záéíóúñ\s'-]+$/i.test(form.name)) {
    validation.name.error = 'El nombre solo puede contener letras, espacios y guiones'
  } else {
    validation.name.success = true
  }
}

// Validar email
const validateEmail = async () => {
  validation.email.error = ''
  validation.email.success = false
  
  const email = form.email.trim()
  
  if (!email) {
    return
  }
  
  if (!emailRegex.test(email)) {
    validation.email.error = 'Por favor ingresa un email válido'
    return
  }
  
  // Verificar disponibilidad en el backend
  validation.email.checking = true
  try {
    const response = await $fetch(`${API_BASE}/api/auth/check-email`, {
      method: 'POST',
      body: { email: email.toLowerCase() },
      timeout: 5000
    })
    
    if (response.available === false) {
      validation.email.error = response.message || 'Este email ya está registrado'
      validation.email.success = false
    } else {
      validation.email.success = true
    }
  } catch (error) {
    // Si hay error de conexión, mostrar mensaje
    validation.email.error = 'Error al validar email. Intenta nuevamente.'
    validation.email.success = false
  } finally {
    validation.email.checking = false
  }
}

// Validar contraseña
const validatePassword = async () => {
  validation.password.error = ''
  validation.password.success = false
  
  const password = form.password
  
  if (!password) {
    return
  }
  
  // Validación en backend
  try {
    const response = await $fetch(`${API_BASE}/api/auth/validate-password`, {
      method: 'POST',
      body: { password: password },
      timeout: 5000
    })
    
    if (!response.valid) {
      validation.password.error = response.message || 'La contraseña no cumple con los requisitos'
      validation.password.success = false
    } else {
      validation.password.success = true
    }
  } catch (error) {
    // Fallback a validación local si falla el backend
    if (password.length < 8) {
      validation.password.error = 'La contraseña debe tener al menos 8 caracteres'
    } else if (password.length > 128) {
      validation.password.error = 'La contraseña no puede exceder 128 caracteres'
    } else if (!/[A-Z]/.test(password)) {
      validation.password.error = 'Debe contener al menos una letra mayúscula'
    } else if (!/[a-z]/.test(password)) {
      validation.password.error = 'Debe contener al menos una letra minúscula'
    } else if (!/[0-9]/.test(password)) {
      validation.password.error = 'Debe contener al menos un número'
    } else if (!/[!@#$%^&*()_+\-=\[\]{};':",./<>?]/.test(password)) {
      validation.password.error = 'Debe contener al menos un carácter especial'
    } else if (password.includes(' ')) {
      validation.password.error = 'No puede contener espacios en blanco'
    } else {
      validation.password.success = true
    }
  }
  
  // Revalidar confirmación si ya está llena
  if (form.confirmPassword) {
    validateConfirmPassword()
  }
}

// Validar confirmación de contraseña
const validateConfirmPassword = () => {
  validation.confirmPassword.error = ''
  validation.confirmPassword.success = false
  
  if (!form.confirmPassword) {
    return
  }
  
  if (form.password !== form.confirmPassword) {
    validation.confirmPassword.error = 'Las contraseñas no coinciden'
  } else {
    validation.confirmPassword.success = true
  }
}

// Calcular fortaleza de contraseña
const passwordStrength = computed(() => {
  const password = form.password
  let strength = 0
  
  if (!password) return { text: '', class: '', textClass: '' }
  
  if (password.length >= 8) strength++
  if (password.length >= 12) strength++
  if (/[a-z]/.test(password)) strength++
  if (/[A-Z]/.test(password)) strength++
  if (/[0-9]/.test(password)) strength++
  if (/[!@#$%^&*]/.test(password)) strength++
  
  const levels = {
    0: { text: 'Muy débil', class: 'strength-very-weak', textClass: 'text-danger' },
    1: { text: 'Débil', class: 'strength-weak', textClass: 'text-warning' },
    2: { text: 'Regular', class: 'strength-fair', textClass: 'text-warning' },
    3: { text: 'Buena', class: 'strength-good', textClass: 'text-info' },
    4: { text: 'Fuerte', class: 'strength-strong', textClass: 'text-success' },
    5: { text: 'Muy fuerte', class: 'strength-very-strong', textClass: 'text-success' },
    6: { text: 'Excelente', class: 'strength-excellent', textClass: 'text-success' }
  }
  
  return levels[Math.min(strength, 6)]
})

// Validación total del formulario
const isFormValid = computed(() => {
  return (
    form.name.trim().length >= 2 &&
    validation.email.success &&
    validation.password.success &&
    validation.confirmPassword.success &&
    form.acceptTerms &&
    !loading.value
  )
})

// Enviar registro
const handleRegister = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const endpoint = `${API_BASE}/api/auth/register`

    const response = await $fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        name: form.name.trim(),
        email: form.email.trim().toLowerCase(),
        password: form.password
      }),
      timeout: 15000
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
      
      // Guardar token usando composable
      const { setToken } = useAuth()
      setToken(response.token)
      
      // Guardar datos en sessionStorage para uso en cliente
      if (typeof window !== 'undefined') {
        sessionStorage.setItem('user', JSON.stringify(user))
      }
      
      successMessage.value = 'Cuenta creada exitosamente. Redirigiendo...'
      
      // Redirect after short delay
      setTimeout(() => {
        navigateTo('/')
      }, 2000)
    } else {
      throw new Error('Respuesta inválida del servidor')
    }

  } catch (error) {
    // Manejo profesional de errores de validación del backend
    if (error.status === 400 && error.data) {
      const response = error.data
      
      // Si tiene errores de validación detallados
      if (response.errors) {
        const fieldErrors = response.errors
        let firstErrorField = null
        let errorSummary = []
        
        // Procesar errores por campo
        Object.keys(fieldErrors).forEach(field => {
          const messages = fieldErrors[field]
          
          if (field === 'name') {
            validation.name.error = messages[0]
            if (!firstErrorField) firstErrorField = 'name'
          } else if (field === 'email') {
            validation.email.error = messages[0]
            validation.email.success = false
            if (!firstErrorField) firstErrorField = 'email'
          } else if (field === 'password') {
            validation.password.error = messages[0]
            validation.password.success = false
            if (!firstErrorField) firstErrorField = 'password'
          }
          
          errorSummary.push(...messages)
        })
        
        errorMessage.value = errorSummary.join(' | ')
      } else {
        // Mensaje genérico de error
        errorMessage.value = response.message || 'Datos de registro inválidos. Verifica los campos.'
      }
    } else if (error.status === 409 || (error.data && error.data.includes('ya está registrado'))) {
      errorMessage.value = 'Este email ya está registrado. Intenta con otro o inicia sesión.'
      validation.email.error = 'Email ya registrado'
      validation.email.success = false
    } else if (error.status === 0 || error.name === 'FetchError') {
      errorMessage.value = 'Error de conexión. Verifica que el servidor esté funcionando.'
    } else if (error.data) {
      errorMessage.value = error.data
    } else {
      errorMessage.value = 'Error inesperado. Por favor, intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}

// Limpiar mensajes al escribir
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
  max-width: 500px;
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

.step-indicator {
  font-size: 0.9rem;
  color: rgba(255, 182, 161, 0.9);
  margin-bottom: 1.5rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 600px) {
  .step-indicator {
    font-size: 0.75rem;
    margin-bottom: 0.75rem;
  }
}

.login-form {
  margin-bottom: 1.5rem;
  animation: slideInForm 0.3s ease-out;
  text-align: left;
}

@media (max-width: 600px) {
  .login-form {
    margin-bottom: 1rem;
  }
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

@media (max-width: 600px) {
  .input-group {
    margin-bottom: 0.85rem;
  }
}

.input-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: rgba(255, 255, 255, 0.9);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 600px) {
  .input-label {
    font-size: 0.75rem;
    margin-bottom: 0.3rem;
  }
}

.input-group input {
  width: 100%;
  padding: 1rem 1.5rem;
  border-radius: 12px !important;
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

/* Password wrapper */
.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.password-wrapper input {
  width: 100%;
  padding-right: 3.5rem;
  border-radius: 12px !important;
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
  .password-wrapper input {
    padding: 0.75rem 2.8rem 0.75rem 1rem;
    font-size: 0.9rem;
  }
}

.password-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.6);
  font-weight: 400;
}

.password-wrapper input:focus {
  border-color: rgba(255, 182, 161, 0.8);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 4px rgba(255, 182, 161, 0.2);
}

.password-wrapper input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.toggle-password {
  position: absolute;
  right: 1rem;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 1.1rem;
  transition: color 0.3s ease;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.toggle-password:hover:not(:disabled) {
  color: rgba(255, 182, 161, 0.8);
}

.toggle-password:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Contenedor de feedback de contraseña con altura FIJA */
.password-feedback-container {
  height: 90px;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  margin-top: 0.4rem;
}

@media (max-width: 600px) {
  .password-feedback-container {
    height: 75px;
    gap: 0.2rem;
    margin-top: 0.3rem;
  }
}

/* Fortaleza de contraseña */
.strength-meter {
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  transition: opacity 0.3s ease;
  opacity: 0;
}

.strength-meter.active {
  opacity: 1;
}

.strength-bar {
  height: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
  width: 0%;
}

.strength-very-weak {
  width: 16%;
  background: #ef4444;
}

.strength-weak {
  width: 32%;
  background: #f97316;
}

.strength-fair {
  width: 48%;
  background: #eab308;
}

.strength-good {
  width: 64%;
  background: #3b82f6;
}

.strength-strong {
  width: 80%;
  background: #22c55e;
}

.strength-very-strong {
  width: 90%;
  background: #16a34a;
}

.strength-excellent {
  width: 100%;
  background: #15803d;
}

/* Feedback de contraseña */
.password-feedback {
  height: 82px;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  justify-content: flex-start;
}

@media (max-width: 600px) {
  .password-feedback {
    height: 68px;
  }
}

.strength-text {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  height: 1.2rem;
  line-height: 1.2;
}

@media (max-width: 600px) {
  .strength-text {
    font-size: 0.75rem;
    height: 1rem;
    line-height: 1;
  }
}

.strength-text-placeholder {
  display: block;
  height: 1.2rem;
}

.text-danger {
  color: #fecaca;
}

.text-warning {
  color: #fde047;
}

.text-info {
  color: #93c5fd;
}

.text-success {
  color: #86efac;
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

.success-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #86efac;
  margin-top: 0.5rem;
  font-weight: 500;
}

.checking-message {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.85rem;
  color: #93c5fd;
  margin-top: 0.5rem;
  font-weight: 500;
}

/* Checkbox */
.checkbox-group {
  margin-bottom: 1.5rem;
  text-align: left;
}

@media (max-width: 600px) {
  .checkbox-group {
    margin-bottom: 1rem;
  }
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
  cursor: pointer;
}

@media (max-width: 600px) {
  .checkbox-label {
    font-size: 0.8rem;
    gap: 0.5rem;
  }
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #ff6348;
}

.checkbox-label a {
  color: #ff6348;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.checkbox-label a:hover {
  color: #ff5722;
  text-decoration: underline;
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
  margin-top: 1rem;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 20px rgba(255, 71, 87, 0.3);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

@media (max-width: 600px) {
  .submit-btn {
    padding: 0.75rem 1rem;
    font-size: 0.95rem;
    margin-top: 0.75rem;
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
  margin: 1.5rem 0;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  font-size: 0.95rem;
  font-weight: 500;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  animation: slideIn 0.3s ease-out;
}

@media (max-width: 600px) {
  .alert {
    margin: 1rem 0;
    padding: 0.75rem 1rem;
    font-size: 0.8rem;
    gap: 0.75rem;
  }
}

.alert i {
  font-size: 1.25rem;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.alert div {
  text-align: left;
}

.alert strong {
  display: block;
  margin-bottom: 0.25rem;
  font-size: 1rem;
}

.alert p {
  margin: 0;
  opacity: 0.95;
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
  color: #ff6348;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.3s ease;
}

.auth-link:hover {
  color: #ff5722;
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
    align-items: center;
    min-height: 100vh;
    height: auto;
    background-attachment: scroll;
  }

  .login-card {
    padding: 2rem 1.5rem;
    max-width: 100%;
  }

  h2 {
    font-size: 1.875rem;
  }

  .input-group input {
    padding: 0.875rem 1.25rem;
  }

  .alert {
    font-size: 0.85rem;
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
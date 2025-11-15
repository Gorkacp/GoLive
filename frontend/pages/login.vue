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
            placeholder="Correo electrónico" 
            required 
            :disabled="loading"
          />
        </div>
        <div class="input-group password-group">
          <input 
            type="password" 
            v-model="password" 
            placeholder="Contraseña" 
            required 
            :disabled="loading"
          />
        </div>

        <button 
          type="submit" 
          :disabled="loading || !email || !password"
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
        <div class="modal-content modal-custom">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="fas fa-key me-2"></i>
              Recuperar contraseña
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>Introduce tu correo electrónico y te enviaremos un enlace para restablecer tu contraseña.</p>
            <input 
              type="email" 
              v-model="resetEmail" 
              placeholder="Correo electrónico" 
              class="modal-input" 
              required
              :disabled="resetLoading"
            />
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-primary" @click="sendResetEmail" :disabled="resetLoading">
              <span v-if="resetLoading"><i class="fas fa-spinner fa-spin me-1"></i> Enviando...</span>
              <span v-else>Enviar enlace</span>
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

import { ref, onMounted, watch } from 'vue'

const email = ref('')
const password = ref('')
const remember = ref(false)
const errorMessage = ref('')
const loading = ref(false)

// Recuperación de contraseña
const resetEmail = ref('')
const resetLoading = ref(false)
const resetMessage = ref('')

// Obtener la base URL de la API
const config = useRuntimeConfig()
const apiBase = config.public.apiBase

// Función login
const loginUser = async () => {
  if (!email.value || !password.value) return
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await $fetch(`${apiBase}/api/auth/login`, {
      method: 'POST',
      body: { email: email.value.trim(), password: password.value },
      headers: { 'Content-Type': 'application/json' }
    })
    if (response.token) {
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify({
        email: response.email,
        name: response.name,
        role: response.role,
        type: response.type || "Bearer"
      }))
      if (remember.value) {
        localStorage.setItem('rememberedEmail', email.value)
        localStorage.setItem('rememberedPassword', password.value)
      } else {
        localStorage.removeItem('rememberedEmail')
        localStorage.removeItem('rememberedPassword')
      }
      navigateTo('/')
    }
  } catch (err) {
    errorMessage.value = err.data?.message || 'Email o contraseña incorrectos'
  } finally {
    loading.value = false
  }
}

// Abrir modal de recuperación
const openResetModal = () => {
  resetEmail.value = ''
  resetMessage.value = ''
  const modalEl = document.getElementById('resetModal')
  if (modalEl) {
    const modal = new bootstrap.Modal(modalEl)
    modal.show()
  }
}

// Enviar correo de recuperación
const sendResetEmail = async () => {
  if (!resetEmail.value) return
  resetLoading.value = true
  try {
    await $fetch(`${apiBase}/api/auth/forgot-password`, {
      method: 'POST',
      body: { email: resetEmail.value.trim() },
      headers: { 'Content-Type': 'application/json' }
    })
    alert('Se ha enviado un enlace de recuperación a tu correo.')
    const modalEl = document.getElementById('resetModal')
    bootstrap.Modal.getInstance(modalEl)?.hide()
  } catch (err) {
    alert(err.data?.message || 'Error al enviar el correo.')
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
watch([email, password], () => { errorMessage.value = '' })
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

.login-form {
  margin-bottom: 1.5rem;
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
  color: #ffb6a1;
  text-decoration: none;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #ff9a8b;
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
</style>
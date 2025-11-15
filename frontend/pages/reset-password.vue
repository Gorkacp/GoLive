<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Restablecer contraseña</h2>
      <p class="subtitle">Introduce tu nueva contraseña</p>

      <form @submit.prevent="resetPassword" class="login-form">
        <div class="input-group">
          <input
            type="password"
            v-model="password"
            placeholder="Nueva contraseña"
            required
            :disabled="loading"
          />
        </div>

        <div class="input-group">
          <input
            type="password"
            v-model="confirmPassword"
            placeholder="Confirmar contraseña"
            required
            :disabled="loading"
          />
        </div>

        <button
          type="submit"
          class="submit-btn"
          :disabled="loading || !password || !confirmPassword"
          :class="{ loading: loading }"
        >
          <span v-if="loading">
            <i class="fas fa-spinner fa-spin me-1"></i> Restableciendo...
          </span>
          <span v-else>Restablecer contraseña</span>
        </button>
      </form>

      <div v-if="message" class="alert alert-success">{{ message }}</div>
      <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const config = useRuntimeConfig()

const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const message = ref('')
const errorMessage = ref('')

// Token desde query params
const token = route.query.token || ''

const resetPassword = async () => {
  if (!password.value || !confirmPassword.value) return
  if (password.value !== confirmPassword.value) {
    errorMessage.value = "Las contraseñas no coinciden"
    return
  }

  loading.value = true
  errorMessage.value = ''
  message.value = ''

  try {
    await $fetch(`${config.public.apiBase}/api/auth/reset-password?token=${token}`, {
      method: 'POST',
      body: { password: password.value },
      headers: { 'Content-Type': 'application/json' }
    })

    message.value = "Contraseña restablecida correctamente. Redirigiendo al login..."
    setTimeout(() => router.push('/login'), 2500)
  } catch (err) {
    errorMessage.value = err.data?.message || 'Error al restablecer la contraseña'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css');

/* Estilos del login optimizados */
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
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: linear-gradient(135deg, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.4) 100%);
  z-index: 0;
}

.login-card {
  position: relative;
  z-index: 1;
  background: rgba(255,255,255,0.12);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.1);
  border-radius: 24px;
  padding: 3rem;
  width: 100%;
  max-width: 440px;
  color: #fff;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0,0,0,0.3), 0 0 0 1px rgba(255,255,255,0.05);
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp { from { opacity: 0; transform: translateY(30px) } to { opacity: 1; transform: translateY(0) } }

h2 {
  margin-bottom: 0.75rem;
  font-size: 2.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #fff 0%, #e0e0e0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.subtitle { font-size: 1rem; margin-bottom: 2rem; font-weight: 500; color: rgba(255,255,255,0.8); line-height: 1.5; }

.input-group { margin-bottom: 1.25rem; }
.input-group input {
  width: 100%; padding: 1rem 1.5rem; border-radius: 16px; border: 2px solid rgba(255,255,255,0.1);
  outline: none; background: rgba(255,255,255,0.08); color: #fff; font-size: 1rem; font-weight: 500;
  transition: all 0.3s ease; box-sizing: border-box;
}
.input-group input::placeholder { color: rgba(255,255,255,0.6); font-weight: 400; }
.input-group input:focus { border-color: rgba(255,182,161,0.8); background: rgba(255,255,255,0.12); box-shadow: 0 0 0 4px rgba(255,182,161,0.2); }

.submit-btn {
  width: 100%; background: linear-gradient(135deg,#ffb6a1 0%,#ff9a8b 100%); border: none; color: #1a1a1a;
  font-weight: 700; border-radius: 16px; padding: 1rem 1.5rem; font-size: 1.1rem; cursor: pointer;
  transition: all 0.3s ease; margin-top: 0.5rem; position: relative; overflow: hidden;
}
.submit-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 12px 24px rgba(255,182,161,0.3); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }

.alert {
  margin: 1.25rem 0; padding: 1rem 1.25rem; border-radius: 12px; font-size: 0.95rem; font-weight: 500;
  display: flex; align-items: center; gap: 0.75rem; animation: slideIn 0.3s ease-out;
}
.alert-success { background: rgba(52,211,153,0.15); border: 1px solid rgba(52,211,153,0.3); color: #22c55e; }
.alert-error { background: rgba(239,68,68,0.15); border: 1px solid rgba(239,68,68,0.3); color: #fecaca; }

@keyframes slideIn { from { opacity:0; transform:translateX(-10px) } to { opacity:1; transform:translateX(0) } }

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
}
</style>

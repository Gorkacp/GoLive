<template>
  <div class="login-container">
    <div class="login-card">
      <h2>Iniciar sesión</h2>
      <p class="subtitle">¿Ya tienes una cuenta?</p>

      <form @submit.prevent="loginUser" class="login-form">
        <div class="input-group">
          <input type="email" v-model="email" placeholder="Correo electrónico" required />
        </div>
        <div class="input-group password-group">
          <input type="password" v-model="password" placeholder="Contraseña" required />
        </div>

        <button type="submit" :disabled="loading">
          {{ loading ? 'Iniciando...' : 'Entrar' }}
        </button>

        <div class="options">
          <label><input type="checkbox" /> Recordarme</label>
          <a href="#" class="forgot-password">¿Olvidaste tu contraseña?</a>
        </div>
      </form>

      <div v-if="errorMessage" class="alert alert-error">
        {{ errorMessage }}
      </div>


      <hr />

      <p class="register-text">
        ¿No tienes cuenta? <NuxtLink to="/register">Regístrate</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { navigateTo } from '#app'

const email = ref('')
const password = ref('')
const remember = ref(false)
const errorMessage = ref('')
const loading = ref(false)

// Cargar los datos guardados si el usuario eligió "Recordarme"
onMounted(() => {
  const savedEmail = localStorage.getItem('rememberedEmail')
  const savedPassword = localStorage.getItem('rememberedPassword')
  if (savedEmail && savedPassword) {
    email.value = savedEmail
    password.value = savedPassword
    remember.value = true
  }
})

const loginUser = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await fetch('http://localhost:8085/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    })

    const text = await response.text()
    let data
    try {
      data = JSON.parse(text)
    } catch (e) {
      errorMessage.value = 'Respuesta inválida del servidor.'
      return
    }

    if (response.ok) {
      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify({
        email: data.email,
        name: data.name,
        role: data.role
      }))

      // Guardar o eliminar los datos según el checkbox
      if (remember.value) {
        localStorage.setItem('rememberedEmail', email.value)
        localStorage.setItem('rememberedPassword', password.value)
      } else {
        localStorage.removeItem('rememberedEmail')
        localStorage.removeItem('rememberedPassword')
      }

      // Redirigir sin mostrar mensaje
      navigateTo('/')
    } else {
      errorMessage.value = data.message || 'Error en el inicio de sesión.'
    }
  } catch (error) {
    errorMessage.value = 'No se pudo conectar al servidor.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Importa Google Fonts */
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');

.login-container {
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1501281668745-f7f57925c3b4?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8Zm9uZG8lMjBkZSUyMHBhbnRhbGxhJTIwZGUlMjBldmVudG9zfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000');
  background-size: cover;
  background-position: center;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: 'Montserrat', sans-serif;
  overflow: hidden;
}

/* Capa oscura sobre la imagen de fondo */
.login-container::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.55); /* Ajusta opacidad según necesites */
  z-index: 0;
}

/* La tarjeta se mantiene por encima del overlay */
.login-card {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  border-radius: 20px;
  padding: 45px;
  width: 100%;
  max-width: 400px;
  color: #fff;
  text-align: center;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.3);
}

h2 {
  margin-bottom: 15px;
  font-size: 28px;
  font-weight: 700;
  color: #ffffff;
}

.subtitle {
  font-size: 16px;
  margin-bottom: 25px;
  font-weight: 500;
  color: #ffffff;
}

.input-group input {
  width: 100%;
  padding: 14px;
  border-radius: 25px;
  border: none;
  outline: none;
  margin-bottom: 18px;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.input-group input::placeholder {
  color: #f0f0f0;
  font-weight: 500;
}

button {
  width: 100%;
  background-color: #ffb6a1;
  border: none;
  color: #222;
  font-weight: 700;
  border-radius: 25px;
  padding: 14px;
  font-size: 16px;
  cursor: pointer;
  transition: 0.3s;
}

button:hover {
  background-color: #ffc1b0;
  transform: scale(1.03);
}

.options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  margin-top: 15px;
  color: #fff;
  font-weight: 600;
}

.options input {
  margin-right: 5px;
}

.options a {
  color: #ffffff;
  text-decoration: none;
  font-weight: 600;
}

.options a:hover {
  text-decoration: underline;
}

.alert {
  margin-top: 15px;
  padding: 10px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
}

.alert-error {
  background: rgba(255, 100, 100, 0.25);
}

.alert-success {
  background: rgba(100, 255, 100, 0.25);
}

.register-text {
  margin-top: 20px;
  font-size: 15px;
  color: #ffffff;
  font-weight: 600;
}
</style>

<template>
  <div class="container d-flex justify-content-center align-items-center vh-100">
    <div class="card p-4 shadow" style="width: 400px;">
      <h3 class="text-center mb-4">Iniciar Sesión</h3>
      <form @submit.prevent="loginEmail" class="d-flex flex-column gap-3">
        <input v-model="email" type="email" class="form-control" placeholder="Correo electrónico" required />
        <input v-model="password" type="password" class="form-control" placeholder="Contraseña" required />
        <button type="submit" class="btn btn-primary w-100">Entrar</button>
      </form>
      <hr />
      <button @click="loginGoogle" class="btn btn-danger w-100">
        <i class="bi bi-google me-2"></i> Iniciar sesión con Google
      </button>
      <p class="mt-3 text-center">
        ¿No tienes cuenta?
        <a href="/register">Regístrate aquí</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useNuxtApp } from '#app'
import { signInWithEmailAndPassword, signInWithPopup } from 'firebase/auth'

const router = useRouter()
const nuxtApp = useNuxtApp()

const auth = nuxtApp.$auth
const googleProvider = nuxtApp.$googleProvider

const email = ref('')
const password = ref('')

// Iniciar sesión con email/password
const loginEmail = async () => {
  try {
    await signInWithEmailAndPassword(auth, email.value, password.value)
    router.push('/') // redirige a inicio
  } catch (err) {
    console.error(err)
    // opcional: mostrar mensaje de error en el formulario
  }
}

// Iniciar sesión con Google
const loginGoogle = async () => {
  try {
    await signInWithPopup(auth, googleProvider)
    router.push('/') // redirige a inicio
  } catch (err) {
    console.error(err)
    // opcional: mostrar mensaje de error en el formulario
  }
}
</script>

<style scoped>
/* Opcional: ajustar altura de input y botones */
input.form-control {
  height: 45px;
}
button.btn {
  height: 45px;
}
</style>

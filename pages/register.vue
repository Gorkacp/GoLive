<template>
  <div class="container d-flex justify-content-center align-items-center vh-100">
    <div class="card p-4 shadow" style="max-width: 400px; width: 100%;">
      <h3 class="text-center text-primary mb-4">Registrarse</h3>

      <form @submit.prevent="registerUser" class="d-flex flex-column gap-3">
        <input type="text" v-model="name" placeholder="Nombre completo" class="form-control" required />
        <input type="email" v-model="email" placeholder="Correo electrónico" class="form-control" required />
        <input type="password" v-model="password" placeholder="Contraseña" class="form-control" required />
        <button type="submit" class="btn btn-primary">Registrarse</button>
      </form>

      <!-- Mostrar errores -->
      <div v-if="errorMessage" class="alert alert-danger mt-3">
        {{ errorMessage }}
      </div>

      <hr>

      <button @click="registerWithGoogle" class="btn btn-outline-danger w-100">
        <i class="bi bi-google"></i> Registrarse con Google
      </button>

      <p class="mt-3 text-center">
        ¿Ya tienes cuenta? <a href="/login">Iniciar sesión</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNuxtApp } from '#app'
import { createUserWithEmailAndPassword, signInWithPopup } from 'firebase/auth'
import { doc, setDoc } from 'firebase/firestore'

const { $auth, $db, $googleProvider } = useNuxtApp()

const name = ref('')
const email = ref('')
const password = ref('')
const errorMessage = ref('') // Para mostrar errores al usuario

const registerUser = async () => {
  errorMessage.value = '' // limpiar mensaje previo
  try {
    const userCredential = await createUserWithEmailAndPassword($auth, email.value, password.value)
    const user = userCredential.user

    // Redirigir inmediatamente al login
    window.location.href = '/login'

    // Guardar datos adicionales en Firestore (sin bloquear la redirección)
    setDoc(doc($db, 'users', user.uid), {
      name: name.value,
      email: email.value,
      role: 'user',
      purchases: []
    }).catch(err => console.error('Error guardando usuario en Firestore:', err))
    
  } catch (error) {
    // Mostrar mensaje de error al usuario
    if (error.code === 'auth/email-already-in-use') {
      errorMessage.value = 'El correo ya está en uso.'
    } else if (error.code === 'auth/invalid-email') {
      errorMessage.value = 'El correo no es válido.'
    } else if (error.code === 'auth/weak-password') {
      errorMessage.value = 'La contraseña debe tener al menos 6 caracteres.'
    } else {
      errorMessage.value = error.message
    }
  }
}


const registerWithGoogle = async () => {
  errorMessage.value = '' // limpiar mensaje previo
  try {
    const result = await signInWithPopup($auth, $googleProvider)
    const user = result.user
    const userRef = doc($db, 'users', user.uid)
    await setDoc(userRef, {
      name: user.displayName,
      email: user.email,
      role: 'user',
      purchases: []
    }, { merge: true })
    window.location.href = '/' // redirige automáticamente a inicio
  } catch (error) {
    errorMessage.value = error.message
  }
}
</script>

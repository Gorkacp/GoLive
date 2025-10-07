<template>
  <nav class="navbar navbar-custom fixed-top">
    <div class="container-fluid">
      <!-- Título animado -->
      <a class="navbar-brand text-light animated-title" href="/">GoLive</a>

      <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas" data-bs-target="#offcanvasNavbar" aria-controls="offcanvasNavbar" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>

      <div class="offcanvas offcanvas-end" tabindex="-1" id="offcanvasNavbar" aria-labelledby="offcanvasNavbarLabel">
        <div class="offcanvas-header">
          <h5 class="offcanvas-title" id="offcanvasNavbarLabel">Menú</h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>

        <div class="offcanvas-body d-flex flex-column justify-content-between h-100">
          <!-- Menú principal -->
          <div>
            <ul class="navbar-nav justify-content-end flex-grow-1 pe-3">
              <li class="nav-item">
                <a class="nav-link text-light active" aria-current="page" href="/conciertos">Conciertos</a>
              </li>
              <li class="nav-item">
                <a class="nav-link text-light" href="#">Festivales</a>
              </li>
              <li class="nav-item">
                <a class="nav-link text-light" href="#">Blog</a>
              </li>
              <li class="nav-item" v-if="userRole === 'admin'">
                <a class="nav-link text-success" href="#">Crear Evento</a>
              </li>
            </ul>

            <!-- Barra de búsqueda: solo en index, conciertos o festivales -->
            <form v-if="showSearch" class="d-flex mt-3" role="search" @submit.prevent="buscar">
              <input
                v-model="busqueda"
                class="form-control me-2"
                type="search"
                placeholder="Buscar"
                aria-label="Buscar"
                @input="buscar"
              />
              <button class="btn btn-outline-success" type="submit">Buscar</button>
            </form>
          </div>

          <!-- Login / Logout -->
          <div class="mt-4 border-top border-secondary pt-3">
            <template v-if="userLogged">
              <button @click="logout" class="btn btn-danger w-100">Cerrar sesión</button>
            </template>
            <template v-else>
              <a href="/login" class="btn btn-primary w-100 mb-2">Iniciar sesión</a>
              <a href="/register" class="btn btn-outline-primary w-100">Registrarse</a>
            </template>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useNuxtApp, useRoute } from '#app'
import { doc, getDoc } from 'firebase/firestore'
import { signOut, onAuthStateChanged } from 'firebase/auth'

const route = useRoute()

// Mostrar búsqueda solo en estas rutas
const showSearch = computed(() => ['/', '/conciertos', '/festivales'].includes(route.path))

// Para emitir el evento de búsqueda
const busqueda = ref('')
const emit = defineEmits(['buscar-evento'])
const buscar = () => emit('buscar-evento', busqueda.value)

const { $db, $auth } = useNuxtApp()
const userLogged = ref(false)
const userRole = ref(null)

const loadUser = async (user) => {
  if (user) {
    userLogged.value = true
    const userRef = doc($db, 'users', user.uid)
    const userSnap = await getDoc(userRef)
    if (userSnap.exists()) userRole.value = userSnap.data().role
    else userRole.value = null
  } else {
    userLogged.value = false
    userRole.value = null
  }
}

onMounted(() => {
  onAuthStateChanged($auth, (user) => {
    loadUser(user)
  })
})

const logout = async () => {
  await signOut($auth)
  userLogged.value = false
  userRole.value = null
  window.location.href = '/'
}
</script>

<style scoped>
/* Estilos existentes ... */
.navbar-custom { background-color: #0a0a0a; }
.animated-title { font-size: 2.5rem; font-weight: 900; text-transform: uppercase; letter-spacing: 2px; color: #ff0057; text-shadow: 2px 2px 8px rgba(0,0,0,0.7); animation: glow 1.5s ease-in-out infinite alternate, scaleTitle 2s ease-in-out infinite alternate; }
@keyframes glow { 0% { text-shadow: 2px 2px 8px rgba(255,0,87,0.6); } 50% { text-shadow: 2px 2px 15px rgba(255,0,87,0.9); } 100% { text-shadow: 2px 2px 8px rgba(255,0,87,0.6); } }
@keyframes scaleTitle { 0% { transform: scale(1); } 50% { transform: scale(1.05); } 100% { transform: scale(1); } }
.navbar-toggler { border-color: rgba(255, 255, 255, 0.1); }
.navbar-toggler-icon { background-image: url("data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba(255, 255, 255, 0.85)' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 7h22M4 15h22M4 23h22'/%3E%3C/svg%3E"); }
.offcanvas { background-color: #0a0a0a; }
.offcanvas-title { color: #ffffff; }
.offcanvas .nav-link { color: #ffffff !important; }
.offcanvas .nav-link:hover { color: #ff0057 !important; }
.btn-close-white { filter: invert(1); }
.offcanvas-header { border-bottom: 1px solid rgba(255,255,255,0.1); }
</style>

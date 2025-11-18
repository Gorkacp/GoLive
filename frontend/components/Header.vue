<template>
  <nav class="navbar navbar-custom fixed-top">
    <div class="container-fluid">
      <!-- Título animado -->
      <NuxtLink class="navbar-brand text-light animated-title" to="/">GoLive</NuxtLink>

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
                <NuxtLink class="nav-link text-light" :class="{ active: $route.path === '/conciertos' }" to="/conciertos">{{ $t('Conciertos') }}</NuxtLink>
              </li>
              <li class="nav-item">
                <NuxtLink class="nav-link text-light" to="/festivales">{{ $t('Festivales') }}</NuxtLink>
              </li>
              <li class="nav-item">
                <NuxtLink class="nav-link text-light" to="/blog">{{ $t('Blog') }}</NuxtLink>
              </li>

              <li class="nav-item" v-if="userRole === 'admin' || userRole === 'super_user'">
                <NuxtLink class="nav-link text-success" to="/crearEvento">
                  <i class="fas fa-plus-circle me-1"></i>{{ $t('Crear Evento') }}
                </NuxtLink>
              </li>

              <li class="nav-item" v-if="userRole === 'super_user'">
                <NuxtLink class="nav-link text-warning" to="/oficina">
                  <i class="fas fa-cog me-1"></i>{{ $t('Oficina') }}
                </NuxtLink>
              </li>

              <li class="nav-item" v-if="userLogged">
                <NuxtLink class="nav-link text-light" to="/misEntradas">
                  <i class="fas fa-ticket-alt me-1"></i>{{ $t('Mis Entradas') }}
                </NuxtLink>
              </li>

              <li class="nav-item" v-if="userLogged">
                <NuxtLink class="nav-link text-light" to="/perfil">
                  <i class="fas fa-user-circle me-1"></i>{{ $t('Perfil') }}
                </NuxtLink>
              </li>
            </ul>
          </div>

          <!-- Login / Logout -->
          <div class="mt-4 border-top border-secondary pt-3">
            <template v-if="userLogged">
              <div class="text-light mb-3">
                <strong>Hola, {{ userData?.name }}</strong><br>
                <small>{{ userData?.email }}</small><br>

                <small class="badge" :class="getRoleBadgeClass(userRole)">
                  {{ userRole }}
                </small>
              </div>
              <button @click="logout" class="btn btn-danger w-100">
                <i class="fas fa-sign-out-alt me-1"></i>{{ $t('Cerrar sesión') }}
              </button>
            </template>

            <template v-else>
              <NuxtLink to="/login" class="btn btn-primary w-100 mb-2">
                <i class="fas fa-sign-in-alt me-1"></i>{{ $t('Iniciar sesión') }}
              </NuxtLink>
              <NuxtLink to="/register" class="btn btn-outline-primary w-100">
                <i class="fas fa-user-plus me-1"></i>{{ $t('Registrarse') }}
              </NuxtLink>
            </template>
          </div>

          <!-- Selector de idioma -->
          <div class="mt-3 border-top border-secondary pt-3 d-flex justify-content-around">
            <span class="fi fi-es" title="Español" @click="setLanguage('es')"></span>
            <span class="fi fi-gb" title="English" @click="setLanguage('en')"></span>
            <span class="fi fi-pt" title="Português" @click="setLanguage('pt')"></span>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
const route = useRoute()

const userLogged = ref(false)
const userRole = ref(null)
const userData = ref(null)
const { isAuthenticated, clearToken } = useAuth()

const getRoleBadgeClass = (role) => {
  switch (role) {
    case 'super_user': return 'bg-warning text-dark'
    case 'admin': return 'bg-success'
    default: return 'bg-secondary'
  }
}

const loadUserFromStorage = () => {
  if (process.client) {
    const userStr = sessionStorage.getItem('user')
    
    if (userStr) {
      try {
        userData.value = JSON.parse(userStr)
        userLogged.value = true
        userRole.value = userData.value.role
      } catch (error) {
        logout()
      }
    } else {
      userLogged.value = false
      userRole.value = null
      userData.value = null
    }
  }
}

onMounted(() => {
  loadUserFromStorage()
  if (process.client) {
    window.addEventListener('storage', loadUserFromStorage)
    
    // Permitir scroll dentro del offcanvas pero no en el body
    const offcanvasElement = document.getElementById('offcanvasNavbar')
    if (offcanvasElement) {
      offcanvasElement.addEventListener('show.bs.offcanvas', () => {
        document.documentElement.style.overflow = 'hidden'
        document.body.style.overflow = 'hidden'
      })
      offcanvasElement.addEventListener('hide.bs.offcanvas', () => {
        document.documentElement.style.overflow = ''
        document.body.style.overflow = ''
      })
    }
  }
})

onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('storage', loadUserFromStorage)
    // Limpiar estilos cuando se desmonta el componente
    document.documentElement.style.overflow = ''
    document.body.style.overflow = ''
  }
})

watch(() => route.path, () => {
  loadUserFromStorage()
  // Limpiar estilos de overflow al cambiar de ruta y cerrar offcanvas
  if (process.client) {
    document.documentElement.style.overflow = ''
    document.body.style.overflow = ''
    // Cerrar offcanvas si está abierto
    const offcanvasElement = document.getElementById('offcanvasNavbar')
    if (offcanvasElement) {
      const offcanvasInstance = bootstrap.Offcanvas.getInstance(offcanvasElement)
      if (offcanvasInstance) {
        offcanvasInstance.hide()
      }
    }
  }
})

const logout = () => {
  if (process.client) {
    clearToken()
    sessionStorage.removeItem('user')
    userLogged.value = false
    userRole.value = null
    userData.value = null
    // Limpiar overflow antes de navegar
    document.documentElement.style.overflow = ''
    document.body.style.overflow = ''
    navigateTo('/')
  }
}

const setLanguage = (lang) => {
  if (process.client) {
    localStorage.setItem('lang', lang)
    window.location.reload()
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.navbar-custom { 
  background-color: #0a0a0a; 
  font-family: 'Poppins', sans-serif;
}

.animated-title { 
  font-size: 2.5rem; 
  font-weight: 700; 
  text-transform: uppercase; 
  letter-spacing: 2px; 
  color: #ff0057; 
  text-shadow: 2px 2px 8px rgba(0,0,0,0.7); 
  animation: glow 1.5s ease-in-out infinite alternate, scaleTitle 2s ease-in-out infinite alternate; 
  text-decoration: none;
}

@keyframes glow { 
  0% { text-shadow: 0 0 8px rgba(255,0,87,0.6); } 
  50% { text-shadow: 0 0 15px rgba(255,0,87,0.9); } 
  100% { text-shadow: 0 0 8px rgba(255,0,87,0.6); } 
}

@keyframes scaleTitle { 
  0% { transform: scale(1); } 
  50% { transform: scale(1.05); } 
  100% { transform: scale(1); } 
}

/* Previene que offcanvas cause scroll horizontal */
:global(body.offcanvas-open),
:global(body.offcanvas-opening) {
  overflow-x: hidden !important;
  max-width: 100vw !important;
}

.navbar-toggler { 
  border-color: rgba(255, 255, 255, 0.1); 
}

.navbar-toggler-icon { 
  background-image: url("data:image/svg+xml;charset=utf8,%3Csvg viewBox='0 0 30 30' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath stroke='rgba(255, 255, 255, 0.85)' stroke-width='2' stroke-linecap='round' stroke-miterlimit='10' d='M4 7h22M4 15h22M4 23h22'/%3E%3C/svg%3E"); 
}

.offcanvas { 
  background-color: #0a0a0a; 
  font-family: 'Poppins', sans-serif;
  max-width: 100vw !important;
}

.offcanvas-title { 
  color: #ffffff; 
  font-weight: 600;
}

.offcanvas .nav-link { 
  color: #ffffff !important; 
  text-decoration: none;
  transition: all 0.3s ease;
  font-weight: 500;
}

.offcanvas .nav-link:hover,
.offcanvas .nav-link.active { 
  color: #ff0057 !important; 
  transform: translateX(5px);
}

.offcanvas .nav-link.text-warning:hover {
  color: #ffc107 !important;
}

.offcanvas .nav-link.text-success:hover {
  color: #198754 !important;
}

.btn-close-white { 
  filter: invert(1); 
}

.offcanvas-header { 
  border-bottom: 1px solid rgba(255,255,255,0.1); 
}

/* Flags */
.fi { 
  font-size: 1.8rem; 
  cursor: pointer; 
  transition: transform 0.2s ease; 
}

.fi:hover { 
  transform: scale(1.3); 
}

/* Badges y iconos */
.badge {
  font-size: 0.7rem;
  margin-top: 0.2rem;
  font-weight: normal;
}

.nav-link i {
  width: 16px;
  text-align: center;
}
</style>

<template>
  <nav class="navbar navbar-custom fixed-top">
    <div class="container-fluid">
      <!-- Desktop: Logo a la izquierda -->
      <NuxtLink class="navbar-brand text-light animated-title desktop-logo" to="/">GoLive</NuxtLink>

      <!-- Mobile: Menú hamburguesa a la izquierda -->
      <button 
        class="navbar-toggler mobile-toggle" 
        type="button" 
        data-bs-toggle="offcanvas" 
        data-bs-target="#offcanvasNavbar" 
        aria-controls="offcanvasNavbar" 
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>

      <!-- Mobile: Logo en el centro -->
      <NuxtLink class="navbar-brand text-light animated-title mobile-logo" to="/">GoLive</NuxtLink>

      <!-- Desktop: Enlaces de navegación -->
      <div class="desktop-nav-links">
        <NuxtLink 
          class="nav-link-desktop" 
          :class="{ active: $route.path === '/' }" 
          to="/"
        >
          {{ $t('Inicio') }}
        </NuxtLink>
        <NuxtLink 
          class="nav-link-desktop" 
          :class="{ active: $route.path === '/conciertos' }" 
          to="/conciertos"
        >
          {{ $t('Conciertos') }}
        </NuxtLink>
        <NuxtLink 
          class="nav-link-desktop" 
          :class="{ active: $route.path === '/festivales' }" 
          to="/festivales"
        >
          {{ $t('Festivales') }}
        </NuxtLink>
      </div>

      <!-- Menú de perfil (Desktop y Mobile) -->
      <div class="profile-menu-wrapper" ref="profileDropdownRef" v-if="!loadingUser">
        <div 
          v-if="userLogged" 
          class="profile-dropdown"
          @click="toggleProfileMenu"
        >
          <div class="profile-avatar">
            <img 
              v-if="userData?.profilePhoto && userData.profilePhoto.trim() !== ''" 
              :src="userData.profilePhoto" 
              alt="Perfil"
              class="avatar-img"
              @error="handleImageError"
            />
            <i v-else class="bi bi-person-circle avatar-icon"></i>
          </div>
          <i class="bi bi-chevron-down dropdown-arrow" :class="{ rotated: showProfileMenu }"></i>
        </div>
        <NuxtLink 
          v-else 
          to="/login" 
          class="login-btn-desktop"
        >
          <i class="bi bi-box-arrow-in-right me-1"></i>{{ $t('Iniciar sesión') }}
        </NuxtLink>

        <!-- Menú desplegable del perfil -->
        <div 
          v-if="userLogged && showProfileMenu" 
          class="profile-dropdown-menu"
          @click.stop
        >
          <NuxtLink 
            to="/perfil" 
            class="dropdown-item-profile"
            @click="closeProfileMenu"
          >
            <i class="bi bi-person me-2"></i>{{ $t('Mi perfil') }}
          </NuxtLink>
          <NuxtLink 
            to="/misEntradas" 
            class="dropdown-item-profile"
            @click="closeProfileMenu"
          >
            <i class="bi bi-ticket-perforated me-2"></i>{{ $t('Mis entradas') }}
          </NuxtLink>
          <NuxtLink 
            v-if="userRole === 'admin' || userRole === 'super_user'"
            to="/backoffice" 
            class="dropdown-item-profile"
            @click="closeProfileMenu"
          >
            <i class="bi bi-gear me-2"></i>{{ $t('Backoffice') }}
          </NuxtLink>
          <NuxtLink 
            v-if="userRole === 'super_user'"
            to="/oficina" 
            class="dropdown-item-profile"
            @click="closeProfileMenu"
          >
            <i class="bi bi-building me-2"></i>{{ $t('Oficina') }}
          </NuxtLink>
          <div class="dropdown-divider"></div>
          <button 
            @click="handleLogout" 
            class="dropdown-item-profile logout-item"
          >
            <i class="bi bi-box-arrow-right me-2"></i>{{ $t('Cerrar sesión') }}
          </button>
        </div>
      </div>

      <!-- Offcanvas para móvil -->
      <div class="offcanvas offcanvas-start" tabindex="-1" id="offcanvasNavbar" aria-labelledby="offcanvasNavbarLabel">
        <div class="offcanvas-header">
          <h5 class="offcanvas-title" id="offcanvasNavbarLabel">Menú</h5>
          <button type="button" class="btn-close btn-close-white" data-bs-dismiss="offcanvas" aria-label="Close"></button>
        </div>

        <div class="offcanvas-body d-flex flex-column justify-content-between h-100">
          <!-- Menú principal -->
          <div>
            <ul class="navbar-nav justify-content-end flex-grow-1 pe-3">
              <li class="nav-item">
                <NuxtLink class="nav-link text-light" :class="{ active: $route.path === '/' }" to="/">{{ $t('Inicio') }}</NuxtLink>
              </li>
              <li class="nav-item">
                <NuxtLink class="nav-link text-light" :class="{ active: $route.path === '/conciertos' }" to="/conciertos">{{ $t('Conciertos') }}</NuxtLink>
              </li>
              <li class="nav-item">
                <NuxtLink class="nav-link text-light" :class="{ active: $route.path === '/festivales' }" to="/festivales">{{ $t('Festivales') }}</NuxtLink>
              </li>
            </ul>
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
const showProfileMenu = ref(false)
const profileDropdownRef = ref(null)
const loadingUser = ref(true)
const { isAuthenticated, clearToken, getCurrentUser } = useAuth()

const getRoleBadgeClass = (role) => {
  switch (role) {
    case 'super_user': return 'bg-warning text-dark'
    case 'admin': return 'bg-success'
    default: return 'bg-secondary'
  }
}

const loadUserFromAuth = async () => {
  if (!process.client) {
    loadingUser.value = false
    return
  }

  loadingUser.value = true

  try {
    if (!isAuthenticated.value) {
      userLogged.value = false
      userRole.value = null
      userData.value = null
      loadingUser.value = false
      return
    }

    const user = await getCurrentUser()
    userData.value = user
    userLogged.value = true
    userRole.value = user?.role || null
  } catch {
    clearToken()
    userLogged.value = false
    userRole.value = null
    userData.value = null
  } finally {
    loadingUser.value = false
  }
}

const toggleProfileMenu = () => {
  showProfileMenu.value = !showProfileMenu.value
}

const closeProfileMenu = () => {
  showProfileMenu.value = false
}

const handleLogout = () => {
  closeProfileMenu()
  logout()
}

// Manejar errores al cargar la imagen
const handleImageError = (event) => {
  // Si la imagen falla al cargar, ocultarla y mostrar el icono por defecto
  if (event.target) {
    event.target.style.display = 'none'
  }
}

// Cerrar menú al hacer clic fuera
const handleClickOutside = (event) => {
  if (process.client && profileDropdownRef.value && !profileDropdownRef.value.contains(event.target)) {
    showProfileMenu.value = false
  }
}

const restoreScroll = () => {
  if (process.client) {
    // Restaurar scroll
    document.documentElement.style.overflow = ''
    document.body.style.overflow = ''
    document.documentElement.classList.remove('offcanvas-open')
    document.body.classList.remove('offcanvas-open')
    document.body.classList.remove('modal-open')
    // Eliminar cualquier padding que Bootstrap pueda haber agregado
    document.body.style.paddingRight = ''
    document.documentElement.style.paddingRight = ''
  }
}

onMounted(() => {
  loadUserFromAuth()
  if (process.client) {
    // Permitir scroll dentro del offcanvas pero no en el body
    const offcanvasElement = document.getElementById('offcanvasNavbar')
    if (offcanvasElement) {
      offcanvasElement.addEventListener('show.bs.offcanvas', () => {
        document.documentElement.style.overflow = 'hidden'
        document.body.style.overflow = 'hidden'
      })
      offcanvasElement.addEventListener('hide.bs.offcanvas', () => {
        // Restaurar scroll cuando empieza a ocultarse
        restoreScroll()
      })
      offcanvasElement.addEventListener('hidden.bs.offcanvas', () => {
        // Asegurar que el scroll esté restaurado después de que se oculte completamente
        restoreScroll()
      })
    }
    
    // Escuchar clics fuera del menú de perfil
    document.addEventListener('click', handleClickOutside)
  }
})

onUnmounted(() => {
  if (process.client) {
    // Limpiar estilos cuando se desmonta el componente
    restoreScroll()
    document.removeEventListener('click', handleClickOutside)
  }
})

watch(() => route.path, () => {
  loadUserFromAuth()
  closeProfileMenu()
  // Limpiar estilos de overflow al cambiar de ruta y cerrar offcanvas
  if (process.client) {
    restoreScroll()
    // Cerrar offcanvas si está abierto
    const offcanvasElement = document.getElementById('offcanvasNavbar')
    if (offcanvasElement) {
      const offcanvasInstance = bootstrap.Offcanvas.getInstance(offcanvasElement)
      if (offcanvasInstance) {
        offcanvasInstance.hide()
      }
    }
    // Asegurar que el scroll esté restaurado después de un pequeño delay
    setTimeout(() => {
      restoreScroll()
    }, 100)
  }
})

const logout = () => {
  if (process.client) {
    clearToken()
    userLogged.value = false
    userRole.value = null
    userData.value = null
    // Limpiar overflow antes de navegar
    restoreScroll()
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
  padding: 0.75rem 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.navbar-custom .container-fluid {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  position: relative;
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
  transition: all 0.3s ease;
}

.animated-title:hover {
  color: #ff0057;
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

/* ============ DESKTOP STYLES ============ */
.desktop-logo {
  display: block;
  margin-right: auto;
}

.mobile-logo {
  display: none;
}

.mobile-toggle {
  display: none;
}

.desktop-nav-links {
  display: flex;
  align-items: center;
  gap: 2rem;
  margin-left: auto;
  margin-right: 2rem;
}

.nav-link-desktop {
  color: #ffffff;
  text-decoration: none;
  font-weight: 500;
  font-size: 1rem;
  padding: 0.5rem 0;
  position: relative;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
}

.nav-link-desktop::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  transition: width 0.3s ease;
}

.nav-link-desktop:hover::after,
.nav-link-desktop.active::after {
  width: 100%;
}

.nav-link-desktop:hover {
  color: #ff0057;
  text-decoration: none;
}

.nav-link-desktop.active {
  color: #ff0057;
}

/* ============ PROFILE MENU ============ */
.profile-menu-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.profile-dropdown {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50px;
  transition: all 0.3s ease;
  position: relative;
}

.profile-dropdown:hover {
  background: rgba(255, 255, 255, 0.1);
}

.profile-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border: none;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-icon {
  font-size: 2rem;
  color: #ff0057;
}

.dropdown-arrow {
  color: #ffffff;
  font-size: 0.8rem;
  transition: transform 0.3s ease;
}

.dropdown-arrow.rotated {
  transform: rotate(180deg);
}

.profile-dropdown-menu {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  background: #1a1a1a;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  min-width: 220px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  z-index: 1000;
  overflow: hidden;
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item-profile {
  display: flex;
  align-items: center;
  padding: 0.75rem 1.25rem;
  color: #ffffff;
  text-decoration: none;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  background: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.dropdown-item-profile:hover {
  background: rgba(255, 0, 87, 0.1);
  color: #ff0057;
  text-decoration: none;
}

.dropdown-item-profile i {
  font-size: 1.1rem;
  width: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.dropdown-item-profile.logout-item {
  color: #ff6b6b;
}

.dropdown-item-profile.logout-item:hover {
  background: rgba(255, 107, 107, 0.1);
  color: #ff5252;
}

.dropdown-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0.5rem 0;
}

.login-btn-desktop {
  display: inline-flex;
  align-items: center;
  padding: 0.6rem 1.25rem;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  text-decoration: none;
  border-radius: 25px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  border: none;
}

.login-btn-desktop:hover {
  background: linear-gradient(135deg, #ff1a6b 0%, #ff7b45 100%);
  color: #ffffff;
  text-decoration: none;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.4);
}

/* ============ MOBILE STYLES ============ */
@media (max-width: 768px) {
  .desktop-logo {
    display: none;
  }

  .mobile-logo {
    display: block;
    font-size: 1.8rem;
    position: static;
    z-index: 1;
    width: auto;
    pointer-events: auto;
    white-space: nowrap;
    text-align: center;
  }

  .mobile-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: auto;
    margin-left: 0;
    padding: 0.25rem 0.5rem;
    z-index: 2;
    position: relative;
    min-width: 44px;
    width: 44px;
  }

  .desktop-nav-links {
    display: none;
  }

  .navbar-custom {
    padding: 0.75rem 0.5rem 0.75rem 0.25rem;
    position: relative;
  }

  .navbar-custom .container-fluid {
    display: grid;
    grid-template-columns: 44px 1fr 44px;
    align-items: center;
    position: relative;
    width: 100%;
    gap: 0.5rem;
  }
  
  .navbar-custom .container-fluid > * {
    flex-shrink: 0;
  }
  
  .desktop-logo {
    display: none !important;
  }
  
  .mobile-toggle {
    grid-column: 1;
    justify-self: start;
  }
  
  .mobile-logo {
    grid-column: 2;
    justify-self: center;
    position: static;
    transform: none;
    left: auto;
    margin: 0 auto;
  }
  
  .profile-menu-wrapper {
    grid-column: 3;
    justify-self: end;
    position: relative;
    z-index: 2;
    min-width: 44px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }

  .profile-menu-wrapper {
    z-index: 2;
  }

  .profile-avatar {
    width: 36px;
    height: 36px;
  }

  .avatar-icon {
    font-size: 1.75rem;
  }

  .profile-dropdown-menu {
    right: 0;
    min-width: 200px;
  }

  .login-btn-desktop {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }
}

/* ============ OFFCANVAS STYLES ============ */
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

/* ============ BOTONES GRADIENTE ============ */
.btn-gradient {
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  border: none;
  color: #fff;
  font-weight: 600;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.95rem;
}

.btn-gradient:hover {
  background: linear-gradient(135deg, #7a0030, #8f3410);
  color: #fff;
  text-decoration: none;
}

.btn-gradient:active {
  transform: scale(0.98);
}

.btn-gradient-outline {
  background: transparent;
  border: 2px solid;
  border-image: linear-gradient(135deg, #8b0035 0%, #a03a14 100%) 1;
  color: #fff;
  font-weight: 600;
  padding: 0.55rem 1.5rem;
  border-radius: 10px;
  transition: all 0.3s ease;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.btn-gradient-outline:hover {
  background: linear-gradient(135deg, rgba(139, 0, 53, 0.1) 0%, rgba(160, 58, 20, 0.1) 100%);
  border-image: linear-gradient(135deg, #a03a14 0%, #8b0035 100%) 1;
  color: #fff;
  text-decoration: none;
}

.btn-gradient-outline:active {
  transform: scale(0.98);
}
</style>

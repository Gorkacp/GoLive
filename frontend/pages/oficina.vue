<template>
  <client-only>
    <Header @buscar-evento="filtrarEventos" />
  </client-only>

  <div class="container-fluid py-4 mt-6 oficina-container">
    <!-- Mensaje de éxito -->
    <transition name="slide-fade">
      <div v-if="successMessage" class="alert alert-success alert-success-toast" role="alert">
        <i class="fas fa-check-circle me-2"></i>{{ successMessage }}
      </div>
    </transition>

    <!-- Cabecera y estadísticas -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
          <h1 class="text-light">
            <i class="fas fa-cog me-3 text-primary"></i>
            Panel de Administración
          </h1>
          <button class="btn btn-primary btn-lg" @click="openAddUserModal">
            <i class="fas fa-plus me-2"></i>Agregar Usuario
          </button>
        </div>
        <p class="text-muted">Gestión completa de usuarios y permisos del sistema</p>
      </div>
    </div>

    <!-- Tarjetas de estadísticas -->
    <div class="stats-container mb-4">
      <div class="stat-card" v-for="(stat, index) in statsArray" :key="index">
        <div class="stat-card-content">
          <div class="stat-info">
            <h6 class="stat-title">{{ stat.title }}</h6>
            <h3 :class="['stat-value', stat.colorClass]">{{ stat.value }}</h3>
          </div>
          <div class="stat-icon">
            <i :class="stat.iconClass"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabla de usuarios -->
    <div class="row">
      <div class="col-12">
        <div class="card card-custom card-users users-card-container">
          <!-- Header Unificado CON Tabla Móvil -->
          <div class="users-section-header users-header-integrated">
            <div class="users-header-content">
              <h2 class="users-section-title">
                <i class="fas fa-users me-3"></i>Lista de Usuarios Registrados
              </h2>
              <div class="users-search-bar">
                <div class="search-box-users">
                  <i class="fas fa-search"></i>
                  <input 
                    v-model="searchTerm"
                    type="text"
                    class="search-input-users"
                    placeholder="Buscar por nombre, email o rol..."
                    @keydown.enter="loadUsers"
                  >
                </div>
                <button class="btn-search-users" @click="loadUsers" title="Buscar usuarios">
                  <i class="fas fa-arrow-right"></i>
                </button>
              </div>
            </div>

            <!-- Vista móvil (tabla horizontal compacta) - DENTRO DEL HEADER -->
            <div class="d-lg-none table-responsive-mobile-wrapper">
              <div class="table-mobile-header">
                <div class="table-mobile-col col-nombre">Nombre</div>
                <div class="table-mobile-col col-rol">Rol</div>
                <div class="table-mobile-col col-acciones">Acciones</div>
              </div>

              <div class="table-responsive-mobile">
                <div v-for="user in filteredUsers" :key="user._id" class="table-mobile-row">
                  <!-- Nombre -->
                  <div class="table-mobile-col col-nombre">
                    <div class="user-cell-content">
                      <div v-if="user.profilePhoto" class="avatar-image">
                        <img :src="user.profilePhoto" :alt="user.name" class="avatar-img">
                      </div>
                      <div v-else class="avatar-circle">{{ user.name?.charAt(0)?.toUpperCase() || 'U' }}</div>
                      <div class="user-info">
                        <div class="user-name">{{ user.name }}</div>
                      </div>
                    </div>
                  </div>

                  <!-- Rol -->
                  <div class="table-mobile-col col-rol">
                    <span class="badge" :class="getRoleBadgeClass(user.role)">{{ formatRole(user.role) }}</span>
                  </div>

                  <!-- Acciones -->
                  <div class="table-mobile-col col-acciones">
                    <div class="actions-container">
                      <button class="btn-action-icon btn-edit" @click="editUser(user)" title="Editar">
                        <i class="fas fa-edit"></i>
                      </button>
                      <button class="btn-action-icon btn-role" @click="changeUserRole(user)" title="Cambiar rol">
                        <i class="fas fa-user-cog"></i>
                      </button>
                      <button class="btn-action-icon btn-delete" @click="confirmDeleteUser(user)" title="Eliminar">
                        <i class="fas fa-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <div v-if="filteredUsers.length === 0" class="empty-state-users d-lg-none">
                <i class="fas fa-users"></i>
                <p><strong>No se encontraron usuarios</strong></p>
                <small>Intenta cambiar tus criterios de búsqueda</small>
              </div>

              <div class="users-table-footer d-lg-none">
                <div class="results-count">
                  <i class="fas fa-check-circle me-2"></i>
                  <span>Mostrando <strong>{{ filteredUsers.length }}</strong> de <strong>{{ users.length }}</strong> usuarios</span>
                </div>
                <div class="footer-info">
                  <i class="fas fa-shield-alt me-1"></i>
                  <span>Panel de administración seguro</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="loading" class="loading-users">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="text-muted mt-3">Cargando usuarios...</p>
          </div>

          <div v-else-if="error" class="alert alert-danger m-4 alert-users">
            <i class="fas fa-exclamation-triangle me-2"></i>
            {{ error }}
          </div>

          <div v-else class="card-body p-0">
            <!-- Vista de escritorio (tabla) -->
            <div class="table-responsive-users d-none d-lg-block">
              <table class="table-users-clean">
                <thead>
                  <tr>
                    <th>Nombre</th>
                    <th>Rol</th>
                    <th>Compras</th>
                    <th>Estado</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="user in filteredUsers" :key="user._id">
                    <td class="cell-nombre">
                      <div class="user-cell-content">
                        <div v-if="user.profilePhoto" class="avatar-image">
                          <img :src="user.profilePhoto" :alt="user.name" class="avatar-img">
                        </div>
                        <div v-else class="avatar-circle">{{ user.name?.charAt(0)?.toUpperCase() || 'U' }}</div>
                        <div class="user-info">
                          <div class="user-name">{{ user.name }}</div>
                          <small class="user-email">{{ user.email }}</small>
                        </div>
                      </div>
                    </td>
                    <td class="cell-rol"><span class="badge" :class="getRoleBadgeClass(user.role)">{{ formatRole(user.role) }}</span></td>
                    <td class="cell-compras"><span class="badge bg-secondary">{{ user.purchases?.length || 0 }}</span></td>
                    <td class="cell-estado"><span class="badge bg-success">Activo</span></td>
                    <td class="cell-acciones">
                      <div class="actions-container">
                        <button class="btn btn-sm btn-outline-primary" @click="editUser(user)">
                          <i class="fas fa-edit me-1"></i>Editar
                        </button>
                        <button class="btn btn-sm btn-outline-info" @click="changeUserRole(user)">
                          <i class="fas fa-user-cog me-1"></i>Rol
                        </button>
                        <button class="btn btn-sm btn-outline-danger" @click="confirmDeleteUser(user)">
                          <i class="fas fa-trash me-1"></i>Eliminar
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="filteredUsers.length === 0" class="empty-state-users d-none d-lg-block">
              <i class="fas fa-users"></i>
              <p><strong>No se encontraron usuarios</strong></p>
              <small>Intenta cambiar tus criterios de búsqueda</small>
            </div>

            <div class="users-table-footer users-table-footer-desktop d-none d-lg-block">
              <div class="results-count">
                <i class="fas fa-check-circle me-2"></i>
                <span>Mostrando <strong>{{ filteredUsers.length }}</strong> de <strong>{{ users.length }}</strong> usuarios</span>
              </div>
              <div class="footer-info">
                <i class="fas fa-shield-alt me-1"></i>
                <span>Panel de administración seguro</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modales -->
    <OfficeUserFormModal :editingUser="editingUser" @save="handleUserSaved" />
    <OfficeRoleChangeModal :user="userToChangeRole" @save="handleRoleChanged" />
    <OfficeDeleteConfirmModal :user="userToDelete" @delete="handleUserDeleted" />
  </div>
</template>

<script setup>
import { useHead } from '#app'

useHead({
  title: 'Oficina | GoLive',
  meta: [
    { name: 'description', content: 'Panel de administración de usuarios y permisos en GoLive. Gestiona roles, usuarios y accede a estadísticas avanzadas.' },
    { property: 'og:title', content: 'Oficina | GoLive' },
    { property: 'og:description', content: 'Panel de administración de usuarios y permisos en GoLive. Gestiona roles, usuarios y accede a estadísticas avanzadas.' },
    { property: 'og:type', content: 'website' },
    { property: 'og:url', content: 'https://golive-hu5d.onrender.com/oficina' },
    { property: 'og:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' },
    { name: 'twitter:card', content: 'summary_large_image' },
    { name: 'twitter:title', content: 'Oficina | GoLive' },
    { name: 'twitter:description', content: 'Panel de administración de usuarios y permisos en GoLive.' },
    { name: 'twitter:image', content: 'https://golive-hu5d.onrender.com/assets/img/1.jpg' }
  ],
  link: [
    { rel: 'canonical', href: 'https://golive-hu5d.onrender.com/oficina' }
  ]
})
definePageMeta({ middleware: 'super-user-only' })

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'http://localhost:8085'

const users = ref([])
const searchTerm = ref('')
const loading = ref(false)
const error = ref('')
const editingUser = ref(null)
const userToChangeRole = ref(null)
const userToDelete = ref(null)
const successMessage = ref('')
const successTimeout = ref(null)

const filteredUsers = computed(() => {
  if (!searchTerm.value) return users.value
  return users.value.filter(u =>
    u.name?.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
    u.email?.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
    u.role?.toLowerCase().includes(searchTerm.value.toLowerCase())
  )
})

const userStats = computed(() => ({
  admins: users.value.filter(u => u.role === 'admin' || u.role === 'super_user').length,
  users: users.value.filter(u => u.role === 'user').length,
  active: users.value.length
}))

const statsArray = computed(() => [
  { title: 'Total Usuarios', value: users.value.length, iconClass: 'fas fa-users fa-2x text-primary', colorClass: 'text-primary' },
  { title: 'Admins', value: userStats.value.admins, iconClass: 'fas fa-user-shield fa-2x text-info', colorClass: 'text-info' },
  { title: 'Usuarios', value: userStats.value.users, iconClass: 'fas fa-user fa-2x text-success', colorClass: 'text-success' },
  { title: 'Activos', value: userStats.value.active, iconClass: 'fas fa-check-circle fa-2x text-warning', colorClass: 'text-warning' }
])

const { getToken } = useAuth()

// Cache de modales
let userFormModal = null
let roleChangeModal = null
let deleteConfirmModal = null

// Cargar usuarios
const loadUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    const token = getToken()
    if (!token) throw new Error('No hay token de autenticación')
    users.value = await $fetch(`${API_BASE}/api/auth/users`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })
  } catch (err) {
    error.value = err.message || 'Error al cargar usuarios'
  } finally {
    loading.value = false
  }
}

// Inicializar modales (solo una vez)
const initializeModals = () => {
  if (process.client) {
    userFormModal = new bootstrap.Modal(document.getElementById('userFormModal'), { backdrop: 'static' })
    roleChangeModal = new bootstrap.Modal(document.getElementById('roleChangeModal'), { backdrop: 'static' })
    deleteConfirmModal = new bootstrap.Modal(document.getElementById('deleteConfirmModal'), { backdrop: 'static' })
  }
}

// Abrir modal de agregar usuario
const openAddUserModal = () => {
  editingUser.value = null
  requestAnimationFrame(() => userFormModal?.show())
}

// Abrir modal de editar usuario
const editUser = (user) => {
  editingUser.value = user
  requestAnimationFrame(() => userFormModal?.show())
}

// Abrir modal de cambiar rol
const changeUserRole = (user) => {
  userToChangeRole.value = user
  requestAnimationFrame(() => roleChangeModal?.show())
}

// Abrir modal de confirmación de eliminación
const confirmDeleteUser = (user) => {
  userToDelete.value = user
  requestAnimationFrame(() => deleteConfirmModal?.show())
}

// Manejar guardado de usuario
const handleUserSaved = async () => {
  successMessage.value = 'Usuario guardado correctamente'
  clearTimeout(successTimeout.value)
  successTimeout.value = setTimeout(() => {
    successMessage.value = ''
  }, 3000)
  await loadUsers()
}

// Manejar cambio de rol
const handleRoleChanged = async () => {
  successMessage.value = 'Rol actualizado correctamente'
  clearTimeout(successTimeout.value)
  successTimeout.value = setTimeout(() => {
    successMessage.value = ''
  }, 3000)
  await loadUsers()
}

// Manejar eliminación de usuario
const handleUserDeleted = async () => {
  successMessage.value = 'Usuario eliminado correctamente'
  clearTimeout(successTimeout.value)
  successTimeout.value = setTimeout(() => {
    successMessage.value = ''
  }, 3000)
  await loadUsers()
}

// Funciones optimizadas con memoización
const getRoleBadgeClass = (role) => {
  const roleMap = {
    'super_user': 'bg-warning text-dark',
    'admin': 'bg-info',
    'user': 'bg-success'
  }
  return roleMap[role] || 'bg-success'
}

const formatRole = (role) => {
  const roleMap = {
    'super_user': 'Super Usuario',
    'admin': 'Administrador',
    'user': 'Usuario'
  }
  return roleMap[role] || 'Usuario'
}

onMounted(() => {
  if (process.client) {
    initializeModals()
  }
  loadUsers()
})

onUnmounted(() => {
  clearTimeout(successTimeout.value)
  if (process.client) {
    userFormModal?.dispose()
    roleChangeModal?.dispose()
    deleteConfirmModal?.dispose()
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

/* Contenedor principal */
.oficina-container {
  background: linear-gradient(135deg, #0a0a0a 0%, #0f0f0f 100%);
  min-height: 100vh;
  font-family: 'Poppins', sans-serif;
  padding-bottom: 2rem;
  margin-top: 75px !important;
  position: relative;
  overflow: hidden;
  contain: layout style paint;
}

.oficina-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 0, 87, 0), rgba(255, 0, 87, 0.5), rgba(255, 0, 87, 0));
  pointer-events: none;
}

.oficina-container h1 {
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(255, 0, 87, 0.1);
  margin-bottom: 0.5rem;
  letter-spacing: -0.5px;
}

.oficina-container .text-muted {
  color: #a0a0a0 !important;
  font-size: 0.95rem;
  margin-top: 0.5rem;
}

/* Tarjetas */
.card-custom {
  background: linear-gradient(135deg, #1a1a1a 0%, #151515 100%);
  border: 1px solid rgba(255, 0, 87, 0.15);
  border-radius: 14px;
  color: #fff;
  transition: border-color 0.2s linear;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  position: relative;
  will-change: border-color;
}

.card-custom:hover {
  border-color: rgba(255, 0, 87, 0.4);
  box-shadow: 0 12px 40px rgba(255, 0, 87, 0.2);
}

.card-custom .card-header {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.12) 0%, rgba(255, 107, 53, 0.08) 100%);
  border-bottom: 1px solid rgba(255, 0, 87, 0.2);
  padding: 1.25rem 1.5rem;
  font-weight: 700;
}

.card-custom .card-header h5 {
  color: #ffffff;
  margin: 0;
  font-size: 1.1rem;
}

.card-custom .card-body {
  padding: 1.5rem;
}

/* Estadísticas */
.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
  width: 100%;
}

.stat-card {
  background: linear-gradient(135deg, #1a1a1a 0%, #151515 100%);
  border: 1px solid rgba(255, 0, 87, 0.15);
  border-radius: 14px;
  padding: 1.5rem;
  transition: border-color 0.2s linear;
  position: relative;
  overflow: hidden;
  will-change: border-color;
}

.stat-card:hover {
  border-color: rgba(255, 0, 87, 0.4);
  box-shadow: 0 12px 40px rgba(255, 0, 87, 0.2);
}

.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-title {
  color: #a0a0a0;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0.75rem;
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 0;
}

.stat-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-left: 1rem;
}

/* Tabla */
.table-dark {
  width: 100%;
  margin-bottom: 0;
  color: #fff;
  border-collapse: separate;
  border-spacing: 0;
  font-family: 'Poppins', sans-serif;
}

.table-dark thead th {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.15) 0%, rgba(255, 107, 53, 0.1) 100%);
  color: #fff;
  font-weight: 800;
  border-bottom: 2px solid rgba(255, 0, 87, 0.3);
  padding: 1.1rem 0.75rem;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.table-dark tbody td {
  background: #0f0f0f;
  color: #e5e7eb;
  border-bottom: 1px solid rgba(255, 0, 87, 0.08);
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

.table-dark tbody tr:hover {
  background: linear-gradient(90deg, rgba(255, 0, 87, 0.08) 0%, rgba(255, 107, 53, 0.05) 100%);
  box-shadow: inset 0 0 0 1px rgba(255, 0, 87, 0.1);
}

.table-dark tbody tr:hover td {
  color: #fff;
}

/* Buscador */
.search-input {
  background: linear-gradient(135deg, #1a1a1a 0%, #151515 100%);
  border: 1.5px solid rgba(255, 0, 87, 0.25);
  color: #fff;
  border-radius: 10px;
  padding: 0.65rem 1rem;
  transition: border-color 0.15s linear;
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  will-change: border-color;
}

.search-input::placeholder {
  color: #555;
  font-weight: 400;
}

.search-input:focus {
  background: linear-gradient(135deg, #1f1f1f 0%, #1a1a1a 100%);
  border-color: rgba(255, 0, 87, 0.6);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1);
  color: #fff;
  outline: none;
}

/* Avatar */
.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  font-size: 0.9rem;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  box-shadow: 0 4px 8px rgba(255, 0, 87, 0.2);
  flex-shrink: 0;
}

.avatar-image {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 8px rgba(255, 0, 87, 0.2);
  border: 2px solid rgba(255, 0, 87, 0.3);
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.user-name {
  color: #fff;
  font-weight: 700;
  font-size: 0.95rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  color: #a0a0a0;
  font-size: 0.8rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Tarjetas de usuario para móvil */
/* Override de card-body para móviles */
.d-lg-none {
  margin: 0 !important;
  padding: 0 !important;
}

.card-body {
  padding: 0 !important;
}

/* Vista móvil - Tabla compacta horizontal sin márgenes */
.table-responsive-mobile-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  width: calc(100% + 40px);
  margin-left: -20px !important;
  margin-right: -20px !important;
  margin-top: 20px !important;
  margin-bottom: 0 !important;
}

.table-responsive-mobile {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 0 !important;
  margin: 0 !important;
  overflow-x: auto;
}

.table-mobile-header {
  display: grid;
  grid-template-columns: 2.5fr 1.2fr 1fr;
  gap: 0;
  background: linear-gradient(135deg, rgba(139, 0, 53, 0.8) 0%, rgba(160, 58, 20, 0.7) 100%);
  padding: 0.6rem 20px;
  margin: 0 !important;
  margin-top: 0 !important;
  position: sticky;
  top: 0;
  z-index: 10;
}

.table-mobile-col {
  color: #fff;
  font-weight: 700;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
}

.table-mobile-header .table-mobile-col {
  justify-content: flex-start;
  padding: 0.4rem 0;
}

.table-mobile-row {
  display: grid;
  grid-template-columns: 2.5fr 1.2fr 1fr;
  gap: 0;
  background: #0f0f0f;
  border-bottom: 1px solid rgba(255, 0, 87, 0.08);
  align-items: center;
  padding: 0;
}

.table-mobile-row .table-mobile-col {
  padding: 0.5rem 20px;
  border-right: 1px solid rgba(255, 0, 87, 0.05);
  min-height: 50px;
  justify-content: flex-start;
}

.table-mobile-row .table-mobile-col:last-child {
  border-right: none;
  justify-content: center;
}

/* Columna Nombre */
.col-nombre {
  padding: 0.5rem;
}

.col-nombre .user-cell-content {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  min-width: 0;
}

.col-nombre .avatar-image,
.col-nombre .avatar-circle {
  width: 35px;
  height: 35px;
  flex-shrink: 0;
}

.col-nombre .user-info {
  min-width: 0;
}

.col-nombre .user-name {
  color: #fff;
  font-weight: 700;
  font-size: 0.9rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Columna Rol */
.col-rol {
  justify-content: center;
  padding: 0.5rem 0.3rem;
}

.col-rol .badge {
  font-size: 0.7rem;
  padding: 0.3rem 0.5rem;
  white-space: nowrap;
}

/* Columna Acciones */
.col-acciones {
  justify-content: center;
  padding: 0.5rem 0.3rem;
}

.actions-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.4rem;
}

.btn-action-icon {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  border-radius: 4px;
  min-width: 30px;
  height: 30px;
  will-change: transform;
}

.btn-action-icon:active {
  transform: scale(0.95);
}

.btn-edit {
  color: #64b5f6;
}

.btn-edit:active {
  background: rgba(100, 181, 246, 0.2);
}

.btn-role {
  color: #4dd0e1;
}

.btn-role:active {
  background: rgba(77, 208, 225, 0.2);
}

.btn-delete {
  color: #ef5350;
}

.btn-delete:active {
  background: rgba(239, 83, 80, 0.2);
}

/* Contenedor de botones en tabla */
.table-users td:last-child .d-flex {
  justify-content: center !important;
  flex-wrap: wrap;
  gap: 8px !important;
}

.table-users .btn-sm {
  padding: 0.4rem 0.7rem;
  font-size: 0.7rem;
  white-space: nowrap;
}

/* Botones */
.btn-primary {
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  border: none;
  border-radius: 10px;
  font-weight: 700;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(255, 0, 87, 0.25);
  font-family: 'Poppins', sans-serif;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  font-size: 0.9rem;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #e60050, #ff5722);
  box-shadow: 0 10px 30px rgba(255, 0, 87, 0.4);
  transform: translateY(-3px);
}

.btn-outline-primary {
  border: 2px solid rgba(255, 0, 87, 0.5);
  color: #ff0057;
  background: transparent;
  border-radius: 10px;
  font-weight: 700;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

.btn-outline-primary:hover {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.12) 0%, rgba(255, 107, 53, 0.08) 100%);
  border-color: rgba(255, 0, 87, 0.9);
  color: #ff0057;
  box-shadow: 0 0 20px rgba(255, 0, 87, 0.2);
}

.btn-outline-info {
  border: 2px solid rgba(100, 200, 255, 0.5);
  color: #64c8ff;
  background: transparent;
  border-radius: 10px;
  font-weight: 700;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

.btn-outline-info:hover {
  background: linear-gradient(135deg, rgba(100, 200, 255, 0.12) 0%, rgba(100, 200, 255, 0.08) 100%);
  border-color: rgba(100, 200, 255, 0.9);
  color: #64c8ff;
  box-shadow: 0 0 20px rgba(100, 200, 255, 0.2);
}

.btn-outline-danger {
  border: 2px solid rgba(220, 53, 69, 0.5);
  color: #ff6b6b;
  background: transparent;
  border-radius: 10px;
  font-weight: 700;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

.btn-outline-danger:hover {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.12) 0%, rgba(255, 107, 107, 0.08) 100%);
  border-color: rgba(220, 53, 69, 0.9);
  color: #ff6b6b;
  box-shadow: 0 0 20px rgba(220, 53, 69, 0.2);
}

.btn-outline-light {
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  transition: all 0.3s ease;
  border-radius: 10px;
  font-weight: 700;
  font-family: 'Poppins', sans-serif;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

.btn-outline-light:hover {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.15) 0%, rgba(255, 107, 53, 0.1) 100%);
  border-color: rgba(255, 0, 87, 0.8);
  color: #fff;
  box-shadow: 0 0 20px rgba(255, 0, 87, 0.15);
}

.btn-danger {
  background: linear-gradient(135deg, #dc3545, #e63950);
  border: none;
  border-radius: 10px;
  font-weight: 700;
  transition: all 0.3s ease;
  box-shadow: 0 6px 20px rgba(220, 53, 69, 0.25);
  font-family: 'Poppins', sans-serif;
  text-transform: uppercase;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
}

.btn-danger:hover {
  background: linear-gradient(135deg, #c82333, #d62840);
  box-shadow: 0 10px 30px rgba(220, 53, 69, 0.4);
  transform: translateY(-3px);
}

/* Badge */
.badge {
  font-size: 0.75rem;
  font-weight: 800;
  padding: 0.5rem 0.8rem;
  border-radius: 8px;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  font-family: 'Poppins', sans-serif;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  display: inline-block;
  white-space: nowrap;
}

.table-users .badge {
  margin: 0 auto;
}

/* Tabla responsive */
.table-responsive {
  border-radius: 12px;
  overflow: hidden;
}

/* Spinner */
.spinner-border {
  border-color: rgba(255, 0, 87, 0.2) !important;
  border-right-color: #ff0057 !important;
}

/* Alert */
.alert-danger {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.12) 0%, rgba(255, 107, 107, 0.08) 100%);
  border: 1.5px solid rgba(220, 53, 69, 0.4);
  color: #ff8787;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.15);
}

/* Responsive */
@media(max-width:992px) {
  .table-dark {
    font-size: 0.9rem;
  }

  .table-dark thead th {
    padding: 0.75rem 0.5rem;
    font-size: 0.8rem;
  }

  .table-dark tbody td {
    padding: 0.75rem 0.5rem;
  }

  .user-info {
    flex-direction: column;
  }

  .user-name {
    font-size: 0.9rem;
  }

  .user-email {
    font-size: 0.75rem;
  }

  .btn {
    padding: 0.35rem 0.5rem;
    font-size: 0.75rem;
  }

  .btn-sm {
    padding: 0.25rem 0.4rem;
  }
}

@media(max-width:768px) {
  .oficina-container h1 {
    font-size: 1.8rem;
  }

  .flex-wrap {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .table-responsive {
    font-size: 0.8rem;
  }

  .table-dark {
    font-size: 0.8rem;
  }

  .table-dark thead th {
    padding: 0.6rem 0.4rem;
    font-size: 0.7rem;
    white-space: nowrap;
  }

  .table-dark tbody td {
    padding: 0.6rem 0.4rem;
  }

  .d-flex.align-items-center.gap-2 {
    gap: 0.5rem !important;
  }

  .avatar-circle,
  .avatar-image {
    width: 32px;
    height: 32px;
    font-size: 0.8rem;
  }

  .user-name {
    font-size: 0.8rem;
  }

  .user-email {
    font-size: 0.7rem;
  }

  .btn {
    padding: 0.25rem 0.35rem;
    font-size: 0.7rem;
  }

  .btn-sm {
    padding: 0.2rem 0.3rem;
  }

  .btn-lg {
    padding: 0.5rem 1rem;
    font-size: 0.85rem;
  }

  .card-header {
    flex-direction: column !important;
    gap: 0.75rem !important;
  }

  .card-header h5 {
    font-size: 1rem;
  }

  .d-flex.gap-2.align-items-center {
    flex-direction: column;
    align-items: stretch !important;
    gap: 0.5rem !important;
  }

  .d-flex.gap-2.align-items-center label {
    margin-bottom: 0.25rem;
  }

  .d-flex.gap-2.align-items-center input,
  .d-flex.gap-2.align-items-center button {
    width: 100%;
  }

  .d-flex.justify-content-center.gap-2.flex-wrap {
    flex-direction: column;
    align-items: stretch;
  }

  .d-flex.justify-content-center.gap-2.flex-wrap .btn {
    width: 100%;
  }

  .text-muted {
    font-size: 0.8rem;
  }

  .badge {
    font-size: 0.65rem;
    padding: 0.3rem 0.5rem;
  }

  h6.text-muted {
    font-size: 0.75rem;
  }

  h3 {
    font-size: 1.5rem;
  }
}

@media(max-width:576px) {
  .oficina-container h1 {
    font-size: 1.4rem;
    margin-bottom: 0.5rem;
  }

  .oficina-container .text-muted {
    font-size: 0.8rem;
    margin-top: 1.2rem;
    margin-bottom: 0.5rem;
  }

  .card-body {
    padding: 1rem !important;
  }

  .table-dark {
    font-size: 0.75rem;
  }

  .table-dark thead th {
    padding: 0.5rem 0.3rem;
    font-size: 0.65rem;
  }

  .table-dark tbody td {
    padding: 0.5rem 0.3rem;
  }

  .avatar-circle,
  .avatar-image {
    width: 28px;
    height: 28px;
    font-size: 0.7rem;
  }

  .user-name {
    font-size: 0.75rem;
  }

  .user-email {
    font-size: 0.65rem;
  }

  .btn {
    padding: 0.2rem 0.3rem;
    font-size: 0.65rem;
  }

  .btn-lg {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
  }

  .card-header h5 {
    font-size: 0.9rem;
  }

  .d-flex.justify-content-center.gap-2.flex-wrap {
    gap: 0.25rem !important;
  }

  .d-flex.justify-content-center.gap-2.flex-wrap .btn {
    padding: 0.2rem 0.25rem;
  }

  .badge {
    font-size: 0.6rem;
    padding: 0.25rem 0.4rem;
  }

  .spinner-border {
    width: 2rem;
    height: 2rem;
  }

  .py-4 {
    padding-top: 1rem !important;
    padding-bottom: 1rem !important;
  }

  .mb-4 {
    margin-bottom: 1rem !important;
  }

  .mt-2 {
    margin-top: 0.5rem !important;
  }

  .card-custom {
    margin-bottom: 0.5rem;
  }
}

/* ============ Tarjeta de Usuarios ============ */
.card-users {
  border: none;
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(135deg, #1a1a1a 0%, #151515 100%);
}

.users-card-container {
  overflow: visible;
}

/* Table responsive container */
.table-responsive-users {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  background: #0f0f0f;
}

/* ============ Tabla Limpia de Usuarios ============ */
.table-users-clean {
  width: 100%;
  margin: 0;
  padding: 0;
  border-collapse: collapse;
  font-family: 'Poppins', sans-serif;
  color: #fff;
  background: transparent;
}

.table-users-clean thead {
  background: linear-gradient(135deg, rgba(139, 0, 53, 0.8) 0%, rgba(160, 58, 20, 0.7) 100%);
}

.table-users-clean thead th {
  color: #fff;
  font-weight: 800;
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  padding: 18px 16px;
  text-align: left;
  border: none;
  background: transparent;
}

.table-users-clean thead th:nth-child(1) {
  width: 38%;
  padding-left: 20px;
}

.table-users-clean thead th:nth-child(2) {
  width: 15%;
  text-align: center;
}

.table-users-clean thead th:nth-child(3) {
  width: 12%;
  text-align: center;
}

.table-users-clean thead th:nth-child(4) {
  width: 12%;
  text-align: center;
}

.table-users-clean thead th:nth-child(5) {
  width: 23%;
  text-align: center;
  padding-right: 20px;
}

.table-users-clean tbody td {
  background: #0f0f0f;
  border-bottom: 1px solid rgba(255, 0, 87, 0.08);
  padding: 16px;
  vertical-align: middle;
  color: #e5e7eb;
}

.table-users-clean tbody td:nth-child(1) {
  width: 38%;
  padding-left: 20px;
}

.table-users-clean tbody td:nth-child(2) {
  width: 15%;
  text-align: center;
}

.table-users-clean tbody td:nth-child(3) {
  width: 12%;
  text-align: center;
}

.table-users-clean tbody td:nth-child(4) {
  width: 12%;
  text-align: center;
}

.table-users-clean tbody td:nth-child(5) {
  width: 23%;
  text-align: center;
  padding-right: 20px;
}

.table-users-clean tbody tr {
  transition: all 0.2s ease;
}

.table-users-clean tbody tr:hover {
  background: linear-gradient(90deg, rgba(255, 0, 87, 0.08) 0%, rgba(255, 107, 53, 0.06) 100%);
}

.table-users-clean tbody tr:hover td {
  color: #fff;
}

/* Contenedor de celda de nombre */
.user-cell-content {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

/* Badge en tabla */
.table-users-clean .badge {
  display: inline-block;
  margin: 0 auto;
}

/* Contenedor de acciones */
.actions-container {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* Loading state */
.loading-users {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  text-align: center;
}

.loading-users .spinner-border {
  width: 60px;
  height: 60px;
  border-width: 4px;
  color: rgba(255, 0, 87, 0.2) !important;
  border-right-color: #ff0057 !important;
}

/* Alert en tabla */
.alert-users {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.12) 0%, rgba(255, 107, 107, 0.08) 100%);
  border: 1.5px solid rgba(220, 53, 69, 0.4);
  color: #ff8787;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.15);
}

/* Empty state */
.empty-state-users {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}

.empty-state-users i {
  font-size: 3.5rem;
  color: rgba(255, 0, 87, 0.3);
  margin-bottom: 20px;
}

.empty-state-users p {
  color: #e5e7eb;
  font-size: 1.05rem;
  margin: 10px 0;
  font-weight: 600;
}

.empty-state-users small {
  color: #a0a0a0;
  font-size: 0.95rem;
  font-weight: 400;
}

/* Footer de tabla */
.users-table-footer {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.05) 0%, rgba(0, 0, 0, 0.05) 100%);
  border-top: 1px solid rgba(255, 0, 87, 0.15);
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  font-weight: 600;
  gap: 20px;
  border-radius: 0 0 14px 14px;
}

.users-table-footer-desktop {
  flex-direction: row !important;
  gap: 20px !important;
  text-align: left !important;
  justify-content: space-between !important;
}

.users-table-footer-desktop .results-count {
  flex: 0 !important;
}

.results-count {
  color: #e5e7eb;
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  will-change: contents;
}

.results-count i {
  color: #ff0057;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.results-count span {
  display: inline;
}

.results-count strong {
  color: #ff0057;
  font-weight: 800;
  display: inline;
  margin: 0 4px;
}

.footer-info {
  color: #a0a0a0;
  display: flex;
  align-items: center;
  font-size: 0.85rem;
  gap: 10px;
  flex-shrink: 0;
}

.footer-info i {
  color: #ff6b35;
  font-size: 1.1rem;
  flex-shrink: 0;
}

/* ============ Sección de Usuarios ============ */
.users-section-header {
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  padding: 40px 20px;
  margin-bottom: 0;
  border-radius: 14px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.users-header-integrated {
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  padding: 40px 20px;
  margin-bottom: 0;
  border-radius: 14px 14px 0 0;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.users-section-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 15% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 85% 70%, rgba(0, 0, 0, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

.users-header-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.users-section-title {
  font-size: 2rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0;
  letter-spacing: 0.5px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
}

.users-section-title i {
  font-size: 2.2rem;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.users-search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-box-users {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.95);
  border: 2.5px solid #ff0057;
  border-radius: 12px;
  padding: 0 18px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-height: 50px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.search-box-users:focus-within {
  border-color: #ff6b35;
  background: #ffffff;
  box-shadow: 0 8px 30px rgba(255, 0, 87, 0.4);
  transform: translateY(-2px);
}

.search-box-users i {
  color: #ff0057;
  font-size: 1.1rem;
  margin-right: 12px;
  flex-shrink: 0;
}

.search-input-users {
  flex: 1;
  border: none;
  background: transparent;
  padding: 0;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  color: #0a0a0a;
  outline: none;
  font-weight: 500;
}

.search-input-users::placeholder {
  color: #a0a0a0;
  font-weight: 400;
}

.btn-search-users {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  border: none;
  border-radius: 12px;
  color: #ffffff;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(255, 0, 87, 0.3);
  flex-shrink: 0;
}

.btn-search-users:hover {
  background: linear-gradient(135deg, #e60050, #ff5722);
  box-shadow: 0 10px 30px rgba(255, 0, 87, 0.4);
  transform: translateY(-3px);
}

.btn-search-users:active {
  transform: translateY(-1px);
}

/* Responsive para usuarios header */
@media(max-width: 1200px) {
  .users-search-bar {
    gap: 10px;
  }

  .search-box-users {
    padding: 0 15px;
  }

  .users-section-title {
    font-size: 1.8rem;
  }
}

@media(max-width: 992px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 1.2rem;
  }

  .stat-card {
    padding: 1.2rem;
  }

  .stat-value {
    font-size: 2rem;
  }

  .users-section-header {
    padding: 30px 15px;
  }

  .users-search-bar {
    flex-direction: row;
    gap: 10px;
  }

  .search-box-users {
    width: 100%;
    min-height: 48px;
  }

  .btn-search-users {
    width: 48px;
    height: 48px;
    flex-shrink: 0;
  }

  .users-section-title {
    font-size: 1.6rem;
  }

  .users-section-title i {
    font-size: 1.8rem;
  }

  .table-users thead th {
    padding: 14px 10px;
    font-size: 0.75rem;
    letter-spacing: 0.8px;
  }

  .table-users tbody td {
    padding: 12px 10px;
    font-size: 0.9rem;
  }

  .users-table-footer {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
}

@media(max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .stat-card {
    padding: 1rem;
  }

  .stat-value {
    font-size: 1.8rem;
  }

  .stat-title {
    font-size: 0.8rem;
  }

  /* Tabla móvil - Tablets */
  .table-responsive-mobile-wrapper {
    margin-left: -20px !important;
    margin-right: -20px !important;
    width: calc(100% + 40px) !important;
    margin-top: 20px !important;
  }

  .table-mobile-header {
    grid-template-columns: 2.8fr 1.3fr 1fr;
    padding: 0.6rem 20px;
    margin: 0 !important;
  }

  .table-mobile-header .table-mobile-col {
    font-size: 0.7rem;
    padding: 0.4rem 0;
  }

  .table-mobile-row {
    grid-template-columns: 2.8fr 1.3fr 1fr;
  }

  .table-mobile-row .table-mobile-col {
    padding: 0.5rem 20px;
    min-height: 48px;
  }

  .col-nombre .avatar-image,
  .col-nombre .avatar-circle {
    width: 36px;
    height: 36px;
  }

  .col-nombre .user-name {
    font-size: 0.85rem;
  }

  .col-rol .badge {
    font-size: 0.72rem;
    padding: 0.3rem 0.5rem;
  }

  .btn-action-icon {
    font-size: 0.8rem;
    min-width: 30px;
    height: 30px;
  }

  .users-section-header {
    padding: 25px 12px;
    margin-bottom: 18px;
  }

  .users-header-content {
    gap: 15px;
  }

  .users-search-bar {
    flex-direction: row;
    gap: 10px;
  }

  .users-section-title {
    font-size: 1.4rem;
  }

  .users-section-title i {
    font-size: 1.6rem;
    margin-right: 0.5rem;
  }

  .search-box-users {
    min-height: 44px;
    padding: 0 12px;
    border-radius: 10px;
    flex: 1;
  }

  .search-box-users i {
    font-size: 0.95rem;
    margin-right: 10px;
  }

  .search-input-users {
    font-size: 0.9rem;
  }

  .btn-search-users {
    width: 44px;
    height: 44px;
    font-size: 0.95rem;
    border-radius: 10px;
    flex-shrink: 0;
  }

  .table-users thead th {
    padding: 12px 8px;
    font-size: 0.7rem;
    letter-spacing: 0.6px;
  }

  .table-users tbody td {
    padding: 10px 8px;
    font-size: 0.85rem;
  }

  .users-table-footer {
    padding: 12px 10px;
    font-size: 0.8rem;
  }

  .results-count {
    font-size: 0.8rem;
  }

  .footer-info {
    font-size: 0.75rem;
  }
}

@media(max-width: 576px) {
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.8rem;
  }

  .stat-card {
    padding: 0.8rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .stat-title {
    font-size: 0.7rem;
    margin-bottom: 0.5rem;
  }

  .stat-icon i {
    font-size: 1.5rem;
  }

  /* Tabla móvil - Móviles pequeños */
  .table-responsive-mobile-wrapper {
    margin-left: -20px !important;
    margin-right: -20px !important;
    width: calc(100% + 40px) !important;
    margin-top: 20px !important;
  }

  .table-mobile-header {
    grid-template-columns: 2fr 1fr 0.8fr;
    padding: 0.5rem 20px;
    margin: 0 !important;
  }

  .table-mobile-header .table-mobile-col {
    font-size: 0.65rem;
    padding: 0.3rem 0;
  }

  .table-mobile-row .table-mobile-col {
    padding: 0.4rem 20px;
  }

  .table-mobile-row {
    grid-template-columns: 2fr 1fr 0.8fr;
  }

  .table-mobile-row .table-mobile-col {
    padding: 0.4rem 1rem;
    min-height: 45px;
    font-size: 0.8rem;
  }

  .col-nombre .avatar-image,
  .col-nombre .avatar-circle {
    width: 32px;
    height: 32px;
  }

  .col-nombre .user-name {
    font-size: 0.8rem;
  }

  .col-rol .badge {
    font-size: 0.65rem;
    padding: 0.2rem 0.4rem;
  }

  .btn-action-icon {
    padding: 0.3rem;
    font-size: 0.75rem;
    min-width: 28px;
    height: 28px;
  }

  .actions-container {
    gap: 0.3rem;
  }

  .users-section-header {
    padding: 20px 10px;
    margin-bottom: 15px;
  }

  .users-header-content {
    gap: 12px;
  }

  .users-search-bar {
    flex-direction: row;
    gap: 8px;
  }

  .users-section-title {
    font-size: 1.1rem;
  }

  .users-section-title i {
    font-size: 1.3rem;
    margin-right: 0.4rem;
  }

  .search-box-users {
    min-height: 40px;
    padding: 0 10px;
    border-radius: 8px;
    flex: 1;
  }

  .search-box-users i {
    font-size: 0.9rem;
    margin-right: 8px;
  }

  .search-input-users {
    font-size: 0.85rem;
  }

  .btn-search-users {
    width: 40px;
    height: 40px;
    font-size: 0.9rem;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .table-users thead th {
    padding: 10px 6px;
    font-size: 0.65rem;
    letter-spacing: 0.5px;
  }

  .table-users tbody td {
    padding: 8px 6px;
    font-size: 0.8rem;
  }

  .users-table-footer {
    padding: 10px 8px;
    font-size: 0.75rem;
    flex-direction: column;
    gap: 8px;
  }

  .empty-state-users {
    padding: 60px 15px;
  }

  .empty-state-users i {
    font-size: 2.5rem;
    margin-bottom: 15px;
  }

  .empty-state-users p {
    font-size: 0.95rem;
  }

  .empty-state-users small {
    font-size: 0.85rem;
  }
}

/* Alertas de éxito */
.alert-success-toast {
  background: linear-gradient(135deg, rgba(40, 167, 69, 0.25) 0%, rgba(34, 139, 34, 0.15) 100%);
  border: 1.5px solid rgba(40, 167, 69, 0.5);
  border-radius: 12px;
  padding: 1.25rem 1.75rem;
  color: #7dd87d;
  font-weight: 700;
  margin-bottom: 1.5rem;
  box-shadow: 0 6px 20px rgba(40, 167, 69, 0.25);
  animation: slideDown 0.3s ease-out;
  letter-spacing: 0.3px;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-1rem);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from {
  transform: translateY(-1rem);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-1rem);
  opacity: 0;
}
</style>


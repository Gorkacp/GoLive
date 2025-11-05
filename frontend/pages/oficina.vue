<template>
  <client-only>
    <Header @buscar-evento="filtrarEventos" />
  </client-only>
  
  <div class="container-fluid py-4 mt-5 oficina-container">
    <div class="row mb-4">
      <div class="col-12">
        <div class="d-flex justify-content-between align-items-center">
          <h1 class="text-light">
            <i class="fas fa-cog me-3 text-primary"></i>
            Panel de Administración
          </h1>
          <button class="btn btn-primary" @click="openAddUserModal">
            <i class="fas fa-plus me-2"></i>Agregar Usuario
          </button>
        </div>
        <p class="text-muted">Gestión completa de usuarios y permisos del sistema</p>
      </div>
    </div>

    <div class="row mb-4">
      <div class="col-md-3 mb-3">
        <div class="card card-custom">
          <div class="card-body">
            <div class="d-flex justify-content-between">
              <div>
                <h5 class="card-title">Total Usuarios</h5>
                <h2 class="text-primary">{{ users.length }}</h2>
              </div>
              <i class="fas fa-users fa-2x text-primary"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card card-custom">
          <div class="card-body">
            <div class="d-flex justify-content-between">
              <div>
                <h5 class="card-title">Admins</h5>
                <h2 class="text-info">{{ userStats.admins }}</h2>
              </div>
              <i class="fas fa-user-shield fa-2x text-info"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card card-custom">
          <div class="card-body">
            <div class="d-flex justify-content-between">
              <div>
                <h5 class="card-title">Usuarios</h5>
                <h2 class="text-success">{{ userStats.users }}</h2>
              </div>
              <i class="fas fa-user fa-2x text-success"></i>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-3 mb-3">
        <div class="card card-custom">
          <div class="card-body">
            <div class="d-flex justify-content-between">
              <div>
                <h5 class="card-title">Activos</h5>
                <h2 class="text-warning">{{ userStats.active }}</h2>
              </div>
              <i class="fas fa-check-circle fa-2x text-warning"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-12">
        <div class="card card-custom">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">
              <i class="fas fa-list me-2"></i>
              Lista de Usuarios Registrados
            </h5>
            <div class="d-flex">
              <input 
                v-model="searchTerm"
                type="text" 
                class="form-control search-input me-2" 
                placeholder="Buscar usuarios..."
                style="width: 250px;"
              >
              <button class="btn btn-outline-secondary" @click="loadUsers">
                <i class="fas fa-sync-alt"></i>
              </button>
            </div>
          </div>
          <div class="card-body">
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Cargando...</span>
              </div>
              <p class="text-muted mt-2">Cargando usuarios...</p>
            </div>
            
            <div v-else-if="error" class="alert alert-danger">
              <i class="fas fa-exclamation-triangle me-2"></i>
              {{ error }}
            </div>
            
            <div v-else>
              <div class="table-responsive">
                <table class="table table-custom table-hover">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Nombre</th>
                      <th>Email</th>
                      <th>Rol</th>
                      <th>Compras</th>
                      <th>Estado</th>
                      <th class="text-center">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="user in filteredUsers" :key="user._id">
                      <td class="text-muted">
                        <small>{{ user._id?.substring(18, 24) || 'N/A' }}</small>
                      </td>
                      <td>
                        <div class="d-flex align-items-center">
                          <div class="avatar-circle bg-primary me-2">
                            {{ user.name?.charAt(0)?.toUpperCase() || 'U' }}
                          </div>
                          <div>
                            <div class="fw-medium">{{ user.name }}</div>
                          </div>
                        </div>
                      </td>
                      <td>{{ user.email }}</td>
                      <td>
                        <span class="badge" :class="getRoleBadgeClass(user.role)">
                          {{ formatRole(user.role) }}
                        </span>
                      </td>
                      <td>
                        <span class="badge bg-secondary">
                          {{ user.purchases?.length || 0 }}
                        </span>
                      </td>
                      <td>
                        <span class="badge bg-success">
                          Activo
                        </span>
                      </td>
                      <td>
                        <div class="d-flex justify-content-center gap-3">
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
              
              <div v-if="filteredUsers.length === 0" class="text-center py-5">
                <i class="fas fa-users fa-3x text-muted mb-3"></i>
                <p class="text-muted">No se encontraron usuarios</p>
              </div>
              
              <div class="d-flex justify-content-between align-items-center mt-4">
                <div class="text-muted">
                  Mostrando {{ filteredUsers.length }} de {{ users.length }} usuarios
                </div>
                <div class="text-muted">
                  <i class="fas fa-info-circle me-1"></i>
                  Gestión de usuarios del sistema
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="userModal" tabindex="-1" data-bs-backdrop="static">
      <div class="modal-dialog modal-lg">
        <div class="modal-content modal-custom">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="fas" :class="editingUser ? 'fa-edit' : 'fa-user-plus'"></i>
              {{ editingUser ? 'Editar Usuario' : 'Agregar Nuevo Usuario' }}
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveUser">
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Nombre</label>
                  <input 
                    v-model="userForm.name" 
                    type="text" 
                    class="form-control modal-input" 
                    required
                  >
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Email</label>
                  <input 
                    v-model="userForm.email" 
                    type="email" 
                    class="form-control modal-input" 
                    required
                  >
                </div>
              </div>
              
              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Contraseña</label>
                  <input 
                    v-model="userForm.password" 
                    type="password" 
                    class="form-control modal-input"
                    :placeholder="editingUser ? 'Dejar vacío para no cambiar' : ''"
                    :required="!editingUser"
                  >
                </div>
                <div class="col-md-6 mb-3">
                  <label class="form-label">Rol</label>
                  <select v-model="userForm.role" class="form-select modal-input" required>
                    <option value="user">Usuario</option>
                    <option value="admin">Administrador</option>
                    <option value="SUPER_USER">Super Usuario</option>
                  </select>
                </div>
              </div>
            </form>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-primary" @click="saveUser">
              <i class="fas fa-save me-1"></i>
              {{ editingUser ? 'Actualizar' : 'Crear' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="deleteModal" tabindex="-1">
      <div class="modal-dialog">
        <div class="modal-content modal-custom">
          <div class="modal-header">
            <h5 class="modal-title text-danger">
              <i class="fas fa-exclamation-triangle me-2"></i>
              Confirmar Eliminación
            </h5>
            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <p>¿Estás seguro de que deseas eliminar al usuario?</p>
            <div class="alert alert-dark">
              <strong>"{{ userToDelete?.name }}"</strong><br>
              <small class="text-muted">{{ userToDelete?.email }}</small>
            </div>
            <p class="text-warning mb-0">
              <small>
                <i class="fas fa-exclamation-circle me-1"></i>
                Esta acción no se puede deshacer.
              </small>
            </p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
            <button type="button" class="btn btn-danger" @click="deleteUser">
              <i class="fas fa-trash me-1"></i>Eliminar
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  middleware: 'super-user-only'
})

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || import.meta.env.VITE_API_URL || 'http://localhost:8085/api'

const users = ref([])
const searchTerm = ref('')
const loading = ref(false)
const error = ref('')
const editingUser = ref(null)
const userToDelete = ref(null)
const userForm = ref({
  name: '',
  email: '',
  password: '',
  role: 'user'
})

const filteredUsers = computed(() => {
  if (!searchTerm.value) return users.value
  return users.value.filter(user => 
    user.name?.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
    user.email?.toLowerCase().includes(searchTerm.value.toLowerCase()) ||
    user.role?.toLowerCase().includes(searchTerm.value.toLowerCase())
  )
})

const userStats = computed(() => {
  return {
    admins: users.value.filter(u => u.role === 'admin' || u.role === 'SUPER_USER').length,
    users: users.value.filter(u => u.role === 'user').length,
    active: users.value.length
  }
})

const loadUsers = async () => {
  loading.value = true
  error.value = ''
  
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      error.value = 'No hay token de autenticación'
      loading.value = false
      return
    }

    const response = await $fetch(`${API_BASE}/auth/users`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    users.value = response
  } catch (err) {
    if (err.status === 403) {
      error.value = 'No tienes permisos para acceder a la gestión de usuarios.'
    } else if (err.status === 401) {
      error.value = 'Sesión expirada. Por favor, inicia sesión nuevamente.'
    } else if (err.status === 404) {
      error.value = `Endpoint no encontrado. Verifica que el backend esté funcionando en: ${API_BASE}`
    } else if (err.status === 0 || err.message?.includes('CORS') || err.message?.includes('Failed to fetch')) {
      error.value = `Error de conexión. Verifica: 1) Backend ejecutándose, 2) URL: ${API_BASE}`
    } else {
      error.value = 'Error al cargar los usuarios: ' + (err.message || 'Error desconocido')
    }
  } finally {
    loading.value = false
  }
}

const openAddUserModal = () => {
  editingUser.value = null
  userForm.value = {
    name: '',
    email: '',
    password: '',
    role: 'user'
  }
  showModal('userModal')
}

const editUser = (user) => {
  editingUser.value = user
  userForm.value = { 
    name: user.name,
    email: user.email,
    password: '',
    role: user.role
  }
  showModal('userModal')
}

const changeUserRole = (user) => {
  editingUser.value = user
  userForm.value = { 
    name: user.name,
    email: user.email,
    password: '',
    role: user.role
  }
  showModal('userModal')
}

const confirmDeleteUser = (user) => {
  userToDelete.value = user
  showModal('deleteModal')
}

const saveUser = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      alert('No hay token de autenticación')
      return
    }

    if (editingUser.value) {
      const response = await $fetch(`${API_BASE}/auth/users/${editingUser.value._id}`, {
        method: 'PUT',
        body: userForm.value,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      const index = users.value.findIndex(u => u._id === editingUser.value._id)
      if (index !== -1) {
        users.value[index] = { ...users.value[index], ...userForm.value }
      }
    } else {
      const response = await $fetch(`${API_BASE}/auth/users`, {
        method: 'POST',
        body: userForm.value,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })
      
      users.value.push(response)
    }
    
    hideModal('userModal')
    resetForm()
  } catch (err) {
    alert('Error al guardar el usuario: ' + (err.data?.message || err.message))
  }
}

const deleteUser = async () => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      alert('No hay token de autenticación')
      return
    }

    await $fetch(`${API_BASE}/auth/users/${userToDelete.value._id}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    users.value = users.value.filter(u => u._id !== userToDelete.value._id)
    hideModal('deleteModal')
    userToDelete.value = null
  } catch (err) {
    alert('Error al eliminar el usuario: ' + (err.data?.message || err.message))
  }
}

const resetForm = () => {
  userForm.value = {
    name: '',
    email: '',
    password: '',
    role: 'user'
  }
  editingUser.value = null
}

const getRoleBadgeClass = (role) => {
  switch(role) {
    case 'SUPER_USER': return 'bg-warning text-dark'
    case 'admin': return 'bg-info'
    case 'user': return 'bg-success'
    default: return 'bg-secondary'
  }
}

const formatRole = (role) => {
  switch(role) {
    case 'SUPER_USER': return 'Super Usuario'
    case 'admin': return 'Administrador'
    case 'user': return 'Usuario'
    default: return role
  }
}

const showModal = (modalId) => {
  const modal = new bootstrap.Modal(document.getElementById(modalId))
  modal.show()
}

const hideModal = (modalId) => {
  const modal = bootstrap.Modal.getInstance(document.getElementById(modalId))
  modal?.hide()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.oficina-container {
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  min-height: 100vh;
}

.card-custom {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 10px;
  color: #fff;
}

.card-custom .card-header {
  background: #252525;
  border-bottom: 1px solid #333;
  padding: 1rem 1.5rem;
}

.card-custom .card-body {
  padding: 1.5rem;
}

.table-custom {
  background: #1a1a1a;
  color: #fff;
  margin-bottom: 0;
}

.table-custom thead th {
  background: #252525;
  border-bottom: 2px solid #333;
  color: #fff;
  font-weight: 600;
  padding: 1rem 0.75rem;
}

.table-custom tbody td {
  border-color: #333;
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

.table-custom tbody tr:hover {
  background: rgba(255, 255, 255, 0.05);
}

.search-input {
  background: #252525;
  border: 1px solid #444;
  color: #fff;
  border-radius: 6px;
}

.search-input:focus {
  background: #2a2a2a;
  border-color: #007bff;
  color: #fff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.modal-custom {
  background: #1a1a1a;
  border: 1px solid #333;
  border-radius: 10px;
  color: #fff;
}

.modal-custom .modal-header {
  background: #252525;
  border-bottom: 1px solid #333;
  padding: 1.5rem;
}

.modal-custom .modal-body {
  padding: 1.5rem;
  background: #1a1a1a;
}

.modal-custom .modal-footer {
  background: #252525;
  border-top: 1px solid #333;
  padding: 1rem 1.5rem;
}

.modal-input {
  background: #252525;
  border: 1px solid #444;
  color: #fff;
  border-radius: 6px;
}

.modal-input:focus {
  background: #2a2a2a;
  border-color: #007bff;
  color: #fff;
  box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
}

.avatar-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #fff;
  font-size: 0.9rem;
}

.badge {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.35rem 0.65rem;
}

.table-responsive {
  border-radius: 8px;
  overflow: hidden;
}

.btn-outline-secondary {
  border-color: #6c757d;
  color: #6c757d;
}

.btn-outline-secondary:hover {
  background-color: #6c757d;
  border-color: #6c757d;
  color: #fff;
}

.alert-dark {
  background: #252525;
  border: 1px solid #444;
  color: #fff;
}

.spinner-border {
  width: 3rem;
  height: 3rem;
}

.btn-sm {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

@media (max-width: 768px) {
  .d-flex.justify-content-between.align-items-center {
    flex-direction: column;
    gap: 1rem;
  }
  
  .card-header .d-flex {
    flex-direction: column;
    gap: 0.5rem;
    width: 100%;
  }
  
  .search-input {
    width: 100% !important;
  }
  
  .d-flex.justify-content-center.gap-3 {
    flex-direction: column;
    gap: 0.5rem !important;
  }
  
  .table-responsive {
    font-size: 0.875rem;
  }
}
</style>
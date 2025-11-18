<template>
  <client-only>
    <Header @buscar-evento="filtrarEventos" />
  </client-only>

  <div class="container-fluid py-4 mt-5 oficina-container">
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
    <div class="row mb-4">
      <div class="col-md-3 mb-3" v-for="(stat, index) in statsArray" :key="index">
        <div class="card card-custom shadow-sm">
          <div class="card-body d-flex justify-content-between align-items-center">
            <div>
              <h6 class="text-muted mb-1">{{ stat.title }}</h6>
              <h3 :class="stat.colorClass">{{ stat.value }}</h3>
            </div>
            <i :class="stat.iconClass"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabla de usuarios -->
    <div class="row">
      <div class="col-12">
        <div class="card card-custom shadow-sm">
          <div class="card-header d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
            <h5 class="mb-2 mb-md-0">
              <i class="fas fa-list me-2"></i>Lista de Usuarios Registrados
            </h5>
            <div class="d-flex gap-2 align-items-center">
              <label class="text-light mb-0 me-2">Búsqueda de usuario:</label>
              <input 
                v-model="searchTerm"
                type="text"
                class="form-control search-input"
                placeholder="Nombre, email o rol..."
              >
              <button class="btn btn-outline-light" @click="loadUsers">
                <i class="fas fa-search"></i> Buscar
              </button>
            </div>
          </div>
          <div class="card-body p-0">
            <div v-if="loading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status"></div>
              <p class="text-muted mt-2">Cargando usuarios...</p>
            </div>

            <div v-else-if="error" class="alert alert-danger m-3">
              <i class="fas fa-exclamation-triangle me-2"></i>
              {{ error }}
            </div>

            <div v-else>
              <div class="table-responsive">
                <table class="table table-dark table-hover mb-0 align-middle">
                  <thead class="table-secondary text-dark">
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
                      <td class="text-muted"><small>{{ user._id?.substring(18, 24) || 'N/A' }}</small></td>
                      <td>
                        <div class="d-flex align-items-center">
                          <div class="avatar-circle bg-primary me-2">{{ user.name?.charAt(0)?.toUpperCase() || 'U' }}</div>
                          <div>{{ user.name }}</div>
                        </div>
                      </td>
                      <td>{{ user.email }}</td>
                      <td><span class="badge" :class="getRoleBadgeClass(user.role)">{{ formatRole(user.role) }}</span></td>
                      <td><span class="badge bg-secondary">{{ user.purchases?.length || 0 }}</span></td>
                      <td><span class="badge bg-success">Activo</span></td>
                      <td class="text-center">
                        <div class="d-flex justify-content-center gap-2 flex-wrap">
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

              <div v-if="filteredUsers.length === 0" class="text-center py-5 text-muted">
                <i class="fas fa-users fa-3x mb-3"></i>
                <p>No se encontraron usuarios</p>
              </div>

              <div class="d-flex justify-content-between align-items-center mt-3 px-3 py-2 border-top border-secondary text-muted">
                <div>Mostrando {{ filteredUsers.length }} de {{ users.length }} usuarios</div>
                <div><i class="fas fa-info-circle me-1"></i>Gestión de usuarios del sistema</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modales (User / Delete) -->
    <UserModal :editingUser="editingUser" :userForm="userForm" @save="saveUser" />
    <DeleteModal :userToDelete="userToDelete" @delete="deleteUser" />
  </div>
</template>

<script setup>
definePageMeta({ middleware: 'super-user-only' })

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'http://localhost:8085'

const users = ref([])
const searchTerm = ref('')
const loading = ref(false)
const error = ref('')
const editingUser = ref(null)
const userToDelete = ref(null)
const userForm = ref({ name: '', email: '', password: '', role: 'user' })

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

// CRUD & Modals
const { getToken } = useAuth()

const loadUsers = async () => {
  loading.value = true; error.value = ''
  try {
    const token = getToken()
    if (!token) throw new Error('No hay token de autenticación')
    users.value = await $fetch(`${API_BASE}/api/auth/users`, { method: 'GET', headers: { Authorization: `Bearer ${token}` } })
  } catch (err) { error.value = err.message } finally { loading.value = false }
}

const openAddUserModal = () => { editingUser.value = null; resetForm(); showModal('userModal') }
const editUser = user => { editingUser.value = user; userForm.value = { name: user.name, email: user.email, password: '', role: user.role }; showModal('userModal') }
const changeUserRole = editUser
const confirmDeleteUser = user => { userToDelete.value = user; showModal('deleteModal') }

const saveUser = async () => { /* ...igual que antes... */ }
const deleteUser = async () => { /* ...igual que antes... */ }
const resetForm = () => { userForm.value = { name: '', email: '', password: '', role: 'user' }; editingUser.value = null }

const getRoleBadgeClass = role => role === 'super_user' ? 'bg-warning text-dark' : role === 'admin' ? 'bg-info' : 'bg-success'
const formatRole = role => role === 'super_user' ? 'Super Usuario' : role === 'admin' ? 'Administrador' : 'Usuario'

const showModal = id => new bootstrap.Modal(document.getElementById(id)).show()
const hideModal = id => bootstrap.Modal.getInstance(document.getElementById(id))?.hide()

onMounted(() => loadUsers())
</script>

<style scoped>
/* Contenedor principal */
.oficina-container {
  background: linear-gradient(135deg, #0a0a0a, #1a1a1a);
  min-height: 100vh;
}

/* Tarjetas */
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

/* Tabla */
.table-dark {
  width: 100%;
  margin-bottom: 0;
  color: #fff;
  border-collapse: separate;
  border-spacing: 0;
}

.table-dark thead th {
  background: #252525;
  color: #fff;
  font-weight: 600;
  border-bottom: 1px solid #333;
  padding: 1rem 0.75rem;
}

.table-dark tbody td {
  background: #1e1e1e;
  color: #fff;
  border-color: #333;
  padding: 1rem 0.75rem;
  vertical-align: middle;
}

.table-dark tbody tr:hover {
  background: rgba(255, 255, 255, 0.05);
}

/* Buscador */
.search-input {
  background: #252525;
  border: 1px solid #444;
  color: #fff;
  border-radius: 6px;
  padding: 0.375rem 0.75rem;
}

.search-input:focus {
  background: #2a2a2a;
  border-color: #007bff;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,0.25);
  color: #fff;
}

/* Avatar */
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

/* Botones */
.btn-outline-light {
  border-color: #6c757d;
  color: #fff;
  transition: all 0.2s;
}

.btn-outline-light:hover {
  background-color: #007bff;
  border-color: #007bff;
  color: #fff;
}

.btn-primary {
  background-color: #007bff;
  border-color: #007bff;
}

.btn-primary:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

.btn-danger {
  background-color: #dc3545;
  border-color: #dc3545;
}

.btn-danger:hover {
  background-color: #b02a37;
  border-color: #b02a37;
}

/* Modal */
.modal-custom {
  background: #1a1a1a;
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
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,0.25);
}

/* Badge */
.badge {
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.35rem 0.65rem;
}

/* Tabla responsive */
.table-responsive {
  border-radius: 8px;
  overflow: hidden;
}

/* Responsive */
@media(max-width:768px) {
  .flex-wrap {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .table-responsive {
    font-size: 0.875rem;
  }
}
</style>


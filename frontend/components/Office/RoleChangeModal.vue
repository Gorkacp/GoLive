<template>
  <div class="modal fade" id="roleChangeModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content modal-custom">
        <!-- Header -->
        <div class="modal-header modal-header-custom">
          <h5 class="modal-title">
            <i class="fas fa-user-cog me-2"></i>Cambiar Rol de Usuario
          </h5>
          <button type="button" class="btn-close btn-close-custom" data-bs-dismiss="modal"></button>
        </div>

        <!-- Body -->
        <div class="modal-body modal-body-custom">
          <div v-if="currentUser" class="mb-4">
            <!-- Información del usuario -->
            <div class="user-info-card mb-4">
              <div class="d-flex align-items-center gap-3">
                <div v-if="currentUser.profilePhoto" class="avatar-image">
                  <img :src="currentUser.profilePhoto" :alt="currentUser.name" class="avatar-img">
                </div>
                <div v-else class="avatar-circle">
                  {{ currentUser.name?.charAt(0)?.toUpperCase() || 'U' }}
                </div>
                <div class="flex-grow-1">
                  <h6 class="mb-0 text-light fw-bold">{{ currentUser.name }}</h6>
                  <small class="text-muted">{{ currentUser.email }}</small>
                  <div class="mt-2">
                    <span class="badge" :class="getCurrentRoleBadgeClass(currentUser.role)">
                      <i class="fas fa-circle me-1"></i>{{ formatRole(currentUser.role) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Selector de rol -->
            <form @submit.prevent="handleSubmit">
              <label for="roleSelect" class="form-label form-label-custom">
                <i class="fas fa-crown me-2"></i>Nuevo Rol
              </label>
              
              <div class="role-selector mb-4">
                <div
                  v-for="role in availableRoles"
                  :key="role.value"
                  class="role-option"
                  :class="{ 'role-selected': selectedRole === role.value }"
                  @click="selectedRole = role.value"
                >
                  <div class="role-option-header">
                    <input
                      type="radio"
                      :id="'role-' + role.value"
                      v-model="selectedRole"
                      :value="role.value"
                      class="form-check-input"
                    >
                    <label :for="'role-' + role.value" class="role-option-label">
                      <i :class="role.icon" class="me-2"></i>{{ role.name }}
                    </label>
                  </div>
                  <p class="role-option-description">{{ role.description }}</p>
                </div>
              </div>

              <!-- Cambios que se realizarán -->
              <div class="changes-summary mb-4">
                <div class="change-item">
                  <span class="label">Rol Actual:</span>
                  <span class="badge" :class="getCurrentRoleBadgeClass(currentUser.role)">
                    {{ formatRole(currentUser.role) }}
                  </span>
                </div>
                <div class="change-item">
                  <i class="fas fa-arrow-down text-muted mx-2"></i>
                </div>
                <div class="change-item">
                  <span class="label">Nuevo Rol:</span>
                  <span class="badge" :class="getNewRoleBadgeClass()">
                    {{ formatRole(selectedRole) }}
                  </span>
                </div>
              </div>

              <!-- Advertencia si es aplicable -->
              <div v-if="selectedRole === 'super_user'" class="alert alert-warning alert-custom mb-3">
                <i class="fas fa-exclamation-triangle me-2"></i>
                <strong>Atención:</strong> Estás asignando permisos de Super Usuario. Esto otorga acceso completo al sistema.
              </div>

              <div v-if="selectedRole === 'user' && currentUser.role !== 'user'" class="alert alert-info alert-custom mb-3">
                <i class="fas fa-info-circle me-2"></i>
                <strong>Nota:</strong> El usuario perderá acceso al panel de administración.
              </div>

              <!-- Mensaje de error -->
              <div v-if="submitError" class="alert alert-danger alert-custom mb-3">
                <i class="fas fa-exclamation-circle me-2"></i>{{ submitError }}
              </div>

              <div v-if="submitSuccess" class="alert alert-success alert-custom mb-3">
                <i class="fas fa-check-circle me-2"></i>{{ submitSuccess }}
              </div>
            </form>
          </div>

          <div v-else class="text-center text-muted py-4">
            <i class="fas fa-hourglass-half fa-2x mb-2 d-block"></i>
            Cargando información del usuario...
          </div>
        </div>

        <!-- Footer -->
        <div class="modal-footer modal-footer-custom">
          <button
            type="button"
            class="btn btn-outline-secondary"
            data-bs-dismiss="modal"
            :disabled="isLoading"
          >
            <i class="fas fa-times me-2"></i>Cancelar
          </button>
          <button
            type="button"
            class="btn btn-primary"
            @click="handleSubmit"
            :disabled="isLoading || selectedRole === currentUser?.role"
          >
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="fas fa-check me-2"></i>
            {{ isLoading ? 'Actualizando...' : 'Cambiar Rol' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  user: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['save', 'close'])

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'http://localhost:8085'
const { getToken } = useAuth()

const selectedRole = ref('user')
const isLoading = ref(false)
const submitError = ref('')
const submitSuccess = ref('')
const currentUser = ref(null)

const availableRoles = [
  {
    value: 'user',
    name: 'Usuario',
    icon: 'fas fa-user',
    description: 'Acceso básico. Puede comprar entradas y gestionar su perfil.'
  },
  {
    value: 'admin',
    name: 'Administrador',
    icon: 'fas fa-user-shield',
    description: 'Acceso a moderación. Puede crear eventos y gestionar contenido.'
  },
  {
    value: 'super_user',
    name: 'Super Usuario',
    icon: 'fas fa-crown',
    description: 'Acceso total. Control completo del sistema y todos los usuarios.'
  }
]

watch(() => props.user, (newUser) => {
  console.log('RoleChangeModal watcher - newUser:', newUser)
  if (newUser) {
    console.log('Asignando usuario a currentUser')
    currentUser.value = newUser
    selectedRole.value = newUser.role || 'user'
    submitError.value = ''
    submitSuccess.value = ''
  }
}, { immediate: true })

const getCurrentRoleBadgeClass = (role) => {
  if (role === 'super_user') return 'bg-warning text-dark'
  if (role === 'admin') return 'bg-info'
  return 'bg-success'
}

const getNewRoleBadgeClass = () => {
  if (selectedRole.value === 'super_user') return 'bg-warning text-dark'
  if (selectedRole.value === 'admin') return 'bg-info'
  return 'bg-success'
}

const formatRole = (role) => {
  if (role === 'super_user') return 'Super Usuario'
  if (role === 'admin') return 'Administrador'
  return 'Usuario'
}

const handleSubmit = async () => {
  console.log('handleSubmit - currentUser:', currentUser.value)
  console.log('handleSubmit - selectedRole:', selectedRole.value)
  
  if (!currentUser.value) {
    submitError.value = 'No hay usuario seleccionado'
    return
  }
  
  if (selectedRole.value === currentUser.value.role) {
    submitError.value = 'El rol es igual al actual'
    return
  }

  isLoading.value = true
  submitError.value = ''
  submitSuccess.value = ''

  try {
    const token = getToken()
    if (!token) throw new Error('No hay token de autenticación')

    const userId = currentUser.value._id || currentUser.value.id
    console.log('Cambiando rol para usuario:', userId, 'Nuevo rol:', selectedRole.value)
    
    if (!userId) {
      throw new Error('No se pudo obtener el ID del usuario')
    }

    await $fetch(`${API_BASE}/api/auth/users/${userId}/role`, {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: { role: selectedRole.value }
    })

    submitSuccess.value = `Rol actualizado a ${formatRole(selectedRole.value)}`
    setTimeout(() => {
      emit('save')
      closeModal()
    }, 1500)
  } catch (error) {
    console.error('Error completo:', error)
    console.error('Error data:', error.data)
    console.error('Error message:', error.message)
    
    let errorMsg = 'Error al cambiar el rol'
    if (error.data?.message) {
      errorMsg = error.data.message
    } else if (error.message) {
      errorMsg = error.message
    } else if (typeof error === 'string') {
      errorMsg = error
    }
    
    submitError.value = errorMsg
  } finally {
    isLoading.value = false
  }
}

const closeModal = () => {
  const modal = bootstrap.Modal.getInstance(document.getElementById('roleChangeModal'))
  modal?.hide()
}

onMounted(() => {
  const modalElement = document.getElementById('roleChangeModal')
  if (modalElement) {
    modalElement.addEventListener('hidden.bs.modal', () => {
      submitError.value = ''
      submitSuccess.value = ''
      currentUser.value = null
    })
    
    // Listener para cuando el modal se abre
    modalElement.addEventListener('show.bs.modal', () => {
      console.log('Modal abriendo - currentUser ya debe estar sincronizado:', currentUser.value)
    })
  }
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.modal-custom {
  background: linear-gradient(135deg, #1a1a1a 0%, #151515 100%);
  border: 1px solid rgba(255, 0, 87, 0.2);
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  color: #fff;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.modal-header-custom {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  border-bottom: 1px solid rgba(255, 0, 87, 0.2);
  padding: 1.5rem;
  border-radius: 12px 12px 0 0;
}

.modal-header-custom .modal-title {
  color: #fff;
  font-weight: 700;
  font-size: 1.2rem;
}

.btn-close-custom {
  filter: invert(1);
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.btn-close-custom:hover {
  opacity: 1;
}

.modal-body-custom {
  padding: 2rem;
}

.user-info-card {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.08) 0%, rgba(255, 107, 53, 0.02) 100%);
  border: 1px solid rgba(255, 0, 87, 0.15);
  border-radius: 10px;
  padding: 1.5rem;
}

.avatar-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  font-size: 1.2rem;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
  flex-shrink: 0;
}

.avatar-image {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
  border: 2px solid rgba(255, 0, 87, 0.3);
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-label-custom {
  color: #fff;
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.form-label-custom i {
  color: #ff0057;
}

.role-selector {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.role-option {
  background: linear-gradient(135deg, #0f0f0f 0%, #0a0a0a 100%);
  border: 2px solid rgba(255, 0, 87, 0.15);
  border-radius: 10px;
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.role-option:hover {
  border-color: rgba(255, 0, 87, 0.4);
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.05) 0%, rgba(255, 107, 53, 0.02) 100%);
}

.role-option-selected {
  border-color: rgba(255, 0, 87, 0.6);
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  box-shadow: 0 0 20px rgba(255, 0, 87, 0.15);
}

.role-option-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.role-option-header label {
  margin: 0;
  cursor: pointer;
  flex: 1;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.form-check-input {
  width: 1.2rem;
  height: 1.2rem;
  border: 2px solid rgba(255, 0, 87, 0.3);
  background: #0a0a0a;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-check-input:checked {
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  border-color: #ff0057;
  box-shadow: 0 0 10px rgba(255, 0, 87, 0.3);
}

.form-check-input:focus {
  border-color: rgba(255, 0, 87, 0.5);
  box-shadow: 0 0 0 0.2rem rgba(255, 0, 87, 0.15);
}

.role-option-label {
  font-size: 0.95rem;
}

.role-option-description {
  color: #a0a0a0;
  font-size: 0.85rem;
  margin: 0;
  padding-left: 2.2rem;
}

.changes-summary {
  background: linear-gradient(135deg, rgba(100, 200, 255, 0.08) 0%, rgba(100, 200, 255, 0.02) 100%);
  border: 1px solid rgba(100, 200, 255, 0.15);
  border-radius: 10px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.change-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.change-item .label {
  color: #a0a0a0;
  font-weight: 600;
  font-size: 0.9rem;
}

.change-item .badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.5rem 0.8rem;
  border-radius: 6px;
  text-transform: uppercase;
}

.alert-custom {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  border: 1px solid rgba(255, 0, 87, 0.3);
  border-radius: 8px;
  padding: 1rem;
  font-weight: 500;
  font-size: 0.9rem;
}

.alert-warning.alert-custom {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1) 0%, rgba(255, 152, 0, 0.05) 100%);
  border-color: rgba(255, 193, 7, 0.3);
  color: #ffc107;
}

.alert-info.alert-custom {
  background: linear-gradient(135deg, rgba(100, 200, 255, 0.1) 0%, rgba(100, 200, 255, 0.05) 100%);
  border-color: rgba(100, 200, 255, 0.3);
  color: #64c8ff;
}

.alert-success.alert-custom {
  background: linear-gradient(135deg, rgba(40, 167, 69, 0.1) 0%, rgba(34, 139, 34, 0.05) 100%);
  border-color: rgba(40, 167, 69, 0.3);
  color: #7dd87d;
}

.alert-danger.alert-custom {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.1) 0%, rgba(255, 107, 107, 0.05) 100%);
  border-color: rgba(220, 53, 69, 0.3);
  color: #ff8787;
}

.modal-footer-custom {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.02) 0%, rgba(0, 0, 0, 0.05) 100%);
  border-top: 1px solid rgba(255, 0, 87, 0.1);
  padding: 1.5rem;
  border-radius: 0 0 12px 12px;
}

.modal-footer-custom .btn {
  border-radius: 8px;
  font-weight: 600;
  padding: 0.7rem 1.5rem;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
}

.modal-footer-custom .btn-primary {
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
}

.modal-footer-custom .btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #e60050, #ff5722);
  box-shadow: 0 8px 20px rgba(255, 0, 87, 0.3);
  transform: translateY(-2px);
}

.modal-footer-custom .btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer-custom .btn-outline-secondary {
  border: 1.5px solid rgba(255, 255, 255, 0.3);
  color: #fff;
}

.modal-footer-custom .btn-outline-secondary:hover:not(:disabled) {
  background: rgba(255, 0, 87, 0.1);
  border-color: rgba(255, 0, 87, 0.6);
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.2em;
  border-color: rgba(255, 0, 87, 0.2) !important;
  border-right-color: #ff0057 !important;
}
</style>
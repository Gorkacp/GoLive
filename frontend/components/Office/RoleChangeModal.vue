<template>
  <Teleport to="body">
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
  </Teleport>
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
let modalInstance = null
let eventListenersAdded = false

// Roles como const - Object.freeze para inmutabilidad total
const availableRoles = Object.freeze([
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
])

// Watcher optimizado - flush post para mejor rendimiento
watch(
  () => props.user,
  (newUser) => {
    if (newUser) {
      currentUser.value = newUser
      selectedRole.value = newUser.role || 'user'
      submitError.value = ''
      submitSuccess.value = ''
    }
  },
  { immediate: true, flush: 'post' }
)

// Cache para clases de badge - Object.freeze para inmutabilidad
const BADGE_CLASSES = Object.freeze({
  'super_user': 'bg-warning text-dark',
  'admin': 'bg-info',
  'user': 'bg-success'
})

const ROLE_NAMES = Object.freeze({
  'super_user': 'Super Usuario',
  'admin': 'Administrador',
  'user': 'Usuario'
})

const getCurrentRoleBadgeClass = (role) => BADGE_CLASSES[role] || BADGE_CLASSES['user']
const getNewRoleBadgeClass = () => BADGE_CLASSES[selectedRole.value] || BADGE_CLASSES['user']
const formatRole = (role) => ROLE_NAMES[role] || ROLE_NAMES['user']

const handleSubmit = async () => {
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
    if (!userId) throw new Error('No se pudo obtener el ID del usuario')

    await $fetch(`${API_BASE}/api/auth/users/${userId}/role`, {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      body: { role: selectedRole.value }
    })

    // Actualizar el rol en currentUser para que se refleje en el UI
    currentUser.value.role = selectedRole.value
    
    submitSuccess.value = `Rol actualizado a ${formatRole(selectedRole.value)}`
    
    // Emitir evento de guardado y cerrar modal inmediatamente
    emit('save')
    
    // Cerrar modal después de mostrar el mensaje de éxito
    requestAnimationFrame(() => {
      setTimeout(() => {
        closeModal()
      }, 600)
    })
  } catch (error) {
    submitError.value = error.data?.message || error.message || 'Error al cambiar el rol'
  } finally {
    isLoading.value = false
  }
}

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide()
  }
}

onMounted(() => {
  if (process.client && !eventListenersAdded) {
    const modalElement = document.getElementById('roleChangeModal')
    if (modalElement) {
      // Usar bootstrap Modal de forma más eficiente
      modalInstance = new bootstrap.Modal(modalElement, {
        backdrop: 'static',
        keyboard: false
      })

      const handleHidden = () => {
        submitError.value = ''
        submitSuccess.value = ''
      }

      // Usar passive: true para mejor performance
      modalElement.addEventListener('hidden.bs.modal', handleHidden, { passive: true })
      
      eventListenersAdded = true
      
      // Limpiar en desmontaje
      onBeforeUnmount(() => {
        if (modalElement) {
          modalElement.removeEventListener('hidden.bs.modal', handleHidden)
          if (modalInstance) {
            modalInstance.dispose()
            modalInstance = null
          }
        }
        eventListenersAdded = false
      })
    }
  }
})
</script>

<style scoped>
/* ============ MODAL BASE ============ */
.modal-custom {
  background: #1a1a1a;
  border: 1px solid rgba(255, 0, 87, 0.2);
  border-radius: 16px;
  font-family: 'Poppins', sans-serif;
  color: #fff;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.6);
  overflow: hidden;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  will-change: transform;
}

/* ============ MODAL HEADER ============ */
.modal-header-custom {
  background: linear-gradient(135deg, #8b0035 0%, #a03a14 100%);
  border-bottom: 1px solid rgba(255, 0, 87, 0.3);
  padding: 1.5rem;
  border-radius: 16px 16px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.modal-header-custom .modal-title {
  color: #fff;
  font-weight: 700;
  font-size: 1.3rem;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.modal-header-custom .modal-title i {
  font-size: 1.5rem;
}

.btn-close-custom {
  width: auto;
  height: auto;
  opacity: 0.8;
  cursor: pointer;
  border: none;
  background: none !important;
  padding: 0;
  font-size: 1.5rem;
  color: #fff !important;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0;
  line-height: 1;
  transition: opacity 0.2s;
}

.btn-close-custom::before {
  content: "✕";
  font-weight: bold;
  font-size: 1.8rem;
}

.btn-close-custom:hover {
  opacity: 1;
}

/* ============ MODAL BODY ============ */
.modal-body-custom {
  padding: 2rem;
  overflow-y: auto;
  flex: 1;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 0, 87, 0.3) transparent;
}

.modal-body-custom::-webkit-scrollbar {
  width: 6px;
}

.modal-body-custom::-webkit-scrollbar-track {
  background: transparent;
}

.modal-body-custom::-webkit-scrollbar-thumb {
  background: rgba(255, 0, 87, 0.3);
  border-radius: 3px;
}

.modal-body-custom::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 0, 87, 0.5);
}

/* ============ USER INFO CARD ============ */
.user-info-card {
  background: rgba(255, 0, 87, 0.05);
  border: 1px solid rgba(255, 0, 87, 0.15);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.d-flex.gap-3 {
  gap: 1rem;
}

.avatar-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  font-size: 1.3rem;
  background: #ff0057;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
  flex-shrink: 0;
}

.avatar-image {
  width: 60px;
  height: 60px;
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

.user-info-card h6 {
  color: #fff;
  font-size: 1rem;
  margin: 0;
}

.user-info-card small {
  color: #e0e0e0 !important;
  font-size: 0.85rem;
}

.user-info-card .badge {
  background: #ff0057;
  font-size: 0.75rem;
}

/* ============ FORM LABELS ============ */
.form-label-custom {
  color: #fff;
  font-weight: 700;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  font-size: 1rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-label-custom i {
  color: #ff6348;
  margin-right: 0.5rem;
}

/* ============ ROLE SELECTOR ============ */
.role-selector {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.role-option {
  background: #0f0f0f;
  border: 2px solid rgba(255, 0, 87, 0.15);
  border-radius: 12px;
  padding: 1.25rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.role-option:hover {
  border-color: rgba(255, 0, 87, 0.4);
  background: rgba(255, 0, 87, 0.08);
  transform: translateX(4px);
}

.role-option.role-selected {
  border-color: rgba(255, 0, 87, 0.6);
  background: rgba(255, 0, 87, 0.1);
  box-shadow: 0 0 20px rgba(255, 0, 87, 0.15);
}

.role-option-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.role-option-header label {
  margin: 0;
  cursor: pointer;
  flex: 1;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.form-check-input {
  width: 1.25rem;
  height: 1.25rem;
  border: 2px solid rgba(255, 0, 87, 0.3);
  background: #0a0a0a;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.form-check-input:checked {
  background: #ff0057;
  border-color: #ff0057;
  box-shadow: 0 0 10px rgba(255, 0, 87, 0.3);
}

.form-check-input:focus {
  border-color: rgba(255, 0, 87, 0.5);
  box-shadow: 0 0 0 0.2rem rgba(255, 0, 87, 0.15);
  outline: none;
}

.role-option-description {
  color: #a0a0a0;
  font-size: 0.85rem;
  margin: 0;
  padding-left: 2.5rem;
  line-height: 1.4;
}

/* ============ CHANGES SUMMARY ============ */
.changes-summary {
  background: rgba(100, 200, 255, 0.05);
  border: 1px solid rgba(100, 200, 255, 0.15);
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.change-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.change-item .label {
  color: #a0a0a0;
  font-weight: 600;
  font-size: 0.9rem;
  white-space: nowrap;
}

.change-item .badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.5rem 0.8rem;
  border-radius: 6px;
  text-transform: uppercase;
  white-space: nowrap;
}

/* ============ ALERTS ============ */
.alert-custom {
  background: rgba(255, 0, 87, 0.1);
  border: 1px solid rgba(255, 0, 87, 0.3);
  border-radius: 10px;
  padding: 1rem;
  font-weight: 500;
  font-size: 0.9rem;
  display: block;
  margin-bottom: 1rem;
  animation: slideIn 0.3s ease-out;
  line-height: 1.6;
}

.alert-custom i {
  margin-right: 0.5rem;
  vertical-align: text-top;
}

.alert-custom strong {
  font-weight: 700;
  margin-right: 0.2rem;
}

.alert-warning.alert-custom {
  background: rgba(255, 193, 7, 0.1);
  border-color: rgba(255, 193, 7, 0.3);
  color: #ffc107;
}

.alert-info.alert-custom {
  background: rgba(100, 200, 255, 0.1);
  border-color: rgba(100, 200, 255, 0.3);
  color: #64c8ff;
}

.alert-success.alert-custom {
  background: rgba(40, 167, 69, 0.1);
  border-color: rgba(40, 167, 69, 0.3);
  color: #7dd87d;
}

.alert-danger.alert-custom {
  background: rgba(220, 53, 69, 0.1);
  border-color: rgba(220, 53, 69, 0.3);
  color: #ff8787;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* ============ MODAL FOOTER ============ */
.modal-footer-custom {
  background: rgba(255, 0, 87, 0.02);
  border-top: 1px solid rgba(255, 0, 87, 0.1);
  padding: 1.5rem;
  border-radius: 0 0 16px 16px;
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  flex-shrink: 0;
}

.modal-footer-custom .btn {
  border-radius: 10px;
  font-weight: 600;
  padding: 0.8rem 1.8rem;
  transition: all 0.3s ease;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.modal-footer-custom .btn-primary {
  background: #ff4757;
  border: none;
  color: #fff;
  box-shadow: 0 6px 16px rgba(255, 0, 87, 0.25);
}

.modal-footer-custom .btn-primary:hover:not(:disabled) {
  background: #ff3842;
  box-shadow: 0 10px 24px rgba(255, 0, 87, 0.35);
  transform: translateY(-2px);
}

.modal-footer-custom .btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

.modal-footer-custom .btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer-custom .btn-outline-secondary {
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  background: transparent;
}

.modal-footer-custom .btn-outline-secondary:hover:not(:disabled) {
  background: rgba(255, 0, 87, 0.1);
  border-color: rgba(255, 0, 87, 0.6);
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.2em;
  border-color: rgba(255, 255, 255, 0.2) !important;
  border-right-color: #fff !important;
  animation: fa-spin 0.8s infinite linear;
}

@keyframes fa-spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* ============ RESPONSIVE DESIGN ============ */
@media (max-width: 768px) {
  .modal-custom {
    border-radius: 12px;
  }

  .modal-header-custom {
    padding: 1.25rem;
    border-radius: 12px 12px 0 0;
  }

  .modal-header-custom .modal-title {
    font-size: 1.1rem;
  }

  .modal-body-custom {
    padding: 1.5rem;
  }

  .user-info-card {
    padding: 1.25rem;
    margin-bottom: 1.25rem;
  }

  .avatar-circle,
  .avatar-image {
    width: 50px;
    height: 50px;
  }

  .form-label-custom {
    font-size: 0.9rem;
    margin-bottom: 0.75rem;
  }

  .role-selector {
    gap: 0.75rem;
    margin-bottom: 1.25rem;
  }

  .role-option {
    padding: 1rem;
  }

  .role-option-description {
    font-size: 0.8rem;
  }

  .changes-summary {
    padding: 1.25rem;
    margin-bottom: 1.25rem;
  }

  .alert-custom {
    font-size: 0.85rem;
    padding: 0.85rem;
    margin-bottom: 0.75rem;
  }

  .modal-footer-custom {
    padding: 1.25rem;
    gap: 0.75rem;
  }

  .modal-footer-custom .btn {
    padding: 0.7rem 1.4rem;
    font-size: 0.85rem;
  }
}

@media (max-width: 576px) {
  .modal-dialog {
    margin: 0.5rem;
  }

  .modal-body-custom {
    padding: 1rem;
    max-height: 70vh;
  }

  .user-info-card {
    padding: 1rem;
    margin-bottom: 1rem;
  }

  .avatar-circle,
  .avatar-image {
    width: 45px;
    height: 45px;
  }

  .user-info-card small {
    font-size: 0.75rem;
  }

  .form-label-custom {
    font-size: 0.85rem;
    margin-bottom: 0.65rem;
  }

  .role-option {
    padding: 0.9rem;
  }

  .form-check-input {
    width: 1.1rem;
    height: 1.1rem;
  }

  .role-option-description {
    font-size: 0.75rem;
  }

  .changes-summary {
    padding: 1rem;
    margin-bottom: 1rem;
  }

  .alert-custom {
    font-size: 0.8rem;
    padding: 0.75rem;
    margin-bottom: 0.65rem;
  }

  .modal-footer-custom {
    padding: 1rem;
    gap: 0.5rem;
    flex-direction: column-reverse;
  }

  .modal-footer-custom .btn {
    width: 100%;
    padding: 0.75rem 1rem;
    font-size: 0.8rem;
    justify-content: center;
  }
}
</style>
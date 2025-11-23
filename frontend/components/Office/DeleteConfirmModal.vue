<template>
  <Teleport to="body">
    <div class="modal fade" id="deleteConfirmModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-custom">
          <!-- Header -->
          <div class="modal-header modal-header-custom">
            <h5 class="modal-title">
              <i class="fas fa-exclamation-circle me-2"></i>Confirmar Eliminación
            </h5>
            <button type="button" class="btn-close btn-close-custom" data-bs-dismiss="modal"></button>
          </div>

          <!-- Body -->
          <div class="modal-body modal-body-custom">
            <div v-if="user" class="text-center">
              <!-- Icono de advertencia -->
              <div class="danger-icon mb-4">
                <i class="fas fa-trash-alt"></i>
              </div>

              <!-- Información del usuario a eliminar -->
              <div class="user-delete-info mb-4">
                <div class="d-flex justify-content-center mb-3">
                  <div v-if="user.profilePhoto" class="avatar-image-delete">
                    <img :src="user.profilePhoto" :alt="user.name" class="avatar-img">
                  </div>
                  <div v-else class="avatar-circle-delete">
                    {{ user.name?.charAt(0)?.toUpperCase() || 'U' }}
                  </div>
                </div>

                <h6 class="text-light mb-1">{{ user.name }}</h6>
                <small class="text-muted d-block mb-3">{{ user.email }}</small>
              </div>

              <!-- Mensaje de advertencia -->
              <div class="alert alert-danger alert-custom mb-4">
                <i class="fas fa-exclamation-triangle me-2"></i>
                <strong>Esta acción es irreversible</strong>
              </div>

              <!-- Detalles que se perderán -->
              <div class="deletion-details mb-4 text-start">
                <p class="text-muted mb-3"><small><i class="fas fa-info-circle me-2" style="color: #fff;"></i>Se eliminarán los siguientes datos:</small></p>
                <ul class="list-unstyled">
                  <li class="deletion-item">
                    <i class="fas fa-user-circle me-2" style="color: #fff;"></i>
                    <span>Perfil de usuario</span>
                  </li>
                  <li class="deletion-item">
                    <i class="fas fa-envelope me-2" style="color: #fff;"></i>
                    <span>Cuenta de correo vinculada</span>
                  </li>
                  <li class="deletion-item">
                    <i class="fas fa-ticket-alt me-2" style="color: #fff;"></i>
                    <span>Historial de compras ({{ user.purchases?.length || 0 }} transacciones)</span>
                  </li>
                  <li class="deletion-item">
                    <i class="fas fa-key me-2" style="color: #fff;"></i>
                    <span>Credenciales de acceso</span>
                  </li>
                </ul>
              </div>

              <!-- Confirmación de tipeo -->
              <div class="confirmation-input mb-4">
                <label for="confirmDelete" class="form-label form-label-custom">
                  <small>Escribe <strong>confirmar</strong> para eliminar:</small>
                </label>
                <input
                  id="confirmDelete"
                  v-model="confirmText"
                  type="text"
                  class="form-control form-control-custom"
                  placeholder="Escribe 'confirmar'"
                  @keydown.enter="handleDelete"
                >
              </div>

              <!-- Mensaje de error -->
              <div v-if="error" class="alert alert-danger alert-custom mb-3">
                <i class="fas fa-exclamation-circle me-2"></i>{{ error }}
              </div>
            </div>

            <div v-else class="text-center text-muted py-4">
              <i class="fas fa-hourglass-half fa-2x mb-2 d-block"></i>
              Cargando información...
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
              class="btn btn-danger"
              @click="handleDelete"
              :disabled="isLoading || confirmText !== 'confirmar'"
            >
              <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="fas fa-trash-alt me-2"></i>
              {{ isLoading ? 'Eliminando...' : 'Eliminar Usuario' }}
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

const emit = defineEmits(['delete', 'close'])

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'http://localhost:8085'
const { getToken } = useAuth()

const confirmText = ref('')
const isLoading = ref(false)
const error = ref('')
let modalInstance = null
let eventListenersAdded = false

// Constantes freezeadas para optimización
const CONFIRM_KEYWORD = Object.freeze({
  text: 'confirmar'
})

const ERROR_MESSAGES = Object.freeze({
  noAuth: 'No hay token de autenticación',
  noUserId: 'No se pudo obtener el ID del usuario',
  deleteFailed: 'Error al eliminar usuario'
})

const handleDelete = async () => {
  if (confirmText.value !== CONFIRM_KEYWORD.text || !props.user) return

  isLoading.value = true
  error.value = ''

  try {
    const token = getToken()
    if (!token) throw new Error(ERROR_MESSAGES.noAuth)

    const userId = props.user?._id || props.user?.id
    if (!userId) throw new Error(ERROR_MESSAGES.noUserId)

    await $fetch(`${API_BASE}/api/auth/users/${userId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` }
    })

    confirmText.value = ''
    emit('delete')
    
    requestAnimationFrame(() => {
      setTimeout(() => {
        closeModal()
      }, 600)
    })
  } catch (err) {
    error.value = err.data?.message || err.message || ERROR_MESSAGES.deleteFailed
  } finally {
    isLoading.value = false
  }
}

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide()
  }
}

const resetForm = () => {
  confirmText.value = ''
  error.value = ''
}

// Lifecycle optimizado para SSR - solo se ejecuta en cliente
onMounted(() => {
  if (process.client && !eventListenersAdded) {
    const modalElement = document.getElementById('deleteConfirmModal')
    if (modalElement) {
      // Inicializar Modal de Bootstrap de forma eficiente
      modalInstance = new bootstrap.Modal(modalElement, {
        backdrop: 'static',
        keyboard: false
      })

      const handleHidden = () => {
        resetForm()
      }

      // Usar passive: true para mejor performance en eventos
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
  border: 1px solid rgba(220, 53, 69, 0.2);
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
  border-bottom: 1px solid rgba(220, 53, 69, 0.3);
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
  scrollbar-color: rgba(220, 53, 69, 0.3) transparent;
}

.modal-body-custom::-webkit-scrollbar {
  width: 6px;
}

.modal-body-custom::-webkit-scrollbar-track {
  background: transparent;
}

.modal-body-custom::-webkit-scrollbar-thumb {
  background: rgba(220, 53, 69, 0.3);
  border-radius: 3px;
}

.modal-body-custom::-webkit-scrollbar-thumb:hover {
  background: rgba(220, 53, 69, 0.5);
}

/* ============ DANGER ICON ============ */
.danger-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: rgba(220, 53, 69, 0.15);
  border: 2px solid rgba(220, 53, 69, 0.3);
  animation: pulse-danger 2s infinite;
}

.danger-icon i {
  font-size: 2rem;
  color: #ff6b6b;
}

@keyframes pulse-danger {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

/* ============ USER INFO CARD ============ */
.user-delete-info {
  background: rgba(220, 53, 69, 0.05);
  border: 1px solid rgba(220, 53, 69, 0.15);
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.avatar-circle-delete {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  font-size: 1.3rem;
  background: #dc3545;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.2);
  flex-shrink: 0;
}

.avatar-image-delete {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.2);
  border: 2px solid rgba(220, 53, 69, 0.3);
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-delete-info h6 {
  color: #fff;
  font-size: 1rem;
  margin: 0;
}

.user-delete-info small {
  color: #e0e0e0 !important;
  font-size: 0.85rem;
}

/* ============ ALERTS ============ */
.alert-custom {
  background: rgba(220, 53, 69, 0.1);
  border: 1px solid rgba(220, 53, 69, 0.3);
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

/* ============ DELETION DETAILS ============ */
.deletion-details {
  background: rgba(220, 53, 69, 0.05);
  border-left: 3px solid rgba(220, 53, 69, 0.3);
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.deletion-details p {
  color: #a0a0a0;
  margin-bottom: 1rem;
  font-size: 0.85rem;
}

.deletion-item {
  color: #e5e7eb;
  padding: 0.5rem 0;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  list-style: none;
}

.deletion-item:hover {
  color: #fff;
  padding-left: 0.5rem;
}

.deletion-item i {
  color: #fff;
  min-width: 20px;
}

/* ============ FORM LABELS ============ */
.form-label-custom {
  color: #a0a0a0;
  font-weight: 600;
  margin-bottom: 0.75rem;
  display: flex;
  font-size: 0.85rem;
}

.form-label-custom strong {
  color: #ff6b6b;
  margin: 0 0.25rem;
}

/* ============ CONFIRMATION INPUT ============ */
.confirmation-input {
  background: rgba(220, 53, 69, 0.05);
  border: 1px solid rgba(220, 53, 69, 0.15);
  border-radius: 12px;
  padding: 1.25rem;
  margin-bottom: 1rem;
}

.form-control-custom {
  background: #0f0f0f;
  border: 1px solid rgba(220, 53, 69, 0.2);
  color: #fff;
  border-radius: 10px;
  padding: 0.9rem 1rem;
  font-family: 'Poppins', sans-serif;
  font-size: 0.95rem;
  transition: all 0.3s ease;
}

.form-control-custom::placeholder {
  color: #666;
  opacity: 1;
}

.form-control-custom:focus {
  background: #151515;
  border-color: rgba(220, 53, 69, 0.6);
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.15);
  color: #fff;
  outline: none;
}

/* ============ MODAL FOOTER ============ */
.modal-footer-custom {
  background: rgba(220, 53, 69, 0.02);
  border-top: 1px solid rgba(220, 53, 69, 0.1);
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

.modal-footer-custom .btn-danger {
  background: #dc3545;
  border: none;
  color: #fff;
  box-shadow: 0 6px 16px rgba(220, 53, 69, 0.25);
}

.modal-footer-custom .btn-danger:hover:not(:disabled) {
  background: #c82333;
  box-shadow: 0 10px 24px rgba(220, 53, 69, 0.35);
  transform: translateY(-2px);
}

.modal-footer-custom .btn-danger:active:not(:disabled) {
  transform: translateY(0);
}

.modal-footer-custom .btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer-custom .btn-outline-secondary {
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  background: transparent;
}

.modal-footer-custom .btn-outline-secondary:hover:not(:disabled) {
  background: rgba(220, 53, 69, 0.1);
  border-color: rgba(220, 53, 69, 0.6);
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

  .danger-icon {
    width: 60px;
    height: 60px;
  }

  .danger-icon i {
    font-size: 1.8rem;
  }

  .user-delete-info {
    padding: 1.25rem;
    margin-bottom: 1.25rem;
  }

  .avatar-circle-delete,
  .avatar-image-delete {
    width: 50px;
    height: 50px;
  }

  .user-delete-info h6 {
    font-size: 0.95rem;
  }

  .deletion-details {
    padding: 0.9rem;
    margin-bottom: 1.25rem;
  }

  .deletion-item {
    font-size: 0.85rem;
    padding: 0.4rem 0;
  }

  .form-label-custom {
    font-size: 0.8rem;
    margin-bottom: 0.65rem;
  }

  .form-control-custom {
    padding: 0.8rem 0.9rem;
    font-size: 0.9rem;
  }

  .confirmation-input {
    padding: 1rem;
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

  .modal-header-custom {
    padding: 1rem;
  }

  .modal-header-custom .modal-title {
    font-size: 1rem;
  }

  .danger-icon {
    width: 55px;
    height: 55px;
  }

  .danger-icon i {
    font-size: 1.6rem;
  }

  .user-delete-info {
    padding: 1rem;
    margin-bottom: 1rem;
  }

  .avatar-circle-delete,
  .avatar-image-delete {
    width: 45px;
    height: 45px;
  }

  .user-delete-info h6 {
    font-size: 0.9rem;
  }

  .user-delete-info small {
    font-size: 0.75rem;
  }

  .deletion-details {
    padding: 0.8rem;
    margin-bottom: 1rem;
    border-left-width: 2px;
  }

  .deletion-details p {
    font-size: 0.75rem;
    margin-bottom: 0.75rem;
  }

  .deletion-item {
    font-size: 0.8rem;
    padding: 0.35rem 0;
  }

  .form-label-custom {
    font-size: 0.75rem;
    margin-bottom: 0.6rem;
  }

  .form-control-custom {
    padding: 0.7rem 0.8rem;
    font-size: 0.85rem;
  }

  .confirmation-input {
    padding: 0.9rem;
    margin-bottom: 0.75rem;
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
<template>
  <div class="modal fade" id="deleteConfirmModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content modal-custom">
        <!-- Header -->
        <div class="modal-header modal-header-custom modal-header-danger">
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
              <p class="text-muted mb-3"><small><i class="fas fa-info-circle me-2"></i>Se eliminarán los siguientes datos:</small></p>
              <ul class="list-unstyled">
                <li class="deletion-item">
                  <i class="fas fa-user-circle me-2 text-muted"></i>
                  <span>Perfil de usuario</span>
                </li>
                <li class="deletion-item">
                  <i class="fas fa-envelope me-2 text-muted"></i>
                  <span>Cuenta de correo vinculada</span>
                </li>
                <li class="deletion-item">
                  <i class="fas fa-ticket-alt me-2 text-muted"></i>
                  <span>Historial de compras ({{ user.purchases?.length || 0 }} transacciones)</span>
                </li>
                <li class="deletion-item">
                  <i class="fas fa-key me-2 text-muted"></i>
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

const handleDelete = async () => {
  if (confirmText.value !== 'confirmar' || !props.user) return

  isLoading.value = true
  error.value = ''

  try {
    const token = getToken()
    if (!token) throw new Error('No hay token de autenticación')

    await $fetch(`${API_BASE}/api/auth/users/${props.user._id}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${token}` }
    })

    confirmText.value = ''
    emit('delete')
    closeModal()
  } catch (err) {
    error.value = err.data?.message || err.message || 'Error al eliminar usuario'
    console.error('Error:', err)
  } finally {
    isLoading.value = false
  }
}

const closeModal = () => {
  const modal = bootstrap.Modal.getInstance(document.getElementById('deleteConfirmModal'))
  modal?.hide()
}

onMounted(() => {
  const modalElement = document.getElementById('deleteConfirmModal')
  modalElement?.addEventListener('hidden.bs.modal', () => {
    confirmText.value = ''
    error.value = ''
  })
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap');

.modal-custom {
  background: linear-gradient(135deg, #1a1a1a 0%, #151515 100%);
  border: 1px solid rgba(220, 53, 69, 0.2);
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  color: #fff;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.5);
}

.modal-header-custom {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.1) 0%, rgba(255, 107, 107, 0.05) 100%);
  border-bottom: 1px solid rgba(220, 53, 69, 0.2);
  padding: 1.5rem;
  border-radius: 12px 12px 0 0;
}

.modal-header-danger {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.15) 0%, rgba(255, 107, 107, 0.08) 100%);
}

.modal-header-custom .modal-title {
  color: #ff6b6b;
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

.danger-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.15) 0%, rgba(255, 107, 107, 0.08) 100%);
  border: 2px solid rgba(220, 53, 69, 0.3);
}

.danger-icon i {
  font-size: 2rem;
  color: #ff6b6b;
}

.user-delete-info {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.05) 0%, rgba(255, 107, 107, 0.02) 100%);
  border: 1px solid rgba(220, 53, 69, 0.15);
  border-radius: 10px;
  padding: 1.5rem;
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
  font-size: 1.4rem;
  background: linear-gradient(135deg, #ff0057, #ff6b35);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
}

.avatar-image-delete {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
  border: 2px solid rgba(255, 0, 87, 0.3);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.alert-custom {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.1) 0%, rgba(255, 107, 107, 0.05) 100%);
  border: 1px solid rgba(220, 53, 69, 0.3);
  border-radius: 8px;
  padding: 1rem;
  font-weight: 500;
}

.alert-danger.alert-custom {
  color: #ff8787;
}

.deletion-details {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.03) 0%, rgba(0, 0, 0, 0.02) 100%);
  border-left: 3px solid rgba(220, 53, 69, 0.3);
  padding: 1rem;
  border-radius: 8px;
}

.deletion-item {
  color: #e5e7eb;
  padding: 0.5rem 0;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}

.deletion-item:hover {
  color: #fff;
  padding-left: 0.5rem;
}

.deletion-item i {
  color: #ff6b6b;
}

.confirmation-input {
  background: linear-gradient(135deg, #0f0f0f 0%, #0a0a0a 100%);
  border: 1px solid rgba(220, 53, 69, 0.2);
  border-radius: 10px;
  padding: 1.25rem;
}

.form-label-custom {
  color: #a0a0a0;
  font-weight: 600;
  margin-bottom: 0.75rem;
  display: flex;
  font-size: 0.85rem;
}

.form-label-custom strong {
  color: #ff6b6b;
}

.form-control-custom {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.02) 0%, rgba(0, 0, 0, 0.02) 100%);
  border: 1px solid rgba(220, 53, 69, 0.3);
  color: #fff;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
}

.form-control-custom::placeholder {
  color: #666;
}

.form-control-custom:focus {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.05) 0%, rgba(0, 0, 0, 0.02) 100%);
  border-color: rgba(220, 53, 69, 0.6);
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
  color: #fff;
  outline: none;
}

.modal-footer-custom {
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.02) 0%, rgba(0, 0, 0, 0.05) 100%);
  border-top: 1px solid rgba(220, 53, 69, 0.1);
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

.modal-footer-custom .btn-danger {
  background: linear-gradient(135deg, #dc3545, #e63950);
  border: none;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.2);
}

.modal-footer-custom .btn-danger:hover:not(:disabled) {
  background: linear-gradient(135deg, #c82333, #d62840);
  box-shadow: 0 8px 20px rgba(220, 53, 69, 0.3);
  transform: translateY(-2px);
}

.modal-footer-custom .btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.modal-footer-custom .btn-outline-secondary {
  border: 1.5px solid rgba(255, 255, 255, 0.3);
  color: #fff;
}

.modal-footer-custom .btn-outline-secondary:hover:not(:disabled) {
  background: rgba(220, 53, 69, 0.1);
  border-color: rgba(220, 53, 69, 0.6);
}

.spinner-border-sm {
  width: 1rem;
  height: 1rem;
  border-width: 0.2em;
  border-color: rgba(220, 53, 69, 0.2) !important;
  border-right-color: #ff6b6b !important;
}
</style>
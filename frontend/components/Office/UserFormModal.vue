[PUT] "http://localhost:8085/api/auth/users/undefined": 403<template>
  <Teleport to="body">
    <div class="modal fade" id="userFormModal" tabindex="-1" data-bs-backdrop="static" data-bs-keyboard="false">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-custom">
          <!-- Header -->
          <div class="modal-header modal-header-custom">
            <h5 class="modal-title">
              <i :class="isEditing ? 'fas fa-edit me-2' : 'fas fa-user-plus me-2'"></i>
              {{ isEditing ? 'Editar Usuario' : 'Agregar Nuevo Usuario' }}
            </h5>
            <button type="button" class="btn-close btn-close-custom" data-bs-dismiss="modal"></button>
          </div>

          <!-- Body -->
          <div class="modal-body modal-body-custom">
            <form @submit.prevent="handleSubmit">
              <!-- Nombre -->
              <div class="mb-3">
                <label for="userName" class="form-label form-label-custom">
                  <i class="fas fa-user me-2"></i>Nombre Completo
                </label>
                <input
                  id="userName"
                  v-model="form.name"
                  type="text"
                  class="form-control form-control-custom"
                  :class="{ 'is-invalid': errors.name }"
                  placeholder="Ej: Juan Pérez"
                  required
                  @blur="validateField('name')"
                >
                <div v-if="errors.name" class="invalid-feedback d-block mt-1">
                  <i class="fas fa-exclamation-circle me-1"></i>{{ errors.name }}
                </div>
              </div>

              <!-- Email -->
              <div class="mb-3">
                <label for="userEmail" class="form-label form-label-custom">
                  <i class="fas fa-envelope me-2"></i>Correo Electrónico
                </label>
                <input
                  id="userEmail"
                  v-model="form.email"
                  type="email"
                  class="form-control form-control-custom"
                  :class="{ 'is-invalid': errors.email }"
                  placeholder="usuario@ejemplo.com"
                  :required="!isEditing"
                  @blur="validateField('email')"
                >
                <div v-if="errors.email" class="invalid-feedback d-block mt-1">
                  <i class="fas fa-exclamation-circle me-1"></i>{{ errors.email }}
                </div>
                <small v-if="isEditing" class="helper-text d-block mt-1">
                  <i class="fas fa-info-circle me-1"></i>Dejar en blanco para no cambiar el email
                </small>
              </div>

              <!-- Contraseña -->
              <div class="mb-3">
                <label for="userPassword" class="form-label form-label-custom">
                  <i class="fas fa-lock me-2"></i>Contraseña
                </label>
                <div class="input-group">
                  <input
                    id="userPassword"
                    v-model="form.password"
                    :type="showPassword ? 'text' : 'password'"
                    class="form-control form-control-custom"
                    :class="{ 'is-invalid': errors.password }"
                    :placeholder="isEditing ? 'Dejar en blanco para no cambiar' : 'Mínimo 8 caracteres'"
                    :required="!isEditing"
                    @blur="validateField('password')"
                  >
                  <button
                    type="button"
                    class="btn btn-outline-secondary btn-password-toggle"
                    @click="showPassword = !showPassword"
                  >
                    <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                  </button>
                </div>
                <div v-if="errors.password" class="invalid-feedback d-block mt-1">
                  <i class="fas fa-exclamation-circle me-1"></i>{{ errors.password }}
                </div>
                <small v-if="isEditing" class="helper-text d-block mt-1">
                  <i class="fas fa-info-circle me-1"></i>Dejar en blanco para no cambiar la contraseña
                </small>
              </div>

              <!-- Confirmar Contraseña -->
              <div v-if="form.password" class="mb-3">
                <label for="confirmPassword" class="form-label form-label-custom">
                  <i class="fas fa-lock me-2"></i>Confirmar Contraseña
                </label>
                <input
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  type="password"
                  class="form-control form-control-custom"
                  :class="{ 'is-invalid': errors.confirmPassword }"
                  placeholder="Repite la contraseña"
                  @blur="validateField('confirmPassword')"
                >
                <div v-if="errors.confirmPassword" class="invalid-feedback d-block mt-1">
                  <i class="fas fa-exclamation-circle me-1"></i>{{ errors.confirmPassword }}
                </div>
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
              :disabled="isLoading || !isFormValid"
            >
              <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else :class="isEditing ? 'fas fa-save me-2' : 'fas fa-plus me-2'"></i>
              {{ isLoading ? 'Guardando...' : (isEditing ? 'Guardar Cambios' : 'Crear Usuario') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  editingUser: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['save', 'close'])

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase || 'http://localhost:8085'
const { getToken } = useAuth()

// Estado del formulario
const showPassword = ref(false)
const isLoading = ref(false)
const submitError = ref('')
const submitSuccess = ref('')
let modalInstance = null
let eventListenersAdded = false

const form = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errors = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isEditing = computed(() => !!props.editingUser)

const isFormValid = computed(() => {
  if (!form.value.name?.trim()) return false
  if (!isEditing.value && !form.value.email?.trim()) return false
  if (!isEditing.value && !form.value.password) return false
  if (form.value.password && form.value.password.length < 8) return false
  if (form.value.password && form.value.password !== form.value.confirmPassword) return false
  return !Object.values(errors.value).some(err => err)
})

// Patrones de validación como const freezeado - para rendimiento
const VALIDATION_PATTERNS = Object.freeze({
  email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
})

const FIELD_CONSTRAINTS = Object.freeze({
  name: { min: 3, max: 100 },
  email: { max: 120 },
  password: { min: 8, max: 50 }
})

const VALIDATION_MESSAGES = Object.freeze({
  name: {
    required: 'El nombre es requerido',
    min: 'El nombre debe tener al menos 3 caracteres',
    max: 'El nombre no puede exceder 100 caracteres'
  },
  email: {
    invalid: 'El email no es válido',
    max: 'El email no puede exceder 120 caracteres'
  },
  password: {
    min: 'La contraseña debe tener al menos 8 caracteres',
    max: 'La contraseña no puede exceder 50 caracteres'
  },
  confirmPassword: {
    mismatch: 'Las contraseñas no coinciden'
  }
})

// Validaciones optimizadas
const validateField = (field) => {
  errors.value[field] = ''

  switch (field) {
    case 'name': {
      const trimmedName = form.value.name?.trim() || ''
      if (!trimmedName) {
        errors.value.name = VALIDATION_MESSAGES.name.required
      } else if (trimmedName.length < FIELD_CONSTRAINTS.name.min) {
        errors.value.name = VALIDATION_MESSAGES.name.min
      } else if (trimmedName.length > FIELD_CONSTRAINTS.name.max) {
        errors.value.name = VALIDATION_MESSAGES.name.max
      }
      break
    }

    case 'email':
      if (form.value.email && !VALIDATION_PATTERNS.email.test(form.value.email)) {
        errors.value.email = VALIDATION_MESSAGES.email.invalid
      } else if (form.value.email?.length > FIELD_CONSTRAINTS.email.max) {
        errors.value.email = VALIDATION_MESSAGES.email.max
      }
      break

    case 'password':
      if (form.value.password && form.value.password.length < FIELD_CONSTRAINTS.password.min) {
        errors.value.password = VALIDATION_MESSAGES.password.min
      } else if (form.value.password && form.value.password.length > FIELD_CONSTRAINTS.password.max) {
        errors.value.password = VALIDATION_MESSAGES.password.max
      }
      break

    case 'confirmPassword':
      if (form.value.password && form.value.confirmPassword !== form.value.password) {
        errors.value.confirmPassword = VALIDATION_MESSAGES.confirmPassword.mismatch
      }
      break
  }
}

// Manejo del envío optimizado
const handleSubmit = async () => {
  if (!isFormValid.value) return

  isLoading.value = true
  submitError.value = ''
  submitSuccess.value = ''

  try {
    const token = getToken()
    if (!token) throw new Error('No hay token de autenticación')

    const payload = {
      name: form.value.name.trim(),
      ...(form.value.email && { email: form.value.email.trim() }),
      ...(form.value.password && { password: form.value.password })
    }

    let response
    if (isEditing.value) {
      const userId = props.editingUser?._id || props.editingUser?.id
      if (!userId) {
        throw new Error('No se pudo obtener el ID del usuario')
      }
      response = await $fetch(`${API_BASE}/api/auth/users/${userId}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: payload
      })
    } else {
      response = await $fetch(`${API_BASE}/api/auth/users`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: { ...payload, role: 'user' }
      })
    }

    submitSuccess.value = isEditing.value ? 'Usuario actualizado correctamente' : 'Usuario creado correctamente'
    
    // Emitir evento y cerrar modal con timing optimizado
    emit('save', response)
    
    requestAnimationFrame(() => {
      setTimeout(() => {
        closeModal()
      }, 600)
    })
  } catch (error) {
    submitError.value = error.data?.message || error.message || 'Error al guardar usuario'
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
  form.value = { name: '', email: '', password: '', confirmPassword: '' }
  errors.value = { name: '', email: '', password: '', confirmPassword: '' }
  showPassword.value = false
  submitError.value = ''
  submitSuccess.value = ''
}

// Watcher optimizado - flush post para mejor rendimiento en SSR
watch(
  () => props.editingUser,
  (newUser) => {
    if (newUser) {
      form.value = {
        name: newUser.name || '',
        email: newUser.email || '',
        password: '',
        confirmPassword: ''
      }
    } else {
      resetForm()
    }
  },
  { immediate: true, flush: 'post' }
)

// Lifecycle optimizado para SSR - solo se ejecuta en cliente
onMounted(() => {
  if (process.client && !eventListenersAdded) {
    const modalElement = document.getElementById('userFormModal')
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

/* ============ FORM CONTROLS ============ */
.form-control-custom {
  background: #0f0f0f;
  border: 1px solid rgba(255, 0, 87, 0.2);
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
  border-color: rgba(255, 0, 87, 0.6);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.15);
  color: #fff;
  outline: none;
}

.form-control-custom.is-invalid {
  border-color: #ff6b6b;
  background: rgba(255, 107, 107, 0.05);
}

.form-control-custom.is-invalid:focus {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15);
}

/* ============ INPUT GROUP & PASSWORD TOGGLE ============ */
.input-group .form-control-custom {
  border-right: none;
  border-radius: 10px 0 0 10px;
}

.btn-password-toggle {
  border: 1px solid rgba(255, 0, 87, 0.2);
  border-left: none;
  border-radius: 0 10px 10px 0;
  color: #a0a0a0;
  background: #0f0f0f;
  transition: all 0.3s ease;
  padding: 0.9rem 1rem;
}

.btn-password-toggle:hover,
.btn-password-toggle:focus {
  background: #151515;
  border-color: rgba(255, 0, 87, 0.5);
  color: #ff0057;
  box-shadow: none;
}

/* ============ VALIDATION FEEDBACK ============ */
.invalid-feedback {
  color: #ff6b6b;
  font-size: 0.85rem;
  font-weight: 500;
  display: block;
  margin-top: 0.5rem;
  animation: slideIn 0.2s ease-out;
}

/* ============ FORM HELPER TEXT ============ */
small {
  font-size: 0.8rem;
  color: #a0a0a0;
  display: block;
  margin-top: 0.5rem;
}

.helper-text {
  font-size: 0.8rem;
  color: #b0b0b0;
  display: block;
  margin-top: 0.5rem;
  font-weight: 500;
}

.helper-text i {
  color: #64c8ff;
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

  .form-label-custom {
    font-size: 0.9rem;
    margin-bottom: 0.75rem;
  }

  .form-control-custom,
  .btn-password-toggle {
    padding: 0.8rem 0.9rem;
    font-size: 0.9rem;
  }

  .invalid-feedback {
    font-size: 0.8rem;
  }

  small {
    font-size: 0.75rem;
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

  .form-label-custom {
    font-size: 0.85rem;
    margin-bottom: 0.65rem;
    text-transform: none;
    letter-spacing: 0;
  }

  .form-control-custom,
  .btn-password-toggle {
    padding: 0.7rem 0.8rem;
    font-size: 0.85rem;
  }

  .input-group .form-control-custom {
    border-radius: 8px 0 0 8px;
  }

  .btn-password-toggle {
    border-radius: 0 8px 8px 0;
  }

  .invalid-feedback {
    font-size: 0.75rem;
    margin-top: 0.4rem;
  }

  small {
    font-size: 0.7rem;
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
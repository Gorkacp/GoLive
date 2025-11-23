<template>
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
              <small v-if="isEditing" class="text-muted d-block mt-1">
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
              <small v-if="isEditing" class="text-muted d-block mt-1">
                <i class="fas fa-info-circle me-1"></i>Dejar en blanco para no cambiar la contraseña
              </small>
            </div>

            <!-- Confirmar Contraseña -->
            <div class="mb-3" v-if="form.password">
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

            <!-- Mensaje informativo -->
            <div v-if="submitError" class="alert alert-danger alert-custom mt-3">
              <i class="fas fa-exclamation-triangle me-2"></i>{{ submitError }}
            </div>

            <div v-if="submitSuccess" class="alert alert-success alert-custom mt-3">
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

const form = ref({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  role: 'user'
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

// Validaciones
const validateField = (field) => {
  errors.value[field] = ''

  switch (field) {
    case 'name':
      if (!form.value.name?.trim()) {
        errors.value.name = 'El nombre es requerido'
      } else if (form.value.name.trim().length < 3) {
        errors.value.name = 'El nombre debe tener al menos 3 caracteres'
      } else if (form.value.name.trim().length > 100) {
        errors.value.name = 'El nombre no puede exceder 100 caracteres'
      }
      break

    case 'email':
      if (form.value.email && !isValidEmail(form.value.email)) {
        errors.value.email = 'El email no es válido'
      } else if (form.value.email?.length > 120) {
        errors.value.email = 'El email no puede exceder 120 caracteres'
      }
      break

    case 'password':
      if (form.value.password && form.value.password.length < 8) {
        errors.value.password = 'La contraseña debe tener al menos 8 caracteres'
      } else if (form.value.password && form.value.password.length > 50) {
        errors.value.password = 'La contraseña no puede exceder 50 caracteres'
      }
      break

    case 'confirmPassword':
      if (form.value.password && form.value.confirmPassword !== form.value.password) {
        errors.value.confirmPassword = 'Las contraseñas no coinciden'
      }
      break
  }
}

const isValidEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

// Manejo del envío
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
      response = await $fetch(`${API_BASE}/api/auth/users/${props.editingUser._id}`, {
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
    setTimeout(() => {
      emit('save', response)
      resetForm()
      closeModal()
    }, 1500)
  } catch (error) {
    submitError.value = error.data?.message || error.message || 'Error al guardar usuario'
    console.error('Error:', error)
  } finally {
    isLoading.value = false
  }
}

const resetForm = () => {
  form.value = { name: '', email: '', password: '', confirmPassword: '', role: 'user' }
  errors.value = { name: '', email: '', password: '', confirmPassword: '' }
  showPassword.value = false
  submitError.value = ''
  submitSuccess.value = ''
}

const closeModal = () => {
  const modal = bootstrap.Modal.getInstance(document.getElementById('userFormModal'))
  modal?.hide()
}

// Watchers
watch(() => props.editingUser, (newUser) => {
  if (newUser) {
    form.value = {
      name: newUser.name || '',
      email: newUser.email || '',
      password: '',
      confirmPassword: '',
      role: newUser.role || 'user'
    }
  } else {
    resetForm()
  }
}, { deep: true })

// Limpiar errores cuando el modal se cierra
onMounted(() => {
  const modalElement = document.getElementById('userFormModal')
  modalElement?.addEventListener('hidden.bs.modal', resetForm)
})

onUnmounted(() => {
  const modalElement = document.getElementById('userFormModal')
  modalElement?.removeEventListener('hidden.bs.modal', resetForm)
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

.form-label-custom {
  color: #fff;
  font-weight: 600;
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  font-size: 0.95rem;
}

.form-label-custom i {
  color: #ff0057;
}

.form-control-custom {
  background: linear-gradient(135deg, #0f0f0f 0%, #0a0a0a 100%);
  border: 1px solid rgba(255, 0, 87, 0.2);
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
  background: linear-gradient(135deg, #151515 0%, #0f0f0f 100%);
  border-color: rgba(255, 0, 87, 0.6);
  box-shadow: 0 0 0 3px rgba(255, 0, 87, 0.1);
  color: #fff;
  outline: none;
}

.form-control-custom.is-invalid {
  border-color: #ff6b6b;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.05) 0%, rgba(255, 0, 87, 0.02) 100%);
}

.form-control-custom.is-invalid:focus {
  border-color: #ff6b6b;
  box-shadow: 0 0 0 3px rgba(255, 107, 107, 0.15);
}

.invalid-feedback {
  color: #ff6b6b;
  font-size: 0.85rem;
  font-weight: 500;
}

.input-group .btn-password-toggle {
  border-color: rgba(255, 0, 87, 0.2);
  color: #a0a0a0;
  background: linear-gradient(135deg, #0f0f0f 0%, #0a0a0a 100%);
  transition: all 0.3s ease;
}

.input-group .btn-password-toggle:hover,
.input-group .btn-password-toggle:focus {
  background: linear-gradient(135deg, #151515 0%, #0f0f0f 100%);
  border-color: rgba(255, 0, 87, 0.5);
  color: #ff0057;
  box-shadow: none;
}

.alert-custom {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.1) 0%, rgba(255, 107, 53, 0.05) 100%);
  border: 1px solid rgba(255, 0, 87, 0.3);
  border-radius: 8px;
  padding: 1rem;
  font-weight: 500;
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

small {
  font-size: 0.8rem;
  color: #a0a0a0;
}
</style>
<template>
  <div class="perfil-page">
    <Header />
    <div class="perfil-container">
      <!-- Encabezado del perfil -->
      <div class="perfil-header">
        <div class="header-content">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <div class="avatar" v-if="userData.profilePhoto" @click="triggerPhotoUpload" :class="{ 'avatar-clickable': true }">
                <img :src="userData.profilePhoto" :alt="userData.name" class="profile-img">
                <div class="avatar-overlay">
                  <i class="fas fa-camera"></i>
                  <span>Cambiar foto</span>
                </div>
              </div>
              <div class="avatar avatar-empty" v-else @click="triggerPhotoUpload">
                <div class="empty-state">
                  <i class="fas fa-image"></i>
                  <p>Añadir foto</p>
                </div>
              </div>
              <input 
                ref="photoInput"
                type="file" 
                accept="image/*" 
                @change="handlePhotoChange"
                class="photo-input"
                :disabled="uploadingPhoto"
              />
            </div>
            <div class="user-info">
              <h1>{{ userData.name || 'Usuario' }}</h1>
              <span :class="['role-badge', userData.role]">
                {{ userData.role === 'super_user' ? 'Super Usuario' : 'Usuario' }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs de navegación -->
      <div class="perfil-tabs">
        <div class="tabs-wrapper">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            :class="['tab-btn', { active: activeTab === tab.id }]"
            @click="activeTab = tab.id"
          >
            <i :class="tab.icon"></i>
            {{ tab.label }}
          </button>
        </div>
      </div>

      <!-- Contenido de los tabs -->
      <div class="perfil-content">
        <!-- TAB: MIS DATOS -->
        <div v-show="activeTab === 'datos'" class="tab-pane active">
          <form @submit.prevent="updateProfile" class="form-section">
            <div style="margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #e5e7eb;">
              <h2 style="margin: 0;">Mis datos personales</h2>
            </div>

            <div class="form-grid">
              <div class="form-group">
                <label for="name">Nombre</label>
                <input
                  id="name"
                  v-model="formData.name"
                  type="text"
                  placeholder="Tu nombre"
                  :disabled="!editMode"
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="lastName">Apellidos</label>
                <input
                  id="lastName"
                  v-model="formData.lastName"
                  type="text"
                  placeholder="Tus apellidos"
                  :disabled="!editMode"
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="email">Correo electrónico</label>
                <input
                  id="email"
                  v-model="formData.email"
                  type="email"
                  placeholder="tu@email.com"
                  :disabled="!editMode"
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="phoneNumber">Teléfono</label>
                <input
                  id="phoneNumber"
                  v-model="formData.phoneNumber"
                  type="tel"
                  placeholder="+34 600 000 000"
                  :disabled="!editMode"
                  class="form-input"
                />
              </div>

              <div class="form-group">
                <label for="dateOfBirth">Fecha de nacimiento</label>
                <input
                  id="dateOfBirth"
                  v-model="formData.dateOfBirth"
                  type="date"
                  class="form-input"
                  style="cursor: pointer;"
                />
              </div>

              <div class="form-group">
                <label for="postalCode">Código postal</label>
                <input
                  id="postalCode"
                  v-model="formData.postalCode"
                  type="text"
                  placeholder="18400"
                  :disabled="!editMode"
                  class="form-input"
                />
              </div>

            </div>

            <!-- Botones en la siguiente fila del grid -->
            <div style="display: flex; gap: 15px; margin-top: 40px; margin-bottom: 60px; position: relative; z-index: 10;">
              <button 
                type="submit" 
                :disabled="savingProfile"
                style="flex: 1; padding: 16px; font-size: 18px; background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%) !important; color: white !important; border: none !important; border-radius: 12px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; font-family: 'Poppins', sans-serif; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 15px rgba(255, 0, 87, 0.5);"
              >
                <span v-if="savingProfile">
                  <i class="fas fa-spinner fa-spin"></i> Guardando...
                </span>
                <span v-else>
                  <i class="fas fa-check"></i> Actualizar datos
                </span>
              </button>
            </div>

            <div v-if="profileMessage" :class="['alert', profileMessage.type]">
              <i :class="profileMessage.icon"></i>
              {{ profileMessage.text }}
            </div>
          </form>
        </div>

        <!-- TAB: SEGURIDAD -->
        <div v-show="activeTab === 'seguridad'" class="tab-pane">
          <div class="form-section">
            <div style="margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #e5e7eb;">
              <h2 style="margin: 0;">Cambiar contraseña</h2>
            </div>

            <form @submit.prevent="changePassword" class="password-form">
              <!-- Contraseña actual -->
              <div class="form-group">
                <label for="currentPassword">
                  <i class="fas fa-lock"></i> Contraseña actual
                </label>
                <div class="password-input-wrapper">
                  <input
                    id="currentPassword"
                    v-model="passwordForm.currentPassword"
                    :type="showCurrentPassword ? 'text' : 'password'"
                    placeholder="Introduce tu contraseña actual"
                    class="form-input"
                    :disabled="changingPassword"
                  />
                  <button
                    type="button"
                    class="toggle-password"
                    @click="showCurrentPassword = !showCurrentPassword"
                    :disabled="changingPassword"
                    :title="showCurrentPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                  >
                    <i :class="showCurrentPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                  </button>
                </div>
                <small v-if="passwordForm.currentPassword" class="validation-hint" style="color: #6b7280; margin-top: 6px; display: block;">
                  <i class="fas fa-check-circle" style="color: #10b981;"></i> Campo completado
                </small>
              </div>

              <!-- Nueva contraseña -->
              <div class="form-group">
                <label for="newPassword">
                  <i class="fas fa-key"></i> Nueva contraseña
                </label>
                <div class="password-input-wrapper">
                  <input
                    id="newPassword"
                    v-model="passwordForm.newPassword"
                    :type="showNewPassword ? 'text' : 'password'"
                    placeholder="Introduce tu nueva contraseña"
                    class="form-input"
                    :disabled="changingPassword"
                  />
                  <button
                    type="button"
                    class="toggle-password"
                    @click="showNewPassword = !showNewPassword"
                    :disabled="changingPassword"
                    :title="showNewPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                  >
                    <i :class="showNewPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                  </button>
                </div>
                <div v-if="passwordForm.newPassword" class="password-strength">
                  <div class="strength-bar">
                    <div :class="['strength-fill', getPasswordStrength(passwordForm.newPassword).class]" :style="{ width: getPasswordStrength(passwordForm.newPassword).percent }"></div>
                  </div>
                  <p class="strength-text" :class="getPasswordStrength(passwordForm.newPassword).class">
                    Fortaleza: {{ getPasswordStrength(passwordForm.newPassword).text }}
                  </p>
                  <div v-if="passwordForm.currentPassword && (passwordForm.currentPassword?.trim?.() ?? '') === (passwordForm.newPassword?.trim?.() ?? '')" class="validation-warning" style="margin-top: 10px; padding: 12px 14px; background: rgba(220, 38, 38, 0.15); border-left: 4px solid #dc2626; border-radius: 8px; color: #7f1d1d; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 10px;">
                    <i class="fas fa-exclamation-circle" style="color: #dc2626; font-size: 16px; flex-shrink: 0;"></i> Debe ser diferente a la contraseña anterior
                  </div>
                  <div v-else-if="passwordForm.newPassword.length < 6" class="validation-warning" style="margin-top: 10px; padding: 12px 14px; background: rgba(249, 115, 22, 0.12); border-left: 4px solid #f97316; border-radius: 8px; color: #92400e; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 10px;">
                    <i class="fas fa-check-circle" style="color: #f97316; font-size: 16px; flex-shrink: 0;"></i> Mínimo 6 caracteres ({{ passwordForm.newPassword.length }}/6)
                  </div>
                  <div v-else class="validation-success" style="margin-top: 10px; padding: 12px 14px; background: rgba(16, 185, 129, 0.15); border-left: 4px solid #10b981; border-radius: 8px; color: #059669; font-size: 13px; font-weight: 600; display: flex; align-items: center; gap: 10px;">
                    <i class="fas fa-check-circle" style="color: #10b981; font-size: 16px; flex-shrink: 0;"></i> Contraseña válida
                  </div>
                </div>
              </div>

              <!-- Repetir contraseña -->
              <div class="form-group">
                <label for="confirmPassword">
                  <i class="fas fa-check"></i> Repetir contraseña
                </label>
                <div class="password-input-wrapper">
                  <input
                    id="confirmPassword"
                    v-model="passwordForm.confirmPassword"
                    :type="showConfirmPassword ? 'text' : 'password'"
                    placeholder="Repite tu nueva contraseña"
                    class="form-input"
                    :disabled="changingPassword"
                  />
                  <button
                    type="button"
                    class="toggle-password"
                    @click="showConfirmPassword = !showConfirmPassword"
                    :disabled="changingPassword"
                    :title="showConfirmPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
                  >
                    <i :class="showConfirmPassword ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
                  </button>
                </div>
                <div v-if="passwordForm.newPassword && passwordForm.confirmPassword" :class="['password-match', (passwordForm.newPassword?.trim?.() ?? '') === (passwordForm.confirmPassword?.trim?.() ?? '') ? 'success' : 'error']">
                  <i :class="(passwordForm.newPassword?.trim?.() ?? '') === (passwordForm.confirmPassword?.trim?.() ?? '') ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'"></i>
                  <span>
                    {{ (passwordForm.newPassword?.trim?.() ?? '') === (passwordForm.confirmPassword?.trim?.() ?? '') ? 'Las contraseñas coinciden' : 'Las contraseñas no coinciden' }}
                  </span>
                </div>
              </div>

              <!-- Mensaje de validación -->
              <div v-if="passwordMessage" :class="['alert', passwordMessage.type]">
                <i v-if="passwordMessage.type === 'alert-success'" class="fas fa-check-circle"></i>
                {{ passwordMessage.text }}
              </div>

              <!-- Botón -->
              <div style="display: flex; gap: 15px; margin-top: 40px; margin-bottom: 60px; position: relative; z-index: 10;">
                <button 
                  type="submit" 
                  :disabled="!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword || passwordForm.newPassword.length < 6 || (passwordForm.newPassword?.trim?.() ?? '') !== (passwordForm.confirmPassword?.trim?.() ?? '') || (passwordForm.currentPassword?.trim?.() ?? '') === (passwordForm.newPassword?.trim?.() ?? '') || changingPassword"
                  style="flex: 1; padding: 16px; font-size: 18px; background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%) !important; color: white !important; border: none !important; border-radius: 12px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; font-family: 'Poppins', sans-serif; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 0 4px 15px rgba(255, 0, 87, 0.5);"
                  :title="!passwordForm.currentPassword ? 'Ingresa tu contraseña actual' : !passwordForm.newPassword ? 'Ingresa una nueva contraseña' : !passwordForm.confirmPassword ? 'Confirma tu nueva contraseña' : passwordForm.newPassword.length < 6 ? 'Mínimo 6 caracteres' : (passwordForm.newPassword?.trim?.() ?? '') !== (passwordForm.confirmPassword?.trim?.() ?? '') ? 'Las contraseñas no coinciden' : (passwordForm.currentPassword?.trim?.() ?? '') === (passwordForm.newPassword?.trim?.() ?? '') ? 'Debe ser diferente a la anterior' : ''"
                >
                  <span v-if="changingPassword">
                    <i class="fas fa-spinner fa-spin"></i> Actualizando...
                  </span>
                  <span v-else>
                    <i class="fas fa-check"></i> Actualizar contraseña
                  </span>
                </button>
              </div>
            </form>
          </div>
        </div>

        <!-- TAB: CUENTA -->
        <div v-show="activeTab === 'cuenta'" class="tab-pane">
          <div class="form-section">
            <div style="margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #e5e7eb;">
              <h2 style="margin: 0;">Gestión de cuenta</h2>
            </div>

            <div class="danger-card">
              <div class="danger-header">
                <div class="danger-icon">
                  <i class="fas fa-exclamation-triangle"></i>
                </div>
                <div class="danger-info">
                  <h3>Borrar mi cuenta</h3>
                  <p>Esta acción es irreversible. Se eliminarán todos tus datos permanentemente.</p>
                </div>
              </div>
              <div class="danger-details">
                <p style="font-weight: 600; margin-bottom: 12px; color: #0a0a0a;">Se eliminarán:</p>
                <ul class="delete-list">
                  <li>Tu perfil y datos personales</li>
                  <li>Tu historial de compras y transacciones</li>
                  <li>Acceso a tus entradas y eventos</li>
                  <li>Tus fotos de perfil y configuración</li>
                </ul>
              </div>
              <button
                type="button"
                class="btn-delete-account"
                @click="openDeleteModal"
              >
                <i class="fas fa-trash-alt"></i> Borrar mi cuenta
              </button>
            </div>

            <!-- Modal de confirmación para borrar cuenta -->
            <div class="modal fade" id="deleteModal" tabindex="-1">
              <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content modal-custom">
                    <div class="modal-header" style="border-bottom: 2px solid var(--danger-color); padding: 25px;">
                      <div class="modal-danger-header" style="margin: 0; width: 100%; display: flex; align-items: flex-start; gap: 16px;">
                        <div class="modal-danger-icon">
                          <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <div style="flex: 1;">
                          <h5 class="modal-danger-title">Confirmar eliminación de cuenta</h5>
                          <p class="modal-danger-subtitle">Esta acción es irreversible</p>
                        </div>
                      </div>
                      <button type="button" class="btn-close" data-bs-dismiss="modal" :disabled="deletingAccount"></button>
                    </div>
                    <div class="modal-body" style="padding: 25px;">
                      <div class="warning-box">
                        <i class="fas fa-exclamation-triangle"></i>
                        <div>
                          <p class="warning-text" style="margin: 0;">Estás a punto de borrar tu cuenta permanentemente. Esta acción no se puede deshacer.</p>
                        </div>
                      </div>

                    <div class="modal-info-box">
                      <div class="modal-info-title">
                        <i class="fas fa-trash-alt"></i>
                        Se eliminarán:
                      </div>
                      <ul class="delete-list-modal">
                        <li>Tu perfil y datos personales</li>
                        <li>Tu historial de compras y transacciones</li>
                        <li>Acceso a tus entradas y eventos</li>
                        <li>Tu cuenta en la plataforma</li>
                      </ul>
                    </div>

                    <div class="email-confirmation">
                      <label for="confirmEmail" class="email-confirmation-label">
                        Escribe tu correo para confirmar:
                      </label>
                      <input
                        id="confirmEmail"
                        v-model="deleteConfirmEmail"
                        type="email"
                        :placeholder="userData.email"
                        class="email-confirmation-input"
                        :class="{ confirmed: deleteConfirmEmail && deleteConfirmEmail === userData.email }"
                        :disabled="deletingAccount"
                      />
                      <div v-if="deleteConfirmEmail" class="email-confirmation-feedback" :class="{ valid: deleteConfirmEmail === userData.email, invalid: deleteConfirmEmail !== userData.email }">
                        <i :class="deleteConfirmEmail === userData.email ? 'fas fa-check-circle' : 'fas fa-times-circle'"></i>
                        <span v-if="deleteConfirmEmail === userData.email">Correo confirmado</span>
                        <span v-else>Correo no coincide</span>
                      </div>
                    </div>

                    <div class="email-confirmation" style="margin-top: 16px;">
                      <label for="deletePassword" class="email-confirmation-label">
                        Ingresa tu contraseña:
                      </label>
                      <div style="position: relative;">
                        <input
                          id="deletePassword"
                          v-model="deletePassword"
                          :type="showDeletePassword ? 'text' : 'password'"
                          placeholder="Tu contraseña"
                          class="email-confirmation-input"
                          :disabled="deletingAccount"
                        />
                        <button
                          type="button"
                          class="password-toggle-delete"
                          @click="showDeletePassword = !showDeletePassword"
                          :disabled="deletingAccount"
                        >
                          <i :class="showDeletePassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                        </button>
                      </div>
                      <div v-if="deletePassword" class="email-confirmation-feedback valid">
                        <i class="fas fa-check-circle"></i>
                        <span>Contraseña ingresada</span>
                      </div>
                    </div>
                  </div>
                  <div class="modal-footer" style="border-top: 1px solid var(--border-color); padding: 20px 25px; gap: 12px;">
                    <button
                      type="button"
                      class="btn btn-secondary"
                      data-bs-dismiss="modal"
                      :disabled="deletingAccount"
                    >
                      Cancelar
                    </button>
                    <button
                      type="button"
                      class="btn-delete-confirm"
                      @click="deleteAccount"
                      :disabled="deleteConfirmEmail !== userData.email || !deletePassword.trim() || deletingAccount"
                    >
                      <span v-if="deletingAccount">
                        <i class="fas fa-spinner fa-spin me-1"></i> Eliminando...
                      </span>
                      <span v-else>
                        <i class="fas fa-trash-alt me-1"></i> Sí, borrar mi cuenta
                      </span>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Header from '~/components/Header.vue'
import Footer from '~/components/Footer.vue'

// API Configuration
const config = useRuntimeConfig()
const apiBase = config.public.apiBase

// State Management
const activeTab = ref('datos')
const editMode = ref(true)  // ✅ Cambiar a true para que siempre esté en modo edición
const savingProfile = ref(false)
const changingPassword = ref(false)
const deletingAccount = ref(false)
const deleteConfirmEmail = ref('')
const deletePassword = ref('')
const showDeletePassword = ref(false)
const uploadingPhoto = ref(false)
const photoInput = ref(null)

// User Data
const userData = ref({
  name: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  dateOfBirth: '',
  postalCode: '',
  role: 'user',
  id: ''
})

const formData = ref({
  name: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  dateOfBirth: '',
  postalCode: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// Estado para mostrar/ocultar contraseñas
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// Messages
const profileMessage = ref(null)
const passwordMessage = ref(null)

// Tabs Configuration
const tabs = [
  { id: 'datos', label: 'Mis datos', icon: 'fas fa-user' },
  { id: 'seguridad', label: 'Seguridad', icon: 'fas fa-lock' },
  { id: 'cuenta', label: 'Cuenta', icon: 'fas fa-cog' }
]

// ========== LIFECYCLE ==========
onMounted(async () => {
  console.log('✅ Componente montado')
  // Actualizar título de la pestaña
  document.title = 'Mi perfil | GoLive'
  await loadUserData()
  console.log('✅ EditMode initial:', editMode.value)
})

// ========== MÉTODOS DE CARGA ==========
const loadUserData = async () => {
  const userString = localStorage.getItem('user')
  const token = localStorage.getItem('token')
  
  if (userString) {
    const user = JSON.parse(userString)
    
    // Si no tiene ID, obtenerlo del backend usando el token
    if (!user.id && token && user.email) {
      try {
        console.log('🔍 Buscando datos del usuario en el backend...')
        const response = await $fetch(`${apiBase}/api/auth/users`, {
          method: 'GET',
          headers: {
            'Authorization': token,
            'Content-Type': 'application/json'
          }
        })
        
        // Buscar el usuario en la lista por email
        const currentUser = response.find(u => u.email === user.email)
        if (currentUser) {
          user.id = currentUser.id
          user.lastName = currentUser.lastName || ''
          user.phoneNumber = currentUser.phoneNumber || ''
          user.dateOfBirth = currentUser.dateOfBirth || ''
          user.postalCode = currentUser.postalCode || ''
          user.profilePhoto = currentUser.profilePhoto || ''
          // Actualizar localStorage
          localStorage.setItem('user', JSON.stringify(user))
        }
      } catch (error) {
        console.error('❌ Error al obtener datos del usuario:', error)
      }
    }
    
    userData.value = {
      name: user.name || '',
      lastName: user.lastName || '',
      email: user.email || '',
      phoneNumber: user.phoneNumber || '',
      dateOfBirth: user.dateOfBirth || '',
      postalCode: user.postalCode || '',
      profilePhoto: user.profilePhoto || '',
      role: user.role || 'user',
      id: user.id || ''
    }
    formData.value = {
      name: userData.value.name,
      lastName: userData.value.lastName,
      email: userData.value.email,
      phoneNumber: userData.value.phoneNumber,
      dateOfBirth: userData.value.dateOfBirth,
      postalCode: userData.value.postalCode
    }
    console.log('✅ Datos cargados:', userData.value)
  }
}

// ========== MÉTODOS DE PERFIL ==========
const startEdit = () => {
  editMode.value = true
  profileMessage.value = null
}

const updateProfile = async () => {
  if (!formData.value.name || !formData.value.email) {
    profileMessage.value = {
      type: 'alert-error',
      text: 'Por favor completa los campos obligatorios',
      icon: 'fas fa-exclamation-circle'
    }
    return
  }

  savingProfile.value = true
  try {
    const token = localStorage.getItem('token')
    const userId = userData.value.id

    const response = await $fetch(`${apiBase}/api/auth/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json'
      },
      body: {
        name: formData.value.name,
        lastName: formData.value.lastName,
        email: formData.value.email,
        phoneNumber: formData.value.phoneNumber,
        dateOfBirth: formData.value.dateOfBirth,
        postalCode: formData.value.postalCode
      }
    })

    // Actualizar datos locales
    userData.value.name = response.name
    userData.value.lastName = response.lastName
    userData.value.email = response.email
    userData.value.phoneNumber = response.phoneNumber
    userData.value.dateOfBirth = response.dateOfBirth
    userData.value.postalCode = response.postalCode
    formData.value.name = response.name
    formData.value.lastName = response.lastName
    formData.value.email = response.email
    formData.value.phoneNumber = response.phoneNumber
    formData.value.dateOfBirth = response.dateOfBirth
    formData.value.postalCode = response.postalCode

    // Actualizar localStorage
    const userLocal = JSON.parse(localStorage.getItem('user'))
    userLocal.name = response.name
    userLocal.lastName = response.lastName
    userLocal.email = response.email
    userLocal.phoneNumber = response.phoneNumber
    userLocal.dateOfBirth = response.dateOfBirth
    userLocal.postalCode = response.postalCode
    localStorage.setItem('user', JSON.stringify(userLocal))

    profileMessage.value = {
      type: 'alert-success',
      text: '✓ Datos actualizados correctamente',
      icon: 'fas fa-check-circle'
    }

    editMode.value = false
    setTimeout(() => { profileMessage.value = null }, 3000)
  } catch (error) {
    profileMessage.value = {
      type: 'alert-error',
      text: error.data?.message || 'Error al actualizar los datos',
      icon: 'fas fa-exclamation-circle'
    }
  } finally {
    savingProfile.value = false
  }
}

const cancelEdit = () => {
  formData.value.name = userData.value.name
  formData.value.lastName = userData.value.lastName
  formData.value.email = userData.value.email
  formData.value.phoneNumber = userData.value.phoneNumber
  formData.value.dateOfBirth = userData.value.dateOfBirth
  formData.value.postalCode = userData.value.postalCode
  editMode.value = false
  profileMessage.value = null
}

// ========== MÉTODOS DE FOTO DE PERFIL ==========
const triggerPhotoUpload = () => {
  if (photoInput.value) {
    photoInput.value.click()
  }
}

const handlePhotoChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  // Validar tipo
  if (!file.type.startsWith('image/')) {
    profileMessage.value = {
      type: 'alert-error',
      text: 'Por favor selecciona una imagen válida',
      icon: 'fas fa-exclamation-circle'
    }
    return
  }

  // Validar tamaño (5MB máximo)
  if (file.size > 5 * 1024 * 1024) {
    profileMessage.value = {
      type: 'alert-error',
      text: 'La imagen no puede pesar más de 5MB',
      icon: 'fas fa-exclamation-circle'
    }
    return
  }

  uploadingPhoto.value = true

  try {
    // Crear preview local inmediatamente
    const reader = new FileReader()
    reader.onload = (e) => {
      userData.value.profilePhoto = e.target?.result || ''
    }
    reader.readAsDataURL(file)

    const token = localStorage.getItem('token')
    const userId = userData.value.id

    // Crear FormData para enviar el archivo
    const formDataToSend = new FormData()
    formDataToSend.append('file', file)

    const response = await $fetch(`${apiBase}/api/auth/users/${userId}/profile-photo`, {
      method: 'POST',
      headers: {
        'Authorization': token
      },
      body: formDataToSend
    })

    // Actualizar foto en userData y localStorage
    userData.value.profilePhoto = response.profilePhoto
    const userLocal = JSON.parse(localStorage.getItem('user'))
    userLocal.profilePhoto = response.profilePhoto
    localStorage.setItem('user', JSON.stringify(userLocal))

    profileMessage.value = {
      type: 'alert-success',
      text: '✓ Foto de perfil actualizada correctamente',
      icon: 'fas fa-check-circle'
    }

    setTimeout(() => { profileMessage.value = null }, 3000)
  } catch (error) {
    profileMessage.value = {
      type: 'alert-error',
      text: error.data?.message || 'Error al subir la foto',
      icon: 'fas fa-exclamation-circle'
    }
  } finally {
    uploadingPhoto.value = false
    // Limpiar el input
    if (photoInput.value) {
      photoInput.value.value = ''
    }
  }
}

// ========== MÉTODOS DE SEGURIDAD ==========
// Función para evaluar la fortaleza de la contraseña
const getPasswordStrength = (password) => {
  let strength = 0
  const hasLowerCase = /[a-z]/.test(password)
  const hasUpperCase = /[A-Z]/.test(password)
  const hasNumbers = /\d/.test(password)
  const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
  const isLengthAdequate = password.length >= 8

  if (hasLowerCase) strength++
  if (hasUpperCase) strength++
  if (hasNumbers) strength++
  if (hasSpecialChar) strength++
  if (isLengthAdequate) strength++

  const strengthMap = {
    0: { text: 'Muy débil', class: 'weak', percent: '20%' },
    1: { text: 'Débil', class: 'weak', percent: '40%' },
    2: { text: 'Media', class: 'medium', percent: '60%' },
    3: { text: 'Fuerte', class: 'strong', percent: '80%' },
    4: { text: 'Muy fuerte', class: 'very-strong', percent: '100%' },
    5: { text: 'Excelente', class: 'very-strong', percent: '100%' }
  }

  return strengthMap[strength] || { text: 'Muy débil', class: 'weak', percent: '20%' }
}

const changePassword = async () => {
  // Validar campos vacíos con mensajes específicos
  if (!passwordForm.value.currentPassword) {
    passwordMessage.value = {
      type: 'alert-error',
      text: 'Ingresa tu contraseña actual'
    }
    return
  }

  if (!passwordForm.value.newPassword) {
    passwordMessage.value = {
      type: 'alert-error',
      text: 'Ingresa una nueva contraseña'
    }
    return
  }

  if (!passwordForm.value.confirmPassword) {
    passwordMessage.value = {
      type: 'alert-error',
      text: 'Confirma tu nueva contraseña'
    }
    return
  }

  // Validar longitud mínima
  if (passwordForm.value.newPassword.length < 6) {
    passwordMessage.value = {
      type: 'alert-error',
      text: 'La contraseña debe tener mínimo 6 caracteres'
    }
    return
  }

  // Validar que coincidan
  if (passwordForm.value.newPassword.trim() !== passwordForm.value.confirmPassword.trim()) {
    passwordMessage.value = {
      type: 'alert-error',
      text: 'Las contraseñas no coinciden. Verifica que hayas escrito la misma en ambos campos'
    }
    return
  }

  // Validar que no sea igual a la anterior
  if (passwordForm.value.currentPassword.trim() === passwordForm.value.newPassword.trim()) {
    passwordMessage.value = {
      type: 'alert-error',
      text: 'La nueva contraseña debe ser diferente a la actual'
    }
    return
  }

  changingPassword.value = true
  try {
    const token = localStorage.getItem('token')
    const userId = userData.value.id

    await $fetch(`${apiBase}/api/auth/users/${userId}/change-password`, {
      method: 'POST',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json'
      },
      body: {
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword,
        confirmPassword: passwordForm.value.confirmPassword
      }
    })

    passwordMessage.value = {
      type: 'alert-success',
      text: 'Contraseña actualizada correctamente. Por seguridad, necesitarás iniciar sesión nuevamente'
    }

    // Limpiar formulario
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
    showCurrentPassword.value = false
    showNewPassword.value = false
    showConfirmPassword.value = false

    // Mostrar mensaje por más tiempo
    setTimeout(() => { passwordMessage.value = null }, 4000)
  } catch (error) {
    console.error('Error al cambiar contraseña:', error)
    
    // Extraer el mensaje de error correctamente
    let errorMsg = ''
    
    // Intentar obtener el mensaje de diferentes formas
    if (typeof error.data === 'string') {
      errorMsg = error.data
    } else if (error.data?.message) {
      errorMsg = error.data.message
    } else if (error.message) {
      errorMsg = error.message
    } else {
      errorMsg = 'Error al cambiar la contraseña'
    }
    
    console.log('Mensaje de error extraído:', errorMsg)
    
    // Mapear errores específicos del backend
    let displayMessage = errorMsg
    if (errorMsg.includes('incorrecta')) {
      displayMessage = 'La contraseña actual es incorrecta'
    } else if (errorMsg.includes('diferente')) {
      displayMessage = 'Debe poner otra contraseña distinta a la anterior'
    } else if (errorMsg.includes('usuario')) {
      displayMessage = 'Usuario no encontrado'
    } else if (errorMsg.includes('6 caracteres')) {
      displayMessage = 'La contraseña debe tener al menos 6 caracteres'
    } else if (errorMsg.includes('coinciden')) {
      displayMessage = 'Las contraseñas no coinciden'
    }

    passwordMessage.value = {
      type: 'alert-error',
      text: displayMessage
    }
  } finally {
    changingPassword.value = false
  }
}

// ========== MÉTODOS DE CUENTA ==========
const openDeleteModal = () => {
  deleteConfirmEmail.value = ''
  const modal = new bootstrap.Modal(document.getElementById('deleteModal'))
  modal.show()
}

const deleteAccount = async () => {
  if (deleteConfirmEmail.value !== userData.value.email || !deletePassword.value.trim()) {
    return
  }

  deletingAccount.value = true
  try {
    const token = localStorage.getItem('token')
    const userId = userData.value.id

    const response = await $fetch(`${apiBase}/api/auth/users/${userId}/account`, {
      method: 'DELETE',
      headers: {
        'Authorization': token,
        'Content-Type': 'application/json'
      },
      body: {
        password: deletePassword.value.trim()
      }
    })

    // Limpiar datos locales
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('rememberedEmail')
    localStorage.removeItem('rememberedPassword')

    // Redirigir al login
    await navigateTo('/login')
  } catch (error) {
    const errorMessage = error.data?.message || error.message || 'Error al eliminar la cuenta'
    alert(errorMessage)
    deletePassword.value = ''
    showDeletePassword.value = false
  } finally {
    deletingAccount.value = false
  }
}
</script>

<style scoped>
/* Variables de color */
:root {
  --primary-color: #ffb6a1;
  --primary-dark: #ff9a8b;
  --success-color: #10b981;
  --danger-color: #ef4444;
  --warning-color: #f59e0b;
  --text-dark: #0a0a0a;
  --text-light: #6b7280;
  --bg-light: #f9fafb;
  --border-color: #e5e7eb;
}

.perfil-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: 'Poppins', sans-serif;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
}

.perfil-container {
  flex: 1;
  padding: 120px 20px 40px 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

/* ========== HEADER DEL PERFIL ========== */
.perfil-header {
  background: white;
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  border: 1px solid var(--border-color);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  text-align: center;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  color: white;
  box-shadow: 0 4px 15px rgba(255, 182, 161, 0.3);
  flex-shrink: 0;
  overflow: hidden;
  position: relative;
}

@media (min-width: 1024px) {
  .avatar {
    width: 180px;
    height: 180px;
    font-size: 80px;
    box-shadow: 0 6px 25px rgba(255, 182, 161, 0.4);
  }
}

.avatar-clickable {
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-clickable:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(255, 182, 161, 0.4);
}

.avatar-clickable:hover .avatar-overlay {
  opacity: 1;
  backdrop-filter: blur(2px);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: all 0.3s ease;
  border-radius: 50%;
  font-size: 24px;
  color: white;
}

.avatar-overlay span {
  font-size: 12px;
  font-weight: 600;
}

.avatar-empty {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px dashed rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
}

.avatar-empty:hover {
  transform: scale(1.05);
  border-color: rgba(255, 255, 255, 0.9);
  box-shadow: 0 6px 20px rgba(255, 182, 161, 0.4);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-align: center;
}

.empty-state i {
  font-size: 32px;
  color: white;
}

.empty-state p {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-input {
  display: none;
}

.user-info h1 {
  margin: 0;
  color: var(--text-dark);
  font-weight: 700;
  font-size: 28px;
  margin-bottom: 5px;
  line-height: 1.2;
}

@media (min-width: 1024px) {
  .user-info h1 {
    font-size: 36px;
    margin-bottom: 12px;
  }
}

.user-info .email {
  color: var(--text-light);
  margin: 0 0 12px 0;
  font-size: 16px;
}

@media (min-width: 1024px) {
  .user-info .email {
    font-size: 18px;
    margin-bottom: 20px;
  }
}

.role-badge {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 50px;
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (min-width: 1024px) {
  .role-badge {
    padding: 10px 20px;
    font-size: 15px;
  }
}

.role-badge.super_user {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
}

.role-badge.user {
  background: var(--bg-light);
  color: var(--text-light);
}

/* ========== TABS ========== */
.perfil-tabs {
  background: white;
  border-radius: 20px 20px 0 0;
  padding: 0;
  border: 1px solid var(--border-color);
  border-bottom: none;
  overflow: hidden;
}

.tabs-wrapper {
  display: flex;
  gap: 0;
  justify-content: center;
  flex-wrap: wrap;
}

.tab-btn {
  flex: 1;
  padding: 20px;
  border: none;
  background: transparent;
  color: var(--text-light);
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 3px solid transparent;
  position: relative;
}

.tab-btn i {
  font-size: 18px;
}

.tab-btn:hover:not(.active) {
  background: var(--bg-light);
  color: var(--text-dark);
}

.tab-btn.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

/* ========== CONTENIDO DE TABS ========== */
.perfil-content {
  background: white;
  border-radius: 0 0 20px 20px;
  border: 1px solid var(--border-color);
  border-top: none;
  padding: 40px;
  min-height: auto;
}

.tab-pane {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== FORM SECTIONS ========== */
.form-section h2 {
  color: var(--text-dark);
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid var(--border-color);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 25px;
  margin-bottom: 30px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  color: var(--text-dark);
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-input {
  padding: 14px 16px;
  border: 2px solid var(--border-color);
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  color: var(--text-dark);
  transition: all 0.3s ease;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(255, 182, 161, 0.1);
}

.form-input:disabled {
  background: var(--bg-light);
  color: var(--text-light);
  cursor: not-allowed;
}

/* ========== FORMULARIO DE CONTRASEÑA ========== */
.password-form {
  max-width: 100%;
}

.password-form .form-group {
  margin-bottom: 24px;
}

.password-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-input-wrapper .form-input {
  padding-right: 45px;
  width: 100%;
}

.toggle-password {
  position: absolute;
  right: 15px;
  background: transparent;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  font-size: 18px;
  transition: all 0.3s ease;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  top: 50%;
  transform: translateY(-50%);
}

.toggle-password:hover:not(:disabled) {
  color: var(--primary-color);
}

.toggle-password:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.password-toggle-delete {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-light);
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: all 0.3s ease;
  font-size: 16px;
}

.password-toggle-delete:hover:not(:disabled) {
  color: var(--primary-color);
}

.password-toggle-delete:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.password-strength {
  margin-top: 12px;
  padding: 12px;
  background: var(--bg-light);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.strength-bar {
  width: 100%;
  height: 6px;
  background: var(--border-color);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 10px;
}

.strength-fill {
  height: 100%;
  border-radius: 10px;
  transition: all 0.4s ease;
}

.strength-fill.weak {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}

.strength-fill.medium {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.strength-fill.strong {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}

.strength-fill.very-strong {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.strength-text {
  font-size: 12px;
  font-weight: 600;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.strength-text.weak {
  color: #ef4444;
}

.strength-text.medium {
  color: #f59e0b;
}

.strength-text.strong {
  color: #3b82f6;
}

.strength-text.very-strong {
  color: #10b981;
}

.password-match {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  animation: slideIn 0.3s ease-out;
}

.password-match i {
  font-size: 18px;
  flex-shrink: 0;
}

.password-match.success {
  background: rgba(16, 185, 129, 0.15);
  color: #059669;
  border-left: 4px solid #10b981;
}

.password-match.success i {
  color: #10b981;
}

.password-match.error {
  background: rgba(220, 38, 38, 0.15);
  color: #7f1d1d;
  border-left: 4px solid #dc2626;
}

.password-match.error i {
  color: #dc2626;
}

.form-group label {
  color: var(--text-dark);
  font-weight: 600;
  margin-bottom: 10px;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.form-group label i {
  margin-right: 8px;
  color: var(--primary-color);
}

/* ========== BOTONES ========== */
.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 12px;
  font-family: 'Poppins', sans-serif;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 182, 161, 0.3);
}

.btn-success {
  background: var(--success-color);
  color: white;
}

.btn-success:hover:not(:disabled) {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(16, 185, 129, 0.3);
}

.btn-secondary {
  background: var(--bg-light);
  color: var(--text-dark);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--border-color);
}

.btn-danger {
  background: var(--danger-color);
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #dc2626;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.3);
}

/* ========== TARJETAS ========== */
.security-card,
.danger-card {
  background: rgba(220, 38, 38, 0.08);
  border: 2px solid rgba(220, 38, 38, 0.3);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 20px;
}

.danger-header {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
}

.danger-icon {
  width: 60px;
  height: 60px;
  background: rgba(220, 38, 38, 0.15);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #dc2626;
  font-size: 28px;
  flex-shrink: 0;
}

.danger-info {
  flex: 1;
}

.danger-info h3 {
  color: #1f2937;
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 8px 0;
  font-family: 'Poppins', sans-serif;
}

.danger-info p {
  color: #4b5563;
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  font-family: 'Poppins', sans-serif;
}

.danger-details {
  background: rgba(255, 255, 255, 0.7);
  border-left: 4px solid #dc2626;
  padding: 16px 18px;
  border-radius: 8px;
  margin-bottom: 25px;
  font-family: 'Poppins', sans-serif;
}

.delete-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.delete-list li {
  padding: 8px 0;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
}

.delete-list li:before {
  content: "•";
  color: #dc2626;
  font-weight: bold;
  margin-right: 12px;
}

.delete-list-modal {
  list-style: none;
  padding: 0;
  margin: 0;
}

.delete-list-modal li {
  padding: 10px 0;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-delete-account {
  width: 100%;
  padding: 16px 20px;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(220, 38, 38, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.btn-delete-account:hover {
  background: linear-gradient(135deg, #b91c1c 0%, #991b1b 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(220, 38, 38, 0.4);
}

.btn-delete-account:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-delete-confirm {
  padding: 12px 24px;
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  font-family: 'Poppins', sans-serif;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.3);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-delete-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #b91c1c 0%, #991b1b 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.4);
}

.btn-delete-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.security-item,
.danger-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.security-info h3,
.danger-info h3 {
  color: var(--text-dark);
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.security-info p,
.danger-info p {
  color: var(--text-light);
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

/* ========== ALERTAS ========== */
.alert {
  padding: 18px 22px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 14px;
  margin: 20px 0 0 0;
  animation: slideIn 0.3s ease-out;
  font-family: 'Poppins', sans-serif;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-left: 4px solid;
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

.alert i {
  font-size: 20px;
  flex-shrink: 0;
  min-width: 24px;
}

.alert-success {
  background: rgba(16, 185, 129, 0.15);
  border-left-color: #10b981;
  color: #059669;
}

.alert-success i {
  color: #10b981;
}

.alert-error {
  background: rgba(220, 38, 38, 0.15);
  border-left-color: #dc2626;
  color: #7f1d1d;
}

/* ========== MODAL ========== */
.modal-custom .modal-content {
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--border-color);
  border-radius: 16px;
}

.modal-custom .modal-header {
  border-bottom: 1px solid var(--border-color);
  padding: 25px;
}

.modal-custom .modal-header.border-danger {
  border-bottom: 2px solid var(--danger-color);
}

.modal-custom .modal-body {
  padding: 25px;
}

.modal-custom .modal-footer {
  border-top: 1px solid var(--border-color);
  padding: 20px 25px;
}

/* ========== DELETE MODAL ========== */
.modal-danger-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.modal-danger-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: rgba(220, 38, 38, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.modal-danger-icon i {
  font-size: 24px;
  color: var(--danger-color);
}

.modal-danger-title {
  color: var(--text-dark);
  font-size: 20px;
  font-weight: 700;
  font-family: 'Poppins', sans-serif;
  margin: 0;
}

.modal-danger-subtitle {
  color: var(--text-light);
  font-size: 14px;
  margin: 0;
  margin-top: 4px;
}

.warning-box {
  background: rgba(220, 38, 38, 0.1);
  border-left: 4px solid var(--danger-color);
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.warning-box i {
  color: var(--danger-color);
  font-size: 18px;
  margin-top: 2px;
  flex-shrink: 0;
}

.warning-text {
  color: var(--danger-color);
  font-weight: 600;
  font-size: 14px;
  line-height: 1.5;
}

.modal-info-box {
  background: rgba(249, 250, 251, 1);
  border: 1px solid var(--border-color);
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 20px;
}

.modal-info-title {
  color: var(--text-dark);
  font-size: 14px;
  font-weight: 700;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.modal-info-title i {
  color: var(--danger-color);
  font-size: 16px;
}

.delete-list,
.delete-list-modal {
  list-style: none;
  padding: 0;
  margin: 0;
}

.delete-list li,
.delete-list-modal li {
  padding: 10px 0;
  padding-left: 28px;
  position: relative;
  color: var(--text-dark);
  font-size: 14px;
  line-height: 1.5;
}

.delete-list li::before,
.delete-list-modal li::before {
  content: "";
  position: absolute;
  left: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--danger-color);
  font-size: 14px;
}

.delete-list li::before {
  content: "✕";
  font-weight: bold;
}

.delete-list-modal li::before {
  content: "•";
  color: var(--danger-color);
  font-size: 20px;
}

.email-confirmation {
  margin: 20px 0;
}

.email-confirmation-label {
  color: var(--text-dark);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
  display: block;
}

.email-confirmation-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--border-color);
  border-radius: 10px;
  font-size: 14px;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s ease;
  background: white;
}

.email-confirmation-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.email-confirmation-input.confirmed {
  border-color: var(--primary-color);
  background: rgba(16, 185, 129, 0.05);
}

.email-confirmation-feedback {
  margin-top: 8px;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
}

.email-confirmation-feedback.valid {
  color: var(--primary-color);
}

.email-confirmation-feedback.invalid {
  color: var(--text-light);
}

/* ========== RESPONSIVE ========== */
@media (max-width: 768px) {
  .perfil-container {
    padding: 40px 15px 40px 15px;
  }

  .perfil-header {
    padding: 30px 20px;
    margin-bottom: 20px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
    gap: 15px;
  }

  .avatar {
    width: 90px;
    height: 90px;
    font-size: 40px;
  }

  .user-info h1 {
    font-size: 22px;
  }

  .tabs-wrapper {
    flex-wrap: wrap;
    justify-content: center;
  }

  .tab-btn {
    flex: 0 1 auto;
    min-width: 110px;
    padding: 15px 12px;
    font-size: 13px;
  }

  .tab-btn i {
    font-size: 16px;
  }

  .perfil-content {
    padding: 20px;
    border-radius: 0 0 16px 16px;
  }

  .form-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }

  .security-item,
  .danger-item {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-primary,
  .btn-danger {
    width: 100%;
  }

  .form-section h2 {
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .perfil-container {
    padding: 100px 10px 40px 10px;
  }

  .perfil-header {
    padding: 20px 15px;
  }

  .avatar {
    width: 80px;
    height: 80px;
    font-size: 32px;
  }

  .user-info h1 {
    font-size: 20px;
  }

  .tabs-wrapper {
    overflow-x: auto;
  }

  .tab-btn {
    flex: 0 0 auto;
    min-width: 95px;
    padding: 12px 8px;
    font-size: 11px;
  }

  .tab-btn i {
    font-size: 14px;
  }

  .role-badge {
    font-size: 11px;
    padding: 5px 10px;
  }

  .perfil-content {
    padding: 15px;
  }

  .form-section h2 {
    font-size: 18px;
  }
}
</style>

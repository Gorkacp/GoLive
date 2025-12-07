<template>
  <div class="chat-assistant-wrapper">
    <!-- Botón flotante para abrir chat -->
    <button 
      v-if="!isOpen"
      @click="toggleChat"
      class="chat-toggle-btn"
      :class="{ 'has-unread': unreadCount > 0 }"
      :title="$t('Abrir chat de asistencia') || 'Abrir chat de asistencia'"
    >
      <i class="bi bi-chat-dots-fill"></i>
      <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
    </button>

    <!-- Panel de chat -->
    <div v-if="isOpen" class="chat-panel">
      <div class="chat-header">
        <div class="chat-header-info">
          <div class="chat-avatar">
            <i class="bi bi-robot"></i>
          </div>
          <div>
            <h5 class="chat-title">{{ $t('Asistente GoLive') || 'Asistente GoLive' }}</h5>
            <span class="chat-status">
              <span class="status-dot"></span>
              {{ $t('En línea') || 'En línea' }}
            </span>
          </div>
        </div>
        <button @click="toggleChat" class="close-chat-btn">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>

      <div ref="messagesContainer" class="chat-messages">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="message"
          :class="{ 'message-user': message.type === 'user', 'message-bot': message.type === 'bot' }"
        >
          <div class="message-content">
            <p v-html="formatMessage(message.text)"></p>
            <span class="message-time">{{ formatTime(message.timestamp) }}</span>
          </div>
        </div>
        
        <div v-if="isTyping" class="message message-bot">
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-quick-actions" v-if="messages.length === 0">
        <button 
          v-for="action in quickActions" 
          :key="action.id"
          @click="sendQuickAction(action)"
          class="quick-action-btn"
        >
          <i :class="action.icon"></i>
          {{ action.label }}
        </button>
      </div>

      <form @submit.prevent="sendMessage" class="chat-input-form">
        <input
          v-model="inputMessage"
          type="text"
          :placeholder="$t('Escribe tu mensaje...') || 'Escribe tu mensaje...'"
          class="chat-input"
          :disabled="isTyping"
        />
        <button type="submit" class="send-btn" :disabled="!inputMessage.trim() || isTyping">
          <i class="bi bi-send-fill"></i>
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'

const isOpen = ref(false)
const inputMessage = ref('')
const messages = ref([])
const isTyping = ref(false)
const unreadCount = ref(0)
const messagesContainer = ref(null)

// Acciones rápidas
const quickActions = [
  { id: 1, label: 'Ver eventos', icon: 'bi bi-calendar-event', action: 'events' },
  { id: 2, label: 'Ayuda con compra', icon: 'bi bi-cart', action: 'purchase' },
  { id: 3, label: 'Mis entradas', icon: 'bi bi-ticket-perforated', action: 'tickets' },
  { id: 4, label: 'Contacto', icon: 'bi bi-envelope', action: 'contact' }
]

const config = useRuntimeConfig()
const API_BASE = config.public.apiBase

// Enviar mensaje
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // Agregar mensaje del usuario
  messages.value.push({
    type: 'user',
    text: userMessage,
    timestamp: new Date()
  })

  scrollToBottom()

  // Simular typing
  isTyping.value = true

  try {
    // Llamar al backend
    const response = await $fetch(`${API_BASE}/api/chat/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: {
        message: userMessage
      }
    })

    // Agregar respuesta del bot
    messages.value.push({
      type: 'bot',
      text: response.response || 'Lo siento, no pude procesar tu mensaje. Por favor, intenta nuevamente.',
      timestamp: new Date()
    })
  } catch (error) {
    console.error('Error al enviar mensaje:', error)
    // Respuesta de fallback
    messages.value.push({
      type: 'bot',
      text: 'Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta nuevamente o contacta con soporte.',
      timestamp: new Date()
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

// Enviar acción rápida
const sendQuickAction = async (action) => {
  const actionMessages = {
    events: 'Quiero ver eventos disponibles',
    purchase: 'Necesito ayuda para comprar entradas',
    tickets: 'Quiero ver mis entradas',
    contact: 'Necesito contactar con soporte'
  }

  const message = actionMessages[action.action] || action.label
  
  // Agregar mensaje del usuario
  messages.value.push({
    type: 'user',
    text: message,
    timestamp: new Date()
  })

  scrollToBottom()
  isTyping.value = true

  try {
    const response = await $fetch(`${API_BASE}/api/chat/message`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: {
        message: message
      }
    })

    messages.value.push({
      type: 'bot',
      text: response.response || 'Lo siento, no pude procesar tu mensaje.',
      timestamp: new Date()
    })
  } catch (error) {
    console.error('Error al enviar acción rápida:', error)
    messages.value.push({
      type: 'bot',
      text: 'Lo siento, hubo un error. Por favor, intenta nuevamente.',
      timestamp: new Date()
    })
  } finally {
    isTyping.value = false
    scrollToBottom()
  }
}

// Formatear mensaje (para links, markdown básico y saltos de línea)
const formatMessage = (text) => {
  if (!text) return ''
  
  let formatted = text
  
  // Convertir saltos de línea a <br>
  formatted = formatted.replace(/\n/g, '<br>')
  
  // Convertir markdown básico **texto** a <strong>
  formatted = formatted.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  
  // Convertir *texto* a <em>
  formatted = formatted.replace(/\*(.+?)\*/g, '<em>$1</em>')
  
  // Convertir URLs a links
  const urlRegex = /(https?:\/\/[^\s<>]+)/g
  formatted = formatted.replace(urlRegex, '<a href="$1" target="_blank" rel="noopener" style="color: #ff6b35; text-decoration: underline;">$1</a>')
  
  // Convertir emails a links
  const emailRegex = /([a-zA-Z0-9._-]+@[a-zA-Z0-9._-]+\.[a-zA-Z0-9_-]+)/g
  formatted = formatted.replace(emailRegex, '<a href="mailto:$1" style="color: #ff6b35; text-decoration: underline;">$1</a>')
  
  return formatted
}

// Formatear hora
const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('es-ES', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

// Scroll al final
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// Toggle chat
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// Mensaje de bienvenida inicial
onMounted(async () => {
  if (messages.value.length === 0) {
    try {
      // Obtener mensaje de bienvenida del backend
      const response = await $fetch(`${API_BASE}/api/chat/message`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: {
          message: 'hola'
        }
      })
      
      messages.value.push({
        type: 'bot',
        text: response.response || '¡Hola! 👋 Soy el asistente virtual de GoLive. ¿En qué puedo ayudarte hoy?',
        timestamp: new Date()
      })
    } catch (error) {
      // Fallback si hay error
      messages.value.push({
        type: 'bot',
        text: '¡Hola! 👋 Soy el asistente virtual de GoLive. ¿En qué puedo ayudarte hoy?',
        timestamp: new Date()
      })
    }
  }
})

// Observar nuevos mensajes cuando el chat está cerrado
watch(() => messages.value.length, (newLength, oldLength) => {
  if (!isOpen.value && newLength > oldLength) {
    unreadCount.value++
  }
})
</script>

<style scoped>
.chat-assistant-wrapper {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  max-width: calc(100vw - 40px);
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.chat-toggle-btn {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  color: #ffffff;
  font-size: 1.5rem;
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(255, 0, 87, 0.4);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
  z-index: 1001;
}

.chat-toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 30px rgba(255, 0, 87, 0.6);
}

.chat-toggle-btn.has-unread {
  animation: pulse 2s infinite;
}

.unread-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #4ade80;
  color: #ffffff;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  border: 2px solid #0a0a0a;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 20px rgba(255, 0, 87, 0.4);
  }
  50% {
    box-shadow: 0 4px 30px rgba(255, 0, 87, 0.8);
  }
}

.chat-panel {
  width: 380px;
  height: 600px;
  max-width: 100%;
  max-height: 100%;
  background: #0a0a0a;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  border: 2px solid rgba(255, 0, 87, 0.3);
  overflow: hidden;
  box-sizing: border-box;
  margin-bottom: 10px;
}

.chat-header {
  padding: 1rem;
  background: rgba(255, 0, 87, 0.1);
  border-bottom: 1px solid rgba(255, 0, 87, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  min-height: 70px;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
  min-width: 0;
}

.chat-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 1.2rem;
}

.chat-title {
  font-family: 'Poppins', sans-serif;
  color: #ffffff;
  font-size: 1rem;
  margin: 0;
  font-weight: 600;
}

.chat-status {
  font-size: 0.75rem;
  color: #b0b0b0;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4ade80;
  display: inline-block;
  animation: blink 2s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.close-chat-btn {
  background: transparent;
  border: none;
  color: #ffffff;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-chat-btn:hover {
  background: rgba(255, 0, 87, 0.2);
  color: #ff0057;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 0, 87, 0.3);
  border-radius: 3px;
}

.message {
  display: flex;
  max-width: 80%;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-user {
  align-self: flex-end;
  margin-left: auto;
}

.message-bot {
  align-self: flex-start;
}

.message-content {
  padding: 0.75rem 1rem;
  border-radius: 12px;
  position: relative;
}

.message-user .message-content {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  border-bottom-right-radius: 4px;
}

.message-bot .message-content {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  border-bottom-left-radius: 4px;
}

.message-content p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.5;
}

.message-content a {
  color: #ff6b35;
  text-decoration: underline;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
  display: block;
  margin-top: 0.25rem;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 0.5rem 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-quick-actions {
  padding: 1rem;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.quick-action-btn {
  padding: 0.75rem;
  background: rgba(255, 0, 87, 0.1);
  border: 1px solid rgba(255, 0, 87, 0.3);
  border-radius: 8px;
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.quick-action-btn:hover {
  background: rgba(255, 0, 87, 0.2);
  border-color: #ff0057;
  transform: translateY(-2px);
}

.quick-action-btn i {
  font-size: 1rem;
}

.chat-input-form {
  display: flex;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 0, 87, 0.05);
  box-sizing: border-box;
  min-width: 0;
}

.chat-input {
  flex: 1;
  min-width: 0;
  padding: 0.75rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: #ffffff;
  font-family: 'Poppins', sans-serif;
  font-size: 0.9rem;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}

.chat-input::placeholder {
  color: #888;
}

.chat-input:focus {
  border-color: #ff0057;
  background: rgba(255, 255, 255, 0.15);
}

.send-btn {
  width: 44px;
  height: 44px;
  min-width: 44px;
  min-height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  color: #ffffff;
  font-size: 1.2rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  box-sizing: border-box;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .chat-assistant-wrapper {
    bottom: 20px;
    right: 15px;
    left: auto;
    width: auto;
    max-width: calc(100vw - 30px);
  }

  .chat-panel {
    width: 90vw;
    max-width: 400px;
    height: 70vh;
    max-height: 600px;
    min-height: 500px;
    border-radius: 16px;
    position: relative;
    margin-bottom: 10px;
  }

  .chat-header {
    padding: 1rem;
    flex-shrink: 0;
  }

  .chat-header-info {
    gap: 0.75rem;
  }

  .chat-avatar {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }

  .chat-title {
    font-size: 1rem;
    margin-bottom: 0.25rem;
  }

  .chat-status {
    font-size: 0.75rem;
  }

  .chat-messages {
    padding: 1rem;
    flex: 1;
    min-height: 0;
  }

  .message {
    max-width: 85%;
  }

  .message-content {
    padding: 0.6rem 0.85rem;
  }

  .message-content p {
    font-size: 0.85rem;
  }

  .chat-quick-actions {
    padding: 0.75rem;
    gap: 0.4rem;
    flex-shrink: 0;
  }

  .quick-action-btn {
    padding: 0.6rem;
    font-size: 0.8rem;
  }

  .chat-input-form {
    padding: 0.75rem;
    gap: 0.4rem;
    flex-shrink: 0;
  }

  .chat-input {
    padding: 0.65rem 0.9rem;
    font-size: 0.85rem;
  }

  .send-btn {
    width: 40px;
    height: 40px;
    min-width: 40px;
    min-height: 40px;
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .chat-assistant-wrapper {
    bottom: 15px;
    right: 10px;
    left: auto;
    width: auto;
    max-width: calc(100vw - 20px);
  }

  .chat-panel {
    width: 85vw;
    max-width: 380px;
    height: 65vh;
    max-height: 550px;
    min-height: 450px;
    border-radius: 14px;
    margin-bottom: 10px;
  }

  .chat-header {
    padding: 0.85rem;
    flex-shrink: 0;
  }

  .chat-header-info {
    gap: 0.6rem;
  }

  .chat-avatar {
    width: 36px;
    height: 36px;
    font-size: 1.1rem;
  }

  .chat-title {
    font-size: 0.95rem;
    margin-bottom: 0.2rem;
  }

  .chat-status {
    font-size: 0.7rem;
  }

  .chat-messages {
    padding: 0.75rem;
    flex: 1;
    min-height: 0;
  }

  .chat-input-form {
    padding: 0.65rem;
    gap: 0.35rem;
    flex-shrink: 0;
  }

  .chat-input {
    padding: 0.6rem 0.8rem;
    font-size: 0.8rem;
  }

  .send-btn {
    width: 38px;
    height: 38px;
    min-width: 38px;
    min-height: 38px;
    font-size: 1rem;
  }

  .chat-quick-actions {
    padding: 0.65rem;
    gap: 0.35rem;
    flex-shrink: 0;
  }
}
</style>

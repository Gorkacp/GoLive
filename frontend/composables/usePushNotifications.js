export const usePushNotifications = () => {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase
  const publicKey = config.public.pushPublicKey

  const { getToken, isAuthenticated } = useAuth()

  const isMobileDevice = () => {
    if (!process.client) return false

    const ua = navigator.userAgent || navigator.vendor || window.opera

    // iOS / Android / otros navegadores móviles
    const isMobileUA = /android|iphone|ipad|ipod|iemobile|opera mini/i.test(ua)

    // Puntero "grueso" suele indicar dispositivo táctil (móvil/tablet)
    const isCoarsePointer = window.matchMedia && window.matchMedia('(pointer: coarse)').matches

    // PWA instalada (standalone) en móviles
    const isStandalone =
      window.matchMedia?.('(display-mode: standalone)').matches ||
      window.navigator.standalone === true

    return isMobileUA || isCoarsePointer || isStandalone
  }

  const urlBase64ToUint8Array = (base64String) => {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
    const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/')

    const rawData = atob(base64)
    const outputArray = new Uint8Array(rawData.length)

    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i)
    }
    return outputArray
  }

  const registerPush = async () => {
    if (!process.client) return
    if (!('serviceWorker' in navigator) || !('PushManager' in window) || !('Notification' in window)) {
      console.warn('Notificaciones push no soportadas en este navegador')
      return
    }

    if (!publicKey) {
      console.warn('Falta la clave pública VAPID (VAPID_PUBLIC_KEY)')
      return
    }

    if (!isAuthenticated.value) {
      return
    }

    try {
      const permission = await Notification.requestPermission()
      if (permission !== 'granted') {
        return
      }

      const registration = await navigator.serviceWorker.ready

      const existing = await registration.pushManager.getSubscription()
      if (existing) {
        await sendSubscriptionToServer(existing)
        return
      }

      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(publicKey)
      })

      await sendSubscriptionToServer(subscription)
    } catch (error) {
      console.error('Error registrando notificaciones push', error)
    }
  }

  const hasActiveSubscription = async () => {
    if (!process.client) return false
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      return false
    }
    try {
      const registration = await navigator.serviceWorker.ready
      const existing = await registration.pushManager.getSubscription()
      return !!existing
    } catch {
      return false
    }
  }

  const unsubscribePush = async () => {
    if (!process.client) return
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      return
    }

    try {
      const registration = await navigator.serviceWorker.ready
      const subscription = await registration.pushManager.getSubscription()
      if (!subscription) return

      try {
        await $fetch(`${apiBase}/api/push/unsubscribe`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: {
            endpoint: subscription.endpoint
          }
        })
      } catch (error) {
        console.error('Error avisando al backend para eliminar la suscripción', error)
      }

      await subscription.unsubscribe()
    } catch (error) {
      console.error('Error desactivando notificaciones push', error)
    }
  }

  const sendSubscriptionToServer = async (subscription) => {
    try {
      const token = getToken()
      if (!token) return

      await $fetch(`${apiBase}/api/push/subscribe`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: {
          endpoint: subscription.endpoint,
          expirationTime: subscription.expirationTime,
          keys: subscription.toJSON().keys
        }
      })
    } catch (error) {
      console.error('Error enviando suscripción push al backend', error)
    }
  }

  return {
    registerPush,
    unsubscribePush,
    hasActiveSubscription,
    isMobileDevice
  }
}



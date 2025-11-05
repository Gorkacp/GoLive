// plugins/firebase.client.js
import { initializeApp } from 'firebase/app'
import { 
  getAuth, 
  GoogleAuthProvider, 
  setPersistence, 
  browserLocalPersistence 
} from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'
import { getStorage } from 'firebase/storage'

export default defineNuxtPlugin(async (nuxtApp) => {
  // Acceso a las variables del entorno desde runtimeConfig
  const config = useRuntimeConfig()

  const firebaseConfig = {
    apiKey: config.public.firebaseApiKey,
    authDomain: config.public.firebaseAuthDomain,
    projectId: config.public.firebaseProjectId,
    storageBucket: config.public.firebaseStorageBucket,
    messagingSenderId: config.public.firebaseMessagingSenderId,
    appId: config.public.firebaseAppId,
    measurementId: config.public.firebaseMeasurementId
  }

  // Inicializar Firebase
  const app = initializeApp(firebaseConfig)

  // Inicializar servicios principales
  const auth = getAuth(app)
  const db = getFirestore(app)
  const storage = getStorage(app)

  // Mantener sesión iniciada (persistencia local)
  await setPersistence(auth, browserLocalPersistence)

  // Proveedor de autenticación con Google
  const googleProvider = new GoogleAuthProvider()

  // Hacer accesibles los servicios desde toda la app
  nuxtApp.provide('auth', auth)
  nuxtApp.provide('db', db)
  nuxtApp.provide('storage', storage)
  nuxtApp.provide('googleProvider', googleProvider)
})

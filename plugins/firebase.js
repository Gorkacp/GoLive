// plugins/firebase.client.js
import { initializeApp } from 'firebase/app'
import { getAuth, GoogleAuthProvider, setPersistence, browserLocalPersistence } from 'firebase/auth'
import { getFirestore } from 'firebase/firestore'
import { getStorage } from 'firebase/storage'

export default defineNuxtPlugin(async (nuxtApp) => {
  const firebaseConfig = {
    apiKey: "AIzaSyATeCCzeZ1mm8b04ydW_T_6EDDnaBIRVdg",
    authDomain: "golive-b79fb.firebaseapp.com",
    projectId: "golive-b79fb",
    storageBucket: "golive-b79fb.firebasestorage.app",
    messagingSenderId: "570488772153",
    appId: "1:570488772153:web:18f34fa22f15c16b721fe8",
    measurementId: "G-M8JHMCKGML"
  }

  // Inicializar Firebase
  const app = initializeApp(firebaseConfig)

  // Auth con persistencia
  const auth = getAuth(app)
  await setPersistence(auth, browserLocalPersistence) // mantiene sesión hasta cerrar sesión manual

  // Firestore y Storage
  const db = getFirestore(app)
  const storage = getStorage(app)

  // Proveedor de Google
  const googleProvider = new GoogleAuthProvider()

  // Proveer a Nuxt
  nuxtApp.provide('auth', auth)
  nuxtApp.provide('db', db)
  nuxtApp.provide('storage', storage)
  nuxtApp.provide('googleProvider', googleProvider)
})

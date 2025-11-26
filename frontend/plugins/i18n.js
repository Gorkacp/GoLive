import { createI18n } from 'vue-i18n'
import { watch, ref } from 'vue'
import es from '~/locales/es.json'
import en from '~/locales/en.json'
import pt from '~/locales/pt.json'

export default defineNuxtPlugin((nuxtApp) => {
  const savedLocale = process.client ? localStorage.getItem('lang') || 'es' : 'es'

  const i18n = createI18n({
    legacy: false,
    locale: savedLocale,
    fallbackLocale: 'es',
    messages: {
      es,
      en,
      pt
    }
  })

  nuxtApp.vueApp.use(i18n)

  // Guardar cambios de idioma en localStorage
  if (process.client) {
    watch(
      () => i18n.global.locale.value,
      (newLocale) => {
        localStorage.setItem('lang', newLocale)
      }
    )
  }
})

<template>
  <footer class="footer-custom text-light pt-5 pb-3">
    <div class="container">
      <div class="row">

        <!-- Sección de información -->
        <div class="col-lg-3 col-md-6 col-12 mb-4">
          <h5 class="fw-bold footer-title">GoLive</h5>
          <p class="footer-description">{{ $t('GoLiveInfo') }}</p>
          <div class="footer-logo d-none d-md-block">
            <span class="logo-text">GoLive</span>
          </div>
        </div>

        <!-- Sección de enlaces rápidos -->
        <div class="col-lg-2 col-md-6 col-6 mb-4">
          <h5 class="fw-bold footer-title">{{ $t('Enlaces') }}</h5>
          <ul class="list-unstyled footer-links">
            <li><NuxtLink to="/" class="footer-link">{{ $t('Inicio') }}</NuxtLink></li>
            <li><NuxtLink to="/conciertos" class="footer-link">{{ $t('Conciertos') }}</NuxtLink></li>
            <li><NuxtLink to="/festivales" class="footer-link">{{ $t('Festivales') }}</NuxtLink></li>
            <li><NuxtLink to="/blog" class="footer-link">{{ $t('Blog') }}</NuxtLink></li>
          </ul>
        </div>

        <!-- Sección de soporte -->
        <div class="col-lg-2 col-md-6 col-6 mb-4">
          <h5 class="fw-bold footer-title">{{ $t('Soporte') || 'Soporte' }}</h5>
          <ul class="list-unstyled footer-links">
            <li><a href="#" class="footer-link">{{ $t('Ayuda') || 'Ayuda' }}</a></li>
            <li><a href="#" class="footer-link">{{ $t('Contacto') || 'Contacto' }}</a></li>
            <li><a href="#" class="footer-link">{{ $t('Términos') || 'Términos y Condiciones' }}</a></li>
            <li><a href="#" class="footer-link">{{ $t('Privacidad') || 'Política de Privacidad' }}</a></li>
          </ul>
        </div>

        <!-- Newsletter -->
        <div class="col-lg-3 col-md-6 col-12 mb-4">
          <h5 class="fw-bold footer-title">{{ $t('Newsletter') || 'Newsletter' }}</h5>
          <p class="footer-newsletter-text">{{ $t('NewsletterText') || 'Recibe las últimas novedades y ofertas exclusivas' }}</p>
          <form @submit.prevent="subscribeNewsletter" class="newsletter-form">
            <div class="input-group">
              <input 
                type="email" 
                v-model="newsletterEmail"
                :placeholder="$t('TuEmail') || 'Tu email'"
                class="form-control newsletter-input"
                required
              >
              <button type="submit" class="btn newsletter-btn">
                <i class="bi bi-send"></i>
              </button>
            </div>
            <div v-if="newsletterMessage" class="newsletter-message mt-2">
              {{ newsletterMessage }}
            </div>
          </form>
        </div>

        <!-- Sección de redes sociales -->
        <div class="col-lg-2 col-md-6 col-12 mb-4">
          <h5 class="fw-bold footer-title">{{ $t('Síguenos') || 'Síguenos' }}</h5>
          <div class="social-links">
            <a href="#" class="social-link" aria-label="Facebook">
              <i class="bi bi-facebook"></i>
            </a>
            <a href="#" class="social-link" aria-label="Twitter">
              <i class="bi bi-twitter-x"></i>
            </a>
            <a href="#" class="social-link" aria-label="Instagram">
              <i class="bi bi-instagram"></i>
            </a>
            <a href="#" class="social-link" aria-label="YouTube">
              <i class="bi bi-youtube"></i>
            </a>
            <a href="#" class="social-link" aria-label="TikTok">
              <i class="bi bi-tiktok"></i>
            </a>
          </div>
        </div>

      </div>

      <!-- Línea de copyright -->
      <div class="text-center mt-4 pt-3 border-top border-secondary">
        <small>&copy; 2025 GoLive. {{ $t('Copyright') }}</small>
        <div class="version-text mt-1">
          <small>Versión {{ version }}</small>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'
import packageInfo from '~/package.json'

const version = packageInfo.version || 'Desarrollo'
const newsletterEmail = ref('')
const newsletterMessage = ref('')

const subscribeNewsletter = () => {
  // Aquí puedes integrar con tu backend para guardar el email
  if (newsletterEmail.value) {
    newsletterMessage.value = '¡Gracias por suscribirte!'
    newsletterEmail.value = ''
    setTimeout(() => {
      newsletterMessage.value = ''
    }, 3000)
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');

.footer-custom {
  background: linear-gradient(180deg, #0a0a0a 0%, #1a1a1a 100%);
  font-family: 'Poppins', sans-serif;
  border-top: 1px solid rgba(255, 0, 87, 0.2);
  padding: 2rem 0 1rem 0;
}

.footer-custom .container {
  padding-left: 1rem;
  padding-right: 1rem;
}

.footer-title {
  color: #ffffff;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  position: relative;
  padding-bottom: 0.5rem;
}

.footer-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
}

.footer-description {
  color: #b0b0b0;
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.footer-logo {
  margin-top: 1rem;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.footer-links {
  list-style: none;
  padding: 0;
}

.footer-links li {
  margin-bottom: 0.5rem;
}

.footer-link {
  color: #b0b0b0;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: inline-block;
  position: relative;
}

.footer-link::before {
  content: '';
  position: absolute;
  left: -12px;
  top: 50%;
  transform: translateY(-50%);
  width: 0;
  height: 2px;
  background: #ff0057;
  transition: width 0.3s ease;
}

.footer-link:hover {
  color: #ff0057;
  text-decoration: none;
  transform: translateX(5px);
}

.footer-link:hover::before {
  width: 8px;
}

.footer-newsletter-text {
  color: #b0b0b0;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.newsletter-form {
  margin-top: 1rem;
}

.newsletter-form .input-group {
  display: flex;
  width: 100%;
}

.newsletter-input {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #ffffff;
  border-radius: 25px 0 0 25px;
  padding: 0.6rem 1rem;
  font-size: 0.9rem;
  flex: 1;
  min-width: 0;
  border-right: none;
}

.newsletter-input::placeholder {
  color: #888;
}

.newsletter-input:focus {
  background: rgba(255, 255, 255, 0.15);
  border-color: #ff0057;
  color: #ffffff;
  box-shadow: 0 0 0 0.2rem rgba(255, 0, 87, 0.25);
}

.newsletter-btn {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 0 25px 25px 0;
  color: #ffffff;
  padding: 0.6rem 1.2rem;
  transition: all 0.3s ease;
}

.newsletter-btn:hover {
  background: linear-gradient(135deg, #ff1a6b 0%, #ff7b45 100%);
  transform: scale(1.05);
}

.newsletter-message {
  color: #4ade80;
  font-size: 0.85rem;
  text-align: center;
}

.social-links {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.social-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  color: #b0b0b0;
  font-size: 1.2rem;
  text-decoration: none;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.social-link:hover {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  color: #ffffff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.4);
  border-color: transparent;
}

footer a:hover {
  color: #ff0057;
  text-decoration: none;
}

.version-text {
  color: #aaa;
  font-size: 0.85rem;
}

@media (max-width: 768px) {
  .footer-custom {
    padding: 1.5rem 0 1rem 0;
  }

  .footer-custom .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }

  .footer-custom .row {
    margin-left: 0;
    margin-right: 0;
  }

  .footer-custom .row > div {
    padding-left: 0;
    padding-right: 0;
    margin-bottom: 1.5rem;
  }

  .footer-title {
    font-size: 0.95rem;
    margin-bottom: 0.75rem;
    padding-bottom: 0.4rem;
  }

  .footer-title::after {
    width: 30px;
  }

  .footer-description {
    font-size: 0.85rem;
    line-height: 1.5;
    margin-bottom: 0.75rem;
  }

  .footer-logo {
    margin-top: 0.5rem;
  }

  .logo-text {
    font-size: 1.2rem;
  }

  .footer-links {
    margin-top: 0.5rem;
  }

  .footer-links li {
    margin-bottom: 0.4rem;
  }

  .footer-link {
    font-size: 0.85rem;
  }

  .footer-newsletter-text {
    font-size: 0.8rem;
    margin-bottom: 0.75rem;
    line-height: 1.4;
  }

  .newsletter-form {
    margin-top: 0.5rem;
  }

  .newsletter-form .input-group {
    flex-wrap: nowrap;
  }

  .newsletter-input {
    padding: 0.5rem 0.85rem;
    font-size: 0.85rem;
    flex: 1;
    min-width: 0;
  }

  .newsletter-btn {
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .social-links {
    justify-content: flex-start;
    gap: 0.75rem;
    margin-top: 0.5rem;
  }

  .social-link {
    width: 36px;
    height: 36px;
    font-size: 1.1rem;
  }

  .footer-custom .text-center {
    padding-top: 1rem !important;
    margin-top: 1rem !important;
  }

  .footer-custom .text-center small {
    font-size: 0.75rem;
  }

  .version-text {
    font-size: 0.75rem;
    margin-top: 0.5rem;
  }
}

@media (max-width: 576px) {
  .footer-custom {
    padding: 1.25rem 0 0.75rem 0;
  }

  .footer-custom .container {
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }

  .footer-custom .row > div {
    margin-bottom: 1.25rem;
  }

  .footer-title {
    font-size: 0.9rem;
    margin-bottom: 0.6rem;
    padding-bottom: 0.35rem;
  }

  .footer-title::after {
    width: 25px;
    height: 1.5px;
  }

  .footer-description {
    font-size: 0.8rem;
    margin-bottom: 0.6rem;
  }

  .logo-text {
    font-size: 1.1rem;
  }

  .footer-links li {
    margin-bottom: 0.35rem;
  }

  .footer-link {
    font-size: 0.8rem;
  }

  .footer-newsletter-text {
    font-size: 0.75rem;
    margin-bottom: 0.6rem;
  }

  .newsletter-form .input-group {
    flex-wrap: nowrap;
  }

  .newsletter-input {
    padding: 0.45rem 0.75rem;
    font-size: 0.8rem;
    border-radius: 20px 0 0 20px;
    flex: 1;
    min-width: 0;
  }

  .newsletter-btn {
    padding: 0.45rem 0.9rem;
    font-size: 0.85rem;
    border-radius: 0 20px 20px 0;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .social-links {
    gap: 0.6rem;
  }

  .social-link {
    width: 34px;
    height: 34px;
    font-size: 1rem;
  }

  .footer-custom .text-center {
    padding-top: 0.75rem !important;
    margin-top: 0.75rem !important;
  }

  .footer-custom .text-center small {
    font-size: 0.7rem;
    display: block;
    margin-bottom: 0.25rem;
  }

  .version-text {
    font-size: 0.7rem;
    margin-top: 0.4rem;
  }
}

@media (max-width: 480px) {
  .footer-custom {
    padding: 1rem 0 0.5rem 0;
  }

  .footer-custom .container {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
  }

  .footer-custom .row > div {
    margin-bottom: 1rem;
  }

  .footer-title {
    font-size: 0.85rem;
  }

  .footer-description {
    font-size: 0.75rem;
  }

  .logo-text {
    font-size: 1rem;
  }

  .footer-link {
    font-size: 0.75rem;
  }

  .newsletter-form .input-group {
    flex-wrap: nowrap;
  }

  .newsletter-input {
    font-size: 0.75rem;
    padding: 0.4rem 0.7rem;
    flex: 1;
    min-width: 0;
  }

  .newsletter-btn {
    padding: 0.4rem 0.8rem;
    font-size: 0.8rem;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .social-link {
    width: 32px;
    height: 32px;
    font-size: 0.95rem;
  }
}
</style>

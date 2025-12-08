<template>
  <footer class="footer-custom text-light pt-5 pb-3">
    <div class="container">
      <div class="row">

        <!-- Sección de información -->
        <div class="col-lg-3 col-md-6 col-12 mb-3 mb-md-4">
          <h5 class="fw-bold footer-title">GoLive</h5>
          <p class="footer-description">{{ $t('GoLiveInfo') }}</p>
          <div class="footer-logo d-none d-md-block">
            <span class="logo-text">GoLive</span>
          </div>
        </div>

        <!-- Sección de enlaces rápidos -->
        <div class="col-lg-2 col-md-6 col-6 mb-3 mb-md-4">
          <h5 class="fw-bold footer-title">{{ $t('Enlaces') }}</h5>
          <ul class="list-unstyled footer-links">
            <li><NuxtLink to="/" class="footer-link">{{ $t('Inicio') }}</NuxtLink></li>
            <li><NuxtLink to="/conciertos" class="footer-link">{{ $t('Conciertos') }}</NuxtLink></li>
            <li><NuxtLink to="/festivales" class="footer-link">{{ $t('Festivales') }}</NuxtLink></li>
          </ul>
        </div>

        <!-- Sección de soporte -->
        <div class="col-lg-2 col-md-6 col-6 mb-3 mb-md-4">
          <h5 class="fw-bold footer-title">{{ $t('Soporte') || 'Soporte' }}</h5>
          <ul class="list-unstyled footer-links">
            <li><a href="#" class="footer-link">{{ $t('Ayuda') || 'Ayuda' }}</a></li>
            <li><a href="#" class="footer-link">{{ $t('Términos') || 'Términos y Condiciones' }}</a></li>
            <li><a href="#" class="footer-link">{{ $t('Privacidad') || 'Política de Privacidad' }}</a></li>
          </ul>
        </div>

        <!-- Sección de Contacto -->
        <div class="col-lg-3 col-md-6 col-12 mb-3 mb-md-4">
          <h5 class="fw-bold footer-title">{{ $t('Contacto') || 'Contacto' }}</h5>
          <div class="contact-info">
            <div class="contact-item">
              <div class="contact-icon">
                <i class="bi bi-envelope"></i>
              </div>
              <div class="contact-details">
                <span class="contact-label">{{ $t('Email') || 'Email' }}</span>
                <a href="mailto:info@golive.com" class="contact-link">info@golive.com</a>
              </div>
            </div>
            <div class="contact-item">
              <div class="contact-icon">
                <i class="bi bi-telephone"></i>
              </div>
              <div class="contact-details">
                <span class="contact-label">{{ $t('Teléfono') || 'Teléfono' }}</span>
                <a href="tel:+34900123456" class="contact-link">+34 900 123 456</a>
              </div>
            </div>
            <div class="contact-item">
              <div class="contact-icon">
                <i class="bi bi-clock"></i>
              </div>
              <div class="contact-details">
                <span class="contact-label">{{ $t('Horario') || 'Horario' }}</span>
                <span class="contact-text">{{ $t('HorarioTexto') || 'Lun - Vie: 9:00 - 18:00' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Sección de redes sociales -->
        <div class="col-lg-2 col-md-6 col-12 mb-3 mb-md-4">
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
      <div class="text-center footer-copyright mt-3 mt-md-4 pt-3 border-top border-secondary">
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
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');

.footer-custom {
  background: linear-gradient(180deg, #0a0a0a 0%, #1a1a1a 100%);
  font-family: 'Poppins', sans-serif;
  border-top: 1px solid rgba(255, 0, 87, 0.2);
  padding: 2rem 0 0 0;
  margin: 0;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.footer-custom .container {
  padding-left: 0;
  padding-right: 0;
  max-width: 100%;
  margin: 0;
  width: 100%;
  box-sizing: border-box;
}

@media (min-width: 769px) {
  .footer-custom .container {
    padding-left: 1rem;
    padding-right: 1rem;
  }
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

.footer-copyright {
  padding-left: 1rem;
  padding-right: 1rem;
  padding-bottom: 1rem;
}

@media (max-width: 768px) {
  .footer-copyright {
    padding-left: 1.25rem;
    padding-right: 1.25rem;
    padding-bottom: 1rem;
  }
}

@media (max-width: 576px) {
  .footer-copyright {
    padding-left: 1rem;
    padding-right: 1rem;
    padding-bottom: 0.875rem;
  }
}

@media (max-width: 480px) {
  .footer-copyright {
    padding-left: 1rem;
    padding-right: 1rem;
    padding-bottom: 0.75rem;
  }
}

/* Estilos para la sección de contacto */
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-top: 0.5rem;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  transition: transform 0.2s ease;
}

.contact-item:hover {
  transform: translateX(3px);
}

.contact-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  min-width: 36px;
  border-radius: 8px;
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.15), rgba(255, 107, 53, 0.15));
  color: #ff6b35;
  font-size: 1rem;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.contact-item:hover .contact-icon {
  background: linear-gradient(135deg, rgba(255, 0, 87, 0.25), rgba(255, 107, 53, 0.25));
  transform: scale(1.1);
}

.contact-details {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  flex: 1;
}

.contact-label {
  display: block;
  font-size: 0.75rem;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 500;
  margin-bottom: 0.2rem;
}

.contact-link {
  color: #b0b0b0;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: inline-block;
  word-break: break-word;
}

.contact-link:hover {
  color: #ff0057;
  text-decoration: none;
  transform: translateX(2px);
}

.contact-text {
  color: #b0b0b0;
  font-size: 0.9rem;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .footer-custom {
    padding: 1.5rem 0 0 0;
    margin: 0;
  }

  .footer-custom .container {
    padding-left: 0;
    padding-right: 0;
    max-width: 100%;
    margin: 0;
  }

  .footer-custom .row {
    margin-left: 0;
    margin-right: 0;
  }

  .footer-custom .row > div {
    padding-left: 1.25rem;
    padding-right: 1.25rem;
    margin-bottom: 1.25rem;
  }

  .footer-title {
    font-size: 0.9rem;
    margin-bottom: 0.65rem;
    padding-bottom: 0.4rem;
    font-weight: 600;
  }

  .footer-title::after {
    width: 30px;
    height: 2px;
  }

  .footer-description {
    font-size: 0.8rem;
    line-height: 1.5;
    margin-bottom: 0.5rem;
    color: #c0c0c0;
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
    padding: 0.15rem 0;
    color: #d0d0d0;
  }

  .social-links {
    justify-content: flex-start;
    gap: 0.7rem;
    margin-top: 0.5rem;
  }

  .social-link {
    width: 36px;
    height: 36px;
    font-size: 1.1rem;
  }

  .contact-info {
    gap: 0.85rem;
    margin-top: 0.5rem;
  }

  .contact-item {
    gap: 0.65rem;
    align-items: center;
  }

  .contact-icon {
    width: 30px;
    height: 30px;
    min-width: 30px;
    font-size: 0.85rem;
    border-radius: 8px;
  }

  .contact-label {
    font-size: 0.65rem;
    margin-bottom: 0.2rem;
    color: #999;
  }

  .contact-link,
  .contact-text {
    font-size: 0.8rem;
    color: #d0d0d0;
    line-height: 1.4;
  }

  .footer-copyright {
    padding: 1rem 1.25rem 1rem 1.25rem !important;
    margin: 0 !important;
  }

  .footer-custom .text-center small {
    font-size: 0.75rem;
    color: #b0b0b0;
    display: block;
    line-height: 1.5;
  }

  .version-text {
    font-size: 0.7rem;
    margin-top: 0.4rem;
    color: #888;
  }
}

@media (max-width: 576px) {
  .footer-custom {
    padding: 1.25rem 0 0 0;
    margin: 0;
  }

  .footer-custom .container {
    padding-left: 0;
    padding-right: 0;
    max-width: 100%;
    margin: 0;
  }

  .footer-custom .row {
    margin-left: 0;
    margin-right: 0;
  }

  .footer-custom .row > div {
    padding-left: 1rem;
    padding-right: 1rem;
    margin-bottom: 1rem;
  }

  .footer-title {
    font-size: 0.85rem;
    margin-bottom: 0.5rem;
    padding-bottom: 0.35rem;
    font-weight: 600;
  }

  .footer-title::after {
    width: 28px;
    height: 2px;
  }

  .footer-description {
    font-size: 0.75rem;
    line-height: 1.5;
    margin-bottom: 0.5rem;
    color: #c0c0c0;
  }

  .logo-text {
    font-size: 1.1rem;
  }

  .footer-links {
    margin-top: 0.4rem;
  }

  .footer-links li {
    margin-bottom: 0.35rem;
  }

  .footer-link {
    font-size: 0.8rem;
    padding: 0.1rem 0;
    color: #d0d0d0;
  }

  .social-links {
    gap: 0.6rem;
    margin-top: 0.4rem;
  }

  .social-link {
    width: 34px;
    height: 34px;
    font-size: 1rem;
  }

  .contact-info {
    gap: 0.7rem;
    margin-top: 0.4rem;
  }

  .contact-item {
    gap: 0.6rem;
    align-items: center;
  }

  .contact-icon {
    width: 28px;
    height: 28px;
    min-width: 28px;
    font-size: 0.8rem;
    border-radius: 7px;
  }

  .contact-label {
    font-size: 0.6rem;
    margin-bottom: 0.15rem;
    color: #999;
  }

  .contact-link,
  .contact-text {
    font-size: 0.75rem;
    color: #d0d0d0;
    line-height: 1.4;
  }

  .footer-copyright {
    padding: 0.875rem 1rem 0.875rem 1rem !important;
    margin: 0 !important;
  }

  .footer-custom .text-center small {
    font-size: 0.7rem;
    display: block;
    line-height: 1.5;
    color: #b0b0b0;
    margin-bottom: 0.25rem;
  }

  .version-text {
    font-size: 0.65rem;
    margin-top: 0.3rem;
    color: #888;
  }
}

@media (max-width: 480px) {
  .footer-custom {
    padding: 1rem 0 0 0;
    margin: 0;
  }

  .footer-custom .container {
    padding-left: 0;
    padding-right: 0;
    max-width: 100%;
    margin: 0;
  }

  .footer-custom .row {
    margin-left: 0;
    margin-right: 0;
  }

  .footer-custom .row > div {
    padding-left: 1rem;
    padding-right: 1rem;
    margin-bottom: 0.875rem;
  }

  .footer-title {
    font-size: 0.8rem;
    margin-bottom: 0.45rem;
    padding-bottom: 0.3rem;
    font-weight: 600;
  }

  .footer-title::after {
    width: 25px;
    height: 2px;
  }

  .footer-description {
    font-size: 0.7rem;
    line-height: 1.5;
    margin-bottom: 0.4rem;
    color: #c0c0c0;
  }

  .logo-text {
    font-size: 1rem;
  }

  .footer-links {
    margin-top: 0.35rem;
  }

  .footer-links li {
    margin-bottom: 0.3rem;
  }

  .footer-link {
    font-size: 0.75rem;
    padding: 0.1rem 0;
    color: #d0d0d0;
  }

  .social-links {
    gap: 0.55rem;
    margin-top: 0.35rem;
  }

  .social-link {
    width: 32px;
    height: 32px;
    font-size: 0.95rem;
  }

  .contact-info {
    gap: 0.65rem;
    margin-top: 0.35rem;
  }

  .contact-item {
    gap: 0.55rem;
    align-items: center;
  }

  .contact-icon {
    width: 26px;
    height: 26px;
    min-width: 26px;
    font-size: 0.75rem;
    border-radius: 6px;
  }

  .contact-label {
    font-size: 0.58rem;
    margin-bottom: 0.1rem;
    color: #999;
  }

  .contact-link,
  .contact-text {
    font-size: 0.7rem;
    color: #d0d0d0;
    line-height: 1.4;
  }

  .footer-copyright {
    padding: 0.75rem 1rem 0.75rem 1rem !important;
    margin: 0 !important;
  }

  .footer-custom .text-center small {
    font-size: 0.68rem;
    display: block;
    line-height: 1.5;
    color: #b0b0b0;
    margin-bottom: 0.2rem;
  }

  .version-text {
    font-size: 0.62rem;
    margin-top: 0.25rem;
    color: #888;
  }
}
</style>

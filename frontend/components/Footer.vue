<template>
  <footer class="footer-custom text-light pt-5 pb-3">
    <div class="container">
      <div class="row">

        <!-- Sección de información -->
        <div class="col-md-4 mb-4">
          <h5 class="fw-bold">GoLive</h5>
          <p>{{ $t('GoLiveInfo') }}</p>
        </div>

        <!-- Sección de enlaces rápidos -->
        <div class="col-md-4 mb-4">
          <h5 class="fw-bold">{{ $t('Enlaces') }}</h5>
          <ul class="list-unstyled">
            <li><a href="/" class="text-light text-decoration-none">{{ $t('Inicio') }}</a></li>
            <li><a href="/conciertos" class="text-light text-decoration-none">{{ $t('Conciertos') }}</a></li>
            <li><a href="/festivales" class="text-light text-decoration-none">{{ $t('Festivales') }}</a></li>
            <li><a href="/blog" class="text-light text-decoration-none">{{ $t('Blog') }}</a></li>
          </ul>
        </div>

        <!-- Sección de redes sociales -->
        <div class="col-md-4 mb-4">
          <h5 class="fw-bold">{{ $t('Síguenos') }}</h5>
          <div class="d-flex gap-3 mt-2">
            <a href="#" class="text-light fs-4"><i class="bi bi-facebook"></i></a>
            <a href="#" class="text-light fs-4"><i class="bi bi-twitter"></i></a>
            <a href="#" class="text-light fs-4"><i class="bi bi-instagram"></i></a>
            <a href="#" class="text-light fs-4"><i class="bi bi-youtube"></i></a>
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
import packageInfo from '~/package.json'
const version = packageInfo.version || 'Desarrollo'
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');

.footer-custom {
  background-color: #0a0a0a;
  font-family: 'Poppins', sans-serif;
}

footer a:hover {
  color: #ff0057;
  text-decoration: none;
}

.version-text {
  color: #aaa;
  font-size: 0.85rem;
}
</style>

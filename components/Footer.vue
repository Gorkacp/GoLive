<template>
  <footer class="footer-custom text-light pt-5 pb-3 mt-5">
    <div class="container">
      <div class="row">

        <!-- Sección de información -->
        <div class="col-md-4 mb-4">
          <h5 class="fw-bold">GoLive</h5>
          <p>Compra entradas para conciertos y festivales de manera rápida y segura. Vive la música al máximo.</p>
        </div>

        <!-- Sección de enlaces rápidos -->
        <div class="col-md-4 mb-4">
          <h5 class="fw-bold">Enlaces</h5>
          <ul class="list-unstyled">
            <li><a href="/" class="text-light text-decoration-none">Inicio</a></li>
            <li><a href="/conciertos" class="text-light text-decoration-none">Conciertos</a></li>
            <li><a href="/festivales" class="text-light text-decoration-none">Festivales</a></li>
            <li><a href="/blog" class="text-light text-decoration-none">Blog</a></li>
          </ul>
        </div>

        <!-- Sección de redes sociales -->
        <div class="col-md-4 mb-4">
          <h5 class="fw-bold">Síguenos</h5>
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
        <small>&copy; 2025 GoLive. Todos los derechos reservados.</small>
      </div>
    </div>
  </footer>
</template>

<script setup>
// No se requiere lógica adicional
</script>

<style scoped>
/* Footer muy oscuro */
.footer-custom {
  background-color: #0a0a0a; /* Negro muy profundo */
}

/* Hover sobre enlaces */
footer a:hover {
  color: #ff0057; /* Color rosa similar al header */
  text-decoration: none;
}
</style>

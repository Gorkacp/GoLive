<template>
  <div class="perfil-page">
    <Header />
    <div class="container mt-5">
      <h1>{{ $t('Perfil') }}</h1>
      <p>{{ $t('Tu información de perfil aparecerá aquí') }}</p>
    </div>
    <Footer />
  </div>
</template>

<script setup>
import Header from '~/components/Header.vue'
import Footer from '~/components/Footer.vue'
</script>

<style scoped>
.perfil-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  flex: 1;
  padding: 20px;
}

h1 {
  font-family: 'Poppins', sans-serif;
  font-weight: 700;
  color: #0a0a0a;
}

p {
  font-family: 'Poppins', sans-serif;
  color: #6b7280;
}
</style>

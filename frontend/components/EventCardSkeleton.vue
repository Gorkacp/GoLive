<template>
  <div class="card-skeleton h-100">
    <div class="skeleton-image"></div>
    <div class="card-body-skeleton d-flex flex-column">
      <div class="skeleton-title"></div>
      <div class="skeleton-text"></div>
      <div class="skeleton-text short"></div>
      <div class="skeleton-price"></div>
    </div>
  </div>
</template>

<style scoped>
.card-skeleton {
  background: linear-gradient(135deg, #ff0057 0%, #ff6b35 100%);
  border: none;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(255, 0, 87, 0.2);
}

.skeleton-image {
  height: 200px;
  width: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.1) 25%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.card-body-skeleton {
  padding: 16px;
  background: #0a0a0a;
  gap: 8px;
}

.skeleton-title {
  height: 20px;
  width: 80%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.1) 25%,
    rgba(255, 255, 255, 0.2) 50%,
    rgba(255, 255, 255, 0.1) 75%
  );
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
  margin-bottom: 8px;
}

.skeleton-text {
  height: 14px;
  width: 100%;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0.08) 25%,
    rgba(255, 255, 255, 0.15) 50%,
    rgba(255, 255, 255, 0.08) 75%
  );
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
  animation-delay: 0.2s;
}

.skeleton-text.short {
  width: 60%;
}

.skeleton-price {
  height: 18px;
  width: 40%;
  background: linear-gradient(
    90deg,
    rgba(255, 0, 87, 0.3) 25%,
    rgba(255, 0, 87, 0.5) 50%,
    rgba(255, 0, 87, 0.3) 75%
  );
  background-size: 200% 100%;
  border-radius: 4px;
  animation: shimmer 1.5s infinite;
  animation-delay: 0.4s;
  margin-top: auto;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}
</style>

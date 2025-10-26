<template>
  <div v-if="message" class="toast">{{ message }}</div>
</template>

<script setup>
const props = defineProps({ message: String })
</script>

<style scoped>
.toast {
  position: fixed;
  right: 16px;
  bottom: 16px;
  background: linear-gradient(135deg, #ff758c, #ff7eb3);
  color: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0,0,0,0.25);
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>

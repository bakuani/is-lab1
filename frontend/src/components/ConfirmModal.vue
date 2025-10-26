<template>
  <div class="modal-backdrop">
    <div class="modal small">
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      <div class="form-actions">
        <button @click="$emit('cancel')">Отмена</button>
        <button @click="$emit('confirm')">Да, удалить</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({ title: { type: String, default: 'Подтвердите' }, message: { type: String, default: '' } })
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal.small {
  width: 360px;
  padding: 16px;
  background: linear-gradient(135deg, #fff1f3, #ffe4e1);
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(0,0,0,0.25);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}
</style>

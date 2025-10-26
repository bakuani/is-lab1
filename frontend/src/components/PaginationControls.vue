<template>
  <div class="pagination">
    <button :disabled="page===0" @click="$emit('change-page', page-1)">Prev</button>
    <span>Page {{ page + 1 }} / {{ totalPages }}</span>
    <button :disabled="page+1>=totalPages" @click="$emit('change-page', page+1)">Next</button>
    <select :value="pageSize" @change="$emit('change-page-size', Number($event.target.value))">
      <option :value="5">5</option>
      <option :value="10">10</option>
      <option :value="25">25</option>
    </select>
  </div>
</template>

<script setup>
const props = defineProps({ page: Number, totalPages: Number, pageSize: Number })
</script>

<style scoped>
.pagination {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.pagination button {
  padding: 6px 10px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(45deg,#ff758c,#ff7eb3);
  color: white;
  cursor: pointer;
}
.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination select {
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  background: white;
}

</style>

<template>
  <div v-if="show" class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal history-modal">
      <h2>История импорта</h2>

      <div v-if="loading" class="loading">Загрузка...</div>

      <div v-else-if="history.length === 0" class="no-history">
        История операций пуста.
      </div>

      <div v-else class="history-table-container">
        <table class="history-table">
          <thead>
          <tr>
            <th>ID</th>
            <th>Время</th>
            <th>Пользователь</th>
            <th>Статус</th>
            <th>Детали</th>
            <th>Файл</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in history" :key="item.id" :class="item.status.toLowerCase()">
            <td>{{ item.id }}</td>
            <td>{{ formatTime(item.operationTime) }}</td>
            <td>{{ item.userName }}</td>
            <td>
              <span class="status-tag">{{ item.status }}</span>
            </td>
            <td>
              <div v-if="item.status === 'SUCCESS'">
                Добавлено: <strong>{{ item.importedCount }}</strong>
              </div>
              <div v-if="item.status === 'FAILED'" class="error-message">
                {{ item.errorMessage }}
              </div>
            </td>
            <td>
              <a v-if="item.status === 'SUCCESS' && item.minioFileKey" :href="downloadUrl(item.id)" target="_blank" rel="noopener">
                Скачать
              </a>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <div class="form-actions">
        <button type="button" @click="$emit('close')">Закрыть</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import * as api from '../utilities/api.js'

const props = defineProps({
  show: Boolean
})
const emit = defineEmits(['close'])

const history = ref([])
const loading = ref(false)

async function fetchHistory() {
  loading.value = true
  try {
    history.value = await api.fetchImportHistory()
  } catch (e) {
    console.error('Не удалось загрузить историю импорта', e)

  } finally {
    loading.value = false
  }
}


watch(() => props.show, (newVal) => {
  if (newVal) {
    history.value = []
    fetchHistory()
  }
})

function formatTime(isoString) {
  if (!isoString) return ''

  const safeIsoString = isoString.endsWith('Z') ? isoString : isoString + 'Z';
  return new Date(safeIsoString).toLocaleString('ru-RU')
}

function downloadUrl(id) {
  return `${api.API_BASE_URL}/import-history/${id}/download`
}
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

.modal {
  background: linear-gradient(135deg, #fff1f3, #ffe4e1);
  padding: 24px;
  border-radius: 16px;
  width: 800px;
  max-height: 90vh;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}

.history-table-container {
  overflow-y: auto;
  max-height: 60vh;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: inset 0 0 5px rgba(0,0,0,0.1);
}

.history-table {
  width: 100%;
  border-collapse: collapse;
}

.history-table th,
.history-table td {
  padding: 8px 12px;
  text-align: left;
  border-bottom: 1px solid #ffe4e1;
}

.history-table th {
  background: #ff7eb3;
  color: white;
  position: sticky;
  top: 0;
}

.history-table tr.success {
  background-color: #f0fff0;
}
.history-table tr.failed {
  background-color: #fff8f8;
}

.status-tag {
  font-weight: bold;
}
.history-table tr.success .status-tag {
  color: #5704ac;
}
.history-table tr.failed .status-tag {
  color: #dc3545;
}

.error-message {
  color: #721c24;
  font-size: 0.9em;
  max-width: 300px;
  word-break: break-word;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ffcdd2;
}

.loading, .no-history {
  padding: 40px;
  text-align: center;
  font-size: 1.1em;
  color: #555;
}
</style>
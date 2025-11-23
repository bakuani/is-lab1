<template>
  <div v-if="show" class="modal-backdrop">
    <div class="modal">
      <h2>{{ mode === 'create' ? 'Создать группу' : mode === 'edit' ? 'Редактировать' : 'Просмотр' }}</h2>
      <form @submit.prevent="onSubmit">
        <div class="form-row"><label>Название</label><input v-model="local.name" :disabled="isView" required/></div>
        <div class="form-row"><label>Students Count</label><input type="number" v-model.number="local.studentsCount" :disabled="isView" min="1" required/></div>
        <div class="form-row"><label>Expelled Students</label><input type="number" v-model.number="local.expelledStudents" :disabled="isView" min="0" required/></div>
        <div class="form-row"><label>Transferred Students</label><input type="number" v-model.number="local.transferredStudents" :disabled="isView" min="0" required/></div>
        <div class="form-row"><label>Average Mark</label><input type="number" v-model.number="local.averageMark" :disabled="isView" min="1" required/></div>

        <div class="form-row">
          <label>Form</label>
          <select v-model="local.formOfEducation" :disabled="isView" required>
            <option disabled value="">Выберите</option>
            <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
            <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
            <option value="EVENING_CLASSES">EVENING_CLASSES</option>
          </select>
        </div>

        <div class="form-row">
          <label>Semester</label>
          <select v-model="local.semesterEnum" :disabled="isView">
            <option value="">(null)</option>
            <option value="FIRST">FIRST</option>
            <option value="SECOND">SECOND</option>
            <option value="SEVENTH">SEVENTH</option>
          </select>
        </div>

        <div class="form-row">
          <label>Coordinates X</label><input type="number" step="any" v-model.number="local.coordinates.x" :disabled="isView" required/>
        </div>
        <div class="form-row">
          <label>Coordinates Y</label><input type="number" step="any" v-model.number="local.coordinates.y" :disabled="isView" required/>
        </div>

        <div class="form-row">
          <label>Should Be Expelled</label>
          <input type="number" v-model.number="local.shouldBeExpelled" :disabled="isView" min="1" required/>
        </div>

        <div class="form-row">
          <label>Admin (choose existing)</label>
          <select v-model="local.groupAdminId" @change="onAdminChange" :disabled="isView">
            <option value="">-- (создать нового / оставить пустым) --</option>
            <option v-for="p in persons" :key="p.id" :value="p.id">{{ p.name }} (id:{{ p.id }})</option>
          </select>
          <button v-if="!isView" type="button" @click="resetGroupAdminToEmpty">Очистить / новый</button>
        </div>

        <div class="admin-nested">
          <h4>Admin</h4>
          <div class="form-row"><label>Name</label><input v-model="local.groupAdmin.name" :disabled="isView" required/></div>
          <div class="form-row"><label>Eye Color</label><select v-model="local.groupAdmin.eyeColor" :disabled="isView" required><option disabled value="">Выберите</option><option value="RED">RED</option><option value="GREEN">GREEN</option><option value="BLUE">BLUE</option></select></div>
          <div class="form-row"><label>Hair Color</label><select v-model="local.groupAdmin.hairColor" :disabled="isView" required><option disabled value="">Выберите</option><option value="RED">RED</option><option value="GREEN">GREEN</option><option value="BLUE">BLUE</option></select></div>
          <div class="form-row"><label>Weight</label><input type="number" v-model.number="local.groupAdmin.weight" :disabled="isView" min="0" required/></div>
          <div class="form-row"><label>Nationality</label><select v-model="local.groupAdmin.nationality" :disabled="isView" required><option disabled value="">Выберите</option><option value="INDIA">INDIA</option><option value="UNITED_KINGDOM">UNITED_KINGDOM</option><option value="JAPAN">JAPAN</option></select></div>
          <div class="form-row"><label>X</label><input type="number" v-model.number="local.groupAdmin.location.x" :disabled="isView" required/></div>
          <div class="form-row"><label>Y</label><input type="number" v-model.number="local.groupAdmin.location.y" :disabled="isView" required/></div>
          <div class="form-row"><label>Name</label><input v-model="local.groupAdmin.location.name" :disabled="isView" required/></div>
        </div>

        <div class="form-actions">
          <button type="button" @click="$emit('close')">Отмена</button>
          <button type="submit" v-if="!isView">Сохранить</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import {computed, reactive, toRefs, watch} from 'vue'

const props = defineProps({
  show: Boolean,
  mode: { type: String, default: 'view' },
  form: Object,
  persons: Array
})
const emit = defineEmits(['close', 'submit'])

const local = reactive(JSON.parse(JSON.stringify(props.form || {})))

watch(() => props.form, (f) => {
  if (!f) return
  Object.assign(local, JSON.parse(JSON.stringify(f)))
}, { deep: true, immediate: true })

const isView = computed(() => props.mode === 'view')

function resetGroupAdminToEmpty() {
  local.groupAdmin = {
    name: '',
    eyeColor: '',
    hairColor: '',
    weight: null,
    nationality: '',
    location: { x: null, y: null, name: '' }
  }
  local.groupAdminId = null
}

function onAdminChange() {
  const id = local.groupAdminId
  const found = (props.persons || []).find(p => p.id === id)
  if (found) {
    local.groupAdmin = JSON.parse(JSON.stringify(found))
    if (!local.groupAdmin.location) local.groupAdmin.location = { x: null, y: null, name: '' }
  } else {
    resetGroupAdminToEmpty()
  }
}

function onSubmit() {
  const adminToSend = (local.groupAdmin && local.groupAdmin.name && local.groupAdmin.name.trim() !== '') ? JSON.parse(JSON.stringify(local.groupAdmin)) : null
  const payload = {
    id: local.id,
    name: local.name,
    coordinates: local.coordinates,
    studentsCount: local.studentsCount,
    expelledStudents: local.expelledStudents,
    transferredStudents: local.transferredStudents,
    formOfEducation: local.formOfEducation,
    shouldBeExpelled: local.shouldBeExpelled,
    averageMark: local.averageMark,
    semesterEnum: local.semesterEnum || null,
    groupAdmin: adminToSend
  }
  emit('submit', payload)
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
  animation: fadeIn 0.25s ease-out;
}

.modal {
  background: linear-gradient(135deg, #fff1f3, #ffe4e1);
  padding: 24px;
  border-radius: 16px;
  width: 680px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
  position: relative;
}

.admin-nested {
  background: #ffeef0;
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px;
}

.form-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
}

.form-row label {
  width: 160px;
  font-weight: 500;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

select {
  appearance: none;
  background: linear-gradient(120deg, #ffd1dc, #ffb6c1);
  border: 1px solid #ccc;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  width: 200px;
  font-size: 14px;
}

select:focus {
  border-color: #ff7eb3;
  box-shadow: 0 0 6px rgba(255, 126, 179, 0.5);
  outline: none;
}

.admin-nested select {
  width: 180px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>

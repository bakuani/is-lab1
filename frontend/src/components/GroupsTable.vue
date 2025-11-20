<template>
  <section class="table-section">
    <table class="groups-table">
      <thead>
      <tr>
        <th @click="$emit('change-sort', 'id')">ID</th>
        <th @click="$emit('change-sort', 'name')">Name</th>
        <th @click="$emit('change-sort', 'studentsCount')">Students</th>
        <th @click="$emit('change-sort', 'expelledStudents')">Expelled</th>
        <th @click="$emit('change-sort', 'shouldBeExpelled')">Should Be Expelled</th>
        <th @click="$emit('change-sort', 'averageMark')">Average Mark</th>
        <th @click="$emit('change-sort', 'formOfEducation')">Form</th>
        <th @click="$emit('change-sort', 'semesterEnum')">Semester</th>
        <th @click="$emit('change-sort', 'groupAdmin.name')">Admin</th>
        <th>Actions</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="g in groups" :key="g.id">
        <td>{{ g.id }}</td>

        <td>
          <div v-if="isEditing(g.id, 'name')">
            <input v-model="editing.value" @blur="saveEdit(g, 'name')" @keydown.enter.prevent="saveEdit(g, 'name')" />
          </div>
          <div v-else @click="startEdit(g, 'name')">{{ g.name }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'studentsCount')">
            <input type="number" v-model.number="editing.value" @blur="saveEdit(g, 'studentsCount')" @keydown.enter.prevent="saveEdit(g, 'studentsCount')" />
          </div>
          <div v-else @click="startEdit(g, 'studentsCount')">{{ g.studentsCount }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'expelledStudents')">
            <input type="number" v-model.number="editing.value" @blur="saveEdit(g, 'expelledStudents')" @keydown.enter.prevent="saveEdit(g, 'expelledStudents')" />
          </div>
          <div v-else @click="startEdit(g, 'expelledStudents')">{{ g.expelledStudents }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'shouldBeExpelled')">
            <input type="number" v-model.number="editing.value" @blur="saveEdit(g, 'shouldBeExpelled')" @keydown.enter.prevent="saveEdit(g, 'shouldBeExpelled')" />
          </div>
          <div v-else @click="startEdit(g, 'shouldBeExpelled')">{{ g.shouldBeExpelled }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'averageMark')">
            <input type="number" v-model.number="editing.value" @blur="saveEdit(g, 'averageMark')" @keydown.enter.prevent="saveEdit(g, 'averageMark')" />
          </div>
          <div v-else @click="startEdit(g, 'averageMark')">{{ g.averageMark }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'formOfEducation')">
            <select v-model="editing.value" @change="saveEdit(g, 'formOfEducation')" @blur="saveEdit(g, 'formOfEducation')">
              <option value="">--</option>
              <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
              <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
              <option value="EVENING_CLASSES">EVENING_CLASSES</option>
            </select>
          </div>
          <div v-else @click="startEdit(g, 'formOfEducation')">{{ g.formOfEducation }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'semesterEnum')">
            <select v-model="editing.value" @change="saveEdit(g, 'semesterEnum')" @blur="saveEdit(g, 'semesterEnum')">
              <option value="">(null)</option>
              <option value="FIRST">FIRST</option>
              <option value="SECOND">SECOND</option>
              <option value="SEVENTH">SEVENTH</option>
            </select>
          </div>
          <div v-else @click="startEdit(g, 'semesterEnum')">{{ g.semesterEnum }}</div>
        </td>

        <td>
          <div v-if="isEditing(g.id, 'groupAdmin.name')">
            <input v-model="editing.value" @blur="saveEdit(g, 'groupAdmin.name')" @keydown.enter.prevent="saveEdit(g, 'groupAdmin.name')" />
          </div>
          <div v-else @click="startEdit(g, 'groupAdmin.name')">{{ g.groupAdmin?.name }}</div>
        </td>

        <td class="actions">
          <button @click="$emit('view-group', g.id)">View</button>
          <button @click="$emit('open-edit', g)">Edit</button>
          <button @click="$emit('confirm-delete', g.id)">Delete</button>
        </td>
      </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
import {reactive} from 'vue'

const props = defineProps({groups: {type: Array, default: () => []}, sortField: String, sortDir: String})
const emit = defineEmits(['change-sort', 'update-group', 'view-group', 'open-edit', 'confirm-delete'])

const editing = reactive({id: null, field: '', value: null})

function isEditing(id, field) {
  return editing.id === id && editing.field === field
}

function startEdit(obj, field) {
  editing.id = obj.id
  editing.field = field
  const parts = field.split('.')
  let v = obj
  for (const p of parts) {
    v = v == null ? undefined : v[p]
  }
  editing.value = v == null ? '' : v
}

function saveEdit(originalGroup, field) {
  emit('update-group', {id: originalGroup.id, field, value: editing.value})
  editing.id = null
  editing.field = ''
  editing.value = null
}
</script>

<style scoped>
.groups-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.groups-table th,
.groups-table td {
  padding: 10px 12px;
  text-align: left;
  vertical-align: middle;
}

.groups-table th {
  background: linear-gradient(90deg, #ff758c, #ff7eb3);
  color: #fff;
  cursor: pointer;
}

.groups-table tr:nth-child(even) {
  background: #f9f9f9;
}

.groups-table td div[role="editable"], .groups-table td input, .groups-table td select {
  width: 100%;
}

.actions {
  display: flex;
  gap: 6px;
  align-items: center;
}

.actions button {
  padding: 6px 8px;
  border-radius: 8px;
  background: linear-gradient(45deg, #6b7280, #9ca3af);
  color: white;
  border: none;
  cursor: pointer;
}

.actions button:hover {
  transform: translateY(-1px);
}

.groups-table td > div:not(.editing):hover {
  background: rgba(255, 126, 179, 0.06);
  cursor: pointer;
  border-radius: 4px;
  padding: 6px;
}
</style>
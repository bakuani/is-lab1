<template>
  <div class="app">
    <header>
      <h1>StudyGroups — Управление группами</h1>
      <div class="controls">
        <button @click="openCreate">Создать группу</button>
        <div class="filter">
          <select v-model="filterField">
            <option value="">-- Фильтр по полю --</option>
            <option value="name">name</option>
            <option value="formOfEducation">formOfEducation</option>
            <option value="semesterEnum">semesterEnum</option>
            <option value="groupAdminName">groupAdmin.name</option>
          </select>
          <input v-model="filterValue" placeholder="Значение (полное совпадение)"/>
          <button @click="applyFilter">Применить</button>
          <button @click="clearFilter">Сброс</button>
        </div>
      </div>
    </header>

    <main>
      <section class="table-section">
        <table class="groups-table">
          <thead>
          <tr>
            <th @click="changeSort('id')">ID <span v-if="sortField==='id'">{{ sortDir }}</span></th>
            <th @click="changeSort('name')">Name <span v-if="sortField==='name'">{{ sortDir }}</span></th>
            <th @click="changeSort('studentsCount')">Students</th>
            <th>Expelled</th>
            <th>Average Mark</th>
            <th>Form</th>
            <th>Semester</th>
            <th>Admin</th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="g in groups" :key="g.id">
            <td>{{ g.id }}</td>
            <td>{{ g.name }}</td>
            <td>{{ g.studentsCount }}</td>
            <td>{{ g.expelledStudents }}</td>
            <td>{{ g.averageMark }}</td>
            <td>{{ g.formOfEducation }}</td>
            <td>{{ g.semesterEnum }}</td>
            <td>{{ g.groupAdmin?.name }}</td>
            <td class="actions">
              <button @click="viewGroup(g.id)">View</button>
              <button @click="openEdit(g)">Edit</button>
              <button @click="confirmDelete(g.id)">Delete</button>
            </td>
          </tr>
          </tbody>
        </table>

        <div class="pagination">
          <button :disabled="page===0" @click="goToPage(page-1)">Prev</button>
          <span>Page {{ page + 1 }} / {{ totalPages }}</span>
          <button :disabled="page+1>=totalPages" @click="goToPage(page+1)">Next</button>
          <select v-model.number="pageSize" @change="fetchGroups">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="25">25</option>
          </select>
        </div>
      </section>

      <aside class="side">
        <h3>Специальные операции</h3>
        <div class="special">
          <!-- ... спецоперации без изменений ... -->
        </div>
      </aside>
    </main>

    <!-- Modals -->
    <div v-if="showModal" class="modal-backdrop">
      <div class="modal">
        <h2>{{ modalMode === 'create' ? 'Создать группу' : modalMode === 'edit' ? 'Редактировать' : 'Просмотр' }}</h2>
        <form @submit.prevent="submitModal">
          <div class="form-row">
            <label>Название</label>
            <input v-model="form.name" :disabled="modalMode==='view'" required/>
          </div>

          <div class="form-row">
            <label>Students Count</label>
            <input type="number" v-model.number="form.studentsCount" :disabled="modalMode==='view'" min="1" required/>
          </div>

          <div class="form-row">
            <label>Expelled Students</label>
            <input type="number" v-model.number="form.expelledStudents" :disabled="modalMode==='view'" min="0"
                   required/>
          </div>

          <div class="form-row">
            <label>Transferred Students</label>
            <input type="number" v-model.number="form.transferredStudents" :disabled="modalMode==='view'" min="0"
                   required/>
          </div>

          <div class="form-row">
            <label>Average Mark</label>
            <input type="number" v-model.number="form.averageMark" :disabled="modalMode==='view'" min="1" required/>
          </div>

          <div class="form-row">
            <label>Form</label>
            <select v-model="form.formOfEducation" :disabled="modalMode==='view'" required>
              <option disabled value="">Выберите</option>
              <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
              <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
              <option value="EVENING_CLASSES">EVENING_CLASSES</option>
            </select>
          </div>

          <div class="form-row">
            <label>Semester</label>
            <select v-model="form.semesterEnum" :disabled="modalMode==='view'">
              <option value="">(null)</option>
              <option value="FIRST">FIRST</option>
              <option value="SECOND">SECOND</option>
              <option value="SEVENTH">SEVENTH</option>
            </select>
          </div>

          <!-- Coordinates -->
          <div class="form-row">
            <label>Coordinates X</label>
            <input type="number" step="any" v-model.number="form.coordinates.x" :disabled="modalMode==='view'"
                   required/>
          </div>
          <div class="form-row">
            <label>Coordinates Y</label>
            <input type="number" step="any" v-model.number="form.coordinates.y" :disabled="modalMode==='view'"
                   required/>
          </div>

          <div class="form-row">
            <label>Should Be Expelled</label>
            <input type="number" v-model.number="form.shouldBeExpelled" :disabled="modalMode==='view'" min="1"
                   required/>
          </div>

          <!-- Admin select: выбор существующего / новый -->
          <div class="form-row">
            <label>Admin (choose existing)</label>
            <select v-model="form.groupAdminId" @change="onAdminChange" :disabled="modalMode==='view'">
              <option value="">-- (создать нового / оставить пустым) --</option>
              <option v-for="p in persons" :key="p.id" :value="p.id">{{ p.name }} (id:{{ p.id }})</option>
            </select>
            <button type="button" v-if="modalMode!=='view'" @click="resetGroupAdminToEmpty">Очистить / новый</button>
          </div>

          <!-- Admin block (ВСЕГДА виден) -->
          <div class="admin-nested">
            <h4>Admin</h4>
            <div class="form-row">
              <label>Name</label>
              <input v-model="form.groupAdmin.name" @input="onAdminFieldChange" required/>
            </div>
            <div class="form-row">
              <label>Eye Color</label>
              <input v-model="form.groupAdmin.eyeColor" @input="onAdminFieldChange" required/>
            </div>
            <div class="form-row">
              <label>Hair Color</label>
              <input v-model="form.groupAdmin.hairColor" @input="onAdminFieldChange" required/>
            </div>
            <div class="form-row">
              <label>Weight</label>
              <input type="number" v-model.number="form.groupAdmin.weight" @input="onAdminFieldChange" min="0"
                     required/>
            </div>
            <div class="form-row">
              <label>Nationality</label>
              <input v-model="form.groupAdmin.nationality" @input="onAdminFieldChange" required/>
            </div>

            <h5>Location</h5>
            <div class="form-row">
              <label>X</label>
              <input type="number" v-model.number="form.groupAdmin.location.x" @input="onAdminFieldChange" required/>
            </div>
            <div class="form-row">
              <label>Y</label>
              <input type="number" v-model.number="form.groupAdmin.location.y" @input="onAdminFieldChange" required/>
            </div>
            <div class="form-row">
              <label>Name</label>
              <input v-model="form.groupAdmin.location.name" @input="onAdminFieldChange" required/>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal">Отмена</button>
            <button type="submit" v-if="modalMode!=='view'">Сохранить</button>
          </div>
        </form>

        <div v-if="modalMode==='view'" class="detail">
          <h3>Связанные объекты</h3>
          <pre>{{ form }}</pre>
        </div>
      </div>
    </div>

    <!-- Toast -->
    <div class="toast" v-if="toast.message">{{ toast.message }}</div>
  </div>
</template>

<script setup>
import {ref, reactive, onMounted, onBeforeUnmount} from 'vue'
import * as api from '../utilities/api.js'
import Stomp from 'stompjs'
import SockJS from 'sockjs-client'

// --- state ---
const groups = ref([])
const page = ref(0)
const pageSize = ref(10)
const totalPages = ref(1)
const sortField = ref('id')
const sortDir = ref('asc')
const filterField = ref('')
const filterValue = ref('')

const showModal = ref(false)
const modalMode = ref('view')

// form: заранее задаём структуру, чтобы v-model не ломался
const form = reactive({
  id: null,
  name: '',
  coordinates: {x: null, y: null},
  studentsCount: 1,
  expelledStudents: 0,
  transferredStudents: 1,
  formOfEducation: '',
  shouldBeExpelled: 1,
  averageMark: 1,
  semesterEnum: '',
  groupAdminId: null, // только для select
  groupAdmin: {       // всегда объект — блок админа всегда виден
    name: '',
    eyeColor: '',
    hairColor: '',
    weight: null,
    nationality: '',
    location: {x: null, y: null, name: ''}
  }
})

const persons = ref([])

const toast = reactive({message: '', timeout: null})
let adminOriginalSnapshot = null

// --- utils ---
function showToast(msg, ms = 4000) {
  clearTimeout(toast.timeout)
  toast.message = msg
  toast.timeout = setTimeout(() => (toast.message = ''), ms)
}

// --- fetchers ---
async function fetchGroups() {
  try {
    const params = {page: page.value, size: pageSize.value}
    if (sortField.value) params.sort = `${sortField.value},${sortDir.value}`
    if (filterField.value && filterValue.value) {
      params.filterField = filterField.value
      params.filterValue = filterValue.value
    }
    const data = await api.fetchGroups(params)
    groups.value = data.content ?? data
    totalPages.value = data.totalPages ?? Math.max(1, Math.ceil((data.totalElements ?? groups.value.length) / pageSize.value))
  } catch (e) {
    console.error(e)
    showToast('Ошибка при загрузке групп')
  }
}

async function fetchPersonsList() {
  try {
    persons.value = await api.fetchPersons()
  } catch (e) {
    console.warn('fetchPersons failed', e)
    persons.value = []
  }
}

// --- admin handlers ---
function resetGroupAdminToEmpty() {
  form.groupAdmin = {
    name: '',
    eyeColor: '',
    hairColor: '',
    weight: null,
    nationality: '',
    location: {x: null, y: null, name: ''}
  }
  form.groupAdminId = null
  adminOriginalSnapshot = null
}

function onAdminChange() {
  const id = form.groupAdminId
  const found = persons.value.find(p => p.id === id)
  if (found) {
    form.groupAdmin = JSON.parse(JSON.stringify(found))
    if (!form.groupAdmin.location) form.groupAdmin.location = {x: null, y: null, name: ''}
    adminOriginalSnapshot = JSON.stringify(form.groupAdmin)
  } else {
    resetGroupAdminToEmpty()
  }
}

function onAdminFieldChange() {
  if (!adminOriginalSnapshot) return
  const current = JSON.stringify(form.groupAdmin)
  if (current !== adminOriginalSnapshot) {
    form.groupAdmin.id = null
    form.groupAdminId = null
    adminOriginalSnapshot = null
    showToast('Изменения админа будут сохранены как новый человек')
  }
}

// --- modal & CRUD ---
function openCreate() {
  modalMode.value = 'create'
  Object.assign(form, {
    id: null,
    name: '',
    coordinates: {x: null, y: null},
    studentsCount: 1,
    expelledStudents: 0,
    transferredStudents: 1,
    formOfEducation: '',
    shouldBeExpelled: 1,
    averageMark: 1,
    semesterEnum: '',
    groupAdminId: null,
    groupAdmin: {
      name: '',
      eyeColor: '',
      hairColor: '',
      weight: null,
      nationality: '',
      location: {x: null, y: null, name: ''}
    }
  })
  resetGroupAdminToEmpty()
  showModal.value = true
}

function openEdit(g) {
  modalMode.value = 'edit'
  Object.assign(form, JSON.parse(JSON.stringify(g)))
  form.groupAdminId = g.groupAdmin?.id ?? null
  form.groupAdmin = g.groupAdmin ? JSON.parse(JSON.stringify(g.groupAdmin)) : {
    name: '',
    eyeColor: '',
    hairColor: '',
    weight: null,
    nationality: '',
    location: {x: null, y: null, name: ''}
  }
  if (!form.groupAdmin.location) form.groupAdmin.location = {x: null, y: null, name: ''}
  adminOriginalSnapshot = form.groupAdmin ? JSON.stringify(form.groupAdmin) : null
  showModal.value = true
}

function closeModal() {
  showModal.value = false;
  adminOriginalSnapshot = null
}

async function submitModal() {
  try {
    const adminToSend = (form.groupAdmin && form.groupAdmin.name.trim() !== '') ? JSON.parse(JSON.stringify(form.groupAdmin)) : null
    const payload = {
      name: form.name,
      coordinates: form.coordinates,
      studentsCount: form.studentsCount,
      expelledStudents: form.expelledStudents,
      transferredStudents: form.transferredStudents,
      formOfEducation: form.formOfEducation,
      shouldBeExpelled: form.shouldBeExpelled,
      averageMark: form.averageMark,
      semesterEnum: form.semesterEnum || null,
      groupAdmin: adminToSend
    }
    if (modalMode.value === 'create') {
      await api.createGroup(payload)
      showToast('Группа создана')
    } else {
      await api.updateGroup(form.id, payload)
      showToast('Группа обновлена')
    }
    showModal.value = false
    fetchGroups()
  } catch (e) {
    console.error(e)
    showToast(e.response?.data?.message || 'Ошибка сохранения')
  } finally {
    adminOriginalSnapshot = null
  }
}

// --- lifecycle ---
onMounted(() => {
  fetchGroups();
  fetchPersonsList()
})
</script>

<style scoped> .app {
  font-family: Inter, Arial, sans-serif;
  padding: 16px
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px
}

.controls {
  display: flex;
  gap: 12px;
  align-items: center
}

.filter {
  display: flex;
  gap: 8px;
  align-items: center
}

main {
  display: flex;
  gap: 16px
}

.table-section {
  flex: 1
}

.side {
  width: 360px;
  background: #fafafa;
  padding: 12px;
  border-radius: 8px
}

.groups-table {
  width: 100%;
  border-collapse: collapse
}

.groups-table th, .groups-table td {
  border: 1px solid #ddd;
  padding: 8px
}

.groups-table th {
  cursor: pointer
}

.actions button {
  margin-right: 6px
}

.pagination {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  align-items: center
}

.modal-backdrop {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center
}

.modal {
  background: white;
  padding: 16px;
  border-radius: 8px;
  width: 640px;
  max-height: 90vh;
  overflow: auto
}

.modal.small {
  width: 320px
}

.form-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px
}

.form-row label {
  width: 160px
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px
}

.toast {
  position: fixed;
  right: 16px;
  bottom: 16px;
  background: #333;
  color: white;
  padding: 8px 12px;
  border-radius: 6px
} </style>
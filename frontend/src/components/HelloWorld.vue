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

        <!-- Delete confirmation modal -->
        <div v-if="showDeleteConfirm" class="modal-backdrop">
          <div class="modal small">
            <h3>Подтвердите удаление</h3>
            <p>Вы уверены, что хотите удалить эту группу?</p>
            <div class="form-actions">
              <button type="button" @click="showDeleteConfirm = false">Отмена</button>
              <button type="button" @click="performDelete">Да, удалить</button>
            </div>
          </div>
        </div>


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
          <!-- Admin block -->
          <div class="admin-nested">
            <h4>Admin</h4>
            <div class="form-row">
              <label>Name</label>
              <input v-model="form.groupAdmin.name" :disabled="modalMode==='view'" required/>
            </div>

            <div class="form-row">
              <label>Eye Color</label>
              <select v-model="form.groupAdmin.eyeColor" :disabled="modalMode==='view'" required>
                <option disabled value="">Выберите</option>
                <option value="RED">RED</option>
                <option value="GREEN">GREEN</option>
                <option value="BLUE">BLUE</option>
              </select>
            </div>

            <div class="form-row">
              <label>Hair Color</label>
              <select v-model="form.groupAdmin.hairColor" :disabled="modalMode==='view'" required>
                <option disabled value="">Выберите</option>
                <option value="RED">RED</option>
                <option value="GREEN">GREEN</option>
                <option value="BLUE">BLUE</option>
              </select>
            </div>

            <div class="form-row">
              <label>Weight</label>
              <input type="number" v-model.number="form.groupAdmin.weight" :disabled="modalMode==='view'" min="0"
                     required/>
            </div>
            <div class="form-row">
              <label>Nationality</label>
              <input v-model="form.groupAdmin.nationality" :disabled="modalMode==='view'" required/>
            </div>

            <h5>Location</h5>
            <div class="form-row">
              <label>X</label>
              <input type="number" v-model.number="form.groupAdmin.location.x" :disabled="modalMode==='view'" required/>
            </div>
            <div class="form-row">
              <label>Y</label>
              <input type="number" v-model.number="form.groupAdmin.location.y" :disabled="modalMode==='view'" required/>
            </div>
            <div class="form-row">
              <label>Name</label>
              <input v-model="form.groupAdmin.location.name" :disabled="modalMode==='view'" required/>
            </div>
          </div>


          <div class="form-actions">
            <button type="button" @click="closeModal">Отмена</button>
            <button type="submit" v-if="modalMode!=='view'">Сохранить</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Errors modal -->
    <div v-if="showErrorsModal" class="modal-backdrop">
      <div class="modal small">
        <h3>Ошибки при сохранении</h3>
        <ul>
          <li v-for="(err, i) in serverErrors" :key="i">{{ err }}</li>
        </ul>
        <div class="form-actions">
          <button type="button" @click="showErrorsModal = false">Закрыть</button>
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
const showDeleteConfirm = ref(false)
const deleteId = ref(null)   // <-- вот здесь

const showModal = ref(false)
const modalMode = ref('view')

const showErrorsModal = ref(false)
const serverErrors = ref([]) // массив строк


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

function confirmDelete(id) {
  deleteId.value = id
  showDeleteConfirm.value = true
}

async function performDelete() {
  try {
    const response = await api.deleteGroup(deleteId.value)
    // Если сервер возвращает JSON с сообщением
    const msg = response?.data?.message || 'Объект успешно удалён'
    showToast(msg)
    showDeleteConfirm.value = false
    fetchGroups()
  } catch (e) {
    // Ошибка: сервер вернул 400/500 с пояснением
    const errMsg = e.response?.data?.message
        || e.response?.data?.error
        || 'Объект невозможно удалить, так как он используется'
    showToast(errMsg)
    showDeleteConfirm.value = false
  }
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

async function viewGroup(id) {
  try {
    Object.assign(form, await api.fetchGroupById(id))
    modalMode.value = 'view'
    showModal.value = true
  } catch (e) {
    showToast('Не удалось получить группу')
  }
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
    serverErrors.value = []
  } catch (e) {
    console.error(e)
    const errors = e.response?.data
    if (errors && typeof errors === 'object') {
      serverErrors.value = Object.entries(errors)
          .map(([field, msg]) => `${field}: ${msg}`)
      showErrorsModal.value = true
    } else {
      serverErrors.value = ['Ошибка сохранения']
      showErrorsModal.value = true
    }
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

<style scoped>
.app {
  font-family: 'Inter', Arial, sans-serif;
  padding: 16px;
  background: linear-gradient(120deg, #ffe4e1, #f0f8ff);
  min-height: 100vh;
  color: #333;
}

header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

button {
  cursor: pointer;
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  font-weight: 500;
  background: linear-gradient(45deg, #ff758c, #ff7eb3);
  color: #fff;
  transition: transform 0.15s, box-shadow 0.2s;
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.filter input, .form-row input, .form-row select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  transition: all 0.2s;
}

.filter input:focus, .form-row input:focus, .form-row select:focus {
  border-color: #ff7eb3;
  box-shadow: 0 0 6px rgba(255, 126, 179, 0.5);
  outline: none;
}

.groups-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.groups-table th, .groups-table td {
  padding: 10px 12px;
  text-align: left;
}

.groups-table th {
  background: linear-gradient(90deg, #ff758c, #ff7eb3);
  color: #fff;
  cursor: pointer;
}

.groups-table tr:nth-child(even) {
  background: #f9f9f9;
}

.pagination {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

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

.toast {
  position: fixed;
  right: 16px;
  bottom: 16px;
  background: linear-gradient(135deg, #ff758c, #ff7eb3);
  color: #fff;
  padding: 10px 16px;
  border-radius: 10px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
  animation: slideUp 0.3s ease-out;
}

select {
  appearance: none;
  background: linear-gradient(120deg, #ffd1dc, #ffb6c1);
  border: 1px solid #ccc;
  padding: 6px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

select:focus {
  border-color: #ff7eb3;
  box-shadow: 0 0 6px rgba(255, 126, 179, 0.5);
  outline: none;
}

/* Animations */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
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

.modal.small {
  width: 360px;
  padding: 16px;
}

/* === Новое модальное окно для ошибок поверх всего === */
.modal.errors {
  width: 400px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 16px;
  background: linear-gradient(135deg, #fff1f3, #ffe4e1);
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(0,0,0,0.25);
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10000; /* выше всех других модалок */
}

.modal.errors h3 {
  margin-top: 0;
  color: #d32f2f;
}

.modal.errors ul {
  margin: 12px 0;
  padding-left: 20px;
}

.modal.errors li {
  margin-bottom: 6px;
  color: #d32f2f;
  font-weight: 500;
}


</style>
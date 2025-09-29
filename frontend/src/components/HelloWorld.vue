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
          <input v-model="filterValue" placeholder="Значение (полное совпадение)" />
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
          <div class="op">
            <label>Count groups with semester < less than</label>
            <select v-model="special.semester">
              <option disabled value="">Выберите</option>
              <option value="FIRST">FIRST</option>
              <option value="SECOND">SECOND</option>
              <option value="SEVENTH">SEVENTH</option>
            </select>
            <button @click="specialCountBySemester">Выполнить</button>
            <div v-if="special.countResult!==null">Результат: {{ special.countResult }}</div>
          </div>

          <div class="op">
            <label>Groups with admin id less than</label>
            <input type="number" v-model.number="special.adminId" />
            <button @click="specialGroupsWithAdminLess">Выполнить</button>
            <div v-if="special.adminGroups?.length">Найдено: {{ special.adminGroups.length }}</div>
          </div>

          <div class="op">
            <label>Distinct shouldBeExpelled</label>
            <button @click="specialDistinctShouldBeExpelled">Выполнить</button>
            <ul>
              <li v-for="val in special.distinctShouldBeExpelled" :key="val">{{ val }}</li>
            </ul>
          </div>

          <div class="op">
            <label>Add student to group (id)</label>
            <input type="number" v-model.number="special.addStudentGroupId" />
            <button @click="specialAddStudent">Добавить</button>
          </div>

          <div class="op">
            <label>Change form of education</label>
            <input type="number" v-model.number="special.changeFormGroupId" placeholder="group id" />
            <select v-model="special.newForm">
              <option disabled value="">Выберите</option>
              <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
              <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
              <option value="EVENING_CLASSES">EVENING_CLASSES</option>
            </select>
            <button @click="specialChangeForm">Изменить</button>
          </div>
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
            <input v-model="form.name" :disabled="modalMode==='view'" required />
          </div>

          <div class="form-row">
            <label>Students Count</label>
            <input type="number" v-model.number="form.studentsCount" :disabled="modalMode==='view'" min="1" required />
          </div>

          <div class="form-row">
            <label>Expelled Students</label>
            <input type="number" v-model.number="form.expelledStudents" :disabled="modalMode==='view'" min="0" required />
          </div>

          <div class="form-row">
            <label>Average Mark</label>
            <input type="number" v-model.number="form.averageMark" :disabled="modalMode==='view'" min="1" required />
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

          <div class="form-row">
            <label>Admin (select existing)</label>
            <select v-model.number="form.groupAdminId" :disabled="modalMode==='view'" required>
              <option disabled value="">Выберите администратора</option>
              <option v-for="p in persons" :key="p.id" :value="p.id">{{ p.name }} (id:{{p.id}})</option>
            </select>
            <button type="button" @click="openCreatePerson">Создать нового админа</button>
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

    <!-- Simple delete confirm -->
    <div v-if="showDeleteConfirm" class="modal-backdrop">
      <div class="modal small">
        <p>Удалить группу с id {{ deleteId }}?</p>
        <div class="form-actions">
          <button @click="showDeleteConfirm=false">Отмена</button>
          <button @click="performDelete">Удалить</button>
        </div>
      </div>
    </div>

    <!-- Simple toast area -->
    <div class="toast" v-if="toast.message">{{ toast.message }}</div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'
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
const modalMode = ref('view') // create | edit | view
const form = reactive({})
const persons = ref([]) // for admin select

const showDeleteConfirm = ref(false)
const deleteId = ref(null)
const toast = reactive({ message: '', timeout: null })

// special ops
const special = reactive({
  semester: '',
  countResult: null,
  adminId: null,
  adminGroups: null,
  distinctShouldBeExpelled: [],
  addStudentGroupId: null,
  changeFormGroupId: null,
  newForm: ''
})

let stompClient = null

// --- utility ---
function showToast(msg, ms = 4000) {
  clearTimeout(toast.timeout)
  toast.message = msg
  toast.timeout = setTimeout(() => (toast.message = ''), ms)
}

// --- API ---
async function fetchGroups() {
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      sort: (sortField.value ? `${sortField.value},${sortDir.value}` : undefined),
    }
    if (filterField.value && filterValue.value) {
      params.filterField = filterField.value
      params.filterValue = filterValue.value
    }
    const resp = await axios.get('/api/groups', { params })
    groups.value = resp.data.content || resp.data // support pageable or plain
    // page metadata fallback
    if (resp.data.totalPages != null) {
      totalPages.value = resp.data.totalPages
    } else {
      totalPages.value = Math.max(1, Math.ceil((resp.data.totalElements || groups.value.length) / pageSize.value))
    }
  } catch (e) {
    console.error(e)
    showToast('Ошибка при загрузке групп')
  }
}

async function fetchPersons() {
  try {
    const resp = await axios.get('/api/persons')
    persons.value = resp.data
  } catch (e) {
    console.warn('persons load failed', e)
  }
}

async function viewGroup(id) {
  try {
    const resp = await axios.get(`/api/groups/${id}`)
    Object.assign(form, resp.data)
    modalMode.value = 'view'
    showModal.value = true
  } catch (e) {
    showToast('Не удалось получить группу')
  }
}

function openCreate() {
  modalMode.value = 'create'
  Object.assign(form, {
    name: '',
    studentsCount: 1,
    expelledStudents: 0,
    transferredStudents: 1,
    formOfEducation: '',
    shouldBeExpelled: null,
    averageMark: 1,
    semesterEnum: '',
    groupAdminId: null
  })
  showModal.value = true
}

function openEdit(g) {
  modalMode.value = 'edit'
  // copy fields
  Object.assign(form, JSON.parse(JSON.stringify(g)))
  // ensure admin id exists
  form.groupAdminId = g.groupAdmin?.id
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function submitModal() {
  try {
    if (modalMode.value === 'create') {
      // prepare payload
      const payload = {
        name: form.name,
        studentsCount: form.studentsCount,
        expelledStudents: form.expelledStudents,
        transferredStudents: form.transferredStudents,
        formOfEducation: form.formOfEducation,
        shouldBeExpelled: form.shouldBeExpelled,
        averageMark: form.averageMark,
        semesterEnum: form.semesterEnum || null,
        groupAdminId: form.groupAdminId
      }
      await axios.post('/api/groups', payload)
      showToast('Группа создана')
    } else if (modalMode.value === 'edit') {
      await axios.put(`/api/groups/${form.id}`, form)
      showToast('Группа обновлена')
    }
    showModal.value = false
    await fetchGroups()
  } catch (err) {
    console.error(err)
    const msg = err.response?.data?.message || 'Ошибка сохранения'
    showToast(msg)
  }
}

function confirmDelete(id) {
  deleteId.value = id
  showDeleteConfirm.value = true
}

async function performDelete() {
  try {
    await axios.delete(`/api/groups/${deleteId.value}`)
    showToast('Удалено')
    showDeleteConfirm.value = false
    await fetchGroups()
  } catch (err) {
    showToast(err.response?.data?.message || 'Ошибка удаления')
    showDeleteConfirm.value = false
  }
}

// --- pagination / sorting / filtering ---
function goToPage(p) {
  page.value = p
  fetchGroups()
}

function changeSort(field) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = 'asc'
  }
  fetchGroups()
}

function applyFilter() {
  page.value = 0
  fetchGroups()
}
function clearFilter() {
  filterField.value = ''
  filterValue.value = ''
  fetchGroups()
}

// --- special operations ---
async function specialCountBySemester() {
  try {
    const resp = await axios.get('/api/special/count-by-semester', { params: { semester: special.semester } })
    special.countResult = resp.data
  } catch (e) {
    showToast('Ошибка специальной операции')
  }
}

async function specialGroupsWithAdminLess() {
  try {
    const resp = await axios.get('/api/special/groups-with-admin-less-than', { params: { adminId: special.adminId } })
    special.adminGroups = resp.data
  } catch (e) {
    showToast('Ошибка специальной операции')
  }
}

async function specialDistinctShouldBeExpelled() {
  try {
    const resp = await axios.get('/api/special/distinct-should-be-expelled')
    special.distinctShouldBeExpelled = resp.data
  } catch (e) {
    showToast('Ошибка специальной операции')
  }
}

async function specialAddStudent() {
  try {
    await axios.post(`/api/groups/${special.addStudentGroupId}/add-student`)
    showToast('Студент добавлен')
    fetchGroups()
  } catch (e) {
    showToast('Ошибка')
  }
}

async function specialChangeForm() {
  try {
    await axios.post(`/api/groups/${special.changeFormGroupId}/change-form`, { form: special.newForm })
    showToast('Форма обучения изменена')
    fetchGroups()
  } catch (e) {
    showToast('Ошибка')
  }
}

// --- WebSocket (STOMP) ---
function connectWs() {
  try {
    const socket = new SockJS('/ws') // depends on server mapping
    stompClient = Stomp.over(socket)
    stompClient.connect({}, () => {
      stompClient.subscribe('/topic/groups', (message) => {
        // message body expected to contain type and payload
        try {
          const body = JSON.parse(message.body)
          // simple strategy: reload current page
          fetchGroups()
        } catch (e) {
          fetchGroups()
        }
      })
    })
  } catch (e) {
    console.warn('WS connect failed', e)
  }
}

function disconnectWs() {
  if (stompClient) {
    stompClient.disconnect(() => {})
  }
}

onMounted(() => {
  fetchGroups()
  fetchPersons()
  connectWs()
})
onBeforeUnmount(() => disconnectWs())
</script>

<style scoped>
.app { font-family: Inter, Arial, sans-serif; padding: 16px; }
header { display:flex; align-items:center; justify-content:space-between; margin-bottom:12px }
.controls { display:flex; gap:12px; align-items:center }
.filter { display:flex; gap:8px; align-items:center }
main { display:flex; gap:16px }
.table-section { flex:1 }
.side { width:360px; background:#fafafa; padding:12px; border-radius:8px }
.groups-table { width:100%; border-collapse:collapse }
.groups-table th, .groups-table td { border:1px solid #ddd; padding:8px }
.groups-table th { cursor:pointer }
.actions button { margin-right:6px }
.pagination { margin-top:8px; display:flex; gap:8px; align-items:center }
.modal-backdrop { position:fixed; left:0;top:0;right:0;bottom:0; background:rgba(0,0,0,0.4); display:flex; align-items:center; justify-content:center }
.modal { background:white; padding:16px; border-radius:8px; width:640px; max-height:90vh; overflow:auto }
.modal.small { width:320px }
.form-row { display:flex; gap:8px; align-items:center; margin-bottom:8px }
.form-row label { width:160px }
.form-actions { display:flex; justify-content:flex-end; gap:8px; margin-top:12px }
.toast { position:fixed; right:16px; bottom:16px; background:#333; color:white; padding:8px 12px; border-radius:6px }
</style>
<template>
  <div class="app">
    <header>
      <h1>StudyGroups — Управление группами</h1>
      <div class="controls">
        <button @click="openCreate">Создать группу</button>
        <div class="filter">
          <select v-model="filterField">
            <option value="">Фильтровать по полю</option>
            <option value="name">Название группы</option>
            <option value="formOfEducation">Форма обучения</option>
            <option value="semesterEnum">Семестр</option>
            <option value="groupAdminName">Админ</option>
          </select>
          <input v-model="filterValue" placeholder="Значение (полное совпадение)"/>
          <button class="filter-reset" @click="clearFilter">Сброс</button>
        </div>
      </div>
    </header>

    <div class="layout">
      <aside class="side-panel">
        <h3>Специальные операции</h3>

        <div class="operation-selector">
          <label>Выберите операцию:</label>
          <select v-model="special.selectedOp">
            <option disabled value="">-- Выберите --</option>
            <option value="countBySemester">Count groups by semester</option>
            <option value="groupsWithAdminLess">Groups with admin id &lt;</option>
            <option value="distinctShouldBeExpelled">Distinct shouldBeExpelled</option>
            <option value="addStudent">Добавить студента в группу</option>
            <option value="changeForm">Change form of education</option>
          </select>
        </div>

        <div class="op-fields">
          <div v-if="special.selectedOp === 'countBySemester'" class="op">
            <label>Semester &lt;</label>
            <select v-model="special.semester">
              <option disabled value="">Выберите</option>
              <option value="FIRST">FIRST</option>
              <option value="SECOND">SECOND</option>
              <option value="SEVENTH">SEVENTH</option>
            </select>
            <button @click="specialCountBySemester">Выполнить</button>
            <div v-if="special.countResult !== null" class="op-result">
              Результат: {{ special.countResult }}
            </div>
          </div>

          <div v-if="special.selectedOp === 'groupsWithAdminLess'" class="op">
            <label>Admin id &lt;</label>
            <input type="number" v-model.number="special.adminId" placeholder="admin id"/>
            <button @click="specialGroupsWithAdminLess">Выполнить</button>
            <div v-if="special.adminGroups?.length" class="op-result">
              Найдено: {{ special.adminGroups.length }}
              <button @click="special.showAdminList = !special.showAdminList" style="margin-left:8px">
                {{ special.showAdminList ? 'Скрыть' : 'Показать' }}
              </button>
              <ul v-if="special.showAdminList">
                <li v-for="g in special.adminGroups" :key="g.id">
                  id: {{ g.id }} — {{ g.name }} — admin: {{ g.groupAdmin?.name ?? '(нет)' }}
                </li>
              </ul>
            </div>
          </div>

          <div v-if="special.selectedOp === 'distinctShouldBeExpelled'" class="op">
            <button @click="specialDistinctShouldBeExpelled">Выполнить</button>
            <div v-if="special.distinctShouldBeExpelled?.length" class="op-result">
              <ul>
                <li v-for="v in special.distinctShouldBeExpelled" :key="String(v)">{{ v }}</li>
              </ul>
            </div>
          </div>

          <div v-if="special.selectedOp === 'addStudent'" class="op">
            <label>ID группы</label>
            <input type="number" v-model.number="special.addStudentGroupId" placeholder="Введите ID группы" min="1"/>
            <button @click="specialAddStudent">Добавить</button>
            <div v-if="special.addStudentResult !== null" class="op-result">
              <strong>Результат:</strong> {{ special.addStudentResult }}
            </div>
          </div>

          <div v-if="special.selectedOp === 'changeForm'" class="op">
            <label>Group ID</label>
            <input type="number" v-model.number="special.changeFormGroupId" placeholder="group id"/>
            <label>New form</label>
            <select v-model="special.newForm">
              <option disabled value="">Выберите</option>
              <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
              <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
              <option value="EVENING_CLASSES">EVENING_CLASSES</option>
            </select>
            <button @click="specialChangeForm">Изменить</button>
            <div v-if="special.changeFormResult !== null" class="op-result">
              {{ special.changeFormResult }}
            </div>
          </div>
        </div>
      </aside>

    <main>
      <section class="table-section">
        <table class="groups-table">
          <thead>
          <tr>
            <th @click="changeSort('id')">ID <span v-if="sortField==='id'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span>
            </th>
            <th @click="changeSort('name')">Name <span v-if="sortField==='name'">{{
                sortDir === 'asc' ? '↑' : '↓'
              }}</span></th>
            <th @click="changeSort('studentsCount')">Students <span
                v-if="sortField==='studentsCount'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span></th>
            <th @click="changeSort('expelledStudents')">Expelled <span
                v-if="sortField==='expelledStudents'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span></th>
            <th @click="changeSort('averageMark')">Average Mark <span
                v-if="sortField==='averageMark'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span></th>
            <th @click="changeSort('formOfEducation')">Form <span
                v-if="sortField==='formOfEducation'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span></th>
            <th @click="changeSort('semesterEnum')">Semester <span
                v-if="sortField==='semesterEnum'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span></th>
            <th @click="changeSort('groupAdmin.name')">Admin <span
                v-if="sortField==='groupAdmin.name'">{{ sortDir === 'asc' ? '↑' : '↓' }}</span></th>
            <th>Actions</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="g in visibleGroups" :key="g.id">
            <td>{{ g.id }}</td>
            <td>
              <div v-if="isEditing(g.id, 'name')">
                <input v-model="editing.value" @blur="finishEdit(g, 'name')"
                       @keydown.enter.prevent="finishEdit(g, 'name')"/>
              </div>
              <div v-else @click="startEdit(g, 'name')">{{ g.name }}</div>
            </td>

            <td>
              <div v-if="isEditing(g.id, 'studentsCount')">
                <input type="number" v-model.number="editing.value" @blur="finishEdit(g, 'studentsCount')"
                       @keydown.enter.prevent="finishEdit(g, 'studentsCount')"/>
              </div>
              <div v-else @click="startEdit(g, 'studentsCount')">{{ g.studentsCount }}</div>
            </td>

            <td @click="startEdit(g, 'expelledStudents')">
              <div v-if="isEditing(g.id, 'expelledStudents')">
                <input type="number" v-model.number="editing.value" @blur="finishEdit(g, 'expelledStudents')"
                       @keydown.enter.prevent="finishEdit(g, 'expelledStudents')"/>
              </div>
              <div v-else>{{ g.expelledStudents }}</div>
            </td>

            <td @click="startEdit(g, 'averageMark')">
              <div v-if="isEditing(g.id, 'averageMark')">
                <input type="number" v-model.number="editing.value" @blur="finishEdit(g, 'averageMark')"
                       @keydown.enter.prevent="finishEdit(g, 'averageMark')"/>
              </div>
              <div v-else>{{ g.averageMark }}</div>
            </td>

            <td>
              <div v-if="isEditing(g.id, 'formOfEducation')">
                <select v-model="editing.value" @change="finishEdit(g, 'formOfEducation')"
                        @blur="finishEdit(g, 'formOfEducation')">
                  <option value="">--</option>
                  <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
                  <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
                  <option value="EVENING_CLASSES">EVENING_CLASSES</option>
                </select>
              </div>
              <div v-else @click="startEdit(g, 'formOfEducation')">{{ g.formOfEducation }}</div>
            </td>
            <td @click="startEdit(g, 'semesterEnum')">
              <div v-if="isEditing(g.id, 'semesterEnum')">
                <select v-model="editing.value" @change="finishEdit(g, 'semesterEnum')"
                        @blur="finishEdit(g, 'semesterEnum')">
                  <option value="">(null)</option>
                  <option value="FIRST">FIRST</option>
                  <option value="SECOND">SECOND</option>
                  <option value="SEVENTH">SEVENTH</option>
                </select>
              </div>
              <div v-else>{{ g.semesterEnum }}</div>
            </td>

            <td>
              <div v-if="isEditing(g.id, 'groupAdmin.name')">
                <input v-model="editing.value" @blur="finishEdit(g, 'groupAdmin.name')"
                       @keydown.enter.prevent="finishEdit(g, 'groupAdmin.name')"/>
              </div>
              <div v-else @click="startEdit(g, 'groupAdmin.name')">{{ g.groupAdmin?.name }}</div>
            </td>

            <td class="actions">
              <button @click="viewGroup(g.id)">View</button>
              <button @click="openEdit(g)">Edit</button>
              <button @click="confirmDelete(g.id)">Delete</button>
            </td>
          </tr>
          </tbody>
        </table>

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
    </main>

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

          <div class="form-row">
            <label>Admin (choose existing)</label>
            <select v-model="form.groupAdminId" @change="onAdminChange" :disabled="modalMode==='view'">
              <option value="">-- (создать нового / оставить пустым) --</option>
              <option v-for="p in persons" :key="p.id" :value="p.id">{{ p.name }} (id:{{ p.id }})</option>
            </select>
            <button type="button" v-if="modalMode!=='view'" @click="resetGroupAdminToEmpty">Очистить / новый</button>
          </div>

          <div class="admin-nested">
            <h4>Admin</h4>
            <div class="form-row">
              <label>Name</label>
              <input v-model="form.groupAdmin.name" :disabled="modalMode==='view'" required
                     @input="onAdminFieldChange"/>
            </div>

            <div class="form-row">
              <label>Eye Color</label>
              <select v-model="form.groupAdmin.eyeColor" :disabled="modalMode==='view'" required
                      @change="onAdminFieldChange">
                <option disabled value="">Выберите</option>
                <option value="RED">RED</option>
                <option value="GREEN">GREEN</option>
                <option value="BLUE">BLUE</option>
              </select>
            </div>

            <div class="form-row">
              <label>Hair Color</label>
              <select v-model="form.groupAdmin.hairColor" :disabled="modalMode==='view'" required
                      @change="onAdminFieldChange">
                <option disabled value="">Выберите</option>
                <option value="RED">RED</option>
                <option value="GREEN">GREEN</option>
                <option value="BLUE">BLUE</option>
              </select>
            </div>

            <div class="form-row">
              <label>Weight</label>
              <input type="number" v-model.number="form.groupAdmin.weight" :disabled="modalMode==='view'" min="0"
                     required @input="onAdminFieldChange"/>
            </div>

            <div class="form-row">
              <label>Nationality</label>
              <select v-model="form.groupAdmin.nationality" :disabled="modalMode==='view'" required
                      @change="onAdminFieldChange">
                <option disabled value="">Выберите</option>
                <option value="INDIA">INDIA</option>
                <option value="UNITED_KINGDOM">UNITED_KINGDOM</option>
                <option value="JAPAN">JAPAN</option>
              </select>
            </div>

            <div class="form-row">
              <label>X</label>
              <input type="number" v-model.number="form.groupAdmin.location.x" :disabled="modalMode==='view'" required
                     @input="onAdminFieldChange"/>
            </div>

            <div class="form-row">
              <label>Y</label>
              <input type="number" v-model.number="form.groupAdmin.location.y" :disabled="modalMode==='view'" required
                     @input="onAdminFieldChange"/>
            </div>

            <div class="form-row">
              <label>Name</label>
              <input v-model="form.groupAdmin.location.name" :disabled="modalMode==='view'" required
                     @input="onAdminFieldChange"/>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal">Отмена</button>
            <button type="submit" v-if="modalMode!=='view'">Сохранить</button>
          </div>
        </form>
      </div>
    </div>

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

    <div class="toast" v-if="toast.message">{{ toast.message }}</div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted, onBeforeUnmount} from 'vue'
import * as api from '../utilities/api.js'
import Stomp from 'stompjs'
import SockJS from 'sockjs-client'

const groups = ref([])
const page = ref(0)
const pageSize = ref(10)
const sortField = ref('id')
const sortDir = ref('asc')
const filterField = ref('')
const filterValue = ref('')
const showDeleteConfirm = ref(false)
const deleteId = ref(null)

const showModal = ref(false)
const modalMode = ref('view')

const showErrorsModal = ref(false)
const serverErrors = ref([])

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

const persons = ref([])

const toast = reactive({message: '', timeout: null})
let adminOriginalSnapshot = null

function showToast(msg, ms = 4000) {
  clearTimeout(toast.timeout)
  toast.message = msg
  toast.timeout = setTimeout(() => (toast.message = ''), ms)
}

function confirmDelete(id) {
  deleteId.value = id
  showDeleteConfirm.value = true
}

function changeSort(field) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = 'asc'
  }
  page.value = 0
}

function getNestedValue(obj, path) {
  if (!path) return undefined
  return path.split('.').reduce((o, key) => (o == null ? undefined : o[key]), obj)
}

const editing = reactive({id: null, field: '', value: null})

function isEditing(id, field) {
  return editing.id === id && editing.field === field
}

function startEdit(obj, field) {
  editing.id = obj.id
  editing.field = field
  const v = getNestedValue(obj, field)
  editing.value = v == null ? '' : v
}

function setNestedValue(obj, path, value) {
  const parts = path.split('.')
  let cur = obj
  for (let i = 0; i < parts.length - 1; i++) {
    const p = parts[i]
    if (cur[p] == null) cur[p] = {}
    cur = cur[p]
  }
  cur[parts[parts.length - 1]] = value
}

async function finishEdit(originalGroup, field) {
  const newValue = editing.value
  editing.id = null
  editing.field = ''
  try {
    const payload = JSON.parse(JSON.stringify(originalGroup))
    setNestedValue(payload, field, (newValue === '' ? null : newValue))
    delete payload.id
    if (payload.semesterEnum === '') payload.semesterEnum = null
    if (payload.groupAdmin && Object.keys(payload.groupAdmin).length === 0) payload.groupAdmin = null
    await api.updateGroup(originalGroup.id, payload)
    showToast('Обновлено')
    fetchGroups()
  } catch (e) {
    console.error(e)
    showToast('Не удалось сохранить изменения')
    fetchGroups()
  }
}

function compareValues(a, b) {
  if (a == null && b == null) return 0
  if (a == null) return -1
  if (b == null) return 1

  const na = Number(a)
  const nb = Number(b)
  if (!Number.isNaN(na) && !Number.isNaN(nb)) return na - nb

  return String(a).localeCompare(String(b))
}

const filteredGroups = computed(() => {
  if (!filterField.value || !filterValue.value) return groups.value.slice()

  const path = filterField.value === 'groupAdminName' ? 'groupAdmin.name' : filterField.value

  return groups.value.filter(g => {
    const val = getNestedValue(g, path)
    if (val == null) return false
    return String(val).trim() === String(filterValue.value).trim()
  })
})

const sortedGroups = computed(() => {
  const arr = filteredGroups.value.slice()
  const field = sortField.value
  if (!field) return arr

  arr.sort((a, b) => {
    const va = getNestedValue(a, field)
    const vb = getNestedValue(b, field)
    const cmp = compareValues(
        typeof va === 'string' ? va.toLowerCase() : va,
        typeof vb === 'string' ? vb.toLowerCase() : vb
    )
    return sortDir.value === 'asc' ? cmp : -cmp
  })
  return arr
})

const totalPages = computed(() => Math.max(1, Math.ceil(sortedGroups.value.length / pageSize.value)))

const visibleGroups = computed(() => {
  const start = page.value * pageSize.value
  return sortedGroups.value.slice(start, start + pageSize.value)
})

function applyFilter() {
  page.value = 0

}

function clearFilter() {
  filterField.value = ''
  filterValue.value = ''
  page.value = 0
}

function sortGroups() {
  groups.value.sort((a, b) => {
    let valA = getNestedValue(a, sortField.value)
    let valB = getNestedValue(b, sortField.value)

    if (valA == null) valA = ''
    if (valB == null) valB = ''

    if (typeof valA === 'string') valA = valA.toLowerCase()
    if (typeof valB === 'string') valB = valB.toLowerCase()

    if (valA < valB) return sortDir.value === 'asc' ? -1 : 1
    if (valA > valB) return sortDir.value === 'asc' ? 1 : -1
    return 0
  })
}

async function performDelete() {
  try {
    const response = await api.deleteGroup(deleteId.value)
    const msg = response?.data?.message || 'Объект успешно удалён'
    showToast(msg)
    showDeleteConfirm.value = false
    fetchGroups()
  } catch (e) {

    const errMsg = e.response?.data?.message
        || e.response?.data?.error
        || 'Объект невозможно удалить, так как он используется'
    showToast(errMsg)
    showDeleteConfirm.value = false
  }
}

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
  if (!adminOriginalSnapshot) return;
  const current = JSON.stringify(form.groupAdmin);
  if (current !== adminOriginalSnapshot) {
    delete form.groupAdmin.id;
    form.groupAdminId = null;
    adminOriginalSnapshot = null;
    showToast('Изменения админа будут сохранены как новый человек');
  }
}


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

const special = reactive({
  semester: '',
  countResult: null,
  adminId: null,
  adminGroups: null,
  showAdminList: false,
  distinctShouldBeExpelled: [],
  addStudentGroupId: null,
  addStudentResult: null,
  changeFormGroupId: null,
  newForm: '',
  changeFormResult: null,
  selectedOp: ''
})

async function specialCountBySemester() {
  try {
    special.countResult = null
    if (!special.semester) throw new Error('Выберите семестр')
    const res = await api.specialCountBySemester(special.semester)
    special.countResult = res
  } catch (e) {
    console.error(e)
    showToast(e?.response?.data?.message || e.message || 'Ошибка специальной операции')
  }
}

async function specialGroupsWithAdminLess() {
  try {
    special.adminGroups = null
    special.showAdminList = false
    if (special.adminId == null) throw new Error('Укажите adminId')
    const res = await api.specialGroupsWithAdminLess(special.adminId)
    special.adminGroups = res || []
    if (!special.adminGroups.length) showToast('Ничего не найдено')
  } catch (e) {
    console.error(e)
    showToast(e?.response?.data?.message || e.message || 'Ошибка специальной операции')
  }
}

async function specialDistinctShouldBeExpelled() {
  try {
    special.distinctShouldBeExpelled = []
    const res = await api.specialDistinctShouldBeExpelled()
    special.distinctShouldBeExpelled = Array.isArray(res) ? res : (res?.data ?? [])
  } catch (e) {
    console.error(e)
    showToast(e?.response?.data?.message || e.message || 'Ошибка специальной операции')
  }
}

async function specialAddStudent() {
  try {
    special.addStudentResult = null
    if (!special.addStudentGroupId) throw new Error('Укажите id группы')
    const res = await api.addStudent(special.addStudentGroupId);
    special.addStudentResult = typeof res === 'string' ? res : (res?.message ?? 'OK')
    showToast('Студент добавлен')
    fetchGroups();
  } catch (e) {
    showToast(e?.response?.data || e.message);
  }
}


async function specialChangeForm() {
  try {
    special.changeFormResult = null
    if (!special.changeFormGroupId) throw new Error('Укажите id группы')
    if (!special.newForm) throw new Error('Укажите новую форму обучения')
    const res = await api.changeForm(special.changeFormGroupId, special.newForm)
    special.changeFormResult = typeof res === 'string' ? res : (res?.message ?? 'OK')
    showToast('Форма обучения изменена')
    fetchGroups()
  } catch (e) {
    console.error(e)
    showToast(e?.response?.data?.message || e.message || 'Ошибка при смене формы')
  }
}

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

.filter select {
  text-align: center;
  text-align-last: center;
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

.modal.errors {
  width: 400px;
  max-height: 70vh;
  overflow-y: auto;
  padding: 16px;
  background: linear-gradient(135deg, #fff1f3, #ffe4e1);
  border-radius: 12px;
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.25);
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10000;
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

.operation-selector {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.op-fields {
  background: #fff0f5;
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.op label {
  font-weight: 500;
  margin-right: 6px;
}

.op button {
  margin-left: 6px;
}

.op-result {
  margin-top: 6px;
  padding: 6px 12px;
  background: #ffe4e1;
  border-radius: 6px;
  font-weight: 500;
}
.layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 20px;
}

/* Левая панель */
.side-panel {
  width: 280px;
  background: #fafafa;
  border-radius: 16px;
  box-shadow: 0 4px 14px rgba(0,0,0,0.1);
  padding: 16px;
  flex-shrink: 0;
}

.side-panel h3 {
  text-align: center;
  margin-bottom: 16px;
  font-size: 1.1rem;
}

.operation-selector {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.op-fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.op {
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: #fff;
  padding: 10px;
  border-radius: 10px;
  box-shadow: inset 0 0 4px rgba(0,0,0,0.05);
}

.op label {
  font-weight: 500;
}

.op button {
  align-self: flex-start;
  background-color: #ff6b81;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
  transition: 0.2s;
}

.op button:hover {
  background-color: #ff4d6d;
}

.op-result {
  background: #ffe8ec;
  padding: 8px;
  border-radius: 8px;
  font-size: 0.9rem;
}
</style>
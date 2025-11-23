<template>
  <div class="app">
    <header>
      <h1>StudyGroups — Управление группами</h1>
      <div class="controls">
        <button @click="openCreate">Создать группу</button>
        <button @click="triggerFileInputClick" class="import-btn">Импорт (JSON)</button>
        <input
            type="file"
            ref="fileInputRef"
            @change="handleFileImport"
            accept=".json,application/json"
            style="display: none"
        />

        <button @click="showHistoryModal = true" class="history-btn">История импорта</button>

        <div class="filter">
          <select v-model="filterField">
            <option value="">Фильтровать по полю</option>
            <option value="name">Название группы</option>
            <option value="formOfEducation">Форма обучения</option>
            <option value="semesterEnum">Семестр</option>
            <option value="groupAdmin.name">Админ</option>
          </select>
          <input v-model="filterValue" placeholder="Значение (полное совпадение)" />
          <button class="filter-reset" @click="clearFilter">Сброс</button>
        </div>
      </div>
    </header>

    <div class="layout">
      <SidePanelSpecial
          :persons="persons"
          :special="special"
          @count-by-semester="onSpecialCountBySemester"
          @groups-with-admin-less="onSpecialGroupsWithAdminLess"
          @distinct-should-be-expelled="onSpecialDistinctShouldBeExpelled"
          @add-student="onSpecialAddStudent"
          @change-form="onSpecialChangeForm"
      />

      <main class="main-content">
        <GroupsTable
            :groups="visibleGroups"
            :sort-field="sortField"
            :sort-dir="sortDir"
            @change-sort="onChangeSort"
            @update-group="onInlineUpdate"
            @view-group="viewGroup"
            @open-edit="openEdit"
            @confirm-delete="confirmDelete"
        />

        <PaginationControls
            :page="page"
            :total-pages="totalPages"
            :page-size="pageSize"
            @change-page="goToPage"
            @change-page-size="onChangePageSize"
        />
      </main>
    </div>

    <GroupModal
        :show="showModal"
        :mode="modalMode"
        :form.sync="form"
        :persons="persons"
        @close="closeModal"
        @submit="submitModal"
    />

    <ConfirmModal
        v-if="showDeleteConfirm"
        title="Подтвердите удаление"
        message="Вы уверены, что хотите удалить эту группу?"
        @cancel="showDeleteConfirm = false"
        @confirm="performDelete"
    />

    <ImportHistoryModal
        :show="showHistoryModal"
        @close="showHistoryModal = false"
    />

    <Toast :message="toast.message" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import * as api from '../utilities/api.js'

import SidePanelSpecial from './SidePanelSpecial.vue'
import GroupsTable from './GroupsTable.vue'
import GroupModal from './GroupModal.vue'
import PaginationControls from './PaginationControls.vue'
import Toast from './Toast.vue'
import ConfirmModal from './ConfirmModal.vue'
import ImportHistoryModal from './ImportHistoryModal.vue'

const groups = ref([])
const persons = ref([])

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

const fileInputRef = ref(null)

const showHistoryModal = ref(false)

const toast = reactive({ message: '', timeout: null })
function showToast(msg, ms = 4000) {
  clearTimeout(toast.timeout)
  toast.message = msg
  toast.timeout = setTimeout(() => (toast.message = ''), ms)
}

async function handleFileImport(event) {
  const file = event.target.files[0]
  if (!file) return

  const reader = new FileReader()

  reader.onload = async (e) => {
    try {
      const fileContent = e.target.result
      const groupsToImport = JSON.parse(fileContent)

      if (!Array.isArray(groupsToImport)) {
        throw new Error('Файл должен содержать JSON-массив (список) групп.')
      }

      const imported = await api.importGroups(groupsToImport)

      showToast(`Успешно импортировано ${imported.length} групп.`)
      fetchGroups()

    } catch (error) {
      console.error('Ошибка импорта:', error)
      let errorMessage = 'Ошибка импорта. '

      if (error.response?.data) {
        const errors = error.response.data
        if (typeof errors === 'object' && errors !== null) {
          errorMessage += Object.values(errors).join(' ')
        } else {
          errorMessage += String(errors)
        }
      } else {
        errorMessage += error.message
      }

      showToast(errorMessage, 6000)
    } finally {
      if (event.target) {
        event.target.value = null
      }
    }
  }

  reader.onerror = () => {
    showToast('Не удалось прочитать файл.', 5000)
    if (event.target) {
      event.target.value = null
    }
  }

  reader.readAsText(file)
}

function triggerFileInputClick() {
  if (fileInputRef.value) {
    fileInputRef.value.click()
  }
}

const form = reactive({
  id: null,
  name: '',
  coordinates: { x: null, y: null },
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
    location: { x: null, y: null, name: '' }
  }
})

const special = reactive({
  semester: '',
  countResult: null,
  adminId: null,
  adminGroups: [],
  showAdminList: false,
  distinctShouldBeExpelled: [],
  addStudentGroupId: null,
  addStudentResult: null,
  changeFormGroupId: null,
  newForm: '',
  changeFormResult: null,
  selectedOp: ''
})

function getNestedValue(obj, path) {
  if (!path) return undefined
  return path.split('.').reduce((o, key) => (o == null ? undefined : o[key]), obj)
}

const filteredGroups = computed(() => {
  if (!filterField.value || !filterValue.value) return groups.value.slice()
  const path = filterField.value
  return groups.value.filter(g => {
    const val = getNestedValue(g, path)
    if (val == null) return false
    return String(val).trim() === String(filterValue.value).trim()
  })
})

function compareValues(a, b) {
  if (a == null && b == null) return 0
  if (a == null) return -1
  if (b == null) return 1
  const na = Number(a)
  const nb = Number(b)
  if (!Number.isNaN(na) && !Number.isNaN(nb)) return na - nb
  return String(a).localeCompare(String(b))
}

const sortedGroups = computed(() => {
  const arr = filteredGroups.value.slice()
  const field = sortField.value
  arr.sort((a, b) => {
    const va = getNestedValue(a, field)
    const vb = getNestedValue(b, field)
    const cmp = compareValues(typeof va === 'string' ? va.toLowerCase() : va, typeof vb === 'string' ? vb.toLowerCase() : vb)
    return sortDir.value === 'asc' ? cmp : -cmp
  })
  return arr
})

const totalPages = computed(() => Math.max(1, Math.ceil(sortedGroups.value.length / pageSize.value)))
const visibleGroups = computed(() => {
  const start = page.value * pageSize.value
  return sortedGroups.value.slice(start, start + pageSize.value)
})

async function fetchGroups() {
  try {
    const params = { page: page.value, size: pageSize.value }
    if (sortField.value) params.sort = `${sortField.value},${sortDir.value}`
    if (filterField.value && filterValue.value) {
      params.filterField = filterField.value
      params.filterValue = filterValue.value
    }
    const data = await api.fetchGroups(params)
    groups.value = data.content ?? data
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

onMounted(() => {
  fetchGroups()
  fetchPersonsList()
})

function onChangeSort(field) {
  if (sortField.value === field) {
    sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortDir.value = 'asc'
  }
  page.value = 0
}

async function onInlineUpdate({ id, field, value }) {
  try {
    const original = groups.value.find(g => g.id === id)
    if (!original) return
    const payload = JSON.parse(JSON.stringify(original))
    const parts = field.split('.')
    let cur = payload
    for (let i = 0; i < parts.length - 1; i++) {
      if (cur[parts[i]] == null) cur[parts[i]] = {}
      cur = cur[parts[i]]
    }
    cur[parts[parts.length - 1]] = value === '' ? null : value
    delete payload.id
    if (payload.semesterEnum === '') payload.semesterEnum = null
    if (payload.groupAdmin && Object.keys(payload.groupAdmin).length === 0) payload.groupAdmin = null
    await api.updateGroup(id, payload)
    showToast('Обновлено')
    fetchGroups()
  } catch (e) {
    console.error(e)
    showToast('Не удалось сохранить изменения')
    fetchGroups()
  }
}

function openCreate() {
  modalMode.value = 'create'
  Object.assign(form, {
    id: null,
    name: '',
    coordinates: { x: null, y: null },
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
      location: { x: null, y: null, name: '' }
    }
  })
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
    location: { x: null, y: null, name: '' }
  }
  showModal.value = true
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

function closeModal() {
  showModal.value = false
}

async function submitModal(payload) {
  try {
    if (modalMode.value === 'create') {
      await api.createGroup(payload)
      showToast('Группа создана')
    } else {
      await api.updateGroup(payload.id, payload)
      showToast('Группа обновлена')
    }
    showModal.value = false
    fetchGroups()
  } catch (e) {
    console.error(e)
    showToast('Ошибка сохранения')
    throw e
  }
}

function confirmDelete(id) {
  deleteId.value = id
  showDeleteConfirm.value = true
}

async function performDelete() {
  try {
    const response = await api.deleteGroup(deleteId.value)
    const msg = response?.data?.message || 'Объект успешно удалён'
    showToast(msg)
    showDeleteConfirm.value = false
    fetchGroups()
  } catch (e) {
    const errMsg = e.response?.data?.message || e.response?.data?.error || 'Объект невозможно удалить'
    showToast(errMsg)
    showDeleteConfirm.value = false
  }
}

function goToPage(n) {
  page.value = n
}
function onChangePageSize(size) {
  pageSize.value = size
  page.value = 0
  fetchGroups()
}

function clearFilter() {
  filterField.value = ''
  filterValue.value = ''
  page.value = 0
  fetchGroups()
}

async function onSpecialCountBySemester(semester) {
  try {
    special.countResult = null
    const res = await api.specialCountBySemester(semester)
    special.countResult = res
  } catch (e) {
    showToast('Ошибка специальной операции')
  }
}

async function onSpecialGroupsWithAdminLess(adminId) {
  try {
    special.adminGroups = []
    const res = await api.specialGroupsWithAdminLess(adminId)
    special.adminGroups = res || []
    if (!special.adminGroups.length) showToast('Ничего не найдено')
  } catch (e) {
    showToast('Ошибка специальной операции')
  }
}

async function onSpecialDistinctShouldBeExpelled() {
  try {
    const res = await api.specialDistinctShouldBeExpelled()
    special.distinctShouldBeExpelled = Array.isArray(res) ? res : (res?.data ?? [])
  } catch (e) {
    showToast('Ошибка специальной операции')
  }
}

async function onSpecialAddStudent(groupId) {
  try {
    const res = await api.addStudent(groupId)
    special.addStudentResult = typeof res === 'string' ? res : (res?.message ?? 'OK')
    showToast('Студент добавлен')
    fetchGroups()
  } catch (e) {
    showToast('Ошибка при добавлении студента, группы с таким id нет')
  }
}

async function onSpecialChangeForm({ groupId, newForm }) {
  try {
    const res = await api.changeForm(groupId, newForm)
    special.changeFormResult = typeof res === 'string' ? res : (res?.message ?? 'OK')
    showToast('Форма обучения изменена')
    fetchGroups()
  } catch (e) {
    showToast('Ошибка при смене формы')
  }
}
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
  flex-wrap: wrap; 
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

.import-btn {
  background: linear-gradient(45deg, #ff02a8, #ff02a8);
}

.history-btn {
  background: linear-gradient(45deg, #d3d1ff, #8784ff);
}

.filter input,
.form-row input,
.form-row select,
.op input,
.op select,
select {
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
  transition: all 0.2s;
  font-size: 14px;
}

.filter input:focus,
.form-row input:focus,
.form-row select:focus,
.op input:focus,
.op select:focus {
  border-color: #ff7eb3;
  box-shadow: 0 0 6px rgba(255, 126, 179, 0.5);
  outline: none;
}

.filter {
  display: flex;
  gap: 8px;
  align-items: center;
}

.filter select {
  text-align: center;
  text-align-last: center;
}

.layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  padding: 20px;
}


.main-content {
  flex: 1;
  min-width: 0; 
  display: flex;
  flex-direction: column;
}

.filter-reset {
  background: linear-gradient(45deg,#6b7280,#4b5563);
  color: white;
  padding: 6px 10px;
  border-radius: 6px;
}
</style>
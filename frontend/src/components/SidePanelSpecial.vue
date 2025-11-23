<template>
  <aside class="side-panel">
    <h3>Специальные операции</h3>

    <div class="operation-selector">
      <label>Выберите операцию:</label>
      <select v-model="local.selectedOp">
        <option disabled value="">-- Выберите --</option>
        <option value="countBySemester">Count groups by semester</option>
        <option value="groupsWithAdminLess">Groups with admin id &lt;</option>
        <option value="distinctShouldBeExpelled">Distinct shouldBeExpelled</option>
        <option value="addStudent">Добавить студента в группу</option>
        <option value="changeForm">Change form of education</option>
      </select>
    </div>

    <div class="op-fields">
      <div v-if="local.selectedOp === 'countBySemester'" class="op">
        <label>Semester &lt;</label>
        <select v-model="local.semester">
          <option disabled value="">Выберите</option>
          <option value="FIRST">FIRST</option>
          <option value="SECOND">SECOND</option>
          <option value="SEVENTH">SEVENTH</option>
        </select>
        <button @click="emitCount">Выполнить</button>
        <div v-if="local.countResult !== null" class="op-result">Результат: {{ local.countResult }}</div>
      </div>

      <div v-if="local.selectedOp === 'groupsWithAdminLess'" class="op">
        <label>Admin id &lt;</label>
        <input type="number" v-model.number="local.adminId" placeholder="admin id" />
        <button @click="emitGroupsWithAdminLess">Выполнить</button>
        <div v-if="local.adminGroups?.length" class="op-result">
          Найдено: {{ local.adminGroups.length }}
          <button @click="local.showAdminList = !local.showAdminList">
            {{ local.showAdminList ? 'Скрыть' : 'Показать' }}
          </button>
          <ul v-if="local.showAdminList">
            <li v-for="g in local.adminGroups" :key="g.id">
              id: {{ g.id }} — {{ g.name }} — admin: {{ g.groupAdmin?.name ?? '(нет)' }}
            </li>
          </ul>
        </div>
      </div>

      <div v-if="local.selectedOp === 'distinctShouldBeExpelled'" class="op">
        <button @click="emitDistinct">Выполнить</button>
        <div v-if="local.distinctShouldBeExpelled?.length" class="op-result">
          <ul>
            <li v-for="v in local.distinctShouldBeExpelled" :key="String(v)">{{ v }}</li>
          </ul>
        </div>
      </div>

      <div v-if="local.selectedOp === 'addStudent'" class="op">
        <label>ID группы</label>
        <input type="number" v-model.number="local.addStudentGroupId" min="1" />
        <button @click="emitAddStudent">Добавить</button>
        <div v-if="local.addStudentResult !== null" class="op-result">
          <strong>Результат:</strong> {{ local.addStudentResult }}
        </div>
      </div>

      <div v-if="local.selectedOp === 'changeForm'" class="op">
        <label>Group ID</label>
        <input type="number" v-model.number="local.changeFormGroupId" />
        <label>New form</label>
        <select v-model="local.newForm">
          <option disabled value="">Выберите</option>
          <option value="DISTANCE_EDUCATION">DISTANCE_EDUCATION</option>
          <option value="FULL_TIME_EDUCATION">FULL_TIME_EDUCATION</option>
          <option value="EVENING_CLASSES">EVENING_CLASSES</option>
        </select>
        <button @click="emitChangeForm">Изменить</button>
        <div v-if="local.changeFormResult !== null" class="op-result">{{ local.changeFormResult }}</div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  persons: { type: Array, default: () => [] },
  special: { type: Object, default: () => ({}) }
})

const emit = defineEmits([
  'count-by-semester',
  'groups-with-admin-less',
  'distinct-should-be-expelled',
  'add-student',
  'change-form'
])

const local = reactive({
  selectedOp: '',
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
  changeFormResult: null
})

watch(
    () => props.special,
    (s) => {
      if (!s) return
      local.countResult = s.countResult
      local.adminGroups = s.adminGroups ?? []
      local.distinctShouldBeExpelled = s.distinctShouldBeExpelled ?? []
      local.addStudentResult = s.addStudentResult
      local.changeFormResult = s.changeFormResult
    },
    { immediate: true, deep: true }
)

function emitCount() {
  emit('count-by-semester', local.semester)
}
function emitGroupsWithAdminLess() {
  emit('groups-with-admin-less', local.adminId)
}
function emitDistinct() {
  emit('distinct-should-be-expelled')
}
function emitAddStudent() {
  emit('add-student', local.addStudentGroupId)
}
function emitChangeForm() {
  emit('change-form', { groupId: local.changeFormGroupId, newForm: local.newForm })
}
</script>

<style scoped>
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
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-direction: column;
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
  margin-top: 6px;
  padding: 6px 12px;
  background: #ffe4e1;
  border-radius: 6px;
  font-weight: 500;
}
</style>

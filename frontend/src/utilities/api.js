import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:9090/api',
    headers: {
        'Content-Type': 'application/json'
    }
})

export const fetchGroups = (params) => api.get('/studygroups', { params }).then(res => res.data)
export const fetchGroupById = (id) => api.get(`/studygroups/${id}`).then(res => res.data)
export const createGroup = (payload) => api.post('/studygroups', payload).then(res => res.data)
export const updateGroup = (id, payload) => api.put(`/studygroups/${id}`, payload).then(res => res.data)
export const deleteGroup = (id) => api.delete(`/studygroups/${id}`).then(res => res.data)
export const addStudent = (id) => api.post(`/special/studygroups/${id}/add-student`).then(res => res.data)
export const changeForm = (id, form) => api.post(`/special/studygroups/${id}/change-form`, { form }).then(res => res.data)

export const fetchPersons = () => api.get('/persons').then(res => res.data)

export const specialCountBySemester = (semester) =>
    api.get('/special/count-by-semester', { params: { semester } }).then(res => res.data)

export const specialGroupsWithAdminLess = (adminId) =>
    api.get('/special/groups-with-admin-less-than', { params: { adminId } }).then(res => res.data)

export const specialDistinctShouldBeExpelled = () =>
    api.get('/special/distinct-should-be-expelled').then(res => res.data)

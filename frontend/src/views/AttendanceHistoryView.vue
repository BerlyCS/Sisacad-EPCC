<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <h1 class="text-3xl font-bold text-gray-900">Historial de Asistencia</h1>
        <p class="text-gray-600">Revisa y gestiona el historial de tus sesiones de clase.</p>
      </header>

      <div class="bg-white p-6 rounded-lg shadow space-y-4">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Curso</label>
            <select v-model="selectedCourseId" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
              <option :value="null">Todos los cursos</option>
              <option v-for="course in courses" :key="course.courseId" :value="course.courseId">
                {{ course.courseName }}
              </option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Desde</label>
            <input type="date" v-model="startDate" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Hasta</label>
            <input type="date" v-model="endDate" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
          </div>
          <div class="flex items-end">
            <button @click="fetchHistory" class="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">
              Filtrar
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="text-center py-8">
        <p class="text-gray-500">Cargando historial...</p>
      </div>

      <div v-else-if="history.length === 0" class="text-center py-8 bg-white rounded-lg shadow">
        <p class="text-gray-500">No se encontraron registros de asistencia para los filtros seleccionados.</p>
      </div>

      <div v-else class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Fecha</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Curso</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tema</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr v-for="record in history" :key="record.attendanceId">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ formatDate(record.date) }} <br>
                <span class="text-xs text-gray-500">{{ formatTime(record.timestamp) }}</span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {{ getCourseName(record.courseId) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {{ record.classType }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
                {{ record.todo || '—' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap">
                <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full"
                  :class="record.status === 'PRESENT' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'">
                  {{ record.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { attendanceService, type ProfessorAttendanceRecord } from '@/services/attendanceService'
import { useProfessorService } from '@/services/professorService'
import { gradeService, type ProfessorCourseSummary } from '@/services/gradeService'

const history = ref<ProfessorAttendanceRecord[]>([])
const loading = ref(false)
const courses = ref<ProfessorCourseSummary[]>([])
const selectedCourseId = ref<number | null>(null)
const startDate = ref(new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().split('T')[0])
const endDate = ref(new Date().toISOString().split('T')[0])
const professorId = ref<number | null>(null)

const { fetchCurrentProfessor } = useProfessorService()

const loadCourses = async () => {
  try {
    const response = await gradeService.fetchProfessorCourseSummaries()
    courses.value = response
  } catch (error) {
    console.error('Error loading courses', error)
  }
}

const fetchHistory = async () => {
  if (!professorId.value) return
  loading.value = true
  try {
    history.value = await attendanceService.getHistory(
      professorId.value,
      selectedCourseId.value ?? undefined,
      startDate.value,
      endDate.value
    )
  } catch (error) {
    console.error('Error fetching history', error)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString()
}

const formatTime = (isoStr: string) => {
  return new Date(isoStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getCourseName = (courseId: number) => {
  return courses.value.find(c => c.courseId === courseId)?.courseName ?? `Curso ${courseId}`
}

onMounted(async () => {
  try {
    const profile = await fetchCurrentProfessor()
    professorId.value = profile?.userId ?? null
    await loadCourses()
    if (professorId.value) {
      await fetchHistory()
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

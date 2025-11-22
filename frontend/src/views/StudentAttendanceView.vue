<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Header with Back Button -->
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">Mi Asistencia</h1>
          <p class="text-sm text-gray-500">Historial de asistencia del curso</p>
        </div>
        <button 
          @click="$router.back()" 
          class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
        >
          <svg class="-ml-1 mr-2 h-5 w-5 text-gray-500" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
            <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd" />
          </svg>
          Volver
        </button>
      </div>

      <div v-if="loading" class="flex justify-center py-12">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>

      <div v-else-if="error" class="bg-red-50 border-l-4 border-red-400 p-4">
        <div class="flex">
          <div class="flex-shrink-0">
            <svg class="h-5 w-5 text-red-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
            </svg>
          </div>
          <div class="ml-3">
            <p class="text-sm text-red-700">{{ error }}</p>
          </div>
        </div>
      </div>
      
      <div v-else class="space-y-6">
        <!-- Stats Cards -->
        <div class="grid grid-cols-1 gap-5 sm:grid-cols-3">
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Total Sesiones</dt>
              <dd class="mt-1 text-3xl font-semibold text-gray-900">{{ stats.total }}</dd>
            </div>
          </div>
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Asistencias</dt>
              <dd class="mt-1 text-3xl font-semibold text-green-600">{{ stats.present }}</dd>
            </div>
          </div>
          <div class="bg-white overflow-hidden shadow rounded-lg">
            <div class="px-4 py-5 sm:p-6">
              <dt class="text-sm font-medium text-gray-500 truncate">Porcentaje</dt>
              <dd class="mt-1 text-3xl font-semibold text-blue-600">{{ stats.percentage }}%</dd>
            </div>
          </div>
        </div>

        <!-- Attendance List -->
        <div class="bg-white shadow overflow-hidden sm:rounded-lg">
          <div class="px-4 py-5 sm:px-6 border-b border-gray-200">
            <h3 class="text-lg leading-6 font-medium text-gray-900">Detalle de Asistencia</h3>
          </div>
          <ul role="list" class="divide-y divide-gray-200">
            <li v-for="record in attendanceRecords" :key="record.id" class="hover:bg-gray-50 transition duration-150 ease-in-out">
              <div class="px-4 py-4 sm:px-6">
                <div class="flex items-center justify-between">
                  <div class="flex items-center">
                    <div class="flex-shrink-0">
                      <span :class="[
                        'inline-flex items-center justify-center h-10 w-10 rounded-full',
                        record.status === 'PRESENT' ? 'bg-green-100' : 'bg-red-100'
                      ]">
                        <span :class="[
                          'text-lg font-medium',
                          record.status === 'PRESENT' ? 'text-green-800' : 'text-red-800'
                        ]">
                          {{ record.status === 'PRESENT' ? 'P' : 'A' }}
                        </span>
                      </span>
                    </div>
                    <div class="ml-4">
                      <div class="text-sm font-medium text-blue-600 truncate">
                        {{ formatClassType(record.classType) }}
                      </div>
                      <div class="flex items-center text-sm text-gray-500">
                        <svg class="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true">
                          <path fill-rule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clip-rule="evenodd" />
                        </svg>
                        {{ formatDate(record.date) }}
                      </div>
                    </div>
                  </div>
                  <div class="flex flex-col items-end">
                    <span :class="[
                      'px-2 inline-flex text-xs leading-5 font-semibold rounded-full',
                      record.status === 'PRESENT' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    ]">
                      {{ formatStatus(record.status) }}
                    </span>
                    <span v-if="record.todo" class="mt-1 text-xs text-gray-500 max-w-xs truncate">
                      Nota: {{ record.todo }}
                    </span>
                  </div>
                </div>
              </div>
            </li>
            <li v-if="attendanceRecords.length === 0" class="px-4 py-8 text-center text-gray-500">
              No hay registros de asistencia para este curso.
            </li>
          </ul>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import AdminLayout from '@/components/ui/TopBar.vue'
import { attendanceService, type StudentAttendanceDTO } from '@/services/attendanceService'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const error = ref('')
const attendanceRecords = ref<StudentAttendanceDTO[]>([])

const stats = computed(() => {
  const total = attendanceRecords.value.length
  const present = attendanceRecords.value.filter(r => r.status === 'PRESENT').length
  const percentage = total > 0 ? Math.round((present / total) * 100) : 0
  return { total, present, percentage }
})

const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleDateString('es-ES', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

const formatClassType = (type: string) => {
  const map: Record<string, string> = {
    'THEORY': 'Teoría',
    'LABORATORY': 'Laboratorio',
    'PRACTICE': 'Práctica'
  }
  return map[type] || type
}

const formatStatus = (status: string) => {
  const map: Record<string, string> = {
    'PRESENT': 'Presente',
    'ABSENT': 'Ausente'
  }
  return map[status] || status
}

onMounted(async () => {
  const courseId = Number(route.params.courseId)
  const studentId = authStore.userCui 

  if (!courseId) {
    error.value = 'Curso no identificado'
    return
  }
  
  if (!studentId) {
    error.value = 'No se pudo identificar al estudiante'
    return
  }

  loading.value = true
  try {
    attendanceRecords.value = await attendanceService.getStudentAttendance(studentId, courseId)
  } catch (e) {
    error.value = 'Error al cargar la asistencia. Intente nuevamente más tarde.'
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

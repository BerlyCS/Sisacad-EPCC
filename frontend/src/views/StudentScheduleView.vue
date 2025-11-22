<template>
  <AdminLayout>
    <div class="space-y-6">
      <!-- Header -->
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Mi Horario Semanal</h2>
          <p class="text-gray-600 mt-1">Visualiza tu horario en formato de tabla profesional</p>
        </div>
        <button
          @click="loadSchedule"
          class="inline-flex items-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50"
          :disabled="loading"
        >
          <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          {{ loading ? 'Cargando...' : 'Actualizar' }}
        </button>
      </div>

      <!-- Schedule Table -->
      <WeeklyScheduleTable
        :schedule="schedule"
        :timeSlots="timeSlots"
        :loading="loading"
        :error="error"
        @refresh="loadSchedule"
      />
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AdminLayout from '../components/ui/TopBar.vue'
import WeeklyScheduleTable from '@/components/features/student/WeeklyScheduleTable.vue'
import { useStudentScheduleService } from '@/services/studentScheduleService'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const { schedule, timeSlots, loading, error, fetchMySchedule } = useStudentScheduleService()

const loadSchedule = async () => {
  if (!authStore.isAuthenticated) {
    await authStore.initializeAuth()
  }
  
  if (authStore.isAuthenticated && authStore.user?.role === 'STUDENT') {
    await fetchMySchedule()
  }
}

onMounted(() => {
  loadSchedule()
})
</script>
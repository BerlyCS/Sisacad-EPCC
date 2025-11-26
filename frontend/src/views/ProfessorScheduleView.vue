<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Mi Agenda Docente</h2>
          <p class="text-gray-600 mt-1">Consulta los cursos asignados y las reservas de aulas que realizaste.</p>
        </div>
        <div class="flex flex-col sm:flex-row gap-3">
          <button
            @click="loadSchedule"
            class="inline-flex items-center justify-center px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50"
            :disabled="scheduleLoading"
          >
            <svg v-if="scheduleLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            {{ scheduleLoading ? 'Cargando horario...' : 'Actualizar horario' }}
          </button>
          <button
            @click="loadReservations"
            class="inline-flex items-center justify-center px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50"
            :disabled="reservationsLoading"
          >
            <svg v-if="reservationsLoading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            {{ reservationsLoading ? 'Cargando reservas...' : 'Actualizar reservas' }}
          </button>
        </div>
      </div>

      <WeeklyScheduleTable
        :schedule="schedule"
        :timeSlots="timeSlots"
        :loading="scheduleLoading"
        :error="scheduleError"
        @refresh="loadSchedule"
      />

      <ProfessorReservationList
        :reservations="reservations"
        :loading="reservationsLoading"
        :error="reservationsError"
        @refresh="loadReservations"
      />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import WeeklyScheduleTable from '@/components/features/student/WeeklyScheduleTable.vue'
import ProfessorReservationList from '@/components/features/professor/ProfessorReservationList.vue'
import { useProfessorScheduleService } from '@/services/professorScheduleService'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const {
  schedule,
  timeSlots,
  reservations,
  scheduleLoading,
  reservationsLoading,
  scheduleError,
  reservationsError,
  fetchSchedule,
  fetchReservations
} = useProfessorScheduleService()

const ensureAuth = async () => {
  if (!authStore.initialized) {
    await authStore.initializeAuth()
  }
}

const canLoad = () => authStore.isAuthenticated && authStore.user?.role === 'PROFESSOR'

const loadSchedule = async () => {
  await ensureAuth()
  if (canLoad()) {
    await fetchSchedule()
  }
}

const loadReservations = async () => {
  await ensureAuth()
  if (canLoad()) {
    await fetchReservations()
  }
}

onMounted(() => {
  loadSchedule()
  loadReservations()
})
</script>

<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <p class="text-sm font-semibold text-indigo-600 uppercase tracking-wide">Mis reservas</p>
          <h1 class="text-3xl font-bold text-gray-900">Reservas de aulas</h1>
          <p class="text-sm text-gray-600">Consulta y cancela las reservas que realizaste.</p>
        </div>
        <div class="flex gap-3">
          <button
            type="button"
            class="inline-flex items-center justify-center gap-2 rounded-md border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
            :disabled="loading"
            @click="reload"
          >
            <i v-if="loading" class="fas fa-spinner fa-spin" />
            <span>{{ loading ? 'Actualizando...' : 'Actualizar' }}</span>
          </button>
        </div>
      </header>

      <ProfessorReservationList
        :reservations="reservations"
        :loading="loading"
        :error="errorMessage"
        :pending-id="cancelingId"
        @refresh="reload"
        @cancel="handleCancel"
      />

      <div v-if="successMessage" class="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
        <i class="fas fa-check-circle mr-2" /> {{ successMessage }}
      </div>
      <div v-if="actionError" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
        <i class="fas fa-exclamation-triangle mr-2" /> {{ actionError }}
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import ProfessorReservationList from '@/components/features/professor/ProfessorReservationList.vue'
import { useProfessorScheduleService } from '@/services/professorScheduleService'
import { reservationService } from '@/services/reservationService'

const {
  reservations,
  reservationsLoading,
  reservationsError,
  fetchReservations
} = useProfessorScheduleService()

const cancelingId = ref<number | null>(null)
const successMessage = ref('')
const actionError = ref('')

const loading = reservationsLoading
const errorMessage = reservationsError

const reload = async () => {
  successMessage.value = ''
  actionError.value = ''
  await fetchReservations()
}

const handleCancel = async (id: number) => {
  const confirmed = window.confirm('¿Deseas cancelar esta reserva?')
  if (!confirmed) return

  try {
    cancelingId.value = id
    actionError.value = ''
    successMessage.value = ''
    await reservationService.deleteReservation(id)
    await fetchReservations()
    successMessage.value = 'Reserva cancelada correctamente.'
  } catch (error: any) {
    console.error('Error al cancelar reserva', error)
    actionError.value = error?.message || 'No se pudo cancelar la reserva.'
  } finally {
    cancelingId.value = null
  }
}

onMounted(async () => {
  await fetchReservations()
})
</script>

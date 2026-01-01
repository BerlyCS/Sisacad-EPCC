<template>
  <div class="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
    <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4 px-6 py-4 border-b border-slate-200 bg-slate-50">
      <div>
        <h3 class="text-lg font-semibold text-slate-800">Reservas Programadas</h3>
        <p class="text-sm text-slate-600">Revisa las aulas que reservaste para asesorías o actividades especiales.</p>
      </div>
      <button
        class="inline-flex items-center px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-md hover:bg-indigo-700 transition-colors disabled:opacity-50"
        :disabled="loading"
        @click="$emit('refresh')"
      >
        <svg v-if="loading" class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
        {{ loading ? 'Actualizando...' : 'Actualizar' }}
      </button>
    </div>

    <div v-if="loading" class="p-8 text-center text-slate-600 text-sm">
      Cargando reservas...
    </div>

    <div v-else-if="error" class="p-8 text-center">
      <p class="text-red-600 text-sm">{{ error }}</p>
      <button
        class="mt-3 px-4 py-2 bg-indigo-600 text-white text-sm rounded-md hover:bg-indigo-700"
        @click="$emit('refresh')"
      >
        Reintentar
      </button>
    </div>

    <div v-else-if="!reservations.length" class="p-8 text-center text-slate-500 text-sm">
      No tienes reservas registradas aún.
    </div>

    <ul v-else class="divide-y divide-slate-100">
      <li v-for="reservation in reservations" :key="reservation.id" class="p-5 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <p class="text-sm font-semibold text-slate-800">{{ reservation.classroomName }}</p>
          <p class="text-xs text-slate-500 mt-1">{{ reservation.purpose }}</p>
        </div>
        <div class="flex flex-col md:flex-row md:items-center gap-3 md:gap-6 text-sm text-slate-600">
          <div>
            <p class="font-medium">{{ formatDate(reservation.reservationDate, reservation.dayOfWeek) }}</p>
            <p class="text-xs text-slate-500">{{ reservation.startTime }} - {{ reservation.endTime }}</p>
          </div>
          <button
            v-if="reservation.canDelete"
            class="inline-flex items-center gap-2 rounded-md border border-red-200 bg-red-50 px-3 py-1.5 text-xs font-semibold text-red-700 hover:bg-red-100 disabled:opacity-60"
            :disabled="pendingId === reservation.id"
            @click="$emit('cancel', reservation.id)"
          >
            <i v-if="pendingId === reservation.id" class="fas fa-spinner fa-spin" />
            <i v-else class="fas fa-trash" />
            <span>{{ pendingId === reservation.id ? 'Cancelando...' : 'Cancelar' }}</span>
          </button>
        </div>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import type { ProfessorReservationEntry } from '@/services/professorScheduleService'
defineProps<{
  reservations: ProfessorReservationEntry[]
  loading: boolean
  error: string
  pendingId?: number | null
}>()

defineEmits<{
  (e: 'refresh'): void
  (e: 'cancel', id: number): void
}>()

const dateFormatter = new Intl.DateTimeFormat('es-PE', { dateStyle: 'full' })

const formatDate = (date: string, day: string) => {
  if (!date) {
    return day || 'Fecha por definir'
  }

  try {
    return dateFormatter.format(new Date(date))
  } catch (error) {
    console.warn('Unable to format reservation date', error)
    return `${day || 'Día pendiente'} · ${date}`
  }
}
</script>

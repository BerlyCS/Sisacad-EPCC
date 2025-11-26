<template>
  <div class="bg-white rounded-lg shadow-sm border border-slate-200 overflow-hidden">
    <!-- Header -->
    <div class="bg-slate-50 px-6 py-4 border-b border-slate-200">
      <h3 class="text-lg font-semibold text-slate-800">Horario Semanal</h3>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="p-8 text-center">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <p class="mt-2 text-slate-600 text-sm">Cargando horario...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="p-8 text-center">
      <div class="text-red-600 text-sm">{{ error }}</div>
      <button 
        @click="$emit('refresh')"
        class="mt-3 px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 transition-colors"
      >
        Reintentar
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="!schedule.length" class="p-12 text-center">
      <div class="text-slate-400">
        <svg class="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      </div>
      <p class="mt-3 text-slate-500 text-sm">No tienes cursos matriculados</p>
    </div>

    <!-- Table -->
    <div v-else class="overflow-x-auto">
      <table class="w-full min-w-[800px]">
        <!-- Header Row -->
        <thead class="bg-slate-50">
          <tr>
            <th class="w-24 px-4 py-3 text-left text-xs font-medium text-slate-600 uppercase tracking-wider border-r border-slate-200">
              Hora
            </th>
            <th 
              v-for="day in weekDays" 
              :key="day.key"
              class="px-4 py-3 text-center text-xs font-medium text-slate-600 uppercase tracking-wider border-r last:border-r-0 border-slate-200"
            >
              {{ day.label }}
            </th>
          </tr>
        </thead>
        
        <!-- Body Rows -->
        <tbody class="divide-y divide-slate-100">
          <tr 
            v-for="slot in timeSlots" 
            :key="slot.startTime"
            class="hover:bg-slate-50 transition-colors"
          >
            <!-- Time Slot -->
            <td class="px-4 py-3 text-sm text-slate-600 bg-slate-50 font-medium border-r border-slate-200 whitespace-nowrap">
              {{ formatTime(slot.startTime) }}<br>
              <span class="text-xs text-slate-400">{{ formatTime(slot.endTime) }}</span>
            </td>
            
            <!-- Days Columns -->
            <td 
              v-for="day in weekDays" 
              :key="day.key"
              class="px-3 py-3 text-center border-r last:border-r-0 border-slate-200 align-top min-h-[80px]"
              :class="getCellClasses(slot, day.key)"
            >
              <div v-if="getCourse(slot, day.key)" class="h-full flex flex-col justify-center">
                <span class="font-medium text-slate-800 text-sm">{{ getCourse(slot, day.key)?.courseName }}</span>
                <span class="text-xs text-slate-600 mt-1">{{ getCourseTypeLabel(getCourse(slot, day.key)?.courseType) }}</span>
                <span class="text-xs text-slate-500 mt-1">{{ getCourse(slot, day.key)?.classroomName }}</span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">

type ScheduleDay = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES'

interface ScheduleEntry {
  courseId?: number | null
  courseCode?: number | null
  courseName: string
  courseType?: string
  dayOfWeek: string
  startTime: string
  endTime: string
  classroomName?: string
}

interface TimeSlot {
  startTime: string
  endTime: string
}

const CLASS_TYPE_LABEL: Record<string, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio',
  PRACTICE: 'Práctica'
}

const weekDays: { key: ScheduleDay; label: string }[] = [
  { key: 'LUNES', label: 'Lunes' },
  { key: 'MARTES', label: 'Martes' },
  { key: 'MIERCOLES', label: 'Miércoles' },
  { key: 'JUEVES', label: 'Jueves' },
  { key: 'VIERNES', label: 'Viernes' }
]

const props = defineProps<{
  schedule: ScheduleEntry[]
  timeSlots: TimeSlot[]
  loading: boolean
  error: string
}>()

defineEmits<{
  (e: 'refresh'): void
}>()

const formatTime = (time: string): string => {
  if (!time) return ''
  const parts = time.split(':')
  if (parts.length < 2 || !parts[0] || !parts[1]) return time
  const hours = parts[0].padStart(2, '0')
  return `${hours}:${parts[1]}`
}

const getCourseTypeLabel = (type?: string): string => {
  if (!type) return ''
  return CLASS_TYPE_LABEL[type.toUpperCase()] || type
}

const normalizeDay = (value: string): string =>
  value
    ? value.toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '')
    : ''

const getCourse = (slot: TimeSlot, day: ScheduleDay): ScheduleEntry | undefined => {
  return props.schedule.find(entry => 
    normalizeDay(entry.dayOfWeek) === day &&
    entry.startTime === slot.startTime &&
    entry.endTime === slot.endTime
  )
}

const getCellClasses = (slot: TimeSlot, day: ScheduleDay): string => {
  const hasCourse = !!getCourse(slot, day)
  return hasCourse 
    ? 'bg-slate-100 hover:bg-slate-200 transition-colors cursor-pointer' 
    : 'bg-white'
}
</script>
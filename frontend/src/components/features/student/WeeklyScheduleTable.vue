<template>
  <div class="bg-white rounded-2xl shadow-lg border border-slate-200 overflow-hidden">
    <!-- Header -->
    <div class="bg-gradient-to-r from-indigo-600 via-blue-600 to-sky-500 px-6 py-5 border-b border-slate-200">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-xs uppercase tracking-[0.18em] text-indigo-100 font-semibold">Agenda académica</p>
          <h3 class="text-xl font-bold text-white">Horario Semanal</h3>
        </div>
        <button
          class="hidden sm:inline-flex items-center gap-2 px-3 py-1.5 text-xs font-semibold text-white border border-white/40 rounded-full backdrop-blur-sm hover:border-white"
          @click="$emit('refresh')"
        >
          Refrescar
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="p-8 text-center space-y-4">
      <div class="inline-flex gap-2">
        <span class="inline-block animate-spin rounded-full h-8 w-8 border-2 border-indigo-200 border-t-indigo-600"></span>
        <span class="inline-block animate-spin rounded-full h-8 w-8 border-2 border-sky-200 border-t-sky-500"></span>
      </div>
      <p class="text-slate-600 text-sm">Cargando horario...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="p-8 text-center space-y-3">
      <div class="inline-flex items-center gap-2 px-3 py-2 rounded-md bg-red-50 text-red-700 text-sm border border-red-100">
        <span class="h-2 w-2 rounded-full bg-red-500 animate-pulse"></span>
        <span>{{ error }}</span>
      </div>
      <button
        @click="$emit('refresh')"
        class="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm rounded-md shadow-sm hover:bg-indigo-700 transition-colors"
      >
        <span class="h-2 w-2 rounded-full bg-white"></span>
        Reintentar
      </button>
    </div>

    <!-- Empty State -->
    <div v-else-if="!schedule.length" class="p-12 text-center space-y-3 bg-slate-50">
      <div class="mx-auto h-14 w-14 rounded-full bg-white shadow-inner flex items-center justify-center text-slate-300">
        <svg class="h-7 w-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
      </div>
      <p class="text-slate-600 text-sm font-medium">No tienes cursos matriculados</p>
      <p class="text-slate-400 text-xs">Cuando agregues cursos verás aquí tu agenda semanal.</p>
    </div>

    <!-- Table -->
    <div v-else class="overflow-x-auto">
      <table class="w-full min-w-[900px]">
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
              class="px-3 py-3 text-center border-r last:border-r-0 border-slate-200 align-top min-h-[88px]"
              :class="getCellClasses(slot, day.key)"
            >
              <div v-if="getCourse(slot, day.key)" class="h-full flex flex-col items-center text-center gap-1">
                <span class="font-semibold text-slate-900 text-sm">{{ getCourse(slot, day.key)?.courseName }}</span>
                <span
                  class="text-[11px] font-semibold px-2 py-0.5 rounded-full"
                  :class="typeBadgeClass(getCourse(slot, day.key)?.courseType)">
                  {{ getCourseTypeLabel(getCourse(slot, day.key)?.courseType) }}
                </span>
                <span class="text-xs text-slate-500">{{ getCourse(slot, day.key)?.classroomName || 'Sin aula' }}</span>
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

const typeBadgeClass = (type?: string): string => {
  if (!type) return 'bg-slate-100 text-slate-500'
  const key = type.toUpperCase()
  if (key === 'LAB') return 'bg-emerald-100 text-emerald-700 border border-emerald-200'
  if (key === 'THEORY') return 'bg-indigo-100 text-indigo-700 border border-indigo-200'
  if (key === 'PRACTICE') return 'bg-amber-100 text-amber-700 border border-amber-200'
  return 'bg-slate-100 text-slate-600'
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
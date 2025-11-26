<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <h1 class="text-3xl font-bold text-gray-900">Registro de Asistencia</h1>
        <p class="text-gray-600">Gestiona tu propia asistencia y la de tus estudiantes por grupo.</p>
      </header>

      <ProfessorCourseList
        :courses="courses"
        :loading="coursesLoading"
        :error="coursesError || ''"
        @select="handleSelectCourse"
        @retry="loadCourses"
      />

      <section v-if="selectedCourse" class="space-y-6 bg-white p-6 rounded-lg shadow">
        <div class="space-y-1">
          <h2 class="text-xl font-semibold text-gray-900">{{ selectedCourse.courseName }}</h2>
          <p class="text-sm text-gray-500">
            Código {{ selectedCourse.courseCode }} · Grupo {{ activeGroup?.groupLetter ?? '—' }} · {{ classTypeLabel }}
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Fecha</label>
            <input
              type="date"
              v-model="sessionDate"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            >
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Tipo de Clase</label>
            <div class="mt-1 block w-full rounded-md border-gray-200 bg-gray-50 px-3 py-2 text-gray-700 shadow-sm">
              {{ classTypeLabel }}
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Bloque horario</label>
            <select
              v-model="selectedTimeSlotKey"
              class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              :disabled="timeSlotsLoading || !timeSlotOptions.length"
            >
              <option v-if="timeSlotsLoading" disabled> Cargando bloques... </option>
              <option v-else-if="!timeSlotOptions.length" disabled>No hay bloques disponibles</option>
              <option v-for="slot in timeSlotOptions" :key="slot.key" :value="slot.key">{{ slot.label }}</option>
            </select>
            <p v-if="timeSlotsError" class="text-xs text-red-600 mt-1">{{ timeSlotsError }}</p>
          </div>
        </div>

        <GroupFilterTabs
          :groups="courseGroups"
          :selected-ids="selectedGroupIds"
          :loading="courseGroupsLoading"
          :error="courseGroupsError || ''"
          @update:selected-ids="handleGroupSelection"
        />

        <div class="flex flex-wrap gap-3">
          <button
            type="button"
            class="px-4 py-2 rounded-full text-sm font-semibold transition-colors"
            :class="attendanceMode === 'PROFESSOR' ? 'bg-blue-600 text-white' : 'bg-blue-50 text-blue-700'"
            @click="attendanceMode = 'PROFESSOR'"
          >
            Mi asistencia
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-full text-sm font-semibold transition-colors"
            :class="attendanceMode === 'STUDENTS' ? 'bg-blue-600 text-white' : 'bg-blue-50 text-blue-700'"
            @click="attendanceMode = 'STUDENTS'"
          >
            Asistencia de estudiantes
          </button>
        </div>

        <div v-if="attendanceMode === 'PROFESSOR'" class="space-y-4">
          <div class="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
            <p>
              La tolerancia para registrar tu asistencia es de 15 minutos desde el inicio del bloque seleccionado.
            </p>
            <p class="text-xs text-slate-500 mt-1">
              Bloque: {{ selectedTimeSlot?.startTime ?? '—' }} - {{ selectedTimeSlot?.endTime ?? '—' }} · Ventana válida: 
              <span v-if="toleranceWindow">
                {{ formatTimeFromDate(toleranceWindow.start) }} – {{ formatTimeFromDate(toleranceWindow.end) }}
              </span>
              <span v-else>Configura un bloque válido</span>
            </p>
          </div>

          <label class="text-sm font-medium text-gray-700 block">
            Nota opcional
            <textarea
              v-model="professorNote"
              rows="3"
              class="mt-1 w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
              placeholder="Observaciones para la sesión"
            ></textarea>
          </label>

          <button
            type="button"
            class="px-4 py-2 rounded-md text-white bg-blue-600 hover:bg-blue-700 transition disabled:opacity-60"
            :disabled="professorAttendanceSubmitting || !withinProfessorTolerance || !activeGroup"
            @click="handleProfessorAttendance"
          >
            {{ professorAttendanceSubmitting ? 'Registrando...' : 'Registrar mi asistencia' }}
          </button>
          <p v-if="!withinProfessorTolerance" class="text-sm text-amber-600">
            Solo puedes registrar tu asistencia dentro de los primeros 15 minutos del bloque elegido.
          </p>
          <p v-if="professorAttendanceError" class="text-sm text-red-600">{{ professorAttendanceError }}</p>
          <p v-if="professorAttendanceSuccess" class="text-sm text-green-600">¡Tu asistencia se registró correctamente!</p>
        </div>

        <div v-else>
          <div v-if="rosterLoading" class="text-center py-4">Cargando estudiantes...</div>
          <div v-else>
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-lg font-medium">Estudiantes</h3>
              <button @click="markAllPresent" class="text-sm text-blue-600 hover:text-blue-800">Marcar todos presentes</button>
            </div>

            <div class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estudiante</th>
                    <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Asistencia</th>
                  </tr>
                </thead>
                <tbody class="bg-white divide-y divide-gray-200">
                  <tr v-for="student in attendanceList" :key="student.studentId">
                    <td class="px-6 py-4 whitespace-nowrap">
                      <div class="text-sm font-medium text-gray-900">{{ student.fullName }}</div>
                      <div class="text-sm text-gray-500">{{ student.studentId }}</div>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <select v-model="student.status" class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm">
                        <option value="PRESENT">Presente</option>
                        <option value="ABSENT">Ausente</option>
                      </select>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="mt-6 flex justify-end">
              <button 
                @click="submitAttendance" 
                :disabled="submitting"
                class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50"
              >
                {{ submitting ? 'Guardando...' : 'Guardar Asistencia' }}
              </button>
            </div>
            <p v-if="submitError" class="mt-2 text-red-600 text-right">{{ submitError }}</p>
            <p v-if="submitSuccess" class="mt-2 text-green-600 text-right">Asistencia guardada correctamente.</p>
          </div>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed, onBeforeUnmount } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { ProfessorCourseList, GroupFilterTabs } from '@/components/features/professor'
import { attendanceService, type ClassType, type AttendanceStatus } from '@/services/attendanceService'
import { useProfessorService } from '@/services/professorService'
import type { CourseRosterEntry, ProfessorCourseSummary } from '@/services/gradeService'
import { useProfessorAttendanceWorkspace } from '@/composables/useProfessorAttendanceWorkspace'

type AttendanceMode = 'PROFESSOR' | 'STUDENTS'

const API_BASE_URL = 'http://localhost:8080/api'
const TOLERANCE_MINUTES = 15

const {
  courses,
  coursesLoading,
  coursesError,
  selectedCourse,
  courseGroups,
  courseGroupsLoading,
  courseGroupsError,
  selectedGroupIds,
  rosterEntries,
  rosterLoading,
  loadCourses,
  selectCourse,
  updateSelectedGroups
} = useProfessorAttendanceWorkspace()

const { fetchCurrentProfessor } = useProfessorService()

const sessionDate = ref(new Date().toISOString().split('T')[0])
const attendanceMode = ref<AttendanceMode>('STUDENTS')
const timeSlots = ref<{ startTime: string; endTime: string }[]>([])
const timeSlotsLoading = ref(false)
const timeSlotsError = ref('')
const selectedTimeSlotKey = ref('')

const submitting = ref(false)
const submitError = ref('')
const submitSuccess = ref(false)

const professorAttendanceSubmitting = ref(false)
const professorAttendanceError = ref('')
const professorAttendanceSuccess = ref(false)
const professorNote = ref('')

const professorId = ref<number | null>(null)
const now = ref(new Date())
let tickingTimer: number | null = null

interface AttendanceEntry {
  studentId: string
  fullName: string
  status: AttendanceStatus
}

const attendanceList = ref<AttendanceEntry[]>([])

const activeGroupId = computed(() => selectedGroupIds.value[0] ?? null)
const activeGroup = computed(() => {
  const id = activeGroupId.value
  if (!id) {
    return null
  }
  return courseGroups.value.find(group => group.groupId === id) ?? null
})

const mapClassType = (groupType?: string | null): ClassType => {
  const normalized = groupType?.toUpperCase()
  if (normalized === 'LAB') {
    return 'LABORATORY'
  }
  if (normalized === 'PRACTICE') {
    return 'PRACTICE'
  }
  return 'THEORY'
}

const CLASS_TYPE_LABEL: Record<ClassType, string> = {
  THEORY: 'Teoría',
  PRACTICE: 'Práctica',
  LABORATORY: 'Laboratorio'
}

const classType = computed<ClassType>(() => mapClassType(activeGroup.value?.courseType ?? selectedCourse.value?.courseType))
const classTypeLabel = computed(() => CLASS_TYPE_LABEL[classType.value] ?? classType.value)

const timeSlotOptions = computed(() => timeSlots.value.map(slot => ({
  key: `${slot.startTime}|${slot.endTime}`,
  label: `${slot.startTime} - ${slot.endTime}`
})))

watch(timeSlotOptions, options => {
  if (!selectedTimeSlotKey.value && options?.length) {
    const first = options[0]
    if (first) {
      selectedTimeSlotKey.value = first.key
    }
  }
})

const selectedTimeSlot = computed(() => {
  if (!selectedTimeSlotKey.value) {
    return null
  }
  const [startTime, endTime] = selectedTimeSlotKey.value.split('|')
  if (!startTime || !endTime) {
    return null
  }
  return { startTime, endTime }
})

const sessionStartDateTime = computed(() => combineDateAndTime(sessionDate.value, selectedTimeSlot.value?.startTime ?? null))
const toleranceWindow = computed(() => {
  const start = sessionStartDateTime.value
  if (!start) {
    return null
  }
  const end = addMinutes(start, TOLERANCE_MINUTES)
  return { start, end }
})

const withinProfessorTolerance = computed(() => {
  const window = toleranceWindow.value
  if (!window) {
    return false
  }
  const nowValue = now.value
  return nowValue >= window.start && nowValue <= window.end
})

const mapStudentToAttendance = (entry: CourseRosterEntry): AttendanceEntry => ({
  studentId: entry.studentCui || String(entry.studentUserId) || '',
  fullName: entry.fullName,
  status: 'ABSENT'
})

watch(rosterEntries, entries => {
  attendanceList.value = entries.map(mapStudentToAttendance)
})

const handleSelectCourse = async (course: ProfessorCourseSummary) => {
  submitSuccess.value = false
  submitError.value = ''
  professorAttendanceSuccess.value = false
  professorAttendanceError.value = ''
  attendanceMode.value = 'STUDENTS'
  await selectCourse(course)
}

const handleGroupSelection = async (groupIds: number[]) => {
  const normalized: number[] = []
  if (groupIds.length) {
    const lastGroupId = groupIds[groupIds.length - 1]
    if (typeof lastGroupId === 'number') {
      normalized.push(lastGroupId)
    }
  }
  submitSuccess.value = false
  submitError.value = ''
  professorAttendanceSuccess.value = false
  professorAttendanceError.value = ''
  await updateSelectedGroups(normalized)
}

const markAllPresent = () => {
  attendanceList.value.forEach(student => {
    student.status = 'PRESENT'
  })
}

const handleProfessorAttendance = async () => {
  professorAttendanceError.value = ''
  professorAttendanceSuccess.value = false

  if (!selectedCourse.value || !activeGroup.value) {
    professorAttendanceError.value = 'Selecciona un grupo válido antes de registrar tu asistencia.'
    return
  }

  if (!professorId.value) {
    professorAttendanceError.value = 'No se pudo identificar al profesor autenticado.'
    return
  }

  if (!selectedTimeSlot.value) {
    professorAttendanceError.value = 'Selecciona un bloque horario para continuar.'
    return
  }

  if (!withinProfessorTolerance.value) {
    professorAttendanceError.value = 'Estás fuera de la ventana de tolerancia de 15 minutos.'
    return
  }

  professorAttendanceSubmitting.value = true
  try {
    await attendanceService.markProfessorAttendance({
      professorId: professorId.value,
      courseId: selectedCourse.value.courseId,
      groupId: activeGroup.value.groupId,
      status: 'PRESENT',
      classType: classType.value,
      date: sessionDate.value,
      timestamp: new Date().toISOString(),
      todo: professorNote.value
    })
    professorAttendanceSuccess.value = true
  } catch (error) {
    console.error('Error while registering professor attendance', error)
    professorAttendanceError.value = 'No se pudo registrar tu asistencia. Intenta nuevamente.'
  } finally {
    professorAttendanceSubmitting.value = false
  }
}

const submitAttendance = async () => {
  submitError.value = ''
  submitSuccess.value = false

  if (!selectedCourse.value || !activeGroup.value) {
    submitError.value = 'Selecciona un grupo válido antes de guardar la asistencia.'
    return
  }

  if (!professorId.value) {
    submitError.value = 'No se pudo identificar al profesor. Recarga la página e intenta nuevamente.'
    return
  }

  submitting.value = true
  try {
    await attendanceService.createSession({
      professorId: professorId.value,
      courseId: selectedCourse.value.courseId,
      groupId: activeGroup.value.groupId,
      date: sessionDate.value,
      classType: classType.value,
      todo: professorNote.value,
      students: attendanceList.value.map(student => ({
        studentId: student.studentId,
        status: student.status
      }))
    })
    submitSuccess.value = true
  } catch (error) {
    console.error('Error while saving student attendance', error)
    submitError.value = 'No se pudo guardar la asistencia de los estudiantes.'
  } finally {
    submitting.value = false
  }
}

const loadTimeSlots = async () => {
  timeSlotsLoading.value = true
  timeSlotsError.value = ''
  try {
    const response = await fetch(`${API_BASE_URL}/courses/timeslots`, {
      credentials: 'include'
    })
    if (!response.ok) {
      throw new Error('HTTP error')
    }
    timeSlots.value = await response.json()
  } catch (error) {
    console.error('Error while loading course time slots', error)
    timeSlots.value = []
    timeSlotsError.value = 'No se pudieron cargar los bloques horarios.'
  } finally {
    timeSlotsLoading.value = false
  }
}

const loadProfessorProfile = async () => {
  try {
    const profile = await fetchCurrentProfessor()
    professorId.value = profile?.userId ?? null
  } catch (error) {
    console.error('No se pudo obtener la información del profesor', error)
    professorId.value = null
  }
}

const combineDateAndTime = (date?: string | null, time?: string | null): Date | null => {
  if (!date || !time) {
    return null
  }
  const [yearStr, monthStr, dayStr] = date.split('-')
  const [hourStr, minuteStr] = time.split(':')
  if (!yearStr || !monthStr || !dayStr || !hourStr || !minuteStr) {
    return null
  }
  const year = Number(yearStr)
  const month = Number(monthStr)
  const day = Number(dayStr)
  const hours = Number(hourStr)
  const minutes = Number(minuteStr)
  if ([year, month, day, hours, minutes].some(value => Number.isNaN(value))) {
    return null
  }
  return new Date(year, month - 1, day, hours, minutes, 0)
}

const addMinutes = (date: Date, minutes: number): Date => {
  return new Date(date.getTime() + minutes * 60 * 1000)
}

const formatTimeFromDate = (date: Date | null) => {
  if (!date) {
    return '—'
  }
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  loadCourses()
  loadTimeSlots()
  loadProfessorProfile()
  tickingTimer = window.setInterval(() => {
    now.value = new Date()
  }, 15_000)
})

onBeforeUnmount(() => {
  if (tickingTimer) {
    clearInterval(tickingTimer)
  }
})
</script>

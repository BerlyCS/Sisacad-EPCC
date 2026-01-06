<template>
  <section class="bg-white shadow rounded-lg p-6 space-y-6">
    <header>
      <h2 class="text-xl font-semibold text-gray-800">Matricular estudiante en un curso</h2>
      <p class="text-sm text-gray-500">Disponible para secretarias y administradores</p>
    </header>

    <form class="space-y-5" @submit.prevent="handleSubmit">
      <div class="grid gap-4 md:grid-cols-[2fr_auto]">
        <div>
          <label class="block text-sm font-medium text-gray-700">CUI del estudiante</label>
          <input
            v-model="studentCui"
            type="text"
            class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
            placeholder="20251234"
            autocomplete="off"
          />
        </div>
        <button
          type="button"
          @click="searchStudent"
          :disabled="isSearching"
          class="self-end rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <span v-if="!isSearching">Buscar estudiante</span>
          <span v-else>Buscando...</span>
        </button>
      </div>
      <p v-if="studentSearchError" class="text-sm text-red-600">{{ studentSearchError }}</p>

      <StudentProfileSummary
        v-if="studentProfile"
        :profile="studentProfile"
        subtitle="Estudiante seleccionado"
      />

      <StudentCoursesCard
        v-if="studentProfile"
        :courses="enrolledCourses"
        :loading="currentCoursesLoading"
        :error="currentCoursesError"
        :refreshable="false"
      />

      <StudentScheduleCard
        v-if="studentProfile"
        :entries="scheduleEntries"
        :loading="currentScheduleLoading"
        :error="currentScheduleError"
        :refreshable="false"
      />

      <div>
        <label class="block text-sm font-medium text-gray-700">Curso</label>
        <div class="mt-1 relative">
          <select
            v-model="selectedCourseId"
            class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900 appearance-none pr-10"
            @change="onCourseChange"
          >
            <option value="">Selecciona un curso</option>
            <option
              v-for="course in availableCourses"
              :key="course.courseId"
              :value="course.courseId"
            >
              {{ course.name }} (Código: {{ course.courseCode }})
            </option>
          </select>
          <!-- Icono de flecha para el dropdown -->
          <div class="absolute inset-y-0 right-0 flex items-center px-2 pointer-events-none">
            <svg class="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </div>
        </div>
        <p v-if="coursesLoading" class="mt-1 text-sm text-gray-500">Cargando cursos...</p>
        <p v-else-if="availableCourses.length === 0" class="mt-1 text-sm text-red-600">No se encontraron cursos disponibles</p>
      </div>

      <div v-if="selectedCourseId">
        <label class="block text-sm font-medium text-gray-700">Selecciona la letra del grupo</label>
        <p class="text-xs text-gray-500">Solo matricula de teoría y práctica</p>
        <p v-if="courseGroupsLoading" class="mt-2 text-sm text-gray-500">Cargando grupos...</p>
        <p v-else-if="groupSelectionOptions.length === 0" class="mt-2 text-sm text-red-600">
          No existen existen grupos para este curso.
        </p>
        <div v-else class="mt-3 grid gap-3 md:grid-cols-2">
          <label
            v-for="option in groupSelectionOptions"
            :key="option.letter"
            class="rounded-2xl border px-4 py-3 transition cursor-pointer"
            :class="[
              selectedGroupLetter === option.letter ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-300',
              option.hasConflict ? 'opacity-60 cursor-not-allowed' : ''
            ]"
          >
            <input
              type="radio"
              class="sr-only"
              :value="option.letter"
              v-model="selectedGroupLetter"
              :disabled="option.hasConflict || isSubmitting"
            >
            <div class="flex items-start justify-between gap-3">
              <div>
                <p class="text-base font-semibold text-gray-900">Grupo {{ option.letter }}</p>
                <div class="mt-1 flex flex-wrap gap-1">
                  <span
                    v-for="group in option.groups"
                    :key="`tag-${group.groupId}`"
                    class="inline-flex items-center rounded-full border border-gray-200 bg-white/80 px-2 py-0.5 text-[11px] font-semibold text-gray-700"
                  >
                    {{ group.typeLabel }}
                  </span>
                </div>
              </div>
              <span
                class="text-xs font-semibold"
                :class="option.hasConflict ? 'text-red-600' : 'text-emerald-600'"
              >
                {{ option.hasConflict ? 'Conflicto de horario' : 'Disponible' }}
              </span>
            </div>
            <ul class="mt-3 space-y-1 text-xs text-gray-600">
              <li v-for="slot in option.scheduleSlots" :key="slot.key">
                {{ slot.typeLabel }} · {{ slot.displayDay }} {{ slot.startTime || 'Sin hora' }} - {{ slot.endTime || 'Sin hora' }}
              </li>
              <li v-if="!option.scheduleSlots.length">Sin horario registrado</li>
            </ul>
            <p v-if="option.hasConflict" class="mt-2 text-xs text-red-600">
              {{ option.conflictDetails[0] }}
              <span v-if="option.conflictDetails.length > 1">(+{{ option.conflictDetails.length - 1 }} más)</span>
            </p>
          </label>
        </div>
        <p v-if="selectedLetterConflicts.length" class="mt-2 text-sm text-red-600">
          No puedes matricular este grupo porque {{ selectedLetterConflicts[0] }}
          <span v-if="selectedLetterConflicts.length > 1"> y {{ selectedLetterConflicts.length - 1 }} conflicto(s) adicional(es)</span>.
        </p>
        <p v-if="courseGroupsError" class="mt-2 text-sm text-red-600">{{ courseGroupsError }}</p>
      </div>

      <div class="flex flex-wrap gap-3">
        <button
          type="submit"
          :disabled="!canSubmit"
          class="rounded-md bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <span v-if="!isSubmitting">Matricular estudiante</span>
          <span v-else>Procesando...</span>
        </button>
        <button type="button" @click="resetForm" class="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">
          Limpiar formulario
        </button>
      </div>
    </form>

    <p v-if="submitError" class="text-sm text-red-600">{{ submitError }}</p>
    <p v-if="successMessage" class="text-sm text-emerald-600">{{ successMessage }}</p>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import StudentProfileSummary from '@/components/features/student/StudentProfileSummary.vue'
import StudentCoursesCard from '@/components/features/student/StudentCoursesCard.vue'
import StudentScheduleCard from '@/components/features/student/StudentScheduleCard.vue'
import { useCourseService } from '@/services/courseService'
import type { CourseGroupSummary, CourseScheduleSlotSummary } from '@/services/courseService'
import { useSecretaryService } from '@/services/secretaryService'
import { useStudentService } from '@/services/studentService'

const studentCui = ref('')
const selectedCourseId = ref<number | ''>('')
const selectedGroupLetter = ref('')
const studentSearchError = ref('')
const submitError = ref('')
const successMessage = ref('')
const isSearching = ref(false)
const isSubmitting = ref(false)

const { studentProfile, profileError, fetchStudentProfile, fetchStudentCourses, fetchStudentSchedule } = useStudentService()
const { courses, loading: coursesLoading, fetchCourses, courseGroups, courseGroupsLoading, courseGroupsError, fetchCourseGroups } = useCourseService()
const { enrollStudentInCourse } = useSecretaryService()

const currentCoursesLoading = ref(false)
const currentCoursesError = ref('')
const currentScheduleLoading = ref(false)
const currentScheduleError = ref('')

const enrolledCourses = computed(() => studentProfile.value?.courses ?? [])
const scheduleEntries = computed(() => studentProfile.value?.schedule ?? [])

const availableCourses = computed(() => courses.value || [])
const DAY_LABELS: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  'MIÉRCOLES': 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  'SÁBADO': 'Sábado'
}

const normalizeDay = (value?: string | null) => value?.trim().toUpperCase() ?? ''
const formatDayLabel = (value?: string | null) => DAY_LABELS[normalizeDay(value)] ?? (value ?? 'Día sin definir')
const toMinutes = (value?: string | null) => {
  if (!value) {
    return null
  }
  const [hours, minutes] = value.split(':')
  const h = Number(hours)
  const m = Number(minutes)
  if (!Number.isFinite(h) || !Number.isFinite(m)) {
    return null
  }
  return h * 60 + m
}

const normalizedStudentSchedule = computed(() => scheduleEntries.value
  .map(entry => ({
    label: `${entry.courseName} (${entry.courseType})`,
    normalizedDay: normalizeDay(entry.dayOfWeek),
    startMinutes: toMinutes(entry.startTime),
    endMinutes: toMinutes(entry.endTime)
  }))
  .filter(block => block.normalizedDay && block.startMinutes !== null && block.endMinutes !== null)
)

type GroupScheduleBlock = {
  key: string
  typeLabel: string
  displayDay: string
  normalizedDay: string
  startTime: string
  endTime: string
}

type CourseGroupSelectionOption = {
  letter: string
  groups: CourseGroupSummary[]
  scheduleSlots: GroupScheduleBlock[]
  hasConflict: boolean
  conflictDetails: string[]
}

const detectConflicts = (slots: GroupScheduleBlock[]) => {
  const conflicts: string[] = []
  slots.forEach(slot => {
    const slotStart = toMinutes(slot.startTime)
    const slotEnd = toMinutes(slot.endTime)
    if (!slot.normalizedDay || slotStart === null || slotEnd === null) {
      return
    }
    normalizedStudentSchedule.value.forEach(existing => {
      if (!existing || existing.normalizedDay !== slot.normalizedDay) {
        return
      }
      if (existing.startMinutes === null || existing.endMinutes === null) {
        return
      }
      const overlaps = Math.max(slotStart, existing.startMinutes) < Math.min(slotEnd, existing.endMinutes)
      if (overlaps) {
        conflicts.push(`${slot.typeLabel} ${slot.displayDay} ${slot.startTime}-${slot.endTime} se cruza con ${existing.label}`)
      }
    })
  })
  return conflicts
}

const groupSelectionOptions = computed<CourseGroupSelectionOption[]>(() => {
  if (!selectedCourseId.value) {
    return []
  }

  const groups = (courseGroups.value || []).filter(group => group.type !== 'LAB')
  if (!groups.length) {
    return []
  }

  const bucket = new Map<string, CourseGroupSelectionOption>()
  groups.forEach(group => {
    const letter = (group.letter ?? '').trim().toUpperCase() || '-'
    let option = bucket.get(letter)
    if (!option) {
      option = {
        letter,
        groups: [],
        scheduleSlots: [],
        hasConflict: false,
        conflictDetails: []
      }
      bucket.set(letter, option)
    }
    option.groups.push(group)
    const slots = (group.scheduleSlots ?? []).map((slot: CourseScheduleSlotSummary, index) => ({
      key: `${group.groupId}-${slot.scheduleId ?? index}-${group.type}`,
      typeLabel: group.typeLabel,
      displayDay: formatDayLabel(slot.dayOfWeek),
      normalizedDay: normalizeDay(slot.dayOfWeek),
      startTime: slot.startTime ?? '',
      endTime: slot.endTime ?? ''
    }))
    option.scheduleSlots.push(...slots)
  })

  return Array.from(bucket.values())
    .map(option => {
      const conflicts = detectConflicts(option.scheduleSlots)
      return {
        ...option,
        hasConflict: conflicts.length > 0,
        conflictDetails: conflicts
      }
    })
    .sort((a, b) => a.letter.localeCompare(b.letter))
})

const selectedLetterOption = computed(() => {
  if (!selectedGroupLetter.value) {
    return null
  }
  return groupSelectionOptions.value.find(option => option.letter === selectedGroupLetter.value) ?? null
})

const selectedGroupIds = computed(() => selectedLetterOption.value
  ? selectedLetterOption.value.groups
      .map(group => group.groupId)
      .filter((id): id is number => typeof id === 'number')
  : [])

const selectedLetterConflicts = computed(() => selectedLetterOption.value?.conflictDetails ?? [])
const hasBlockingConflicts = computed(() => selectedLetterConflicts.value.length > 0)

watch(groupSelectionOptions, options => {
  if (!selectedCourseId.value) {
    selectedGroupLetter.value = ''
    return
  }
  if (!options.length) {
    selectedGroupLetter.value = ''
    return
  }
  if (selectedGroupLetter.value) {
    const current = options.find(option => option.letter === selectedGroupLetter.value && !option.hasConflict)
    if (current) {
      return
    }
  }
  const nextOption = options.find(option => !option.hasConflict) ?? options[0]
  selectedGroupLetter.value = nextOption ? nextOption.letter : ''
})

const selectedCourse = computed(() => {
  if (!selectedCourseId.value) {
    return null
  }
  const courseId = Number(selectedCourseId.value)
  return courses.value.find(course => course.courseId === courseId) || null
})

const canSubmit = computed(() => Boolean(studentProfile.value && selectedGroupIds.value.length && !isSubmitting.value && !hasBlockingConflicts.value))

const onCourseChange = async () => {
  selectedGroupLetter.value = ''
  if (selectedCourseId.value) {
    await fetchCourseGroups(Number(selectedCourseId.value))
  }
}

const loadAcademicData = async (cui: string) => {
  if (!cui) {
    return
  }

  currentCoursesError.value = ''
  currentScheduleError.value = ''
  currentCoursesLoading.value = true
  currentScheduleLoading.value = true

  const coursePromise = (async () => {
    try {
      await fetchStudentCourses(cui)
    } catch (error) {
      currentCoursesError.value = error instanceof Error
        ? error.message
        : 'No se pudieron cargar los cursos matriculados'
    } finally {
      currentCoursesLoading.value = false
    }
  })()

  const schedulePromise = (async () => {
    try {
      await fetchStudentSchedule(cui)
    } catch (error) {
      currentScheduleError.value = error instanceof Error
        ? error.message
        : 'No se pudo cargar el horario del estudiante'
    } finally {
      currentScheduleLoading.value = false
    }
  })()

  await Promise.all([coursePromise, schedulePromise])
}

const searchStudent = async () => {
  studentSearchError.value = ''
  submitError.value = ''
  successMessage.value = ''

  if (!studentCui.value.trim()) {
    studentSearchError.value = 'Ingresa el CUI del estudiante'
    return
  }

  isSearching.value = true
  try {
    const cui = studentCui.value.trim()
    await fetchStudentProfile(cui)
    if (profileError.value) {
      studentSearchError.value = profileError.value
      studentProfile.value = null
      return
    }
    await loadAcademicData(cui)
  } catch (error) {
    console.error('Error buscando estudiante:', error)
    studentSearchError.value = 'No se pudo cargar el estudiante'
  } finally {
    isSearching.value = false
  }
}

const handleSubmit = async () => {
  submitError.value = ''
  successMessage.value = ''

  if (!canSubmit.value || !studentProfile.value || !selectedGroupIds.value.length) {
    submitError.value = 'Selecciona un estudiante y un grupo de teoría/práctica disponible'
    return
  }

  const userId = studentProfile.value.student?.userId
  const cui = studentProfile.value.student?.cui || studentCui.value.trim()

  if (!userId && !cui) {
    submitError.value = 'No se pudo reconocer al estudiante'
    return
  }

  isSubmitting.value = true
  try {
    await enrollStudentInCourse({
      studentId: userId || undefined,
      studentCui: cui,
      courseGroupIds: selectedGroupIds.value
    })
    const groupSummary = selectedLetterOption.value?.groups
      .map(group => `${group.typeLabel} ${group.letter}`)
      .join(' y ')
    successMessage.value = `Se matriculó correctamente al estudiante en ${selectedCourse.value?.name ?? 'el curso'} (${groupSummary ?? 'sin detalle'})`
    await fetchStudentProfile(cui)
    await loadAcademicData(cui)
  } catch (error) {
    console.error('Error matriculando estudiante:', error)
    submitError.value = error instanceof Error ? error.message : 'No se pudo matricular al estudiante'
  } finally {
    isSubmitting.value = false
  }
}

const resetForm = () => {
  studentCui.value = ''
  selectedCourseId.value = ''
  selectedGroupLetter.value = ''
  studentSearchError.value = ''
  submitError.value = ''
  successMessage.value = ''
  currentCoursesError.value = ''
  currentScheduleError.value = ''
}

onMounted(async () => {
  if (!courses.value.length) {
    await fetchCourses()
  }
})
</script>

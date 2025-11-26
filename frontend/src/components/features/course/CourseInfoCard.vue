<template>
  <section class="space-y-6">
    <div class="bg-white shadow rounded-lg p-6 space-y-5">
      <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
        <div class="space-y-2">
          <div class="flex items-center gap-3 flex-wrap">
            <h2 class="text-2xl font-semibold text-gray-800">{{ details.name }}</h2>
            <span
              class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
              :class="badgeClass"
            >
              {{ details.courseTypeLabel }}
            </span>
          </div>
          <p class="text-gray-600 text-sm">
            Código: <span class="font-medium text-gray-900">{{ details.courseCode ?? 'Sin código' }}</span>
          </p>
        </div>
        <div class="flex flex-col gap-2 sm:flex-row">
          <a
            :href="syllabusDownloadUrl || undefined"
            target="_blank"
            rel="noopener"
            class="px-4 py-2 rounded-md text-center text-sm font-semibold transition"
            :class="syllabusDownloadUrl
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-gray-200 text-gray-500 cursor-not-allowed pointer-events-none'"
          >
            Descargar sílabo
          </a>
          <button
            type="button"
            @click="router.push({ name: 'student-attendance', params: { courseId: details.courseId } })"
            class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition text-sm"
          >
            Ver Asistencia
          </button>
          <button
            v-if="details.labCourse"
            type="button"
            @click="openLabCourse"
            class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition text-sm"
          >
            Ver laboratorio asociado
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div class="bg-blue-50 rounded-md p-4">
          <p class="text-xs uppercase tracking-wide text-blue-600 font-semibold">Créditos</p>
          <p class="text-lg font-semibold text-blue-900">{{ details.creditNumber }}</p>
        </div>
        <div class="bg-slate-50 rounded-md p-4">
          <p class="text-xs uppercase tracking-wide text-slate-600 font-semibold">Grupo</p>
          <p class="text-lg font-semibold text-slate-900">{{ details.groupLetter || 'Sin grupo' }}</p>
        </div>
        <div class="bg-emerald-50 rounded-md p-4">
          <p class="text-xs uppercase tracking-wide text-emerald-600 font-semibold">Año académico</p>
          <p class="text-lg font-semibold text-emerald-900">{{ details.anio ?? 'No definido' }}</p>
        </div>
        <div class="bg-indigo-50 rounded-md p-4">
          <p class="text-xs uppercase tracking-wide text-indigo-600 font-semibold">Estudiantes</p>
          <p class="text-lg font-semibold text-indigo-900">{{ details.enrolledCount }}</p>
        </div>
        <div class="bg-amber-50 rounded-md p-4">
          <p class="text-xs uppercase tracking-wide text-amber-600 font-semibold">Docentes asignados</p>
          <p class="text-lg font-semibold text-amber-900">{{ teacherCountLabel }}</p>
        </div>
        <div class="bg-gray-50 rounded-md p-4" v-if="details.labPrerequisiteCourseId">
          <p class="text-xs uppercase tracking-wide text-gray-600 font-semibold">Laboratorio vinculado</p>
          <p class="text-lg font-semibold text-gray-900">
            {{ details.labCourse?.name ?? `Curso #${details.labPrerequisiteCourseId}` }}
          </p>
        </div>
      </div>
    </div>

    <div class="bg-white shadow rounded-lg p-6 space-y-4">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 class="text-lg font-semibold text-gray-800">Grupos del curso</h3>
          <p class="text-sm text-gray-500">Revisa teoría, práctica y laboratorio asignados.</p>
        </div>
        <span v-if="groupCountLabel" class="text-sm text-gray-500">{{ groupCountLabel }}</span>
      </div>

      <p v-if="groupsLoading" class="text-sm text-blue-600">Cargando grupos...</p>
      <p v-else-if="groupsError" class="text-sm text-red-600">{{ groupsError }}</p>
      <p v-else-if="!sortedGroups.length" class="text-sm text-gray-500">Aún no hay grupos configurados para este curso.</p>

      <div v-else class="grid gap-4 sm:grid-cols-2">
        <article
          v-for="group in sortedGroups"
          :key="group.groupId ?? `${group.type}-${group.letter}`"
          class="rounded-xl border border-gray-200 p-4 space-y-3"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm font-semibold text-gray-900">
                {{ group.typeLabel || resolveGroupTypeLabel(group.type) }} {{ group.letter || '-' }}
              </p>
              <p class="text-xs text-gray-500">{{ formatCapacityLabel(group) }}</p>
              <p class="text-[11px] text-gray-400">{{ teacherStatusLabel(group.teacherId) }}</p>
            </div>
            <span
              class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold"
              :class="groupBadgeClass(group.type)"
            >
              {{ resolveGroupTypeLabel(group.type) }}
            </span>
          </div>

          <div>
            <p class="text-[11px] font-semibold uppercase tracking-wide text-gray-600">Horario</p>
            <ul v-if="group.scheduleSlots?.length" class="mt-2 space-y-1 text-xs text-gray-700">
              <li
                v-for="slot in sortScheduleSlots(group.scheduleSlots)"
                :key="slot.scheduleId ?? `${slot.dayOfWeek}-${slot.startTime}-${slot.endTime}-${slot.classroomId}`"
              >
                {{ formatScheduleSlot(slot) }}
              </li>
            </ul>
            <p v-else class="mt-2 text-xs text-gray-500">Sin horario definido.</p>
          </div>
        </article>
      </div>
    </div>

    <div class="bg-white shadow rounded-lg p-6 space-y-4" v-if="details.syllabus">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-800">Temario del curso</h3>
        <span class="text-sm text-gray-500">{{ details.syllabus.topics.length }} temas</span>
      </div>

      <div v-if="!details.syllabus.topics.length" class="text-sm text-gray-500">
        No se registraron temas en el sílabo.
      </div>
      <ul v-else class="space-y-3">
        <li
          v-for="topic in details.syllabus.topics"
          :key="`${topic.name}-${topic.sessionDate ?? topic.weight ?? topic.status}`"
          class="rounded-md border border-gray-100 bg-gray-50 px-4 py-3 space-y-2"
        >
          <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm font-semibold text-gray-800">{{ topic.name }}</p>
              <p class="text-xs text-gray-500">{{ formatTopicDate(topic.sessionDate) }}</p>
            </div>
            <div class="flex flex-wrap items-center gap-2">
              <span
                v-if="topic.weight != null"
                class="inline-flex items-center rounded-full bg-white px-3 py-1 text-xs font-semibold text-gray-600"
              >
                Peso {{ topic.weight }}%
              </span>
              <span
                class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold"
                :class="statusBadgeClasses(topic.status)"
              >
                {{ statusLabel(topic.status) }}
              </span>
            </div>
          </div>

          <details
            v-if="topic.status === 'COMPLETED'"
            class="rounded-md bg-white/70 px-3 py-2 text-xs text-gray-600"
          >
            <summary class="cursor-pointer select-none text-gray-700">
              Ver más detalles
            </summary>
            <p class="mt-2">
              Este tema se completó el {{ formatTopicDate(topic.sessionDate) }}. Revisa el sílabo descargado para repasar materiales adicionales.
            </p>
          </details>
        </li>
      </ul>

    </div>

    <div class="bg-white shadow rounded-lg p-6 space-y-4">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-gray-800">Estudiantes matriculados</h3>
        <span class="text-sm text-gray-500">{{ details.enrolledCount }} estudiantes</span>
      </div>

      <div v-if="!details.enrolledStudents.length" class="text-sm text-gray-500">
        No hay estudiantes matriculados en este curso.
      </div>

      <div v-else class="space-y-3">
        <select
          v-model="selectedStudentId"
          class="w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-200"
        >
          <option value="">Selecciona un estudiante</option>
          <option
            v-for="student in details.enrolledStudents"
            :key="student.userId || student.cui"
            :value="student.userId"
          >
            {{ formatStudentName(student) }}
          </option>
        </select>

        <div
          v-if="selectedStudent"
          class="rounded-md bg-gray-50 p-4 text-sm text-gray-700 space-y-1"
        >
          <p><span class="font-semibold">CUI:</span> {{ selectedStudent.cui || 'No registrado' }}</p>
          <p><span class="font-semibold">User ID:</span> {{ selectedStudent.userId }}</p>
          <p><span class="font-semibold">Correo:</span> {{ selectedStudent.institutionalEmail || 'No registrado' }}</p>
          <p><span class="font-semibold">Año:</span> {{ selectedStudent.enrollmentYear ?? 'Sin dato' }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type {
  CourseDetails,
  CourseGroupSummary,
  CourseScheduleSlotSummary,
  CourseStudentSummary,
  CourseTopicSummary,
  CourseType
} from '@/services/courseService'
import { syllabusService } from '@/services/syllabusService'

const props = withDefaults(defineProps<{
  details: CourseDetails
  groups?: CourseGroupSummary[]
  groupsLoading?: boolean
  groupsError?: string
}>(), {
  groups: () => [],
  groupsLoading: false,
  groupsError: ''
})
const emit = defineEmits<{ (e: 'open-lab', courseId: number): void }>()
const router = useRouter()

const selectedStudentId = ref<number | null>(null)

const selectedStudent = computed(() =>
  props.details.enrolledStudents.find(student => student.userId === selectedStudentId.value)
)

watch(
  () => props.details.courseId,
  () => {
    selectedStudentId.value = null
  }
)

const groupsLoading = computed(() => props.groupsLoading)
const groupsError = computed(() => props.groupsError ?? '')

const COURSE_TYPE_ORDER: Record<CourseType, number> = {
  THEORY: 0,
  PRACTICE: 1,
  LAB: 2
}

const COURSE_TYPE_BADGE_CLASSES: Record<CourseType, string> = {
  THEORY: 'bg-blue-100 text-blue-700',
  PRACTICE: 'bg-amber-100 text-amber-800',
  LAB: 'bg-purple-100 text-purple-700'
}

const COURSE_TYPE_LABELS: Record<CourseType, string> = {
  THEORY: 'Teoría',
  PRACTICE: 'Práctica',
  LAB: 'Laboratorio'
}

const sortedGroups = computed(() => {
  const groups = props.groups ?? []
  return [...groups].sort((a, b) => {
    const typeOrder = (COURSE_TYPE_ORDER[a.type] ?? 99) - (COURSE_TYPE_ORDER[b.type] ?? 99)
    if (typeOrder !== 0) {
      return typeOrder
    }
    return a.letter.localeCompare(b.letter)
  })
})

const groupCountLabel = computed(() => {
  const count = sortedGroups.value.length
  if (!count) {
    return ''
  }
  return `${count} grupo${count === 1 ? '' : 's'}`
})

const formatStudentName = (student: CourseStudentSummary) =>
  [student.firstNames, student.paternalSurname, student.maternalSurname]
    .filter(Boolean)
    .join(' ')

const badgeClass = computed(() =>
  props.details.courseType === 'LAB'
    ? 'bg-purple-100 text-purple-700'
    : 'bg-blue-100 text-blue-700'
)

const resolveGroupTypeLabel = (type?: CourseType) => {
  if (!type) {
    return COURSE_TYPE_LABELS.THEORY
  }
  return COURSE_TYPE_LABELS[type] ?? COURSE_TYPE_LABELS.THEORY
}

const groupBadgeClass = (type?: CourseType) => {
  if (!type) {
    return COURSE_TYPE_BADGE_CLASSES.THEORY
  }
  return COURSE_TYPE_BADGE_CLASSES[type] ?? COURSE_TYPE_BADGE_CLASSES.THEORY
}

const formatCapacityLabel = (group: CourseGroupSummary) => {
  if (group.maxCapacity == null) {
    return 'Capacidad no definida'
  }
  if (group.availableCapacity == null) {
    return `${group.maxCapacity} cupos totales`
  }
  return `${group.maxCapacity} cupos · ${group.availableCapacity} libres`
}

const teacherStatusLabel = (teacherId: CourseGroupSummary['teacherId']) =>
  teacherId ? 'Docente asignado' : 'Docente pendiente'

const DAY_OF_WEEK_LABELS: Record<string, string> = {
  MONDAY: 'Lunes',
  TUESDAY: 'Martes',
  WEDNESDAY: 'Miércoles',
  THURSDAY: 'Jueves',
  FRIDAY: 'Viernes',
  SATURDAY: 'Sábado',
  SUNDAY: 'Domingo'
}

const DAY_OF_WEEK_ORDER: Record<string, number> = {
  MONDAY: 0,
  TUESDAY: 1,
  WEDNESDAY: 2,
  THURSDAY: 3,
  FRIDAY: 4,
  SATURDAY: 5,
  SUNDAY: 6
}

const normalizeDay = (day?: string) => (typeof day === 'string' ? day.toUpperCase() : '')

const resolveDayLabel = (day?: string) => {
  const normalized = normalizeDay(day)
  return DAY_OF_WEEK_LABELS[normalized] ?? day ?? 'Día no definido'
}

const sortScheduleSlots = (slots?: CourseScheduleSlotSummary[] | null): CourseScheduleSlotSummary[] => {
  if (!Array.isArray(slots)) {
    return []
  }
  return [...slots].sort((a, b) => {
    const dayOrder = (DAY_OF_WEEK_ORDER[normalizeDay(a.dayOfWeek)] ?? 7) - (DAY_OF_WEEK_ORDER[normalizeDay(b.dayOfWeek)] ?? 7)
    if (dayOrder !== 0) {
      return dayOrder
    }
    const startDiff = (a.startTime ?? '').localeCompare(b.startTime ?? '')
    if (startDiff !== 0) {
      return startDiff
    }
    return (a.endTime ?? '').localeCompare(b.endTime ?? '')
  })
}

const formatTime = (time?: string | null) => {
  if (!time) {
    return '--:--'
  }
  return time.slice(0, 5)
}

const formatScheduleSlot = (slot: CourseScheduleSlotSummary) => {
  const dayLabel = resolveDayLabel(slot.dayOfWeek)
  const start = formatTime(slot.startTime)
  const end = formatTime(slot.endTime)
  const classroom = slot.classroomId ? ` · Aula ${slot.classroomId}` : ''
  return `${dayLabel} ${start} - ${end}${classroom}`
}

const teacherCountLabel = computed(() => {
  const count = props.details.teacherIds.length
  if (!count) {
    return 'Sin docentes asignados'
  }
  return `${count} docente${count === 1 ? '' : 's'}`
})

const syllabusDownloadUrl = computed(() => {
  const syllabusId = props.details.syllabus?.syllabusId ?? props.details.syllabusId
  return syllabusService.buildDownloadUrl(syllabusId ?? undefined)
})

const formatTopicDate = (isoDate: string | null) => {
  if (!isoDate) {
    return 'Sin fecha programada'
  }
  const date = new Date(isoDate)
  if (Number.isNaN(date.getTime())) {
    return isoDate
  }
  return date.toLocaleDateString('es-PE', {
    weekday: 'short',
    day: 'numeric',
    month: 'short'
  })
}

const statusBadgeClasses = (status: CourseTopicSummary['status']) => {
  switch (status) {
    case 'COMPLETED':
      return 'bg-green-100 text-green-700'
    case 'TODAY':
      return 'bg-blue-100 text-blue-700'
    case 'UPCOMING':
      return 'bg-amber-100 text-amber-800'
    default:
      return 'bg-gray-200 text-gray-600'
  }
}

const statusLabel = (status: CourseTopicSummary['status']) => {
  switch (status) {
    case 'COMPLETED':
      return 'Completado'
    case 'TODAY':
      return 'Sesión de hoy'
    case 'UPCOMING':
      return 'Próximo'
    default:
      return 'Sin fecha'
  }
}

const openLabCourse = () => {
  if (!props.details.labCourse) {
    return
  }
  emit('open-lab', props.details.labCourse.courseId)
}
</script>

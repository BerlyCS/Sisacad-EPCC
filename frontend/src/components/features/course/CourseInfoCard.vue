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
import type { CourseDetails, CourseStudentSummary, CourseTopicSummary } from '@/services/courseService'
import { syllabusService } from '@/services/syllabusService'

const props = defineProps<{ details: CourseDetails }>()
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

const formatStudentName = (student: CourseStudentSummary) =>
  [student.firstNames, student.paternalSurname, student.maternalSurname]
    .filter(Boolean)
    .join(' ')

const badgeClass = computed(() =>
  props.details.courseType === 'LAB'
    ? 'bg-purple-100 text-purple-700'
    : 'bg-blue-100 text-blue-700'
)

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

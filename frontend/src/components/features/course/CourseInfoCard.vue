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
          <button
            type="button"
            class="px-4 py-2 bg-gray-200 text-gray-600 rounded-md cursor-not-allowed text-sm"
            disabled
          >
            Descargar sílabo
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
      <ul v-else class="space-y-2">
        <li
          v-for="topic in details.syllabus.topics"
          :key="`${topic.name}-${topic.weight}`"
          class="flex items-center justify-between rounded-md bg-gray-50 px-3 py-2"
        >
          <span class="text-gray-700">{{ topic.name }}</span>
          <span v-if="topic.weight != null" class="text-sm text-gray-500">{{ topic.weight }}%</span>
        </li>
      </ul>

      <div v-if="details.syllabus.content" class="text-xs text-gray-500">
        Archivo relacionado: <span class="font-medium">{{ details.syllabus.content.name || 'Sin archivo' }}</span>
        <span v-if="details.syllabus.content.type"> ({{ details.syllabus.content.type }})</span>
      </div>
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
            :key="student.documentoIdentidad || student.cui"
            :value="student.documentoIdentidad"
          >
            {{ formatStudentName(student) }}
          </option>
        </select>

        <div
          v-if="selectedStudent"
          class="rounded-md bg-gray-50 p-4 text-sm text-gray-700 space-y-1"
        >
          <p><span class="font-semibold">CUI:</span> {{ selectedStudent.cui || 'No registrado' }}</p>
          <p><span class="font-semibold">Documento:</span> {{ selectedStudent.documentoIdentidad }}</p>
          <p><span class="font-semibold">Correo:</span> {{ selectedStudent.correoInstitucional || 'No registrado' }}</p>
          <p><span class="font-semibold">Año:</span> {{ selectedStudent.anio ?? 'Sin dato' }}</p>
        </div>
        <p v-else class="text-xs text-gray-500">Selecciona un estudiante para ver sus datos de contacto.</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { CourseDetails, CourseStudentSummary } from '@/services/courseService'

const props = defineProps<{ details: CourseDetails }>()
const emit = defineEmits<{ (e: 'open-lab', courseId: number): void }>()

const selectedStudentId = ref('')

const selectedStudent = computed(() =>
  props.details.enrolledStudents.find(student => student.documentoIdentidad === selectedStudentId.value)
)

watch(
  () => props.details.courseId,
  () => {
    selectedStudentId.value = ''
  }
)

const formatStudentName = (student: CourseStudentSummary) =>
  [student.nombres, student.apellidoPaterno, student.apellidoMaterno]
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

const openLabCourse = () => {
  if (!props.details.labCourse) {
    return
  }
  emit('open-lab', props.details.labCourse.courseId)
}
</script>

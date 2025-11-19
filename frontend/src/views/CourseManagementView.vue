<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Gestión académica</p>
          <h2 class="text-xl font-semibold text-gray-800">Gestión de Cursos</h2>
          <p class="text-gray-600 mt-1">Administra los cursos y asigna docentes responsables.</p>
        </div>
        <p v-if="professorsError" class="text-sm text-red-600">{{ professorsError }}</p>
      </div>

      <div class="p-6">
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando cursos...</p>
        </div>

        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button
            @click="fetchCourses"
            class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
          >
            Reintentar
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Créditos</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Grupo</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase"># Estudiantes</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Docentes asignados</th>
                <th v-if="canAssignProfessors" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Acciones</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="course in courses" :key="course.courseId" class="hover:bg-gray-50">
                <td class="px-6 py-4 text-sm font-mono text-gray-900">{{ course.courseId }}</td>
                <td class="px-6 py-4 text-sm text-gray-900">{{ course.name }}</td>
                <td class="px-6 py-4 text-sm text-blue-700 font-semibold">
                  {{ course.creditNumber }} créditos
                </td>
                <td class="px-6 py-4 text-sm text-purple-700">
                  Grupo {{ course.groupLetter || '-' }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">
                  {{ course.enrolledStudentIDs.length }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">
                  <div class="flex flex-wrap gap-2">
                    <span
                      v-for="professorId in course.teacherIDs"
                      :key="`${course.courseId}-${professorId}`"
                      class="inline-flex items-center gap-1 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700"
                    >
                      {{ resolveProfessorName(professorId) }}
                    </span>
                    <span v-if="course.teacherIDs.length === 0" class="text-xs text-gray-400">Sin docentes asignados</span>
                  </div>
                </td>
                <td v-if="canAssignProfessors" class="px-6 py-4 text-sm">
                  <button
                    class="inline-flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:border-blue-400 hover:text-blue-600"
                    @click="openAssignmentModal(course.courseId)"
                  >
                    Gestionar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="courses.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron cursos registrados.</p>
          </div>
        </div>

        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Cursos</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ courses.length }}</p>
          </div>
          <div class="bg-green-50 border border-green-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-green-800">Créditos Totales</h3>
            <p class="text-3xl font-bold text-green-600 mt-2">
              {{ courses.reduce((total, course) => total + (course.creditNumber || 0), 0) }}
            </p>
          </div>
          <div class="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-purple-800">Total Estudiantes Inscritos</h3>
            <p class="text-3xl font-bold text-purple-600 mt-2">
              {{ courses.reduce((total, course) => total + course.enrolledStudentIDs.length, 0) }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showAssignmentModal && selectedCourse"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4"
    >
      <div class="w-full max-w-2xl rounded-2xl bg-white shadow-2xl">
        <div class="flex items-start justify-between border-b px-6 py-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Gestión de docentes</p>
            <h3 class="text-xl font-semibold text-gray-900">{{ selectedCourse.name }}</h3>
            <p class="text-sm text-gray-500">
              Grupo {{ selectedCourse.groupLetter || '-' }} · {{ selectedCourse.creditNumber }} créditos
            </p>
          </div>
          <button
            class="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            @click="closeAssignmentModal"
          >
            <span class="sr-only">Cerrar</span>
            ✕
          </button>
        </div>

        <div class="px-6 py-5 space-y-6">
          <div>
            <h4 class="text-sm font-semibold text-gray-700">Docentes asignados</h4>
            <div v-if="assignedProfessors.length > 0" class="mt-3 space-y-3">
              <div
                v-for="professor in assignedProfessors"
                :key="professor.id"
                class="flex items-center justify-between rounded-xl border border-gray-200 px-4 py-3"
              >
                <div>
                  <p class="font-semibold text-gray-900">{{ professorFullName(professor) }}</p>
                  <p class="text-xs text-gray-500">{{ professor.correo }}</p>
                </div>
                <button
                  class="text-sm font-medium text-red-600 hover:text-red-700"
                  :disabled="assignmentLoading"
                  @click="handleRemoveProfessor(professor.id)"
                >
                  Quitar
                </button>
              </div>
            </div>
            <p v-else class="mt-3 text-sm text-gray-500">Este curso aún no tiene docentes asignados.</p>
          </div>

          <div class="border-t pt-4">
            <h4 class="text-sm font-semibold text-gray-700">Asignar nuevo docente</h4>
            <div class="mt-2 space-y-3">
              <select
                v-model.number="selectedProfessorId"
                class="w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                :disabled="availableProfessors.length === 0 || professorsLoading || assignmentLoading"
              >
                <option :value="null" disabled>Selecciona un docente</option>
                <option
                  v-for="professor in availableProfessors"
                  :key="professor.id"
                  :value="professor.id"
                >
                  {{ professorFullName(professor) }}
                </option>
              </select>
              <p v-if="availableProfessors.length === 0" class="text-xs text-gray-500">
                Todos los docentes registrados ya están asignados a este curso.
              </p>
              <button
                class="w-full rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                :disabled="assignmentLoading || !selectedProfessorId || availableProfessors.length === 0"
                @click="handleAssignProfessor"
              >
                {{ assignmentLoading ? 'Guardando...' : 'Asignar docente' }}
              </button>
            </div>
          </div>

          <p v-if="assignmentError" class="text-sm text-red-600">{{ assignmentError }}</p>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watchEffect } from 'vue'
import { storeToRefs } from 'pinia'
import AdminLayout from '../components/ui/TopBar.vue'
import { useCourseService } from '../services/courseService'
import { useProfessorService } from '@/services/professorService'
import { useAuthStore } from '@/stores/auth'
import type { Professor } from '@/services/professorService'

const authStore = useAuthStore()
const { isAdmin, isSecretary } = storeToRefs(authStore)

const {
  courses,
  loading,
  error,
  fetchCourses,
  assignProfessorToCourse,
  removeProfessorFromCourse
} = useCourseService()

const {
  professors,
  loading: professorsLoading,
  error: professorsError,
  fetchProfessors
} = useProfessorService()

const showAssignmentModal = ref(false)
const selectedCourseId = ref<number | null>(null)
const selectedProfessorId = ref<number | null>(null)
const assignmentError = ref('')
const assignmentLoading = ref(false)

const professorDirectory = computed(() => {
  const directory = new Map<number, Professor>()
  professors.value.forEach(professor => {
    directory.set(professor.id, professor)
  })
  return directory
})

const selectedCourse = computed(() => {
  if (selectedCourseId.value == null) {
    return null
  }
  return courses.value.find(course => course.courseId === selectedCourseId.value) ?? null
})

const assignedProfessors = computed(() => {
  if (!selectedCourse.value) {
    return []
  }
  return selectedCourse.value.teacherIDs
    .map(id => professorDirectory.value.get(id))
    .filter((professor): professor is Professor => Boolean(professor))
})

const availableProfessors = computed(() => {
  if (!selectedCourse.value) {
    return []
  }
  const assignedIds = new Set(selectedCourse.value.teacherIDs)
  return professors.value.filter(professor => !assignedIds.has(professor.id))
})

const canAssignProfessors = computed(() => isAdmin.value || isSecretary.value)

const professorFullName = (professor: Professor) => {
  return [professor.nombres, professor.apellidoPaterno, professor.apellidoMaterno]
    .filter(Boolean)
    .join(' ')
}

const resolveProfessorName = (professorId: number) => {
  const professor = professorDirectory.value.get(professorId)
  if (!professor) {
    return `Docente #${professorId}`
  }
  return professorFullName(professor)
}

const openAssignmentModal = (courseId: number) => {
  selectedCourseId.value = courseId
  assignmentError.value = ''
  showAssignmentModal.value = true
}

const closeAssignmentModal = () => {
  showAssignmentModal.value = false
  selectedCourseId.value = null
  assignmentError.value = ''
}

const handleAssignProfessor = async () => {
  if (!selectedCourse.value || !selectedProfessorId.value) {
    return
  }

  assignmentLoading.value = true
  assignmentError.value = ''

  try {
    await assignProfessorToCourse(selectedCourse.value.courseId, selectedProfessorId.value)
    await fetchCourses()
    selectedProfessorId.value = null
  } catch (err) {
    assignmentError.value = err instanceof Error ? err.message : 'No se pudo asignar el docente'
  } finally {
    assignmentLoading.value = false
  }
}

const handleRemoveProfessor = async (professorId: number) => {
  if (!selectedCourse.value) {
    return
  }

  assignmentLoading.value = true
  assignmentError.value = ''

  try {
    await removeProfessorFromCourse(selectedCourse.value.courseId, professorId)
    await fetchCourses()
  } catch (err) {
    assignmentError.value = err instanceof Error ? err.message : 'No se pudo actualizar el curso'
  } finally {
    assignmentLoading.value = false
  }
}

watchEffect(() => {
  if (!showAssignmentModal.value) {
    selectedProfessorId.value = null
    return
  }

  if (!selectedProfessorId.value && availableProfessors.value.length > 0) {
    const nextProfessor = availableProfessors.value[0]
    selectedProfessorId.value = nextProfessor ? nextProfessor.id : null
  }
})

onMounted(() => {
  fetchCourses()
  fetchProfessors()
})
</script>

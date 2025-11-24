<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Gestión académica</p>
          <h2 class="text-xl font-semibold text-gray-800">Gestión de Cursos</h2>
          <p class="text-gray-600 mt-1">Administra los cursos y asigna docentes responsables.</p>
        </div>
        <div v-if="canAddCourses" class="flex gap-2">
          <button
            @click="$router.push('/admin/courses/add')"
            class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
          >
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Agregar Curso
          </button>
        </div>
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
                  {{ course.credits }} créditos
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">
                  {{ course.enrolledStudentIDs?.length || 0 }}
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
                    <span v-if="(course.teacherIDs?.length || 0) === 0" class="text-xs text-gray-400">Sin docentes asignados</span>
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
              {{ courses.reduce((total, course) => total + (course.credits || 0), 0) }}
            </p>
          </div>
          <div class="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-purple-800">Total Estudiantes Inscritos</h3>
            <p class="text-3xl font-bold text-purple-600 mt-2">
              {{ courses.reduce((total, course) => total + (course.enrolledStudentIDs?.length || 0), 0) }}
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
              {{ selectedCourse.credits }} créditos
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
            <h4 class="text-sm font-semibold text-gray-700">Selecciona el grupo y el tipo</h4>
            <p class="text-xs text-gray-500">Los docentes se asignan individualmente por grupo.</p>
            <p v-if="courseGroupsLoading" class="mt-3 text-sm text-gray-500">Cargando grupos...</p>
            <p v-else-if="courseGroupsError" class="mt-3 text-sm text-red-600">{{ courseGroupsError }}</p>
            <p v-else-if="courseGroups.length === 0" class="mt-3 text-sm text-gray-500">
              Este curso aún no tiene grupos configurados.
            </p>
            <div v-else class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
              <button
                v-for="group in courseGroups"
                :key="group.groupId"
                type="button"
                class="rounded-xl border px-4 py-3 text-left transition"
                :class="[
                  group.groupId === selectedGroupId
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-300'
                ]"
                @click="selectedGroupId = group.groupId"
              >
                <p class="text-sm font-semibold text-gray-900">
                  {{ group.typeLabel }} {{ group.letter }}
                </p>
                <p class="text-xs text-gray-500">
                  {{ group.teacherId ? 'Docente asignado' : 'Sin docente asignado' }}
                </p>
              </button>
            </div>
          </div>

          <div v-if="selectedGroup" class="space-y-6 border-t pt-4">
            <div>
              <h4 class="text-sm font-semibold text-gray-700">
                Docentes asignados para {{ selectedGroupLabel }}
              </h4>
              <div v-if="assignedProfessors.length > 0" class="mt-3 space-y-3">
                <div
                  v-for="professor in assignedProfessors"
                  :key="professor.userId"
                  class="flex items-center justify-between rounded-xl border border-gray-200 px-4 py-3"
                >
                  <div>
                    <p class="font-semibold text-gray-900">{{ professorFullName(professor) }}</p>
                    <p class="text-xs text-gray-500">{{ professor.institutionalEmail }}</p>
                  </div>
                  <button
                    class="text-sm font-medium text-red-600 hover:text-red-700"
                    :disabled="assignmentLoading"
                    @click="handleRemoveProfessor(professor.userId)"
                  >
                    Quitar
                  </button>
                </div>
              </div>
              <p v-else class="mt-3 text-sm text-gray-500">
                Este grupo aún no tiene docentes asignados.
              </p>
            </div>

            <div>
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
                    :key="professor.userId"
                    :value="professor.userId"
                  >
                    {{ professorFullName(professor) }}
                  </option>
                </select>
                <p v-if="availableProfessors.length === 0" class="text-xs text-gray-500">
                  Todos los docentes disponibles ya están asignados a este grupo.
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
          </div>

          <p v-if="assignmentError" class="text-sm text-red-600">{{ assignmentError }}</p>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, watchEffect } from 'vue'
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
  courseGroups,
  courseGroupsLoading,
  courseGroupsError,
  fetchCourseGroups,
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
const selectedGroupId = ref<number | null>(null)
const assignmentError = ref('')
const assignmentLoading = ref(false)

const professorDirectory = computed(() => {
  const directory = new Map<number, Professor>()
  professors.value.forEach(professor => {
    directory.set(professor.userId, professor)
  })
  return directory
})

const selectedCourse = computed(() => {
  if (selectedCourseId.value == null) {
    return null
  }
  return courses.value.find(course => course.courseId === selectedCourseId.value) ?? null
})

const selectedGroup = computed(() => {
  if (!selectedGroupId.value) {
    return null
  }
  return courseGroups.value.find(group => group.groupId === selectedGroupId.value) ?? null
})

const assignedProfessorIds = computed(() => {
  const teacherId = selectedGroup.value?.teacherId
  return teacherId ? [teacherId] : []
})

const selectedGroupLabel = computed(() => {
  if (!selectedGroup.value) {
    return ''
  }
  return `${selectedGroup.value.typeLabel} ${selectedGroup.value.letter}`
})

const assignedProfessors = computed(() => {
  if (!professorDirectory.value) {
    return []
  }
  return assignedProfessorIds.value
    .map(id => professorDirectory.value.get(id))
    .filter((professor): professor is Professor => Boolean(professor))
})

const availableProfessors = computed(() => {
  if (!professors.value.length || !selectedGroup.value) {
    return []
  }
  const assignedIds = new Set(assignedProfessorIds.value)
  if (!assignedIds.size) {
    return professors.value
  }
  return professors.value.filter(professor => !assignedIds.has(professor.userId))
})

const canAssignProfessors = computed(() => isAdmin.value || isSecretary.value)
const canAddCourses = computed(() => isAdmin.value || isSecretary.value)

const professorFullName = (professor: Professor) => {
  return [professor.firstNames, professor.paternalSurname, professor.maternalSurname]
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

const openAssignmentModal = async (courseId: number) => {
  selectedCourseId.value = courseId
  assignmentError.value = ''
  showAssignmentModal.value = true
  selectedGroupId.value = null
  await fetchCourseGroups(courseId)
  if (courseGroups.value.length > 0) {
    const firstGroup = courseGroups.value[0]
    selectedGroupId.value = firstGroup ? firstGroup.groupId : null
  }
}

const closeAssignmentModal = () => {
  showAssignmentModal.value = false
  selectedCourseId.value = null
  selectedGroupId.value = null
  assignmentError.value = ''
}

const handleAssignProfessor = async () => {
  if (!selectedCourse.value || !selectedGroupId.value || !selectedProfessorId.value) {
    return
  }

  assignmentLoading.value = true
  assignmentError.value = ''

  try {
    await assignProfessorToCourse(selectedCourse.value.courseId, selectedGroupId.value, selectedProfessorId.value)
    await fetchCourseGroups(selectedCourse.value.courseId)
    selectedProfessorId.value = null
  } catch (err) {
    assignmentError.value = err instanceof Error ? err.message : 'No se pudo asignar el docente'
  } finally {
    assignmentLoading.value = false
  }
}

const handleRemoveProfessor = async (professorId: number) => {
  if (!selectedCourse.value || !selectedGroupId.value) {
    return
  }

  assignmentLoading.value = true
  assignmentError.value = ''

  try {
    await removeProfessorFromCourse(selectedCourse.value.courseId, selectedGroupId.value, professorId)
    await fetchCourseGroups(selectedCourse.value.courseId)
  } catch (err) {
    assignmentError.value = err instanceof Error ? err.message : 'No se pudo actualizar el curso'
  } finally {
    assignmentLoading.value = false
  }
}

watch(courseGroups, groups => {
  if (!groups.length) {
    selectedGroupId.value = null
    return
  }
  if (!groups.some(group => group.groupId === selectedGroupId.value)) {
    const firstGroup = groups[0]
    selectedGroupId.value = firstGroup ? firstGroup.groupId : null
  }
})

watch([showAssignmentModal, availableProfessors, selectedGroup], ([modalOpen]) => {
  if (!modalOpen) {
    selectedProfessorId.value = null
    return
  }

  if (!selectedProfessorId.value && availableProfessors.value.length > 0) {
    const nextProfessor = availableProfessors.value[0]
    selectedProfessorId.value = nextProfessor ? nextProfessor.userId : null
  }
})

watch(selectedGroupId, () => {
  if (!showAssignmentModal.value) {
    return
  }
  selectedProfessorId.value = null
})

onMounted(() => {
  fetchCourses()
  fetchProfessors()
})
</script>

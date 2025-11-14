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

      <StudentScheduleCard
        v-if="studentProfile"
        :entries="studentProfile.schedule"
        :loading="profileLoading"
        :error="profileError"
        :refreshable="false"
      />

      <div>
        <label class="block text-sm font-medium text-gray-700">Curso</label>
        <div class="mt-1 relative">
          <select
            v-model="selectedCourseId"
            class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 bg-white text-gray-900 appearance-none pr-10"
          >
            <option value="">Selecciona un curso</option>
            <option
              v-for="course in availableCourses"
              :key="course.courseId"
              :value="course.courseId"
            >
              {{ course.name }} - Grupo {{ course.groupLetter || '-' }} ({{ course.courseTypeLabel }})
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
import { computed, onMounted, ref } from 'vue'
import StudentProfileSummary from '@/components/features/student/StudentProfileSummary.vue'
import StudentScheduleCard from '@/components/features/student/StudentScheduleCard.vue'
import { useCourseService } from '@/services/courseService'
import { useSecretaryService } from '@/services/secretaryService'
import { useStudentService } from '@/services/studentService'

const studentCui = ref('')
const selectedCourseId = ref<number | ''>('')
const studentSearchError = ref('')
const submitError = ref('')
const successMessage = ref('')
const isSearching = ref(false)
const isSubmitting = ref(false)

const { studentProfile, profileError, profileLoading, fetchStudentProfile } = useStudentService()
const { courses, loading: coursesLoading, fetchCourses } = useCourseService()
const { enrollStudentInCourse } = useSecretaryService()

const availableCourses = computed(() => courses.value || [])

const selectedCourse = computed(() => {
  if (!selectedCourseId.value) {
    return null
  }
  const courseId = Number(selectedCourseId.value)
  return courses.value.find(course => course.courseId === courseId) || null
})

const canSubmit = computed(() => Boolean(studentProfile.value && selectedCourse.value && !isSubmitting.value))

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
    await fetchStudentProfile(studentCui.value.trim())
    if (profileError.value) {
      studentSearchError.value = profileError.value
    }
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

  if (!canSubmit.value || !studentProfile.value || !selectedCourse.value) {
    submitError.value = 'Selecciona un estudiante y un curso'
    return
  }

  const documentId = studentProfile.value.student?.documentoIdentidad
  const cui = studentProfile.value.student?.cui || studentCui.value.trim()

  if (!documentId && !cui) {
    submitError.value = 'No se pudo reconocer al estudiante'
    return
  }

  isSubmitting.value = true
  try {
    await enrollStudentInCourse({
      studentDocumentoIdentidad: documentId || undefined,
      studentCui: cui,
      courseId: selectedCourse.value.courseId
    })
    successMessage.value = `Se matriculó correctamente al estudiante en ${selectedCourse.value.name}`
    await fetchStudentProfile(cui)
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
  studentSearchError.value = ''
  submitError.value = ''
  successMessage.value = ''
}

onMounted(async () => {
  if (!courses.value.length) {
    await fetchCourses()
  }
})
</script>

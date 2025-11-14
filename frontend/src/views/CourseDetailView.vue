<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <h1 class="text-2xl font-semibold text-gray-800">Información del curso</h1>
        </div>
        <button
          type="button"
          @click="goBack"
          class="inline-flex items-center justify-center rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 transition"
        >
          Volver
        </button>
      </div>

      <div v-if="courseDetailsLoading" class="bg-white shadow rounded-lg p-10 text-center space-y-3">
        <div class="mx-auto h-10 w-10 animate-spin rounded-full border-4 border-blue-200 border-t-blue-600"></div>
        <p class="text-gray-600">Cargando información del curso...</p>
      </div>

      <div v-else-if="courseDetailsError" class="bg-red-50 border border-red-200 rounded-lg p-6 space-y-3">
        <h2 class="text-lg font-semibold text-red-800">No se pudo cargar la información</h2>
        <p class="text-red-700 text-sm">{{ courseDetailsError }}</p>
        <div class="flex gap-2">
          <button
            type="button"
            @click="retry"
            class="px-4 py-2 rounded-md bg-red-600 text-white text-sm hover:bg-red-700 transition"
          >
            Reintentar
          </button>
          <button
            type="button"
            @click="goBack"
            class="px-4 py-2 rounded-md border border-red-200 text-sm text-red-700 hover:bg-red-100 transition"
          >
            Volver al panel
          </button>
        </div>
      </div>

      <CourseInfoCard
        v-else-if="courseDetails"
        :details="courseDetails"
        @open-lab="openLabCourse"
      />

      <div v-else class="bg-white shadow rounded-lg p-8 text-center text-gray-600">
        No encontramos información para el curso solicitado.
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '@/components/ui/TopBar.vue'
import CourseInfoCard from '@/components/features/course/CourseInfoCard.vue'
import { useCourseService } from '@/services/courseService'

const route = useRoute()
const router = useRouter()

const { courseDetails, courseDetailsLoading, courseDetailsError, fetchCourseDetails } = useCourseService()

const courseIdParam = computed(() => {
  const raw = route.params.courseId
  return Array.isArray(raw) ? raw[0] : raw
})

const courseId = computed<number | null>(() => {
  const raw = courseIdParam.value
  if (raw === undefined || raw === null || raw === '') {
    return null
  }
  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : null
})

watch(
  courseId,
  newId => {
    if (newId === null) {
      courseDetails.value = null
      courseDetailsError.value = 'Identificador de curso inválido'
      return
    }

    courseDetailsError.value = ''
    fetchCourseDetails(newId)
  },
  { immediate: true }
)

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/bienvenido')
}

const retry = () => {
  if (courseId.value !== null) {
    fetchCourseDetails(courseId.value)
  }
}

const openLabCourse = (labCourseId: number) => {
  if (!labCourseId || labCourseId === courseId.value) {
    return
  }
  router.push({ name: 'course-detail', params: { courseId: labCourseId } })
}
</script>

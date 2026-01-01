<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-xs uppercase tracking-wide text-gray-500">Temario oficial</p>
          <h1 class="text-2xl font-semibold text-gray-900">{{ courseTitle }}</h1>
          <p class="text-sm text-gray-500">Consulta los temas planificados para el curso.</p>
        </div>
        <button
          type="button"
          @click="goBack"
          class="inline-flex items-center justify-center rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-100 transition"
        >
          Volver al curso
        </button>
      </div>

      <div v-if="courseDetailsLoading" class="bg-white shadow rounded-lg p-8 text-center space-y-3">
        <div class="mx-auto h-10 w-10 animate-spin rounded-full border-4 border-blue-200 border-t-blue-600"></div>
        <p class="text-gray-600">Cargando información del sílabo...</p>
      </div>

      <div v-else-if="courseDetailsError" class="bg-red-50 border border-red-200 rounded-lg p-6 space-y-3">
        <h2 class="text-lg font-semibold text-red-800">No se pudo cargar el temario</h2>
        <p class="text-red-700 text-sm">{{ courseDetailsError }}</p>
      </div>

      <div v-else-if="!hasSyllabus" class="bg-white shadow rounded-lg p-6 text-sm text-gray-500">
        No hay un sílabo publicado para este curso.
      </div>

      <section v-else class="bg-white shadow rounded-lg p-6 space-y-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 class="text-xl font-semibold text-gray-900">Temario actualizado</h2>
            <p class="text-sm text-gray-500">Última actualización al cargar la página.</p>
          </div>
          <a
            v-if="downloadUrl"
            :href="downloadUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-flex items-center gap-2 rounded-md border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-700 hover:bg-blue-100 transition"
          >
            Descargar sílabo
          </a>
        </div>

        <div v-if="syllabus?.topics.length" class="space-y-4">
          <article
            v-for="topic in syllabus?.topics"
            :key="`${topic.name}-${topic.sessionDate}-${topic.weight}`"
            class="rounded-xl border border-gray-100 bg-gray-50 p-4 space-y-2"
          >
            <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
              <div>
                <p class="text-sm font-semibold text-gray-900">{{ topic.name }}</p>
                <p class="text-xs text-gray-500">
                  {{ topicWeightLabel(topic) }}
                  · {{ formatTopicDate(topic.sessionDate) }}
                </p>
              </div>
              <span
                class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold"
                :class="statusBadgeClasses(topic.status)"
              >
                {{ statusLabel(topic.status) }}
              </span>
            </div>
            <p class="text-xs text-gray-500">
              {{ topic.status === 'COMPLETED' ? 'Revisa el texto correspondiente para repasar el contenido.' : 'Programa el seguimiento según tu disponibilidad.' }}
            </p>
          </article>
        </div>
        <p v-else class="text-sm text-gray-500">El temario está en proceso de construcción. Revisa más tarde.</p>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '@/components/ui/TopBar.vue'
import { useCourseService } from '@/services/courseService'
import { syllabusService, type TopicScheduleStatus } from '@/services/syllabusService'

const route = useRoute()
const router = useRouter()

const {
  courseDetails,
  courseDetailsLoading,
  courseDetailsError,
  fetchCourseDetails
} = useCourseService()

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
  id => {
    if (id === null) {
      courseDetails.value = null
      courseDetailsError.value = 'Identificador de curso inválido'
      return
    }

    courseDetailsError.value = ''
    fetchCourseDetails(id)
  },
  { immediate: true }
)

const syllabus = computed(() => courseDetails.value?.syllabus ?? null)
const hasSyllabus = computed(() => !!syllabus.value)
const courseTitle = computed(() => courseDetails.value?.name ?? 'Curso')
const downloadUrl = computed(() => {
  const syllabusId = syllabus.value?.syllabusId ?? courseDetails.value?.syllabusId
  return syllabusService.buildDownloadUrl(syllabusId ?? undefined)
})

const formatTopicDate = (isoDate?: string | null) => {
  if (!isoDate) {
    return 'Fecha por definir'
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

const topicWeightLabel = (topic: { weight: number | null }) =>
  topic.weight != null ? `Peso ${topic.weight}%` : 'Peso no definido'

const statusBadgeClasses = (status: TopicScheduleStatus) => {
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

const statusLabel = (status: TopicScheduleStatus) => {
  switch (status) {
    case 'COMPLETED':
      return 'Completado'
    case 'TODAY':
      return 'Sesión de hoy'
    case 'UPCOMING':
      return 'Próximo'
    default:
      return 'Pendiente'
  }
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push({ name: 'course-detail', params: { courseId: courseId.value } })
}
</script>

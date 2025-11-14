<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Perfil del Estudiante</h2>
        </div>
        <button
          @click="goBack"
          class="inline-flex items-center px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition"
        >
          Volver
        </button>
      </div>

      <div v-if="profileLoading" class="bg-white shadow rounded-lg p-6 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p class="text-gray-600 mt-3">Cargando perfil del estudiante...</p>
      </div>

      <div v-else-if="profileError" class="bg-red-50 border border-red-200 rounded-lg p-6">
        <h3 class="text-lg font-semibold text-red-800">No se pudo cargar el perfil</h3>
        <p class="text-red-700 mt-2">{{ profileError }}</p>
        <button
          @click="reloadProfile"
          class="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
        >
          Reintentar
        </button>
      </div>

      <template v-else-if="studentProfile">
        <StudentProfileSummary :profile="studentProfile" />

        <StudentCoursesCard
          :courses="courses"
          :loading="profileLoading"
          :error="profileError"
          :refreshable="false"
          @select="openCourseDetail"
        />

        <StudentScheduleCard
          :entries="schedule"
          :loading="profileLoading"
          :error="profileError"
          :refreshable="false"
        />
      </template>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../components/ui/TopBar.vue'
import StudentProfileSummary from '../components/features/student/StudentProfileSummary.vue'
import StudentCoursesCard from '../components/features/student/StudentCoursesCard.vue'
import StudentScheduleCard from '../components/features/student/StudentScheduleCard.vue'
import { useStudentProfile } from '../composables/useStudentProfile'

// Composable state (was missing before causing undefined references)
const { studentProfile, profileLoading, profileError, courses, schedule, loadProfile } = useStudentProfile()

const route = useRoute()
const router = useRouter()

const cui = computed(() => String(route.params.cui ?? ''))

const loadProfileForCui = () => {
  if (!cui.value) return
  loadProfile(cui.value)
}

const reloadProfile = () => {
  loadProfileForCui()
}

const openCourseDetail = course => {
  if (!course || typeof course.courseId !== 'number') {
    return
  }
  router.push({ name: 'course-detail', params: { courseId: course.courseId } })
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/admin/students')
  }
}

onMounted(() => {
  loadProfileForCui()
})

watch(
  () => route.params.cui,
  () => {
    loadProfileForCui()
  }
)
</script>

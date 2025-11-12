<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Perfil del Estudiante</h2>
          <p class="text-gray-600">Consulta la información académica y los cursos matriculados.</p>
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
import AdminLayout from '../components/TopBar.vue'
import StudentProfileSummary from '../components/student/StudentProfileSummary.vue'
import StudentCoursesCard from '../components/student/StudentCoursesCard.vue'
import StudentScheduleCard from '../components/student/StudentScheduleCard.vue'
import { useStudentService } from '../services/studentService'

const route = useRoute()
const router = useRouter()

const { studentProfile, profileLoading, profileError, fetchStudentProfile } = useStudentService()

const cui = computed(() => String(route.params.cui ?? ''))
const courses = computed(() => studentProfile.value?.courses ?? [])
const schedule = computed(() => studentProfile.value?.schedule ?? [])

const loadProfile = () => {
  if (!cui.value) {
    return
  }
  fetchStudentProfile(cui.value)
}

const reloadProfile = () => {
  loadProfile()
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/admin/students')
  }
}

onMounted(() => {
  loadProfile()
})

watch(
  () => route.params.cui,
  () => {
    loadProfile()
  }
)
</script>

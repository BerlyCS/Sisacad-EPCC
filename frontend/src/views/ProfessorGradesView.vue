<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Seguimiento de notas</p>
        <h1 class="text-3xl font-bold text-gray-900">Panel de Calificaciones</h1>
        <p class="text-gray-600">Explora los cursos que dictas y revisa estadísticas consolidadas.</p>
      </header>

      <ProfessorCourseList
        :courses="courses"
        :loading="coursesLoading"
        :error="coursesError"
        @select="handleSelectCourse"
      />

      <ProfessorGradeStatsDrawer
        :open="!!selectedCourse"
        :course="selectedCourse"
        :stats="selectedCourseStats"
        :loading="statsLoading"
        :error="statsError"
        @close="closeDrawer"
      />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { ProfessorCourseList, ProfessorGradeStatsDrawer } from '@/components/features/professor'
import { useProfessorGrades } from '@/composables/useProfessorGrades'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const {
  courses,
  coursesLoading,
  coursesError,
  selectedCourse,
  selectedCourseStats,
  statsLoading,
  statsError,
  loadCourses,
  openCourseStats,
  closeCourseStats
} = useProfessorGrades()

const handleSelectCourse = async (course: { courseCode: string }) => {
  if (!course?.courseCode) {
    return
  }
  await openCourseStats(course.courseCode)
}

const closeDrawer = () => {
  closeCourseStats()
}

onMounted(async () => {
  await authStore.initializeAuth()
  if (authStore.isAuthenticated && authStore.user?.role === 'PROFESSOR') {
    await loadCourses()
  }
})
</script>

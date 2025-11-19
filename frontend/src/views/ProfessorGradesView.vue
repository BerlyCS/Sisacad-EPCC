<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Seguimiento de notas</p>
        <h1 class="text-3xl font-bold text-gray-900">Panel de Calificaciones</h1>
      </header>

            <ProfessorCourseList
              :courses="courses"
              :loading="coursesLoading"
              :error="coursesError || ''"
              @select="handleSelectCourse"
              @retry="loadCourses"
            />

      <ProfessorGradeStatsDrawer
        :open="!!selectedCourse"
        :course="selectedCourse"
        :stats="selectedCourseStats"
        :loading="statsLoading"
        :error="statsError"
        @close="closeCourseStats"
      />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { ProfessorCourseList, ProfessorGradeStatsDrawer } from '@/components/features/professor'
import { useProfessorGrades } from '@/composables/useProfessorGrades'
import { useAuthStore } from '@/stores/auth'
import type { ProfessorCourseSummary } from '@/services/gradeService'

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

const hasAttemptedInitialLoad = ref(false)
const rolesWithGradeAccess = new Set(['PROFESSOR', 'ADMIN'])

const canLoadProfessorCourses = computed(() => {
  if (!authStore.isAuthenticated) {
    return false
  }
  const role = authStore.user?.role
  return typeof role === 'string' && rolesWithGradeAccess.has(role)
})

const ensureAuthContext = async () => {
  if (!authStore.initialized) {
    try {
      await authStore.initializeAuth()
    } catch (error) {
      console.warn('No se pudo inicializar la sesión en ProfessorGradesView:', error)
    }
  }
}

void ensureAuthContext()

watchEffect(() => {
  if (!canLoadProfessorCourses.value) {
    hasAttemptedInitialLoad.value = false
    closeCourseStats()
    return
  }

  if (!hasAttemptedInitialLoad.value) {
    hasAttemptedInitialLoad.value = true
    void loadCourses()
  }
})

const handleSelectCourse = async (course: ProfessorCourseSummary) => {
  if (!course?.courseCode) {
    return
  }
  await openCourseStats(course.courseCode)
}
</script>

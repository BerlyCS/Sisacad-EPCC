<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="flex flex-col gap-4 rounded-2xl border border-blue-100 bg-blue-50/70 px-5 py-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Gestión de laboratorios</p>
          <h1 class="text-3xl font-bold text-gray-900">Inscripción a Laboratorios</h1>
        </div>
        <div class="rounded-xl bg-white px-5 py-3 shadow-sm">
          <p class="text-xs text-gray-500">Laboratorios pendientes</p>
          <p class="text-2xl font-semibold text-blue-600">{{ pendingCourses.length }}</p>
        </div>
      </header>

      <p v-if="coursesError" class="rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-600">
        {{ coursesError }}
      </p>

      <section class="grid gap-6 lg:grid-cols-[minmax(0,0.42fr)_minmax(0,0.58fr)]">
        <StudentLabCourseList
          :courses="theoryCourses"
          :lab-assignments="labAssignmentsRecord"
          :selected-course-id="selectedTheoryCourseId"
          :loading="coursesLoading"
          :error="coursesError"
          @select="handleCourseSelect"
        />

        <StudentLabSectionList
          :course="selectedTheoryCourse"
          :assigned-lab="selectedLabAssignment"
          :sections="labSections"
          :loading="labSectionsLoading"
          :error="labSectionsError"
          :validation-result="validationResult"
          :enrollment-result="enrollmentResult"
          :info-message="infoMessage"
          :error-message="errorMessage"
          :enrollment-loading="enrollmentLoading"
          @validate="handleValidate"
          @enroll="handleEnroll"
        />
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { StudentLabCourseList, StudentLabSectionList } from '@/components/features/student'
import { useStudentLabEnrollment } from '@/composables/useStudentLabEnrollment'
import { useAuthStore } from '@/stores/auth'
import type { Course } from '@/services/studentCourseService'

const authStore = useAuthStore()
const {
  theoryCourses,
  pendingCourses,
  labAssignmentsMap,
  selectedTheoryCourseId,
  selectedTheoryCourse,
  selectedLabAssignment,
  labSections,
  labSectionsLoading,
  labSectionsError,
  validationResult,
  enrollmentResult,
  enrollmentLoading,
  infoMessage,
  errorMessage,
  coursesLoading,
  coursesError,
  fetchMyCourses,
  selectTheoryCourse,
  validateLabSelection,
  confirmLabSelection
} = useStudentLabEnrollment()

const labAssignmentsRecord = computed<Record<number, Course | undefined>>(() => {
  const record: Record<number, Course | undefined> = {}
  labAssignmentsMap.value.forEach((lab, theoryId) => {
    record[theoryId] = lab
  })
  return record
})

const handleCourseSelect = (courseId: number) => {
  void selectTheoryCourse(courseId)
}

const handleValidate = (labCourseId: number) => {
  void validateLabSelection(labCourseId)
}

const handleEnroll = (labCourseId: number) => {
  void confirmLabSelection(labCourseId)
}

onMounted(async () => {
  await authStore.initializeAuth()
  if (authStore.isAuthenticated && authStore.user?.role === 'STUDENT') {
    await fetchMyCourses()
  }
})
</script>

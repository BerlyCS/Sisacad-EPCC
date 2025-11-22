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

      <section v-if="selectedCourse" class="space-y-4">
        <div class="rounded-2xl border border-blue-100 bg-blue-50 px-4 py-3 flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold text-blue-600 uppercase tracking-wide">Curso activo</p>
            <p class="text-lg font-semibold text-gray-900">{{ selectedCourse.courseName }} ({{ selectedCourse.courseCode }})</p>
            <p class="text-sm text-gray-600">Gestiona hasta {{ rosterEntries.length }} estudiantes en los grupos seleccionados.</p>
          </div>
          <div class="flex gap-2">
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-3 py-1.5 text-sm font-semibold text-gray-700 hover:bg-white"
              :disabled="rosterLoading"
              @click="handleRefreshRoster"
            >
              {{ rosterLoading ? 'Actualizando...' : 'Actualizar lista' }}
            </button>
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-3 py-1.5 text-sm font-semibold text-gray-700 hover:bg-white"
              @click="statsDrawerOpen = true"
            >
              Ver estadísticas
            </button>
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-3 py-1.5 text-sm font-semibold text-gray-700"
              @click="handleCloseCourse"
            >
              Cerrar curso
            </button>
          </div>
        </div>

        <div class="grid gap-6 lg:grid-cols-[minmax(0,0.55fr)_minmax(0,0.45fr)]">
          <div class="space-y-4">
            <GroupFilterTabs
              :groups="courseGroups"
              :selected-ids="selectedGroupIds"
              :loading="courseGroupsLoading"
              :error="courseGroupsError || ''"
              @update:selected-ids="handleGroupSelection"
            />

            <GradeRosterTable
              :students="rosterEntries"
              :loading="rosterLoading"
              :error="rosterError"
              :selected-key="selectedStudentKey"
              @select="handleSelectStudent"
            />
          </div>

          <GradeEditorPanel
            :student="selectedRosterStudent"
            :rubric="courseRubric"
            :rubric-loading="rubricLoading"
            :read-only="isReadonlyPanel"
            :saving="gradeMutationLoading"
            :error="gradeMutationError"
            @refresh="handleRefreshRoster"
            @submit="handleSubmitGrade"
          />
        </div>
      </section>

      <p v-else class="rounded-2xl border border-dashed border-gray-200 bg-white p-6 text-sm text-gray-500">
        Selecciona un curso para ver los grupos y registrar calificaciones.
      </p>

      <ProfessorGradeStatsDrawer
        :open="statsDrawerOpen"
        :course="selectedCourse"
        :stats="selectedCourseStats"
        :loading="statsLoading"
        :error="statsError"
        @close="statsDrawerOpen = false"
      />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, ref, watch, watchEffect } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { ProfessorCourseList, ProfessorGradeStatsDrawer, GroupFilterTabs, GradeRosterTable, GradeEditorPanel } from '@/components/features/professor'
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
  openCourseWorkspace,
  closeCourseStats,
  courseGroups,
  courseGroupsLoading,
  courseGroupsError,
  selectedGroupIds,
  rosterEntries,
  rosterLoading,
  rosterError,
  selectedRosterStudent,
  courseRubric,
  rubricLoading,
  gradeMutationLoading,
  gradeMutationError,
  updateSelectedGroups,
  refreshRoster,
  selectRosterStudent,
  submitGrade
} = useProfessorGrades()

const hasAttemptedInitialLoad = ref(false)
const rolesWithGradeAccess = new Set(['PROFESSOR', 'ADMIN'])
const statsDrawerOpen = ref(false)

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
  await openCourseWorkspace(course)
}

const handleCloseCourse = () => {
  statsDrawerOpen.value = false
  closeCourseStats()
}

const handleGroupSelection = async (groupIds: number[]) => {
  await updateSelectedGroups(groupIds)
}

const handleSelectStudent = (student: Parameters<typeof selectRosterStudent>[0]) => {
  selectRosterStudent(student)
}

const handleRefreshRoster = async () => {
  await refreshRoster()
}

const selectedStudentKey = computed(() => {
  if (!selectedRosterStudent.value) {
    return null
  }
  return `${selectedRosterStudent.value.courseId}-${selectedRosterStudent.value.studentDocumentoIdentidad}`
})

const isReadonlyPanel = computed(() => {
  if (!selectedRosterStudent.value) {
    return true
  }
  return !selectedRosterStudent.value.canGrade
})

const handleSubmitGrade = async (payload: { continuousGrades: number[]; examGrades: number[]; status: 'SUBMITTED' | 'DRAFT' }) => {
  if (!selectedRosterStudent.value) {
    return
  }
  await submitGrade({
    studentDocumentoIdentidad: selectedRosterStudent.value.studentDocumentoIdentidad,
    groupId: selectedRosterStudent.value.courseId,
    continuousGrades: payload.continuousGrades,
    examGrades: payload.examGrades,
    status: payload.status
  })
}

watch(() => selectedCourse.value, (course) => {
  if (!course) {
    statsDrawerOpen.value = false
  }
})
</script>

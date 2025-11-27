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
        description="Solo se mostrarán los cursos teóricos donde estás asignado para calificar."
        @select="handleSelectCourse"
        @retry="loadCourses"
      />

      <p
        v-if="showTheoryRestrictionMessage"
        class="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900"
      >
        Solo los profesores asignados a grupos teóricos pueden registrar calificaciones en esta vista. Si no ves cursos
        disponibles, solicita al coordinador académico que te asigne un grupo teórico.
      </p>

      <section v-if="selectedCourse" class="space-y-4">
        <div class="rounded-2xl border border-blue-100 bg-blue-50 px-4 py-3 flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold text-blue-600 uppercase tracking-wide">Curso activo</p>
            <p class="text-lg font-semibold text-gray-900">{{ selectedCourse.courseName }} ({{ selectedCourse.courseCode }})</p>
            <p class="text-sm text-gray-600">Gestiona notas únicas para {{ rosterEntries.length }} estudiantes matriculados.</p>
            <p class="text-xs text-gray-500">Las calificaciones calculadas se redondean hacia arriba para alinearse con el sistema institucional.</p>
          </div>
          <div class="flex gap-2">
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-3 py-1.5 text-sm font-semibold text-gray-700 hover:bg-white"
              :class="{ 'bg-blue-50 border-blue-200 text-blue-700': isBulkMode }"
              @click="isBulkMode = !isBulkMode"
            >
              {{ isBulkMode ? 'Modo Individual' : 'Modo Masivo' }}
            </button>
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

        <div v-if="isBulkMode" class="h-[600px]">
          <BulkGradeEditor
            :students="rosterEntries"
            :rubric="courseRubric"
            :read-only="isGroupReadOnly" 
            :saving="gradeMutationLoading"
            @save="handleBulkSubmit"
          />
        </div>

        <div v-else class="grid gap-6 lg:grid-cols-[minmax(0,0.55fr)_minmax(0,0.45fr)]">
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
import { ProfessorCourseList, ProfessorGradeStatsDrawer, GroupFilterTabs, GradeRosterTable, GradeEditorPanel, BulkGradeEditor } from '@/components/features/professor'
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
  submitGrade,
  submitBulkGrades
} = useProfessorGrades()

const hasAttemptedInitialLoad = ref(false)
const rolesWithGradeAccess = new Set(['PROFESSOR', 'ADMIN'])
const statsDrawerOpen = ref(false)
const isBulkMode = ref(false)

const resolvedUserRole = computed(() => {
  const role = authStore.userRole
  if (typeof role === 'string') {
    return role
  }
  if (role && typeof role === 'object' && 'value' in role) {
    return (role as { value?: string }).value ?? ''
  }
  return ''
})
const showTheoryRestrictionMessage = computed(() => {
  return resolvedUserRole.value === 'PROFESSOR' && !coursesLoading.value && !coursesError.value && courses.value.length === 0
})

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
  return `${selectedRosterStudent.value.groupId}-${selectedRosterStudent.value.studentUserId}`
})

const isGroupReadOnly = computed(() => {
  if (!selectedGroupIds.value.length) return true
  // If any of the selected groups is not gradeable, we treat the bulk operation as read-only (or mixed, but safer to lock)
  // Or better: check if the *currently displayed* students can be graded.
  // Since we filter by group, let's check the groups corresponding to selectedGroupIds.
  const selectedGroups = courseGroups.value.filter(g => selectedGroupIds.value.includes(g.groupId))
  return selectedGroups.some(g => !g.canGrade)
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
    studentUserId: selectedRosterStudent.value.studentUserId,
    groupId: selectedRosterStudent.value.groupId,
    continuousGrades: payload.continuousGrades,
    examGrades: payload.examGrades,
    status: payload.status
  })
}

const handleBulkSubmit = async (payload: { students: any[], status: 'SUBMITTED' | 'DRAFT' }) => {
  if (!selectedGroupIds.value.length) return
  
  // Assuming bulk edit is for the first selected group if multiple (or we iterate)
  // For simplicity, let's take the first group ID as the target for now, 
  // or filter students by group if the backend requires per-group calls.
  // The backend endpoint is /courses/{courseId}/groups/{groupId}/bulk
  // So we must group by groupId.
  
  const studentsByGroup = payload.students.reduce((acc, student) => {
    const gid = student.groupId || selectedGroupIds.value[0]
    if (!acc[gid]) acc[gid] = []
    acc[gid].push(student)
    return acc
  }, {} as Record<number, any[]>)

  for (const groupId of Object.keys(studentsByGroup)) {
    await submitBulkGrades({
      groupId: Number(groupId),
      students: studentsByGroup[Number(groupId)],
      status: payload.status
    })
  }
}

watch(() => selectedCourse.value, (course) => {
  if (!course) {
    statsDrawerOpen.value = false
  }
})
</script>

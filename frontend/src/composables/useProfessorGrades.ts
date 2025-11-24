import { storeToRefs } from 'pinia'
import { useGradeStore } from '@/stores/grades'
import type { CourseRosterEntry, ProfessorCourseSummary } from '@/services/gradeService'

export const useProfessorGrades = () => {
  const gradeStore = useGradeStore()

  const {
    sortedProfessorCourses,
    professorCoursesLoading,
    professorCoursesError,
    selectedCourse,
    selectedCourseStats,
    statsLoading,
    statsError,
    statsByCourse,
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
    rubricError,
    gradeMutationLoading,
    gradeMutationError
  } = storeToRefs(gradeStore)

  const loadCourses = async () => {
    await gradeStore.loadProfessorCourses()
  }

  const openCourseWorkspace = async (course: ProfessorCourseSummary | null) => {
    if (!course?.courseCode || !course.courseId) {
      return
    }

    gradeStore.setSelectedCourse(course.courseCode)

    const pending: Promise<unknown>[] = []
    if (!statsByCourse.value[course.courseCode]) {
      pending.push(gradeStore.loadCourseStats(course.courseCode))
    }
    pending.push(gradeStore.initializeCourseWorkspace(course.courseId))
    await Promise.all(pending)
  }

  const closeCourseStats = () => {
    gradeStore.clearSelectedCourse()
  }

  const updateSelectedGroups = async (groupIds: number[]) => {
    await gradeStore.updateSelectedGroups(groupIds)
  }

  const refreshRoster = async () => {
    await gradeStore.refreshCourseRoster()
  }

  const selectRosterStudent = (entry: CourseRosterEntry | null) => {
    gradeStore.selectRosterStudent(entry)
  }

  const submitGrade = async (payload: { studentUserId: number; groupId: number; continuousGrades: number[]; examGrades: number[]; status?: string }) => {
    await gradeStore.submitRosterGrade(payload)
    await refreshRoster()
  }

  return {
    courses: sortedProfessorCourses,
    coursesLoading: professorCoursesLoading,
    coursesError: professorCoursesError,
    selectedCourse,
    selectedCourseStats,
    statsLoading,
    statsError,
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
    rubricError,
    gradeMutationLoading,
    gradeMutationError,
    loadCourses,
    openCourseWorkspace,
    closeCourseStats,
    updateSelectedGroups,
    refreshRoster,
    selectRosterStudent,
    submitGrade
  }
}

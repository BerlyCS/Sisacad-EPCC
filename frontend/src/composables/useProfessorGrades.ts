import { storeToRefs } from 'pinia'
import { useGradeStore } from '@/stores/grades'
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
    statsByCourse
  } = storeToRefs(gradeStore)

  const loadCourses = async () => {
    await gradeStore.loadProfessorCourses()
  }

  const openCourseStats = async (courseCode: string) => {
    if (!courseCode) {
      return
    }

    gradeStore.setSelectedCourse(courseCode)

    if (!statsByCourse.value[courseCode]) {
      await gradeStore.loadCourseStats(courseCode)
    }
  }

  const closeCourseStats = () => {
    gradeStore.clearSelectedCourse()
  }

  return {
    courses: sortedProfessorCourses,
    coursesLoading: professorCoursesLoading,
    coursesError: professorCoursesError,
    selectedCourse,
    selectedCourseStats,
    statsLoading,
    statsError,
    loadCourses,
    openCourseStats,
    closeCourseStats
  }
}

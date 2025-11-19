import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import type { CourseGradeStats, ProfessorCourseSummary, StudentGrade } from '@/services/gradeService'
import { gradeService } from '@/services/gradeService'

export const useGradeStore = defineStore('grades', () => {
  const studentGrades = ref<StudentGrade[]>([])
  const studentGradesLoading = ref(false)
  const studentGradesError = ref('')

  const professorCourses = ref<ProfessorCourseSummary[]>([])
  const professorCoursesLoading = ref(false)
  const professorCoursesError = ref('')

  const statsByCourse = ref<Record<string, CourseGradeStats>>({})
  const statsLoading = ref(false)
  const statsError = ref('')

  const selectedCourseCode = ref('')

  const sortedStudentGrades = computed(() => {
    return [...studentGrades.value].sort((a, b) => {
      return a.courseName.localeCompare(b.courseName)
    })
  })

  const sortedProfessorCourses = computed(() => {
    return [...professorCourses.value].sort((a, b) => a.courseName.localeCompare(b.courseName))
  })

  const selectedCourse = computed(() => {
    if (!selectedCourseCode.value) {
      return null
    }
    return professorCourses.value.find(course => course.courseCode === selectedCourseCode.value) ?? null
  })

  const selectedCourseStats = computed(() => {
    if (!selectedCourseCode.value) {
      return null
    }
    return statsByCourse.value[selectedCourseCode.value] ?? null
  })

  const passedCourses = computed(() => studentGrades.value.filter(grade => (grade.finalGrade ?? 0) >= 11).length)
  const pendingCourses = computed(() => studentGrades.value.filter(grade => (grade.finalGrade ?? 0) < 11).length)

  const finalAverage = computed(() => {
    const validGrades = studentGrades.value
      .map(grade => grade.finalGrade)
      .filter((value): value is number => typeof value === 'number' && Number.isFinite(value))

    if (!validGrades.length) {
      return null
    }

    const sum = validGrades.reduce((acc, grade) => acc + grade, 0)
    return Number((sum / validGrades.length).toFixed(2))
  })

  const performanceStatus = computed(() => {
    if (finalAverage.value == null) {
      return 'SIN DATOS'
    }
    if (finalAverage.value >= 15) {
      return 'ALTO'
    }
    if (finalAverage.value >= 11) {
      return 'REGULAR'
    }
    return 'EN RIESGO'
  })

  const setSelectedCourse = (courseCode: string) => {
    selectedCourseCode.value = courseCode
  }

  const clearSelectedCourse = () => {
    selectedCourseCode.value = ''
  }

  const loadStudentGrades = async (studentDocumento: string) => {
    studentGradesLoading.value = true
    studentGradesError.value = ''

    try {
      studentGrades.value = await gradeService.fetchStudentGrades(studentDocumento)
    } catch (error) {
      console.error('Error while loading student grades', error)
      studentGradesError.value = error instanceof Error ? error.message : 'No se pudieron cargar las calificaciones'
      studentGrades.value = []
    } finally {
      studentGradesLoading.value = false
    }
  }

  const loadProfessorCourses = async () => {
    professorCoursesLoading.value = true
    professorCoursesError.value = ''

    try {
      professorCourses.value = await gradeService.fetchProfessorCourseSummaries()
    } catch (error) {
      console.error('Error while loading professor courses', error)
      professorCoursesError.value = error instanceof Error ? error.message : 'No se pudieron cargar los cursos asignados'
      professorCourses.value = []
    } finally {
      professorCoursesLoading.value = false
    }
  }

  const loadCourseStats = async (courseCode: string) => {
    if (!courseCode) {
      return
    }

    statsLoading.value = true
    statsError.value = ''

    try {
      const stats = await gradeService.fetchCourseStatistics(courseCode)
      statsByCourse.value = {
        ...statsByCourse.value,
        [courseCode]: stats
      }
    } catch (error) {
      console.error('Error while loading course stats', error)
      const status = (error as Error & { status?: number }).status
      if (status === 404) {
        statsError.value = 'No hay calificaciones registradas para este curso'
      } else {
        statsError.value = error instanceof Error ? error.message : 'No se pudieron cargar las estadísticas'
      }
    } finally {
      statsLoading.value = false
    }
  }

  return {
    // state
    studentGrades,
    studentGradesLoading,
    studentGradesError,
    professorCourses,
    professorCoursesLoading,
    professorCoursesError,
    statsByCourse,
    statsLoading,
    statsError,
    selectedCourseCode,

    // getters
    sortedStudentGrades,
    finalAverage,
    performanceStatus,
    passedCourses,
    pendingCourses,
    sortedProfessorCourses,
    selectedCourse,
    selectedCourseStats,

    // actions
    setSelectedCourse,
    clearSelectedCourse,
    loadStudentGrades,
    loadProfessorCourses,
    loadCourseStats
  }
})

import { computed, ref, type ComputedRef } from 'vue'
import { defineStore } from 'pinia'
import type {
  CourseGradeStats,
  CourseGroupSummary,
  CourseRosterEntry,
  GradingRubric,
  ProfessorCourseSummary,
  StudentGrade
} from '@/services/gradeService'
import { gradeService } from '@/services/gradeService'
import { useAuthStore } from '@/stores/auth'

export const useGradeStore = defineStore('grades', () => {
  const authStore = useAuthStore()
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
  const selectedCourseId = ref<number | null>(null)

  const courseGroups = ref<CourseGroupSummary[]>([])
  const courseGroupsLoading = ref(false)
  const courseGroupsError = ref('')

  const selectedGroupIds = ref<number[]>([])

  const rosterEntries = ref<CourseRosterEntry[]>([])
  const rosterLoading = ref(false)
  const rosterError = ref('')

  const selectedRosterStudent = ref<CourseRosterEntry | null>(null)

  const courseRubric = ref<GradingRubric | null>(null)
  const rubricLoading = ref(false)
  const rubricError = ref('')

  const gradeMutationLoading = ref(false)
  const gradeMutationError = ref('')

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

  const activeCourseId = computed(() => selectedCourseId.value)

  const selectedCourseStats = computed(() => {
    if (!selectedCourseCode.value) {
      return null
    }
    return statsByCourse.value[selectedCourseCode.value] ?? null
  })

  const passedCourses = computed(() => studentGrades.value.filter(grade => (grade.finalGrade ?? 0) >= 10.5).length)
  const pendingCourses = computed(() => studentGrades.value.filter(grade => (grade.finalGrade ?? 0) < 10.5).length)

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
    if (finalAverage.value >= 10.5) {
      return 'REGULAR'
    }
    return 'EN RIESGO'
  })

  const setSelectedCourse = (courseCode: string) => {
    selectedCourseCode.value = courseCode
    const found = professorCourses.value.find(course => course.courseCode === courseCode)
    selectedCourseId.value = found?.courseId ?? null
  }

  const clearSelectedCourse = () => {
    selectedCourseCode.value = ''
    selectedCourseId.value = null
    courseGroups.value = []
    selectedGroupIds.value = []
    rosterEntries.value = []
    selectedRosterStudent.value = null
    courseRubric.value = null
  }

  const loadStudentGrades = async (studentUserId: number) => {
    studentGradesLoading.value = true
    studentGradesError.value = ''

    const maxAttempts = 2
    let attempt = 0

    while (attempt < maxAttempts) {
      try {
        studentGrades.value = await gradeService.fetchStudentGrades(studentUserId)
        studentGradesLoading.value = false
        return
      } catch (error) {
        const status = (error as Error & { status?: number }).status
        const canRetry = status === 401 || status === 403
        attempt += 1

        if (canRetry && attempt < maxAttempts) {
          console.warn('Student grades request was unauthorized, revalidating session and retrying...')
          await authStore.initializeAuth(true)
          continue
        }

        console.error('Error while loading student grades', error)
        studentGradesError.value = error instanceof Error ? error.message : 'No se pudieron cargar las calificaciones'
        studentGrades.value = []
        studentGradesLoading.value = false
        return
      }
    }
  }

  const removeCourseStats = (courseCode: string) => {
    if (!courseCode) {
      return
    }
    const nextStats = { ...statsByCourse.value }
    delete nextStats[courseCode]
    statsByCourse.value = nextStats
  }

  const resolveUserRole = (): string => {
    const roleSource = authStore.userRole as string | ComputedRef<string>
    if (typeof roleSource === 'string') {
      return roleSource
    }
    if (roleSource && typeof roleSource === 'object' && 'value' in roleSource) {
      const resolved = roleSource.value
      return typeof resolved === 'string' ? resolved : ''
    }
    return ''
  }

  const ensureSelectionConsistency = () => {
    if (!selectedCourseCode.value) {
      return
    }

    const stillExists = professorCourses.value.some(
      course => course.courseCode === selectedCourseCode.value
    )

    if (!stillExists) {
      removeCourseStats(selectedCourseCode.value)
      selectedCourseCode.value = ''
    }
  }

  const loadProfessorCourses = async () => {
    professorCoursesLoading.value = true
    professorCoursesError.value = ''

    try {
      const fetched = await gradeService.fetchProfessorCourseSummaries()
      const role = resolveUserRole()
      professorCourses.value = role === 'PROFESSOR'
        ? fetched.filter(course => course.courseType === 'THEORY')
        : fetched
    } catch (error) {
      console.error('Error while loading professor courses', error)
      professorCoursesError.value = error instanceof Error ? error.message : 'No se pudieron cargar los cursos asignados'
      professorCourses.value = []
    } finally {
      ensureSelectionConsistency()
      professorCoursesLoading.value = false
    }
  }

  const loadCourseStats = async (courseCode: string) => {
    if (!courseCode) {
      return
    }

    const courseId = selectedCourseId.value
    if (!courseId) {
      statsError.value = 'Curso inválido'
      return
    }

    statsLoading.value = true
    statsError.value = ''

    try {
      const stats = await gradeService.fetchCourseStatistics(courseId)
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

  const loadCourseGroups = async (courseId: number) => {
    if (!courseId) {
      courseGroups.value = []
      return
    }

    courseGroupsLoading.value = true
    courseGroupsError.value = ''
    try {
      const fetchedGroups = await gradeService.fetchCourseGroups(courseId)
      const role = resolveUserRole()

      const eligibleGroups = role === 'PROFESSOR'
        ? fetchedGroups.filter(group => group.courseType === 'THEORY' || group.courseType === 'PRACTICE')
        : fetchedGroups

      const dedupedByLetter: Record<string, CourseGroupSummary> = {}
      for (const group of eligibleGroups) {
        const letterKey = (group.groupLetter || '').toUpperCase() || '-'
        const existing = dedupedByLetter[letterKey]
        if (!existing) {
          dedupedByLetter[letterKey] = group
          continue
        }
        const preferCurrent = group.canGrade && !existing.canGrade
        dedupedByLetter[letterKey] = preferCurrent ? group : existing
      }

      const filteredGroups = Object.values(dedupedByLetter)
        .sort((a, b) => a.groupLetter.localeCompare(b.groupLetter))

      if (role === 'PROFESSOR' && fetchedGroups.length && !filteredGroups.length) {
        courseGroupsError.value = 'Este curso no tiene grupos disponibles para tu rol.'
      }

      courseGroups.value = filteredGroups
      const sanitizedSelection = selectedGroupIds.value.filter(groupId =>
        courseGroups.value.some(group => group.groupId === groupId)
      )
      if (sanitizedSelection.length) {
        selectedGroupIds.value = sanitizedSelection
      } else if (courseGroups.value.length) {
        const preferred = courseGroups.value.find(group => group.canGrade) ?? courseGroups.value[0]
        selectedGroupIds.value = preferred ? [preferred.groupId] : []
      } else {
        selectedGroupIds.value = []
      }
    } catch (error) {
      console.error('Error while loading course groups', error)
      courseGroupsError.value = error instanceof Error ? error.message : 'No se pudieron cargar los grupos'
      courseGroups.value = []
      selectedGroupIds.value = []
    } finally {
      courseGroupsLoading.value = false
    }
  }

  const loadCourseRubric = async (courseId: number) => {
    if (!courseId) {
      courseRubric.value = null
      return
    }

    rubricLoading.value = true
    rubricError.value = ''
    try {
      courseRubric.value = await gradeService.fetchCourseRubric(courseId)
    } catch (error) {
      console.error('Error while loading course rubric', error)
      rubricError.value = error instanceof Error ? error.message : 'No se pudo cargar la rúbrica'
      courseRubric.value = null
    } finally {
      rubricLoading.value = false
    }
  }

  const loadCourseRoster = async (courseId: number, groupIds?: number[]) => {
    if (!courseId) {
      rosterEntries.value = []
      return
    }

    rosterLoading.value = true
    rosterError.value = ''
    try {
      const response = await gradeService.fetchCourseRoster(courseId, {
        groupIds: groupIds && groupIds.length ? groupIds : undefined
      })
      rosterEntries.value = response.students
      if (selectedRosterStudent.value) {
        const refreshed = response.students.find(
          entry => entry.studentUserId === selectedRosterStudent.value?.studentUserId &&
            entry.groupId === selectedRosterStudent.value?.groupId
        )
        selectedRosterStudent.value = refreshed ?? (response.students[0] ?? null)
      } else if (response.students.length) {
        selectedRosterStudent.value = response.students[0] ?? null
      } else {
        selectedRosterStudent.value = null
      }
    } catch (error) {
      console.error('Error while loading roster', error)
      rosterError.value = error instanceof Error ? error.message : 'No se pudieron cargar los estudiantes'
      rosterEntries.value = []
      selectedRosterStudent.value = null
    } finally {
      rosterLoading.value = false
    }
  }

  const initializeCourseWorkspace = async (courseId: number) => {
    if (!courseId) {
      return
    }
    selectedGroupIds.value = []
    selectedRosterStudent.value = null
    await loadCourseGroups(courseId)
    await Promise.all([loadCourseRubric(courseId), loadCourseRoster(courseId, selectedGroupIds.value)])
  }

  const updateSelectedGroups = async (groupIds: number[]) => {
    selectedGroupIds.value = groupIds
    if (activeCourseId.value) {
      await loadCourseRoster(activeCourseId.value, selectedGroupIds.value)
    }
  }

  const refreshCourseRoster = async () => {
    if (!activeCourseId.value) {
      return
    }
    await loadCourseRoster(activeCourseId.value, selectedGroupIds.value)
  }

  const selectRosterStudent = (entry: CourseRosterEntry | null) => {
    selectedRosterStudent.value = entry
  }

  const submitRosterGrade = async (payload: { studentUserId: number; groupId: number; continuousGrades: number[]; examGrades: number[]; status?: string }) => {
    if (!activeCourseId.value) {
      throw new Error('No hay un curso activo seleccionado')
    }

    gradeMutationLoading.value = true
    gradeMutationError.value = ''

    try {
      const response = await gradeService.submitGrade(
        activeCourseId.value,
        payload.studentUserId,
        payload.groupId,
        {
          continuousGrades: payload.continuousGrades,
          examGrades: payload.examGrades,
          status: payload.status ?? 'SUBMITTED'
        }
      )

      rosterEntries.value = rosterEntries.value.map(entry => {
        if (entry.studentUserId === response.studentUserId && entry.groupId === response.groupId) {
          const updated: CourseRosterEntry = {
            ...entry,
            continuousGrades: response.continuousGrades,
            examGrades: response.examGrades,
            finalGrade: response.finalGrade,
            submissionStatus: response.status
          }
          if (selectedRosterStudent.value?.studentUserId === updated.studentUserId &&
            selectedRosterStudent.value?.groupId === updated.groupId) {
            selectedRosterStudent.value = updated
          }
          return updated
        }
        return entry
      })
    } catch (error) {
      console.error('Error while submitting grade', error)
      gradeMutationError.value = error instanceof Error ? error.message : 'No se pudo registrar la nota'
      throw error
    } finally {
      gradeMutationLoading.value = false
    }
  }

  const submitBulkGrades = async (payload: { groupId: number; students: any[]; status: string }) => {
    if (!activeCourseId.value) {
      throw new Error('No hay un curso activo seleccionado')
    }

    gradeMutationLoading.value = true
    gradeMutationError.value = ''

    try {
      const responses = await gradeService.submitGradesBulk(
        activeCourseId.value,
        payload.groupId,
        payload.students
      )

      // Update local state for all affected students
      rosterEntries.value = rosterEntries.value.map(entry => {
        const update = responses.find(r => r.studentUserId === entry.studentUserId)
        if (update) {
          return {
            ...entry,
            continuousGrades: update.continuousGrades,
            examGrades: update.examGrades,
            finalGrade: update.finalGrade,
            submissionStatus: update.status
          }
        }
        return entry
      })
    } catch (error) {
      console.error('Error while submitting bulk grades', error)
      gradeMutationError.value = error instanceof Error ? error.message : 'No se pudo registrar la nota masiva'
      throw error
    } finally {
      gradeMutationLoading.value = false
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
    selectedCourseId,
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

    // getters
    sortedStudentGrades,
    finalAverage,
    performanceStatus,
    passedCourses,
    pendingCourses,
    sortedProfessorCourses,
    selectedCourse,
    activeCourseId,
    selectedCourseStats,

    // actions
    setSelectedCourse,
    clearSelectedCourse,
    loadStudentGrades,
    loadProfessorCourses,
    loadCourseStats,
    loadCourseGroups,
    loadCourseRoster,
    loadCourseRubric,
    initializeCourseWorkspace,
    updateSelectedGroups,
    refreshCourseRoster,
    selectRosterStudent,
    submitRosterGrade,
    submitBulkGrades
  }
})

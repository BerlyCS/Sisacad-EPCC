import { ref } from 'vue'
import { gradeService, type CourseGroupSummary, type CourseRosterEntry, type ProfessorCourseSummary } from '@/services/gradeService'

const normalizeSelection = (groupIds: number[], groups: CourseGroupSummary[]): number[] => {
  if (!groupIds.length) {
    return []
  }
  const availableIds = new Set(groups.map(group => group.groupId))
  const sanitized = groupIds.filter(id => availableIds.has(id))
  if (sanitized.length) {
    return sanitized.slice(0, 1)
  }
  const fallback = groups[0]
  return fallback ? [fallback.groupId] : []
}

export const useProfessorAttendanceWorkspace = () => {
  const courses = ref<ProfessorCourseSummary[]>([])
  const coursesLoading = ref(false)
  const coursesError = ref('')
  const selectedCourse = ref<ProfessorCourseSummary | null>(null)

  const courseGroups = ref<CourseGroupSummary[]>([])
  const courseGroupsLoading = ref(false)
  const courseGroupsError = ref('')
  const selectedGroupIds = ref<number[]>([])

  const rosterEntries = ref<CourseRosterEntry[]>([])
  const rosterLoading = ref(false)
  const rosterError = ref('')

  const loadCourses = async () => {
    coursesLoading.value = true
    coursesError.value = ''
    try {
      const response = await gradeService.fetchProfessorCourseSummaries()
      courses.value = [...response].sort((a, b) => a.courseName.localeCompare(b.courseName))
    } catch (error) {
      console.error('Error while loading professor courses for attendance', error)
      courses.value = []
      coursesError.value = 'No se pudieron cargar tus cursos asignados'
    } finally {
      coursesLoading.value = false
    }
  }

  const resetCourseContext = () => {
    courseGroups.value = []
    courseGroupsError.value = ''
    selectedGroupIds.value = []
    rosterEntries.value = []
    rosterError.value = ''
  }

  const selectCourse = async (course: ProfessorCourseSummary | null) => {
    selectedCourse.value = course
    if (!course) {
      resetCourseContext()
      return
    }
    await loadCourseGroups(course.courseId)
  }

  const loadCourseGroups = async (courseId: number | null) => {
    if (!courseId) {
      resetCourseContext()
      return
    }
    courseGroupsLoading.value = true
    courseGroupsError.value = ''
    try {
      const groups = await gradeService.fetchCourseGroups(courseId)
      courseGroups.value = groups
      selectedGroupIds.value = groups.length && groups[0]?.groupId != null ? [groups[0].groupId] : []
      await loadCourseRoster()
    } catch (error) {
      console.error('Error while loading course groups for attendance', error)
      courseGroups.value = []
      selectedGroupIds.value = []
      courseGroupsError.value = 'No se pudieron obtener los grupos del curso'
      rosterEntries.value = []
    } finally {
      courseGroupsLoading.value = false
    }
  }

  const loadCourseRoster = async () => {
    const course = selectedCourse.value
    if (!course || !course.courseId) {
      rosterEntries.value = []
      return
    }
    rosterLoading.value = true
    rosterError.value = ''
    try {
      const groupIds = selectedGroupIds.value.length ? selectedGroupIds.value : undefined
      const roster = await gradeService.fetchCourseRoster(course.courseId, {
        groupIds,
        page: 0,
        size: 250
      })
      rosterEntries.value = roster.students
    } catch (error) {
      console.error('Error while loading roster for attendance', error)
      rosterEntries.value = []
      rosterError.value = 'No se pudo cargar la lista de estudiantes'
    } finally {
      rosterLoading.value = false
    }
  }

  const updateSelectedGroups = async (groupIds: number[]) => {
    selectedGroupIds.value = normalizeSelection(groupIds, courseGroups.value)
    await loadCourseRoster()
  }

  const refreshRoster = async () => {
    await loadCourseRoster()
  }

  return {
    courses,
    coursesLoading,
    coursesError,
    selectedCourse,
    courseGroups,
    courseGroupsLoading,
    courseGroupsError,
    selectedGroupIds,
    rosterEntries,
    rosterLoading,
    rosterError,
    loadCourses,
    selectCourse,
    updateSelectedGroups,
    refreshRoster
  }
}

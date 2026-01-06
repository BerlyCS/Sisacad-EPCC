import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

type CourseType = 'THEORY' | 'LAB'

const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio'
}

export interface Course {
  courseId: number
  courseCode: number | null
  name: string
  creditNumber: number | null
  groupLetter: string | null
  syllabusID: number | null
  semesterNumber: number | null
  courseType: CourseType
  labPrerequisiteCourseId: number | null
  labCapacity: number | null
  enrolledStudentIDs: number[]
  teacherIDs: number[]
  courseTypeLabel: string
}

export const useStudentCourseService = () => {
  const courses = ref<Course[]>([])
  const loading = ref(false)
  const error = ref('')

  const normalizeCourseType = (raw: unknown): CourseType => {
    const normalized = typeof raw === 'string' ? raw.toUpperCase() : 'THEORY'
    return normalized === 'LAB' ? 'LAB' : 'THEORY'
  }

  const normalizeGroupLetter = (raw: unknown): string | null => {
    if (raw == null) {
      return null
    }
    if (typeof raw === 'string') {
      const trimmed = raw.trim()
      if (!trimmed || trimmed === '\u0000') {
        return null
      }
      return trimmed
    }
    return null
  }

  const toNumberOrNull = (raw: unknown): number | null => {
    if (raw == null) {
      return null
    }
    const parsed = Number(raw)
    return Number.isFinite(parsed) ? parsed : null
  }

  const ensureNumberArray = (input: unknown): number[] =>
    Array.isArray(input)
      ? input
          .map((value: unknown) => Number(value))
          .filter((value: number) => Number.isFinite(value))
      : []

  const fetchMyCourses = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/students/my-courses`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error(`Error ${response.status}`)
      }

      const data: any[] = await response.json()

      if (!Array.isArray(data)) {
        courses.value = []
        return
      }

      const flattened: Course[] = []

      data.forEach((course: any) => {
        const courseId = toNumberOrNull(course.courseId ?? course.courseID) ?? 0
        const courseCode = toNumberOrNull(course.courseCode) ?? courseId
        const creditNumber = toNumberOrNull(course.creditNumber)
        const syllabusID = toNumberOrNull(course.syllabusID ?? course.syllabusId)
        const semesterNumber = toNumberOrNull(course.semesterNumber)
        const groups: any[] = Array.isArray(course.groups) ? course.groups : []

        const theoryGroup = groups.find(g => normalizeCourseType(g.courseType) === 'THEORY')

        flattened.push({
          courseId,
          courseCode,
          name: course.name ?? 'Curso',
          creditNumber,
          groupLetter: normalizeGroupLetter(theoryGroup?.groupLetter ?? theoryGroup?.letter),
          syllabusID,
          semesterNumber,
          courseType: 'THEORY',
          labPrerequisiteCourseId: null,
          labCapacity: null,
          enrolledStudentIDs: [],
          teacherIDs: ensureNumberArray(theoryGroup?.teacherIDs),
          courseTypeLabel: COURSE_TYPE_LABEL.THEORY
        })

        groups
          .filter(g => normalizeCourseType(g.courseType) === 'LAB')
          .forEach(labGroup => {
            const labGroupId = toNumberOrNull(labGroup.courseGroupId ?? labGroup.id ?? labGroup.courseId)
            const labCapacity = toNumberOrNull(labGroup.labCapacity ?? labGroup.maxCapacity)
            flattened.push({
              courseId: labGroupId ?? 0,
              courseCode: courseCode ?? labGroupId ?? courseId,
              name: course.name ?? 'Laboratorio',
              creditNumber,
              groupLetter: normalizeGroupLetter(labGroup.groupLetter ?? labGroup.letter),
              syllabusID,
              semesterNumber,
              courseType: 'LAB',
              labPrerequisiteCourseId: courseId,
              labCapacity,
              enrolledStudentIDs: ensureNumberArray(labGroup.enrolledStudentIDs),
              teacherIDs: ensureNumberArray(labGroup.teacherIDs),
              courseTypeLabel: COURSE_TYPE_LABEL.LAB
            })
          })
      })

      courses.value = flattened

    } catch (err) {
      error.value = 'No se pudieron cargar los cursos'
      console.error('Error fetching student courses:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    courses,
    loading,
    error,
    fetchMyCourses
  }
}
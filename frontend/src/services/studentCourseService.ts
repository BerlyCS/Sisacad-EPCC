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
  anio: number | null
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
      courses.value = Array.isArray(data)
        ? data.map((course: any) => {
            const courseId = toNumberOrNull(course.courseId ?? course.courseID) ?? 0
            const courseType = normalizeCourseType(course.courseType)
            const teacherIDs = ensureNumberArray(course.teacherIDs ?? [])
            const enrolledStudentIDs = ensureNumberArray(course.enrolledStudentIDs ?? [])
            const labCapacity = toNumberOrNull(course.labCapacity)
            return {
              courseId,
              courseCode: toNumberOrNull(course.courseCode) ?? courseId,
              name: course.name ?? 'Curso',
              creditNumber: toNumberOrNull(course.creditNumber),
              groupLetter: normalizeGroupLetter(course.groupLetter),
              syllabusID: toNumberOrNull(course.syllabusID),
              anio: toNumberOrNull(course.anio),
              courseType,
              labPrerequisiteCourseId: toNumberOrNull(course.labPrerequisiteCourseId),
              labCapacity: labCapacity,
              enrolledStudentIDs,
              teacherIDs,
              courseTypeLabel: COURSE_TYPE_LABEL[courseType]
            } as Course
          })
        : []

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
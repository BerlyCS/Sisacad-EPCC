import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export type CourseType = 'THEORY' | 'LAB'

const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio'
}

export interface Course {
  courseId: number
  courseCode: number
  name: string
  creditNumber: number
  groupLetter: string
  syllabusID: number | null
  courseType: CourseType
  labPrerequisiteCourseId: number | null
  enrolledStudentIDs: number[]
  teacherIDs: number[]
  courseTypeLabel: string
}

export interface CourseStudentSummary {
  documentoIdentidad: string
  cui: string
  nombres: string
  apellidoPaterno: string
  apellidoMaterno: string
  correoInstitucional: string
  anio: number | null
}

export interface CourseTopicSummary {
  name: string
  weight: number | null
}

export interface CourseContentSummary {
  name: string | null
  type: string | null
  url: string | null
  sizeBytes: number | null
}

export interface CourseSyllabusSummary {
  syllabusId: number
  content: CourseContentSummary | null
  topics: CourseTopicSummary[]
}

export interface CourseLabSummary {
  courseId: number
  courseCode: number | null
  name: string
  groupLetter: string
  courseTypeLabel: string
}

export interface CourseDetails {
  courseId: number
  courseCode: number | null
  name: string
  creditNumber: number
  groupLetter: string
  anio: number | null
  courseType: CourseType
  courseTypeLabel: string
  labPrerequisiteCourseId: number | null
  labCourse: CourseLabSummary | null
  syllabusId: number | null
  syllabus: CourseSyllabusSummary | null
  enrolledStudents: CourseStudentSummary[]
  enrolledCount: number
  teacherIds: number[]
}

const resolveCourseTypeLabel = (type?: CourseType | string | null): string => {
  if (!type) {
    return COURSE_TYPE_LABEL.THEORY
  }

  const normalized = typeof type === 'string' ? type.toUpperCase() : type
  return COURSE_TYPE_LABEL[normalized as CourseType] ?? COURSE_TYPE_LABEL.THEORY
}

export const useCourseService = () => {
  const courses = ref<Course[]>([])
  const loading = ref(false)
  const error = ref('')

  const courseDetails = ref<CourseDetails | null>(null)
  const courseDetailsLoading = ref(false)
  const courseDetailsError = ref('')

  const fetchCourses = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/courses`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al cargar los cursos')
      }

      const data: any[] = await response.json()
      courses.value = data.map(course => ({
        courseId: Number(course.courseId ?? course.courseID),
        courseCode: Number(course.courseCode ?? course.courseID),
        name: course.name ?? 'Curso',
        creditNumber: Number(course.creditNumber ?? 0),
        groupLetter: course.groupLetter ?? '',
        syllabusID: course.syllabusID != null ? Number(course.syllabusID) : null,
        courseType: (course.courseType ?? 'THEORY').toUpperCase() as CourseType,
        labPrerequisiteCourseId: course.labPrerequisiteCourseId != null ? Number(course.labPrerequisiteCourseId) : null,
        enrolledStudentIDs: Array.isArray(course.enrolledStudentIDs) ? course.enrolledStudentIDs : [],
        teacherIDs: Array.isArray(course.teacherIDs) ? course.teacherIDs : [],
        courseTypeLabel: COURSE_TYPE_LABEL[(course.courseType ?? 'THEORY').toUpperCase() as CourseType] ?? COURSE_TYPE_LABEL.THEORY
      }))
    } catch (err) {
      error.value = 'No se pudieron cargar los cursos'
      console.error('Error fetching courses:', err)
    } finally {
      loading.value = false
    }
  }

  const mapCourseDetailsResponse = (data: any): CourseDetails => {
    const courseType = (typeof data.courseType === 'string' ? data.courseType.toUpperCase() : 'THEORY') as CourseType

    const students: CourseStudentSummary[] = Array.isArray(data.enrolledStudents)
      ? data.enrolledStudents.map((student: any) => ({
          documentoIdentidad: student.documentoIdentidad ?? '',
          cui: student.cui ?? '',
          nombres: student.nombres ?? '',
          apellidoPaterno: student.apellidoPaterno ?? '',
          apellidoMaterno: student.apellidoMaterno ?? '',
          correoInstitucional: student.correoInstitucional ?? '',
          anio: student.anio != null ? Number(student.anio) : null
        }))
      : []

    const labCourse: CourseLabSummary | null = data.labCourse
      ? {
          courseId: Number(data.labCourse.courseId),
          courseCode: data.labCourse.courseCode != null ? Number(data.labCourse.courseCode) : null,
          name: data.labCourse.name ?? 'Curso de laboratorio',
          groupLetter: data.labCourse.groupLetter ?? '',
          courseTypeLabel: data.labCourse.courseTypeLabel ?? resolveCourseTypeLabel('LAB')
        }
      : null

    const syllabus: CourseSyllabusSummary | null = data.syllabus
      ? {
          syllabusId: Number(data.syllabus.syllabusId),
          content: data.syllabus.content
            ? {
                name: data.syllabus.content.name ?? null,
                type: data.syllabus.content.type ?? null,
                url: data.syllabus.content.url ?? null,
                sizeBytes: data.syllabus.content.sizeBytes != null
                  ? Number(data.syllabus.content.sizeBytes)
                  : null
              }
            : null,
          topics: Array.isArray(data.syllabus.topics)
            ? data.syllabus.topics.map((topic: any) => ({
                name: topic.name ?? 'Tema',
                weight: topic.weight != null ? Number(topic.weight) : null
              }))
            : []
        }
      : null

      const teacherIds: number[] = Array.isArray(data.teacherIds)
        ? data.teacherIds
            .map((id: unknown) => Number(id))
            .filter((id: number) => Number.isFinite(id))
        : []

    return {
      courseId: Number(data.courseId),
      courseCode: data.courseCode != null ? Number(data.courseCode) : null,
      name: data.name ?? 'Curso',
      creditNumber: data.creditNumber != null ? Number(data.creditNumber) : 0,
      groupLetter: data.groupLetter ?? '',
      anio: data.anio != null ? Number(data.anio) : null,
      courseType,
      courseTypeLabel: data.courseTypeLabel ?? resolveCourseTypeLabel(courseType),
      labPrerequisiteCourseId: data.labPrerequisiteCourseId != null ? Number(data.labPrerequisiteCourseId) : null,
      labCourse,
      syllabusId: data.syllabusId != null ? Number(data.syllabusId) : null,
      syllabus,
      enrolledStudents: students,
      enrolledCount: data.enrolledCount != null ? Number(data.enrolledCount) : students.length,
      teacherIds
    }
  }

  const fetchCourseDetails = async (courseId: number | string) => {
    if (courseId === null || courseId === undefined || courseId === '') {
      courseDetailsError.value = 'Identificador del curso inválido'
      courseDetails.value = null
      return
    }

    courseDetailsLoading.value = true
    courseDetailsError.value = ''
    courseDetails.value = null

    try {
      const response = await fetch(`${API_BASE_URL}/courses/${courseId}`, {
        credentials: 'include'
      })

      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('No se encontró la información del curso')
        }
        throw new Error('Error al cargar la información del curso')
      }

      const data = await response.json()
      courseDetails.value = mapCourseDetailsResponse(data)
    } catch (err) {
      courseDetailsError.value = err instanceof Error
        ? err.message
        : 'No se pudo cargar la información del curso'
      console.error('Error fetching course details:', err)
    } finally {
      courseDetailsLoading.value = false
    }
  }

  return {
    courses,
    loading,
    error,
    fetchCourses,
    courseDetails,
    courseDetailsLoading,
    courseDetailsError,
    fetchCourseDetails
  }
}

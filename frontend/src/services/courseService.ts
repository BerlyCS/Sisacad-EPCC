import { ref } from 'vue'
import type { TopicScheduleStatus } from './syllabusService'

const API_BASE_URL = 'http://localhost:8080/api'

export type CourseType = 'THEORY' | 'LAB' | 'PRACTICE'

const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio',
  PRACTICE: 'Práctica'
}

export interface CourseScheduleSlotSummary {
  scheduleId: number | null
  dayOfWeek: string
  startTime: string
  endTime: string
  classroomId: number | null
  sequenceOrder: number | null
}

export interface Course {
  courseId: number
  courseCode: number | null
  name: string | null
  credits: number | null
  syllabusId: number | null
  labHours: number | null
  practiceHours: number | null
  theoryHours: number | null
  semesterNumber: number | null
  enrolledStudentIDs?: number[]
  teacherIDs?: number[]
}

export interface CourseGroupSummary {
  groupId: number
  courseId: number
  letter: string
  type: CourseType
  typeLabel: string
  maxCapacity: number | null
  availableCapacity: number | null
  teacherId: number | null
  scheduleSlots?: CourseScheduleSlotSummary[]
}

export interface CourseStudentSummary {
  userId: number
  cui: string
  firstNames: string
  paternalSurname: string
  maternalSurname: string
  institutionalEmail: string
  enrollmentYear: number | null
}

export interface CourseTopicSummary {
  name: string
  weight: number | null
  sessionDate: string | null
  status: TopicScheduleStatus
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

export interface CourseScheduleSlotPayload {
  dayOfWeek: string
  startTime: string
  endTime: string
  classroomId: number | null
}

export interface CreateCourseGroupPayload {
  letter: string
  type: CourseType
  capacity: number
  scheduleSlots: CourseScheduleSlotPayload[]
}

export interface CourseTimeSlot {
  startTime: string
  endTime: string
  label: string
}

export const useCourseService = () => {
  const courses = ref<Course[]>([])
  const loading = ref(false)
  const error = ref('')

  const courseDetails = ref<CourseDetails | null>(null)
  const courseDetailsLoading = ref(false)
  const courseDetailsError = ref('')
  const courseGroups = ref<CourseGroupSummary[]>([])
  const courseGroupsLoading = ref(false)
  const courseGroupsError = ref('')
  const courseTimeSlots = ref<CourseTimeSlot[]>([])
  const courseTimeSlotsLoading = ref(false)
  const courseTimeSlotsError = ref('')

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
        courseCode: course.courseCode != null ? Number(course.courseCode) : null,
        name: course.name ?? null,
        credits: course.credits != null ? Number(course.credits) : null,
        syllabusId: course.syllabusId != null ? Number(course.syllabusId) : null,
        labHours: course.labHours != null ? Number(course.labHours) : null,
        practiceHours: course.practiceHours != null ? Number(course.practiceHours) : null,
        theoryHours: course.theoryHours != null ? Number(course.theoryHours) : null,
        semesterNumber: course.semesterNumber != null ? Number(course.semesterNumber) : null,
        enrolledStudentIDs: [],
        teacherIDs: []
      }))
    } catch (err) {
      error.value = 'No se pudieron cargar los cursos'
      console.error('Error fetching courses:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchCourseGroups = async (courseId: number | null | undefined) => {
    if (!courseId) {
      courseGroups.value = []
      return
    }

    courseGroupsLoading.value = true
    courseGroupsError.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/courses/${courseId}/groups`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al cargar los grupos del curso')
      }

      const data: CourseGroupSummary[] = await response.json()
      courseGroups.value = data
    } catch (err) {
      courseGroupsError.value = 'No se pudieron cargar los grupos del curso'
      console.error('Error fetching course groups:', err)
      courseGroups.value = []
    } finally {
      courseGroupsLoading.value = false
    }
  }

  const mapCourseDetailsResponse = (data: any): CourseDetails => {
    const courseType = (typeof data.courseType === 'string' ? data.courseType.toUpperCase() : 'THEORY') as CourseType

    const students: CourseStudentSummary[] = Array.isArray(data.enrolledStudents)
      ? data.enrolledStudents.map((student: any) => ({
          userId: student.userId ?? 0,
          cui: student.cui ?? '',
          firstNames: student.firstNames ?? '',
          paternalSurname: student.paternalSurname ?? '',
          maternalSurname: student.maternalSurname ?? '',
          institutionalEmail: student.institutionalEmail ?? '',
          enrollmentYear: student.enrollmentYear != null ? Number(student.enrollmentYear) : null
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
                weight: topic.weight != null ? Number(topic.weight) : null,
                sessionDate: topic.sessionDate ?? null,
                status: ((topic.status ?? 'UNSCHEDULED').toString().toUpperCase()) as TopicScheduleStatus
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

  const assignProfessorToCourse = async (courseId: number, groupId: number, professorId: number) => {
    if (!courseId || !groupId || !professorId) {
      throw new Error('Curso o docente inválido')
    }

    const response = await fetch(`${API_BASE_URL}/courses/${courseId}/professors/${professorId}?groupId=${groupId}`, {
      method: 'POST',
      credentials: 'include'
    })

    if (!response.ok) {
      const body = await response.json().catch(() => ({}))
      const message = body?.message || body?.error || 'No se pudo asignar el docente'
      throw new Error(message)
    }

    return response.json()
  }

  const createCourseGroup = async (courseId: number, payload: CreateCourseGroupPayload) => {
    if (!courseId) {
      throw new Error('Curso inválido')
    }

    const response = await fetch(`${API_BASE_URL}/courses/${courseId}/groups`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify({
        letter: payload.letter,
        type: payload.type,
        capacity: payload.capacity,
        scheduleSlots: payload.scheduleSlots
      })
    })

    if (!response.ok) {
      const body = await response.json().catch(() => ({}))
      const message = body?.message || body?.error || 'No se pudo crear el grupo'
      throw new Error(message)
    }

    return response.json()
  }

  const fetchCourseTimeSlots = async () => {
    if (courseTimeSlotsLoading.value) return

    courseTimeSlotsLoading.value = true
    courseTimeSlotsError.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/courses/timeslots`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al obtener los bloques horarios')
      }

      const data: Array<{ startTime: string; endTime: string }> = await response.json()
      courseTimeSlots.value = data.map(slot => ({
        startTime: slot.startTime,
        endTime: slot.endTime,
        label: `${slot.startTime} - ${slot.endTime}`
      }))
    } catch (err) {
      courseTimeSlotsError.value = 'No se pudieron cargar los bloques horarios'
      console.error('Error fetching course time slots:', err)
      courseTimeSlots.value = []
    } finally {
      courseTimeSlotsLoading.value = false
    }
  }

  const createCourse = async (course: Omit<Course, 'courseId' | 'syllabusId'>): Promise<Course> => {
    const response = await fetch(`${API_BASE_URL}/courses`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify(course)
    })

    if (!response.ok) {
      const body = await response.json().catch(() => ({}))
      const message = body?.message || body?.error || 'No se pudo crear el curso'
      throw new Error(message)
    }

    return response.json()
  }

  const removeProfessorFromCourse = async (courseId: number, groupId: number, professorId: number) => {
    if (!courseId || !groupId || !professorId) {
      throw new Error('Curso o docente inválido')
    }

    const response = await fetch(`${API_BASE_URL}/courses/${courseId}/professors/${professorId}?groupId=${groupId}`, {
      method: 'DELETE',
      credentials: 'include'
    })

    if (!response.ok) {
      const body = await response.json().catch(() => ({}))
      const message = body?.message || body?.error || 'No se pudo quitar el docente'
      throw new Error(message)
    }

    return response.json()
  }

  return {
    courses,
    loading,
    error,
    fetchCourses,
    courseDetails,
    courseDetailsLoading,
    courseDetailsError,
    courseGroups,
    courseGroupsLoading,
    courseGroupsError,
    fetchCourseGroups,
    fetchCourseDetails,
    assignProfessorToCourse,
    removeProfessorFromCourse,
    createCourse,
    createCourseGroup,
    courseTimeSlots,
    courseTimeSlotsLoading,
    courseTimeSlotsError,
    fetchCourseTimeSlots
  }
}

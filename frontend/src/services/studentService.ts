import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Student {
  userId: number
  cui: string
  firstNames: string
  paternalSurname: string
  maternalSurname: string
  institutionalEmail: string
  enrollmentYear?: number | null
}

type CourseType = 'THEORY' | 'LAB'

interface StudentCourseSummary {
  courseId: number
  courseCode: number
  name: string
  groupLetter: string
  courseType: CourseType
  courseTypeLabel: string
  creditNumber?: number | null
}

export interface StudentScheduleEntry {
  courseId: number
  courseCode: number
  courseName: string
  courseType: CourseType
  dayOfWeek: string
  startTime: string
  endTime: string
  classroomName: string
}

export interface StudentProfile {
  student: Student
  courses: StudentCourseSummary[]
  schedule: StudentScheduleEntry[]
}

const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio'
}

export const useStudentService = () => {
  const students = ref<Student[]>([])
  const loading = ref(false)
  const error = ref('')
  const studentProfile = ref<StudentProfile | null>(null)
  const profileLoading = ref(false)
  const profileError = ref('')

  const fetchStudentsSorted = async (sortBy: string = 'dni', direction: string = 'asc') => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/students/list?sortBy=${sortBy}&direction=${direction}`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('Error al cargar los estudiantes')
      }
      
      students.value = await response.json()
    } catch (err) {
      error.value = 'No se pudieron cargar los estudiantes'
      console.error('Error fetching students:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchStudentProfile = async (cui: string) => {
    if (!cui) {
      profileError.value = 'CUI inválido'
      return
    }

    profileLoading.value = true
    profileError.value = ''
    studentProfile.value = null

    try {
      const response = await fetch(`${API_BASE_URL}/students/profile/${encodeURIComponent(cui)}`, {
        credentials: 'include'
      })

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('No tienes permisos para ver este perfil')
        }

        if (response.status === 404) {
          throw new Error('No se encontró el estudiante solicitado')
        }

        throw new Error('Error al cargar el perfil del estudiante')
      }

      const data = await response.json()

      const studentData: Student = {
        userId: data.userId ?? 0,
        cui: data.cui ?? cui,
        firstNames: data.firstNames ?? '',
        paternalSurname: data.paternalSurname ?? '',
        maternalSurname: data.maternalSurname ?? '',
        institutionalEmail: data.institutionalEmail ?? '',
        enrollmentYear: data.enrollmentYear ?? null
      }

      studentProfile.value = {
        student: studentData,
        courses: [],
        schedule: []
      }
    } catch (err) {
      profileError.value = err instanceof Error ? err.message : 'No se pudo cargar el perfil del estudiante'
      console.error('Error fetching student profile:', err)
    } finally {
      profileLoading.value = false
    }
  }

  const fetchMyProfile = async () => {
    profileLoading.value = true
    profileError.value = ''
    studentProfile.value = null

    try {
      const response = await fetch(`${API_BASE_URL}/students/my-profile`, {
        credentials: 'include'
      })

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error('No tienes permisos para ver este perfil')
        }

        if (response.status === 404) {
          throw new Error('No se encontró el estudiante solicitado')
        }

        throw new Error('Error al cargar el perfil del estudiante')
      }

      const data = await response.json()

      const studentData: Student = {
        userId: data.userId ?? 0,
        cui: data.cui ?? '',
        firstNames: data.firstNames ?? '',
        paternalSurname: data.paternalSurname ?? '',
        maternalSurname: data.maternalSurname ?? '',
        institutionalEmail: data.institutionalEmail ?? '',
        enrollmentYear: data.enrollmentYear ?? null
      }

      studentProfile.value = {
        student: studentData,
        courses: [],
        schedule: []
      }
    } catch (err) {
      profileError.value = err instanceof Error ? err.message : 'No se pudo cargar el perfil del estudiante'
      console.error('Error fetching student profile:', err)
    } finally {
      profileLoading.value = false
    }
  }

  const fetchStudentCourses = async (cui: string) => {
    if (!cui) return

    try {
      const response = await fetch(`${API_BASE_URL}/students/${encodeURIComponent(cui)}/courses`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al cargar los cursos')
      }

      const data = await response.json()

      const parsedCourses: StudentCourseSummary[] = Array.isArray(data)
        ? data.map((course: any) => {
            const type = (course.courseType || 'THEORY').toUpperCase() as CourseType
            return {
              courseId: Number(course.courseId ?? course.courseID ?? 0),
              courseCode: Number(course.courseCode ?? course.courseId ?? 0),
              name: course.name ?? 'Curso sin nombre',
              groupLetter: course.groupLetter ?? '-',
              courseType: type,
              courseTypeLabel: COURSE_TYPE_LABEL[type] ?? type,
              creditNumber: course.creditNumber ?? null
            }
          })
        : []

      if (studentProfile.value) {
        studentProfile.value.courses = parsedCourses
      }
    } catch (err) {
      console.error('Error fetching student courses:', err)
      throw err instanceof Error ? err : new Error('No se pudieron cargar los cursos del estudiante')
    }
  }

  const fetchMyCourses = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/students/my-courses`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al cargar los cursos')
      }

      const data = await response.json()

      const parsedCourses: StudentCourseSummary[] = Array.isArray(data)
        ? data.map((course: any) => {
            const type = (course.courseType || 'THEORY').toUpperCase() as CourseType
            return {
              courseId: Number(course.courseId ?? course.courseID ?? 0),
              courseCode: Number(course.courseCode ?? course.courseId ?? 0),
              name: course.name ?? 'Curso sin nombre',
              groupLetter: course.groupLetter ?? '-',
              courseType: type,
              courseTypeLabel: COURSE_TYPE_LABEL[type] ?? type,
              creditNumber: course.creditNumber ?? null
            }
          })
        : []

      if (studentProfile.value) {
        studentProfile.value.courses = parsedCourses
      }
    } catch (err) {
      console.error('Error fetching student courses:', err)
      throw err instanceof Error ? err : new Error('No se pudieron cargar los cursos')
    }
  }

  const fetchStudentSchedule = async (cui: string) => {
    if (!cui) return

    try {
      const response = await fetch(`${API_BASE_URL}/students/${encodeURIComponent(cui)}/schedule`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al cargar el horario')
      }

      const data = await response.json()

      const parsedSchedule: StudentScheduleEntry[] = Array.isArray(data)
        ? data.map((entry: any) => ({
            courseId: Number(entry.courseId ?? 0),
            courseCode: Number(entry.courseCode ?? 0),
            courseName: entry.courseName ?? 'Curso',
            courseType: (entry.courseType || 'THEORY').toUpperCase() as CourseType,
            dayOfWeek: entry.dayOfWeek ?? '',
            startTime: entry.startTime ?? '',
            endTime: entry.endTime ?? '',
            classroomName: entry.classroomName ?? ''
          }))
        : []

      if (studentProfile.value) {
        studentProfile.value.schedule = parsedSchedule
      }
    } catch (err) {
      console.error('Error fetching student schedule:', err)
      throw err instanceof Error ? err : new Error('No se pudo cargar el horario del estudiante')
    }
  }

  const fetchMySchedule = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/students/my-schedule`, {
        credentials: 'include'
      })

      if (!response.ok) {
        throw new Error('Error al cargar el horario')
      }

      const data = await response.json()

      const parsedSchedule: StudentScheduleEntry[] = Array.isArray(data)
        ? data.map((entry: any) => ({
            courseId: Number(entry.courseId ?? 0),
            courseCode: Number(entry.courseCode ?? 0),
            courseName: entry.courseName ?? 'Curso',
            courseType: (entry.courseType || 'THEORY').toUpperCase() as CourseType,
            dayOfWeek: entry.dayOfWeek ?? '',
            startTime: entry.startTime ?? '',
            endTime: entry.endTime ?? '',
            classroomName: entry.classroomName ?? ''
          }))
        : []

      if (studentProfile.value) {
        studentProfile.value.schedule = parsedSchedule
      }
    } catch (err) {
      console.error('Error fetching student schedule:', err)
      throw err instanceof Error ? err : new Error('No se pudo cargar el horario del estudiante')
    }
  }

  return {
    students,
    loading,
    error,
    studentProfile,
    profileLoading,
    profileError,
    fetchStudentsSorted,
    fetchStudentProfile,
    fetchMyProfile,
    fetchStudentCourses,
    fetchMyCourses,
    fetchStudentSchedule,
    fetchMySchedule
  }
}
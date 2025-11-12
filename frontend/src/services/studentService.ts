import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Student {
  documentoIdentidad: string
  cui: string
  nombres: string
  apellidoPaterno: string
  apellidoMaterno: string
  correoInstitucional: string
  anio?: number | null
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

      const parsedCourses: StudentCourseSummary[] = Array.isArray(data.courses)
        ? data.courses.map((course: any) => {
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

      const parsedSchedule: StudentScheduleEntry[] = Array.isArray(data.schedule)
        ? data.schedule.map((entry: any) => ({
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

      const studentData: Student = {
        documentoIdentidad: data.student?.documentoIdentidad ?? '',
        cui: data.student?.cui ?? cui,
        nombres: data.student?.nombres ?? '',
        apellidoPaterno: data.student?.apellidoPaterno ?? '',
        apellidoMaterno: data.student?.apellidoMaterno ?? '',
        correoInstitucional: data.student?.correoInstitucional ?? '',
        anio: data.student?.anio ?? null
      }

      studentProfile.value = {
        student: studentData,
        courses: parsedCourses,
        schedule: parsedSchedule
      }
    } catch (err) {
      profileError.value = err instanceof Error ? err.message : 'No se pudo cargar el perfil del estudiante'
      console.error('Error fetching student profile:', err)
    } finally {
      profileLoading.value = false
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
    fetchStudentProfile
  }
}
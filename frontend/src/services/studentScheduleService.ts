import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

type CourseType = 'THEORY' | 'LAB'

const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio'
}

export interface StudentScheduleEntry {
  courseId: number
  courseCode: number
  courseName: string
  courseType: CourseType
  courseTypeLabel: string
  dayOfWeek: string
  startTime: string
  endTime: string
  classroomName: string
}

export const useStudentScheduleService = () => {
  const schedule = ref<StudentScheduleEntry[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchMySchedule = async () => {
    loading.value = true
    error.value = ''

    try {
      const response = await fetch(`${API_BASE_URL}/students/my-schedule`, {
        credentials: 'include'
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText || 'Error al cargar el horario')
      }

      const data: any[] = await response.json()
      schedule.value = data.map(entry => {
        const type = (entry.courseType as CourseType) || 'THEORY'
        return {
          courseId: entry.courseId,
          courseCode: entry.courseCode ?? entry.courseId,
          courseName: entry.courseName,
          courseType: type,
          courseTypeLabel: COURSE_TYPE_LABEL[type],
          dayOfWeek: entry.dayOfWeek,
          startTime: entry.startTime,
          endTime: entry.endTime,
          classroomName: entry.classroomName
        }
      })
    } catch (err) {
      console.error('Frontend: Error fetching student schedule:', err)
      error.value = 'No se pudo cargar el horario'
    } finally {
      loading.value = false
    }
  }

  return {
    schedule,
    loading,
    error,
    fetchMySchedule
  }
}

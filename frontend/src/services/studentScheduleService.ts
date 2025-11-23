import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

type CourseType = 'THEORY' | 'LAB'

export interface TimeSlot {
  startTime: string
  endTime: string
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

export const useStudentScheduleService = () => {
  const schedule = ref<StudentScheduleEntry[]>([])
  const timeSlots = ref<TimeSlot[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchMySchedule = async () => {
    loading.value = true
    error.value = ''

    try {
      const [slotsResponse, scheduleResponse] = await Promise.all([
        fetch(`${API_BASE_URL}/courses/timeslots`, { credentials: 'include' }),
        fetch(`${API_BASE_URL}/students/my-schedule`, { credentials: 'include' })
      ])

      if (!slotsResponse.ok) {
        throw new Error('Error al cargar los horarios')
      }
      
      if (!scheduleResponse.ok) {
        throw new Error('Error al cargar el horario')
      }

      const slotsData = await slotsResponse.json()
      const scheduleData = await scheduleResponse.json()
      
      timeSlots.value = slotsData || []
      schedule.value = scheduleData.map((entry: any) => ({
        courseId: Number(entry.courseId ?? 0),
        courseCode: Number(entry.courseCode ?? 0),
        courseName: entry.courseName ?? 'Curso',
        courseType: (entry.courseType || 'THEORY').toUpperCase() as CourseType,
        dayOfWeek: entry.dayOfWeek ?? '',
        startTime: entry.startTime ?? '',
        endTime: entry.endTime ?? '',
        classroomName: entry.classroomName ?? ''
      }))
    } catch (err) {
      console.error('Frontend: Error fetching schedule:', err)
      error.value = 'No se pudo cargar el horario'
    } finally {
      loading.value = false
    }
  }

  return {
    schedule,
    timeSlots,
    loading,
    error,
    fetchMySchedule
  }
}
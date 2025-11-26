import { ref } from 'vue'
import { reservationService, type Reservation } from './reservationService'

const API_BASE_URL = 'http://localhost:8080/api'

export interface TimeSlot {
  startTime: string
  endTime: string
}

export interface ProfessorScheduleEntry {
  courseId: number | null
  courseCode: number | null
  courseName: string
  groupLetter?: string
  courseType?: string
  dayOfWeek: string
  startTime: string
  endTime: string
  classroomName?: string
}

export interface ProfessorReservationEntry {
  id: number
  purpose: string
  classroomName: string
  reservationDate: string
  startTime: string
  endTime: string
  dayOfWeek: string
}

const mapReservation = (reservation: Reservation): ProfessorReservationEntry => ({
  id: reservation.id ?? 0,
  purpose: reservation.purpose ?? 'Reserva',
  classroomName: reservation.classroomName ?? 'Aula no asignada',
  reservationDate: reservation.reservationDate ?? '',
  startTime: reservation.schedule?.startTime ?? '',
  endTime: reservation.schedule?.endTime ?? '',
  dayOfWeek: reservation.schedule?.dayOfWeek ?? ''
})

export const useProfessorScheduleService = () => {
  const schedule = ref<ProfessorScheduleEntry[]>([])
  const timeSlots = ref<TimeSlot[]>([])
  const reservations = ref<ProfessorReservationEntry[]>([])
  const scheduleLoading = ref(false)
  const reservationsLoading = ref(false)
  const scheduleError = ref('')
  const reservationsError = ref('')

  const fetchSchedule = async () => {
    scheduleLoading.value = true
    scheduleError.value = ''

    try {
      const [slotResponse, scheduleResponse] = await Promise.all([
        fetch(`${API_BASE_URL}/courses/timeslots`, { credentials: 'include' }),
        fetch(`${API_BASE_URL}/professors/me/schedule`, { credentials: 'include' })
      ])

      if (!slotResponse.ok) {
        throw new Error('No se pudo obtener los bloques de horario')
      }

      if (!scheduleResponse.ok) {
        throw new Error('No se pudo obtener el horario del profesor')
      }

      const slotData = await slotResponse.json()
      const scheduleData = await scheduleResponse.json()

      timeSlots.value = slotData || []
      schedule.value = (scheduleData || []).map((entry: any) => ({
        courseId: entry.courseId ?? null,
        courseCode: entry.courseCode ?? null,
        courseName: entry.courseName ?? 'Curso',
        groupLetter: entry.groupLetter ?? '',
        courseType: entry.courseType ?? '',
        dayOfWeek: entry.dayOfWeek ?? '',
        startTime: entry.startTime ?? '',
        endTime: entry.endTime ?? '',
        classroomName: entry.classroomName ?? 'Aula por confirmar'
      }))
    } catch (error) {
      console.error('Error loading professor schedule', error)
      scheduleError.value = 'No se pudo cargar el horario asignado'
    } finally {
      scheduleLoading.value = false
    }
  }

  const fetchReservations = async () => {
    reservationsLoading.value = true
    reservationsError.value = ''

    try {
      const data = await reservationService.getMyReservations()
      reservations.value = data.map(mapReservation)
    } catch (error) {
      console.error('Error loading professor reservations', error)
      reservationsError.value = 'No se pudo cargar las reservas realizadas'
    } finally {
      reservationsLoading.value = false
    }
  }

  return {
    schedule,
    timeSlots,
    reservations,
    scheduleLoading,
    reservationsLoading,
    scheduleError,
    reservationsError,
    fetchSchedule,
    fetchReservations
  }
}

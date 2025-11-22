import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface TheoryCourseSummary {
  courseId: number
  courseCode: number | null
  name: string
  groupLetter: string | null
  anio: number | null
  creditNumber: number | null
}

export interface LabScheduleSlot {
  classroomName: string
  dayOfWeek: string
  startTime: string
  endTime: string
}

export interface ClassroomOption {
  classroomId: number
  name: string
  building: string | null
  floor: number | null
  number: number | null
  capacity: number | null
  classroomType: string | null
  lab: boolean
}

export interface LabSection {
  courseId: number
  name: string
  groupLetter: string | null
  labCapacity: number
  enrolledCount: number
  remainingSeats: number
  theoryCourseId: number
  courseTypeLabel: string
  scheduleSlots: LabScheduleSlot[]
}

export interface LabSlotSuggestion {
  classroomName: string
  dayOfWeek: string
  startTime: string
  endTime: string
  groupLetter?: string | null
  courseName?: string | null
}

export interface CreateLabSectionPayload {
  theoryCourseId: number
  name: string
  groupLetter: string
  labCapacity: number
  scheduleSlots: LabScheduleSlot[]
}

export interface UpdateLabSectionPayload {
  name?: string
  groupLetter?: string
  labCapacity?: number
  scheduleSlots?: LabScheduleSlot[]
}

const toNumberOrNull = (value: unknown): number | null => {
  if (value === null || value === undefined) {
    return null
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const normalizeGroupLetter = (value: unknown): string | null => {
  if (typeof value !== 'string') {
    return null
  }
  const trimmed = value.trim()
  return trimmed === '' || trimmed === '\u0000' ? null : trimmed
}

const handleResponse = async <T>(response: Response, defaultMessage: string): Promise<T> => {
  const payload = await response.json().catch(() => ({}))
  if (!response.ok) {
    const message = (payload as { message?: string }).message || defaultMessage
    throw new Error(message)
  }
  return payload as T
}

const fetchTheoryCourses = async (): Promise<TheoryCourseSummary[]> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs/theory`, {
    credentials: 'include'
  })

  const data = await handleResponse<any[]>(response, 'No se pudieron cargar los cursos teóricos')
  return Array.isArray(data)
    ? data.map(course => ({
        courseId: Number(course.courseId ?? course.courseID),
        courseCode: toNumberOrNull(course.courseCode),
        name: course.name ?? 'Curso',
        groupLetter: normalizeGroupLetter(course.groupLetter),
        anio: toNumberOrNull(course.anio),
        creditNumber: toNumberOrNull(course.creditNumber)
      }))
    : []
}

const fetchLabSections = async (theoryCourseId: number): Promise<LabSection[]> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs/theory/${theoryCourseId}`, {
    credentials: 'include'
  })
  const data = await handleResponse<any[]>(response, 'No se pudieron cargar los laboratorios')
  return Array.isArray(data)
    ? data.map(section => ({
        courseId: Number(section.courseId),
        name: section.name ?? 'Laboratorio',
        groupLetter: normalizeGroupLetter(section.groupLetter),
        labCapacity: Number(section.labCapacity ?? 0),
        enrolledCount: Number(section.enrolledCount ?? 0),
        remainingSeats: Number(section.remainingSeats ?? 0),
        theoryCourseId: Number(section.theoryCourseId ?? section.labPrerequisiteCourseId),
        courseTypeLabel: section.courseTypeLabel ?? 'Laboratorio',
        scheduleSlots: Array.isArray(section.scheduleSlots) ? section.scheduleSlots : []
      }))
    : []
}

const fetchClassrooms = async (labOnly = true): Promise<ClassroomOption[]> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs/classrooms?labOnly=${labOnly}`, {
    credentials: 'include'
  })

  const data = await handleResponse<any[]>(response, 'No se pudieron cargar las aulas disponibles')
  return Array.isArray(data)
    ? data.map(option => ({
        classroomId: Number(option.classroomId ?? option.id ?? 0),
        name: option.name ?? 'Aula',
        building: option.building ?? null,
        floor: typeof option.floor === 'number' ? option.floor : null,
        number: typeof option.number === 'number' ? option.number : null,
        capacity: typeof option.capacity === 'number' ? option.capacity : null,
        classroomType: option.classroomType ?? null,
        lab: Boolean(option.lab)
      }))
    : []
}

const fetchSlotSuggestions = async (theoryCourseId: number): Promise<LabSlotSuggestion[]> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs/theory/${theoryCourseId}/suggested-slots`, {
    credentials: 'include'
  })
  const data = await handleResponse<any[]>(response, 'No se pudieron cargar los horarios sugeridos')
  return Array.isArray(data)
    ? data.map(slot => ({
        classroomName: slot.classroomName ?? '',
        dayOfWeek: slot.dayOfWeek ?? '',
        startTime: slot.startTime ?? '',
        endTime: slot.endTime ?? '',
        groupLetter: normalizeGroupLetter(slot.groupLetter),
        courseName: slot.courseName ?? ''
      }))
    : []
}

const createLabSection = async (payload: CreateLabSectionPayload): Promise<LabSection> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })
  return handleResponse<LabSection>(response, 'No se pudo crear el laboratorio')
}

const updateLabSection = async (labCourseId: number, payload: UpdateLabSectionPayload): Promise<LabSection> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs/${labCourseId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  })
  return handleResponse<LabSection>(response, 'No se pudo actualizar el laboratorio')
}

const deleteLabSection = async (labCourseId: number): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/secretary/labs/${labCourseId}`, {
    method: 'DELETE',
    credentials: 'include'
  })

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}))
    const message = (payload as { message?: string }).message || 'No se pudo eliminar el laboratorio'
    throw new Error(message)
  }
}

export const useSecretaryLabService = () => {
  const theoryCourses = ref<TheoryCourseSummary[]>([])
  const theoryLoading = ref(false)
  const theoryError = ref('')

  const loadTheoryCourses = async () => {
    theoryLoading.value = true
    theoryError.value = ''
    try {
      theoryCourses.value = await fetchTheoryCourses()
    } catch (error) {
      theoryError.value = error instanceof Error ? error.message : 'No se pudieron cargar los cursos teóricos'
    } finally {
      theoryLoading.value = false
    }
  }

  return {
    theoryCourses,
    theoryLoading,
    theoryError,
    loadTheoryCourses,
    fetchClassrooms,
    fetchLabSections,
    fetchSlotSuggestions,
    createLabSection,
    updateLabSection,
    deleteLabSection
  }
}

const API_BASE_URL = 'http://localhost:8080/api'

export interface EnrollmentPayload {
  studentId?: number
  studentCui?: string
}

export interface EnrollmentValidationResult {
  allowed: boolean
  errorCode: string | null
  message: string
  courseId: number | null
  remainingSeats: number | null
}

export interface LabSection {
  courseId: number
  courseCode: number | null
  name: string
  groupLetter: string | null
  labCapacity: number | null
  enrolledCount: number
  remainingSeats: number | null
  labPrerequisiteCourseId: number | null
  courseTypeLabel: string
}

const LAB_LABEL = 'Laboratorio'

const toNumberOrNull = (raw: unknown): number | null => {
  if (raw == null) {
    return null
  }
  const parsed = Number(raw)
  return Number.isFinite(parsed) ? parsed : null
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

const mapLabSection = (payload: any): LabSection => {
  const groupId = toNumberOrNull(payload?.id ?? payload?.groupId ?? payload?.courseGroupId) ?? 0
  const theoryCourseId = toNumberOrNull(payload?.courseId ?? payload?.course?.courseId ?? payload?.course?.id)
  const courseCode = toNumberOrNull(payload?.courseCode ?? payload?.course?.courseCode) ?? theoryCourseId ?? groupId
  const labCapacity = toNumberOrNull(payload?.labCapacity ?? payload?.maxCapacity)
  const enrolledCount = Array.isArray(payload?.enrolledStudentIDs)
    ? payload.enrolledStudentIDs.length
    : toNumberOrNull(payload?.enrolledCount ?? payload?.currentEnrollments)

  const remainingSeats = labCapacity != null
    ? Math.max(labCapacity - (Number.isFinite(enrolledCount ?? 0) ? Number(enrolledCount) : 0), 0)
    : toNumberOrNull(payload?.remainingSeats ?? payload?.availableCapacity)

  return {
    courseId: groupId,
    courseCode,
    name: payload?.name ?? payload?.course?.name ?? 'Laboratorio',
    groupLetter: normalizeGroupLetter(payload?.groupLetter ?? payload?.letter),
    labCapacity,
    enrolledCount: Number(enrolledCount) || 0,
    remainingSeats,
    labPrerequisiteCourseId: theoryCourseId,
    courseTypeLabel: payload?.courseTypeLabel ?? LAB_LABEL
  }
}

const handleResponse = async <T>(response: Response, fallbackMessage: string): Promise<T> => {
  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const message = (payload as any)?.message || fallbackMessage
    throw new Error(message)
  }
  return payload as T
}

const fetchLabSections = async (theoryCourseId: number): Promise<LabSection[]> => {
  if (!Number.isFinite(theoryCourseId)) {
    throw new Error('Curso teórico inválido')
  }

  const response = await fetch(`${API_BASE_URL}/labs/course/${theoryCourseId}`, {
    credentials: 'include'
  })

  const data = await handleResponse<any[]>(response, 'No se pudieron cargar los laboratorios')
  return Array.isArray(data) ? data.map(mapLabSection) : []
}

const postEnrollmentRequest = async (
  labCourseId: number,
  action: 'validate' | 'confirm',
  payload?: EnrollmentPayload
): Promise<EnrollmentValidationResult> => {
  if (!Number.isFinite(labCourseId)) {
    throw new Error('Laboratorio inválido')
  }

  const response = await fetch(`${API_BASE_URL}/labs/${labCourseId}/${action}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: payload ? JSON.stringify(payload) : '{}'
  })

  return handleResponse<EnrollmentValidationResult>(response, 'No se pudo procesar la solicitud')
}

const validateEnrollment = async (
  labCourseId: number,
  payload?: EnrollmentPayload
): Promise<EnrollmentValidationResult> => postEnrollmentRequest(labCourseId, 'validate', payload)

const confirmEnrollment = async (
  labCourseId: number,
  payload?: EnrollmentPayload
): Promise<EnrollmentValidationResult> => postEnrollmentRequest(labCourseId, 'confirm', payload)

export const useLabEnrollmentService = () => ({
  fetchLabSections,
  validateEnrollment,
  confirmEnrollment
})

import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Secretary {
  dni: string
  name: string
  paternalSurname: string
  maternalSurname: string
  institutionalEmail: string
}

export interface EnrollmentPayload {
  studentId?: number
  studentCui?: string
  courseGroupIds: number[]
}

export interface EnrollmentResponse {
  success: boolean
  message: string
  studentId?: number
  courseId?: number | null
  courseGroupIds?: number[]
}

export const useSecretaryService = () => {
  const secretaries = ref<Secretary[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchSecretaries = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/secretaries`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('Error al cargar las secretarias')
      }
      
      secretaries.value = await response.json()
    } catch (err) {
      error.value = 'No se pudieron cargar las secretarias'
      console.error('Error fetching secretaries:', err)
    } finally {
      loading.value = false
    }
  }

  const enrollStudentInCourse = async (payload: EnrollmentPayload): Promise<EnrollmentResponse> => {
    const sanitizedGroupIds = Array.isArray(payload.courseGroupIds)
      ? Array.from(new Set(payload.courseGroupIds.filter(id => typeof id === 'number' && Number.isFinite(id))))
      : []

    if (!sanitizedGroupIds.length) {
      throw new Error('Debe seleccionar al menos un grupo de curso')
    }

    const response = await fetch(`${API_BASE_URL}/secretary/enrollments`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        studentId: payload.studentId,
        studentCui: payload.studentCui,
        courseGroupIds: sanitizedGroupIds
      })
    })

    const data: EnrollmentResponse = await response.json().catch(() => ({
      success: false,
      message: 'No se pudo interpretar la respuesta del servidor'
    }))

    if (!response.ok || data.success === false) {
      throw new Error(data.message || 'No se pudo matricular al estudiante')
    }

    return data
  }

  return {
    secretaries,
    loading,
    error,
    fetchSecretaries,
    enrollStudentInCourse
  }
}
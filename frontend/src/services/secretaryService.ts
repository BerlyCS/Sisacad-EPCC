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
  studentDocumentoIdentidad?: string
  studentCui?: string
  courseId: number
}

export interface EnrollmentResponse {
  success: boolean
  message: string
  studentDocumentoIdentidad?: string
  courseId?: number
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
    if (!payload.courseId) {
      throw new Error('Debe seleccionar un curso')
    }

    const response = await fetch(`${API_BASE_URL}/secretary/enrollments`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
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
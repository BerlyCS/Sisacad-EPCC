import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Professor {
  userId: number
  firstNames: string
  paternalSurname: string
  maternalSurname: string
  institutionalEmail: string
}

export const useProfessorService = () => {
  const professors = ref<Professor[]>([])
  const loading = ref(false)
  const error = ref('')
  const currentProfessor = ref<Professor | null>(null)
  const currentProfessorLoading = ref(false)
  const currentProfessorError = ref('')

  const fetchProfessors = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/professors`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('Error al cargar los profesores')
      }
      
      professors.value = await response.json()
    } catch (err) {
      error.value = 'No se pudieron cargar los profesores'
      console.error('Error fetching professors:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchCurrentProfessor = async (): Promise<Professor | null> => {
    currentProfessorLoading.value = true
    currentProfessorError.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/professors/me`, {
        credentials: 'include'
      })

      if (response.status === 401 || response.status === 403) {
        currentProfessor.value = null
        currentProfessorError.value = 'No autorizado para obtener los datos del profesor'
        return null
      }

      if (response.status === 404) {
        currentProfessor.value = null
        currentProfessorError.value = 'No se encontró información del profesor'
        return null
      }

      if (!response.ok) {
        throw new Error('Error al cargar el profesor actual')
      }

      const data: Professor = await response.json()
      currentProfessor.value = data
      return data
    } catch (err) {
      currentProfessor.value = null
      currentProfessorError.value = 'No se pudo cargar el profesor actual'
      console.error('Error fetching current professor:', err)
      return null
    } finally {
      currentProfessorLoading.value = false
    }
  }

  return {
    professors,
    loading,
    error,
    currentProfessor,
    currentProfessorLoading,
    currentProfessorError,
    fetchProfessors,
    fetchCurrentProfessor
  }
}
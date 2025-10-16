import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Professor {
  id: number
  nombres: string
  apellidoPaterno: string
  apellidoMaterno: string
  correo: string
}

export const useProfessorService = () => {
  const professors = ref<Professor[]>([])
  const loading = ref(false)
  const error = ref('')

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

  return {
    professors,
    loading,
    error,
    fetchProfessors
  }
}
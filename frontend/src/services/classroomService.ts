import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Classroom {
  id: number
  name: string
  capacity: number
  location: string
  type: string
}

export const useClassroomService = () => {
  const classrooms = ref<Classroom[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchClassrooms = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/classrooms`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('Error al cargar las aulas')
      }
      
      classrooms.value = await response.json()
    } catch (err) {
      error.value = 'No se pudieron cargar las aulas'
      console.error('Error fetching classrooms:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    classrooms,
    loading,
    error,
    fetchClassrooms
  }
}
import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Student {
  documentoIdentidad: string
  cui: string
  nombres: string
  apellidoPaterno: string
  apellidoMaterno: string
  correoInstitucional: string
}

export const useStudentService = () => {
  const students = ref<Student[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchStudentsSorted = async (sortBy: string = 'dni', direction: string = 'asc') => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/students/list?sortBy=${sortBy}&direction=${direction}`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('Error al cargar los estudiantes')
      }
      
      students.value = await response.json()
    } catch (err) {
      error.value = 'No se pudieron cargar los estudiantes'
      console.error('Error fetching students:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    students,
    loading,
    error,
    fetchStudentsSorted
  }
}
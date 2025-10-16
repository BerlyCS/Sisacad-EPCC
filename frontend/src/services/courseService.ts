import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Course {
  id: number
  name: string
  code: string
  credits: number
  description: string
}

export const useCourseService = () => {
  const courses = ref<Course[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchCourses = async () => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/courses`, {
        credentials: 'include'
      })
      
      if (!response.ok) {
        throw new Error('Error al cargar los cursos')
      }
      
      courses.value = await response.json()
    } catch (err) {
      error.value = 'No se pudieron cargar los cursos'
      console.error('Error fetching courses:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    courses,
    loading,
    error,
    fetchCourses
  }
}
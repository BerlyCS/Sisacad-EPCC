import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

type CourseType = 'THEORY' | 'LAB'

const COURSE_TYPE_LABEL: Record<CourseType, string> = {
  THEORY: 'Teoría',
  LAB: 'Laboratorio'
}

interface ApiCourse {
  courseID: number
  name: string
  creditNumber: number
  groupLetter: string
  syllabusID: number
  anio: number
  courseType: CourseType
  labPrerequisiteCourseId: number | null
  enrolledStudentIDs: number[]
  teacherIDs: number[]
}

export interface Course extends ApiCourse {
  courseTypeLabel: string
}

export const useStudentCourseService = () => {
  const courses = ref<Course[]>([])
  const loading = ref(false)
  const error = ref('')

  const fetchMyCourses = async () => {
    loading.value = true
    error.value = ''
    try {
      console.log('Frontend: Intentando obtener cursos desde:', `${API_BASE_URL}/students/my-courses`)
      
      const response = await fetch(`${API_BASE_URL}/students/my-courses`, {
        credentials: 'include'
      })
      
      console.log('Frontend: Respuesta recibida - Status:', response.status)
      
      if (!response.ok) {
        const errorText = await response.text()
        console.error('Frontend: Error en respuesta:', errorText)
        throw new Error(`Error ${response.status}: ${errorText}`)
      }
      
      const data: ApiCourse[] = await response.json()
      console.log('Frontend: Cursos recibidos:', data)
      courses.value = data.map(course => ({
        ...course,
        courseTypeLabel: COURSE_TYPE_LABEL[course.courseType]
      }))
      
    } catch (err) {
      error.value = 'No se pudieron cargar los cursos'
      console.error('Frontend: Error fetching student courses:', err)
    } finally {
      loading.value = false
    }
  }

  return {
    courses,
    loading,
    error,
    fetchMyCourses
  }
}
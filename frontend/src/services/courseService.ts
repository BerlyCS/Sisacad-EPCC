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
  syllabusID: number | null
  courseType: CourseType
  labPrerequisiteCourseId: number | null
  enrolledStudentIDs: number[]
  teacherIDs: number[]
}

export interface Course extends ApiCourse {
  courseTypeLabel: string
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

      const data: ApiCourse[] = await response.json()
      courses.value = data.map(course => ({
        ...course,
        courseTypeLabel: COURSE_TYPE_LABEL[course.courseType]
      }))
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

import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

// Interfaz original esperada por la vista
export interface Classroom {
  id: number
  name: string
  capacity: number
  location: string
  type: string
}

// Interfaz para reflejar la estructura real del backend
interface RawClassroom {
  classroomID: number
  place: {
    building: string
    floor: number
    number: number
    capacity: number
    classroomType: string
  }
  occupiedSchedules: any[]
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

      const rawData: RawClassroom[] = await response.json()

      // Adaptar al formato esperado por el frontend
      classrooms.value = rawData.map((item) => ({
        id: item.classroomID,
        name: `Aula ${item.place.number}`,
        capacity: item.place.capacity,
        location: `${item.place.building} - Piso ${item.place.floor}`,
        type: item.place.classroomType
      }))
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

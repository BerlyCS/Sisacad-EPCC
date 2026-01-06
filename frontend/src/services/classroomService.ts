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

  const createClassroom = async (payload: { place: { building: string; floor?: number; number?: number; capacity?: number; classroomType?: string } }) => {
    loading.value = true
    error.value = ''
    try {
      const response = await fetch(`${API_BASE_URL}/classrooms`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })

      if (!response.ok) {
        throw new Error('Error al crear el aula')
      }

      // Backend may return created classroom or echo; refresh list
      await fetchClassrooms()
      return true
    } catch (err) {
      error.value = 'No se pudo crear el aula'
      console.error('Error creating classroom:', err)
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    classrooms,
    loading,
    error,
    fetchClassrooms,
    createClassroom
  }
}

export interface ClassroomImportResult {
  created: ClassroomImportSummary[]
  errors: string[]
  processedRows: number
  skippedRows: number
}

export interface ClassroomImportSummary {
  classroomId?: number | null
  building?: string | null
  number?: number | null
  floor?: number | null
  capacity?: number | null
  classroomType?: string | null
}

const normalizeEntry = (raw: any): ClassroomImportSummary => ({
  classroomId: typeof raw?.classroomID === 'number' ? raw.classroomID : raw?.classroomId ?? null,
  building: raw?.place?.building ?? null,
  number: typeof raw?.place?.number === 'number' ? raw.place.number : null,
  floor: typeof raw?.place?.floor === 'number' ? raw.place.floor : null,
  capacity: typeof raw?.place?.capacity === 'number' ? raw.place.capacity : null,
  classroomType: raw?.place?.classroomType ?? null
})

const normalizeResult = (raw: any): ClassroomImportResult => ({
  created: Array.isArray(raw?.created) ? raw.created.map(normalizeEntry) : [],
  errors: Array.isArray(raw?.errors) ? raw.errors.filter((entry) => typeof entry === 'string') : [],
  processedRows: typeof raw?.processedRows === 'number' ? raw.processedRows : 0,
  skippedRows: typeof raw?.skippedRows === 'number' ? raw.skippedRows : 0
})

export const importClassroomsFile = async (file: File): Promise<ClassroomImportResult> => {
  const form = new FormData()
  form.append('file', file)

  const response = await fetch(`${API_BASE_URL}/classrooms/import`, {
    method: 'POST',
    credentials: 'include',
    body: form
  })

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const fallback =
      payload?.message ||
      (Array.isArray(payload?.errors) && payload.errors[0]) ||
      response.statusText ||
      'No se pudo procesar el archivo'
    throw new Error(fallback)
  }

  return normalizeResult(payload)
}

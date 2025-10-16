import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8080/api'

export interface Secretary {
  dni: string
  name: string
  paternalSurname: string
  maternalSurname: string
  institutionalEmail: string
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

  return {
    secretaries,
    loading,
    error,
    fetchSecretaries
  }
}
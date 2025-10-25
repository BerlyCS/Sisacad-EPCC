import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authService } from '../services/authService'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<any>(null)
  const loading = ref(false)
  const error = ref('')
  const initialized = ref(false)

  const setUser = (userData: any) => {
    user.value = userData
  }

  const setLoading = (isLoading: boolean) => {
    loading.value = isLoading
  }

  const setError = (errorMessage: string) => {
    error.value = errorMessage
  }

  const clearError = () => {
    error.value = ''
  }

  const initializeAuth = async () => {
    if (initialized.value) return
    
    setLoading(true)
    try {
      const userData = await authService.getCurrentUser()
      if (userData.authenticated) {
        user.value = userData
      }
      initialized.value = true
    } catch (error) {
      console.warn('Error inicializando auth:', error)
      user.value = null
    } finally {
      setLoading(false)
    }
  }

  const logout = async (): Promise<boolean> => {
    setLoading(true)
    setError('')

    try {
      await authService.logout()

      // Limpia toda la sesión local
      user.value = null
      initialized.value = false

      // Borra datos persistentes si existieran
      localStorage.removeItem('user')
      sessionStorage.clear()

      return true
    } catch (error: any) {
      console.warn('Logout API falló, usando redirección directa:', error)
      window.location.href = 'http://localhost:8080/logout'
      return false
    } finally {
      setLoading(false)
    }
  }

  const isAuthenticated = computed(() => {
    return user.value !== null && user.value.authenticated
  })

  return {
    user,
    loading,
    error,
    isAuthenticated,
    initialized,
    setUser,
    setLoading,
    setError,
    clearError,
    initializeAuth,
    logout
  }
})
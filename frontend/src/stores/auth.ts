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

  const setInitialized = (value: boolean) => {
    initialized.value = value
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

  const loginAsDemo = async (role: string) => {
    setLoading(true)
    clearError()

    try {
      const response = await authService.demoLogin(role)
      if (response?.authenticated) {
        user.value = response
        initialized.value = true
      }
      return response
    } catch (error: any) {
      const message = error?.message || 'No se pudo iniciar sesión de demostración'
      setError(message)
      throw new Error(message)
    } finally {
      setLoading(false)
    }
  }

  const isAuthenticated = computed(() => {
    return user.value !== null && user.value.authenticated
  })

  // Computed properties para acceso fácil
  const userRole = computed(() => {
    return user.value?.role || ''
  })

  const userName = computed(() => {
    return user.value?.name || ''
  })

  const userEmail = computed(() => {
    return user.value?.email || ''
  })

  const userDocumentoIdentidad = computed(() => {
    return user.value?.documentoIdentidad || ''
  })

  const isAdmin = computed(() => {
    return user.value?.isAdmin || user.value?.role === 'ADMIN'
  })

  const isStudent = computed(() => {
    return user.value?.role === 'STUDENT'
  })

  const isProfessor = computed(() => {
    return user.value?.role === 'PROFESSOR'
  })

  const isSecretary = computed(() => {
    return user.value?.role === 'SECRETARY'
  })

  return {
    user,
    loading,
    error,
    isAuthenticated,
    initialized,
    userRole,
    userName,
    userEmail,
    userDocumentoIdentidad,
    isAdmin,
    isStudent,
    isProfessor,
    isSecretary,
    setUser,
    setLoading,
    setInitialized,
    setError,
    clearError,
    initializeAuth,
    logout,
    loginAsDemo
  }
})
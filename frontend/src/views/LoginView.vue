<template>
  <div class="flex items-center justify-center min-h-screen bg-blueEPCC px-4">
    <div class="w-full max-w-[400px] p-6 bg-white shadow-lg rounded-md text-center space-y-6 text-gray-900">

      <!-- LOGO -->
      <div class="flex items-center justify-center space-x-4">
        <img
          src="https://scontent.flim6-4.fna.fbcdn.net/v/t39.30808-6/332319294_2237668383092057_2920168158318927642_n.jpg?_nc_cat=101&ccb=1-7&_nc_sid=6ee11a&_nc_ohc=KyYOuUAnLFAQ7kNvwE-PtBc&_nc_oc=Admul2ULA1QEY371EzaDwnzTdcq1wNt8THbu6EjdrATs7dV9nVC-fZs--duSPa22ilE&_nc_zt=23&_nc_ht=scontent.flim6-4.fna&_nc_gid=7adnapXoIMd7BR-9QXlo5Q&oh=00_AfepJgWIfXfSokI7Y-4yqEcdpVjHXCpaQlprdfz_LfaF-Q&oe=68F62921"
          alt="Logo EPCC"
          class="h-12 max-w-[160px] object-contain"
        />
      </div>

      <!-- TEXTO -->
      <p class="text-md font-medium m-4">Identifíquese usando su cuenta en:</p>

      <!-- BOTÓN PRINCIPAL - Ahora abre popup de Google -->
      <button
        @click="initiateGoogleAuth"
        :disabled="loading"
        class="w-full flex items-center justify-center space-x-3 px-4 py-2 bg-red-700 text-white font-semibold rounded hover:bg-red-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <img
          src="https://dutic.unsa.edu.pe/logo_unsa.png"
          alt="Icono UNSA"
          class="h-6 max-w-[20px] object-contain"
        />
        <span v-if="!loading">Ingresar con Correo UNSA</span>
        <span v-else>Verificando...</span>
      </button>

      <!-- Mensaje de error -->
      <div v-if="errorMessage" class="text-red-600 text-sm bg-red-50 p-2 rounded border border-red-200">
        {{ errorMessage }}
      </div>

      <!-- Mensaje de éxito -->
      <div v-if="successMessage" class="text-green-600 text-sm bg-green-50 p-2 rounded border border-green-200">
        {{ successMessage }}
      </div>

      <!-- Modal para ingresar email manualmente (fallback) -->
      <div v-if="showEmailModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white p-6 rounded-lg w-96">
          <h3 class="text-lg font-semibold mb-4">Ingrese su correo UNSA</h3>
          <input
            v-model="manualEmail"
            type="email"
            placeholder="usuario@unsa.edu.pe"
            class="w-full p-2 border border-gray-300 rounded mb-4"
            @keyup.enter="verifyManualEmail"
          />
          <div class="flex justify-end space-x-2">
            <button
              @click="showEmailModal = false"
              class="px-4 py-2 text-gray-600 hover:text-gray-800"
            >
              Cancelar
            </button>
            <button
              @click="verifyManualEmail"
              :disabled="!manualEmail"
              class="px-4 py-2 bg-red-700 text-white rounded hover:bg-red-800 disabled:opacity-50"
            >
              Verificar
            </button>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { authService } from '../services/authService'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

// Verificar si ya estamos autenticados al cargar la página
onMounted(async () => {
  await authStore.initializeAuth()
  
  // Si ya está autenticado, redirigir a bienvenido
  if (authStore.isAuthenticated) {
    router.push('/bienvenido')
    return
  }
  
  // Manejar parámetros de la URL
  const urlParams = new URLSearchParams(window.location.search)
  const error = urlParams.get('error')
  const logout = urlParams.get('logout')
  
  if (error === 'auth_failed') {
    errorMessage.value = 'Error en la autenticación. Por favor, intente nuevamente.'
  } else if (error === 'not_authenticated') {
    errorMessage.value = 'Su sesión ha expirado. Por favor, ingrese nuevamente.'
  }
  
  if (logout === 'success') {
    successMessage.value = 'Sesión cerrada correctamente.'
  }
  
  // Limpiar parámetros de la URL
  if (error || logout) {
    const newUrl = window.location.origin + window.location.pathname
    window.history.replaceState({}, document.title, newUrl)
  }
})

const initiateGoogleAuth = () => {
  authStore.clearError()
  errorMessage.value = ''
  
  // Redirección directa a OAuth de Google
  window.location.href = 'http://localhost:8080/oauth2/authorization/google'
}

const verifyManualEmail = async () => {
  if (!manualEmail.value) return

  loading.value = true
  errorMessage.value = ''

  try {
    const result = await authService.verifyEmail(manualEmail.value)
    
    if (result.valid) {
      // Email válido, redirigir a OAuth de Google
      window.location.href = result.oauthUrl
    } else {
      errorMessage.value = result.message
    }
  } catch (error) {
    console.error('Error verificando email:', error)
    errorMessage.value = 'Error al verificar el correo. Intente nuevamente.'
  } finally {
    loading.value = false
    showEmailModal.value = false
  }
}

// Escuchar parámetros de error en la URL
// Escuchar parámetros en la URL
onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search)
  const error = urlParams.get('error')
  const logout = urlParams.get('logout')
  
  if (error === 'auth_failed') {
    errorMessage.value = 'Error en la autenticación. Por favor, intente nuevamente.'
  } else if (error === 'not_authenticated') {
    errorMessage.value = 'Su sesión ha expirado. Por favor, ingrese nuevamente.'
  }
  
  if (logout === 'success') {
    successMessage.value = 'Sesión cerrada correctamente.'
  }
  
  // Limpiar los parámetros de la URL sin recargar la página
  if (error || logout) {
    const newUrl = window.location.origin + window.location.pathname
    window.history.replaceState({}, document.title, newUrl)
  }
})
</script>
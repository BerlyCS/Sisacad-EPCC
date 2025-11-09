<template>
  <div class="flex items-center justify-center min-h-screen bg-blueEPCC px-4">
    <div class="w-full max-w-[400px] p-6 bg-white shadow-lg rounded-md text-center space-y-6 text-gray-900">

      <!-- TÍTULO -->
      <h2 class="text-3xl font-bold">SISACAD - EPCC</h2>

      <!-- ÍCONO DE USUARIO -->
      <div class="flex items-center">
        <UserCircleIcon class="h-25 w-25 text-gray-400 mx-auto" />
      </div>

      <!-- TEXTO -->
      <p class="text-md font-medium m-4">Identifíquese usando su cuenta en:</p>

      <!-- Mensaje de error -->
      <div v-if="errorMessage" class="text-red-600 text-sm bg-red-50 p-2 rounded border border-red-200">
        {{ errorMessage }}
      </div>

      <!-- Mensaje de éxito -->
      <div v-if="successMessage" class="text-green-600 text-sm bg-green-50 p-2 rounded border border-green-200">
        {{ successMessage }}
      </div>

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

      <!-- DEMO LOGIN (deshabilitar con VITE_ENABLE_DEMO_LOGIN=false) -->
      <div v-if="demoLoginEnabled" class="space-y-3 pt-4 border-t border-gray-200">
        <p class="text-sm text-gray-500">
          Accesos de demostración (solo para pruebas internas)
        </p>
        <div class="grid grid-cols-1 gap-2">
          <button
            v-for="profile in demoProfiles"
            :key="profile.key"
            @click="handleDemoLogin(profile)"
            :disabled="loading"
            class="w-full px-4 py-2 border border-gray-300 rounded hover:bg-gray-100 transition text-sm disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {{ profile.label }}
          </button>
        </div>
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

import { UserCircleIcon } from '@heroicons/vue/16/solid'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const showEmailModal = ref(false)
const manualEmail = ref('')

const demoLoginEnabled = import.meta.env.VITE_ENABLE_DEMO_LOGIN !== 'false'
const demoProfiles = [
  { key: 'student', label: 'Ingresar como Estudiante', role: 'STUDENT' },
  { key: 'professor', label: 'Ingresar como Profesor', role: 'PROFESSOR' },
  { key: 'admin', label: 'Ingresar como Administrador', role: 'ADMIN' },
  { key: 'secretary', label: 'Ingresar como Secretaria', role: 'SECRETARY' }
]

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

const handleDemoLogin = async (profile) => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const result = await authStore.loginAsDemo(profile.role)
    if (result?.authenticated) {
      successMessage.value = `Sesión demo iniciada como ${profile.label.toLowerCase()}.`
      router.push('/bienvenido')
      return
    }

    errorMessage.value = 'No se pudo iniciar sesión de demostración.'
  } catch (error) {
    console.error('Error en demo login:', error)
    errorMessage.value = error instanceof Error ? error.message : 'No se pudo iniciar sesión de demostración.'
  } finally {
    loading.value = false
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
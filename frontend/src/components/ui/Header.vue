<template>
   <!-- Header -->
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="md:flex md:justify-between items-center py-4 gap-4">
          <div class="flex items-center gap-3">
            <img
              v-if="user && user.picture"
              :src="user.picture"
              alt="Foto de perfil"
              class="h-12 w-12 rounded-full object-cover ring-2 ring-blue-100"
            />
            <div>
              <h1 class="text-2xl font-bold text-gray-900">
                {{ roleTitle }}
              </h1>
              <p v-if="user && user.name" class="text-sm text-gray-600">{{ user.name }}</p>
            </div>
          </div>
          <div class="flex items-center md:justify-normal justify-between space-x-4">
            <span class="text-gray-700">Hola, {{ user && user.authenticated && user.name ? user.name : 'Usuario' }}</span>
            <button 
              v-if="user && user.authenticated"
              @click="goToDashboard"
              class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
            >
              Dashboard
            </button>
            <button 
              v-if="user && user.authenticated"
              @click="logout"
              class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
            >
              Cerrar Sesión
            </button>
          </div>
        </div>
      </div>
    </header>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { user, userRole, initialized } = storeToRefs(authStore)

const roleTitle = computed(() => {
  if (!user.value || !user.value.authenticated) {
    return 'SISACAD - EPCC'
  }
  
  switch (userRole.value) {
    case 'ADMIN':
      return 'Panel de Administración'
    case 'SECRETARY':
      return 'Panel de Secretaría'
    case 'PROFESSOR':
      return 'Panel del Profesor'
    case 'STUDENT':
      return 'Panel del Estudiante'
    default:
      return 'SISACAD - EPCC'
  }
})

onMounted(async () => {
  if (!initialized.value) {
    try {
      await authStore.initializeAuth()
    } catch (error) {
      console.warn('No se pudo inicializar la sesión en Header:', error)
    }
  }
})

const goToDashboard = () => {
  router.push('/bienvenido')
}

const logout = async () => {
  const success = await authStore.logout()
  
  if (success) {
    router.replace('/?logout=success')
  }
}
</script>
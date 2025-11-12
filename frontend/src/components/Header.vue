<template>
   <!-- Header -->
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="md:flex md:justify-between items-cente py-4">
          <div class="flex items-center">
            <h1 class="text-2xl font-bold text-gray-900 mx-auto mb-5 md:m-0">
              {{ userRole === 'ADMIN' ? 'Panel de Administración' : 
                userRole === 'SECRETARY' ? 'Panel de Secretaría' :  
                userRole === 'PROFESSOR' ? 'Panel del Profesor' :
                userRole === 'STUDENT' ? 'Panel del Estudiante' : 'Invitado' }}
            </h1>
          </div>
          <div class="flex items-center md:justify-normal justify-between space-x-4">
            <span class="text-gray-700">Hola, {{ user.name }}</span>
            <button 
              @click="goToDashboard"
              class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
            >
              Dashboard
            </button>
            <button 
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
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const user = authStore.user
const userRole = user?.role || ''

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
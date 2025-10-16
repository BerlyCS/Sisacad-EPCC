<template>
  <div class="flex items-center justify-center min-h-screen bg-green-50 text-center">
    <div class="bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
      <div v-if="authStore.user && authStore.isAuthenticated">
        <img 
          v-if="authStore.user.picture" 
          :src="authStore.user.picture" 
          alt="User" 
          class="w-20 h-20 rounded-full mx-auto mb-4"
        />
        <h1 class="text-3xl font-bold text-green-800 mb-2">
          ¡Bienvenido, {{ authStore.user.name }}!
        </h1>
        <p class="text-gray-600 mb-2">{{ authStore.user.email }}</p>
        <p class="text-blue-600 font-semibold mb-4">
          Rol: {{ authStore.user.role }} 
          <span v-if="authStore.user.isAdmin" class="text-red-600">(Administrador)</span>
        </p>
        <button 
          @click="handleLogout"
          :disabled="authStore.loading"
          class="px-6 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="!authStore.loading">Cerrar Sesión</span>
          <span v-else>Cerrando sesión...</span>
        </button>
                
        <!-- Botones de administración solo para administradores -->
        <div v-if="authStore.user.isAdmin" class="mt-6 pt-4 border-t border-gray-200">
          <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel de Administración</h3>
          <div class="grid grid-cols-2 gap-3">
            <router-link 
              to="/admin/classrooms"
              class="px-4 py-3 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition text-center"
            >
              📚 Gestionar Aulas
            </router-link>
            <router-link 
              to="/admin/courses"
              class="px-4 py-3 bg-green-600 text-white text-sm rounded hover:bg-green-700 transition text-center"
            >
              🎓 Gestionar Cursos
            </router-link>
            <router-link 
              to="/admin/professors"
              class="px-4 py-3 bg-purple-600 text-white text-sm rounded hover:bg-purple-700 transition text-center"
            >
              👨‍🏫 Gestionar Profesores
            </router-link>
            <router-link 
              to="/admin/students"
              class="px-4 py-3 bg-orange-600 text-white text-sm rounded hover:bg-orange-700 transition text-center"
            >
              👥 Gestionar Estudiantes
            </router-link>
            <router-link 
              to="/admin/secretaries"
              class="px-4 py-3 bg-pink-600 text-white text-sm rounded hover:bg-pink-700 transition text-center"
            >
              💼 Gestionar Secretarias
            </router-link>
          </div>
        </div>

      </div>
      
      <div v-else-if="authStore.loading">
        <p class="text-gray-600">Cargando...</p>
      </div>
      
      <div v-else>
        <div class="text-red-600 mb-4">
          <p>No tienes acceso a esta página.</p>
          <p class="text-sm mt-2">Por favor, inicia sesión primero.</p>
        </div>
        <button 
          @click="goToLogin"
          class="px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
        >
          Ir al Login
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  // Inicializar el estado de autenticación
  await authStore.initializeAuth()
  
  // Si no está autenticado, redirigir al login después de un breve delay
  if (!authStore.isAuthenticated) {
    console.log('Usuario no autenticado, redirigiendo...')
    setTimeout(() => {
      router.push('/?error=not_authenticated')
    }, 2000)
  }
})

const handleLogout = async () => {
  const success = await authStore.logout()
  
  // Si el logout fue exitoso via API, redirigir
  if (success) {
    router.push('/?logout=success')
  }
  // Si falló, el método logout ya hizo la redirección directa
}

const goToLogin = () => {
  router.push('/')
}
</script>
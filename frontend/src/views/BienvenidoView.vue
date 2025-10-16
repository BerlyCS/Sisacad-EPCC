<template>
  <div class="flex items-center justify-center min-h-screen bg-green-50 text-center">
    <div class="bg-white p-8 rounded-lg shadow-lg max-w-md w-full">
      <div v-if="user">
        <img 
          v-if="user.picture" 
          :src="user.picture" 
          alt="User" 
          class="w-20 h-20 rounded-full mx-auto mb-4"
        />
        <h1 class="text-3xl font-bold text-green-800 mb-2">
          ¡Bienvenido, {{ user.name }}!
        </h1>
        <p class="text-gray-600 mb-4">{{ user.email }}</p>
        <button 
          @click="logout"
          class="px-6 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
        >
          Cerrar Sesión
        </button>
      </div>
      <div v-else>
        <p class="text-gray-600">Cargando...</p>
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
const user = ref(null)

onMounted(async () => {
  try {
    const userData = await authService.getCurrentUser()
    if (userData.authenticated) {
      user.value = userData
      authStore.setUser(userData)
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error('Error obteniendo usuario:', error)
    router.push('/')
  }
})

const logout = () => {
  window.location.href = 'http://localhost:8080/logout'
}
</script>
<template>
  <div class="min-h-screen bg-gray-100">
    <!-- Header -->
    <header class="bg-white shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-4">
          <div class="flex items-center">
            <h1 class="text-2xl font-bold text-gray-900">Panel de Administración</h1>
          </div>
          <div class="flex items-center space-x-4">
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

    <!-- Navigation -->
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex space-x-8 py-3">
          <!-- Enlaces de navegación -->
          <router-link 
            v-for="item in navigation" 
            :key="item.name"
            :to="item.to"
            class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition flex items-center space-x-2"
            :class="{ 'bg-blue-100 text-blue-700': $route.path === item.to }"
          >
            <template v-if="item.icon">
              <i :class="item.icon"></i>
            </template>
            <span>| {{ item.name }} |</span>
          </router-link>

          <!-- 🔹 Enlace adicional solo para ADMIN -->
          <router-link 
            v-if="userRole === 'ADMIN'" 
            to="/reservation-management" 
            class="text-gray-600 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium transition flex items-center space-x-2"
            :class="{ 'bg-blue-100 text-blue-700': $route.path === '/reservation-management' }"
          >
            <i class="fas fa-tasks"></i>
            <span>| Gestionar Reservas |</span>
          </router-link>
        </div>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div class="px-4 py-6 sm:px-0">
        <slot></slot>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const user = authStore.user
const userRole = user?.role || '' // 👈 obtenemos el rol del usuario

const navigation = [
  { name: 'Aulas', to: '/admin/classrooms' },
  { name: 'Cursos', to: '/admin/courses' },
  { name: 'Profesores', to: '/admin/professors' },
  { name: 'Estudiantes', to: '/admin/students' },
  { name: 'Secretarias', to: '/admin/secretaries' },
  { name: 'Reservar Aula', to: '/classrooms', icon: 'fas fa-calendar-alt' }
]

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

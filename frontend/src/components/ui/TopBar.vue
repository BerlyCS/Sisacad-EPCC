<template>
  <div class="min-h-screen bg-gray-100">
    <Header />

    <!-- Navigation -->
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        <!-- Barra superior -->
        <div class="flex justify-between items-center py-4">
          <!-- Botón hamburguesa (solo en pantallas pequeñas) -->
          <button
            @click="isMenuOpen = !isMenuOpen"
            class="md:hidden p-2 rounded-md text-gray-700 hover:bg-gray-200 focus:outline-none"
          >
            <svg class="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none"
                 viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>

          <!-- Navegación en pantallas grandes -->
          <div class="hidden md:flex space-x-5">
            <template v-if="userRole === 'ADMIN'">
              <router-link 
                v-for="item in adminNavigation" 
                :key="item.name"
                :to="item.to"
                class="text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
                :class="{ 'bg-blue-100 text-blue-700': $route.path === item.to }"
              >
                <template v-if="item.icon">
                  <i :class="item.icon"></i>
                </template>
                <span>{{ item.name }}</span>
              </router-link>
            </template>

            <template v-if="userRole === 'SECRETARY'">
              <router-link 
                v-for="item in secretaryNavigation" 
                :key="item.name"
                :to="item.to"
                class="text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
                :class="{ 'bg-blue-100 text-blue-700': $route.path === item.to }"
              >
                <span>{{ item.name }}</span>
              </router-link>
            </template>

            <router-link 
              v-if="userRole === 'PROFESSOR' || userRole === 'ADMIN'"
              to="/classrooms" 
              class="text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/classrooms' }"
            >
              <i class="fas fa-calendar-alt"></i>
              <span>Reservar Aula</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT' && userCui"
              :to="`/students/${userCui}/profile`"
              class="text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path.includes(`/students/${userCui}/profile`) }"
            >
              <i class="fas fa-user-graduate"></i>
              <span>Mi Perfil</span>
            </router-link>

            <router-link 
              v-if="userRole === 'ADMIN'" 
              to="/reservation-management" 
              class="text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/reservation-management' }"
            >
              <i class="fas fa-tasks"></i>
              <span>Gestionar Reservas</span>
            </router-link>
          </div>
        </div>

        <!-- Menú desplegable (para móviles) -->
        <transition name="fade">
          <div
            v-show="isMenuOpen"
            class="md:hidden border-t border-gray-200 py-3 space-y-2"
          >
            <template v-if="userRole === 'ADMIN'">
              <router-link 
                v-for="item in adminNavigation" 
                :key="item.name"
                :to="item.to"
                class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
                :class="{ 'bg-blue-100 text-blue-700': $route.path === item.to }"
                @click="isMenuOpen = false"
              >
                {{ item.name }}
              </router-link>
            </template>

            <template v-if="userRole === 'SECRETARY'">
              <router-link 
                v-for="item in secretaryNavigation" 
                :key="item.name"
                :to="item.to"
                class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
                :class="{ 'bg-blue-100 text-blue-700': $route.path === item.to }"
                @click="isMenuOpen = false"
              >
                {{ item.name }}
              </router-link>
            </template>

            <router-link 
              v-if="userRole === 'PROFESSOR' || userRole === 'ADMIN'"
              to="/classrooms"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/classrooms' }"
              @click="isMenuOpen = false"
            >
              Reservar Aula
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT' && userCui"
              :to="`/students/${userCui}/profile`"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path.includes(`/students/${userCui}/profile`) }"
              @click="isMenuOpen = false"
            >
              Mi Perfil
            </router-link>

            <router-link 
              v-if="userRole === 'ADMIN'"
              to="/reservation-management"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/reservation-management' }"
              @click="isMenuOpen = false"
            >
              Gestionar Reservas
            </router-link>
          </div>
        </transition>
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
import { ref } from 'vue'
import { storeToRefs } from 'pinia'
import Header from './Header.vue'
import { useAuthStore } from '@/stores/auth'

const isMenuOpen = ref(false)
const authStore = useAuthStore()

const { userRole, userCui } = storeToRefs(authStore)

const adminNavigation = [
  { name: 'Aulas', to: '/admin/classrooms' },
  { name: 'Cursos', to: '/admin/courses' },
  { name: 'Profesores', to: '/admin/professors' },
  { name: 'Estudiantes', to: '/admin/students' },
  { name: 'Secretarias', to: '/admin/secretaries' }
]

const secretaryNavigation = [
  { name: 'Estudiantes', to: '/admin/students' },
  { name: 'Matricular', to: '/admin/student-enrollment' }
]
</script>

<style scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>

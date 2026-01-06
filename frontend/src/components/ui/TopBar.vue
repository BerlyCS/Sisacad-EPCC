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
          <div class="hidden md:flex flex-wrap items-center gap-2 overflow-x-auto">
            <template v-if="userRole === 'ADMIN'">
              <router-link 
                v-for="item in adminNavigation" 
                :key="item.name"
                :to="item.to"
                class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
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
                class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
                :class="{ 'bg-blue-100 text-blue-700': $route.path === item.to }"
              >
                <template v-if="item.icon">
                  <i :class="item.icon"></i>
                </template>
                <span>{{ item.name }}</span>
              </router-link>
            </template>

            <router-link 
              v-if="userRole === 'PROFESSOR' || userRole === 'ADMIN'"
              to="/classrooms" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/classrooms' }"
            >
              <i class="fas fa-calendar-alt"></i>
              <span>Reservar Aula</span>
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/grades" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/grades' }"
            >
              <i class="fas fa-chart-bar"></i>
              <span>Calificaciones</span>
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/schedule" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/schedule' }"
            >
              <i class="fas fa-calendar-week"></i>
              <span>Mi Horario</span>
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/reservations" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/reservations' }"
            >
              <i class="fas fa-bookmark"></i>
              <span>Mis Reservas</span>
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/exams" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/exams' }"
            >
              <i class="fas fa-file-pdf"></i>
              <span>Exámenes</span>
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/attendance" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/attendance' }"
            >
              <i class="fas fa-clipboard-check"></i>
              <span>Asistencia</span>
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/syllabus" 
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/syllabus' }"
            >
              <i class="fas fa-file-upload"></i>
              <span>Sílabo y temario</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/profile"
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/profile' }"
            >
              <i class="fas fa-user-graduate"></i>
              <span>Mi Perfil</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/courses"
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/courses' }"
            >
              <i class="fas fa-book"></i>
              <span>Mis Cursos</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/schedule"
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/schedule' }"
            >
              <i class="fas fa-calendar"></i>
              <span>Mi Horario</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/grades"
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/grades' }"
            >
              <i class="fas fa-chart-line"></i>
              <span>Mis Calificaciones</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/attendance"
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/attendance' }"
            >
              <i class="fas fa-clipboard-check"></i>
              <span>Asistencia</span>
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/labs"
              class="whitespace-nowrap text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-all duration-200 px-3 py-2 rounded-md text-sm font-medium flex items-center space-x-2"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/labs' }"
            >
              <i class="fas fa-flask"></i>
              <span>Laboratorios</span>
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
                <template v-if="item.icon">
                  <i :class="item.icon + ' mr-2'"></i>
                </template>
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
                <template v-if="item.icon">
                  <i :class="item.icon + ' mr-2'"></i>
                </template>
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
              v-if="userRole === 'PROFESSOR'"
              to="/professor/grades"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/grades' }"
              @click="isMenuOpen = false"
            >
              Calificaciones
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/schedule"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/schedule' }"
              @click="isMenuOpen = false"
            >
              Mi Horario
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/reservations"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/reservations' }"
              @click="isMenuOpen = false"
            >
              Mis Reservas
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/exams"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/exams' }"
              @click="isMenuOpen = false"
            >
              Exámenes
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/attendance"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/attendance' }"
              @click="isMenuOpen = false"
            >
              Asistencia
            </router-link>

            <router-link 
              v-if="userRole === 'PROFESSOR'"
              to="/professor/syllabus"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/professor/syllabus' }"
              @click="isMenuOpen = false"
            >
              Sílabo
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/profile"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/profile' }"
              @click="isMenuOpen = false"
            >
              Mi Perfil
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/courses"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/courses' }"
              @click="isMenuOpen = false"
            >
              Mis Cursos
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/schedule"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/schedule' }"
              @click="isMenuOpen = false"
            >
              Mi Horario
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/grades"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/grades' }"
              @click="isMenuOpen = false"
            >
              Mis Calificaciones
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/attendance"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/attendance' }"
              @click="isMenuOpen = false"
            >
              Asistencia
            </router-link>

            <router-link
              v-if="userRole === 'STUDENT'"
              to="/student/labs"
              class="block text-gray-700 hover:bg-gray-100 rounded-md px-3 py-2 text-sm font-medium"
              :class="{ 'bg-blue-100 text-blue-700': $route.path === '/student/labs' }"
              @click="isMenuOpen = false"
            >
              Laboratorios
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
  { name: 'Aulas', to: '/admin/classrooms', icon: 'fas fa-building' },
  { name: 'Importar Aulas', to: '/admin/classrooms/import', icon: 'fas fa-door-open' },
  { name: 'Cursos', to: '/admin/courses', icon: 'fas fa-book' },
  { name: 'Importar Cursos', to: '/admin/courses/import', icon: 'fas fa-file-import' },
  { name: 'Importar Usuarios', to: '/admin/users/import', icon: 'fas fa-users' },
  { name: 'Profesores', to: '/admin/professors', icon: 'fas fa-chalkboard-teacher' },
  { name: 'Estudiantes', to: '/admin/students', icon: 'fas fa-user-graduate' },
  { name: 'Secretarias', to: '/admin/secretaries', icon: 'fas fa-user' },
  { name: 'Matricular', to: '/admin/student-enrollment', icon: 'fas fa-user-plus' },
  { name: 'Reservas', to: '/reservation-management', icon: 'fas fa-calendar-check' }
]

const secretaryNavigation = [
  { name: 'Gestionar Aulas', to: '/admin/classrooms', icon: 'fas fa-building' },
  { name: 'Importar Aulas', to: '/admin/classrooms/import', icon: 'fas fa-door-open' },
  { name: 'Ver Estudiantes', to: '/admin/students', icon: 'fas fa-user-graduate' },
  { name: 'Gestionar Cursos', to: '/admin/courses', icon: 'fas fa-book' },
  { name: 'Importar Cursos', to: '/admin/courses/import', icon: 'fas fa-file-import' },
  { name: 'Importar Usuarios', to: '/admin/users/import', icon: 'fas fa-users' },
  { name: 'Matricular', to: '/admin/student-enrollment', icon: 'fas fa-user-plus' },
  { name: 'Gestionar Profesores', to: '/admin/professors', icon: 'fas fa-chalkboard-teacher' },
  { name: 'Gestionar Secretarias', to: '/admin/secretaries', icon: 'fas fa-user' },
  { name: 'Reservar Aula', to: '/classrooms', icon: 'fas fa-calendar-alt' },
  { name: 'Gestionar Reservas', to: '/reservation-management', icon: 'fas fa-calendar-check' }
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

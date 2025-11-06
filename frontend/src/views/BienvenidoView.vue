<template>
  <div class="min-h-screen bg-zinc-100">
    <!-- Panel superior -->
    <header class="bg-white shadow-md py-4 px-8 flex items-center justify-between border-b border-zinc-100">
      <!-- Lado izquierdo: info del usuario -->
      <div class="flex items-center space-x-4">
        <img 
          v-if="authStore.user.picture" 
          :src="authStore.user.picture" 
          alt="User" 
          class="w-14 h-14 rounded-full object-cover ring-2 ring-gray-300"
        />
        <div class="text-left">
          <h1 class="text-xl font-semibold text-sky-800">
            ¡Bienvenido, {{ authStore.user.name }}!
          </h1>
          <p class="text-gray-600 text-sm">{{ authStore.user.email }}</p>
          <p class="text-sky-600 font-medium text-sm">
            Rol: {{ authStore.user.role }}
            <span v-if="authStore.user.isAdmin" class="text-red-600 font-semibold">(Administrador)</span>
          </p>
        </div>
      </div>

      <!-- Lado derecho: botón -->
      <button 
        @click="handleLogout"
        :disabled="authStore.loading"
        class="px-5 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 
               transition-all duration-300 ease-in-out shadow-sm 
               disabled:opacity-50 disabled:cursor-not-allowed">
        <span v-if="!authStore.loading">Cerrar Sesión</span>
        <span v-else>Cerrando sesión...</span>
      </button>
    </header>

    <div class="flex justify-center bg-zinc-200 text-center">
      <div class="bg-white p-8 rounded-lg shadow-lg max-w-9/10 w-full mt-4">
        <div v-if="authStore.user && authStore.isAuthenticated"> 
          <!-- Botones de administración solo para administradores -->
          <div v-if="authStore.user.isAdmin" class="pt-4 border-gray-200">
            <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel de Administración</h3>
            <div class="grid grid-cols-1 gap-4 lg:grid-cols-4 sm:grid-cols-2 mt-5">
              <!-- Botones existentes de administración -->
              <PrincipalButton color="teal" to="/admin/classrooms" >
                <building-library-icon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Gestionar Aulas</h4>
              </PrincipalButton>
              <PrincipalButton color="orange" to="/admin/courses" >
                <BookOpenIcon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Gestionar Cursos</h4>
              </PrincipalButton>
              <PrincipalButton color="purple" to="/admin/professors" >
                <briefcase-icon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Gestionar Profesores</h4>
              </PrincipalButton>
              <PrincipalButton color="pink" to="/admin/students" >
                <academic-cap-icon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Gestionar Estudiantes</h4>
              </PrincipalButton>
              <PrincipalButton color="gray" to="/admin/secretaries" >
                <UserIcon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Gestionar Secretarias</h4>
              </PrincipalButton>
              
              <!-- NUEVOS BOTONES PARA ADMINISTRADORES -->
              <PrincipalButton color="blue" to="/classrooms" >
                <CalendarIcon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Reservar Aula</h4>
              </PrincipalButton>
              
              <PrincipalButton color="green" to="/reservation-management" >
                <ClipboardDocumentListIcon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Gestionar Reservas</h4>
              </PrincipalButton>
            </div>
          </div>

          <!-- Panel para profesores (no administradores) -->
          <div v-else-if="authStore.user.role === 'PROFESSOR'" class="pt-4 border-gray-200">
            <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel del Profesor</h3>
            <div class="grid grid-cols-1 gap-4 lg:grid-cols-3 sm:grid-cols-2 mt-5">
              <!-- Solo el botón de Reservar Aula para profesores -->
              <PrincipalButton color="blue" to="/classrooms" >
                <CalendarIcon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Reservar Aula</h4>
              </PrincipalButton>
            </div>
          </div>

          <!-- Panel para estudiantes -->
          <div v-else-if="authStore.user.role === 'STUDENT'" class="pt-4 border-gray-200">
            <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel del Estudiante</h3>
            
            <!-- Sección para ver cursos matriculados -->
            <div class="mt-6 p-6 bg-blue-50 rounded-lg border border-blue-200">
              <div class="flex items-center justify-between mb-4">
                <h4 class="text-lg font-semibold text-blue-800">Mis Cursos Matriculados</h4>
                <button 
                  @click="loadMyCourses"
                  :disabled="coursesLoading"
                  class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition disabled:opacity-50"
                >
                  <span v-if="!coursesLoading">Actualizar</span>
                  <span v-else>Cargando...</span>
                </button>
              </div>

              <!-- Estado de carga -->
              <div v-if="coursesLoading" class="text-center py-4">
                <p class="text-blue-600">Cargando cursos...</p>
              </div>

              <!-- Mensaje de error -->
              <div v-else-if="coursesError" class="text-center py-4">
                <p class="text-red-600">{{ coursesError }}</p>
                <button 
                  @click="loadMyCourses"
                  class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
                >
                  Reintentar
                </button>
              </div>

              <!-- Lista de cursos -->
              <div v-else-if="studentCourses.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div 
                  v-for="course in studentCourses" 
                  :key="course.courseID"
                  class="bg-white p-4 rounded-lg shadow border border-gray-200 hover:shadow-md transition"
                >
                  <h5 class="font-semibold text-gray-800 text-lg">{{ course.name }}</h5>
                  <div class="mt-2 text-sm text-gray-600 space-y-1">
                    <p><span class="font-medium">Código:</span> {{ course.courseID }}</p>
                    <p><span class="font-medium">Créditos:</span> {{ course.creditNumber }}</p>
                    <p><span class="font-medium">Grupo:</span> {{ course.groupLetter }}</p>
                    <p><span class="font-medium">Año:</span> {{ course.anio }}</p>
                    <p><span class="font-medium"></span> {{ course.courseTypeLabel }}</p>
                  </div>
                </div>
              </div>

              <!-- Mensaje cuando no hay cursos -->
              <div v-else class="text-center py-6">
                <p class="text-gray-500">No estás matriculado en ningún curso.</p>
                <p class="text-sm text-gray-400 mt-1">Contacta con administración para matricularte.</p>
              </div>
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
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useStudentCourseService } from '../services/studentCourseService'

import PrincipalButton from '../components/PrincipalButton.vue'
import { BookOpenIcon, AcademicCapIcon, BriefcaseIcon, BuildingLibraryIcon, UserIcon, CalendarIcon, ClipboardDocumentListIcon } from '@heroicons/vue/16/solid'

const router = useRouter()
const authStore = useAuthStore()

// Servicio para cursos del estudiante - IMPORTANTE: usar la destructuración correcta
const { courses: studentCourses, loading: coursesLoading, error: coursesError, fetchMyCourses } = useStudentCourseService()

onMounted(async () => {
  // Inicializar el estado de autenticación
  await authStore.initializeAuth()
  
  // Si es estudiante, cargar sus cursos automáticamente
  if (authStore.isAuthenticated && authStore.user.role === 'STUDENT') {
    await loadMyCourses()
  }
  
  // Si no está autenticado, redirigir al login después de un breve delay
  if (!authStore.isAuthenticated) {
    console.log('Usuario no autenticado, redirigiendo...')
    setTimeout(() => {
      router.push('/?error=not_authenticated')
    }, 2000)
  }
})

const loadMyCourses = async () => {
  try {
    await fetchMyCourses() // Ahora está definido porque viene del servicio
  } catch (error) {
    console.error('Error cargando cursos:', error)
  }
}

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
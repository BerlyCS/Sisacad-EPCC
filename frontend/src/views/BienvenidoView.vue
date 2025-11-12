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
          <div v-else-if="authStore.user.role === 'STUDENT'" class="pt-4 border-gray-200 space-y-6">
            <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">

              <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 w-full md:w-auto">
                <PrincipalButton color="blue" :to="studentProfileRoute">
                  <UserIcon class="w-15 h-15 mx-auto mt-2 mb-3" />
                  <h4 class="text-lg">Mi Perfil</h4>
                </PrincipalButton>
              </div>
            </div>

            <div v-if="profileLoading" class="bg-white shadow rounded-lg p-6 text-center">
              <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p class="text-gray-600 mt-3">Cargando tu información...</p>
            </div>

            <div v-else-if="profileError" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
              <h4 class="text-lg font-semibold text-red-800">No se pudo cargar tu información</h4>
              <p class="text-red-700 mt-2">{{ profileError }}</p>
              <button
                @click="loadStudentProfile"
                class="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
              >
                Reintentar
              </button>
            </div>

            <template v-else-if="studentProfile">
              <StudentProfileSummary :profile="studentProfile" subtitle="Información personal del estudiante" />

              <StudentCoursesCard
                :courses="studentCourses"
                :loading="profileLoading"
                :error="profileError"
                @refresh="loadStudentProfile"
                @select="openCourseDetail"
              />

              <StudentScheduleCard
                :entries="studentScheduleEntries"
                :loading="profileLoading"
                :error="''"
                @refresh="loadStudentProfile"
              />
            </template>

            <div v-else class="bg-white shadow rounded-lg p-6 text-center text-gray-500">
              No hay información disponible para tu perfil.
            </div>
          </div>

          <!-- Panel para secretarias -->
          <div v-else-if="authStore.user.role === 'SECRETARY'" class="pt-4 border-gray-200">
            <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel de la Secretaria</h3>
            <div class="grid grid-cols-1 gap-4 lg:grid-cols-2 sm:grid-cols-1 mt-5">
              <PrincipalButton color="blue" to="/admin/students" >
                <AcademicCapIcon class="w-15 h-15 mx-auto mt-2 mb-4"/>
                <h4 class="text-xl">Ver Estudiantes</h4>
              </PrincipalButton>
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
import { computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useStudentService } from '../services/studentService'

import PrincipalButton from '../components/PrincipalButton.vue'
import StudentProfileSummary from '../components/student/StudentProfileSummary.vue'
import StudentCoursesCard from '../components/student/StudentCoursesCard.vue'
import StudentScheduleCard from '../components/student/StudentScheduleCard.vue'
import { BookOpenIcon, AcademicCapIcon, BriefcaseIcon, BuildingLibraryIcon, UserIcon, CalendarIcon, ClipboardDocumentListIcon, ArrowPathIcon } from '@heroicons/vue/16/solid'

const router = useRouter()
const authStore = useAuthStore()
const { studentProfile, profileLoading, profileError, fetchStudentProfile } = useStudentService()

const userCui = computed(() => authStore.userCui)
const studentCourses = computed(() => studentProfile.value?.courses ?? [])
const studentScheduleEntries = computed(() => studentProfile.value?.schedule ?? [])
const studentProfileRoute = computed(() => (userCui.value ? `/students/${userCui.value}/profile` : '/students/profile'))

const loadStudentProfile = async () => {
  if (!userCui.value) {
    return
  }

  try {
    await fetchStudentProfile(userCui.value)
  } catch (error) {
    console.error('Error cargando perfil del estudiante:', error)
  }
}

const openCourseDetail = course => {
  if (!course || typeof course.courseId !== 'number') {
    return
  }

  router.push({ name: 'course-detail', params: { courseId: course.courseId } })
}

onMounted(async () => {
  // Inicializar el estado de autenticación
  await authStore.initializeAuth()

  if (authStore.isAuthenticated && authStore.user.role === 'STUDENT') {
    await loadStudentProfile()
  }

  // Si no está autenticado, redirigir al login después de un breve delay
  if (!authStore.isAuthenticated) {
    console.log('Usuario no autenticado, redirigiendo...')
    setTimeout(() => {
      router.push('/?error=not_authenticated')
    }, 2000)
  }
})

watch(userCui, async newCui => {
  if (!newCui || authStore.user.role !== 'STUDENT') {
    return
  }

  await loadStudentProfile()
})

const handleLogout = async () => {
  const success = await authStore.logout()

  if (success) {
    router.push('/?logout=success')
  }
}

const goToLogin = () => {
  router.push('/')
}
</script>
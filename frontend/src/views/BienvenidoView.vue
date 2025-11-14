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
          <AdminDashboard v-if="authStore.user.isAdmin" />
          <ProfessorDashboard v-else-if="authStore.user.role === 'PROFESSOR'" />
          <StudentDashboard
            v-else-if="authStore.user.role === 'STUDENT'"
            :student-profile="studentProfile"
            :profile-loading="profileLoading"
            :profile-error="profileError"
            :student-courses="studentCourses"
            :student-schedule-entries="studentScheduleEntries"
            :student-profile-route="studentProfileRoute"
            :load-student-profile="reloadStudentProfile"
            :open-course-detail="openCourseDetail"
          />
          <SecretaryDashboard v-else-if="authStore.user.role === 'SECRETARY'" />
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
import { useStudentProfile } from '../composables/useStudentProfile'

import { AdminDashboard, ProfessorDashboard, StudentDashboard, SecretaryDashboard } from '@/components/features/dashboard'

const router = useRouter()
const authStore = useAuthStore()
const { studentProfile, profileLoading, profileError, courses: studentCourses, schedule: studentScheduleEntries, loadProfile: loadStudentProfile } = useStudentProfile()

const resolveCui = value => {
  if (typeof value === 'string') {
    return value
  }
  if (value && typeof value === 'object' && 'value' in value) {
    const maybeValue = value.value
    return typeof maybeValue === 'string' ? maybeValue : ''
  }
  return ''
}

const getCurrentUserCui = () => resolveCui(authStore.userCui)

// Ruta al perfil del estudiante, requiere el CUI del usuario autenticado
const studentProfileRoute = computed(() => {
  const cui = getCurrentUserCui()
  return cui ? { name: 'student-profile', params: { cui } } : null
})

const fetchProfileByCui = async cui => {
  const targetCui = cui ?? getCurrentUserCui()
  if (!targetCui) {
    return
  }
  await loadStudentProfile(targetCui)
}

const reloadStudentProfile = () => fetchProfileByCui()

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
    await fetchProfileByCui()
  }

  // Si no está autenticado, redirigir al login después de un breve delay
  if (!authStore.isAuthenticated) {
    console.log('Usuario no autenticado, redirigiendo...')
    setTimeout(() => {
      router.push('/?error=not_authenticated')
    }, 2000)
  }
})

watch(() => authStore.userCui, async newCui => {
  if (authStore.user.role !== 'STUDENT') {
    return
  }

  const resolved = resolveCui(newCui)
  if (!resolved) {
    return
  }

  await fetchProfileByCui(resolved)
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
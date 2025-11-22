<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Asistencia</h2>
          <p class="text-gray-600 mt-1">Selecciona un curso para ver tu registro de asistencia.</p>
        </div>
        <button @click="$router.back()" class="text-gray-600 hover:text-gray-800">Volver</button>
      </div>

      <StudentCoursesCard
        :courses="studentCourses"
        :loading="profileLoading"
        :error="profileError"
        @refresh="loadStudentProfile"
        @select="openAttendance"
      />
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AdminLayout from '../components/ui/TopBar.vue'
import { StudentCoursesCard } from '@/components/features/student'
import { useStudentProfile } from '@/composables/useStudentProfile'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const authStore = useAuthStore()
const { courses: studentCourses, profileLoading, profileError, loadProfile: loadStudentProfile } = useStudentProfile()

const openAttendance = (course) => {
  if (!course) {
    return
  }

  const targetCourseId = course.courseId ?? course.courseCode
  if (!targetCourseId) {
    console.warn('No se pudo determinar el identificador del curso seleccionado')
    return
  }

  router.push({ name: 'student-attendance', params: { courseId: targetCourseId } })
}

const resolveCui = (value) => {
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

const fetchProfileByCui = async (cui) => {
  const targetCui = cui ?? getCurrentUserCui()
  if (!targetCui) {
    return
  }
  await loadStudentProfile(targetCui)
}

onMounted(async () => {
  await authStore.initializeAuth()
  if (authStore.isAuthenticated && authStore.user.role === 'STUDENT') {
    await loadStudentProfile()
  }
})
</script>

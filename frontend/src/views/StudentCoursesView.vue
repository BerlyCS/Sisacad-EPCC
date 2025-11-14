<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Mis Cursos</h2>
          <p class="text-gray-600 mt-1">Aquí puedes ver todos los cursos en los que estás matriculado.</p>
        </div>
      </div>

      <StudentCoursesCard
        :courses="studentCourses"
        :loading="profileLoading"
        :error="profileError"
        @refresh="loadStudentProfile"
        @select="openCourseDetail"
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

const openCourseDetail = (course) => {
  router.push(`/courses/${course.id}`)
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
    await fetchProfileByCui()
  }
})
</script>
<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Seguimiento académico</p>
          <h1 class="text-3xl font-bold text-gray-900">Mis Calificaciones</h1>
          <p class="text-gray-600 mt-1">Consulta el detalle de cada curso y verifica tu progreso en tiempo real.</p>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-blue-600 text-white font-medium shadow-sm hover:bg-blue-700"
          @click="handleReload"
          :disabled="loading"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke-width="1.5"
            stroke="currentColor"
            class="w-5 h-5"
          >
            <path stroke-linecap="round" stroke-linejoin="round" d="M16.862 4.487a9.46 9.46 0 0 1 2.598 3.642m.854 3.516a9.424 9.424 0 0 1-.964 3.31m-1.838 2.66a9.46 9.46 0 0 1-3.642 2.598m-3.516.854a9.424 9.424 0 0 1-3.31-.964m-2.66-1.838a9.46 9.46 0 0 1-2.598-3.642m-.854-3.516a9.424 9.424 0 0 1 .964-3.31m1.838-2.66a9.46 9.46 0 0 1 3.642-2.598m3.516-.854a9.424 9.424 0 0 1 3.31.964" />
          </svg>
          Actualizar
        </button>
      </header>

      <section class="grid gap-4 md:grid-cols-3">
        <div class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm">
          <p class="text-sm text-gray-500">Promedio general</p>
          <p class="text-4xl font-bold text-gray-900 mt-2">{{ finalAverage ?? '--' }}</p>
          <p class="text-xs text-gray-400 mt-1">Calculado a partir de tus cursos calificados</p>
        </div>
        <div class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm">
          <p class="text-sm text-gray-500">Estado</p>
          <p class="text-2xl font-semibold mt-2" :class="statusClass">{{ performanceStatus }}</p>
          <p class="text-xs text-gray-400 mt-1">Promedio mínimo para aprobar: 11.00</p>
        </div>
        <div class="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm">
          <p class="text-sm text-gray-500">Resumen</p>
          <div class="flex items-end gap-6 mt-3">
            <div>
              <p class="text-3xl font-bold text-green-600">{{ passedCourses }}</p>
              <p class="text-sm text-gray-500">Cursos aprobados</p>
            </div>
            <div>
              <p class="text-3xl font-bold text-amber-500">{{ pendingCourses }}</p>
              <p class="text-sm text-gray-500">Por mejorar</p>
            </div>
          </div>
        </div>
      </section>

      <StudentGradesList
        :grades="grades"
        :loading="loading"
        :error="error"
        @reload="handleReload"
      />
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { StudentGradesList } from '@/components/features/student'
import { useStudentGrades } from '@/composables/useStudentGrades'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const {
  grades,
  loading,
  error,
  finalAverage,
  performanceStatus,
  passedCourses,
  pendingCourses,
  refresh
} = useStudentGrades()

const statusClass = computed(() => {
  switch (performanceStatus.value) {
    case 'ALTO':
      return 'text-green-600'
    case 'REGULAR':
      return 'text-blue-600'
    case 'EN RIESGO':
      return 'text-red-500'
    default:
      return 'text-gray-400'
  }
})

const handleReload = async () => {
  try {
    await refresh()
  } catch (err) {
    console.error('No se pudieron recargar las calificaciones', err)
  }
}

onMounted(async () => {
  await authStore.initializeAuth()
  if (authStore.isAuthenticated && authStore.user?.role === 'STUDENT') {
    await handleReload()
  }
})
</script>

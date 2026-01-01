<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">
            Mi perfil
          </h2>
        </div>
        <button
          @click="goBack"
          class="inline-flex items-center px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition"
        >
          Volver
        </button>
      </div>

      <div v-if="profileLoading" class="bg-white shadow rounded-lg p-6 text-center">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p class="text-gray-600 mt-3">Cargando perfil del estudiante...</p>
      </div>

      <div v-else-if="profileError" class="bg-red-50 border border-red-200 rounded-lg p-6">
        <h3 class="text-lg font-semibold text-red-800">No se pudo cargar el perfil</h3>
        <p class="text-red-700 mt-2">{{ profileError }}</p>
        <button
          @click="reloadProfile"
          class="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
        >
          Reintentar
        </button>
      </div>

      <template v-else-if="studentProfile">
        <StudentProfileSummary :profile="studentProfile" subtitle="Resumen académico" />

        <section class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          <article
            v-for="card in statCards"
            :key="card.label"
            class="rounded-xl border border-indigo-100 bg-indigo-50 p-4"
          >
            <p class="text-xs font-semibold uppercase tracking-wide text-indigo-700">{{ card.label }}</p>
            <p class="mt-2 text-3xl font-bold text-gray-900">{{ card.value }}</p>
          </article>
        </section>

        <section class="grid grid-cols-1 gap-6 md:grid-cols-2">
          <article class="rounded-xl border border-emerald-100 bg-white p-6 shadow-sm">
            <header class="flex items-center justify-between mb-4">
              <div>
                <h3 class="text-lg font-semibold text-gray-900">Próxima sesión</h3>
                <p class="text-sm text-gray-500">Actualizado automáticamente según el horario</p>
              </div>
              <span class="inline-flex items-center rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-600">
                {{ nextSessionInfo?.startsInLabel ?? 'Sin horario' }}
              </span>
            </header>
            <div v-if="nextSessionInfo" class="space-y-2 text-sm text-gray-700">
              <p class="text-base font-semibold text-gray-900">{{ nextSessionInfo.courseName }}</p>
              <p class="text-gray-600">{{ nextSessionInfo.dayLabel }} · {{ nextSessionInfo.timeRange }}</p>
              <p class="text-gray-600">{{ nextSessionInfo.classroomName }}</p>
              <p class="text-xs inline-flex rounded-full bg-emerald-100 px-2 py-1 font-semibold text-emerald-800 uppercase tracking-wide">
                {{ nextSessionInfo.courseTypeLabel }}
              </p>
            </div>
            <p v-else class="text-sm text-gray-500">No hay sesiones próximas registradas.</p>
          </article>

          <article class="rounded-xl border border-blue-100 bg-white p-6 shadow-sm">
            <header class="mb-4">
              <h3 class="text-lg font-semibold text-gray-900">Cursos activos</h3>
              <p class="text-sm text-gray-500">Listado general del ciclo</p>
            </header>
            <div v-if="courses.length" class="overflow-x-auto">
              <table class="min-w-full divide-y divide-gray-200 text-sm">
                <thead>
                  <tr class="text-left text-xs font-semibold uppercase tracking-wide text-gray-500">
                    <th class="py-2 pr-4">Curso</th>
                    <th class="py-2 pr-4">Grupos</th>
                    <th class="py-2">Créditos</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 text-gray-700">
                  <tr v-for="course in courses" :key="course.courseId">
                    <td class="py-2 pr-4">
                      <p class="font-semibold">{{ course.name }}</p>
                      <p class="text-xs text-gray-500">Código {{ course.courseCode }}</p>
                    </td>
                    <td class="py-2 pr-4 text-sm">
                      <div v-if="course.groups?.length" class="flex flex-wrap gap-2">
                        <span
                          v-for="group in course.groups"
                          :key="group.courseGroupId"
                          class="inline-flex items-center gap-1 rounded-full bg-blue-50 px-2 py-1 text-xs font-semibold text-blue-700"
                        >
                          <span>{{ COURSE_TYPE_LABEL[group.courseType] ?? group.courseType }}</span>
                          <span class="text-gray-600">{{ group.groupLetter }}</span>
                        </span>
                      </div>
                      <span v-else class="text-gray-500">Sin grupos registrados</span>
                    </td>
                    <td class="py-2 text-sm">{{ course.creditNumber ?? '—' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p v-else class="text-sm text-gray-500">Aún no hay cursos registrados.</p>
          </article>
        </section>

        <section class="rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
          <header class="mb-4">
            <h3 class="text-lg font-semibold text-gray-900">Horario semanal</h3>
            <p class="text-sm text-gray-500">Organizado por día y bloque horario</p>
          </header>
          <div v-if="scheduleDays.length" class="divide-y divide-gray-100">
            <article v-for="day in scheduleDays" :key="day.key" class="py-4">
              <p class="text-sm font-semibold uppercase tracking-wide text-gray-500">{{ day.label }}</p>
              <ul class="mt-3 space-y-2">
                <li
                  v-for="slot in day.slots"
                  :key="slot.id"
                  class="rounded-lg border border-gray-100 bg-gray-50 px-4 py-2 text-sm text-gray-700"
                >
                  <div class="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                    <div>
                      <p class="font-semibold text-gray-900">{{ slot.courseName }}</p>
                      <p class="text-xs text-gray-500">{{ slot.courseTypeLabel }} · Aula {{ slot.classroomName }}</p>
                    </div>
                    <span class="text-sm font-medium text-gray-800">{{ slot.timeRange }}</span>
                  </div>
                </li>
              </ul>
            </article>
          </div>
          <p v-else class="text-sm text-gray-500">No se encontró un horario asignado.</p>
        </section>
      </template>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../components/ui/TopBar.vue'
import StudentProfileSummary from '../components/features/student/StudentProfileSummary.vue'
import { useStudentProfile } from '../composables/useStudentProfile'
import { useAuthStore } from '../stores/auth'

// Composable state (was missing before causing undefined references)
const {
  studentProfile,
  profileLoading,
  profileError,
  loadProfile,
  courses,
  courseStats,
  scheduleByDay,
  nextSession
} = useStudentProfile()

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const cui = computed(() => {
  const paramCui = route.params.cui
  if (paramCui) return String(paramCui)
  // For own profile route
  return authStore.userCui ? String(authStore.userCui) : ''
})

const loadProfileForCui = () => {
  if (cui.value) {
    loadProfile(cui.value)
    return
  }
  loadProfile()
}

const reloadProfile = () => {
  loadProfileForCui()
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/bienvenido')
  }
}

const COURSE_TYPE_LABEL: Record<string, string> = {
  LAB: 'Laboratorio',
  PRACTICE: 'Práctica',
  THEORY: 'Teoría'
}

const statCards = computed(() => [
  {
    label: 'Cursos',
    value: courseStats.value.totalCourses,
  },
  {
    label: 'Créditos',
    value: courseStats.value.totalCredits,
  },
  {
    label: 'Horas laboratorio',
    value: courseStats.value.labSections,
  }
])

const scheduleDays = computed(() => scheduleByDay.value)

const nextSessionInfo = computed(() => {
  if (!nextSession.value) {
    return null
  }
  return {
    ...nextSession.value,
    courseTypeLabel: COURSE_TYPE_LABEL[nextSession.value.courseType] ?? 'Teoría'
  }
})

onMounted(() => {
  loadProfileForCui()
})

watch(
  () => route.params.cui,
  () => {
    loadProfileForCui()
  }
)
</script>

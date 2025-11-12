<template>
  <AdminLayout>
    <div class="space-y-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h2 class="text-2xl font-semibold text-gray-800">Perfil del Estudiante</h2>
          <p class="text-gray-600">Consulta la información académica y los cursos matriculados.</p>
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

      <div v-else-if="studentProfile" class="space-y-6">
        <!-- Datos del estudiante -->
        <section class="bg-white shadow rounded-lg p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-4">Información personal</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
              <p class="text-sm text-blue-700 uppercase tracking-wide">Nombre completo</p>
              <p class="text-gray-900 font-semibold mt-1">
                {{ studentProfile.student.nombres }} {{ studentProfile.student.apellidoPaterno }} {{ studentProfile.student.apellidoMaterno }}
              </p>
            </div>
            <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
              <p class="text-sm text-blue-700 uppercase tracking-wide">CUI</p>
              <p class="text-gray-900 font-semibold mt-1">{{ studentProfile.student.cui }}</p>
            </div>
            <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
              <p class="text-sm text-blue-700 uppercase tracking-wide">DNI</p>
              <p class="text-gray-900 font-semibold mt-1">{{ studentProfile.student.documentoIdentidad }}</p>
            </div>
            <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
              <p class="text-sm text-blue-700 uppercase tracking-wide">Correo institucional</p>
              <p class="text-gray-900 font-semibold mt-1 break-words">{{ studentProfile.student.correoInstitucional }}</p>
            </div>
            <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
              <p class="text-sm text-blue-700 uppercase tracking-wide">Año académico</p>
              <p class="text-gray-900 font-semibold mt-1">{{ studentProfile.student.anio ?? 'No registrado' }}</p>
            </div>
          </div>
        </section>

        <!-- Cursos matriculados -->
        <section class="bg-white shadow rounded-lg p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800">Cursos matriculados</h3>
            <span class="text-sm text-gray-600">Total: {{ studentProfile.courses.length }}</span>
          </div>

          <div v-if="studentProfile.courses.length === 0" class="text-center py-6 text-gray-500">
            El estudiante no tiene cursos matriculados.
          </div>

          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Código</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombre</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Grupo</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Créditos</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="course in studentProfile.courses" :key="course.courseId" class="hover:bg-gray-50">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">{{ course.courseCode }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ course.name }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ course.courseTypeLabel }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ course.groupLetter || '-' }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ course.creditNumber ?? '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>

        <!-- Horario -->
        <section class="bg-white shadow rounded-lg p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-800">Horario académico</h3>
            <span class="text-sm text-gray-600">Módulo en construcción</span>
          </div>

          <div v-if="hasSchedule" class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Día</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Curso</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tipo</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Horario</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Aula</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="slot in studentProfile.schedule" :key="`${slot.courseId}-${slot.dayOfWeek}-${slot.startTime}`">
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ slot.dayOfWeek }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ slot.courseName }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ formatCourseType(slot.courseType) }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ slot.startTime }} - {{ slot.endTime }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ slot.classroomName || '-' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="border border-dashed border-gray-300 rounded-lg p-6 text-center text-gray-500">
            Aún no hemos habilitado el módulo de horario interactivo.
          </div>
        </section>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminLayout from '../components/TopBar.vue'
import { useStudentService } from '../services/studentService'

const route = useRoute()
const router = useRouter()

const { studentProfile, profileLoading, profileError, fetchStudentProfile } = useStudentService()

const cui = computed(() => String(route.params.cui ?? ''))

const loadProfile = () => {
  if (!cui.value) {
    return
  }
  fetchStudentProfile(cui.value)
}

const reloadProfile = () => {
  loadProfile()
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/admin/students')
  }
}

const hasSchedule = computed(() => Boolean(studentProfile.value?.schedule?.length))

const formatCourseType = (type) => {
  if (!type) return 'Teoría'
  return type === 'LAB' ? 'Laboratorio' : 'Teoría'
}

onMounted(() => {
  loadProfile()
})

watch(
  () => route.params.cui,
  () => {
    loadProfile()
  }
)
</script>

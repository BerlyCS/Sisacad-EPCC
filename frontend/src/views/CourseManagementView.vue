<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800">Gestión de Cursos</h2>
        <p class="text-gray-600 mt-1">Administra los cursos académicos</p>
      </div>

      <div class="p-6">
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando cursos...</p>
        </div>

        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button 
            @click="fetchCourses"
            class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
          >
            Reintentar
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Créditos</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Grupo</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase"># Estudiantes</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase"># Docentes</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="course in courses" :key="course.courseID" class="hover:bg-gray-50">
                <td class="px-6 py-4 text-sm font-mono text-gray-900">{{ course.courseID }}</td>
                <td class="px-6 py-4 text-sm text-gray-900">{{ course.name }}</td>
                <td class="px-6 py-4 text-sm text-blue-700 font-semibold">
                  {{ course.creditNumber }} créditos
                </td>
                <td class="px-6 py-4 text-sm text-purple-700">
                  Grupo {{ course.groupLetter || '-' }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">
                  {{ course.enrolledStudentIDs.length }}
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">
                  {{ course.teacherIDs.length }}
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="courses.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron cursos registrados.</p>
          </div>
        </div>

        <!-- Estadísticas -->
        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Cursos</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ courses.length }}</p>
          </div>
          <div class="bg-green-50 border border-green-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-green-800">Créditos Totales</h3>
            <p class="text-3xl font-bold text-green-600 mt-2">
              {{ courses.reduce((total, course) => total + (course.creditNumber || 0), 0) }}
            </p>
          </div>
          <div class="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-purple-800">Total Estudiantes Inscritos</h3>
            <p class="text-3xl font-bold text-purple-600 mt-2">
              {{ courses.reduce((total, course) => total + course.enrolledStudentIDs.length, 0) }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AdminLayout from '../components/AdminLayout.vue'
import { useCourseService } from '../services/courseService'

const { courses, loading, error, fetchCourses } = useCourseService()

onMounted(() => {
  fetchCourses()
})
</script>

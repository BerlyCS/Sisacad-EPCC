<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800">Gestión de Estudiantes</h2>
        <p class="text-gray-600 mt-1">Administra los estudiantes registrados</p>
      </div>
      
      <div class="p-6">
        <!-- Controles de ordenamiento (usados por admin y secretaria) -->
        <div class="mb-4 flex flex-wrap gap-4 items-end">
          <div>
            <label class="block text-sm font-medium text-gray-700">Ordenar por</label>
            <select v-model="sortBy" @change="fetchSorted" class="mt-1 block w-48 pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
              <option value="dni">DNI</option>
              <option value="cui">CUI</option>
              <option value="name">Nombre</option>
              <option value="apellidos">Apellidos</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Dirección</label>
            <select v-model="direction" @change="fetchSorted" class="mt-1 block w-48 pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
              <option value="asc">Ascendente</option>
              <option value="desc">Descendente</option>
            </select>
          </div>
          <button @click="fetchSorted" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition">Aplicar</button>
        </div>
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando estudiantes...</p>
        </div>
        
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button 
            @click="fetchSorted"
            class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
          >
            Reintentar
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  DNI
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  CUI
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombres
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Apellidos
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Correo
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="student in students" :key="student.documentoIdentidad" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                  {{ student.documentoIdentidad }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                  {{ student.cui }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ student.nombres }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ student.apellidoPaterno }} {{ student.apellidoMaterno }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <a :href="`mailto:${student.correoInstitucional}`" class="text-blue-600 hover:text-blue-800">
                    {{ student.correoInstitucional }}
                  </a>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="students.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron estudiantes registrados.</p>
          </div>
        </div>

        <!-- Estadísticas -->
        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Estudiantes</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ students.length }}</p>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AdminLayout from '../components/TopBar.vue'
import { useStudentService } from '../services/studentService'

const sortBy = ref('dni')
const direction = ref('asc')
const { students, loading, error, fetchStudentsSorted } = useStudentService()

const fetchSorted = () => {
  fetchStudentsSorted(sortBy.value, direction.value)
}

onMounted(() => {
  fetchSorted()
})
</script>
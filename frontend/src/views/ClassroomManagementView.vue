<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800">Gestión de Aulas</h2>
        <p class="text-gray-600 mt-1">Administra las aulas disponibles en la institución</p>
      </div>
      
      <div class="p-6">
        <!-- Estado de carga y error -->
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando aulas...</p>
        </div>
        
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button 
            @click="fetchClassrooms"
            class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
          >
            Reintentar
          </button>
        </div>

        <!-- Tabla de aulas -->
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  ID
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombre
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Capacidad
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Ubicación
                </th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tipo
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="classroom in classrooms" :key="classroom.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ classroom.id }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ classroom.name }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ classroom.capacity }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ classroom.location }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ classroom.type }}
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Mensaje cuando no hay datos -->
          <div v-if="classrooms.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron aulas registradas.</p>
          </div>
        </div>

        <!-- Estadísticas -->
        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Aulas</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ classrooms.length }}</p>
          </div>
          <div class="bg-green-50 border border-green-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-green-800">Capacidad Total</h3>
            <p class="text-3xl font-bold text-green-600 mt-2">
              {{ classrooms.reduce((total, room) => total + (room.capacity || 0), 0) }}
            </p>
          </div>
          <div class="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-purple-800">Tipos de Aula</h3>
            <p class="text-3xl font-bold text-purple-600 mt-2">
              {{ new Set(classrooms.map(room => room.type)).size }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AdminLayout from '../components/TopBar.vue'
import { useClassroomService } from '../services/classroomService'

const { classrooms, loading, error, fetchClassrooms } = useClassroomService()

onMounted(() => {
  fetchClassrooms()
})
</script>
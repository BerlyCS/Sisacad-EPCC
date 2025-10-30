<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200">
        <h2 class="text-xl font-semibold text-gray-800">Gestión de Secretarias</h2>
        <p class="text-gray-600 mt-1">Administra el personal administrativo</p>
      </div>
      
      <div class="p-6">
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando secretarias...</p>
        </div>
        
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button 
            @click="fetchSecretaries"
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
              <tr v-for="secretary in secretaries" :key="secretary.dni" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                  {{ secretary.dni }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ secretary.name }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {{ secretary.paternalSurname }} {{ secretary.maternalSurname }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  <a :href="`mailto:${secretary.institutionalEmail}`" class="text-blue-600 hover:text-blue-800">
                    {{ secretary.institutionalEmail }}
                  </a>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="secretaries.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron secretarias registradas.</p>
          </div>
        </div>

        <!-- Estadísticas -->
        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Secretarias</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ secretaries.length }}</p>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted } from 'vue'
import AdminLayout from '../components/TopBar.vue'
import { useSecretaryService } from '../services/secretaryService'

const { secretaries, loading, error, fetchSecretaries } = useSecretaryService()

onMounted(() => {
  fetchSecretaries()
})
</script>
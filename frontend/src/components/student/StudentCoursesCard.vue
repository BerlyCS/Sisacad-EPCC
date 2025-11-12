<template>
  <section class="bg-white shadow rounded-lg p-6">
    <header class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between mb-4">
      <div>
        <h3 class="text-lg font-semibold text-gray-800">Cursos matriculados</h3>
        <p class="text-sm text-gray-500">Listado de asignaturas activas del estudiante.</p>
      </div>
      <button
        v-if="refreshable"
        @click="$emit('refresh')"
        :disabled="loading"
        class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span v-if="!loading">Actualizar</span>
        <span v-else>Cargando...</span>
      </button>
    </header>

    <div v-if="loading" class="text-center py-6 text-blue-600">
      Cargando cursos...
    </div>

    <div v-else-if="error" class="text-center py-6 text-red-600">
      {{ error }}
    </div>

    <div v-else-if="!courses?.length" class="text-center py-6 text-gray-500">
      No se encontraron cursos matriculados.
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
          <tr v-for="course in courses" :key="course.courseId">
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
</template>

<script setup>
const props = defineProps({
  courses: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  refreshable: {
    type: Boolean,
    default: true
  }
})

defineEmits(['refresh'])
</script>

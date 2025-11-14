<template>
  <section class="bg-white shadow rounded-lg p-6">
    <header class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between mb-4">
      <div>
        <h3 class="text-lg font-semibold text-gray-800">Horario académico</h3>
      </div>
      <button
        v-if="refreshable"
        @click="$emit('refresh')"
        :disabled="loading"
        class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <span v-if="!loading">Actualizar</span>
        <span v-else>Cargando...</span>
      </button>
    </header>

    <div v-if="loading" class="text-center py-6 text-indigo-600">
      Cargando horario...
    </div>

    <div v-else-if="error" class="text-center py-6 text-red-600">
      {{ error }}
    </div>

    <div v-else-if="!entries?.length" class="border border-dashed border-gray-300 rounded-lg p-6 text-center text-gray-500">
      Aún no se registran horarios para este estudiante.
    </div>

    <div v-else class="overflow-x-auto">
      <StudentScheduleTable :entries="entries" />
    </div>
  </section>
</template>

<script setup>
import StudentScheduleTable from './StudentScheduleTable.vue'

defineProps({
  entries: {
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

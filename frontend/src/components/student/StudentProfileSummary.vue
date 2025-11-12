<template>
  <section class="bg-white shadow rounded-lg p-6">
    <header class="mb-4">
      <h3 class="text-lg font-semibold text-gray-800">Información personal</h3>
      <p v-if="subtitle" class="text-sm text-gray-500 mt-1">{{ subtitle }}</p>
    </header>

    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
      <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
        <p class="text-xs font-semibold text-blue-700 uppercase tracking-wide">
          Nombre completo
        </p>
        <p class="mt-1 text-gray-900 font-semibold">
          {{ fullName }}
        </p>
      </div>

      <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
        <p class="text-xs font-semibold text-blue-700 uppercase tracking-wide">
          CUI
        </p>
        <p class="mt-1 text-gray-900 font-semibold">{{ student.cui || 'No disponible' }}</p>
      </div>

      <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
        <p class="text-xs font-semibold text-blue-700 uppercase tracking-wide">
          DNI
        </p>
        <p class="mt-1 text-gray-900 font-semibold">{{ student.documentoIdentidad || 'No disponible' }}</p>
      </div>

      <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
        <p class="text-xs font-semibold text-blue-700 uppercase tracking-wide">
          Correo institucional
        </p>
        <p class="mt-1 text-gray-900 font-semibold break-words">
          {{ student.correoInstitucional || 'No registrado' }}
        </p>
      </div>

      <div class="bg-blue-50 border border-blue-100 rounded-lg p-4">
        <p class="text-xs font-semibold text-blue-700 uppercase tracking-wide">
          Año académico
        </p>
        <p class="mt-1 text-gray-900 font-semibold">{{ student.anio ?? 'No registrado' }}</p>
      </div>

      <slot name="extra"></slot>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  profile: {
    type: Object,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})

const student = computed(() => props.profile?.student ?? {})

const fullName = computed(() => {
  const nombres = student.value.nombres || ''
  const apellidoPaterno = student.value.apellidoPaterno || ''
  const apellidoMaterno = student.value.apellidoMaterno || ''
  return `${nombres} ${apellidoPaterno} ${apellidoMaterno}`.trim() || 'Nombre no disponible'
})
</script>

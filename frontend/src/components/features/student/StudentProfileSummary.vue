<template>
  <section class="rounded-2xl border border-slate-200 bg-white shadow-lg">
    <div class="px-6 py-5 lg:px-8">
      <header class="flex flex-col gap-1">
        <h3 class="text-2xl font-semibold text-slate-900">Mi Información personal</h3>
      </header>

      <div class="mt-5 grid gap-6 md:grid-cols-[1.2fr_0.8fr]">
        <div>
          <p class="text-xs uppercase tracking-[0.3em] text-slate-400">Estudiante</p>
          <p class="text-2xl font-semibold text-slate-900">{{ fullName }}</p>
          <p class="mt-2 text-sm text-slate-500">{{ student.institutionalEmail || 'Correo no registrado' }}</p>
        </div>
        <div class="grid gap-3 text-sm text-slate-600">
          <div class="flex items-center justify-between border-b border-slate-100 pb-2">
            <span>CUI</span>
            <span class="font-semibold text-slate-900">{{ student.cui || '—' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span>Año académico</span>
            <span class="font-semibold text-slate-900">{{ student.enrollmentYear ?? '—' }}</span>
          </div>
        </div>
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
  const firstNames = student.value.firstNames || ''
  const paternalSurname = student.value.paternalSurname || ''
  const maternalSurname = student.value.maternalSurname || ''
  return `${firstNames} ${paternalSurname} ${maternalSurname}`.trim() || 'Nombre no disponible'
})

</script>

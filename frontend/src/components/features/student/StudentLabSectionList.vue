<template>
  <section class="bg-white shadow rounded-2xl p-5 space-y-4 h-full">
    <header class="space-y-1">
      <p class="text-xs font-semibold uppercase tracking-wide text-indigo-500">Laboratorios disponibles</p>
      <h2 class="text-xl font-semibold text-gray-900">
        {{ course ? course.name : 'Selecciona un curso' }}
      </h2>
    </header>

    <div v-if="!course" class="rounded-xl border border-dashed border-gray-200 p-6 text-sm text-gray-500">
      Elige primero un curso para ver los laboratorios.
    </div>

    <div v-else class="space-y-4">
      <div v-if="assignedLab" class="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3">
        <p class="text-sm font-semibold text-emerald-800">Laboratorio asignado</p>
        <p class="text-sm text-emerald-700">
          Estás inscrito en el grupo {{ assignedLab.groupLetter ?? '—' }} ({{ assignedLab.name }}).
        </p>
      </div>

      <div v-if="loading" class="py-6 text-sm text-blue-600">Cargando laboratorios...</div>
      <p v-else-if="error" class="py-6 text-sm text-red-600">{{ error }}</p>
      <p v-else-if="sections.length === 0" class="py-6 text-sm text-gray-500">
        Este curso no tiene laboratorios configurados.
      </p>

      <div v-else class="space-y-3">
        <article
          v-for="section in sections"
          :key="section.courseId"
          class="rounded-xl border px-4 py-3 transition"
          :class="sectionCardClass(section)"
        >
          <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <p class="text-sm font-semibold text-gray-900">
                {{ section.name }} · Grupo {{ section.groupLetter ?? '—' }}
              </p>
              <p class="text-xs text-gray-500">
                Código {{ section.courseCode ?? '—' }} · Capacidad {{ section.labCapacity ?? '—' }}
              </p>
              <p class="text-xs mt-1" :class="remainingClass(section)">
                {{ remainingCopy(section) }}
              </p>
            </div>
            <div class="flex flex-col gap-2 md:flex-row md:items-center">
              <button
                type="button"
                class="rounded-lg border border-gray-200 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
                :disabled="loading"
                @click="$emit('validate', section.courseId)"
              >
                Validar disponibilidad
              </button>
              <button
                type="button"
                class="rounded-lg px-3 py-1.5 text-sm font-semibold text-white shadow-sm"
                :class="confirmButtonClass(section)"
                :disabled="isDisabledSection(section)"
                @click="handleEnroll(section)"
              >
                Confirmar laboratorio
              </button>
            </div>
          </div>
        </article>
      </div>

      <div v-if="infoMessage" class="rounded-lg border border-blue-100 bg-blue-50 px-4 py-2 text-sm text-blue-700">
        {{ infoMessage }}
      </div>
      <div v-if="errorMessage" class="rounded-lg border border-red-100 bg-red-50 px-4 py-2 text-sm text-red-600">
        {{ errorMessage }}
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { Course } from '@/services/studentCourseService'
import type { LabSection, EnrollmentValidationResult } from '@/services/labEnrollmentService'

const props = defineProps<{
  course: Course | null
  assignedLab: Course | null
  sections: LabSection[]
  loading?: boolean
  error?: string
  validationResult?: EnrollmentValidationResult | null
  enrollmentResult?: EnrollmentValidationResult | null
  infoMessage?: string
  errorMessage?: string
  enrollmentLoading?: boolean
}>()

const emit = defineEmits<{ (e: 'validate', labCourseId: number): void; (e: 'enroll', labCourseId: number): void }>()

const isDisabledSection = (section: LabSection): boolean => {
  if (props.enrollmentLoading) {
    return true
  }
  if (props.assignedLab && props.assignedLab.courseId === section.courseId) {
    return true
  }
  if (section.remainingSeats !== null && section.remainingSeats <= 0) {
    return true
  }
  return false
}

const sectionCardClass = (section: LabSection) => {
  if (props.assignedLab && props.assignedLab.courseId === section.courseId) {
    return 'border-emerald-300 bg-emerald-50/60'
  }
  if (props.validationResult && props.validationResult.courseId === section.courseId) {
    return props.validationResult.allowed
      ? 'border-blue-400 bg-blue-50/60'
      : 'border-red-200 bg-red-50/60'
  }
  return 'border-gray-200 hover:border-blue-200 hover:bg-blue-50/40'
}

const remainingCopy = (section: LabSection) => {
  if (section.remainingSeats === null) {
    return 'Vacantes no disponibles'
  }
  if (section.remainingSeats === 0) {
    return 'Sin vacantes disponibles'
  }
  return `${section.remainingSeats} vacantes libres`
}

const remainingClass = (section: LabSection) => {
  if (section.remainingSeats === null) {
    return 'text-gray-500'
  }
  if (section.remainingSeats === 0) {
    return 'text-red-600'
  }
  if (section.remainingSeats <= 3) {
    return 'text-amber-600'
  }
  return 'text-emerald-600'
}

const confirmButtonClass = (section: LabSection) => [
  isDisabledSection(section)
    ? 'bg-gray-300 cursor-not-allowed'
    : 'bg-indigo-600 hover:bg-indigo-700',
  'disabled:opacity-50 disabled:cursor-not-allowed'
]

const handleEnroll = (section: LabSection) => {
  if (isDisabledSection(section)) {
    return
  }
  emit('enroll', section.courseId)
}
</script>

<template>
  <transition name="fade">
    <div v-if="open" class="fixed inset-0 z-40 flex justify-end">
      <div class="flex-1 bg-black/40" @click="$emit('close')"></div>
      <aside class="w-full max-w-md bg-white h-full shadow-xl p-6 overflow-y-auto">
        <header class="flex items-start justify-between mb-6">
          <div>
            <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Resumen del curso</p>
            <h2 class="text-2xl font-bold text-gray-900">{{ course?.courseName ?? 'Selecciona un curso' }}</h2>
            <p class="text-gray-500">{{ course?.courseCode }}</p>
          </div>
          <button
            type="button"
            class="text-gray-400 hover:text-gray-600"
            @click="$emit('close')"
          >
            <span class="sr-only">Cerrar</span>
            ✕
          </button>
        </header>

        <div v-if="loading" class="text-blue-600">Calculando estadísticas...</div>
        <div v-else-if="error" class="text-red-500">{{ error }}</div>
        <div v-else-if="stats" class="space-y-6">
          <div class="grid grid-cols-2 gap-4">
            <div class="rounded-2xl border border-gray-200 p-4">
              <p class="text-xs text-gray-500">Promedio evaluaciones</p>
              <p class="text-2xl font-semibold text-gray-900">{{ formatMetric(stats.meanGrade) }}</p>
            </div>
            <div class="rounded-2xl border border-gray-200 p-4">
              <p class="text-xs text-gray-500">Promedio final</p>
              <p class="text-2xl font-semibold text-gray-900">{{ formatMetric(stats.meanFinalGrade) }}</p>
            </div>
            <div class="rounded-2xl border border-gray-200 p-4">
              <p class="text-xs text-gray-500">Nota más alta</p>
              <p class="text-2xl font-semibold text-green-600">{{ formatMetric(stats.highestFinalGrade) }}</p>
            </div>
            <div class="rounded-2xl border border-gray-200 p-4">
              <p class="text-xs text-gray-500">Nota más baja</p>
              <p class="text-2xl font-semibold text-red-500">{{ formatMetric(stats.lowestFinalGrade) }}</p>
            </div>
          </div>

          <div class="rounded-2xl border border-gray-100 bg-gray-50 p-4">
            <p class="text-sm text-gray-600">
              Estudiantes calificados: <span class="font-semibold text-gray-900">{{ stats.gradedStudents }}</span>
            </p>
            <p class="text-xs text-gray-500 mt-1">Incluye notas continuas y exámenes.</p>
          </div>
        </div>
        <div v-else class="text-gray-500 text-sm">
          Selecciona un curso para ver sus estadísticas.
        </div>
      </aside>
    </div>
  </transition>
</template>

<script setup lang="ts">
import type { CourseGradeStats, ProfessorCourseSummary } from '@/services/gradeService'

const props = defineProps<{
  open: boolean
  course: ProfessorCourseSummary | null
  stats: CourseGradeStats | null
  loading: boolean
  error: string
}>()

defineEmits<{ (e: 'close'): void }>()

const formatMetric = (value: number | null | undefined): string => {
  if (value == null || Number.isNaN(value)) {
    return '--'
  }
  return value.toFixed(2)
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

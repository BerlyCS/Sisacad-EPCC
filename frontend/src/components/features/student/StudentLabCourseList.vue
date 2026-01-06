<template>
  <section class="bg-white shadow rounded-2xl p-5 space-y-4 h-full">
    <header class="space-y-1">
      <div class="flex items-center justify-between gap-4">
        <h2 class="text-xl font-semibold text-gray-900">Selecciona el curso</h2>
        <span
          v-if="courses.length"
          class="inline-flex items-center gap-1 rounded-full bg-blue-50 px-3 py-1 text-xs font-medium text-blue-700"
        >
          {{ pendingCount }} pendientes
        </span>
      </div>
    </header>

    <div v-if="loading" class="py-6 text-sm text-blue-600">Cargando cursos...</div>
    <p v-else-if="error" class="py-6 text-sm text-red-600">{{ error }}</p>
    <p v-else-if="!courses.length" class="py-6 text-sm text-gray-500">No tienes cursos teóricos disponibles.</p>

    <div v-else class="space-y-3 max-h-[480px] overflow-y-auto pr-1">
      <button
        v-for="course in courses"
        :key="course.courseId"
        type="button"
        :class="[
          'w-full text-left rounded-xl border px-4 py-3 transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2',
          course.courseId === selectedCourseId
            ? 'border-blue-500 bg-blue-50/70'
            : 'border-gray-200 hover:border-blue-300 hover:bg-blue-50/40'
        ]"
        @click="$emit('select', course.courseId)"
      >
        <div class="flex items-center justify-between gap-3">
          <div>
            <p class="font-semibold text-gray-900">{{ course.name }}</p>
            <p class="text-xs text-gray-500">Código {{ course.courseCode ?? '—' }} · Grupo {{ course.groupLetter ?? '—' }}</p>
          </div>
          <span
            class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
            :class="assignmentBadgeClass(course.courseId)"
          >
            {{ assignmentLabel(course.courseId) }}
          </span>
        </div>
        <p v-if="labAssignments[course.courseId]" class="mt-1 text-sm text-gray-600">
          Laboratorio: {{ labAssignments[course.courseId]?.name }} · Grupo {{ labAssignments[course.courseId]?.groupLetter ?? '—' }}
        </p>
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Course } from '@/services/studentCourseService'

const props = defineProps<{
  courses: Course[]
  labAssignments: Record<number, Course | undefined>
  labAvailability: Record<number, 'unknown' | 'available' | 'unavailable'>
  selectedCourseId: number | null
  loading?: boolean
  error?: string
}>()

defineEmits<{ (e: 'select', value: number): void }>()

const pendingCount = computed(() =>
  props.courses.filter(course =>
    props.labAvailability[course.courseId] !== 'unavailable' &&
    !props.labAssignments[course.courseId]
  ).length
)

const assignmentLabel = (courseId: number): string => {
  if (props.labAssignments[courseId]) {
    return 'Laboratorio asignado'
  }
  if (props.labAvailability[courseId] === 'unavailable') {
    return 'No disponible'
  }
  return 'Pendiente'
}

const assignmentBadgeClass = (courseId: number) =>
  props.labAssignments[courseId]
    ? 'bg-emerald-50 text-emerald-700 border border-emerald-100'
    : props.labAvailability[courseId] === 'unavailable'
      ? 'bg-gray-100 text-gray-600 border border-gray-200'
      : 'bg-amber-50 text-amber-700 border border-amber-100'
</script>

<template>
  <section class="space-y-4">
    <div v-if="loading" class="bg-white border border-blue-100 rounded-2xl p-6 flex items-center justify-center text-blue-600">
      Cargando tus calificaciones...
    </div>

    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-2xl p-6">
      <p class="text-red-700 mb-4">{{ error }}</p>
      <button
        type="button"
        class="inline-flex items-center px-4 py-2 rounded-lg bg-red-600 text-white text-sm font-medium hover:bg-red-700"
        @click="$emit('reload')"
      >
        Intentar nuevamente
      </button>
    </div>

    <div v-else-if="!grades.length" class="bg-white border border-dashed border-gray-200 rounded-2xl p-6 text-center text-gray-500">
      Aún no registramos calificaciones para ti.
    </div>

    <div v-else class="grid gap-5 md:grid-cols-2">
      <StudentGradeCard
        v-for="grade in grades"
        :key="`${grade.courseCode}-${grade.courseId}`"
        :grade="grade"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import type { StudentGrade } from '@/services/gradeService'
import StudentGradeCard from './StudentGradeCard.vue'

defineProps<{
  grades: StudentGrade[]
  loading: boolean
  error: string
}>()

defineEmits<{ (e: 'reload'): void }>()
</script>

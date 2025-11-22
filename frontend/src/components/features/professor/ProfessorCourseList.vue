<template>
  <section class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-4">
    <header class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">{{ heading }}</p>
        <h2 class="text-2xl font-bold text-gray-900">{{ subheading }}</h2>
        <p class="text-gray-500">{{ description }}</p>
      </div>
      <input
        v-model="query"
        type="search"
        placeholder="Buscar por nombre o código"
        class="w-full md:w-64 rounded-xl border-gray-200 focus:border-blue-500 focus:ring-blue-200"
      >
    </header>

    <div v-if="loading" class="text-blue-600 text-sm">Cargando cursos asignados...</div>
    <div v-else-if="error" class="text-red-500 text-sm flex items-center gap-4">
      <span>{{ error }}</span>
      <button
        type="button"
        class="text-blue-600 text-xs font-semibold underline hover:text-blue-800"
        @click="emit('retry')"
      >
        Reintentar
      </button>
    </div>
    <div v-else-if="!filteredCourses.length" class="text-gray-500 text-sm">No se encontraron cursos.</div>

    <ul class="space-y-3" v-else>
      <li v-for="course in filteredCourses" :key="course.courseCode">
        <button
          type="button"
          class="w-full text-left rounded-2xl border border-gray-200 px-4 py-3 hover:border-blue-400 hover:bg-blue-50 transition-colors"
          @click="handleSelect(course)"
        >
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-gray-500 uppercase tracking-wide">{{ course.courseCode }}</p>
              <p class="text-lg font-semibold text-gray-900">{{ course.courseName }}</p>
            </div>
            <div class="text-right text-sm text-gray-500">
              <p>Grupo {{ course.groupLetter }}</p>
              <p v-if="course.creditNumber != null">{{ course.creditNumber }} créditos</p>
            </div>
          </div>
        </button>
      </li>
    </ul>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ProfessorCourseSummary } from '@/services/gradeService'

const props = withDefaults(defineProps<{
  courses: ProfessorCourseSummary[]
  loading: boolean
  error?: string
  heading?: string
  subheading?: string
  description?: string
}>(), {
  heading: 'Mis cursos',
  subheading: 'Selecciona un curso',
  description: 'El panel mostrará estadísticas de calificaciones para el curso elegido.'
})

const heading = computed(() => props.heading)
const subheading = computed(() => props.subheading)
const description = computed(() => props.description)

const emit = defineEmits<{
  (e: 'select', course: ProfessorCourseSummary): void
  (e: 'retry'): void
}>()

const query = ref('')

const filteredCourses = computed(() => {
  const normalizedQuery = query.value.trim().toLowerCase()
  if (!normalizedQuery) {
    return props.courses
  }
  return props.courses.filter(course => {
    return (
      course.courseName.toLowerCase().includes(normalizedQuery) ||
      course.courseCode.toLowerCase().includes(normalizedQuery)
    )
  })
})

const handleSelect = (course: ProfessorCourseSummary) => {
  emit('select', course)
}
</script>

<template>
  <section class="rounded-2xl border border-gray-100 bg-white p-4 space-y-4">
    <header class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <div>
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Listado de estudiantes</p>
        <p class="text-xs text-gray-500">Cada estudiante tiene una única calificación final.</p>
      </div>
      <input
        v-model="query"
        type="search"
        placeholder="Buscar por nombre o CUI"
        class="w-full md:w-64 rounded-xl border-gray-200 focus:border-blue-500 focus:ring-blue-200"
      >
    </header>

    <p v-if="loading" class="text-sm text-blue-600">Cargando estudiantes...</p>
    <p v-else-if="error" class="text-sm text-red-500">{{ error }}</p>
    <p v-else-if="!filteredStudents.length" class="text-sm text-gray-500">No hay estudiantes para mostrar.</p>

    <div v-else class="max-h-[480px] overflow-y-auto border border-gray-100 rounded-xl">
      <table class="min-w-full divide-y divide-gray-100 text-sm">
        <thead class="bg-gray-50 text-gray-500">
          <tr>
            <th class="px-3 py-2 text-left font-medium">Estudiante</th>
            <th class="px-3 py-2 text-left font-medium">Estado</th>
            <th class="px-3 py-2 text-right font-medium">Nota final</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr
            v-for="student in filteredStudents"
            :key="rowKey(student)"
            :class="rowClass(student)"
            @click="select(student)"
          >
            <td class="px-3 py-2">
              <p class="font-semibold text-gray-900">{{ student.fullName }}</p>
              <p class="text-xs text-gray-500">{{ student.studentCui || student.studentUserId }}</p>
            </td>
            <td class="px-3 py-2">
              <span :class="statusClass(student.submissionStatus)">{{ formatStatus(student.submissionStatus) }}</span>
            </td>
            <td class="px-3 py-2 text-right text-gray-900 font-semibold">
              {{ formatGrade(student.finalGrade) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CourseRosterEntry } from '@/services/gradeService'

const props = defineProps<{
  students: CourseRosterEntry[]
  loading: boolean
  error: string
  selectedKey: string | null
}>()

const emit = defineEmits<{ (e: 'select', student: CourseRosterEntry): void }>()

const query = ref('')

const normalizedQuery = computed(() => query.value.trim().toLowerCase())

const filteredStudents = computed(() => {
  if (!normalizedQuery.value) {
    return props.students
  }
  return props.students.filter(student => {
    return (
      student.fullName.toLowerCase().includes(normalizedQuery.value) ||
      student.studentUserId.toString().toLowerCase().includes(normalizedQuery.value) ||
      (student.studentCui ?? '').toLowerCase().includes(normalizedQuery.value)
    )
  })
})

const rowKey = (student: CourseRosterEntry) => `${student.groupId}-${student.studentUserId}`

const select = (student: CourseRosterEntry) => {
  emit('select', student)
}

const rowClass = (student: CourseRosterEntry) => {
  const key = rowKey(student)
  const isSelected = props.selectedKey === key
  return [
    'cursor-pointer transition bg-white hover:bg-blue-50',
    isSelected ? 'bg-blue-100 hover:bg-blue-100' : ''
  ]
}

const statusClass = (status: string) => {
  const normalized = status?.toUpperCase() ?? 'PENDING'
  if (normalized === 'SUBMITTED') {
    return 'text-xs font-semibold text-green-600'
  }
  if (normalized === 'DRAFT') {
    return 'text-xs font-semibold text-yellow-600'
  }
  return 'text-xs font-semibold text-gray-500'
}

const formatStatus = (status: string) => {
  if (!status) {
    return 'Pendiente'
  }
  switch (status.toUpperCase()) {
    case 'SUBMITTED':
      return 'Enviado'
    case 'DRAFT':
      return 'Borrador'
    default:
      return 'Pendiente'
  }
}

const formatGrade = (grade: number | null | undefined) => {
  if (grade == null || Number.isNaN(grade)) {
    return '--'
  }
  return Math.ceil(Number(grade)).toString()
}
</script>

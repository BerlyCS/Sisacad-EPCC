<template>
  <section class="rounded-2xl border border-gray-100 bg-white p-4 space-y-3">
    <header class="flex items-center justify-between">
      <div>
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Grupos</p>
        <p class="text-xs text-gray-500">Selecciona uno o varios grupos para ver su listado.</p>
      </div>
      <span class="text-xs text-gray-400" v-if="selectedIds.length">{{ selectedIds.length }} seleccionados</span>
    </header>

    <p v-if="loading" class="text-sm text-blue-600">Cargando grupos...</p>
    <p v-else-if="error" class="text-sm text-red-500">{{ error }}</p>
    <p v-else-if="!groups.length" class="text-sm text-gray-500">Sin grupos disponibles.</p>

    <div v-else class="flex flex-wrap gap-3">
      <button
        v-for="group in groups"
        :key="group.groupId"
        type="button"
        :class="groupButtonClass(group.groupId)"
        @click="toggleGroup(group.groupId)"
      >
        <div class="flex items-center justify-between w-full">
          <div>
            <p class="text-sm font-semibold text-gray-900">Grupo {{ group.groupLetter }}</p>
            <p class="text-xs text-gray-500">{{ group.studentCount }}/{{ group.maxCapacity }} estudiantes</p>
            <p class="text-[11px] text-gray-400 mt-0.5">{{ resolveCourseTypeLabel(group.courseType) }}</p>
          </div>
          <span
            v-if="!group.canGrade"
            class="text-[10px] uppercase tracking-wide bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded-full"
          >Solo lectura</span>
        </div>
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { CourseGroupSummary } from '@/services/gradeService'

const props = defineProps<{
  groups: CourseGroupSummary[]
  selectedIds: number[]
  loading: boolean
  error: string
}>()

const emit = defineEmits<{ (e: 'update:selected-ids', value: number[]): void }>()

const toggleGroup = (groupId: number) => {
  if (!groupId) {
    return
  }
  const alreadySelected = props.selectedIds.includes(groupId)
  let nextSelection: number[]
  if (alreadySelected) {
    nextSelection = props.selectedIds.filter(id => id !== groupId)
    if (!nextSelection.length && props.groups.length) {
      const fallback = props.groups.find(group => group.canGrade) ?? props.groups[0]
      nextSelection = fallback ? [fallback.groupId] : []
    }
  } else {
    nextSelection = [...props.selectedIds, groupId]
  }
  emit('update:selected-ids', nextSelection)
}

const groupButtonClass = (groupId: number) => {
  const isActive = props.selectedIds.includes(groupId)
  return [
    'w-full rounded-2xl border px-4 py-3 text-left transition-colors',
    isActive
      ? 'border-blue-500 bg-blue-50 shadow-sm'
      : 'border-gray-200 hover:border-blue-300 hover:bg-gray-50'
  ]
}

const COURSE_TYPE_LABEL: Record<string, string> = {
  THEORY: 'Teoría',
  PRACTICE: 'Práctica',
  LAB: 'Laboratorio'
}

const resolveCourseTypeLabel = (type?: string) => {
  if (!type) {
    return COURSE_TYPE_LABEL.THEORY
  }
  return COURSE_TYPE_LABEL[type.toUpperCase()] ?? COURSE_TYPE_LABEL.THEORY
}
</script>

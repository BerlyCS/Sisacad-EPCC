<template>
  <section class="rounded-2xl border border-gray-100 bg-white p-5 space-y-5 h-full flex flex-col">
    <header class="space-y-1">
      <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Edición Masiva</p>
      <div class="flex items-center justify-between gap-2">
        <h2 class="text-2xl font-bold text-gray-900">Registro de Notas</h2>
        <span class="text-xs font-semibold text-gray-500">{{ students.length }} estudiantes</span>
      </div>
      <p class="text-xs text-gray-500">
        Edita las notas de múltiples estudiantes y guarda los cambios en bloque.
      </p>
    </header>

    <div class="flex-1 overflow-auto border border-gray-100 rounded-xl">
      <table class="min-w-full divide-y divide-gray-100 text-sm">
        <thead class="bg-gray-50 text-gray-500 sticky top-0 z-10">
          <tr>
            <th class="px-3 py-2 text-left font-medium bg-gray-50">Estudiante</th>
            <th v-for="(weight, i) in rubric?.continuousWeights" :key="`h-cont-${i}`" class="px-2 py-2 text-center font-medium bg-gray-50 w-20">
              C{{ i + 1 }}<br><span class="text-xs text-gray-400">{{ weight }}%</span>
            </th>
            <th v-for="(weight, i) in rubric?.examWeights" :key="`h-exam-${i}`" class="px-2 py-2 text-center font-medium bg-gray-50 w-20">
              E{{ i + 1 }}<br><span class="text-xs text-gray-400">{{ weight }}%</span>
            </th>
            <th class="px-3 py-2 text-right font-medium bg-gray-50">Final</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-100">
          <tr v-for="student in localStudents" :key="student.studentUserId" class="hover:bg-gray-50">
            <td class="px-3 py-2">
              <p class="font-semibold text-gray-900 truncate max-w-[150px]" :title="student.fullName">{{ student.fullName }}</p>
              <p class="text-xs text-gray-500">{{ student.studentCui }}</p>
            </td>
            <!-- Continuous Grades -->
            <td v-for="(weight, i) in rubric?.continuousWeights" :key="`cont-${student.studentUserId}-${i}`" class="px-1 py-2 text-center">
              <input
                type="number"
                min="0"
                max="20"
                step="0.1"
                v-model.number="student.continuousGrades[i]"
                class="w-16 rounded-lg border-gray-200 text-center text-sm focus:border-blue-500 focus:ring-blue-200 p-1"
                :disabled="readOnly"
              >
            </td>
            <!-- Exam Grades -->
            <td v-for="(weight, i) in rubric?.examWeights" :key="`exam-${student.studentUserId}-${i}`" class="px-1 py-2 text-center">
              <input
                type="number"
                min="0"
                max="20"
                step="0.1"
                v-model.number="student.examGrades[i]"
                class="w-16 rounded-lg border-gray-200 text-center text-sm focus:border-blue-500 focus:ring-blue-200 p-1"
                :disabled="readOnly"
              >
            </td>
            <td class="px-3 py-2 text-right font-bold text-gray-900">
              {{ calculateFinal(student) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="flex justify-end gap-3 pt-2 border-t border-gray-100">
      <button
        type="button"
        class="rounded-xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
        :disabled="readOnly || saving"
        @click="handleSave('DRAFT')"
      >
        Guardar Borrador
      </button>
      <button
        type="button"
        class="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:opacity-50"
        :disabled="readOnly || saving"
        @click="handleSave('SUBMITTED')"
      >
        {{ saving ? 'Guardando...' : 'Publicar Notas' }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import type { CourseRosterEntry, GradingRubric } from '@/services/gradeService'

const props = defineProps<{
  students: CourseRosterEntry[]
  rubric: GradingRubric | null
  readOnly: boolean
  saving: boolean
}>()

const emit = defineEmits<{
  (e: 'save', payload: { students: any[], status: 'SUBMITTED' | 'DRAFT' }): void
}>()

// Local copy for editing
const localStudents = ref<any[]>([])

watch(() => props.students, (newStudents) => {
  localStudents.value = newStudents.map(s => ({
    ...s,
    continuousGrades: [...(s.continuousGrades || [])],
    examGrades: [...(s.examGrades || [])]
  }))
}, { immediate: true })

const calculateFinal = (student: any) => {
  if (!props.rubric) return '--'
  
  const weightedAvg = (grades: number[], weights: number[]) => {
    if (!grades?.length || !weights?.length) return 0
    const totalWeight = weights.reduce((a, b) => a + b, 0)
    if (totalWeight === 0) return 0
    
    let sum = 0
    for (let i = 0; i < weights.length; i++) {
      sum += (grades[i] || 0) * (weights[i] / totalWeight)
    }
    return sum
  }

  const contAvg = weightedAvg(student.continuousGrades, props.rubric.continuousWeights)
  const examAvg = weightedAvg(student.examGrades, props.rubric.examWeights)
  
  const contWeightSum = props.rubric.continuousWeights.reduce((a, b) => a + b, 0)
  const examWeightSum = props.rubric.examWeights.reduce((a, b) => a + b, 0)
  
  const final = (contAvg * (contWeightSum / 100)) + (examAvg * (examWeightSum / 100))
  
  // Custom rounding rule: >= .45 rounds up
  const integer = Math.floor(final)
  const fractional = final - integer
  const rounded = fractional >= 0.45 ? integer + 1 : integer
  
  return rounded
}

const handleSave = (status: 'SUBMITTED' | 'DRAFT') => {
  const payload = localStudents.value.map(s => ({
    studentId: s.studentUserId,
    continuousGrades: s.continuousGrades,
    examGrades: s.examGrades,
    status
  }))
  emit('save', { students: payload, status })
}
</script>

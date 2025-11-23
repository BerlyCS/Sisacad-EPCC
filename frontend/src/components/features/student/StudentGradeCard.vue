<template>
  <article class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 flex flex-col gap-4 hover:border-blue-200 transition-colors">
    <header class="flex items-start justify-between gap-4">
      <div>
        <p class="text-xs font-medium text-blue-500 uppercase tracking-wide">{{ grade.courseCode }}</p>
        <h3 class="text-xl font-semibold text-gray-900">{{ grade.courseName }}</h3>
      </div>
      <div class="text-right">
        <p class="text-xs text-gray-500 uppercase">Promedio final</p>
        <p :class="['text-3xl font-bold', finalGradeClass]">{{ finalGradeLabel }}</p>
      </div>
    </header>

    <section>
      <div class="flex items-center justify-between text-sm text-gray-500 mb-2">
        <span>Evaluaciones continuas</span>
        <span>{{ continuousWeightLabel }}</span>
      </div>
      <div v-if="continuousBreakdown.length" class="space-y-2">
        <div
          v-for="item in continuousBreakdown"
          :key="item.label"
          class="flex items-center justify-between text-sm text-gray-700"
        >
          <span class="font-medium">{{ item.label }}</span>
          <span>
            <span class="font-semibold text-gray-900">{{ formatGrade(item.value) }}</span>
            <span class="text-gray-400"> · {{ item.weightLabel }}</span>
          </span>
        </div>
      </div>
      <p v-else class="text-sm text-gray-400">Sin registros continuos.</p>
    </section>

    <section>
      <div class="flex items-center justify-between text-sm text-gray-500 mb-2">
        <span>Exámenes</span>
        <span>{{ examWeightLabel }}</span>
      </div>
      <div v-if="examBreakdown.length" class="space-y-2">
        <div
          v-for="item in examBreakdown"
          :key="item.label"
          class="flex items-center justify-between text-sm text-gray-700"
        >
          <span class="font-medium">{{ item.label }}</span>
          <span>
            <span class="font-semibold text-gray-900">{{ formatGrade(item.value) }}</span>
            <span class="text-gray-400"> · {{ item.weightLabel }}</span>
          </span>
        </div>
      </div>
      <p v-else class="text-sm text-gray-400">Sin exámenes registrados.</p>
    </section>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StudentGrade } from '@/services/gradeService'

const props = defineProps<{ grade: StudentGrade }>()

const percentageLabel = (weight: number | null): string => {
  if (weight == null) {
    return 'sin peso'
  }
  return `${Math.round(weight)}%`
}

const buildBreakdown = (values: number[], weights: number[], labelPrefix: string) => {
  return values.map((value, index) => ({
    label: `${labelPrefix} ${index + 1}`,
    value,
    weightLabel: percentageLabel(weights[index] ?? null)
  }))
}

const formatGrade = (value?: number): string => {
  if (value == null || Number.isNaN(value)) {
    return '--'
  }
  return value.toFixed(1)
}

const finalGradeLabel = computed(() => {
  return props.grade.finalGrade != null ? props.grade.finalGrade.toFixed(2) : '--'
})

const finalGradeClass = computed(() => {
  if (props.grade.finalGrade == null) {
    return 'text-gray-400'
  }
  return props.grade.finalGrade >= 10.5 ? 'text-green-600' : 'text-red-500'
})

const continuousBreakdown = computed(() => buildBreakdown(props.grade.continuousGrades, props.grade.continuousWeights, 'Continuo'))
const examBreakdown = computed(() => buildBreakdown(props.grade.examGrades, props.grade.examWeights, 'Examen'))

const totalWeight = (weights: number[]) => {
  return weights.reduce((acc, value) => acc + (Number.isFinite(value) ? value : 0), 0)
}

const continuousWeightLabel = computed(() => `${Math.round(totalWeight(props.grade.continuousWeights))}% del curso`)
const examWeightLabel = computed(() => `${Math.round(totalWeight(props.grade.examWeights))}% del curso`)
</script>

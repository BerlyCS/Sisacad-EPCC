<template>
  <section class="rounded-2xl border border-gray-100 bg-white p-5 space-y-5 h-full">
    <header class="space-y-1">
      <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Panel de calificación</p>
      <div class="flex items-center justify-between gap-2">
        <h2 class="text-2xl font-bold text-gray-900">{{ student?.fullName || 'Selecciona un estudiante' }}</h2>
        <span v-if="student" class="text-xs font-semibold text-gray-500">{{ student.studentDocumentoIdentidad }}</span>
      </div>
      <p class="text-sm text-gray-500" v-if="student">
        Grupo {{ student.groupLetter }} · {{ student.courseType === 'LAB' ? 'Laboratorio' : 'Teoría' }}
      </p>
    </header>

    <div v-if="!student" class="text-sm text-gray-500">
      Selecciona un estudiante en la lista para revisar y registrar sus notas.
    </div>

    <template v-else>
      <div v-if="readOnly" class="rounded-xl border border-yellow-200 bg-yellow-50 px-3 py-2 text-sm text-yellow-800">
        Este grupo es de solo lectura. Puedes revisar sus notas, pero no registrarlas.
      </div>

      <div v-if="!rubric && !rubricLoading" class="rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
        No se pudo cargar la rúbrica del curso. Intenta recargar el panel.
      </div>

      <div v-else-if="rubricLoading" class="text-sm text-blue-600">Cargando rúbrica...</div>

      <div v-else class="space-y-6">
        <div class="space-y-3">
          <h3 class="text-sm font-semibold text-gray-700 uppercase tracking-wide">Componentes continuos</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <label
              v-for="(weight, index) in continuousWeights"
              :key="`continuous-${index}`"
              class="flex flex-col rounded-xl border border-gray-200 px-3 py-2"
            >
              <span class="text-xs text-gray-500">Peso {{ weight }}%</span>
              <input
                type="number"
                min="0"
                max="20"
                step="0.1"
                :value="continuousInputs[index] ?? ''"
                :disabled="readOnly"
                class="mt-1 rounded-lg border-gray-200 focus:border-blue-500 focus:ring-blue-200"
                @input="onContinuousChange($event, index)"
              >
            </label>
          </div>
        </div>

        <div class="space-y-3">
          <h3 class="text-sm font-semibold text-gray-700 uppercase tracking-wide">Evaluaciones finales</h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <label
              v-for="(weight, index) in examWeights"
              :key="`exam-${index}`"
              class="flex flex-col rounded-xl border border-gray-200 px-3 py-2"
            >
              <span class="text-xs text-gray-500">Peso {{ weight }}%</span>
              <input
                type="number"
                min="0"
                max="20"
                step="0.1"
                :value="examInputs[index] ?? ''"
                :disabled="readOnly"
                class="mt-1 rounded-lg border-gray-200 focus:border-blue-500 focus:ring-blue-200"
                @input="onExamChange($event, index)"
              >
            </label>
          </div>
        </div>

        <div class="rounded-2xl border border-gray-100 bg-gray-50 p-4 flex flex-col gap-2">
          <div class="flex items-center justify-between">
            <p class="text-sm font-semibold text-gray-600">Nota calculada</p>
            <p class="text-3xl font-bold text-gray-900">{{ formattedFinalGrade }}</p>
          </div>
          <p class="text-xs text-gray-500">El cálculo usa los pesos de la rúbrica actual.</p>
          <p class="text-xs text-gray-400" v-if="student.finalGrade != null">
            Nota registrada actualmente: <span class="font-semibold text-gray-600">{{ Number(student.finalGrade).toFixed(2) }}</span>
          </p>
        </div>

        <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <button
            type="button"
            class="text-sm font-semibold text-gray-500 underline"
            :disabled="saving"
            @click="emit('refresh')"
          >
            Recargar datos
          </button>
          <div class="flex flex-col gap-2 sm:flex-row">
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              :disabled="readOnly || saving || !student"
              @click="handleSubmit('DRAFT')"
            >
              Guardar borrador
            </button>
            <button
              type="button"
              class="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="readOnly || saving || !student"
              @click="handleSubmit('SUBMITTED')"
            >
              {{ saving ? 'Registrando...' : 'Registrar nota' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { CourseRosterEntry, GradingRubric } from '@/services/gradeService'

interface SubmitPayload {
  continuousGrades: number[]
  examGrades: number[]
  status: 'SUBMITTED' | 'DRAFT'
}

const props = defineProps<{
  student: CourseRosterEntry | null
  rubric: GradingRubric | null
  readOnly: boolean
  saving: boolean
  error: string
  rubricLoading: boolean
}>()

const emit = defineEmits<{
  (e: 'submit', payload: SubmitPayload): void
  (e: 'refresh'): void
}>()

const continuousInputs = ref<number[]>([])
const examInputs = ref<number[]>([])
const continuousWeights = computed(() => props.rubric?.continuousWeights ?? [])
const examWeights = computed(() => props.rubric?.examWeights ?? [])

const syncInputs = () => {
  if (!props.student || !props.rubric) {
    continuousInputs.value = []
    examInputs.value = []
    return
  }
  continuousInputs.value = buildInputs(props.student.continuousGrades, continuousWeights.value.length)
  examInputs.value = buildInputs(props.student.examGrades, examWeights.value.length)
}

watch([() => props.student, () => props.rubric], syncInputs, { immediate: true })

const buildInputs = (source: number[] | undefined, expectedLength: number) => {
  const base = Array.isArray(source) ? [...source] : []
  const normalized: number[] = []
  for (let i = 0; i < expectedLength; i += 1) {
    const value = base[i]
    normalized.push(typeof value === 'number' ? value : 0)
  }
  return normalized
}

const onContinuousChange = (event: Event, index: number) => {
  const value = parseInputValue(event)
  continuousInputs.value[index] = value
}

const onExamChange = (event: Event, index: number) => {
  const value = parseInputValue(event)
  examInputs.value[index] = value
}

const parseInputValue = (event: Event) => {
  const target = event.target as HTMLInputElement | null
  if (!target) {
    return 0
  }
  const parsed = Number(target.value)
  if (Number.isNaN(parsed)) {
    return 0
  }
  return Math.min(Math.max(parsed, 0), 20)
}

const calculatedFinalGrade = computed(() => {
  if (!props.rubric) {
    return null
  }
  const continuous = weightedAverage(continuousInputs.value, continuousWeights.value)
  const exams = weightedAverage(examInputs.value, examWeights.value)
  const total = continuous + exams
  if (!Number.isFinite(total)) {
    return null
  }
  return Number(total.toFixed(2))
})

const weightedAverage = (values: number[], weights: number[]) => {
  if (!values.length || !weights.length) {
    return 0
  }
  const weightSum = weights.reduce((acc, weight) => acc + weight, 0)
  if (!weightSum) {
    return 0
  }
  let total = 0
  for (let i = 0; i < weights.length; i += 1) {
    const weight = weights[i] ?? 0
    total += (values[i] ?? 0) * (weight / weightSum)
  }
  return total
}

const formattedFinalGrade = computed(() => {
  if (calculatedFinalGrade.value == null) {
    return '--'
  }
  return calculatedFinalGrade.value.toFixed(2)
})

const handleSubmit = (status: 'SUBMITTED' | 'DRAFT') => {
  if (!props.student || !props.rubric || props.readOnly) {
    return
  }
  emit('submit', {
    status,
    continuousGrades: [...continuousInputs.value],
    examGrades: [...examInputs.value]
  })
}
</script>

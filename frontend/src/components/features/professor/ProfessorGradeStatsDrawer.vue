<template>
  <transition name="fade">
    <div v-if="open" class="fixed inset-0 z-50">
      <div class="absolute inset-0 bg-black/50" @click="$emit('close')"></div>
      <div class="absolute inset-0 flex items-center justify-center p-4">
        <aside class="w-full max-w-2xl max-h-[90vh] bg-white rounded-3xl shadow-2xl p-6 overflow-y-auto border border-white/40 backdrop-blur">
          <header class="flex items-start justify-between mb-6">
            <div>
              <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Resumen del curso</p>
              <h2 class="text-2xl font-bold text-gray-900 truncate">{{ course?.courseName ?? 'Selecciona un curso' }}</h2>
              <p class="text-gray-500">{{ course ? `Codigo: ${course.courseCode}` : 'Sin código' }}</p>
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
            <div class="grid gap-4 md:grid-cols-2">
              <section class="rounded-2xl border border-gray-200 p-4 bg-white">
                <div class="flex items-center justify-between mb-3">
                  <p class="text-sm font-semibold text-gray-600">Comparativa de medias</p>
                  <span class="text-xs text-gray-400">Escala 0-20</span>
                </div>
                <div class="h-48">
                  <Bar :data="gradeSummaryData" :options="gradeSummaryOptions" class="h-full" />
                </div>
              </section>

              <section class="rounded-2xl border border-gray-200 p-4 bg-white">
                <div class="flex items-center justify-between mb-3">
                  <p class="text-sm font-semibold text-gray-600">Estado de calificación</p>
                  <span class="text-xs text-gray-400">
                    {{ approvedStudentsCount }} aprobados · {{ failedStudentsCount }} desaprobados · {{ pendingStudents }} sin nota
                  </span>
                </div>
                <div class="h-48">
                  <Pie :data="approvalStatusData" :options="approvalStatusOptions" class="h-full" />
                </div>
                <p class="text-xs text-gray-500 mt-2">
                  {{ gradeRatioText }}
                </p>
              </section>
            </div>
          <div class="mt-6"></div>
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

            <div class="rounded-2xl border border-gray-100 bg-gray-50 p-5 space-y-1">
              <p class="text-sm text-gray-600">
                Estudiantes calificados: <span class="font-semibold text-gray-900">{{ gradedStudents }}</span>
              </p>
              <p class="text-xs text-gray-500">Incluye calificaciones continuas y exámenes.</p>
              <p v-if="totalStudents > 0" class="text-xs text-gray-500">
                Total en el curso: <span class="font-semibold text-gray-900">{{ totalStudents }}</span>
              </p>
            </div>

            <div class="grid gap-4 md:grid-cols-2">
              <section class="rounded-2xl border border-gray-200 p-4 bg-white">
                <p class="text-sm font-semibold text-gray-600">Notas de examen</p>
                <div class="mt-3 space-y-2 text-sm text-gray-600">
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Promedio</span>
                    <span class="font-medium text-gray-900">{{ formatMetric(examGradeStats.mean) }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Nota más alta</span>
                    <span class="font-medium text-green-600">{{ formatMetric(examGradeStats.highest) }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Nota más baja</span>
                    <span class="font-medium text-red-500">{{ formatMetric(examGradeStats.lowest) }}</span>
                  </div>
                </div>
              </section>

              <section class="rounded-2xl border border-gray-200 p-4 bg-white">
                <p class="text-sm font-semibold text-gray-600">Notas continuas</p>
                <div class="mt-3 space-y-2 text-sm text-gray-600">
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Promedio</span>
                    <span class="font-medium text-gray-900">{{ formatMetric(continuousGradeStats.mean) }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Nota más alta</span>
                    <span class="font-medium text-green-600">{{ formatMetric(continuousGradeStats.highest) }}</span>
                  </div>
                  <div class="flex items-center justify-between">
                    <span class="text-gray-500">Nota más baja</span>
                    <span class="font-medium text-red-500">{{ formatMetric(continuousGradeStats.lowest) }}</span>
                  </div>
                </div>
              </section>
            </div>

            <div class="rounded-2xl border border-gray-200 bg-white/80 p-4">
              <div class="flex items-center justify-between">
                <p class="text-sm font-semibold text-gray-600">Notas por estudiante</p>
                <span class="text-xs text-gray-400">Orden descendente</span>
              </div>
              <div class="mt-3 max-h-60 divide-y divide-gray-100 overflow-y-auto">
                <div
                  v-for="student in studentGradeBreakdown"
                  :key="student.studentUserId"
                  class="flex items-center justify-between py-2 text-sm text-gray-700"
                >
                  <span class="font-medium text-gray-800 truncate">{{ student.fullName }}</span>
                  <span class="text-gray-600">{{ student.finalGrade == null ? 'Pendiente' : formatMetric(student.finalGrade) }}</span>
                </div>
                <p v-if="studentGradeBreakdown.length === 0" class="py-2 text-xs text-gray-500">
                  Ningún estudiante tiene nota registrada.
                </p>
              </div>
            </div>

          </div>
          <div v-else class="text-gray-500 text-sm">
            Selecciona un curso para ver sus estadísticas.
          </div>
        </aside>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bar, Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js'
import type { CourseGradeStats, CourseRosterEntry, ProfessorCourseSummary } from '@/services/gradeService'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

const props = defineProps<{
  open: boolean
  course: ProfessorCourseSummary | null
  stats: CourseGradeStats | null
  loading: boolean
  error: string
  students: CourseRosterEntry[]
}>()

defineEmits<{ (e: 'close'): void }>()

const PASSING_GRADE_THRESHOLD = 11

const gradeSummaryData = computed(() => ({
  labels: ['Promedio final', 'Nota más alta', 'Nota más baja'],
  datasets: [
    {
      label: 'Notas',
      data: [
        props.stats?.meanFinalGrade ?? 0,
        props.stats?.highestFinalGrade ?? 0,
        props.stats?.lowestFinalGrade ?? 0
      ],
      backgroundColor: ['#2563eb', '#16a34a', '#dc2626'],
      borderRadius: 8
    }
  ]
}))

const gradeSummaryOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    y: {
      beginAtZero: true,
      max: 20,
      ticks: {
        stepSize: 2
      }
    }
  },
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: (context: any) => `${context.parsed?.y ?? context.parsed ?? 0} pts`
      }
    }
  }
}

const totalStudents = computed(() => props.students?.length ?? 0)
const approvedStudentsCount = computed(() =>
  props.students.filter(student => typeof student.finalGrade === 'number' && student.finalGrade >= PASSING_GRADE_THRESHOLD).length
)
const failedStudentsCount = computed(() =>
  props.students.filter(student => typeof student.finalGrade === 'number' && student.finalGrade < PASSING_GRADE_THRESHOLD).length
)
const gradedStudents = computed(() => approvedStudentsCount.value + failedStudentsCount.value)
const pendingStudents = computed(() => Math.max(totalStudents.value - gradedStudents.value, 0))

const approvalStatusData = computed(() => ({
  labels: ['Aprobados', 'Desaprobados'],
  datasets: [
    {
      data: [approvedStudentsCount.value, failedStudentsCount.value],
      backgroundColor: ['#22c55e', '#f97316'],
      borderWidth: 0
    }
  ]
}))

const approvalStatusOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom'
    },
    tooltip: {
      callbacks: {
        label: (context: any) => `${context.label}: ${context.parsed ?? 0} estudiantes`
      }
    }
  }
}

type GradeComponentStats = {
  mean: number | null
  highest: number | null
  lowest: number | null
}

const collectGradeSeries = (selector: (student: CourseRosterEntry) => number[]): number[] => {
  const grades: number[] = []
  for (const student of props.students) {
    for (const value of selector(student)) {
      if (typeof value === 'number' && !Number.isNaN(value)) {
        grades.push(value)
      }
    }
  }
  return grades
}

const deriveStats = (grades: number[]): GradeComponentStats => {
  const valid = grades.filter(value => typeof value === 'number' && !Number.isNaN(value))
  if (!valid.length) {
    return { mean: null, highest: null, lowest: null }
  }
  const sum = valid.reduce((acc, value) => acc + value, 0)
  return {
    mean: sum / valid.length,
    highest: Math.max(...valid),
    lowest: Math.min(...valid)
  }
}

const continuousGradeStats = computed(() => deriveStats(collectGradeSeries(student => student.continuousGrades)))
const examGradeStats = computed(() => deriveStats(collectGradeSeries(student => student.examGrades)))

const studentGradeBreakdown = computed(() => {
  return [...props.students]
    .sort((a, b) => {
      const gradeA = a.finalGrade ?? -1
      const gradeB = b.finalGrade ?? -1
      if (gradeB !== gradeA) {
        return gradeB - gradeA
      }
      return a.fullName.localeCompare(b.fullName)
    })
})

const gradeRatioText = computed(() => {
  if (totalStudents.value === 0) {
    return 'Aún no se han registrado estudiantes para este curso.'
  }
  const ratio = ((gradedStudents.value / totalStudents.value) * 100)
  return `${gradedStudents.value} de ${totalStudents.value} estudiantes (${ratio.toFixed(1)}%) tienen calificaciones.`
})

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

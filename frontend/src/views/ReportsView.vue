<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <h1 class="text-3xl font-bold text-gray-900">Reportes de Asistencia</h1>
        <p class="text-gray-600">Visualiza estadísticas y exporta reportes de tus cursos.</p>
      </header>

      <div class="bg-white p-6 rounded-lg shadow">
        <label class="block text-sm font-medium text-gray-700 mb-2">Selecciona un Curso</label>
        <select v-model="selectedCourseId" @change="loadStats" class="block w-full md:w-1/3 rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
          <option :value="null" disabled>Selecciona un curso...</option>
          <option v-for="course in courses" :key="course.courseId" :value="course.courseId">
            {{ course.courseName }}
          </option>
        </select>
      </div>

      <div v-if="selectedCourseId && stats" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Key Metrics -->
        <div class="bg-white p-6 rounded-lg shadow space-y-4">
          <h3 class="text-lg font-medium text-gray-900">Métricas Clave</h3>
          <div class="grid grid-cols-2 gap-4">
            <div class="p-4 bg-blue-50 rounded-lg">
              <p class="text-sm text-blue-600 font-medium">Asistencia Promedio</p>
              <p class="text-2xl font-bold text-blue-900">{{ stats.attendancePercentage.toFixed(1) }}%</p>
            </div>
            <div class="p-4 bg-green-50 rounded-lg">
              <p class="text-sm text-green-600 font-medium">Avance de Sílabo</p>
              <p class="text-2xl font-bold text-green-900">{{ stats.syllabusProgress.toFixed(1) }}%</p>
            </div>
            <div class="p-4 bg-gray-50 rounded-lg">
              <p class="text-sm text-gray-600 font-medium">Sesiones Totales</p>
              <p class="text-2xl font-bold text-gray-900">{{ stats.totalSessions }}</p>
            </div>
            <div class="p-4 bg-purple-50 rounded-lg">
              <p class="text-sm text-purple-600 font-medium">Total Asistencias</p>
              <p class="text-2xl font-bold text-purple-900">{{ stats.presentStudents }}</p>
            </div>
          </div>
        </div>

        <!-- Charts -->
        <div class="bg-white p-6 rounded-lg shadow">
          <h3 class="text-lg font-medium text-gray-900 mb-4">Distribución de Asistencia</h3>
          <div class="h-64">
            <Bar v-if="chartData" :data="chartData" :options="chartOptions" />
          </div>
        </div>
      </div>

      <div v-if="selectedCourseId" class="flex justify-end space-x-4">
        <button @click="downloadExcel" class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition flex items-center gap-2">
          <span>Descargar Excel</span>
        </button>
        <button @click="generatePDF" class="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition flex items-center gap-2">
          <span>Descargar PDF</span>
        </button>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { gradeService, type ProfessorCourseSummary } from '@/services/gradeService'
import { reportService, type AttendanceStatsDTO } from '@/services/reportService'
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'
import jsPDF from 'jspdf'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

const courses = ref<ProfessorCourseSummary[]>([])
const selectedCourseId = ref<number | null>(null)
const stats = ref<AttendanceStatsDTO | null>(null)

const loadCourses = async () => {
  try {
    courses.value = await gradeService.fetchProfessorCourseSummaries()
  } catch (error) {
    console.error('Error loading courses', error)
  }
}

const loadStats = async () => {
  if (!selectedCourseId.value) return
  try {
    stats.value = await reportService.getStats(selectedCourseId.value)
  } catch (error) {
    console.error('Error loading stats', error)
  }
}

const chartData = computed(() => {
  if (!stats.value) return null
  return {
    labels: ['Presentes', 'Ausentes'],
    datasets: [
      {
        label: 'Estudiantes',
        backgroundColor: ['#4ade80', '#f87171'],
        data: [stats.value.presentStudents, stats.value.absentStudents]
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false
}

const downloadExcel = async () => {
  if (!selectedCourseId.value) return
  try {
    await reportService.downloadExcel(selectedCourseId.value)
  } catch (error) {
    console.error('Error downloading excel', error)
    alert('Error al descargar el reporte Excel')
  }
}

const generatePDF = () => {
  if (!stats.value || !selectedCourseId.value) return
  const doc = new jsPDF()
  
  const courseName = courses.value.find(c => c.courseId === selectedCourseId.value)?.courseName || 'Curso'
  
  doc.setFontSize(20)
  doc.text(`Reporte de Asistencia: ${courseName}`, 10, 20)
  
  doc.setFontSize(12)
  doc.text(`Fecha de generación: ${new Date().toLocaleDateString()}`, 10, 30)
  
  doc.text(`Asistencia Promedio: ${stats.value.attendancePercentage.toFixed(1)}%`, 10, 50)
  doc.text(`Avance de Sílabo: ${stats.value.syllabusProgress.toFixed(1)}%`, 10, 60)
  doc.text(`Sesiones Totales: ${stats.value.totalSessions}`, 10, 70)
  doc.text(`Total Presentes: ${stats.value.presentStudents}`, 10, 80)
  doc.text(`Total Ausentes: ${stats.value.absentStudents}`, 10, 90)
  
  doc.save(`reporte_asistencia_${selectedCourseId.value}.pdf`)
}

onMounted(() => {
  loadCourses()
})
</script>

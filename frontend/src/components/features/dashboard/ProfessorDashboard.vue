<template>
  <div class="pt-4 border-gray-200">
    <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel del Profesor</h3>
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-5 sm:grid-cols-2 mt-5">
      <PrincipalButton color="blue" to="/classrooms">
        <CalendarIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Reservar Aula</h4>
      </PrincipalButton>
      <PrincipalButton color="purple" to="/professor/grades">
        <ChartBarIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Ver Calificaciones</h4>
      </PrincipalButton>
      <PrincipalButton color="cyan" to="/professor/schedule">
        <CalendarDaysIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Mi Horario</h4>
      </PrincipalButton>
      <PrincipalButton color="green" to="/professor/attendance">
        <ClipboardDocumentCheckIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Registrar Asistencia</h4>
      </PrincipalButton>
      <PrincipalButton color="amber" to="/professor/syllabus">
        <DocumentArrowUpIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Gestionar Sílabos</h4>
      </PrincipalButton>
      <PrincipalButton color="teal" @click="toggleReportPanel">
        <ArrowDownTrayIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Reporte de Notas</h4>
      </PrincipalButton>
    </div>

    <transition name="fade-slide">
      <div v-if="showReportPanel" class="report-panel">
        <div class="panel-header">
          <div>
            <p class="text-sm font-semibold text-slate-600">Generador de reporte</p>
            <p class="text-xs text-slate-500">Descarga un Excel con todos los estudiantes y sus calificaciones.</p>
          </div>
          <button type="button" class="close-button" @click="closeReportPanel">
            <XMarkIcon class="w-5 h-5" aria-hidden="true" />
            <span class="sr-only">Cerrar</span>
          </button>
        </div>

        <div class="mt-4 space-y-3">
          <label class="block text-sm font-medium text-slate-700">Curso</label>
          <select
            class="form-select"
            v-model="selectedCourseId"
            :disabled="loadingCourses || isDownloading"
          >
            <option value="" disabled>Selecciona un curso...</option>
            <option v-for="course in courses" :key="course.courseId" :value="String(course.courseId)">
              {{ formatCourseOption(course) }}
            </option>
          </select>

          <p v-if="loadingCourses" class="text-xs text-slate-500">Cargando cursos disponibles…</p>
          <p v-if="reportError" class="text-xs text-red-600">{{ reportError }}</p>

          <div class="flex gap-3">
            <button
              type="button"
              class="action-button"
              :disabled="!selectedCourseId || isDownloading"
              @click="downloadReport"
            >
              <span v-if="isDownloading">Generando…</span>
              <span v-else>Descargar Excel</span>
            </button>
            <button type="button" class="secondary-button" @click="refreshCourses" :disabled="loadingCourses">
              Actualizar cursos
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { PrincipalButton } from '@/components/ui'
import { gradeService, type ProfessorCourseSummary } from '@/services/gradeService'
import { reportService } from '@/services/reportService'
import {
  ArrowDownTrayIcon,
  CalendarDaysIcon,
  CalendarIcon,
  ChartBarIcon,
  ClipboardDocumentCheckIcon,
  DocumentArrowUpIcon,
  XMarkIcon
} from '@heroicons/vue/24/solid'

const showReportPanel = ref(false)
const loadingCourses = ref(false)
const courses = ref<ProfessorCourseSummary[]>([])
const coursesLoaded = ref(false)
const selectedCourseId = ref('')
const isDownloading = ref(false)
const reportError = ref('')

const toggleReportPanel = async () => {
  const nextState = !showReportPanel.value
  showReportPanel.value = nextState
  reportError.value = ''

  if (nextState && !coursesLoaded.value) {
    await loadCourses()
  }
}

const closeReportPanel = () => {
  showReportPanel.value = false
}

const refreshCourses = async () => {
  reportError.value = ''
  coursesLoaded.value = false
  await loadCourses()
}

const loadCourses = async () => {
  try {
    loadingCourses.value = true
    const data = await gradeService.fetchProfessorCourseSummaries()
    courses.value = data
    coursesLoaded.value = true
    if (!selectedCourseId.value && data.length === 1) {
      selectedCourseId.value = String(data[0].courseId)
    }
  } catch (error) {
    console.error('Error loading professor courses', error)
    reportError.value = 'No se pudieron cargar los cursos.'
  } finally {
    loadingCourses.value = false
  }
}

const downloadReport = async () => {
  const courseId = selectedCourseId.value ? Number(selectedCourseId.value) : null

  if (!courseId) {
    reportError.value = 'Selecciona un curso para generar el reporte.'
    return
  }

  try {
    isDownloading.value = true
    reportError.value = ''
    await reportService.downloadGradeExcel(courseId)
  } catch (error: any) {
    console.error('Error downloading grade report', error)
    reportError.value = error?.message ?? 'No se pudo generar el reporte.'
  } finally {
    isDownloading.value = false
  }
}

const formatCourseOption = (course: ProfessorCourseSummary) => {
  const letter = course.groupLetter ? ` - ${course.groupLetter}` : ''
  return `${course.courseName}${letter}`
}
</script>

<style scoped>
.report-panel {
  margin-top: 1.5rem;
  padding: 1.5rem;
  border-radius: 1rem;
  border: 1px solid rgb(226 232 240);
  background: white;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.08);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.close-button {
  border: none;
  border-radius: 999px;
  background: rgb(241 245 249);
  color: rgb(71 85 105);
  padding: 0.35rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.form-select {
  width: 100%;
  border-radius: 0.5rem;
  border: 1px solid rgb(203 213 225);
  padding: 0.65rem 0.85rem;
}

.action-button {
  background: rgb(5 150 105);
  color: white;
  border: none;
  border-radius: 999px;
  padding: 0.65rem 1.5rem;
  font-weight: 600;
  min-width: 160px;
}

.action-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-button {
  border-radius: 999px;
  padding: 0.65rem 1.25rem;
  border: 1px solid rgb(203 213 225);
  color: rgb(71 85 105);
  background: white;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>

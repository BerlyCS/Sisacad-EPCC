<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Gestión de exámenes</p>
        <h1 class="text-3xl font-bold text-gray-900">Resumenes PDF de exámenes</h1>
        <p class="text-gray-600">
          Selecciona uno de tus cursos teóricos para cargar el PDF con la nota media, la peor y la mejor calificación de cada examen.
          Solo los profesores asignados a grupos teóricos pueden realizar esta operación.
        </p>
      </header>

      <ProfessorCourseList
        :courses="courses"
        :loading="coursesLoading"
        :error="coursesError"
        heading="Cursos disponibles"
        subheading="Elige un curso teórico"
        description="Solo se mostrarán los grupos teóricos que tienen habilitado el registro de exámenes."
        @select="handleSelectCourse"
        @retry="loadCourses"
      />

      <section v-if="selectedCourse" class="space-y-6">
        <div class="rounded-2xl border border-blue-100 bg-blue-50 px-4 py-3 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold text-blue-600 uppercase tracking-wide">Curso activo</p>
            <p class="text-lg font-semibold text-gray-900">{{ selectedCourse.courseName }} ({{ selectedCourse.courseCode }})</p>
            <p class="text-sm text-gray-600">Selecciona el grupo teórico y sube los PDFs con las estadísticas del examen.</p>
          </div>
          <button
            type="button"
            class="rounded-xl border border-gray-200 px-3 py-1.5 text-sm font-semibold text-gray-700"
            @click="handleClearCourse"
          >
            Limpiar selección
          </button>
        </div>

        <article class="rounded-2xl border border-gray-200 bg-white p-5 space-y-5">
          <header class="space-y-1">
            <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">Grupos teóricos</p>
            <h2 class="text-2xl font-bold text-gray-900">Selecciona el grupo asignado</h2>
            <p class="text-sm text-gray-500">Solo se listan los grupos teóricos en los que puedes gestionar calificaciones.</p>
          </header>

          <div v-if="theoryGroupsLoading" class="text-blue-600 text-sm">Cargando grupos teóricos...</div>
          <div v-else-if="theoryGroupsError" class="text-red-500 text-sm">{{ theoryGroupsError }}</div>
          <div v-else-if="!theoryGroups.length" class="rounded-xl border border-dashed border-gray-300 p-4 text-sm text-gray-500">
            No tienes grupos teóricos asignados para este curso. Selecciona otro curso para continuar.
          </div>
          <div v-else class="grid gap-4 md:grid-cols-2">
            <label class="text-sm font-semibold text-gray-700">
              Grupo teórico
              <select
                v-model.number="selectedTheoryGroupId"
                class="mt-1 w-full rounded-xl border-gray-200 focus:border-blue-500 focus:ring-blue-200"
              >
                <option
                  v-for="group in theoryGroups"
                  :key="group.groupId"
                  :value="group.groupId"
                >
                  Grupo {{ group.groupLetter }} · {{ group.studentCount }} estudiantes
                </option>
              </select>
            </label>
            <div class="rounded-xl border border-gray-100 bg-gray-50 px-4 py-3 text-sm text-gray-600">
              <p class="font-semibold text-gray-800">Detalles del grupo</p>
              <p v-if="currentTheoryGroup">
                Capacidad {{ currentTheoryGroup.maxCapacity }} estudiantes · Tipo {{ currentTheoryGroup.courseType }}
              </p>
              <p v-else>
                Selecciona un grupo para ver el detalle.
              </p>
            </div>
          </div>
        </article>

        <div v-if="currentTheoryGroup" class="grid gap-6 lg:grid-cols-[minmax(0,0.5fr)_minmax(0,0.5fr)]">
          <article class="rounded-2xl border border-gray-200 bg-white p-5 space-y-4">
            <header class="space-y-1">
              <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">Historial de PDFs</p>
              <h2 class="text-2xl font-bold text-gray-900">Resúmenes subidos</h2>
            </header>

            <div v-if="examPdfsLoading" class="text-blue-600 text-sm">Cargando archivos...</div>
            <div v-else-if="examPdfsError" class="text-red-500 text-sm">{{ examPdfsError }}</div>
            <div v-else-if="!examPdfs.length" class="rounded-xl border border-dashed border-gray-300 p-4 text-sm text-gray-500">
              Todavía no se cargaron resúmenes de exámenes para este grupo.
            </div>
            <ul v-else class="space-y-3">
              <li
                v-for="summary in examPdfs"
                :key="summary.summaryId"
                class="rounded-xl border border-gray-200 p-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between"
              >
                <div>
                  <p class="text-sm font-semibold text-gray-900">Examen {{ summary.examNumber }} · {{ summaryTypeLabel(summary.summaryType) }}</p>
                  <p class="text-xs text-gray-500">{{ summary.fileName || 'Archivo sin nombre' }} · {{ formatBytes(summary.fileSizeBytes) }}</p>
                </div>
                <div class="flex flex-wrap gap-2">
                  <a
                    :href="buildDownloadUrl(summary)"
                    target="_blank"
                    rel="noopener"
                    class="inline-flex items-center gap-1 rounded-xl border border-blue-200 bg-blue-50 px-3 py-1.5 text-sm font-semibold text-blue-700 hover:bg-blue-100"
                  >
                    <DocumentArrowDownIcon class="h-4 w-4" />
                    Descargar
                  </a>
                  <button
                    type="button"
                    class="inline-flex items-center gap-1 rounded-xl border border-red-200 bg-red-50 px-3 py-1.5 text-sm font-semibold text-red-600 hover:bg-red-100 disabled:opacity-50"
                    :disabled="deleteInProgressId === summary.summaryId"
                    @click="handleDeleteSummary(summary.summaryId)"
                  >
                    <TrashIcon class="h-4 w-4" />
                    {{ deleteInProgressId === summary.summaryId ? 'Eliminando...' : 'Eliminar' }}
                  </button>
                </div>
              </li>
            </ul>
          </article>

          <article class="rounded-2xl border border-gray-200 bg-white p-5 space-y-5">
            <header class="space-y-1">
              <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">Carga de PDF</p>
              <h2 class="text-2xl font-bold text-gray-900">Subir examen</h2>
              <p class="text-sm text-gray-500">Formato PDF · Máx. 20 MB · Se reemplaza el archivo existente para el mismo tipo y examen.</p>
            </header>

            <div class="grid gap-4 md:grid-cols-2">
              <label class="text-sm font-semibold text-gray-700">
                Número de examen
                <select
                  v-model.number="uploadForm.examNumber"
                  class="mt-1 w-full rounded-xl border-gray-200 focus:border-blue-500 focus:ring-blue-200"
                >
                  <option v-for="exam in examNumberOptions" :key="exam" :value="exam">
                    Examen {{ exam }}
                  </option>
                </select>
              </label>
              <label class="text-sm font-semibold text-gray-700">
                Estadística
                <select
                  v-model="uploadForm.summaryType"
                  class="mt-1 w-full rounded-xl border-gray-200 focus:border-blue-500 focus:ring-blue-200"
                >
                  <option value="MEAN">Promedio del examen</option>
                  <option value="BEST">Mejor nota</option>
                  <option value="WORST">Peor nota</option>
                </select>
              </label>
            </div>

            <div class="space-y-3">
              <label class="text-sm font-semibold text-gray-700">
                Archivo PDF
                <input
                  :key="fileInputKey"
                  type="file"
                  accept="application/pdf"
                  class="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:rounded-full file:border-0 file:bg-blue-50 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-blue-700 hover:file:bg-blue-100"
                  @change="handleFileChange"
                >
              </label>
              <p class="text-xs text-gray-500">Incluye solo la información requerida por la secretaría académica.</p>
              <p v-if="fileError" class="text-xs text-red-500">{{ fileError }}</p>
            </div>

            <div class="space-y-2">
              <button
                type="button"
                class="w-full inline-flex items-center justify-center gap-2 rounded-2xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
                :disabled="!canUpload || uploadInProgress"
                @click="handleUploadExamPdf"
              >
                <ArrowUpTrayIcon class="h-5 w-5" />
                {{ uploadInProgress ? 'Subiendo archivo...' : 'Guardar PDF del examen' }}
              </button>
              <p v-if="uploadError" class="text-sm text-red-500">{{ uploadError }}</p>
              <p v-if="uploadSuccess" class="text-sm text-green-600">{{ uploadSuccess }}</p>
            </div>
          </article>
        </div>
      </section>

      <p v-else class="rounded-2xl border border-dashed border-gray-200 bg-white p-6 text-sm text-gray-500">
        Selecciona un curso para comenzar a gestionar los PDF de los exámenes.
      </p>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ArrowUpTrayIcon, DocumentArrowDownIcon, TrashIcon } from '@heroicons/vue/24/outline'
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import ProfessorCourseList from '@/components/features/professor/ProfessorCourseList.vue'
import { useAuthStore } from '@/stores/auth'
import { gradeService, type CourseGroupExamPdf, type CourseGroupSummary, type ProfessorCourseSummary } from '@/services/gradeService'

const authStore = useAuthStore()
const allowedRoles = new Set(['PROFESSOR', 'ADMIN'])

const courses = ref<ProfessorCourseSummary[]>([])
const coursesLoading = ref(false)
const coursesError = ref('')
const selectedCourse = ref<ProfessorCourseSummary | null>(null)

const theoryGroups = ref<CourseGroupSummary[]>([])
const theoryGroupsLoading = ref(false)
const theoryGroupsError = ref('')
const selectedTheoryGroupId = ref<number | null>(null)

const examPdfs = ref<CourseGroupExamPdf[]>([])
const examPdfsLoading = ref(false)
const examPdfsError = ref('')
const deleteInProgressId = ref<number | null>(null)

const uploadForm = ref<{ examNumber: number; summaryType: CourseGroupExamPdf['summaryType']; file: File | null }>({
  examNumber: 1,
  summaryType: 'MEAN',
  file: null
})
const fileInputKey = ref(Date.now())
const fileError = ref('')
const uploadError = ref('')
const uploadSuccess = ref('')
const uploadInProgress = ref(false)

const currentTheoryGroup = computed(() => {
  if (!selectedTheoryGroupId.value) {
    return null
  }
  return theoryGroups.value.find(group => group.groupId === selectedTheoryGroupId.value) ?? null
})

const examNumberOptions = [1, 2, 3]

const canUpload = computed(() => Boolean(uploadForm.value.file) && Boolean(currentTheoryGroup.value))

const summaryTypeLabel = (type: CourseGroupExamPdf['summaryType']) => {
  switch (type) {
    case 'BEST':
      return 'Mejor nota'
    case 'WORST':
      return 'Peor nota'
    default:
      return 'Promedio del examen'
  }
}

const formatBytes = (bytes?: number | null) => {
  if (!bytes || bytes <= 0) {
    return 'Tamaño no disponible'
  }
  const units = ['B', 'KB', 'MB', 'GB']
  let value = bytes
  let unit = 0
  while (value >= 1024 && unit < units.length - 1) {
    value /= 1024
    unit += 1
  }
  return `${value.toFixed(value >= 10 || unit === 0 ? 0 : 1)} ${units[unit]}`
}

const resolveUserRole = (): string => {
  const role = authStore.userRole
  if (typeof role === 'string') {
    return role
  }
  if (role && typeof role === 'object' && 'value' in role && typeof role.value === 'string') {
    return role.value
  }
  return ''
}

const loadCourses = async () => {
  if (!allowedRoles.has(resolveUserRole())) {
    courses.value = []
    coursesError.value = 'Tu rol no tiene acceso a esta vista.'
    return
  }
  coursesLoading.value = true
  coursesError.value = ''
  try {
    courses.value = await gradeService.fetchProfessorCourseSummaries()
  } catch (error) {
    console.error('Error al cargar cursos para PDFs de examen', error)
    coursesError.value = error instanceof Error ? error.message : 'No se pudieron cargar los cursos asignados'
    courses.value = []
  } finally {
    coursesLoading.value = false
  }
}

const loadTheoryGroups = async (courseId: number) => {
  if (!courseId) {
    theoryGroups.value = []
    selectedTheoryGroupId.value = null
    return
  }
  theoryGroupsLoading.value = true
  theoryGroupsError.value = ''
  try {
    const groups = await gradeService.fetchCourseGroups(courseId)
    theoryGroups.value = groups.filter(group => group.courseType === 'THEORY' && group.canGrade)
    selectedTheoryGroupId.value = theoryGroups.value[0]?.groupId ?? null
  } catch (error) {
    console.error('Error al cargar grupos teóricos', error)
    theoryGroupsError.value = error instanceof Error ? error.message : 'No se pudieron cargar los grupos teóricos'
    theoryGroups.value = []
    selectedTheoryGroupId.value = null
  } finally {
    theoryGroupsLoading.value = false
  }
}

const refreshExamPdfs = async (groupId: number | null) => {
  if (!groupId) {
    examPdfs.value = []
    return
  }
  examPdfsLoading.value = true
  examPdfsError.value = ''
  try {
    examPdfs.value = await gradeService.fetchGroupExamPdfs(groupId)
  } catch (error) {
    console.error('Error al cargar PDFs de examen', error)
    examPdfsError.value = error instanceof Error ? error.message : 'No se pudieron cargar los archivos del examen'
    examPdfs.value = []
  } finally {
    examPdfsLoading.value = false
  }
}

const handleSelectCourse = async (course: ProfessorCourseSummary) => {
  selectedCourse.value = course
  uploadForm.value.examNumber = 1
  uploadForm.value.summaryType = 'MEAN'
  uploadForm.value.file = null
  uploadSuccess.value = ''
  uploadError.value = ''
  fileError.value = ''
  fileInputKey.value = Date.now()
  await loadTheoryGroups(course.courseId)
}

const handleClearCourse = () => {
  selectedCourse.value = null
  theoryGroups.value = []
  selectedTheoryGroupId.value = null
  examPdfs.value = []
  uploadSuccess.value = ''
  uploadError.value = ''
  fileError.value = ''
}

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]
  uploadSuccess.value = ''
  uploadError.value = ''
  fileError.value = ''
  if (!file) {
    uploadForm.value.file = null
    return
  }
  if (file.type !== 'application/pdf') {
    fileError.value = 'El archivo debe estar en formato PDF.'
    uploadForm.value.file = null
    return
  }
  const maxBytes = 20 * 1024 * 1024
  if (file.size > maxBytes) {
    fileError.value = 'El archivo supera el límite de 20 MB.'
    uploadForm.value.file = null
    return
  }
  uploadForm.value.file = file
}

const handleUploadExamPdf = async () => {
  if (!currentTheoryGroup.value) {
    uploadError.value = 'Selecciona un grupo teórico válido.'
    return
  }
  if (!uploadForm.value.file) {
    fileError.value = 'Selecciona un archivo PDF antes de continuar.'
    return
  }

  uploadError.value = ''
  uploadSuccess.value = ''
  uploadInProgress.value = true
  try {
    await gradeService.uploadGroupExamPdf(currentTheoryGroup.value.groupId, {
      examNumber: uploadForm.value.examNumber,
      summaryType: uploadForm.value.summaryType,
      file: uploadForm.value.file
    })
    uploadSuccess.value = 'El PDF se guardó correctamente.'
    uploadForm.value.file = null
    fileInputKey.value = Date.now()
    fileError.value = ''
    await refreshExamPdfs(currentTheoryGroup.value.groupId)
  } catch (error) {
    console.error('Error al subir el PDF del examen', error)
    uploadError.value = error instanceof Error ? error.message : 'No se pudo subir el PDF del examen'
  } finally {
    uploadInProgress.value = false
  }
}

const handleDeleteSummary = async (summaryId: number) => {
  if (!currentTheoryGroup.value) {
    return
  }
  const confirmed = window.confirm('¿Seguro que deseas eliminar este PDF?')
  if (!confirmed) {
    return
  }

  deleteInProgressId.value = summaryId
  uploadSuccess.value = ''
  uploadError.value = ''
  try {
    await gradeService.deleteGroupExamPdf(currentTheoryGroup.value.groupId, summaryId)
    await refreshExamPdfs(currentTheoryGroup.value.groupId)
  } catch (error) {
    console.error('Error al eliminar el PDF del examen', error)
    uploadError.value = error instanceof Error ? error.message : 'No se pudo eliminar el PDF'
  } finally {
    deleteInProgressId.value = null
  }
}

const buildDownloadUrl = (summary: CourseGroupExamPdf) => {
  return gradeService.buildExamPdfDownloadUrl(summary.groupId, summary.summaryId)
}

const initialize = async () => {
  if (!authStore.initialized) {
    try {
      await authStore.initializeAuth()
    } catch (error) {
      console.warn('No se pudo inicializar la sesión del profesor para PDFs de examen', error)
    }
  }
  await loadCourses()
}

onMounted(() => {
  void initialize()
})

watch(selectedTheoryGroupId, (groupId) => {
  void refreshExamPdfs(groupId)
})

watch(() => authStore.userRole, () => {
  const role = resolveUserRole()
  if (!allowedRoles.has(role)) {
    handleClearCourse()
    courses.value = []
    coursesError.value = 'Tu rol no tiene acceso a esta vista.'
  }
})
</script>

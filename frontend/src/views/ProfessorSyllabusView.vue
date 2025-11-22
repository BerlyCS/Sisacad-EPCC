<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Gestión de sílabos</p>
        <h1 class="text-3xl font-bold text-gray-900">Planificación de cursos</h1>
        <p class="text-gray-600">Selecciona uno de tus cursos para revisar el sílabo vigente, administrar los temas programados y subir una nueva versión oficial en PDF.</p>
      </header>

      <ProfessorCourseList
        :courses="courses"
        :loading="coursesLoading"
        :error="coursesError"
        heading="Cursos dictados"
        subheading="Selecciona un curso para gestionar su sílabo"
        description="El archivo cargado debe ser PDF y reflejar la programación oficial de temas y sesiones."
        @select="handleSelectCourse"
        @retry="loadCourses"
      />

      <section v-if="selectedCourse" class="space-y-6">
        <div class="rounded-2xl border border-blue-100 bg-blue-50 px-4 py-3 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold text-blue-600 uppercase tracking-wide">Curso activo</p>
            <p class="text-lg font-semibold text-gray-900">{{ selectedCourse.courseName }} ({{ selectedCourse.courseCode }})</p>
            <p class="text-sm text-gray-700">Grupo {{ selectedCourse.groupLetter }} · {{ selectedCourse.courseType }}</p>
          </div>
          <div class="text-sm text-gray-600">
            <p>Última actualización: {{ lastUpdatedLabel }}</p>
            <p>Temas totales registrados: {{ currentSyllabus?.topics.length || 0 }}</p>
          </div>
        </div>

        <div class="grid gap-6 lg:grid-cols-[minmax(0,0.45fr)_minmax(0,0.55fr)]">
          <article class="rounded-2xl border border-gray-200 bg-white p-5 space-y-5">
            <header class="space-y-1">
              <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">Archivo del sílabo</p>
              <h2 class="text-2xl font-bold text-gray-900">Carga y reemplazo del PDF</h2>
              <p class="text-sm text-gray-500">El archivo debe ser PDF (máx. 100&nbsp;MB) y se almacenará con el formato <code>{{ computedFilename }}</code>.</p>
            </header>

            <div class="space-y-4">
              <div v-if="syllabusLoading" class="text-blue-600 text-sm">Cargando información del sílabo...</div>
              <div v-else-if="syllabusError" class="text-sm text-red-500">{{ syllabusError }}</div>
              <div v-else-if="!currentSyllabus" class="rounded-xl border border-dashed border-gray-300 p-4 text-sm text-gray-500">
                Aún no se ha cargado un sílabo para este curso. Sube el PDF oficial y luego podrás gestionar los temas.
              </div>
              <div v-else class="rounded-xl border border-gray-200 bg-gray-50 p-4 space-y-2">
                <p class="text-xs font-semibold text-gray-500 uppercase tracking-wide">Archivo vigente</p>
                <p class="text-lg font-semibold text-gray-900">{{ currentSyllabus.content?.name || 'syllabus.pdf' }}</p>
                <p class="text-xs text-gray-600">{{ formatBytes(currentSyllabus.content?.sizeBytes) }} · Actualizado {{ lastUpdatedLabel }}</p>
              </div>

              <div class="space-y-3">
                <label class="text-sm font-semibold text-gray-700" for="syllabus-file-input">Selecciona el nuevo PDF</label>
                <label
                  for="syllabus-file-input"
                  class="flex flex-col gap-2 rounded-2xl border-2 border-dashed border-gray-300 bg-gray-50 px-4 py-5 text-left hover:border-blue-400 hover:bg-white transition-colors cursor-pointer"
                >
                  <div class="flex items-start gap-3">
                    <ArrowUpTrayIcon class="h-10 w-10 text-blue-500" />
                    <div class="flex-1">
                      <p class="text-sm font-semibold text-gray-900">{{ selectedFileSummary }}</p>
                      <p class="text-xs text-gray-500">{{ selectedFileDetails }}</p>
                    </div>
                    <span class="text-sm font-semibold text-blue-600">Explorar</span>
                  </div>
                </label>
                <input
                  :key="fileInputKey"
                  id="syllabus-file-input"
                  type="file"
                  accept="application/pdf"
                  class="sr-only"
                  @change="handleFileChange"
                >
                <p class="text-xs text-gray-500">Incluye portada con información del curso y firma del coordinador académico.</p>
                <p v-if="fileError" class="text-xs text-red-500">{{ fileError }}</p>
              </div>

              <div class="space-y-3">
                <button
                  type="button"
                  class="w-full rounded-2xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-50"
                  :disabled="!canUploadFile"
                  @click="handleUploadFile"
                >
                  {{ uploadingFile ? 'Subiendo archivo...' : currentSyllabus ? 'Actualizar archivo del sílabo' : 'Subir archivo del sílabo' }}
                </button>
                <div class="flex flex-wrap gap-2">
                  <a
                    class="inline-flex items-center justify-center rounded-xl border border-blue-600 px-3 py-1.5 text-sm font-semibold text-blue-700 hover:bg-blue-50 disabled:opacity-50"
                    :class="{ 'pointer-events-none opacity-50': !downloadUrl }"
                    :href="downloadUrl || undefined"
                    target="_blank"
                    rel="noopener"
                  >
                    Descargar PDF
                  </a>
                  <button
                    type="button"
                    class="inline-flex items-center justify-center rounded-xl border border-red-200 bg-red-50 px-3 py-1.5 text-sm font-semibold text-red-700 hover:bg-red-100 disabled:opacity-50"
                    :disabled="!currentSyllabus || deleting"
                    @click="handleDeleteSyllabus"
                  >
                    {{ deleting ? 'Eliminando...' : 'Eliminar archivo' }}
                  </button>
                </div>
                <p v-if="fileActionError" class="text-sm text-red-500">{{ fileActionError }}</p>
                <p v-if="fileActionSuccess" class="text-sm text-green-600">{{ fileActionSuccess }}</p>
              </div>
            </div>
          </article>

          <article class="rounded-2xl border border-gray-200 bg-white p-5 space-y-5">
            <header class="space-y-1">
              <p class="text-sm font-semibold text-gray-500 uppercase tracking-wide">Gestión de temas</p>
              <h2 class="text-2xl font-bold text-gray-900">Planifica el cronograma</h2>
              <p class="text-sm text-gray-500">Puedes registrar o limpiar el temario sin necesidad de volver a subir el archivo. Los temas son opcionales.</p>
            </header>

            <section class="space-y-2">
              <h3 class="text-lg font-semibold text-gray-900">Temario actual</h3>
              <div v-if="!currentSyllabus || !currentSyllabus.topics.length" class="rounded-xl border border-dashed border-gray-300 p-4 text-sm text-gray-500">
                Aún no se han configurado temas para este curso.
              </div>
              <ul v-else class="space-y-3 max-h-64 overflow-auto pr-1">
                <li v-for="topic in currentSyllabus.topics" :key="topic.name + topic.sessionDate" class="rounded-xl border border-gray-200 p-4">
                  <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                    <div>
                      <p class="text-sm font-semibold text-gray-900">{{ topic.name }}</p>
                      <p class="text-xs text-gray-500">Sesión: {{ formatDate(topic.sessionDate) }}</p>
                    </div>
                    <div class="flex flex-wrap gap-2">
                      <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold text-gray-700 bg-gray-100">
                        Peso: {{ topic.weight ?? '—' }}%
                      </span>
                      <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold" :class="statusBadgeClasses(topic.status)">
                        {{ statusLabel(topic.status) }}
                      </span>
                    </div>
                  </div>
                </li>
              </ul>
            </section>

            <section class="space-y-3">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-sm font-semibold text-gray-700">Editor de temas</p>
                  <p class="text-xs text-gray-500">Define el título, peso y fecha de cada sesión.</p>
                </div>
                <button type="button" class="text-sm font-semibold text-blue-600" @click="addTopicRow">Añadir tema</button>
              </div>

              <div class="space-y-4">
                <div v-for="(topic, index) in topicsForm" :key="topic.id" class="rounded-xl border border-gray-200 p-4 space-y-3">
                  <div class="flex items-center justify-between">
                    <p class="text-sm font-semibold text-gray-700">Tema {{ index + 1 }}</p>
                    <button
                      v-if="topicsForm.length > 1"
                      type="button"
                      class="text-xs font-semibold text-red-600"
                      @click="removeTopicRow(topic.id)"
                    >Eliminar</button>
                  </div>

                  <label class="block text-sm text-gray-600">
                    Nombre
                    <input v-model="topic.name" type="text" class="mt-1 w-full rounded-lg border-gray-300 text-sm" placeholder="Introducción al curso">
                  </label>

                  <div class="grid gap-3 md:grid-cols-2">
                    <label class="block text-sm text-gray-600">
                      Peso (%)
                      <input v-model="topic.weight" type="number" min="0" step="0.1" class="mt-1 w-full rounded-lg border-gray-300 text-sm" placeholder="5">
                    </label>
                    <label class="block text-sm text-gray-600">
                      Fecha de sesión
                      <input v-model="topic.sessionDate" type="date" class="mt-1 w-full rounded-lg border-gray-300 text-sm">
                    </label>
                  </div>
                </div>
              </div>
            </section>

            <div class="space-y-2">
              <div class="flex flex-wrap gap-3">
                <button
                  type="button"
                  class="flex-1 rounded-2xl bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:opacity-50"
                  :disabled="savingTopics"
                  @click="handleSubmitTopics"
                >
                  {{ savingTopics ? 'Guardando temario...' : 'Guardar temario' }}
                </button>
                <button
                  type="button"
                  class="rounded-2xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
                  @click="resetTopicsForm"
                >
                  Restablecer formulario
                </button>
              </div>
              <p v-if="topicsError" class="text-sm text-red-500">{{ topicsError }}</p>
              <p v-if="topicsSuccess" class="text-sm text-green-600">{{ topicsSuccess }}</p>
            </div>
          </article>
        </div>
      </section>

      <p v-else class="rounded-2xl border border-dashed border-gray-200 bg-white p-6 text-sm text-gray-500">
        Selecciona un curso para comenzar a cargar el sílabo y planificar los temas.
      </p>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ArrowUpTrayIcon } from '@heroicons/vue/24/outline'
import { computed, onMounted, ref, watch } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import ProfessorCourseList from '@/components/features/professor/ProfessorCourseList.vue'
import { useAuthStore } from '@/stores/auth'
import { gradeService, type ProfessorCourseSummary } from '@/services/gradeService'
import { syllabusService, type CourseSyllabusSummary, type SyllabusTopicPayload, type TopicScheduleStatus } from '@/services/syllabusService'

type TopicFormRow = {
  id: string
  name: string
  weight: string | number | ''
  sessionDate: string
}

const statusBadgeClasses = (status: TopicScheduleStatus) => {
  switch (status) {
    case 'COMPLETED':
      return 'bg-green-100 text-green-700'
    case 'TODAY':
      return 'bg-blue-100 text-blue-700'
    case 'UPCOMING':
      return 'bg-amber-100 text-amber-800'
    default:
      return 'bg-gray-200 text-gray-700'
  }
}

const statusLabel = (status: TopicScheduleStatus) => {
  switch (status) {
    case 'COMPLETED':
      return 'Finalizado'
    case 'TODAY':
      return 'Sesión de hoy'
    case 'UPCOMING':
      return 'Próximo'
    default:
      return 'Sin fecha'
  }
}

const formatBytes = (size?: number | null) => {
  if (!size || size <= 0) {
    return 'Tamaño no disponible'
  }
  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let unitIndex = 0
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }
  return `${value.toFixed(1)} ${units[unitIndex]}`
}

const formatDate = (isoDate?: string | null) => {
  if (!isoDate) {
    return 'Sin fecha'
  }
  const date = new Date(isoDate)
  if (Number.isNaN(date.getTime())) {
    return isoDate
  }
  return date.toLocaleDateString('es-PE', {
    weekday: 'short',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const resolveRefString = (value: unknown): string => {
  if (typeof value === 'string') {
    return value
  }
  if (value && typeof value === 'object' && 'value' in value) {
    const candidate = (value as { value?: unknown }).value
    return typeof candidate === 'string' ? candidate : ''
  }
  return ''
}

const createEmptyTopicRow = (defaults?: Partial<TopicFormRow>): TopicFormRow => ({
  id: Math.random().toString(36).slice(2, 9),
  name: defaults?.name ?? '',
  weight: defaults?.weight ?? '',
  sessionDate: defaults?.sessionDate ?? ''
})

const authStore = useAuthStore()
const courses = ref<ProfessorCourseSummary[]>([])
const coursesLoading = ref(false)
const coursesError = ref('')
const selectedCourse = ref<ProfessorCourseSummary | null>(null)

const syllabusLoading = ref(false)
const syllabusError = ref('')
const currentSyllabus = ref<CourseSyllabusSummary | null>(null)

const topicsForm = ref<TopicFormRow[]>([createEmptyTopicRow()])
const fileInputKey = ref(Date.now())
const syllabusFile = ref<File | null>(null)
const fileError = ref('')
const fileActionError = ref('')
const fileActionSuccess = ref('')
const topicsError = ref('')
const topicsSuccess = ref('')
const uploadingFile = ref(false)
const savingTopics = ref(false)
const deleting = ref(false)

const allowedRoles = new Set(['PROFESSOR', 'ADMIN'])

const canLoadCourses = computed(() => {
  const role = resolveRefString(authStore.userRole)
  return allowedRoles.has(role)
})

const computedFilename = computed(() => {
  if (!selectedCourse.value) {
    return '{curso}_syllabus.pdf'
  }
  const normalizedName = selectedCourse.value.courseCode.replace(/[^a-zA-Z0-9_-]/g, '-').toLowerCase()
  return `${normalizedName}_syllabus.pdf`
})

const downloadUrl = computed(() => syllabusService.buildDownloadUrl(currentSyllabus.value?.syllabusId))

const lastUpdatedLabel = computed(() => {
  if (!currentSyllabus.value?.topics.length) {
    return 'Sin registros'
  }
  const sortedDates = currentSyllabus.value.topics
    .map(topic => topic.sessionDate)
    .filter((value): value is string => Boolean(value))
    .sort()
  const latest = sortedDates.length ? sortedDates[sortedDates.length - 1] : null
  if (!latest) {
    return 'Sin fecha'
  }
  return formatDate(latest)
})

const buildTopicsPayloadFromForm = (): SyllabusTopicPayload[] => {
  return topicsForm.value
    .map(topic => ({
      name: topic.name.trim(),
      weight: topic.weight === '' ? NaN : Number(topic.weight),
      sessionDate: topic.sessionDate
    }))
    .filter(topic => Boolean(topic.name) && Number.isFinite(topic.weight) && topic.weight > 0 && Boolean(topic.sessionDate)) as SyllabusTopicPayload[]
}

const mapTopicsToPayload = (topics?: CourseSyllabusSummary['topics']): SyllabusTopicPayload[] => {
  if (!topics?.length) {
    return []
  }
  return topics
    .map(topic => ({
      name: (topic.name ?? '').trim(),
      weight: typeof topic.weight === 'number'
        ? topic.weight
        : (typeof topic.weight === 'string' ? Number(topic.weight) : NaN),
      sessionDate: topic.sessionDate ?? ''
    }))
    .filter(topic => Boolean(topic.name) && Number.isFinite(topic.weight) && topic.weight > 0 && Boolean(topic.sessionDate)) as SyllabusTopicPayload[]
}

const hasIncompleteTopicRows = () => {
  return topicsForm.value.some(topic => {
    const hasAnyField = Boolean(topic.name.trim()) || (topic.weight !== '' && topic.weight != null) || Boolean(topic.sessionDate)
    if (!hasAnyField) {
      return false
    }
    const numericWeight = Number(topic.weight)
    const isComplete = Boolean(topic.name.trim()) && topic.weight !== '' && Number.isFinite(numericWeight) && numericWeight > 0 && Boolean(topic.sessionDate)
    return !isComplete
  })
}

const formHasTopicData = () => {
  return topicsForm.value.some(topic =>
    Boolean(topic.name.trim()) || (topic.weight !== '' && topic.weight != null) || Boolean(topic.sessionDate)
  )
}

const canUploadFile = computed(() => Boolean(syllabusFile.value) && !uploadingFile.value)

const selectedFileSummary = computed(() => {
  if (syllabusFile.value) {
    return syllabusFile.value.name
  }
  if (currentSyllabus.value?.content?.name) {
    return `Último cargado: ${currentSyllabus.value.content.name}`
  }
  return 'Selecciona o arrastra un archivo PDF para reemplazar el sílabo'
})

const selectedFileDetails = computed(() => {
  if (syllabusFile.value) {
    return formatBytes(syllabusFile.value.size)
  }
  const size = currentSyllabus.value?.content?.sizeBytes
  const numericSize = typeof size === 'number'
    ? size
    : (typeof size === 'string' ? Number(size) : null)
  if (typeof numericSize === 'number' && Number.isFinite(numericSize) && numericSize > 0) {
    return `${formatBytes(numericSize)} · Archivo vigente`
  }
  return 'Hasta 100 MB · Solo PDF'
})

const loadCourses = async () => {
  if (!canLoadCourses.value) {
    courses.value = []
    coursesError.value = 'Tu rol no tiene acceso a sílabos de profesor'
    return
  }

  coursesLoading.value = true
  coursesError.value = ''
  try {
    courses.value = await gradeService.fetchProfessorCourseSummaries()
  } catch (error) {
    console.error('Error cargando cursos para sílabos', error)
    coursesError.value = error instanceof Error ? error.message : 'No se pudieron cargar los cursos asignados'
    courses.value = []
  } finally {
    coursesLoading.value = false
  }
}

const loadCourseSyllabus = async (courseId: number) => {
  if (!courseId) {
    currentSyllabus.value = null
    return
  }
  syllabusLoading.value = true
  syllabusError.value = ''
  try {
    const syllabus = await syllabusService.fetchByCourse(courseId)
    currentSyllabus.value = syllabus
    if (syllabus?.topics?.length) {
      prefillTopicsFromSyllabus(syllabus)
    } else {
      resetTopicsForm()
    }
  } catch (error) {
    console.error('Error cargando sílabo del curso', error)
    syllabusError.value = error instanceof Error ? error.message : 'No se pudo cargar el sílabo'
    currentSyllabus.value = null
    resetTopicsForm()
  } finally {
    syllabusLoading.value = false
  }
}

const handleSelectCourse = async (course: ProfessorCourseSummary) => {
  selectedCourse.value = course
  fileActionError.value = ''
  fileActionSuccess.value = ''
  topicsError.value = ''
  topicsSuccess.value = ''
  syllabusFile.value = null
  fileError.value = ''
  fileInputKey.value = Date.now()
  await loadCourseSyllabus(course.courseId)
}

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]
  fileError.value = ''
  fileActionSuccess.value = ''
  if (!file) {
    syllabusFile.value = null
    return
  }
  if (file.type !== 'application/pdf') {
    fileError.value = 'El archivo debe ser PDF'
    syllabusFile.value = null
    return
  }
  const maxBytes = 100 * 1024 * 1024
  if (file.size > maxBytes) {
    fileError.value = 'El archivo supera el límite de 100 MB'
    syllabusFile.value = null
    return
  }
  syllabusFile.value = file
}

const addTopicRow = () => {
  topicsForm.value = [...topicsForm.value, createEmptyTopicRow()]
}

const removeTopicRow = (id: string) => {
  if (topicsForm.value.length === 1) {
    return
  }
  topicsForm.value = topicsForm.value.filter(topic => topic.id !== id)
}

const resetTopicsForm = () => {
  topicsForm.value = [createEmptyTopicRow()]
}

const prefillTopicsFromSyllabus = (syllabus: CourseSyllabusSummary) => {
  if (!syllabus.topics.length) {
    resetTopicsForm()
    return
  }
  topicsForm.value = syllabus.topics.map(topic => createEmptyTopicRow({
    name: topic.name,
    weight: topic.weight != null ? topic.weight.toString() : '',
    sessionDate: topic.sessionDate ?? ''
  }))
}

const handleUploadFile = async () => {
  fileActionError.value = ''
  fileActionSuccess.value = ''
  topicsSuccess.value = ''

  if (!selectedCourse.value) {
    fileActionError.value = 'Selecciona un curso antes de subir el sílabo'
    return
  }
  if (!syllabusFile.value) {
    fileError.value = 'Selecciona un archivo PDF válido'
    return
  }

  const formHasData = formHasTopicData()
  if (formHasData && hasIncompleteTopicRows()) {
    fileActionError.value = 'Completa los campos de cada tema o elimina las filas incompletas antes de subir el sílabo'
    return
  }

  const preservedTopics = formHasData
    ? buildTopicsPayloadFromForm()
    : mapTopicsToPayload(currentSyllabus.value?.topics)

  uploadingFile.value = true
  try {
    const syllabus = await syllabusService.upload({
      courseId: selectedCourse.value.courseId,
      file: syllabusFile.value,
      topics: preservedTopics
    })
    currentSyllabus.value = syllabus
    fileActionSuccess.value = 'El archivo del sílabo se guardó correctamente'
    prefillTopicsFromSyllabus(syllabus)
    syllabusFile.value = null
    fileInputKey.value = Date.now()
    fileError.value = ''
  } catch (error) {
    console.error('Error al subir el sílabo', error)
    fileActionError.value = error instanceof Error ? error.message : 'No se pudo subir el sílabo'
  } finally {
    uploadingFile.value = false
  }
}

const handleSubmitTopics = async () => {
  topicsError.value = ''
  topicsSuccess.value = ''

  if (!selectedCourse.value) {
    topicsError.value = 'Selecciona un curso para registrar los temas'
    return
  }
  if (!currentSyllabus.value?.syllabusId) {
    topicsError.value = 'Sube el archivo del sílabo antes de guardar el temario'
    return
  }
  if (hasIncompleteTopicRows()) {
    topicsError.value = 'Completa los campos de cada tema o elimina las filas incompletas'
    return
  }

  const validTopics = buildTopicsPayloadFromForm()

  savingTopics.value = true
  try {
    const syllabus = await syllabusService.updateTopics(selectedCourse.value.courseId, validTopics)
    currentSyllabus.value = syllabus
    topicsSuccess.value = validTopics.length
      ? 'Temario actualizado correctamente'
      : 'Se guardó el sílabo sin temas. Puedes añadirlos más adelante.'
    prefillTopicsFromSyllabus(syllabus)
  } catch (error) {
    console.error('Error al registrar los temas del sílabo', error)
    topicsError.value = error instanceof Error ? error.message : 'No se pudieron actualizar los temas'
  } finally {
    savingTopics.value = false
  }
}

const handleDeleteSyllabus = async () => {
  if (!currentSyllabus.value?.syllabusId) {
    return
  }
  const confirmed = window.confirm('¿Seguro que deseas eliminar el sílabo actual?')
  if (!confirmed) {
    return
  }

  deleting.value = true
  fileActionSuccess.value = ''
  fileActionError.value = ''
  try {
    await syllabusService.delete(currentSyllabus.value.syllabusId)
    currentSyllabus.value = null
    resetTopicsForm()
    fileActionSuccess.value = 'El sílabo fue eliminado. Puedes subir una nueva versión.'
  } catch (error) {
    console.error('Error al eliminar el sílabo', error)
    fileActionError.value = error instanceof Error ? error.message : 'No se pudo eliminar el sílabo'
  } finally {
    deleting.value = false
  }
}

const initialize = async () => {
  if (!authStore.initialized) {
    await authStore.initializeAuth()
  }
  await loadCourses()
}

onMounted(() => {
  void initialize()
})

watch(() => authStore.userRole, () => {
  if (!allowedRoles.has(resolveRefString(authStore.userRole))) {
    selectedCourse.value = null
    currentSyllabus.value = null
  }
})
</script>

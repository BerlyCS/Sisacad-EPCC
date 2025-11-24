<template>
  <AdminLayout>
    <div class="min-h-screen bg-gray-50 py-8">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="bg-white shadow-xl rounded-lg overflow-hidden">
          <div class="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4">
            <div class="flex items-center">
              <div class="flex-shrink-0">
                <svg class="h-8 w-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                </svg>
              </div>
              <div class="ml-4">
                <p class="text-sm font-semibold text-blue-100 uppercase tracking-wide">Gestión académica</p>
                <h2 class="text-2xl font-bold text-white">Agregar Nuevo Curso</h2>
                <p class="text-blue-100 mt-1">Complete la información para crear un nuevo curso en el sistema</p>
              </div>
            </div>
          </div>

          <div class="p-8">
            <form @submit.prevent="handleSubmit" class="space-y-8">
              <!-- Información Básica -->
              <div class="bg-gray-50 rounded-lg p-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg class="h-5 w-5 text-blue-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                  </svg>
                  Información Básica
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label for="courseCode" class="block text-sm font-medium text-gray-700 mb-2">
                      Código del Curso <span class="text-red-500">*</span>
                    </label>
                    <div class="relative">
                      <input
                        id="courseCode"
                        v-model.number="form.courseCode"
                        type="number"
                        required
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: 12345"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Código único del curso</p>
                  </div>

                  <div>
                    <label for="name" class="block text-sm font-medium text-gray-700 mb-2">
                      Nombre del Curso <span class="text-red-500">*</span>
                    </label>
                    <div class="relative">
                      <input
                        id="name"
                        v-model="form.name"
                        type="text"
                        required
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: Matemáticas Avanzadas"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Nombre completo del curso</p>
                  </div>
                </div>
              </div>

              <!-- Créditos y Semestre -->
              <div class="bg-gray-50 rounded-lg p-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg class="h-5 w-5 text-blue-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                  </svg>
                  Créditos y Semestre
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label for="credits" class="block text-sm font-medium text-gray-700 mb-2">
                      Créditos <span class="text-red-500">*</span>
                    </label>
                    <div class="relative">
                      <input
                        id="credits"
                        v-model.number="form.credits"
                        type="number"
                        min="1"
                        max="10"
                        required
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: 4"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z"></path>
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Número de créditos del curso (1-10)</p>
                  </div>

                  <div>
                    <label for="semesterNumber" class="block text-sm font-medium text-gray-700 mb-2">
                      Número de Semestre <span class="text-red-500">*</span>
                    </label>
                    <div class="relative">
                      <input
                        id="semesterNumber"
                        v-model.number="form.semesterNumber"
                        type="number"
                        min="1"
                        max="10"
                        required
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: 5"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Semestre en el que se imparte el curso</p>
                  </div>
                </div>
              </div>

              <!-- Horas -->
              <div class="bg-gray-50 rounded-lg p-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg class="h-5 w-5 text-blue-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                  </svg>
                  Distribución de Horas
                </h3>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div>
                    <label for="theoryHours" class="block text-sm font-medium text-gray-700 mb-2">
                      Horas de Teoría
                    </label>
                    <div class="relative">
                      <input
                        id="theoryHours"
                        v-model.number="form.theoryHours"
                        type="number"
                        min="0"
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: 2"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.746 0 3.332.477 4.5 1.253v13C19.832 18.477 18.246 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Horas semanales de teoría</p>
                  </div>

                  <div>
                    <label for="practiceHours" class="block text-sm font-medium text-gray-700 mb-2">
                      Horas de Práctica
                    </label>
                    <div class="relative">
                      <input
                        id="practiceHours"
                        v-model.number="form.practiceHours"
                        type="number"
                        min="0"
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: 4"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Horas semanales de práctica</p>
                  </div>

                  <div>
                    <label for="labHours" class="block text-sm font-medium text-gray-700 mb-2">
                      Horas de Laboratorio
                    </label>
                    <div class="relative">
                      <input
                        id="labHours"
                        v-model.number="form.labHours"
                        type="number"
                        min="0"
                        class="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition duration-150 ease-in-out"
                        placeholder="Ej: 2"
                      />
                      <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z"></path>
                        </svg>
                      </div>
                    </div>
                    <p class="mt-1 text-sm text-gray-500">Horas semanales de laboratorio</p>
                  </div>
                </div>
              </div>

              <!-- Sílabo -->
              <div class="bg-gray-50 rounded-lg p-6">
                <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                  <svg class="h-5 w-5 text-blue-600 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                  </svg>
                  Documentación
                </h3>
                <div>
                  <label for="syllabusFile" class="block text-sm font-medium text-gray-700 mb-2">
                    Archivo del Sílabo (PDF, opcional)
                  </label>
                  <div class="mt-1 flex justify-center px-6 pt-5 pb-6 border-2 border-gray-300 border-dashed rounded-lg hover:border-blue-400 transition duration-150 ease-in-out">
                    <div class="space-y-1 text-center">
                      <svg class="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                        <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28m0 0l4 4m4-24h8m-4-4v8m-12 4h.02" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"></path>
                      </svg>
                      <div class="flex text-sm text-gray-600">
                        <label for="syllabusFile" class="relative cursor-pointer bg-white rounded-md font-medium text-blue-600 hover:text-blue-500 focus-within:outline-none focus-within:ring-2 focus-within:ring-offset-2 focus-within:ring-blue-500">
                          <span>Subir un archivo</span>
                          <input
                            id="syllabusFile"
                            ref="syllabusFileRef"
                            type="file"
                            accept=".pdf"
                            class="sr-only"
                          />
                        </label>
                        <p class="pl-1">o arrastra y suelta</p>
                      </div>
                      <p class="text-xs text-gray-500">PDF hasta 10MB</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Mensajes -->
              <div v-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-red-800">{{ error }}</p>
                  </div>
                </div>
              </div>

              <div v-if="success" class="bg-green-50 border border-green-200 rounded-lg p-4">
                <div class="flex">
                  <div class="flex-shrink-0">
                    <svg class="h-5 w-5 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                  </div>
                  <div class="ml-3">
                    <p class="text-sm text-green-800">{{ success }}</p>
                  </div>
                </div>
              </div>

              <!-- Botones -->
              <div class="flex justify-end space-x-4 pt-6 border-t border-gray-200">
                <button
                  type="button"
                  @click="$router.push('/admin/courses')"
                  class="px-6 py-3 border border-gray-300 rounded-lg shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150 ease-in-out"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  :disabled="loading"
                  class="px-6 py-3 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition duration-150 ease-in-out flex items-center"
                >
                  <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  {{ loading ? 'Creando...' : 'Crear Curso' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../components/ui/TopBar.vue'
import { useCourseService } from '../services/courseService'

const router = useRouter()
const { createCourse } = useCourseService()

const syllabusFileRef = ref<HTMLInputElement | null>(null)

const loading = ref(false)
const error = ref('')
const success = ref('')

const form = reactive({
  courseCode: null as number | null,
  name: '',
  credits: null as number | null,
  semesterNumber: null as number | null,
  theoryHours: null as number | null,
  practiceHours: null as number | null,
  labHours: null as number | null
})

const resetForm = () => {
  form.courseCode = null
  form.name = ''
  form.credits = null
  form.semesterNumber = null
  form.theoryHours = null
  form.practiceHours = null
  form.labHours = null
  if (syllabusFileRef.value) {
    syllabusFileRef.value.value = ''
  }
}

const handleSubmit = async () => {
  if (!form.courseCode || !form.name || !form.credits || !form.semesterNumber) {
    error.value = 'Por favor completa todos los campos requeridos'
    success.value = ''
    return
  }

  loading.value = true
  error.value = ''
  success.value = ''

  try {
    const course = await createCourse({
      courseCode: form.courseCode,
      name: form.name,
      credits: form.credits,
      semesterNumber: form.semesterNumber,
      theoryHours: form.theoryHours,
      practiceHours: form.practiceHours,
      labHours: form.labHours
    })

    // If syllabus file is provided, upload it
    const file = syllabusFileRef.value?.files?.[0]
    if (file && course.courseId) {
      // TODO: Upload syllabus using syllabusService
      // await uploadSyllabus(course.courseId, file, [], authentication)
    }

    success.value = 'Curso creado exitosamente'
    resetForm()

    // Redirect after 2 seconds
    setTimeout(() => {
      router.push('/admin/courses')
    }, 2000)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudo crear el curso'
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200">
        <div>
          <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Gestión académica</p>
          <h2 class="text-xl font-semibold text-gray-800">Agregar Nuevo Curso</h2>
          <p class="text-gray-600 mt-1">Crea un nuevo curso en el sistema</p>
        </div>
      </div>

      <div class="p-6">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label for="courseCode" class="block text-sm font-medium text-gray-700">
                Código del Curso
              </label>
              <input
                id="courseCode"
                v-model.number="form.courseCode"
                type="number"
                required
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: 12345"
              />
            </div>

            <div>
              <label for="name" class="block text-sm font-medium text-gray-700">
                Nombre del Curso
              </label>
              <input
                id="name"
                v-model="form.name"
                type="text"
                required
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: Matemáticas Avanzadas"
              />
            </div>

            <div>
              <label for="credits" class="block text-sm font-medium text-gray-700">
                Créditos
              </label>
              <input
                id="credits"
                v-model.number="form.credits"
                type="number"
                min="1"
                max="10"
                required
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: 4"
              />
            </div>

            <div>
              <label for="semesterNumber" class="block text-sm font-medium text-gray-700">
                Número de Semestre
              </label>
              <input
                id="semesterNumber"
                v-model.number="form.semesterNumber"
                type="number"
                min="1"
                max="10"
                required
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: 5"
              />
            </div>

            <div>
              <label for="theoryHours" class="block text-sm font-medium text-gray-700">
                Horas de Teoría
              </label>
              <input
                id="theoryHours"
                v-model.number="form.theoryHours"
                type="number"
                min="0"
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: 45"
              />
            </div>

            <div>
              <label for="practiceHours" class="block text-sm font-medium text-gray-700">
                Horas de Práctica
              </label>
              <input
                id="practiceHours"
                v-model.number="form.practiceHours"
                type="number"
                min="0"
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: 30"
              />
            </div>

            <div>
              <label for="labHours" class="block text-sm font-medium text-gray-700">
                Horas de Laboratorio
              </label>
              <input
                id="labHours"
                v-model.number="form.labHours"
                type="number"
                min="0"
                class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                placeholder="Ej: 15"
              />
            </div>

            <div class="md:col-span-2">
              <label for="syllabusFile" class="block text-sm font-medium text-gray-700">
                Archivo del Sílabo (PDF, opcional)
              </label>
              <input
                id="syllabusFile"
                ref="syllabusFileRef"
                type="file"
                accept=".pdf"
                class="mt-1 block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
              />
            </div>
          </div>

          <div v-if="error" class="bg-red-50 border border-red-200 rounded-md p-4">
            <p class="text-red-800">{{ error }}</p>
          </div>

          <div class="flex justify-end space-x-3">
            <button
              type="button"
              @click="$router.push('/admin/courses')"
              class="px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Cancelar
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
            >
              {{ loading ? 'Creando...' : 'Crear Curso' }}
            </button>
          </div>
        </form>
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

const form = reactive({
  courseCode: null as number | null,
  name: '',
  credits: null as number | null,
  semesterNumber: null as number | null,
  theoryHours: null as number | null,
  practiceHours: null as number | null,
  labHours: null as number | null
})

const handleSubmit = async () => {
  if (!form.courseCode || !form.name || !form.credits || !form.semesterNumber) {
    error.value = 'Por favor completa todos los campos requeridos'
    return
  }

  loading.value = true
  error.value = ''

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

    router.push('/admin/courses')
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'No se pudo crear el curso'
  } finally {
    loading.value = false
  }
}
</script>
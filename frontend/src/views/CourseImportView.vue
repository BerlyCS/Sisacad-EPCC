<template>
  <AdminLayout>
    <section class="space-y-8 text-slate-900">
      <header class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div class="max-w-3xl space-y-2">
            <p class="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">Importación de cursos</p>
            <h1 class="text-3xl font-semibold leading-tight">Sube cursos desde CSV o Excel</h1>
            <p class="text-sm text-slate-600">
              Revisa que la plantilla tenga columnas en el mismo orden. El sistema valida que los pesos sumen 100 % y que los
              campos obligatorios estén completos.
            </p>
          </div>
          <div class="flex flex-col gap-2 text-sm text-slate-600">
            <span class="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-1">CSV o XLSX</span>
            <span class="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-1">Validación automática</span>
          </div>
        </div>
        <div class="mt-4 flex flex-wrap gap-2 text-sm">
          <router-link
            to="/admin/users/import"
            class="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-slate-700 hover:bg-slate-50"
          >
            <i class="fas fa-users"></i>
            Importar usuarios
          </router-link>
          <router-link
            to="/admin/classrooms/import"
            class="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-slate-700 hover:bg-slate-50"
          >
            <i class="fas fa-door-open"></i>
            Importar aulas
          </router-link>
        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[1.4fr,1fr]">
        <div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="space-y-1">
            <p class="text-sm font-semibold text-slate-800">Archivo a subir</p>
            <p class="text-sm text-slate-600">
              Selecciona un archivo .csv o .xlsx con los pesos de evaluaciones continuas y exámenes completos.
            </p>
          </div>

          <form class="space-y-6" @submit.prevent="handleSubmit">
            <label class="block rounded-xl border border-dashed border-slate-300 bg-slate-50 p-5 text-center transition hover:border-slate-400">
              <input
                ref="fileInput"
                type="file"
                accept=".csv,.xlsx,.xls"
                class="hidden"
                @change="handleFileSelection"
              />
              <div class="flex flex-col items-center justify-center gap-2">
                <i class="fas fa-cloud-upload-alt text-3xl text-slate-400"></i>
                <p class="text-sm font-semibold text-slate-700">Arrastra o selecciona el archivo</p>
                <p class="text-xs text-slate-500">Límite sugerido: 10 MB</p>
                <p v-if="selectedFile" class="text-xs font-semibold text-slate-600">Archivo elegido: {{ selectedFile.name }}</p>
              </div>
            </label>

            <div class="flex flex-wrap items-center gap-3">
              <button
                type="submit"
                class="inline-flex items-center justify-center gap-2 rounded-xl bg-slate-900 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:opacity-60"
                :disabled="importing"
              >
                <i class="fas fa-file-upload" aria-hidden="true"></i>
                <span v-if="!importing">Importar cursos</span>
                <span v-else>Importando…</span>
              </button>
              <button
                type="button"
                class="rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                @click="resetSelection"
              >
                Cambiar archivo
              </button>
              <a
                class="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                href="/sample-data/courses-import-sample.csv"
                download
              >
                <i class="fas fa-download"></i>
                Descargar plantilla
              </a>
            </div>
          </form>

          <div v-if="importError" class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {{ importError }}
          </div>

          <div v-if="importResult" class="space-y-4">
            <div class="grid gap-3 sm:grid-cols-3">
              <div class="rounded-xl bg-slate-50 p-3 text-center">
                <p class="text-xs uppercase tracking-[0.15em] text-slate-500">Procesadas</p>
                <p class="text-2xl font-semibold text-slate-900">{{ importResult.processedRows }}</p>
              </div>
              <div class="rounded-xl bg-emerald-50 p-3 text-center">
                <p class="text-xs uppercase tracking-[0.15em] text-emerald-600">Importadas</p>
                <p class="text-2xl font-semibold text-emerald-700">{{ importResult.createdCourses.length }}</p>
              </div>
              <div class="rounded-xl bg-amber-50 p-3 text-center">
                <p class="text-xs uppercase tracking-[0.15em] text-amber-600">Omitidas</p>
                <p class="text-2xl font-semibold text-amber-700">{{ importResult.skippedRows }}</p>
              </div>
            </div>

            <div v-if="importResult.createdCourses.length" class="rounded-xl border border-slate-200 bg-white p-4">
              <p class="text-sm font-semibold text-slate-800">Cursos registrados</p>
              <ul class="mt-2 space-y-1 text-sm text-slate-600">
                <li v-for="course in importResult.createdCourses.slice(0, 5)" :key="course.courseId">
                  {{ course.courseCode ?? 'SIN CÓDIGO' }} · {{ course.name ?? 'Sin nombre' }}
                </li>
              </ul>
              <p v-if="importResult.createdCourses.length > 5" class="mt-2 text-xs text-slate-500">
                Se registraron {{ importResult.createdCourses.length }} cursos, se muestran los primeros 5.
              </p>
            </div>

            <div v-if="importResult.errors.length" class="rounded-xl border border-red-100 bg-red-50 p-4 text-sm text-red-700">
              <p class="font-semibold">Errores detectados</p>
              <ul class="mt-2 list-disc pl-5">
                <li v-for="(message, index) in importResult.errors" :key="`import-error-${index}`">
                  {{ message }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="space-y-2">
            <h2 class="text-xl font-semibold">Plantilla requerida</h2>
            <p class="text-sm text-slate-600">
              Orden exacto de columnas. Los pesos deben ser enteros y sumar 100 %.
            </p>
          </div>
          <div class="grid grid-cols-2 gap-2 text-sm text-slate-700">
            <span class="rounded-lg bg-slate-50 px-3 py-2">1. course_code</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">2. name</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">3. credits</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">4. semester_number</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">5. theory_hours</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">6. practice_hours</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">7. lab_hours</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">8. continuous_weight_1</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">9. continuous_weight_2</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">10. continuous_weight_3</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">11. exam_weight_1</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">12. exam_weight_2</span>
            <span class="rounded-lg bg-slate-50 px-3 py-2">13. exam_weight_3</span>
          </div>
          <p class="text-xs uppercase tracking-[0.35em] text-slate-400">Los pesos continuos + exámenes = 100 %</p>
        </div>
      </div>
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { useCourseService, type CourseImportResult } from '@/services/courseService'

const { importCourses } = useCourseService()
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const importError = ref('')
const importResult = ref<CourseImportResult | null>(null)

const handleFileSelection = (event: Event) => {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0] ?? null
  importError.value = ''
}

const resetSelection = () => {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  selectedFile.value = null
  importResult.value = null
  importError.value = ''
}

const handleSubmit = async () => {
  if (!selectedFile.value) {
    importError.value = 'Selecciona un archivo antes de importar'
    return
  }

  importing.value = true
  importError.value = ''
  try {
    importResult.value = await importCourses(selectedFile.value)
  } catch (error) {
    importResult.value = null
    importError.value = error instanceof Error ? error.message : 'No se pudo completar la importación'
  } finally {
    importing.value = false
  }
}
</script>

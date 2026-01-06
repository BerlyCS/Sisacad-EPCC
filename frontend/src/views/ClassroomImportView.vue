<template>
  <AdminLayout>
    <section class="space-y-8 text-slate-900">
      <header class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div class="max-w-3xl space-y-2">
            <h1 class="text-3xl font-semibold leading-tight">Registrar aulas desde CSV o Excel</h1>
            <p class="text-sm text-slate-600">
              Usa la plantilla base, corrige advertencias y vuelve a subir el mismo archivo. Validamos columnas y campos obligatorios.
            </p>
          </div>
          <div class="flex flex-col gap-2 text-sm text-slate-700">
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
            to="/admin/courses/import"
            class="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-slate-700 hover:bg-slate-50"
          >
            <i class="fas fa-book"></i>
            Importar cursos
          </router-link>
        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[1.4fr,1fr]">
        <form class="space-y-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="handleSubmit">
          <div class="space-y-1">
            <p class="text-sm font-semibold text-slate-800">Archivo a subir</p>
            <p class="text-sm text-slate-600">Selecciona el archivo y limpia la selección si cambias de versión.</p>
          </div>

          <div class="flex flex-col gap-3">
            <div class="flex flex-wrap items-center gap-3">
              <label class="sr-only" for="file-input">Selecciona tu archivo</label>
              <input
                id="file-input"
                ref="fileInput"
                type="file"
                accept=".csv,.xlsx,.xls"
                class="w-full rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm text-slate-700 shadow-sm focus:border-slate-500 focus:outline-none"
                @change="handleFileSelection"
              />
              <button
                type="button"
                class="rounded-xl border border-slate-200 px-3 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
                @click="resetSelection"
              >
                Limpiar
              </button>
            </div>
            <p class="text-xs text-slate-500">Tamaño recomendado: hasta 10 MB. Solo se procesan las columnas indicadas.</p>
          </div>

          <div class="flex flex-wrap gap-3">
            <button
              type="submit"
              class="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="importing"
            >
              <i class="fas fa-file-upload"></i>
              <span v-if="!importing">Importar aulas</span>
              <span v-else>Procesando…</span>
            </button>
            <a
              href="/sample-data/classrooms-import-sample.csv"
              download
              class="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              <i class="fas fa-download"></i>
              Descargar plantilla
            </a>
          </div>
          <p v-if="statusMessage" aria-live="polite" class="text-sm text-slate-600">{{ statusMessage }}</p>
          <p v-if="importError" class="text-sm font-semibold text-rose-600">{{ importError }}</p>
        </form>

        <section aria-label="Resultados" class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 class="text-base font-semibold text-slate-900">Resumen</h2>
          <dl class="grid grid-cols-3 gap-3 text-sm text-slate-600">
            <div class="flex flex-col rounded-xl bg-slate-50 p-3 text-center">
              <span class="text-xs uppercase tracking-[0.15em] text-slate-400">Procesadas</span>
              <span class="text-lg font-semibold text-slate-900">{{ importResult?.processedRows ?? 0 }}</span>
            </div>
            <div class="flex flex-col rounded-xl bg-emerald-50 p-3 text-center">
              <span class="text-xs uppercase tracking-[0.15em] text-emerald-600">Importadas</span>
              <span class="text-lg font-semibold text-emerald-700">{{ importResult?.created.length ?? 0 }}</span>
            </div>
            <div class="flex flex-col rounded-xl bg-amber-50 p-3 text-center">
              <span class="text-xs uppercase tracking-[0.15em] text-amber-600">Omitidas</span>
              <span class="text-lg font-semibold text-amber-700">{{ importResult?.skippedRows ?? 0 }}</span>
            </div>
          </dl>

          <div v-if="importResult && importResult.errors.length" class="rounded-xl border border-rose-100 bg-rose-50 p-4 text-sm text-rose-700">
            <p class="font-semibold text-rose-700">Errores detectados</p>
            <ul class="mt-2 list-disc space-y-1 pl-5">
              <li v-for="(message, index) in importResult.errors" :key="`classroom-import-error-${index}`">
                {{ message }}
              </li>
            </ul>
          </div>
        </section>
      </div>

      <section class="space-y-3 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-base font-semibold text-slate-900">Formato esperado</h2>
        <p class="text-sm text-slate-600">
          Ordena las columnas exactamente como se muestra, sin encabezados adicionales. Los campos obligatorios están marcados.
        </p>
        <ol class="space-y-2 text-sm text-slate-700">
          <li><span class="font-semibold">1.</span> classroom_id (opcional, se ignora)</li>
          <li><span class="font-semibold">2.</span> building (nombre del edificio) *</li>
          <li><span class="font-semibold">3.</span> capacity (número mayor a cero) *</li>
          <li><span class="font-semibold">4.</span> classroom_type (por ejemplo: AULA o LAB)</li>
          <li><span class="font-semibold">5.</span> floor (piso)</li>
          <li><span class="font-semibold">6.</span> number (número visible del aula) *</li>
        </ol>
        <p class="text-xs uppercase tracking-[0.4em] text-slate-400">*- requerido</p>
      </section>
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { importClassroomsFile, type ClassroomImportResult } from '@/services/classroomService'

const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const importError = ref('')
const importResult = ref<ClassroomImportResult | null>(null)
const statusMessage = ref('')

const handleFileSelection = (event: Event) => {
  const target = event.target as HTMLInputElement
  selectedFile.value = target.files?.[0] ?? null
  importError.value = ''
  statusMessage.value = selectedFile.value ? `Archivo listo: ${selectedFile.value.name}` : ''
}

const resetSelection = () => {
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  selectedFile.value = null
  importResult.value = null
  statusMessage.value = ''
  importError.value = ''
}

const handleSubmit = async () => {
  if (!selectedFile.value) {
    importError.value = 'Primero debes seleccionar un archivo.'
    return
  }

  importing.value = true
  importError.value = ''
  statusMessage.value = 'Enviando archivo al servidor...'

  try {
    importResult.value = await importClassroomsFile(selectedFile.value)
    statusMessage.value = 'Importación completada. Revisa el resumen.'
  } catch (error) {
    importError.value = error instanceof Error ? error.message : 'No se pudo completar la importación'
    importResult.value = null
    statusMessage.value = 'Hubo un problema al procesar el archivo.'
  } finally {
    importing.value = false
  }
}
</script>

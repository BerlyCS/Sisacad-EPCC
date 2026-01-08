<template>
  <AdminLayout>
    <section class="space-y-8 text-slate-900">
      <header class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div class="max-w-3xl space-y-2">
            <h1 class="text-3xl font-semibold leading-tight">Importar matrículas con notas iniciales</h1>
            <p class="text-sm text-slate-600">
              Sube un CSV o Excel con columnas: CUI, curso_codigo, grupo, cont1, cont2, exam1, exam2.
              Validamos que el CUI exista, el curso y grupo estén disponibles y que las notas estén entre 0 y 20.
            </p>
          </div>
          <div class="flex flex-col gap-2 text-sm text-slate-700">
            <span class="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-1">Solo ADMIN/SECRETARY</span>
            <span class="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-1">CSV o XLSX</span>
          </div>
        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[1.4fr,1fr]">
        <form class="space-y-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="handleSubmit">
          <div class="space-y-1">
            <p class="text-sm font-semibold text-slate-800">Archivo a subir</p>
            <p class="text-sm text-slate-600">Selecciona tu archivo y limpia si necesitas reemplazarlo.</p>
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
            <p class="text-xs text-slate-500">Hasta 10 MB. Procesamos solo las columnas indicadas.</p>
          </div>

          <div class="flex flex-wrap gap-3">
            <button
              type="submit"
              class="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="importing"
            >
              <i class="fas fa-file-upload"></i>
              <span v-if="!importing">Importar matrículas</span>
              <span v-else>Procesando…</span>
            </button>
            <a
              href="/sample-data/enrollment-import-sample.csv"
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
              <span class="text-xs uppercase tracking-[0.15em] text-emerald-600">Exitosas</span>
              <span class="text-lg font-semibold text-emerald-700">{{ importResult?.created.length ?? 0 }}</span>
            </div>
            <div class="flex flex-col rounded-xl bg-amber-50 p-3 text-center">
              <span class="text-xs uppercase tracking-[0.15em] text-amber-600">Omitidas</span>
              <span class="text-lg font-semibold text-amber-700">{{ importResult?.skippedRows ?? 0 }}</span>
            </div>
          </dl>

          <div v-if="importResult?.created.length" class="rounded-xl border border-emerald-100 bg-emerald-50 p-4 text-sm text-emerald-800">
            <p class="font-semibold">Matrículas registradas</p>
            <ul class="mt-2 list-disc space-y-1 pl-5">
              <li v-for="(item, index) in importResult.created.slice(0, 6)" :key="`enroll-ok-${index}`">
                {{ item }}
              </li>
              <li v-if="importResult.created.length > 6" class="italic">+{{ importResult.created.length - 6 }} más</li>
            </ul>
          </div>

          <div v-if="importResult && importResult.errors.length" class="rounded-xl border border-rose-100 bg-rose-50 p-4 text-sm text-rose-700">
            <p class="font-semibold text-rose-700">Errores detectados</p>
            <ul class="mt-2 list-disc space-y-1 pl-5">
              <li v-for="(message, index) in importResult.errors" :key="`enroll-error-${index}`">
                {{ message }}
              </li>
            </ul>
          </div>
        </section>
      </div>

      <section class="space-y-3 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-base font-semibold text-slate-900">Formato esperado</h2>
        <p class="text-sm text-slate-600">Una fila por matrícula. Las notas son opcionales; si se dejan vacías no se registran.</p>
        <ol class="space-y-2 text-sm text-slate-700">
          <li><span class="font-semibold">1.</span> CUI *</li>
          <li><span class="font-semibold">2.</span> curso_codigo *</li>
          <li><span class="font-semibold">3.</span> grupo (letra) *</li>
          <li><span class="font-semibold">4.</span> cont1 (0-20)</li>
          <li><span class="font-semibold">5.</span> cont2 (0-20)</li>
          <li><span class="font-semibold">6.</span> exam1 (0-20)</li>
          <li><span class="font-semibold">7.</span> exam2 (0-20)</li>
        </ol>
        <p class="text-xs uppercase tracking-[0.4em] text-slate-400">*- requerido</p>
      </section>
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { importEnrollmentsFile, type EnrollmentImportResult } from '@/services/enrollmentImportService'

const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const importError = ref('')
const importResult = ref<EnrollmentImportResult | null>(null)
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
    importResult.value = await importEnrollmentsFile(selectedFile.value)
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

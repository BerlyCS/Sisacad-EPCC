<template>
  <AdminLayout>
    <section class="space-y-8 text-slate-900">
      <header class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div class="max-w-3xl space-y-2">
            <p class="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">Importación de personas</p>
            <h1 class="text-3xl font-semibold leading-tight">Carga estudiantes, profesores y secretarias</h1>
            <p class="text-sm text-slate-600">
              Usa un solo formulario para los tres tipos. Validamos correos institucionales, detectamos duplicados y te
              mostramos qué filas necesitan corrección.
            </p>
          </div>
          <div class="flex flex-col gap-2 text-sm text-slate-700">
            <span class="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-1">CSV o XLSX</span>
            <span class="inline-flex items-center gap-2 rounded-lg bg-slate-100 px-3 py-1">Validación automática</span>
          </div>
        </div>
        <div class="mt-4 flex flex-wrap gap-2 text-sm">
          <router-link
            to="/admin/courses/import"
            class="inline-flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-slate-700 hover:bg-slate-50"
          >
            <i class="fas fa-book"></i>
            Importar cursos
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
        <div class="space-y-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="text-sm font-semibold text-slate-800">Qué vas a importar</p>
              <p class="text-sm text-slate-600">Selecciona el tipo y sube la plantilla correspondiente.</p>
            </div>
            <p class="text-xs uppercase tracking-[0.25em] text-slate-500">Un solo flujo</p>
          </div>

          <div class="flex flex-wrap gap-3">
            <button
              v-for="option in targetOptions"
              :key="option.value"
              type="button"
              :class="['rounded-full px-4 py-1 text-xs font-semibold uppercase tracking-wide transition border',
                option.value === selectedTarget
                  ? 'border-slate-900 bg-slate-900 text-white shadow-sm'
                  : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50']"
              @click="switchTarget(option.value)"
            >
              {{ option.label }}
            </button>
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

            <div class="flex flex-wrap gap-3">
              <button
                type="submit"
                class="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-5 py-2 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-800 disabled:opacity-60"
                :disabled="importing"
              >
                <i class="fas fa-file-upload" aria-hidden="true"></i>
                <span v-if="!importing">Importar {{ currentTarget.label }}</span>
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
                :href="currentTarget.sample"
                download
                class="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-50"
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
                <p class="text-xs uppercase tracking-[0.15em] text-slate-500">Procesados</p>
                <p class="text-2xl font-semibold text-slate-900">{{ importResult.processedRows }}</p>
              </div>
              <div class="rounded-xl bg-emerald-50 p-3 text-center">
                <p class="text-xs uppercase tracking-[0.15em] text-emerald-600">Importados</p>
                <p class="text-2xl font-semibold text-emerald-700">{{ importResult.created.length }}</p>
              </div>
              <div class="rounded-xl bg-amber-50 p-3 text-center">
                <p class="text-xs uppercase tracking-[0.15em] text-amber-600">Omitidos</p>
                <p class="text-2xl font-semibold text-amber-700">{{ importResult.skippedRows }}</p>
              </div>
            </div>

            <div v-if="createdPreview.length" class="rounded-xl border border-slate-200 bg-white p-4">
              <p class="text-sm font-semibold text-slate-800">Registros creados</p>
              <ul class="mt-2 space-y-1 text-sm text-slate-600">
                <li v-for="entry in createdPreview" :key="entry.institutionalEmail + entry.firstNames">
                  {{ entry.firstNames }} {{ entry.paternalSurname }} {{ entry.maternalSurname }} · {{ entry.institutionalEmail }}
                </li>
              </ul>
              <p v-if="importResult.created.length > createdPreview.length" class="mt-2 text-xs text-slate-500">
                Se registraron {{ importResult.created.length }} registros, se muestran los primeros {{ createdPreview.length }}.
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

        <div class="space-y-5 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="space-y-2">
            <p class="text-xs font-semibold uppercase tracking-[0.3em] text-slate-500">Plantilla {{ currentTarget.label }}</p>
            <p class="text-sm text-slate-600">Orden exacto de columnas para {{ currentTarget.label.toLowerCase() }}.</p>
          </div>
          <div class="flex flex-wrap gap-2 text-sm text-slate-700">
            <span
              v-for="column in currentTarget.columns"
              :key="column"
              class="rounded-lg bg-slate-50 px-3 py-1"
            >
              {{ column }}
            </span>
          </div>
          <div class="rounded-xl border border-slate-100 bg-slate-50 p-4 text-xs text-slate-600">
            <p class="font-semibold text-slate-700">Lo que verificamos</p>
            <ul class="mt-2 space-y-2">
              <li v-for="hint in currentTarget.highlights" :key="hint" class="flex items-start gap-2">
                <span class="mt-1 h-2 w-2 rounded-full bg-slate-700"></span>
                <span>{{ hint }}</span>
              </li>
            </ul>
            <p class="mt-3">La primera fila puede ser un encabezado si incluye palabras como "CUI" o "Apellido".</p>
          </div>
        </div>
      </div>
    </section>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { importUserData, type UserImportEntry, type UserImportResult, type UserImportTarget } from '@/services/userImportService'

const targetOptions = [
  {
    value: 'students' as UserImportTarget,
    label: 'Estudiantes',
    description: 'Registra la identidad completa de los estudiantes con su CUI y correo institucional.',
    accent: 'from-emerald-500 to-emerald-700',
    columns: ['CUI', 'Apellido paterno', 'Apellido materno', 'Nombres', 'Correo institucional', 'Año de ingreso (opcional)'],
    sample: '/sample-data/students-import-sample.csv',
    highlights: [
      'El CUI se limpia de espacios y guiones antes de validar',
      'Se valida el dominio institucional y evita duplicados',
      'El año de ingreso ayuda a estadísticas en la Secretaría'
    ]
  },
  {
    value: 'professors' as UserImportTarget,
    label: 'Profesores',
    description: 'Carga docentes con nombre completo y correo institucional en segundos.',
    accent: 'from-indigo-500 to-sky-600',
    columns: ['Apellido paterno', 'Apellido materno', 'Nombres', 'Correo institucional'],
    sample: '/sample-data/professors-import-sample.csv',
    highlights: [
      'Detecta correos ya registrados y los omite',
      'Los nombres y apellidos se guardan capitalizados',
      'Sirve tanto CSV como XLSX sin cambiar formato'
    ]
  },
  {
    value: 'secretaries' as UserImportTarget,
    label: 'Secretarias',
    description: 'Sincroniza el equipo de secretarias para usuarios administrativos.',
    accent: 'from-amber-500 to-orange-500',
    columns: ['Apellido paterno', 'Apellido materno', 'Nombres', 'Correo institucional'],
    sample: '/sample-data/secretaries-import-sample.csv',
    highlights: [
      'Se bloquean emails duplicados para proteger el directorio',
      'Los nombres se normalizan para mantener consistencia',
      'Todas las secretarias importadas quedan activas inmediatamente'
    ]
  }
]

const selectedTarget = ref<UserImportTarget>('students')
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)
const importing = ref(false)
const importError = ref('')
const importResult = ref<UserImportResult<UserImportEntry> | null>(null)

const currentTarget = computed(() =>
  targetOptions.find((option) => option.value === selectedTarget.value) ?? targetOptions[0]
)

const createdPreview = computed(() => importResult.value?.created.slice(0, 5) ?? [])

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

const switchTarget = (target: UserImportTarget) => {
  if (selectedTarget.value === target) {
    return
  }
  selectedTarget.value = target
  importResult.value = null
  importError.value = ''
  if (fileInput.value) {
    fileInput.value.value = ''
  }
  selectedFile.value = null
}

const handleSubmit = async () => {
  if (!selectedFile.value) {
    importError.value = 'Selecciona un archivo antes de importar'
    return
  }

  importing.value = true
  importError.value = ''

  try {
    importResult.value = await importUserData(selectedTarget.value, selectedFile.value)
  } catch (error) {
    importResult.value = null
    importError.value = error instanceof Error ? error.message : 'No se pudo completar la importación'
  } finally {
    importing.value = false
  }
}
</script>

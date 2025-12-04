<template>
  <AdminLayout>
    <div class="space-y-8">
      <section class="bg-white shadow rounded-lg">
        <div class="px-6 py-4 border-b border-gray-200 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Equipo administrativo</p>
            <h2 class="text-xl font-semibold text-gray-800">Gestión de Secretarias</h2>
            <p class="text-gray-600 mt-1">Administra el personal administrativo</p>
          </div>
          <button
            @click="openCreateSecretaryModal"
            class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
          >
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Agregar secretaria
          </button>
        </div>

        <div class="p-6">
          <div v-if="loading" class="text-center py-8">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p class="text-gray-600 mt-2">Cargando secretarias...</p>
          </div>

          <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
            <p class="text-red-800">{{ error }}</p>
            <button
              @click="fetchSecretaries"
              class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
            >
              Reintentar
            </button>
          </div>

          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-24">ID</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nombres</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Apellidos</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Correo</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr
                  v-for="secretary in secretaries"
                  :key="secretary.userId ?? secretary.institutionalEmail"
                  class="hover:bg-gray-50"
                >
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                    {{ secretary.userId ?? '—' }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {{ secretary.firstNames || 'Sin nombres' }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {{ formatSurnames(secretary) }}
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    <a
                      v-if="secretary.institutionalEmail"
                      :href="`mailto:${secretary.institutionalEmail}`"
                      class="text-blue-600 hover:text-blue-800"
                    >
                      {{ secretary.institutionalEmail }}
                    </a>
                    <span v-else class="text-gray-400">Sin correo</span>
                  </td>
                </tr>
              </tbody>
            </table>

            <div v-if="secretaries.length === 0" class="text-center py-8">
              <p class="text-gray-500">No se encontraron secretarias registradas.</p>
            </div>
          </div>

          <!-- Estadísticas -->
          <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
              <h3 class="text-lg font-semibold text-blue-800">Total de Secretarias</h3>
              <p class="text-3xl font-bold text-blue-600 mt-2">{{ secretaries.length }}</p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <div
      v-if="showCreateSecretaryModal"
      class="fixed inset-0 z-50 flex items-start md:items-center justify-center bg-black/40 p-4 overflow-auto"
    >
      <div class="w-full max-w-2xl rounded-2xl bg-white shadow-2xl max-h-[calc(100vh-2rem)] overflow-y-auto">
        <div class="flex items-start justify-between border-b px-6 py-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Nuevo registro</p>
            <h3 class="text-xl font-semibold text-gray-900">Agregar secretaria</h3>
            <p class="text-sm text-gray-500">Completa los datos básicos para el personal administrativo.</p>
          </div>
          <button
            class="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            @click="closeCreateSecretaryModal"
            :disabled="createSecretaryLoading"
          >
            <span class="sr-only">Cerrar</span>
            ✕
          </button>
        </div>

        <form class="px-6 py-5 space-y-6" @submit.prevent="handleCreateSecretary">
          <div
            v-if="secretaryFormErrors.length || createSecretaryError"
            class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700"
          >
            <p class="font-semibold">Revisa los siguientes puntos:</p>
            <ul v-if="secretaryFormErrors.length" class="mt-2 list-disc pl-5">
              <li v-for="(message, index) in secretaryFormErrors" :key="`secretary-error-${index}`">
                {{ message }}
              </li>
            </ul>
            <p v-if="createSecretaryError" class="mt-2">{{ createSecretaryError }}</p>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <label class="text-sm font-medium text-gray-700">
              Nombres
              <input
                v-model="newSecretaryForm.firstNames"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="María Fernanda"
                :disabled="createSecretaryLoading"
              />
            </label>
            <label class="text-sm font-medium text-gray-700">
              Apellido paterno
              <input
                v-model="newSecretaryForm.paternalSurname"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="Quispe"
                :disabled="createSecretaryLoading"
              />
            </label>
            <label class="text-sm font-medium text-gray-700">
              Apellido materno
              <input
                v-model="newSecretaryForm.maternalSurname"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="Huamán"
                :disabled="createSecretaryLoading"
              />
            </label>
            <label class="text-sm font-medium text-gray-700 md:col-span-2">
              Correo institucional
              <input
                v-model="newSecretaryForm.institutionalEmail"
                type="email"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="secretaria@unsa.edu.pe"
                :disabled="createSecretaryLoading"
              />
            </label>
          </div>

          <div class="flex flex-col gap-2 border-t pt-4 sm:flex-row sm:justify-end">
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
              @click="closeCreateSecretaryModal"
              :disabled="createSecretaryLoading"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
              :disabled="createSecretaryLoading"
            >
              {{ createSecretaryLoading ? 'Guardando...' : 'Registrar secretaria' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import AdminLayout from '../components/ui/TopBar.vue'
import { useSecretaryService } from '../services/secretaryService'

const { secretaries, loading, error, fetchSecretaries, createSecretary } = useSecretaryService()

const showCreateSecretaryModal = ref(false)
const createSecretaryLoading = ref(false)
const createSecretaryError = ref('')
const secretaryFormErrors = ref([])

const createEmptySecretaryForm = () => ({
  firstNames: '',
  paternalSurname: '',
  maternalSurname: '',
  institutionalEmail: ''
})

const newSecretaryForm = ref(createEmptySecretaryForm())

const formatSurnames = (secretary) => {
  const paterno = secretary.paternalSurname?.trim()
  const materno = secretary.maternalSurname?.trim()
  const combined = [paterno, materno].filter(Boolean).join(' ')
  return combined || 'Sin apellidos'
}

const resetSecretaryForm = () => {
  secretaryFormErrors.value = []
  createSecretaryError.value = ''
  newSecretaryForm.value = createEmptySecretaryForm()
}

const openCreateSecretaryModal = () => {
  resetSecretaryForm()
  showCreateSecretaryModal.value = true
}

const closeCreateSecretaryModal = () => {
  if (createSecretaryLoading.value) return
  showCreateSecretaryModal.value = false
  resetSecretaryForm()
}

const validateSecretaryForm = () => {
  const errors = []
  const payload = newSecretaryForm.value

  if (!payload.firstNames.trim()) {
    errors.push('Ingresa los nombres de la secretaria.')
  }
  if (!payload.paternalSurname.trim()) {
    errors.push('Ingresa el apellido paterno.')
  }
  if (!payload.maternalSurname.trim()) {
    errors.push('Ingresa el apellido materno.')
  }
  const email = payload.institutionalEmail.trim()
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(email)) {
    errors.push('Ingresa un correo institucional válido.')
  }

  secretaryFormErrors.value = errors
  return errors.length === 0
}

const handleCreateSecretary = async () => {
  if (!validateSecretaryForm()) {
    return
  }

  createSecretaryLoading.value = true
  createSecretaryError.value = ''

  try {
    const payload = {
      firstNames: newSecretaryForm.value.firstNames.trim(),
      paternalSurname: newSecretaryForm.value.paternalSurname.trim(),
      maternalSurname: newSecretaryForm.value.maternalSurname.trim(),
      institutionalEmail: newSecretaryForm.value.institutionalEmail.trim().toLowerCase()
    }

    await createSecretary(payload)
    resetSecretaryForm()
    showCreateSecretaryModal.value = false
    fetchSecretaries()
  } catch (err) {
    createSecretaryError.value = err instanceof Error ? err.message : 'No se pudo registrar la secretaria'
  } finally {
    createSecretaryLoading.value = false
  }
}

onMounted(() => {
  fetchSecretaries()
})

</script>
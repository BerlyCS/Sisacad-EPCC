<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 class="text-xl font-semibold text-gray-800">Gestión de Estudiantes</h2>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <button
            @click="openCreateStudentModal"
            class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
          >
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Agregar Estudiante
          </button>
          <router-link
            :to="{ path: '/admin/users/import', query: { target: 'students' } }"
            class="inline-flex items-center gap-2 rounded-lg border border-blue-200 px-4 py-2 text-sm font-semibold text-blue-600 hover:bg-blue-50"
          >
            <i class="fas fa-file-import"></i>
            Importar estudiantes
          </router-link>
        </div>
      </div>
      
      <div class="p-6">
        <!-- Controles de ordenamiento (usados por admin y secretaria) -->
        <div class="mb-4 flex flex-wrap gap-4 items-end">
          <div>
            <label class="block text-sm font-medium text-gray-700">Ordenar por</label>
            <select v-model="sortBy" @change="fetchSorted" class="mt-1 block w-48 pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
              <option value="dni">DNI</option>
              <option value="cui">CUI</option>
              <option value="name">Nombre</option>
              <option value="apellidos">Apellidos</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Dirección</label>
            <select v-model="direction" @change="fetchSorted" class="mt-1 block w-48 pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm rounded-md">
              <option value="asc">Ascendente</option>
              <option value="desc">Descendente</option>
            </select>
          </div>
          <button @click="fetchSorted" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition">Aplicar</button>
        </div>
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando estudiantes...</p>
        </div>
        
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button 
            @click="fetchSorted"
            class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
          >
            Reintentar
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 table-fixed">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-24">
                  User ID
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-28">
                  CUI
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombres
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Apellidos
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Correo
                </th>
                <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-32 text-center">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="student in students" :key="student.userId" class="hover:bg-gray-50">
                <td class="px-4 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                  {{ student.userId }}
                </td>
                <td class="px-4 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                  {{ student.cui }}
                </td>
                <td class="px-4 py-4 text-sm text-gray-900 break-words">
                  {{ student.firstNames }}
                </td>
                <td class="px-4 py-4 text-sm text-gray-900 break-words">
                  {{ student.paternalSurname }} {{ student.maternalSurname }}
                </td>
                <td class="px-4 py-4 text-sm text-gray-900 break-words">
                  <a :href="`mailto:${student.institutionalEmail}`" class="text-blue-600 hover:text-blue-800 break-all">
                    {{ student.institutionalEmail }}
                  </a>
                </td>
                <td class="px-4 py-4 text-sm text-gray-900 text-center">
                  <button
                    @click="goToProfile(student)"
                    class="px-3 py-1.5 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
                  >
                    Ver perfil
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="students.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron estudiantes registrados.</p>
          </div>
        </div>

        <!-- Estadísticas -->
        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Estudiantes</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ students.length }}</p>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showCreateStudentModal"
      class="fixed inset-0 z-50 flex items-start md:items-center justify-center bg-black/40 p-4 overflow-auto"
    >
      <div class="w-full max-w-2xl rounded-2xl bg-white shadow-2xl max-h-[calc(100vh-2rem)] overflow-y-auto">
        <div class="flex items-start justify-between border-b px-6 py-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Nuevo estudiante</p>
            <h3 class="text-xl font-semibold text-gray-900">Registrar estudiante</h3>
          </div>
          <button
            class="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            @click="closeCreateStudentModal"
            :disabled="createStudentLoading"
          >
            <span class="sr-only">Cerrar</span>
            ✕
          </button>
        </div>

        <form class="px-6 py-5 space-y-6" @submit.prevent="handleCreateStudent">
          <div v-if="studentFormErrors.length || createStudentError" class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            <p class="font-semibold">Revisa los siguientes campos:</p>
            <ul v-if="studentFormErrors.length" class="mt-2 list-disc pl-5">
              <li v-for="(message, index) in studentFormErrors" :key="`student-error-${index}`">{{ message }}</li>
            </ul>
            <p v-if="createStudentError" class="mt-2">{{ createStudentError }}</p>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <label class="text-sm font-medium text-gray-700">
              Nombres
              <input
                v-model="newStudentForm.firstNames"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="Ivan"
                :disabled="createStudentLoading"
                required
              />
            </label>
            <label class="text-sm font-medium text-gray-700">
              Apellido paterno
              <input
                v-model="newStudentForm.paternalSurname"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="Zegarra"
                :disabled="createStudentLoading"
                required
              />
            </label>
            <label class="text-sm font-medium text-gray-700">
              Apellido materno
              <input
                v-model="newStudentForm.maternalSurname"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="López"
                :disabled="createStudentLoading"
                required
              />
            </label>
            <label class="text-sm font-medium text-gray-700">
              CUI
              <input
                v-model="newStudentForm.cui"
                type="text"
                inputmode="numeric"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="20201234"
                :disabled="createStudentLoading"
                required
              />
            </label>
            <label class="text-sm font-medium text-gray-700 md:col-span-2">
              Correo institucional
              <input
                v-model="newStudentForm.institutionalEmail"
                type="email"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="estudiante@unsa.edu.pe"
                :disabled="createStudentLoading"
                required
              />
            </label>
          </div>

          <div class="flex flex-col gap-2 border-t pt-4 sm:flex-row sm:justify-end">
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
              @click="closeCreateStudentModal"
              :disabled="createStudentLoading"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
              :disabled="createStudentLoading"
            >
              {{ createStudentLoading ? 'Guardando...' : 'Registrar estudiante' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AdminLayout from '../components/ui/TopBar.vue'
import { useStudentService } from '../services/studentService'

const sortBy = ref('dni')
const direction = ref('asc')
const router = useRouter()

const { students, loading, error, fetchStudentsSorted, createStudent } = useStudentService()

const showCreateStudentModal = ref(false)
const createStudentLoading = ref(false)
const createStudentError = ref('')
const studentFormErrors = ref([])

const createEmptyStudentForm = () => ({
  firstNames: '',
  paternalSurname: '',
  maternalSurname: '',
  cui: '',
  institutionalEmail: ''
})

const newStudentForm = ref(createEmptyStudentForm())

const fetchSorted = () => {
  fetchStudentsSorted(sortBy.value, direction.value)
}

const goToProfile = (student) => {
  if (!student?.cui) return
  router.push({ name: 'student-profile', params: { cui: student.cui } })
}

const resetStudentForm = () => {
  studentFormErrors.value = []
  createStudentError.value = ''
  newStudentForm.value = createEmptyStudentForm()
}

const openCreateStudentModal = () => {
  resetStudentForm()
  showCreateStudentModal.value = true
}

const closeCreateStudentModal = () => {
  if (createStudentLoading.value) return
  showCreateStudentModal.value = false
  resetStudentForm()
}

const validateStudentForm = () => {
  const errors = []
  const payload = newStudentForm.value

  if (!payload.firstNames.trim()) {
    errors.push('Ingresa los nombres del estudiante.')
  }
  if (!payload.paternalSurname.trim()) {
    errors.push('Ingresa el apellido paterno.')
  }
  if (!payload.maternalSurname.trim()) {
    errors.push('Ingresa el apellido materno.')
  }
  const cuiDigits = payload.cui.replace(/\D/g, '')
  if (cuiDigits.length < 6) {
    errors.push('El CUI debe tener al menos 6 dígitos.')
  }
  const emailValue = payload.institutionalEmail.trim()
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailPattern.test(emailValue)) {
    errors.push('Ingresa un correo institucional válido.')
  }

  studentFormErrors.value = errors
  return errors.length === 0
}

const handleCreateStudent = async () => {
  if (!validateStudentForm()) {
    return
  }

  createStudentLoading.value = true
  createStudentError.value = ''

  try {
    const payload = {
      firstNames: newStudentForm.value.firstNames.trim(),
      paternalSurname: newStudentForm.value.paternalSurname.trim(),
      maternalSurname: newStudentForm.value.maternalSurname.trim(),
      cui: newStudentForm.value.cui.replace(/\D/g, ''),
      institutionalEmail: newStudentForm.value.institutionalEmail.trim()
    }

    await createStudent(payload)
    resetStudentForm()
    showCreateStudentModal.value = false
    fetchSorted()
  } catch (err) {
    createStudentError.value = err instanceof Error ? err.message : 'No se pudo crear el estudiante'
  } finally {
    createStudentLoading.value = false
  }
}

onMounted(() => {
  fetchSorted()
})
</script>
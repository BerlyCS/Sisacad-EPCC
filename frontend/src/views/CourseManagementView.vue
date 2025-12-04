<template>
  <AdminLayout>
    <div class="bg-white shadow rounded-lg">
      <div class="px-6 py-4 border-b border-gray-200 flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-sm font-semibold text-blue-500 uppercase tracking-wide">Gestión académica</p>
          <h2 class="text-xl font-semibold text-gray-800">Gestión de Cursos</h2>
          <p class="text-gray-600 mt-1">Administra los cursos y asigna docentes responsables.</p>
        </div>
        <div v-if="canAddCourses" class="flex gap-2">
          <button
            @click="openCreateCourseModal"
            class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700"
          >
            <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
            </svg>
            Agregar Curso
          </button>
        </div>
      </div>

      <div class="p-6">
        <div v-if="loading" class="text-center py-8">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p class="text-gray-600 mt-2">Cargando cursos...</p>
        </div>

        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
          <p class="text-red-800">{{ error }}</p>
          <button
            @click="fetchCourses"
            class="mt-2 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
          >
            Reintentar
          </button>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Nombre</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Créditos</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase"># Estudiantes</th>
                <th v-if="canAssignProfessors" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Acciones</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="course in courses" :key="course.courseId" class="hover:bg-gray-50">
                <td class="px-6 py-4 text-sm font-mono text-gray-900">{{ course.courseId }}</td>
                <td class="px-6 py-4 text-sm text-gray-900">{{ course.name }}</td>
                <td class="px-6 py-4 text-sm text-blue-700 font-semibold">
                  {{ course.credits }} créditos
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">
                  {{ course.enrolledStudentIDs?.length || 0 }}
                </td>
                <td v-if="canAssignProfessors" class="px-6 py-4 text-sm">
                  <div class="flex flex-wrap gap-2">
                    <button
                      class="inline-flex items-center gap-2 rounded-lg border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:border-blue-400 hover:text-blue-600"
                      @click="openAssignmentModal(course.courseId)"
                    >
                      Gestionar
                    </button>
                    <button
                      class="inline-flex items-center gap-2 rounded-lg border border-blue-200 px-4 py-2 text-sm font-semibold text-blue-700 bg-blue-50 hover:border-blue-400"
                      @click="openGroupSettingsModal(course.courseId)"
                    >
                      Aperturar grupo
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="courses.length === 0" class="text-center py-8">
            <p class="text-gray-500">No se encontraron cursos registrados.</p>
          </div>
        </div>

        <div class="mt-8 grid grid-cols-1 md:grid-cols-3 gap-6">
          <div class="bg-blue-50 border border-blue-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-blue-800">Total de Cursos</h3>
            <p class="text-3xl font-bold text-blue-600 mt-2">{{ courses.length }}</p>
          </div>
          <div class="bg-green-50 border border-green-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-green-800">Créditos Totales</h3>
            <p class="text-3xl font-bold text-green-600 mt-2">
              {{ courses.reduce((total, course) => total + (course.credits || 0), 0) }}
            </p>
          </div>
          <div class="bg-purple-50 border border-purple-200 rounded-lg p-6">
            <h3 class="text-lg font-semibold text-purple-800">Total Estudiantes Inscritos</h3>
            <p class="text-3xl font-bold text-purple-600 mt-2">
              {{ courses.reduce((total, course) => total + (course.enrolledStudentIDs?.length || 0), 0) }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <div
      v-if="showCreateCourseModal"
      class="fixed inset-0 z-50 flex items-start md:items-center justify-center bg-black/40 p-4 overflow-auto"
    >
      <div class="w-full max-w-3xl rounded-2xl bg-white shadow-2xl max-h-[calc(100vh-2rem)] overflow-y-auto">
        <div class="flex items-start justify-between border-b px-6 py-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Nuevo curso</p>
            <h3 class="text-xl font-semibold text-gray-900">Registrar curso académico</h3>
            <p class="text-sm text-gray-500">Completa los datos básicos para habilitar la matrícula.</p>
          </div>
          <button
            class="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            @click="closeCreateCourseModal"
            :disabled="createCourseLoading"
          >
            <span class="sr-only">Cerrar</span>
            ✕
          </button>
        </div>

        <form class="px-6 py-5 space-y-6" @submit.prevent="handleCreateCourse">
          <div v-if="courseFormErrors.length || createCourseError" class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            <p class="font-semibold">Revisa los siguientes campos:</p>
            <ul class="mt-2 list-disc pl-5">
              <li v-for="(message, index) in courseFormErrors" :key="`course-error-${index}`">{{ message }}</li>
            </ul>
            <p v-if="createCourseError" class="mt-2">{{ createCourseError }}</p>
          </div>

          <section class="rounded-2xl border border-gray-200 p-4 space-y-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Información básica</p>
              <h4 class="text-lg font-semibold text-gray-900">Identificación del curso</h4>
            </div>
            <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
              <label class="text-sm font-medium text-gray-700">
                Código del curso
                <input
                  v-model="newCourseForm.courseCode"
                  type="number"
                  inputmode="numeric"
                  class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="Ej. 1701101"
                  :disabled="createCourseLoading"
                  min="1"
                />
              </label>
              <label class="text-sm font-medium text-gray-700">
                Créditos
                <input
                  v-model="newCourseForm.credits"
                  type="number"
                  inputmode="numeric"
                  class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="Ej. 3"
                  :disabled="createCourseLoading"
                  min="1"
                />
              </label>
              <label class="text-sm font-medium text-gray-700">
                Semestre
                <input
                  v-model="newCourseForm.semesterNumber"
                  type="number"
                  inputmode="numeric"
                  class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="1"
                  :disabled="createCourseLoading"
                  min="1"
                />
              </label>
            </div>
            <label class="text-sm font-medium text-gray-700">
              Nombre del curso
              <input
                v-model="newCourseForm.name"
                type="text"
                class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                placeholder="Ingresa el nombre oficial"
                :disabled="createCourseLoading"
              />
            </label>
          </section>

          <section class="rounded-2xl border border-gray-200 p-4 space-y-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Plan académico</p>
              <h4 class="text-lg font-semibold text-gray-900">Distribución de horas</h4>
            </div>
            <div class="grid grid-cols-1 gap-4 md:grid-cols-3">
              <label class="text-sm font-medium text-gray-700">
                Horas teoría
                <input
                  v-model="newCourseForm.theoryHours"
                  type="number"
                  inputmode="numeric"
                  class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="0"
                  :disabled="createCourseLoading"
                  min="0"
                />
              </label>
              <label class="text-sm font-medium text-gray-700">
                Horas práctica
                <input
                  v-model="newCourseForm.practiceHours"
                  type="number"
                  inputmode="numeric"
                  class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="0"
                  :disabled="createCourseLoading"
                  min="0"
                />
              </label>
              <label class="text-sm font-medium text-gray-700">
                Horas laboratorio
                <input
                  v-model="newCourseForm.labHours"
                  type="number"
                  inputmode="numeric"
                  class="mt-2 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  placeholder="0"
                  :disabled="createCourseLoading"
                  min="0"
                />
              </label>
            </div>
          </section>

          <section class="rounded-2xl border border-gray-200 bg-white/70 p-5 space-y-6 shadow-sm">
            <div class="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
              <div>
                <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Evaluaciones</p>
                <h4 class="text-lg font-semibold text-gray-900">Porcentaje de evaluaciones</h4>
              </div>
              <div class="inline-flex items-center gap-2 rounded-full border px-4 py-1 text-sm font-semibold"
                :class="isTotalWeightBalanced ? 'border-green-200 bg-green-50 text-green-700' : 'border-red-200 bg-red-50 text-red-700'">
                <span>{{ Math.round(totalWeightSum) }}%</span>
                <span class="text-xs text-gray-400">/ 100%</span>
              </div>
            </div>

            <div class="grid gap-4 lg:grid-cols-2">
              <div class="rounded-2xl border border-gray-200 bg-gray-50/60 p-4 space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Evaluaciones continuas</p>
                  </div>
                  <span class="text-xs font-semibold" :class="isTotalWeightBalanced ? 'text-green-600' : 'text-amber-600'">
                    {{ Math.round(continuousWeightSum) }}%
                  </span>
                </div>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
                  <label
                    v-for="(label, index) in continuousWeightFieldLabels"
                    :key="`continuous-${index}`"
                    class="text-sm font-medium text-gray-700"
                  >
                    {{ label }}
                    <input
                      v-model="newCourseForm.continuousWeights[index]"
                      type="number"
                      min="0"
                      max="100"
                      step="1"
                      class="mt-1 w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      placeholder="0"
                      :disabled="createCourseLoading"
                    />
                  </label>
                </div>
              </div>

              <div class="rounded-2xl border border-gray-200 bg-gray-50/60 p-4 space-y-4">
                <div class="flex items-center justify-between">
                  <div>
                    <p class="text-xs font-semibold uppercase tracking-wide text-amber-500">Exámenes</p>
                  </div>
                  <span class="text-xs font-semibold" :class="isTotalWeightBalanced ? 'text-green-600' : 'text-amber-600'">
                    {{ Math.round(examWeightSum) }}%
                  </span>
                </div>
                <div class="grid grid-cols-1 gap-3 md:grid-cols-3">
                  <label
                    v-for="(label, index) in examWeightFieldLabels"
                    :key="`exam-${index}`"
                    class="text-sm font-medium text-gray-700"
                  >
                    {{ label }}
                    <input
                      v-model="newCourseForm.examWeights[index]"
                      type="number"
                      min="0"
                      max="100"
                      step="1"
                      class="mt-1 w-full rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                      placeholder="0"
                      :disabled="createCourseLoading"
                    />
                  </label>
                </div>
              </div>
            </div>
            <p class="text-xs text-gray-500">La suma conjunta de evaluaciones continuas y exámenes debe ser 100%.</p>
          </section>

          <section class="rounded-2xl border border-gray-200 p-4 space-y-4">
            <div>
              <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Documentación</p>
              <h4 class="text-lg font-semibold text-gray-900">Sílabo (opcional)</h4>
              <p class="text-sm text-gray-500">Adjunta un PDF de hasta 10MB para completar el registro.</p>
            </div>
            <div class="flex flex-col gap-3 md:flex-row md:items-center md:gap-6">
              <label class="flex-1 cursor-pointer rounded-2xl border-2 border-dashed border-gray-300 px-6 py-4 text-center hover:border-blue-400">
                <span class="text-sm font-medium text-blue-600">Seleccionar archivo PDF</span>
                <input
                  ref="syllabusFileRef"
                  type="file"
                  accept=".pdf"
                  class="sr-only"
                  @change="handleSyllabusFileChange"
                />
              </label>
              <div class="flex flex-1 flex-wrap items-center gap-2 text-sm text-gray-600">
                <span class="font-medium">{{ selectedSyllabusFileName }}</span>
                <button
                  v-if="selectedSyllabusFile"
                  type="button"
                  class="text-xs font-semibold text-red-600 hover:text-red-700"
                  @click="clearSyllabusSelection"
                >
                  Quitar
                </button>
              </div>
            </div>
            <p class="text-xs text-gray-500">La carga del archivo se completará una vez registrado el curso.</p>
          </section>

          <div class="flex flex-col gap-2 border-t pt-4 sm:flex-row sm:justify-end">
            <button
              type="button"
              class="rounded-xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
              @click="closeCreateCourseModal"
              :disabled="createCourseLoading"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
              :disabled="createCourseLoading"
            >
              {{ createCourseLoading ? 'Guardando...' : 'Registrar curso' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <div
      v-if="showAssignmentModal && selectedCourse"
      class="fixed inset-0 z-50 flex items-start md:items-center justify-center bg-black/40 p-4 overflow-auto"
    >
      <div class="w-full max-w-2xl rounded-2xl bg-white shadow-2xl max-h-[calc(100vh-2rem)] overflow-y-auto">
        <div class="flex items-start justify-between border-b px-6 py-4">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-blue-500">Gestión de docentes</p>
            <h3 class="text-xl font-semibold text-gray-900">{{ selectedCourse.name }}</h3>
            <p class="text-sm text-gray-500">
              {{ selectedCourse.credits }} créditos
            </p>
          </div>
          <button
            class="rounded-full p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
            @click="closeAssignmentModal"
          >
            <span class="sr-only">Cerrar</span>
            ✕
          </button>
        </div>

        <div class="px-6 py-5 space-y-6">
          <div class="flex flex-wrap items-center gap-2 border-b pb-3 text-sm font-semibold">
            <button
              class="rounded-full px-4 py-2"
              :class="assignmentActiveTab === 'professors' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600'"
              @click="assignmentActiveTab = 'professors'"
            >
              Docentes
            </button>
            <button
              class="rounded-full px-4 py-2"
              :class="assignmentActiveTab === 'settings' ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600'"
              @click="assignmentActiveTab = 'settings'"
            >
              Configuración de grupos
            </button>
          </div>

          <div v-if="assignmentActiveTab === 'professors'" class="space-y-6">
            <div>
              <h4 class="text-sm font-semibold text-gray-700">Selecciona el grupo y el tipo</h4>
              <p class="text-xs text-gray-500">Los docentes se asignan individualmente por grupo y solo puede haber uno por grupo.</p>
              <p v-if="courseGroupsLoading" class="mt-3 text-sm text-gray-500">Cargando grupos...</p>
              <p v-else-if="courseGroupsError" class="mt-3 text-sm text-red-600">{{ courseGroupsError }}</p>
              <p v-else-if="courseGroups.length === 0" class="mt-3 text-sm text-gray-500">
                Este curso aún no tiene grupos configurados.
              </p>
              <div v-else class="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
                <button
                  v-for="group in courseGroups"
                  :key="group.groupId"
                  type="button"
                  class="rounded-xl border px-4 py-3 text-left transition"
                  :class="[
                    group.groupId === selectedGroupId
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-200 hover:border-blue-300'
                  ]"
                  @click="selectedGroupId = group.groupId"
                >
                  <p class="text-sm font-semibold text-gray-900">
                    {{ group.typeLabel }} {{ group.letter }}
                  </p>
                  <p class="text-xs text-gray-500">
                    {{ group.teacherId ? 'Docente asignado' : 'Sin docente asignado' }}
                  </p>
                </button>
              </div>
            </div>

            <div v-if="selectedGroup" class="space-y-6 border-t pt-4">
              <div>
                <h4 class="text-sm font-semibold text-gray-700">
                  Docente asignado para {{ selectedGroupLabel }}
                </h4>
                <div v-if="assignedProfessors.length > 0" class="mt-3 space-y-3">
                  <div
                    v-for="professor in assignedProfessors"
                    :key="professor.userId"
                    class="flex items-center justify-between rounded-xl border border-gray-200 px-4 py-3"
                  >
                    <div>
                      <p class="font-semibold text-gray-900">{{ professorFullName(professor) }}</p>
                      <p class="text-xs text-gray-500">{{ professor.institutionalEmail }}</p>
                    </div>
                    <button
                      class="text-sm font-medium text-red-600 hover:text-red-700"
                      :disabled="assignmentLoading"
                      @click="handleRemoveProfessor(professor.userId)"
                    >
                      Quitar
                    </button>
                  </div>
                </div>
                <p v-else class="mt-3 text-sm text-gray-500">
                  Este grupo aún no tiene docentes asignados.
                </p>
              </div>

              <div>
                <h4 class="text-sm font-semibold text-gray-700">Asignar nuevo docente</h4>
                <div class="mt-2 space-y-3">
                  <select
                    v-model.number="selectedProfessorId"
                    class="w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                    :disabled="selectedGroup?.teacherId != null || availableProfessors.length === 0 || professorsLoading || assignmentLoading"
                  >
                    <option :value="null" disabled>Selecciona un docente</option>
                    <option
                      v-for="professor in availableProfessors"
                      :key="professor.userId"
                      :value="professor.userId"
                    >
                      {{ professorFullName(professor) }}
                    </option>
                  </select>
                  <p v-if="selectedGroup?.teacherId" class="text-xs text-gray-500">
                    Quita el docente actual para poder asignar uno nuevo.
                  </p>
                  <p v-else-if="availableProfessors.length === 0" class="text-xs text-gray-500">
                    No hay docentes disponibles para este curso.
                  </p>
                  <button
                    class="w-full rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
                    :disabled="assignmentLoading || !selectedProfessorId || availableProfessors.length === 0 || selectedGroup?.teacherId != null"
                    @click="handleAssignProfessor"
                  >
                    {{ assignmentLoading ? 'Guardando...' : 'Asignar docente' }}
                  </button>
                </div>
              </div>
            </div>

            <p v-if="assignmentError" class="text-sm text-red-600">{{ assignmentError }}</p>
          </div>

          <div v-else class="space-y-6">
            <div class="grid gap-4 sm:grid-cols-3">
              <div
                v-for="type in courseTypeOrder"
                :key="type"
                class="rounded-xl border border-gray-200 p-4"
              >
                <p class="text-xs uppercase tracking-wide text-gray-500">Horas {{ courseTypeLabels[type] }}</p>
                <p class="text-2xl font-semibold text-gray-800">
                  {{ courseHourSummary[type] || 0 }}
                </p>
              </div>
            </div>

            <div class="rounded-2xl border border-gray-200 p-4 space-y-4">
              <div class="grid gap-4 md:grid-cols-3">
                <label class="text-sm font-medium text-gray-700">
                  Letra del grupo
                  <input
                    v-model="groupForm.letter"
                    type="text"
                    class="mt-1 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm uppercase focus:border-blue-500 focus:outline-none"
                    maxlength="2"
                    placeholder="A"
                  />
                </label>
                <label class="text-sm font-medium text-gray-700">
                  Tipo de sesión
                  <select
                    v-model="groupForm.type"
                    class="mt-1 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  >
                    <option value="THEORY">Teoría</option>
                    <option value="PRACTICE">Práctica</option>
                    <option value="LAB">Laboratorio</option>
                  </select>
                </label>
                <label class="text-sm font-medium text-gray-700">
                  Capacidad máxima
                  <input
                    v-model="groupForm.capacity"
                    type="number"
                    min="1"
                    class="mt-1 w-full rounded-xl border border-gray-200 px-4 py-2 text-sm focus:border-blue-500 focus:outline-none"
                    placeholder="30"
                  />
                </label>
              </div>
              <p class="text-xs text-gray-500">Cada bloque representa 50 minutos continuos. Combina los bloques para cumplir con las horas del curso.</p>
            </div>

            <div class="rounded-2xl border border-gray-200 p-4 space-y-4">
              <div class="flex items-center justify-between">
                <h4 class="text-sm font-semibold text-gray-700">Horario y aula</h4>
                <button
                  type="button"
                  class="rounded-full border border-dashed border-blue-400 px-4 py-1 text-sm font-medium text-blue-600 hover:bg-blue-50"
                  @click="addScheduleSlot"
                >
                  Agregar bloque (50 min)
                </button>
              </div>
              <div v-if="courseTimeSlotsLoading || classroomsLoading" class="text-sm text-gray-500">
                Preparando opciones de horario...
              </div>
              <div v-else-if="courseTimeSlotsError || classroomsError" class="text-sm text-red-600">
                {{ courseTimeSlotsError || classroomsError }}
              </div>
              <div v-else class="space-y-3">
                <div
                  v-for="slot in groupScheduleSlots"
                  :key="slot.id"
                  class="grid gap-3 rounded-xl border border-gray-200 p-3 md:grid-cols-[1fr,1fr,1fr,auto]"
                >
                  <select
                    v-model="slot.dayOfWeek"
                    class="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  >
                    <option
                      v-for="day in dayOptions"
                      :key="day.value"
                      :value="day.value"
                    >
                      {{ day.label }}
                    </option>
                  </select>
                  <select
                    v-model="slot.timeRange"
                    class="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                    @change="onSlotTimeSelect(slot, $event)"
                  >
                    <option :value="null" disabled>Selecciona un bloque</option>
                    <option
                      v-for="timeSlot in courseTimeSlots"
                      :key="timeKeyForSlot(timeSlot.startTime, timeSlot.endTime)"
                      :value="timeKeyForSlot(timeSlot.startTime, timeSlot.endTime)"
                    >
                      {{ timeSlot.label }}
                    </option>
                  </select>
                  <select
                    v-model.number="slot.classroomId"
                    class="rounded-xl border border-gray-200 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none"
                  >
                    <option :value="null">Sin aula</option>
                    <option v-for="room in classrooms" :key="room.id" :value="room.id">
                      {{ room.name }} · {{ room.location }}
                    </option>
                  </select>
                  <button
                    type="button"
                    class="rounded-full border border-red-200 px-3 py-2 text-sm text-red-600 hover:bg-red-50"
                    @click="removeScheduleSlot(slot.id)"
                  >
                    Eliminar
                  </button>
                </div>
                <p v-if="groupScheduleSlots.length === 0" class="text-sm text-gray-500">
                  Añade al menos un bloque para definir el horario del grupo.
                </p>
              </div>
            </div>

            <div class="rounded-2xl border border-gray-200 p-4 space-y-2">
              <p class="text-sm font-semibold text-gray-700">Resumen del plan</p>
              <p class="text-sm text-gray-600">
                Bloques planificados: <span class="font-semibold text-gray-900">{{ plannedBlocks }}</span> · Equivalente a
                <span class="font-semibold text-gray-900">{{ plannedHoursDisplay }} h</span>
              </p>
              <p v-if="remainingMinutes > 0" class="text-sm text-amber-600">
                Faltan aproximadamente {{ remainingBlocks }} bloque(s) ({{ Math.ceil(remainingMinutes / 10) * 10 }} minutos) para completar las horas de {{ courseTypeLabels[groupForm.type] }}.
              </p>
              <p v-else class="text-sm text-green-600">
                Has cubierto las horas requeridas para {{ courseTypeLabels[groupForm.type] }}.
              </p>
            </div>

            <div v-if="groupCreationErrors.length" class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              <p class="font-semibold">Revisa la configuración:</p>
              <ul class="mt-2 list-disc pl-5">
                <li v-for="(message, index) in groupCreationErrors" :key="`group-error-${index}`">{{ message }}</li>
              </ul>
            </div>

            <div class="flex flex-col gap-2 border-t pt-4 sm:flex-row sm:justify-end">
              <button
                type="button"
                class="rounded-xl border border-gray-200 px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50"
                @click="resetGroupSetup"
                :disabled="createGroupLoading"
              >
                Limpiar
              </button>
              <button
                type="button"
                class="rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:bg-blue-300"
                :disabled="createGroupLoading"
                @click="handleCreateGroup"
              >
                {{ createGroupLoading ? 'Creando...' : 'Crear grupo' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { storeToRefs } from 'pinia'
import AdminLayout from '../components/ui/TopBar.vue'
import { useCourseService } from '../services/courseService'
import { useProfessorService } from '@/services/professorService'
import { useClassroomService } from '@/services/classroomService'
import { useAuthStore } from '@/stores/auth'
import type { Professor } from '@/services/professorService'
import type { CourseType } from '../services/courseService'

const authStore = useAuthStore()
const { isAdmin, isSecretary } = storeToRefs(authStore)

const {
  courses,
  loading,
  error,
  fetchCourses,
  courseGroups,
  courseGroupsLoading,
  courseGroupsError,
  fetchCourseGroups,
  assignProfessorToCourse,
  removeProfessorFromCourse,
  createCourse,
  createCourseGroup,
  courseTimeSlots,
  courseTimeSlotsLoading,
  courseTimeSlotsError,
  fetchCourseTimeSlots
} = useCourseService()

const {
  professors,
  loading: professorsLoading,
  error: professorsError,
  fetchProfessors
} = useProfessorService()

const {
  classrooms,
  loading: classroomsLoading,
  error: classroomsError,
  fetchClassrooms
} = useClassroomService()

const showAssignmentModal = ref(false)
const selectedCourseId = ref<number | null>(null)
const selectedProfessorId = ref<number | null>(null)
const selectedGroupId = ref<number | null>(null)
const assignmentError = ref('')
const assignmentLoading = ref(false)
const showCreateCourseModal = ref(false)
const createCourseLoading = ref(false)
const createCourseError = ref('')
const courseFormErrors = ref<string[]>([])
const syllabusFileRef = ref<HTMLInputElement | null>(null)
const selectedSyllabusFile = ref<File | null>(null)
const newCourseForm = reactive({
  courseCode: '',
  name: '',
  credits: '',
  theoryHours: '',
  practiceHours: '',
  labHours: '',
  semesterNumber: '',
  continuousWeights: ['', '', ''] as string[],
  examWeights: ['', '', ''] as string[]
})
const continuousWeightFieldLabels = ['Evaluación continua 1', 'Evaluación continua 2', 'Evaluación continua 3']
const examWeightFieldLabels = ['Examen 1', 'Examen 2', 'Examen 3']
const sanitizePercentageInput = (value: string) => {
  if (value == null) {
    return 0
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : 0
}
const sumWeightValues = (values: string[]) => values.reduce((total, value) => total + sanitizePercentageInput(value), 0)
const continuousWeightSum = computed(() => sumWeightValues(newCourseForm.continuousWeights))
const examWeightSum = computed(() => sumWeightValues(newCourseForm.examWeights))
const totalWeightSum = computed(() => continuousWeightSum.value + examWeightSum.value)
const isContinuousWeightBalanced = computed(() => Math.round(continuousWeightSum.value) === 100)
const isExamWeightBalanced = computed(() => Math.round(examWeightSum.value) === 100)
const isTotalWeightBalanced = computed(() => Math.round(totalWeightSum.value) === 100)
const selectedSyllabusFileName = computed(() => selectedSyllabusFile.value?.name ?? 'Ningún archivo seleccionado')
const handleSyllabusFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement | null
  selectedSyllabusFile.value = input?.files?.[0] ?? null
}
const clearSyllabusSelection = () => {
  selectedSyllabusFile.value = null
  if (syllabusFileRef.value) {
    syllabusFileRef.value.value = ''
  }
}
const weightPayloadFrom = (values: string[]) => values.map(value => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) {
    return 0
  }
  return Math.max(0, Math.min(100, Math.round(parsed)))
})
const validateWeightSet = (values: string[], label: string) => {
  const issues: string[] = []
  const parsedValues: number[] = []
  values.forEach((value, index) => {
    if (value === '' || value == null) {
      issues.push(`El porcentaje ${index + 1} de ${label} es obligatorio.`)
      parsedValues.push(0)
      return
    }
    const parsed = Number(value)
    if (!Number.isFinite(parsed) || parsed < 0 || parsed > 100) {
      issues.push(`El porcentaje ${index + 1} de ${label} debe estar entre 0 y 100.`)
    }
    parsedValues.push(Number.isFinite(parsed) ? parsed : 0)
  })
  const sum = parsedValues.reduce((total, value) => total + value, 0)
  if (Math.round(sum) !== 100) {
    issues.push(`La suma de los porcentajes de ${label} debe ser 100%. Actualmente es ${sum.toFixed(1)}%.`)
  }
  return issues
}

const validateTotalWeights = (continuousValues: string[], examValues: string[]) => {
  const issues: string[] = []
  const allValues = [...continuousValues, ...examValues]
  const parsedValues: number[] = []
  allValues.forEach((value, index) => {
    if (value === '' || value == null) {
      const label = index < 3 ? 'las evaluaciones continuas' : 'los exámenes'
      const num = index < 3 ? index + 1 : index - 2
      issues.push(`El porcentaje ${num} de ${label} es obligatorio.`)
      parsedValues.push(0)
      return
    }
    const parsed = Number(value)
    if (!Number.isFinite(parsed) || parsed < 0 || parsed > 100) {
      const label = index < 3 ? 'las evaluaciones continuas' : 'los exámenes'
      const num = index < 3 ? index + 1 : index - 2
      issues.push(`El porcentaje ${num} de ${label} debe estar entre 0 y 100.`)
    }
    parsedValues.push(Number.isFinite(parsed) ? parsed : 0)
  })
  const sum = parsedValues.reduce((total, value) => total + value, 0)
  if (Math.round(sum) !== 100) {
    issues.push(`La suma total de los porcentajes de evaluaciones continuas y exámenes debe ser 100%. Actualmente es ${sum.toFixed(1)}%.`)
  }
  return issues
}

const assignmentActiveTab = ref<'professors' | 'settings'>('professors')
const groupCreationErrors = ref<string[]>([])
const createGroupLoading = ref(false)
const scheduleBlockMinutes = 50
const courseTypeLabels: Record<CourseType, string> = {
  THEORY: 'Teoría',
  PRACTICE: 'Práctica',
  LAB: 'Laboratorio'
}
const courseTypeOrder: CourseType[] = ['THEORY', 'PRACTICE', 'LAB']

type GroupScheduleSlotForm = {
  id: string
  dayOfWeek: string
  timeRange: string | null
  startTime: string
  endTime: string
  classroomId: number | null
}

const groupForm = reactive({
  letter: '',
  type: 'THEORY' as CourseType,
  capacity: ''
})

const groupScheduleSlots = ref<GroupScheduleSlotForm[]>([])

const dayOptions = [
  { value: 'LUNES', label: 'Lunes' },
  { value: 'MARTES', label: 'Martes' },
  { value: 'MIERCOLES', label: 'Miércoles' },
  { value: 'JUEVES', label: 'Jueves' },
  { value: 'VIERNES', label: 'Viernes' },
  { value: 'SABADO', label: 'Sábado' }
]

const schedulingAssetsLoaded = reactive({
  classrooms: false,
  timeSlots: false
})

const professorDirectory = computed(() => {
  const directory = new Map<number, Professor>()
  professors.value.forEach(professor => {
    directory.set(professor.userId, professor)
  })
  return directory
})

const selectedCourse = computed(() => {
  if (selectedCourseId.value == null) {
    return null
  }
  return courses.value.find(course => course.courseId === selectedCourseId.value) ?? null
})

const selectedGroup = computed(() => {
  if (!selectedGroupId.value) {
    return null
  }
  return courseGroups.value.find(group => group.groupId === selectedGroupId.value) ?? null
})

const assignedProfessorIds = computed(() => {
  const teacherId = selectedGroup.value?.teacherId
  return teacherId ? [teacherId] : []
})

const selectedGroupLabel = computed(() => {
  if (!selectedGroup.value) {
    return ''
  }
  return `${selectedGroup.value.typeLabel} ${selectedGroup.value.letter}`
})

const assignedProfessors = computed(() => {
  if (!professorDirectory.value) {
    return []
  }
  return assignedProfessorIds.value
    .map(id => professorDirectory.value.get(id))
    .filter((professor): professor is Professor => Boolean(professor))
})

const availableProfessors = computed(() => {
  if (!professors.value.length || !selectedGroup.value) {
    return []
  }
  const assignedIds = new Set(assignedProfessorIds.value)
  if (!assignedIds.size) {
    return professors.value
  }
  return professors.value.filter(professor => !assignedIds.has(professor.userId))
})

const canAssignProfessors = computed(() => isAdmin.value || isSecretary.value)
const canAddCourses = computed(() => isAdmin.value || isSecretary.value)

const professorFullName = (professor: Professor) => {
  return [professor.firstNames, professor.paternalSurname, professor.maternalSurname]
    .filter(Boolean)
    .join(' ')
}

const suggestedGroupLetter = computed(() => {
  // Suggest a letter that is not already used for the currently selected group type
  const usedLetters = new Set(
    courseGroups.value
      .filter(group => group.type === groupForm.type)
      .map(group => group.letter?.trim().toUpperCase())
      .filter(Boolean) as string[]
  )
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')
  const next = alphabet.find(letter => !usedLetters.has(letter))
  return next ?? `GRP${courseGroups.value.length + 1}`
})

const resetGroupSetup = () => {
  groupForm.letter = ''
  groupForm.type = 'THEORY'
  groupForm.capacity = ''
  groupScheduleSlots.value = []
  groupCreationErrors.value = []
}

const generateSlotId = () => `slot-${Date.now()}-${Math.random().toString(36).slice(2, 7)}`

const createEmptySlot = (): GroupScheduleSlotForm => ({
  id: generateSlotId(),
  dayOfWeek: dayOptions[0]?.value ?? 'LUNES',
  timeRange: null,
  startTime: '',
  endTime: '',
  classroomId: null
})

const addScheduleSlot = () => {
  groupScheduleSlots.value.push(createEmptySlot())
}

const removeScheduleSlot = (slotId: string) => {
  groupScheduleSlots.value = groupScheduleSlots.value.filter(slot => slot.id !== slotId)
}

const timeKeyForSlot = (start: string, end: string) => `${start}|${end}`

const handleSlotTimeChange = (slot: GroupScheduleSlotForm, timeKey: string | null) => {
  slot.timeRange = timeKey
  if (!timeKey) {
    slot.startTime = ''
    slot.endTime = ''
    return
  }
  const [start, end] = timeKey.split('|')
  slot.startTime = start ?? ''
  slot.endTime = end ?? ''
}

const onSlotTimeSelect = (slot: GroupScheduleSlotForm, event: Event) => {
  const target = event.target as HTMLSelectElement | null
  const value = target?.value ?? null
  handleSlotTimeChange(slot, value)
}

const ensureScheduleSlotPresence = () => {
  if (groupScheduleSlots.value.length === 0) {
    addScheduleSlot()
  }
}

const ensureSchedulingAssets = async () => {
  const tasks: Promise<unknown>[] = []
  if (!schedulingAssetsLoaded.classrooms) {
    tasks.push(
      fetchClassrooms()
        .then(() => {
          schedulingAssetsLoaded.classrooms = true
        })
        .catch(() => {
          schedulingAssetsLoaded.classrooms = false
        })
    )
  }
  if (!schedulingAssetsLoaded.timeSlots) {
    tasks.push(
      fetchCourseTimeSlots()
        .then(() => {
          schedulingAssetsLoaded.timeSlots = true
        })
        .catch(() => {
          schedulingAssetsLoaded.timeSlots = false
        })
    )
  }
  if (tasks.length) {
    await Promise.all(tasks)
  }
}

const courseHourSummary = computed<Record<CourseType, number>>(() => {
  const course = selectedCourse.value
  return {
    THEORY: Number(course?.theoryHours ?? 0),
    PRACTICE: Number(course?.practiceHours ?? 0),
    LAB: Number(course?.labHours ?? 0)
  }
})

const activeTypeHours = computed(() => courseHourSummary.value[groupForm.type] ?? 0)

const validScheduleSlots = computed(() =>
  groupScheduleSlots.value.filter(slot => Boolean(slot.dayOfWeek && slot.startTime && slot.endTime))
)

const plannedBlocks = computed(() => validScheduleSlots.value.length)
const plannedMinutes = computed(() => plannedBlocks.value * scheduleBlockMinutes)
const plannedHoursDisplay = computed(() => (plannedMinutes.value / 60).toFixed(2))
const remainingMinutes = computed(() => Math.max(activeTypeHours.value * 50 - plannedMinutes.value, 0))
const remainingBlocks = computed(() => (remainingMinutes.value <= 0 ? 0 : Math.ceil(remainingMinutes.value / scheduleBlockMinutes)))

const openAssignmentModal = async (courseId: number) => {
  selectedCourseId.value = courseId
  assignmentError.value = ''
  showAssignmentModal.value = true
  assignmentActiveTab.value = 'professors'
  selectedGroupId.value = null
  resetGroupSetup()
  await fetchCourseGroups(courseId)
  if (courseGroups.value.length > 0) {
    const firstGroup = courseGroups.value[0]
    selectedGroupId.value = firstGroup ? firstGroup.groupId : null
  }
  await ensureSchedulingAssets()
}

const closeAssignmentModal = () => {
  showAssignmentModal.value = false
  selectedCourseId.value = null
  selectedGroupId.value = null
  assignmentError.value = ''
  assignmentActiveTab.value = 'professors'
  resetGroupSetup()
}

const openGroupSettingsModal = async (courseId: number) => {
  await openAssignmentModal(courseId)
  assignmentActiveTab.value = 'settings'
  await ensureSchedulingAssets()
  ensureScheduleSlotPresence()
  if (!groupForm.letter) {
    groupForm.letter = suggestedGroupLetter.value
  }
}

const handleAssignProfessor = async () => {
  if (!selectedCourse.value || !selectedGroupId.value || !selectedProfessorId.value) {
    return
  }

  assignmentLoading.value = true
  assignmentError.value = ''

  try {
    await assignProfessorToCourse(selectedCourse.value.courseId, selectedGroupId.value, selectedProfessorId.value)
    await fetchCourseGroups(selectedCourse.value.courseId)
    selectedProfessorId.value = null
  } catch (err) {
    assignmentError.value = err instanceof Error ? err.message : 'No se pudo asignar el docente'
  } finally {
    assignmentLoading.value = false
  }
}

const handleRemoveProfessor = async (professorId: number) => {
  if (!selectedCourse.value || !selectedGroupId.value) {
    return
  }

  assignmentLoading.value = true
  assignmentError.value = ''

  try {
    await removeProfessorFromCourse(selectedCourse.value.courseId, selectedGroupId.value, professorId)
    await fetchCourseGroups(selectedCourse.value.courseId)
  } catch (err) {
    assignmentError.value = err instanceof Error ? err.message : 'No se pudo actualizar el curso'
  } finally {
    assignmentLoading.value = false
  }
}

const handleCreateGroup = async () => {
  if (!selectedCourse.value) {
    groupCreationErrors.value = ['Selecciona un curso válido antes de crear un grupo.']
    return
  }

  const errors: string[] = []
  const normalizedLetter = groupForm.letter.trim().toUpperCase()
  if (!normalizedLetter) {
    errors.push('Define una letra para el grupo (Ej. A, B, C).')
  } else if (courseGroups.value.some(group => group.letter?.toUpperCase() === normalizedLetter && group.type === groupForm.type)) {
    // Only treat as duplicate when the same letter is already used for the same session type
    errors.push(`La letra ${normalizedLetter} ya está en uso para este curso.`)
  }

  const capacity = Number(groupForm.capacity)
  if (!Number.isFinite(capacity) || capacity <= 0) {
    errors.push('La capacidad debe ser un número mayor a cero.')
  }

  if (!validScheduleSlots.value.length) {
    errors.push('Agrega al menos un bloque horario de 50 minutos.')
  }

  groupCreationErrors.value = errors
  if (errors.length) {
    return
  }

  createGroupLoading.value = true
  try {
    await createCourseGroup(selectedCourse.value.courseId, {
      letter: normalizedLetter,
      type: groupForm.type,
      capacity,
      scheduleSlots: validScheduleSlots.value.map(slot => ({
        dayOfWeek: slot.dayOfWeek,
        startTime: slot.startTime,
        endTime: slot.endTime,
        classroomId: slot.classroomId
      }))
    })

    await fetchCourseGroups(selectedCourse.value.courseId)
    resetGroupSetup()
    groupForm.letter = suggestedGroupLetter.value
    assignmentActiveTab.value = 'professors'
  } catch (err) {
    const message = err instanceof Error ? err.message : 'No se pudo crear el grupo'
    groupCreationErrors.value = [message]
  } finally {
    createGroupLoading.value = false
  }
}

const resetCourseForm = () => {
  newCourseForm.courseCode = ''
  newCourseForm.name = ''
  newCourseForm.credits = ''
  newCourseForm.theoryHours = ''
  newCourseForm.practiceHours = ''
  newCourseForm.labHours = ''
  newCourseForm.semesterNumber = ''
  newCourseForm.continuousWeights = ['', '', '']
  newCourseForm.examWeights = ['', '', '']
  clearSyllabusSelection()
}

const openCreateCourseModal = () => {
  resetCourseForm()
  courseFormErrors.value = []
  createCourseError.value = ''
  showCreateCourseModal.value = true
}

const closeCreateCourseModal = () => {
  showCreateCourseModal.value = false
  createCourseError.value = ''
  courseFormErrors.value = []
}

const parseNonNegativeNumber = (value: string) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : 0
}

const validateCourseForm = () => {
  const errors: string[] = []
  const code = Number(newCourseForm.courseCode)
  if (!Number.isFinite(code) || code <= 0) {
    errors.push('Ingresa un código de curso válido mayor a cero.')
  }

  if (!newCourseForm.name.trim()) {
    errors.push('El nombre del curso es obligatorio.')
  }

  const credits = Number(newCourseForm.credits)
  if (!Number.isFinite(credits) || credits <= 0) {
    errors.push('Los créditos deben ser un número positivo.')
  }

  const theory = parseNonNegativeNumber(newCourseForm.theoryHours)
  const practice = parseNonNegativeNumber(newCourseForm.practiceHours)
  const lab = parseNonNegativeNumber(newCourseForm.labHours)
  if (theory + practice + lab === 0) {
    errors.push('Define al menos una hora de teoría, práctica o laboratorio.')
  }

  if (newCourseForm.semesterNumber) {
    const semester = Number(newCourseForm.semesterNumber)
    if (!Number.isFinite(semester) || semester <= 0) {
      errors.push('El semestre debe ser mayor a cero o puede dejarse vacío.')
    }
  }

  errors.push(...validateTotalWeights(newCourseForm.continuousWeights, newCourseForm.examWeights))

  courseFormErrors.value = errors
  return errors.length === 0
}

const handleCreateCourse = async () => {
  if (!validateCourseForm()) {
    return
  }

  createCourseLoading.value = true
  createCourseError.value = ''

  const payload = {
    courseCode: Number(newCourseForm.courseCode),
    name: newCourseForm.name.trim(),
    credits: Number(newCourseForm.credits),
    theoryHours: parseNonNegativeNumber(newCourseForm.theoryHours),
    practiceHours: parseNonNegativeNumber(newCourseForm.practiceHours),
    labHours: parseNonNegativeNumber(newCourseForm.labHours),
    semesterNumber: newCourseForm.semesterNumber
      ? Number(newCourseForm.semesterNumber)
      : null,
    continuousGradeWeights: weightPayloadFrom(newCourseForm.continuousWeights),
    examGradeWeights: weightPayloadFrom(newCourseForm.examWeights)
  }

  try {
    const createdCourse = await createCourse(payload)

    if (selectedSyllabusFile.value && createdCourse?.courseId) {
      // TODO: Integrar carga de sílabo cuando el servicio esté disponible
    }
    await fetchCourses()
    closeCreateCourseModal()
  } catch (err) {
    createCourseError.value = err instanceof Error ? err.message : 'No se pudo registrar el curso'
  } finally {
    createCourseLoading.value = false
  }
}

watch(courseGroups, groups => {
  if (!groups.length) {
    selectedGroupId.value = null
  } else if (!groups.some(group => group.groupId === selectedGroupId.value)) {
    const firstGroup = groups[0]
    selectedGroupId.value = firstGroup ? firstGroup.groupId : null
  }
  if (assignmentActiveTab.value === 'settings' && !groupForm.letter) {
    groupForm.letter = suggestedGroupLetter.value
  }
})

watch([showAssignmentModal, availableProfessors, selectedGroup], ([modalOpen]) => {
  if (!modalOpen) {
    selectedProfessorId.value = null
    return
  }

  if (!selectedProfessorId.value && availableProfessors.value.length > 0) {
    const nextProfessor = availableProfessors.value[0]
    selectedProfessorId.value = nextProfessor ? nextProfessor.userId : null
  }
})

watch(selectedGroupId, () => {
  if (!showAssignmentModal.value) {
    return
  }
  selectedProfessorId.value = null
})

watch(
  () => ({ tab: assignmentActiveTab.value, open: showAssignmentModal.value }),
  ({ tab, open }) => {
    if (tab === 'settings' && open) {
      ensureScheduleSlotPresence()
      ensureSchedulingAssets()
      if (!groupForm.letter) {
        groupForm.letter = suggestedGroupLetter.value
      }
    }
  }
)

onMounted(() => {
  fetchCourses()
  fetchProfessors()
})
</script>

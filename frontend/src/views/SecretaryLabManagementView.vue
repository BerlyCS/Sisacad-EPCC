<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="rounded-2xl border border-emerald-100 bg-emerald-50/70 p-5">
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <p class="text-xs font-semibold uppercase tracking-wide text-emerald-600">Gestión de laboratorios</p>
            <h1 class="text-3xl font-bold text-gray-900">Laboratorios por curso teórico</h1>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div class="rounded-xl bg-white px-4 py-3 text-center shadow-sm">
              <p class="text-xs text-gray-500">Cursos teóricos</p>
              <p class="text-2xl font-semibold text-emerald-700">{{ theoryCourses.length }}</p>
            </div>
            <div class="rounded-xl bg-white px-4 py-3 text-center shadow-sm">
              <p class="text-xs text-gray-500">Labs activos</p>
              <p class="text-2xl font-semibold text-emerald-700">{{ totalLabs }}</p>
            </div>
          </div>
        </div>
      </header>

      <div class="grid gap-6 lg:grid-cols-[minmax(0,0.38fr)_minmax(0,0.62fr)]">
        <section class="space-y-4 rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900">Cursos teóricos</h2>
            <button
              class="text-sm font-medium text-emerald-600 hover:text-emerald-700"
              type="button"
              @click="refreshTheoryCourses"
            >
              Recargar
            </button>
          </div>

          <p v-if="theoryError" class="rounded-lg border border-red-100 bg-red-50 px-3 py-2 text-sm text-red-600">
            {{ theoryError }}
          </p>

          <div v-else class="space-y-3">
            <div
              v-for="course in theoryCourses"
              :key="course.courseId"
              class="cursor-pointer rounded-xl border px-4 py-3 text-left transition"
              :class="course.courseId === selectedTheoryCourseId
                ? 'border-emerald-300 bg-emerald-50'
                : 'border-gray-200 hover:border-emerald-200 hover:bg-emerald-50/40'"
              @click="handleCourseSelect(course.courseId)"
            >
              <p class="text-sm font-semibold text-gray-800">{{ course.name }}</p>
              <p class="text-xs text-gray-500">Año {{ course.anio ?? '—' }} · Grupo {{ course.groupLetter ?? '—' }}</p>
              <p class="text-xs text-gray-500">Créditos: {{ course.creditNumber ?? '—' }}</p>
            </div>

            <p v-if="!theoryLoading && theoryCourses.length === 0" class="text-sm text-gray-500">
              No se encontraron cursos teóricos disponibles.
            </p>

            <div v-if="theoryLoading" class="space-y-2">
              <div v-for="n in 3" :key="n" class="h-20 animate-pulse rounded-xl bg-gray-100" />
            </div>
          </div>
        </section>

        <section class="space-y-6">
          <div class="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 class="text-lg font-semibold text-gray-900">Laboratorios registrados</h2>
              </div>
              <div v-if="notification" class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
                {{ notification }}
              </div>
            </div>

            <p v-if="labSectionsError" class="mt-3 rounded-lg border border-red-100 bg-red-50 px-3 py-2 text-sm text-red-600">
              {{ labSectionsError }}
            </p>

            <div v-if="labSectionsLoading" class="mt-4 space-y-3">
              <div v-for="n in 2" :key="n" class="h-16 animate-pulse rounded-xl bg-gray-100" />
            </div>

            <div v-else class="space-y-3">
              <article
                v-for="lab in labSections"
                :key="lab.courseId"
                class="rounded-xl border border-gray-200 p-4 transition hover:border-emerald-200"
              >
                <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
                  <div>
                    <p class="text-sm font-semibold text-gray-900">
                      {{ lab.name }} · Grupo {{ lab.groupLetter ?? '—' }}
                    </p>
                    <p class="text-xs text-gray-500">Capacidad {{ lab.labCapacity }} · Inscritos {{ lab.enrolledCount }} · Disponibles {{ lab.remainingSeats }}</p>
                  </div>
                  <div class="flex gap-2 text-sm">
                    <button
                      class="rounded-full border border-gray-200 px-3 py-1 text-gray-700 transition hover:border-emerald-300 hover:text-emerald-700"
                      type="button"
                      @click="startEditing(lab)"
                    >
                      Editar
                    </button>
                    <button
                      class="rounded-full border border-red-200 px-3 py-1 text-red-600 transition hover:bg-red-50"
                      type="button"
                      @click="() => void handleDelete(lab)"
                    >
                      Eliminar
                    </button>
                  </div>
                </div>
                <div class="mt-3 grid gap-2 md:grid-cols-2">
                  <p
                    v-for="slot in lab.scheduleSlots"
                    :key="slot.classroomName + slot.dayOfWeek + slot.startTime + slot.endTime"
                    class="rounded-lg bg-gray-50 px-3 py-2 text-xs font-medium text-gray-700"
                  >
                    {{ slot.dayOfWeek }} {{ slot.startTime }}-{{ slot.endTime }} · {{ slot.classroomName }}
                  </p>
                </div>
              </article>

              <p v-if="!labSections.length && selectedTheoryCourseId" class="text-sm text-gray-500">
                Aún no registras laboratorios para este curso.
              </p>
            </div>
          </div>

          <div class="rounded-2xl border border-gray-200 bg-white p-5 shadow-sm">
            <div class="flex items-center justify-between">
              <div>
                <h2 class="text-lg font-semibold text-gray-900">
                  {{ isEditing ? 'Editar laboratorio' : 'Nuevo laboratorio' }}
                </h2>
              </div>
              <button
                v-if="isEditing"
                class="text-sm font-medium text-gray-500 hover:text-gray-700"
                type="button"
                @click="resetForm"
              >
                Cancelar
              </button>
            </div>

            <form class="mt-4 space-y-4" @submit.prevent="handleSubmit">
              <div>
                <label class="text-sm font-medium text-gray-700">Nombre</label>
                <input
                  v-model="form.name"
                  type="text"
                  class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
                  placeholder="Laboratorio de ..."
                />
              </div>

              <div class="grid gap-4 md:grid-cols-3">
                <div>
                  <label class="text-sm font-medium text-gray-700">Grupo</label>
                  <input
                    v-model="form.groupLetter"
                    maxlength="1"
                    class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
                    placeholder="A"
                  />
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-700">Capacidad</label>
                  <input
                    v-model.number="form.labCapacity"
                    type="number"
                    min="1"
                    class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
                  />
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-700">Bloques añadidos</label>
                  <p class="mt-1 text-sm font-semibold text-gray-800">{{ form.scheduleSlots.length }}</p>
                </div>
              </div>

              <div class="rounded-xl border border-dashed border-gray-300 p-4">
                <div class="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <p class="text-sm font-semibold text-gray-800">Agregar bloque horario</p>
                  <div class="flex items-center gap-2 text-xs">
                    <span v-if="classroomsLoading" class="text-gray-500">Actualizando aulas...</span>
                    <button
                      class="font-semibold text-emerald-600 transition hover:text-emerald-700 disabled:cursor-not-allowed disabled:opacity-50"
                      type="button"
                      :disabled="classroomsLoading"
                      @click="refreshClassroomsList"
                    >
                      Actualizar laboratorios
                    </button>
                  </div>
                </div>
                <p v-if="classroomsError" class="mt-2 text-xs text-red-600">{{ classroomsError }}</p>
                <p v-else-if="!classroomsLoading && !labClassrooms.length" class="mt-2 text-xs text-amber-600">
                  No hay laboratorios registrados en la base de datos.
                </p>
                <div class="mt-3 grid gap-3 md:grid-cols-4">
                  <div>
                    <label class="text-xs font-medium text-gray-500">Laboratorio</label>
                    <select
                      v-model="slotDraft.classroomName"
                      :disabled="slotActionsDisabled"
                      class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm uppercase focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200 disabled:bg-gray-100"
                    >
                      <option value="" disabled>
                        {{ classroomsLoading ? 'Cargando laboratorios...' : 'Selecciona un laboratorio' }}
                      </option>
                      <option v-for="room in labClassrooms" :key="room.classroomId" :value="room.name">
                        {{ room.name }} · {{ room.classroomType ?? 'Laboratorio' }}
                        <span v-if="room.capacity"> · Cap. {{ room.capacity }}</span>
                      </option>
                    </select>
                  </div>
                  <div>
                    <label class="text-xs font-medium text-gray-500">Día</label>
                    <select
                      v-model="slotDraft.dayOfWeek"
                      class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
                    >
                      <option v-for="day in weekDays" :key="day" :value="day">{{ day }}</option>
                    </select>
                  </div>
                  <div>
                    <label class="text-xs font-medium text-gray-500">Inicio</label>
                    <input
                      v-model="slotDraft.startTime"
                      type="time"
                      class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
                    />
                  </div>
                  <div>
                    <label class="text-xs font-medium text-gray-500">Fin</label>
                    <input
                      v-model="slotDraft.endTime"
                      type="time"
                      class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-2 focus:ring-emerald-200"
                    />
                  </div>
                </div>
                <button
                  class="mt-3 rounded-lg border border-emerald-200 px-4 py-2 text-sm font-medium text-emerald-700 transition hover:bg-emerald-50 disabled:cursor-not-allowed disabled:opacity-50"
                  type="button"
                  :disabled="slotActionsDisabled || !slotDraftReady"
                  @click="addSlotFromDraft"
                >
                  Añadir bloque
                </button>

                <div v-if="form.scheduleSlots.length" class="mt-4 grid gap-2 md:grid-cols-2">
                  <div
                    v-for="(slot, index) in form.scheduleSlots"
                    :key="slot.classroomName + slot.dayOfWeek + slot.startTime + slot.endTime + index"
                    class="flex items-center justify-between rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-800"
                  >
                    <span>{{ slot.dayOfWeek }} {{ slot.startTime }}-{{ slot.endTime }} · {{ slot.classroomName }}</span>
                    <button class="text-xs font-medium" type="button" @click="removeSlot(index)">
                      Quitar
                    </button>
                  </div>
                </div>
              </div>

              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <span v-if="slotSuggestionsLoading" class="text-xs text-gray-500">Cargando...</span>
                </div>
                <div class="flex flex-wrap gap-2">
                  <button
                    v-for="slot in limitedSuggestions"
                    :key="slot.classroomName + slot.dayOfWeek + slot.startTime + slot.endTime"
                    class="rounded-full border border-emerald-200 px-3 py-1 text-xs text-emerald-700 hover:bg-emerald-50"
                    type="button"
                    @click="addSlotFromSuggestion(slot)"
                  >
                    {{ slot.dayOfWeek }} {{ slot.startTime }}-{{ slot.endTime }} · {{ slot.classroomName }}
                  </button>
                  <p v-if="!limitedSuggestions.length && !slotSuggestionsLoading" class="text-xs text-gray-500">
                  </p>
                </div>
              </div>

              <p v-if="errorMessage" class="rounded-lg border border-red-100 bg-red-50 px-3 py-2 text-sm text-red-600">
                {{ errorMessage }}
              </p>

              <button
                :disabled="!canSubmit || !selectedTheoryCourseId"
                class="w-full rounded-xl border border-emerald-500 bg-emerald-600 px-4 py-2 text-center text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-40"
                type="submit"
              >
                {{ isEditing ? 'Actualizar laboratorio' : 'Crear laboratorio' }}
              </button>
            </form>
          </div>
        </section>
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { useSecretaryLabManagement } from '@/composables/useSecretaryLabManagement'
import type { LabSection, LabScheduleSlot, LabSlotSuggestion } from '@/services/secretaryLabService'

const {
  theoryCourses,
  theoryError,
  theoryLoading,
  initialize,
  selectedTheoryCourseId,
  setSelectedTheoryCourse,
  labSections,
  labSectionsLoading,
  labSectionsError,
  slotSuggestions,
  slotSuggestionsLoading,
  labClassrooms,
  classroomsLoading,
  classroomsError,
  loadClassrooms,
  notification,
  errorMessage,
  createLab,
  updateLab,
  removeLab
} = useSecretaryLabManagement()

const weekDays = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES']
const defaultDayOfWeek = weekDays[0] ?? 'LUNES'
const isEditing = ref(false)
const editingLabId = ref<number | null>(null)

const form = reactive({
  name: '',
  groupLetter: '',
  labCapacity: 20,
  scheduleSlots: [] as LabScheduleSlot[]
})

const slotDraft = reactive({
  classroomName: '',
  dayOfWeek: defaultDayOfWeek,
  startTime: '',
  endTime: ''
})

const totalLabs = computed(() => labSections.value.length)

const classroomLookup = computed(() => {
  const map = new Map<string, string>()
  labClassrooms.value.forEach(room => {
    if (!room.name) {
      return
    }
    map.set(normalizeClassroomKey(room.name), room.name)
  })
  return map
})

const slotActionsDisabled = computed(() => classroomsLoading.value || labClassrooms.value.length === 0)
const slotDraftReady = computed(() => Boolean(slotDraft.classroomName && slotDraft.startTime && slotDraft.endTime))

const tryResolveClassroomName = (value: string | null | undefined): string | null => {
  if (!value) {
    return null
  }
  return classroomLookup.value.get(normalizeClassroomKey(value)) ?? null
}

const ensureClassroomName = (value: string | null | undefined): string | null => {
  if (!value) {
    errorMessage.value = 'Selecciona un laboratorio válido'
    return null
  }
  const resolved = tryResolveClassroomName(value)
  if (!resolved) {
    errorMessage.value = `El laboratorio ${value} no está registrado`
  }
  return resolved
}

const normalizeClassroomKey = (value: string) => {
  if (!value) {
    return ''
  }
  let normalized = value
    .normalize('NFD')
    .replace(/[^\p{ASCII}]/gu, '')
    .toUpperCase()
    .replace(/LABORATORIO/g, 'LAB')
    .replace(/\s+/g, ' ')
    .trim()

  const labMatch = normalized.match(/^LAB\s*([0-9]+)/)
  if (labMatch?.[1]) {
    const digits = labMatch[1].padStart(2, '0')
    return `LAB ${digits}`
  }

  const aulaMatch = normalized.match(/^AULA\s*([0-9]+)/)
  if (aulaMatch?.[1]) {
    return `AULA ${aulaMatch[1]}`
  }

  return normalized
}

const validSlotSuggestions = computed(() =>
  slotSuggestions.value.filter(slot => Boolean(tryResolveClassroomName(slot.classroomName)))
)

const limitedSuggestions = computed(() => validSlotSuggestions.value.slice(0, 10))

const setDefaultClassroom = () => {
  slotDraft.classroomName = labClassrooms.value[0]?.name ?? ''
}

const resetSlotDraftFields = () => {
  slotDraft.dayOfWeek = defaultDayOfWeek
  slotDraft.startTime = ''
  slotDraft.endTime = ''
  setDefaultClassroom()
}

const refreshClassroomsList = () => {
  void loadClassrooms(true)
}

const canSubmit = computed(() => {
  return Boolean(
    selectedTheoryCourseId.value &&
      form.name.trim() &&
      form.groupLetter.trim() &&
      form.labCapacity > 0 &&
      form.scheduleSlots.length > 0
  )
})

const resetForm = () => {
  isEditing.value = false
  editingLabId.value = null
  form.name = ''
  form.groupLetter = ''
  form.labCapacity = 20
  form.scheduleSlots = []
  resetSlotDraftFields()
}

const addSlotFromDraft = () => {
  if (!slotDraftReady.value) {
    return
  }

  const classroomName = ensureClassroomName(slotDraft.classroomName)
  if (!classroomName) {
    return
  }

  const slot: LabScheduleSlot = {
    classroomName,
    dayOfWeek: slotDraft.dayOfWeek,
    startTime: slotDraft.startTime,
    endTime: slotDraft.endTime
  }
  form.scheduleSlots = [...form.scheduleSlots, slot]
  resetSlotDraftFields()
}

const addSlotFromSuggestion = (slot: LabSlotSuggestion) => {
  const classroomName = ensureClassroomName(slot.classroomName)
  if (!classroomName) {
    return
  }

  const exists = form.scheduleSlots.some(existing =>
    existing.classroomName === classroomName &&
    existing.dayOfWeek === slot.dayOfWeek &&
    existing.startTime === slot.startTime &&
    existing.endTime === slot.endTime
  )

  if (!exists) {
    form.scheduleSlots = [...form.scheduleSlots, {
      classroomName,
      dayOfWeek: slot.dayOfWeek,
      startTime: slot.startTime,
      endTime: slot.endTime
    }]
  }
}

const removeSlot = (index: number) => {
  form.scheduleSlots = form.scheduleSlots.filter((_, idx) => idx !== index)
}

const startEditing = (lab: LabSection) => {
  isEditing.value = true
  editingLabId.value = lab.courseId
  form.name = lab.name
  form.groupLetter = lab.groupLetter ?? ''
  form.labCapacity = lab.labCapacity
  form.scheduleSlots = lab.scheduleSlots.map(slot => ({ ...slot }))
}

const handleDelete = async (lab: LabSection) => {
  if (!window.confirm(`¿Eliminar el laboratorio ${lab.name}?`)) {
    return
  }
  await removeLab(lab.courseId)
  if (editingLabId.value === lab.courseId) {
    resetForm()
  }
}

const buildPayload = () => ({
  theoryCourseId: selectedTheoryCourseId.value!,
  name: form.name.trim(),
  groupLetter: form.groupLetter.trim().toUpperCase(),
  labCapacity: form.labCapacity,
  scheduleSlots: form.scheduleSlots
})

const handleSubmit = async () => {
  if (!selectedTheoryCourseId.value || !canSubmit.value) {
    return
  }
  const payload = buildPayload()
  if (isEditing.value && editingLabId.value) {
    await updateLab(editingLabId.value, payload)
  } else {
    await createLab(payload)
  }
  resetForm()
}

const refreshTheoryCourses = () => {
  void initialize()
}

const handleCourseSelect = (courseId: number) => {
  void setSelectedTheoryCourse(courseId)
}

watch(labClassrooms, (labs) => {
  if (!labs.length) {
    slotDraft.classroomName = ''
    return
  }
  const resolved = tryResolveClassroomName(slotDraft.classroomName)
  const fallbackName = labs[0]?.name ?? ''
  slotDraft.classroomName = resolved ?? fallbackName
})

watch(selectedTheoryCourseId, (courseId) => {
  if (!courseId) {
    resetForm()
  } else if (!isEditing.value) {
    const course = theoryCourses.value.find(item => item.courseId === courseId)
    form.name = course ? `${course.name} - Laboratorio` : ''
    form.groupLetter = course?.groupLetter ?? ''
  }
})

onMounted(() => {
  void initialize()
})
</script>

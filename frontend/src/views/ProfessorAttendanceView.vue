<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="space-y-2">
        <h1 class="text-3xl font-bold text-gray-900">Registro de Asistencia</h1>
      </header>

      <ProfessorCourseList
        :courses="courses"
        :loading="coursesLoading"
        :error="coursesError || ''"
        @select="handleSelectCourse"
        @retry="loadCourses"
      />

      <section v-if="selectedCourse" class="space-y-6 bg-white p-6 rounded-lg shadow">
        <h2 class="text-xl font-semibold">{{ selectedCourse.courseName }}</h2>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700">Fecha</label>
            <input type="date" v-model="sessionDate" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500">
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Tipo de Clase</label>
            <div class="mt-1 block w-full rounded-md border-gray-300 bg-gray-100 px-3 py-2 text-gray-700 shadow-sm">
              {{ classTypeLabel }}
            </div>
          </div>
        </div>

        <GroupFilterTabs
          :groups="courseGroups"
          :selected-ids="selectedGroupIds"
          :loading="courseGroupsLoading"
          :error="courseGroupsError || ''"
          @update:selected-ids="handleGroupSelection"
        />

        <div v-if="rosterLoading" class="text-center py-4">Cargando estudiantes...</div>
        <div v-else>
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-lg font-medium">Estudiantes</h3>
            <button @click="markAllPresent" class="text-sm text-blue-600 hover:text-blue-800">Marcar todos presentes</button>
          </div>
          
          <div class="overflow-x-auto">
            <table class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estudiante</th>
                  <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Asistencia</th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="student in attendanceList" :key="student.studentId">
                  <td class="px-6 py-4 whitespace-nowrap">
                    <div class="text-sm font-medium text-gray-900">{{ student.fullName }}</div>
                    <div class="text-sm text-gray-500">{{ student.studentId }}</div>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap">
                    <select v-model="student.status" class="rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm">
                      <option value="PRESENT">Presente</option>
                      <option value="ABSENT">Ausente</option>
                    </select>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="mt-6 flex justify-end">
            <button 
              @click="submitAttendance" 
              :disabled="submitting"
              class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition disabled:opacity-50"
            >
              {{ submitting ? 'Guardando...' : 'Guardar Asistencia' }}
            </button>
          </div>
          <p v-if="submitError" class="mt-2 text-red-600 text-right">{{ submitError }}</p>
          <p v-if="submitSuccess" class="mt-2 text-green-600 text-right">Asistencia guardada correctamente.</p>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import AdminLayout from '@/components/ui/TopBar.vue'
import { ProfessorCourseList, GroupFilterTabs } from '@/components/features/professor'
import { useProfessorGrades } from '@/composables/useProfessorGrades'
import { attendanceService, type ClassType, type AttendanceStatus } from '@/services/attendanceService'
import { useProfessorService } from '@/services/professorService'
import type { CourseRosterEntry } from '@/services/gradeService'

const {
  courses,
  coursesLoading,
  coursesError,
  selectedCourse,
  loadCourses,
  openCourseWorkspace,
  courseGroups,
  courseGroupsLoading,
  courseGroupsError,
  rosterEntries,
  rosterLoading,
  selectedGroupIds,
  updateSelectedGroups
} = useProfessorGrades()
const { fetchCurrentProfessor } = useProfessorService()

const sessionDate = ref(new Date().toISOString().split('T')[0])
const submitting = ref(false)
const submitError = ref('')
const submitSuccess = ref(false)
const professorId = ref<number | null>(null)

const activeGroup = computed(() => {
  const groups = courseGroups.value ?? []
  if (!groups.length) {
    return null
  }
  const activeId = selectedGroupIds.value[0]
  if (!activeId) {
    return groups[0]
  }
  return groups.find(group => group.courseId === activeId) ?? groups[0]
})

const classType = computed<ClassType>(() => {
  const sourceType = activeGroup.value?.courseType ?? selectedCourse.value?.courseType ?? 'THEORY'
  if (sourceType === 'LAB') return 'LABORATORY'
  if (sourceType === 'PRACTICE') return 'PRACTICE'
  return 'THEORY'
})

const classTypeLabel = computed(() => {
  const map: Record<string, string> = {
    'THEORY': 'Teoría',
    'LABORATORY': 'Laboratorio',
    'PRACTICE': 'Práctica'
  }
  return map[classType.value] || classType.value
})

interface AttendanceEntry {
  studentId: string
  fullName: string
  status: AttendanceStatus
}

const attendanceList = ref<AttendanceEntry[]>([])

const resolveStudentId = (entry: CourseRosterEntry): string => {
  return entry.studentCui || String(entry.studentUserId) || ''
}

watch(rosterEntries, (entries) => {
  attendanceList.value = entries.map(e => ({
    studentId: resolveStudentId(e),
    fullName: e.fullName,
    status: 'ABSENT'
  }))
})

watch(courseGroups, (groups) => {
  if (!groups?.length) {
    return
  }
  if (selectedGroupIds.value.length === 0) {
    const preferred = groups.find(group => group.canGrade) ?? groups[0]
    if (preferred) {
      updateSelectedGroups([preferred.courseId])
    }
  }
}, { immediate: true })

const handleSelectCourse = async (course: any) => {
  submitSuccess.value = false
  await openCourseWorkspace(course)
}

const handleGroupSelection = async (groupIds: number[]) => {
  await updateSelectedGroups(groupIds)
}

const markAllPresent = () => {
  attendanceList.value.forEach(s => s.status = 'PRESENT')
}

const loadProfessorProfile = async () => {
  try {
    const profile = await fetchCurrentProfessor()
    professorId.value = profile?.userId ?? null
  } catch (error) {
    console.error('No se pudo obtener la información del profesor', error)
    professorId.value = null
  }
}

const submitAttendance = async () => {
  const course = selectedCourse.value
  if (!course) return
  
  submitting.value = true
  submitError.value = ''
  submitSuccess.value = false

  try {
    const professor = professorId.value
    if (!professor) {
      submitError.value = 'No se pudo identificar al profesor. Recarga la página e intenta nuevamente.'
      return
    }

    const groupId = activeGroup.value?.courseId
    if (!groupId) {
      submitError.value = 'Selecciona un grupo antes de guardar la asistencia.'
      return
    }

    await attendanceService.createSession({
      professorId: professor,
      courseId: course.courseId,
      groupId: groupId,
      date: sessionDate.value,
      classType: classType.value,
      todo: '',
      students: attendanceList.value.map(s => ({
        studentId: s.studentId,
        status: s.status
      }))
    })
    submitSuccess.value = true
  } catch (e) {
    submitError.value = 'Error al guardar asistencia'
    console.error(e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCourses()
  void loadProfessorProfile()
})
</script>

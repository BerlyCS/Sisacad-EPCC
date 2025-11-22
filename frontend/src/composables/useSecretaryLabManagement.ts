import { computed, ref } from 'vue'
import {
  useSecretaryLabService,
  type TheoryCourseSummary,
  type LabSection,
  type LabSlotSuggestion,
  type CreateLabSectionPayload,
  type UpdateLabSectionPayload,
  type ClassroomOption
} from '@/services/secretaryLabService'

export const useSecretaryLabManagement = () => {
  const {
    theoryCourses,
    theoryLoading,
    theoryError,
    loadTheoryCourses,
    fetchClassrooms,
    fetchLabSections,
    fetchSlotSuggestions,
    createLabSection,
    updateLabSection,
    deleteLabSection
  } = useSecretaryLabService()

  const selectedTheoryCourseId = ref<number | null>(null)
  const labSections = ref<LabSection[]>([])
  const labSectionsLoading = ref(false)
  const labSectionsError = ref('')

  const slotSuggestions = ref<LabSlotSuggestion[]>([])
  const slotSuggestionsLoading = ref(false)
  const slotSuggestionsError = ref('')

  const classrooms = ref<ClassroomOption[]>([])
  const classroomsLoading = ref(false)
  const classroomsError = ref('')

  const notification = ref('')
  const errorMessage = ref('')

  const selectedTheoryCourse = computed<TheoryCourseSummary | null>(() => {
    if (!selectedTheoryCourseId.value) {
      return null
    }
    return theoryCourses.value.find(course => course.courseId === selectedTheoryCourseId.value) ?? null
  })

  const labClassrooms = computed(() => classrooms.value.filter(option => option.lab))

  const loadClassrooms = async (labsOnly = true) => {
    classroomsLoading.value = true
    classroomsError.value = ''
    try {
      classrooms.value = await fetchClassrooms(labsOnly)
    } catch (error) {
      classroomsError.value = error instanceof Error ? error.message : 'No se pudieron cargar las aulas'
      classrooms.value = []
    } finally {
      classroomsLoading.value = false
    }
  }

  const setSelectedTheoryCourse = async (courseId: number) => {
    if (selectedTheoryCourseId.value === courseId) {
      return
    }
    selectedTheoryCourseId.value = courseId
    await refreshActiveTheoryData()
  }

  const loadSlotSuggestions = async () => {
    if (!selectedTheoryCourseId.value) {
      slotSuggestions.value = []
      return
    }
    slotSuggestionsLoading.value = true
    slotSuggestionsError.value = ''
    try {
      slotSuggestions.value = await fetchSlotSuggestions(selectedTheoryCourseId.value)
    } catch (error) {
      slotSuggestionsError.value = error instanceof Error ? error.message : 'No se pudieron cargar los horarios sugeridos'
      slotSuggestions.value = []
    } finally {
      slotSuggestionsLoading.value = false
    }
  }

  const loadLabSections = async () => {
    if (!selectedTheoryCourseId.value) {
      labSections.value = []
      return
    }
    labSectionsLoading.value = true
    labSectionsError.value = ''
    try {
      labSections.value = await fetchLabSections(selectedTheoryCourseId.value)
    } catch (error) {
      labSectionsError.value = error instanceof Error ? error.message : 'No se pudieron cargar los laboratorios'
      labSections.value = []
    } finally {
      labSectionsLoading.value = false
    }
  }

  const refreshActiveTheoryData = async () => {
    await Promise.all([loadLabSections(), loadSlotSuggestions()])
  }

  const initialize = async () => {
    await Promise.all([loadTheoryCourses(), loadClassrooms(true)])
    const firstCourse = theoryCourses.value[0]
    if (firstCourse) {
      selectedTheoryCourseId.value = firstCourse.courseId
      await refreshActiveTheoryData()
    } else {
      labSections.value = []
      slotSuggestions.value = []
    }
  }

  const createLab = async (payload: CreateLabSectionPayload) => {
    notification.value = ''
    errorMessage.value = ''
    try {
      const created = await createLabSection(payload)
      notification.value = `Se creó el laboratorio ${created.name}`
      await refreshActiveTheoryData()
      return created
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : 'No se pudo crear el laboratorio'
      throw error
    }
  }

  const updateLab = async (labCourseId: number, payload: UpdateLabSectionPayload) => {
    notification.value = ''
    errorMessage.value = ''
    try {
      const updated = await updateLabSection(labCourseId, payload)
      notification.value = `Se actualizó el laboratorio ${updated.name}`
      await refreshActiveTheoryData()
      return updated
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : 'No se pudo actualizar el laboratorio'
      throw error
    }
  }

  const removeLab = async (labCourseId: number) => {
    notification.value = ''
    errorMessage.value = ''
    try {
      await deleteLabSection(labCourseId)
      notification.value = 'Laboratorio eliminado'
      await refreshActiveTheoryData()
    } catch (error) {
      errorMessage.value = error instanceof Error ? error.message : 'No se pudo eliminar el laboratorio'
      throw error
    }
  }

  return {
    theoryCourses,
    theoryLoading,
    theoryError,
    initialize,
    selectedTheoryCourseId,
    selectedTheoryCourse,
    setSelectedTheoryCourse,
    labSections,
    labSectionsLoading,
    labSectionsError,
    slotSuggestions,
    slotSuggestionsLoading,
    slotSuggestionsError,
    classrooms,
    labClassrooms,
    classroomsLoading,
    classroomsError,
    loadClassrooms,
    notification,
    errorMessage,
    createLab,
    updateLab,
    removeLab,
    refreshActiveTheoryData
  }
}

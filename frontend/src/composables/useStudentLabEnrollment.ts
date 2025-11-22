import { computed, ref, watch } from 'vue'
import { useStudentCourseService, type Course } from '@/services/studentCourseService'
import { useLabEnrollmentService, type LabSection, type EnrollmentValidationResult } from '@/services/labEnrollmentService'

export const useStudentLabEnrollment = () => {
  const {
    courses,
    loading: coursesLoading,
    error: coursesError,
    fetchMyCourses
  } = useStudentCourseService()

  const { fetchLabSections, validateEnrollment, confirmEnrollment } = useLabEnrollmentService()

  const selectedTheoryCourseId = ref<number | null>(null)
  const labSections = ref<LabSection[]>([])
  const labSectionsLoading = ref(false)
  const labSectionsError = ref('')
  const validationResult = ref<EnrollmentValidationResult | null>(null)
  const enrollmentResult = ref<EnrollmentValidationResult | null>(null)
  const enrollmentLoading = ref(false)
  const infoMessage = ref('')
  const errorMessage = ref('')

  const theoryCourses = computed(() => courses.value.filter(course => course.courseType === 'THEORY'))

  const labAssignmentsMap = computed(() => {
    const mapping = new Map<number, Course>()
    courses.value.forEach(course => {
      if (course.courseType === 'LAB' && course.labPrerequisiteCourseId) {
        mapping.set(course.labPrerequisiteCourseId, course)
      }
    })
    return mapping
  })

  const selectedTheoryCourse = computed(() =>
    theoryCourses.value.find(course => course.courseId === selectedTheoryCourseId.value) ?? null
  )

  const selectedLabAssignment = computed(() => {
    if (!selectedTheoryCourseId.value) {
      return null
    }
    return labAssignmentsMap.value.get(selectedTheoryCourseId.value) ?? null
  })

  const pendingCourses = computed(() =>
    theoryCourses.value.filter(course => !labAssignmentsMap.value.has(course.courseId))
  )

  const resetFeedback = () => {
    labSectionsError.value = ''
    validationResult.value = null
    enrollmentResult.value = null
    infoMessage.value = ''
    errorMessage.value = ''
  }

  const loadLabSections = async (courseId: number) => {
    if (!Number.isFinite(courseId)) {
      labSections.value = []
      labSectionsError.value = 'Selecciona un curso válido'
      return
    }

    labSectionsLoading.value = true
    resetFeedback()

    try {
      const sections = await fetchLabSections(courseId)
      labSections.value = sections
      if (sections.length === 0) {
        labSectionsError.value = 'No se encontraron laboratorios disponibles para este curso'
      }
    } catch (error) {
      labSections.value = []
      labSectionsError.value = error instanceof Error
        ? error.message
        : 'No se pudieron cargar los laboratorios'
    } finally {
      labSectionsLoading.value = false
    }
  }

  const selectTheoryCourse = async (courseId: number) => {
    if (!Number.isFinite(courseId)) {
      selectedTheoryCourseId.value = null
      labSections.value = []
      resetFeedback()
      return
    }

    if (selectedTheoryCourseId.value === courseId && labSections.value.length) {
      return
    }

    selectedTheoryCourseId.value = courseId
    await loadLabSections(courseId)
  }

  const validateLabSelection = async (labCourseId: number) => {
    if (!Number.isFinite(labCourseId)) {
      errorMessage.value = 'Selecciona un laboratorio válido'
      return
    }

    validationResult.value = null
    infoMessage.value = ''
    errorMessage.value = ''

    try {
      const result = await validateEnrollment(labCourseId)
      validationResult.value = result
      if (result.allowed) {
        infoMessage.value = 'Todo está listo para confirmar tu laboratorio. Haz clic en "Confirmar" para finalizar.'
      } else {
        errorMessage.value = result.message
      }
    } catch (error) {
      validationResult.value = null
      errorMessage.value = error instanceof Error
        ? error.message
        : 'No se pudo validar la matrícula'
    }
  }

  const confirmLabSelection = async (labCourseId: number) => {
    if (!Number.isFinite(labCourseId)) {
      errorMessage.value = 'Selecciona un laboratorio válido'
      return
    }

    enrollmentLoading.value = true
    enrollmentResult.value = null
    errorMessage.value = ''
    infoMessage.value = ''

    try {
      const result = await confirmEnrollment(labCourseId)
      enrollmentResult.value = result

      if (result.allowed) {
        infoMessage.value = 'Tu laboratorio fue registrado exitosamente.'
        await fetchMyCourses()
        if (selectedTheoryCourseId.value) {
          await loadLabSections(selectedTheoryCourseId.value)
        }
      } else {
        errorMessage.value = result.message
      }
    } catch (error) {
      enrollmentResult.value = null
      errorMessage.value = error instanceof Error
        ? error.message
        : 'No se pudo confirmar la matrícula'
    } finally {
      enrollmentLoading.value = false
    }
  }

  watch(theoryCourses, (newCourses, oldCourses) => {
    if (!newCourses.length) {
      selectedTheoryCourseId.value = null
      labSections.value = []
      return
    }

    if (!selectedTheoryCourseId.value) {
      const pending = pendingCourses.value[0]
      const fallback = newCourses[0]
      const nextCourse = pending ?? fallback
      if (nextCourse) {
        void selectTheoryCourse(nextCourse.courseId)
      }
      return
    }

    const stillExists = newCourses.some(course => course.courseId === selectedTheoryCourseId.value)
    if (!stillExists) {
      selectedTheoryCourseId.value = null
      labSections.value = []
    }
  }, { immediate: true })

  return {
    courses,
    coursesLoading,
    coursesError,
    theoryCourses,
    pendingCourses,
    labAssignmentsMap,
    selectedTheoryCourseId,
    selectedTheoryCourse,
    selectedLabAssignment,
    labSections,
    labSectionsLoading,
    labSectionsError,
    validationResult,
    enrollmentResult,
    enrollmentLoading,
    infoMessage,
    errorMessage,
    fetchMyCourses,
    selectTheoryCourse,
    loadLabSections,
    validateLabSelection,
    confirmLabSelection
  }
}

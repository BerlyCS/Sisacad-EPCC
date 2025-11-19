import { storeToRefs } from 'pinia'
import { useGradeStore } from '@/stores/grades'
import { useAuthStore } from '@/stores/auth'

const resolveRefString = (value: unknown): string => {
  if (typeof value === 'string') {
    return value
  }
  if (value && typeof value === 'object' && 'value' in value) {
    const candidate = (value as { value?: unknown }).value
    return typeof candidate === 'string' ? candidate : ''
  }
  return ''
}

export const useStudentGrades = () => {
  const gradeStore = useGradeStore()
  const authStore = useAuthStore()

  const {
    sortedStudentGrades,
    studentGradesLoading,
    studentGradesError,
    finalAverage,
    performanceStatus,
    passedCourses,
    pendingCourses
  } = storeToRefs(gradeStore)

  const refresh = async (documento?: string) => {
    const targetDocumento = documento || resolveRefString(authStore.userDocumentoIdentidad)

    if (!targetDocumento) {
      throw new Error('No se pudo determinar el documento del estudiante autenticado')
    }

    await gradeStore.loadStudentGrades(targetDocumento)
  }

  return {
    grades: sortedStudentGrades,
    loading: studentGradesLoading,
    error: studentGradesError,
    finalAverage,
    performanceStatus,
    passedCourses,
    pendingCourses,
    refresh
  }
}

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

  const refresh = async (userId?: number) => {
    let targetUserId = userId || Number(resolveRefString(authStore.userDocumentoIdentidad))

    if (!targetUserId || !Number.isFinite(targetUserId)) {
      // Fallback: try to resolve userId from the student's CUI via the student profile endpoint
      const cui = resolveRefString(authStore.userCui)
      if (cui) {
        try {
          const resp = await fetch(`/api/students/profile/${encodeURIComponent(cui)}`, { credentials: 'include' })
          if (resp.ok) {
            const data = await resp.json()
            const resolved = Number(data.userId ?? data.user_id ?? data.userID)
            if (resolved && Number.isFinite(resolved)) {
              targetUserId = resolved
            }
          }
        } catch (err) {
          console.warn('No se pudo resolver userId por CUI:', err)
        }
      }
    }

    if (!targetUserId || !Number.isFinite(targetUserId)) {
      throw new Error('No se pudo determinar el ID del estudiante autenticado')
    }

    await gradeStore.loadStudentGrades(targetUserId)
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

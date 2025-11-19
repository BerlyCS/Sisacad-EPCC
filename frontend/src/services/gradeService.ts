const API_BASE_URL = 'http://localhost:8080/api'

export interface StudentGrade {
  courseCode: string
  courseId: number
  courseName: string
  continuousGrades: number[]
  examGrades: number[]
  continuousWeights: number[]
  examWeights: number[]
  finalGrade: number | null
}

export interface CourseGradeStats {
  courseCode: string
  meanGrade: number | null
  highestGrade: number | null
  lowestGrade: number | null
  meanFinalGrade: number | null
  highestFinalGrade: number | null
  lowestFinalGrade: number | null
  gradedStudents: number
}

export interface ProfessorCourseSummary {
  courseId: number
  courseCode: string
  courseName: string
  groupLetter: string
  creditNumber: number | null
}

const toNumber = (value: unknown): number | null => {
  const parsed = typeof value === 'string' ? Number(value) : value as number
  return typeof parsed === 'number' && Number.isFinite(parsed) ? parsed : null
}

const mapStudentGrade = (payload: any): StudentGrade => {
  const normalizeArray = (items: unknown[]): number[] => {
    return Array.isArray(items)
      ? items
          .map(value => toNumber(value))
          .filter((value): value is number => value !== null)
      : []
  }

  return {
    courseCode: payload.courseCode ?? payload.course_id ?? '',
    courseId: Number(payload.courseId ?? payload.course_id ?? 0),
    courseName: payload.courseName ?? payload.course_name ?? 'Curso',
    continuousGrades: normalizeArray(payload.continuousGrades ?? []),
    examGrades: normalizeArray(payload.examGrades ?? []),
    continuousWeights: normalizeArray(payload.continuousWeights ?? []),
    examWeights: normalizeArray(payload.examWeights ?? []),
    finalGrade: toNumber(payload.finalGrade)
  }
}

const mapCourseStats = (payload: any): CourseGradeStats => {
  return {
    courseCode: payload.courseCode ?? '',
    meanGrade: toNumber(payload.meanGrade),
    highestGrade: toNumber(payload.highestGrade),
    lowestGrade: toNumber(payload.lowestGrade),
    meanFinalGrade: toNumber(payload.meanFinalGrade),
    highestFinalGrade: toNumber(payload.highestFinalGrade),
    lowestFinalGrade: toNumber(payload.lowestFinalGrade),
    gradedStudents: typeof payload.gradedStudents === 'number'
      ? payload.gradedStudents
      : Number(payload.gradedStudents ?? 0)
  }
}

const mapProfessorCourseSummary = (payload: any): ProfessorCourseSummary => {
  return {
    courseId: Number(payload.courseId ?? payload.courseID ?? 0),
    courseCode: String(payload.courseCode ?? payload.courseId ?? ''),
    courseName: payload.name ?? 'Curso',
    groupLetter: payload.groupLetter ?? '-',
    creditNumber: payload.creditNumber != null ? Number(payload.creditNumber) : null
  }
}

const requestJson = async (url: string) => {
  const response = await fetch(url, { credentials: 'include' })

  if (!response.ok) {
    let message = 'Error en la solicitud'
    try {
      const parsed = await response.clone().json()
      message = parsed?.message ?? parsed?.error ?? message
    } catch {
      message = (await response.text().catch(() => message)) || message
    }

    const error = new Error(message)
    ;(error as Error & { status?: number }).status = response.status
    throw error
  }

  return response.json()
}

export const gradeService = {
  async fetchStudentGrades(studentDocumento: string): Promise<StudentGrade[]> {
    if (!studentDocumento) {
      return []
    }
    const data = await requestJson(`${API_BASE_URL}/grades/students/${encodeURIComponent(studentDocumento)}`)
    return Array.isArray(data) ? data.map(mapStudentGrade) : []
  },

  async fetchGradeForCourse(studentDocumento: string, courseCode: string): Promise<StudentGrade | null> {
    if (!studentDocumento || !courseCode) {
      return null
    }
    try {
      const data = await requestJson(`${API_BASE_URL}/grades/students/${encodeURIComponent(studentDocumento)}/courses/${encodeURIComponent(courseCode)}`)
      return data ? mapStudentGrade(data) : null
    } catch (error) {
      if ((error as Error & { status?: number }).status === 404) {
        return null
      }
      throw error
    }
  },

  async fetchCourseStatistics(courseCode: string): Promise<CourseGradeStats> {
    if (!courseCode) {
      throw new Error('Código de curso inválido')
    }
    const data = await requestJson(`${API_BASE_URL}/grades/courses/${encodeURIComponent(courseCode)}/statistics`)
    return mapCourseStats(data)
  },

  async fetchProfessorCourseSummaries(): Promise<ProfessorCourseSummary[]> {
    const courses = await requestJson(`${API_BASE_URL}/professors/me/courses`)
    return Array.isArray(courses) ? courses.map(mapProfessorCourseSummary) : []
  }
}

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
  courseType: 'THEORY' | 'LAB' | 'PRACTICE'
}

export interface CourseGroupSummary {
  courseId: number
  courseCode: string
  courseName: string
  groupLetter: string
  courseType: 'THEORY' | 'LAB' | 'PRACTICE'
  canGrade: boolean
  studentCount: number
  maxCapacity: number
}

export interface CourseRosterEntry {
  studentDocumentoIdentidad: string
  studentCui: string
  fullName: string
  email: string
  courseId: number
  courseCode: string
  groupLetter: string
  courseType: 'THEORY' | 'LAB' | 'PRACTICE'
  canGrade: boolean
  continuousGrades: number[]
  examGrades: number[]
  finalGrade: number | null
  submissionStatus: string
}

export interface CourseRosterPage {
  students: CourseRosterEntry[]
  total: number
  page: number
  size: number
}

export interface GradingRubric {
  courseId: number
  courseCode: string
  continuousWeights: number[]
  examWeights: number[]
}

export interface GradeSubmissionPayload {
  continuousGrades: number[]
  examGrades: number[]
  status?: string
  feedback?: string
}

export interface GradeSubmissionResponse {
  courseId: number
  courseCode: string
  studentDocumentoIdentidad: string
  continuousGrades: number[]
  examGrades: number[]
  finalGrade: number | null
  status: string
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
    creditNumber: payload.creditNumber != null ? Number(payload.creditNumber) : null,
    courseType: ((payload.courseType ?? 'THEORY').toString().toUpperCase()) as 'THEORY' | 'LAB' | 'PRACTICE'
  }
}

const requestJson = async (url: string) => {
  const response = await fetch(url, { credentials: 'include' })

  if (!response.ok) {
    throw await buildHttpError(response)
  }

  return response.json()
}

const requestJsonWithBody = async (url: string, method: string, body: unknown) => {
  const response = await fetch(url, {
    method,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })

  if (!response.ok) {
    throw await buildHttpError(response)
  }
  return response.json()
}

const buildHttpError = async (response: Response) => {
  let message = 'Error en la solicitud'
  try {
    const parsed = await response.clone().json()
    message = parsed?.message ?? parsed?.error ?? message
  } catch {
    message = (await response.text().catch(() => message)) || message
  }

  const error = new Error(`${message} (HTTP ${response.status})`)
  ;(error as Error & { status?: number }).status = response.status
  return error
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
  },

  async fetchCourseGroups(courseId: number): Promise<CourseGroupSummary[]> {
    if (!courseId) {
      return []
    }
    try {
      const data = await requestJson(`${API_BASE_URL}/grades/courses/${courseId}/groups`)
      return Array.isArray(data) ? data.map(mapCourseGroupSummary) : []
    } catch (error) {
      if ((error as Error & { status?: number }).status === 404) {
        return []
      }
      throw error
    }
  },

  async fetchCourseRoster(courseId: number, options?: { groupIds?: number[]; page?: number; size?: number }): Promise<CourseRosterPage> {
    if (!courseId) {
      return { students: [], total: 0, page: 0, size: 0 }
    }

    const params = new URLSearchParams()
    options?.groupIds?.forEach(id => params.append('groupIds', String(id)))
    params.set('page', String(options?.page ?? 0))
    params.set('size', String(options?.size ?? 150))

    const url = `${API_BASE_URL}/grades/courses/${courseId}/students?${params.toString()}`
    try {
      const data = await requestJson(url)
      return mapCourseRosterPage(data)
    } catch (error) {
      if ((error as Error & { status?: number }).status === 404) {
        return { students: [], total: 0, page: 0, size: options?.size ?? 150 }
      }
      throw error
    }
  },

  async fetchCourseRubric(courseId: number): Promise<GradingRubric> {
    if (!courseId) {
      throw new Error('Curso inválido para la rúbrica')
    }
    const data = await requestJson(`${API_BASE_URL}/grades/courses/${courseId}/rubric`)
    return mapRubric(data)
  },

  async submitGrade(courseId: number, studentDocumento: string, groupId: number, payload: GradeSubmissionPayload): Promise<GradeSubmissionResponse> {
    if (!courseId || !studentDocumento || !groupId) {
      throw new Error('Datos incompletos para registrar la nota')
    }

    const url = `${API_BASE_URL}/grades/courses/${courseId}/students/${encodeURIComponent(studentDocumento)}/groups/${groupId}`
    const data = await requestJsonWithBody(url, 'POST', payload)
    return mapGradeSubmissionResponse(data)
  }
}

const toNumberArray = (source: unknown): number[] => {
  if (!Array.isArray(source)) {
    return []
  }
  return source
    .map(item => toNumber(item))
    .filter((value): value is number => typeof value === 'number')
}

const mapCourseGroupSummary = (payload: any): CourseGroupSummary => ({
  courseId: Number(payload.courseId ?? payload.courseID ?? 0),
  courseCode: String(payload.courseCode ?? payload.courseId ?? ''),
  courseName: payload.courseName ?? payload.name ?? 'Curso',
  groupLetter: String(payload.groupLetter ?? '').trim() || '-',
  courseType: ((payload.courseType ?? 'THEORY').toString().toUpperCase()) as 'THEORY' | 'LAB' | 'PRACTICE',
  canGrade: Boolean(payload.canGrade ?? false),
  studentCount: Number(payload.studentCount ?? 0),
  maxCapacity: Number(payload.maxCapacity ?? 150)
})

const mapCourseRosterEntry = (payload: any): CourseRosterEntry => ({
  studentDocumentoIdentidad: payload.studentDocumentoIdentidad ?? '',
  studentCui: payload.studentCui ?? '',
  fullName: payload.fullName ?? 'Estudiante',
  email: payload.email ?? '',
  courseId: Number(payload.courseId ?? 0),
  courseCode: String(payload.courseCode ?? ''),
  groupLetter: String(payload.groupLetter ?? '').trim() || '-',
  courseType: ((payload.courseType ?? 'THEORY').toString().toUpperCase()) as 'THEORY' | 'LAB' | 'PRACTICE',
  canGrade: Boolean(payload.canGrade ?? false),
  continuousGrades: toNumberArray(payload.continuousGrades ?? []),
  examGrades: toNumberArray(payload.examGrades ?? []),
  finalGrade: toNumber(payload.finalGrade),
  submissionStatus: payload.submissionStatus ?? 'PENDING'
})

const mapCourseRosterPage = (payload: any): CourseRosterPage => ({
  students: Array.isArray(payload?.students) ? payload.students.map(mapCourseRosterEntry) : [],
  total: Number(payload?.total ?? 0),
  page: Number(payload?.page ?? 0),
  size: Number(payload?.size ?? 0)
})

const mapRubric = (payload: any): GradingRubric => ({
  courseId: Number(payload.courseId ?? 0),
  courseCode: String(payload.courseCode ?? ''),
  continuousWeights: toNumberArray(payload.continuousWeights ?? []),
  examWeights: toNumberArray(payload.examWeights ?? [])
})

const mapGradeSubmissionResponse = (payload: any): GradeSubmissionResponse => ({
  courseId: Number(payload.courseId ?? 0),
  courseCode: String(payload.courseCode ?? ''),
  studentDocumentoIdentidad: payload.studentDocumentoIdentidad ?? '',
  continuousGrades: toNumberArray(payload.continuousGrades ?? []),
  examGrades: toNumberArray(payload.examGrades ?? []),
  finalGrade: toNumber(payload.finalGrade),
  status: payload.status ?? 'SUBMITTED'
})

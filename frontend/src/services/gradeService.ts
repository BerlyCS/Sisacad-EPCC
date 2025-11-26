const rawApiBase = (import.meta.env.VITE_API_BASE_URL as string | undefined) ?? '/api'
const API_BASE_URL = rawApiBase.replace(/\/+$/, '') || '/api'

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
  groupId: number
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
  studentUserId: number
  studentCui: string
  fullName: string
  email: string
  groupId: number
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
  groupId: number
  courseId: number
  courseCode: string
  studentUserId: number
  continuousGrades: number[]
  examGrades: number[]
  finalGrade: number | null
  status: string
}

export interface CourseGroupExamPdf {
  summaryId: number
  groupId: number
  examNumber: number
  summaryType: 'MEAN' | 'BEST' | 'WORST'
  fileName: string | null
  fileSizeBytes: number | null
}

export interface ExamPdfUploadPayload {
  examNumber: number
  summaryType: 'MEAN' | 'BEST' | 'WORST'
  file: File
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

const ensureJsonPayload = async (response: Response) => {
  const clone = response.clone()
  try {
    return await clone.json()
  } catch (error) {
    const text = await response.text().catch(() => '')
    const snippet = text.trim().slice(0, 200)
    const detail = snippet ? ` Fragmento: ${snippet}` : ''
    throw new Error(`Respuesta inválida del servidor (HTTP ${response.status}).${detail}`)
  }
}

const requestJson = async (url: string) => {
  const response = await fetch(url, { credentials: 'include' })

  if (!response.ok) {
    throw await buildHttpError(response)
  }

  return ensureJsonPayload(response)
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
  return ensureJsonPayload(response)
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
  async fetchStudentGrades(studentUserId: number): Promise<StudentGrade[]> {
    if (!studentUserId) {
      return []
    }
    const data = await requestJson(`${API_BASE_URL}/grades/students/${encodeURIComponent(String(studentUserId))}`)
    return Array.isArray(data) ? data.map(mapStudentGrade) : []
  },

  async fetchGradeForCourse(studentUserId: number, courseId: number): Promise<StudentGrade | null> {
    if (!studentUserId || !courseId) {
      return null
    }
    try {
      const data = await requestJson(`${API_BASE_URL}/grades/students/${encodeURIComponent(String(studentUserId))}/courses/${courseId}`)
      return data ? mapStudentGrade(data) : null
    } catch (error) {
      if ((error as Error & { status?: number }).status === 404) {
        return null
      }
      throw error
    }
  },

  async fetchCourseStatistics(courseId: number): Promise<CourseGradeStats> {
    if (!courseId) {
      throw new Error('Curso inválido')
    }
    const data = await requestJson(`${API_BASE_URL}/grades/courses/${courseId}/statistics`)
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

  async submitGrade(courseId: number, studentUserId: number, groupId: number, payload: GradeSubmissionPayload): Promise<GradeSubmissionResponse> {
    if (!courseId || !studentUserId || !groupId) {
      throw new Error('Datos incompletos para registrar la nota')
    }

    const url = `${API_BASE_URL}/grades/courses/${courseId}/students/${encodeURIComponent(String(studentUserId))}/groups/${groupId}`
    const data = await requestJsonWithBody(url, 'POST', payload)
    return mapGradeSubmissionResponse(data)
  },

  async fetchGroupExamPdfs(groupId: number): Promise<CourseGroupExamPdf[]> {
    if (!groupId) {
      return []
    }
    const data = await requestJson(`${API_BASE_URL}/grades/groups/${groupId}/exam-summaries`)
    return Array.isArray(data) ? data.map(mapCourseGroupExamPdf) : []
  },

  async uploadGroupExamPdf(groupId: number, payload: ExamPdfUploadPayload): Promise<CourseGroupExamPdf> {
    if (!groupId || !payload?.file) {
      throw new Error('Datos incompletos para subir el PDF')
    }
    const formData = new FormData()
    formData.append('metadata', new Blob([
      JSON.stringify({ examNumber: payload.examNumber, summaryType: payload.summaryType })
    ], { type: 'application/json' }))
    formData.append('file', payload.file)

    const response = await fetch(`${API_BASE_URL}/grades/groups/${groupId}/exam-summaries`, {
      method: 'POST',
      credentials: 'include',
      body: formData
    })

    if (!response.ok) {
      throw await buildHttpError(response)
    }
    const data = await response.json()
    return mapCourseGroupExamPdf(data)
  },

  async deleteGroupExamPdf(groupId: number, summaryId: number): Promise<void> {
    if (!groupId || !summaryId) {
      return
    }
    const response = await fetch(`${API_BASE_URL}/grades/groups/${groupId}/exam-summaries/${summaryId}`, {
      method: 'DELETE',
      credentials: 'include'
    })
    if (!response.ok && response.status !== 404) {
      throw await buildHttpError(response)
    }
  },

  buildExamPdfDownloadUrl(groupId: number, summaryId: number): string {
    return `${API_BASE_URL}/grades/groups/${groupId}/exam-summaries/${summaryId}/download`
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
  groupId: Number(payload.groupId ?? payload.groupID ?? payload.id ?? payload.courseId ?? 0),
  courseId: Number(payload.courseId ?? payload.parentCourseId ?? payload.anchorCourseId ?? 0),
  courseCode: String(payload.courseCode ?? payload.courseId ?? ''),
  courseName: payload.courseName ?? payload.name ?? 'Curso',
  groupLetter: String(payload.groupLetter ?? '').trim() || '-',
  courseType: ((payload.courseType ?? 'THEORY').toString().toUpperCase()) as 'THEORY' | 'LAB' | 'PRACTICE',
  canGrade: Boolean(payload.canGrade ?? false),
  studentCount: Number(payload.studentCount ?? 0),
  maxCapacity: Number(payload.maxCapacity ?? 150)
})

const mapCourseRosterEntry = (payload: any): CourseRosterEntry => ({
  studentUserId: Number(payload.studentUserId ?? payload.studentId ?? payload.studentDocumentoIdentidad ?? 0),
  studentCui: payload.studentCui ?? '',
  fullName: payload.fullName ?? 'Estudiante',
  email: payload.email ?? '',
  groupId: Number(payload.groupId ?? payload.groupID ?? payload.courseGroupId ?? payload.courseId ?? 0),
  courseId: Number(payload.courseId ?? payload.parentCourseId ?? payload.anchorCourseId ?? 0),
  courseCode: String(payload.courseCode ?? ''),
  groupLetter: String(payload.groupLetter ?? '').trim() || '-',
  courseType: ((payload.courseType ?? 'THEORY').toString().toUpperCase()) as 'THEORY' | 'LAB' | 'PRACTICE',
  canGrade: Boolean(payload.canGrade ?? false),
  continuousGrades: toNumberArray(payload.continuousGrades ?? []),
  examGrades: toNumberArray(payload.examGrades ?? []),
  finalGrade: toNumber(payload.finalGrade),
  submissionStatus: payload.submissionStatus ?? payload.status ?? 'PENDING'
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
  groupId: Number(payload.groupId ?? payload.groupID ?? payload.courseGroupId ?? payload.courseId ?? 0),
  courseId: Number(payload.courseId ?? payload.parentCourseId ?? 0),
  courseCode: String(payload.courseCode ?? ''),
  studentUserId: Number(payload.studentUserId ?? payload.studentId ?? payload.studentDocumentoIdentidad ?? 0),
  continuousGrades: toNumberArray(payload.continuousGrades ?? []),
  examGrades: toNumberArray(payload.examGrades ?? []),
  finalGrade: toNumber(payload.finalGrade),
  status: payload.status ?? 'SUBMITTED'
})

const mapCourseGroupExamPdf = (payload: any): CourseGroupExamPdf => ({
  summaryId: Number(payload.summaryId ?? payload.id ?? 0),
  groupId: Number(payload.groupId ?? 0),
  examNumber: Number(payload.examNumber ?? 0),
  summaryType: ((payload.summaryType ?? 'MEAN').toString().toUpperCase()) as 'MEAN' | 'BEST' | 'WORST',
  fileName: payload.fileName ?? payload.name ?? null,
  fileSizeBytes: typeof payload.fileSizeBytes === 'number'
    ? payload.fileSizeBytes
    : (payload.fileSizeBytes != null ? Number(payload.fileSizeBytes) : null)
})

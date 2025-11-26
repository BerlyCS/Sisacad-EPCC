const API_BASE_URL = 'http://localhost:8080/api'

export type ClassType = 'THEORY' | 'LABORATORY' | 'PRACTICE'
export type AttendanceStatus = 'PRESENT' | 'ABSENT'

export interface StudentAttendanceDTO {
  id: number
  date: string
  classType: ClassType
  status: AttendanceStatus
  todo: string
}

export interface AttendanceSession {
  attendanceId: number
  professorId: number
  courseId: number
  groupId: number
  status: AttendanceStatus
  timestamp: string
  date: string
  classType: ClassType
  todo: string
}

export interface ProfessorAttendanceRecord {
  attendanceId: number
  professorId: number
  courseId: number
  courseGroupId?: number
  status: AttendanceStatus
  timestamp: string
  date: string
  classType: ClassType
  todo?: string
}

export interface StudentAttendanceRequest {
  studentId: string
  status: AttendanceStatus
}

export interface SessionRequest {
  professorId: number
  courseId: number
  groupId: number
  latitude?: number
  longitude?: number
  timestamp?: string
  date?: string
  classType: ClassType
  todo: string
  students: StudentAttendanceRequest[]
}

export interface ProfessorAttendanceRequest {
  professorId: number
  courseId: number
  groupId: number
  status: AttendanceStatus
  date?: string
  timestamp?: string
  classType: ClassType
  todo?: string
  latitude?: number
  longitude?: number
}

const requestJson = async (url: string) => {
  const response = await fetch(url, { credentials: 'include' })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

const requestJsonWithBody = async (url: string, method: string, body: unknown) => {
  const response = await fetch(url, {
    method,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  })
  if (!response.ok) throw new Error(`HTTP ${response.status}`)
  return response.json()
}

export const attendanceService = {
  async createSession(data: SessionRequest): Promise<AttendanceSession> {
    return requestJsonWithBody(`${API_BASE_URL}/attendances/session`, 'POST', data)
  },

  async markProfessorAttendance(data: ProfessorAttendanceRequest): Promise<ProfessorAttendanceRecord> {
    return requestJsonWithBody(`${API_BASE_URL}/attendances`, 'POST', data)
  },

  async getStudentAttendance(studentId: string, courseId: number): Promise<StudentAttendanceDTO[]> {
    return requestJson(`${API_BASE_URL}/attendances/student/${encodeURIComponent(studentId)}/course/${courseId}`)
  }
}

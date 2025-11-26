const API_BASE_URL = 'http://localhost:8080/api'

export interface AttendanceStatsDTO {
    attendancePercentage: number
    syllabusProgress: number
    totalSessions: number
    presentStudents: number
    absentStudents: number
}

const requestJson = async (url: string) => {
    const response = await fetch(url, { credentials: 'include' })
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return response.json()
}

export const reportService = {
    async getStats(courseId: number): Promise<AttendanceStatsDTO> {
        return requestJson(`${API_BASE_URL}/reports/attendance/${courseId}/stats`)
    },

    async downloadExcel(courseId: number) {
        const response = await fetch(`${API_BASE_URL}/reports/attendance/${courseId}/export/excel`, {
            credentials: 'include'
        })
        if (!response.ok) throw new Error(`HTTP ${response.status}`)

        const blob = await response.blob()
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `attendance_report_${courseId}.xlsx`
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)
    }
}

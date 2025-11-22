const API_BASE_URL = 'http://localhost:8080/api'

export type TopicScheduleStatus = 'UPCOMING' | 'TODAY' | 'COMPLETED' | 'UNSCHEDULED'

export interface SyllabusContentSummary {
  name: string | null
  type: string | null
  url: string | null
  sizeBytes: number | null
}

export interface SyllabusTopicSummary {
  name: string
  weight: number | null
  sessionDate: string | null
  status: TopicScheduleStatus
}

export interface CourseSyllabusSummary {
  syllabusId: number
  courseId: number
  content: SyllabusContentSummary | null
  topics: SyllabusTopicSummary[]
}

export interface SyllabusTopicPayload {
  name: string
  weight: number
  sessionDate: string
}

export interface SyllabusUploadPayload {
  courseId: number
  file: File
  topics?: SyllabusTopicPayload[]
}

const buildHttpError = async (response: Response) => {
  let message = 'Error en la solicitud de sílabo'
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

const requestJson = async (url: string, options?: RequestInit) => {
  const response = await fetch(url, {
    credentials: 'include',
    ...options
  })

  if (!response.ok) {
    throw await buildHttpError(response)
  }

  return response.json()
}

const mapSyllabusContent = (payload: any): SyllabusContentSummary => ({
  name: payload?.name ?? null,
  type: payload?.type ?? null,
  url: payload?.url ?? null,
  sizeBytes: typeof payload?.sizeBytes === 'number'
    ? payload.sizeBytes
    : (typeof payload?.sizeBytes === 'string' ? Number(payload.sizeBytes) : null)
})

const mapTopic = (payload: any): SyllabusTopicSummary => ({
  name: payload?.name ?? 'Tema',
  weight: typeof payload?.weight === 'number'
    ? payload.weight
    : (typeof payload?.weight === 'string' ? Number(payload.weight) : null),
  sessionDate: payload?.sessionDate ?? null,
  status: ((payload?.status ?? 'UNSCHEDULED').toString().toUpperCase()) as TopicScheduleStatus
})

const mapSyllabus = (payload: any): CourseSyllabusSummary => ({
  syllabusId: Number(payload?.syllabusId ?? payload?.id ?? 0),
  courseId: Number(payload?.courseId ?? 0),
  content: payload?.content ? mapSyllabusContent(payload.content) : null,
  topics: Array.isArray(payload?.topics) ? payload.topics.map(mapTopic) : []
})

export const syllabusService = {
  async fetchByCourse(courseId: number): Promise<CourseSyllabusSummary | null> {
    if (!courseId) {
      return null
    }

    try {
      const data = await requestJson(`${API_BASE_URL}/syllabus/course/${courseId}`)
      return data ? mapSyllabus(data) : null
    } catch (error) {
      if ((error as Error & { status?: number }).status === 404) {
        return null
      }
      throw error
    }
  },

  async upload(payload: SyllabusUploadPayload): Promise<CourseSyllabusSummary> {
    if (!payload.courseId || !payload.file) {
      throw new Error('Datos incompletos para subir el sílabo')
    }

    const formData = new FormData()
    formData.append('file', payload.file)
    formData.append(
      'metadata',
      new Blob([
        JSON.stringify({
          courseId: payload.courseId,
          topics: Array.isArray(payload.topics) ? payload.topics : []
        })
      ], { type: 'application/json' })
    )

    const data = await requestJson(`${API_BASE_URL}/syllabus`, {
      method: 'POST',
      body: formData
    })
    return mapSyllabus(data)
  },

  async updateTopics(courseId: number, topics: SyllabusTopicPayload[]): Promise<CourseSyllabusSummary> {
    if (!courseId) {
      throw new Error('Curso inválido para actualizar el temario')
    }

    const data = await requestJson(`${API_BASE_URL}/syllabus/course/${courseId}/topics`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ courseId, topics })
    })
    return mapSyllabus(data)
  },

  async delete(syllabusId: number): Promise<void> {
    if (!syllabusId) {
      throw new Error('Identificador del sílabo inválido')
    }

    const response = await fetch(`${API_BASE_URL}/syllabus/${syllabusId}`, {
      method: 'DELETE',
      credentials: 'include'
    })

    if (!response.ok) {
      throw await buildHttpError(response)
    }
  },

  buildDownloadUrl(syllabusId?: number | null): string {
    if (!syllabusId) {
      return ''
    }
    return `${API_BASE_URL}/syllabus/${syllabusId}/download`
  }
}

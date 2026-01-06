const API_BASE_URL = 'http://localhost:8080/api'

export type UserImportTarget = 'students' | 'professors' | 'secretaries'

export interface UserImportEntry {
  userId?: number | null
  cui?: string | null
  firstNames: string
  paternalSurname: string
  maternalSurname: string
  institutionalEmail: string
  enrollmentYear?: number | null
}

export interface UserImportResult<T = UserImportEntry> {
  created: T[]
  errors: string[]
  processedRows: number
  skippedRows: number
}

const targetEndpoints: Record<UserImportTarget, string> = {
  students: 'students/import',
  professors: 'professors/import',
  secretaries: 'secretaries/import'
}

const normalizeEntry = (raw: any): UserImportEntry => ({
  userId: typeof raw?.userId === 'number' ? raw.userId : null,
  cui: raw?.cui ?? null,
  firstNames: String(raw?.firstNames ?? raw?.nombres ?? raw?.name ?? ''),
  paternalSurname: String(raw?.paternalSurname ?? raw?.apellidoPaterno ?? ''),
  maternalSurname: String(raw?.maternalSurname ?? raw?.apellidoMaterno ?? ''),
  institutionalEmail: String(
    raw?.institutionalEmail ?? raw?.correoInstitucional ?? raw?.email ?? ''
  ),
  enrollmentYear: typeof raw?.enrollmentYear === 'number' ? raw.enrollmentYear : null
})

const normalizeResult = (raw: any): UserImportResult => ({
  created: Array.isArray(raw?.created) ? raw.created.map(normalizeEntry) : [],
  errors: Array.isArray(raw?.errors)
    ? raw.errors.filter((entry) => typeof entry === 'string')
    : [],
  processedRows: typeof raw?.processedRows === 'number' ? raw.processedRows : 0,
  skippedRows: typeof raw?.skippedRows === 'number' ? raw.skippedRows : 0
})

export const importUserData = async (target: UserImportTarget, file: File): Promise<UserImportResult> => {
  const endpoint = targetEndpoints[target]
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(`${API_BASE_URL}/${endpoint}`, {
    method: 'POST',
    credentials: 'include',
    body: formData
  })

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const fallback =
      payload?.message ||
      (Array.isArray(payload?.errors) && payload.errors[0]) ||
      response.statusText ||
      'No se pudo subir el archivo'
    throw new Error(fallback)
  }

  return normalizeResult(payload)
}

const API_BASE_URL = 'http://localhost:8080/api'

export interface EnrollmentImportResult {
  created: string[]
  errors: string[]
  processedRows: number
  skippedRows: number
}

const normalizeResult = (raw: any): EnrollmentImportResult => ({
  created: Array.isArray(raw?.created)
    ? raw.created.filter((entry: unknown) => typeof entry === 'string')
    : [],
  errors: Array.isArray(raw?.errors)
    ? raw.errors.filter((entry: unknown) => typeof entry === 'string')
    : [],
  processedRows: typeof raw?.processedRows === 'number' ? raw.processedRows : 0,
  skippedRows: typeof raw?.skippedRows === 'number' ? raw.skippedRows : 0
})

export const importEnrollmentsFile = async (file: File): Promise<EnrollmentImportResult> => {
  const form = new FormData()
  form.append('file', file)

  const response = await fetch(`${API_BASE_URL}/secretary/enrollments/import`, {
    method: 'POST',
    credentials: 'include',
    body: form
  })

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const fallback =
      payload?.message ||
      (Array.isArray(payload?.errors) && payload.errors[0]) ||
      response.statusText ||
      'No se pudo procesar el archivo'
    throw new Error(fallback)
  }

  return normalizeResult(payload)
}

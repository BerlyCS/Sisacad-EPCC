const API_BASE_URL = 'http://localhost:8080/api'

export const authService = {
  async verifyEmail(email: string): Promise<{ valid: boolean; message: string; oauthUrl?: string }> {
    const response = await fetch(`${API_BASE_URL}/auth/verify-email`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email }),
    })

    if (!response.ok) {
      throw new Error('Error al verificar el correo')
    }

    return await response.json()
  },

  async getCurrentUser() {
    const response = await fetch(`${API_BASE_URL}/user/me`, {
      credentials: 'include' // Para incluir cookies de sesión
    })

    if (!response.ok) {
      throw new Error('Error al obtener información del usuario')
    }

    return await response.json()
  }
}
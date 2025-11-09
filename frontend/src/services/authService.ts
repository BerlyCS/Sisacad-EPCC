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
    try {
      const response = await fetch(`${API_BASE_URL}/user/me`, {
        credentials: 'include',
      })

      if (response.status === 401 || response.status === 403) {
        return { authenticated: false }
      }

      if (!response.ok) {
        return { authenticated: false }
      }

      return await response.json()
    } catch (error) {
      console.warn('Error obteniendo usuario:', error)
      return { authenticated: false }
    }
  },

  async logout(): Promise<{ message: string }> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/logout`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      })

      if (!response.ok) {
        throw new Error('Error durante el logout')
      }

      return await response.json()
    } catch (error) {
      console.warn('Error en logout API, usando fallback:', error)
      throw new Error('Usando fallback de logout')
    }
  },

  async demoLogin(role: string) {
    const response = await fetch(`${API_BASE_URL}/auth/demo-login`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ role })
    })

    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({}))
      const message = errorBody.error || 'No se pudo iniciar sesión de demostración'
      throw new Error(message)
    }

    return await response.json()
  }
}
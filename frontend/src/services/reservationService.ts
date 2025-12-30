// frontend/src/services/reservationService.ts

const API_BASE_URL = 'http://localhost:8080/api';

export interface Reservation {
  id?: number;
  classroomName: string;
  reservedBy: string;
  purpose: string;
  schedule: {
    dayOfWeek: string;
    startTime: string;
    endTime: string;
  };
  reservationDate?: string;
  createdAt?: string;
  ownedByCurrentUser?: boolean;
}

export interface CreateReservationPayload {
  classroomName: string;
  purpose: string;
  reservationDate: string; // yyyy-MM-dd
  startTime: string; // HH:mm
  endTime: string;   // HH:mm
}

export interface ClassroomSchedule {
  classroomName: string;
  schedule: {
    [day: string]: Array<{
      startTime: string;
      endTime: string;
      courseName: string;
      type: 'FIXED' | 'RESERVED' | 'AVAILABLE';
    }>;
  };
}

export interface WeeklyAvailability {
  classroomName: string;
  days: string[];
  timeSlots: string[];
  availability: boolean[][];
}

class ReservationService {
  private async fetchWithAuth(url: string, options: RequestInit = {}) {
    const response = await fetch(`${API_BASE_URL}${url}`, {
      ...options,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });

    if (!response.ok) {
      let message = `HTTP error ${response.status}`;
      try {
        const data = await response.json();
        const apiMessage = (data as any)?.message || (data as any)?.error;
        if (apiMessage) {
          message = apiMessage;
        }
      } catch (_) {
        try {
          const text = await response.text();
          if (text) {
            message = text;
          }
        } catch (_) {
          /* ignore */
        }
      }
      const error: any = new Error(message);
      error.status = response.status;
      throw error;
    }

    return response.json();
  }

  async getAvailableClassrooms(): Promise<string[]> {
    return this.fetchWithAuth('/reservations/classrooms');
  }

  async getClassroomSchedule(classroomName: string): Promise<ClassroomSchedule> {
    return this.fetchWithAuth(`/reservations/schedule/${encodeURIComponent(classroomName)}`);
  }

  async getWeeklyAvailability(classroomName: string): Promise<WeeklyAvailability> {
    return this.fetchWithAuth(`/reservations/weekly-availability/${encodeURIComponent(classroomName)}`);
  }

  async checkAvailability(
    classroomName: string,
    reservationDate: string,
    startTime: string,
    endTime: string
  ): Promise<boolean> {
    const params = new URLSearchParams({
      reservationDate,
      startTime,
      endTime
    });
    
    const response = await this.fetchWithAuth(
      `/reservations/availability/${encodeURIComponent(classroomName)}?${params}`
    );
    return response.available;
  }

  async createReservation(payload: CreateReservationPayload): Promise<Reservation> {
    return this.fetchWithAuth('/reservations', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async getMyReservations(): Promise<Reservation[]> {
    return this.fetchWithAuth('/reservations/my-reservations');
  }

  async getAllReservations(): Promise<Reservation[]> {
    return this.fetchWithAuth('/reservations');
  }

  async getReservationsByClassroom(classroomName: string): Promise<Reservation[]> {
    return this.fetchWithAuth(`/reservations/classroom/${encodeURIComponent(classroomName)}`);
  }

  async deleteReservation(id: number): Promise<void> {
    await this.fetchWithAuth(`/reservations/${id}`, {
      method: 'DELETE',
    });
  }
}

export const reservationService = new ReservationService();

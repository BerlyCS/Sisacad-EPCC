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
  createdAt?: string;
  status: string;
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
      throw new Error(`HTTP error! status: ${response.status}`);
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
    dayOfWeek: string, 
    startTime: string, 
    endTime: string
  ): Promise<boolean> {
    const params = new URLSearchParams({
      dayOfWeek,
      startTime,
      endTime
    });
    
    const response = await this.fetchWithAuth(
      `/reservations/availability/${encodeURIComponent(classroomName)}?${params}`
    );
    return response.available;
  }

  async createReservation(reservation: Omit<Reservation, 'id' | 'reservedBy' | 'createdAt'>): Promise<Reservation> {
    return this.fetchWithAuth('/reservations', {
      method: 'POST',
      body: JSON.stringify(reservation),
    });
  }

  async getMyReservations(): Promise<Reservation[]> {
    return this.fetchWithAuth('/reservations/my-reservations');
  }

  async getAllReservations(): Promise<Reservation[]> {
    return this.fetchWithAuth('/reservations');
  }

  async updateReservationStatus(id: number, status: string): Promise<Reservation> {
    return this.fetchWithAuth(`/reservations/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
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

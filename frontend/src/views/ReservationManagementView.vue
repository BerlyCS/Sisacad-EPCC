<!-- frontend/src/views/ReservationManagementView.vue -->
<template>
  <AdminLayout>
    <div class="reservation-management-container">
      <div class="header">
        <h1>Gestión de Reservas</h1>
        <p>Administra y aprueba las reservas de aulas pendientes</p>
      </div>

      <!-- Filtros -->
      <div class="filters">
        <div class="filter-group">
          <label>Filtrar por estado:</label>
          <select v-model="statusFilter" @change="loadReservations">
            <option value="ALL">Todas</option>
            <option value="PENDING">Pendientes</option>
            <option value="APPROVED">Aprobadas</option>
            <option value="REJECTED">Rechazadas</option>
          </select>
        </div>
      </div>

      <!-- Lista de Reservas -->
      <div class="reservations-list">
        <div v-if="loading" class="loading">
          <i class="fas fa-spinner fa-spin"></i>
          Cargando reservas...
        </div>

        <div v-else-if="reservations.length === 0" class="empty-state">
          <i class="fas fa-calendar-times"></i>
          <p>No hay reservas {{ statusFilter !== 'ALL' ? statusFilter.toLowerCase() : '' }}</p>
        </div>

        <div v-else class="reservation-cards">
          <div 
            v-for="reservation in reservations" 
            :key="reservation.id"
            class="reservation-card"
            :class="reservation.status.toLowerCase()"
          >
            <div class="reservation-header">
              <h3>{{ reservation.classroomName }}</h3>
              <span class="status-badge" :class="reservation.status.toLowerCase()">
                {{ getStatusText(reservation.status) }}
              </span>
            </div>

            <div class="reservation-details">
              <div class="detail-item">
                <i class="fas fa-user"></i>
                <span><strong>Solicitado por:</strong> {{ reservation.reservedBy }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-calendar"></i>
                <span><strong>Día:</strong> {{ reservation.schedule.dayOfWeek }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-clock"></i>
                <span><strong>Horario:</strong> {{ reservation.schedule.startTime }} - {{ reservation.schedule.endTime }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-file-alt"></i>
                <span><strong>Propósito:</strong> {{ reservation.purpose }}</span>
              </div>
              <div class="detail-item">
                <i class="fas fa-calendar-plus"></i>
                <span><strong>Solicitado:</strong> {{ formatDate(reservation.createdAt) }}</span>
              </div>
            </div>

            <!-- Acciones para reservas pendientes -->
            <div v-if="reservation.status === 'PENDING'" class="reservation-actions">
              <button 
                class="btn-approve"
                @click="updateReservationStatus(reservation.id, 'APPROVED')"
                :disabled="updating"
              >
                <i class="fas fa-check"></i>
                Aprobar
              </button>
              <button 
                class="btn-reject"
                @click="updateReservationStatus(reservation.id, 'REJECTED')"
                :disabled="updating"
              >
                <i class="fas fa-times"></i>
                Rechazar
              </button>
            </div>

            <!-- Información para reservas aprobadas/rechazadas -->
            <div v-else class="reservation-info">
              <p><strong>Estado:</strong> {{ getStatusText(reservation.status) }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal de confirmación -->
      <div v-if="showConfirmModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>Confirmar acción</h3>
            <button class="close-button" @click="showConfirmModal = false">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="modal-body">
            <p>¿Estás seguro de que deseas {{ pendingAction === 'APPROVED' ? 'aprobar' : 'rechazar' }} esta reserva?</p>
            <div class="modal-actions">
              <button class="btn-secondary" @click="showConfirmModal = false">
                Cancelar
              </button>
              <button 
                class="btn-primary" 
                :class="pendingAction === 'APPROVED' ? 'btn-approve' : 'btn-reject'"
                @click="confirmStatusUpdate"
                :disabled="updating"
              >
                <span v-if="updating">
                  <i class="fas fa-spinner fa-spin"></i>
                  Procesando...
                </span>
                <span v-else>
                  {{ pendingAction === 'APPROVED' ? 'Aprobar' : 'Rechazar' }}
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Error Message -->
      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        {{ error }}
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import AdminLayout from '@/components/TopBar.vue';
import { reservationService, type Reservation } from '@/services/reservationService';

const reservations = ref<Reservation[]>([]);
const loading = ref(false);
const updating = ref(false);
const error = ref('');
const statusFilter = ref('PENDING');
const showConfirmModal = ref(false);
const pendingReservationId = ref<number | null>(null);
const pendingAction = ref<'APPROVED' | 'REJECTED'>('APPROVED');

const loadReservations = async () => {
  try {
    loading.value = true;
    error.value = '';
    
    const allReservations = await reservationService.getAllReservations();
    
    if (statusFilter.value === 'ALL') {
      reservations.value = allReservations;
    } else {
      reservations.value = allReservations.filter(
        reservation => reservation.status === statusFilter.value
      );
    }
    
    // Ordenar por fecha de creación (más recientes primero)
    reservations.value.sort((a, b) => {
      const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      return bTime - aTime;
    });
    
  } catch (err) {
    error.value = 'Error al cargar las reservas';
    console.error('Error loading reservations:', err);
  } finally {
    loading.value = false;
  }
};

const updateReservationStatus = (reservationId: number | undefined, status: 'APPROVED' | 'REJECTED') => {
  if (reservationId === undefined || reservationId === null) {
    error.value = 'No se pudo identificar la reserva seleccionada.';
    return;
  }

  pendingReservationId.value = reservationId;
  pendingAction.value = status;
  showConfirmModal.value = true;
};

const confirmStatusUpdate = async () => {
  if (pendingReservationId.value === null) return;
  
  try {
    updating.value = true;
    error.value = '';
    
    await reservationService.updateReservationStatus(pendingReservationId.value, pendingAction.value);
    
    // Recargar la lista
    await loadReservations();
    
    showConfirmModal.value = false;
    pendingReservationId.value = null;
    
  } catch (err: any) {
    error.value = err.message || 'Error al actualizar el estado de la reserva';
    console.error('Error updating reservation status:', err);
  } finally {
    updating.value = false;
  }
};

const getStatusText = (status: string) => {
  const statusMap: { [key: string]: string } = {
    'PENDING': 'Pendiente',
    'APPROVED': 'Aprobada',
    'REJECTED': 'Rechazada'
  };
  return statusMap[status] || status;
};

const formatDate = (dateString?: string) => {
  if (!dateString) {
    return 'Sin fecha';
  }

  const date = new Date(dateString);
  return date.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  loadReservations();
});
</script>

<style scoped>
.reservation-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.header p {
  color: #7f8c8d;
  font-size: 1.1em;
}

.filters {
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-group label {
  font-weight: 600;
  color: #2c3e50;
}

.filter-group select {
  padding: 8px 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-size: 1em;
}

.reservations-list {
  min-height: 400px;
}

.loading, .empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
  font-size: 1.1em;
}

.empty-state i {
  font-size: 3em;
  margin-bottom: 20px;
  color: #bdc3c7;
}

.reservation-cards {
  display: grid;
  gap: 20px;
}

.reservation-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  border-left: 6px solid #f39c12;
  transition: all 0.3s ease;
}

.reservation-card.pending {
  border-left-color: #f39c12;
}

.reservation-card.approved {
  border-left-color: #2ecc71;
}

.reservation-card.rejected {
  border-left-color: #e74c3c;
}

.reservation-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.reservation-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 1.3em;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.approved {
  background: #d4edda;
  color: #155724;
}

.status-badge.rejected {
  background: #f8d7da;
  color: #721c24;
}

.reservation-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  color: #495057;
}

.detail-item i {
  width: 16px;
  color: #6c757d;
}

.reservation-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.btn-approve, .btn-reject {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover:not(:disabled) {
  background: #218838;
  transform: translateY(-1px);
}

.btn-reject {
  background: #dc3545;
  color: white;
}

.btn-reject:hover:not(:disabled) {
  background: #c82333;
  transform: translateY(-1px);
}

.btn-approve:disabled, .btn-reject:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.reservation-info {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
  color: #495057;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e9ecef;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.2em;
  color: #6c757d;
  cursor: pointer;
  padding: 5px;
}

.close-button:hover {
  color: #495057;
}

.modal-body {
  padding: 20px;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 15px;
  border-radius: 8px;
  margin-top: 20px;
  border-left: 4px solid #e74c3c;
}

.error-message i {
  margin-right: 10px;
}

@media (max-width: 768px) {
  .reservation-management-container {
    padding: 10px;
  }
  
  .reservation-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .reservation-actions {
    flex-direction: column;
  }
  
  .modal-actions {
    flex-direction: column;
  }
  
  .detail-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
</style>

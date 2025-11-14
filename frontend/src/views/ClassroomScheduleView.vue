<template>
  <AdminLayout>
    <div class="p-5">
      <!-- Header -->
      <div class="flex justify-between items-start mb-8 flex-wrap gap-6">
        <button class="flex items-center gap-2 bg-gray-400 hover:bg-gray-500 transition-all duration-200 px-4 py-2 text-white rounded-xl cursor-pointer text-md" @click="$router.push('/classrooms')">
          <ArrowLeftIcon class="w-5 h-5"/>
          Volver
        </button>
        <div>
          <h1 class="text-gray-700 m-0 text-2xl">{{ classroomName }}</h1>
          <p class="text-gray-600 mt-1 text-md">Horario semanal - Seleccione un espacio disponible para reservar</p>
        </div>
        <div class="flex lg:flex-col lg:gap-2 gap-5">
          <div class="flex items-center gap-2 text-sm">
            <div class="w-5 h-5 rounded-md border-1 border-gray-300 bg-red-500"></div>
            <span>Ocupado (Horario fijo)</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <div class="w-5 h-5 rounded-md border-1 border-gray-300 bg-green-500"></div>
            <span>Disponible</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <div class="w-5 h-5 rounded-md border-1 border-gray-300 bg-yellow-500"></div>
            <span>Reservado</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <div class="w-5 h-5 rounded-md border-1 border-gray-300 bg-white"></div>
            <span>Pendiente de aprobación</span>
          </div>
        </div>
      </div>

      <!-- Schedule Table -->
      <div class="flex bg-white rounded-2xl shadow-lg overflow-hidden">
        <div class="w-30 flex-shrink-0 bg-gray-100 border-r-2 border-gray-200">
          <div class="p-5 bg-[#3498db] text-white font-bold text-center h-15">Hora</div>
          <div 
            v-for="timeSlot in timeSlots" 
            :key="timeSlot"
            class="px-4 py-3 border-b border-gray-200 text-center text-sm font-medium text-gray-700 h-15 flex items-center justify-center"
          >
            {{ timeSlot }}
          </div>
        </div>

        <div class="flex">
          <div 
            v-for="day in days" 
            :key="day"
            class="flex-1 min-w-0"
          >
            <div class="p-5 bg-[#3498db] text-white font-bold text-center h-15">{{ day }}</div>
            <div
              v-for="timeSlot in timeSlots"
              :key="`${day}-${timeSlot}`"
              class="schedule-cell"
              :class="getCellClass(day, timeSlot)"
              @click="handleCellClick(day, timeSlot)"
              @mouseenter="showTooltip(day, timeSlot, $event)"
              @mouseleave="hideTooltip"
            >
              <div class="cell-content">
                <div v-if="getCellContent(day, timeSlot)" class="course-name">
                  {{ getCellContent(day, timeSlot) }}
                </div>
                <div v-else-if="isAvailable(day, timeSlot)" class="available-label">
                  <i class="fas fa-plus"></i>
                  Disponible
                </div>
                <div v-else class="no-class">
                  -
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tooltip -->
      <div v-if="showTooltipContent" class="tooltip" :style="tooltipStyle">
        <strong>{{ tooltipContent.courseName }}</strong>
        <br>
        {{ tooltipContent.day }} {{ tooltipContent.timeSlot }}
      </div>

      <!-- Reservation Modal -->
      <div v-if="showReservationModal" class="modal-overlay">
        <div class="modal-content">
          <div class="modal-header">
            <h3>Reservar Aula</h3>
            <button class="close-button" @click="closeModal">
              <XMarkIcon class="w-5 h-5"/>
            </button>
          </div>
          
          <div class="modal-body">
            <div class="reservation-info">
              <p><strong>Aula:</strong> {{ classroomName }}</p>
              <p><strong>Día:</strong> {{ selectedSlot?.day }}</p>
              <p><strong>Horario:</strong> {{ selectedSlot?.timeSlot }}</p>
            </div>

            <form @submit.prevent="submitReservation" class="reservation-form">
              <div class="form-group">
                <label for="purpose">Propósito de la reserva:</label>
                <textarea
                  id="purpose"
                  v-model="reservationForm.purpose"
                  placeholder="Describa el propósito de esta reserva..."
                  rows="4"
                  required
                ></textarea>
              </div>

              <div class="form-actions">
                <button 
                  type="button" 
                  class="btn-secondary" 
                  @click="closeModal"
                  :disabled="loading"
                >
                  Cancelar
                </button>
                <button 
                  type="submit" 
                  class="btn-primary" 
                  :disabled="loading || !reservationForm.purpose"
                >
                  <span v-if="loading">
                    <i class="fas fa-spinner fa-spin"></i>
                    Procesando...
                  </span>
                  <span v-else>
                    <i class="fas fa-calendar-plus"></i>
                    Confirmar Reserva
                  </span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center p-10 text-blue-500 text-md">
        <i class="fas fa-spinner fa-spin"></i>
        Cargando horario...
      </div>

      <!-- Error State -->
      <div v-if="error" class="text-center p-5 bg-[#f8d7da] text-[#721c24] rounded-lg mt-5">
        <ExclamationTriangleIcon class="w-10 h-10 inline-block mb-3"/>
        <p>{{ error }}</p>
      </div>
    </div>
  </AdminLayout>  
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AdminLayout from '../components/ui/TopBar.vue';
import { reservationService, type Reservation } from '@/services/reservationService';
import { ArrowLeftIcon, XMarkIcon, ExclamationTriangleIcon } from '@heroicons/vue/16/solid';

const route = useRoute();
const router = useRouter();

const classroomName = ref('');
const scheduleData = ref<any>({});
const reservations = ref<Reservation[]>([]); // ← NUEVO: Reservas de la BD
const loading = ref(false);
const error = ref('');
const showReservationModal = ref(false);
const selectedSlot = ref<{day: string; timeSlot: string} | null>(null);
const reservationForm = ref({
  purpose: ''
});

// Estados para el tooltip
const showTooltipContent = ref(false);
const tooltipContent = ref({
  courseName: '',
  day: '',
  timeSlot: '',
  type: '' // ← NUEVO: Tipo de ocupación
});
const tooltipStyle = ref({
  left: '0px',
  top: '0px'
});

// Días y horarios predefinidos
const days = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES'];
const timeSlots = [
  '7:00-7:50', '7:50-8:40', '8:50-09:40', '09:40-10:30',
  '10:40-11:30', '11:30-12:20', '12:20-13:10', '13:10-14:00',
  '14:00-14:50', '14:50-15:40', '15:50-16:40', '16:40-17:30',
  '17:40-18:30', '18:30-19:20', '19:20-20:10'
];

// Mapeo de timeSlots a rangos de tiempo para búsqueda
const timeSlotMap = computed(() => {
  const map: { [key: string]: { start: string; end: string } } = {};
  timeSlots.forEach(slot => {
    const [start = '', end = ''] = slot.split('-');
    map[slot] = { start, end };
  });
  return map;
});

const loadSchedule = async () => {
  try {
    loading.value = true;
    error.value = '';
    const classroom = decodeURIComponent(route.params.classroomName as string);
    classroomName.value = classroom;
    
    // Cargar horario fijo del Excel
    const schedule = await reservationService.getClassroomSchedule(classroom);
    scheduleData.value = schedule;
    
    // ← NUEVO: Cargar reservas de la base de datos
    const classroomReservations = await reservationService.getReservationsByClassroom(classroom);
    reservations.value = classroomReservations.filter(reservation => 
      reservation.status !== 'REJECTED' // Solo mostrar reservas activas
    );
    
    console.log('Loaded schedule:', schedule);
    console.log('Loaded reservations:', reservations.value);
    
  } catch (err) {
    error.value = 'Error al cargar el horario del aula';
    console.error('Error loading schedule:', err);
  } finally {
    loading.value = false;
  }
};

// ← NUEVO: Buscar reserva en un horario específico
const getReservationForTimeSlot = (day: string, timeSlot: string) => {
  const slotInfo = timeSlotMap.value[timeSlot];
  if (!slotInfo) return null;

  return reservations.value.find(reservation => 
    reservation.schedule.dayOfWeek === day &&
    reservation.schedule.startTime === slotInfo.start &&
    reservation.schedule.endTime === slotInfo.end
  );
};

const getCourseForTimeSlot = (day: string, timeSlot: string) => {
  if (!scheduleData.value.schedule || !scheduleData.value.schedule[day]) {
    return null;
  }

  const slotInfo = timeSlotMap.value[timeSlot];
  if (!slotInfo) return null;

  const daySchedule = scheduleData.value.schedule[day];
  
  // Buscar el curso que coincide con este horario exacto
  return daySchedule.find((course: any) => 
    course.startTime === slotInfo.start && 
    course.endTime === slotInfo.end
  );
};

// ← MODIFICADO: Ahora también muestra información de reservas
const getCellContent = (day: string, timeSlot: string) => {
  const course = getCourseForTimeSlot(day, timeSlot);
  if (course) {
    return course.courseName;
  }
  
  const reservation = getReservationForTimeSlot(day, timeSlot);
  if (reservation) {
    if (reservation.status === 'PENDING') {
      return '⏳ Pendiente';
    } else if (reservation.status === 'APPROVED') {
      return `📅 ${reservation.purpose.substring(0, 20)}${reservation.purpose.length > 20 ? '...' : ''}`;
    }
  }
  
  return '';
};

// ← MODIFICADO: Verificar disponibilidad considerando reservas
const isAvailable = (day: string, timeSlot: string) => {
  const course = getCourseForTimeSlot(day, timeSlot);
  if (course) return false; // Ocupado por curso fijo
  
  const reservation = getReservationForTimeSlot(day, timeSlot);
  if (reservation) return false; // Ocupado por reserva
  
  return true; // Realmente disponible
};

// ← MODIFICADO: Nueva lógica para clases de celdas
const getCellClass = (day: string, timeSlot: string) => {
  const course = getCourseForTimeSlot(day, timeSlot);
  if (course) {
    return 'occupied'; // Curso fijo del Excel
  }
  
  const reservation = getReservationForTimeSlot(day, timeSlot);
  if (reservation) {
    if (reservation.status === 'PENDING') {
      return 'pending'; // Reserva pendiente
    } else if (reservation.status === 'APPROVED') {
      return 'reserved'; // Reserva aprobada
    }
  }
  
  return 'available'; // Disponible
};

// ← MODIFICADO: Manejar click en celda
const handleCellClick = async (day: string, timeSlot: string) => {
  const course = getCourseForTimeSlot(day, timeSlot);
  const reservation = getReservationForTimeSlot(day, timeSlot);
  
  // Si está ocupado, no hacer nada
  if (course || reservation) {
    return;
  }
  
  // Verificar disponibilidad específica
  const slotInfo = timeSlotMap.value[timeSlot];
  if (!slotInfo) {
    error.value = 'Horario inválido';
    return;
  }
  try {
    const available = await reservationService.checkAvailability(
      classroomName.value,
      day,
      slotInfo.start,
      slotInfo.end
    );
    
    if (available) {
      selectedSlot.value = { day, timeSlot };
      reservationForm.value.purpose = '';
      showReservationModal.value = true;
    } else {
      error.value = 'Este horario ya no está disponible';
    }
  } catch (err) {
    error.value = 'Error al verificar disponibilidad';
    console.error('Error checking availability:', err);
  }
};

const showTooltip = (day: string, timeSlot: string, event: MouseEvent) => {
  const course = getCourseForTimeSlot(day, timeSlot);
  const reservation = getReservationForTimeSlot(day, timeSlot);
  
  if (course) {
    tooltipContent.value = {
      courseName: course.courseName,
      day: day,
      timeSlot: timeSlot,
      type: 'CURSO_FIJO'
    };
  } else if (reservation) {
    let statusText = '';
    if (reservation.status === 'PENDING') {
      statusText = '⏳ Pendiente de aprobación';
    } else if (reservation.status === 'APPROVED') {
      statusText = '✅ Reserva aprobada';
    }
    
    tooltipContent.value = {
      courseName: `${reservation.purpose} (${statusText})`,
      day: day,
      timeSlot: timeSlot,
      type: 'RESERVA'
    };
  } else {
    return; // No mostrar tooltip para celdas disponibles
  }
  
  tooltipStyle.value = {
    left: `${event.clientX + 10}px`,
    top: `${event.clientY + 10}px`
  };
  
  showTooltipContent.value = true;
};

const hideTooltip = () => {
  showTooltipContent.value = false;
};

const submitReservation = async () => {
  if (!selectedSlot.value) return;
  
  try {
    loading.value = true;
    const slotInfo = timeSlotMap.value[selectedSlot.value.timeSlot];
    if (!slotInfo) {
      error.value = 'Horario inválido';
      return;
    }
    
    await reservationService.createReservation({
      classroomName: classroomName.value,
      purpose: reservationForm.value.purpose,
      schedule: {
        dayOfWeek: selectedSlot.value.day,
        startTime: slotInfo.start,
        endTime: slotInfo.end
      },
      status: 'PENDING'
    });
    
    closeModal();
    // Recargar el horario para reflejar la nueva reserva
    await loadSchedule();
    
    // Mostrar mensaje de éxito
    alert('Reserva creada exitosamente. Está pendiente de aprobación.');
    
  } catch (err: any) {
    error.value = err.message || 'Error al crear la reserva';
    console.error('Error creating reservation:', err);
  } finally {
    loading.value = false;
  }
};

const closeModal = () => {
  showReservationModal.value = false;
  selectedSlot.value = null;
  reservationForm.value.purpose = '';
};

onMounted(() => {
  loadSchedule();
});
</script>

<style scoped>
.color-box {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.color-box.occupied {
  background: #e74c3c;
}

.color-box.available {
  background: #2ecc71;
}

.color-box.reserved {
  background: #f39c12;
}

.schedule-cell {
  height: 60px;
  border-bottom: 1px solid #e9ecef;
  border-right: 1px solid #e9ecef;
  padding: 5px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.schedule-cell:hover {
  transform: scale(1.02);
  z-index: 1;
}

.schedule-cell.available {
  background: #d4edda;
  border: 2px solid #c3e6cb;
}

.schedule-cell.available:hover {
  background: #c3e6cb;
  box-shadow: 0 2px 8px rgba(46, 204, 113, 0.3);
}

.schedule-cell.occupied {
  background: #f8d7da;
  border: 2px solid #f5c6cb;
  cursor: not-allowed;
}

.schedule-cell.reserved {
  background: #fff3cd;
  border: 2px solid #ffeaa7;
  cursor: not-allowed;
}

.cell-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 5px;
  font-size: 0.8em;
}

/* ESTILOS NUEVOS PARA LOS NOMBRES DE CURSOS */
.course-name {
  color: #721c24;
  font-weight: 500;
  line-height: 1.2;
  font-size: 0.75em;
  overflow: hidden;
  display: -webkit-box;
  line-clamp: 3;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  max-height: 50px;
}

.no-class {
  color: #6c757d;
  font-style: italic;
  font-size: 0.8em;
}

.available-label {
  color: #155724;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.8em;
}

/* ESTILOS PARA EL TOOLTIP */
.tooltip {
  position: fixed;
  background: rgba(0, 0, 0, 0.9);
  color: white;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.85em;
  z-index: 1000;
  pointer-events: none;
  max-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.tooltip strong {
  color: #3498db;
  display: block;
  margin-bottom: 4px;
  font-size: 0.9em;
}

/* MEJORAS VISUALES PARA LAS CELDAS OCUPADAS */
.schedule-cell.occupied .cell-content {
  background: rgba(231, 76, 60, 0.1);
  border-radius: 4px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
}

.schedule-cell.available .cell-content {
  background: rgba(46, 204, 113, 0.1);
  border-radius: 4px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
}

.schedule-cell.reserved .cell-content {
  background: rgba(243, 156, 18, 0.1);
  border-radius: 4px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 4px;
}

/* MEJORAR LA LEGIBILIDAD DEL TEXTO EN CELDAS OCUPADAS */
.schedule-cell.occupied {
  background: linear-gradient(135deg, #f8d7da, #f1b0b7);
  border: 2px solid #f5c6cb;
}

.schedule-cell.available {
  background: linear-gradient(135deg, #d4edda, #c3e6cb);
  border: 2px solid #c3e6cb;
}

.schedule-cell.reserved {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  border: 2px solid #ffeaa7;
}

/* EFECTO HOVER MEJORADO */
.schedule-cell.available:hover {
  background: linear-gradient(135deg, #c3e6cb, #b1dfbb);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.4);
  transform: translateY(-1px);
}

.schedule-cell.occupied:hover {
  background: linear-gradient(135deg, #f1b0b7, #ea9a9a);
  transform: translateY(-1px);
}

.schedule-cell.reserved:hover {
  background: linear-gradient(135deg, #ffeaa7, #ffdf7e);
  transform: translateY(-1px);
}

/* MODAL STYLES */
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
  max-height: 90vh;
  overflow-y: auto;
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
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.close-button:hover {
  color: #495057;
  background: #f8f9fa;
}

.modal-body {
  padding: 20px;
}

.reservation-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #3498db;
}

.reservation-info p {
  margin: 5px 0;
  color: #495057;
  font-size: 0.95em;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #495057;
  font-size: 0.95em;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 2px solid #e9ecef;
  border-radius: 6px;
  font-family: inherit;
  font-size: 1em;
  resize: vertical;
  transition: all 0.2s ease;
  background: #f8f9fa;
}

.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
  background: white;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 25px;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 1em;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
}

.btn-primary:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background: #5a6268;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  border-radius: 8px;
  margin: 20px 0;
  border-left: 4px solid #e74c3c;
}
</style>

<template>
  <AdminLayout>
    <div class="reservation-management-container">
      <section class="header">
        <div>
          <h1>Reservas de aulas</h1>
          <p>Registra y consulta reservas confirmadas al instante.</p>
        </div>
      </section>

      <section class="card">
        <h2>Nueva reserva</h2>
        <form class="form-grid" @submit.prevent="submitReservation">
          <label>
            Aula
            <select v-model="form.classroomName" required>
              <option value="" disabled>Seleccione un aula</option>
              <option v-for="room in classrooms" :key="room" :value="room">{{ room }}</option>
            </select>
          </label>

          <label>
            Fecha
            <input v-model="form.reservationDate" type="date" required />
          </label>

          <label>
            Inicio
            <input v-model="form.startTime" type="time" step="60" required />
          </label>

          <label>
            Fin
            <input v-model="form.endTime" type="time" step="60" required />
          </label>

          <label class="full-width">
            Propósito
            <textarea v-model="form.purpose" rows="3" placeholder="Describe el motivo" required />
          </label>

          <div class="form-actions full-width">
            <button type="submit" :disabled="creating">
              <span v-if="creating">
                <i class="fas fa-spinner fa-spin" /> Procesando...
              </span>
              <span v-else>Guardar</span>
            </button>
          </div>
        </form>
        <p class="helper-text">Los horarios no requieren aprobación previa; evita choques con cursos y otras reservas.</p>
      </section>

      <section class="card">
        <div class="filters">
          <div>
            <label>Aula</label>
            <select v-model="filters.classroom">
              <option value="ALL">Todas</option>
              <option v-for="room in classrooms" :key="room" :value="room">{{ room }}</option>
            </select>
          </div>
          <div>
            <label>Fecha</label>
            <input v-model="filters.date" type="date" />
          </div>
          <button type="button" class="reset" @click="resetFilters" :disabled="filters.classroom === 'ALL' && !filters.date">
            Limpiar filtros
          </button>
        </div>

        <div v-if="loading" class="state">
          <i class="fas fa-spinner fa-spin" /> Cargando reservas...
        </div>
        <div v-else-if="filteredReservations.length === 0" class="state">
          <i class="fas fa-calendar-times" /> No hay reservas que coincidan.
        </div>
        <div v-else class="reservation-list">
          <article v-for="reservation in filteredReservations" :key="reservation.id" class="reservation-card">
            <header>
              <div>
                <p class="classroom">{{ reservation.classroomName }}</p>
                <p class="date">{{ formatDate(reservation.reservationDate) }}</p>
              </div>
              <p class="time">{{ reservation.schedule.startTime }} - {{ reservation.schedule.endTime }}</p>
            </header>
            <p class="purpose">{{ reservation.purpose }}</p>
            <footer>
              Reservado por {{ reservation.reservedBy || 'Usuario' }}
            </footer>
          </article>
        </div>
      </section>

      <div v-if="error" class="error">
        <i class="fas fa-exclamation-triangle" />
        {{ error }}
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import AdminLayout from '@/components/ui/TopBar.vue';
import { reservationService, type Reservation, type CreateReservationPayload } from '@/services/reservationService';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const canViewAll = computed(() => auth.isAdmin || auth.isSecretary);

const classrooms = ref<string[]>([]);
const reservations = ref<Reservation[]>([]);
const loading = ref(false);
const creating = ref(false);
const error = ref('');

const form = ref<CreateReservationPayload>({
  classroomName: '',
  reservationDate: '',
  startTime: '',
  endTime: '',
  purpose: ''
});

const filters = ref({
  classroom: 'ALL',
  date: ''
});

const filteredReservations = computed(() => {
  return reservations.value
    .filter(reservation => {
      const matchesClassroom = filters.value.classroom === 'ALL' || reservation.classroomName === filters.value.classroom;
      const matchesDate = !filters.value.date || reservation.reservationDate === filters.value.date;
      return matchesClassroom && matchesDate;
    })
    .sort((a, b) => {
      const aKey = `${a.reservationDate ?? ''} ${a.schedule?.startTime ?? ''}`;
      const bKey = `${b.reservationDate ?? ''} ${b.schedule?.startTime ?? ''}`;
      return bKey.localeCompare(aKey);
    });
});

const resetFilters = () => {
  filters.value = { classroom: 'ALL', date: '' };
};

const loadClassrooms = async () => {
  try {
    classrooms.value = await reservationService.getAvailableClassrooms();
    if (!form.value.classroomName && classrooms.value.length) {
      form.value.classroomName = classrooms.value[0] ?? '';
    }
  } catch (err) {
    error.value = 'No se pudieron cargar las aulas disponibles.';
    console.error(err);
  }
};

const loadReservations = async () => {
  try {
    loading.value = true;
    error.value = '';
    const data = canViewAll.value
      ? await reservationService.getAllReservations()
      : await reservationService.getMyReservations();
    reservations.value = data;
  } catch (err) {
    error.value = 'No se pudieron cargar las reservas.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const submitReservation = async () => {
  if (!form.value.classroomName || !form.value.reservationDate || !form.value.startTime || !form.value.endTime || !form.value.purpose) {
    error.value = 'Complete todos los campos antes de crear la reserva.';
    return;
  }

  try {
    creating.value = true;
    error.value = '';

    const available = await reservationService.checkAvailability(
      form.value.classroomName,
      form.value.reservationDate,
      form.value.startTime,
      form.value.endTime
    );

    if (!available) {
      error.value = 'El aula no está disponible en ese horario.';
      return;
    }

    await reservationService.createReservation({ ...form.value });
    form.value.reservationDate = '';
    form.value.startTime = '';
    form.value.endTime = '';
    form.value.purpose = '';
    await loadReservations();
  } catch (err: any) {
    error.value = err?.message || 'No se pudo crear la reserva.';
    console.error(err);
  } finally {
    creating.value = false;
  }
};

const formatDate = (value?: string) => {
  if (!value) return 'Sin fecha';
  return parseLocalDate(value).toLocaleDateString('es-PE', {
    weekday: 'long',
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

const parseLocalDate = (value: string) => {
  // Avoid UTC parsing that shifts one day for negative timezones
  const [y, m, d] = value.split('-').map(Number);
  return Number.isFinite(y) && Number.isFinite(m) && Number.isFinite(d)
    ? new Date(y, m - 1, d)
    : new Date(value);
};

onMounted(async () => {
  await Promise.all([loadClassrooms(), loadReservations()]);
});
</script>

<style scoped>
.reservation-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.header h1 {
  margin: 0;
  color: #2c3e50;
}

.header p {
  margin: 6px 0 0;
  color: #6c757d;
}

.card {
  background: #fff;
  border-radius: 14px;
  padding: 24px;
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.08);
}

.card h2 {
  margin: 0;
  color: #34495e;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: #495057;
}

select,
input,
textarea {
  padding: 10px;
  border-radius: 10px;
  border: 1px solid #dce1e7;
  font-size: 0.95rem;
}

textarea {
  resize: vertical;
}

.full-width {
  grid-column: 1 / -1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.form-actions button {
  background: #3498db;
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 20px;
  cursor: pointer;
  font-weight: 600;
}

.form-actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.helper-text {
  margin-top: 12px;
  color: #6c757d;
  font-size: 0.9rem;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
  margin-bottom: 20px;
}

.filters .reset {
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid #dce1e7;
  background: transparent;
  cursor: pointer;
}

.state {
  text-align: center;
  color: #6c757d;
  padding: 40px 10px;
}

.reservation-list {
  display: grid;
  gap: 16px;
}

.reservation-card {
  border: 1px solid #edf1f7;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.reservation-card header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.reservation-card .classroom {
  margin: 0;
  font-weight: 600;
  color: #2c3e50;
}

.reservation-card .date {
  margin: 0;
  color: #6c757d;
  text-transform: capitalize;
}

.reservation-card .time {
  font-weight: 600;
  color: #0d6efd;
}

.reservation-card .purpose {
  margin: 0;
  color: #495057;
}

.reservation-card footer {
  color: #6c757d;
  font-size: 0.9rem;
}

.error {
  background: #f8d7da;
  color: #721c24;
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid #e74c3c;
}

@media (max-width: 768px) {
  .form-actions {
    justify-content: stretch;
  }

  .form-actions button {
    width: 100%;
  }
}
</style>
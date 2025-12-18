<template>
  <AdminLayout>
    <div class="schedule-page">
      <header class="page-header">
        <div class="page-header__left">
          <button class="back-button" @click="router.back()">
            <span aria-hidden="true">←</span>
            Volver
          </button>
          <div>
            <p class="eyebrow">Agenda semanal</p>
            <h1>{{ classroomName }}</h1>
            <p class="subtitle">Disponible para estudiantes, profesores y secretaría.</p>
          </div>
        </div>
        <div class="week-controls">
          <div class="week-nav">
            <button class="nav" @click="prevWeek" aria-label="Semana anterior">‹</button>
            <div class="week-pill">
              <div class="week-label">{{ formatWeekRange(currentWeekStart) }}</div>
              <button class="today-btn" @click="goToToday" title="Ir a la semana actual">Hoy</button>
            </div>
            <button class="nav" @click="nextWeek" aria-label="Siguiente semana">›</button>
          </div>
          <label class="week-picker" aria-hidden="true">
            <input type="date" :value="isoDateForInput(currentWeekStart)" @change="onDateChange" aria-label="Seleccionar fecha" />
          </label>
          <button v-if="canReserve" class="primary-button" @click="toggleReservationForm">
            <i class="fas fa-plus"></i>
            {{ showReservationForm ? 'Ocultar formulario' : 'Nueva reserva' }}
          </button>
        </div>
      </header>

      <transition name="fade-slide">
        <section v-if="canReserve && showReservationForm" class="card reservation-card">
          <h2>Crear reserva</h2>
          <form class="form-grid" @submit.prevent="submitReservation">
            <label>
              Fecha
              <input v-model="form.reservationDate" type="date" required />
            </label>
            <label>
              Inicio
              <input v-model="form.startTime" type="time" step="300" required />
            </label>
            <label>
              Fin
              <input v-model="form.endTime" type="time" step="300" required />
            </label>
            <label class="full-width">
              Propósito
              <textarea v-model="form.purpose" rows="3" placeholder="Ej. Ensayo, asesoría, práctica extraordinaria" required />
            </label>
            <div class="form-actions full-width">
              <button type="submit" :disabled="creating">
                <span v-if="creating">
                  <i class="fas fa-spinner fa-spin" /> Registrando...
                </span>
                <span v-else>Confirmar reserva</span>
              </button>
            </div>
          </form>
        </section>
      </transition>

      <section class="card schedule-card">
        <div class="schedule-card__header">
          <div>
            <h2>Calendario de lunes a viernes</h2>
            <p>Los cursos y las reservas confirmadas aparecen con colores diferentes.</p>
          </div>
          <div class="legend">
            <span><span class="legend-dot course"></span> Curso programado</span>
            <span><span class="legend-dot reservation"></span> Reserva confirmada</span>
          </div>
        </div>

        <div v-if="gridLoading" class="state">
          <i class="fas fa-spinner fa-spin" /> Cargando horario...
        </div>
        <div v-else class="schedule-grid" role="table" aria-label="Horario semanal">
          <div class="times-column" role="rowgroup">
            <div class="day-header-placeholder"></div>
            <div
              v-for="segment in timeSegments"
              :key="segment.startMinutes"
              class="time-slot"
              role="rowheader"
            >
              {{ segment.label }}
            </div>
          </div>
          <div class="day-columns" role="rowgroup">
            <div
              v-for="day in DAYS"
              :key="day"
              class="day-column"
              role="column"
            >
              <div class="day-header">{{ formatDayLabel(day) }}</div>
              <div class="day-body" :style="slotCountStyle">
                <div
                  v-for="segment in timeSegments"
                  :key="segment.startMinutes"
                  class="slot-guide"
                ></div>
                <div
                  v-for="event in eventsByDay[day]"
                  :key="event.id"
                  class="event-block"
                  :class="event.type"
                  :style="eventStyle(event)"
                >
                  <p class="event-title">{{ event.title }}</p>
                  <p class="event-time">{{ event.start }} - {{ event.end }}</p>
                </div>
              </div>
            </div>
          </div>
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
import { useRoute, useRouter } from 'vue-router';
import AdminLayout from '@/components/ui/TopBar.vue';
import { reservationService, type Reservation, type ClassroomSchedule, type CreateReservationPayload } from '@/services/reservationService';
import { useAuthStore } from '@/stores/auth';

const DAYS = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES'] as const;
const START_MINUTES = 7 * 60;
const END_MINUTES = 21 * 60;
const SLOT_INTERVAL = 20; // minutes (1 hour)

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const classroomName = ref(decodeURIComponent(route.params.classroomName as string));

const canReserve = computed(() => auth.isAdmin || auth.isSecretary || auth.isProfessor);
const showReservationForm = ref(false);

const form = ref<CreateReservationPayload>({
  classroomName: classroomName.value,
  reservationDate: '',
  startTime: '',
  endTime: '',
  purpose: ''
});

const creating = ref(false);
const error = ref('');
const gridLoading = ref(true);
const reservations = ref<Reservation[]>([]);
const courseSlots = ref<Array<{ id: string; day: string; start: string; end: string; title: string }>>([]);

const timeSegments = computed(() => buildTimeSegments(START_MINUTES, END_MINUTES, SLOT_INTERVAL));
const slotCountStyle = computed(() => ({ '--slots-count': timeSegments.value.length }));

// Week selector state: current week start (Monday)
const today = new Date();
const currentWeekStart = ref(startOfWeek(today));

function startOfWeek(d: Date) {
  const copy = new Date(d);
  const day = (copy.getDay() + 6) % 7; // make Monday=0
  copy.setDate(copy.getDate() - day);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

function addDays(date: Date, days: number) {
  const r = new Date(date);
  r.setDate(r.getDate() + days);
  return r;
}

const formatWeekRange = (weekStart: Date) => {
  const start = weekStart;
  const end = addDays(weekStart, 4); // Monday..Friday
  const options: Intl.DateTimeFormatOptions = { day: '2-digit', month: 'short' };
  return `${start.toLocaleDateString('es-PE', options)} — ${end.toLocaleDateString('es-PE', options)}`;
};

const prevWeek = () => {
  currentWeekStart.value = startOfWeek(addDays(currentWeekStart.value, -7));
};
const nextWeek = () => {
  currentWeekStart.value = startOfWeek(addDays(currentWeekStart.value, 7));
};

const goToToday = () => {
  currentWeekStart.value = startOfWeek(new Date());
};

const isoDateForInput = (d: Date) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const onDateChange = (e: Event) => {
  const input = e.target as HTMLInputElement;
  if (!input?.value) return;
  const chosen = new Date(input.value + 'T00:00:00');
  currentWeekStart.value = startOfWeek(chosen);
};

type CalendarEvent = {
  id: string;
  day: string;
  start: string;
  end: string;
  title: string;
  type: 'course' | 'reservation';
  startMinutes: number;
  endMinutes: number;
};

const eventsByDay = computed<Record<string, CalendarEvent[]>>(() => {
  const map: Record<string, CalendarEvent[]> = Object.fromEntries(DAYS.map(day => [day, []]));
  const courseEvents = courseSlots.value.map(slot => toCalendarEvent(slot, 'course'));
  const reservationEvents = reservations.value
    .map(reservation => reservationToEvent(reservation))
    .filter((event): event is CalendarEvent => Boolean(event));

  [...courseEvents, ...reservationEvents].forEach(event => {
    const column = map[event.day];
    if (!column) return;
    column.push(event);
  });

  Object.values(map).forEach(list => list.sort((a, b) => a.startMinutes - b.startMinutes));
  return map;
});

const toggleReservationForm = () => {
  showReservationForm.value = !showReservationForm.value;
};

const loadReservations = async () => {
  try {
    const data = await reservationService.getReservationsByClassroom(classroomName.value);
    reservations.value = data;
  } catch (err) {
    error.value = 'No se pudieron cargar las reservas del aula.';
    console.error(err);
  }
};

const loadCourseSchedule = async () => {
  try {
    const schedule: ClassroomSchedule = await reservationService.getClassroomSchedule(classroomName.value);
    const entries: Array<{ id: string; day: string; start: string; end: string; title: string }> = [];
    Object.entries(schedule.schedule ?? {}).forEach(([day, slots]) => {
      slots.forEach(slot => {
        if (slot.type === 'FIXED') {
          entries.push({
            id: `course-${day}-${slot.startTime}-${slot.endTime}-${slot.courseName}`,
            day: normalizeDay(day),
            start: slot.startTime,
            end: slot.endTime,
            title: slot.courseName ?? 'Curso'
          });
        }
      });
    });
    courseSlots.value = entries;
  } catch (err) {
    error.value = 'No se pudo obtener el horario oficial del aula.';
    console.error(err);
  }
};

const submitReservation = async () => {
  if (!canReserve.value) {
    error.value = 'No tiene permisos para reservar.';
    return;
  }

  if (!form.value.reservationDate || !form.value.startTime || !form.value.endTime || !form.value.purpose) {
    error.value = 'Complete todos los campos para crear la reserva.';
    return;
  }

  try {
    creating.value = true;
    error.value = '';
    form.value.classroomName = classroomName.value;

    const available = await reservationService.checkAvailability(
      classroomName.value,
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
    await Promise.all([loadReservations()]);
  } catch (err: any) {
    error.value = err?.message || 'No se pudo registrar la reserva.';
    console.error(err);
  } finally {
    creating.value = false;
  }
};

const formatDayLabel = (day: string) => {
  const labels: Record<string, string> = {
    LUNES: 'Lunes',
    MARTES: 'Martes',
    MIERCOLES: 'Miércoles',
    JUEVES: 'Jueves',
    VIERNES: 'Viernes'
  };
  return labels[day] ?? day;
};

const eventStyle = (event: CalendarEvent) => {
  const totalMinutes = END_MINUTES - START_MINUTES;
  const clampedStart = Math.max(event.startMinutes, START_MINUTES);
  const clampedEnd = Math.min(event.endMinutes, END_MINUTES);
  const top = ((clampedStart - START_MINUTES) / totalMinutes) * 100;
  const height = Math.max(((clampedEnd - clampedStart) / totalMinutes) * 100, 4);
  return {
    top: `${top}%`,
    height: `${height}%`
  };
};

const parseLocalDate = (value: string) => {
  // Avoid UTC parsing that shifts one day for negative timezones
  const [y, m, d] = value.split('-').map(Number);
  return Number.isFinite(y) && Number.isFinite(m) && Number.isFinite(d)
    ? new Date(y, m - 1, d)
    : new Date(value);
};

const isDateInWeek = (dateStr: string | undefined, weekStart: Date) => {
  if (!dateStr) return false;
  const d = parseLocalDate(dateStr);
  d.setHours(0, 0, 0, 0);
  const weekEnd = addDays(weekStart, 4);
  return d >= weekStart && d <= weekEnd;
};

const reservationToEvent = (reservation: Reservation): CalendarEvent | null => {
  if (!reservation.schedule) return null;
  if (!isDateInWeek(reservation.reservationDate, currentWeekStart.value)) return null;
  const day = resolveDay(reservation);
  if (!day || !DAYS.includes(day as typeof DAYS[number])) return null;
  return {
    id: `reservation-${reservation.id}`,
    day,
    start: reservation.schedule.startTime,
    end: reservation.schedule.endTime,
    title: reservation.purpose,
    type: 'reservation',
    startMinutes: timeToMinutes(reservation.schedule.startTime),
    endMinutes: timeToMinutes(reservation.schedule.endTime)
  };
};

const toCalendarEvent = (
  slot: { id: string; day: string; start: string; end: string; title: string },
  type: 'course' | 'reservation'
): CalendarEvent => ({
  id: slot.id,
  day: slot.day,
  start: slot.start,
  end: slot.end,
  title: slot.title,
  type,
  startMinutes: timeToMinutes(slot.start),
  endMinutes: timeToMinutes(slot.end)
});

const buildTimeSegments = (start: number, end: number, interval: number) => {
  const segments: Array<{ label: string; startMinutes: number; endMinutes: number }> = [];
  for (let minutes = start; minutes < end; minutes += interval) {
    segments.push({
      label: formatMinutes(minutes),
      startMinutes: minutes,
      endMinutes: minutes + interval
    });
  }
  return segments;
};

const formatMinutes = (minutes: number) => {
  const hours = Math.floor(minutes / 60)
    .toString()
    .padStart(2, '0');
  const mins = (minutes % 60).toString().padStart(2, '0');
  return `${hours}:${mins}`;
};

const timeToMinutes = (value: string) => {
  const [hoursPart, minutesPart] = value.split(':');
  const hours = Number(hoursPart ?? 0);
  const minutes = Number(minutesPart ?? 0);
  return hours * 60 + minutes;
};

const normalizeDay = (value: string) =>
  value
    .trim()
    .toUpperCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '');

const resolveDay = (reservation: Reservation) => {
  if (reservation.reservationDate) {
    const date = parseLocalDate(reservation.reservationDate);
    return normalizeDay(date.toLocaleDateString('es-PE', { weekday: 'long' }));
  }
  return reservation.schedule?.dayOfWeek ? normalizeDay(reservation.schedule.dayOfWeek) : '';
};

onMounted(async () => {
  if (!classroomName.value) {
    router.push('/classrooms');
    return;
  }
  gridLoading.value = true;
  try {
    await Promise.all([loadReservations(), loadCourseSchedule()]);
  } finally {
    gridLoading.value = false;
  }
});
</script>

<style scoped>
.schedule-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.page-header__left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.eyebrow {
  margin: 0;
  font-size: 0.85rem;
  text-transform: uppercase;
  color: #6c757d;
  letter-spacing: 0.08em;
}

.subtitle {
  margin: 4px 0 0;
  color: #6c757d;
}

.back-button {
  border: 1px solid #dcdfe3;
  border-radius: 999px;
  padding: 8px 16px;
  background: transparent;
  cursor: pointer;
  color: #2c3e50;
}

.primary-button {
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 999px;
  padding: 12px 20px;
  font-weight: 600;
  display: inline-flex;
  gap: 8px;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 10px 25px rgba(37, 99, 235, 0.25);
}

.week-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.week-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  border: none;
  background: #f1f5f9;
  color: #0f172a;
  font-size: 1.1rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.week-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(37,99,235,0.08), rgba(99,102,241,0.04));
  border: 1px solid rgba(37,99,235,0.08);
}

.week-label {
  font-weight: 700;
  color: #0f172a;
}

.today-btn {
  background: transparent;
  border: 1px solid rgba(15,23,42,0.06);
  padding: 6px 8px;
  border-radius: 8px;
  font-size: 0.8rem;
  cursor: pointer;
}

.week-picker input {
  border: none;
  background: transparent;
  padding: 6px 8px;
  border-radius: 8px;
  font-size: 0.95rem;
  color: #0f172a;
  cursor: pointer;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.08);
}

.reservation-card {
  border: 1px solid #e0e7ff;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-weight: 600;
  color: #475467;
}

input,
textarea {
  padding: 12px;
  border-radius: 10px;
  border: 1px solid #dfe3ec;
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
  border: none;
  border-radius: 10px;
  padding: 12px 20px;
  background: #2563eb;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.form-actions button:disabled {
  opacity: 0.6;
}

.schedule-card__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.legend {
  display: flex;
  gap: 16px;
  align-items: center;
  color: #4b5563;
  font-size: 0.9rem;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  display: inline-block;
  margin-right: 6px;
}

.legend-dot.course {
  background: #2563eb;
}

.legend-dot.reservation {
  background: #16a34a;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 0;
  margin-top: 24px;
}

.times-column {
  display: flex;
  flex-direction: column;
}

.day-header-placeholder {
  height: 48px;
}

.time-slot {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding-right: 12px;
  color: #94a3b8;
  font-size: 0.85rem;
  border-right: 1px solid #e2e8f0;
}

.day-columns {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  border-left: 1px solid #e2e8f0;
}

.day-column {
  display: flex;
  flex-direction: column;
  border-right: 1px solid #e2e8f0;
}

.day-header {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #1f2937;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.day-body {
  position: relative;
  height: calc(40px * var(--slots-count, 0));
}

.slot-guide {
  height: 40px;
  border-bottom: 1px dashed rgba(226, 232, 240, 0.7);
}

.event-block {
  position: absolute;
  left: 12px;
  right: 12px;
  border-radius: 12px;
  padding: 10px 12px;
  color: #fff;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.15);
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.85rem;
}

.event-block.course {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
}

.event-block.reservation {
  background: linear-gradient(135deg, #16a34a, #22c55e);
}

.event-title {
  margin: 0;
  font-weight: 600;
}

.event-time {
  margin: 0;
  font-size: 0.8rem;
  opacity: 0.9;
}

.state {
  text-align: center;
  color: #64748b;
  padding: 40px 0;
}

.error {
  background: #fef2f2;
  color: #b91c1c;
  padding: 16px;
  border-radius: 12px;
  border-left: 4px solid #dc2626;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}

@media (max-width: 900px) {
  .schedule-grid {
    grid-template-columns: 70px 1fr;
  }

  .day-columns {
    grid-template-columns: repeat(5, minmax(160px, 1fr));
    overflow-x: auto;
  }

  .day-column:first-child {
    border-left: none;
  }
}

@media (max-width: 600px) {
  .page-header__left {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions button,
  .primary-button,
  .back-button {
    width: 100%;
    justify-content: center;
  }
}
</style>

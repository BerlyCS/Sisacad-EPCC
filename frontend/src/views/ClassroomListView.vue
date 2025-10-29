<template>
  <AdminLayout>
    <div class="classroom-list-container">
      <div class="header">
        <h1>Reservar Aula</h1>
        <p>Seleccione un aula o laboratorio para ver su disponibilidad</p>
      </div>

      <div class="classroom-grid">
        <div 
          v-for="classroom in classrooms" 
          :key="classroom"
          class="classroom-card"
          @click="selectClassroom(classroom)"
        >
          <div class="classroom-icon">
            <i class="fas" :class="getClassroomIcon(classroom)"></i>
          </div>
          <div class="classroom-info">
            <h3>{{ classroom }}</h3>
            <span class="availability-badge" :class="getAvailabilityClass(classroom)">
              {{ getAvailabilityText(classroom) }}
            </span>
          </div>
          <div class="classroom-arrow">
            <i class="fas fa-chevron-right"></i>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading">
        <i class="fas fa-spinner fa-spin"></i>
        Cargando aulas...
      </div>

      <!-- Error State -->
      <div v-if="error" class="error-message">
        <i class="fas fa-exclamation-triangle"></i>
        {{ error }}
      </div>
    </div>
  </AdminLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AdminLayout from '@/components/AdminLayout.vue';
import { reservationService } from '@/services/reservationService';

const router = useRouter();
const classrooms = ref<string[]>([]);
const loading = ref(false);
const error = ref('');

const getClassroomIcon = (classroom: string) => {
  if (classroom.includes('LAB')) return 'fa-flask';
  if (classroom.includes('AULA')) return 'fa-chalkboard';
  return 'fa-building';
};

const getAvailabilityClass = (classroom: string) => {
  // Por ahora, todas se muestran como disponibles
  // En una implementación real, podrías verificar la disponibilidad general
  return 'available';
};

const getAvailabilityText = (classroom: string) => {
  return 'Disponible';
};

const selectClassroom = (classroom: string) => {
  router.push(`/classroom-schedule/${encodeURIComponent(classroom)}`);
};

const loadClassrooms = async () => {
  try {
    loading.value = true;
    error.value = '';
    classrooms.value = await reservationService.getAvailableClassrooms();
  } catch (err) {
    error.value = 'Error al cargar la lista de aulas';
    console.error('Error loading classrooms:', err);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadClassrooms();
});
</script>

<style scoped>
.classroom-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  text-align: center;
  margin-bottom: 40px;
}

.header h1 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.header p {
  color: #7f8c8d;
  font-size: 1.1em;
}

.classroom-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 30px;
}

.classroom-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.classroom-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-color: #3498db;
}

.classroom-icon {
  font-size: 2em;
  color: #3498db;
  margin-right: 15px;
  width: 60px;
  text-align: center;
}

.classroom-info {
  flex: 1;
}

.classroom-info h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 1.2em;
}

.availability-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 600;
}

.availability-badge.available {
  background: #d4edda;
  color: #155724;
}

.availability-badge.occupied {
  background: #f8d7da;
  color: #721c24;
}

.availability-badge.partial {
  background: #fff3cd;
  color: #856404;
}

.classroom-arrow {
  color: #bdc3c7;
  font-size: 1.2em;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #3498db;
  font-size: 1.1em;
}

.loading i {
  margin-right: 10px;
}

.error-message {
  text-align: center;
  padding: 20px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 8px;
  margin: 20px 0;
}

.error-message i {
  margin-right: 10px;
}

@media (max-width: 768px) {
  .classroom-grid {
    grid-template-columns: 1fr;
  }
  
  .classroom-list-container {
    padding: 10px;
  }
}
</style>

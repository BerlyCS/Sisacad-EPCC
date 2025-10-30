<template>
  <AdminLayout>
    <div class="p-5">
      <div class="text-center mb-10">
        <h1 class="mb-2 text-gray-600 font-medium text-3xl">Reservar Aula</h1>
        <p class="text-gray-700">Seleccione un aula o laboratorio para ver su disponibilidad</p>
      </div>

      <div class="grid grid-cols-1 gap-5 lg:grid-cols-3 sm:grid-cols-2 mt-[30px]">
        <div 
          v-for="classroom in classrooms" 
          :key="classroom"
          class="flex items-center p-5 bg-white rounded-xl shadow-md cursor-pointer hover:shadow-lg transition-all duration-300 border-2 border-transparent hover:border-cyan-700 classroom-card"
          @click="selectClassroom(classroom)"
        >

          <div v-if="classroom.includes('LAB')">
            <BeakerIcon class="text-cyan-600 w-11 h-11 flex items-center justify-center mr-4" />
          </div>
          <div v-else class="other-badge">
            <BuildingOfficeIcon class="text-cyan-600 w-11 h-11 flex items-center justify-center mr-4"/>
          </div>

          <div class="flex-1">
            <h3 class="text-gray-600 mb-2 text-md">{{ classroom }}</h3>
            <span class="bg-[#d4edda] px-3 py-1 rounded-2xl font-medium text-[#155724]" :class="getAvailabilityClass(classroom)">
              {{ getAvailabilityText(classroom) }}
            </span>
          </div>
          
          <ChevronRightIcon class="w-10 h-10 mx-auto mt-2 mb-4 text-gray-400"/>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center p-10 text-blue-500 text-md">
        Cargando aulas...
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AdminLayout from '../components/TopBar.vue';
import { reservationService } from '@/services/reservationService';
import { BeakerIcon, BuildingOfficeIcon, ChevronRightIcon, ExclamationTriangleIcon } from '@heroicons/vue/16/solid';

const router = useRouter();
const classrooms = ref<string[]>([]);
const loading = ref(false);
const error = ref('');

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
</style>

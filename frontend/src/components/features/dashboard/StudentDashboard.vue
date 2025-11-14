<template>
  <div class="pt-4 border-gray-200 space-y-6">
    <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 w-full md:w-auto">
        <PrincipalButton color="blue" :to="studentProfileRoute">
          <UserIcon class="w-15 h-15 mx-auto mt-2 mb-3" />
          <h4 class="text-lg">Mi Perfil</h4>
        </PrincipalButton>
      </div>
    </div>

    <div v-if="profileLoading" class="bg-white shadow rounded-lg p-6 text-center">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
      <p class="text-gray-600 mt-3">Cargando tu información...</p>
    </div>

    <div v-else-if="profileError" class="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
      <h4 class="text-lg font-semibold text-red-800">No se pudo cargar tu información</h4>
      <p class="text-red-700 mt-2">{{ profileError }}</p>
      <button
        @click="loadStudentProfile"
        class="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition"
      >
        Reintentar
      </button>
    </div>

    <template v-else-if="studentProfile">
      <StudentProfileSummary :profile="studentProfile" subtitle="Información personal del estudiante" />

      <StudentCoursesCard
        :courses="studentCourses"
        :loading="profileLoading"
        :error="profileError"
        @refresh="loadStudentProfile"
        @select="openCourseDetail"
      />

      <StudentScheduleCard
        :entries="studentScheduleEntries"
        :loading="profileLoading"
        :error="''"
        @refresh="loadStudentProfile"
      />
    </template>

    <div v-else class="bg-white shadow rounded-lg p-6 text-center text-gray-500">
      No hay información disponible para tu perfil.
    </div>
  </div>
</template>

<script setup>
import { PrincipalButton } from '@/components/ui'
import {
  StudentCoursesCard,
  StudentProfileSummary,
  StudentScheduleCard
} from '@/components/features/student'
import { UserIcon } from '@heroicons/vue/16/solid'

defineProps({
  studentProfile: { type: Object, default: null },
  profileLoading: { type: Boolean, default: false },
  profileError: { type: String, default: '' },
  studentCourses: { type: Array, default: () => [] },
  studentScheduleEntries: { type: Array, default: () => [] },
  studentProfileRoute: { type: [Object, null], default: null },
  loadStudentProfile: { type: Function, required: true },
  openCourseDetail: { type: Function, required: true }
})
</script>

<template>
  <div class="pt-4 border-gray-200">
    <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel de la Secretaria</h3>
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-2 sm:grid-cols-1 mt-5">
      <PrincipalButton color="blue" to="/admin/students">
        <AcademicCapIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Ver Estudiantes</h4>
      </PrincipalButton>
      <PrincipalButton color="green" to="/admin/student-enrollment">
        <UserIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Matricular Estudiantes</h4>
      </PrincipalButton>
    </div>
  </div>
</template>

<script setup>
import { PrincipalButton } from '@/components/ui'
import { AcademicCapIcon, UserIcon } from '@heroicons/vue/16/solid'
</script>

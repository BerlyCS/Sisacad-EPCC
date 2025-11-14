<template>
  <div class="pt-4 border-gray-200">
    <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel del Profesor</h3>
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3 sm:grid-cols-2 mt-5">
      <PrincipalButton color="blue" to="/classrooms">
        <CalendarIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Reservar Aula</h4>
      </PrincipalButton>
    </div>
  </div>
</template>

<script setup>
import { PrincipalButton } from '@/components/ui'
import { CalendarIcon } from '@heroicons/vue/16/solid'
</script>

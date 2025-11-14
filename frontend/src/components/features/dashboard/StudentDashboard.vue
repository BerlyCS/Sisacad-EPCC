<template>
  <div class="pt-4 border-gray-200">
    <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel del Estudiante</h3>
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-3 sm:grid-cols-2 mt-5">
      <PrincipalButton
        v-for="option in navOptions"
        :key="option.id"
        :color="option.color"
        :to="option.route"
      >
        <component :is="option.icon" class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">{{ option.label }}</h4>
      </PrincipalButton>
    </div>
  </div>
</template>

<script setup>
import { PrincipalButton } from '@/components/ui'
import {
  UserCircleIcon,
  BookOpenIcon,
  CalendarDaysIcon
} from '@heroicons/vue/16/solid'

const navOptions = [
  { id: 'profile', label: 'Mi Perfil', icon: UserCircleIcon, color: 'purple', route: '/student/profile' },
  { id: 'courses', label: 'Mis Cursos', icon: BookOpenIcon, color: 'teal', route: '/student/courses' },
  { id: 'schedule', label: 'Mi Horario', icon: CalendarDaysIcon, color: 'orange', route: '/student/schedule' }
]
</script>

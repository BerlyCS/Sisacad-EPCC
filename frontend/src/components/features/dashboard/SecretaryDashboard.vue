<template>
  <div class="pt-4 border-gray-200">
    <h3 class="text-lg font-semibold text-gray-800 mb-3">Panel de la Secretaria</h3>
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-4 sm:grid-cols-2 mt-5">
      <PrincipalButton color="teal" to="/admin/classrooms">
        <BuildingOffice2Icon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Gestionar Aulas</h4>
      </PrincipalButton>
      <PrincipalButton color="blue" to="/admin/students">
        <AcademicCapIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Ver Estudiantes</h4>
      </PrincipalButton>
      <PrincipalButton color="orange" to="/admin/courses">
        <BookOpenIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Gestionar Cursos</h4>
      </PrincipalButton>
      <PrincipalButton color="green" to="/admin/student-enrollment">
        <UserIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Matricular Estudiantes</h4>
      </PrincipalButton>

      <PrincipalButton color="purple" to="/admin/professors">
        <BriefcaseIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Gestionar Profesores</h4>
      </PrincipalButton>
      <PrincipalButton color="gray" to="/admin/secretaries">
        <UserIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Gestionar Secretarias</h4>
      </PrincipalButton>
      <PrincipalButton color="cyan" to="/classrooms">
        <CalendarIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Reservar Aula</h4>
      </PrincipalButton>
      <PrincipalButton color="lime" to="/reservation-management">
        <ClipboardDocumentListIcon class="w-15 h-15 mx-auto mt-2 mb-4" />
        <h4 class="text-xl">Gestionar Reservas</h4>
      </PrincipalButton>
    </div>
  </div>
</template>

<script setup>
import { PrincipalButton } from '@/components/ui'
import {
  AcademicCapIcon,
  BookOpenIcon,
  BuildingOffice2Icon,
  BriefcaseIcon,
  CalendarIcon,
  ClipboardDocumentListIcon,
  UserIcon
} from '@heroicons/vue/16/solid'
</script>

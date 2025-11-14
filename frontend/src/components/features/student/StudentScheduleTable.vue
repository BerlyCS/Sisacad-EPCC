<template>
  <div>
    <div v-if="!entries.length" class="text-gray-500 text-sm text-center py-6">
      No hay horarios para mostrar.
    </div>
    <div v-else class="space-y-6">
      <div
        v-for="day in orderedDays"
        :key="day.key"
        class="bg-white border border-slate-200 rounded-lg shadow-sm"
      >
        <div class="bg-slate-100 px-4 py-2 rounded-t-lg">
          <h5 class="text-sm font-semibold text-slate-700">{{ day.label }}</h5>
        </div>
        <ul class="divide-y divide-slate-100">
          <li
            v-for="slot in day.slots"
            :key="slot.startTime + slot.courseCode"
            class="px-4 py-3 flex flex-col md:flex-row md:items-center md:justify-between text-sm"
          >
            <div class="flex-1 text-left">
              <p class="font-semibold text-slate-800">{{ slot.courseName }}</p>
              <p class="text-xs text-slate-500">{{ slot.courseTypeLabel }}</p>
            </div>
            <div class="mt-2 md:mt-0 md:flex md:items-center md:space-x-6 text-xs text-slate-600">
              <span class="font-medium">{{ slot.startTime }} - {{ slot.endTime }}</span>
              <span>{{ slot.classroomName }}</span>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StudentScheduleEntry } from '@/services/studentScheduleService'

type DayKey = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO'

const DAY_ORDER: DayKey[] = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO']
const DAY_LABEL: Record<DayKey, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo'
}

const props = defineProps<{
  entries: StudentScheduleEntry[]
}>()

const orderedDays = computed(() => {
  const grouped = new Map<DayKey, StudentScheduleEntry[]>()

  for (const day of DAY_ORDER) {
    grouped.set(day, [])
  }

  props.entries.forEach(entry => {
    const key = entry.dayOfWeek.toUpperCase() as DayKey
    const slots = grouped.get(key)
    if (slots) {
      slots.push(entry)
    }
  })

  return DAY_ORDER
    .map(day => {
      const slots = (grouped.get(day) ?? []).slice().sort((a, b) => {
        if (a.startTime === b.startTime) {
          return a.courseName.localeCompare(b.courseName)
        }
        return a.startTime.localeCompare(b.startTime)
      })
      return {
        key: day,
        label: DAY_LABEL[day],
        slots
      }
    })
    .filter(day => day.slots.length > 0)
})
</script>

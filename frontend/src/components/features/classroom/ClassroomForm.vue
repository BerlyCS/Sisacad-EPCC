<template>
  <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
    <h3 class="text-lg font-semibold text-gray-800 mb-3">Agregar nueva aula</h3>
    <form @submit.prevent="onSubmit" class="grid grid-cols-1 gap-3">
      <div>
        <label class="block text-sm font-medium text-gray-700">Edificio</label>
        <input v-model="form.building" type="text" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
      </div>

      <div class="grid grid-cols-3 gap-3">
        <div>
          <label class="block text-sm font-medium text-gray-700">Piso</label>
          <input v-model.number="form.floor" type="number" min="0" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Número</label>
          <input v-model.number="form.number" type="number" min="0" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Capacidad</label>
          <input v-model.number="form.capacity" type="number" min="0" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm" />
        </div>
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700">Tipo</label>
        <select v-model="form.classroomType" class="mt-1 block w-full rounded-md border-gray-300 shadow-sm">
          <option value="AULA">Aula</option>
          <option value="LAB">Laboratorio</option>
          <option value="AUD">Auditorio</option>
        </select>
      </div>

      <div class="flex items-center gap-3">
        <button :disabled="submitting" type="submit" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-60">
          <span v-if="!submitting">Crear aula</span>
          <span v-else>Creando...</span>
        </button>
        <p v-if="localError" class="text-red-600 text-sm">{{ localError }}</p>
      </div>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'

const emit = defineEmits(['submit'])
const props = defineProps({ resetKey: { type: Number, default: 0 } })

const form = reactive({ building: '', floor: null, number: null, capacity: null, classroomType: 'AULA' })
const submitting = ref(false)
const localError = ref('')

watch(() => props.resetKey, () => {
  // reset form when parent signals a new key
  form.building = ''
  form.floor = null
  form.number = null
  form.capacity = null
  form.classroomType = 'AULA'
  localError.value = ''
  submitting.value = false
})

const validate = () => {
  localError.value = ''
  if (!form.building || form.building.trim() === '') {
    localError.value = 'El edificio es requerido.'
    return false
  }
  if (form.capacity == null || form.capacity <= 0) {
    localError.value = 'La capacidad debe ser mayor que 0.'
    return false
  }
  return true
}

const onSubmit = () => {
  if (!validate()) return
  submitting.value = true
  const payload = {
    place: {
      building: form.building,
      floor: form.floor,
      number: form.number,
      capacity: form.capacity,
      classroomType: form.classroomType
    }
  }
  emit('submit', payload)
}
</script>

<style scoped>
/* minor spacing adjustments if needed */
</style>

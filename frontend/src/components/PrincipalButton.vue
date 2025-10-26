<template>
  <component 
    :is="to ? RouterLink : 'button'"
    v-bind="to ? { to } : { type: 'button' }"
    class="p-5 rounded-lg text-white font-bold text-center w-auto m-3 cursor-pointer border-2 border-transparent transition-all duration-300 ease-in-out transform hover:-translate-y-2"
    :class="variantClasses"
  >
    <slot></slot>
  </component>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  color: {
    type: String,
    default: 'teal'
  },
  to: {
    type: [String, Object],
    default: null,
  }
})

const variantClasses = computed(() => {
  const map = {
    teal: 'bg-teal-500 hover:bg-white hover:text-teal-500 hover:border-teal-500',
    orange: 'bg-orange-500 hover:bg-white hover:text-orange-500 hover:border-orange-500',
    purple: 'bg-indigo-500 hover:bg-white hover:text-indigo-500 hover:border-indigo-500',
    pinck: 'bg-pink-500 hover:bg-white hover:text-pink-500 hover:border-pink-500',
    gray: 'bg-gray-500 hover:bg-white hover:text-gray-500 hover:border-gray-500',
    green: 'bg-emerald-500 hover:bg-white hover:text-emerald-500 hover:border-emerald-500',
  }
  return map[props.color] ?? map.teal
})
</script>
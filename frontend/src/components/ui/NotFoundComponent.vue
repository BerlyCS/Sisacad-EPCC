<template>
  <div class="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
    <div class="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
      <div class="bg-white py-8 px-4 shadow sm:rounded-lg sm:px-10">
        <div class="text-center">
          <!-- Icon -->
          <div class="mx-auto flex items-center justify-center h-24 w-24 rounded-full bg-red-100">
            <svg class="h-12 w-12 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
          </div>

          <!-- Title -->
          <h1 class="mt-6 text-3xl font-extrabold text-gray-900">
            Página no encontrada
          </h1>

          <!-- Description -->
          <p class="mt-2 text-sm text-gray-600">
            Lo sentimos, no pudimos encontrar la página que estás buscando.
          </p>

          <!-- Error Code -->
          <div class="mt-4">
            <span class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-red-100 text-red-800">
              Error 404
            </span>
          </div>

          <!-- Actions -->
          <div class="mt-6 space-y-3">
            <button
              @click="goBack"
              class="w-full flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              ← Volver atrás
            </button>

            <button
              @click="goHome"
              class="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            >
              Ir al inicio
            </button>
          </div>

          <!-- Additional Help -->
          <div class="mt-6 text-xs text-gray-500">
            <p>Si crees que esto es un error, contacta al administrador del sistema.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    goHome()
  }
}

const goHome = () => {
  router.push('/bienvenido')
}
</script>

<style scoped>
</style>

<template>
  <div class="relative" ref="container">
    <button @click="toggleDropdown" class="relative p-2 text-gray-600 hover:text-gray-800 focus:outline-none">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
      </svg>
      <span v-if="unreadCount > 0" class="absolute top-0 right-0 inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-red-100 transform translate-x-1/4 -translate-y-1/4 bg-red-600 rounded-full">
        {{ unreadCount }}
      </span>
    </button>

    <div v-if="isOpen" class="absolute right-0 mt-2 w-80 bg-white rounded-md shadow-lg overflow-hidden z-50 border border-gray-200">
      <div class="py-2">
        <div class="px-4 py-2 text-sm font-medium text-gray-700 border-b border-gray-200">
          Notificaciones
        </div>
        <div v-if="notifications.length === 0" class="px-4 py-6 text-center text-gray-500 text-sm">
          No tienes notificaciones.
        </div>
        <ul v-else class="max-h-64 overflow-y-auto">
          <li v-for="notification in notifications" :key="notification.id" 
              class="px-4 py-3 hover:bg-gray-50 border-b border-gray-100 last:border-b-0 cursor-pointer"
              :class="{ 'bg-blue-50': !notification.read }"
              @click="markAsRead(notification)">
            <div class="flex items-start">
              <div class="flex-shrink-0 pt-0.5">
                <span v-if="notification.type === 'WARNING'" class="text-yellow-500">⚠️</span>
                <span v-else-if="notification.type === 'ERROR'" class="text-red-500">❌</span>
                <span v-else class="text-blue-500">ℹ️</span>
              </div>
              <div class="ml-3 w-0 flex-1">
                <p class="text-sm font-medium text-gray-900">{{ notification.message }}</p>
                <p class="mt-1 text-xs text-gray-500">{{ formatDate(notification.timestamp) }}</p>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

interface Notification {
  id: number
  message: string
  type: string
  read: boolean
  timestamp: string
}

const notifications = ref<Notification[]>([])
const isOpen = ref(false)
const container = ref<HTMLElement | null>(null)

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const fetchNotifications = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/notifications', { credentials: 'include' })
    if (response.ok) {
      notifications.value = await response.json()
    }
  } catch (e) {
    console.error('Error fetching notifications', e)
  }
}

const markAsRead = async (notification: Notification) => {
  if (notification.read) return
  try {
    const response = await fetch(`http://localhost:8080/api/notifications/${notification.id}/read`, {
      method: 'PUT',
      credentials: 'include'
    })
    if (response.ok) {
      notification.read = true
    }
  } catch (e) {
    console.error('Error marking as read', e)
  }
}

const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

const handleClickOutside = (event: MouseEvent) => {
  if (container.value && !container.value.contains(event.target as Node)) {
    isOpen.value = false
  }
}

const formatDate = (isoStr: string) => {
  return new Date(isoStr).toLocaleString()
}

let intervalId: number

onMounted(() => {
  fetchNotifications()
  intervalId = setInterval(fetchNotifications, 30000) // Poll every 30s
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  clearInterval(intervalId)
  document.removeEventListener('click', handleClickOutside)
})
</script>

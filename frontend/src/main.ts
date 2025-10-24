import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'

import App from './App.vue'
import router from './router'

import './assets/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// inicializar aouth antes de montar para que el layout/routers vean el usar ya cargado
const authStore = useAuthStore()
authStore.initializeAuth().finally(() => {
    app.mount('#app')
})

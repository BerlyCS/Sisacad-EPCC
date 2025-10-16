import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import BienvenidoView from '../views/BienvenidoView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/bienvenido',
      name: 'bienvenido',
      component: BienvenidoView,
    },
  ],
})

export default router

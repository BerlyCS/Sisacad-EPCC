import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import BienvenidoView from '../views/BienvenidoView.vue'
import ClassroomManagementView from '../views/ClassroomManagementView.vue'
import CourseManagementView from '../views/CourseManagementView.vue'
import ProfessorManagementView from '../views/ProfessorManagementView.vue'
import StudentManagementView from '../views/StudentManagementView.vue'
import SecretaryManagementView from '../views/SecretaryManagementView.vue'
import { useAuthStore } from '../stores/auth'

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
    {
      path: '/admin/classrooms',
      name: 'classrooms',
      component: ClassroomManagementView,
    },
    {
      path: '/admin/courses',
      name: 'courses',
      component: CourseManagementView,
    },
    {
      path: '/admin/professors',
      name: 'professors',
      component: ProfessorManagementView,
    },
    {
      path: '/admin/students',
      name: 'students',
      component: StudentManagementView,
    },
    {
      path: '/admin/secretaries',
      name: 'secretaries',
      component: SecretaryManagementView,
    },
  ],
})


router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()

  // Si el usuario no ha inicializado la sesión, intenta hacerlo
  if (!auth.initialized) {
    await auth.initializeAuth()
  }

  // Si intenta acceder a rutas admin sin estar autenticado
  if (to.path.startsWith('/admin') && !auth.isAuthenticated) {
    return next({ path: '/', query: { auth: 'required' } })
  }

  // Si intenta ir a login estando autenticado, redirigir a bienvenido
  if (to.path === '/' && auth.isAuthenticated) {
    return next('/bienvenido')
  }

  next()
})

export default router

import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import BienvenidoView from '../views/BienvenidoView.vue'
import ClassroomManagementView from '../views/ClassroomManagementView.vue'
import CourseManagementView from '../views/CourseManagementView.vue'
import ProfessorManagementView from '../views/ProfessorManagementView.vue'
import StudentManagementView from '../views/StudentManagementView.vue'
import SecretaryManagementView from '../views/SecretaryManagementView.vue'

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

export default router

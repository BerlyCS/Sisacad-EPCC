import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import BienvenidoView from '../views/BienvenidoView.vue'
import ClassroomManagementView from '../views/ClassroomManagementView.vue'
import CourseManagementView from '../views/CourseManagementView.vue'
import ProfessorManagementView from '../views/ProfessorManagementView.vue'
import StudentManagementView from '../views/StudentManagementView.vue'
import SecretaryManagementView from '../views/SecretaryManagementView.vue'
import NotFoundComponent from '@/components/ui/NotFoundComponent.vue'
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
    {
      path: '/admin/student-enrollment',
      name: 'student-enrollment',
      component: () => import('@/views/StudentEnrollmentView.vue'),
      meta: { requiresAuth: true, allowedRoles: ['ADMIN', 'SECRETARY'] }
    },
    {
      path: '/classrooms',
      name: 'ClassroomList',
      component: () => import('@/views/ClassroomListView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/classroom-schedule/:classroomName',
      name: 'ClassroomSchedule',
      component: () => import('@/views/ClassroomScheduleView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/courses/:courseId',
      name: 'course-detail',
      component: () => import('@/views/CourseDetailView.vue'),
      meta: { requiresAuth: true, allowedRoles: ['ADMIN', 'SECRETARY', 'PROFESSOR', 'STUDENT'] }
    },
    {
      path: '/reservation-management',
      name: 'ReservationManagement',
      component: () => import('@/views/ReservationManagementView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/students/:cui/profile',
      name: 'student-profile',
      component: () => import('@/views/StudentProfileView.vue'),
      meta: { requiresAuth: true, allowedRoles: ['ADMIN', 'SECRETARY', 'PROFESSOR', 'STUDENT'] }
    },
    {
      path: '/student/profile',
      name: 'student-own-profile',
      component: () => import('@/views/StudentProfileView.vue'),
      meta: { requiresAuth: true, allowedRoles: ['STUDENT'] }
    },
    {
      path: '/student/courses',
      name: 'student-courses',
      component: () => import('@/views/StudentCoursesView.vue'),
      meta: { requiresAuth: true, allowedRoles: ['STUDENT'] }
    },
    {
      path: '/student/schedule',
      name: 'student-schedule',
      component: () => import('@/views/StudentScheduleView.vue'),
      meta: { requiresAuth: true, allowedRoles: ['STUDENT'] }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundComponent
    },
  ],
})


router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()

  // Si el usuario no ha inicializado la sesión, intenta hacerlo
  if (!auth.initialized) {
    await auth.initializeAuth()
  }

  if (to.meta?.requiresAuth && !auth.isAuthenticated) {
    return next({ path: '/', query: { auth: 'required' } })
  }

  // Si intenta acceder a rutas admin sin estar autenticado
  if (to.path.startsWith('/admin') && !auth.isAuthenticated) {
    return next({ path: '/', query: { auth: 'required' } })
  }

  const allowedRoles = (to.meta as any)?.allowedRoles as string[] | undefined
  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = typeof auth.userRole === 'string'
      ? auth.userRole
      : ((auth.userRole as unknown as { value?: string })?.value ?? '')
    if (!userRole || !allowedRoles.includes(userRole)) {
      return next({ path: '/bienvenido', query: { auth: 'forbidden' } })
    }

    if (userRole === 'STUDENT' && to.name === 'student-profile') {
      const requestedCui = typeof to.params.cui === 'string' ? to.params.cui : Array.isArray(to.params.cui) ? to.params.cui[0] : ''
      const userCui = typeof auth.userCui === 'string'
        ? auth.userCui
        : ((auth.userCui as unknown as { value?: string })?.value ?? '')
      if (userCui && requestedCui && userCui !== requestedCui) {
        return next({ path: '/bienvenido', query: { auth: 'restricted' } })
      }
    }
  }

  // Si intenta ir a login estando autenticado, redirigir a bienvenido
  if (to.path === '/' && auth.isAuthenticated) {
    return next('/bienvenido')
  }

  next()
})

export default router

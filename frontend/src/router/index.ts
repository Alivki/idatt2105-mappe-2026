import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import LoginView from '@/views/loginView.vue'
import BusinessView from '@/views/businessView.vue'
import ChecklistView from '@/views/ChecklistView.vue'
import TemperatureView from '@/views/TemperatureView.vue'
import DeviationsView from '@/views/DeviationsView.vue'
import TrainingView from '@/views/TrainingView.vue'

const TEMP_AUTH_KEY = 'ik_temp_auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true },
    },
    {
      path: '/sjekklister',
      name: 'sjekklister',
      component: ChecklistView,
      meta: { requiresAuth: true },
    },
    {
      path: '/temperatur',
      name: 'temperatur',
      component: TemperatureView,
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/business',
      name: 'business',
      component: BusinessView,
      meta: { requiresAuth: true },
    },
    {
      path: '/opplæring',
      name: 'opplæring',
      component: TrainingView,
      meta: { requiresAuth: true },
    },
    {
      path: '/bevilling',
      name: 'bevilling',
      component: DeviationsView,
      meta: { requiresAuth: true },
    },
    {
      path: '/avvik',
      name: 'avvik',
      component: DeviationsView,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const isLoggedIn = localStorage.getItem(TEMP_AUTH_KEY) === 'true'
  if (to.meta.requiresAuth && !isLoggedIn) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.path === '/login' && isLoggedIn) {
    return '/dashboard'
  }

  return true
})

export default router

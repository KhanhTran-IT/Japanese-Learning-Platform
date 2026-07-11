import { useAuthStore } from '@/stores/auth.store'

export default function setupGuards(router) {
  router.beforeEach((to, from) => {
    const authStore = useAuthStore()
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    if (requiresAuth && !authStore.isAuthenticated) {
      return '/login'
    }

    if (authStore.isAuthenticated && (to.path === '/login' || to.path === '/register')) {
      return '/student/dashboard'
    }
  })
}

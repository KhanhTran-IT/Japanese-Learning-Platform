<template>
  <div>
    <!-- Header -->
    <div class="text-center mb-8">
      <h2 class="font-headline-md text-headline-md text-ink-black">Đăng nhập</h2>
      <p class="text-sm text-secondary mt-2 font-body-md">Chào mừng bạn trở lại với BrianJP</p>
    </div>

    <!-- Success alert -->
    <div
      v-if="successMsg"
      class="success-alert flex items-start gap-3 p-4 rounded-xl bg-success-green/10 border border-success-green/30 mb-6"
      role="alert"
    >
      <span class="material-symbols-outlined text-success-green text-[20px] mt-0.5" style="font-variation-settings: 'FILL' 1;">check_circle</span>
      <span class="text-sm font-body-md text-success-green">{{ successMsg }}</span>
    </div>

    <!-- Error alert -->
    <div
      v-if="errorMsg"
      class="error-alert flex items-start gap-3 p-4 rounded-xl bg-error/10 border border-error/30 mb-6"
      role="alert"
    >
      <span class="material-symbols-outlined text-error text-[20px] mt-0.5">error</span>
      <span class="text-sm font-body-md text-error">{{ errorMsg }}</span>
    </div>

    <!-- Form -->
    <form @submit.prevent="handleLogin" class="space-y-5">
      <div>
        <label class="block text-sm font-medium text-on-surface mb-1.5 font-body-md">Email</label>
        <input
          type="email"
          v-model="email"
          @input="clearMessages"
          required
          placeholder="you@example.com"
          class="w-full px-4 py-3 rounded-xl border border-paper-shadow bg-surface-container-lowest text-on-surface placeholder:text-outline font-body-md text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-on-surface mb-1.5 font-body-md">Mật khẩu</label>
        <input
          type="password"
          v-model="password"
          @input="clearMessages"
          required
          placeholder="••••••••"
          class="w-full px-4 py-3 rounded-xl border border-paper-shadow bg-surface-container-lowest text-on-surface placeholder:text-outline font-body-md text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
        />
      </div>

      <button
        type="submit"
        class="btn-submit w-full py-3 rounded-xl font-button text-button bg-primary text-on-primary shadow-md hover:bg-primary-container hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all mt-2"
        :disabled="isLoading"
      >
        <span v-if="isLoading" class="inline-flex items-center gap-2">
          <svg class="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"/></svg>
          Đang đăng nhập...
        </span>
        <span v-else>Đăng nhập</span>
      </button>
    </form>

    <!-- Switch to register -->
    <p class="text-center text-sm text-secondary mt-8 font-body-md">
      Chưa có tài khoản?
      <router-link to="/register" class="text-primary font-semibold hover:underline">
        Đăng ký ngay
      </router-link>
    </p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { AuthService } from '@/services/auth.service'
import { useAuthStore } from '@/stores/auth.store'
import { getApiErrorMessage } from '@/utils/api-error'

const email = ref('')
const password = ref('')
const errorMsg = ref('')
const successMsg = ref('')
const isLoading = ref(false)

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

onMounted(() => {
  if (route.query.registered === 'success') {
    successMsg.value = 'Đăng ký thành công! Vui lòng đăng nhập.'
    // clean up the URL query
    router.replace({ query: {} })
  }
})

const clearMessages = () => {
  errorMsg.value = ''
  successMsg.value = ''
}

const handleLogin = async () => {
  errorMsg.value = ''
  successMsg.value = ''
  
  if (!email.value.trim() || !password.value) {
    errorMsg.value = 'Vui lòng nhập đầy đủ email và mật khẩu.'
    return
  }

  isLoading.value = true
  
  try {
    const res = await AuthService.login(email.value, password.value)
    
    if (res.data.code === 1000) {
      const { accessToken } = res.data.result
      authStore.setTokens(accessToken)
      
      // fetch user profile
      const userRes = await AuthService.getCurrentUser()
      const userData = userRes.data.result
      authStore.setUser(userData)
      
      // redirect based on role
      const userRoles = userData.roles || []
      if (userRoles.includes('ADMIN') || userRoles.includes('SUPER_ADMIN')) {
        router.push('/admin/dashboard')
      } else {
        const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : ''
        const isSafeRedirect = redirectPath.startsWith('/') && !redirectPath.startsWith('//')
        router.push(isSafeRedirect ? redirectPath : '/student/dashboard')
      }
    } else {
      errorMsg.value = res.data.message || 'Đăng nhập thất bại'
    }
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Thông tin đăng nhập không chính xác.')
  } finally {
    isLoading.value = false
  }
}
</script>

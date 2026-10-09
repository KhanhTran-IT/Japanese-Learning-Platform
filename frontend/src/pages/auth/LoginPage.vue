<template>
  <div>
    <h1 class="text-3xl font-stitch-serif font-bold mb-1">Đăng nhập</h1>
    <p class="text-stitch-muted-foreground text-sm mb-8">
      Chưa có tài khoản? 
      <router-link to="/register" class="text-stitch-primary font-medium hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded">Đăng ký miễn phí</router-link>
    </p>

    <!-- Success alert -->
    <div v-if="successMsg" class="mb-6 p-3 rounded-xl bg-green-50 text-green-700 text-sm font-medium flex items-start gap-2">
      <span class="mt-0.5">✓</span>
      <span>{{ successMsg }}</span>
    </div>

    <!-- Error alert (form level) -->
    <div v-if="formErrorMsg" class="mb-6 p-3 rounded-xl bg-red-50 text-red-600 text-sm font-medium flex items-start gap-2">
      <span class="mt-0.5">⚠️</span>
      <span>{{ formErrorMsg }}</span>
    </div>

    <!-- Social auth -->
    <div class="grid grid-cols-2 gap-3 mb-6">
      <button type="button" class="flex items-center justify-center gap-2 px-4 py-2.5 border border-stitch-border rounded-xl text-sm font-medium hover:bg-stitch-muted transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-primary">
        <span class="text-lg">G</span> Google
      </button>
      <button type="button" class="flex items-center justify-center gap-2 px-4 py-2.5 border border-stitch-border rounded-xl text-sm font-medium hover:bg-stitch-muted transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-primary">
        <span class="text-lg">f</span> Facebook
      </button>
    </div>

    <div class="flex items-center gap-3 mb-6">
      <div class="flex-1 h-px bg-stitch-border" />
      <span class="text-xs text-stitch-muted-foreground">hoặc</span>
      <div class="flex-1 h-px bg-stitch-border" />
    </div>

    <form @submit.prevent="handleLogin" class="space-y-4">
      <div>
        <label for="email" class="block text-sm font-medium mb-1.5 text-stitch-foreground">Email</label>
        <input 
          id="email"
          type="email" 
          v-model="email" 
          @input="clearErrors('email')"
          autocomplete="email"
          required 
          placeholder="email@example.com"
          class="w-full px-4 py-3 rounded-xl border text-sm bg-white focus:outline-none focus:ring-2 focus:ring-stitch-primary/30 transition-colors"
          :class="fieldErrors.email ? 'border-red-400 focus:border-red-400' : 'border-stitch-border focus:border-stitch-primary'"
        />
        <p v-if="fieldErrors.email" class="text-xs text-red-500 mt-1">{{ fieldErrors.email }}</p>
      </div>

      <div>
        <div class="flex items-center justify-between mb-1.5">
          <label for="password" class="block text-sm font-medium text-stitch-foreground">Mật khẩu</label>
          <button type="button" class="text-xs text-stitch-primary hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded">Quên mật khẩu?</button>
        </div>
        <div class="relative">
          <input 
            id="password"
            :type="showPassword ? 'text' : 'password'" 
            v-model="password" 
            @input="clearErrors('password')"
            autocomplete="current-password"
            required 
            placeholder="••••••••"
            class="w-full px-4 py-3 pr-12 rounded-xl border text-sm bg-white focus:outline-none focus:ring-2 focus:ring-stitch-primary/30 transition-colors"
            :class="fieldErrors.password ? 'border-red-400 focus:border-red-400' : 'border-stitch-border focus:border-stitch-primary'"
          />
          <button 
            type="button" 
            @click="showPassword = !showPassword"
            class="absolute right-3 top-1/2 -translate-y-1/2 text-stitch-muted-foreground hover:text-stitch-foreground text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded"
          >
            {{ showPassword ? '🙈' : '👁' }}
          </button>
        </div>
        <p v-if="fieldErrors.password" class="text-xs text-red-500 mt-1">{{ fieldErrors.password }}</p>
      </div>

      <button 
        type="submit" 
        :disabled="isLoading"
        class="w-full py-3.5 bg-stitch-primary text-white font-semibold rounded-xl hover:bg-stitch-primary/90 disabled:opacity-60 transition-all text-sm relative overflow-hidden focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-stitch-primary mt-2"
      >
        <span v-if="isLoading" class="flex items-center justify-center gap-2">
          <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          Đang xử lý...
        </span>
        <span v-else>Đăng nhập</span>
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { AuthService } from '@/services/auth.service'
import { useAuthStore } from '@/stores/auth.store'

const email = ref('')
const password = ref('')
const showPassword = ref(false)

const formErrorMsg = ref('')
const fieldErrors = ref({})
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

const clearErrors = (field) => {
  formErrorMsg.value = ''
  if (field && fieldErrors.value[field]) {
    fieldErrors.value[field] = ''
  } else if (!field) {
    fieldErrors.value = {}
  }
}

const handleLogin = async () => {
  clearErrors()
  successMsg.value = ''
  
  let isValid = true;
  if (!email.value.trim()) {
    fieldErrors.value.email = 'Vui lòng nhập email.'
    isValid = false;
  }
  if (!password.value) {
    fieldErrors.value.password = 'Vui lòng nhập mật khẩu.'
    isValid = false;
  }
  
  if (!isValid) return;

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
      formErrorMsg.value = res.data.message || 'Đăng nhập thất bại'
    }
  } catch (error) {
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      if (status === 401) {
        formErrorMsg.value = 'Email hoặc mật khẩu không chính xác.';
      } else if (status === 422) {
        if (data.result && typeof data.result === 'object') {
            for (const key in data.result) {
                fieldErrors.value[key] = data.result[key];
            }
        } else {
            formErrorMsg.value = data.message || 'Dữ liệu không hợp lệ.';
        }
      } else if (status === 429) {
        formErrorMsg.value = 'Bạn đã thử quá nhiều lần. Vui lòng thử lại sau.';
      } else {
        formErrorMsg.value = data.message || 'Đã xảy ra lỗi từ máy chủ.';
      }
    } else if (error.request) {
      formErrorMsg.value = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng.';
    } else {
      formErrorMsg.value = 'Đã xảy ra lỗi không xác định.';
    }
  } finally {
    isLoading.value = false
  }
}
</script>

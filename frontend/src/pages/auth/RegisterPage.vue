<template>
  <div>
    <!-- Header -->
    <div class="text-center mb-8">
      <h2 class="font-headline-md text-headline-md text-ink-black">Đăng ký tài khoản</h2>
      <p class="text-sm text-secondary mt-2 font-body-md">Tạo tài khoản miễn phí để bắt đầu học</p>
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
    <form @submit.prevent="handleRegister" class="space-y-5">
      <div>
        <label class="block text-sm font-medium text-on-surface mb-1.5 font-body-md">Họ và tên</label>
        <input
          type="text"
          v-model="form.fullName"
          @input="errorMsg = ''"
          required
          placeholder="Nguyễn Văn A"
          class="w-full px-4 py-3 rounded-xl border border-paper-shadow bg-surface-container-lowest text-on-surface placeholder:text-outline font-body-md text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-on-surface mb-1.5 font-body-md">Email</label>
        <input
          type="email"
          v-model="form.email"
          @input="errorMsg = ''"
          required
          placeholder="you@example.com"
          class="w-full px-4 py-3 rounded-xl border border-paper-shadow bg-surface-container-lowest text-on-surface placeholder:text-outline font-body-md text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-on-surface mb-1.5 font-body-md">Mật khẩu</label>
        <input
          type="password"
          v-model="form.password"
          @input="errorMsg = ''"
          required
          placeholder="Tối thiểu 8 ký tự"
          class="w-full px-4 py-3 rounded-xl border border-paper-shadow bg-surface-container-lowest text-on-surface placeholder:text-outline font-body-md text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
        />
      </div>

      <div>
        <label class="block text-sm font-medium text-on-surface mb-1.5 font-body-md">Xác nhận mật khẩu</label>
        <input
          type="password"
          v-model="form.confirmPassword"
          @input="errorMsg = ''"
          required
          placeholder="Nhập lại mật khẩu"
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
          Đang xử lý...
        </span>
        <span v-else>Đăng ký</span>
      </button>
    </form>

    <!-- Switch to login -->
    <p class="text-center text-sm text-secondary mt-8 font-body-md">
      Đã có tài khoản?
      <router-link to="/login" class="text-primary font-semibold hover:underline">
        Đăng nhập
      </router-link>
    </p>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '@/services/auth.service'
import { getApiErrorMessage } from '@/utils/api-error'

const router = useRouter()

const form = reactive({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errorMsg = ref('')
const isLoading = ref(false)

const validateForm = () => {
  errorMsg.value = ''
  
  if (!form.fullName.trim() || !form.email.trim() || !form.password || !form.confirmPassword) {
    errorMsg.value = 'Vui lòng điền đầy đủ thông tin.'
    return false
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.email)) {
    errorMsg.value = 'Email không hợp lệ.'
    return false
  }
  
  if (form.password.length < 8) {
    errorMsg.value = 'Mật khẩu phải có tối thiểu 8 ký tự.'
    return false
  }
  
  if (form.password !== form.confirmPassword) {
    errorMsg.value = 'Mật khẩu xác nhận không khớp.'
    return false
  }
  
  return true
}

const handleRegister = async () => {
  if (!validateForm()) return
  
  isLoading.value = true
  errorMsg.value = ''
  
  try {
    const res = await AuthService.register(form.email, form.password, form.confirmPassword, form.fullName)
    if (res.data.code === 1000) {
      router.push({ path: '/login', query: { registered: 'success' } })
    } else {
      errorMsg.value = res.data.message || 'Đăng ký thất bại'
    }
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Đăng ký thất bại. Vui lòng thử lại sau.')
  } finally {
    isLoading.value = false
  }
}
</script>

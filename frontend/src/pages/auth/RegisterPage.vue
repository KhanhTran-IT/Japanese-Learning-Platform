<template>
  <div>
    <h1 class="text-3xl font-stitch-serif font-bold mb-1">Tạo tài khoản</h1>
    <p class="text-stitch-muted-foreground text-sm mb-8">
      Đã có tài khoản? 
      <router-link to="/login" class="text-stitch-primary font-medium hover:underline focus:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring rounded">Đăng nhập</router-link>
    </p>

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

    <form @submit.prevent="handleRegister" class="space-y-4">
      <div>
        <label for="fullName" class="block text-sm font-medium mb-1.5 text-stitch-foreground">Họ và tên</label>
        <input 
          id="fullName"
          type="text" 
          v-model="form.fullName" 
          @input="clearErrors('fullName')"
          autocomplete="name"
          required 
          placeholder="Nguyễn Văn A"
          class="w-full px-4 py-3 rounded-xl border text-sm bg-white focus:outline-none focus:ring-2 focus:ring-stitch-primary/30 transition-colors"
          :class="fieldErrors.fullName ? 'border-red-400 focus:border-red-400' : 'border-stitch-border focus:border-stitch-primary'"
        />
        <p v-if="fieldErrors.fullName" class="text-xs text-red-500 mt-1">{{ fieldErrors.fullName }}</p>
      </div>

      <div>
        <label for="email" class="block text-sm font-medium mb-1.5 text-stitch-foreground">Email</label>
        <input 
          id="email"
          type="email" 
          v-model="form.email" 
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
        <label for="password" class="block text-sm font-medium mb-1.5 text-stitch-foreground">Mật khẩu</label>
        <div class="relative">
          <input 
            id="password"
            :type="showPassword ? 'text' : 'password'" 
            v-model="form.password" 
            @input="clearErrors('password')"
            autocomplete="new-password"
            required 
            placeholder="Tối thiểu 8 ký tự"
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
        
        <div class="mt-2 flex gap-1" v-if="form.password.length > 0">
          <div v-for="i in 4" :key="i" class="flex-1 h-1 rounded-full transition-all"
               :class="form.password.length >= i * 2 ? (i < 3 ? 'bg-amber-400' : 'bg-green-500') : 'bg-stitch-border'">
          </div>
        </div>
      </div>

      <div>
        <label for="confirmPassword" class="block text-sm font-medium mb-1.5 text-stitch-foreground">Xác nhận mật khẩu</label>
        <input 
          id="confirmPassword"
          :type="showPassword ? 'text' : 'password'" 
          v-model="form.confirmPassword" 
          @input="clearErrors('confirmPassword')"
          autocomplete="new-password"
          required 
          placeholder="Nhập lại mật khẩu"
          class="w-full px-4 py-3 rounded-xl border text-sm bg-white focus:outline-none focus:ring-2 focus:ring-stitch-primary/30 transition-colors"
          :class="fieldErrors.confirmPassword ? 'border-red-400 focus:border-red-400' : 'border-stitch-border focus:border-stitch-primary'"
        />
        <p v-if="fieldErrors.confirmPassword" class="text-xs text-red-500 mt-1">{{ fieldErrors.confirmPassword }}</p>
      </div>

      <p class="text-xs text-stitch-muted-foreground leading-relaxed mt-4">
        Bằng cách đăng ký, bạn đồng ý với <a href="#" class="text-stitch-primary hover:underline">Điều khoản sử dụng</a> và <a href="#" class="text-stitch-primary hover:underline">Chính sách bảo mật</a> của chúng tôi.
      </p>

      <button 
        type="submit" 
        :disabled="isLoading"
        class="w-full py-3.5 bg-stitch-primary text-white font-semibold rounded-xl hover:bg-stitch-primary/90 disabled:opacity-60 transition-all text-sm relative overflow-hidden focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-stitch-primary mt-4"
      >
        <span v-if="isLoading" class="flex items-center justify-center gap-2">
          <span class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
          Đang xử lý...
        </span>
        <span v-else>Tạo tài khoản miễn phí</span>
      </button>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthService } from '@/services/auth.service'

const router = useRouter()

const form = reactive({
  fullName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)

const formErrorMsg = ref('')
const fieldErrors = ref({})
const isLoading = ref(false)

const clearErrors = (field) => {
  formErrorMsg.value = ''
  if (field && fieldErrors.value[field]) {
    fieldErrors.value[field] = ''
  } else if (!field) {
    fieldErrors.value = {}
  }
}

const validateForm = () => {
  clearErrors()
  let isValid = true
  
  if (!form.fullName.trim()) {
    fieldErrors.value.fullName = 'Vui lòng nhập họ và tên.'
    isValid = false
  }
  
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!form.email.trim() || !emailRegex.test(form.email)) {
    fieldErrors.value.email = 'Email không hợp lệ.'
    isValid = false
  }
  
  if (form.password.length < 8) {
    fieldErrors.value.password = 'Mật khẩu phải có tối thiểu 8 ký tự.'
    isValid = false
  }
  
  if (form.password !== form.confirmPassword) {
    fieldErrors.value.confirmPassword = 'Mật khẩu xác nhận không khớp.'
    isValid = false
  }
  
  return isValid
}

const handleRegister = async () => {
  if (!validateForm()) return
  
  isLoading.value = true
  
  try {
    const res = await AuthService.register(form.email, form.password, form.confirmPassword, form.fullName)
    if (res.data.code === 1000) {
      router.push({ path: '/login', query: { registered: 'success' } })
    } else {
      formErrorMsg.value = res.data.message || 'Đăng ký thất bại'
    }
  } catch (error) {
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      if (status === 409 || data?.code === 1002) {
        fieldErrors.value.email = 'Email này đã được đăng ký.';
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

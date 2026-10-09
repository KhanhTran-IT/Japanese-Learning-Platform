<template>
  <div class="fixed inset-0 bg-[#0f1117]/80 backdrop-blur-sm flex items-center justify-center z-50 p-4" @click.self="$emit('close')">
    <div class="bg-[#161b27] rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl border border-white/10">
      <div class="flex justify-between items-center p-6 border-b border-white/10 bg-white/[0.02]">
        <h2 class="text-xl font-stitch-serif font-bold text-white">{{ isEditMode ? 'Cập nhật Khóa học' : 'Tạo Khóa học mới' }}</h2>
        <button class="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-white/50 hover:text-white transition-colors" @click="$emit('close')">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div v-if="apiError" class="bg-red-500/10 text-red-400 p-4 flex justify-between items-center border-b border-red-500/20 text-sm font-medium">
        <div class="flex items-center gap-2"><span>⚠️</span> {{ apiError }}</div>
        <button @click="apiError = ''" class="text-red-400 hover:text-red-300">✕</button>
      </div>

      <form @submit.prevent="handleSubmit" class="p-6 overflow-y-auto flex-1 flex flex-col gap-5">
        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Tên khóa học <span class="text-red-400">*</span></label>
          <input v-model="form.title" type="text" maxlength="255" placeholder="VD: Khóa học N5 nhập môn cho người mới" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:bg-white/10 transition-colors', errors.title ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
          <span v-if="errors.title" class="text-red-400 text-xs mt-1 block">{{ errors.title }}</span>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Slug (đường dẫn)</label>
          <input v-model="form.slug" type="text" maxlength="255" placeholder="Để trống sẽ tự tạo từ tên khóa học" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
          <span class="text-white/40 text-xs mt-1 block">VD: khoa-hoc-n5-nhap-mon</span>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Mô tả ngắn</label>
          <input v-model="form.shortDescription" type="text" placeholder="Giới thiệu tóm tắt khóa học" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Mô tả chi tiết</label>
          <textarea v-model="form.description" rows="4" placeholder="Nội dung mô tả chi tiết khóa học..." class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors resize-none"></textarea>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">URL ảnh đại diện</label>
          <input v-model="form.thumbnailUrl" type="text" placeholder="https://example.com/thumbnail.jpg" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Cấp độ <span class="text-red-400">*</span></label>
            <select v-model="form.level" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.level ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']">
              <option value="" class="bg-[#161b27]">-- Chọn cấp độ --</option>
              <option value="N5" class="bg-[#161b27]">N5</option>
              <option value="N4" class="bg-[#161b27]">N4</option>
              <option value="N3" class="bg-[#161b27]">N3</option>
              <option value="N2" class="bg-[#161b27]">N2</option>
              <option value="N1" class="bg-[#161b27]">N1</option>
              <option value="ALL_LEVELS" class="bg-[#161b27]">Tất cả cấp độ</option>
            </select>
            <span v-if="errors.level" class="text-red-400 text-xs mt-1 block">{{ errors.level }}</span>
          </div>

          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Loại khóa học <span class="text-red-400">*</span></label>
            <select v-model="form.courseType" @change="onCourseTypeChange" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.courseType ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']">
              <option value="" class="bg-[#161b27]">-- Chọn loại --</option>
              <option value="FREE" class="bg-[#161b27]">Miễn phí</option>
              <option value="PAID" class="bg-[#161b27]">Trả phí</option>
            </select>
            <span v-if="errors.courseType" class="text-red-400 text-xs mt-1 block">{{ errors.courseType }}</span>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Giá gốc (VNĐ)</label>
            <input v-model.number="form.originalPrice" type="number" min="0" step="1000" :disabled="form.courseType === 'FREE'" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors disabled:opacity-50 disabled:cursor-not-allowed', errors.originalPrice ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
            <span v-if="errors.originalPrice" class="text-red-400 text-xs mt-1 block">{{ errors.originalPrice }}</span>
          </div>

          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Giá khuyến mãi (VNĐ)</label>
            <input v-model.number="form.salePrice" type="number" min="0" step="1000" :disabled="form.courseType === 'FREE'" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors disabled:opacity-50 disabled:cursor-not-allowed', errors.salePrice ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
            <span v-if="errors.salePrice" class="text-red-400 text-xs mt-1 block">{{ errors.salePrice }}</span>
          </div>
        </div>

        <div v-if="isEditMode">
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Trạng thái <span class="text-red-400">*</span></label>
          <select v-model="form.status" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.status ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']">
            <option value="DRAFT" class="bg-[#161b27]">Bản nháp</option>
            <option value="PUBLISHED" class="bg-[#161b27]">Đã xuất bản</option>
            <option value="HIDDEN" class="bg-[#161b27]">Đang ẩn</option>
            <option value="ARCHIVED" class="bg-[#161b27]">Đã lưu trữ</option>
          </select>
          <span v-if="errors.status" class="text-red-400 text-xs mt-1 block">{{ errors.status }}</span>
        </div>

        <div class="pt-4 border-t border-white/10 flex justify-end gap-3 mt-2">
          <button type="button" class="px-5 py-2.5 rounded-xl border border-white/10 bg-transparent text-white/80 hover:bg-white/5 transition-colors font-medium text-sm" @click="$emit('close')">Hủy</button>
          <button type="submit" class="px-6 py-2.5 rounded-xl bg-stitch-primary text-white font-medium text-sm hover:bg-stitch-primary/90 transition-colors shadow-lg disabled:opacity-50" :disabled="isSubmitting">
            {{ isSubmitting ? 'Đang xử lý...' : (isEditMode ? 'Cập nhật' : 'Tạo khóa học') }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'

const props = defineProps({
  editingCourse: { type: Object, default: null }
})
const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingCourse)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({
  title: '', slug: '', shortDescription: '', description: '', thumbnailUrl: '',
  level: '', courseType: '', originalPrice: 0, salePrice: 0, status: 'DRAFT'
})

const errors = reactive({
  title: '', level: '', courseType: '', originalPrice: '', salePrice: '', status: ''
})

onMounted(() => {
  if (props.editingCourse) {
    Object.assign(form, {
      title: props.editingCourse.title || '',
      slug: props.editingCourse.slug || '',
      shortDescription: props.editingCourse.shortDescription || '',
      description: props.editingCourse.description || '',
      thumbnailUrl: props.editingCourse.thumbnailUrl || '',
      level: props.editingCourse.level || '',
      courseType: props.editingCourse.courseType || '',
      originalPrice: props.editingCourse.originalPrice || 0,
      salePrice: props.editingCourse.salePrice || 0,
      status: props.editingCourse.status || 'DRAFT'
    })
  }
})

const onCourseTypeChange = () => {
  if (form.courseType === 'FREE') { form.originalPrice = 0; form.salePrice = 0; }
}

const validate = () => {
  Object.keys(errors).forEach(k => errors[k] = '')
  let isValid = true

  if (!form.title.trim()) { errors.title = 'Tên không được trống.'; isValid = false }
  else if (form.title.trim().length > 255) { errors.title = 'Tên quá dài.'; isValid = false }
  if (!form.level) { errors.level = 'Vui lòng chọn cấp độ.'; isValid = false }
  if (!form.courseType) { errors.courseType = 'Vui lòng chọn loại khóa học.'; isValid = false }
  if (form.originalPrice < 0) { errors.originalPrice = 'Giá gốc không được âm.'; isValid = false }
  if (form.salePrice < 0) { errors.salePrice = 'Giá khuyến mãi không được âm.'; isValid = false }
  if (form.courseType === 'PAID' && form.salePrice > form.originalPrice && form.originalPrice > 0) {
    errors.salePrice = 'Giá khuyến mãi không lớn hơn giá gốc.'; isValid = false
  }
  if (isEditMode.value && !form.status) { errors.status = 'Vui lòng chọn trạng thái.'; isValid = false }

  return isValid
}

const handleSubmit = async () => {
  if (!validate()) return
  isSubmitting.value = true
  apiError.value = ''

  try {
    const payload = {
      title: form.title.trim(), slug: form.slug.trim() || null,
      shortDescription: form.shortDescription.trim() || null, description: form.description.trim() || null,
      thumbnailUrl: form.thumbnailUrl.trim() || null, level: form.level, courseType: form.courseType,
      originalPrice: form.originalPrice || 0, salePrice: form.salePrice || 0
    }
    if (isEditMode.value) payload.status = form.status

    if (isEditMode.value) await AdminService.updateCourse(props.editingCourse.id, payload)
    else await AdminService.createCourse(payload)

    emit('saved')
  } catch (error) {
    apiError.value = getApiErrorMessage(error, 'Đã xảy ra lỗi.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

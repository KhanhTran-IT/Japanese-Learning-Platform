<template>
  <div class="fixed inset-0 bg-[#0f1117]/80 backdrop-blur-sm flex items-center justify-center z-50 p-4" @click.self="$emit('close')">
    <div class="bg-[#161b27] rounded-2xl w-full max-w-2xl max-h-[90vh] flex flex-col shadow-2xl border border-white/10">
      <div class="flex justify-between items-center p-6 border-b border-white/10 bg-white/[0.02]">
        <h2 class="text-xl font-stitch-serif font-bold text-white">{{ isEditMode ? 'Cập nhật Bài học' : 'Thêm Bài học mới' }}</h2>
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
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Tên bài học <span class="text-red-400">*</span></label>
          <input v-model="form.title" type="text" maxlength="255" placeholder="VD: Bài 1: Bảng chữ cái Hiragana" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:bg-white/10 transition-colors', errors.title ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
          <span v-if="errors.title" class="text-red-400 text-xs mt-1 block">{{ errors.title }}</span>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Slug (không bắt buộc)</label>
          <input v-model="form.slug" type="text" maxlength="255" placeholder="VD: bai-1-hiragana" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Nội dung text (không bắt buộc)</label>
          <textarea v-model="form.content" rows="4" placeholder="Nhập nội dung bài học..." class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors resize-none"></textarea>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">URL Video (không bắt buộc)</label>
          <input v-model="form.videoUrl" type="text" placeholder="https://example.com/video.mp4" class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Thời lượng (phút)</label>
            <input v-model.number="form.durationMinutes" type="number" min="0" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.durationMinutes ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
            <span v-if="errors.durationMinutes" class="text-red-400 text-xs mt-1 block">{{ errors.durationMinutes }}</span>
          </div>

          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Thứ tự hiển thị</label>
            <input v-model.number="form.sortOrder" type="number" min="0" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.sortOrder ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
            <span v-if="errors.sortOrder" class="text-red-400 text-xs mt-1 block">{{ errors.sortOrder }}</span>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-4 items-center">
          <label class="flex items-center gap-3 p-3 rounded-xl border border-white/10 bg-white/[0.02] cursor-pointer hover:bg-white/[0.04] transition-colors h-[42px] mt-6">
            <input type="checkbox" v-model="form.isPreview" class="w-4 h-4 rounded border-white/20 text-stitch-primary focus:ring-stitch-primary focus:ring-offset-[#161b27]" />
            <span class="text-sm font-medium text-white/90">Cho phép học thử (Preview)</span>
          </label>

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
        </div>

        <div class="pt-4 border-t border-white/10 flex justify-end gap-3 mt-2">
          <button type="button" class="px-5 py-2.5 rounded-xl border border-white/10 bg-transparent text-white/80 hover:bg-white/5 transition-colors font-medium text-sm" @click="$emit('close')">Hủy</button>
          <button type="submit" class="px-6 py-2.5 rounded-xl bg-stitch-primary text-white font-medium text-sm hover:bg-stitch-primary/90 transition-colors shadow-lg disabled:opacity-50" :disabled="isSubmitting">
            {{ isSubmitting ? 'Đang lưu...' : (isEditMode ? 'Cập nhật' : 'Thêm mới') }}
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
  sectionId: { type: [Number, String], required: true },
  editingLesson: { type: Object, default: null }
})
const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingLesson)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({
  title: '', slug: '', content: '', videoUrl: '', durationMinutes: 0, sortOrder: 1, isPreview: false, status: 'DRAFT'
})
const errors = reactive({ title: '', durationMinutes: '', sortOrder: '', status: '' })

onMounted(() => {
  if (props.editingLesson) {
    form.title = props.editingLesson.title || ''
    form.slug = props.editingLesson.slug || ''
    form.content = props.editingLesson.content || ''
    form.videoUrl = props.editingLesson.videoUrl || ''
    form.durationMinutes = props.editingLesson.durationMinutes || 0
    form.sortOrder = props.editingLesson.sortOrder !== undefined ? props.editingLesson.sortOrder : 1
    form.isPreview = !!props.editingLesson.isPreview
    form.status = props.editingLesson.status || 'DRAFT'
  }
})

const validate = () => {
  Object.keys(errors).forEach(k => errors[k] = '')
  let isValid = true

  if (!form.title.trim()) { errors.title = 'Tên không để trống.'; isValid = false }
  else if (form.title.trim().length > 255) { errors.title = 'Quá 255 ký tự.'; isValid = false }
  if (form.durationMinutes < 0) { errors.durationMinutes = 'Không nhỏ hơn 0.'; isValid = false }
  if (form.sortOrder < 0) { errors.sortOrder = 'Không nhỏ hơn 0.'; isValid = false }
  if (isEditMode.value && !form.status) { errors.status = 'Chọn trạng thái.'; isValid = false }

  return isValid
}

const handleSubmit = async () => {
  if (!validate()) return
  isSubmitting.value = true
  apiError.value = ''

  try {
    const payload = {
      title: form.title.trim(), slug: form.slug.trim() || null, content: form.content.trim() || null,
      videoUrl: form.videoUrl.trim() || null, durationMinutes: form.durationMinutes, sortOrder: form.sortOrder,
      isPreview: form.isPreview
    }
    if (isEditMode.value) {
      payload.status = form.status
      await AdminService.updateLesson(props.editingLesson.id, payload)
    } else {
      await AdminService.createLesson(props.sectionId, payload)
    }
    emit('saved')
  } catch (error) {
    apiError.value = getApiErrorMessage(error, 'Không thể lưu bài học.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

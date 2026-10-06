<template>
  <div class="fixed inset-0 bg-[#0f1117]/80 backdrop-blur-sm flex items-center justify-center z-50 p-4" @click.self="$emit('close')">
    <div class="bg-[#161b27] rounded-2xl w-full max-w-lg max-h-[90vh] flex flex-col shadow-2xl border border-white/10">
      <div class="flex justify-between items-center p-6 border-b border-white/10 bg-white/[0.02]">
        <h2 class="text-xl font-stitch-serif font-bold text-white">{{ isEditMode ? 'Cập nhật Tài liệu' : 'Thêm Tài liệu mới' }}</h2>
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
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Tên tài liệu <span class="text-red-400">*</span></label>
          <input v-model="form.title" type="text" maxlength="255" placeholder="VD: Tài liệu luyện đọc N5" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:bg-white/10 transition-colors', errors.title ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
          <span v-if="errors.title" class="text-red-400 text-xs mt-1 block">{{ errors.title }}</span>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Loại tài liệu <span class="text-red-400">*</span></label>
            <select v-model="form.resourceType" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.resourceType ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']">
              <option value="" class="bg-[#161b27]">-- Chọn loại --</option>
              <option value="PDF" class="bg-[#161b27]">PDF</option>
              <option value="DOCUMENT" class="bg-[#161b27]">Document</option>
              <option value="AUDIO" class="bg-[#161b27]">Audio</option>
              <option value="VIDEO" class="bg-[#161b27]">Video</option>
              <option value="EXTERNAL_LINK" class="bg-[#161b27]">External Link</option>
            </select>
            <span v-if="errors.resourceType" class="text-red-400 text-xs mt-1 block">{{ errors.resourceType }}</span>
          </div>

          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Thứ tự hiển thị</label>
            <input v-model.number="form.sortOrder" type="number" min="0" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:bg-white/10 transition-colors', errors.sortOrder ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
            <span v-if="errors.sortOrder" class="text-red-400 text-xs mt-1 block">{{ errors.sortOrder }}</span>
          </div>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">URL tài liệu <span class="text-red-400">*</span></label>
          <input v-model="form.fileUrl" type="text" maxlength="1000" placeholder="https://example.com/document.pdf" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:bg-white/10 transition-colors', errors.fileUrl ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
          <span v-if="errors.fileUrl" class="text-red-400 text-xs mt-1 block">{{ errors.fileUrl }}</span>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Kích thước file (bytes, không bắt buộc)</label>
          <input v-model.number="form.fileSize" type="number" min="0" placeholder="VD: 1024000" :class="['w-full bg-white/5 border rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:bg-white/10 transition-colors', errors.fileSize ? 'border-red-500 focus:border-red-500' : 'border-white/10 focus:border-stitch-primary']" />
          <span v-if="errors.fileSize" class="text-red-400 text-xs mt-1 block">{{ errors.fileSize }}</span>
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
  lessonId: { type: [Number, String], required: true },
  editingResource: { type: Object, default: null }
})
const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingResource)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({ title: '', resourceType: '', fileUrl: '', fileSize: null, sortOrder: 0 })
const errors = reactive({ title: '', resourceType: '', fileUrl: '', fileSize: '', sortOrder: '' })

onMounted(() => {
  if (props.editingResource) {
    form.title = props.editingResource.title || ''
    form.resourceType = props.editingResource.resourceType || ''
    form.fileUrl = props.editingResource.fileUrl || ''
    form.fileSize = props.editingResource.fileSize || null
    form.sortOrder = props.editingResource.sortOrder !== undefined ? props.editingResource.sortOrder : 0
  }
})

const validate = () => {
  Object.keys(errors).forEach(k => errors[k] = '')
  let isValid = true

  if (!form.title.trim()) { errors.title = 'Tên không để trống.'; isValid = false }
  if (!form.resourceType) { errors.resourceType = 'Chọn loại tài liệu.'; isValid = false }
  if (!form.fileUrl.trim()) { errors.fileUrl = 'URL không để trống.'; isValid = false }
  if (form.fileSize !== null && form.fileSize !== '' && form.fileSize < 0) { errors.fileSize = 'Không được âm.'; isValid = false }
  if (form.sortOrder < 0) { errors.sortOrder = 'Không được âm.'; isValid = false }

  return isValid
}

const handleSubmit = async () => {
  if (!validate()) return
  isSubmitting.value = true
  apiError.value = ''

  try {
    const payload = {
      title: form.title.trim(), resourceType: form.resourceType, fileUrl: form.fileUrl.trim(),
      fileSize: (form.fileSize !== null && form.fileSize !== '') ? form.fileSize : null, sortOrder: form.sortOrder
    }
    if (isEditMode.value) await AdminService.updateLessonResource(props.editingResource.id, payload)
    else await AdminService.createLessonResource(props.lessonId, payload)
    emit('saved')
  } catch (error) {
    apiError.value = getApiErrorMessage(error, 'Không thể lưu tài liệu.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

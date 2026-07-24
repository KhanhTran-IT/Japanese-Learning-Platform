<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h2 class="modal-title">{{ isEditMode ? 'Cập nhật Bài học' : 'Thêm Bài học mới' }}</h2>
        <button class="btn-close" @click="$emit('close')">&times;</button>
      </div>

      <div v-if="apiError" class="form-error-banner">
        {{ apiError }}
        <button @click="apiError = ''" class="btn-dismiss">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <div class="form-group">
          <label for="lf-title">Tên bài học <span class="required">*</span></label>
          <input
            id="lf-title"
            v-model="form.title"
            type="text"
            placeholder="VD: Bài 1: Bảng chữ cái Hiragana"
            :class="{ 'input-error': errors.title }"
          />
          <span v-if="errors.title" class="field-error">{{ errors.title }}</span>
        </div>

        <div class="form-group">
          <label for="lf-slug">Slug (không bắt buộc)</label>
          <input
            id="lf-slug"
            v-model="form.slug"
            type="text"
            placeholder="VD: bai-1-hiragana"
          />
        </div>

        <div class="form-group">
          <label for="lf-content">Nội dung text (không bắt buộc)</label>
          <textarea
            id="lf-content"
            v-model="form.content"
            rows="4"
            placeholder="Nhập nội dung bài học..."
          ></textarea>
        </div>

        <div class="form-group">
          <label for="lf-video">URL Video (không bắt buộc)</label>
          <input
            id="lf-video"
            v-model="form.videoUrl"
            type="text"
            placeholder="https://example.com/video.mp4"
          />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="lf-duration">Thời lượng (phút)</label>
            <input
              id="lf-duration"
              v-model.number="form.durationMinutes"
              type="number"
              min="0"
              :class="{ 'input-error': errors.durationMinutes }"
            />
            <span v-if="errors.durationMinutes" class="field-error">{{ errors.durationMinutes }}</span>
          </div>

          <div class="form-group">
            <label for="lf-sort">Thứ tự hiển thị</label>
            <input
              id="lf-sort"
              v-model.number="form.sortOrder"
              type="number"
              min="0"
              :class="{ 'input-error': errors.sortOrder }"
            />
            <span v-if="errors.sortOrder" class="field-error">{{ errors.sortOrder }}</span>
          </div>
        </div>

        <div class="form-row">
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.isPreview" />
              <span>Cho phép học thử (Preview)</span>
            </label>
          </div>

          <div v-if="isEditMode" class="form-group">
            <label for="lf-status">Trạng thái <span class="required">*</span></label>
            <select id="lf-status" v-model="form.status" :class="{ 'input-error': errors.status }">
              <option value="DRAFT">Bản nháp</option>
              <option value="PUBLISHED">Đã xuất bản</option>
              <option value="HIDDEN">Đang ẩn</option>
              <option value="ARCHIVED">Đã lưu trữ</option>
            </select>
            <span v-if="errors.status" class="field-error">{{ errors.status }}</span>
          </div>
        </div>

        <div class="modal-footer">
          <button type="button" class="btn-cancel" @click="$emit('close')">Hủy</button>
          <button type="submit" class="btn-submit" :disabled="isSubmitting">
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
  sectionId: {
    type: [Number, String],
    required: true
  },
  editingLesson: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingLesson)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({
  title: '',
  slug: '',
  content: '',
  videoUrl: '',
  durationMinutes: 0,
  sortOrder: 1,
  isPreview: false,
  status: 'DRAFT'
})

const errors = reactive({
  title: '',
  durationMinutes: '',
  sortOrder: '',
  status: ''
})

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

const clearErrors = () => {
  Object.keys(errors).forEach(key => errors[key] = '')
}

const validate = () => {
  clearErrors()
  let isValid = true

  if (!form.title.trim()) {
    errors.title = 'Tên bài học không được để trống.'
    isValid = false
  }
  
  if (form.durationMinutes < 0) {
    errors.durationMinutes = 'Thời lượng không được nhỏ hơn 0.'
    isValid = false
  }

  if (form.sortOrder < 0) {
    errors.sortOrder = 'Thứ tự không được nhỏ hơn 0.'
    isValid = false
  }

  if (isEditMode.value && !form.status) {
    errors.status = 'Vui lòng chọn trạng thái.'
    isValid = false
  }

  return isValid
}

const handleSubmit = async () => {
  if (!validate()) return

  isSubmitting.value = true
  apiError.value = ''

  try {
    const payload = {
      title: form.title.trim(),
      slug: form.slug.trim() || null,
      content: form.content.trim() || null,
      videoUrl: form.videoUrl.trim() || null,
      durationMinutes: form.durationMinutes,
      sortOrder: form.sortOrder,
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

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}
.modal-container {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}
.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #0f172a;
}
.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #94a3b8;
  cursor: pointer;
}
.btn-close:hover {
  color: #0f172a;
}
.form-error-banner {
  background-color: #fef2f2;
  color: #b91c1c;
  padding: 0.75rem 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
  border-bottom: 1px solid #fecaca;
}
.btn-dismiss {
  background: none;
  border: none;
  color: #b91c1c;
  cursor: pointer;
  font-size: 1.2rem;
}
.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}
.form-group {
  margin-bottom: 1.25rem;
}
.form-group label {
  display: block;
  font-weight: 600;
  font-size: 0.9rem;
  color: #334155;
  margin-bottom: 0.375rem;
}
.required {
  color: #ef4444;
}
.form-group input:not([type="checkbox"]),
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.95rem;
  outline: none;
  box-sizing: border-box;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.input-error {
  border-color: #ef4444 !important;
}
.field-error {
  display: block;
  color: #ef4444;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}
.form-group textarea {
  resize: vertical;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}
.checkbox-group {
  display: flex;
  align-items: center;
  margin-top: 1.5rem;
}
.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 500 !important;
}
.checkbox-label input {
  width: 1.1rem;
  height: 1.1rem;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
  margin-top: 0.5rem;
}
.btn-cancel, .btn-submit {
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}
.btn-cancel {
  border: 1px solid #cbd5e1;
  background: white;
  color: #475569;
}
.btn-cancel:hover {
  background: #f8fafc;
}
.btn-submit {
  background: #3b82f6;
  color: white;
  border: none;
}
.btn-submit:hover:not(:disabled) {
  background: #2563eb;
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

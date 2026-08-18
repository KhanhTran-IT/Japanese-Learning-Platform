<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h2 class="modal-title">{{ isEditMode ? 'Cập nhật Chương học' : 'Thêm Chương mới' }}</h2>
        <button class="btn-close" @click="$emit('close')">&times;</button>
      </div>

      <div v-if="apiError" class="form-error-banner">
        {{ apiError }}
        <button @click="apiError = ''" class="btn-dismiss">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <div class="form-group">
          <label for="sf-title">Tên chương <span class="required">*</span></label>
          <input
            id="sf-title"
            v-model="form.title"
            type="text"
            placeholder="VD: Chương 1: Giới thiệu"
            :class="{ 'input-error': errors.title }"
          />
          <span v-if="errors.title" class="field-error">{{ errors.title }}</span>
        </div>

        <div class="form-group">
          <label for="sf-desc">Mô tả (không bắt buộc)</label>
          <textarea
            id="sf-desc"
            v-model="form.description"
            rows="3"
            placeholder="Nhập mô tả ngắn gọn cho chương này..."
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="sf-sort">Thứ tự hiển thị</label>
            <input
              id="sf-sort"
              v-model.number="form.sortOrder"
              type="number"
              min="0"
              :class="{ 'input-error': errors.sortOrder }"
            />
            <span v-if="errors.sortOrder" class="field-error">{{ errors.sortOrder }}</span>
          </div>

          <div v-if="isEditMode" class="form-group">
            <label for="sf-status">Trạng thái <span class="required">*</span></label>
            <select id="sf-status" v-model="form.status" :class="{ 'input-error': errors.status }">
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
  courseId: {
    type: [Number, String],
    required: true
  },
  editingSection: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingSection)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({
  title: '',
  description: '',
  sortOrder: 1,
  status: 'DRAFT'
})

const errors = reactive({
  title: '',
  sortOrder: '',
  status: ''
})

onMounted(() => {
  if (props.editingSection) {
    form.title = props.editingSection.title || ''
    form.description = props.editingSection.description || ''
    form.sortOrder = props.editingSection.sortOrder !== undefined ? props.editingSection.sortOrder : 1
    form.status = props.editingSection.status || 'DRAFT'
  }
})

const clearErrors = () => {
  Object.keys(errors).forEach(key => errors[key] = '')
}

const validate = () => {
  clearErrors()
  let isValid = true

  if (!form.title.trim()) {
    errors.title = 'Tên chương không được để trống.'
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
      description: form.description.trim() || null,
      sortOrder: form.sortOrder
    }

    if (isEditMode.value) {
      payload.status = form.status
      await AdminService.updateSection(props.editingSection.id, payload)
    } else {
      await AdminService.createSection(props.courseId, payload)
    }

    emit('saved')
  } catch (error) {
    apiError.value = getApiErrorMessage(error, 'Không thể lưu chương học.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
/* Tái sử dụng CSS từ CourseFormModal */
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
  max-width: 550px;
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
.form-group input,
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

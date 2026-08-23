<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h2 class="modal-title">{{ isEditMode ? 'Cập nhật Tài liệu' : 'Thêm Tài liệu mới' }}</h2>
        <button class="btn-close" @click="$emit('close')">&times;</button>
      </div>

      <div v-if="apiError" class="form-error-banner">
        {{ apiError }}
        <button @click="apiError = ''" class="btn-dismiss">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <div class="form-group">
          <label for="rf-title">Tên tài liệu <span class="required">*</span></label>
          <input
            id="rf-title"
            v-model="form.title"
            type="text"
            maxlength="255"
            placeholder="VD: Tài liệu luyện đọc N5"
            :class="{ 'input-error': errors.title }"
          />
          <span v-if="errors.title" class="field-error">{{ errors.title }}</span>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label for="rf-type">Loại tài liệu <span class="required">*</span></label>
            <select id="rf-type" v-model="form.resourceType" :class="{ 'input-error': errors.resourceType }">
              <option value="">-- Chọn loại --</option>
              <option value="PDF">PDF</option>
              <option value="DOCUMENT">Document</option>
              <option value="AUDIO">Audio</option>
              <option value="VIDEO">Video</option>
              <option value="EXTERNAL_LINK">External Link</option>
            </select>
            <span v-if="errors.resourceType" class="field-error">{{ errors.resourceType }}</span>
          </div>

          <div class="form-group">
            <label for="rf-sort">Thứ tự hiển thị</label>
            <input
              id="rf-sort"
              v-model.number="form.sortOrder"
              type="number"
              min="0"
              :class="{ 'input-error': errors.sortOrder }"
            />
            <span v-if="errors.sortOrder" class="field-error">{{ errors.sortOrder }}</span>
          </div>
        </div>

        <div class="form-group">
          <label for="rf-url">URL tài liệu <span class="required">*</span></label>
          <input
            id="rf-url"
            v-model="form.fileUrl"
            type="text"
            maxlength="1000"
            placeholder="https://example.com/document.pdf"
            :class="{ 'input-error': errors.fileUrl }"
          />
          <span v-if="errors.fileUrl" class="field-error">{{ errors.fileUrl }}</span>
        </div>

        <div class="form-group">
          <label for="rf-size">Kích thước file (bytes, không bắt buộc)</label>
          <input
            id="rf-size"
            v-model.number="form.fileSize"
            type="number"
            min="0"
            placeholder="VD: 1024000"
            :class="{ 'input-error': errors.fileSize }"
          />
          <span v-if="errors.fileSize" class="field-error">{{ errors.fileSize }}</span>
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
  lessonId: {
    type: [Number, String],
    required: true
  },
  editingResource: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingResource)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({
  title: '',
  resourceType: '',
  fileUrl: '',
  fileSize: null,
  sortOrder: 0
})

const errors = reactive({
  title: '',
  resourceType: '',
  fileUrl: '',
  fileSize: '',
  sortOrder: ''
})

onMounted(() => {
  if (props.editingResource) {
    form.title = props.editingResource.title || ''
    form.resourceType = props.editingResource.resourceType || ''
    form.fileUrl = props.editingResource.fileUrl || ''
    form.fileSize = props.editingResource.fileSize || null
    form.sortOrder = props.editingResource.sortOrder !== undefined ? props.editingResource.sortOrder : 0
  }
})

const clearErrors = () => {
  Object.keys(errors).forEach(key => errors[key] = '')
}

const validate = () => {
  clearErrors()
  let isValid = true

  if (!form.title.trim()) {
    errors.title = 'Tên tài liệu không được để trống.'
    isValid = false
  } else if (form.title.trim().length > 255) {
    errors.title = 'Tên tài liệu không được quá 255 ký tự.'
    isValid = false
  }

  if (!form.resourceType) {
    errors.resourceType = 'Vui lòng chọn loại tài liệu.'
    isValid = false
  }

  if (!form.fileUrl.trim()) {
    errors.fileUrl = 'URL tài liệu không được để trống.'
    isValid = false
  } else if (form.fileUrl.trim().length > 1000) {
    errors.fileUrl = 'URL không được quá 1000 ký tự.'
    isValid = false
  }

  if (form.fileSize !== null && form.fileSize !== '' && form.fileSize < 0) {
    errors.fileSize = 'Kích thước file không được âm.'
    isValid = false
  }

  if (form.sortOrder < 0) {
    errors.sortOrder = 'Thứ tự không được nhỏ hơn 0.'
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
      resourceType: form.resourceType,
      fileUrl: form.fileUrl.trim(),
      fileSize: (form.fileSize !== null && form.fileSize !== '') ? form.fileSize : null,
      sortOrder: form.sortOrder
    }

    if (isEditMode.value) {
      await AdminService.updateLessonResource(props.editingResource.id, payload)
    } else {
      await AdminService.createLessonResource(props.lessonId, payload)
    }

    emit('saved')
  } catch (error) {
    apiError.value = getApiErrorMessage(error, 'Không thể lưu tài liệu.')
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
.form-group select {
  width: 100%;
  padding: 0.625rem 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.95rem;
  outline: none;
  box-sizing: border-box;
}
.form-group input:focus,
.form-group select:focus {
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

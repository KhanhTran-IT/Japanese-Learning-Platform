<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h2 class="modal-title">{{ isEditMode ? 'Cập nhật Khóa học' : 'Tạo Khóa học mới' }}</h2>
        <button class="btn-close" @click="$emit('close')">&times;</button>
      </div>

      <!-- Inline form error from API -->
      <div v-if="apiError" class="form-error-banner">
        {{ apiError }}
        <button @click="apiError = ''" class="btn-dismiss">&times;</button>
      </div>

      <form @submit.prevent="handleSubmit" class="modal-body">
        <!-- Title -->
        <div class="form-group">
          <label for="cf-title">Tên khóa học <span class="required">*</span></label>
          <input
            id="cf-title"
            v-model="form.title"
            type="text"
            maxlength="255"
            placeholder="VD: Khóa học N5 nhập môn cho người mới"
            :class="{ 'input-error': errors.title }"
          />
          <span v-if="errors.title" class="field-error">{{ errors.title }}</span>
        </div>

        <!-- Slug -->
        <div class="form-group">
          <label for="cf-slug">Slug (đường dẫn)</label>
          <input
            id="cf-slug"
            v-model="form.slug"
            type="text"
            maxlength="255"
            placeholder="Để trống sẽ tự tạo từ tên khóa học"
          />
          <span class="field-hint">VD: khoa-hoc-n5-nhap-mon</span>
        </div>

        <!-- Short Description -->
        <div class="form-group">
          <label for="cf-short-desc">Mô tả ngắn</label>
          <input
            id="cf-short-desc"
            v-model="form.shortDescription"
            type="text"
            placeholder="Giới thiệu tóm tắt khóa học"
          />
        </div>

        <!-- Description -->
        <div class="form-group">
          <label for="cf-desc">Mô tả chi tiết</label>
          <textarea
            id="cf-desc"
            v-model="form.description"
            rows="4"
            placeholder="Nội dung mô tả chi tiết khóa học..."
          ></textarea>
        </div>

        <!-- Thumbnail URL -->
        <div class="form-group">
          <label for="cf-thumb">URL ảnh đại diện</label>
          <input
            id="cf-thumb"
            v-model="form.thumbnailUrl"
            type="text"
            placeholder="https://example.com/thumbnail.jpg"
          />
        </div>

        <!-- Level + CourseType row -->
        <div class="form-row">
          <div class="form-group">
            <label for="cf-level">Cấp độ <span class="required">*</span></label>
            <select id="cf-level" v-model="form.level" :class="{ 'input-error': errors.level }">
              <option value="">-- Chọn cấp độ --</option>
              <option value="N5">N5</option>
              <option value="N4">N4</option>
              <option value="N3">N3</option>
              <option value="N2">N2</option>
              <option value="N1">N1</option>
              <option value="ALL_LEVELS">Tất cả cấp độ</option>
            </select>
            <span v-if="errors.level" class="field-error">{{ errors.level }}</span>
          </div>

          <div class="form-group">
            <label for="cf-type">Loại khóa học <span class="required">*</span></label>
            <select id="cf-type" v-model="form.courseType" :class="{ 'input-error': errors.courseType }" @change="onCourseTypeChange">
              <option value="">-- Chọn loại --</option>
              <option value="FREE">Miễn phí</option>
              <option value="PAID">Trả phí</option>
            </select>
            <span v-if="errors.courseType" class="field-error">{{ errors.courseType }}</span>
          </div>
        </div>

        <!-- Prices row -->
        <div class="form-row">
          <div class="form-group">
            <label for="cf-original-price">Giá gốc (VNĐ)</label>
            <input
              id="cf-original-price"
              v-model.number="form.originalPrice"
              type="number"
              min="0"
              step="1000"
              :disabled="form.courseType === 'FREE'"
              :class="{ 'input-error': errors.originalPrice }"
            />
            <span v-if="errors.originalPrice" class="field-error">{{ errors.originalPrice }}</span>
          </div>

          <div class="form-group">
            <label for="cf-sale-price">Giá khuyến mãi (VNĐ)</label>
            <input
              id="cf-sale-price"
              v-model.number="form.salePrice"
              type="number"
              min="0"
              step="1000"
              :disabled="form.courseType === 'FREE'"
              :class="{ 'input-error': errors.salePrice }"
            />
            <span v-if="errors.salePrice" class="field-error">{{ errors.salePrice }}</span>
          </div>
        </div>

        <!-- Status (chỉ hiện khi Update) -->
        <div v-if="isEditMode" class="form-group">
          <label for="cf-status">Trạng thái <span class="required">*</span></label>
          <select id="cf-status" v-model="form.status" :class="{ 'input-error': errors.status }">
            <option value="DRAFT">Bản nháp</option>
            <option value="PUBLISHED">Đã xuất bản</option>
            <option value="HIDDEN">Đang ẩn</option>
            <option value="ARCHIVED">Đã lưu trữ</option>
          </select>
          <span v-if="errors.status" class="field-error">{{ errors.status }}</span>
        </div>

        <!-- Actions -->
        <div class="modal-footer">
          <button type="button" class="btn-cancel" @click="$emit('close')">Hủy</button>
          <button type="submit" class="btn-submit" :disabled="isSubmitting">
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
  /** Truyền object course để chỉnh sửa. Nếu null → chế độ Create. */
  editingCourse: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const isEditMode = computed(() => !!props.editingCourse)
const isSubmitting = ref(false)
const apiError = ref('')

const form = reactive({
  title: '',
  slug: '',
  shortDescription: '',
  description: '',
  thumbnailUrl: '',
  level: '',
  courseType: '',
  originalPrice: 0,
  salePrice: 0,
  status: 'DRAFT'
})

const errors = reactive({
  title: '',
  level: '',
  courseType: '',
  originalPrice: '',
  salePrice: '',
  status: ''
})

// Pre-fill dữ liệu khi ở chế độ Update
onMounted(() => {
  if (props.editingCourse) {
    form.title = props.editingCourse.title || ''
    form.slug = props.editingCourse.slug || ''
    form.shortDescription = props.editingCourse.shortDescription || ''
    form.description = props.editingCourse.description || ''
    form.thumbnailUrl = props.editingCourse.thumbnailUrl || ''
    form.level = props.editingCourse.level || ''
    form.courseType = props.editingCourse.courseType || ''
    form.originalPrice = props.editingCourse.originalPrice || 0
    form.salePrice = props.editingCourse.salePrice || 0
    form.status = props.editingCourse.status || 'DRAFT'
  }
})

const onCourseTypeChange = () => {
  if (form.courseType === 'FREE') {
    form.originalPrice = 0
    form.salePrice = 0
  }
}

const clearErrors = () => {
  Object.keys(errors).forEach(key => errors[key] = '')
}

const validate = () => {
  clearErrors()
  let isValid = true

  if (!form.title.trim()) {
    errors.title = 'Tên khóa học không được để trống.'
    isValid = false
  } else if (form.title.trim().length > 255) {
    errors.title = 'Tên khóa học không được quá 255 ký tự.'
    isValid = false
  }

  if (!form.level) {
    errors.level = 'Vui lòng chọn cấp độ.'
    isValid = false
  }

  if (!form.courseType) {
    errors.courseType = 'Vui lòng chọn loại khóa học.'
    isValid = false
  }

  if (form.originalPrice < 0) {
    errors.originalPrice = 'Giá gốc không được âm.'
    isValid = false
  }

  if (form.salePrice < 0) {
    errors.salePrice = 'Giá khuyến mãi không được âm.'
    isValid = false
  }

  if (form.courseType === 'PAID' && form.salePrice > form.originalPrice && form.originalPrice > 0) {
    errors.salePrice = 'Giá khuyến mãi không được lớn hơn giá gốc.'
    isValid = false
  }

  if (isEditMode.value && !form.status) {
    errors.status = 'Vui lòng chọn trạng thái.'
    isValid = false
  }

  return isValid
}

const buildPayload = () => {
  const payload = {
    title: form.title.trim(),
    slug: form.slug.trim() || null,
    shortDescription: form.shortDescription.trim() || null,
    description: form.description.trim() || null,
    thumbnailUrl: form.thumbnailUrl.trim() || null,
    level: form.level,
    courseType: form.courseType,
    originalPrice: form.originalPrice || 0,
    salePrice: form.salePrice || 0
  }

  if (isEditMode.value) {
    payload.status = form.status
  }

  return payload
}

const handleSubmit = async () => {
  if (!validate()) return

  isSubmitting.value = true
  apiError.value = ''

  try {
    const payload = buildPayload()

    if (isEditMode.value) {
      await AdminService.updateCourse(props.editingCourse.id, payload)
    } else {
      await AdminService.createCourse(payload)
    }

    emit('saved')
  } catch (error) {
    apiError.value = getApiErrorMessage(error, 'Đã xảy ra lỗi. Vui lòng thử lại.')
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
  max-width: 680px;
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
  line-height: 1;
}
.btn-close:hover {
  color: #0f172a;
}

/* Error banner */
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

/* Body */
.modal-body {
  padding: 1.5rem;
  overflow-y: auto;
  flex: 1;
}

/* Form groups */
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
  transition: border-color 0.2s;
  box-sizing: border-box;
  background-color: white;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.form-group input:disabled,
.form-group select:disabled {
  background-color: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
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
.field-hint {
  display: block;
  color: #94a3b8;
  font-size: 0.8rem;
  margin-top: 0.25rem;
}
.form-group textarea {
  resize: vertical;
}

/* Two-column row */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

/* Footer */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
  margin-top: 0.5rem;
}
.btn-cancel {
  padding: 0.625rem 1.25rem;
  border: 1px solid #cbd5e1;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  color: #475569;
}
.btn-cancel:hover {
  background: #f8fafc;
}
.btn-submit {
  padding: 0.625rem 1.25rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}
.btn-submit:hover:not(:disabled) {
  background: #2563eb;
}
.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>

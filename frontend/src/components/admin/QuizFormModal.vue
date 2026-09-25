<template>
  <div class="modal-backdrop">
    <div class="modal-content">
      <div class="modal-header">
        <h2>{{ editingQuiz ? 'Cập nhật Bài Tập' : 'Tạo Bài Tập Mới' }}</h2>
        <button @click="$emit('close')" class="btn-close">✕</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>Tiêu đề *</label>
            <input v-model="formData.title" type="text" required placeholder="Nhập tiêu đề bài tập..." />
          </div>
          
          <div class="form-group">
            <label>Mô tả</label>
            <textarea v-model="formData.description" rows="3" placeholder="Mô tả bài tập..."></textarea>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Thời gian làm bài (Phút) *</label>
              <input v-model.number="formData.timeLimitMinutes" type="number" min="0" required />
            </div>
            
            <div class="form-group">
              <label>Điểm qua môn *</label>
              <input v-model.number="formData.passingScore" type="number" min="0" max="100" required />
            </div>
          </div>
          
          <div class="form-group">
            <label>Số lần làm tối đa *</label>
            <input v-model.number="formData.maxAttempts" type="number" min="1" required />
          </div>
          
          <div class="form-group" v-if="!editingQuiz">
            <label>Liên kết (Course ID hoặc Lesson ID) *</label>
            <div class="link-type-selector">
              <label>
                <input type="radio" v-model="linkType" value="course" /> Course ID
              </label>
              <label>
                <input type="radio" v-model="linkType" value="lesson" /> Lesson ID
              </label>
            </div>
            <input v-if="linkType === 'course'" v-model.number="formData.courseId" type="number" min="1" placeholder="Nhập Course ID" required />
            <input v-if="linkType === 'lesson'" v-model.number="formData.lessonId" type="number" min="1" placeholder="Nhập Lesson ID" required />
          </div>

          <div v-if="errorMsg" class="form-error">
            {{ errorMsg }}
          </div>
          
          <div class="form-actions">
            <button type="button" @click="$emit('close')" class="btn-cancel" :disabled="isSubmitting">Hủy</button>
            <button type="submit" class="btn-submit" :disabled="isSubmitting">
              {{ isSubmitting ? 'Đang lưu...' : 'Lưu' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'

const props = defineProps({
  editingQuiz: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close', 'saved'])

const linkType = ref('course')
const isSubmitting = ref(false)
const errorMsg = ref('')

const formData = reactive({
  title: '',
  description: '',
  timeLimitMinutes: 0,
  passingScore: 80,
  maxAttempts: 1,
  courseId: null,
  lessonId: null
})

onMounted(() => {
  if (props.editingQuiz) {
    formData.title = props.editingQuiz.title
    formData.description = props.editingQuiz.description
    formData.timeLimitMinutes = props.editingQuiz.timeLimitMinutes || 0
    formData.passingScore = props.editingQuiz.passingScore
    formData.maxAttempts = props.editingQuiz.maxAttempts
  }
})

const handleSubmit = async () => {
  errorMsg.value = ''
  isSubmitting.value = true
  
  try {
    const payload = {
      title: formData.title,
      description: formData.description,
      timeLimitMinutes: formData.timeLimitMinutes || null,
      passingScore: formData.passingScore,
      maxAttempts: formData.maxAttempts
    }
    
    if (!props.editingQuiz) {
      if (linkType.value === 'course') {
        payload.courseId = formData.courseId
      } else {
        payload.lessonId = formData.lessonId
      }
      await AdminService.createQuiz(payload)
    } else {
      await AdminService.updateQuiz(props.editingQuiz.id, payload)
    }
    
    emit('saved')
  } catch (error) {
    errorMsg.value = getApiErrorMessage(error, 'Đã xảy ra lỗi khi lưu bài tập.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  width: 100%;
  max-width: 500px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  overflow: hidden;
}
.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #0f172a;
  margin: 0;
}
.btn-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #64748b;
  cursor: pointer;
}
.modal-body {
  padding: 1.5rem;
}
.form-group {
  margin-bottom: 1.25rem;
}
.form-row {
  display: flex;
  gap: 1rem;
}
.form-row .form-group {
  flex: 1;
}
.link-type-selector {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
}
label {
  display: block;
  font-size: 0.85rem;
  font-weight: 600;
  color: #475569;
  margin-bottom: 0.5rem;
}
input, textarea, select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 0.95rem;
  transition: border-color 0.2s;
}
input:focus, textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}
.form-error {
  color: #b91c1c;
  background-color: #fef2f2;
  padding: 0.75rem;
  border-radius: 6px;
  margin-bottom: 1.25rem;
  font-size: 0.9rem;
  border-left: 3px solid #ef4444;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}
.btn-cancel {
  padding: 0.75rem 1.25rem;
  background: white;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  color: #475569;
  font-weight: 500;
  cursor: pointer;
}
.btn-cancel:hover {
  background: #f8fafc;
}
.btn-submit {
  padding: 0.75rem 1.25rem;
  background: #3b82f6;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 500;
  cursor: pointer;
}
.btn-submit:hover:not(:disabled) {
  background: #2563eb;
}
.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>

<template>
  <div class="learning-page-container">
    <!-- Header/Navigation -->
    <header class="learning-header">
      <router-link to="/student/my-courses" class="btn-back">
        <span>←</span> Quay lại Khóa học
      </router-link>
      <div v-if="lesson" class="lesson-meta-header">
        <span class="badge">{{ lesson.durationMinutes }} phút</span>
      </div>
    </header>

    <!-- Loading State -->
    <div v-if="isLoading" class="state-container">
      <div class="spinner"></div>
      <p>Đang tải bài học...</p>
    </div>

    <!-- Error/Forbidden State -->
    <div v-else-if="errorMsg" class="state-container error-state">
      <h2>Không thể truy cập</h2>
      <p>{{ errorMsg }}</p>
      <router-link to="/student/my-courses" class="btn-primary">Về danh sách khóa học</router-link>
    </div>

    <!-- Main Content -->
    <main v-else-if="lesson" class="learning-main">
      <h1 class="lesson-title">{{ lesson.title }}</h1>
      
      <!-- Video Section -->
      <div v-if="lesson.videoUrl" class="video-container">
        <video :src="lesson.videoUrl" controls class="lesson-video">
          Trình duyệt của bạn không hỗ trợ video.
        </video>
      </div>

      <!-- Text Content (Safe text rendering using pre-wrap instead of v-html) -->
      <div v-if="lesson.content" class="content-container">
        <p class="safe-content">{{ lesson.content }}</p>
      </div>

      <!-- Progress Tracking Panel -->
      <aside class="progress-panel">
        <h3 class="panel-title">Tiến độ của bạn</h3>
        
        <div class="progress-display">
          <div class="progress-bar-track">
            <div class="progress-bar-fill" :style="{ width: progressForm.watchedPercent + '%' }"></div>
          </div>
          <span class="progress-text">{{ Math.round(progressForm.watchedPercent) }}%</span>
        </div>

        <div class="progress-controls">
          <div class="range-wrapper">
            <label>Cập nhật phần trăm đã học:</label>
            <input 
              type="range" 
              min="0" 
              max="100" 
              v-model.number="progressForm.watchedPercent" 
              class="range-slider"
              :disabled="isSaving"
            />
          </div>

          <div class="action-buttons">
            <button 
              @click="saveProgress" 
              class="btn-save" 
              :disabled="isSaving"
            >
              {{ isSaving ? 'Đang lưu...' : 'Lưu tiến độ' }}
            </button>
            <button 
              @click="markCompleted" 
              class="btn-complete" 
              :class="{ 'is-completed': lesson.isCompleted }"
              :disabled="isSaving || lesson.isCompleted"
            >
              {{ lesson.isCompleted ? '✓ Đã hoàn thành' : 'Đánh dấu hoàn thành' }}
            </button>
          </div>
        </div>
        
        <div v-if="saveMessage" class="save-feedback" :class="saveStatus">
          {{ saveMessage }}
        </div>
      </aside>

      <!-- Resources Panel -->
      <aside class="resources-panel">
        <h3 class="panel-title">📎 Tài liệu đính kèm</h3>
        <div v-if="isLoadingResources" class="resources-loading">
          Đang tải tài liệu...
        </div>
        <div v-else-if="resourceError" class="resources-error">
          {{ resourceError }}
        </div>
        <div v-else-if="resources.length === 0" class="resources-empty">
          Bài học này chưa có tài liệu đính kèm.
        </div>
        <ul v-else class="resources-list">
          <li v-for="res in resources" :key="res.id" class="resource-item">
            <div class="resource-info">
              <span class="resource-type-badge">{{ res.resourceType }}</span>
              <a :href="res.fileUrl" target="_blank" rel="noopener noreferrer" class="resource-link">
                {{ res.title }}
              </a>
            </div>
            <span v-if="res.fileSize" class="resource-size">{{ formatFileSize(res.fileSize) }}</span>
          </li>
        </ul>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import { LearningService } from '@/services/learning.service'
import { getApiErrorMessage } from '@/utils/api-error'

const route = useRoute()

// States
const isLoading = ref(true)
const isSaving = ref(false)
const errorMsg = ref('')
const saveMessage = ref('')
const saveStatus = ref('') // 'success' or 'error'

const lesson = ref(null)
const resources = ref([])
const isLoadingResources = ref(false)
const resourceError = ref('')

const progressForm = reactive({
  watchedPercent: 0,
  isCompleted: false
})

const fetchLesson = async () => {
  const lessonId = parseInt(route.params.id)
  
  if (isNaN(lessonId)) {
    errorMsg.value = 'Đường dẫn bài học không hợp lệ.'
    isLoading.value = false
    return
  }

  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await LearningService.getLessonDetail(lessonId)
    if (res.data && res.data.code === 1000) {
      lesson.value = res.data.result
      progressForm.watchedPercent = lesson.value.watchedPercent || 0
      progressForm.isCompleted = lesson.value.isCompleted || false
    } else {
      throw new Error(res.data?.message || 'Lỗi lấy dữ liệu')
    }
  } catch (error) {
    console.error('Learning error:', error)
    // Đặc tả xử lý riêng cho 403 (FORBIDDEN_ACCESS)
    if (error.response && error.response.status === 403) {
      errorMsg.value = 'Bạn chưa ghi danh khóa học này nên không thể xem bài học (không phải bài học thử).'
    } else {
      errorMsg.value = getApiErrorMessage(error)
    }
  } finally {
    isLoading.value = false
  }
}

const fetchResources = async (lessonId) => {
  isLoadingResources.value = true
  resourceError.value = ''
  try {
    const res = await LearningService.getLessonResources(lessonId)
    if (res.data && res.data.code === 1000) {
      resources.value = res.data.result || []
    }
  } catch (error) {
    // Silently handle — don't break lesson content
    resourceError.value = 'Không thể tải tài liệu đính kèm.'
    console.error('Resource fetch error:', error)
  } finally {
    isLoadingResources.value = false
  }
}

const submitProgress = async (payload) => {
  if (!lesson.value) return
  
  isSaving.value = true
  saveMessage.value = ''
  
  try {
    const res = await LearningService.updateProgress(lesson.value.id, payload)
    if (res.data && res.data.code === 1000) {
      saveStatus.value = 'success'
      saveMessage.value = 'Đã lưu tiến độ thành công!'
      
      // Update local state to reflect successful save
      const currentPercent = lesson.value.watchedPercent || 0
      lesson.value.watchedPercent = Math.max(currentPercent, payload.watchedPercent)
      lesson.value.isCompleted = payload.isCompleted || lesson.value.isCompleted
      
      progressForm.watchedPercent = lesson.value.watchedPercent
      progressForm.isCompleted = lesson.value.isCompleted
      
      // Ẩn thông báo sau 3s
      setTimeout(() => { saveMessage.value = '' }, 3000)
    }
  } catch (error) {
    saveStatus.value = 'error'
    saveMessage.value = getApiErrorMessage(error)
  } finally {
    isSaving.value = false
  }
}

const saveProgress = () => {
  // Validate input client-side
  let percent = progressForm.watchedPercent
  if (percent < 0) percent = 0
  if (percent > 100) percent = 100
  progressForm.watchedPercent = percent
  
  submitProgress({
    watchedPercent: percent,
    isCompleted: progressForm.isCompleted
  })
}

const markCompleted = async () => {
  if (!lesson.value) return

  isSaving.value = true
  saveMessage.value = ''

  try {
    const res = await LearningService.completeLesson(lesson.value.id)
    if (res.data && res.data.code === 1000) {
      saveStatus.value = 'success'
      saveMessage.value = 'Đã hoàn thành bài học!'

      // Update local state
      lesson.value.watchedPercent = 100
      lesson.value.isCompleted = true
      progressForm.watchedPercent = 100
      progressForm.isCompleted = true

      setTimeout(() => { saveMessage.value = '' }, 3000)
    }
  } catch (error) {
    saveStatus.value = 'error'
    saveMessage.value = getApiErrorMessage(error)
  } finally {
    isSaving.value = false
  }
}

onMounted(() => {
  fetchLesson()
})

watch(() => route.params.id, () => {
  fetchLesson()
})

// Watch lesson loaded to fetch resources
watch(lesson, (newLesson) => {
  if (newLesson && newLesson.id) {
    fetchResources(newLesson.id)
  }
})

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}
</script>

<style scoped>
.learning-page-container {
  min-height: 100vh;
  background-color: var(--bg-color, #f8fafc);
  display: flex;
  flex-direction: column;
}

/* Header */
.learning-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  position: sticky;
  top: 0;
  z-index: 10;
}
.btn-back {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #475569;
  text-decoration: none;
  font-weight: 500;
  font-size: 0.95rem;
  transition: color 0.2s;
}
.btn-back:hover {
  color: var(--primary-color, #3B82F6);
}
.badge {
  background: #f1f5f9;
  color: #475569;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  font-weight: 600;
}

/* States */
.state-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 1rem;
  text-align: center;
}
.spinner {
  width: 45px;
  height: 45px;
  border: 4px solid #e2e8f0;
  border-top-color: var(--primary-color, #3B82F6);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  max-width: 500px;
  margin: 0 auto;
}
.error-state h2 {
  color: #1e293b;
  margin-bottom: 0.5rem;
}
.error-state p {
  color: #64748b;
  margin-bottom: 2rem;
  line-height: 1.5;
}

/* Main Content */
.learning-main {
  flex: 1;
  max-width: 900px;
  margin: 0 auto;
  width: 100%;
  padding: 2rem 1rem 4rem;
}
.lesson-title {
  font-size: 2rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 1.5rem;
  line-height: 1.3;
}

/* Video Section */
.video-container {
  background: black;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lesson-video {
  width: 100%;
  height: 100%;
  display: block;
}

/* Text Content */
.content-container {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  margin-bottom: 2.5rem;
}
.safe-content {
  white-space: pre-wrap; /* Quan trọng: Giữ xuống dòng nhưng không render HTML */
  color: #334155;
  line-height: 1.7;
  font-size: 1.05rem;
}

/* Progress Panel */
.progress-panel {
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
}
.panel-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #1e293b;
}

.progress-display {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}
.progress-bar-track {
  flex: 1;
  height: 10px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}
.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #60a5fa);
  transition: width 0.4s ease;
}
.progress-text {
  font-weight: 700;
  color: var(--primary-color, #3B82F6);
  min-width: 45px;
}

.progress-controls {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.range-wrapper label {
  display: block;
  font-size: 0.9rem;
  color: #475569;
  margin-bottom: 0.5rem;
}
.range-slider {
  width: 100%;
  cursor: pointer;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}
.btn-save, .btn-complete, .btn-primary {
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}
.btn-primary {
  background: var(--primary-color, #3B82F6);
  color: white;
}
.btn-primary:hover {
  background: #2563eb;
}

.btn-save {
  flex: 1;
  background: #f1f5f9;
  color: #475569;
}
.btn-save:hover:not(:disabled) {
  background: #e2e8f0;
}
.btn-save:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-complete {
  flex: 1;
  background: #10B981;
  color: white;
}
.btn-complete:hover:not(:disabled) {
  background: #059669;
}
.btn-complete.is-completed {
  background: #059669;
  cursor: default;
}
.btn-complete:disabled:not(.is-completed) {
  opacity: 0.7;
  cursor: not-allowed;
}

.save-feedback {
  margin-top: 1rem;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.9rem;
  text-align: center;
  font-weight: 500;
}
.save-feedback.success {
  background: #d1fae5;
  color: #065f46;
}
.save-feedback.error {
  background: #fee2e2;
  color: #991b1b;
}

/* Resources Panel */
.resources-panel {
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #e2e8f0;
  margin-top: 1.5rem;
}
.resources-loading {
  color: #64748b;
  font-size: 0.9rem;
  font-style: italic;
}
.resources-error {
  color: #b91c1c;
  font-size: 0.9rem;
  background: #fef2f2;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
}
.resources-empty {
  color: #94a3b8;
  font-size: 0.9rem;
  font-style: italic;
}
.resources-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.resource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border: 1px solid #f1f5f9;
  border-radius: 6px;
  background: #fdfdfd;
  transition: border-color 0.2s;
}
.resource-item:hover {
  border-color: #e2e8f0;
}
.resource-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}
.resource-type-badge {
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 700;
  background: #e0f2fe;
  color: #0369a1;
  white-space: nowrap;
  flex-shrink: 0;
}
.resource-link {
  font-size: 0.95rem;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-link:hover {
  text-decoration: underline;
  color: #2563eb;
}
.resource-size {
  font-size: 0.8rem;
  color: #94a3b8;
  white-space: nowrap;
  flex-shrink: 0;
}
</style>

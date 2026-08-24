<template>
  <div class="curriculum-sidebar">
    <div class="sidebar-header">
      <h2 class="course-title">{{ curriculum?.courseTitle || 'Đang tải...' }}</h2>
    </div>

    <div v-if="isLoading" class="sidebar-loading">
      <div class="spinner-sm"></div>
      <span>Đang tải chương trình...</span>
    </div>

    <div v-else-if="errorMsg" class="sidebar-error">
      {{ errorMsg }}
    </div>

    <div v-else-if="curriculum" class="sections-list">
      <div v-for="section in curriculum.sections" :key="section.id" class="section-item">
        <h3 class="section-title">{{ section.title }}</h3>
        
        <ul class="lessons-list">
          <li v-for="lesson in section.lessons" :key="lesson.id" 
              class="lesson-item"
              :class="{ 'is-active': lesson.id === currentLessonId, 'is-completed': lesson.isCompleted }"
              @click="goToLesson(lesson.id)">
            
            <div class="lesson-status-icon">
              <span v-if="lesson.isCompleted" class="icon-check">✓</span>
              <span v-else-if="lesson.id === currentLessonId" class="icon-play">▶</span>
              <span v-else class="icon-doc">📄</span>
            </div>

            <div class="lesson-info">
              <span class="lesson-title">{{ lesson.title }}</span>
              <div class="lesson-meta">
                <span class="duration">{{ lesson.durationMinutes }} phút</span>
                <span v-if="lesson.isPreview" class="badge-preview">Preview</span>
              </div>
            </div>
            
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  curriculum: {
    type: Object,
    default: null
  },
  currentLessonId: {
    type: [Number, String],
    default: null
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  errorMsg: {
    type: String,
    default: ''
  }
})

const router = useRouter()

const goToLesson = (lessonId) => {
  if (lessonId !== props.currentLessonId) {
    router.push(`/student/lessons/${lessonId}`)
  }
}
</script>

<style scoped>
.curriculum-sidebar {
  background: white;
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.sidebar-header {
  padding: 1.25rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.course-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  line-height: 1.4;
}

.sidebar-loading, .sidebar-error {
  padding: 2rem 1rem;
  text-align: center;
  color: #64748b;
  font-size: 0.95rem;
}

.sidebar-error {
  color: #ef4444;
  background: #fef2f2;
}

.spinner-sm {
  width: 24px;
  height: 24px;
  border: 3px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 0.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.sections-list {
  flex: 1;
  overflow-y: auto;
}

.section-item {
  border-bottom: 1px solid #e2e8f0;
}

.section-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #334155;
  padding: 1rem 1rem 0.5rem;
  margin: 0;
  background: #fdfdfd;
}

.lessons-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.lesson-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background 0.2s;
  border-left: 3px solid transparent;
}

.lesson-item:hover {
  background: #f1f5f9;
}

.lesson-item.is-active {
  background: #e0f2fe;
  border-left-color: #3b82f6;
}

.lesson-item.is-completed .lesson-title {
  color: #64748b;
}

.lesson-status-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.icon-check {
  color: #10b981;
  font-weight: bold;
}

.icon-play {
  color: #3b82f6;
  font-size: 0.85rem;
}

.icon-doc {
  color: #94a3b8;
  font-size: 0.85rem;
}

.lesson-info {
  flex: 1;
  min-width: 0;
}

.lesson-title {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 0.25rem;
  line-height: 1.3;
}

.lesson-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: #64748b;
}

.badge-preview {
  background: #fef3c7;
  color: #d97706;
  padding: 0.1rem 0.3rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.7rem;
}
</style>

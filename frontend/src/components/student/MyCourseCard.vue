<template>
  <div class="course-card">
    <div class="card-thumbnail">
      <img v-if="course.thumbnailUrl" :src="course.thumbnailUrl" :alt="course.courseName" />
      <div v-else class="thumbnail-placeholder">
        <span>JP</span>
      </div>
    </div>

    <div class="card-content">
      <h3 class="course-title">{{ course.courseName }}</h3>

      <div class="progress-section">
        <div class="progress-bar-track">
          <div
            class="progress-bar-fill"
            :style="{ width: progressPercent + '%' }"
            :class="progressClass"
          ></div>
        </div>
        <span class="progress-text">{{ Math.round(progressPercent) }}% hoàn thành</span>
      </div>

      <div class="card-meta">
        <span class="meta-item">{{ course.completedLessons || 0 }}/{{ course.totalLessons || 0 }} bài học</span>
        <span v-if="course.lastLessonName" class="meta-item last-lesson" :title="course.lastLessonName">
          Học tiếp: {{ course.lastLessonName }}
        </span>
      </div>

      <button class="btn-continue" @click="$emit('continue', course)">
        {{ course.lastLessonName ? 'Học tiếp' : 'Bắt đầu học' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  course: { type: Object, required: true }
})

defineEmits(['continue'])

const progressPercent = computed(() => {
  if (typeof props.course.progressPercent === 'number') {
    return Math.min(Math.max(props.course.progressPercent, 0), 100)
  }
  const total = props.course.totalLessons || 0
  const completed = props.course.completedLessons || 0
  if (total === 0) return 0
  return Math.min((completed / total) * 100, 100)
})

const progressClass = computed(() => {
  if (progressPercent.value >= 100) return 'complete'
  if (progressPercent.value >= 50) return 'halfway'
  return ''
})
</script>

<style scoped>
.course-card {
  background: var(--card-bg, #fff);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
}
.course-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-thumbnail {
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.card-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumbnail-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  font-weight: 800;
  color: white;
}

.card-content {
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
}

.course-title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-color, #1F2937);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.progress-section {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}
.progress-bar-track {
  width: 100%;
  height: 8px;
  background-color: #e5e7eb;
  border-radius: 9999px;
  overflow: hidden;
}
.progress-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #60a5fa);
  border-radius: 9999px;
  transition: width 0.6s ease;
}
.progress-bar-fill.halfway {
  background: linear-gradient(90deg, #f59e0b, #fbbf24);
}
.progress-bar-fill.complete {
  background: linear-gradient(90deg, #10B981, #34d399);
}
.progress-text {
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
}

.card-meta {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.meta-item {
  font-size: 0.8rem;
  color: #6b7280;
}
.last-lesson {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.btn-continue {
  margin-top: auto;
  padding: 0.625rem 1rem;
  background: var(--primary-color, #3B82F6);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color 0.2s ease, transform 0.1s ease;
}
.btn-continue:hover {
  background-color: #2563eb;
  transform: scale(1.02);
}
.btn-continue:active {
  transform: scale(0.98);
}
</style>

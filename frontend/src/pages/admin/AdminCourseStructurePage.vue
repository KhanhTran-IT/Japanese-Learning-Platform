<template>
  <div class="admin-course-structure">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <button class="btn-back" @click="router.push('/admin/courses')">
          &larr; Quay lại
        </button>
        <h1 class="page-title">
          Cấu trúc khóa học: <span class="text-primary">{{ courseTitle || 'Đang tải...' }}</span>
        </h1>
        <p class="page-subtitle">Quản lý các chương và bài học bên trong khóa học.</p>
      </div>
      <div class="header-actions">
        <button class="btn-primary" @click="handleCreateSection" :disabled="!courseTitle">
          Thêm Chương Học
        </button>
      </div>
    </div>

    <!-- Inline Error -->
    <div v-if="actionError" class="inline-error">
      {{ actionError }}
      <button @click="actionError = ''" class="btn-close-error">✕</button>
    </div>

    <!-- Main Content Area -->
    <div class="content-area">
      <!-- Loading Course / Sections -->
      <div v-if="isLoadingSections" class="loading-state">
        <div class="spinner"></div>
        <p>Đang tải cấu trúc khóa học...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="error-state">
        <div class="error-icon">⚠️</div>
        <p>{{ errorMsg }}</p>
        <button @click="fetchData" class="btn-retry">Thử lại</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="sections.length === 0" class="empty-state">
        Khóa học này chưa có chương nào. Hãy tạo chương đầu tiên!
      </div>

      <!-- Sections List -->
      <div v-else class="sections-list">
        <div v-for="(section, index) in sections" :key="section.id" class="section-card">
          <!-- Section Header -->
          <div class="section-header" @click="toggleSection(section)">
            <div class="section-title-wrap">
              <span class="chevron" :class="{ 'is-open': section.isExpanded }">▶</span>
              <h3 class="section-title">{{ section.title }}</h3>
              <span class="badge badge-outline">Thứ tự: {{ section.sortOrder }}</span>
              <span :class="['badge', getStatusBadgeClass(section.status)]">
                {{ formatStatus(section.status) }}
              </span>
            </div>
            <div class="section-actions" @click.stop>
              <button class="btn-text btn-create-lesson" @click="handleCreateLesson(section)">
                + Bài học
              </button>
              <button class="btn-text btn-edit" @click="handleEditSection(section)">
                Sửa
              </button>
              <button class="btn-text btn-delete" @click="handleDeleteSection(section)">
                Xóa
              </button>
            </div>
          </div>

          <!-- Section Body (Lessons) -->
          <div v-if="section.isExpanded" class="section-body">
            <!-- Loading Lessons -->
            <div v-if="section.isLoadingLessons" class="lesson-loading">
              Đang tải bài học...
            </div>
            
            <!-- Lessons List -->
            <div v-else>
              <div v-if="!section.lessons || section.lessons.length === 0" class="lesson-empty">
                Chưa có bài học nào trong chương này.
              </div>
              <ul v-else class="lessons-list">
                <li v-for="lesson in section.lessons" :key="lesson.id" class="lesson-item-wrapper">
                  <div class="lesson-item">
                    <div class="lesson-info">
                      <span class="lesson-icon">📄</span>
                      <span class="lesson-title">{{ lesson.title }}</span>
                      <span v-if="lesson.isPreview" class="badge badge-info">Preview</span>
                      <span class="lesson-meta text-gray">
                        (Thứ tự: {{ lesson.sortOrder }} - {{ lesson.durationMinutes }} phút)
                      </span>
                      <span :class="['badge', getStatusBadgeClass(lesson.status)]">
                        {{ formatStatus(lesson.status) }}
                      </span>
                    </div>
                    <div class="lesson-actions">
                      <button class="btn-text btn-resource-sm" @click="toggleResources(lesson)">📎 Tài liệu</button>
                      <button class="btn-text btn-edit-sm" @click="handleEditLesson(section, lesson)">Sửa</button>
                      <button class="btn-text btn-delete-sm" @click="handleDeleteLesson(section, lesson)">Xóa</button>
                    </div>
                  </div>

                  <!-- Inline Resources Panel -->
                  <div v-if="lesson.showResources" class="resources-panel">
                    <div class="resources-header">
                      <span class="resources-label">Tài liệu đính kèm</span>
                      <button class="btn-text btn-add-resource" @click="handleCreateResource(lesson)">+ Thêm</button>
                    </div>
                    <div v-if="lesson.isLoadingResources" class="resources-loading">Đang tải...</div>
                    <div v-else-if="!lesson.resources || lesson.resources.length === 0" class="resources-empty">Chưa có tài liệu.</div>
                    <ul v-else class="resources-list">
                      <li v-for="res in lesson.resources" :key="res.id" class="resource-item">
                        <div class="resource-info">
                          <span class="resource-type-badge">{{ res.resourceType }}</span>
                          <a :href="res.fileUrl" target="_blank" rel="noopener noreferrer" class="resource-link">{{ res.title }}</a>
                          <span v-if="res.fileSize" class="resource-size text-gray">{{ formatFileSize(res.fileSize) }}</span>
                        </div>
                        <div class="resource-actions">
                          <button class="btn-text btn-edit-sm" @click="handleEditResource(lesson, res)">Sửa</button>
                          <button class="btn-text btn-delete-sm" @click="handleDeleteResource(lesson, res)">Xóa</button>
                        </div>
                      </li>
                    </ul>
                  </div>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <SectionFormModal
      v-if="showSectionModal"
      :courseId="id"
      :editingSection="editingSection"
      @close="closeSectionModal"
      @saved="handleSectionSaved"
    />

    <LessonFormModal
      v-if="showLessonModal"
      :sectionId="activeSectionIdForLesson"
      :editingLesson="editingLesson"
      @close="closeLessonModal"
      @saved="handleLessonSaved"
    />

    <ResourceFormModal
      v-if="showResourceModal"
      :lessonId="activeLessonIdForResource"
      :editingResource="editingResource"
      @close="closeResourceModal"
      @saved="handleResourceSaved"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'
import SectionFormModal from '@/components/admin/SectionFormModal.vue'
import LessonFormModal from '@/components/admin/LessonFormModal.vue'
import ResourceFormModal from '@/components/admin/ResourceFormModal.vue'

const props = defineProps({
  id: {
    type: [String, Number],
    required: true
  }
})

const router = useRouter()

// State
const courseTitle = ref('')
const sections = ref([])
const isLoadingSections = ref(true)
const errorMsg = ref('')
const actionError = ref('')

// Modals State
const showSectionModal = ref(false)
const editingSection = ref(null)

const showLessonModal = ref(false)
const editingLesson = ref(null)
const activeSectionIdForLesson = ref(null)

const showResourceModal = ref(false)
const editingResource = ref(null)
const activeLessonIdForResource = ref(null)
const activeLessonRefForResource = ref(null)

// Init
onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  isLoadingSections.value = true
  errorMsg.value = ''
  
  try {
    // 1. Get Course Detail to show title
    const courseRes = await AdminService.getCourseDetail(props.id)
    if (courseRes.data.code === 1000) {
      courseTitle.value = courseRes.data.result.title
    }

    // 2. Get Sections
    const sectionRes = await AdminService.getSectionsByCourse(props.id)
    if (sectionRes.data.code === 1000) {
      // Add custom properties for lazy loading lessons
      sections.value = sectionRes.data.result.map(sec => ({
        ...sec,
        isExpanded: false,
        isLoadingLessons: false,
        lessons: []
      }))
    }
  } catch (error) {
    if (error.response && error.response.status === 404) {
      errorMsg.value = 'Không tìm thấy khóa học này.'
    } else {
      errorMsg.value = getApiErrorMessage(error, 'Không thể tải dữ liệu cấu trúc.')
    }
  } finally {
    isLoadingSections.value = false
  }
}

// Lazy Load Lessons
const toggleSection = async (section) => {
  section.isExpanded = !section.isExpanded
  
  // If expanding and lessons not loaded yet
  if (section.isExpanded && (!section.lessons || section.lessons.length === 0)) {
    await fetchLessonsForSection(section)
  }
}

const fetchLessonsForSection = async (section) => {
  section.isLoadingLessons = true
  try {
    const res = await AdminService.getLessonsBySection(section.id)
    if (res.data.code === 1000) {
      section.lessons = res.data.result || []
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, `Lỗi tải bài học của chương: ${section.title}`)
  } finally {
    section.isLoadingLessons = false
  }
}

// --- Section Actions ---
const handleCreateSection = () => {
  editingSection.value = null
  showSectionModal.value = true
}

const handleEditSection = (section) => {
  editingSection.value = { ...section }
  showSectionModal.value = true
}

const handleDeleteSection = async (section) => {
  if (!window.confirm(`Bạn có chắc chắn muốn xóa chương "${section.title}"?\nNếu chương đang có bài học sẽ không thể xóa.`)) {
    return
  }
  
  actionError.value = ''
  try {
    const res = await AdminService.deleteSection(section.id)
    if (res.data.code === 1000) {
      sections.value = sections.value.filter(s => s.id !== section.id)
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa chương học.')
  }
}

const closeSectionModal = () => {
  showSectionModal.value = false
  editingSection.value = null
}

const handleSectionSaved = () => {
  closeSectionModal()
  // Tải lại toàn bộ section (có thể tối ưu không reload, nhưng fetch lại cho an toàn)
  fetchData()
}

// --- Lesson Actions ---
const handleCreateLesson = (section) => {
  activeSectionIdForLesson.value = section.id
  editingLesson.value = null
  showLessonModal.value = true
}

const handleEditLesson = (section, lesson) => {
  activeSectionIdForLesson.value = section.id
  editingLesson.value = { ...lesson }
  showLessonModal.value = true
}

const handleDeleteLesson = async (section, lesson) => {
  if (!window.confirm(`Bạn có chắc chắn muốn xóa bài học "${lesson.title}"?`)) {
    return
  }
  
  actionError.value = ''
  try {
    const res = await AdminService.deleteLesson(lesson.id)
    if (res.data.code === 1000) {
      section.lessons = section.lessons.filter(l => l.id !== lesson.id)
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa bài học.')
  }
}

const closeLessonModal = () => {
  showLessonModal.value = false
  editingLesson.value = null
  activeSectionIdForLesson.value = null
}

const handleLessonSaved = async () => {
  const targetSectionId = activeSectionIdForLesson.value
  closeLessonModal()
  
  // Reload only the lessons of that section
  const section = sections.value.find(s => s.id === targetSectionId)
  if (section) {
    section.isExpanded = true
    await fetchLessonsForSection(section)
  }
}

// --- Resource Actions ---
const toggleResources = async (lesson) => {
  lesson.showResources = !lesson.showResources
  if (lesson.showResources && (!lesson.resources || lesson.resources.length === 0)) {
    await fetchResourcesForLesson(lesson)
  }
}

const fetchResourcesForLesson = async (lesson) => {
  lesson.isLoadingResources = true
  try {
    const res = await AdminService.getLessonResources(lesson.id)
    if (res.data.code === 1000) {
      lesson.resources = res.data.result || []
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, `Lỗi tải tài liệu: ${lesson.title}`)
  } finally {
    lesson.isLoadingResources = false
  }
}

const handleCreateResource = (lesson) => {
  activeLessonIdForResource.value = lesson.id
  activeLessonRefForResource.value = lesson
  editingResource.value = null
  showResourceModal.value = true
}

const handleEditResource = (lesson, resource) => {
  activeLessonIdForResource.value = lesson.id
  activeLessonRefForResource.value = lesson
  editingResource.value = { ...resource }
  showResourceModal.value = true
}

const handleDeleteResource = async (lesson, resource) => {
  if (!window.confirm(`Bạn có chắc chắn muốn xóa tài liệu "${resource.title}"?`)) {
    return
  }
  actionError.value = ''
  try {
    const res = await AdminService.deleteLessonResource(resource.id)
    if (res.data.code === 1000) {
      lesson.resources = lesson.resources.filter(r => r.id !== resource.id)
    }
  } catch (error) {
    actionError.value = getApiErrorMessage(error, 'Không thể xóa tài liệu.')
  }
}

const closeResourceModal = () => {
  showResourceModal.value = false
  editingResource.value = null
  activeLessonIdForResource.value = null
}

const handleResourceSaved = async () => {
  const targetLesson = activeLessonRefForResource.value
  closeResourceModal()
  if (targetLesson) {
    targetLesson.showResources = true
    await fetchResourcesForLesson(targetLesson)
  }
}

// --- Helpers ---
const formatStatus = (status) => {
  const statusMap = {
    'DRAFT': 'Bản nháp',
    'PUBLISHED': 'Đã xuất bản',
    'HIDDEN': 'Đang ẩn',
    'ARCHIVED': 'Đã lưu trữ'
  }
  return statusMap[status] || status
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'PUBLISHED': return 'badge-success'
    case 'HIDDEN': return 'badge-warning'
    case 'ARCHIVED': return 'badge-danger'
    default: return 'badge-draft' // DRAFT
  }
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}
</script>

<style scoped>
.admin-course-structure {
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: 3rem;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
}
.header-left {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.btn-back {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  font-weight: 500;
  margin-bottom: 0.5rem;
  padding: 0;
}
.btn-back:hover {
  color: #3b82f6;
  text-decoration: underline;
}
.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 0.25rem;
}
.text-primary {
  color: #3b82f6;
}
.page-subtitle {
  color: #64748b;
  font-size: 0.95rem;
}
.btn-primary {
  padding: 0.75rem 1.25rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary:hover:not(:disabled) {
  background-color: #2563eb;
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Inline Error */
.inline-error {
  background-color: #fef2f2;
  color: #b91c1c;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-left: 4px solid #ef4444;
}
.btn-close-error {
  background: none;
  border: none;
  color: #b91c1c;
  cursor: pointer;
  font-size: 1.2rem;
}

/* Loading & Error */
.loading-state, .error-state, .empty-state {
  background: white;
  padding: 4rem;
  text-align: center;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  color: #64748b;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f1f5f9;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 1rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.error-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}
.btn-retry {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

/* Sections List */
.sections-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.section-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.section-header {
  padding: 1rem 1.25rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8fafc;
  cursor: pointer;
  user-select: none;
}
.section-header:hover {
  background: #f1f5f9;
}
.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.chevron {
  font-size: 0.8rem;
  color: #94a3b8;
  transition: transform 0.2s;
}
.chevron.is-open {
  transform: rotate(90deg);
}
.section-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
.section-actions {
  display: flex;
  gap: 0.5rem;
}

/* Badges */
.badge {
  padding: 0.2rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.7rem;
  font-weight: 600;
  white-space: nowrap;
}
.badge-outline {
  border: 1px solid #cbd5e1;
  color: #475569;
  background: white;
}
.badge-info {
  background-color: #e0f2fe;
  color: #0369a1;
}
.badge-success {
  background-color: #dcfce7;
  color: #15803d;
}
.badge-draft {
  background-color: #f1f5f9;
  color: #475569;
}
.badge-warning {
  background-color: #fef3c7;
  color: #b45309;
}
.badge-danger {
  background-color: #fee2e2;
  color: #b91c1c;
}

/* Section Body & Lessons */
.section-body {
  padding: 1rem 1.25rem;
  border-top: 1px solid #e2e8f0;
  background: white;
}
.lesson-loading, .lesson-empty {
  color: #64748b;
  font-size: 0.9rem;
  text-align: center;
  padding: 1rem;
  font-style: italic;
}
.lessons-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border: 1px solid #f1f5f9;
  border-radius: 6px;
  background: #fdfdfd;
}
.lesson-item:hover {
  background: #f8fafc;
  border-color: #e2e8f0;
}
.lesson-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.lesson-icon {
  font-size: 1.1rem;
}
.lesson-title {
  font-weight: 500;
  color: #334155;
  font-size: 0.95rem;
}
.lesson-meta {
  font-size: 0.85rem;
}
.text-gray {
  color: #64748b;
}
.lesson-actions {
  display: flex;
  gap: 0.5rem;
}

/* Buttons */
.btn-text {
  padding: 0.25rem 0.6rem;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
  background: white;
  transition: all 0.2s;
  font-size: 0.85rem;
  font-weight: 500;
}
.btn-create-lesson {
  border-color: #bae6fd;
  color: #0369a1;
}
.btn-create-lesson:hover {
  background: #f0f9ff;
}
.btn-edit {
  border-color: #cbd5e1;
  color: #334155;
}
.btn-edit:hover {
  background: #f1f5f9;
}
.btn-delete {
  border-color: #fca5a5;
  color: #b91c1c;
}
.btn-delete:hover {
  background: #fef2f2;
}

.btn-edit-sm, .btn-delete-sm {
  padding: 0.2rem 0.5rem;
  font-size: 0.8rem;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
}
.btn-edit-sm {
  color: #3b82f6;
}
.btn-edit-sm:hover {
  background: #eff6ff;
}
.btn-delete-sm {
  color: #ef4444;
}
.btn-delete-sm:hover {
  background: #fef2f2;
}

/* Resources Panel */
.lesson-item-wrapper {
  display: flex;
  flex-direction: column;
}
.btn-resource-sm {
  color: #0369a1;
  padding: 0.2rem 0.5rem;
  font-size: 0.8rem;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
}
.btn-resource-sm:hover {
  background: #e0f2fe;
}
.resources-panel {
  margin-top: 0.25rem;
  padding: 0.75rem 1rem 0.75rem 2.5rem;
  background: #fafbfc;
  border-top: 1px dashed #e2e8f0;
}
.resources-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.resources-label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  letter-spacing: 0.025em;
}
.btn-add-resource {
  color: #0369a1;
  font-size: 0.8rem;
  font-weight: 600;
  padding: 0.2rem 0.5rem;
  border: 1px solid #bae6fd;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}
.btn-add-resource:hover {
  background: #f0f9ff;
}
.resources-loading, .resources-empty {
  font-size: 0.8rem;
  color: #94a3b8;
  font-style: italic;
  padding: 0.25rem 0;
}
.resources-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}
.resource-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem 0.5rem;
  border-radius: 4px;
  background: white;
  border: 1px solid #f1f5f9;
}
.resource-item:hover {
  border-color: #e2e8f0;
}
.resource-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
}
.resource-type-badge {
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.65rem;
  font-weight: 700;
  background: #e0f2fe;
  color: #0369a1;
  white-space: nowrap;
}
.resource-link {
  font-size: 0.85rem;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.resource-link:hover {
  text-decoration: underline;
}
.resource-size {
  font-size: 0.75rem;
  white-space: nowrap;
}
.resource-actions {
  display: flex;
  gap: 0.25rem;
  flex-shrink: 0;
}
</style>

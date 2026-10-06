<template>
  <div class="admin-course-structure max-w-[1280px] mx-auto pb-12">
    <!-- Header -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <button @click="router.push('/admin/courses')" class="w-10 h-10 rounded-full bg-white/5 hover:bg-white/10 flex items-center justify-center transition-colors text-white/70 hover:text-white">
          <span class="material-symbols-outlined">arrow_back</span>
        </button>
        <div>
          <h1 class="text-xl font-stitch-serif font-bold text-white mb-1">
            Cấu trúc khóa học: <span class="text-stitch-primary">{{ courseTitle || 'Đang tải...' }}</span>
          </h1>
          <p class="text-sm text-white/40">Quản lý các chương và bài học bên trong khóa học.</p>
        </div>
      </div>
      
      <button @click="handleCreateSection" :disabled="!courseTitle" class="bg-stitch-primary text-white px-5 py-2.5 rounded-xl font-medium hover:bg-stitch-primary/90 transition-colors shadow-lg disabled:opacity-50">
        Thêm Chương Học
      </button>
    </div>

    <!-- Inline Error -->
    <div v-if="actionError" class="bg-red-500/10 border border-red-500/20 rounded-xl p-4 mb-6 flex items-center justify-between">
      <div class="flex items-center gap-3 text-red-400 font-medium">
        <span>⚠️</span>
        {{ actionError }}
      </div>
      <button @click="actionError = ''" class="text-red-400 hover:text-red-300 text-lg">✕</button>
    </div>

    <!-- Main Content Area -->
    <div class="bg-[#161b27] border border-white/5 rounded-2xl min-h-[400px]">
      <!-- Loading Course / Sections -->
      <div v-if="isLoadingSections" class="flex flex-col items-center justify-center py-20 text-white/50">
        <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
        <p>Đang tải cấu trúc khóa học...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-white/50">
        <div class="text-4xl mb-4 text-red-400">⚠️</div>
        <p class="text-red-400/80 mb-6">{{ errorMsg }}</p>
        <button @click="fetchData" class="bg-white/10 text-white px-6 py-2.5 rounded-lg hover:bg-white/20 transition-colors font-medium">Thử lại</button>
      </div>

      <!-- Empty State -->
      <div v-else-if="sections.length === 0" class="flex flex-col items-center justify-center py-20 text-white/50">
        <span class="text-4xl mb-4">📚</span>
        <p>Khóa học này chưa có chương nào. Hãy tạo chương đầu tiên!</p>
      </div>

      <!-- Sections List -->
      <div v-else class="p-6 flex flex-col gap-4">
        <div v-for="(section, index) in sections" :key="section.id" class="border border-white/10 rounded-xl overflow-hidden bg-white/[0.02]">
          <!-- Section Header -->
          <div 
            class="flex items-center justify-between p-4 bg-white/[0.03] hover:bg-white/[0.05] cursor-pointer transition-colors"
            @click="toggleSection(section)"
          >
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-white/30 transition-transform duration-200" :class="{ 'rotate-90': section.isExpanded }">chevron_right</span>
              <h3 class="text-base font-semibold text-white/90">{{ section.title }}</h3>
              <span class="text-[10px] px-2 py-0.5 rounded border border-white/20 text-white/50">Thứ tự: {{ section.sortOrder }}</span>
              <span :class="['text-[10px] px-2 py-0.5 rounded-full uppercase tracking-wide font-medium border', getStatusBadgeClass(section.status)]">
                {{ formatStatus(section.status) }}
              </span>
            </div>
            <div class="flex items-center gap-2" @click.stop>
              <button @click="handleCreateLesson(section)" class="px-2.5 py-1.5 text-xs font-medium rounded border border-stitch-primary/30 bg-stitch-primary/10 text-stitch-primary hover:bg-stitch-primary/20 transition-colors">+ Bài học</button>
              <button @click="handleEditSection(section)" class="px-2.5 py-1.5 text-xs font-medium rounded border border-white/10 bg-white/5 text-white/70 hover:bg-white/10 hover:text-white transition-colors">Sửa</button>
              <button @click="handleDeleteSection(section)" class="px-2.5 py-1.5 text-xs font-medium rounded border border-red-500/30 bg-red-500/10 text-red-400 hover:bg-red-500/20 transition-colors">Xóa</button>
            </div>
          </div>

          <!-- Section Body (Lessons) -->
          <div v-if="section.isExpanded" class="border-t border-white/5 bg-[#161b27] p-4">
            <!-- Loading Lessons -->
            <div v-if="section.isLoadingLessons" class="py-4 text-center text-sm text-white/40 italic">
              Đang tải bài học...
            </div>
            
            <!-- Lessons List -->
            <div v-else>
              <div v-if="!section.lessons || section.lessons.length === 0" class="py-4 text-center text-sm text-white/30 italic bg-white/[0.01] rounded-lg border border-white/5 border-dashed">
                Chưa có bài học nào trong chương này.
              </div>
              <ul v-else class="flex flex-col gap-2">
                <li v-for="lesson in section.lessons" :key="lesson.id" class="flex flex-col">
                  <div class="flex items-center justify-between p-3 rounded-lg border border-white/5 bg-white/[0.02] hover:bg-white/[0.04] transition-colors">
                    <div class="flex items-center gap-3">
                      <span class="text-xl">📄</span>
                      <span class="text-sm font-medium text-white/90">{{ lesson.title }}</span>
                      <span v-if="lesson.isPreview" class="text-[9px] px-1.5 py-0.5 rounded bg-blue-500/20 text-blue-400 uppercase tracking-wider font-bold">Preview</span>
                      <span class="text-xs text-white/40">(Thứ tự: {{ lesson.sortOrder }} - {{ lesson.durationMinutes }} phút)</span>
                      <span :class="['text-[9px] px-1.5 py-0.5 rounded-full uppercase tracking-wider font-medium border', getStatusBadgeClass(lesson.status)]">
                        {{ formatStatus(lesson.status) }}
                      </span>
                    </div>
                    <div class="flex items-center gap-1.5">
                      <button @click="toggleResources(lesson)" class="px-2 py-1 text-xs font-medium rounded border border-white/10 text-white/70 hover:bg-white/10 hover:text-white transition-colors flex items-center gap-1">
                        <span>📎</span> Tài liệu
                      </button>
                      <button @click="handleEditLesson(section, lesson)" class="px-2 py-1 text-xs font-medium rounded border border-white/10 text-white/70 hover:bg-white/10 hover:text-white transition-colors">Sửa</button>
                      <button @click="handleDeleteLesson(section, lesson)" class="px-2 py-1 text-xs font-medium rounded border border-red-500/20 text-red-400 hover:bg-red-500/10 transition-colors">Xóa</button>
                    </div>
                  </div>

                  <!-- Inline Resources Panel -->
                  <div v-if="lesson.showResources" class="ml-8 mt-1 p-3 rounded-lg bg-white/[0.01] border border-white/5 border-dashed relative before:absolute before:-left-[17px] before:top-4 before:w-4 before:h-[1px] before:bg-white/10">
                    <div class="flex items-center justify-between mb-2">
                      <span class="text-xs font-semibold text-white/40 uppercase tracking-wider">Tài liệu đính kèm</span>
                      <button @click="handleCreateResource(lesson)" class="px-2 py-1 text-[10px] font-medium rounded border border-stitch-primary/30 text-stitch-primary hover:bg-stitch-primary/10 transition-colors uppercase tracking-wide">+ Thêm</button>
                    </div>
                    
                    <div v-if="lesson.isLoadingResources" class="text-xs text-white/40 italic py-2">Đang tải...</div>
                    <div v-else-if="!lesson.resources || lesson.resources.length === 0" class="text-xs text-white/30 italic py-2">Chưa có tài liệu.</div>
                    
                    <ul v-else class="flex flex-col gap-1.5">
                      <li v-for="res in lesson.resources" :key="res.id" class="flex items-center justify-between p-2 rounded bg-white/5">
                        <div class="flex items-center gap-2">
                          <span class="text-[9px] px-1.5 py-0.5 rounded bg-white/10 text-white/70">{{ res.resourceType }}</span>
                          <a :href="res.fileUrl" target="_blank" rel="noopener noreferrer" class="text-xs text-stitch-primary hover:underline font-medium">{{ res.title }}</a>
                          <span v-if="res.fileSize" class="text-xs text-white/40">{{ formatFileSize(res.fileSize) }}</span>
                        </div>
                        <div class="flex items-center gap-1">
                          <button @click="handleEditResource(lesson, res)" class="p-1 text-white/50 hover:text-white transition-colors"><span class="material-symbols-outlined text-[14px]">edit</span></button>
                          <button @click="handleDeleteResource(lesson, res)" class="p-1 text-red-400/70 hover:text-red-400 transition-colors"><span class="material-symbols-outlined text-[14px]">delete</span></button>
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
      sections.value = sectionRes.data.result.map(sec => ({
        ...sec,
        isExpanded: false,
        isLoadingLessons: false,
        lessons: []
      }))
    }
  } catch (error) {
    if (error.response?.status === 404) {
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
  if (!window.confirm(`Bạn có chắc chắn muốn xóa chương "${section.title}"?\nNếu chương đang có bài học sẽ không thể xóa.`)) return
  
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
  if (!window.confirm(`Bạn có chắc chắn muốn xóa bài học "${lesson.title}"?`)) return
  
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
  if (!window.confirm(`Bạn có chắc chắn muốn xóa tài liệu "${resource.title}"?`)) return
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
    case 'PUBLISHED': return 'bg-green-500/10 border-green-500/20 text-green-400'
    case 'HIDDEN': return 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400'
    case 'ARCHIVED': return 'bg-red-500/10 border-red-500/20 text-red-400'
    default: return 'bg-white/5 border-white/10 text-white/50'
  }
}

const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(i > 0 ? 1 : 0) + ' ' + units[i]
}
</script>

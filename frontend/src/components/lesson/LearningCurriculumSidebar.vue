<template>
  <div class="h-full flex flex-col bg-[#161b27] font-stitch-sans text-white border-l border-white/5">
    <!-- Header -->
    <div class="p-4 border-b border-white/5 shrink-0 bg-[#161b27] sticky top-0 z-10">
      <h3 class="font-semibold text-sm truncate" :title="curriculum?.courseTitle">
        {{ curriculum?.courseTitle || 'Đang tải...' }}
      </h3>
      <!-- Progress Bar (Optional, if we have course progress, but we only have it in dashboard. Let's just omit it or show a placeholder) -->
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto py-2 custom-scrollbar">
      <!-- Loading State -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-10 text-white/40">
        <span class="material-symbols-outlined animate-spin text-3xl mb-2">autorenew</span>
        <span class="text-xs">Đang tải chương trình...</span>
      </div>
      
      <!-- Error State -->
      <div v-else-if="errorMsg" class="bg-red-500/10 text-red-400 p-4 rounded-xl text-sm border border-red-500/20 flex flex-col items-center text-center gap-2 m-4">
        <span class="material-symbols-outlined text-[24px]">error</span>
        {{ errorMsg }}
      </div>
      
      <!-- Curriculum List -->
      <div v-else-if="curriculum && curriculum.sections" class="space-y-4">
        <!-- Section Group -->
        <div v-for="(section, idx) in curriculum.sections" :key="section.id">
          
          <!-- Section Header -->
          <div class="px-4 py-2 text-xs text-white/30 uppercase tracking-wider">
            Chương {{ idx + 1 }}: {{ section.title }}
          </div>
          
          <!-- Lessons List -->
          <div v-if="section.lessons && section.lessons.length > 0">
            <a 
              v-for="lesson in section.lessons" 
              :key="lesson.id"
              href="javascript:void(0)"
              @click.prevent="goToLesson(lesson.id)"
              class="flex items-start gap-3 px-4 py-3 cursor-pointer transition-colors border-l-2"
              :class="[
                currentLessonId === lesson.id 
                  ? 'bg-stitch-primary/10 border-stitch-accent' 
                  : 'border-transparent hover:bg-white/5'
              ]"
            >
              <!-- Status Icon -->
              <div class="w-6 h-6 rounded-full flex items-center justify-center shrink-0 mt-0.5 text-xs"
                :class="lesson.isCompleted ? 'bg-green-500 text-white' : currentLessonId === lesson.id ? 'bg-stitch-primary text-white' : 'bg-white/10 text-white/40'">
                <span v-if="lesson.isCompleted">✓</span>
                <span v-else-if="currentLessonId === lesson.id">▶</span>
                <span v-else>{{ lesson.id }}</span>
              </div>
              
              <!-- Lesson Info -->
              <div class="flex-1 min-w-0">
                <div class="text-xs leading-snug transition-colors mb-0.5"
                      :class="[
                        currentLessonId === lesson.id ? 'text-white font-medium' : lesson.isCompleted ? 'text-white/40 line-through' : 'text-white/60'
                      ]">
                  {{ lesson.title }}
                </div>
                
                <div class="flex items-center gap-2">
                  <span class="text-xs text-white/25">{{ lesson.durationMinutes }} phút</span>
                  <!-- Preview Badge -->
                  <span v-if="lesson.isPreview" class="shrink-0 px-1.5 py-0.5 rounded bg-stitch-primary/20 text-stitch-primary text-[9px] uppercase tracking-wider">
                    Preview
                  </span>
                </div>
              </div>
            </a>
          </div>
          
          <!-- Empty Section -->
          <div v-else class="px-4 py-3 text-center text-white/30 text-xs italic">
            Chưa có bài học
          </div>
        </div>
      </div>
      
      <!-- Empty Curriculum -->
      <div v-else class="text-center py-10 text-white/30 text-sm italic border border-dashed border-white/10 rounded-xl m-4">
        Không có dữ liệu chương trình học.
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
/* Custom Scrollbar for dark theme sidebar */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>

<template>
  <div class="h-full flex flex-col bg-surface-container-lowest font-body-md text-on-surface">
    <!-- Header -->
    <div class="p-6 border-b border-paper-shadow shrink-0 bg-surface-container-lowest sticky top-0 z-10">
      <h3 class="font-headline-md text-lg text-ink-black flex items-center gap-2">
        <span class="material-symbols-outlined text-primary">menu_book</span>
        <span class="truncate" :title="curriculum?.courseTitle">{{ curriculum?.courseTitle || 'Đang tải...' }}</span>
      </h3>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto p-4 custom-scrollbar">
      <!-- Loading State -->
      <div v-if="isLoading" class="flex flex-col items-center justify-center py-10 text-secondary">
        <span class="material-symbols-outlined animate-spin text-3xl mb-2">autorenew</span>
        <span class="font-body-md text-sm">Đang tải chương trình...</span>
      </div>
      
      <!-- Error State -->
      <div v-else-if="errorMsg" class="bg-error-container/50 text-error p-4 rounded-xl text-sm border border-error/20 flex flex-col items-center text-center gap-2 m-2">
        <span class="material-symbols-outlined text-[24px]">error</span>
        {{ errorMsg }}
      </div>
      
      <!-- Curriculum List -->
      <div v-else-if="curriculum && curriculum.sections" class="space-y-4">
        <!-- Section Group -->
        <div v-for="(section, idx) in curriculum.sections" :key="section.id" class="rounded-xl overflow-hidden border border-paper-shadow bg-surface-container-lowest">
          
          <!-- Section Header -->
          <div class="bg-surface-container-low px-4 py-3 flex items-center gap-3 border-b border-paper-shadow">
            <div class="w-6 h-6 rounded-full bg-surface-container-highest flex items-center justify-center font-label-sm text-secondary font-bold text-xs shrink-0">
              {{ idx + 1 }}
            </div>
            <h4 class="font-button text-sm text-ink-black truncate" :title="section.title">{{ section.title }}</h4>
          </div>
          
          <!-- Lessons List -->
          <ul class="divide-y divide-paper-shadow" v-if="section.lessons && section.lessons.length > 0">
            <li v-for="lesson in section.lessons" :key="lesson.id">
              <a 
                href="javascript:void(0)"
                @click.prevent="goToLesson(lesson.id)"
                class="flex items-start gap-3 px-4 py-3 transition-colors group relative overflow-hidden"
                :class="[
                  currentLessonId === lesson.id 
                    ? 'bg-primary-container/30 border-l-4 border-primary' 
                    : 'border-l-4 border-transparent hover:bg-surface-container-highest'
                ]"
              >
                <!-- Status Icon -->
                <div class="shrink-0 flex items-center justify-center mt-0.5">
                  <span v-if="lesson.isCompleted" class="material-symbols-outlined text-success-green text-[18px]">check_circle</span>
                  <span v-else-if="currentLessonId === lesson.id" class="material-symbols-outlined text-primary text-[18px] animate-pulse">play_circle</span>
                  <span v-else class="material-symbols-outlined text-outline-variant text-[18px] group-hover:text-secondary transition-colors">radio_button_unchecked</span>
                </div>
                
                <!-- Lesson Info -->
                <div class="flex-1 min-w-0">
                  <span class="block font-body-md text-sm transition-colors mb-1 leading-tight"
                        :class="[
                          currentLessonId === lesson.id ? 'text-primary font-bold' : 'text-on-surface-variant group-hover:text-ink-black',
                          lesson.isCompleted ? 'text-secondary line-through opacity-70' : ''
                        ]">
                    {{ lesson.title }}
                  </span>
                  
                  <div class="flex items-center gap-2">
                    <span class="font-label-sm text-secondary text-[11px]">{{ lesson.durationMinutes }} phút</span>
                    <!-- Preview Badge -->
                    <span v-if="lesson.isPreview" class="shrink-0 px-1.5 py-0.5 rounded bg-tertiary-fixed text-on-tertiary-fixed-variant font-label-sm text-[9px] uppercase tracking-wider">
                      Preview
                    </span>
                  </div>
                </div>
              </a>
            </li>
          </ul>
          
          <!-- Empty Section -->
          <div v-else class="px-4 py-3 text-center text-secondary font-body-md text-sm italic">
            Chưa có bài học
          </div>
        </div>
      </div>
      
      <!-- Empty Curriculum -->
      <div v-else class="text-center py-10 text-secondary font-body-md italic border border-dashed border-outline-variant rounded-xl m-2">
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
/* Custom Scrollbar for sidebar */
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 10px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>

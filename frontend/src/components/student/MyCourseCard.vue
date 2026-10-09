<template>
  <div 
    class="bg-stitch-card rounded-[24px] border border-stitch-border overflow-hidden hover:shadow-xl transition-all duration-300 cursor-pointer group flex flex-col"
    @click="$emit('continue', course)"
  >
    <!-- Thumbnail -->
    <div class="h-40 overflow-hidden bg-stitch-muted relative">
      <img 
        v-if="course.thumbnailUrl" 
        :src="course.thumbnailUrl" 
        :alt="course.courseName" 
        loading="lazy"
        decoding="async"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" 
      />
      <div v-else class="w-full h-full flex items-center justify-center bg-gradient-to-br from-stitch-primary/20 to-stitch-accent/20 group-hover:scale-105 transition-transform duration-500">
        <span class="font-stitch-serif text-4xl text-stitch-primary/50 font-bold">日</span>
      </div>
      <!-- Level Badge -->
      <div v-if="course.level" class="absolute top-3 left-3">
        <span class="text-xs bg-black/60 backdrop-blur-md text-white px-3 py-1 rounded-full font-medium shadow-sm">
          {{ course.level }}
        </span>
      </div>
    </div>
    
    <!-- Content -->
    <div class="p-5 flex flex-col flex-1">
      <h3 class="font-stitch-serif font-bold text-lg mb-2 leading-snug text-stitch-foreground line-clamp-2 group-hover:text-stitch-primary transition-colors">
        {{ course.courseName }}
      </h3>
      
      <p class="text-sm text-stitch-muted-foreground mb-4 line-clamp-1">
        <span v-if="course.lastLessonName">Bài tiếp theo: {{ course.lastLessonName }}</span>
        <span v-else>Chưa bắt đầu học</span>
      </p>
      
      <div class="mt-auto">
        <div class="flex items-center justify-between mb-2">
          <span class="text-xs font-medium text-stitch-muted-foreground">{{ Math.round(progressPercent) }}% hoàn thành</span>
          <span class="text-xs font-bold text-stitch-primary">{{ course.completedLessons || 0 }}/{{ course.totalLessons || 0 }} bài</span>
        </div>
        <div class="h-2 bg-stitch-muted rounded-full overflow-hidden mb-5">
          <div 
            class="h-full rounded-full transition-all duration-1000 ease-out" 
            :class="progressPercent === 100 ? 'bg-gradient-to-r from-green-400 to-green-500' : 'bg-gradient-to-r from-stitch-primary to-stitch-accent'"
            :style="{ width: `${progressPercent}%` }" 
          ></div>
        </div>
        
        <button class="w-full py-2.5 bg-stitch-primary/10 text-stitch-primary text-sm font-semibold rounded-xl group-hover:bg-stitch-primary group-hover:text-white transition-all duration-300">
          {{ course.lastLessonName ? 'Tiếp tục học →' : 'Bắt đầu học →' }}
        </button>
      </div>
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
</script>

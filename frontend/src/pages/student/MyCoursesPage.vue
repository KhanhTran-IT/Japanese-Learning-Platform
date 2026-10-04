<template>
  <div class="max-w-7xl mx-auto space-y-8">
    <div class="mb-2">
      <h1 class="font-stitch-serif text-2xl md:text-3xl text-stitch-foreground font-bold mb-2">Khóa học của tôi</h1>
      <p class="text-sm md:text-base text-stitch-muted-foreground">Tiếp tục hành trình chinh phục tiếng Nhật của bạn.</p>
    </div>

    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-32 text-stitch-muted-foreground">
      <span class="material-symbols-outlined animate-spin text-4xl mb-4">autorenew</span>
      <p class="text-sm font-medium">Đang tải danh sách khóa học...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="errorMsg" class="flex flex-col items-center justify-center py-20 text-red-500 text-center bg-red-50 rounded-[24px] border border-red-100">
      <span class="material-symbols-outlined text-5xl mb-4">error</span>
      <p class="font-medium mb-6">{{ errorMsg }}</p>
      <button @click="fetchMyCourses" class="bg-red-500 text-white px-6 py-2.5 rounded-xl font-medium hover:bg-red-600 transition-colors">Thử lại</button>
    </div>

    <!-- Main Content -->
    <template v-else>
      <!-- Empty State -->
      <div v-if="courses.length === 0" class="bg-stitch-card p-12 md:p-20 text-center rounded-[24px] flex flex-col items-center justify-center border-2 border-dashed border-stitch-border">
        <div class="w-20 h-20 bg-stitch-muted rounded-full flex items-center justify-center mb-6">
          <span class="material-symbols-outlined text-4xl text-stitch-muted-foreground">menu_book</span>
        </div>
        <h3 class="font-stitch-serif font-bold text-xl text-stitch-foreground mb-3">Bạn chưa ghi danh khóa học nào</h3>
        <p class="text-sm text-stitch-muted-foreground mb-8 max-w-md">Hãy khám phá các khóa học chất lượng và bắt đầu hành trình học tiếng Nhật theo phong cách tự nhiên nhất!</p>
        <router-link to="/courses" class="bg-stitch-primary hover:bg-stitch-primary/90 text-white px-8 py-3.5 rounded-xl font-semibold transition-all shadow-md hover:shadow-lg active:scale-95">
          Khám phá khóa học
        </router-link>
      </div>

      <!-- Courses Grid -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <MyCourseCard
          v-for="course in courses"
          :key="course.courseId"
          :course="course"
          @continue="handleContinue"
        />
        
        <!-- Placeholder Card to add more -->
        <router-link 
          to="/courses"
          class="bg-stitch-card rounded-[24px] border-2 border-dashed border-stitch-border flex flex-col items-center justify-center p-8 cursor-pointer hover:border-stitch-primary/50 hover:bg-stitch-primary/5 transition-all group min-h-[300px]"
        >
          <div class="w-14 h-14 bg-stitch-muted rounded-full flex items-center justify-center text-stitch-muted-foreground group-hover:bg-stitch-primary/10 group-hover:text-stitch-primary transition-colors mb-4">
            <span class="material-symbols-outlined text-2xl">add</span>
          </div>
          <div class="font-semibold text-stitch-foreground group-hover:text-stitch-primary transition-colors">Thêm khóa học mới</div>
        </router-link>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { StudentService } from '@/services/student.service'
import MyCourseCard from '@/components/student/MyCourseCard.vue'

const router = useRouter()

const isLoading = ref(true)
const errorMsg = ref('')
const courses = ref([])

const fetchMyCourses = async () => {
  isLoading.value = true
  errorMsg.value = ''

  try {
    const res = await StudentService.getMyCourses()
    if (res.data && res.data.code === 1000) {
      courses.value = res.data.result || []
    }
  } catch (error) {
    errorMsg.value = 'Không thể tải danh sách khóa học. Vui lòng kiểm tra lại kết nối mạng.'
    console.error('My courses fetch error:', error)
  } finally {
    isLoading.value = false
  }
}

const handleContinue = (course) => {
  if (course.lastLessonId) {
    router.push(`/student/lessons/${course.lastLessonId}`)
  } else {
    router.push(course.slug ? `/courses/${course.slug}` : '/courses')
  }
}

onMounted(() => {
  fetchMyCourses()
})
</script>

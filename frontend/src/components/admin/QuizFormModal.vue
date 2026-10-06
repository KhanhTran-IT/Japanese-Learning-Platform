<template>
  <div class="fixed inset-0 bg-[#0f1117]/80 backdrop-blur-sm flex items-center justify-center z-50 p-4" @click.self="$emit('close')">
    <div class="bg-[#161b27] rounded-2xl w-full max-w-lg max-h-[90vh] flex flex-col shadow-2xl border border-white/10">
      <div class="flex justify-between items-center p-6 border-b border-white/10 bg-white/[0.02]">
        <h2 class="text-xl font-stitch-serif font-bold text-white">{{ editingQuiz ? 'Cập nhật Bài Tập' : 'Tạo Bài Tập Mới' }}</h2>
        <button class="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-white/50 hover:text-white transition-colors" @click="$emit('close')">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>

      <div v-if="errorMsg" class="bg-red-500/10 text-red-400 p-4 flex justify-between items-center border-b border-red-500/20 text-sm font-medium">
        <div class="flex items-center gap-2"><span>⚠️</span> {{ errorMsg }}</div>
        <button @click="errorMsg = ''" class="text-red-400 hover:text-red-300">✕</button>
      </div>

      <form @submit.prevent="handleSubmit" class="p-6 overflow-y-auto flex-1 flex flex-col gap-5">
        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Tiêu đề <span class="text-red-400">*</span></label>
          <input v-model="formData.title" type="text" required placeholder="Nhập tiêu đề bài tập..." class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Mô tả</label>
          <textarea v-model="formData.description" rows="3" placeholder="Mô tả bài tập..." class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors resize-none"></textarea>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Thời gian làm bài (Phút) <span class="text-red-400">*</span></label>
            <input v-model.number="formData.timeLimitMinutes" type="number" min="0" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
          </div>

          <div>
            <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Điểm qua môn <span class="text-red-400">*</span></label>
            <input v-model.number="formData.passingScore" type="number" min="0" max="100" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
          </div>
        </div>

        <div>
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider mb-2">Số lần làm tối đa <span class="text-red-400">*</span></label>
          <input v-model.number="formData.maxAttempts" type="number" min="1" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors" />
        </div>

        <div v-if="!editingQuiz" class="p-4 rounded-xl border border-white/10 bg-white/[0.02] flex flex-col gap-3">
          <label class="block text-xs font-medium text-white/60 uppercase tracking-wider">Liên kết (Course ID hoặc Lesson ID) <span class="text-red-400">*</span></label>
          <div class="flex gap-4">
            <label class="flex items-center gap-2 cursor-pointer text-sm text-white/80 hover:text-white">
              <input type="radio" v-model="linkType" value="course" class="text-stitch-primary focus:ring-stitch-primary bg-white/5 border-white/20" />
              Course ID
            </label>
            <label class="flex items-center gap-2 cursor-pointer text-sm text-white/80 hover:text-white">
              <input type="radio" v-model="linkType" value="lesson" class="text-stitch-primary focus:ring-stitch-primary bg-white/5 border-white/20" />
              Lesson ID
            </label>
          </div>
          <input v-if="linkType === 'course'" v-model.number="formData.courseId" type="number" min="1" placeholder="Nhập Course ID" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors mt-1" />
          <input v-if="linkType === 'lesson'" v-model.number="formData.lessonId" type="number" min="1" placeholder="Nhập Lesson ID" required class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-2.5 text-sm text-white placeholder-white/20 focus:outline-none focus:border-stitch-primary focus:bg-white/10 transition-colors mt-1" />
        </div>

        <div class="pt-4 border-t border-white/10 flex justify-end gap-3 mt-2">
          <button type="button" class="px-5 py-2.5 rounded-xl border border-white/10 bg-transparent text-white/80 hover:bg-white/5 transition-colors font-medium text-sm" @click="$emit('close')" :disabled="isSubmitting">Hủy</button>
          <button type="submit" class="px-6 py-2.5 rounded-xl bg-stitch-primary text-white font-medium text-sm hover:bg-stitch-primary/90 transition-colors shadow-lg disabled:opacity-50" :disabled="isSubmitting">
            {{ isSubmitting ? 'Đang lưu...' : 'Lưu bài tập' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { AdminService } from '@/services/admin.service'
import { getApiErrorMessage } from '@/utils/api-error'

const props = defineProps({ editingQuiz: { type: Object, default: null } })
const emit = defineEmits(['close', 'saved'])

const linkType = ref('course')
const isSubmitting = ref(false)
const errorMsg = ref('')

const formData = reactive({
  title: '', description: '', timeLimitMinutes: 0, passingScore: 80, maxAttempts: 1, courseId: null, lessonId: null
})

onMounted(() => {
  if (props.editingQuiz) {
    Object.assign(formData, {
      title: props.editingQuiz.title,
      description: props.editingQuiz.description,
      timeLimitMinutes: props.editingQuiz.timeLimitMinutes || 0,
      passingScore: props.editingQuiz.passingScore,
      maxAttempts: props.editingQuiz.maxAttempts
    })
  }
})

const handleSubmit = async () => {
  errorMsg.value = ''
  isSubmitting.value = true
  
  try {
    const payload = {
      title: formData.title, description: formData.description,
      timeLimitMinutes: formData.timeLimitMinutes || null,
      passingScore: formData.passingScore, maxAttempts: formData.maxAttempts
    }
    
    if (!props.editingQuiz) {
      if (linkType.value === 'course') payload.courseId = formData.courseId
      else payload.lessonId = formData.lessonId
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

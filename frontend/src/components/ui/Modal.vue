<script setup>
import { onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  description: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['close']);

const close = () => {
  emit('close');
};

const handleEscape = (e) => {
  if (e.key === 'Escape' && props.isOpen) {
    close();
  }
};

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
});

onMounted(() => {
  document.addEventListener('keydown', handleEscape);
});

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscape);
  document.body.style.overflow = '';
});
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="isOpen" class="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm" @click="close"></div>
    </Transition>

    <Transition
      enter-active-class="transition ease-out duration-200"
      enter-from-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
      enter-to-class="opacity-100 translate-y-0 sm:scale-100"
      leave-active-class="transition ease-in duration-150"
      leave-from-class="opacity-100 translate-y-0 sm:scale-100"
      leave-to-class="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
    >
      <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none">
        <div class="w-full max-w-lg rounded-stitch border border-stitch-border bg-stitch-card p-6 shadow-lg pointer-events-auto font-stitch-sans text-stitch-foreground">
          <div class="flex flex-col space-y-1.5 text-center sm:text-left mb-4">
            <h2 v-if="title" class="text-lg font-semibold leading-none tracking-tight font-stitch-serif">{{ title }}</h2>
            <p v-if="description" class="text-sm text-stitch-muted-foreground">{{ description }}</p>
          </div>
          
          <div class="relative">
            <slot />
          </div>
          
          <div v-if="$slots.footer" class="flex flex-col-reverse sm:flex-row sm:justify-end sm:space-x-2 mt-6">
            <slot name="footer" />
          </div>
          
          <button @click="close" class="absolute right-4 top-4 rounded-sm opacity-70 ring-offset-stitch-background transition-opacity hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-stitch-ring focus:ring-offset-2">
            <span class="sr-only">Close</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-4 w-4"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

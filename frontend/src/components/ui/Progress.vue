<script setup>
import { computed } from 'vue';

const props = defineProps({
  value: {
    type: Number,
    default: 0
  },
  max: {
    type: Number,
    default: 100
  }
});

const percentage = computed(() => {
  return Math.min(Math.max((props.value / props.max) * 100, 0), 100);
});
</script>

<template>
  <div class="relative h-2 w-full overflow-hidden rounded-full bg-stitch-muted">
    <div
      class="h-full w-full flex-1 bg-stitch-primary transition-all duration-500 ease-in-out"
      :style="{ transform: `translateX(-${100 - percentage}%)` }"
    ></div>
  </div>
</template>

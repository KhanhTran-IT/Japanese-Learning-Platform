<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'error', 'success', 'warning'].includes(v)
  },
  title: {
    type: String,
    default: ''
  }
});

const classes = computed(() => {
  const base = "relative w-full rounded-stitch border p-4 [&>svg]:absolute [&>svg]:left-4 [&>svg]:top-4 [&>svg]:text-foreground [&>svg~*]:pl-7 font-stitch-sans";
  
  const variants = {
    default: "bg-stitch-background text-stitch-foreground border-stitch-border",
    error: "border-red-500/50 text-red-600 dark:border-red-500 [&>svg]:text-red-600 bg-red-50",
    success: "border-green-500/50 text-green-600 dark:border-green-500 [&>svg]:text-green-600 bg-green-50",
    warning: "border-yellow-500/50 text-yellow-600 dark:border-yellow-500 [&>svg]:text-yellow-600 bg-yellow-50"
  };

  return `${base} ${variants[props.variant]}`;
});
</script>

<template>
  <div :class="classes" role="alert">
    <div v-if="$slots.icon" class="absolute left-4 top-4">
      <slot name="icon" />
    </div>
    <h5 v-if="title" class="mb-1 font-medium leading-none tracking-tight">
      {{ title }}
    </h5>
    <div class="text-sm [&_p]:leading-relaxed">
      <slot />
    </div>
  </div>
</template>

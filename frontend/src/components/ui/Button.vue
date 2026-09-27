<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'secondary', 'outline', 'ghost', 'link', 'danger'].includes(v)
  },
  size: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'sm', 'lg', 'icon'].includes(v)
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['click']);

const classes = computed(() => {
  const base = "inline-flex items-center justify-center rounded-stitch text-sm font-stitch-sans font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring disabled:pointer-events-none disabled:opacity-50";
  
  const variants = {
    default: "bg-stitch-primary text-stitch-primary-foreground hover:bg-stitch-primary/90 shadow-sm",
    secondary: "bg-stitch-secondary text-stitch-secondary-foreground hover:bg-stitch-secondary/80",
    outline: "border border-stitch-border bg-stitch-background hover:bg-stitch-muted hover:text-stitch-foreground",
    ghost: "hover:bg-stitch-muted hover:text-stitch-foreground",
    link: "text-stitch-primary underline-offset-4 hover:underline",
    danger: "bg-red-500 text-white hover:bg-red-600 shadow-sm"
  };

  const sizes = {
    default: "h-10 px-4 py-2",
    sm: "h-9 px-3 text-xs",
    lg: "h-11 px-8 text-base",
    icon: "h-10 w-10"
  };

  return `${base} ${variants[props.variant]} ${sizes[props.size]}`;
});
</script>

<template>
  <button :class="classes" :disabled="disabled" @click="emit('click', $event)">
    <slot />
  </button>
</template>

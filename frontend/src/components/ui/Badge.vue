<script setup>
import { computed } from 'vue';

const props = defineProps({
  variant: {
    type: String,
    default: 'default',
    validator: (v) => ['default', 'secondary', 'outline', 'destructive'].includes(v)
  }
});

const classes = computed(() => {
  const base = "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-stitch-ring focus:ring-offset-2 font-stitch-sans";
  
  const variants = {
    default: "border-transparent bg-stitch-primary text-stitch-primary-foreground shadow hover:bg-stitch-primary/80",
    secondary: "border-transparent bg-stitch-secondary text-stitch-secondary-foreground hover:bg-stitch-secondary/80",
    outline: "text-stitch-foreground border-stitch-border",
    destructive: "border-transparent bg-red-500 text-white shadow hover:bg-red-500/80"
  };

  return `${base} ${variants[props.variant]}`;
});
</script>

<template>
  <div :class="classes">
    <slot />
  </div>
</template>

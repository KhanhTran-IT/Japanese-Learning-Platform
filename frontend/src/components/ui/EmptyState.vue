<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    default: ''
  }
});
</script>

<template>
  <div class="flex flex-col items-center justify-center p-8 text-center animate-in fade-in-50 font-stitch-sans">
    <div class="mb-4 rounded-full bg-stitch-secondary p-4 text-stitch-primary">
      <slot name="icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="h-8 w-8 opacity-80"><circle cx="12" cy="12" r="10"/><path d="M16 16s-1.5-2-4-2-4 2-4 2"/><line x1="9" x2="9.01" y1="9" y2="9"/><line x1="15" x2="15.01" y1="9" y2="9"/></svg>
      </slot>
    </div>
    
    <h3 class="mt-2 text-xl font-semibold font-stitch-serif text-stitch-foreground">
      {{ title }}
    </h3>
    
    <p v-if="description" class="mt-2 text-sm text-stitch-muted-foreground max-w-sm">
      {{ description }}
    </p>
    
    <div v-if="$slots.action" class="mt-6">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  className: {
    type: String,
    default: ''
  }
});
</script>

<template>
  <div :class="['animate-pulse rounded-stitch bg-stitch-muted', className]"></div>
</template>

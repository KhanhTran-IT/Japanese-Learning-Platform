<script setup>
import { computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  label: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue']);

const updateValue = (event) => {
  emit('update:modelValue', event.target.value);
};

const inputClasses = computed(() => {
  const base = "flex h-10 w-full rounded-stitch border bg-stitch-background px-3 py-2 text-sm ring-offset-stitch-background font-stitch-sans placeholder:text-stitch-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-stitch-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 transition-colors";
  const borderClass = props.error ? "border-red-500 focus-visible:ring-red-500" : "border-stitch-border";
  return `${base} ${borderClass}`;
});
</script>

<template>
  <div class="flex flex-col gap-1.5 w-full font-stitch-sans">
    <label v-if="label" class="text-sm font-medium leading-none text-stitch-foreground" :class="{'text-red-500': error}">
      {{ label }}
    </label>
    <input 
      :type="type"
      :value="modelValue"
      @input="updateValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :class="inputClasses"
    />
    <p v-if="error" class="text-xs text-red-500 font-medium">{{ error }}</p>
  </div>
</template>

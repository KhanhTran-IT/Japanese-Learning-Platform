<template>
  <div class="progress-card" :style="{ borderLeftColor: color }">
    <div class="card-icon" :style="{ backgroundColor: color + '18', color: color }">
      <span>{{ icon }}</span>
    </div>
    <div class="card-body">
      <p class="card-label">{{ label }}</p>
      <p class="card-value">{{ displayValue }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [Number, String], default: 0 },
  icon: { type: String, default: '📊' },
  color: { type: String, default: '#3B82F6' },
  isPercent: { type: Boolean, default: false }
})

const displayValue = computed(() => {
  if (props.isPercent) {
    return `${Math.round(props.value)}%`
  }
  return props.value
})
</script>

<style scoped>
.progress-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: var(--card-bg, #fff);
  padding: 1.25rem 1.5rem;
  border-radius: 12px;
  border-left: 4px solid;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08), 0 1px 2px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.progress-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  flex-shrink: 0;
}
.card-body {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}
.card-label {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.02em;
}
.card-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-color, #1F2937);
  line-height: 1;
}
</style>

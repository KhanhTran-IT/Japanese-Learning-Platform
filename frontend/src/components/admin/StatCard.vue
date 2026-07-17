<template>
  <div class="stat-card">
    <div class="card-icon" :style="{ backgroundColor: color + '20', color: color }">
      <span class="icon">{{ icon }}</span>
    </div>
    <div class="card-content">
      <p class="title">{{ title }}</p>
      <p class="value">{{ displayValue }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  value: { type: Number, default: 0 },
  icon: { type: String, default: '📌' },
  color: { type: String, default: '#3B82F6' } // Màu mặc định: xanh blue
})

const displayValue = computed(() => {
  return new Intl.NumberFormat('vi-VN').format(props.value)
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  background-color: #ffffff;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}
.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  flex-shrink: 0;
}
.card-content {
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 0.25rem;
  font-weight: 500;
}
.value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}
</style>

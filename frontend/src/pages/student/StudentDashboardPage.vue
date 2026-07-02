<template>
  <div class="dashboard">
    <h1>Bảng điều khiển học tập</h1>
    <p>Chào mừng bạn quay lại. Hệ thống đang tải dữ liệu học tập của bạn...</p>
    
    <div class="stats-grid">
      <div class="stat-card">
        <h3>Khóa học đang học</h3>
        <p class="text-3xl">0</p>
      </div>
      <div class="stat-card">
        <h3>Bài học đã hoàn thành</h3>
        <p class="text-3xl">0</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard h1 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}
.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.stat-card h3 {
  font-size: 1rem;
  color: #6b7280;
  margin-bottom: 0.5rem;
}
.text-3xl {
  font-size: 1.875rem;
  font-weight: bold;
  color: var(--primary-color);
}
</style>

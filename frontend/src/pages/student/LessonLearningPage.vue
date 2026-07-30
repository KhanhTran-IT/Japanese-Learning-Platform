<template>
  <div class="lesson-learning-placeholder">
    <h1>Khu vực học tập</h1>
    <p>Bài học: {{ $route.params.slug }}</p>
    <p>Trang này đang được xây dựng...</p>
    <router-link to="/student/dashboard">Quay lại bảng điều khiển</router-link>
  </div>
</template>

<style scoped>
.lesson-learning-placeholder {
  max-width: 800px;
  margin: 4rem auto;
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
</style>

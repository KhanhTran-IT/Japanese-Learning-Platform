<template>
  <div class="course-detail-placeholder">
    <h1>Chi tiết khóa học</h1>
    <p>Slug: {{ $route.params.slug }}</p>
    <p>Trang này đang được xây dựng...</p>
    <router-link to="/">Quay lại trang chủ</router-link>
  </div>
</template>

<style scoped>
.course-detail-placeholder {
  max-width: 800px;
  margin: 4rem auto;
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
</style>

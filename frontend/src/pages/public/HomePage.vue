<template>
  <div class="home">
    <h1>Chào mừng đến với BrianJP</h1>
    <p>Nền tảng học tiếng Nhật trực tuyến hàng đầu.</p>
    <div class="actions">
      <router-link to="/courses" class="btn btn-primary">Xem khóa học</router-link>
      <router-link to="/login" class="btn btn-secondary">Đăng nhập</router-link>
    </div>
  </div>
</template>

<style scoped>
.home {
  text-align: center;
  padding: 4rem 0;
}
.home h1 {
  font-size: 2.5rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
}
.actions {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin-top: 2rem;
}
.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
}
.btn-primary {
  background-color: var(--primary-color);
  color: white;
}
.btn-secondary {
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
}
</style>

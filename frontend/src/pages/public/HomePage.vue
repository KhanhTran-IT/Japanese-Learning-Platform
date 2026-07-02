<template>
  <div class="home">
    <h1>Chào mừng đến với BrianJP</h1>
    <p>Nền tảng học tiếng Nhật trực tuyến hàng đầu.</p>
    <router-link to="/login" class="btn">Bắt đầu học ngay</router-link>
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
.btn {
  display: inline-block;
  margin-top: 2rem;
  padding: 0.75rem 1.5rem;
  background-color: var(--primary-color);
  color: white;
  border-radius: 4px;
  font-weight: 500;
}
</style>

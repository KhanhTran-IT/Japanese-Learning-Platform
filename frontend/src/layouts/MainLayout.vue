<template>
  <div class="main-layout">
    <header class="header">
      <div class="logo">BrianJP</div>
      <nav>
        <router-link to="/">Trang chủ</router-link>
        <router-link to="/courses">Khóa học</router-link>
        <router-link to="/login">Đăng nhập</router-link>
      </nav>
    </header>
    <main class="content">
      <router-view></router-view>
    </main>
    <footer class="footer">
      <p>&copy; 2026 BrianJP Learning Platform</p>
    </footer>
  </div>
</template>

<script setup>
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  padding: 1rem 2rem;
  background-color: var(--card-bg);
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.header nav {
  display: flex;
  gap: 1rem;
}
.content {
  flex: 1;
  padding: 2rem;
}
.footer {
  text-align: center;
  padding: 1rem;
  background-color: var(--card-bg);
}
</style>
